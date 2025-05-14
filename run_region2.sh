# Stop and remove existing master container if it exists

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
    # Build the MasterServer Docker image
    docker build -t regionserver:latest .
else
    echo "跳过重新编译Docker容器。"
fi



docker stop regionserver2 2>/dev/null || true
docker rm regionserver2 2>/dev/null || true

docker run -d --rm \
  --name regionserver2  \
  --network regionserver-net \
  --ip 172.20.0.4  \
  --hostname regionserver2   \
  --add-host=host.docker.internal:host-gateway \
  -p 20002:22222  \
  regionserver:latest


docker logs -f regionserver2