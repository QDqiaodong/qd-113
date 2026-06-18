<template>
  <div class="tea-form-page">
    <div class="page-header">
      <el-button @click="$router.back()" :icon="ArrowLeft" plain>返回</el-button>
      <h2 class="section-title">{{ isEdit ? '编辑茶叶档案' : '新建茶叶档案' }}</h2>
    </div>

    <el-card>
      <el-steps :active="currentStep" align-center class="form-steps">
        <el-step title="基础信息" description="茶品名称、产区、年份" />
        <el-step title="储存状态" description="储存方式、现有存量" />
        <el-step title="冲泡参数" description="水温、投茶量、出汤时长" />
        <el-step title="确认提交" description="核对信息" />
      </el-steps>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="step-form">
        <div v-show="currentStep === 0">
          <el-form-item label="茶品名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入茶品名称" maxlength="100" />
          </el-form-item>
          <el-form-item label="茶类" prop="teaCategory">
            <el-select v-model="form.teaCategory" placeholder="请选择茶类" style="width:100%">
              <el-option v-for="c in TEA_CATEGORIES" :key="c" :label="c" :value="c" />
            </el-select>
          </el-form-item>
          <el-form-item label="产区" prop="originRegion">
            <el-select v-model="form.originRegion" placeholder="请选择或输入产区" filterable allow-create style="width:100%">
              <el-option v-for="r in originRegions" :key="r" :label="r" :value="r" />
            </el-select>
          </el-form-item>
          <el-form-item label="采摘年份" prop="harvestYear">
            <el-date-picker 
              v-model="form.harvestYear" 
              type="year" 
              placeholder="选择采摘年份" 
              style="width:100%" 
              value-format="YYYY"
              :disabled-date="disabledHarvestYear"
            />
            <div v-if="form.teaCategory" class="field-hint">
              <el-text size="small" type="info">
                {{ getHarvestYearHint() }}
              </el-text>
            </div>
          </el-form-item>
          <el-form-item v-if="isAgingTeaCategory" label="年份说明" prop="yearNote">
            <el-input 
              v-model="form.yearNote" 
              type="textarea" 
              :rows="2" 
              placeholder="如：头春茶、雨前茶、古树茶等年份相关说明" 
              maxlength="200" 
              show-word-limit 
            />
          </el-form-item>
          <el-form-item label="茶叶描述">
            <el-input v-model="form.description" type="textarea" :rows="3" placeholder="简要描述茶叶特点" maxlength="500" show-word-limit />
          </el-form-item>
        </div>

        <div v-show="currentStep === 1">
          <el-form-item label="储存方式">
            <el-select v-model="form.storageMethod" placeholder="请选择储存方式" style="width:100%">
              <el-option v-for="s in STORAGE_METHODS" :key="s" :label="s" :value="s" />
            </el-select>
          </el-form-item>
          <el-form-item label="现有存量">
            <el-input-number v-model="form.currentStock" :min="0" :precision="2" :step="10" />
          </el-form-item>
          <el-form-item label="计量单位">
            <el-select v-model="form.stockUnit" style="width:200px">
              <el-option v-for="u in STOCK_UNITS" :key="u" :label="u" :value="u" />
            </el-select>
          </el-form-item>
          <el-form-item label="图片链接">
            <el-input v-model="form.imageUrl" placeholder="茶叶图片URL" />
          </el-form-item>
        </div>

        <div v-show="currentStep === 2">
          <el-alert title="冲泡参数可在创建茶叶档案后继续添加和编辑" type="info" :closable="false" show-icon style="margin-bottom:20px" />
          <div class="quick-template">
            <span class="template-label">快速填充模板：</span>
            <el-button v-for="c in TEA_CATEGORIES" :key="c" size="small" @click="fillTemplate(c)" plain>{{ c }}</el-button>
          </div>
          <el-form-item label="参数名称">
            <el-input v-model="brewingForm.paramName" placeholder="如：日常冲泡、品鉴冲泡" />
          </el-form-item>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="水温(℃)">
                <el-slider v-model="brewingForm.waterTemperature" :min="50" :max="100" show-input />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="投茶量(克)">
                <el-input-number v-model="brewingForm.teaAmount" :min="0.5" :max="50" :step="0.5" :precision="1" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="茶水比">
                <el-input v-model="brewingForm.teaRatio" placeholder="1:30" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="注水量(ml)">
                <el-input-number v-model="brewingForm.waterAmount" :min="50" :max="500" :step="10" />
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
          <el-divider content-position="left">出汤时长(秒)</el-divider>
          <el-row :gutter="16">
            <el-col :span="6">
              <el-form-item label="第一泡">
                <el-input-number v-model="brewingForm.firstInfusionTime" :min="1" :max="120" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="第二泡">
                <el-input-number v-model="brewingForm.secondInfusionTime" :min="1" :max="120" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="第三泡">
                <el-input-number v-model="brewingForm.thirdInfusionTime" :min="1" :max="120" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="后续泡">
                <el-input-number v-model="brewingForm.subsequentInfusionTime" :min="1" :max="180" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="可泡次数">
                <el-input-number v-model="brewingForm.totalInfusions" :min="1" :max="30" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="设为默认">
                <el-switch v-model="brewingForm.isDefault" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="备注">
            <el-input v-model="brewingForm.notes" type="textarea" :rows="2" placeholder="冲泡注意事项" />
          </el-form-item>

          <BrewingCurveEditor 
            :brewing-param="brewingForm"
            :editable="true"
            :auto-sync="false"
            @update:brewing-param="brewingForm = $event"
            @curve-change="handleCurveChange"
          />
        </div>

        <div v-show="currentStep === 3">
          <el-descriptions title="茶叶档案信息" :column="2" border>
            <el-descriptions-item label="茶品名称">{{ form.name }}</el-descriptions-item>
            <el-descriptions-item label="茶类">{{ form.teaCategory }}</el-descriptions-item>
            <el-descriptions-item label="产区">{{ form.originRegion }}</el-descriptions-item>
            <el-descriptions-item label="采摘年份">{{ form.harvestYear || '未设置' }}</el-descriptions-item>
            <el-descriptions-item v-if="isAgingTeaCategory && form.yearNote" label="年份说明" :span="2">{{ form.yearNote }}</el-descriptions-item>
            <el-descriptions-item label="储存方式">{{ form.storageMethod || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="现有存量">{{ form.currentStock }}{{ form.stockUnit }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ form.description || '暂无' }}</el-descriptions-item>
          </el-descriptions>
          <el-descriptions v-if="brewingForm.waterTemperature" title="冲泡参数" :column="2" border style="margin-top:16px">
            <el-descriptions-item label="参数名称">{{ brewingForm.paramName || '默认' }}</el-descriptions-item>
            <el-descriptions-item label="水温">{{ brewingForm.waterTemperature }}℃</el-descriptions-item>
            <el-descriptions-item label="投茶量">{{ brewingForm.teaAmount }}克</el-descriptions-item>
            <el-descriptions-item label="茶水比">{{ brewingForm.teaRatio }}</el-descriptions-item>
            <el-descriptions-item label="第一泡">{{ brewingForm.firstInfusionTime }}秒</el-descriptions-item>
            <el-descriptions-item label="第二泡">{{ brewingForm.secondInfusionTime }}秒</el-descriptions-item>
            <el-descriptions-item label="第三泡">{{ brewingForm.thirdInfusionTime }}秒</el-descriptions-item>
            <el-descriptions-item label="后续泡">{{ brewingForm.subsequentInfusionTime }}秒</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="form-actions">
          <el-button v-if="currentStep > 0" @click="currentStep--">上一步</el-button>
          <el-button v-if="currentStep < 3" type="primary" @click="handleNext">下一步</el-button>
          <el-button v-if="currentStep === 3" type="success" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '保存修改' : '创建档案' }}
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { createTea, updateTea, getTeaById, createBrewingParam, getBrewingTemplateByCategory, getRegions } from '../api/tea'
import { TEA_CATEGORIES, STORAGE_METHODS, STOCK_UNITS, WATER_QUALITY_OPTIONS } from '../utils/constants'
import BrewingCurveEditor from '../components/BrewingCurveEditor.vue'
import { useBrewingCurveStore } from '../store/brewingCurve'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const currentStep = ref(0)
const submitting = ref(false)
const formRef = ref(null)

