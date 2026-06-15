<template>
  <div class="tasting-radar-panel">
    <div class="panel-header">
      <h3 class="panel-title">品鉴雷达分析</h3>
      <div class="tasting-selector" v-if="tastingNotes.length > 1">
        <el-select v-model="selectedIndex" size="small" style="width: 180px">
          <el-option 
            v-for="(note, idx) in tastingNotes" 
            :key="note.id" 
            :label="`${formatDate(note.tastingDate)} - ${note.brewingMethod || '未指定'}`"
            :value="idx"
          />
        </el-select>
      </div>
    </div>

    <el-empty v-if="tastingNotes.length === 0" description="暂无品鉴记录，先添加一条品鉴笔记吧" />

    <div v-else class="radar-content">
      <div class="radar-chart-wrapper">
        <svg :width="chartSize" :height="chartSize" class="radar-svg">
          <g :transform="`translate(${center}, ${center})`">
            <polygon 
              v-for="level in 5" 
              :key="level"
              :points="getPolygonPoints(level * radius / 5)"
              class="radar-grid"
              :class="`level-${level}`"
            />
            
            <line 
              v-for="(axis, idx) in axes" 
              :key="idx"
              x1="0" 
              y1="0" 
              :x2="getAxisPoint(idx, radius).x" 
              :y2="getAxisPoint(idx, radius).y"
              class="radar-axis"
            />
            
            <text 
              v-for="(axis, idx) in axes" 
              :key="`label-${idx}`"
              :x="getAxisPoint(idx, radius + 28).x"
              :y="getAxisPoint(idx, radius + 28).y"
              text-anchor="middle"
              dominant-baseline="middle"
              class="axis-label"
            >
              {{ axis.label }}
            </text>

            <polygon 
              :points="dataPoints"
              class="radar-area"
            />
            
            <circle 
              v-for="(point, idx) in dataPointArray" 
              :key="`point-${idx}`"
              :cx="point.x"
              :cy="point.y"
              r="5"
              class="data-point"
            />

            <text 
              v-for="(point, idx) in dataPointArray" 
              :key="`value-${idx}`"
              :x="point.x"
              :y="point.y - 12"
              text-anchor="middle"
              class="point-value"
            >
              {{ displayValues[idx] }}
            </text>
          </g>
        </svg>

        <div class="overall-score">
          <div class="score-label">综合评分</div>
          <div class="score-value">{{ currentNote?.overallScore || 0 }}</div>
          <div class="score-max">/ 100</div>
        </div>
      </div>

      <div class="impression-section">
        <div class="section-subtitle">品鉴印象</div>
        <div class="impression-card">
          <div class="impression-header">
            <span class="impression-date">{{ formatTime(currentNote?.tastingDate) }}</span>
            <el-tag size="small">{{ currentNote?.brewingMethod || '未指定' }}</el-tag>
          </div>
          <div class="impression-content" v-if="currentNote?.impression">
            {{ currentNote.impression }}
          </div>
          <div class="impression-content empty" v-else>
            暂无印象记录
          </div>
        </div>

        <div class="score-details">
          <div 
            v-for="(axis, idx) in axes" 
            :key="`detail-${idx}`" 
            class="score-detail-item"
          >
            <div class="detail-header">
              <span class="detail-label">{{ axis.label }}</span>
              <div class="detail-score">
                <el-rate v-model="scoreValues[idx]" disabled :max="5" />
                <span class="detail-num">{{ displayValues[idx] }}分</span>
              </div>
            </div>
            <div class="detail-desc" v-if="getScoreDesc(idx)">
              {{ getScoreDesc(idx) }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="history-section" v-if="tastingNotes.length > 1">
      <div class="section-subtitle">历史品鉴对比</div>
      <div class="history-chart">
        <svg :width="historyChartWidth" :height="200" class="history-svg">
          <g :transform="`translate(40, 20)`">
            <line 
              v-for="i in 5" 
              :key="`grid-${i}`"
              :x1="0" 
              :y1="(i - 1) * 35" 
              :x2="historyChartWidth - 80" 
              :y2="(i - 1) * 35"
              class="history-grid"
            />
            <text 
              v-for="i in 6" 
              :key="`ylabel-${i}`"
              x="-8" 
              :y="(5 - (i - 1)) * 35 + 4"
              text-anchor="end"
              class="history-y-label"
            >
              {{ (i - 1) * 20 }}
            </text>
            
            <g v-for="(axis, axisIdx) in axes" :key="`line-${axisIdx}`">
              <path 
                :d="getHistoryLinePath(axis.key)"
                class="history-line"
                :class="`line-${axisIdx}`"
                fill="none"
              />
              <circle 
                v-for="(note, noteIdx) in tastingNotes" 
                :key="`hpoint-${axisIdx}-${noteIdx}`"
                :cx="noteIdx * pointSpacing"
                :cy="getHistoryPointY(note, axis.key)"
                r="4"
                class="history-point"
                :class="`line-${axisIdx}`"
              />
            </g>

            <text 
              v-for="(note, idx) in tastingNotes" 
              :key="`xlabel-${idx}`"
              :x="idx * pointSpacing"
              y="175"
              text-anchor="middle"
              class="history-x-label"
            >
              {{ formatShortDate(note.tastingDate) }}
            </text>
          </g>
        </svg>
        <div class="history-legend">
          <span 
            v-for="(axis, idx) in axes" 
            :key="`legend-${idx}`"
            class="legend-item"
          >
            <span class="legend-line" :class="`line-${idx}`"></span>
            {{ axis.label }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  tastingNotes: {
    type: Array,
    default: () => []
  }
})

