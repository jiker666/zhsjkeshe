<template>
  <el-container class="app-shell">
    <el-aside width="232px" class="sidebar">
      <div class="brand">
        <div class="brand-mark">AI</div>
        <div>
          <div class="brand-title">安全测试控制台</div>
          <div class="brand-subtitle">Agent Workbench</div>
        </div>
      </div>
      <el-menu :default-active="$route.path" router class="side-menu">
        <el-menu-item v-for="item in menus" :key="item.index" :index="item.index">
          <el-icon><component :is="item.icon" /></el-icon><span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div>
          <div class="topbar-title">自主安全测试智能体可视化平台</div>
          <div class="topbar-subtitle">AI Agent Driven Security Testing Platform</div>
        </div>
        <div class="topbar-actions">
          <el-tag effect="dark" :type="backendUp ? 'success' : 'danger'">后端{{ backendUp ? '在线' : '离线' }}</el-tag>
          <el-tag effect="dark" :type="dbUp ? 'success' : 'warning'">数据库{{ dbUp ? '正常' : '异常' }}</el-tag>
          <span class="clock">{{ currentTime }}</span>
          <span class="user">{{ user.nickname || user.username || 'admin' }} · {{ roleText }}</span>
          <el-button :icon="SwitchButton" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { CirclePlus, DataAnalysis, Document, InfoFilled, Reading, SwitchButton, Tickets, Warning } from '@element-plus/icons-vue'
import { getHealth } from '../api'

const router = useRouter()
const currentTime = ref('')
const backendUp = ref(false)
const dbUp = ref(false)
const user = reactive(JSON.parse(localStorage.getItem('user') || '{}'))
const roleText = computed(() => user.role === 'ADMIN' ? '管理员' : '安全测试人员')
const adminMenus = [
  { index: '/admin/dashboard', label: '管理员首页', icon: DataAnalysis },
  { index: '/admin/users', label: '用户管理', icon: Tickets },
  { index: '/tasks', label: '全部测试任务', icon: Tickets },
  { index: '/tasks/new', label: '新建任务', icon: CirclePlus },
  { index: '/results', label: '全部漏洞结果', icon: Warning },
  { index: '/tickets', label: '整改工单', icon: Tickets },
  { index: '/logs', label: '测试日志', icon: Document },
  { index: '/reports', label: '报告中心', icon: Reading },
  { index: '/knowledge', label: '安全知识库', icon: Reading },
  { index: '/challenges', label: '趣味靶场', icon: DataAnalysis },
  { index: '/admin/challenges', label: '靶场题目管理', icon: Tickets },
  { index: '/admin/audit-logs', label: '操作审计', icon: Document },
  { index: '/admin/settings', label: '系统配置', icon: InfoFilled },
  { index: '/profile', label: '个人中心', icon: InfoFilled },
  { index: '/system-info', label: '系统说明', icon: InfoFilled }
]
const userMenus = [
  { index: '/user/dashboard', label: '用户首页', icon: DataAnalysis },
  { index: '/tasks', label: '我的测试任务', icon: Tickets },
  { index: '/tasks/new', label: '新建任务', icon: CirclePlus },
  { index: '/results', label: '我的漏洞结果', icon: Warning },
  { index: '/tickets', label: '我的整改工单', icon: Tickets },
  { index: '/logs', label: '我的测试日志', icon: Document },
  { index: '/reports', label: '我的报告', icon: Reading },
  { index: '/knowledge', label: '安全知识库', icon: Reading },
  { index: '/challenges', label: '趣味靶场', icon: DataAnalysis },
  { index: '/challenge-stats', label: '我的靶场成绩', icon: Tickets },
  { index: '/profile', label: '个人中心', icon: InfoFilled },
  { index: '/system-info', label: '系统说明', icon: InfoFilled }
]
const menus = computed(() => user.role === 'ADMIN' ? adminMenus : userMenus)
let timer
let healthTimer

function tick() {
  currentTime.value = new Date().toLocaleString('zh-CN', { hour12: false })
}

async function loadHealth() {
  try {
    const data = await getHealth()
    backendUp.value = data.status === 'UP'
    dbUp.value = String(data.database || '').startsWith('UP')
  } catch {
    backendUp.value = false
    dbUp.value = false
  }
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

onMounted(() => {
  tick()
  loadHealth()
  timer = setInterval(tick, 1000)
  healthTimer = setInterval(loadHealth, 15000)
})

onBeforeUnmount(() => {
  clearInterval(timer)
  clearInterval(healthTimer)
})
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: #07111f;
}

.sidebar {
  background:
    linear-gradient(180deg, rgba(15, 23, 42, 0.98), rgba(7, 17, 31, 0.98)),
    radial-gradient(circle at 30% 0%, rgba(59, 130, 246, 0.22), transparent 38%);
  border-right: 0;
  box-shadow: inset -1px 0 0 rgba(96, 165, 250, 0.18);
}

.brand {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  color: #fff;
}

.brand-mark {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  background: linear-gradient(135deg, #22d3ee, #7c3aed);
  font-weight: 900;
}

.brand-title {
  font-size: 16px;
  font-weight: 800;
}

.brand-subtitle {
  margin-top: 2px;
  font-size: 11px;
  color: #93c5fd;
}

.side-menu {
  border-right: 0;
  background: transparent;
}

.side-menu :deep(.el-menu-item) {
  color: #a8c3e8;
  margin: 4px 10px;
  border-radius: 10px;
}

.side-menu :deep(.el-menu-item.is-active),
.side-menu :deep(.el-menu-item:hover) {
  color: #fff;
  background: linear-gradient(90deg, rgba(37, 99, 235, 0.42), rgba(124, 58, 237, 0.28));
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  background: rgba(15, 23, 42, 0.86);
  border-bottom: 1px solid rgba(96, 165, 250, 0.18);
  backdrop-filter: blur(14px);
}

.topbar-title {
  font-weight: 700;
  color: #f8fafc;
}

.topbar-subtitle {
  margin-top: 2px;
  font-size: 12px;
  color: #7dd3fc;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #bfdbfe;
}

.clock {
  min-width: 170px;
  color: #93c5fd;
}

.user {
  padding: 6px 10px;
  border: 1px solid rgba(96, 165, 250, 0.22);
  border-radius: 999px;
  background: rgba(30, 41, 59, 0.7);
}

.main {
  padding: 0;
  background: #07111f;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
