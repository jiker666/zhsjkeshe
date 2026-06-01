# Contributing

欢迎围绕课程设计、演示体验、文档和安全教育题目改进本项目。

## 本地启动

```bash
cp .env.example .env.local
./scripts/start_all.sh
```

如 MySQL 账号密码不同，请设置：

```bash
export DB_USER=root
export DB_PASSWORD=你的密码
```

## 提交 Issue

请说明：

- 问题页面或接口
- 复现步骤
- 期望结果
- 实际结果
- 运行环境

## 提交 PR

- 后端提交前运行 `cd backend && mvn test`
- 前端提交前运行 `cd frontend && npm run build`
- 不提交 `node_modules/`、`target/`、`logs/`、`.env.local`
- 不加入真实攻击公网目标的功能

## 代码风格

- 后端保持 Controller / Service / Mapper 分层清晰。
- 前端保持 Vue 3 Composition API 风格。
- 页面文案应面向课程设计答辩，清晰、友好、不过度商业化。
