#!/usr/bin/env python3
import json
import os
import re
import sqlite3
import time
from datetime import datetime, timezone, timedelta
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlparse

HOST = "0.0.0.0"
PORT = int(os.environ.get("SCORE_RUSH_PORT", "30008"))
BASE_DIR = Path(__file__).resolve().parent
DB_PATH = BASE_DIR / "score_rush.db"
TARGET_SCORE = int(os.environ.get("TARGET_SCORE", "52000"))
FLAG = os.environ.get("SCORE_RUSH_FLAG", "flag{score_tamper_first_blood_milktea}")
TZ = timezone(timedelta(hours=8))


def now_text():
    return datetime.now(TZ).strftime("%Y-%m-%d %H:%M:%S")


def db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    conn.execute(
        """
        create table if not exists first_blood (
            id integer primary key check (id = 1),
            nickname text not null,
            score integer not null,
            ip text not null,
            user_agent text,
            solved_at text not null
        )
        """
    )
    conn.execute(
        """
        create table if not exists submissions (
            id integer primary key autoincrement,
            nickname text not null,
            score integer not null,
            success integer not null,
            ip text not null,
            user_agent text,
            submitted_at text not null
        )
        """
    )
    conn.commit()
    return conn


def first_blood():
    with db() as conn:
        row = conn.execute("select * from first_blood where id = 1").fetchone()
        return dict(row) if row else None


def record_submission(nickname, score, success, ip, user_agent):
    solved_at = now_text()
    with db() as conn:
        conn.execute(
            "insert into submissions(nickname, score, success, ip, user_agent, submitted_at) values (?, ?, ?, ?, ?, ?)",
            (nickname, score, 1 if success else 0, ip, user_agent, solved_at),
        )
        if success:
            conn.execute(
                "insert or ignore into first_blood(id, nickname, score, ip, user_agent, solved_at) values (1, ?, ?, ?, ?, ?)",
                (nickname, score, ip, user_agent, solved_at),
            )
        conn.commit()


def stats():
    with db() as conn:
        total = conn.execute("select count(*) c from submissions").fetchone()["c"]
        solved = conn.execute("select count(*) c from submissions where success = 1").fetchone()["c"]
        recent = [
            dict(row)
            for row in conn.execute(
                "select nickname, score, success, submitted_at from submissions order by id desc limit 10"
            )
        ]
    return {"firstBlood": first_blood(), "totalSubmissions": total, "solvedSubmissions": solved, "recent": recent}


def clean_name(value):
    value = (value or "").strip()
    value = re.sub(r"[^\w\u4e00-\u9fa5\- ]", "", value)
    return value[:24] or "anonymous"


