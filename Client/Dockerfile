# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# 1. 复制源代码（假设 Client 代码在 DisMiniSQL/Client/src 目录）
COPY ./src /app/src

# 2. 安装工具并创建依赖目录
RUN apt-get update && \
    apt-get install -y wget && \
    mkdir -p /app/lib

# 3. 定义带重试的下载脚本（与 MasterServer 完全一致）
RUN echo '#!/bin/bash\n\
for i in {1..3}; do\n\
    wget -q --no-check-certificate "$1" -P "$2" && break\n\
    echo "Retry $i..."\n\
    sleep 2\n\
done' > /usr/local/bin/download-with-retry && \
    chmod +x /usr/local/bin/download-with-retry

# 4. 下载 Client 依赖的 JAR 文件
RUN download-with-retry "https://maven.aliyun.com/repository/public/org/apache/thrift/libthrift/0.13.0/libthrift-0.13.0.jar" /app/lib && \
    echo "Downloaded libthrift" && \
    download-with-retry "https://maven.aliyun.com/repository/public/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar" /app/lib && \
    echo "Downloaded slf4j-api" && \
    download-with-retry "https://maven.aliyun.com/repository/public/commons-net/commons-net/3.9.0/commons-net-3.9.0.jar" /app/lib && \
    echo "Downloaded commons-net" && \
    download-with-retry "https://maven.aliyun.com/repository/public/com/google/guava/guava/16.0.1/guava-16.0.1.jar" /app/lib && \
    echo "Downloaded guava" && \
    download-with-retry "https://maven.aliyun.com/repository/public/org/projectlombok/lombok/1.18.30/lombok-1.18.30.jar" /app/lib && \
    echo "Downloaded lombok"

# 5. 可选：添加 Client 特有依赖（根据实际代码补充）
# RUN download-with-retry "https://example.com/new-dependency.jar" /app/lib && \
#     echo "Downloaded new-dependency"

# 6. 验证下载的 JAR
RUN ls -l /app/lib/*.jar

# 7. 编译代码（排除测试文件）
RUN find /app/src -name "*.java" | grep -v "test" > sources.txt && \
    javac -cp "/app/src:/app/lib/*" -d /app/bin @sources.txt && \
    rm sources.txt

# ----------------------------
# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 1. 安装网络工具（与 MasterServer 一致）
RUN apt-get update && \
    apt-get install -y iputils-ping netcat && \
    rm -rf /var/lib/apt/lists/*

# 2. 复制构建产物
COPY --from=builder /app/bin /app/bin
COPY --from=builder /app/lib /app/lib

# 3. 设置环境变量（指向 Master 节点）
ENV MASTER_IP="master" MASTER_PORT=12345

# 4. 启动命令（直接运行 Client 类）
CMD ["java", "-cp", "/app/bin:/app/lib/*", "Client"]