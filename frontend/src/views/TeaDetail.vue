<template>
  <div class="tea-detail-page" v-loading="loading">
    <div class="page-header">
      <el-button @click="$router.back()" :icon="ArrowLeft" plain>返回</el-button>
      <h2 class="section-title">{{ tea.name }}</h2>
      <div class="header-actions">
        <el-button type="primary" plain @click="$router.push(`/tea/${tea.id}/edit`)">编辑档案</el-button>
        <el-button type="danger" plain @click="handleDeleteTea">删除档案</el-button>
      </div>
    </div>

    <el-card class="info-card">
      <div class="tea-hero">
        <div class="tea-hero-img">
          <img v-if="tea.imageUrl" :src="tea.imageUrl" :alt="tea.name" />
          <div v-else class="tea-hero-placeholder">
            <span>🍵</span>
          </div>
        </div>
        <div class="tea-hero-info">
          <h1>{{ tea.name }}</h1>
          <div class="tea-tags">
            <el-tag :type="getCategoryTagType(tea.teaCategory)" size="large">{{ tea.teaCategory }}</el-tag>
            <el-tag type="info" size="large">{{ tea.originRegion }}</el-tag>
            <el-tag v-if="tea.harvestYear" size="large">{{ tea.harvestYear }}年</el-tag>
          </div>
          <el-descriptions :column="2" class="tea-desc">
            <el-descriptions-item label="储存方式">{{ tea.storageMethod || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="现有存量">{{ tea.currentStock || 0 }} {{ tea.stockUnit }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatTime(tea.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatTime(tea.updatedAt) }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ tea.description || '暂无描述' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-card>

    <el-tabs v-model="activeTab" class="detail-tabs">
      <el-tab-pane label="冲泡参数" name="brewing">
        <div class="tab-header">
          <span class="section-title">冲泡参数设置</span>
          <el-button type="primary" size="small" @click="openBrewingDialog()">添加参数</el-button>
        </div>
        <el-row :gutter="16">
          <el-col v-for="param in brewingParams" :key="param.id" :xs="24" :sm="12" :md="8">
            <el-card class="param-card" shadow="hover">
              <div class="param-card-header">
                <span class="param-name">{{ param.paramName || '冲泡方案' }}</span>
                <div>
                  <el-tag v-if="param.isDefault" type="success" size="small">默认</el-tag>
                  <el-button size="small" text type="primary" @click="openBrewingDialog(param)">编辑</el-button>
                  <el-button size="small" text type="danger" @click="handleDeleteBrewing(param.id)">删除</el-button>
                </div>
              </div>
              <div class="param-grid">
                <div class="param-item">
                  <span class="param-label">水温</span>
                  <span class="param-value">{{ param.waterTemperature }}℃</span>
                </div>
                <div class="param-item">
                  <span class="param-label">投茶量</span>
                  <span class="param-value">{{ param.teaAmount }}克</span>
                </div>
                <div class="param-item">
                  <span class="param-label">茶水比</span>
                  <span class="param-value">{{ param.teaRatio || '-' }}</span>
                </div>
                <div class="param-item">
                  <span class="param-label">注水量</span>
                  <span class="param-value">{{ param.waterAmount || '-' }}ml</span>
                </div>
              </div>
              <el-divider style="margin:12px 0" />
              <div class="infusion-chart">
                <div class="infusion-bar-group">
                  <div class="infusion-bar" v-for="(val, idx) in getInfusionData(param)" :key="idx"
                    :style="{ height: Math.max(10, val / 2) + 'px' }">
                    <span class="bar-label">{{ val }}s</span>
                  </div>
                </div>
                <div class="infusion-labels">
                  <span v-for="n in Math.min(4, param.totalInfusions || 4)" :key="n">第{{ n }}泡</span>
                  <span v-if="(param.totalInfusions || 4) > 4">+</span>
                </div>
              </div>
              <div v-if="param.notes" class="param-notes">
                <el-text type="info" size="small">{{ param.notes }}</el-text>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="brewingParams.length === 0" description="暂无冲泡参数，快来添加吧" />
      </el-tab-pane>

      <el-tab-pane label="仓储状态" name="storage">
        <div class="tab-header">
          <span class="section-title">仓储状态更新</span>
          <el-button type="primary" size="small" @click="openStorageDialog()">添加记录</el-button>
        </div>
        <el-table :data="storageRecords" stripe style="width:100%">
          <el-table-column prop="recordDate" label="记录日期" width="180">
            <template #default="{ row }">{{ formatTime(row.recordDate) }}</template>
          </el-table-column>
          <el-table-column prop="storageLocation" label="储存位置" width="120" />
          <el-table-column prop="temperature" label="温度(℃)" width="100" />
          <el-table-column prop="humidity" label="湿度(%)" width="100" />
          <el-table-column prop="sealCondition" label="密封状况" width="120" />
          <el-table-column prop="stockChange" label="存量变化" width="120">
            <template #default="{ row }">
              <span :style="{ color: row.stockChange > 0 ? '#67c23a' : row.stockChange < 0 ? '#f56c6c' : '' }">
                {{ row.stockChange > 0 ? '+' : '' }}{{ row.stockChange }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="currentStock" label="当前存量" width="120" />
          <el-table-column prop="notes" label="备注" min-width="150" />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button size="small" text type="primary" @click="openStorageDialog(row)">编辑</el-button>
              <el-button size="small" text type="danger" @click="handleDeleteStorage(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="storageRecords.length === 0" description="暂无仓储记录" />
      </el-tab-pane>

      <el-tab-pane label="品饮记录" name="tasting">
        <div class="tab-header">
          <span class="section-title">品饮体感备注</span>
          <el-button type="primary" size="small" @click="openTastingDialog()">添加记录</el-button>
        </div>
        <el-row :gutter="16">
          <el-col v-for="note in tastingNotes" :key="note.id" :xs="24" :sm="12">
            <el-card class="tasting-card" shadow="hover">
              <div class="tasting-card-header">
                <span class="tasting-date">{{ formatTime(note.tastingDate) }}</span>
                <div>
                  <el-tag size="small">{{ note.brewingMethod || '未指定' }}</el-tag>
                  <el-button size="small" text type="primary" @click="openTastingDialog(note)">编辑</el-button>
                  <el-button size="small" text type="danger" @click="handleDeleteTasting(note.id)">删除</el-button>
                </div>
              </div>
              <div class="tasting-scores">
                <div class="score-item">
                  <span class="score-label">香气</span>
                  <el-rate v-model="note.aromaScore" disabled :max="5" />
                  <span class="score-desc">{{ note.aromaDesc }}</span>
                </div>
                <div class="score-item">
                  <span class="score-label">汤色</span>
                  <el-rate v-model="note.liquorColorScore" disabled :max="5" />
                  <span class="score-desc">{{ note.liquorColorDesc }}</span>
                </div>
                <div class="score-item">
                  <span class="score-label">口感</span>
                  <el-rate v-model="note.tasteScore" disabled :max="5" />
                  <span class="score-desc">{{ note.tasteDesc }}</span>
                </div>
                <div class="score-item">
                  <span class="score-label">回甘</span>
                  <el-rate v-model="note.aftertasteScore" disabled :max="5" />
                  <span class="score-desc">{{ note.aftertasteDesc }}</span>
                </div>
              </div>
              <div v-if="note.overallScore" class="tasting-overall">
                综合评分：<span class="overall-num">{{ note.overallScore }}</span>/100
              </div>
              <div v-if="note.impression" class="tasting-impression">
                {{ note.impression }}
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="tastingNotes.length === 0" description="暂无品饮记录，记录你的品茶体验吧" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="brewingDialogVisible" :title="editingBrewing ? '编辑冲泡参数' : '添加冲泡参数'" width="700px" destroy-on-close>
      <el-form :model="brewingForm" label-width="100px">
        <el-form-item label="参数名称">
          <el-input v-model="brewingForm.paramName" placeholder="如：日常冲泡" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="水温(℃)">
              <el-slider v-model="brewingForm.waterTemperature" :min="50" :max="100" show-input />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="投茶量(克)">
              <el-input-number v-model="brewingForm.teaAmount" :min="0.5" :max="50" :step="0.5" :precision="1" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="茶水比">
              <el-input v-model="brewingForm.teaRatio" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="注水量(ml)">
              <el-input-number v-model="brewingForm.waterAmount" :min="50" :max="500" :step="10" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="水质">
              <el-select v-model="brewingForm.waterQuality" style="width:100%">
                <el-option v-for="w in WATER_QUALITY_OPTIONS" :key="w" :label="w" :value="w" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider>出汤时长(秒)</el-divider>
        <el-row :gutter="16">
          <el-col :span="6"><el-form-item label="第一泡"><el-input-number v-model="brewingForm.firstInfusionTime" :min="1" :max="120" style="width:100%" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="第二泡"><el-input-number v-model="brewingForm.secondInfusionTime" :min="1" :max="120" style="width:100%" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="第三泡"><el-input-number v-model="brewingForm.thirdInfusionTime" :min="1" :max="120" style="width:100%" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="后续泡"><el-input-number v-model="brewingForm.subsequentInfusionTime" :min="1" :max="180" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="可泡次数"><el-input-number v-model="brewingForm.totalInfusions" :min="1" :max="30" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设为默认"><el-switch v-model="brewingForm.isDefault" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="brewingForm.notes" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="brewingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveBrewing" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="storageDialogVisible" :title="editingStorage ? '编辑仓储记录' : '添加仓储记录'" width="600px" destroy-on-close>
      <el-form :model="storageForm" label-width="100px">
        <el-form-item label="储存位置">
          <el-input v-model="storageForm.storageLocation" placeholder="如：书房储物柜" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="温度(℃)">
              <el-input-number v-model="storageForm.temperature" :precision="1" :step="0.5" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="湿度(%)">
              <el-input-number v-model="storageForm.humidity" :precision="1" :step="1" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="密封状况">
          <el-select v-model="storageForm.sealCondition" style="width:100%">
            <el-option v-for="s in SEAL_CONDITIONS" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="存量变化">
              <el-input-number v-model="storageForm.stockChange" :precision="2" :step="10" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="当前存量">
              <el-input-number v-model="storageForm.currentStock" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="storageForm.notes" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="storageDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveStorage" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="tastingDialogVisible" :title="editingTasting ? '编辑品饮记录' : '添加品饮记录'" width="700px" destroy-on-close>
      <el-form :model="tastingForm" label-width="100px">
        <el-form-item label="冲泡方式">
          <el-select v-model="tastingForm.brewingMethod" style="width:100%">
            <el-option v-for="b in BREWING_METHODS" :key="b" :label="b" :value="b" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="香气评分">
              <el-rate v-model="tastingForm.aromaScore" :max="5" show-text />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="汤色评分">
              <el-rate v-model="tastingForm.liquorColorScore" :max="5" show-text />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="香气描述">
              <el-input v-model="tastingForm.aromaDesc" placeholder="如：花香馥郁" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="汤色描述">
              <el-input v-model="tastingForm.liquorColorDesc" placeholder="如：金黄透亮" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="口感评分">
              <el-rate v-model="tastingForm.tasteScore" :max="5" show-text />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="回甘评分">
              <el-rate v-model="tastingForm.aftertasteScore" :max="5" show-text />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="口感描述">
              <el-input v-model="tastingForm.tasteDesc" placeholder="如：醇厚甘甜" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="回甘描述">
              <el-input v-model="tastingForm.aftertasteDesc" placeholder="如：回甘持久" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="综合评分">
          <el-slider v-model="tastingForm.overallScore" :min="0" :max="100" show-input />
        </el-form-item>
        <el-form-item label="整体感受">
          <el-input v-model="tastingForm.impression" type="textarea" :rows="3" placeholder="记录你的品饮体验" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tastingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveTasting" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getTeaById, deleteTea,
  getBrewingParams, createBrewingParam, updateBrewingParam, deleteBrewingParam,
  getStorageRecords, createStorageRecord, updateStorageRecord, deleteStorageRecord,
  getTastingNotes, createTastingNote, updateTastingNote, deleteTastingNote
} from '../api/tea'
import { SEAL_CONDITIONS, WATER_QUALITY_OPTIONS, BREWING_METHODS } from '../utils/constants'

const route = useRoute()
const router = useRouter()
const teaId = route.params.id
const loading = ref(false)
const saving = ref(false)
const activeTab = ref('brewing')

const tea = ref({})
const brewingParams = ref([])
const storageRecords = ref([])
const tastingNotes = ref([])

const brewingDialogVisible = ref(false)
const storageDialogVisible = ref(false)
const tastingDialogVisible = ref(false)

const editingBrewing = ref(null)
const editingStorage = ref(null)
const editingTasting = ref(null)

const brewingForm = ref({})
const storageForm = ref({})
const tastingForm = ref({})

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

function getCategoryTagType(category) {
  const map = { '绿茶': 'success', '红茶': 'danger', '乌龙茶': 'warning', '白茶': 'info', '黄茶': 'warning', '花茶': 'success' }
  return map[category] || ''
}

function getInfusionData(param) {
  return [param.firstInfusionTime, param.secondInfusionTime, param.thirdInfusionTime, param.subsequentInfusionTime].filter(v => v != null)
}

async function loadTea() {
  loading.value = true
  try {
    const [teaRes, brewingRes, storageRes, tastingRes] = await Promise.all([
      getTeaById(teaId),
      getBrewingParams(teaId),
      getStorageRecords(teaId),
      getTastingNotes(teaId)
    ])
    tea.value = teaRes.data
    brewingParams.value = brewingRes.data || []
    storageRecords.value = storageRes.data || []
    tastingNotes.value = tastingRes.data || []
  } finally {
    loading.value = false
  }
}

async function handleDeleteTea() {
  await ElMessageBox.confirm('确定删除此茶叶档案？所有关联数据将一并删除', '确认删除', { type: 'warning' })
  await deleteTea(teaId)
  ElMessage.success('删除成功')
  router.push('/')
}

function openBrewingDialog(param = null) {
  editingBrewing.value = param
  brewingForm.value = param ? { ...param } : {
    paramName: '', waterTemperature: 90, teaAmount: 5, teaRatio: '1:30',
    waterAmount: 150, firstInfusionTime: 15, secondInfusionTime: 20,
    thirdInfusionTime: 25, subsequentInfusionTime: 30, totalInfusions: 5,
    waterQuality: '纯净水', notes: '', isDefault: false
  }
  brewingDialogVisible.value = true
}

async function handleSaveBrewing() {
  saving.value = true
  try {
    if (editingBrewing.value) {
      await updateBrewingParam(teaId, editingBrewing.value.id, brewingForm.value)
    } else {
      await createBrewingParam(teaId, brewingForm.value)
    }
    ElMessage.success('保存成功')
    brewingDialogVisible.value = false
    const res = await getBrewingParams(teaId)
    brewingParams.value = res.data || []
  } finally {
    saving.value = false
  }
}

async function handleDeleteBrewing(id) {
  await ElMessageBox.confirm('确定删除此冲泡参数？', '确认删除', { type: 'warning' })
  await deleteBrewingParam(teaId, id)
  ElMessage.success('删除成功')
  const res = await getBrewingParams(teaId)
  brewingParams.value = res.data || []
}

function openStorageDialog(record = null) {
  editingStorage.value = record
  storageForm.value = record ? { ...record } : {
    storageLocation: '', temperature: null, humidity: null,
    sealCondition: '', stockChange: null, currentStock: null, notes: ''
  }
  storageDialogVisible.value = true
}

async function handleSaveStorage() {
  saving.value = true
  try {
    if (editingStorage.value) {
      await updateStorageRecord(teaId, editingStorage.value.id, storageForm.value)
    } else {
      await createStorageRecord(teaId, storageForm.value)
    }
    ElMessage.success('保存成功')
    storageDialogVisible.value = false
    const res = await getStorageRecords(teaId)
    storageRecords.value = res.data || []
  } finally {
    saving.value = false
  }
}

async function handleDeleteStorage(id) {
  await ElMessageBox.confirm('确定删除此仓储记录？', '确认删除', { type: 'warning' })
  await deleteStorageRecord(teaId, id)
  ElMessage.success('删除成功')
  const res = await getStorageRecords(teaId)
  storageRecords.value = res.data || []
}

function openTastingDialog(note = null) {
  editingTasting.value = note
  tastingForm.value = note ? { ...note } : {
    brewingMethod: '', aromaScore: 0, aromaDesc: '', liquorColorScore: 0,
    liquorColorDesc: '', tasteScore: 0, tasteDesc: '', aftertasteScore: 0,
    aftertasteDesc: '', overallScore: 80, impression: ''
  }
  tastingDialogVisible.value = true
}

async function handleSaveTasting() {
  saving.value = true
  try {
    if (editingTasting.value) {
      await updateTastingNote(teaId, editingTasting.value.id, tastingForm.value)
    } else {
      await createTastingNote(teaId, tastingForm.value)
    }
    ElMessage.success('保存成功')
    tastingDialogVisible.value = false
    const res = await getTastingNotes(teaId)
    tastingNotes.value = res.data || []
  } finally {
    saving.value = false
  }
}

async function handleDeleteTasting(id) {
  await ElMessageBox.confirm('确定删除此品饮记录？', '确认删除', { type: 'warning' })
  await deleteTastingNote(teaId, id)
  ElMessage.success('删除成功')
  const res = await getTastingNotes(teaId)
  tastingNotes.value = res.data || []
}

onMounted(loadTea)
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.header-actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.info-card {
  margin-bottom: 20px;
}

.tea-hero {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.tea-hero-img {
  width: 200px;
  min-width: 200px;
  height: 200px;
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(135deg, #d8f3dc, #b7e4c7);
  flex-shrink: 0;
}

.tea-hero-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.tea-hero-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  font-size: 72px;
}

.tea-hero-info {
  flex: 1;
}

.tea-hero-info h1 {
  font-size: 24px;
  color: #1b4332;
  margin-bottom: 12px;
}

.tea-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.detail-tabs {
  margin-top: 4px;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.param-card {
  margin-bottom: 16px;
}

.param-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.param-name {
  font-weight: 600;
  font-size: 15px;
  color: #1b4332;
}

.param-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.param-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 10px;
  background: #f8f9fa;
  border-radius: 6px;
}

.param-label {
  font-size: 13px;
  color: #6c757d;
}

.param-value {
  font-size: 14px;
  font-weight: 600;
  color: #2d6a4f;
}

.infusion-chart {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.infusion-bar-group {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  height: 60px;
}

.infusion-bar {
  width: 36px;
  background: linear-gradient(180deg, #52b788, #2d6a4f);
  border-radius: 4px 4px 0 0;
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: center;
}

.bar-label {
  font-size: 10px;
  color: #fff;
  padding-top: 2px;
}

.infusion-labels {
  display: flex;
  gap: 12px;
  font-size: 11px;
  color: #6c757d;
  margin-top: 4px;
}

.infusion-labels span {
  width: 36px;
  text-align: center;
}

.param-notes {
  margin-top: 8px;
}

.tasting-card {
  margin-bottom: 16px;
}

.tasting-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.tasting-date {
  font-size: 14px;
  color: #6c757d;
}

.tasting-scores {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.score-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.score-label {
  font-size: 13px;
  color: #6c757d;
  width: 36px;
  flex-shrink: 0;
}

.score-desc {
  font-size: 13px;
  color: #495057;
}

.tasting-overall {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #eee;
  font-size: 14px;
  color: #2d6a4f;
}

.overall-num {
  font-size: 24px;
  font-weight: 700;
  color: #2d6a4f;
}

.tasting-impression {
  margin-top: 8px;
  font-size: 14px;
  color: #495057;
  line-height: 1.6;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 6px;
}

@media (max-width: 768px) {
  .tea-hero {
    flex-direction: column;
    align-items: center;
  }
  .tea-hero-img {
    width: 100%;
    min-width: unset;
    height: 200px;
  }
}
</style>