INDEX_HTML = r"""<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>奶茶一血：Score Rush</title>
  <style>
    :root {
      --bg: #10151f;
      --panel: rgba(18, 27, 42, .86);
      --line: rgba(127, 190, 255, .22);
      --text: #f4fbff;
      --muted: #9fb5d0;
      --cyan: #35d5ff;
      --green: #4ade80;
      --gold: #fbbf24;
      --red: #fb7185;
    }
    * { box-sizing: border-box; }
    html, body { margin: 0; min-height: 100%; font-family: ui-sans-serif, "PingFang SC", "Microsoft YaHei", sans-serif; color: var(--text); background: var(--bg); overflow-x: hidden; }
    body {
      background:
        linear-gradient(rgba(79, 164, 255, .08) 1px, transparent 1px),
        linear-gradient(90deg, rgba(79, 164, 255, .08) 1px, transparent 1px),
        radial-gradient(circle at 18% 12%, rgba(53, 213, 255, .22), transparent 28%),
        radial-gradient(circle at 80% 20%, rgba(251, 191, 36, .18), transparent 26%),
        linear-gradient(135deg, #0b1020, #101827 52%, #15111f);
      background-size: 36px 36px, 36px 36px, auto, auto, auto;
    }
    .wrap { width: min(1180px, calc(100vw - 32px)); margin: 0 auto; padding: 34px 0 46px; }
    .hero { display: grid; grid-template-columns: 1.1fr .9fr; gap: 18px; align-items: stretch; margin-bottom: 18px; }
    .card { border: 1px solid var(--line); background: var(--panel); border-radius: 14px; box-shadow: 0 18px 60px rgba(0,0,0,.35); backdrop-filter: blur(14px); }
    .intro { padding: 28px; min-height: 260px; position: relative; overflow: hidden; }
    .intro:after { content: ""; position: absolute; inset: auto -80px -120px auto; width: 260px; height: 260px; border-radius: 50%; background: rgba(53,213,255,.12); }
    .tag { display: inline-block; padding: 7px 10px; border: 1px solid rgba(53,213,255,.32); border-radius: 999px; color: #a5f3fc; background: rgba(8, 47, 73, .28); font-size: 13px; }
    h1 { margin: 20px 0 12px; font-size: clamp(34px, 5vw, 58px); line-height: 1.05; }
    .intro p { margin: 0; max-width: 620px; color: #cfe6ff; line-height: 1.75; }
    .rules { padding: 24px; }
    .rules h2, .panel-title { margin: 0 0 14px; font-size: 18px; }
    .rules ul { margin: 0; padding-left: 18px; color: var(--muted); line-height: 1.9; }
    .grid { display: grid; grid-template-columns: 1fr 360px; gap: 18px; }
    .game { padding: 18px; }
    .bar { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 10px; margin-bottom: 12px; }
    .metric { padding: 12px; border: 1px solid rgba(127,190,255,.18); border-radius: 10px; background: rgba(12, 19, 32, .72); }
    .metric span { display: block; color: var(--muted); font-size: 12px; }
    .metric strong { display: block; margin-top: 5px; font-size: 24px; }
    #arena { position: relative; height: 430px; border: 1px solid rgba(127,190,255,.26); border-radius: 12px; overflow: hidden; background: linear-gradient(180deg, rgba(12, 19, 32, .94), rgba(7, 12, 22, .98)); }
    .bug { position: absolute; width: 46px; height: 46px; display: grid; place-items: center; cursor: pointer; user-select: none; border-radius: 12px; color: #06121d; font-weight: 900; box-shadow: 0 10px 26px rgba(0,0,0,.34); background: linear-gradient(135deg, #67e8f9, #a7f3d0); transition: transform .08s ease; }
    .bug.hot { background: linear-gradient(135deg, #fde68a, #fb7185); }
    .bug:active { transform: scale(.88); }
    .controls { display: flex; gap: 10px; align-items: center; margin-top: 12px; flex-wrap: wrap; }
    input { height: 40px; border: 1px solid rgba(127,190,255,.26); border-radius: 9px; padding: 0 12px; color: var(--text); background: rgba(7, 12, 22, .82); outline: none; }
    button { height: 40px; border: 0; border-radius: 9px; padding: 0 15px; color: #04111b; background: linear-gradient(135deg, var(--cyan), #86efac); font-weight: 800; cursor: pointer; }
    button.secondary { color: var(--text); background: rgba(127,190,255,.14); border: 1px solid rgba(127,190,255,.25); }
    .side { display: grid; gap: 18px; }
    .side .card { padding: 18px; }
    .flag { display: none; margin-top: 12px; padding: 14px; border: 1px solid rgba(74,222,128,.36); border-radius: 10px; color: #bbf7d0; background: rgba(20, 83, 45, .24); overflow-wrap: anywhere; }
    .msg { min-height: 24px; margin-top: 10px; color: var(--muted); }
    .first { border-color: rgba(251,191,36,.38); background: rgba(120, 53, 15, .18); }
    .first strong { color: var(--gold); }
    .recent { display: grid; gap: 8px; max-height: 230px; overflow: auto; }
    .row { display: flex; justify-content: space-between; gap: 10px; padding: 9px 10px; border-radius: 8px; background: rgba(8, 13, 24, .62); color: #dbeafe; font-size: 13px; }
    .ok { color: var(--green); }
    .no { color: var(--red); }
    code { color: #a5f3fc; }
    @media (max-width: 900px) { .hero, .grid { grid-template-columns: 1fr; } .bar { grid-template-columns: 1fr 1fr; } }
  </style>
</head>
<body>
  <main class="wrap">
    <section class="hero">
      <div class="card intro">
        <div class="tag">Web Game Challenge · 一血奖励奶茶</div>
        <h1>奶茶一血：Score Rush</h1>
        <p>你是软件工程班的临时性能优化工程师。正常玩游戏也许能拿到高分，但目标分数非常离谱。真正的突破点在 Web 前后端信任边界里。</p>
      </div>
      <div class="card rules">
        <h2>题目规则</h2>
        <ul>
          <li>输入昵称，开始游戏。</li>
          <li>在 35 秒内尽量点击分数块。</li>
          <li>达到 <code id="targetText">52000</code> 分即可拿到 flag。</li>
          <li>服务器会记录第一个成功提交者作为“一血”。</li>
        </ul>
      </div>
    </section>
    <section class="grid">
      <div class="card game">
        <div class="bar">
          <div class="metric"><span>当前分数</span><strong id="score">0</strong></div>
          <div class="metric"><span>剩余时间</span><strong id="time">35</strong></div>
          <div class="metric"><span>连击</span><strong id="combo">0</strong></div>
          <div class="metric"><span>目标分</span><strong id="target">52000</strong></div>
        </div>
        <div id="arena"></div>
        <div class="controls">
          <input id="nickname" maxlength="24" placeholder="昵称，例如：张三" />
          <button id="startBtn">开始挑战</button>
          <button class="secondary" id="submitBtn">提交分数</button>
        </div>
        <div class="msg" id="msg">提示：真正的高分，往往不只在鼠标速度里。</div>
        <div class="flag" id="flagBox"></div>
      </div>
      <aside class="side">
        <div class="card first">
          <div class="panel-title">一血记录</div>
          <div id="firstBlood">正在读取...</div>
        </div>
        <div class="card">
          <div class="panel-title">最近提交</div>
          <div class="recent" id="recent"></div>
        </div>
      </aside>
    </section>
  </main>
  <script>
    let score = 0, combo = 0, timeLeft = 35, playing = false, tick = null, spawnTick = null;
    const target = TARGET_SCORE_FROM_SERVER;
    const arena = document.querySelector('#arena');
    const scoreEl = document.querySelector('#score');
    const comboEl = document.querySelector('#combo');
    const timeEl = document.querySelector('#time');
    const targetEls = [document.querySelector('#target'), document.querySelector('#targetText')];
    targetEls.forEach(el => el.textContent = target);

    function setMsg(text) { document.querySelector('#msg').textContent = text; }
    function sync() { scoreEl.textContent = score; comboEl.textContent = combo; timeEl.textContent = timeLeft; }
    function nickname() { return document.querySelector('#nickname').value.trim() || 'anonymous'; }
    function resetGame() {
      score = 0; combo = 0; timeLeft = 35; playing = true; arena.innerHTML = ''; document.querySelector('#flagBox').style.display = 'none'; sync();
      clearInterval(tick); clearInterval(spawnTick);
      tick = setInterval(() => { timeLeft--; sync(); if (timeLeft <= 0) finishGame(); }, 1000);
      spawnTick = setInterval(spawnBug, 420);
      setMsg('游戏开始。正常打到目标分很难，但不是没有路。');
    }
    function finishGame() {
      playing = false; clearInterval(tick); clearInterval(spawnTick); arena.innerHTML = '';
      setMsg('时间到，可以提交分数。');
    }
    function spawnBug() {
      if (!playing) return;
      const bug = document.createElement('div');
      const hot = Math.random() < .12;
      const value = hot ? 450 : 80 + Math.floor(Math.random() * 120);
      bug.className = 'bug' + (hot ? ' hot' : '');
      bug.textContent = '+' + value;
      bug.style.left = Math.floor(Math.random() * Math.max(1, arena.clientWidth - 54)) + 'px';
      bug.style.top = Math.floor(Math.random() * Math.max(1, arena.clientHeight - 54)) + 'px';
      bug.onclick = () => { combo++; score += value + Math.min(combo * 4, 160); bug.remove(); sync(); };
      arena.appendChild(bug);
      setTimeout(() => { if (bug.isConnected) { combo = 0; bug.remove(); sync(); } }, 920);
    }
    async function submitScore() {
      const res = await fetch('/api/finish', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nickname: nickname(), score: score, combo: combo })
      });
      const data = await res.json();
      if (data.success) {
        document.querySelector('#flagBox').style.display = 'block';
        document.querySelector('#flagBox').textContent = data.flag;
        setMsg(data.firstBloodMine ? '你拿到一血了，奶茶安排！' : '挑战成功，但一血已经被拿走了。');
      } else {
        setMsg(data.message || '分数还不够。');
      }
      loadStats();
    }
    async function loadStats() {
      const data = await fetch('/api/stats').then(r => r.json());
      const fb = data.firstBlood;
      document.querySelector('#firstBlood').innerHTML = fb
        ? `<strong>${fb.nickname}</strong><br>分数：${fb.score}<br>时间：${fb.solved_at}<br>IP：${fb.ip}`
        : '暂无一血，第一杯奶茶还在路上。';
      document.querySelector('#recent').innerHTML = data.recent.map(item =>
        `<div class="row"><span>${item.nickname}</span><span class="${item.success ? 'ok' : 'no'}">${item.score}</span></div>`
      ).join('') || '<div class="row"><span>暂无提交</span><span>-</span></div>';
    }
    document.querySelector('#startBtn').onclick = resetGame;
    document.querySelector('#submitBtn').onclick = submitScore;
    sync(); loadStats(); setInterval(loadStats, 5000);
  </script>
</body>
</html>"""


