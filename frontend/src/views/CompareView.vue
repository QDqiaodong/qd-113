<template>
  <div class="compare-page">
    <h2 class="section-title">数据对比视图</h2>

    <el-card class="select-card">
      <div class="select-row">
        <span class="select-label">选择茶叶进行对比（最多4款）：</span>
        <el-select v-model="selectedTeaIds" multiple :max-collapse-tags="4" collapse-tags collapse-tags-tooltip placeholder="搜索并选择茶叶" filterable style="width:500px;max-width:100%">
          <el-option v-for="t in allTeas" :key="t.id" :label="`${t.name} (${t.teaCategory})`" :value="t.id" />
        </el-select>
        <el-button type="primary" @click="loadCompareData" :loading="loading" :disabled="selectedTeaIds.length < 2">开始对比</el-button>
      </div>
    </el-card>

    <template v-if="compareData.length >= 2">
      <el-card class="compare-card">
        <h3 class="section-title">基础信息对比</h3>
        <el-table :data="compareTableData" border stripe>
          <el-table-column prop="field" label="对比项" width="120" fixed />
          <el-table-column v-for="tea in compareData" :key="tea.id" :label="tea.name" min-width="150">
            <template #default="{ row }">
              {{ row.values[tea.id] ?? '-' }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-row :gutter="16">
        <el-col :xs="24" :lg="12">
          <el-card class="compare-card">
            <h3 class="section-title">冲泡参数对比</h3>
            <div class="compare-chart">
              <div class="chart-row" v-for="metric in brewingMetrics" :key="metric.key">
                <div class="chart-label">{{ metric.label }}</div>
                <div class="chart-bars">
                  <div v-for="tea in compareData" :key="tea.id" class="chart-bar-item">
                    <div class="chart-bar"
                      :style="{ width: getBarWidth(tea, metric.key, metric.max) + '%', background: getColor(tea.id) }">
                    </div>
                    <span class="chart-value">{{ getDefaultBrewingValue(tea, metric.key) }}{{ metric.unit }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div class="chart-legend">
              <span v-for="tea in compareData" :key="tea.id" class="legend-item">
                <span class="legend-dot" :style="{ background: getColor(tea.id) }"></span>
                {{ tea.name }}
              </span>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="12">
          <el-card class="compare-card">
            <h3 class="section-title">品饮评分对比</h3>
            <div v-if="hasTastingData" class="radar-container">
              <div class="radar-chart">
                <div class="radar-row" v-for="metric in tastingMetrics" :key="metric.key">
                  <div class="radar-label">{{ metric.label }}</div>
                  <div class="radar-bars">
                    <div v-for="tea in compareData" :key="tea.id" class="radar-bar-item">
                      <div class="radar-bar"
                        :style="{ width: getTastingBarWidth(tea, metric.key) + '%', background: getColor(tea.id) }">
                      </div>
                      <span class="radar-value">{{ getLatestTastingValue(tea, metric.key) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无品饮评分数据" />
            <div class="chart-legend" v-if="hasTastingData">
              <span v-for="tea in compareData" :key="tea.id" class="legend-item">
                <span class="legend-dot" :style="{ background: getColor(tea.id) }"></span>
                {{ tea.name }}
              </span>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="compare-card">
        <h3 class="section-title">出汤时长对比</h3>
        <div class="infusion-compare">
          <div v-for="tea in compareData" :key="tea.id" class="infusion-tea-row">
            <div class="infusion-tea-name" :style="{ color: getColor(tea.id) }">{{ tea.name }}</div>
            <div class="infusion-timeline">
              <div v-for="(val, idx) in getInfusionTimeline(tea)" :key="idx" class="infusion-step">
                <div class="infusion-bubble" :style="{ background: getColor(tea.id), width: Math.max(30, val / 1.5) + 'px', height: Math.max(30, val / 1.5) + 'px' }">
                  {{ val }}s
                </div>
                <span class="infusion-step-label">第{{ idx + 1 }}泡</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </template>

    <el-empty v-else-if="!loading" description="请选择至少2款茶叶进行对比" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getTeaList, getTeaById, getBrewingParams, getTastingNotes } from '../api/tea'

const allTeas = ref([])
const selectedTeaIds = ref([])
const loading = ref(false)
const compareData = ref([])
const brewingDataMap = ref({})
const tastingDataMap = ref({})

const COLORS = ['#2d6a4f', '#e76f51', '#264653', '#e9c46a']

const brewingMetrics = [
  { key: 'waterTemperature', label: '水温', unit: '℃', max: 100 },
  { key: 'teaAmount', label: '投茶量', unit: '克', max: 20 },
  { key: 'firstInfusionTime', label: '第一泡', unit: '秒', max: 60 },
  { key: 'secondInfusionTime', label: '第二泡', unit: '秒', max: 60 },
  { key: 'thirdInfusionTime', label: '第三泡', unit: '秒', max: 60 }
]

const tastingMetrics = [
  { key: 'aromaScore', label: '香气' },
  { key: 'liquorColorScore', label: '汤色' },
  { key: 'tasteScore', label: '口感' },
  { key: 'aftertasteScore', label: '回甘' },
  { key: 'overallScore', label: '综合' }
]

const hasTastingData = computed(() => {
  return compareData.value.some(t => {
    const notes = tastingDataMap.value[t.id]
    return notes && notes.length > 0
  })
})

const compareTableData = computed(() => {
  const fields = [
    { field: '茶类', key: 'teaCategory' },
    { field: '产区', key: 'originRegion' },
    { field: '采摘年份', key: 'harvestYear' },
    { field: '储存方式', key: 'storageMethod' },
    { field: '现有存量', key: 'currentStock', suffix: 'stockUnit' }
  ]
  return fields.map(f => {
    const row = { field: f.field, values: {} }
    compareData.value.forEach(t => {
      let val = t[f.key] ?? '-'
      if (f.suffix && val !== '-') val = `${val}${t[f.suffix] || ''}`
      row.values[t.id] = val
    })
    return row
  })
})

function getColor(id) {
  const idx = compareData.value.findIndex(t => t.id === id)
  return COLORS[idx % COLORS.length]
}

function getDefaultBrewingValue(tea, key) {
  const params = brewingDataMap.value[tea.id]
  if (!params || params.length === 0) return '-'
  const def = params.find(p => p.isDefault) || params[0]
  return def[key] ?? '-'
}

function getBarWidth(tea, key, max) {
  const val = getDefaultBrewingValue(tea, key)
  if (val === '-') return 0
  return Math.min(100, (val / max) * 100)
}

function getLatestTastingValue(tea, key) {
  const notes = tastingDataMap.value[tea.id]
  if (!notes || notes.length === 0) return '-'
  if (key === 'overallScore') return notes[0].overallScore ?? '-'
  return notes[0][key] ?? '-'
}

function getTastingBarWidth(tea, key) {
  const val = getLatestTastingValue(tea, key)
  if (val === '-') return 0
  if (key === 'overallScore') return Math.min(100, val)
  return Math.min(100, (val / 5) * 100)
}

function getInfusionTimeline(tea) {
  const params = brewingDataMap.value[tea.id]
  if (!params || params.length === 0) return []
  const def = params.find(p => p.isDefault) || params[0]
  return [def.firstInfusionTime, def.secondInfusionTime, def.thirdInfusionTime, def.subsequentInfusionTime].filter(v => v != null)
}

async function loadCompareData() {
  if (selectedTeaIds.value.length < 2) return
  loading.value = true
  try {
    const results = await Promise.all(
      selectedTeaIds.value.map(async id => {
        const [teaRes, brewingRes, tastingRes] = await Promise.all([
          getTeaById(id),
          getBrewingParams(id),
          getTastingNotes(id)
        ])
        brewingDataMap.value[id] = brewingRes.data || []
        tastingDataMap.value[id] = tastingRes.data || []
        return teaRes.data
      })
    )
    compareData.value = results
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const res = await getTeaList()
  allTeas.value = res.data || []
})
</script>

<style scoped>
.select-card {
  margin-bottom: 20px;
}

.select-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.select-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.compare-card {
  margin-bottom: 16px;
}

.compare-chart {
  padding: 8px 0;
}

.chart-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.chart-label {
  width: 80px;
  flex-shrink: 0;
  font-size: 13px;
  color: #6c757d;
}

.chart-bars {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chart-bar-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-bar {
  height: 16px;
  border-radius: 8px;
  transition: width 0.5s;
  min-width: 4px;
}

.chart-value {
  font-size: 12px;
  color: #495057;
  min-width: 50px;
}

.chart-legend {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #eee;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #495057;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.radar-chart {
  padding: 8px 0;
}

.radar-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.radar-label {
  width: 50px;
  flex-shrink: 0;
  font-size: 13px;
  color: #6c757d;
}

.radar-bars {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.radar-bar-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.radar-bar {
  height: 14px;
  border-radius: 7px;
  transition: width 0.5s;
  min-width: 4px;
}

.radar-value {
  font-size: 12px;
  color: #495057;
  min-width: 30px;
}

.infusion-compare {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.infusion-tea-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.infusion-tea-name {
  width: 100px;
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 600;
  padding-top: 20px;
}

.infusion-timeline {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  flex: 1;
}

.infusion-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.infusion-bubble {
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
}

.infusion-step-label {
  font-size: 11px;
  color: #6c757d;
}
</style>
