<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ isAdmin ? '全部漏洞结果' : '我的漏洞结果' }}</h1>
        <div class="page-subtitle">{{ isAdmin ? '管理员查看全平台 Agent 分析结果' : '仅展示我创建任务下的漏洞结果' }}</div>
      </div>
    </div>

    <section class="panel">
      <div class="toolbar filter-bar">
        <el-select v-model="query.riskLevel" placeholder="风险等级" clearable>
          <el-option label="高危" value="HIGH" />
          <el-option label="中危" value="MEDIUM" />
          <el-option label="低危" value="LOW" />
        </el-select>
        <el-select v-model="query.vulnerabilityType" placeholder="漏洞类型" clearable>
          <el-option v-for="item in vulnTypes" :key="item" :label="item" :value="item" />
        </el-select>
        <el-button :icon="Search" @click="loadData">查询</el-button>
      </div>

      <el-table :data="results" stripe empty-text="暂无漏洞结果">
        <el-table-column prop="vulnerabilityName" label="漏洞名称" min-width="190" />
        <el-table-column prop="riskLevel" label="风险等级" width="110">
          <template #default="{ row }">
            <el-tag :type="riskTag(row.riskLevel)" effect="dark">{{ row.riskLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="vulnerabilityType" label="漏洞类型" width="120" />
        <el-table-column prop="riskScore" label="风险评分" width="110">
          <template #default="{ row }">
            <span class="risk-score" :class="{ danger: row.riskScore >= 7 }">{{ row.riskScore || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="处理状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" effect="dark">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="url" label="影响地址" min-width="260" />
        <el-table-column prop="description" label="漏洞描述" min-width="300" show-overflow-tooltip />
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看详情</el-button>
            <el-button link type="success" @click="createTicket(row)">生成工单</el-button>
            <el-button v-if="isAdmin" link type="danger" @click="removeResult(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </section>

    <el-dialog v-model="dialogVisible" title="漏洞详情" width="860px" class="vuln-dialog">
      <template v-if="current">
        <div class="detail-head">
          <div>
            <h2>{{ current.vulnerabilityName }}</h2>
            <p>{{ current.url }}</p>
          </div>
          <el-tag :type="riskTag(current.riskLevel)" effect="dark" size="large">{{ current.riskLevel }}</el-tag>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="所属任务">{{ current.taskId }}</el-descriptions-item>
          <el-descriptions-item label="漏洞类型">{{ current.vulnerabilityType }}</el-descriptions-item>
          <el-descriptions-item label="风险评分">
            <span class="risk-score large" :class="{ danger: current.riskScore >= 7 }">{{ current.riskScore || '-' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="风险向量">{{ current.riskVector || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理状态" :span="2">
            <el-select v-model="current.status" style="width: 220px" @change="changeStatus">
              <el-option label="未处理" value="OPEN" />
              <el-option label="已确认" value="CONFIRMED" />
              <el-option label="已修复" value="FIXED" />
              <el-option label="已忽略" value="IGNORED" />
            </el-select>
          </el-descriptions-item>
          <el-descriptions-item label="漏洞描述" :span="2">{{ current.description }}</el-descriptions-item>
          <el-descriptions-item label="复现步骤" :span="2">
            <pre>{{ current.reproduceSteps }}</pre>
          </el-descriptions-item>
          <el-descriptions-item label="修复建议" :span="2">{{ current.suggestion }}</el-descriptions-item>
          <el-descriptions-item label="联动操作" :span="2">
            <el-button type="success" @click="createTicket(current)">生成整改工单</el-button>
            <el-button type="primary" @click="goKnowledge(current)">查看知识库说明</el-button>
          </el-descriptions-item>
        </el-descriptions>
        <div class="code-grid">
          <div>
            <h3>请求示例</h3>
            <pre class="code-block">{{ current.requestExample }}</pre>
          </div>
          <div>
            <h3>响应示例</h3>
            <pre class="code-block">{{ current.responseExample }}</pre>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { createTicketFromResult, deleteResult, listResults, updateResultStatus } from '../api'

const vulnTypes = ['访问控制', '身份认证', '信息泄露', '业务安全', '输入校验', '安全配置']
const results = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const current = ref(null)
const user = JSON.parse(localStorage.getItem('user') || '{}')
const isAdmin = user.role === 'ADMIN'
const query = reactive({
  riskLevel: '',
  vulnerabilityType: '',
  page: 1,
  size: 10
})

function riskTag(level) {
  return level === 'HIGH' ? 'danger' : level === 'MEDIUM' ? 'warning' : 'success'
}

function statusText(status) {
  const map = { OPEN: '未处理', CONFIRMED: '已确认', FIXED: '已修复', IGNORED: '已忽略' }
  return map[status] || status
}

function statusTag(status) {
  if (status === 'FIXED') return 'success'
  if (status === 'CONFIRMED') return 'warning'
  if (status === 'IGNORED') return 'info'
  return 'danger'
}

function openDetail(row) {
  current.value = row
  dialogVisible.value = true
}

async function loadData() {
  const data = await listResults(query)
  results.value = data.records || data
  total.value = data.total || results.value.length
}

async function changeStatus(status) {
  await updateResultStatus(current.value.id, status)
  ElMessage.success('处理状态已更新')
  await loadData()
}

async function createTicket(row) {
  const ticket = await createTicketFromResult(row.id)
  ElMessage.success(`整改工单已生成：${ticket.title}`)
}

function goKnowledge(row) {
  window.location.href = `/knowledge?type=${encodeURIComponent(row.vulnerabilityType || row.vulnerabilityName)}`
}

async function removeResult(id) {
  await ElMessageBox.confirm('确认删除该漏洞结果？', '删除确认', { type: 'warning' })
  await deleteResult(id)
  ElMessage.success('删除成功')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.filter-bar {
  justify-content: flex-start;
}

.filter-bar .el-select {
  width: 190px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.detail-head {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 16px;
}

.detail-head h2 {
  margin: 0 0 8px;
  color: #f8fafc;
}

.detail-head p {
  margin: 0;
  color: #93c5fd;
}

pre {
  white-space: pre-wrap;
  margin: 0;
}

.code-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin-top: 16px;
}

.code-grid h3 {
  margin: 0 0 8px;
  color: #bfdbfe;
}

.code-block {
  min-height: 170px;
  padding: 14px;
  border-radius: 10px;
  color: #d1fae5;
  background: #020617;
  border: 1px solid rgba(34, 211, 238, 0.22);
  overflow: auto;
}

.risk-score {
  display: inline-flex;
  min-width: 48px;
  justify-content: center;
  padding: 4px 10px;
  border-radius: 999px;
  color: #facc15;
  background: rgba(250, 204, 21, 0.12);
  border: 1px solid rgba(250, 204, 21, 0.28);
  font-weight: 900;
}

.risk-score.danger {
  color: #fecaca;
  background: rgba(239, 68, 68, 0.18);
  border-color: rgba(248, 113, 113, 0.36);
}

.risk-score.large {
  font-size: 18px;
}
</style>
