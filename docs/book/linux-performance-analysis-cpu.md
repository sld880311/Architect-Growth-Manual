<!-- TOC -->

- [CPU过高](#cpu过高)
    - [CPU使用率过高](#cpu使用率过高)
        - [计算方式](#计算方式)
        - [原因分析](#原因分析)
        - [定位步骤](#定位步骤)
        - [处理方式](#处理方式)
    - [load过高](#load过高)
    - [cpu_wait过高](#cpu_wait过高)
    - [参考](#参考)

<!-- /TOC -->

# CPU过高

## CPU使用率过高

在高负载情况下CPU持续使用率高一般没有问题，但是导致任务无法正常调度或者load持续增加就需要重点关注，并且定位具体原因。（**一般计算密集型应用 CPU 使用率偏高 load 偏低，IO 密集型相反。**）

### 计算方式

<div align=center>

![1587429499354.png](..\images\1587429499354.png)

</div>

### 原因分析

1. 频繁 FullGC/YongGC
2. 代码异常，出现死循环
3. 死锁

### 定位步骤

### 处理方式

1. 登录服务器之后使用[top](#_Top)命令，并且输入1按照cpu使用率降序排序获取使用率最高的线程
2. perf top -g -p pid查看调用关系（\-g开启调用关系分析，\-p指定线程号）
3. top -Hp pid可以查看某个进程的线程信息
4. jstack 线程ID 可以查看某个线程的堆栈情况，特别对于hung挂死的线程，可以使用选项\-F强制打印dump信息jstack -F pid
5. 执行 jstack pid|grep -A 10 pid的16进制，得到线程堆栈信息中1371这个线程所在行的后面10行

## load过高

## cpu_wait过高

## 参考

1. [重启大法好！线上常见问题排查手册](https://developer.aliyun.com/article/757655?spm=a2cdg.index.lists.18)
2. [arthas开源组件](https://github.com/alibaba/arthas)