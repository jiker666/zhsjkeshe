<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ title }}</h1>
        <div class="page-subtitle">{{ subtitle }}</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="$router.push('/tasks/new')">新建测试任务</el-button>
    </div>

    <div class="stats-grid">
      <div v-for="item in statCards" :key="item.label" class="stat-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.hint }}</small>
      </div>
    </div>

    <div class="charts-grid">
      <section class="panel">
        <div class="section-title">风险等级分布</div>
        <div ref="riskChartRef" class="chart"></div>
      </section>
      <section class="panel">
        <div class="section-title">最近 7 天任务数量</div>
        <div ref="trendChartRef" class="chart"></div>
      </section>
      <section class="panel">
        <div class="section-title">测试类型分布</div>
        <div ref="typeChartRef" class="chart"></div>
      </section>
      <section class="panel">
        <div class="section-title">任务状态分布</div>
        <div ref="statusChartRef" class="chart"></div>
      </section>
    </div>

    <div class="content-grid">
      <section class="panel">
        <div class="section-title">最近任务</div>
        <el-table :data="stats.recentTasks" height="300" empty-text="暂无任务">
          <el-table-column prop="taskName" label="任务名称" min-width="150" />
          <el-table-column prop="testType" label="测试类型" min-width="150" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="statusTag(row.status)" effect="dark">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="110">
            <template #default="{ row }">
              <el-button size="small" link type="primary" @click="$router.push(`/tasks/${row.id}`)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="panel">
        <div class="section-title">高危漏洞 Top</div>
        <div v-if="stats.highRiskTop.length" class="risk-list">
          <div v-for="item in stats.highRiskTop" :key="item.id" class="risk-item">
            <div>
              <strong>{{ item.vulnerabilityName }}</strong>
              <p>{{ item.description }}</p>
            </div>
            <el-tag type="danger" effect="dark">HIGH</el-tag>
          </div>
        </div>
        <el-empty v-else description="暂无高危漏洞" />
      </section>

      <section class="panel agent-panel">
        <div class="section-title">Agent 工作状态</div>
        <div class="agent-steps">
          <div v-for="step in agentSteps" :key="step" class="agent-step">
            <span class="dot"></span>
            <span>{{ step }}</span>
          </div>
        </div>
      </section>

      <section class="panel">
        <div class="section-title">风险评分 Top 5</div>
        <div v-if="stats.riskScoreTop.length" class="score-list">
          <div v-for="item in stats.riskScoreTop" :key="item.id" class="score-item">
            <div>
              <strong>{{ item.vulnerabilityName }}</strong>
              <p>{{ item.riskVector }}</p>
            </div>
            <span :class="{ danger: item.riskScore >= 7 }">{{ item.riskScore }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无评分数据" />
      </section>

      <section class="panel">
        <div class="section-title">整改闭环状态</div>
        <div class="ticket-bars">
          <div v-for="(value, key) in stats.ticketStatusDistribution" :key="key" class="ticket-row">
            <span>{{ ticketText(key) }}</span>
            <el-progress :percentage="ticketPercent(value)" :show-text="false" />
            <strong>{{ value }}</strong>
          </div>
        </div>
      </section>

      <section v-if="isAdmin" class="panel">
        <div class="section-title">最近审计日志</div>
        <div class="audit-list">
          <div v-for="item in stats.recentAuditLogs" :key="item.id" class="audit-item">
            <span>{{ item.module }}</span>
            <strong>{{ item.action }}</strong>
            <small>{{ item.username }} · {{ item.result }}</small>
          </div>
        </div>
        <el-empty v-if="!stats.recentAuditLogs.length" description="暂无审计日志" />
      </section>

      <section class="panel">
        <div class="section-title">{{ isAdmin ? '靶场积分 Top 5' : '最近挑战记录' }}</div>
        <div v-if="isAdmin && stats.scoreTop.length" class="audit-list">
          <div v-for="item in stats.scoreTop" :key="item.id" class="audit-item">
            <span>{{ item.username }}</span>
            <strong>{{ item.totalScore }} 分</strong>
            <small>完成 {{ item.solvedCount }} 题 · 正确率 {{ item.correctRate }}%</small>
          </div>
        </div>
        <div v-if="!isAdmin && stats.recentChallengeSubmissions.length" class="audit-list">
          <div v-for="item in stats.recentChallengeSubmissions" :key="item.id" class="audit-item">
            <span>题目 {{ item.challengeId }}</span>
            <strong>{{ item.correct === 1 ? '挑战成功' : '继续尝试' }}</strong>
            <small>得分 {{ item.scoreGot }} · {{ item.submitTime }}</small>
          </div>
        </div>
        <el-empty v-if="(isAdmin && !stats.scoreTop.length) || (!isAdmin && !stats.recentChallengeSubmissions.length)" description="暂无靶场数据" />
      </section>
    </div>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { getDashboardStats } from '../api'

defineProps({
  title: { type: String, default: '安全态势仪表盘' },
  subtitle: { type: String, default: '任务、风险、Agent 执行状态一屏掌握' }
})

const riskChartRef = ref()
const trendChartRef = ref()
const typeChartRef = ref()
const statusChartRef = ref()
const charts = []
const agentSteps = ['任务理解', '计划生成', '自动执行', '结果分析', '报告生成']
const stats = reactive({
  totalTasks: 0,
  runningTasks: 0,
  completedTasks: 0,
  totalVulnerabilities: 0,
  highRiskCount: 0,
  openVulnerabilities: 0,
  todayTests: 0,
  totalUsers: 0,
  pendingTickets: 0,
  closedTickets: 0,
  totalReports: 0,
  todayAuditCount: 0,
  agentDecisionCount: 0,
  challengeCount: 0,
  todayChallengeSubmits: 0,
  myChallengeScore: 0,
  myChallengeSolved: 0,
  myAchievementCount: 0,
  ticketClosureRate: 0,
  averageRiskScore: 0,
  maxRiskScore: 0,
  challengePassRate: 0,
  riskDistribution: { HIGH: 0, MEDIUM: 0, LOW: 0 },
  taskTrend: {},
  testTypeDistribution: {},
  taskStatusDistribution: {},
  ticketStatusDistribution: {},
  recentTasks: [],
  highRiskTop: [],
  riskScoreTop: [],
  recentTickets: [],
  recentAuditLogs: [],
  scoreTop: [],
  recentChallengeSubmissions: []
})
const user = JSON.parse(localStorage.getItem('user') || '{}')
const isAdmin = user.role === 'ADMIN'

const statCards = computed(() => [
  ...(isAdmin ? [{ label: '总用户数', value: stats.totalUsers, hint: '平台账号规模' }] : []),
  { label: '总任务数', value: stats.totalTasks, hint: '累计创建测试任务' },
  { label: '运行中任务', value: stats.runningTasks, hint: '计划/执行/分析中' },
  { label: '已完成任务', value: stats.completedTasks, hint: '形成闭环报告' },
  { label: '发现漏洞数', value: stats.totalVulnerabilities, hint: '模拟分析结果' },
  { label: '高危漏洞数', value: stats.highRiskCount, hint: '需优先修复' },
  { label: '未处理漏洞', value: stats.openVulnerabilities, hint: '待确认/修复' },
  { label: '待处理工单', value: stats.pendingTickets, hint: '整改闭环中' },
  { label: '闭环率', value: `${stats.ticketClosureRate}%`, hint: '已关闭工单占比' },
  { label: '平均风险评分', value: stats.averageRiskScore, hint: 'CVSS-Lite 均值' },
  { label: '最高风险评分', value: stats.maxRiskScore, hint: '最高优先级风险' },
  { label: '今日测试次数', value: stats.todayTests, hint: '当天新增任务' },
  ...(isAdmin ? [{ label: '今日操作次数', value: stats.todayAuditCount, hint: '审计日志统计' }] : []),
  { label: 'Agent 决策次数', value: stats.agentDecisionCount, hint: '可解释链路记录' },
  ...(isAdmin
    ? [
        { label: '靶场题目数', value: stats.challengeCount, hint: '安全教育题库' },
        { label: '今日提交次数', value: stats.todayChallengeSubmits, hint: '靶场答题活跃度' },
        { label: '靶场通过率', value: `${stats.challengePassRate}%`, hint: '全部提交统计' }
      ]
    : [
        { label: '我的积分', value: stats.myChallengeScore, hint: '趣味靶场总分' },
        { label: '已完成题目', value: stats.myChallengeSolved, hint: '我的通过数' },
        { label: '成就数量', value: stats.myAchievementCount, hint: '已解锁徽章' }
      ])
])

function statusText(status) {
  const map = { PENDING: '待执行', PLANNING: '计划中', RUNNING: '执行中', ANALYZING: '分析中', COMPLETED: '已完成', FAILED: '失败' }
  return map[status] || status
}

function statusTag(status) {
  if (status === 'COMPLETED') return 'success'
  if (['RUNNING', 'PLANNING', 'ANALYZING'].includes(status)) return 'primary'
  if (status === 'FAILED') return 'danger'
  return 'warning'
}

function ticketText(status) {
  const map = { TODO: '待处理', CONFIRMED: '已确认', FIXING: '修复中', RETESTING: '复测中', CLOSED: '已关闭', IGNORED: '已忽略' }
  return map[status] || status
}

function ticketPercent(value) {
  const total = Object.values(stats.ticketStatusDistribution || {}).reduce((sum, item) => sum + item, 0)
  return total ? Math.round((value / total) * 100) : 0
}

async function loadData() {
  const data = await getDashboardStats()
  Object.assign(stats, data)
  await nextTick()
  renderCharts()
}

function chart(el) {
  const instance = echarts.init(el)
  charts.push(instance)
  return instance
}

function renderCharts() {
  charts.splice(0).forEach((item) => item.dispose())
  const risk = stats.riskDistribution || {}
  chart(riskChartRef.value).setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: '#cbd5e1' } },
    color: ['#ef4444', '#f59e0b', '#10b981'],
    series: [{ type: 'pie', radius: ['45%', '70%'], data: [
      { name: '高危', value: risk.HIGH || 0 },
      { name: '中危', value: risk.MEDIUM || 0 },
      { name: '低危', value: risk.LOW || 0 }
    ] }]
  })

  const trend = stats.taskTrend || {}
  chart(trendChartRef.value).setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 36, right: 20, top: 24, bottom: 36 },
    xAxis: { type: 'category', data: Object.keys(trend).map((d) => d.slice(5)), axisLabel: { color: '#cbd5e1' } },
    yAxis: { type: 'value', axisLabel: { color: '#cbd5e1' }, splitLine: { lineStyle: { color: 'rgba(148,163,184,.16)' } } },
    series: [{ type: 'line', smooth: true, areaStyle: {}, data: Object.values(trend), color: '#38bdf8' }]
  })

  const types = stats.testTypeDistribution || {}
  chart(typeChartRef.value).setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 36, right: 20, top: 24, bottom: 58 },
    xAxis: { type: 'category', data: Object.keys(types), axisLabel: { color: '#cbd5e1', rotate: 20 } },
    yAxis: { type: 'value', axisLabel: { color: '#cbd5e1' }, splitLine: { lineStyle: { color: 'rgba(148,163,184,.16)' } } },
    series: [{ type: 'bar', data: Object.values(types), color: '#818cf8', barWidth: 24 }]
  })

  const status = stats.taskStatusDistribution || {}
  chart(statusChartRef.value).setOption({
    tooltip: { trigger: 'item' },
    color: ['#f59e0b', '#60a5fa', '#38bdf8', '#a78bfa', '#10b981', '#ef4444'],
    series: [{ type: 'pie', roseType: 'radius', radius: [24, 100], data: Object.entries(status).map(([name, value]) => ({ name: statusText(name), value })) }]
  })
}

