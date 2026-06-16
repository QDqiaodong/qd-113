<template>
  <div id="app">
    <el-container class="app-container">
      <el-header class="app-header">
        <div class="header-left" @click="$router.push('/')">
          <span class="logo-icon">🍵</span>
          <span class="logo-text">茶叶品类与冲泡参数记录本</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          :ellipsis="false"
          class="header-menu"
          router
        >
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            茶叶档案
          </el-menu-item>
          <el-menu-item index="/compare">
            <el-icon><DataAnalysis /></el-icon>
            数据对比
          </el-menu-item>
          <el-menu-item index="/aging-timeline">
            <el-icon><Timer /></el-icon>
            陈化时间轴
          </el-menu-item>
        </el-menu>
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { HomeFilled, DataAnalysis, Timer } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => {
  if (route.path.startsWith('/compare')) return '/compare'
  if (route.path.startsWith('/aging-timeline')) return '/aging-timeline'
  return '/'
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background-color: #f5f0eb;
  min-height: 100vh;
}

.app-container {
  min-height: 100vh;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #2d6a4f 0%, #40916c 50%, #52b788 100%);
  padding: 0 24px;
  height: 64px !important;
  box-shadow: 0 2px 12px rgba(45, 106, 79, 0.3);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  cursor: pointer;
  white-space: nowrap;
}

.logo-icon {
  font-size: 28px;
  margin-right: 10px;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 2px;
}

.header-menu {
  background: transparent !important;
  border: none !important;
}

.header-menu .el-menu-item {
  color: rgba(255, 255, 255, 0.85) !important;
  border-bottom-color: transparent !important;
  font-size: 15px;
  font-weight: 500;
}

.header-menu .el-menu-item:hover {
  color: #fff !important;
  background-color: rgba(255, 255, 255, 0.15) !important;
}

.header-menu .el-menu-item.is-active {
  color: #fff !important;
  border-bottom-color: #fff !important;
  background-color: rgba(255, 255, 255, 0.1) !important;
}

.app-main {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.el-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #2d6a4f;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 20px;
  background: linear-gradient(180deg, #2d6a4f, #52b788);
  border-radius: 2px;
}
</style>
