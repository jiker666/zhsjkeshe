<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">操作审计</h1>
        <div class="page-subtitle">记录平台关键操作，支持安全追溯和答辩展示</div>
      </div>
    </div>

    <section class="panel">
      <div class="toolbar filter-bar">
        <el-input v-model="query.username" placeholder="用户名" clearable />
        <el-input v-model="query.module" placeholder="模块" clearable />
        <el-select v-model="query.action" placeholder="操作类型" clearable>
          <el-option v-for="item in actions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="query.result" placeholder="结果" clearable>
          <el-option label="成功" value="SUCCESS" />
          <el-option label="失败" value="FAILED" />
        </el-select>
        <el-button :icon="Search" @click="loadData">查询</el-button>
      </div>

      <el-table v-loading="loading" :data="logs" stripe empty-text="暂无审计日志">
        <el-table-column prop="createTime" label="时间" width="180" />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'" effect="dark">{{ row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="130" />
        <el-table-column prop="action" label="操作" width="170" />
        <el-table-column prop="detail" label="详情" min-width="260" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="100" />
        <el-table-column prop="result" label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.result === 'SUCCESS' ? 'success' : 'danger'" effect="dark">{{ row.result }}</el-tag>
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { listAuditLogs } from '../api'

const loading = ref(false)
const logs = ref([])
const total = ref(0)
const actions = ['LOGIN', 'REGISTER', 'CREATE_TASK', 'EXECUTE_TASK', 'EDIT_TASK', 'DELETE_TASK', 'UPDATE_RESULT_STATUS', 'EXPORT_REPORT', 'CREATE_TICKET', 'UPDATE_TICKET_STATUS']
const query = reactive({ username: '', module: '', action: '', result: '', page: 1, size: 10 })

async function loadData() {
  loading.value = true
  try {
    const data = await listAuditLogs(query)
    logs.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.filter-bar {
  justify-content: flex-start;
}

.filter-bar .el-input,
.filter-bar .el-select {
  width: 170px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
