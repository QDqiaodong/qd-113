<template>
  <div class="storage-environment-panel">
    <div class="panel-header">
      <h3 class="panel-title">茶仓环境监控</h3>
      <div class="legend">
        <div class="legend-item">
          <span class="legend-dot status-good"></span>
          <span>适宜</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot status-warning"></span>
          <span>注意</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot status-danger"></span>
          <span>不适宜</span>
        </div>
      </div>
    </div>

    <el-empty v-if="groupedRecords.length === 0" description="暂无仓储环境数据" />

    <el-row v-else :gutter="16">
      <el-col v-for="group in groupedRecords" :key="group.location" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="location-card" shadow="hover">
          <div class="card-header">
            <div class="location-icon">📍</div>
            <div class="location-info">
              <h4 class="location-name">{{ group.location }}</h4>
              <span class="record-count">{{ group.records.length }}条记录</span>
            </div>
          </div>

          <div class="latest-record" v-if="group.latest">
            <div class="record-time">{{ formatTime(group.latest.recordDate) }}</div>

            <div class="env-metrics">
              <div class="metric-item" :class="getTemperatureStatus(group.latest.temperature)">
                <div class="metric-icon">🌡️</div>
                <div class="metric-content">
                  <span class="metric-label">温度</span>
                  <span class="metric-value">{{ group.latest.temperature }}℃</span>
                  <span class="metric-range">适宜: {{ suitableRange.tempMin }}-{{ suitableRange.tempMax }}℃</span>
                </div>
                <div class="metric-status" :class="getTemperatureStatus(group.latest.temperature)">
                  {{ getStatusText(getTemperatureStatus(group.latest.temperature)) }}
                </div>
              </div>

              <div class="metric-item" :class="getHumidityStatus(group.latest.humidity)">
                <div class="metric-icon">💧</div>
                <div class="metric-content">
                  <span class="metric-label">湿度</span>
                  <span class="metric-value">{{ group.latest.humidity }}%</span>
                  <span class="metric-range">适宜: {{ suitableRange.humidityMin }}-{{ suitableRange.humidityMax }}%</span>
                </div>
                <div class="metric-status" :class="getHumidityStatus(group.latest.humidity)">
                  {{ getStatusText(getHumidityStatus(group.latest.humidity)) }}
                </div>
              </div>

              <div class="metric-item" :class="getSealStatus(group.latest.sealCondition)">
                <div class="metric-icon">🔒</div>
                <div class="metric-content">
                  <span class="metric-label">密封状态</span>
                  <span class="metric-value">{{ group.latest.sealCondition || '未记录' }}</span>
                </div>
                <div class="metric-status" :class="getSealStatus(group.latest.sealCondition)">
                  {{ getStatusText(getSealStatus(group.latest.sealCondition)) }}
                </div>
              </div>

              <div class="metric-item stock-item">
                <div class="metric-icon">📦</div>
                <div class="metric-content">
                  <span class="metric-label">库存变化</span>
                  <span class="metric-value" :class="getStockChangeClass(group.latest.stockChange)">
                    {{ group.latest.stockChange > 0 ? '+' : '' }}{{ group.latest.stockChange }} {{ stockUnit }}
                  </span>
                  <span class="metric-range">当前: {{ group.latest.currentStock }} {{ stockUnit }}</span>
                </div>
                <div class="metric-trend" :class="getStockChangeClass(group.latest.stockChange)">
                  <span v-if="group.latest.stockChange > 0">↑</span>
                  <span v-else-if="group.latest.stockChange < 0">↓</span>
                  <span v-else>-</span>
                </div>
              </div>
            </div>

            <div class="env-trend" v-if="group.records.length > 1">
              <div class="trend-title">近期趋势</div>
              <div class="trend-chart">
                <div class="trend-bars">
                  <div 
                    v-for="(record, idx) in group.records.slice(-6)" 
                    :key="idx"
                    class="trend-bar-group"
                  >
                    <div class="temp-bar" :style="{ height: getTempBarHeight(record.temperature) }">
                      <div class="bar-fill" :class="getTemperatureStatus(record.temperature)"></div>
                    </div>
                    <div class="humidity-bar" :style="{ height: getHumidityBarHeight(record.humidity) }">
                      <div class="bar-fill" :class="getHumidityStatus(record.humidity)"></div>
                    </div>
                    <div class="bar-date">{{ formatShortDate(record.recordDate) }}</div>
                  </div>
                </div>
                <div class="trend-legend">
                  <span class="temp-legend">温度</span>
                  <span class="humidity-legend">湿度</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { TEA_STORAGE_CONDITIONS } from '../utils/constants'

