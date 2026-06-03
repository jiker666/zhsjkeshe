import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import MainLayout from '../layout/MainLayout.vue'
import AdminDashboard from '../views/AdminDashboard.vue'
import UserDashboard from '../views/UserDashboard.vue'
import TaskList from '../views/TaskList.vue'
import TaskCreate from '../views/TaskCreate.vue'
import TaskDetail from '../views/TaskDetail.vue'
import ResultList from '../views/ResultList.vue'
import Logs from '../views/Logs.vue'
import ReportPreview from '../views/ReportPreview.vue'
import SystemInfo from '../views/SystemInfo.vue'
import UserManagement from '../views/UserManagement.vue'
import Profile from '../views/Profile.vue'
import SystemSettings from '../views/SystemSettings.vue'
import Forbidden from '../views/Forbidden.vue'
import TicketManagement from '../views/TicketManagement.vue'
import SecurityKnowledge from '../views/SecurityKnowledge.vue'
import AuditLog from '../views/AuditLog.vue'
import ChallengeArena from '../views/ChallengeArena.vue'
import ChallengeDetail from '../views/ChallengeDetail.vue'
import MyChallengeStats from '../views/MyChallengeStats.vue'
import ChallengeManagement from '../views/ChallengeManagement.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login },
    { path: '/register', component: Register },
    {
      path: '/',
      component: MainLayout,
      redirect: () => roleHome(),
      children: [
        { path: 'dashboard', redirect: () => roleHome() },
        { path: 'admin/dashboard', component: AdminDashboard, meta: { role: 'ADMIN' } },
        { path: 'user/dashboard', component: UserDashboard },
        { path: 'admin/users', component: UserManagement, meta: { role: 'ADMIN' } },
        { path: 'admin/settings', component: SystemSettings, meta: { role: 'ADMIN' } },
        { path: 'tasks', component: TaskList },
        { path: 'tasks/new', component: TaskCreate },
        { path: 'tasks/:id', component: TaskDetail },
        { path: 'results', component: ResultList },
        { path: 'tickets', component: TicketManagement },
        { path: 'logs', component: Logs },
        { path: 'reports/:taskId?', component: ReportPreview },
        { path: 'knowledge', component: SecurityKnowledge },
        { path: 'challenges', component: ChallengeArena },
        { path: 'challenges/:id', component: ChallengeDetail },
        { path: 'challenge-stats', component: MyChallengeStats },
        { path: 'admin/challenges', component: ChallengeManagement, meta: { role: 'ADMIN' } },
        { path: 'admin/audit-logs', component: AuditLog, meta: { role: 'ADMIN' } },
        { path: 'profile', component: Profile },
        { path: 'forbidden', component: Forbidden },
        { path: 'system-info', component: SystemInfo }
      ]
    },
    { path: '/:pathMatch(.*)*', redirect: () => (localStorage.getItem('token') ? roleHome() : '/login') }
  ]
})

router.beforeEach((to) => {
  if (!['/login', '/register'].includes(to.path) && !localStorage.getItem('token')) {
    return '/login'
  }
  if (['/login', '/register'].includes(to.path) && localStorage.getItem('token')) {
    return roleHome()
  }
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  if (to.meta.role && user.role !== to.meta.role) {
    return '/forbidden'
  }
})

function roleHome() {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  return user.role === 'ADMIN' ? '/admin/dashboard' : '/user/dashboard'
}

export default router
