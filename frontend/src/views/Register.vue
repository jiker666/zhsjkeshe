<template>
  <div class="login-page register-page">
    <div class="grid-bg"></div>
    <section class="hero-copy">
      <div class="eyebrow">Create Security Tester Account</div>
      <h1>注册安全测试人员账号</h1>
      <p>注册后默认进入普通用户端，只能管理自己的任务、漏洞、日志和报告。</p>
      <div class="hero-metrics">
        <span>我的任务</span>
        <span>我的漏洞</span>
        <span>我的报告</span>
        <span>个人中心</span>
      </div>
    </section>

    <section class="login-panel">
      <div class="login-title">创建账号</div>
      <div class="login-subtitle">{{ settings.enableRegister ? '默认角色：安全测试人员 USER' : '管理员已关闭自助注册' }}</div>
      <div v-if="!settings.enableRegister" class="closed-box">
        当前平台暂不开放新用户自助注册，请使用演示账号或联系管理员创建账号。
      </div>
      <el-form v-else :model="form" label-position="top">
        <el-form-item label="用户名"><el-input v-model="form.username" size="large" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" size="large" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" size="large" /></el-form-item>
        <el-form-item label="手机号（可选）"><el-input v-model="form.phone" size="large" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" size="large" show-password /></el-form-item>
        <el-form-item label="确认密码"><el-input v-model="form.confirmPassword" type="password" size="large" show-password /></el-form-item>
        <el-button type="primary" size="large" class="login-button" :loading="loading" @click="submit">注册</el-button>
      </el-form>
      <div class="login-links">
        <span>已有账号？</span>
        <el-button link type="primary" @click="$router.push('/login')">返回登录</el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPublicSettings, register } from '../api'

const router = useRouter()
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: '',
  phone: ''
})
const settings = reactive({
  enableRegister: true
})

async function loadSettings() {
  try {
    Object.assign(settings, await getPublicSettings())
  } catch {
    settings.enableRegister = true
  }
}

function validate() {
  if (!form.username) return '用户名不能为空'
  if (!form.password || form.password.length < 6) return '密码至少 6 位'
  if (form.password !== form.confirmPassword) return '两次密码不一致'
  if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(form.email)) return '邮箱格式不正确'
  return ''
}

async function submit() {
  if (!settings.enableRegister) {
    ElMessage.warning('系统已关闭注册')
    return
  }
  const message = validate()
  if (message) {
    ElMessage.warning(message)
    return
  }
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

onMounted(loadSettings)
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 480px;
  gap: 56px;
  align-items: center;
  padding: 48px min(7vw, 96px);
  overflow: hidden;
  background:
    radial-gradient(circle at 18% 18%, rgba(34, 211, 238, 0.22), transparent 28%),
    radial-gradient(circle at 82% 26%, rgba(124, 58, 237, 0.28), transparent 30%),
    linear-gradient(135deg, #030712, #07111f 48%, #0f172a);
}

.grid-bg {
  position: absolute;
  inset: 0;
  opacity: 0.34;
  background-image:
    linear-gradient(rgba(96, 165, 250, 0.18) 1px, transparent 1px),
    linear-gradient(90deg, rgba(96, 165, 250, 0.18) 1px, transparent 1px);
  background-size: 42px 42px;
  mask-image: linear-gradient(to bottom, #000, transparent 90%);
}

.hero-copy,
.login-panel {
  position: relative;
  z-index: 1;
}

.hero-copy {
  color: #f8fafc;
}

.eyebrow {
  display: inline-flex;
  padding: 8px 12px;
  border-radius: 999px;
  color: #7dd3fc;
  border: 1px solid rgba(125, 211, 252, 0.34);
  background: rgba(15, 23, 42, 0.48);
}

.hero-copy h1 {
  max-width: 680px;
  margin: 22px 0 14px;
  font-size: clamp(34px, 5vw, 58px);
  line-height: 1.08;
}

.hero-copy p {
  max-width: 640px;
  margin: 0;
  color: #bfdbfe;
  font-size: 18px;
  line-height: 1.7;
}

.hero-metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 34px;
}

.hero-metrics span {
  padding: 10px 14px;
  border-radius: 8px;
  background: rgba(30, 41, 59, 0.66);
  border: 1px solid rgba(96, 165, 250, 0.24);
  color: #dbeafe;
}

.login-panel {
  width: min(460px, calc(100vw - 36px));
  padding: 28px;
  border-radius: 10px;
  background: rgba(15, 23, 42, 0.78);
  border: 1px solid rgba(125, 211, 252, 0.28);
  box-shadow: 0 24px 90px rgba(0, 0, 0, 0.38);
  backdrop-filter: blur(18px);
}

.login-title {
  font-size: 26px;
  font-weight: 800;
  color: #f8fafc;
}

.login-subtitle {
  margin: 8px 0 22px;
  color: #93c5fd;
}

.login-button {
  width: 100%;
}

.login-links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 14px;
  color: #9fb5d5;
}

.closed-box {
  padding: 18px;
  margin-bottom: 18px;
  border: 1px solid rgba(251, 191, 36, 0.34);
  border-radius: 8px;
  color: #fde68a;
  line-height: 1.7;
  background: rgba(120, 53, 15, 0.24);
}
</style>