const selectedIndex = ref(0)

watch(() => props.tastingNotes, (notes) => {
  if (notes && notes.length > 0 && selectedIndex.value >= notes.length) {
    selectedIndex.value = 0
  }
}, { deep: true })

const currentNote = computed(() => {
  return props.tastingNotes[selectedIndex.value] || null
})

const chartSize = 380
const center = chartSize / 2
const radius = 120

const axes = [
  { key: 'aromaScore', label: '香气', max: 5 },
  { key: 'liquorColorScore', label: '汤色', max: 5 },
  { key: 'tasteScore', label: '滋味', max: 5 },
  { key: 'aftertasteScore', label: '回甘', max: 5 },
  { key: 'overallScore', label: '综合', max: 100 }
]

const scoreValues = computed(() => {
  if (!currentNote.value) return [0, 0, 0, 0, 0]
  return [
    currentNote.value.aromaScore || 0,
    currentNote.value.liquorColorScore || 0,
    currentNote.value.tasteScore || 0,
    currentNote.value.aftertasteScore || 0,
    Math.round((currentNote.value.overallScore || 0) / 20)
  ]
})

const displayValues = computed(() => {
  if (!currentNote.value) return [0, 0, 0, 0, 0]
  return [
    currentNote.value.aromaScore || 0,
    currentNote.value.liquorColorScore || 0,
    currentNote.value.tasteScore || 0,
    currentNote.value.aftertasteScore || 0,
    currentNote.value.overallScore || 0
  ]
})

const dataPointArray = computed(() => {
  return axes.map((axis, idx) => {
    const value = scoreValues.value[idx]
    const maxValue = 5
    const r = (value / maxValue) * radius
    return getAxisPoint(idx, r)
  })
})

const dataPoints = computed(() => {
  return dataPointArray.value.map(p => `${p.x},${p.y}`).join(' ')
})

const historyChartWidth = computed(() => {
  return Math.max(400, props.tastingNotes.length * 80 + 80)
})

const pointSpacing = computed(() => {
  return Math.max(60, (historyChartWidth.value - 80) / Math.max(1, props.tastingNotes.length - 1))
})

function getAxisPoint(index, r) {
  const angle = (Math.PI * 2 * index) / axes.length - Math.PI / 2
  return {
    x: Math.cos(angle) * r,
    y: Math.sin(angle) * r
  }
}

function getPolygonPoints(r) {
  return axes.map((_, idx) => {
    const point = getAxisPoint(idx, r)
    return `${point.x},${point.y}`
  }).join(' ')
}

function getScoreDesc(idx) {
  if (!currentNote.value) return ''
  const descKeys = ['aromaDesc', 'liquorColorDesc', 'tasteDesc', 'aftertasteDesc', '']
  return currentNote.value[descKeys[idx]] || ''
}

function getHistoryLinePath(key) {
  if (props.tastingNotes.length === 0) return ''
  return props.tastingNotes.map((note, idx) => {
    const x = idx * pointSpacing.value
    const y = getHistoryPointY(note, key)
    return `${idx === 0 ? 'M' : 'L'} ${x} ${y}`
  }).join(' ')
}

function getHistoryPointY(note, key) {
  const value = key === 'overallScore' 
    ? (note[key] || 0) 
    : ((note[key] || 0) * 20)
  const maxY = 140
  return maxY - (value / 100) * maxY
}

