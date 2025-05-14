# 使用手册

项目分别由`client`,'masterServer`和`regionServer`三个部分组成，下面是各个部分的使用手册。目前支持通过docker实现本地模拟测试


## 前提条件
- 开启`zookeeper`服务
- 开启`ftp`服务
- 启动`docker-desktop`

## 启动
0. 构建docker网络
    ```bash
    docker network create --subnet=172.20.0.0/16 --gateway=172.20.0.1 regionserver-net
    ```
    这里创建一个docker网络，后续所有的容器都在这个网络中运行
1. 启动masterServer
   ```bash
   cd masterServer
   ./run_master.sh -n
   ```
   第一次构建容器需要下载镜像，时间较长，后续启动会快很多。后续如果不需要重新构建容器，可以省去`-n`参数
   
2. 启动regionServer
   ```bash
   ./run_region1.sh -n
   ./run_region2.sh -n
   ```
    这里启动两个regionServer，分别对应`region1`和`region2`，后续可以根据需要增加regionServer的数量
   

3. 启动client
    ```bash
    cd client
    ./run_client.sh -n
    ```
    这里启动client，后续可以根据需要增加client的数量，但是可能需要调整构建脚本