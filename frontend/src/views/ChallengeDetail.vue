<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ challenge.title || '靶场题目' }}</h1>
        <div class="page-subtitle">本题仅使用本地模拟数据，用于安全教育和课程设计演示</div>
      </div>
      <div class="header-actions">
        <el-button @click="$router.push('/challenges')">返回靶场</el-button>
        <el-button type="success" @click="$router.push('/challenge-stats')">我的成绩</el-button>
      </div>
    </div>

    <div v-if="successVisible" class="confetti-layer">
      <span v-for="i in 36" :key="i" :style="pieceStyle(i)"></span>
    </div>

    <div class="detail-layout">
      <section class="panel">
        <div class="challenge-head">
          <div>
            <el-tag effect="dark">{{ challenge.category }}</el-tag>
            <el-tag :type="difficultyTag(challenge.difficulty)" effect="dark">{{ challenge.difficulty }}</el-tag>
          </div>
          <strong>+{{ challenge.score }} 分</strong>
        </div>
        <h2>题目背景</h2>
        <p class="story">{{ challenge.description }}</p>
        <h2>模拟请求信息</h2>
        <pre class="code-block">{{ challenge.requestExample }}</pre>
        <div class="target-box">
          <span>目标地址</span>
          <code>{{ challenge.targetUrl }}</code>
        </div>

        <div class="answer-box" :class="{ shake: shaking }">
          <el-input v-model="answer" placeholder="请输入 flag{...}" @keyup.enter="submit" />
          <el-button type="primary" :loading="submitting" @click="submit">提交答案</el-button>
        </div>
        <div class="actions-row">
          <el-button @click="showHint">查看提示</el-button>
          <el-button type="warning" @click="loadAnalysis">让 Agent 分析思路</el-button>
          <el-button v-if="challenge.explanation" type="success" @click="explanationVisible = true">查看解析</el-button>
        </div>
      </section>

      <aside class="side-stack">
        <section class="panel mini-panel">
          <div class="section-title">关联知识点</div>
          <p>题目关联知识库 ID：{{ challenge.knowledgeId || '-' }}</p>
          <el-button type="primary" @click="$router.push(`/knowledge?id=${challenge.knowledgeId || ''}&type=${encodeURIComponent(challenge.category || '')}`)">查看知识库</el-button>
        </section>

        <section v-if="hint" class="panel mini-panel hint-panel">
          <div class="section-title">提示</div>
          <p>{{ hint }}</p>
        </section>

        <section v-if="analysis" class="panel mini-panel">
          <div class="section-title">Agent 分析</div>
          <p>{{ analysis.taskUnderstanding }}</p>
          <ul>
            <li v-for="item in analysis.keyClues" :key="item">{{ item }}</li>
          </ul>
          <p>{{ analysis.recommendedApproach }}</p>
          <p class="muted">关联知识：{{ analysis.relatedKnowledge }}</p>
          <p class="risk-note">{{ analysis.riskNote }}</p>
        </section>
      </aside>
    </div>

    <el-dialog v-model="successVisible" width="560px" class="success-dialog" :show-close="false">
      <div class="success-box">
        <div class="success-medal">✓</div>
        <h2>挑战成功！</h2>
        <p>{{ submitResult.message }}</p>
        <strong>获得积分：+{{ submitResult.scoreGot }}</strong>
        <div v-if="submitResult.unlockedAchievements?.length" class="badges">
          <div v-for="item in submitResult.unlockedAchievements" :key="item.id" class="badge">
            <span>{{ item.icon }}</span>
            <div>
              <b>{{ item.achievementName }}</b>
              <small>{{ item.achievementDesc }}</small>
            </div>
          </div>
        </div>
        <div class="dialog-actions">
          <el-button @click="explanationVisible = true">查看解析</el-button>
          <el-button type="primary" @click="nextChallenge">继续下一题</el-button>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="explanationVisible" title="题目解析" width="720px">
      <p class="explanation">{{ submitResult.explanation || challenge.explanation || '答对后显示解析。' }}</p>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getChallenge, getChallengeAnalysis, getChallengeHint, submitChallenge } from '../api'

const route = useRoute()
const router = useRouter()
const challenge = reactive({})
const answer = ref('')
const hint = ref('')
const analysis = ref(null)
const usedHint = ref(false)
const submitting = ref(false)
const shaking = ref(false)
const successVisible = ref(false)
const explanationVisible = ref(false)
const submitResult = reactive({})

function difficultyTag(value) {
  if (value === '困难') return 'danger'
  if (value === '中等') return 'warning'
  return 'success'
}