function formatDate(t) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function formatShortDate(t) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getMonth() + 1}/${d.getDate()}`
}

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}
</script>

<style scoped>
.tasting-radar-panel {
  width: 100%;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: #1b4332;
  margin: 0;
}

.section-subtitle {
  font-size: 15px;
  font-weight: 600;
  color: #2d6a4f;
  margin-bottom: 12px;
}

.radar-content {
  display: flex;
  gap: 24px;
  align-items: flex-start;
  flex-wrap: wrap;
}

.radar-chart-wrapper {
  position: relative;
  flex-shrink: 0;
}

.radar-svg {
  display: block;
}

.radar-grid {
  fill: none;
  stroke: #e9ecef;
  stroke-width: 1;
}

.radar-grid.level-1 { fill: rgba(82, 183, 136, 0.05); }
.radar-grid.level-2 { fill: rgba(82, 183, 136, 0.08); }
.radar-grid.level-3 { fill: rgba(82, 183, 136, 0.1); }
.radar-grid.level-4 { fill: rgba(82, 183, 136, 0.12); }
.radar-grid.level-5 { fill: rgba(82, 183, 136, 0.15); }

.radar-axis {
  stroke: #dee2e6;
  stroke-width: 1;
}

.axis-label {
  font-size: 13px;
  font-weight: 500;
  fill: #495057;
}

.radar-area {
  fill: rgba(82, 183, 136, 0.3);
  stroke: #52b788;
  stroke-width: 2;
  transition: all 0.3s ease;
}

.radar-area:hover {
  fill: rgba(82, 183, 136, 0.4);
}

.data-point {
  fill: #52b788;
  stroke: #fff;
  stroke-width: 2;
  cursor: pointer;
  transition: all 0.2s ease;
}

.data-point:hover {
  fill: #40916c;
  r: 7;
}

.point-value {
  font-size: 11px;
  font-weight: 600;
  fill: #2d6a4f;
}

.overall-score {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  background: rgba(255, 255, 255, 0.9);
  padding: 12px 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.score-label {
  font-size: 12px;
  color: #6c757d;
  margin-bottom: 4px;
}

.score-value {
  font-size: 32px;
  font-weight: 700;
  color: #2d6a4f;
  line-height: 1;
}

.score-max {
  font-size: 14px;
  color: #adb5bd;
  margin-top: 2px;
}

.impression-section {
  flex: 1;
  min-width: 280px;
}

.impression-card {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.impression-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.impression-date {
  font-size: 13px;
  color: #6c757d;
}

.impression-content {
  font-size: 14px;
  line-height: 1.8;
  color: #495057;
  background: #fff;
  padding: 12px;
  border-radius: 8px;
  border-left: 3px solid #52b788;
}

.impression-content.empty {
  color: #adb5bd;
  font-style: italic;
  border-left-color: #dee2e6;
}

.score-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.score-detail-item {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 8px;
  transition: all 0.2s;
}

.score-detail-item:hover {
  background: #e9ecef;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.detail-label {
  font-size: 13px;
  font-weight: 500;
  color: #495057;
  min-width: 40px;
}

.detail-score {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-num {
  font-size: 12px;
  color: #6c757d;
}

.detail-desc {
  font-size: 13px;
  color: #6c757d;
  padding-left: 8px;
  border-left: 2px solid #dee2e6;
}

.history-section {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e9ecef;
}

.history-chart {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 16px;
  overflow-x: auto;
}

.history-svg {
  display: block;
  min-width: 400px;
}

.history-grid {
  stroke: #e9ecef;
  stroke-width: 1;
  stroke-dasharray: 4, 4;
}

.history-y-label,
.history-x-label {
  font-size: 11px;
  fill: #adb5bd;
}

.history-line {
  stroke-width: 2;
  transition: stroke-width 0.2s;
}

.history-line:hover {
  stroke-width: 3;
}

.history-line.line-0 { stroke: #52b788; }
.history-line.line-1 { stroke: #409eff; }
.history-line.line-2 { stroke: #f4a261; }
.history-line.line-3 { stroke: #a66cff; }
.history-line.line-4 { stroke: #e63946; }

.history-point {
  fill: #fff;
  stroke-width: 2;
  transition: all 0.2s;
}

.history-point:hover {
  r: 6;
}

.history-point.line-0 { stroke: #52b788; }
.history-point.line-1 { stroke: #409eff; }
.history-point.line-2 { stroke: #f4a261; }
.history-point.line-3 { stroke: #a66cff; }
.history-point.line-4 { stroke: #e63946; }

.history-legend {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #6c757d;
}

.legend-line {
  width: 20px;
  height: 3px;
  border-radius: 2px;
}

.legend-line.line-0 { background: #52b788; }
.legend-line.line-1 { background: #409eff; }
.legend-line.line-2 { background: #f4a261; }
.legend-line.line-3 { background: #a66cff; }
.legend-line.line-4 { background: #e63946; }

@media (max-width: 768px) {
  .radar-content {
    flex-direction: column;
    align-items: center;
  }
  
  .impression-section {
    width: 100%;
  }
}
</style>
