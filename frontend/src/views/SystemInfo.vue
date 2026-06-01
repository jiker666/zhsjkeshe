<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">系统说明</h1>
        <div class="page-subtitle">面向课程设计答辩的项目背景、架构和亮点说明</div>
      </div>
    </div>

    <div class="info-grid">
      <section v-for="section in sections" :key="section.title" class="panel info-card">
        <div class="icon">{{ section.icon }}</div>
        <h2>{{ section.title }}</h2>
        <p>{{ section.desc }}</p>
        <ul>
          <li v-for="item in section.items" :key="item">{{ item }}</li>
        </ul>
      </section>
    </div>
  </div>
</template>

<script setup>
const sections = [
  {
    icon: '01',
    title: '平台背景',
    desc: 'Web 系统安全测试过程通常涉及任务规划、执行记录、风险整理和报告编写，本平台将这些环节可视化串联。',
    items: ['适用于课程演示、本地靶场和已授权目标', '突出软件工程完整流程', '第一版不执行破坏性扫描']
  },
  {
    icon: '02',
    title: '设计目标',
    desc: '平台强调课程设计可交付性，用清晰业务闭环展示需求分析、系统设计、编码实现、测试验证和报告输出。',
    items: ['能启动、能登录、能演示', '任务到报告形成闭环', '老师和同学容易理解']
  },
  {
    icon: '03',
    title: '功能模块',
    desc: '系统围绕安全测试任务生命周期设计，形成从登录到报告的闭环。',
    items: ['用户登录与接口鉴权', '测试任务 CRUD 与状态流转', '漏洞结果、日志、报告中心', '仪表盘统计与图表']
  },
  {
    icon: '04',
    title: '技术架构',
    desc: '采用前后端分离架构，结构清晰，便于课程设计讲解和后续扩展。',
    items: ['Spring Boot + MyBatis Plus + MySQL', 'Vue 3 + Element Plus + ECharts', 'REST API 统一响应结构']
  },
  {
    icon: '05',
    title: 'Agent 工作流程',
    desc: '当前使用规则模拟版 Agent，保留后续接入真实 AI Agent 和扫描器的扩展点。',
    items: ['任务理解', '计划生成', '工具选择', '自动执行', '结果分析', '报告生成', '决策链可解释记录']
  },
  {
    icon: '06',
    title: '创新点',
    desc: '将安全测试任务、智能体流程和可视化报告结合起来，区别于普通管理系统和单一命令行扫描脚本。',
    items: ['Agent 决策链可视化', '漏洞风险智能评分', '整改工单闭环', '安全知识库沉淀', '平台操作审计']
  },
  {
    icon: '07',
    title: '课程设计亮点',
    desc: '项目不只是页面展示，而是包含数据库、接口、业务流程、可视化和文档的完整系统。',
    items: ['演示数据完整', '报告可预览和导出', '状态流转清晰', '文档覆盖软件工程流程']
  },
  {
    icon: '08',
    title: '对应关系',
    desc: '系统功能与软件工程综合实践要求一一对应，方便在报告和答辩中说明工作量。',
    items: ['可行性、需求、总体设计、详细设计', '数据库设计、系统实现、测试报告', '部署运行和答辩展示材料']
  },
  {
    icon: '09',
    title: '内置演示靶场',
    desc: '系统内置 demo-target 模拟接口，答辩时不依赖外部靶场即可演示完整测试流程。',
    items: ['仅返回课程演示模拟数据', '包含未授权、越权、参数篡改等演示场景', '不执行真实攻击和破坏性扫描']
  },
  {
    icon: '10',
    title: '后续扩展方向',
    desc: '在保持授权合规前提下，可逐步增强智能化和工程化能力。',
    items: ['接入真实大模型生成测试计划', '接入低频授权扫描器', '多租户 RBAC 权限模型', '知识库辅助 Agent 自学习']
  }
]
</script>

<style scoped>
.info-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.info-card {
  min-height: 260px;
}

.icon {
  width: 46px;
  height: 46px;
  display: grid;
  place-items: center;
  margin-bottom: 14px;
  border-radius: 14px;
  color: #fff;
  background: linear-gradient(135deg, #22d3ee, #7c3aed);
  font-weight: 900;
}

h2 {
  margin: 0 0 10px;
  color: #f8fafc;
}

p {
  color: #a8c3e8;
  line-height: 1.7;
}

ul {
  margin: 14px 0 0;
  padding-left: 18px;
  color: #dbeafe;
  line-height: 1.9;
}

@media (max-width: 1100px) {
  .info-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 720px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
