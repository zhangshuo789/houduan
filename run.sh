#!/bin/bash
# run.sh - 一键部署脚本（停止+构建+启动）
# 适用于 CentOS 服务器

set -e

# ==================== 配置区域 ====================
IMAGE_NAME="volleyball-backend"
CONTAINER_NAME="volleyball-backend"
HOST_PORT=8090
DATA_DIR="/tools/volleyball-community/static"

# ==================== 命令处理 ====================
case "$1" in
  logs)
    echo "========== 查看容器日志 =========="
    docker logs -f $CONTAINER_NAME
    ;;
  status)
    echo "========== 容器状态 =========="
    docker ps -a | grep $CONTAINER_NAME
    ;;
  stop)
    echo "========== 停止容器 =========="
    docker stop $CONTAINER_NAME 2>/dev/null || true
    docker rm $CONTAINER_NAME 2>/dev/null || true
    echo "容器已停止并删除"
    ;;
  restart)
    echo "========== 重启容器 =========="
    docker restart $CONTAINER_NAME
    ;;
  *)
    # ==================== 一键部署 ====================
    echo "========== 排球社区后端部署 =========="

    # 1. 停止并删除旧容器
    echo "[1/3] 停止并删除旧容器..."
    docker stop $CONTAINER_NAME 2>/dev/null || true
    docker rm $CONTAINER_NAME 2>/dev/null || true

    # 2. 构建镜像
    echo "[2/3] 构建Docker镜像..."
    docker build -t $IMAGE_NAME .

    # 3. 创建目录并启动容器
    echo "[3/3] 启动容器..."
    mkdir -p $DATA_DIR
    docker run -d \
      -p $HOST_PORT:$HOST_PORT \
      --name $CONTAINER_NAME \
      -v $DATA_DIR:/app/static \
      $IMAGE_NAME

    echo "========== 部署完成 =========="
    echo "访问地址: http://121.40.154.188:$HOST_PORT"
    echo ""
    echo "常用命令:"
    echo "  ./run.sh logs    - 查看日志"
    echo "  ./run.sh status  - 查看状态"
    echo "  ./run.sh stop    - 停止服务"
    echo "  ./run.sh restart - 重启容器"
    ;;
esac
