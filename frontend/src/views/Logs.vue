<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ isAdmin ? '测试日志' : '我的测试日志' }}</h1>
        <div class="page-subtitle">{{ isAdmin ? '按任务和级别追踪全平台 Agent 执行过程' : '只查看我创建任务的 Agent 执行日志' }}</div>
      </div>
    </div>

    <section class="panel">
      <div class="toolbar filter-bar">
        <el-select v-model="query.taskId" placeholder="选择任务" clearable filterable>
          <el-option v-for="task in tasks" :key="task.id" :label="task.taskName" :value="task.id" />
        </el-select>
        <el-select v-model="query.logLevel" placeholder="日志级别" clearable>
          <el-option v-for="level in levels" :key="level" :label="level" :value="level" />
        </el-select>
        <el-button :icon="Search" @click="loadLogs">查询</el-button>
      </div>

      <div ref="logBoxRef" class="log-box">
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
        <el-empty v-if="logs.length === 0" description="暂无日志" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { listLogs, listTasks } from '../api'

const logs = ref([])
const tasks = ref([])
const logBoxRef = ref()
const levels = ['INFO', 'WARN', 'ERROR', 'SUCCESS']
const user = JSON.parse(localStorage.getItem('user') || '{}')
const isAdmin = user.role === 'ADMIN'
const query = reactive({
  taskId: '',
  logLevel: ''
})

function levelTag(level) {
  return level === 'ERROR' ? 'danger' : level === 'WARN' ? 'warning' : level === 'SUCCESS' ? 'success' : 'info'
}

function timelineType(level) {
  return level === 'ERROR' ? 'danger' : level === 'WARN' ? 'warning' : level === 'SUCCESS' ? 'success' : 'primary'
}

async function loadLogs() {
  const data = await listLogs(query)
  logs.value = (data.records || data).slice().reverse()
  await nextTick()
  logBoxRef.value?.scrollTo({ top: logBoxRef.value.scrollHeight, behavior: 'smooth' })
}

onMounted(async () => {
  const data = await listTasks({})
  tasks.value = data.records || data
  await loadLogs()
})
</script>

<style scoped>
.filter-bar {
  justify-content: flex-start;
}

.filter-bar .el-select {
  width: 260px;
}

.log-box {
  max-height: 620px;
  overflow: auto;
  padding-right: 8px;
}

.log-message {
  margin-left: 10px;
  color: #dbeafe;
}
</style>