class Handler(BaseHTTPRequestHandler):
    def log_message(self, fmt, *args):
        return

    def client_ip(self):
        forwarded = self.headers.get("X-Forwarded-For")
        if forwarded:
            return forwarded.split(",")[0].strip()
        return self.client_address[0]

    def send_json(self, payload, code=200):
        data = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def do_HEAD(self):
        path = urlparse(self.path).path
        if path == "/":
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.end_headers()
            return
        if path == "/api/stats":
            self.send_response(200)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.end_headers()
            return
        self.send_response(404)
        self.end_headers()

    def do_GET(self):
        path = urlparse(self.path).path
        if path == "/":
            html = INDEX_HTML.replace("TARGET_SCORE_FROM_SERVER", str(TARGET_SCORE)).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Content-Length", str(len(html)))
            self.end_headers()
            self.wfile.write(html)
            return
        if path == "/api/stats":
            self.send_json(stats())
            return
        self.send_json({"error": "not found"}, 404)

    def do_POST(self):
        path = urlparse(self.path).path
        if path != "/api/finish":
            self.send_json({"error": "not found"}, 404)
            return
        try:
            length = min(int(self.headers.get("Content-Length", "0")), 4096)
            payload = json.loads(self.rfile.read(length).decode("utf-8") or "{}")
        except Exception:
            self.send_json({"success": False, "message": "提交格式错误"}, 400)
            return
        nickname = clean_name(payload.get("nickname"))
        try:
            score = int(payload.get("score", 0))
        except Exception:
            score = 0
        success = score >= TARGET_SCORE
        before = first_blood()
        record_submission(nickname, score, success, self.client_ip(), self.headers.get("User-Agent", ""))
        after = first_blood()
        response = {
            "success": success,
            "score": score,
            "target": TARGET_SCORE,
            "message": "分数还不够，继续优化你的游戏策略。",
            "firstBlood": after,
        }
        if success:
            response["flag"] = FLAG
            response["message"] = "挑战成功。"
            response["firstBloodMine"] = before is None and after and after.get("nickname") == nickname
        self.send_json(response)


if __name__ == "__main__":
    db().close()
    print(f"Score Rush listening on http://{HOST}:{PORT}")
    ThreadingHTTPServer((HOST, PORT), Handler).serve_forever()
