#!/usr/bin/env bash
set -u

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="$ROOT_DIR/logs"
mkdir -p "$LOG_DIR"

need_cmd() {
  if command -v "$1" >/dev/null 2>&1; then
    echo "[OK] $1: $($1 --version 2>/dev/null | head -1)"
    return 0
  fi
  echo "[MISS] $1 未安装或不在 PATH"
  return 1
}

port_status() {
  local port="$1"
  if lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "[USED] 端口 $port 已占用"
    lsof -nP -iTCP:"$port" -sTCP:LISTEN | tail -n +2
  else
    echo "[FREE] 端口 $port 可用"
  fi
}

echo "== 环境检查 =="
need_cmd java || true
if command -v java >/dev/null 2>&1; then
  JAVA_VERSION="$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F. '{print ($1=="1") ? $2 : $1}')"
  if [ "${JAVA_VERSION:-0}" -lt 17 ]; then
    echo "[WARN] Java 版本低于 17，请安装 JDK 17+"
  fi
fi
need_cmd mvn || true
need_cmd node || true
need_cmd npm || true
need_cmd mysql || true

echo
echo "== MySQL 连接检查 =="
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD-root}"
if command -v mysql >/dev/null 2>&1; then
  if MYSQL_PWD="$DB_PASSWORD" mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -e "select 1" >/dev/null 2>&1; then
    echo "[OK] MySQL 可连接：$DB_HOST:$DB_PORT，用户 $DB_USER"
  else
    echo "[WARN] MySQL 暂不可连接：$DB_HOST:$DB_PORT，用户 $DB_USER"
    echo "       可通过 DB_HOST/DB_PORT/DB_USER/DB_PASSWORD 环境变量修改连接配置。"
    if [[ "$(uname)" == "Darwin" ]] && command -v brew >/dev/null 2>&1; then
      echo "       macOS 可尝试：brew services start mysql"
    fi
  fi
fi

echo
echo "== 端口检查 =="
for port in 8080 18080 5173 5174; do
  port_status "$port"
done

echo
echo "== 依赖检查 =="
if [ ! -d "$ROOT_DIR/frontend/node_modules" ] && command -v npm >/dev/null 2>&1; then
  echo "[ACTION] 前端依赖缺失，执行 npm install"
  (cd "$ROOT_DIR/frontend" && npm install)
else
  echo "[OK] 前端依赖已存在"
fi

if command -v mvn >/dev/null 2>&1; then
  echo "[ACTION] 解析后端依赖并运行编译测试"
  (cd "$ROOT_DIR/backend" && mvn test)
fi

echo "== 环境检查完成 =="