const props = defineProps({
  storageRecords: {
    type: Array,
    default: () => []
  },
  teaCategory: {
    type: String,
    default: ''
  },
  stockUnit: {
    type: String,
    default: '克'
  }
})

const suitableRange = computed(() => {
  return TEA_STORAGE_CONDITIONS[props.teaCategory] || TEA_STORAGE_CONDITIONS['默认']
})

const groupedRecords = computed(() => {
  const groups = {}
  props.storageRecords.forEach(record => {
    const location = record.storageLocation || '未指定位置'
    if (!groups[location]) {
      groups[location] = {
        location,
        records: []
      }
    }
    groups[location].records.push(record)
  })

  return Object.values(groups).map(group => {
    const sortedRecords = [...group.records].sort((a, b) => 
      new Date(a.recordDate) - new Date(b.recordDate)
    )
    return {
      ...group,
      records: sortedRecords,
      latest: sortedRecords[sortedRecords.length - 1]
    }
  }).sort((a, b) => b.latest?.recordDate - a.latest?.recordDate)
})

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

function formatShortDate(t) {
  if (!t) return ''
  const date = new Date(t)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

function getTemperatureStatus(temp) {
  if (temp == null) return 'status-unknown'
  const { tempMin, tempMax } = suitableRange.value
  if (temp >= tempMin && temp <= tempMax) return 'status-good'
  if (temp >= tempMin - 3 && temp <= tempMax + 3) return 'status-warning'
  return 'status-danger'
}

function getHumidityStatus(humidity) {
  if (humidity == null) return 'status-unknown'
  const { humidityMin, humidityMax } = suitableRange.value
  if (humidity >= humidityMin && humidity <= humidityMax) return 'status-good'
  if (humidity >= humidityMin - 5 && humidity <= humidityMax + 5) return 'status-warning'
  return 'status-danger'
}

function getSealStatus(condition) {
  if (!condition) return 'status-unknown'
  const goodSeals = ['完全密封', '重新密封']
  const warningSeals = ['半密封']
  if (goodSeals.includes(condition)) return 'status-good'
  if (warningSeals.includes(condition)) return 'status-warning'
  return 'status-danger'
}

function getStatusText(status) {
  const map = {
    'status-good': '良好',
    'status-warning': '注意',
    'status-danger': '警告',
    'status-unknown': '未知'
  }
  return map[status] || '未知'
}

function getStockChangeClass(change) {
  if (change > 0) return 'stock-increase'
  if (change < 0) return 'stock-decrease'
  return 'stock-unchanged'
}

function getTempBarHeight(temp) {
  if (temp == null) return '10%'
  const min = 0
  const max = 40
  const percent = Math.max(10, Math.min(100, ((temp - min) / (max - min)) * 100))
  return `${percent}%`
}

function getHumidityBarHeight(humidity) {
  if (humidity == null) return '10%'
  const percent = Math.max(10, Math.min(100, humidity))
  return `${percent}%`
}
</script>

<style scoped>
.storage-environment-panel {
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

.legend {
  display: flex;
  gap: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #6c757d;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-dot.status-good {
  background: #52b788;
}

.legend-dot.status-warning {
  background: #f4a261;
}

.legend-dot.status-danger {
  background: #e63946;
}

.location-card {
  margin-bottom: 16px;
}

.location-card :deep(.el-card__body) {
  padding: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e9ecef;
}

.location-icon {
  font-size: 24px;
}

.location-info {
  flex: 1;
}

.location-name {
  font-size: 16px;
  font-weight: 600;
  color: #1b4332;
  margin: 0;
}

.record-count {
  font-size: 12px;
  color: #6c757d;
}

.latest-record {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.record-time {
  font-size: 12px;
  color: #adb5bd;
  text-align: right;
}

.env-metrics {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.metric-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f8f9fa;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.metric-item.status-good {
  background: linear-gradient(135deg, #f0fff4, #d8f3dc);
  border-color: #52b788;
}

.metric-item.status-warning {
  background: linear-gradient(135deg, #fff8e6, #ffecd2);
  border-color: #f4a261;
}

.metric-item.status-danger {
  background: linear-gradient(135deg, #fff0f0, #ffd6d6);
  border-color: #e63946;
}

.metric-item.status-unknown {
  background: #f8f9fa;
  border-color: #dee2e6;
}

.metric-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.metric-content {
  flex: 1;
  min-width: 0;
}

.metric-label {
  display: block;
  font-size: 12px;
  color: #6c757d;
  margin-bottom: 2px;
}

.metric-value {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #1b4332;
}

.metric-range {
  display: block;
  font-size: 11px;
  color: #adb5bd;
  margin-top: 2px;
}

.metric-status {
  font-size: 12px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 10px;
  flex-shrink: 0;
}

.metric-status.status-good {
  background: #52b788;
  color: #fff;
}

.metric-status.status-warning {
  background: #f4a261;
  color: #fff;
}

.metric-status.status-danger {
  background: #e63946;
  color: #fff;
}

.metric-status.status-unknown {
  background: #adb5bd;
  color: #fff;
}

.metric-value.stock-increase {
  color: #52b788;
}

.metric-value.stock-decrease {
  color: #e63946;
}

.metric-trend {
  font-size: 20px;
  font-weight: bold;
}

.metric-trend.stock-increase {
  color: #52b788;
}

.metric-trend.stock-decrease {
  color: #e63946;
}

.metric-trend.stock-unchanged {
  color: #adb5bd;
}

.env-trend {
  margin-top: 8px;
  padding-top: 12px;
  border-top: 1px dashed #dee2e6;
}

.trend-title {
  font-size: 12px;
  color: #6c757d;
  margin-bottom: 8px;
}

.trend-chart {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.trend-bars {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  height: 80px;
  width: 100%;
  justify-content: center;
}

.trend-bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  flex: 1;
  min-width: 24px;
}

.temp-bar,
.humidity-bar {
  width: 10px;
  min-height: 4px;
  display: flex;
  align-items: flex-end;
  border-radius: 2px 2px 0 0;
  overflow: hidden;
}

.temp-bar .bar-fill {
  width: 100%;
  height: 100%;
  border-radius: 2px 2px 0 0;
}

.humidity-bar .bar-fill {
  width: 100%;
  height: 100%;
  border-radius: 2px 2px 0 0;
  opacity: 0.7;
}

.bar-fill.status-good {
  background: #52b788;
}

.bar-fill.status-warning {
  background: #f4a261;
}

.bar-fill.status-danger {
  background: #e63946;
}

.bar-fill.status-unknown {
  background: #dee2e6;
}

.bar-date {
  font-size: 10px;
  color: #adb5bd;
}

.trend-legend {
  display: flex;
  gap: 12px;
  font-size: 11px;
  color: #6c757d;
}

.temp-legend::before {
  content: '';
  display: inline-block;
  width: 8px;
  height: 8px;
  background: #52b788;
  border-radius: 2px;
  margin-right: 4px;
  vertical-align: middle;
}

.humidity-legend::before {
  content: '';
  display: inline-block;
  width: 8px;
  height: 8px;
  background: #adb5bd;
  border-radius: 2px;
  margin-right: 4px;
  vertical-align: middle;
  opacity: 0.7;
}
</style>
