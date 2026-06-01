<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ isAdmin ? '全部测试任务' : '我的安全测试任务' }}</h1>
        <div class="page-subtitle">{{ isAdmin ? '管理员查看和维护全平台测试任务' : '安全测试人员只管理自己创建的任务' }}</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="$router.push('/tasks/new')">新建任务</el-button>
    </div>

    <section class="panel task-panel">
      <div class="toolbar filter-bar">
        <el-input v-model="query.keyword" placeholder="任务名称 / 目标地址" clearable />
        <el-select v-model="query.testType" placeholder="测试类型" clearable>
          <el-option v-for="item in testTypes" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="query.status" placeholder="任务状态" clearable>
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="query.riskLevel" placeholder="风险等级" clearable>
          <el-option label="高危" value="HIGH" />
          <el-option label="中危" value="MEDIUM" />
          <el-option label="低危" value="LOW" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      </div>

      <el-table :data="tasks" stripe class="task-table" empty-text="暂无测试任务，可先创建示例任务">
        <el-table-column prop="taskName" label="任务名称" min-width="170" />
        <el-table-column prop="targetUrl" label="目标地址" min-width="230" />
        <el-table-column prop="testType" label="测试类型" min-width="160" />
        <el-table-column prop="testDepth" label="深度" width="90" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="tagType(row.status)" effect="dark">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="风险" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'COMPLETED'" type="danger" effect="dark">已分析</el-tag>
            <span v-else class="muted">待分析</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="190" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <div class="action-bar">
              <el-button size="small" text type="primary" @click="$router.push(`/tasks/${row.id}`)">详情</el-button>
              <el-button size="small" text type="warning" @click="openEdit(row)">编辑</el-button>
              <el-button size="small" type="primary" :loading="executingId === row.id" @click="run(row.id)">执行</el-button>
              <el-button size="small" text type="success" @click="$router.push(`/reports/${row.id}`)">报告</el-button>
              <el-button v-if="isAdmin" size="small" text type="danger" @click="remove(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[5, 10, 20]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadData"
          @size-change="loadData"
        />
      </div>
    </section>

    <el-dialog v-model="editVisible" title="编辑测试任务" width="720px">
      <el-form :model="editForm" label-width="110px">
        <el-form-item label="任务名称"><el-input v-model="editForm.taskName" /></el-form-item>
        <el-form-item label="目标地址"><el-input v-model="editForm.targetUrl" /></el-form-item>
        <el-form-item label="测试类型">
          <el-select v-model="editForm.testType" style="width: 100%">
            <el-option v-for="item in testTypes" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="测试深度"><el-segmented v-model="editForm.testDepth" :options="['快速', '标准', '深入']" /></el-form-item>
        <el-form-item label="生成报告"><el-switch v-model="editForm.generateReport" active-text="是" inactive-text="否" /></el-form-item>
        <el-form-item label="任务描述"><el-input v-model="editForm.description" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { deleteTask, executeTask, listTasks, updateTask } from '../api'

const testTypes = ['未授权访问测试', '弱口令测试', '越权访问测试', '敏感信息泄露测试', '接口参数篡改测试']
const statusOptions = [
  { label: '待执行', value: 'PENDING' },
  { label: '计划中', value: 'PLANNING' },
  { label: '执行中', value: 'RUNNING' },
  { label: '分析中', value: 'ANALYZING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '失败', value: 'FAILED' }
]
const tasks = ref([])
const total = ref(0)
const executingId = ref(null)
const editVisible = ref(false)
const user = JSON.parse(localStorage.getItem('user') || '{}')
const isAdmin = computed(() => user.role === 'ADMIN')
const editForm = reactive({
  id: null,
  taskName: '',
  targetUrl: '',
  testType: '',
  testDepth: '标准',
  generateReport: true,
  description: ''
})
const query = reactive({
  keyword: '',
  testType: '',
  status: '',
  riskLevel: '',
  page: 1,
  size: 10
})

function statusText(status) {
  return statusOptions.find((item) => item.value === status)?.label || status
}

function tagType(status) {
  if (status === 'COMPLETED') return 'success'
  if (['RUNNING', 'PLANNING', 'ANALYZING'].includes(status)) return 'primary'
  if (status === 'FAILED') return 'danger'
  return 'warning'
}

async function loadData() {
  const data = await listTasks(query)
  tasks.value = data.records || data
  total.value = data.total || tasks.value.length
}

async function run(id) {
  executingId.value = id
  try {
    await executeTask(id)
    ElMessage.success('Agent 模拟测试执行完成')
    await loadData()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    executingId.value = null
  }
}

function openEdit(row) {
  Object.assign(editForm, {
    id: row.id,
    taskName: row.taskName,
    targetUrl: row.targetUrl,
    testType: row.testType,
    testDepth: row.testDepth || '标准',
    generateReport: row.generateReport !== 0,
    description: row.description
  })
  editVisible.value = true
}

async function saveEdit() {
  await updateTask(editForm.id, { ...editForm, generateReport: editForm.generateReport ? 1 : 0 })
  ElMessage.success('任务更新成功')
  editVisible.value = false
  await loadData()
}

async function remove(id) {
  await ElMessageBox.confirm('确认删除该测试任务及其结果、日志和报告？', '删除确认', { type: 'warning' })
  await deleteTask(id)
  ElMessage.success('删除成功')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.task-panel {
  overflow: hidden;
}

.filter-bar {
  display: grid;
  grid-template-columns: minmax(220px, 1.4fr) repeat(3, minmax(150px, 0.8fr)) auto;
  padding: 12px;
  border: 1px solid rgba(118, 171, 255, 0.12);
  border-radius: 8px;
  background: rgba(8, 15, 28, 0.42);
}

.task-table {
  margin-top: 4px;
}

.action-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: nowrap;
}

.action-bar :deep(.el-button) {
  margin-left: 0;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid rgba(118, 171, 255, 0.12);
}

@media (max-width: 1000px) {
  .filter-bar {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
