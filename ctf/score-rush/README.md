# 奶茶一血：Score Rush

这是一个用于班级 CTF/课程趣味赛的 Web 游戏题。

## 题目说明

- 正常玩法：在 35 秒内点击分数块，尝试达到目标分。
- 目标分数：`52000`
- 预期解法：观察前端提交请求，修改提交到 `/api/finish` 的 `score` 字段。
- 一血机制：服务端使用 SQLite 记录第一个成功提交者的昵称、IP、时间和分数。

## 默认 Flag

```text
flag{score_tamper_first_blood_milktea}
```

## 启动

```bash
SCORE_RUSH_PORT=30008 python3 server.py
```

## 接口

- `GET /`：游戏页面
- `GET /api/stats`：一血和最近提交记录
- `POST /api/finish`：提交分数

## 安全边界

本题是本地/授权环境中的安全教育题，故意演示“服务端过度信任客户端分数”的逻辑缺陷，不涉及真实攻击公网目标。