const { setCurveData, clearCurveData } = useBrewingCurveStore()

const AGING_TEA_CATEGORIES = ['普洱茶', '白茶', '黑茶']
const MIN_AGING_HARVEST_YEAR = 1900
const MIN_REGULAR_HARVEST_YEAR = 1950

const form = ref({
  name: '',
  teaCategory: '',
  originRegion: '',
  harvestYear: null,
  yearNote: '',
  storageMethod: '',
  currentStock: 0,
  stockUnit: '克',
  description: '',
  imageUrl: ''
})

const isAgingTeaCategory = computed(() => {
  return AGING_TEA_CATEGORIES.includes(form.value.teaCategory)
})

const currentYear = new Date().getFullYear()

function disabledHarvestYear(date) {
  const year = date.getFullYear()
  const minYear = isAgingTeaCategory.value ? MIN_AGING_HARVEST_YEAR : MIN_REGULAR_HARVEST_YEAR
  return year < minYear || year > currentYear
}

function getHarvestYearHint() {
  const minYear = isAgingTeaCategory.value ? MIN_AGING_HARVEST_YEAR : MIN_REGULAR_HARVEST_YEAR
  if (isAgingTeaCategory.value) {
    return `可陈化茶类，采摘年份范围：${minYear} - ${currentYear}年`
  }
  return `采摘年份范围：${minYear} - ${currentYear}年`
}

