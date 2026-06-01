#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-security_agent_platform}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD-root}"

if ! command -v mysql >/dev/null 2>&1; then
  echo "[ERROR] 未找到 mysql 客户端，请先安装 MySQL Client。"
  exit 1
fi

echo "[INFO] 初始化数据库：$DB_NAME@$DB_HOST:$DB_PORT，用户：$DB_USER"
if ! MYSQL_PWD="$DB_PASSWORD" mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -e "select 1" >/dev/null 2>&1; then
  echo "[ERROR] MySQL 连接失败。"
  echo "        请检查 MySQL 是否启动，以及 DB_HOST/DB_PORT/DB_USER/DB_PASSWORD 是否正确。"
  echo "        macOS Homebrew 可尝试：brew services start mysql"
  exit 1
fi

MYSQL_PWD="$DB_PASSWORD" mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" < "$ROOT_DIR/sql/init.sql"
echo "[OK] 数据库初始化完成。脚本仅重建本项目数据库 $DB_NAME 下的项目表。"
