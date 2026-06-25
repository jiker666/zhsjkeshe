#!/usr/bin/env bash
set -euo pipefail

PORT="${SCORE_RUSH_PORT:-30011}"
APP_DIR="/opt/score-rush"
ARCHIVE="/tmp/score-rush.tar.gz"

if ss -lntp | grep -q ":${PORT} "; then
  echo "[WARN] port ${PORT} is already in use before install"
  ss -lntp | grep ":${PORT} "
fi

mkdir -p "$APP_DIR"
rm -rf /tmp/score-rush
tar -xzf "$ARCHIVE" -C /tmp
cp /tmp/score-rush/server.py "$APP_DIR/server.py"
cp /tmp/score-rush/README.md "$APP_DIR/README.md"
chmod +x "$APP_DIR/server.py"

cat >/etc/systemd/system/score-rush.service <<UNIT
[Unit]
Description=Milk Tea First Blood Score Rush CTF Challenge
After=network.target

[Service]
Type=simple
WorkingDirectory=${APP_DIR}
Environment=SCORE_RUSH_PORT=${PORT}
Environment=TARGET_SCORE=80000
Environment=SCORE_RUSH_FLAG=flag{score_tamper_first_blood_milktea}
ExecStart=/usr/bin/python3 ${APP_DIR}/server.py
Restart=always
RestartSec=3
User=root

[Install]
WantedBy=multi-user.target
UNIT

systemctl daemon-reload
systemctl enable --now score-rush.service
sleep 1
systemctl --no-pager --full status score-rush.service | sed -n '1,18p'
echo "---LISTEN---"
ss -lntp | grep ":${PORT} " || true
