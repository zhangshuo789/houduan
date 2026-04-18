#!/bin/bash
# run.sh - 服务器上直接运行容器的脚本
# 适用于 CentOS 服务器

set -e

# ==================== 配置区域 ====================
IMAGE_NAME="volleyball-backend"
CONTAINER_NAME="volleyball-backend"
HOST_PORT=8090
DATA_DIR="/tools/volleyball-community/static"

# ==================== 命令处理 ====================
case "$1" in
    build)
        echo "构建Docker镜像..."
        docker build -t $IMAGE_NAME .
        echo "镜像构建完成"
        ;;
    start)
        echo "启动容器..."
        mkdir -p $DATA_DIR
        docker run -d \
          -p $HOST_PORT:$HOST_PORT \
          --name $CONTAINER_NAME \
          -v $DATA_DIR:/app/static \
          $IMAGE_NAME
        echo "容器已启动"
        ;;
    stop)
        echo "停止容器..."
        docker stop $CONTAINER_NAME
        docker rm $CONTAINER_NAME
        echo "容器已停止并删除"
        ;;
    restart)
        echo "重启容器..."
        docker restart $CONTAINER_NAME
        echo "容器已重启"
        ;;
    redeploy)
        echo "重新部署..."
        $0 stop
        $0 build
        $0 start
        ;;
    logs)
        echo "查看日志 (Ctrl+C 退出)..."
        docker logs -f $CONTAINER_NAME
        ;;
    status)
        docker ps -a | grep $CONTAINER_NAME
        ;;
    *)
        echo "用法: $0 {build|start|stop|restart|redeploy|logs|status}"
        exit 1
        ;;
esac
