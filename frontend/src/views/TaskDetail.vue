<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">任务详情</h1>
        <div class="page-subtitle">展示 Agent 计划生成、执行、分析和报告生成全过程</div>
      </div>
      <div>
        <el-button @click="$router.push('/tasks')">返回列表</el-button>
        <el-button type="primary" :loading="loading" @click="run">{{ task.status === 'COMPLETED' ? '重新执行' : '执行测试' }}</el-button>
        <el-button type="success" @click="$router.push(`/reports/${task.id}`)">查看报告</el-button>
      </div>
    </div>

    <section class="panel">
      <el-steps :active="activeStep" finish-status="success" process-status="process" align-center>
        <el-step title="待执行" />
        <el-step title="生成计划中" />
        <el-step title="执行中" />
        <el-step title="分析结果中" />
        <el-step title="已完成" />
      </el-steps>
    </section>

    <section class="panel detail-panel">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="任务名称">{{ task.taskName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTag(task.status)" effect="dark">{{ statusText(task.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="目标地址">{{ task.targetUrl }}</el-descriptions-item>
        <el-descriptions-item label="测试类型">{{ task.testType }}</el-descriptions-item>
        <el-descriptions-item label="测试深度">{{ task.testDepth || '标准' }}</el-descriptions-item>
        <el-descriptions-item label="生成报告">{{ task.generateReport === 0 ? '否' : '是' }}</el-descriptions-item>
        <el-descriptions-item label="任务描述" :span="2">{{ task.description }}</el-descriptions-item>
      </el-descriptions>
    </section>

    <section class="panel">
      <div class="section-title">Agent 自动生成的测试计划</div>
      <pre class="plan-text">{{ task.plan }}</pre>
    </section>

    <section class="panel">
      <div class="section-title">Agent 决策链可视化</div>
      <div v-if="decisions.length" class="decision-chain">
        <article v-for="item in decisions" :key="item.id" class="decision-card">
          <div class="decision-index">{{ item.stepNo }}</div>
          <div class="decision-body">
            <div class="decision-top">
              <div>
                <strong>{{ stageText(item.stage) }}</strong>
                <h3>{{ item.decisionTitle }}</h3>
              </div>
              <el-tag effect="dark">{{ item.toolName }}</el-tag>
            </div>
            <p>{{ item.decisionReason }}</p>
            <div class="decision-meta">
              <span>输入：{{ item.inputSummary }}</span>
              <span>输出：{{ item.outputSummary }}</span>
            </div>
            <div class="confidence">
              <span>置信度 {{ Math.round((item.confidence || 0) * 100) }}%</span>
              <el-progress :percentage="Math.round((item.confidence || 0) * 100)" :show-text="false" />
            </div>
          </div>
        </article>
      </div>
      <el-empty v-else description="执行任务后生成 Agent 决策链" />
    </section>

    <div class="detail-grid">
      <section class="panel">
        <div class="section-title">测试日志时间线</div>
        <el-timeline>
          <el-timeline-item
            v-for="log in logs"
            :key="log.id"
            :timestamp="log.createdAt"
            :type="timelineType(log.logLevel)"
          >
            <el-tag size="small" :type="levelTag(log.logLevel)" effect="dark">{{ log.logLevel }}</el-tag>
            <span class="log-message">{{ log.message }}</span>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-if="logs.length === 0" description="暂无执行日志" />
      </section>

      <section class="panel">
        <div class="section-title">漏洞结果列表</div>
        <el-table :data="results" stripe empty-text="执行测试后生成漏洞结果">
          <el-table-column prop="vulnerabilityName" label="漏洞名称" min-width="170" />
          <el-table-column prop="riskLevel" label="等级" width="90">
            <template #default="{ row }">
              <el-tag :type="riskTag(row.riskLevel)" effect="dark">{{ row.riskLevel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="vulnerabilityType" label="类型" width="110" />
          <el-table-column prop="riskScore" label="评分" width="90">
            <template #default="{ row }">
              <span class="score" :class="{ high: row.riskScore >= 7 }">{{ row.riskScore || '-' }}</span>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { executeTask, getAgentDecisions, getTask, listLogs, listResults } from '../api'

const route = useRoute()
const loading = ref(false)
const results = ref([])
const logs = ref([])
const decisions = ref([])
const task = reactive({})
const statusOrder = ['PENDING', 'PLANNING', 'RUNNING', 'ANALYZING', 'COMPLETED']

const activeStep = computed(() => Math.max(0, statusOrder.indexOf(task.status)))

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

function riskTag(level) {
  return level === 'HIGH' ? 'danger' : level === 'MEDIUM' ? 'warning' : 'success'
}

function levelTag(level) {
  return level === 'ERROR' ? 'danger' : level === 'WARN' ? 'warning' : level === 'SUCCESS' ? 'success' : 'info'
}

function timelineType(level) {
  return level === 'ERROR' ? 'danger' : level === 'WARN' ? 'warning' : level === 'SUCCESS' ? 'success' : 'primary'
}

function stageText(stage) {
  const map = {
    TASK_UNDERSTANDING: '任务理解',
    PLAN_GENERATION: '计划生成',
    TOOL_SELECTION: '工具选择',
    TEST_EXECUTION: '测试执行',
    RESULT_ANALYSIS: '结果分析',
    REPORT_GENERATION: '报告生成'
  }
  return map[stage] || stage
}

async function loadData() {
  Object.assign(task, await getTask(route.params.id))
  const resultData = await listResults({ taskId: route.params.id })
  results.value = resultData.records || resultData
  const logData = await listLogs({ taskId: route.params.id })
  logs.value = (logData.records || logData).slice().reverse()
  decisions.value = await getAgentDecisions(route.params.id)
}

async function run() {
  loading.value = true
  try {
    await executeTask(route.params.id)
    ElMessage.success('Agent 模拟测试执行完成')
    await loadData()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.panel + .panel,
.detail-panel {
  margin-top: 16px;
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(420px, 0.9fr);
  gap: 16px;
  margin-top: 16px;
}

.section-title {
  margin-bottom: 12px;
  font-weight: 800;
  color: #e0f2fe;
}

.plan-text {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.75;
  color: #cbd5e1;
  font-family: inherit;
}

.decision-chain {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.decision-card {
  display: flex;
  gap: 14px;
  padding: 16px;
  border-radius: 14px;
  background: linear-gradient(145deg, rgba(14, 165, 233, 0.12), rgba(124, 58, 237, 0.12));
  border: 1px solid rgba(96, 165, 250, 0.22);
}

.decision-index {
  flex: 0 0 34px;
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #020617;
  font-weight: 900;
  background: linear-gradient(135deg, #67e8f9, #a78bfa);
  box-shadow: 0 0 22px rgba(34, 211, 238, 0.32);
}

.decision-body {
  min-width: 0;
}

.decision-top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.decision-top strong {
  color: #67e8f9;
  font-size: 13px;
}

.decision-top h3 {
  margin: 6px 0 0;
  color: #f8fafc;
  font-size: 16px;
}

.decision-body p,
.decision-meta {
  color: #cbd5e1;
  line-height: 1.7;
}

.decision-meta {
  display: grid;
  gap: 6px;
  margin: 10px 0;
  font-size: 13px;
}

.confidence {
  display: grid;
  gap: 6px;
  color: #bfdbfe;
}

.score {
  font-weight: 800;
  color: #facc15;
}

.score.high {
  color: #f87171;
}

.log-message {
  margin-left: 10px;
  color: #dbeafe;
}

@media (max-width: 980px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .decision-chain {
    grid-template-columns: 1fr;
  }
}
</style>
