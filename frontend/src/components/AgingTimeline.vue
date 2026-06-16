<template>
  <div class="aging-timeline">
    <div v-if="timeline" class="timeline-header">
      <div class="timeline-summary">
        <div class="tea-info">
          <h3 class="tea-name">{{ timeline.teaName }}</h3>
          <div class="tea-tags">
            <span class="tag category-tag">{{ timeline.teaCategory }}</span>
            <span v-if="timeline.harvestYear" class="tag year-tag">{{ timeline.harvestYear }}年</span>
            <span v-if="timeline.isAgingRecommended" class="tag aging-recommended">
              <svg class="icon" viewBox="0 0 20 20" fill="currentColor">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
              </svg>
              适合陈化
            </span>
          </div>
        </div>
        <div class="aging-stats">
          <div class="stat-item">
            <div class="stat-value">{{ timeline.currentAgingYears }}<span class="stat-unit">年</span></div>
            <div class="stat-label">陈化时间</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ timeline.totalAgingMonths }}<span class="stat-unit">月</span></div>
            <div class="stat-label">总计月份</div>
          </div>
          <div class="stat-item highlight">
            <div class="stat-value">{{ timeline.agingStatus }}</div>
            <div class="stat-label">当前状态</div>
          </div>
        </div>
      </div>
      <div class="aging-description">
        <p>{{ timeline.agingDescription }}</p>
        <p v-if="timeline.optimalAgingStartYears !== null">
          最佳陈化期：{{ timeline.optimalAgingStartYears }} - {{ timeline.optimalAgingEndYears }} 年
        </p>
      </div>
    </div>

    <div v-if="timeline && timeline.nodes && timeline.nodes.length > 0" class="timeline-content">
      <div class="timeline-legend">
        <div class="legend-item">
          <span class="legend-dot" style="background: #22c55e"></span>
          <span>采摘</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot" style="background: #ef4444"></span>
          <span>里程碑</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot" style="background: #3b82f6"></span>
          <span>转化阶段</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot" style="background: #f59e0b"></span>
          <span>仓储记录</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot" style="background: #8b5cf6"></span>
          <span>品鉴记录</span>
        </div>
      </div>

      <div class="timeline-track">
        <div
          v-for="node in timeline.nodes"
          :key="node.id"
          class="timeline-node"
          :class="{ 
            'highlighted': node.isHighlighted,
            'milestone': node.nodeType === 'MILESTONE',
            'harvest': node.nodeType === 'HARVEST'
          }"
        >
          <div class="node-connector">
            <div class="node-dot" :style="{ background: node.color }">
              <svg v-if="node.nodeType === 'HARVEST'" class="node-icon" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M6.267 3.455a3.066 3.066 0 001.745-.723 3.066 3.066 0 013.976 0 3.066 3.066 0 001.745.723 3.066 3.066 0 012.812 2.812c.051.643.304 1.254.723 1.745a3.066 3.066 0 010 3.976 3.066 3.066 0 00-.723 1.745 3.066 3.066 0 01-2.812 2.812 3.066 3.066 0 00-1.745.723 3.066 3.066 0 01-3.976 0 3.066 3.066 0 00-1.745-.723 3.066 3.066 0 01-2.812-2.812 3.066 3.066 0 00-.723-1.745 3.066 3.066 0 010-3.976 3.066 3.066 0 00.723-1.745 3.066 3.066 0 012.812-2.812zm7.44 5.252a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
              </svg>
              <svg v-else-if="node.nodeType === 'MILESTONE'" class="node-icon" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd" />
              </svg>
              <svg v-else-if="node.nodeType === 'STAGE'" class="node-icon" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M3 3a1 1 0 000 2v8a2 2 0 002 2h2.586l-1.293 1.293a1 1 0 101.414 1.414L10 15.414l2.293 2.293a1 1 0 001.414-1.414L12.414 15H15a2 2 0 002-2V5a1 1 0 100-2H3zm11.707 4.707a1 1 0 00-1.414-1.414L10 9.586 8.707 8.293a1 1 0 00-1.414 0l-2 2a1 1 0 101.414 1.414L8 10.414l1.293 1.293a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
              </svg>
              <svg v-else-if="node.nodeType === 'STORAGE'" class="node-icon" viewBox="0 0 20 20" fill="currentColor">
                <path d="M5 4a2 2 0 012-2h6a2 2 0 012 2v14l-5-2.5L5 18V4z" />
              </svg>
              <svg v-else-if="node.nodeType === 'TASTING'" class="node-icon" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
              </svg>
            </div>
            <div class="node-line" :style="{ background: node.color }"></div>
          </div>
          <div class="node-content">
            <div class="node-header">
              <span class="node-date">{{ formatDate(node.nodeDate) }}</span>
              <span v-if="node.yearsSinceHarvest !== null" class="node-years">
                陈化{{ node.yearsSinceHarvest }}年{{ node.monthsSinceHarvest % 12 }}月
              </span>
            </div>
            <h4 class="node-title" :style="{ color: node.color }">{{ node.nodeTitle }}</h4>
            <p class="node-description">{{ node.nodeDescription }}</p>
            <div v-if="node.nodeType === 'STORAGE'" class="node-details">
              <span v-if="node.temperature !== null" class="detail-item">
                🌡️ {{ node.temperature }}°C
              </span>
              <span v-if="node.humidity !== null" class="detail-item">
                💧 {{ node.humidity }}%
              </span>
              <span v-if="node.storageLocation" class="detail-item">
                📍 {{ node.storageLocation }}
              </span>
              <span v-if="node.sealCondition" class="detail-item">
                🔒 {{ node.sealCondition }}
              </span>
            </div>
            <div class="node-importance" :class="node.importance?.toLowerCase()">
              {{ getImportanceLabel(node.importance) }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="!loading" class="empty-state">
      <svg class="empty-icon" viewBox="0 0 20 20" fill="currentColor">
        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd" />
      </svg>
      <p>暂无陈化时间轴数据</p>
      <p class="empty-hint">请确保茶叶档案包含采摘年份信息</p>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getAgingTimeline } from '../api/tea'

