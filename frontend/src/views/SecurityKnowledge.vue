<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">安全知识库</h1>
        <div class="page-subtitle">沉淀漏洞成因、风险影响与修复建议，为 Agent 生成建议提供知识来源</div>
      </div>
    </div>

    <section class="panel">
      <div class="toolbar filter-bar">
        <el-input v-model="query.keyword" placeholder="搜索知识标题 / 描述" clearable />
        <el-select v-model="query.riskLevel" placeholder="风险等级" clearable>
          <el-option label="高危" value="HIGH" />
          <el-option label="中危" value="MEDIUM" />
          <el-option label="低危" value="LOW" />
        </el-select>
        <el-input v-model="query.vulnType" placeholder="漏洞类型" clearable />
        <el-button :icon="Search" @click="loadData">查询</el-button>
      </div>

      <div v-loading="loading" class="knowledge-grid">
        <article v-for="item in records" :key="item.id" class="knowledge-card" @click="open(item)">
          <div class="card-top">
            <el-tag :type="riskTag(item.riskLevel)" effect="dark">{{ item.riskLevel }}</el-tag>
            <span>{{ item.vulnType }}</span>
          </div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.description }}</p>
          <div class="advice">{{ item.fixAdvice }}</div>
        </article>
      </div>
      <el-empty v-if="!loading && records.length === 0" description="暂无知识库记录" />

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

    <el-drawer v-model="drawerVisible" title="知识库详情" size="520px">
      <template v-if="current">
        <div class="knowledge-detail">
          <el-tag :type="riskTag(current.riskLevel)" effect="dark">{{ current.riskLevel }}</el-tag>
          <h2>{{ current.title }}</h2>
          <p class="type">{{ current.vulnType }}</p>
          <h3>漏洞说明</h3>
          <p>{{ current.description }}</p>
          <h3>形成原因</h3>
          <p>{{ current.cause }}</p>
          <h3>影响范围</h3>
          <p>{{ current.impact }}</p>
          <h3>修复建议</h3>
          <p>{{ current.fixAdvice }}</p>
          <h3>参考</h3>
          <p>{{ current.reference }}</p>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { listKnowledge } from '../api'

const route = useRoute()
const loading = ref(false)
const records = ref([])
const total = ref(0)
const drawerVisible = ref(false)
const current = ref(null)
const query = reactive({ keyword: '', riskLevel: '', vulnType: route.query.type || '', page: 1, size: 12 })

function riskTag(level) {
  return level === 'HIGH' ? 'danger' : level === 'MEDIUM' ? 'warning' : 'success'
}

function open(item) {
  current.value = item
  drawerVisible.value = true
}

async function loadData() {
  loading.value = true
  try {
    const data = await listKnowledge(query)
    records.value = data.records || []
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

.filter-bar .el-input {
  width: 260px;
}

.filter-bar .el-select {
  width: 160px;
}

.knowledge-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  min-height: 220px;
}

.knowledge-card {
  min-height: 230px;
  padding: 18px;
  border-radius: 16px;
  cursor: pointer;
  background: linear-gradient(145deg, rgba(15, 23, 42, 0.92), rgba(30, 41, 59, 0.64));
  border: 1px solid rgba(96, 165, 250, 0.22);
  transition: transform 0.16s ease, border-color 0.16s ease;
}

.knowledge-card:hover {
  transform: translateY(-4px);
  border-color: rgba(34, 211, 238, 0.48);
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #93c5fd;
}

.knowledge-card h3 {
  margin: 16px 0 10px;
  color: #f8fafc;
}

.knowledge-card p {
  color: #cbd5e1;
  line-height: 1.7;
}

.advice {
  margin-top: 12px;
  padding: 10px;
  border-radius: 10px;
  color: #d1fae5;
  background: rgba(16, 185, 129, 0.12);
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.knowledge-detail h2 {
  margin: 14px 0 4px;
  color: #f8fafc;
}

.knowledge-detail h3 {
  margin: 22px 0 8px;
  color: #bfdbfe;
}

.knowledge-detail p {
  color: #cbd5e1;
  line-height: 1.8;
}

.type {
  color: #67e8f9 !important;
}

@media (max-width: 1200px) {
  .knowledge-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .knowledge-grid {
    grid-template-columns: 1fr;
  }
}
</style>
