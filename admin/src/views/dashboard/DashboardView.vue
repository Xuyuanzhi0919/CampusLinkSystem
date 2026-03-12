<template>
  <div class="dashboard">
    <h2 class="page-title">仪表板</h2>

    <!-- 核心指标卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="Math.floor(24 / statCards.length)" v-for="(card, i) in statCards" :key="card.label">
        <div class="stat-card" :style="{ background: card.gradient, animationDelay: `${i * 0.08}s` }">
          <div class="stat-icon">
            <el-icon :size="22"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ loading ? '—' : card.value?.toLocaleString() }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
          <div class="stat-today" v-if="card.today !== undefined">
            今日 +{{ card.today }}
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 待处理提醒 -->
    <el-row :gutter="16" class="alert-row" v-if="!loading && (data?.pendingResources || data?.pendingReports)">
      <el-col :span="12" v-if="data?.pendingResources">
        <el-alert
          :title="`有 ${data.pendingResources} 个资源待审核`"
          type="warning"
          show-icon
          :closable="false"
        >
          <template #default>
            <el-button type="warning" size="small" text @click="$router.push('/content')">
              立即处理 →
            </el-button>
          </template>
        </el-alert>
      </el-col>
      <el-col :span="12" v-if="data?.pendingReports">
        <el-alert
          :title="`有 ${data.pendingReports} 条举报待处理`"
          type="error"
          show-icon
          :closable="false"
        >
          <template #default>
            <el-button type="danger" size="small" text @click="$router.push('/reports')">
              立即处理 →
            </el-button>
          </template>
        </el-alert>
      </el-col>
    </el-row>

    <!-- 趋势图表 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <div class="chart-card">
          <h3 class="chart-title">近7天用户注册趋势</h3>
          <v-chart class="chart" :option="userChartOption" autoresize />
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card">
          <h3 class="chart-title">近7天内容发布趋势</h3>
          <v-chart class="chart" :option="contentChartOption" autoresize />
        </div>
      </el-col>
    </el-row>

    <!-- 底部快捷入口 -->
    <el-row :gutter="16" class="quick-row">
      <el-col :span="6">
        <div class="quick-card" @click="$router.push('/content?status=0')">
          <div class="quick-icon pending">
            <el-icon :size="20"><DocumentChecked /></el-icon>
          </div>
          <div class="quick-info">
            <div class="quick-num">{{ data?.pendingResources ?? '-' }}</div>
            <div class="quick-label">待审核资源</div>
          </div>
          <el-icon class="quick-arrow"><ArrowRight /></el-icon>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="quick-card" @click="$router.push('/reports?status=0')">
          <div class="quick-icon report">
            <el-icon :size="20"><Flag /></el-icon>
          </div>
          <div class="quick-info">
            <div class="quick-num">{{ data?.pendingReports ?? '-' }}</div>
            <div class="quick-label">待处理举报</div>
          </div>
          <el-icon class="quick-arrow"><ArrowRight /></el-icon>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="quick-card" @click="$router.push('/users?status=0')">
          <div class="quick-icon banned">
            <el-icon :size="20"><UserFilled /></el-icon>
          </div>
          <div class="quick-info">
            <div class="quick-num">{{ data?.bannedUsers ?? '-' }}</div>
            <div class="quick-label">封禁用户数</div>
          </div>
          <el-icon class="quick-arrow"><ArrowRight /></el-icon>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="quick-card" @click="$router.push('/activities')">
          <div class="quick-icon activity">
            <el-icon :size="20"><Calendar /></el-icon>
          </div>
          <div class="quick-info">
            <div class="quick-num">{{ data?.totalActivities ?? '-' }}</div>
            <div class="quick-label">活动总数</div>
          </div>
          <el-icon class="quick-arrow"><ArrowRight /></el-icon>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { getDashboard } from '@/api/stats'
import type { DashboardVO } from '@/types'

use([CanvasRenderer, LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent])

const loading = ref(true)
const data = ref<DashboardVO | null>(null)