function pieceStyle(i) {
  const colors = ['#38bdf8', '#a78bfa', '#facc15', '#34d399', '#fb7185']
  return {
    left: `${(i * 37) % 100}%`,
    background: colors[i % colors.length],
    animationDelay: `${(i % 9) * 0.08}s`,
    transform: `rotate(${i * 19}deg)`
  }
}

async function loadData() {
  Object.assign(challenge, await getChallenge(route.params.id))
}

async function showHint() {
  const data = await getChallengeHint(route.params.id)
  hint.value = data.hint
  usedHint.value = true
}

async function loadAnalysis() {
  analysis.value = await getChallengeAnalysis(route.params.id)
}

async function submit() {
  submitting.value = true
  try {
    const data = await submitChallenge(route.params.id, { answer: answer.value, usedHint: usedHint.value })
    Object.assign(submitResult, data)
    if (data.correct) {
      successVisible.value = true
      await loadData()
    } else {
      ElMessage.warning(data.message)
      shaking.value = true
      setTimeout(() => { shaking.value = false }, 520)
    }
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submitting.value = false
  }
}

function nextChallenge() {
  const next = Number(route.params.id) + 1
  successVisible.value = false
  router.push(`/challenges/${next > 6 ? 1 : next}`)
}

onMounted(loadData)
</script>

<style scoped>
.header-actions,
.challenge-head,
.answer-box,
.actions-row,
.dialog-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.detail-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 16px;
}

.challenge-head {
  justify-content: space-between;
}

.challenge-head strong {
  color: #facc15;
  font-size: 28px;
}

h2 {
  color: #f8fafc;
}

.story,
.mini-panel p,
.mini-panel li,
.explanation {
  color: #cbd5e1;
  line-height: 1.8;
}

.code-block {
  min-height: 130px;
  padding: 14px;
  border-radius: 12px;
  color: #d1fae5;
  background: #020617;
  border: 1px solid rgba(34, 211, 238, 0.22);
  white-space: pre-wrap;
}

.target-box {
  display: grid;
  gap: 8px;
  margin: 16px 0;
  color: #93c5fd;
}

.target-box code {
  padding: 10px;
  border-radius: 10px;
  color: #e0f2fe;
  background: rgba(59, 130, 246, 0.12);
}

.answer-box {
  margin-top: 18px;
}

.answer-box .el-input {
  max-width: 460px;
}

.shake {
  animation: shake 0.5s ease;
}

.side-stack {
  display: grid;
  gap: 16px;
}

.section-title {
  margin-bottom: 12px;
  color: #e0f2fe;
  font-weight: 800;
}

.hint-panel {
  border-color: rgba(250, 204, 21, 0.34);
}

.risk-note {
  color: #facc15 !important;
}

.confetti-layer {
  pointer-events: none;
  position: fixed;
  inset: 0;
  z-index: 3000;
  overflow: hidden;
}

.confetti-layer span {
  position: absolute;
  top: -20px;
  width: 10px;
  height: 18px;
  border-radius: 3px;
  animation: confetti 1.7s ease-out forwards;
}

.success-box {
  text-align: center;
}

.success-medal {
  width: 74px;
  height: 74px;
  display: grid;
  place-items: center;
  margin: 0 auto 14px;
  border-radius: 50%;
  color: #052e16;
  font-size: 40px;
  font-weight: 900;
  background: linear-gradient(135deg, #86efac, #22d3ee);
  box-shadow: 0 0 40px rgba(34, 211, 238, 0.38);
  animation: medalPop 0.5s ease;
}

.success-box h2 {
  margin: 0 0 8px;
}

.success-box strong {
  display: block;
  color: #facc15;
  font-size: 22px;
}

.badges {
  display: grid;
  gap: 10px;
  margin: 18px 0;
}

.badge {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  background: rgba(56, 217, 150, 0.12);
  border: 1px solid rgba(56, 217, 150, 0.28);
  text-align: left;
}

.badge span {
  font-size: 24px;
}

.badge b,
.badge small {
  display: block;
}

.badge small {
  color: #bfdbfe;
}

@keyframes confetti {
  to {
    top: 100vh;
    transform: translateX(80px) rotate(520deg);
    opacity: 0;
  }
}

@keyframes medalPop {
  0% { transform: scale(0.3); opacity: 0; }
  70% { transform: scale(1.12); }
  100% { transform: scale(1); opacity: 1; }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20%, 60% { transform: translateX(-8px); }
  40%, 80% { transform: translateX(8px); }
}

@media (max-width: 1000px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
}
</style>
