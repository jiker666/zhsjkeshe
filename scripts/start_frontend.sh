#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="$ROOT_DIR/logs"
mkdir -p "$LOG_DIR"

BACKEND_PORT="${BACKEND_PORT:-}"
if [ -z "$BACKEND_PORT" ] && [ -f "$LOG_DIR/backend.port" ]; then
  BACKEND_PORT="$(cat "$LOG_DIR/backend.port")"
fi
BACKEND_PORT="${BACKEND_PORT:-8080}"

FRONTEND_PORT="${FRONTEND_PORT:-5173}"
if lsof -nP -iTCP:"$FRONTEND_PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  echo "[WARN] 前端端口 $FRONTEND_PORT 已占用，自动切换到 5174"
  FRONTEND_PORT=5174
fi
if lsof -nP -iTCP:"$FRONTEND_PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  echo "[ERROR] 备用端口 $FRONTEND_PORT 也被占用，请释放端口或设置 FRONTEND_PORT。"
  lsof -nP -iTCP:"$FRONTEND_PORT" -sTCP:LISTEN
  exit 1
fi

cat > "$ROOT_DIR/frontend/.env.development" <<EOF
VITE_API_BASE_URL=http://localhost:${BACKEND_PORT}/api
VITE_DEV_PORT=${FRONTEND_PORT}
EOF

if [ ! -d "$ROOT_DIR/frontend/node_modules" ]; then
  echo "[ACTION] 前端依赖缺失，执行 npm install"
  (cd "$ROOT_DIR/frontend" && npm install)
fi

echo "$FRONTEND_PORT" > "$LOG_DIR/frontend.port"
echo "[INFO] 启动前端，端口：$FRONTEND_PORT，API：http://localhost:${BACKEND_PORT}/api"
(
  cd "$ROOT_DIR/frontend"
  nohup npm run dev -- --port "$FRONTEND_PORT" --host 0.0.0.0 --strictPort > "$LOG_DIR/frontend.log" 2>&1 &
  echo $! > "$LOG_DIR/frontend.pid"
)

echo "[INFO] 前端 PID：$(cat "$LOG_DIR/frontend.pid")"
for i in {1..30}; do
  if curl -s "http://localhost:$FRONTEND_PORT" >/dev/null 2>&1; then
    echo "[OK] 前端已启动：http://localhost:$FRONTEND_PORT"
    exit 0
  fi
  sleep 1
done

echo "[WARN] 前端未在预期时间内完成启动，请查看 logs/frontend.log"
