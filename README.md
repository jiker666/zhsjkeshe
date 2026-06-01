# 基于 AI Agent 的自主安全测试可视化平台设计与实现

> Spring Boot + Vue 3 + MySQL 的课程设计最终版。项目面向本地靶场、授权环境和安全教育演示，包含 AI Agent 安全测试流程、双端权限、可视化统计、报告导出和趣味安全靶场。

技术栈：`Java 17` `Spring Boot` `MyBatis Plus` `MySQL` `Vue 3` `Element Plus` `ECharts`

安全声明：本项目仅用于课程设计、本地靶场和已授权环境，不得用于未授权测试、攻击公网目标或破坏性扫描。

## 项目截图

```text
docs/screenshots/login.png              登录页截图占位
docs/screenshots/dashboard.png          首页仪表盘截图占位
docs/screenshots/challenge-arena.png    趣味靶场截图占位
docs/screenshots/report.png             报告中心截图占位
```

## 1. 项目简介

本项目是《软件工程综合实践》课程设计作品，目标是实现一个可运行、可演示、可答辩的自主安全测试智能体可视化平台。系统围绕 Web 安全测试任务展开，支持任务创建、Agent 规则模拟执行、漏洞结果展示、日志追踪、态势可视化和测试报告导出。

本项目不执行未授权攻击，不内置破坏性扫描逻辑，默认用于本地靶场、自建测试系统、课程演示数据或已授权目标。

## 2. 项目背景

传统课程设计中，安全测试类项目容易停留在脚本或命令行工具层面，不便展示完整的软件工程过程。本项目将安全测试流程拆解为需求、设计、数据库、接口、前端页面、可视化、报告和测试文档等环节，使老师能够直观看到系统分析、设计实现和工程交付能力。

## 3. 功能模块

- 用户登录、Token 鉴权、ADMIN/USER 简单角色控制
- 首页安全态势仪表盘，展示任务、漏洞、风险分布和 Agent 状态
- 测试任务管理，支持新增、编辑、删除、分页、筛选和执行
- 规则模拟版 AI Agent，生成测试计划、日志、漏洞结果和修复建议
- 漏洞结果管理，支持风险筛选、详情查看、处理状态更新
- 测试日志时间线，支持任务与日志级别筛选
- 报告中心，支持 Markdown/HTML 预览、HTML 导出、Word 导出和浏览器打印 PDF
- 系统说明页，面向答辩展示项目背景、架构、流程和亮点

## 4. 技术栈

- 后端：Java 17、Spring Boot、MyBatis Plus、MySQL、Maven、Apache POI
- 前端：Vue 3、Vite、Element Plus、ECharts、Axios
- 数据库：MySQL
- 文档：Markdown 课程设计文档与最终交付辅助文档

## 5. 系统架构

系统采用前后端分离架构：

```text
浏览器/Vue 前端
    ↓ Axios REST API
Spring Boot 后端
    ↓ Service/Agent/Mapper
MyBatis Plus
    ↓
MySQL 数据库
```

Agent 模块采用接口抽象，当前实现为规则模拟版，后续可以替换为真实大模型、授权扫描器或人工复核流程。

## 6. 目录结构

```text
zhsjkeshe/
├── backend/          Spring Boot + MyBatis Plus 后端
├── frontend/         Vue 3 + Element Plus + ECharts 前端
├── sql/init.sql      MySQL 初始化脚本和演示数据
├── docs/             课程设计文档
├── docs/final/       最终交付、答辩和验收辅助文档
├── scripts/          环境检查、初始化和启动脚本
├── README.md
└── .gitignore
```

## 7. 环境要求

- Java 17 或以上
- Maven 3.8 或以上
- Node.js 18 或以上
- npm
- MySQL 5.7/8.0

可以先执行环境检查：

```bash
./scripts/check_env.sh
```

如果缺少 Java、Maven、Node 或 MySQL，脚本会给出提示。macOS 且安装 Homebrew 时，可参考脚本输出的安装命令手动安装系统级软件。

## 8. 一键启动方式

