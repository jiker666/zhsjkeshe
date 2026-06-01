#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="$ROOT_DIR/logs"

stop_pid() {
  local name="$1"
  local file="$LOG_DIR/$name.pid"
  if [ -f "$file" ]; then
    local pid
    pid="$(cat "$file")"
    if ps -p "$pid" >/dev/null 2>&1; then
      echo "[INFO] 停止 $name，PID：$pid"
      kill "$pid"
    else
      echo "[INFO] $name 进程不存在，跳过"
    fi
    rm -f "$file"
  else
    echo "[INFO] 未找到 $name pid 文件，跳过"
  fi
}

stop_pid frontend
stop_pid backend
echo "[OK] 已停止本项目脚本启动的前后端进程。"