const statCards = computed(() => {
  if (!data.value) return []
  return [
    { label: '注册用户', value: data.value.totalUsers, today: data.value.todayNewUsers, icon: 'User', gradient: 'linear-gradient(135deg, #6d28d9 0%, #a855f7 100%)' },
    { label: '资源总数', value: data.value.totalResources, today: data.value.todayNewResources, icon: 'Document', gradient: 'linear-gradient(135deg, #1e40af 0%, #3b82f6 100%)' },
    { label: '问题总数', value: data.value.totalQuestions, today: data.value.todayNewQuestions, icon: 'ChatDotRound', gradient: 'linear-gradient(135deg, #047857 0%, #10b981 100%)' },
    { label: '任务总数', value: data.value.totalTasks, today: data.value.todayNewTasks, icon: 'List', gradient: 'linear-gradient(135deg, #b45309 0%, #f59e0b 100%)' },
    { label: '活动总数', value: data.value.totalActivities, today: undefined, icon: 'Calendar', gradient: 'linear-gradient(135deg, #0e7490 0%, #22d3ee 100%)' }
  ]
})

const chartBase = (categories: string[], series: number[], color: string) => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 10, right: 10, bottom: 10, top: 10, containLabel: true },
  xAxis: { type: 'category', data: categories, axisLine: { show: false }, axisTick: { show: false } },
  yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed' } } },
  series: [{
    type: 'line',
    data: series,
    smooth: true,
    lineStyle: { color },
    itemStyle: { color },
    areaStyle: { color: color + '22' }
  }]
})

const userChartOption = computed(() => {
  const trend = data.value?.userTrend || []
  return chartBase(trend.map(t => t.date), trend.map(t => t.count), '#409eff')
})

const contentChartOption = computed(() => {
  const trend = data.value?.contentTrend || []
  return chartBase(trend.map(t => t.date), trend.map(t => t.count), '#67c23a')
})

onMounted(async () => {
  try {
    data.value = await getDashboard()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.dashboard { padding: 0; }
.page-title { font-size: 20px; font-weight: 600; color: #1a1a2e; margin-bottom: 20px; }

.stat-cards { margin-bottom: 20px; }
.stat-card {
  border-radius: 14px;
  padding: 22px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.14);
  position: relative;
  overflow: hidden;
  animation: cp-fade-up 0.4s ease both;
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.2);
}
.stat-card::before {
  content: '';
  position: absolute;
  top: -30px; right: -30px;
  width: 100px; height: 100px;
  background: rgba(255,255,255,0.12);
  border-radius: 50%;
}
.stat-icon {
  width: 48px; height: 48px;
  background: rgba(255,255,255,0.22);
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.stat-value { font-size: 28px; font-weight: 800; color: #fff; font-family: 'Outfit', sans-serif; letter-spacing: -0.5px; }
.stat-label { font-size: 12px; color: rgba(255,255,255,0.75); margin-top: 3px; font-weight: 500; }
.stat-today {
  position: absolute; right: 14px; bottom: 14px;
  font-size: 11px; color: rgba(255,255,255,0.9);
  background: rgba(255,255,255,0.2); padding: 2px 8px; border-radius: 12px;
  font-weight: 600;
}

.alert-row { margin-bottom: 20px; }

.chart-row { margin-bottom: 16px; }
.chart-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.chart-title { font-size: 15px; font-weight: 600; color: #374151; margin-bottom: 16px; }
.chart { height: 220px; }

.quick-row { margin-top: 0; }
.quick-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.quick-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.quick-icon {
  width: 44px; height: 44px;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
}
.quick-icon.pending  { background: linear-gradient(135deg,#fde68a,#fbbf24); color: #78350f; }
.quick-icon.report   { background: linear-gradient(135deg,#fca5a5,#ef4444); color: #fff; }
.quick-icon.banned   { background: linear-gradient(135deg,#c4b5fd,#7c3aed); color: #fff; }
.quick-icon.activity { background: linear-gradient(135deg,#67e8f9,#0891b2); color: #fff; }
.quick-num { font-size: 24px; font-weight: 700; color: #1a1a2e; }
.quick-label { font-size: 12px; color: #9ca3af; margin-top: 2px; }
.quick-arrow { margin-left: auto; color: #d1d5db; }
</style>
