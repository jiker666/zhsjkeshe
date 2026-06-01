<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">平台用户管理</h1>
        <div class="page-subtitle">管理员维护平台账号、角色、状态和密码</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增用户</el-button>
    </div>

    <section class="panel">
      <div class="toolbar filter-bar">
        <el-input v-model="query.keyword" placeholder="用户名 / 昵称 / 邮箱" clearable />
        <el-select v-model="query.role" placeholder="角色" clearable>
          <el-option label="管理员" value="ADMIN" />
          <el-option label="普通用户" value="USER" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable>
          <el-option label="启用" value="ENABLED" />
          <el-option label="禁用" value="DISABLED" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      </div>

      <el-table :data="users" stripe empty-text="暂无用户">
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="email" label="邮箱" min-width="220" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'" effect="dark">{{ row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" effect="dark">{{ row.status === 'ENABLED' ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="330" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button text type="warning" @click="toggleStatus(row)">{{ row.status === 'ENABLED' ? '禁用' : '启用' }}</el-button>
            <el-button text type="success" @click="resetPassword(row)">重置密码</el-button>
            <el-button text type="danger" :disabled="row.id === currentUser.id" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="loadData" />
      </div>
    </section>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="680px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名"><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item>
        <el-form-item v-if="!form.id" label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { createUser, deleteUser, listUsers, resetUserPassword, updateUser, updateUserStatus } from '../api'

const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
const users = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const query = reactive({ keyword: '', role: '', status: '', page: 1, size: 10 })
const form = reactive({ id: null, username: '', password: '', nickname: '', email: '', phone: '', role: 'USER', status: 'ENABLED' })

async function loadData() {
  const data = await listUsers(query)
  users.value = data.records
  total.value = data.total
}

function openCreate() {
  Object.assign(form, { id: null, username: '', password: '123456', nickname: '', email: '', phone: '', role: 'USER', status: 'ENABLED' })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, { ...row, password: '' })
  dialogVisible.value = true
}

async function save() {
  if (form.id) {
    await updateUser(form.id, form)
  } else {
    await createUser(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  await loadData()
}

async function toggleStatus(row) {
  await updateUserStatus(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED')
  ElMessage.success('状态已更新')
  await loadData()
}

async function resetPassword(row) {
  const { value } = await ElMessageBox.prompt(`为 ${row.username} 设置新密码`, '重置密码', { inputValue: '123456' })
  await resetUserPassword(row.id, value)
  ElMessage.success('密码已重置')
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除用户 ${row.username}？`, '删除确认', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.filter-bar {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) 180px 180px auto;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
