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
        </div>
      </div>

      <el-divider />

      <div class="summary-cards">
        <div class="summary-card stock-summary-card" :class="getStockSummaryClass()">
          <div class="summary-icon">📦</div>
          <div class="summary-content">
            <div class="summary-label">
              库存
              <el-tooltip v-if="tea.lastStorageDate" :content="`最近仓储: ${formatTime(tea.lastStorageDate)}`">
                <el-icon class="info-icon"><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
            <div class="summary-value">
              {{ tea.currentStock || 0 }} {{ tea.stockUnit || '克' }}
            </div>
            <div class="summary-sub" v-if="tea.lastStorageDate">
              最近仓储: {{ formatShortDate(tea.lastStorageDate) }}
            </div>
            <div class="summary-sub" v-else>
              暂无仓储记录
            </div>
          </div>
        </div>
        <div class="summary-card">
          <div class="summary-icon">📅</div>
          <div class="summary-content">
            <div class="summary-label">年份</div>
            <div class="summary-value">{{ tea.harvestYear || '未记录' }}</div>
          </div>
        </div>
        <div class="summary-card" v-if="defaultBrewing">
          <div class="summary-icon">☕</div>
          <div class="summary-content">
            <div class="summary-label">默认水温</div>
            <div class="summary-value">{{ defaultBrewing.waterTemperature }}℃</div>
          </div>
        </div>
        <div class="summary-card" v-if="defaultBrewing">
          <div class="summary-icon">🍃</div>
          <div class="summary-content">
            <div class="summary-label">默认投茶量</div>
            <div class="summary-value">{{ defaultBrewing.teaAmount }}克</div>
          </div>
        </div>
        <div class="summary-card" v-if="defaultBrewing">
          <div class="summary-icon">⏱️</div>
          <div class="summary-content">
            <div class="summary-label">默认冲泡方案</div>
            <div class="summary-value">{{ defaultBrewing.paramName || '默认方案' }}</div>
          </div>
        </div>
      </div>

      <div class="stock-trend-card" v-if="tea.stockTrend && tea.stockTrend.length > 0">
        <div class="stock-trend-card-header">
          <h3 class="stock-trend-title">
            <el-icon><TrendCharts /></el-icon>
            库存变化趋势
          </h3>
          <div class="stock-trend-summary">
            <span class="trend-summary-item">
              初始: <strong>{{ getFirstStock() }}</strong> {{ tea.stockUnit || '克' }}
            </span>
            <span class="trend-summary-divider">→</span>
            <span class="trend-summary-item">
              当前: <strong>{{ tea.currentStock || 0 }}</strong> {{ tea.stockUnit || '克' }}
            </span>
            <span class="trend-summary-item" :class="getTotalChangeClass()">
              ({{ getTotalChangeText() }})
            </span>
          </div>
        </div>
        <div class="stock-trend-chart-wrapper">
          <StockTrendMini :trend="tea.stockTrend" :width="700" :height="120" />
        </div>
        <div class="stock-trend-stats">
          <div class="stat-item">
            <div class="stat-label">最大存量</div>
            <div class="stat-value">{{ getMaxStock() }} {{ tea.stockUnit || '克' }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">最小存量</div>
            <div class="stat-value">{{ getMinStock() }} {{ tea.stockUnit || '克' }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">入库次数</div>
            <div class="stat-value stock-increase">{{ getInCount() }}次</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">出库次数</div>
            <div class="stat-value stock-decrease">{{ getOutCount() }}次</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">累计入库</div>
            <div class="stat-value stock-increase">+{{ getTotalIn() }} {{ tea.stockUnit || '克' }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">累计出库</div>
            <div class="stat-value stock-decrease">-{{ getTotalOut() }} {{ tea.stockUnit || '克' }}</div>
          </div>
        </div>
      </div>
    </el-card>

    <el-tabs v-model="activeTab" class="detail-tabs">
      <el-tab-pane label="基础档案" name="basic">
        <div class="tab-header">
          <span class="section-title">基础档案信息</span>
        </div>
        <el-descriptions :column="2" border class="basic-desc">
          <el-descriptions-item label="茶品名称">{{ tea.name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="茶类">{{ tea.teaCategory || '-' }}</el-descriptions-item>
          <el-descriptions-item label="产区">{{ tea.originRegion || '-' }}</el-descriptions-item>
          <el-descriptions-item label="采摘年份">{{ tea.harvestYear || '-' }}</el-descriptions-item>
          <el-descriptions-item label="储存方式">{{ tea.storageMethod || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="现有存量">
            <span :class="getStockTextClass()">{{ tea.currentStock || 0 }} {{ tea.stockUnit || '克' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="最近仓储记录">{{ tea.lastStorageDate ? formatTime(tea.lastStorageDate) : '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(tea.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatTime(tea.updatedAt) }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ tea.description || '暂无描述' }}</el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>

      <el-tab-pane label="冲泡参数" name="brewing">
        <div class="tab-header">
          <span class="section-title">冲泡参数设置</span>
          <div class="tab-header-actions">
            <el-tooltip content="红色表示偏高，蓝色表示偏低，灰色表示与模板一致">
              <el-button size="small" plain :icon="InfoFilled">差异说明</el-button>
            </el-tooltip>
            <el-button type="primary" size="small" @click="openBrewingDialog()">添加参数</el-button>
          </div>
        </div>
        <div v-if="templateVersionInfo" class="template-version-bar">
          <span class="version-label">📋 {{ tea.teaCategory }}模板版本</span>
          <span class="version-tag">v{{ templateVersionInfo.version }}</span>
          <span class="version-source">来源：{{ templateVersionInfo.paramSource }}</span>
          <span class="version-time">更新：{{ formatTime(templateVersionInfo.updatedAt) }}</span>
        </div>
        <div v-for="(group, method) in groupedBrewingParams" :key="method" class="brewing-method-group">
          <div class="group-header">
            <span class="group-title">
              <span>☕</span>
              {{ method || '未指定冲泡方法' }}
            </span>
            <el-tag v-if="getDefaultByMethod(method)" type="success" size="small">
              已有默认方案
            </el-tag>
          </div>
          <el-row :gutter="16">
            <el-col v-for="param in group" :key="param.id" :xs="24" :sm="12" :md="8">
              <el-card class="param-card" shadow="hover" :class="{ 'param-card-default': param.isDefault }">
                <div class="param-card-header">
                  <div>
                    <span class="param-name">{{ param.paramName || '冲泡方案' }}</span>
                  </div>
                  <div>
                    <el-tag v-if="param.isDefault" type="success" size="small">默认</el-tag>
                    <el-button size="small" text type="primary" @click="openCurveDialog(param)">查看曲线</el-button>
                    <el-button size="small" text type="primary" @click="openBrewingDialog(param)">编辑</el-button>
                    <el-button size="small" text type="danger" @click="handleDeleteBrewing(param.id)">删除</el-button>
                  </div>
                </div>
                <div class="param-grid">
                  <div class="param-item" :class="getDeviationClass(param, 'waterTemperature')">
                    <span class="param-label">水温</span>
                    <span class="param-value">
                      {{ param.waterTemperature }}℃
                      <span class="deviation-info" v-if="param.deviations?.waterTemperature?.deviates">
                        (模板:{{ param.deviations.waterTemperature.templateValue }}℃
                        <span :class="param.deviations.waterTemperature.direction === 'higher' ? 'deviation-higher' : 'deviation-lower'">
                          {{ param.deviations.waterTemperature.direction === 'higher' ? '↑' : '↓' }}
                          {{ Math.abs(param.deviations.waterTemperature.deviationPercent) }}%
                        </span>)
                      </span>
                    </span>
                  </div>
                  <div class="param-item" :class="getDeviationClass(param, 'teaAmount')">
                    <span class="param-label">投茶量</span>
                    <span class="param-value">
                      {{ param.teaAmount }}克
                      <span class="deviation-info" v-if="param.deviations?.teaAmount?.deviates">
                        (模板:{{ param.deviations.teaAmount.templateValue }}克
                        <span :class="param.deviations.teaAmount.direction === 'higher' ? 'deviation-higher' : 'deviation-lower'">
                          {{ param.deviations.teaAmount.direction === 'higher' ? '↑' : '↓' }}
                          {{ Math.abs(param.deviations.teaAmount.deviationPercent) }}%
                        </span>)
                      </span>
                    </span>
                  </div>
                  <div class="param-item">
                    <span class="param-label">茶水比</span>
                    <span class="param-value">{{ param.teaRatio || '-' }}</span>
                  </div>
                  <div class="param-item" :class="getDeviationClass(param, 'waterAmount')">
                    <span class="param-label">注水量</span>
                    <span class="param-value">
                      {{ param.waterAmount || '-' }}ml
                      <span class="deviation-info" v-if="param.deviations?.waterAmount?.deviates">
                        (模板:{{ param.deviations.waterAmount.templateValue }}ml
                        <span :class="param.deviations.waterAmount.direction === 'higher' ? 'deviation-higher' : 'deviation-lower'">
                          {{ param.deviations.waterAmount.direction === 'higher' ? '↑' : '↓' }}
                          {{ Math.abs(param.deviations.waterAmount.deviationPercent) }}%
                        </span>)
                      </span>
                    </span>
                  </div>
                </div>
                <el-divider style="margin:12px 0" />
                <div class="infusion-chart">
                  <div class="infusion-bar-group">
                    <div class="infusion-bar" v-for="(item, idx) in getInfusionDataWithDeviation(param)" :key="idx"
                      :class="item.deviationClass"
                      :style="{ height: Math.max(10, item.value / 2) + 'px' }">
                      <span class="bar-label">{{ item.value }}s</span>
                    </div>
                  </div>
                  <div class="infusion-labels">
                    <span v-for="n in Math.min(4, param.totalInfusions || 4)" :key="n">第{{ n }}泡</span>
                    <span v-if="(param.totalInfusions || 4) > 4">+</span>
                  </div>
                  <div class="infusion-template-ref" v-if="getInfusionTemplateValues(param).length > 0">
                    <el-text size="small" type="info">模板参考: {{ getInfusionTemplateValues(param).join('s / ') }}s</el-text>
                  </div>
                </div>
                <div v-if="param.notes" class="param-notes">
                  <el-text type="info" size="small">{{ param.notes }}</el-text>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
        <el-empty v-if="brewingParams.length === 0" description="暂无冲泡参数，快来添加吧" />
      </el-tab-pane>

      <el-tab-pane label="仓储记录" name="storage">
        <div class="tab-header">
          <span class="section-title">仓储记录</span>
          <el-button type="primary" size="small" @click="openStorageDialog()">添加记录</el-button>
        </div>

        <el-card class="environment-card" shadow="never" v-if="storageRecords.length > 0">
          <StorageEnvironmentPanel 
            :storage-records="storageRecords"
            :tea-category="tea.teaCategory"
            :stock-unit="tea.stockUnit || '克'"
          />
        </el-card>

        <div class="tab-header" style="margin-top: 20px;">
          <span class="section-subtitle">详细记录</span>
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

      <el-tab-pane label="品鉴笔记" name="tasting">
        <div class="tab-header">
          <span class="section-title">品鉴笔记</span>
          <el-button type="primary" size="small" @click="openTastingDialog()">添加记录</el-button>
        </div>

        <el-card class="radar-card" shadow="never" v-if="tastingNotes.length > 0">
          <TastingRadarChart :tasting-notes="tastingNotes" />
        </el-card>

        <div class="tab-header" style="margin-top: 20px;">
          <span class="section-subtitle">品鉴记录列表</span>
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

      <el-tab-pane label="冲泡记录" name="session">
        <div class="tab-header">
          <span class="section-title">单次冲泡记录</span>
          <el-button type="primary" size="small" @click="openSessionDialog()">添加记录</el-button>
        </div>
        <el-row :gutter="16">
          <el-col v-for="session in brewingSessions" :key="session.id" :xs="24" :sm="12">
            <el-card class="session-card" shadow="hover">
              <div class="session-card-header">
                <span class="session-date">{{ formatTime(session.sessionDate) }}</span>
                <div>
                  <el-tag v-if="session.brewingParamName" size="small" type="info">{{ session.brewingParamName }}</el-tag>
                  <el-button size="small" text type="primary" @click="openSessionDialog(session)">编辑</el-button>
                  <el-button size="small" text type="danger" @click="handleDeleteSession(session.id)">删除</el-button>
                </div>
              </div>
              <div class="session-grid">
                <div class="session-item">
                  <span class="session-label">实际水温</span>
                  <span class="session-value">{{ session.actualWaterTemperature != null ? session.actualWaterTemperature + '℃' : '-' }}</span>
                </div>
                <div class="session-item">
                  <span class="session-label">第一泡</span>
                  <span class="session-value">{{ session.firstInfusionTime != null ? session.firstInfusionTime + 's' : '-' }}</span>
                </div>
                <div class="session-item">
                  <span class="session-label">第二泡</span>
                  <span class="session-value">{{ session.secondInfusionTime != null ? session.secondInfusionTime + 's' : '-' }}</span>
                </div>
                <div class="session-item">
                  <span class="session-label">第三泡</span>
                  <span class="session-value">{{ session.thirdInfusionTime != null ? session.thirdInfusionTime + 's' : '-' }}</span>
                </div>
                <div class="session-item">
                  <span class="session-label">后续泡</span>
                  <span class="session-value">{{ session.subsequentInfusionTime != null ? session.subsequentInfusionTime + 's' : '-' }}</span>
                </div>
              </div>
              <div v-if="session.tasteImpression" class="session-impression">
                {{ session.tasteImpression }}
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="brewingSessions.length === 0" description="暂无冲泡记录，记录你的每次冲泡体验吧" />
      </el-tab-pane>

      <el-tab-pane label="陈化时间轴" name="aging">
        <div class="tab-header">
          <span class="section-title">年份陈化时间轴</span>
        </div>
        <AgingTimeline :tea-id="teaId" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="brewingDialogVisible" :title="editingBrewing ? '编辑冲泡参数' : '添加冲泡参数'" width="700px" destroy-on-close>
      <el-form :model="brewingForm" label-width="100px">
        <div class="quick-template" v-if="!editingBrewing">
          <span class="template-label">快速填充模板：</span>
          <el-button v-for="c in TEA_CATEGORIES" :key="c" size="small" @click="fillBrewingTemplate(c)" plain>{{ c }}</el-button>
        </div>
        <el-form-item label="参数名称">
          <el-input v-model="brewingForm.paramName" placeholder="如：日常冲泡" />
        </el-form-item>
        <el-form-item label="冲泡方法">
          <el-select v-model="brewingForm.brewingMethod" placeholder="请选择冲泡方法" style="width:100%">
            <el-option v-for="m in BREWING_METHODS" :key="m" :label="m" :value="m" />
          </el-select>
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

    <el-dialog v-model="curveDialogVisible" :title="`${editingCurveParam?.paramName || '冲泡方案'} - 冲泡曲线`" width="900px" destroy-on-close>
      <BrewingCurveEditor 
        v-if="curveEditingParam"
        :brewing-param="curveEditingParam"
        :editable="true"
        :auto-sync="false"
        @update:brewing-param="curveEditingParam = $event"
        @curve-change="handleCurveChange"
      />
      <template #footer>
        <el-button @click="closeCurveDialog">取消</el-button>
        <el-button type="primary" @click="handleSaveCurve" :loading="saving">保存曲线</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="storageDialogVisible" :title="editingStorage ? '编辑仓储记录' : '添加仓储记录'" width="650px" destroy-on-close>
      <el-form :model="storageForm" label-width="100px">
        <el-alert
          v-if="tea.storageMethod"
          :title="`当前储存方式：${tea.storageMethod}`"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 16px;"
        >
          <template v-if="getStorageGuidelines()" #default>
            <div style="margin-top: 8px;">
              <el-text size="small">{{ getStorageGuidelines().sealTip }}</el-text>
            </div>
          </template>
        </el-alert>
        <el-form-item label="储存位置">
          <el-input v-model="storageForm.storageLocation" placeholder="如：书房储物柜" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="温度(℃)">
              <el-input-number v-model="storageForm.temperature" :precision="1" :step="0.5" style="width:100%" />
              <div v-if="getStorageGuidelines()" class="field-guideline">
                <el-tag :type="getTemperatureStatus() === 'normal' ? 'success' : getTemperatureStatus() === 'warning' ? 'warning' : 'info'" size="small">
                  {{ getStorageGuidelines().tempSuggestion.label }}
                </el-tag>
                <el-text v-if="storageForm.temperature != null" size="small" :type="getTemperatureStatus() === 'normal' ? 'success' : getTemperatureStatus() === 'warning' ? 'warning' : 'info'" style="margin-left: 8px;">
                  {{ getTemperatureStatusText() }}
                </el-text>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="湿度(%)">
              <el-input-number v-model="storageForm.humidity" :precision="1" :step="1" style="width:100%" />
              <div v-if="getStorageGuidelines()" class="field-guideline">
                <el-tag :type="getHumidityStatus() === 'normal' ? 'success' : getHumidityStatus() === 'warning' ? 'warning' : 'info'" size="small">
                  {{ getStorageGuidelines().humiditySuggestion.label }}
                </el-tag>
                <el-text v-if="storageForm.humidity != null" size="small" :type="getHumidityStatus() === 'normal' ? 'success' : getHumidityStatus() === 'warning' ? 'warning' : 'info'" style="margin-left: 8px;">
                  {{ getHumidityStatusText() }}
                </el-text>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="密封状况">
          <el-select v-model="storageForm.sealCondition" style="width:100%">
            <el-option v-for="s in SEAL_CONDITIONS" :key="s" :label="s" :value="s">
              <span>{{ s }}</span>
              <el-tag v-if="getStorageGuidelines() && getStorageGuidelines().sealSuggestion.includes(s)" type="success" size="small" style="margin-left: 8px;">
                推荐
              </el-tag>
            </el-option>
          </el-select>
          <div v-if="getStorageGuidelines()" class="field-guideline">
            <el-text size="small" type="info">
              推荐密封状态：{{ getStorageGuidelines().sealSuggestion.join('、') }}
            </el-text>
          </div>
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
        <el-alert
          v-if="storageForm.stockChange != null"
          :title="getStockPreview()"
          :type="getStockPreviewType()"
          :closable="false"
          show-icon
          style="margin-bottom: 16px;"
        />
        <el-card v-if="getStorageGuidelines() && getStorageGuidelines().tips.length > 0" shadow="never" class="tips-card">
          <template #header>
            <div class="tips-card-header">
              <el-icon><InfoFilled /></el-icon>
              <span>储存小贴士</span>
            </div>
          </template>
          <ul class="tips-list">
            <li v-for="(tip, idx) in getStorageGuidelines().tips" :key="idx">{{ tip }}</li>
          </ul>
        </el-card>
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

    <el-dialog v-model="sessionDialogVisible" :title="editingSession ? '编辑冲泡记录' : '添加冲泡记录'" width="600px" destroy-on-close>
      <el-form :model="sessionForm" label-width="100px">
        <el-form-item label="冲泡方案">
          <el-select v-model="sessionForm.brewingParamId" placeholder="选择关联的冲泡方案（可选）" clearable style="width:100%">
            <el-option v-for="p in brewingParams" :key="p.id" :label="p.paramName || '冲泡方案'" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="实际水温(℃)">
          <el-slider v-model="sessionForm.actualWaterTemperature" :min="50" :max="100" show-input />
        </el-form-item>
        <el-divider>各泡出汤时长(秒)</el-divider>
        <el-row :gutter="16">
          <el-col :span="6"><el-form-item label="第一泡"><el-input-number v-model="sessionForm.firstInfusionTime" :min="1" :max="300" style="width:100%" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="第二泡"><el-input-number v-model="sessionForm.secondInfusionTime" :min="1" :max="300" style="width:100%" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="第三泡"><el-input-number v-model="sessionForm.thirdInfusionTime" :min="1" :max="300" style="width:100%" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="后续泡"><el-input-number v-model="sessionForm.subsequentInfusionTime" :min="1" :max="300" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="口感印象">
          <el-input v-model="sessionForm.tasteImpression" type="textarea" :rows="3" placeholder="记录本次冲泡的口感体验" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sessionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveSession" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, InfoFilled, TrendCharts } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getTeaById, deleteTea,
  getBrewingParams, createBrewingParam, updateBrewingParam, deleteBrewingParam,
  getStorageRecords, createStorageRecord, updateStorageRecord, deleteStorageRecord,
  getTastingNotes, createTastingNote, updateTastingNote, deleteTastingNote,
  getBrewingTemplateByCategory,
  getBrewingSessions, createBrewingSession, updateBrewingSession, deleteBrewingSession,
  getTemplateVersion
} from '../api/tea'
import { SEAL_CONDITIONS, WATER_QUALITY_OPTIONS, BREWING_METHODS, TEA_CATEGORIES, STORAGE_METHOD_GUIDELINES, TEA_STORAGE_CONDITIONS } from '../utils/constants'
import BrewingCurveEditor from '../components/BrewingCurveEditor.vue'
import StorageEnvironmentPanel from '../components/StorageEnvironmentPanel.vue'
import TastingRadarChart from '../components/TastingRadarChart.vue'
import StockTrendMini from '../components/StockTrendMini.vue'
import AgingTimeline from '../components/AgingTimeline.vue'
import { useBrewingCurveStore } from '../store/brewingCurve'

const route = useRoute()
const router = useRouter()
const teaId = route.params.id
const loading = ref(false)
const saving = ref(false)
const activeTab = ref('basic')

const { setCurveData, clearCurveData, getBrewingParamFromCurve } = useBrewingCurveStore()

const curveDialogVisible = ref(false)
const editingCurveParam = ref(null)
const curveEditingParam = ref(null)

const defaultBrewing = computed(() => {
  return brewingParams.value.find(p => p.isDefault) || null
})

const groupedBrewingParams = computed(() => {
  const groups = {}
  brewingParams.value.forEach(param => {
    const method = param.brewingMethod || ''
    if (!groups[method]) {
      groups[method] = []
    }
    groups[method].push(param)
  })
  return groups
})

function getDefaultByMethod(method) {
  const group = groupedBrewingParams.value[method]
  if (!group) return null
  return group.find(p => p.isDefault) || null
}

const tea = ref({})
const brewingParams = ref([])
const storageRecords = ref([])
const tastingNotes = ref([])
const brewingSessions = ref([])
const templateVersionInfo = ref(null)

const brewingDialogVisible = ref(false)
const storageDialogVisible = ref(false)
const tastingDialogVisible = ref(false)
const sessionDialogVisible = ref(false)

const editingBrewing = ref(null)
const editingStorage = ref(null)
const editingTasting = ref(null)
const editingSession = ref(null)

const brewingForm = ref({})
const storageForm = ref({})
const tastingForm = ref({})
const sessionForm = ref({})
const loadingTemplate = ref(false)

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

function formatShortDate(t) {
  if (!t) return '-'
  const date = new Date(t)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function getCategoryTagType(category) {
  const map = { '绿茶': 'success', '红茶': 'danger', '乌龙茶': 'warning', '白茶': 'info', '黄茶': 'warning', '花茶': 'success' }
  return map[category] || ''
}

function getInfusionData(param) {
  return [param.firstInfusionTime, param.secondInfusionTime, param.thirdInfusionTime, param.subsequentInfusionTime].filter(v => v != null)
}

function getDeviationClass(param, field) {
  const dev = param.deviations?.[field]
  if (!dev?.deviates) return ''
  return dev.direction === 'higher' ? 'deviate-higher' : 'deviate-lower'
}

function getInfusionDataWithDeviation(param) {
  const fields = ['firstInfusionTime', 'secondInfusionTime', 'thirdInfusionTime', 'subsequentInfusionTime']
  const values = [param.firstInfusionTime, param.secondInfusionTime, param.thirdInfusionTime, param.subsequentInfusionTime]
  const result = []
  values.forEach((val, idx) => {
    if (val != null) {
      const dev = param.deviations?.[fields[idx]]
      let deviationClass = ''
      if (dev?.deviates) {
        deviationClass = dev.direction === 'higher' ? 'infusion-bar-higher' : 'infusion-bar-lower'
      }
      result.push({ value: val, deviationClass })
    }
  })
  return result
}

function getInfusionTemplateValues(param) {
  const fields = ['firstInfusionTime', 'secondInfusionTime', 'thirdInfusionTime', 'subsequentInfusionTime']
  const values = [param.firstInfusionTime, param.secondInfusionTime, param.thirdInfusionTime, param.subsequentInfusionTime]
  const result = []
  values.forEach((val, idx) => {
    if (val != null && param.deviations?.[fields[idx]]?.templateValue != null) {
      result.push(param.deviations[fields[idx]].templateValue)
    }
  })
  return result
}

function getStockSummaryClass() {
  const stock = Number(tea.value.currentStock) || 0
  if (stock <= 0) return 'stock-summary-empty'
  if (stock < 50) return 'stock-summary-low'
  return 'stock-summary-normal'
}

function getStockTextClass() {
  const stock = Number(tea.value.currentStock) || 0
  if (stock <= 0) return 'stock-empty-text'
  if (stock < 50) return 'stock-low-text'
  return ''
}

function getFirstStock() {
  if (!tea.value.stockTrend || tea.value.stockTrend.length === 0) return 0
  return Number(tea.value.stockTrend[0].stock) || 0
}

function getMaxStock() {
  if (!tea.value.stockTrend || tea.value.stockTrend.length === 0) return 0
  return Math.max(...tea.value.stockTrend.map(p => Number(p.stock) || 0))
}

function getMinStock() {
  if (!tea.value.stockTrend || tea.value.stockTrend.length === 0) return 0
  return Math.min(...tea.value.stockTrend.map(p => Number(p.stock) || 0))
}

function getInCount() {
  if (!tea.value.stockTrend) return 0
  return tea.value.stockTrend.filter(p => Number(p.change) > 0).length
}

function getOutCount() {
  if (!tea.value.stockTrend) return 0
  return tea.value.stockTrend.filter(p => Number(p.change) < 0).length
}

function getTotalIn() {
  if (!tea.value.stockTrend) return 0
  return tea.value.stockTrend
    .filter(p => Number(p.change) > 0)
    .reduce((sum, p) => sum + Number(p.change), 0)
}

function getTotalOut() {
  if (!tea.value.stockTrend) return 0
  return Math.abs(
    tea.value.stockTrend
      .filter(p => Number(p.change) < 0)
      .reduce((sum, p) => sum + Number(p.change), 0)
  )
}

function getTotalChangeClass() {
  const first = getFirstStock()
  const current = Number(tea.value.currentStock) || 0
  if (current > first) return 'stock-increase'
  if (current < first) return 'stock-decrease'
  return ''
}

function getTotalChangeText() {
  const first = getFirstStock()
  const current = Number(tea.value.currentStock) || 0
  const diff = current - first
  if (diff > 0) return `+${diff} ${tea.value.stockUnit || '克'}`
  if (diff < 0) return `${diff} ${tea.value.stockUnit || '克'}`
  return `±0 ${tea.value.stockUnit || '克'}`
}

function getStockPreview() {
  const current = Number(tea.value.currentStock) || 0
  const change = Number(storageForm.value.stockChange) || 0
  const result = current + change
  if (editingStorage.value) {
    const oldChange = Number(editingStorage.value.stockChange) || 0
    const effective = current - oldChange
    const newResult = effective + change
    if (newResult < 0) {
      return `⚠️ 警告：变动会导致负库存！回退原变动后: ${effective}${tea.value.stockUnit || '克'}，新变动: ${change > 0 ? '+' : ''}${change}，结果: ${newResult}${tea.value.stockUnit || '克'}`
    }
    return `回退原变动后库存: ${effective}${tea.value.stockUnit || '克'}，新变动: ${change > 0 ? '+' : ''}${change}，结果: ${newResult}${tea.value.stockUnit || '克'}`
  }
  if (result < 0) {
    return `⚠️ 警告：变动会导致负库存！当前: ${current}${tea.value.stockUnit || '克'}，变动: ${change > 0 ? '+' : ''}${change}，结果: ${result}${tea.value.stockUnit || '克'}`
  }
  return `当前库存: ${current}${tea.value.stockUnit || '克'}，变动: ${change > 0 ? '+' : ''}${change}，变动后: ${result}${tea.value.stockUnit || '克'}`
}

function getStockPreviewType() {
  const current = Number(tea.value.currentStock) || 0
  const change = Number(storageForm.value.stockChange) || 0
  let result
  if (editingStorage.value) {
    const oldChange = Number(editingStorage.value.stockChange) || 0
    result = (current - oldChange) + change
  } else {
    result = current + change
  }
  if (result < 0) return 'error'
  if (result < 50) return 'warning'
  return 'success'
}

function getStorageGuidelines() {
  if (!tea.value.storageMethod) return null
  return STORAGE_METHOD_GUIDELINES[tea.value.storageMethod] || null
}

function getTemperatureStatus() {
  if (storageForm.value.temperature == null) return 'unknown'
  const guidelines = getStorageGuidelines()
  if (!guidelines) return 'unknown'
  const { min, max } = guidelines.tempSuggestion
  const temp = Number(storageForm.value.temperature)
  if (temp >= min && temp <= max) return 'normal'
  if (temp < min - 5 || temp > max + 5) return 'warning'
  return 'warning'
}

function getTemperatureStatusText() {
  if (storageForm.value.temperature == null) return ''
  const guidelines = getStorageGuidelines()
  if (!guidelines) return ''
  const { min, max } = guidelines.tempSuggestion
  const temp = Number(storageForm.value.temperature)
  if (temp >= min && temp <= max) return '✓ 温度适宜'
  if (temp < min) return `⚠ 温度偏低，建议不低于${min}℃`
  return `⚠ 温度偏高，建议不高于${max}℃`
}

function getHumidityStatus() {
  if (storageForm.value.humidity == null) return 'unknown'
  const guidelines = getStorageGuidelines()
  if (!guidelines) return 'unknown'
  const { min, max } = guidelines.humiditySuggestion
  const hum = Number(storageForm.value.humidity)
  if (hum >= min && hum <= max) return 'normal'
  return 'warning'
}

function getHumidityStatusText() {
  if (storageForm.value.humidity == null) return ''
  const guidelines = getStorageGuidelines()
  if (!guidelines) return ''
  const { min, max } = guidelines.humiditySuggestion
  const hum = Number(storageForm.value.humidity)
  if (hum >= min && hum <= max) return '✓ 湿度适宜'
  if (hum < min) return `⚠ 湿度偏低，建议不低于${min}%`
  return `⚠ 湿度偏高，建议不高于${max}%`
}

async function loadTea() {
  loading.value = true
  try {
    const [teaRes, brewingRes, storageRes, tastingRes, sessionRes] = await Promise.all([
      getTeaById(teaId),
      getBrewingParams(teaId),
      getStorageRecords(teaId),
      getTastingNotes(teaId),
      getBrewingSessions(teaId)
    ])
    tea.value = teaRes.data
    brewingParams.value = brewingRes.data || []
    storageRecords.value = storageRes.data || []
    tastingNotes.value = tastingRes.data || []
    brewingSessions.value = sessionRes.data || []

    if (teaRes.data.teaCategory) {
      try {
        const verRes = await getTemplateVersion(teaRes.data.teaCategory)
        templateVersionInfo.value = verRes.data
      } catch {
        templateVersionInfo.value = null
      }
    }
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
    paramName: '', brewingMethod: '盖碗冲泡', waterTemperature: 90, teaAmount: 5, teaRatio: '1:30',
    waterAmount: 150, firstInfusionTime: 15, secondInfusionTime: 20,
    thirdInfusionTime: 25, subsequentInfusionTime: 30, totalInfusions: 5,
    waterQuality: '纯净水', notes: '', isDefault: false
  }
  brewingDialogVisible.value = true
}

async function fillBrewingTemplate(category) {
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

function openCurveDialog(param) {
  editingCurveParam.value = param
  curveEditingParam.value = { ...param }
  setCurveData(curveEditingParam.value)
  curveDialogVisible.value = true
}

function closeCurveDialog() {
  curveDialogVisible.value = false
  editingCurveParam.value = null
  curveEditingParam.value = null
  clearCurveData()
}

function handleCurveChange(change) {
  setCurveData(curveEditingParam.value)
}

async function handleSaveCurve() {
  if (!editingCurveParam.value || !curveEditingParam.value) return
  
  saving.value = true
  try {
    const updateData = {
      ...curveEditingParam.value
    }
    
    await updateBrewingParam(teaId, editingCurveParam.value.id, updateData)
    ElMessage.success('曲线保存成功')
    curveDialogVisible.value = false
    
    const res = await getBrewingParams(teaId)
    brewingParams.value = res.data || []
    
    clearCurveData()
  } finally {
    saving.value = false
  }
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
  const current = Number(tea.value.currentStock) || 0
  const change = Number(storageForm.value.stockChange) || 0
  let resultStock
  let previewText

  if (editingStorage.value) {
    const oldChange = Number(editingStorage.value.stockChange) || 0
    const effective = current - oldChange
    resultStock = effective + change
    previewText = `即将编辑仓储记录：\n\n回退原变动后库存：${effective} ${tea.value.stockUnit || '克'}\n本次变动：${change > 0 ? '+' : ''}${change} ${tea.value.stockUnit || '克'}\n变动后库存：${resultStock} ${tea.value.stockUnit || '克'}`
  } else {
    resultStock = current + change
    previewText = `即将添加仓储记录：\n\n当前库存：${current} ${tea.value.stockUnit || '克'}\n本次变动：${change > 0 ? '+' : ''}${change} ${tea.value.stockUnit || '克'}\n变动后库存：${resultStock} ${tea.value.stockUnit || '克'}`
  }

  if (storageForm.value.temperature != null || storageForm.value.humidity != null || storageForm.value.sealCondition) {
    previewText += '\n\n环境记录：'
    if (storageForm.value.temperature != null) {
      previewText += `\n温度：${storageForm.value.temperature}℃`
    }
    if (storageForm.value.humidity != null) {
      previewText += `\n湿度：${storageForm.value.humidity}%`
    }
    if (storageForm.value.sealCondition) {
      previewText += `\n密封状况：${storageForm.value.sealCondition}`
    }
  }

  if (resultStock < 0) {
    ElMessage.error('库存变动会导致负库存，请调整')
    return
  }

  try {
    await ElMessageBox.confirm(previewText, '确认提交仓储记录', {
      type: resultStock < 50 ? 'warning' : 'info',
      confirmButtonText: '确认提交',
      cancelButtonText: '取消',
      dangerouslyUseHTMLString: false
    })
  } catch {
    return
  }

  saving.value = true
  try {
    if (editingStorage.value) {
      await updateStorageRecord(teaId, editingStorage.value.id, storageForm.value)
    } else {
      await createStorageRecord(teaId, storageForm.value)
    }
    ElMessage.success('保存成功')
    storageDialogVisible.value = false
    const [teaRes, storageRes] = await Promise.all([
      getTeaById(teaId),
      getStorageRecords(teaId)
    ])
    tea.value = teaRes.data
    storageRecords.value = storageRes.data || []
  } finally {
    saving.value = false
  }
}

async function handleDeleteStorage(id) {
  await ElMessageBox.confirm('确定删除此仓储记录？库存将根据记录变动回退', '确认删除', { type: 'warning' })
  await deleteStorageRecord(teaId, id)
  ElMessage.success('删除成功')
  const [teaRes, storageRes] = await Promise.all([
    getTeaById(teaId),
    getStorageRecords(teaId)
  ])
  tea.value = teaRes.data
  storageRecords.value = storageRes.data || []
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

function openSessionDialog(session = null) {
  editingSession.value = session
  sessionForm.value = session ? { ...session } : {
    brewingParamId: null,
    actualWaterTemperature: null,
    firstInfusionTime: null,
    secondInfusionTime: null,
    thirdInfusionTime: null,
    subsequentInfusionTime: null,
    tasteImpression: ''
  }
  sessionDialogVisible.value = true
}

async function handleSaveSession() {
  saving.value = true
  try {
    if (editingSession.value) {
      await updateBrewingSession(teaId, editingSession.value.id, sessionForm.value)
    } else {
      await createBrewingSession(teaId, sessionForm.value)
    }
    ElMessage.success('保存成功')
    sessionDialogVisible.value = false
    const res = await getBrewingSessions(teaId)
    brewingSessions.value = res.data || []
  } finally {
    saving.value = false
  }
}

async function handleDeleteSession(id) {
  await ElMessageBox.confirm('确定删除此冲泡记录？', '确认删除', { type: 'warning' })
  await deleteBrewingSession(teaId, id)
  ElMessage.success('删除成功')
  const res = await getBrewingSessions(teaId)
  brewingSessions.value = res.data || []
}

onMounted(loadTea)

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

.summary-cards {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.summary-card {
  flex: 1;
  min-width: 160px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 12px;
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
}

.summary-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stock-summary-card {
  min-width: 220px;
}

.stock-summary-card.stock-summary-normal {
  background: linear-gradient(135deg, #f0fff4, #d8f3dc);
  border: 1px solid #52b788;
}

.stock-summary-card.stock-summary-low {
  background: linear-gradient(135deg, #fff8e6, #ffecd2);
  border: 1px solid #f4a261;
}

.stock-summary-card.stock-summary-empty {
  background: linear-gradient(135deg, #fff0f0, #ffd6d6);
  border: 1px solid #e63946;
}

.summary-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.summary-content {
  flex: 1;
  min-width: 0;
}

.summary-label {
  font-size: 13px;
  color: #6c757d;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.info-icon {
  font-size: 12px;
  color: #adb5bd;
  cursor: help;
}

.summary-value {
  font-size: 18px;
  font-weight: 700;
  color: #1b4332;
}

.summary-sub {
  font-size: 11px;
  color: #868e96;
  margin-top: 4px;
}

.stock-trend-card {
  margin-top: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa, #ffffff);
  border-radius: 12px;
  border: 1px solid #e9ecef;
}

.stock-trend-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.stock-trend-title {
  font-size: 16px;
  font-weight: 600;
  color: #1b4332;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.stock-trend-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #495057;
  flex-wrap: wrap;
}

.trend-summary-item {
  display: inline-flex;
  align-items: center;
}

.trend-summary-item.stock-increase {
  color: #52b788;
  font-weight: 500;
}

.trend-summary-item.stock-decrease {
  color: #e63946;
  font-weight: 500;
}

.trend-summary-divider {
  color: #adb5bd;
}

.stock-trend-chart-wrapper {
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  margin-bottom: 16px;
  display: flex;
  justify-content: center;
}

.stock-trend-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

.stat-item {
  padding: 12px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  text-align: center;
}

.stat-label {
  font-size: 12px;
  color: #6c757d;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 15px;
  font-weight: 600;
  color: #1b4332;
}

.stat-value.stock-increase {
  color: #52b788;
}

.stat-value.stock-decrease {
  color: #e63946;
}

.stock-empty-text {
  color: #e63946;
  font-weight: 600;
}

.stock-low-text {
  color: #f4a261;
  font-weight: 600;
}

.basic-desc {
  margin-top: 16px;
}

.detail-tabs {
  margin-top: 20px;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.tab-header-actions {
  display: flex;
  gap: 8px;
}

.brewing-method-group {
  margin-bottom: 24px;
}

.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e9ecef;
}

.group-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 600;
  color: #2d6a4f;
}

.param-card {
  margin-bottom: 16px;
}

.param-card-default {
  border: 2px solid #52b788;
  box-shadow: 0 2px 12px rgba(82, 183, 136, 0.2);
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

.brewing-method-tag {
  margin-left: 8px;
  background: #e9ecef;
  color: #495057;
  border: none;
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
  transition: all 0.2s;
}

.param-item.deviate-higher {
  background: linear-gradient(135deg, #fff5f5, #ffe0e0);
  border: 1px solid #f56c6c;
}

.param-item.deviate-lower {
  background: linear-gradient(135deg, #ecf5ff, #d9ecff);
  border: 1px solid #409eff;
}

.param-label {
  font-size: 13px;
  color: #6c757d;
}

.param-value {
  font-size: 14px;
  font-weight: 600;
  color: #2d6a4f;
  text-align: right;
}

.deviation-info {
  font-size: 11px;
  font-weight: normal;
  color: #909399;
  margin-left: 4px;
}

.deviation-higher {
  color: #f56c6c;
  font-weight: 600;
}

.deviation-lower {
  color: #409eff;
  font-weight: 600;
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
  transition: all 0.2s;
}

.infusion-bar-higher {
  background: linear-gradient(180deg, #f56c6c, #c0392b) !important;
  box-shadow: 0 0 8px rgba(245, 108, 108, 0.4);
}

.infusion-bar-lower {
  background: linear-gradient(180deg, #409eff, #1d6fe0) !important;
  box-shadow: 0 0 8px rgba(64, 158, 255, 0.4);
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

.infusion-template-ref {
  margin-top: 8px;
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

.environment-card,
.radar-card {
  margin-bottom: 16px;
  background: linear-gradient(135deg, #f8f9fa, #ffffff);
}

.environment-card :deep(.el-card__body),
.radar-card :deep(.el-card__body) {
  padding: 20px;
}

.section-subtitle {
  font-size: 16px;
  font-weight: 600;
  color: #2d6a4f;
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

.template-version-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #f0f9ff, #e0f2fe);
  border: 1px solid #7dd3fc;
  border-radius: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.version-label {
  font-size: 14px;
  font-weight: 600;
  color: #0369a1;
}

.version-tag {
  display: inline-block;
  padding: 2px 10px;
  background: #0284c7;
  color: #fff;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.version-source {
  font-size: 13px;
  color: #64748b;
}

.version-time {
  font-size: 12px;
  color: #94a3b8;
  margin-left: auto;
}

.session-card {
  margin-bottom: 16px;
}

.session-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.session-date {
  font-size: 14px;
  color: #6c757d;
  font-weight: 500;
}

.session-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 8px;
}

.session-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 10px;
  background: #f8f9fa;
  border-radius: 6px;
}

.session-label {
  font-size: 13px;
  color: #6c757d;
}

.session-value {
  font-size: 14px;
  font-weight: 600;
  color: #2d6a4f;
}

.session-impression {
  margin-top: 10px;
  font-size: 14px;
  color: #495057;
  line-height: 1.6;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 6px;
}

.field-guideline {
  margin-top: 6px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.tips-card {
  margin-bottom: 16px;
  background: linear-gradient(135deg, #f0fdf4, #ecfdf5);
  border: 1px solid #86efac;
}

.tips-card :deep(.el-card__header) {
  padding: 10px 16px;
  background: transparent;
  border-bottom: 1px dashed #86efac;
}

.tips-card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: #166534;
  font-size: 14px;
}

.tips-card :deep(.el-card__body) {
  padding: 12px 16px;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
}

.tips-list li {
  font-size: 13px;
  color: #166534;
  line-height: 1.8;
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
  .stock-trend-summary {
    justify-content: flex-start;
  }
}
</style>
