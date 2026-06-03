<template>
  <div class="login-page">
    <div class="grid-bg"></div>
    <section class="hero-copy">
      <div class="eyebrow">Security Agent Console</div>
      <h1>自主安全测试智能体可视化平台</h1>
      <p>AI Agent Driven Security Testing Platform</p>
      <div class="hero-metrics">
        <span>任务编排</span>
        <span>模拟执行</span>
        <span>风险分析</span>
        <span>报告生成</span>
      </div>
    </section>
    <section class="login-panel">
      <div class="login-title">欢迎登录</div>
      <div class="login-subtitle">进入安全测试控制台</div>
      <el-form :model="form" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" size="large" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" class="login-button" :loading="loading" @click="submit">
          登录
        </el-button>
      </el-form>
      <div v-if="settings.enableRegister" class="login-links">
        <span>没有账号？</span>
        <el-button link type="primary" @click="$router.push('/register')">立即注册</el-button>
      </div>
      <div v-else class="register-disabled">当前已关闭自助注册，请联系管理员创建账号</div>
      <div class="demo-account">管理员：admin / 123456　普通用户：user / 123456</div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPublicSettings, login } from '../api'

const router = useRouter()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: '123456'
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

async function submit() {
  loading.value = true
  try {
    const data = await login(form)
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user))
    ElMessage.success('登录成功')
    router.push(data.user.role === 'ADMIN' ? '/admin/dashboard' : '/user/dashboard')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

onMounted(loadSettings)
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 440px;
  gap: 56px;
  align-items: center;
  padding: 60px min(7vw, 96px);
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
  font-size: clamp(36px, 5vw, 64px);
  line-height: 1.08;
}

.hero-copy p {
  margin: 0;
  color: #bfdbfe;
  font-size: 20px;
}

.hero-metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 34px;
}

.hero-metrics span {
  padding: 10px 14px;
  border-radius: 10px;
  background: rgba(30, 41, 59, 0.66);
  border: 1px solid rgba(96, 165, 250, 0.24);
  color: #dbeafe;
}

.login-panel {
  width: min(420px, calc(100vw - 36px));
  padding: 30px;
  border-radius: 18px;
  background: rgba(15, 23, 42, 0.76);
  border: 1px solid rgba(125, 211, 252, 0.28);
  box-shadow: 0 24px 90px rgba(0, 0, 0, 0.38);
  backdrop-filter: blur(18px);
}

.login-title {
  font-size: 28px;
  font-weight: 800;
  color: #f8fafc;
}

.login-subtitle {
  margin: 8px 0 26px;
  color: #93c5fd;
}

.login-button {
  width: 100%;
}

.demo-account {
  margin-top: 16px;
  color: #93c5fd;
  font-size: 13px;
}

.login-links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 14px;
  color: #9fb5d5;
}

.register-disabled {
  margin-top: 14px;
  text-align: center;
  color: #fbbf24;
  font-size: 13px;
}

@media (max-width: 920px) {
  .login-page {
    grid-template-columns: 1fr;
    padding: 32px 18px;
  }

  .hero-copy h1 {
    font-size: 34px;
  }
}
</style>
