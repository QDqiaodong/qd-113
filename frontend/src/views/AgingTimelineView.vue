<template>
  <div class="aging-timeline-page">
    <div class="page-header">
      <h2 class="page-title">
        <svg class="title-icon" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd" />
        </svg>
        年份陈化时间轴
      </h2>
      <p class="page-subtitle">追踪普洱、白茶、黑茶等茶叶的陈化历程与转化节点</p>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="filterCategory" @change="loadTimelines">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="普洱茶">普洱茶</el-radio-button>
        <el-radio-button value="白茶">白茶</el-radio-button>
        <el-radio-button value="黑茶">黑茶</el-radio-button>
        <el-radio-button value="乌龙茶">乌龙茶</el-radio-button>
      </el-radio-group>
      <div class="filter-stats">
        <span v-if="timelines.length > 0">
          共 <strong>{{ timelines.length }}</strong> 款茶叶
          <span v-if="agingRecommendedCount > 0">，其中适合陈化 <strong>{{ agingRecommendedCount }}</strong> 款</span>
        </span>
      </div>
    </div>

    <div v-if="loading" class="loading-wrapper">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="timelines.length > 0" class="timeline-list">
      <div
        v-for="timeline in timelines"
        :key="timeline.teaId"
        class="timeline-card"
        :class="{ 'recommended': timeline.isAgingRecommended }"
        @click="goToDetail(timeline.teaId)"
      >
        <div class="card-header">
          <div class="card-title-section">
            <h3 class="tea-name">{{ timeline.teaName }}</h3>
            <div class="tea-meta">
              <span class="category-badge">{{ timeline.teaCategory }}</span>
              <span v-if="timeline.harvestYear" class="year-badge">{{ timeline.harvestYear }}年</span>
              <span v-if="timeline.storageMethod" class="storage-badge">
                <svg class="meta-icon" viewBox="0 0 20 20" fill="currentColor">
                  <path d="M5 4a2 2 0 012-2h6a2 2 0 012 2v14l-5-2.5L5 18V4z" />
                </svg>
                {{ timeline.storageMethod }}
              </span>
            </div>
          </div>
          <div v-if="timeline.isAgingRecommended" class="recommended-badge">
            <svg class="badge-icon" viewBox="0 0 20 20" fill="currentColor">
              <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
            </svg>
            适合陈化
          </div>
        </div>

        <div class="card-stats">
          <div class="stat">
            <div class="stat-value">{{ timeline.currentAgingYears }}<span class="stat-unit">年</span></div>
            <div class="stat-label">陈化时间</div>
          </div>
          <div class="stat">
            <div class="stat-value">{{ timeline.totalAgingMonths }}<span class="stat-unit">月</span></div>
            <div class="stat-label">总计月份</div>
          </div>
          <div class="stat highlight">
            <div class="stat-value">{{ timeline.agingStatus }}</div>
            <div class="stat-label">当前状态</div>
          </div>
          <div class="stat">
            <div class="stat-value">{{ timeline.nodes?.length || 0 }}<span class="stat-unit">个</span></div>
            <div class="stat-label">时间节点</div>
          </div>
        </div>

        <div class="card-description">
          <p>{{ timeline.agingDescription }}</p>
          <p v-if="timeline.optimalAgingStartYears !== null" class="optimal-range">
            最佳陈化期：{{ timeline.optimalAgingStartYears }} - {{ timeline.optimalAgingEndYears }} 年
          </p>
        </div>

        <div class="card-progress" v-if="timeline.optimalAgingEndYears && timeline.harvestYear">
          <div class="progress-label">
            <span>陈化进度</span>
            <span>{{ getProgressPercent(timeline) }}%</span>
          </div>
          <div class="progress-bar">
            <div
              class="progress-fill"
              :style="{ width: getProgressPercent(timeline) + '%' }"
            ></div>
            <div
              class="progress-marker optimal-start"
              :style="{ left: getOptimalStartPercent(timeline) + '%' }"
              title="最佳陈化期开始"
            ></div>
            <div
              class="progress-marker optimal-end"
              :style="{ left: getOptimalEndPercent(timeline) + '%' }"
              title="最佳陈化期结束"
            ></div>
          </div>
          <div class="progress-legend">
            <span class="legend-item"><span class="legend-dot optimal"></span>最佳陈化期</span>
            <span class="legend-item"><span class="legend-dot current"></span>当前陈化</span>
          </div>
        </div>

        <div class="card-footer">
          <span class="view-detail">
            查看详情
            <svg class="arrow-icon" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" />
            </svg>
          </span>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <svg class="empty-icon" viewBox="0 0 20 20" fill="currentColor">
        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd" />
      </svg>
      <p>暂无符合条件的茶叶档案</p>
      <p class="empty-hint">请先添加茶叶档案，并填写采摘年份信息</p>
      <el-button type="primary" @click="$router.push('/tea/create')">添加茶叶档案</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAgingTimelines } from '../api/tea'

const router = useRouter()
const loading = ref(false)
const filterCategory = ref('')
const timelines = ref([])

const agingRecommendedCount = computed(() => {
  return timelines.value.filter(t => t.isAgingRecommended).length
})

