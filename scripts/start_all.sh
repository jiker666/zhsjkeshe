#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

"$ROOT_DIR/scripts/check_env.sh"
"$ROOT_DIR/scripts/init_db.sh"
"$ROOT_DIR/scripts/start_backend.sh"
"$ROOT_DIR/scripts/start_frontend.sh"

BACKEND_PORT="$(cat "$ROOT_DIR/logs/backend.port")"
FRONTEND_PORT="$(cat "$ROOT_DIR/logs/frontend.port")"

cat <<EOF

================ 启动完成 ================
前端地址：http://localhost:${FRONTEND_PORT}
后端地址：http://localhost:${BACKEND_PORT}
健康检查：http://localhost:${BACKEND_PORT}/api/health
默认账号：admin / 123456

建议演示流程：
1. 登录系统
2. 查看首页态势仪表盘
3. 新建任务并使用示例任务填充
4. 执行 Agent 模拟测试
5. 查看漏洞详情和日志时间线
6. 进入报告中心导出 HTML/Word 或打印 PDF
7. 打开系统说明页讲解项目亮点
==========================================
EOF
