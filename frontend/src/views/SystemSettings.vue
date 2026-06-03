<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">系统配置</h1>
        <div class="page-subtitle">管理员端配置中心，保存后立即影响平台注册与默认策略</div>
      </div>
    </div>

    <section class="panel settings-panel" v-loading="loading">
      <el-form :model="form" label-width="150px">
        <el-form-item label="平台名称"><el-input v-model="form.platformName" /></el-form-item>
        <el-form-item label="Agent 模式">
          <el-radio-group v-model="form.agentMode">
            <el-radio-button label="规则模拟模式" />
            <el-radio-button label="真实 AI 模式（暂未启用）" disabled />
          </el-radio-group>
        </el-form-item>
        <el-form-item label="默认测试深度">
          <el-segmented v-model="form.defaultDepth" :options="['快速', '标准', '深入']" />
        </el-form-item>
        <el-form-item label="自动生成报告"><el-switch v-model="form.autoReport" active-text="开启" inactive-text="关闭" /></el-form-item>
        <el-form-item label="开启注册">
          <el-switch v-model="form.enableRegister" active-text="允许新用户注册" inactive-text="关闭注册入口" />
        </el-form-item>
        <el-form-item label="安全声明">
          <el-input v-model="form.statement" type="textarea" :rows="5" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存配置</el-button>
          <el-button :loading="saving" @click="reset">恢复默认</el-button>
        </el-form-item>
      </el-form>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getSystemSettings, resetSystemSettings, updateSystemSettings } from '../api'

const defaults = {
  platformName: '自主安全测试智能体可视化平台',
  agentMode: '规则模拟模式',
  defaultDepth: '标准',
  autoReport: true,
  enableRegister: true,
  statement: '本平台仅用于本地靶场、自建测试系统、课程演示环境或已授权目标，不执行未授权攻击或破坏性扫描。'
}
const form = reactive({ ...defaults })
const loading = ref(false)
const saving = ref(false)

async function loadSettings() {
  loading.value = true
  try {
    Object.assign(form, defaults, await getSystemSettings())
  } catch (error) {
    ElMessage.error(error.message || '配置加载失败')
  } finally {
    loading.value = false
  }
}

async function save() {
  saving.value = true
  try {
    Object.assign(form, await updateSystemSettings(form))
    ElMessage.success('系统配置已保存')
  } catch (error) {
    ElMessage.error(error.message || '配置保存失败')
  } finally {
    saving.value = false
  }
}

async function reset() {
  saving.value = true
  try {
    Object.assign(form, defaults, await resetSystemSettings())
    ElMessage.success('已恢复默认配置')
  } catch (error) {
    ElMessage.error(error.message || '恢复默认失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadSettings)
</script>

<style scoped>
.settings-panel {
  max-width: 920px;
}
</style>
