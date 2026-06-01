# GitHub 发布检查清单

## 基础文件

- [x] README.md 包含项目简介、技术栈、快速启动、默认账号、演示流程和安全声明。
- [x] LICENSE 已添加，当前使用 MIT License。
- [x] SECURITY.md 已添加，说明使用边界和安全问题反馈方式。
- [x] CONTRIBUTING.md 已添加，说明启动、Issue、PR 和代码风格。
- [x] CHANGELOG.md 已添加，记录 v1.0.0 课程设计最终版。

## 配置与敏感信息

- [x] `.env.example` 已提供示例配置。
- [x] `backend/.env.example` 和 `frontend/.env.example` 已提供。
- [x] 未提交真实密钥、真实公网目标或真实攻击配置。
- [x] README 明确项目仅用于课程设计、本地靶场和授权环境。

## 忽略规则

- [x] `.gitignore` 忽略 `node_modules/`、`target/`、`backend/target/`、`frontend/dist/`、`logs/`、`*.log`、`*.pid`、`.idea/`、`.vscode/`、`.DS_Store`。
- [x] 不忽略 `sql/init.sql`、`scripts/*.sh`、`docs/`、`README.md`、`.env.example`。

## 构建与启动

- [x] 后端 `mvn test` 通过。
- [x] 前端 `npm run build` 通过。
- [x] `scripts/start_all.sh` 可用于一键启动。
- [x] 默认账号 `admin / 123456`、`user / 123456` 已在 README 中说明。

## 功能验收

- [x] 双端登录、注册、权限隔离可演示。
- [x] Agent 决策链、风险评分、整改工单、知识库、审计日志可演示。
- [x] 趣味靶场题目、答题、积分、成就、管理员题目管理可演示。
- [x] Word/HTML/打印 PDF 报告导出可演示。

## 上传前建议

1. 删除本地运行产物：`logs/`、`backend/target/`、`frontend/dist/`。
2. 确认没有提交 `.env.local`。
3. 执行 `mvn test` 和 `npm run build`。
4. 在 GitHub 页面检查 README 排版。
5. 创建 release 标签 `v1.0.0`。
