<template>
  <div class="suitability-ledger-page" v-loading="loading">
    <div class="page-header">
      <el-button @click="$router.back()" :icon="ArrowLeft" plain>返回</el-button>
      <h2 class="section-title">仓储适宜度账册</h2>
      <div class="header-actions">
        <el-tag :type="getCategoryTagType(tea.teaCategory)" size="large">{{ tea.teaCategory }}</el-tag>
        <span class="tea-name">{{ tea.name }}</span>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :xs="24" :md="10">
        <el-card class="evaluate-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">📝 环境评估</span>
            </div>
          </template>

          <el-form :model="evaluateForm" label-width="100px" @submit.prevent="handleEvaluate">
            <el-form-item label="储存方式">
              <el-select v-model="evaluateForm.storageMethod" placeholder="请选择储存方式" style="width: 100%">
                <el-option v-for="method in STORAGE_METHODS" :key="method" :label="method" :value="method" />
              </el-select>
            </el-form-item>

            <el-form-item label="温度">
              <el-input-number
                v-model="evaluateForm.temperature"
                :min="-50"
                :max="80"
                :step="0.5"
                :precision="1"
                style="width: 100%"
                controls-position="right"
              />
              <span class="unit">℃</span>
            </el-form-item>

            <el-form-item label="湿度">
              <el-input-number
                v-model="evaluateForm.humidity"
                :min="0"
                :max="100"
                :step="1"
                :precision="0"
                style="width: 100%"
                controls-position="right"
              />
              <span class="unit">%</span>
            </el-form-item>

            <el-form-item label="密封状态">
              <el-select v-model="evaluateForm.sealCondition" placeholder="请选择密封状态" style="width: 100%">
                <el-option v-for="condition in SEAL_CONDITIONS" :key="condition" :label="condition" :value="condition" />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleEvaluate" :loading="evaluating" style="width: 100%">
                生成适宜度评估
              </el-button>
            </el-form-item>
          </el-form>

          <el-divider />

          <div class="reference-info">
            <div class="reference-title">📊 {{ tea.teaCategory }}储存参考</div>
            <div class="reference-item">
              <span class="ref-label">适宜温度:</span>
              <span class="ref-value">{{ suitableRange.tempMin }} - {{ suitableRange.tempMax }} ℃</span>
            </div>
            <div class="reference-item">
              <span class="ref-label">适宜湿度:</span>
              <span class="ref-value">{{ suitableRange.humidityMin }} - {{ suitableRange.humidityMax }} %</span>
            </div>
            <div class="reference-item">
              <span class="ref-label">推荐方式:</span>
              <span class="ref-value">{{ recommendedStorage.join('、') }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="14">
        <el-card class="result-card" v-if="latestRecord">
          <template #header>
            <div class="card-header">
              <span class="card-title">🎯 最新评估结果</span>
              <span class="record-date">{{ formatTime(latestRecord.recordDate) }}</span>
            </div>
          </template>

          <div class="score-overview">
            <div class="score-circle" :class="getLevelClass(latestRecord.suitabilityLevel)">
              <div class="score-value">{{ latestRecord.totalScore }}</div>
              <div class="score-label">总分</div>
            </div>
            <div class="level-info">
              <div class="level-badge" :class="getLevelClass(latestRecord.suitabilityLevel)">
                {{ latestRecord.suitabilityLevel }}
              </div>
              <div class="level-desc">
                仓储环境{{ latestRecord.suitabilityLevel }}，
                {{ getLevelDescription(latestRecord.suitabilityLevel) }}
              </div>
            </div>
          </div>

          <el-divider />

          <div class="score-details">
            <div class="score-item">
              <div class="score-header">
                <span class="score-icon">🌡️</span>
                <span class="score-name">温度适宜度</span>
                <span class="score-num">{{ latestRecord.temperatureScore }}/30</span>
              </div>
              <el-progress
                :percentage="Math.round((latestRecord.temperatureScore / 30) * 100)"
                :color="getScoreColor(latestRecord.temperatureScore, 30)"
                :stroke-width="8"
                :show-text="false"
              />
            </div>

            <div class="score-item">
              <div class="score-header">
                <span class="score-icon">💧</span>
                <span class="score-name">湿度适宜度</span>
                <span class="score-num">{{ latestRecord.humidityScore }}/30</span>
              </div>
              <el-progress
                :percentage="Math.round((latestRecord.humidityScore / 30) * 100)"
                :color="getScoreColor(latestRecord.humidityScore, 30)"
                :stroke-width="8"
                :show-text="false"
              />
            </div>

            <div class="score-item">
              <div class="score-header">
                <span class="score-icon">🔒</span>
                <span class="score-name">密封状态</span>
                <span class="score-num">{{ latestRecord.sealScore }}/20</span>
              </div>
              <el-progress
                :percentage="Math.round((latestRecord.sealScore / 20) * 100)"
                :color="getScoreColor(latestRecord.sealScore, 20)"
                :stroke-width="8"
                :show-text="false"
              />
            </div>

            <div class="score-item">
              <div class="score-header">
                <span class="score-icon">📦</span>
                <span class="score-name">储存方式匹配</span>
                <span class="score-num">{{ latestRecord.storageMethodScore }}/20</span>
              </div>
              <el-progress
                :percentage="Math.round((latestRecord.storageMethodScore / 20) * 100)"
                :color="getScoreColor(latestRecord.storageMethodScore, 20)"
                :stroke-width="8"
                :show-text="false"
              />
            </div>
          </div>

          <el-divider />

          <div class="suggestions-box">
            <div class="suggestions-title">💡 改进建议</div>
            <div class="suggestions-content">{{ latestRecord.suggestions }}</div>
          </div>
        </el-card>

        <el-card class="empty-card" v-else>
          <el-empty description="暂无评估记录，请先进行环境评估" />
        </el-card>
      </el-col>
    </el-row>

    <el-card class="history-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span class="card-title">📚 历史账册</span>
          <span class="record-count">共 {{ records.length }} 条记录</span>
        </div>
      </template>

      <el-table :data="records" v-loading="loadingRecords" empty-text="暂无历史记录">
        <el-table-column prop="recordDate" label="记录时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.recordDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="storageMethod" label="储存方式" width="120" />
        <el-table-column prop="temperature" label="温度(℃)" width="100" />
        <el-table-column prop="humidity" label="湿度(%)" width="100" />
        <el-table-column prop="sealCondition" label="密封状态" width="120" />
        <el-table-column label="评分" width="180">
          <template #default="{ row }">
            <div class="table-score">
              <span class="table-score-num" :class="getLevelClass(row.suitabilityLevel)">
                {{ row.totalScore }}
              </span>
              <span class="table-score-total">/100</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="suitabilityLevel" label="等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.suitabilityLevel)" size="small">
              {{ row.suitabilityLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" text size="small" @click="handleDelete(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import {
  evaluateSuitability,
  getSuitabilityRecords,
  getTeaById,
  deleteSuitability
} from '../api/tea'
import {
  TEA_STORAGE_CONDITIONS,
  STORAGE_METHODS,
  SEAL_CONDITIONS,
  TEA_RECOMMENDED_STORAGE
} from '../utils/constants'

const route = useRoute()
const teaId = route.params.id

const loading = ref(false)
const evaluating = ref(false)
const loadingRecords = ref(false)
const tea = ref({})
const records = ref([])
const latestRecord = ref(null)

const evaluateForm = ref({
  storageMethod: '',
  temperature: null,
  humidity: null,
  sealCondition: ''
})

const suitableRange = computed(() => {
  return TEA_STORAGE_CONDITIONS[tea.value.teaCategory] || TEA_STORAGE_CONDITIONS['默认']
})

const recommendedStorage = computed(() => {
  return TEA_RECOMMENDED_STORAGE[tea.value.teaCategory] || TEA_RECOMMENDED_STORAGE['默认']
})

async function fetchTea() {
  loading.value = true
  try {
    const res = await getTeaById(teaId)
    tea.value = res.data
    if (tea.value.storageMethod) {
      evaluateForm.value.storageMethod = tea.value.storageMethod
    }
  } catch (e) {
    ElMessage.error('获取茶叶信息失败')
  } finally {
    loading.value = false
  }
}

async function fetchRecords() {
  loadingRecords.value = true
  try {
    const res = await getSuitabilityRecords(teaId)
    records.value = res.data || []
    if (records.value.length > 0) {
      latestRecord.value = records.value[0]
    }
  } catch (e) {
    ElMessage.error('获取适宜度记录失败')
  } finally {
    loadingRecords.value = false
  }
}

async function handleEvaluate() {
  if (!evaluateForm.value.temperature && !evaluateForm.value.humidity) {
    ElMessage.warning('请至少填写温度或湿度')
    return
  }

  evaluating.value = true
  try {
    const res = await evaluateSuitability(teaId, evaluateForm.value)
    latestRecord.value = res.data
    records.value.unshift(res.data)
    ElMessage.success('评估完成')
  } catch (e) {
    ElMessage.error('评估失败')
  } finally {
    evaluating.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除这条适宜度记录吗？', '确认删除', {
      type: 'warning'
    })
    await deleteSuitability(teaId, id)
    records.value = records.value.filter(r => r.id !== id)
    if (latestRecord.value && latestRecord.value.id === id) {
      latestRecord.value = records.value[0] || null
    }
    ElMessage.success('删除成功')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

function getLevelClass(level) {
  const map = {
    '优秀': 'level-excellent',
    '良好': 'level-good',
    '一般': 'level-fair',
    '较差': 'level-poor',
    '不适宜': 'level-unsuitable'
  }
  return map[level] || 'level-unknown'
}

function getLevelTagType(level) {
  const map = {
    '优秀': 'success',
    '良好': '',
    '一般': 'warning',
    '较差': 'danger',
    '不适宜': 'danger'
  }
  return map[level] || 'info'
}

function getCategoryTagType(category) {
  const map = {
    '绿茶': 'success',
    '红茶': 'danger',
    '乌龙茶': 'warning',
    '普洱茶': '',
    '白茶': 'info',
    '黑茶': '',
    '黄茶': 'success',
    '花茶': 'danger'
  }
  return map[category] || 'info'
}

function getScoreColor(score, maxScore) {
  const percent = (score / maxScore) * 100
  if (percent >= 85) return '#52b788'
  if (percent >= 70) return '#409eff'
  if (percent >= 50) return '#f4a261'
  return '#e63946'
}

function getLevelDescription(level) {
  const map = {
    '优秀': '非常适合茶叶储存',
    '良好': '基本满足储存要求',
    '一般': '需要适当调整',
    '较差': '对茶叶品质有影响',
    '不适宜': '严重影响茶叶品质'
  }
  return map[level] || ''
}

onMounted(() => {
  fetchTea()
  fetchRecords()
})
</script>

<style scoped>
.suitability-ledger-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.section-title {
  flex: 1;
  margin: 0;
  font-size: 24px;
  color: #1b4332;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tea-name {
  font-size: 16px;
  font-weight: 600;
  color: #1b4332;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1b4332;
}

.record-date {
  font-size: 12px;
  color: #6c757d;
}

.record-count {
  font-size: 14px;
  color: #6c757d;
}

.unit {
  margin-left: 8px;
  color: #6c757d;
}

.reference-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
}

.reference-title {
  font-size: 14px;
  font-weight: 600;
  color: #1b4332;
  margin-bottom: 12px;
}

.reference-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
}

.ref-label {
  color: #6c757d;
}

.ref-value {
  color: #1b4332;
  font-weight: 500;
}

.score-overview {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 20px 0;
}

.score-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.score-circle.level-excellent {
  background: linear-gradient(135deg, #d8f3dc, #52b788);
  color: #fff;
}

.score-circle.level-good {
  background: linear-gradient(135deg, #d0e7ff, #409eff);
  color: #fff;
}

.score-circle.level-fair {
  background: linear-gradient(135deg, #ffecd2, #f4a261);
  color: #fff;
}

.score-circle.level-poor {
  background: linear-gradient(135deg, #ffd6d6, #e63946);
  color: #fff;
}

.score-circle.level-unsuitable {
  background: linear-gradient(135deg, #ffb3b3, #c92a2a);
  color: #fff;
}

.score-value {
  font-size: 36px;
  font-weight: bold;
  line-height: 1;
}

.score-label {
  font-size: 12px;
  margin-top: 4px;
}

.level-info {
  flex: 1;
}

.level-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
}

.level-badge.level-excellent {
  background: #d8f3dc;
  color: #2d6a4f;
}

.level-badge.level-good {
  background: #d0e7ff;
  color: #1971c2;
}

.level-badge.level-fair {
  background: #ffecd2;
  color: #d9480f;
}

.level-badge.level-poor {
  background: #ffd6d6;
  color: #c92a2a;
}

.level-badge.level-unsuitable {
  background: #ffb3b3;
  color: #c92a2a;
}

.level-desc {
  font-size: 14px;
  color: #6c757d;
  line-height: 1.5;
}

.score-details {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.score-item {
  padding: 0 8px;
}

.score-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.score-icon {
  font-size: 18px;
}

.score-name {
  flex: 1;
  font-size: 14px;
  color: #495057;
}

.score-num {
  font-size: 14px;
  font-weight: 600;
  color: #1b4332;
}

.suggestions-box {
  background: linear-gradient(135deg, #fff8e6, #fff4d6);
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #ffe066;
}

.suggestions-title {
  font-size: 14px;
  font-weight: 600;
  color: #d9480f;
  margin-bottom: 8px;
}

.suggestions-content {
  font-size: 13px;
  color: #495057;
  line-height: 1.6;
}

.table-score {
  display: flex;
  align-items: baseline;
}

.table-score-num {
  font-size: 18px;
  font-weight: bold;
}

.table-score-num.level-excellent {
  color: #52b788;
}

.table-score-num.level-good {
  color: #409eff;
}

.table-score-num.level-fair {
  color: #f4a261;
}

.table-score-num.level-poor {
  color: #e63946;
}

.table-score-num.level-unsuitable {
  color: #c92a2a;
}

.table-score-total {
  font-size: 12px;
  color: #adb5bd;
  margin-left: 2px;
}

.empty-card {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.history-card {
  margin-top: 20px;
}
</style>
