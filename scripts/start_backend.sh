#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="$ROOT_DIR/logs"
mkdir -p "$LOG_DIR"

PORT="${BACKEND_PORT:-8080}"
if lsof -nP -iTCP:"$PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  echo "[WARN] 后端端口 $PORT 已占用，自动切换到 18080"
  PORT=18080
fi
if lsof -nP -iTCP:"$PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  echo "[ERROR] 备用端口 $PORT 也被占用，请释放端口或设置 BACKEND_PORT。"
  lsof -nP -iTCP:"$PORT" -sTCP:LISTEN
  exit 1
fi

echo "$PORT" > "$LOG_DIR/backend.port"
echo "[INFO] 启动后端，端口：$PORT"
(
  cd "$ROOT_DIR/backend"
  SERVER_PORT="$PORT" nohup mvn spring-boot:run > "$LOG_DIR/backend.log" 2>&1 &
  echo $! > "$LOG_DIR/backend.pid"
)

echo "[INFO] 后端 PID：$(cat "$LOG_DIR/backend.pid")"
echo "[INFO] 等待后端健康检查..."
for i in {1..40}; do
  if curl -s "http://localhost:$PORT/api/health" >/dev/null 2>&1; then
    echo "[OK] 后端已启动：http://localhost:$PORT"
    echo "[OK] 健康检查：http://localhost:$PORT/api/health"
    exit 0
  fi
  sleep 1
done

echo "[WARN] 后端未在预期时间内完成健康检查，请查看 logs/backend.log"
