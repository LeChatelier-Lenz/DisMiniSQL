#!/bin/bash
# check_network.sh

MASTER="172.20.0.1"
PORT="12345"
MAX_RETRIES=10
RETRY_DELAY=2

echo "等待连接到 masterserver..."

for i in $(seq 1 $MAX_RETRIES); do
    # 测试连接是否成功
    nc -zv $MASTER $PORT
    if [ $? -eq 0 ]; then
        echo "连接成功！"
        break
    else
        echo "连接失败，$i/$MAX_RETRIES 次重试，等待 $RETRY_DELAY 秒..."
        sleep $RETRY_DELAY
    fi
done

# 如果重试多次仍然失败，退出
if [ $? -ne 0 ]; then
    echo "无法连接到 masterserver，退出。"
    exit 1
fi
