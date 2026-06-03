<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">我的靶场成绩</h1>
        <div class="page-subtitle">积分、成就、类型掌握情况和最近挑战记录</div>
      </div>
      <el-button type="primary" @click="$router.push('/challenges')">继续挑战</el-button>
    </div>

    <div class="stats-grid">
      <div v-for="item in cards" :key="item.label" class="stat-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.hint }}</small>
      </div>
    </div>

    <div class="charts-grid">
      <section class="panel">
        <div class="section-title">类型掌握雷达图</div>
        <div ref="radarRef" class="chart"></div>
      </section>
      <section class="panel">
        <div class="section-title">最近 7 天积分趋势</div>
        <div ref="trendRef" class="chart"></div>
      </section>
    </div>

    <div class="content-grid">
      <section class="panel">
        <div class="section-title">成就徽章</div>
        <div v-if="stats.achievements.length" class="badge-grid">
          <div v-for="item in stats.achievements" :key="item.id" class="badge">
            <span>{{ iconText(item.icon) }}</span>
            <div>
              <strong>{{ item.achievementName }}</strong>
              <p>{{ item.achievementDesc }}</p>
            </div>
          </div>
        </div>
        <el-empty v-else description="还没有成就，先去答一道题吧" />
      </section>

      <section class="panel">
        <div class="section-title">最近挑战记录</div>
        <div v-if="stats.recentSubmissions.length" class="table-shell">
          <el-table :data="stats.recentSubmissions" height="310">
            <el-table-column prop="challengeId" label="题目" width="80" />
            <el-table-column prop="correct" label="结果" width="90">
              <template #default="{ row }">
                <el-tag :type="row.correct === 1 ? 'success' : 'danger'" effect="dark">{{ row.correct === 1 ? '正确' : '错误' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="scoreGot" label="得分" width="80" />
            <el-table-column prop="submitTime" label="时间" min-width="150" show-overflow-tooltip />
          </el-table>
        </div>
        <div v-else class="empty-records">
          <el-empty description="暂无提交记录，去完成第一道题吧" />
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { getMyChallengeStats } from '../api'

const radarRef = ref()
const trendRef = ref()
const charts = []
const stats = reactive({
  totalScore: 0,
  solvedCount: 0,
  submitCount: 0,
  correctRate: 0,
  streak: 0,
  recentSubmissions: [],
  achievements: [],
  typeMastery: {},
  scoreTrend: {}
})

const cards = computed(() => [
  { label: '总积分', value: stats.totalScore, hint: '答对首次计分' },
  { label: '已完成题目', value: stats.solvedCount, hint: '去重后的通过数' },
  { label: '正确率', value: `${stats.correctRate}%`, hint: '所有提交统计' },
  { label: '连续答对', value: stats.streak, hint: '最近连续正确次数' },
  { label: '成就数量', value: stats.achievements.length, hint: '徽章解锁数' }
])

function iconText(icon) {
  const map = { ice: 'ICE', shield: 'SHD', crown: 'TOP', target: 'P1', agent: 'AI' }
  return map[icon] || 'OK'
}

async function loadData() {
  Object.assign(stats, await getMyChallengeStats())
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
  const mastery = stats.typeMastery || {}
  const types = ['信息泄露', '水平越权', '弱口令', '接口参数篡改', '敏感信息泄露', 'Agent 推理']
  chart(radarRef.value).setOption({
    radar: { indicator: types.map((name) => ({ name, max: 3 })), axisName: { color: '#cbd5e1' }, splitLine: { lineStyle: { color: 'rgba(148,163,184,.18)' } } },
    series: [{ type: 'radar', areaStyle: {}, data: [{ value: types.map((name) => mastery[name] || 0), name: '掌握度' }], color: '#38bdf8' }]
  })
  const trend = stats.scoreTrend || {}
  chart(trendRef.value).setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 36, right: 20, top: 24, bottom: 36 },
    xAxis: { type: 'category', data: Object.keys(trend).map((d) => d.slice(5)), axisLabel: { color: '#cbd5e1' } },
    yAxis: { type: 'value', axisLabel: { color: '#cbd5e1' }, splitLine: { lineStyle: { color: 'rgba(148,163,184,.16)' } } },
    series: [{ type: 'line', smooth: true, areaStyle: {}, data: Object.values(trend), color: '#a78bfa' }]
  })
}

onMounted(loadData)
onBeforeUnmount(() => charts.forEach((item) => item.dispose()))
</script>

<style scoped>
.stats-grid,
.charts-grid,
.content-grid {
  display: grid;
  gap: 16px;
  min-width: 0;
}

.stats-grid {
  grid-template-columns: repeat(5, minmax(0, 1fr));
  margin-bottom: 16px;
}

.charts-grid,
.content-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-bottom: 16px;
}

.panel,
.stat-card,
.chart,
.table-shell {
  min-width: 0;
}

.stat-card {
  padding: 18px;
  border-radius: 16px;
  border: 1px solid rgba(96, 165, 250, 0.22);
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.16), rgba(124, 58, 237, 0.12));
}

.stat-card span,
.stat-card small {
  display: block;
  color: #93c5fd;
}

.stat-card strong {
  display: block;
  margin: 10px 0 6px;
  color: #f8fafc;
  font-size: 30px;
}

.section-title {
  margin-bottom: 14px;
  color: #e0f2fe;
  font-weight: 800;
}

.chart {
  height: 320px;
}

.badge-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.badge {
  display: flex;
  gap: 12px;
  padding: 14px;
  border-radius: 14px;
  background: rgba(56, 217, 150, 0.12);
  border: 1px solid rgba(56, 217, 150, 0.26);
}

.badge span {
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  background: linear-gradient(135deg, #22d3ee, #86efac);
  color: #052e16;
  font-weight: 900;
}

.badge strong {
  color: #f8fafc;
}

.badge p {
  margin: 6px 0 0;
  color: #bfdbfe;
}

.table-shell {
  width: 100%;
  overflow: hidden;
}

.empty-records {
  height: 310px;
  display: grid;
  place-items: center;
  overflow: hidden;
  border: 1px solid rgba(118, 171, 255, 0.14);
  border-radius: 8px;
  background: rgba(8, 15, 28, 0.38);
}

@media (max-width: 1100px) {
  .stats-grid,
  .charts-grid,
  .content-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 760px) {
  .stats-grid,
  .charts-grid,
  .content-grid,
  .badge-grid {
    grid-template-columns: 1fr;
  }
}
</style>
