<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ isAdmin ? '整改工单' : '我的整改工单' }}</h1>
        <div class="page-subtitle">跟踪漏洞从发现、确认、修复、复测到关闭的完整闭环</div>
      </div>
    </div>

    <div class="ticket-kpis">
      <div v-for="item in kpis" :key="item.label" class="ticket-kpi">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </div>
    </div>

    <section class="panel">
      <div class="toolbar filter-bar">
        <el-select v-model="query.status" placeholder="工单状态" clearable>
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="query.riskLevel" placeholder="风险等级" clearable>
          <el-option label="高危" value="HIGH" />
          <el-option label="中危" value="MEDIUM" />
          <el-option label="低危" value="LOW" />
        </el-select>
        <el-input v-if="isAdmin" v-model="query.assigneeId" placeholder="负责人ID" clearable />
        <el-button :icon="Search" @click="loadData">查询</el-button>
      </div>

      <el-table v-loading="loading" :data="tickets" stripe empty-text="暂无整改工单">
        <el-table-column prop="title" label="工单标题" min-width="240" />
        <el-table-column prop="riskLevel" label="风险" width="90">
          <template #default="{ row }">
            <el-tag :type="riskTag(row.riskLevel)" effect="dark">{{ row.riskLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="ticketTag(row.status)" effect="dark">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="负责人" width="140" />
        <el-table-column prop="retestResult" label="复测结果" min-width="240" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link type="success" @click="runRetest(row)">一键复测</el-button>
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

    <el-dialog v-model="dialogVisible" title="整改工单详情" width="760px">
      <template v-if="current">
        <div class="ticket-head">
          <div>
            <h2>{{ current.title }}</h2>
            <p>任务ID：{{ current.taskId }} / 漏洞ID：{{ current.resultId }}</p>
          </div>
          <el-tag :type="riskTag(current.riskLevel)" effect="dark" size="large">{{ current.riskLevel }}</el-tag>
        </div>
        <el-form label-width="100px">
          <el-form-item label="当前状态">
            <el-select v-model="current.status" style="width: 220px" @change="changeStatus">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="负责人">
            <el-input v-model="current.assigneeName" :disabled="!isAdmin" />
          </el-form-item>
          <el-form-item label="修复建议">
            <el-input v-model="current.fixSuggestion" type="textarea" :rows="4" />
          </el-form-item>
          <el-form-item label="复测结果">
            <div class="retest-box">{{ current.retestResult || '尚未复测' }}</div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveCurrent">保存</el-button>
            <el-button type="success" @click="runRetest(current)">一键复测</el-button>
          </el-form-item>
        </el-form>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { listTickets, retestTicket, updateTicket, updateTicketStatus } from '../api'

const user = JSON.parse(localStorage.getItem('user') || '{}')
const isAdmin = user.role === 'ADMIN'
const loading = ref(false)
const tickets = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const current = ref(null)
const query = reactive({ status: '', riskLevel: '', assigneeId: '', page: 1, size: 10 })
const statusOptions = [
  { label: '待处理', value: 'TODO' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '修复中', value: 'FIXING' },
  { label: '复测中', value: 'RETESTING' },
  { label: '已关闭', value: 'CLOSED' },
  { label: '已忽略', value: 'IGNORED' }
]

const kpis = computed(() => [
  { label: '工单总数', value: total.value },
  { label: '待处理', value: tickets.value.filter((item) => ['TODO', 'CONFIRMED', 'FIXING', 'RETESTING'].includes(item.status)).length },
  { label: '已关闭', value: tickets.value.filter((item) => item.status === 'CLOSED').length },
  { label: '高危工单', value: tickets.value.filter((item) => item.riskLevel === 'HIGH').length }
])

function statusText(status) {
  return statusOptions.find((item) => item.value === status)?.label || status
}

function ticketTag(status) {
  if (status === 'CLOSED') return 'success'
  if (status === 'IGNORED') return 'info'
  if (status === 'FIXING' || status === 'RETESTING') return 'warning'
  return 'danger'
}

function riskTag(level) {
  return level === 'HIGH' ? 'danger' : level === 'MEDIUM' ? 'warning' : 'success'
}

async function loadData() {
  loading.value = true
  try {
    const params = { ...query, assigneeId: query.assigneeId || undefined }
    const data = await listTickets(params)
    tickets.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function openDetail(row) {
  current.value = { ...row }
  dialogVisible.value = true
}

async function changeStatus(status) {
  current.value = await updateTicketStatus(current.value.id, status)
  ElMessage.success('工单状态已更新')
  await loadData()
}

async function saveCurrent() {
  current.value = await updateTicket(current.value.id, current.value)
  ElMessage.success('工单已保存')
  await loadData()
}

async function runRetest(row) {
  const updated = await retestTicket(row.id)
  ElMessage.success(updated.status === 'CLOSED' ? '复测通过，工单已关闭' : '已进入复测中，仍需继续整改')
  if (current.value?.id === updated.id) current.value = updated
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.ticket-kpis {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.ticket-kpi {
  padding: 18px;
  border-radius: 16px;
  border: 1px solid rgba(96, 165, 250, 0.22);
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.16), rgba(124, 58, 237, 0.12));
}

.ticket-kpi span {
  color: #93c5fd;
}

.ticket-kpi strong {
  display: block;
  margin-top: 10px;
  font-size: 30px;
  color: #f8fafc;
}

.filter-bar {
  justify-content: flex-start;
}

.filter-bar .el-select,
.filter-bar .el-input {
  width: 180px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.ticket-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.ticket-head h2 {
  margin: 0 0 8px;
  color: #f8fafc;
}

.ticket-head p,
.retest-box {
  color: #bfdbfe;
}

.retest-box {
  width: 100%;
  min-height: 44px;
  padding: 12px;
  border-radius: 10px;
  background: rgba(15, 23, 42, 0.72);
  border: 1px solid rgba(96, 165, 250, 0.18);
}

@media (max-width: 900px) {
  .ticket-kpis {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
