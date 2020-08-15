
# docker性能数据分析

## 常用命令

## 接口

## java内存在容器中的表现

### docker stats性能数据

```bash
docker stats baecf59a2f70 --no-stream
CONTAINER ID        NAME    CPU %               MEM USAGE / LIMIT   MEM %               NET I/O             BLOCK I/O           PIDS
baecf59a2f70        myapp   0.06%               605.3MiB / 8GiB     7.39%               0B / 0B             214MB / 0B          114
```

### ps性能数据

```bash
docker exec baecf59a2f70 ps -o rss,vsz,sz 1
RSS    VSZ    SZ
630232 13733060 3433265
```

RSS = Heap size + MetaSpace + OffHeap size

```bash
docker inspect baecf59a2f70|grep -i pid
ps -aux|grep 43315
```

## 参考

1. [Analyzing java memory usage in a Docker container](http://trustmeiamadeveloper.com/2016/03/18/where-is-my-memory-java/)
2. [docker api](https://docs.docker.com/engine/api/v1.24/#31-containers)
