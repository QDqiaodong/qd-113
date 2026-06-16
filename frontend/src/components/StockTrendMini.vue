<template>
  <div class="stock-trend-mini">
    <div v-if="!trend || trend.length === 0" class="empty-trend">
      <span class="empty-text">暂无仓储数据</span>
    </div>
    <div v-else class="trend-content">
      <svg :width="svgWidth" :height="svgHeight" class="trend-svg">
        <defs>
          <linearGradient :id="gradientId" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" :style="{ stopColor: trendColor, stopOpacity: 0.4 }" />
            <stop offset="100%" :style="{ stopColor: trendColor, stopOpacity: 0.05 }" />
          </linearGradient>
        </defs>

        <polyline
          v-if="areaPoints"
          :points="areaPoints"
          :fill="`url(#${gradientId})`"
          stroke="none"
        />

        <polyline
          :points="linePoints"
          fill="none"
          :stroke="trendColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        />

        <circle
          v-for="(p, idx) in chartPoints"
          :key="idx"
          :cx="p.x"
          :cy="p.y"
          r="3"
          :fill="p.change >= 0 ? '#52b788' : '#e63946'"
          stroke="#fff"
          stroke-width="1"
        />
      </svg>

      <div class="trend-axis">
        <span v-if="trend && trend.length > 0" class="axis-start">{{ formatShortDate(trend[0].recordDate) }}</span>
        <span v-if="trend && trend.length > 0" class="axis-end">{{ formatShortDate(trend[trend.length - 1].recordDate) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  trend: {
    type: Array,
    default: () => []
  },
  width: {
    type: Number,
    default: 180
  },
  height: {
    type: Number,
    default: 60
  }
})

const gradientId = computed(() => `stock-gradient-${Math.random().toString(36).substring(2, 11)}`)

const svgWidth = computed(() => props.width)
const svgHeight = computed(() => props.height)

const padding = { top: 8, right: 8, bottom: 4, left: 8 }

const chartPoints = computed(() => {
  if (!props.trend || props.trend.length === 0) return []

  const stocks = props.trend.map(p => Number(p.stock) || 0)
  const minStock = Math.min(...stocks, 0)
  const maxStock = Math.max(...stocks, minStock + 1)

  const chartWidth = svgWidth.value - padding.left - padding.right
  const chartHeight = svgHeight.value - padding.top - padding.bottom

  const range = maxStock - minStock || 1

  return props.trend.map((p, idx) => {
    const x = padding.left + (props.trend.length === 1
      ? chartWidth / 2
      : (idx / (props.trend.length - 1)) * chartWidth)
    const y = padding.top + chartHeight - ((Number(p.stock) - minStock) / range) * chartHeight
    return {
      x,
      y,
      change: Number(p.change) || 0,
      stock: Number(p.stock) || 0
    }
  })
})

const linePoints = computed(() => {
  if (chartPoints.value.length === 0) return ''
  return chartPoints.value.map(p => `${p.x},${p.y}`).join(' ')
})

const areaPoints = computed(() => {
  if (chartPoints.value.length === 0) return ''
  const firstX = chartPoints.value[0].x
  const lastX = chartPoints.value[chartPoints.value.length - 1].x
  const bottom = svgHeight.value - padding.bottom
  return `${firstX},${bottom} ${linePoints.value} ${lastX},${bottom}`
})

const trendColor = computed(() => {
  if (!props.trend || props.trend.length === 0) return '#adb5bd'
  const first = Number(props.trend[0].stock) || 0
  const last = Number(props.trend[props.trend.length - 1].stock) || 0
  if (last > first) return '#52b788'
  if (last < first) return '#e63946'
  return '#409eff'
})

function formatShortDate(t) {
  if (!t) return ''
  const date = new Date(t)
  return `${date.getMonth() + 1}/${date.getDate()}`
}
</script>

<style scoped>
.stock-trend-mini {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

.empty-trend {
  width: 100%;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 6px;
}

.empty-text {
  font-size: 11px;
  color: #adb5bd;
}

.trend-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.trend-svg {
  display: block;
}

.trend-axis {
  display: flex;
  justify-content: space-between;
  padding: 0 4px;
}

.trend-axis span {
  font-size: 10px;
  color: #adb5bd;
}
</style>