推荐第一次运行先设置数据库账号密码：

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=security_agent_platform
export DB_USER=root
export DB_PASSWORD=root
```

然后执行：

```bash
./scripts/start_all.sh
```

脚本会依次执行环境检查、数据库初始化、后端启动和前端启动。若 8080 被占用，后端自动尝试 18080；若 5173 被占用，前端自动尝试 5174。

停止本项目脚本启动的进程：

```bash
./scripts/stop_all.sh
```

## 9. 手动启动方式

后端：

```bash
cd backend
mvn spring-boot:run
```

前端：

```bash
cd frontend
npm install
npm run dev
```

前端接口地址可在 `frontend/.env.development` 中调整：

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

## 10. 数据库初始化

方式一，使用脚本：

```bash
./scripts/init_db.sh
```

方式二，手动执行：

```bash
mysql -uroot -p < sql/init.sql
```

默认数据库名为 `security_agent_platform`。初始化脚本只操作本项目数据库中的项目表，不会删除用户其他数据库。

## 11. 默认账号

```text
管理员账号：admin
默认密码：123456
默认角色：ADMIN
```

## 12. 演示流程

1. 打开前端登录页，使用 `admin / 123456` 登录。
2. 进入首页仪表盘，展示任务总数、漏洞数量、风险分布和 Agent 工作状态。
3. 点击“新建任务”，使用示例任务一键填充。
4. 创建任务后进入任务详情页，查看基础信息和测试计划。
5. 点击“执行测试”，展示任务状态流转、日志时间线和漏洞结果。
6. 进入漏洞结果页，查看漏洞详情、请求响应示例和修复建议。
7. 进入报告中心，预览正式测试报告。
8. 导出 HTML、导出 Word，或通过浏览器打印为 PDF。
9. 打开系统说明页，讲解技术架构、Agent 流程和课程设计亮点。

## 13. 报告导出说明

- HTML 导出：前端将当前报告内容导出为 `.html` 文件。
- Word 导出：后端接口 `GET /api/reports/{id}/export/docx` 使用 Apache POI 生成 `.docx`。
- PDF 导出：使用浏览器打印功能，选择“另存为 PDF”。

## 14. 常见问题

- MySQL 连接失败：确认 MySQL 已启动，并通过 `DB_USER`、`DB_PASSWORD` 设置正确账号密码。
- 8080 端口占用：使用 `./scripts/start_backend.sh`，脚本会自动切换到 18080。
- 5173 端口占用：使用 `./scripts/start_frontend.sh`，脚本会自动切换到 5174。
- 前端请求后端失败：检查 `frontend/.env.development` 中的 `VITE_API_BASE_URL` 是否与后端实际端口一致。
- 登录失败：确认已执行 `sql/init.sql`，且默认账号数据已写入。
- Maven 下载慢：可配置国内 Maven 镜像源后重新执行 `mvn test`。
- npm install 失败：检查 Node/npm 版本，必要时清理 `node_modules` 后重新安装。

## 15. 课程设计文档说明

`docs/` 目录包含软件工程课程设计所需的 8 份主体文档：

- 可行性分析报告
- 软件需求规格说明书
- 系统总体设计说明书
- 详细设计说明书
- 数据库设计说明书
- 软件测试报告
- 部署运行说明
- 答辩 PPT 大纲

`docs/final/` 目录包含最终交付辅助材料，包括交付清单、答辩演示流程、截图点清单、常见答辩问题、项目亮点、故障排查和教师验收说明。

## 双端角色说明

系统现已支持管理员端和普通用户端：

```text
管理员：admin / 123456，角色 ADMIN
普通用户：user / 123456，角色 USER
```

管理员登录后进入 `/admin/dashboard`，可访问用户管理、全部测试任务、全部漏洞结果、测试日志、报告中心、系统配置和系统说明。普通用户登录后进入 `/user/dashboard`，只能访问我的测试任务、我的漏洞结果、我的测试日志、我的报告、个人中心和系统说明。

## 注册与个人中心

登录页提供“立即注册”入口。注册用户默认角色为 USER，密码在后端以 SHA-256 哈希保存。登录后可在个人中心修改昵称、邮箱、手机号和密码。

## 双端演示流程

管理员端演示：登录 admin，查看平台安全运营总览，进入用户管理维护账号，查看全部任务、漏洞、日志和报告，展示系统配置页。

普通用户端演示：退出后登录 user，查看我的安全测试工作台，创建并执行自己的测试任务，查看自己的漏洞、日志和报告，进入个人中心维护资料。

权限演示：使用 user 访问 `/admin/users` 或请求管理员用户接口，系统返回无权限，后端响应 403。

## 创新加分项功能

本轮新增 6 个课程设计加分项，均与现有任务、漏洞、报告流程联动：

- Agent 决策链可视化：任务执行后生成任务理解、计划生成、工具选择、测试执行、结果分析、报告生成 6 个决策阶段。
- 漏洞风险智能评分：漏洞结果增加 0.0 - 10.0 风险评分、影响分、可利用性分、利用难度分和风险向量。
- 整改工单闭环：支持从漏洞一键生成工单，并流转 TODO、CONFIRMED、FIXING、RETESTING、CLOSED、IGNORED。
- 安全知识库：沉淀常见漏洞类型、成因、影响和修复建议，漏洞详情可跳转查看对应知识。
- 平台操作审计：记录登录、注册、任务执行、漏洞状态修改、报告导出、工单流转等关键操作。
- 内置演示靶场：提供 `/api/demo-target/**` 模拟接口，用于答辩稳定演示，不执行真实攻击扫描。
- 趣味安全靶场：提供 6 道本地模拟安全题，支持在线答题、提示、解析、积分、成就和管理员题目管理。

## 加分项推荐演示流程

1. 使用 `admin / 123456` 登录管理员端。
2. 打开首页，展示平均风险评分、最高风险评分、待处理工单、Agent 决策次数和最近审计日志。
3. 进入“新建任务”，点击“使用内置演示靶场”，创建任务并执行。
4. 在任务详情页展示 Agent 决策链，说明每一步决策原因、工具和置信度。
5. 进入漏洞结果，展示风险评分卡片，并从漏洞一键生成整改工单。
6. 进入整改工单页，修改状态并执行一键复测。
7. 打开安全知识库，展示漏洞类型对应的成因和修复建议。
8. 打开操作审计，展示系统关键操作可追溯。
9. 进入报告中心，导出 Word 报告，说明报告包含决策链、风险评分和整改信息。
10. 打开“趣味靶场”，答对一道题展示彩带动画、积分和成就徽章。