const props = defineProps({
  teaId: {
    type: [Number, String],
    required: true
  }
})

const timeline = ref(null)
const loading = ref(false)

const loadTimeline = async () => {
  if (!props.teaId) return
  
  loading.value = true
  try {
    const res = await getAgingTimeline(props.teaId)
    if (res.code === 200) {
      timeline.value = res.data
    }
  } catch (err) {
    console.error('加载陈化时间轴失败:', err)
  } finally {
    loading.value = false
  }
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

const getImportanceLabel = (importance) => {
  const labels = {
    'HIGH': '重要',
    'MEDIUM': '一般',
    'LOW': '普通'
  }
  return labels[importance] || ''
}

watch(() => props.teaId, () => {
  loadTimeline()
})

onMounted(() => {
  loadTimeline()
})
</script>

<style scoped>
.aging-timeline {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
}

.timeline-header {
  margin-bottom: 24px;
}

.timeline-summary {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 16px;
}

.tea-info {
  flex: 1;
  min-width: 250px;
}

.tea-name {
  font-size: 24px;
  font-weight: 700;
  color: #92400e;
  margin: 0 0 12px 0;
}

.tea-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.category-tag {
  background: #78350f;
  color: #fef3c7;
}

.year-tag {
  background: #065f46;
  color: #d1fae5;
}

.aging-recommended {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
}

.aging-recommended .icon {
  width: 14px;
  height: 14px;
}

.aging-stats {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.stat-item {
  text-align: center;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.stat-item.highlight {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #92400e;
  line-height: 1;
}

.stat-item.highlight .stat-value {
  color: white;
  font-size: 20px;
}

.stat-unit {
  font-size: 14px;
  font-weight: 500;
  margin-left: 4px;
}

.stat-label {
  font-size: 12px;
  color: #78350f;
  margin-top: 4px;
  opacity: 0.8;
}

.stat-item.highlight .stat-label {
  color: white;
}

.aging-description {
  background: rgba(255, 255, 255, 0.5);
  padding: 12px 16px;
  border-radius: 8px;
  color: #78350f;
  font-size: 14px;
  line-height: 1.6;
}

.aging-description p {
  margin: 0 0 4px 0;
}

.aging-description p:last-child {
  margin-bottom: 0;
  font-weight: 600;
}

.timeline-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 8px;
  margin-bottom: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #78350f;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.timeline-content {
  position: relative;
}

.timeline-track {
  position: relative;
  padding-left: 40px;
}

.timeline-node {
  position: relative;
  padding-bottom: 24px;
  display: flex;
  gap: 16px;
}

.timeline-node:last-child {
  padding-bottom: 0;
}

.node-connector {
  position: relative;
  width: 40px;
  flex-shrink: 0;
}

.node-dot {
  position: absolute;
  left: -8px;
  top: 0;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 2;
}

.timeline-node.highlighted .node-dot {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
}

.node-icon {
  width: 18px;
  height: 18px;
}

.node-line {
  position: absolute;
  left: 9px;
  top: 36px;
  width: 2px;
  height: calc(100% - 36px + 24px);
  opacity: 0.3;
}

.timeline-node:last-child .node-line {
  display: none;
}

.node-content {
  flex: 1;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.timeline-node.highlighted .node-content {
  background: white;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  border-left: 4px solid;
}

.timeline-node.milestone .node-content {
  border-left-color: #ef4444;
}

.timeline-node.harvest .node-content {
  border-left-color: #22c55e;
}

.node-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  flex-wrap: wrap;
  gap: 8px;
}

.node-date {
  font-size: 13px;
  color: #78350f;
  font-weight: 600;
  opacity: 0.8;
}

.node-years {
  font-size: 12px;
  padding: 2px 8px;
  background: #fef3c7;
  color: #92400e;
  border-radius: 12px;
}

.node-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.node-description {
  font-size: 14px;
  color: #451a03;
  line-height: 1.6;
  margin: 0 0 12px 0;
}

.node-details {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
  padding: 8px 12px;
  background: rgba(245, 158, 11, 0.1);
  border-radius: 6px;
}

.detail-item {
  font-size: 13px;
  color: #92400e;
}

.node-importance {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
}

.node-importance.high {
  background: #fef2f2;
  color: #dc2626;
}

.node-importance.medium {
  background: #fff7ed;
  color: #ea580c;
}

.node-importance.low {
  background: #f0fdf4;
  color: #16a34a;
}

.empty-state,
.loading-state {
  text-align: center;
  padding: 40px 20px;
  color: #78350f;
}

.empty-icon {
  width: 48px;
  height: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-hint {
  font-size: 13px;
  opacity: 0.7;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(245, 158, 11, 0.2);
  border-top-color: #f59e0b;
  border-radius: 50%;
  margin: 0 auto 16px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 640px) {
  .aging-timeline {
    padding: 16px;
  }

  .timeline-summary {
    flex-direction: column;
  }

  .aging-stats {
    width: 100%;
    justify-content: space-between;
    gap: 12px;
  }

  .stat-item {
    flex: 1;
    padding: 8px 12px;
    min-width: 80px;
  }

  .stat-value {
    font-size: 22px;
  }

  .timeline-track {
    padding-left: 32px;
  }

  .node-dot {
    width: 30px;
    height: 30px;
    left: -5px;
  }

  .node-line {
    left: 9px;
  }

  .node-icon {
    width: 15px;
    height: 15px;
  }
}
</style>
