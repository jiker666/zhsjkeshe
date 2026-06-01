<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ isAdmin ? '报告中心' : '我的报告' }}</h1>
        <div class="page-subtitle">{{ isAdmin ? '预览全平台课程设计演示报告' : '只预览和导出我创建任务下的测试报告' }}</div>
      </div>
      <div>
        <el-button :disabled="!report" @click="exportWord">导出 Word</el-button>
        <el-button :disabled="!report" @click="exportHtml">导出 HTML</el-button>
        <el-button type="primary" :disabled="!report" @click="printPdf">打印 / 导出 PDF</el-button>
      </div>
    </div>

    <section class="panel">
      <div class="toolbar filter-bar">
        <el-select v-model="selectedTaskId" placeholder="选择任务" clearable filterable>
          <el-option v-for="task in tasks" :key="task.id" :label="task.taskName" :value="task.id" />
        </el-select>
        <el-button :icon="Search" @click="loadReport">预览报告</el-button>
      </div>

      <div v-if="report" ref="reportRef" class="report-paper">
        <div class="report-cover">
          <div class="report-badge">课程设计演示用报告</div>
          <h1>{{ report.title }}</h1>
          <p>AI Agent Driven Security Testing Platform</p>
        </div>
        <article class="markdown-body" v-html="reportHtml"></article>
      </div>
      <el-empty v-else description="请选择已执行并生成报告的任务" />
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { exportReportDocx, getReportByTask, listTasks } from '../api'

const route = useRoute()
const tasks = ref([])
const report = ref(null)
const reportRef = ref()
const selectedTaskId = ref(route.params.taskId ? Number(route.params.taskId) : '')
const user = JSON.parse(localStorage.getItem('user') || '{}')
const isAdmin = user.role === 'ADMIN'

const reportHtml = computed(() => markdownToHtml(report.value?.content || ''))

function markdownToHtml(markdown) {
  return markdown
    .replace(/```(\w+)?\n([\s\S]*?)```/g, '<pre class="code">$2</pre>')
    .replace(/^# (.*)$/gm, '<h1>$1</h1>')
    .replace(/^## (.*)$/gm, '<h2>$1</h2>')
    .replace(/^### (.*)$/gm, '<h3>$1</h3>')
    .replace(/^\> (.*)$/gm, '<blockquote>$1</blockquote>')
    .replace(/\|(.+)\|\n\|(.+)\|/g, '<table>')
    .replace(/^- (.*)$/gm, '<li>$1</li>')
    .replace(/\n/g, '<br />')
}

async function loadReport() {
  if (!selectedTaskId.value) {
    report.value = null
    return
  }
  const data = await getReportByTask(selectedTaskId.value)
  if (!data) {
    ElMessage.warning('该任务尚未生成报告，请先执行测试')
  }
  report.value = data
}

function exportHtml() {
  const html = `<!doctype html><html><head><meta charset="utf-8"><title>${report.value.title}</title><style>body{font-family:Arial,"Microsoft YaHei",sans-serif;line-height:1.7;padding:32px;color:#111827}.code,pre{background:#f3f4f6;padding:12px;border-radius:6px;white-space:pre-wrap}h1,h2,h3{color:#0f172a}</style></head><body>${reportRef.value.innerHTML}</body></html>`
  const blob = new Blob([html], { type: 'text/html;charset=utf-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `${report.value.title}.html`
  link.click()
  URL.revokeObjectURL(link.href)
}

async function exportWord() {
  const response = await exportReportDocx(report.value.id)
  const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `${report.value.title}.docx`
  link.click()
  URL.revokeObjectURL(link.href)
}

function printPdf() {
  window.print()
}

onMounted(async () => {
  const data = await listTasks({})
  tasks.value = data.records || data
  if (selectedTaskId.value) {
    await loadReport()
  }
})
</script>

<style scoped>
.filter-bar {
  justify-content: flex-start;
}

.filter-bar .el-select {
  width: 380px;
}

.report-paper {
  max-width: 960px;
  margin: 18px auto 0;
  padding: 34px;
  border-radius: 16px;
  background: #f8fafc;
  color: #111827;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.28);
}

.report-cover {
  padding: 28px;
  margin-bottom: 24px;
  border-radius: 14px;
  color: #fff;
  background: linear-gradient(135deg, #0f172a, #1d4ed8 52%, #7c3aed);
}

.report-badge {
  display: inline-block;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
}

.report-cover h1 {
  margin: 18px 0 8px;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  color: #0f172a;
}

.markdown-body :deep(blockquote) {
  margin: 12px 0;
  padding: 12px 14px;
  border-left: 4px solid #2563eb;
  background: #eff6ff;
}

.markdown-body :deep(.code) {
  white-space: pre-wrap;
  padding: 14px;
  border-radius: 8px;
  color: #d1fae5;
  background: #020617;
}

@media print {
  .page-header,
  .filter-bar,
  :global(.sidebar),
  :global(.topbar) {
    display: none !important;
  }

  .page,
  .panel {
    padding: 0;
    background: #fff;
    box-shadow: none;
  }

  .report-paper {
    box-shadow: none;
    margin: 0;
    max-width: none;
  }
}
</style>
