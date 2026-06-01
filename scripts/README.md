# 启动脚本说明

这些脚本用于课程设计最终交付演示，默认支持 macOS/Linux。

## 脚本列表

- `check_env.sh`：检查 Java、Maven、Node、npm、MySQL、端口和依赖。
- `init_db.sh`：按环境变量初始化 MySQL 数据库。
- `start_backend.sh`：自动选择 `8080` 或 `18080` 启动后端。
- `start_frontend.sh`：自动选择 `5173` 或 `5174` 启动前端，并写入前端 API 地址。
- `start_all.sh`：环境检查、数据库初始化、后端启动、前端启动一条龙。
- `stop_all.sh`：只停止脚本记录的本项目进程。

## 数据库环境变量

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=security_agent_platform
export DB_USER=root
export DB_PASSWORD=root
```

如果本机 MySQL root 用户为空密码，可以显式传入空密码：

```bash
DB_PASSWORD= ./scripts/init_db.sh
DB_PASSWORD= ./scripts/start_backend.sh
```

## 一键启动

```bash
chmod +x scripts/*.sh
./scripts/start_all.sh
```

## 停止项目

```bash
./scripts/stop_all.sh
```
