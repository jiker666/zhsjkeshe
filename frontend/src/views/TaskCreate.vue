<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">新建测试任务</h1>
        <div class="page-subtitle">填写目标、测试类型与执行策略，Agent 将自动生成测试计划</div>
      </div>
      <div class="header-actions">
        <el-button :icon="MagicStick" @click="fillDemoTarget">使用内置演示靶场</el-button>
        <el-button :icon="MagicStick" @click="fillExample">示例任务</el-button>
      </div>
    </div>

    <section class="panel form-panel">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="form.taskName" placeholder="例如：演示靶场越权访问测试" />
        </el-form-item>
        <el-form-item label="目标地址" prop="targetUrl">
          <el-input v-model="form.targetUrl" placeholder="http://localhost:8081" />
        </el-form-item>
        <el-form-item label="测试类型" prop="testType">
          <el-select v-model="form.testType" style="width: 100%">
            <el-option v-for="item in testTypes" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="测试深度" prop="testDepth">
          <el-segmented v-model="form.testDepth" :options="['快速', '标准', '深入']" />
        </el-form-item>
        <el-form-item label="生成报告">
          <el-switch v-model="form.generateReport" active-text="是" inactive-text="否" />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="6" placeholder="说明测试边界、授权范围和演示目标" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submit">保存任务</el-button>
          <el-button @click="$router.push('/tasks')">返回</el-button>
        </el-form-item>
      </el-form>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MagicStick } from '@element-plus/icons-vue'
import { createTask } from '../api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const testTypes = ['未授权访问测试', '弱口令测试', '越权访问测试', '敏感信息泄露测试', '接口参数篡改测试']
const form = reactive({
  taskName: '',
  targetUrl: '',
  testType: '越权访问测试',
  testDepth: '标准',
  generateReport: true,
  description: ''
})

const rules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  targetUrl: [{ required: true, message: '请输入目标地址', trigger: 'blur' }],
  testType: [{ required: true, message: '请选择测试类型', trigger: 'change' }],
  testDepth: [{ required: true, message: '请选择测试深度', trigger: 'change' }],
  description: [{ required: true, message: '请输入任务描述', trigger: 'blur' }]
}

function fillExample() {
  Object.assign(form, {
    taskName: '课程演示靶场越权访问测试',
    targetUrl: 'http://localhost:8081',
    testType: '越权访问测试',
    testDepth: '标准',
    generateReport: true,
    description: '面向本地课程演示靶场，验证订单详情接口是否存在水平越权风险。测试过程仅使用规则模拟 Agent，不执行破坏性扫描。'
  })
}

function fillDemoTarget() {
  const base = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:18080/api').replace(/\/api$/, '')
  Object.assign(form, {
    taskName: '内置演示靶场访问控制综合测试',
    targetUrl: `${base}/api/demo-target`,
    testType: '未授权访问测试',
    testDepth: '标准',
    generateReport: true,
    description: '对内置演示靶场进行授权范围内的安全测试演示，重点展示未授权访问、越权访问和接口参数篡改等风险的 Agent 决策链、风险评分与报告导出。'
  })
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    const payload = { ...form, generateReport: form.generateReport ? 1 : 0 }
    const task = await createTask(payload)
    ElMessage.success('任务创建成功，Agent 已生成测试计划')
    router.push(`/tasks/${task.id}`)
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.form-panel {
  max-width: 840px;
}

.header-actions {
  display: flex;
  gap: 10px;
}
</style>