const brewingForm = ref({
  paramName: '日常冲泡',
  waterTemperature: 90,
  teaAmount: 5,
  teaRatio: '1:30',
  waterAmount: 150,
  firstInfusionTime: 15,
  secondInfusionTime: 20,
  thirdInfusionTime: 25,
  subsequentInfusionTime: 30,
  totalInfusions: 5,
  waterQuality: '纯净水',
  notes: '',
  isDefault: true
})

const rules = {
  name: [{ required: true, message: '请输入茶品名称', trigger: 'blur' }],
  teaCategory: [{ required: true, message: '请选择茶类', trigger: 'change' }],
  originRegion: [{ required: true, message: '请选择产区', trigger: 'change' }]
}

const loadingTemplate = ref(false)
const originRegions = ref([])

async function loadRegions() {
  try {
    const res = await getRegions()
    originRegions.value = res.data || []
  } catch (e) {
    console.error('加载产区列表失败', e)
  }
}

async function fillTemplate(category) {
  if (loadingTemplate.value) return
  loadingTemplate.value = true
  try {
    const res = await getBrewingTemplateByCategory(category)
    const tpl = res.data
    if (tpl) {
      Object.assign(brewingForm.value, {
        waterTemperature: tpl.waterTemperature,
        teaAmount: tpl.teaAmount,
        teaRatio: tpl.teaRatio,
        waterAmount: tpl.waterAmount,
        firstInfusionTime: tpl.firstInfusionTime,
        secondInfusionTime: tpl.secondInfusionTime,
        thirdInfusionTime: tpl.thirdInfusionTime,
        subsequentInfusionTime: tpl.subsequentInfusionTime,
        totalInfusions: tpl.totalInfusions,
        waterQuality: tpl.waterQuality,
        notes: tpl.tips || ''
      })
      ElMessage.success(`已填充${category}推荐参数`)
    }
  } catch (e) {
    ElMessage.error('获取模板失败')
  } finally {
    loadingTemplate.value = false
  }
}

function handleCurveChange(change) {
  setCurveData(brewingForm.value)
}

watch(brewingForm, (newVal) => {
  if (newVal && newVal.waterTemperature) {
    setCurveData(newVal)
  }
}, { deep: true, immediate: true })

async function handleNext() {
  if (currentStep.value === 0) {
    try {
      await formRef.value.validate()
    } catch {
      return
    }
  }
  currentStep.value++
}

async function handleSubmit() {
  submitting.value = true
  try {
    let teaId
    if (isEdit.value) {
      teaId = route.params.id
      await updateTea(teaId, form.value)
      ElMessage.success('更新成功')
    } else {
      const res = await createTea(form.value)
      teaId = res.data.id
      ElMessage.success('创建成功')
      if (brewingForm.value.waterTemperature) {
        await createBrewingParam(teaId, brewingForm.value)
      }
    }
    router.push(`/tea/${teaId}`)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadRegions()
  if (isEdit.value) {
    const res = await getTeaById(route.params.id)
    const tea = res.data
    form.value = {
      name: tea.name,
      teaCategory: tea.teaCategory,
      originRegion: tea.originRegion,
      harvestYear: tea.harvestYear,
      yearNote: tea.yearNote || '',
      storageMethod: tea.storageMethod,
      currentStock: tea.currentStock,
      stockUnit: tea.stockUnit,
      description: tea.description,
      imageUrl: tea.imageUrl
    }
  }
})

onUnmounted(() => {
  clearCurveData()
})
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.form-steps {
  margin-bottom: 32px;
}

.step-form {
  max-width: 800px;
  margin: 0 auto;
}

.quick-template {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.template-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
</style>
