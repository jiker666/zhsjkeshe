<template>
  <div class="page forbidden-page">
    <section class="panel forbidden-card">
      <div class="code">403</div>
      <h1>无权限访问该资源</h1>
      <p>当前账号没有访问该管理功能的权限。普通用户只能查看和管理自己的测试任务、漏洞、日志和报告。</p>
      <div>
        <el-button type="primary" @click="$router.push(home)">返回工作台</el-button>
        <el-button @click="$router.push('/system-info')">查看系统说明</el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
const user = JSON.parse(localStorage.getItem('user') || '{}')
const home = user.role === 'ADMIN' ? '/admin/dashboard' : '/user/dashboard'
</script>

<style scoped>
.forbidden-page {
  display: grid;
  place-items: center;
}

.forbidden-card {
  max-width: 620px;
  text-align: center;
}

.code {
  font-size: 72px;
  font-weight: 900;
  color: #7da5ff;
}

h1 {
  margin: 0 0 10px;
  color: #f8fbff;
}

p {
  color: #9fb5d5;
  line-height: 1.8;
}
</style>
