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
              <div class="card-header-left">
                <span class="card-title">🎯 最新评估结果</span>
                <span class="record-date">{{ formatTime(latestRecord.recordDate) }}</span>
              </div>
              <div class="card-header-right">
                <el-tag v-if="latestRecord.hasRisk" :type="getRiskTagType(latestRecord.riskLevel)" effect="dark" size="small">
                  {{ latestRecord.riskLevel }}
                </el-tag>
                <el-tag v-else type="success" effect="dark" size="small">无风险</el-tag>
                <el-button
                  v-if="latestRecord.hasRisk"
                  type="danger"
                  size="small"
                  :icon="Warning"
                  style="margin-left: 8px"
                  @click="openRiskDialog(latestRecord)"
                >
                  记录处置
                </el-button>
              </div>
            </div>
          </template>

          <div v-if="latestRecord.hasRisk" class="risk-alert" :class="`risk-${latestRecord.riskLevel}`">
            <div class="risk-alert-header">
              <el-icon class="risk-icon"><Warning /></el-icon>
              <span class="risk-title">检测到仓储风险</span>
            </div>
            <div class="risk-items">
              <el-tag
                v-for="(item, idx) in latestRecord.abnormalItems"
                :key="idx"
                type="danger"
                effect="light"
                size="small"
              >
                {{ item }}
              </el-tag>
            </div>
          </div>

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
                <span class="score-num" :class="{ 'abnormal': latestRecord.temperatureScore < 20 }">
                  {{ latestRecord.temperatureScore }}/30
                </span>
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
                <span class="score-num" :class="{ 'abnormal': latestRecord.humidityScore < 20 }">
                  {{ latestRecord.humidityScore }}/30
                </span>
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
                <span class="score-num" :class="{ 'abnormal': latestRecord.sealScore < 12 }">
                  {{ latestRecord.sealScore }}/20
                </span>
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
                <span class="score-num" :class="{ 'abnormal': latestRecord.storageMethodScore < 15 }">
                  {{ latestRecord.storageMethodScore }}/20
                </span>
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

    <el-card class="risk-action-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <div class="card-header-left">
            <span class="card-title">🛡️ 风险处置记录</span>
            <span class="record-count">共 {{ riskActions.length }} 条记录</span>
          </div>
          <el-radio-group v-model="riskFilterStatus" size="small" @change="filterRiskActions">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="处理中">处理中</el-radio-button>
            <el-radio-button label="已完成">已完成</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div v-if="filteredRiskActions.length === 0" class="risk-empty">
        <el-empty description="暂无风险处置记录" :image-size="80" />
      </div>

      <div v-else class="risk-timeline">
        <el-timeline>
          <el-timeline-item
            v-for="action in filteredRiskActions"
            :key="action.id"
            :timestamp="formatFullTime(action.actionDate || action.createdAt)"
            placement="top"
            :type="getTimelineType(action.riskLevel)"
            :hollow="action.resultStatus === '已完成'"
          >
            <el-card class="timeline-card" shadow="hover">
              <div class="timeline-card-header">
                <div class="timeline-header-left">
                  <el-tag :type="getRiskTagType(action.riskLevel)" size="small" effect="dark">
                    {{ action.riskLevel }}
                  </el-tag>
                  <span class="timeline-risk-type">{{ action.riskType }}</span>
                </div>
                <div class="timeline-header-right">
                  <el-tag :type="getResultTagType(action.resultStatus)" size="small">
                    {{ action.resultStatus || '处理中' }}
                  </el-tag>
                  <el-dropdown v-if="action.resultStatus !== '已完成'" style="margin-left: 8px">
                    <el-button text size="small">
                      操作<el-icon><ArrowDown /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item @click="openEditRiskDialog(action)">
                          <el-icon><Edit /></el-icon> 编辑
                        </el-dropdown-item>
                        <el-dropdown-item @click="markAsComplete(action)">
                          <el-icon><Check /></el-icon> 标记完成
                        </el-dropdown-item>
                        <el-dropdown-item divided @click="handleDeleteRisk(action.id)">
                          <el-icon style="color: #f56c6c"><Delete /></el-icon> 删除
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                  <el-dropdown v-else style="margin-left: 8px">
                    <el-button text size="small">
                      操作<el-icon><ArrowDown /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item @click="handleDeleteRisk(action.id)">
                          <el-icon style="color: #f56c6c"><Delete /></el-icon> 删除
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>

              <div v-if="action.riskDescription" class="timeline-desc">
                <span class="desc-label">风险描述：</span>
                <span>{{ action.riskDescription }}</span>
              </div>

              <div class="timeline-compare">
                <div class="compare-col compare-before">
                  <div class="compare-title">处置前</div>
                  <div class="compare-item">
                    <span>🌡️</span>
                    <span>{{ action.temperatureBefore != null ? action.temperatureBefore + '℃' : '-' }}</span>
                  </div>
                  <div class="compare-item">
                    <span>💧</span>
                    <span>{{ action.humidityBefore != null ? action.humidityBefore + '%' : '-' }}</span>
                  </div>
                  <div class="compare-item">
                    <span>🔒</span>
                    <span>{{ action.sealBefore || '-' }}</span>
                  </div>
                </div>

                <div class="compare-arrow">
                  <el-icon :size="24" :color="action.resultStatus === '已完成' ? '#67c23a' : '#e6a23c'">
                    <Right />
                  </el-icon>
                </div>

                <div class="compare-col compare-after">
                  <div class="compare-title">处置后</div>
                  <div class="compare-item">
                    <span>🌡️</span>
                    <span :class="{ 'has-value': action.temperatureAfter != null }">
                      {{ action.temperatureAfter != null ? action.temperatureAfter + '℃' : '待更新' }}
                    </span>
                  </div>
                  <div class="compare-item">
                    <span>💧</span>
                    <span :class="{ 'has-value': action.humidityAfter != null }">
                      {{ action.humidityAfter != null ? action.humidityAfter + '%' : '待更新' }}
                    </span>
                  </div>
                  <div class="compare-item">
                    <span>🔒</span>
                    <span :class="{ 'has-value': action.sealAfter }">
                      {{ action.sealAfter || '待更新' }}
                    </span>
                  </div>
                </div>
              </div>

              <div class="timeline-action-box">
                <div class="action-row">
                  <span class="action-label">处置方式：</span>
                  <span>{{ action.actionType || '-' }}</span>
                </div>
                <div v-if="action.actionDescription" class="action-row">
                  <span class="action-label">处置详情：</span>
                  <span>{{ action.actionDescription }}</span>
                </div>
                <div v-if="action.actionBy" class="action-row">
                  <span class="action-label">处置人：</span>
                  <span>{{ action.actionBy }}</span>
                </div>
              </div>

              <div v-if="action.resultDescription" class="timeline-result-box">
                <div class="result-title">📋 处置结果</div>
                <div class="result-content">{{ action.resultDescription }}</div>
              </div>

              <div v-if="action.followUpDate" class="timeline-followup">
                <el-icon><Clock /></el-icon>
                <span>复查时间：{{ formatFullTime(action.followUpDate) }}</span>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>

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
        <el-table-column label="风险" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.hasRisk" :type="getRiskTagType(row.riskLevel)" size="small">
              {{ row.riskLevel }}
            </el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
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
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button
              v-if="row.hasRisk"
              type="warning"
              text
              size="small"
              @click="openRiskDialog(row)"
            >
              记录处置
            </el-button>
            <el-button type="danger" text size="small" @click="handleDelete(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="riskDialogVisible"
      :title="isEditRisk ? '编辑处置记录' : '记录风险处置'"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form :model="riskForm" label-width="120px" class="risk-form">
        <div class="form-section-title">⚠️ 风险信息</div>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="风险类型" required>
              <el-select v-model="riskForm.riskType" placeholder="请选择风险类型" style="width: 100%">
                <el-option v-for="type in RISK_TYPES" :key="type" :label="type" :value="type" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="风险等级" required>
              <el-select v-model="riskForm.riskLevel" placeholder="请选择风险等级" style="width: 100%">
                <el-option label="低风险" value="低风险" />
                <el-option label="中风险" value="中风险" />
                <el-option label="高风险" value="高风险" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="风险描述">
          <el-input v-model="riskForm.riskDescription" type="textarea" :rows="2" placeholder="请描述具体风险情况" />
        </el-form-item>

        <div class="form-section-title">📊 处置前环境</div>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="温度(℃)">
              <el-input-number
                v-model="riskForm.temperatureBefore"
                :min="-50"
                :max="80"
                :step="0.5"
                :precision="1"
                style="width: 100%"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="湿度(%)">
              <el-input-number
                v-model="riskForm.humidityBefore"
                :min="0"
                :max="100"
                :step="1"
                :precision="0"
                style="width: 100%"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="密封状态">
              <el-select v-model="riskForm.sealBefore" placeholder="请选择" style="width: 100%" clearable>
                <el-option v-for="condition in SEAL_CONDITIONS" :key="condition" :label="condition" :value="condition" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <div class="form-section-title">🛠️ 处置措施</div>
        <el-form-item label="处置方式">
          <el-select v-model="riskForm.actionType" placeholder="请选择处置方式" style="width: 100%" clearable>
            <el-option v-for="action in ACTION_TYPES" :key="action" :label="action" :value="action" />
          </el-select>
        </el-form-item>
        <el-form-item label="处置详情">
          <el-input v-model="riskForm.actionDescription" type="textarea" :rows="3" placeholder="请描述具体处置措施" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="处置人">
              <el-input v-model="riskForm.actionBy" placeholder="请输入处置人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处置时间">
              <el-date-picker
                v-model="riskForm.actionDate"
                type="datetime"
                placeholder="选择处置时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DDTHH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="form-section-title">✅ 处置结果</div>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="温度(℃)">
              <el-input-number
                v-model="riskForm.temperatureAfter"
                :min="-50"
                :max="80"
                :step="0.5"
                :precision="1"
                style="width: 100%"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="湿度(%)">
              <el-input-number
                v-model="riskForm.humidityAfter"
                :min="0"
                :max="100"
                :step="1"
                :precision="0"
                style="width: 100%"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="密封状态">
              <el-select v-model="riskForm.sealAfter" placeholder="请选择" style="width: 100%" clearable>
                <el-option v-for="condition in SEAL_CONDITIONS" :key="condition" :label="condition" :value="condition" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="处理状态">
              <el-select v-model="riskForm.resultStatus" placeholder="请选择状态" style="width: 100%">
                <el-option label="处理中" value="处理中" />
                <el-option label="已完成" value="已完成" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="复查时间">
              <el-date-picker
                v-model="riskForm.followUpDate"
                type="datetime"
                placeholder="选择复查时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DDTHH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="结果描述">
          <el-input v-model="riskForm.resultDescription" type="textarea" :rows="2" placeholder="请描述处置结果" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="riskDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingRisk" @click="saveRiskAction">
          {{ isEditRisk ? '保存修改' : '保存记录' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Warning, ArrowDown, Edit, Delete, Check, Right, Clock } from '@element-plus/icons-vue'
import {
  evaluateSuitability,
  getSuitabilityRecords,
  getTeaById,
  deleteSuitability,
  createRiskAction,
  updateRiskAction,
  getRiskActions,
  deleteRiskAction
} from '../api/tea'
import {
  TEA_STORAGE_CONDITIONS,
  STORAGE_METHODS,
  SEAL_CONDITIONS,
  TEA_RECOMMENDED_STORAGE
} from '../utils/constants'

const route = useRoute()
const teaId = route.params.id

const RISK_TYPES = ['温度异常', '湿度异常', '密封异常', '储存方式不匹配', '综合风险']
const ACTION_TYPES = [
  '调整空调温度',
  '启动除湿设备',
  '启动加湿设备',
  '转移储存位置',
  '更换包装材料',
  '重新密封包装',
  '更换储存容器',
  '通风处理',
  '其他措施'
]

const loading = ref(false)
const evaluating = ref(false)
const loadingRecords = ref(false)
const loadingRiskActions = ref(false)
const savingRisk = ref(false)
const tea = ref({})
const records = ref([])
const latestRecord = ref(null)
const riskActions = ref([])
const riskFilterStatus = ref('')

const riskDialogVisible = ref(false)
const isEditRisk = ref(false)
const editingRiskId = ref(null)
const currentSuitabilityId = ref(null)
const riskForm = ref({
  riskType: '',
  riskLevel: '',
  riskDescription: '',
  temperatureBefore: null,
  humidityBefore: null,
  sealBefore: '',
  actionType: '',
  actionDescription: '',
  actionBy: '',
  actionDate: '',
  temperatureAfter: null,
  humidityAfter: null,
  sealAfter: '',
  resultStatus: '处理中',
  resultDescription: '',
  followUpDate: ''
})

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

const filteredRiskActions = computed(() => {
  if (!riskFilterStatus.value) return riskActions.value
  return riskActions.value.filter(a => a.resultStatus === riskFilterStatus.value)
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

async function fetchRiskActions() {
  loadingRiskActions.value = true
  try {
    const res = await getRiskActions(teaId)
    riskActions.value = res.data || []
  } catch (e) {
    ElMessage.error('获取风险处置记录失败')
  } finally {
    loadingRiskActions.value = false
  }
}

function filterRiskActions() {
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

function openRiskDialog(suitability) {
  isEditRisk.value = false
  editingRiskId.value = null
  currentSuitabilityId.value = suitability.id
  riskForm.value = {
    riskType: suitability.abnormalItems?.[0] || '',
    riskLevel: suitability.riskLevel === '正常' ? '低风险' : suitability.riskLevel,
    riskDescription: suitability.suggestions || '',
    temperatureBefore: suitability.temperature,
    humidityBefore: suitability.humidity,
    sealBefore: suitability.sealCondition,
    actionType: '',
    actionDescription: '',
    actionBy: '',
    actionDate: new Date().toISOString().slice(0, 19),
    temperatureAfter: null,
    humidityAfter: null,
    sealAfter: '',
    resultStatus: '处理中',
    resultDescription: '',
    followUpDate: ''
  }
  riskDialogVisible.value = true
}

function openEditRiskDialog(action) {
  isEditRisk.value = true
  editingRiskId.value = action.id
  currentSuitabilityId.value = action.suitabilityId
  riskForm.value = {
    riskType: action.riskType || '',
    riskLevel: action.riskLevel || '',
    riskDescription: action.riskDescription || '',
    temperatureBefore: action.temperatureBefore,
    humidityBefore: action.humidityBefore,
    sealBefore: action.sealBefore || '',
    actionType: action.actionType || '',
    actionDescription: action.actionDescription || '',
    actionBy: action.actionBy || '',
    actionDate: action.actionDate ? action.actionDate.slice(0, 19) : '',
    temperatureAfter: action.temperatureAfter,
    humidityAfter: action.humidityAfter,
    sealAfter: action.sealAfter || '',
    resultStatus: action.resultStatus || '处理中',
    resultDescription: action.resultDescription || '',
    followUpDate: action.followUpDate ? action.followUpDate.slice(0, 19) : ''
  }
  riskDialogVisible.value = true
}

async function saveRiskAction() {
  if (!riskForm.value.riskType || !riskForm.value.riskLevel) {
    ElMessage.warning('请填写风险类型和风险等级')
    return
  }

  savingRisk.value = true
  try {
    const payload = {
      ...riskForm.value,
      suitabilityId: currentSuitabilityId.value
    }

    let res
    if (isEditRisk.value) {
      res = await updateRiskAction(teaId, editingRiskId.value, payload)
      ElMessage.success('修改成功')
    } else {
      res = await createRiskAction(teaId, payload)
      ElMessage.success('记录成功')
    }

    riskDialogVisible.value = false
    await fetchRiskActions()
  } catch (e) {
    ElMessage.error(isEditRisk.value ? '修改失败' : '保存失败')
  } finally {
    savingRisk.value = false
  }
}

async function markAsComplete(action) {
  try {
    await ElMessageBox.confirm('确定要将此处置记录标记为已完成吗？', '确认完成', {
      type: 'success'
    })
    await updateRiskAction(teaId, action.id, { resultStatus: '已完成' })
    ElMessage.success('已标记为完成')
    await fetchRiskActions()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

async function handleDeleteRisk(id) {
  try {
    await ElMessageBox.confirm('确定要删除这条风险处置记录吗？', '确认删除', {
      type: 'warning'
    })
    await deleteRiskAction(teaId, id)
    riskActions.value = riskActions.value.filter(a => a.id !== id)
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

function formatFullTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').replace(/\.\d+$/, '').substring(0, 19)
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

function getRiskTagType(level) {
  const map = {
    '低风险': 'warning',
    '中风险': 'warning',
    '高风险': 'danger',
    '正常': 'success'
  }
  return map[level] || 'info'
}

function getResultTagType(status) {
  const map = {
    '处理中': 'warning',
    '已完成': 'success'
  }
  return map[status] || 'info'
}

function getTimelineType(level) {
  const map = {
    '低风险': 'warning',
    '中风险': 'warning',
    '高风险': 'danger'
  }
  return map[level] || 'primary'
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
  fetchRiskActions()
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

.card-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-header-right {
  display: flex;
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

.risk-alert {
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  border-left: 4px solid;
}

.risk-alert.risk-低风险 {
  background: #fdf6ec;
  border-color: #e6a23c;
}

.risk-alert.risk-中风险 {
  background: #fdf6ec;
  border-color: #e6a23c;
}

.risk-alert.risk-高风险 {
  background: #fef0f0;
  border-color: #f56c6c;
}

.risk-alert-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.risk-icon {
  font-size: 18px;
  color: #e6a23c;
}

.risk-高风险 .risk-icon {
  color: #f56c6c;
}

.risk-title {
  font-weight: 600;
  color: #303133;
}

.risk-items {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
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

.score-num.abnormal {
  color: #e63946;
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

.risk-empty {
  padding: 40px 0;
}

.risk-timeline {
  padding: 10px 0;
}

.timeline-card {
  margin-bottom: 4px;
}

.timeline-card :deep(.el-card__body) {
  padding: 16px;
}

.timeline-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.timeline-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.timeline-risk-type {
  font-size: 15px;
  font-weight: 600;
  color: #1b4332;
}

.timeline-header-right {
  display: flex;
  align-items: center;
}

.timeline-desc {
  font-size: 13px;
  color: #495057;
  margin-bottom: 12px;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.desc-label {
  font-weight: 600;
  color: #6c757d;
}

.timeline-compare {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.compare-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 6px;
}

.compare-before {
  background: #fff0f0;
  border: 1px solid #fbc4c4;
}

.compare-after {
  background: #f0f9eb;
  border: 1px solid #c2e7b0;
}

.compare-title {
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 4px;
  color: #606266;
}

.compare-before .compare-title {
  color: #f56c6c;
}

.compare-after .compare-title {
  color: #67c23a;
}

.compare-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #495057;
}

.compare-item .has-value {
  color: #67c23a;
  font-weight: 600;
}

.compare-arrow {
  flex-shrink: 0;
}

.timeline-action-box {
  background: #ecf5ff;
  border: 1px solid #d9ecff;
  border-radius: 6px;
  padding: 10px 14px;
  margin-bottom: 12px;
}

.action-row {
  display: flex;
  font-size: 13px;
  margin-bottom: 4px;
}

.action-row:last-child {
  margin-bottom: 0;
}

.action-label {
  font-weight: 600;
  color: #409eff;
  width: 80px;
  flex-shrink: 0;
}

.timeline-result-box {
  background: linear-gradient(135deg, #f0f9eb, #e1f3d8);
  border-radius: 6px;
  padding: 12px 14px;
  margin-bottom: 12px;
  border: 1px solid #c2e7b0;
}

.result-title {
  font-size: 13px;
  font-weight: 600;
  color: #67c23a;
  margin-bottom: 6px;
}

.result-content {
  font-size: 13px;
  color: #495057;
  line-height: 1.5;
}

.timeline-followup {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #909399;
  padding: 8px 12px;
  background: #f4f4f5;
  border-radius: 6px;
}

.form-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #1b4332;
  padding: 8px 0 8px 12px;
  margin: 8px 0 16px;
  border-left: 3px solid #52b788;
  background: #f8f9fa;
  border-radius: 0 4px 4px 0;
}

.risk-form :deep(.el-form-item) {
  margin-bottom: 16px;
}
</style>
