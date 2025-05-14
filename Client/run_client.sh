#!/bin/bash

recompile=false

# 处理命令行选项
while getopts ":n" opt; do
  case $opt in
    n)
      recompile=true
      ;;
    \?)
      echo "无效选项: -$OPTARG" >&2
      exit 1
      ;;
  esac
done

shift $((OPTIND - 1))  # 移除已处理的选项

if [ "$recompile" = true ]; then
    echo "重新编译Docker容器..."
    # Build the clientServer Docker image
    docker build -t client:latest .
else
    echo "跳过重新编译Docker容器。"
fi



# Stop and remove existing client container if it exists
docker stop client 2>/dev/null || true
docker rm client 2>/dev/null || true

# Run the clientServer container
docker run -it --rm \
    --name client \
    --network regionserver-net \
    --ip 172.20.0.5 \
    --hostname client \
    --add-host=host.docker.internal:host-gateway \
    -p 10005:10005 \
    client:latest

# Wait for the container to start
sleep 5


# Check network connectivity
echo "Checking network connectivity..."
docker exec client ping -c 1 host.docker.internal
docker exec client nc -zv host.docker.internal 2181

docker logs -f client