#!/bin/bash
# deploy.sh - 部署排球社区后端服务
# 适用于 CentOS 服务器

set -e

# ==================== 配置区域 ====================
# 服务器地址
HOST_IP="121.40.154.188"

# Docker配置
IMAGE_NAME="volleyball-backend"
CONTAINER_NAME="volleyball-backend"

# 端口配置
HOST_PORT_HTTP=8090
CONTAINER_PORT_HTTP=8090

# 文件存储目录（宿主机）
HOST_DATA_DIR="/tools/volleyball-community/static"
CONTAINER_DATA_DIR="/app/static"

# ==================== 部署脚本 ====================
echo "========== 排球社区后端部署脚本 =========="

# 1. 构建项目
echo "[1/5] 构建项目..."
cd "$(dirname "$0")"
./mvnw clean package -DskipTests -q
if [ $? -ne 0 ]; then
    echo "构建失败！"
    exit 1
fi
echo "[1/5] 构建完成"

# 2. 创建必要的目录
echo "[2/5] 创建数据目录..."
ssh root@$HOST_IP "mkdir -p $HOST_DATA_DIR"
echo "[2/5] 目录创建完成"

# 3. 停止并删除旧容器
echo "[3/5] 停止并删除旧容器..."
ssh root@$HOST_IP "docker stop $CONTAINER_NAME 2>/dev/null || true"
ssh root@$HOST_IP "docker rm $CONTAINER_NAME 2>/dev/null || true"
echo "[3/5] 旧容器清理完成"

# 4. 复制 jar 包到服务器
echo "[4/5] 复制 jar 包到服务器..."
scp target/volleyball-community-backend-0.0.1.jar root@$HOST_IP:/tmp/app.jar
ssh root@$HOST_IP "docker build -t $IMAGE_NAME /tmp/app.jar -f - <<EOF
FROM openjdk:17-jdk-slim
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod
COPY app.jar /app/app.jar
ENTRYPOINT [\"java\", \"-jar\", \"/app/app.jar\"]
EXPOSE 8090
EOF"
echo "[4/5] Docker镜像构建完成"

# 5. 启动新容器
echo "[5/5] 启动新容器..."
ssh root@$HOST_IP "docker run -d \
  -p $HOST_PORT_HTTP:$CONTAINER_PORT_HTTP \
  --name $CONTAINER_NAME \
  -v $HOST_DATA_DIR:$CONTAINER_DATA_DIR \
  $IMAGE_NAME"

echo "========== 部署完成 =========="
echo "访问地址: http://$HOST_IP:$HOST_PORT_HTTP"
echo ""
echo "常用命令:"
echo "  查看日志: docker logs -f $CONTAINER_NAME"
echo "  重启服务: docker restart $CONTAINER_NAME"
echo "  停止服务: docker stop $CONTAINER_NAME"