onMounted(loadData)
onBeforeUnmount(() => charts.forEach((item) => item.dispose()))
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.stat-card {
  min-height: 126px;
  padding: 18px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.86), rgba(15, 23, 42, 0.66));
  border: 1px solid rgba(96, 165, 250, 0.24);
  box-shadow: 0 18px 50px rgba(0, 0, 0, 0.24);
}

.stat-card span,
.stat-card small {
  display: block;
  color: #93a9c8;
}

.stat-card strong {
  display: block;
  margin: 12px 0 8px;
  font-size: 32px;
  color: #f8fafc;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.section-title {
  margin-bottom: 14px;
  font-weight: 800;
  color: #e0f2fe;
}

.chart {
  height: 280px;
}

.risk-list {
  display: grid;
  gap: 12px;
}

.risk-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  background: rgba(127, 29, 29, 0.22);
  border: 1px solid rgba(248, 113, 113, 0.28);
}

.risk-item strong {
  color: #fee2e2;
}

.risk-item p {
  margin: 6px 0 0;
  color: #fecaca;
  font-size: 13px;
}

.agent-steps {
  display: grid;
  gap: 14px;
}

.score-list,
.ticket-bars,
.audit-list {
  display: grid;
  gap: 12px;
}

.score-item,
.audit-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.56);
  border: 1px solid rgba(96, 165, 250, 0.18);
}

.score-item strong,
.audit-item strong {
  color: #f8fafc;
}

.score-item p,
.audit-item small {
  margin: 6px 0 0;
  color: #93c5fd;
  font-size: 12px;
}

.score-item > span {
  min-width: 52px;
  text-align: center;
  padding: 6px 10px;
  border-radius: 999px;
  color: #facc15;
  background: rgba(250, 204, 21, 0.12);
  font-weight: 900;
}

.score-item > span.danger {
  color: #fecaca;
  background: rgba(239, 68, 68, 0.18);
}

.ticket-row {
  display: grid;
  grid-template-columns: 74px 1fr 36px;
  align-items: center;
  gap: 10px;
  color: #bfdbfe;
}

.audit-item {
  align-items: flex-start;
}

.audit-item span {
  color: #67e8f9;
}

.agent-step {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #dbeafe;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #22d3ee;
  box-shadow: 0 0 18px #22d3ee;
}

@media (max-width: 1200px) {
  .stats-grid,
  .charts-grid,
  .content-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 760px) {
  .stats-grid,
  .charts-grid,
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
