#!/bin/bash

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

echo "========================================="
echo "  茶叶品类与冲泡参数记录本 - 启动脚本"
echo "========================================="

ENV_FILE="$PROJECT_DIR/.env"
if [ ! -f "$ENV_FILE" ]; then
    echo "错误：.env 文件不存在"
    exit 1
fi

set -a
source "$ENV_FILE"
set +a

check_port_occupied() {
    local port=$1
    lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1
}

echo ""
echo "检查端口可用性（占用时自动顺延）..."

resolve_port() {
    local var_name=$1
    local label=$2
    local start_port=${!var_name}
    local port=$start_port
    local max_port=$((start_port + 200))

    while check_port_occupied "$port"; do
        echo "⚠️  $label 端口 $port 已占用，尝试 $((port + 1))"
        port=$((port + 1))
        if [ "$port" -gt "$max_port" ]; then
            echo "错误：$label 从 $start_port 起连续 200 个端口均不可用"
            exit 1
        fi
    done

    export "$var_name=$port"
    if [ "$port" != "$start_port" ]; then
        echo "✅ $label 已自动顺延为 $port"
    else
        echo "✅ $label 端口 $port 可用"
    fi
}

resolve_port FRONTEND_PORT "前端"
resolve_port BACKEND_PORT "后端"
resolve_port MYSQL_PORT "MySQL"
resolve_port REDIS_PORT "Redis"
echo ""

echo "构建并启动 Docker 容器集群..."
docker compose --env-file "$ENV_FILE" up -d --build

echo ""
echo "等待服务启动..."
sleep 3

echo ""
echo "检查服务状态..."
sleep 5

MAX_WAIT=180
WAITED=0
BACKEND_READY=0

while [ $WAITED -lt $MAX_WAIT ]; do
    if curl -s "http://127.0.0.1:$BACKEND_PORT/api/teas/categories" > /dev/null 2>&1; then
        BACKEND_READY=1
        break
    fi
    sleep 5
    WAITED=$((WAITED + 5))
    echo "等待后端服务启动... (${WAITED}s/${MAX_WAIT}s)"
done

echo ""
echo "========================================="
echo "  服务启动完成！"
echo "========================================="
echo ""
echo "  前端访问地址：http://localhost:$FRONTEND_PORT"
echo "  前端访问地址：http://127.0.0.1:$FRONTEND_PORT"
echo ""
echo "  后端API地址：http://localhost:$BACKEND_PORT/api"
echo "  后端API地址：http://127.0.0.1:$BACKEND_PORT/api"
echo ""
echo "  MySQL连接：127.0.0.1:$MYSQL_PORT"
echo "  Redis连接：127.0.0.1:$REDIS_PORT"
echo ""
echo "  日志查看：docker compose logs -f"
echo "  停止服务：docker compose down"
echo "========================================="

if [ "$BACKEND_READY" -eq 0 ]; then
    echo ""
    echo "⚠️  警告：后端服务可能尚未完全就绪，请稍后再试"
fi
