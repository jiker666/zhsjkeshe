import request from './request'
import axios from 'axios'

export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)
export const getHealth = () => request.get('/health')
export const getPublicSettings = () => request.get('/settings/public')
export const getSystemSettings = () => request.get('/admin/settings')
export const updateSystemSettings = (data) => request.put('/admin/settings', data)
export const resetSystemSettings = () => request.post('/admin/settings/reset')
export const getDashboardStats = () => request.get('/dashboard/stats')

export const listTasks = (params) => request.get('/tasks', { params })
export const getTask = (id) => request.get(`/tasks/${id}`)
export const createTask = (data) => request.post('/tasks', data)
export const updateTask = (id, data) => request.put(`/tasks/${id}`, data)
export const deleteTask = (id) => request.delete(`/tasks/${id}`)
export const executeTask = (id) => request.post(`/tasks/${id}/execute`)
export const getAgentDecisions = (taskId) => request.get(`/tasks/${taskId}/agent-decisions`)

export const listResults = (params) => request.get('/results', { params })
export const getResult = (id) => request.get(`/results/${id}`)
export const updateResultStatus = (id, status) => request.put(`/results/${id}/status`, null, { params: { status } })
export const deleteResult = (id) => request.delete(`/results/${id}`)
export const listLogs = (params) => request.get('/logs', { params })
export const getReportByTask = (taskId) => request.get(`/reports/task/${taskId}`)
export const listReports = (params) => request.get('/reports/page', { params })
export const deleteReport = (id) => request.delete(`/reports/${id}`)

export const listTickets = (params) => request.get('/tickets/page', { params })
export const createTicketFromResult = (resultId) => request.post(`/tickets/from-result/${resultId}`)
export const updateTicket = (id, data) => request.put(`/tickets/${id}`, data)
export const updateTicketStatus = (id, status) => request.put(`/tickets/${id}/status`, null, { params: { status } })
export const retestTicket = (id) => request.post(`/tickets/${id}/retest`)

export const listKnowledge = (params) => request.get('/knowledge/page', { params })
export const getKnowledge = (id) => request.get(`/knowledge/${id}`)
export const getKnowledgeByType = (vulnType) => request.get(`/knowledge/by-type/${encodeURIComponent(vulnType)}`)
export const listAuditLogs = (params) => request.get('/admin/audit-logs/page', { params })

export const listChallenges = (params) => request.get('/challenges/page', { params })
export const getChallenge = (id) => request.get(`/challenges/${id}`)
export const submitChallenge = (id, data) => request.post(`/challenges/${id}/submit`, data)
export const getChallengeHint = (id) => request.get(`/challenges/${id}/hint`)
export const getChallengeAnalysis = (id) => request.post(`/challenges/${id}/agent-analyze`)
export const getMyChallengeStats = () => request.get('/challenges/my/stats')
export const listMyChallengeSubmissions = (params) => request.get('/challenges/my/submissions', { params })
export const listMyChallengeAchievements = () => request.get('/challenges/my/achievements')

export const listAdminChallenges = (params) => request.get('/admin/challenges/page', { params })
export const createAdminChallenge = (data) => request.post('/admin/challenges', data)
export const updateAdminChallenge = (id, data) => request.put(`/admin/challenges/${id}`, data)
export const updateAdminChallengeStatus = (id, status) => request.put(`/admin/challenges/${id}/status`, null, { params: { status } })
export const deleteAdminChallenge = (id) => request.delete(`/admin/challenges/${id}`)
export const listAdminChallengeSubmissions = (id, params) => request.get(`/admin/challenges/${id}/submissions`, { params })

export const listUsers = (params) => request.get('/admin/users/page', { params })
export const createUser = (data) => request.post('/admin/users', data)
export const updateUser = (id, data) => request.put(`/admin/users/${id}`, data)
export const updateUserStatus = (id, status) => request.put(`/admin/users/${id}/status`, null, { params: { status } })
export const resetUserPassword = (id, password) => request.put(`/admin/users/${id}/password`, { password })
export const deleteUser = (id) => request.delete(`/admin/users/${id}`)

export const getProfile = () => request.get('/user/profile')
export const updateProfile = (data) => request.put('/user/profile', data)
export const updatePassword = (data) => request.put('/user/password', data)

export const exportReportDocx = (reportId) => {
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
  const token = localStorage.getItem('token')
  return axios.get(`${baseURL}/reports/${reportId}/export/docx`, {
    responseType: 'blob',
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  })
}
