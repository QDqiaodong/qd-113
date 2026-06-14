<template>
  <div class="tea-list-page">
    <div class="page-header">
      <h2 class="section-title">茶叶档案</h2>
      <el-button type="primary" @click="$router.push('/tea/create')">
        <el-icon><Plus /></el-icon>
        新建档案
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-row :gutter="16" align="middle">
        <el-col :xs="24" :sm="8" :md="6">
          <el-input v-model="searchKeyword" placeholder="搜索茶品名称..." clearable @clear="loadTeas" @keyup.enter="handleSearch">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :xs="12" :sm="6" :md="5">
          <el-select v-model="filterCategory" placeholder="按茶类筛选" clearable @change="loadTeas" style="width:100%">
            <el-option v-for="c in TEA_CATEGORIES" :key="c" :label="c" :value="c" />
          </el-select>
        </el-col>
        <el-col :xs="12" :sm="6" :md="5">
          <el-select v-model="filterRegion" placeholder="按产区筛选" clearable @change="loadTeas" style="width:100%">
            <el-option v-for="r in ORIGIN_REGIONS" :key="r" :label="r" :value="r" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="4" :md="4">
          <el-button @click="loadTeas" type="success" plain>刷新</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="16" class="tea-cards" v-loading="loading">
      <el-col v-for="tea in teas" :key="tea.id" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="tea-card" shadow="hover" @click="$router.push(`/tea/${tea.id}`)">
          <div class="tea-card-img">
            <img v-if="tea.imageUrl" :src="tea.imageUrl" :alt="tea.name" />
            <div v-else class="tea-card-placeholder">
              <span class="placeholder-icon">🍵</span>
              <span class="placeholder-cat">{{ tea.teaCategory }}</span>
            </div>
          </div>
          <div class="tea-card-body">
            <h3 class="tea-name">{{ tea.name }}</h3>
            <div class="tea-meta">
              <el-tag size="small" :type="getCategoryTagType(tea.teaCategory)">{{ tea.teaCategory }}</el-tag>
              <span class="tea-region">{{ tea.originRegion }}</span>
            </div>
            <div class="tea-info">
              <span v-if="tea.harvestYear">{{ tea.harvestYear }}年</span>
              <span v-if="tea.currentStock">{{ tea.currentStock }}{{ tea.stockUnit }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="24" v-if="!loading && teas.length === 0">
        <el-empty description="暂无茶叶档案，快来创建第一个吧！" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { getTeaList } from '../api/tea'
import { TEA_CATEGORIES, ORIGIN_REGIONS } from '../utils/constants'

const teas = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const filterCategory = ref('')
const filterRegion = ref('')

async function loadTeas() {
  loading.value = true
  try {
    const params = {}
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterCategory.value) params.category = filterCategory.value
    if (filterRegion.value) params.region = filterRegion.value
    const res = await getTeaList(params)
    teas.value = res.data || []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  loadTeas()
}

function getCategoryTagType(category) {
  const map = {
    '绿茶': 'success',
    '红茶': 'danger',
    '乌龙茶': 'warning',
    '普洱茶': '',
    '白茶': 'info',
    '黑茶': '',
    '黄茶': 'warning',
    '花茶': 'success'
  }
  return map[category] || ''
}

onMounted(loadTeas)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.tea-cards {
  margin-top: 8px;
}

.tea-card {
  cursor: pointer;
  margin-bottom: 16px;
  transition: transform 0.2s;
  overflow: hidden;
}

.tea-card:hover {
  transform: translateY(-4px);
}

.tea-card :deep(.el-card__body) {
  padding: 0;
}

.tea-card-img {
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: linear-gradient(135deg, #d8f3dc, #b7e4c7);
}

.tea-card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.tea-card:hover .tea-card-img img {
  transform: scale(1.05);
}

.tea-card-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.placeholder-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.placeholder-cat {
  color: #2d6a4f;
  font-size: 16px;
  font-weight: 500;
}

.tea-card-body {
  padding: 14px 16px;
}

.tea-name {
  font-size: 16px;
  font-weight: 600;
  color: #1b4332;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tea-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.tea-region {
  font-size: 13px;
  color: #6c757d;
}

.tea-info {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #868e96;
}
</style>
