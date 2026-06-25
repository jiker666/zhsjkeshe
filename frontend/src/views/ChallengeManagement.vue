<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">靶场题目管理</h1>
        <div class="page-subtitle">管理员维护趣味靶场题目、状态和提交记录</div>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增题目</el-button>
    </div>

    <section class="panel">
      <div class="toolbar filter-bar">
        <el-input v-model="query.keyword" placeholder="题目标题 / 描述" clearable />
        <el-select v-model="query.category" placeholder="类型" clearable>
          <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable>
          <el-option label="启用" value="ENABLED" />
          <el-option label="禁用" value="DISABLED" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      </div>

      <el-table :data="records" stripe empty-text="暂无题目">
        <el-table-column prop="title" label="题目" min-width="180" />
        <el-table-column prop="category" label="类型" width="130" />
        <el-table-column prop="difficulty" label="难度" width="90" />
        <el-table-column prop="score" label="积分" width="80" />
        <el-table-column prop="passRate" label="通过率" width="100">
          <template #default="{ row }">{{ row.passRate }}%</template>
        </el-table-column>
        <el-table-column prop="submitCount" label="提交" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" effect="dark">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button text type="warning" @click="toggle(row)">{{ row.status === 'ENABLED' ? '禁用' : '启用' }}</el-button>
            <el-button text type="success" @click="openSubmissions(row)">提交记录</el-button>
            <el-button text type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination v-model:current-page="query.page" :total="total" layout="total, prev, pager, next" @current-change="loadData" />
      </div>
    </section>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑题目' : '新增题目'" width="820px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="题目标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.category" style="width:100%">
            <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-segmented v-model="form.difficulty" :options="['简单', '中等', '困难']" />
        </el-form-item>
        <el-form-item label="积分"><el-input-number v-model="form.score" :min="1" :max="100" /></el-form-item>
        <el-form-item label="目标地址"><el-input v-model="form.targetUrl" /></el-form-item>
        <el-form-item label="请求方法"><el-input v-model="form.requestMethod" /></el-form-item>
        <el-form-item label="题目描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="请求示例"><el-input v-model="form.requestExample" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="提示"><el-input v-model="form.hint" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="答案">
          <el-input v-model="form.answer" placeholder="新增必填，编辑时为空则保持原答案" />
        </el-form-item>
        <el-form-item label="解析"><el-input v-model="form.explanation" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="知识库ID"><el-input-number v-model="form.knowledgeId" :min="1" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
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

    <el-dialog v-model="submitVisible" title="提交记录" width="860px">
      <div v-if="firstBlood" class="first-blood">
        <div class="blood-cup">1st</div>
        <div>
          <strong>一血记录：{{ firstBlood.username }}</strong>
          <p>{{ currentChallenge?.title }} 于 {{ firstBlood.submitTime }} 首次正确提交，可作为奶茶奖励依据。</p>
        </div>
        <el-tag type="success" effect="dark">+{{ firstBlood.scoreGot }} 分</el-tag>
      </div>
      <el-alert v-else title="当前题目还没有正确提交，一血仍然空缺" type="info" show-icon :closable="false" class="first-empty" />
      <el-table :data="submissions" stripe empty-text="暂无提交">
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="submitAnswer" label="答案" min-width="220" show-overflow-tooltip />
        <el-table-column prop="correct" label="结果" width="90">
          <template #default="{ row }">
            <el-tag :type="row.correct === 1 ? 'success' : 'danger'" effect="dark">{{ row.correct === 1 ? '正确' : '错误' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="scoreGot" label="得分" width="80" />
        <el-table-column prop="usedHint" label="提示" width="80" />
        <el-table-column prop="submitTime" label="提交时间" width="180" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { createAdminChallenge, deleteAdminChallenge, listAdminChallengeSubmissions, listAdminChallenges, updateAdminChallenge, updateAdminChallengeStatus } from '../api'

const categories = ['信息泄露', '水平越权', '弱口令', '接口参数篡改', '敏感信息泄露', 'Agent 推理', '游戏题']
const records = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const submitVisible = ref(false)
const submissions = ref([])
const currentChallenge = ref(null)
const query = reactive({ keyword: '', category: '', status: '', page: 1, size: 10 })
const form = reactive(emptyForm())
const firstBlood = computed(() => {
  const correct = submissions.value
    .filter(item => item.correct === 1)
    .sort((a, b) => new Date(a.submitTime) - new Date(b.submitTime))
  return correct[0] || null
})

function emptyForm() {
  return { id: null, title: '', category: '信息泄露', difficulty: '简单', score: 10, description: '', targetUrl: '', requestMethod: 'GET', requestExample: '', hint: '', answer: '', explanation: '', knowledgeId: 1, status: 'ENABLED' }
}

async function loadData() {
  const data = await listAdminChallenges(query)
  records.value = data.records || []
  total.value = data.total || 0
}

function openCreate() {
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, { ...row, answer: '' })
  dialogVisible.value = true
}

async function save() {
  if (form.id) {
    await updateAdminChallenge(form.id, form)
  } else {
    await createAdminChallenge(form)
  }
  ElMessage.success('题目已保存')
  dialogVisible.value = false
  await loadData()
}

async function toggle(row) {
  await updateAdminChallengeStatus(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED')
  ElMessage.success('状态已更新')
  await loadData()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除题目“${row.title}”？`, '删除确认', { type: 'warning' })
  await deleteAdminChallenge(row.id)
  ElMessage.success('删除成功')
  await loadData()
}

async function openSubmissions(row) {
  const data = await listAdminChallengeSubmissions(row.id, { page: 1, size: 50 })
  currentChallenge.value = row
  submissions.value = data.records || []
  submitVisible.value = true
}

onMounted(loadData)
</script>

<style scoped>
.filter-bar {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) 180px 160px auto;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.first-blood {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 14px;
  padding: 14px;
  border-radius: 14px;
  border: 1px solid rgba(250, 204, 21, 0.35);
  background:
    radial-gradient(circle at 0% 0%, rgba(250, 204, 21, 0.24), transparent 34%),
    rgba(15, 23, 42, 0.82);
}

.blood-cup {
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  color: #422006;
  font-weight: 900;
  background: linear-gradient(135deg, #fde68a, #f59e0b);
  box-shadow: 0 0 28px rgba(245, 158, 11, 0.28);
}

.first-blood strong {
  color: #fef3c7;
}

.first-blood p {
  margin: 4px 0 0;
  color: #cbd5e1;
}

.first-empty {
  margin-bottom: 14px;
}
</style>