const loadTimelines = async () => {
  loading.value = true
  try {
    const res = await getAgingTimelines(filterCategory.value || undefined)
    if (res.code === 200) {
      timelines.value = res.data || []
    }
  } catch (err) {
    console.error('加载陈化时间轴列表失败:', err)
  } finally {
    loading.value = false
  }
}

const getProgressPercent = (timeline) => {
  if (!timeline.optimalAgingEndYears || !timeline.harvestYear) return 0
  const totalYears = timeline.optimalAgingEndYears + 5
  const progress = Math.min(100, (timeline.currentAgingYears / totalYears) * 100)
  return Math.round(progress)
}

const getOptimalStartPercent = (timeline) => {
  if (!timeline.optimalAgingEndYears || !timeline.optimalAgingStartYears) return 30
  const totalYears = timeline.optimalAgingEndYears + 5
  return Math.round((timeline.optimalAgingStartYears / totalYears) * 100)
}

const getOptimalEndPercent = (timeline) => {
  if (!timeline.optimalAgingEndYears) return 80
  const totalYears = timeline.optimalAgingEndYears + 5
  return Math.round((timeline.optimalAgingEndYears / totalYears) * 100)
}

const goToDetail = (teaId) => {
  router.push(`/tea/${teaId}?tab=aging`)
}

onMounted(() => {
  loadTimelines()
})
</script>

<style scoped>
.aging-timeline-page {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #92400e;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.title-icon {
  width: 36px;
  height: 36px;
  color: #f59e0b;
}

.page-subtitle {
  font-size: 16px;
  color: #78350f;
  margin: 0;
  opacity: 0.8;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 12px;
}

.filter-stats {
  font-size: 14px;
  color: #78350f;
}

.filter-stats strong {
  color: #92400e;
}

.loading-wrapper {
  text-align: center;
  padding: 60px 20px;
  color: #78350f;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 3px solid rgba(245, 158, 11, 0.2);
  border-top-color: #f59e0b;
  border-radius: 50%;
  margin: 0 auto 16px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.timeline-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 24px;
}

.timeline-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.timeline-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.timeline-card.recommended {
  border-color: #f59e0b;
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.card-title-section {
  flex: 1;
}

.tea-name {
  font-size: 20px;
  font-weight: 700;
  color: #92400e;
  margin: 0 0 8px 0;
}

.tea-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-badge,
.year-badge,
.storage-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.category-badge {
  background: #78350f;
  color: #fef3c7;
}

.year-badge {
  background: #065f46;
  color: #d1fae5;
}

.storage-badge {
  background: #1e40af;
  color: #dbeafe;
}

.meta-icon {
  width: 12px;
  height: 12px;
}

.recommended-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.badge-icon {
  width: 14px;
  height: 14px;
}

.card-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
  padding: 16px;
  background: rgba(254, 243, 199, 0.5);
  border-radius: 12px;
}

.timeline-card.recommended .card-stats {
  background: rgba(255, 255, 255, 0.6);
}

.stat {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #92400e;
  line-height: 1;
}

.stat.highlight .stat-value {
  font-size: 18px;
  color: #d97706;
}

.stat-unit {
  font-size: 12px;
  font-weight: 500;
  margin-left: 2px;
}

.stat-label {
  font-size: 11px;
  color: #78350f;
  margin-top: 4px;
  opacity: 0.8;
}

.card-description {
  font-size: 13px;
  color: #451a03;
  line-height: 1.6;
  margin-bottom: 16px;
}

.card-description p {
  margin: 0 0 4px 0;
}

.optimal-range {
  font-weight: 600;
  color: #92400e;
}

.card-progress {
  margin-bottom: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 12px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #78350f;
  margin-bottom: 8px;
  font-weight: 600;
}

.progress-bar {
  position: relative;
  height: 12px;
  background: #e5e7eb;
  border-radius: 6px;
  overflow: visible;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #f59e0b 0%, #d97706 100%);
  border-radius: 6px;
  transition: width 0.5s ease;
}

.progress-marker {
  position: absolute;
  top: -4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  transform: translateX(-50%);
  z-index: 2;
}

.progress-marker.optimal-start {
  background: #10b981;
  border: 2px solid white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.progress-marker.optimal-end {
  background: #ef4444;
  border: 2px solid white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.progress-legend {
  display: flex;
  gap: 16px;
  font-size: 11px;
  color: #78350f;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.legend-dot.optimal {
  background: linear-gradient(90deg, #10b981 0%, #ef4444 100%);
}

.legend-dot.current {
  background: #f59e0b;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
  border-top: 1px solid rgba(120, 53, 15, 0.1);
}

.view-detail {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 600;
  color: #d97706;
  transition: gap 0.2s ease;
}

.timeline-card:hover .view-detail {
  gap: 8px;
}

.arrow-icon {
  width: 16px;
  height: 16px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #78350f;
}

.empty-icon {
  width: 64px;
  height: 64px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-hint {
  font-size: 14px;
  opacity: 0.7;
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .aging-timeline-page {
    padding: 16px;
  }

  .page-title {
    font-size: 24px;
  }

  .timeline-list {
    grid-template-columns: 1fr;
  }

  .card-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
