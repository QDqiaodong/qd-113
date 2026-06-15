<template>
  <div class="brewing-curve-editor">
    <div class="curve-header">
      <h3 class="section-title" style="margin-bottom: 0">冲泡曲线</h3>
      <div class="curve-actions">
        <el-tooltip content="显示所有指标">
          <el-button size="small" :type="showAllMetrics ? 'primary' : ''" plain @click="toggleAllMetrics">
            <el-icon><DataLine /></el-icon>
            全部指标
          </el-button>
        </el-tooltip>
        <el-tooltip content="添加冲泡点">
          <el-button size="small" type="success" plain @click="handleAddBrew" :disabled="!editable">
            <el-icon><Plus /></el-icon>
            添加一泡
          </el-button>
        </el-tooltip>
      </div>
    </div>

    <el-card class="curve-card">
      <div v-if="!curvePoints || curvePoints.length === 0" class="curve-empty">
        <el-empty description="请先选择或创建冲泡参数以查看曲线" />
      </div>

      <template v-else>
        <div class="chart-container">
          <div class="chart-y-axis-left">
            <span class="y-label">水温 (℃)</span>
            <span class="y-label">投茶量 (g)</span>
            <span class="y-label">注水量 (ml)</span>
          </div>
          
          <div class="chart-main">
            <svg 
              ref="chartSvg"
              :width="chartWidth" 
              :height="chartHeight" 
              class="curve-svg"
              @mousemove="handleMouseMove"
              @mouseleave="handleMouseLeave"
            >
              <defs>
                <linearGradient id="tempGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" style="stop-color:#e76f51;stop-opacity:0.3" />
                  <stop offset="100%" style="stop-color:#e76f51;stop-opacity:0.05" />
                </linearGradient>
                <linearGradient id="teaGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" style="stop-color:#2d6a4f;stop-opacity:0.3" />
                  <stop offset="100%" style="stop-color:#2d6a4f;stop-opacity:0.05" />
                </linearGradient>
                <linearGradient id="waterGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" style="stop-color:#2196f3;stop-opacity:0.3" />
                  <stop offset="100%" style="stop-color:#2196f3;stop-opacity:0.05" />
                </linearGradient>
                <linearGradient id="timeGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" style="stop-color:#ff9800;stop-opacity:0.3" />
                  <stop offset="100%" style="stop-color:#ff9800;stop-opacity:0.05" />
                </linearGradient>
              </defs>

              <g class="grid-lines">
                <line 
                  v-for="i in 5" 
                  :key="'h'+i" 
                  :x1="padding.left" 
                  :y1="padding.top + (chartHeight - padding.top - padding.bottom) * i / 5" 
                  :x2="chartWidth - padding.right" 
                  :y2="padding.top + (chartHeight - padding.top - padding.bottom) * i / 5" 
                  stroke="#e0e0e0" 
                  stroke-dasharray="4,4"
                />
                <line 
                  v-for="(point, i) in curvePoints" 
                  :key="'v'+i" 
                  :x1="getX(i)" 
                  :y1="padding.top" 
                  :x2="getX(i)" 
                  :y2="chartHeight - padding.bottom" 
                  stroke="#f0f0f0" 
                  stroke-dasharray="2,2"
                />
              </g>

              <g class="x-axis-labels">
                <text 
                  v-for="(point, i) in curvePoints" 
                  :key="'xlabel'+i" 
                  :x="getX(i)" 
                  :y="chartHeight - 10" 
                  text-anchor="middle" 
                  class="x-label"
                >
                  第{{ point.brewNumber }}泡
                </text>
              </g>

              <template v-if="visibleMetrics.waterTemperature">
                <path 
                  :d="getAreaPath('waterTemperature', tempMax, tempMin)" 
                  fill="url(#tempGradient)"
                />
                <path 
                  :d="getLinePath('waterTemperature', tempMax, tempMin)" 
                  fill="none" 
                  :stroke="metricColors.waterTemperature" 
                  stroke-width="2.5"
                  class="curve-line"
                />
              </template>

              <template v-if="visibleMetrics.teaAmount">
                <path 
                  :d="getAreaPath('teaAmount', teaMax, teaMin)" 
                  fill="url(#teaGradient)"
                />
                <path 
                  :d="getLinePath('teaAmount', teaMax, teaMin)" 
                  fill="none" 
                  :stroke="metricColors.teaAmount" 
                  stroke-width="2.5"
                  class="curve-line"
                />
              </template>

              <template v-if="visibleMetrics.waterAmount">
                <path 
                  :d="getAreaPath('waterAmount', waterMax, waterMin)" 
                  fill="url(#waterGradient)"
                />
                <path 
                  :d="getLinePath('waterAmount', waterMax, waterMin)" 
                  fill="none" 
                  :stroke="metricColors.waterAmount" 
                  stroke-width="2.5"
                  class="curve-line"
                />
              </template>

              <template v-if="visibleMetrics.infusionTime">
                <path 
                  :d="getAreaPath('infusionTime', timeMax, timeMin)" 
                  fill="url(#timeGradient)"
                />
                <path 
                  :d="getLinePath('infusionTime', timeMax, timeMin)" 
                  fill="none" 
                  :stroke="metricColors.infusionTime" 
                  stroke-width="2.5"
                  class="curve-line"
                />
              </template>

              <template v-if="visibleMetrics.waterTemperature">
                <g v-for="(point, i) in curvePoints" :key="'temp-point-'+i">
                  <circle 
                    :cx="getX(i)" 
                    :cy="getY(point.waterTemperature, tempMax, tempMin)" 
                    r="7" 
                    :fill="metricColors.waterTemperature"
                    class="data-point"
                    :class="{ 'dragging': draggingPoint?.index === i && draggingPoint?.field === 'waterTemperature' }"
                    @mousedown="startDrag(i, 'waterTemperature', $event)"
                    v-if="editable"
                  />
                  <circle 
                    :cx="getX(i)" 
                    :cy="getY(point.waterTemperature, tempMax, tempMin)" 
                    r="5" 
                    :fill="metricColors.waterTemperature"
                    class="data-point"
                    v-else
                  />
                  <title>第{{ point.brewNumber }}泡: {{ point.waterTemperature }}℃</title>
                </g>
              </template>

              <template v-if="visibleMetrics.teaAmount">
                <g v-for="(point, i) in curvePoints" :key="'tea-point-'+i">
                  <circle 
                    :cx="getX(i)" 
                    :cy="getY(point.teaAmount, teaMax, teaMin)" 
                    r="7" 
                    :fill="metricColors.teaAmount"
                    class="data-point"
                    :class="{ 'dragging': draggingPoint?.index === i && draggingPoint?.field === 'teaAmount' }"
                    @mousedown="startDrag(i, 'teaAmount', $event)"
                    v-if="editable"
                  />
                  <circle 
                    :cx="getX(i)" 
                    :cy="getY(point.teaAmount, teaMax, teaMin)" 
                    r="5" 
                    :fill="metricColors.teaAmount"
                    class="data-point"
                    v-else
                  />
                  <title>第{{ point.brewNumber }}泡: {{ point.teaAmount }}g</title>
                </g>
              </template>

              <template v-if="visibleMetrics.waterAmount">
                <g v-for="(point, i) in curvePoints" :key="'water-point-'+i">
                  <circle 
                    :cx="getX(i)" 
                    :cy="getY(point.waterAmount, waterMax, waterMin)" 
                    r="7" 
                    :fill="metricColors.waterAmount"
                    class="data-point"
                    :class="{ 'dragging': draggingPoint?.index === i && draggingPoint?.field === 'waterAmount' }"
                    @mousedown="startDrag(i, 'waterAmount', $event)"
                    v-if="editable"
                  />
                  <circle 
                    :cx="getX(i)" 
                    :cy="getY(point.waterAmount, waterMax, waterMin)" 
                    r="5" 
                    :fill="metricColors.waterAmount"
                    class="data-point"
                    v-else
                  />
                  <title>第{{ point.brewNumber }}泡: {{ point.waterAmount }}ml</title>
                </g>
              </template>

              <template v-if="visibleMetrics.infusionTime">
                <g v-for="(point, i) in curvePoints" :key="'time-point-'+i">
                  <circle 
                    :cx="getX(i)" 
                    :cy="getY(point.infusionTime, timeMax, timeMin)" 
                    r="7" 
                    :fill="metricColors.infusionTime"
                    class="data-point"
                    :class="{ 'dragging': draggingPoint?.index === i && draggingPoint?.field === 'infusionTime' }"
                    @mousedown="startDrag(i, 'infusionTime', $event)"
                    v-if="editable"
                  />
                  <circle 
                    :cx="getX(i)" 
                    :cy="getY(point.infusionTime, timeMax, timeMin)" 
                    r="5" 
                    :fill="metricColors.infusionTime"
                    class="data-point"
                    v-else
                  />
                  <title>第{{ point.brewNumber }}泡: {{ point.infusionTime }}秒</title>
                </g>
              </template>
            </svg>
          </div>

          <div class="chart-y-axis-right">
            <span class="y-label">出汤时长 (秒)</span>
          </div>
        </div>

        <div class="chart-legend">
          <div 
            v-for="(config, key) in metricConfig" 
            :key="key"
            class="legend-item"
            :class="{ 'active': visibleMetrics[key] }"
            @click="toggleMetric(key)"
          >
            <span class="legend-dot" :style="{ background: config.color }"></span>
            <span class="legend-text">{{ config.label }}</span>
            <span class="legend-value" v-if="hoveredPoint != null">
              {{ getHoverValue(key) }}
            </span>
          </div>
        </div>

        <div class="point-actions" v-if="editable && hoveredPoint != null">
          <el-button 
            size="small" 
            type="danger" 
            plain
            @click="handleRemoveBrew(hoveredPoint)"
            :disabled="curvePoints.length <= 1"
          >
            <el-icon><Delete /></el-icon>
            删除第{{ curvePoints[hoveredPoint]?.brewNumber }}泡
          </el-button>
        </div>

        <el-divider />

        <div class="points-table">
          <h4 class="table-title">详细数据</h4>
          <el-table :data="curvePoints" size="small" border>
            <el-table-column prop="brewNumber" label="泡数" width="80" align="center" />
            <el-table-column label="水温(℃)" width="120">
              <template #default="{ row, $index }">
                <el-input-number 
                  v-if="editable"
                  v-model="row.waterTemperature" 
                  :min="50" 
                  :max="100" 
                  size="small"
                  @change="handlePointChange($index, 'waterTemperature', row.waterTemperature)"
                />
                <span v-else>{{ row.waterTemperature }}</span>
              </template>
            </el-table-column>
            <el-table-column label="投茶量(g)" width="120">
              <template #default="{ row, $index }">
                <el-input-number 
                  v-if="editable"
                  v-model="row.teaAmount" 
                  :min="0.5" 
                  :max="50" 
                  :step="0.5" 
                  :precision="1"
                  size="small"
                  @change="handlePointChange($index, 'teaAmount', row.teaAmount)"
                />
                <span v-else>{{ row.teaAmount }}</span>
              </template>
            </el-table-column>
            <el-table-column label="注水量(ml)" width="120">
              <template #default="{ row, $index }">
                <el-input-number 
                  v-if="editable"
                  v-model="row.waterAmount" 
                  :min="50" 
                  :max="500" 
                  :step="10"
                  size="small"
                  @change="handlePointChange($index, 'waterAmount', row.waterAmount)"
                />
                <span v-else>{{ row.waterAmount }}</span>
              </template>
            </el-table-column>
            <el-table-column label="出汤时长(秒)" width="140">
              <template #default="{ row, $index }">
                <el-input-number 
                  v-if="editable"
                  v-model="row.infusionTime" 
                  :min="1" 
                  :max="180"
                  size="small"
                  @change="handlePointChange($index, 'infusionTime', row.infusionTime)"
                />
                <span v-else>{{ row.infusionTime }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center" v-if="editable">
              <template #default="{ $index }">
                <el-button 
                  type="danger" 
                  text 
                  size="small"
                  @click="handleRemoveBrew($index)"
                  :disabled="curvePoints.length <= 1"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { Plus, Delete, DataLine } from '@element-plus/icons-vue'
import { useBrewingCurveStore } from '../store/brewingCurve'

const props = defineProps({
  brewingParam: {
    type: Object,
    default: null
  },
  editable: {
    type: Boolean,
    default: true
  },
  autoSync: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:brewingParam', 'curveChange'])

const { 
  state, 
  setCurveData, 
  updateCurvePoint, 
  addBrewPoint, 
  removeBrewPoint,
  generateCurvePoints 
} = useBrewingCurveStore()

const chartSvg = ref(null)
const chartWidth = ref(800)
const chartHeight = ref(350)
const hoveredPoint = ref(null)
const draggingPoint = ref(null)
const dragStartY = ref(0)
const dragStartValue = ref(0)

const padding = {
  top: 30,
  right: 30,
  bottom: 40,
  left: 30
}

const metricColors = {
  waterTemperature: '#e76f51',
  teaAmount: '#2d6a4f',
  waterAmount: '#2196f3',
  infusionTime: '#ff9800'
}

const metricConfig = {
  waterTemperature: { label: '水温', color: '#e76f51', unit: '℃', min: 50, max: 100 },
  teaAmount: { label: '投茶量', color: '#2d6a4f', unit: 'g', min: 0, max: 20 },
  waterAmount: { label: '注水量', color: '#2196f3', unit: 'ml', min: 50, max: 300 },
  infusionTime: { label: '出汤时长', color: '#ff9800', unit: 's', min: 0, max: 120 }
}

const visibleMetrics = ref({
  waterTemperature: true,
  teaAmount: true,
  waterAmount: true,
  infusionTime: true
})

const curvePoints = computed(() => {
  if (props.brewingParam) {
    return generateCurvePoints(props.brewingParam)
  }
  return state.currentCurveData?.curvePoints || []
})

const tempMax = computed(() => Math.max(...curvePoints.value.map(p => p.waterTemperature || 0), 100))
const tempMin = computed(() => Math.min(...curvePoints.value.map(p => p.waterTemperature || 100), 50))
const teaMax = computed(() => Math.max(...curvePoints.value.map(p => p.teaAmount || 0), 10) * 1.1)
const teaMin = computed(() => 0)
const waterMax = computed(() => Math.max(...curvePoints.value.map(p => p.waterAmount || 0), 200) * 1.1)
const waterMin = computed(() => 0)
const timeMax = computed(() => Math.max(...curvePoints.value.map(p => p.infusionTime || 0), 60) * 1.1)
const timeMin = computed(() => 0)

function getX(index) {
  const innerWidth = chartWidth.value - padding.left - padding.right
  const step = innerWidth / (curvePoints.value.length - 1 || 1)
  return padding.left + step * index
}

function getY(value, max, min) {
  const innerHeight = chartHeight.value - padding.top - padding.bottom
  const range = max - min || 1
  return padding.top + innerHeight - ((value - min) / range) * innerHeight
}

function getLinePath(field, max, min) {
  return curvePoints.value.map((p, i) => {
    const x = getX(i)
    const y = getY(p[field], max, min)
    return i === 0 ? `M ${x} ${y}` : `L ${x} ${y}`
  }).join(' ')
}

function getAreaPath(field, max, min) {
  const linePath = getLinePath(field, max, min)
  const lastX = getX(curvePoints.value.length - 1)
  const firstX = getX(0)
  const baseY = chartHeight.value - padding.bottom
  return `${linePath} L ${lastX} ${baseY} L ${firstX} ${baseY} Z`
}

function toggleMetric(key) {
  visibleMetrics.value[key] = !visibleMetrics.value[key]
}

function toggleAllMetrics() {
  const allVisible = Object.values(visibleMetrics.value).every(v => v)
  const newValue = !allVisible
  Object.keys(visibleMetrics.value).forEach(key => {
    visibleMetrics.value[key] = newValue
  })
}

function showAllMetrics() {
  return Object.values(visibleMetrics.value).every(v => v)
}

function handleMouseMove(event) {
  if (!chartSvg.value) return
  
  const rect = chartSvg.value.getBoundingClientRect()
  const x = event.clientX - rect.left
  
  let closestIndex = null
  let closestDist = Infinity
  
  curvePoints.value.forEach((_, i) => {
    const pointX = getX(i)
    const dist = Math.abs(x - pointX)
    if (dist < closestDist && dist < 40) {
      closestDist = dist
      closestIndex = i
    }
  })
  
  hoveredPoint.value = closestIndex
  
  if (draggingPoint.value && props.editable) {
    const y = event.clientY - rect.top
    const innerHeight = chartHeight.value - padding.top - padding.bottom
    
    const { index, field } = draggingPoint.value
    const config = metricConfig[field]
    
    let max, min
    if (field === 'waterTemperature') {
      max = tempMax.value
      min = tempMin.value
    } else if (field === 'teaAmount') {
      max = teaMax.value
      min = teaMin.value
    } else if (field === 'waterAmount') {
      max = waterMax.value
      min = waterMin.value
    } else {
      max = timeMax.value
      min = timeMin.value
    }
    
    const range = max - min || 1
    const relativeY = Math.max(0, Math.min(innerHeight, y - padding.top))
    let newValue = Math.round(max - (relativeY / innerHeight) * range)
    
    newValue = Math.max(config.min, Math.min(config.max, newValue))
    
    if (field === 'teaAmount') {
      newValue = Math.round(newValue * 2) / 2
    }
    
    handlePointChange(index, field, newValue)
  }
}

function handleMouseLeave() {
  hoveredPoint.value = null
  if (draggingPoint.value) {
    draggingPoint.value = null
  }
}

function startDrag(index, field, event) {
  if (!props.editable) return
  event.preventDefault()
  
  draggingPoint.value = { index, field }
  dragStartY.value = event.clientY
  dragStartValue.value = curvePoints.value[index][field]
}

function handlePointChange(index, field, value) {
  if (props.autoSync) {
    updateCurvePoint(index, field, value)
  }
  
  if (props.brewingParam) {
    const newParam = { ...props.brewingParam }
    const points = generateCurvePoints(newParam)
    points[index][field] = value
    
    if (index === 0) newParam.firstInfusionTime = points[0].infusionTime
    if (index === 1) newParam.secondInfusionTime = points[1].infusionTime
    if (index === 2) newParam.thirdInfusionTime = points[2].infusionTime
    if (index >= 3) newParam.subsequentInfusionTime = points[index].infusionTime
    
    if (field === 'waterTemperature') newParam.waterTemperature = value
    if (field === 'teaAmount') newParam.teaAmount = value
    if (field === 'waterAmount') newParam.waterAmount = value
    
    emit('update:brewingParam', newParam)
  }
  
  emit('curveChange', { index, field, value })
}

function handleAddBrew() {
  if (props.brewingParam) {
    const newParam = { ...props.brewingParam }
    const points = generateCurvePoints(newParam)
    const lastPoint = points[points.length - 1]
    
    points.push({
      brewNumber: points.length + 1,
      waterTemperature: lastPoint?.waterTemperature || 90,
      teaAmount: lastPoint?.teaAmount || 5,
      waterAmount: lastPoint?.waterAmount || 150,
      infusionTime: (lastPoint?.infusionTime || 30) + 5
    })
    
    newParam.totalInfusions = points.length
    if (points.length >= 4) {
      newParam.subsequentInfusionTime = points[3].infusionTime
    }
    if (points.length >= 3) {
      newParam.thirdInfusionTime = points[2].infusionTime
    }
    if (points.length >= 2) {
      newParam.secondInfusionTime = points[1].infusionTime
    }
    newParam.firstInfusionTime = points[0].infusionTime
    
    emit('update:brewingParam', newParam)
  } else {
    addBrewPoint()
  }
  emit('curveChange', { type: 'add' })
}

function handleRemoveBrew(index) {
  if (curvePoints.value.length <= 1) return
  
  if (props.brewingParam) {
    const newParam = { ...props.brewingParam }
    const points = generateCurvePoints(newParam)
    points.splice(index, 1)
    
    points.forEach((p, i) => {
      p.brewNumber = i + 1
    })
    
    newParam.totalInfusions = points.length
    newParam.firstInfusionTime = points[0]?.infusionTime
    newParam.secondInfusionTime = points[1]?.infusionTime
    newParam.thirdInfusionTime = points[2]?.infusionTime
    newParam.subsequentInfusionTime = points[3]?.infusionTime
    newParam.waterTemperature = points[0]?.waterTemperature
    newParam.teaAmount = points[0]?.teaAmount
    newParam.waterAmount = points[0]?.waterAmount
    
    emit('update:brewingParam', newParam)
  } else {
    removeBrewPoint(index)
  }
  emit('curveChange', { type: 'remove', index })
}

function getHoverValue(key) {
  if (hoveredPoint.value == null) return ''
  const point = curvePoints.value[hoveredPoint.value]
  if (!point) return ''
  const config = metricConfig[key]
  return `${point[key]}${config.unit}`
}

function updateChartSize() {
  const container = chartSvg.value?.parentElement
  if (container) {
    chartWidth.value = container.clientWidth
  }
}

let resizeObserver = null

onMounted(() => {
  updateChartSize()
  resizeObserver = new ResizeObserver(updateChartSize)
  const container = chartSvg.value?.parentElement
  if (container) {
    resizeObserver.observe(container)
  }
  
  window.addEventListener('mouseup', () => {
    if (draggingPoint.value) {
      draggingPoint.value = null
    }
  })
})

onUnmounted(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
  }
})

watch(() => props.brewingParam, (newParam) => {
  if (newParam && props.autoSync) {
    setCurveData(newParam)
  }
}, { immediate: true, deep: true })
</script>

<style scoped>
.brewing-curve-editor {
  margin-top: 16px;
}

.curve-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.curve-actions {
  display: flex;
  gap: 8px;
}

.curve-card {
  padding: 16px;
}

.curve-empty {
  padding: 40px 0;
}

.chart-container {
  display: flex;
  gap: 8px;
  align-items: stretch;
}

.chart-y-axis-left,
.chart-y-axis-right {
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  width: 100px;
  flex-shrink: 0;
}

.chart-y-axis-left {
  text-align: right;
  padding-right: 8px;
}

.chart-y-axis-right {
  text-align: left;
  padding-left: 8px;
  justify-content: center;
}

.y-label {
  font-size: 11px;
  color: #909399;
}

.chart-main {
  flex: 1;
  min-width: 0;
  overflow-x: auto;
}

.curve-svg {
  display: block;
  width: 100%;
  height: auto;
  cursor: crosshair;
}

.curve-line {
  transition: stroke-width 0.2s;
}

.data-point {
  cursor: pointer;
  transition: r 0.2s, fill 0.2s;
  stroke: #fff;
  stroke-width: 2;
}

.data-point:hover {
  r: 9;
}

.data-point.dragging {
  r: 10;
  opacity: 0.8;
}

.x-label {
  font-size: 11px;
  fill: #909399;
}

.chart-legend {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s, opacity 0.2s;
  opacity: 0.5;
}

.legend-item.active {
  opacity: 1;
  background: #f5f7fa;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.legend-text {
  font-size: 13px;
  color: #606266;
}

.legend-value {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-left: 4px;
}

.point-actions {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}

.table-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 16px 0 12px;
}

.points-table {
  margin-top: 8px;
}

@media (max-width: 768px) {
  .chart-y-axis-left,
  .chart-y-axis-right {
    width: 70px;
  }
  
  .y-label {
    font-size: 10px;
  }
  
  .chart-legend {
    gap: 12px;
  }
  
  .legend-item {
    gap: 4px;
  }
}
</style>
