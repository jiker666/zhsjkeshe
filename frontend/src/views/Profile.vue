<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">个人中心</h1>
        <div class="page-subtitle">维护个人资料、联系方式和登录密码</div>
      </div>
    </div>

    <div class="profile-grid">
      <section class="panel profile-card">
        <div class="avatar">{{ avatarText }}</div>
        <h2>{{ profile.nickname || profile.username }}</h2>
        <p>{{ profile.username }}</p>
        <el-tag :type="profile.role === 'ADMIN' ? 'danger' : 'primary'" effect="dark">{{ profile.role }}</el-tag>
        <el-tag :type="profile.status === 'ENABLED' ? 'success' : 'info'" effect="dark">{{ profile.status }}</el-tag>
      </section>

      <section class="panel">
        <div class="section-title">资料设置</div>
        <el-form :model="profile" label-width="90px">
          <el-form-item label="昵称"><el-input v-model="profile.nickname" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="profile.email" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="profile.phone" /></el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveProfile">保存资料</el-button>
          </el-form-item>
        </el-form>
      </section>

      <section class="panel">
        <div class="section-title">修改密码</div>
        <el-form :model="passwordForm" label-width="90px">
          <el-form-item label="旧密码"><el-input v-model="passwordForm.oldPassword" type="password" show-password /></el-form-item>
          <el-form-item label="新密码"><el-input v-model="passwordForm.newPassword" type="password" show-password /></el-form-item>
          <el-form-item label="确认密码"><el-input v-model="passwordForm.confirmPassword" type="password" show-password /></el-form-item>
          <el-form-item>
            <el-button type="warning" @click="savePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updatePassword, updateProfile } from '../api'

const profile = reactive({ id: '', username: '', nickname: '', email: '', phone: '', role: '', status: '' })
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const avatarText = computed(() => (profile.nickname || profile.username || 'U').slice(0, 1).toUpperCase())

async function loadData() {
  Object.assign(profile, await getProfile())
}

async function saveProfile() {
  const data = await updateProfile(profile)
  Object.assign(profile, data)
  localStorage.setItem('user', JSON.stringify(data))
  ElMessage.success('个人资料已更新')
}

async function savePassword() {
  await updatePassword(passwordForm)
  ElMessage.success('密码修改成功')
  Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
}

onMounted(loadData)
</script>

<style scoped>
.profile-grid {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 16px;
}

.profile-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 88px;
  height: 88px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  color: #fff;
  font-size: 34px;
  font-weight: 900;
  background: linear-gradient(135deg, #35d4ff, #6555f6);
}

.profile-card h2,
.profile-card p {
  margin: 0;
}

.profile-card p {
  color: #91a6c8;
}

.section-title {
  margin-bottom: 16px;
  color: #f8fbff;
  font-weight: 800;
}
</style>
