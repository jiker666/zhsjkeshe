<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">趣味安全靶场</h1>
        <div class="page-subtitle">用本地模拟题练习信息泄露、越权、弱口令、参数篡改和 Agent 推理</div>
      </div>
      <el-button type="primary" @click="$router.push('/challenge-stats')">我的成绩</el-button>
    </div>

    <section class="panel">
      <div class="toolbar filter-bar">
        <el-input v-model="query.keyword" placeholder="搜索题目" clearable />
        <el-select v-model="query.category" placeholder="题目类型" clearable>
          <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="query.difficulty" placeholder="难度" clearable>
          <el-option label="简单" value="简单" />
          <el-option label="中等" value="中等" />
          <el-option label="困难" value="困难" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      </div>

      <div v-loading="loading" class="challenge-grid">
        <article v-for="item in challenges" :key="item.id" class="challenge-card" :class="{ solved: item.solved }">
          <div class="card-top">
            <el-tag :type="difficultyTag(item.difficulty)" effect="dark">{{ item.difficulty }}</el-tag>
            <span class="score">+{{ item.score }} 分</span>
          </div>
          <h2>{{ item.title }}</h2>
          <p>{{ item.description }}</p>
          <div class="meta-row">
            <el-tag effect="plain">{{ item.category }}</el-tag>
            <span>通过率 {{ item.passRate }}%</span>
          </div>
          <div class="card-bottom">
            <span :class="item.solved ? 'done' : 'todo'">{{ item.solved ? '已完成' : '待挑战' }}</span>
            <el-button type="primary" @click="$router.push(`/challenges/${item.id}`)">开始挑战</el-button>
          </div>
        </article>
      </div>
      <el-empty v-if="!loading && challenges.length === 0" description="暂无题目" />

      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
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
import { listChallenges } from '../api'

const categories = ['信息泄露', '水平越权', '弱口令', '接口参数篡改', '敏感信息泄露', 'Agent 推理', '游戏题']
const loading = ref(false)
const challenges = ref([])
const total = ref(0)
const query = reactive({ keyword: '', category: '', difficulty: '', page: 1, size: 12 })

function difficultyTag(value) {
  if (value === '困难') return 'danger'
  if (value === '中等') return 'warning'
  return 'success'
}

async function loadData() {
  loading.value = true
  try {
    const data = await listChallenges(query)
    challenges.value = data.records || []
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
  width: 170px;
}

.challenge-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  min-height: 260px;
}

.challenge-card {
  position: relative;
  overflow: hidden;
  min-height: 280px;
  padding: 20px;
  border-radius: 16px;
  background:
    radial-gradient(circle at 90% 0%, rgba(34, 211, 238, 0.18), transparent 32%),
    linear-gradient(145deg, rgba(15, 23, 42, 0.94), rgba(30, 41, 59, 0.72));
  border: 1px solid rgba(96, 165, 250, 0.24);
  box-shadow: 0 18px 44px rgba(0, 0, 0, 0.26);
}

.challenge-card.solved {
  border-color: rgba(56, 217, 150, 0.52);
}

.card-top,
.meta-row,
.card-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.score {
  color: #facc15;
  font-weight: 900;
}

.challenge-card h2 {
  margin: 18px 0 10px;
  color: #f8fafc;
}

.challenge-card p {
  min-height: 94px;
  color: #cbd5e1;
  line-height: 1.75;
}

.meta-row {
  color: #93c5fd;
}

.card-bottom {
  margin-top: 18px;
}

.done {
  color: #86efac;
  font-weight: 800;
}

.todo {
  color: #bfdbfe;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 1100px) {
  .challenge-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 760px) {
  .challenge-grid {
    grid-template-columns: 1fr;
  }
}
</style>
