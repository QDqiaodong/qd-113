<template>
  <div class="compare-page">
    <h2 class="section-title">数据对比视图</h2>

    <el-card class="select-card">
      <div class="select-row">
        <span class="select-label">选择茶叶进行对比（最多4款）：</span>
        <el-select v-model="selectedTeaIds" multiple :multiple-limit="4" :max-collapse-tags="4" collapse-tags collapse-tags-tooltip placeholder="搜索并选择茶叶（最多4款）" filterable style="width:500px;max-width:100%">
          <el-option v-for="t in allTeas" :key="t.id" :label="`${t.name} (${t.teaCategory})`" :value="t.id" />
        </el-select>
        <el-button type="primary" @click="loadCompareData" :loading="loading" :disabled="selectedTeaIds.length < 2">开始对比</el-button>
      </div>
    </el-card>

    <template v-if="compareData.length >= 2">
      <el-card class="compare-card">
        <h3 class="section-title">核心维度对比</h3>
        <el-row :gutter="16" class="dim-grid">
          <el-col v-for="dim in dimensionCards" :key="dim.key" :xs="24" :sm="12" :md="8" :lg="6">
            <div class="dim-card" :style="{ background: dim.gradient }">
              <div class="dim-card-header">
                <span class="dim-icon">{{ dim.icon }}</span>
                <span class="dim-label">{{ dim.label }}</span>
              </div>
              <div class="dim-tea-list">
                <div v-for="tea in compareData" :key="tea.id" class="dim-tea-item">
                  <span class="dim-tea-dot" :style="{ background: getColor(tea.id) }"></span>
                  <span class="dim-tea-name">{{ tea.name }}</span>
                  <span class="dim-tea-value" :style="{ color: getColor(tea.id) }">{{ dim.values[tea.id] }}</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>

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
            <div class="card-header">
              <h3 class="section-title">冲泡参数对比</h3>
              <el-button type="primary" size="small" :disabled="compareData.length < 2" @click="openSyncDialog('source', compareData[0]?.id)">
                同步参数
              </el-button>
            </div>
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

    <el-dialog v-model="syncDialogVisible" title="同步冲泡参数" width="600px" :close-on-click-modal="false">
      <el-form label-width="100px" class="sync-form">
        <el-form-item label="来源茶叶">
          <el-select v-model="sourceTeaId" placeholder="选择来源茶叶" style="width: 100%" @change="onSourceTeaChange">
            <el-option v-for="tea in compareData" :key="tea.id" :label="tea.name + ' (' + tea.teaCategory + ')'" :value="tea.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源参数">
          <el-select v-model="sourceParamId" placeholder="选择要同步的参数" style="width: 100%" :disabled="!sourceTeaId" @change="loadSyncPreview">
            <el-option v-for="p in sourceParams" :key="p.id" :label="(p.paramName || '未命名') + (p.isDefault ? ' [默认]' : '')" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标茶叶">
          <el-select v-model="targetTeaId" placeholder="选择目标茶叶（同茶类）" style="width: 100%" @change="loadSyncPreview">
            <el-option 
              v-for="tea in sameCategoryTeas.length ? sameCategoryTeas : compareData.filter(t => t.id !== sourceTeaId)" 
              :key="tea.id" 
              :label="tea.name + ' (' + tea.teaCategory + ')'" 
              :value="tea.id"
              :disabled="sourceTeaId && tea.teaCategory !== sourceTea?.teaCategory"
            />
          </el-select>
          <div v-if="sourceTeaId && targetTeaId && sourceTea?.teaCategory !== targetTea?.teaCategory" class="form-tip error">
            只能同步同茶类的冲泡参数
          </div>
          <div v-else-if="sourceTeaId && sameCategoryTeas.length === 0" class="form-tip">
            当前对比中没有同茶类的其他茶叶
          </div>
        </el-form-item>
        <el-form-item label="参数名称">
          <el-input v-model="targetParamName" placeholder="留空则使用来源名称 + (同步)" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="setAsDefault" />
        </el-form-item>
      </el-form>

      <div v-if="syncPreviewLoading" class="preview-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载差异预览...</span>
      </div>

      <div v-else-if="syncPreview.length > 0" class="diff-preview">
        <div class="diff-title">字段差异预览（与目标默认参数对比）</div>
        <el-table :data="syncPreview" size="small" border stripe>
          <el-table-column prop="fieldLabel" label="字段" width="120" />
          <el-table-column label="来源值" width="180">
            <template #default="{ row }">
              <span class="diff-source">{{ formatValue(row.sourceValue) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="目标原值" width="180">
            <template #default="{ row }">
              <span class="diff-target">{{ formatValue(row.targetValue) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-tag type="success" size="small">将覆盖</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-else-if="sourceParamId && targetTeaId && !syncPreviewLoading" class="diff-empty">
        <el-empty description="无差异，参数完全相同" :image-size="80" />
      </div>

      <template #footer>
        <el-button @click="syncDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="syncLoading" :disabled="!canSync" @click="confirmSync">
          确认同步
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getTeaList, getTeaById, getBrewingParams, getTastingNotes, previewBrewingParamSync, syncBrewingParam } from '../api/tea'

const allTeas = ref([])
const selectedTeaIds = ref([])
const loading = ref(false)
const compareData = ref([])
const brewingDataMap = ref({})
const tastingDataMap = ref({})

const syncDialogVisible = ref(false)
const syncLoading = ref(false)
const syncPreviewLoading = ref(false)
const sourceTeaId = ref(null)
const targetTeaId = ref(null)
const sourceParamId = ref(null)
const targetParamName = ref('')
const setAsDefault = ref(true)
const syncPreview = ref([])
const syncType = ref('')

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

const sourceTea = computed(() => compareData.value.find(t => t.id === sourceTea.value))
const targetTea = computed(() => compareData.value.find(t => t.id === targetTea.value))

const sameCategoryTeas = computed(() => {
  if (!sourceTea.value) return []
  return compareData.value.filter(t => t.id !== sourceTea.value.id && t.teaCategory === sourceTea.value.teaCategory)
})

const sourceParams = computed(() => {
  const params = brewingDataMap.value[sourceTeaId.value] || []
  return params
})

const defaultSourceParam = computed(() => {
  const params = sourceParams.value
  if (!params || params.length === 0) return null
  const def = params.find(p => p.isDefault)
  return def || params[0]
})

const canSync = computed(() => {
  return sourceParamId.value 
    && targetTeaId.value 
    && sourceTea.value 
    && targetTea.value 
    && sourceTea.value.teaCategory === targetTea.value.teaCategory
})

function onSourceTeaChange() {
  sourceParamId.value = null
  syncPreview.value = []
  if (defaultSourceParam.value) {
    sourceParamId.value = defaultSourceParam.value.id
    loadSyncPreview()
  }
}

watch(sourceTeaId, () => {
  onSourceTeaChange()
})

watch([sourceParamId, targetTeaId], () => {
  loadSyncPreview()
})

function formatValue(val) {
  if (val === null || val === undefined || val === '') return '-'
  return String(val)
}

const dimensionCards = computed(() => {
  return [
    {
      key: 'teaCategory',
      label: '茶类',
      icon: '🍵',
      gradient: 'linear-gradient(135deg, #e0f2fe, #bae6fd)',
      values: Object.fromEntries(
        compareData.value.map(t => [t.id, t.teaCategory || '-'])
      )
    },
    {
      key: 'originRegion',
      label: '产区',
      icon: '📍',
      gradient: 'linear-gradient(135deg, #fef3c7, #fde68a)',
      values: Object.fromEntries(
        compareData.value.map(t => [t.id, t.originRegion || '-'])
      )
    },
    {
      key: 'harvestYear',
      label: '年份',
      icon: '📅',
      gradient: 'linear-gradient(135deg, #fce7f3, #fbcfe8)',
      values: Object.fromEntries(
        compareData.value.map(t => [t.id, t.harvestYear || '未记录'])
      )
    },
    {
      key: 'currentStock',
      label: '库存',
      icon: '📦',
      gradient: 'linear-gradient(135deg, #dbeafe, #bfdbfe)',
      values: Object.fromEntries(
        compareData.value.map(t => [t.id, t.currentStock != null ? `${t.currentStock}${t.stockUnit || ''}` : '-'])
      )
    },
    {
      key: 'waterTemperature',
      label: '默认水温',
      icon: '🌡️',
      gradient: 'linear-gradient(135deg, #fee2e2, #fecaca)',
      values: Object.fromEntries(
        compareData.value.map(t => {
          const params = brewingDataMap.value[t.id]
          if (!params || params.length === 0) return [t.id, '-']
          const def = params.find(p => p.isDefault)
          return [t.id, def && def.waterTemperature != null ? `${def.waterTemperature}℃` : '-']
        })
      )
    },
    {
      key: 'totalInfusions',
      label: '总泡数',
      icon: '🫖',
      gradient: 'linear-gradient(135deg, #d1fae5, #a7f3d0)',
      values: Object.fromEntries(
        compareData.value.map(t => {
          const params = brewingDataMap.value[t.id]
          if (!params || params.length === 0) return [t.id, '-']
          const def = params.find(p => p.isDefault)
          return [t.id, def && def.totalInfusions != null ? `${def.totalInfusions}泡` : '-']
        })
      )
    },
    {
      key: 'avgTasting',
      label: '平均品鉴分',
      icon: '⭐',
      gradient: 'linear-gradient(135deg, #ede9fe, #ddd6fe)',
      values: Object.fromEntries(
        compareData.value.map(t => {
          const notes = tastingDataMap.value[t.id]
          if (!notes || notes.length === 0) return [t.id, '-']
          const validScores = notes.map(n => n.overallScore).filter(s => s != null)
          if (validScores.length === 0) return [t.id, '-']
          const avg = validScores.reduce((a, b) => a + b, 0) / validScores.length
          return [t.id, avg.toFixed(1)]
        })
      )
    }
  ]
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
  const def = params.find(p => p.isDefault)
  return def ? (def[key] ?? '-') : '-'
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
  const def = params.find(p => p.isDefault)
  return def ? [def.firstInfusionTime, def.secondInfusionTime, def.thirdInfusionTime, def.subsequentInfusionTime].filter(v => v != null) : []
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

function openSyncDialog(type, teaId) {
  syncType.value = type
  if (type === 'source') {
    sourceTeaId.value = teaId
    targetTeaId.value = null
  } else {
    targetTeaId.value = teaId
    sourceTeaId.value = null
  }
  sourceParamId.value = null
  targetParamName.value = ''
  setAsDefault.value = true
  syncPreview.value = []
  syncDialogVisible.value = true
}

async function loadSyncPreview() {
  if (!sourceParamId.value || !targetTeaId.value) {
    syncPreview.value = []
    return
  }
  syncPreviewLoading.value = true
  try {
    const res = await previewBrewingParamSync(sourceTeaId.value, sourceParamId.value, targetTeaId.value)
    syncPreview.value = res.data || []
  } catch (e) {
    syncPreview.value = []
  } finally {
    syncPreviewLoading.value = false
  }
}

async function confirmSync() {
  if (!sourceParamId.value || !targetTeaId.value) {
    ElMessage.warning('请选择来源参数和目标茶叶')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定要将「${sourceTea.value?.name}」的冲泡参数同步到「${targetTea.value?.name}」吗？`,
      '确认同步',
      { type: 'warning' }
    )
  } catch {
    return
  }

  syncLoading.value = true
  try {
    const res = await syncBrewingParam(sourceTeaId.value, {
      sourceTeaId: sourceTeaId.value,
      sourceParamId: sourceParamId.value,
      targetTeaId: targetTeaId.value,
      targetParamName: targetParamName.value || undefined,
      setAsDefault: setAsDefault.value
    })
    ElMessage.success('参数同步成功')
    syncDialogVisible.value = false
    const brewingRes = await getBrewingParams(targetTeaId.value)
    brewingDataMap.value[targetTeaId.value] = brewingRes.data || []
  } catch (e) {
    ElMessage.error(e.message || '同步失败')
  } finally {
    syncLoading.value = false
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

.dim-grid {
  margin-top: 4px;
}

.dim-card {
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 16px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.dim-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.dim-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.6);
}

.dim-icon {
  font-size: 22px;
  line-height: 1;
}

.dim-label {
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
}

.dim-tea-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.dim-tea-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.dim-tea-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.dim-tea-name {
  color: #4b5563;
  min-width: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dim-tea-value {
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
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

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.card-header .section-title {
  margin: 0;
}

.sync-form {
  margin-bottom: 20px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.form-tip.error {
  color: #f56c6c;
}

.preview-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30px 0;
  gap: 8px;
  color: #909399;
  font-size: 13px;
}

.diff-preview {
  margin-top: 16px;
  border-top: 1px solid #eee;
  padding-top: 16px;
}

.diff-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.diff-source {
  color: #67c23a;
  font-weight: 500;
}

.diff-target {
  color: #909399;
  text-decoration: line-through;
}

.diff-empty {
  padding: 20px 0;
}
</style>
