<!-- TOC -->

- [page cache详解](#page-cache详解)
  - [相关场景](#相关场景)
  - [什么是page cache](#什么是page-cache)
    - [在Linux可以通过以下方式查看](#在linux可以通过以下方式查看)
    - [page cache指标说明](#page-cache指标说明)
    - [SwapCached说明(生产环境中不建议开启，防止IO引起性能抖动)](#swapcached说明生产环境中不建议开启防止io引起性能抖动)
    - [Shmem](#shmem)
    - [frem命令的说明](#frem命令的说明)
      - [缓存的具体含义](#缓存的具体含义)
        - [官方定义](#官方定义)
        - [具体解释](#具体解释)
  - [为什么使用page cache](#为什么使用page-cache)
  - [page cache的产生](#page-cache的产生)
    - [产生方式](#产生方式)
    - [产生方式的区别](#产生方式的区别)
    - [创建文件的过程](#创建文件的过程)
      - [脏页](#脏页)
  - [page cache的消亡](#page-cache的消亡)
  - [其他](#其他)
  - [参考](#参考)

<!-- /TOC -->
# page cache详解

## 相关场景

1. 服务器的 load 飙高；
2. 服务器的 I/O 吞吐飙高；
3. 业务响应时延出现大的毛刺；
4. 业务平均访问时延明显增加。

## 什么是page cache

<div align=center>

![1597721768372.png](..\images\1597721768372.png)
应用程序产生page cache的逻辑示意图

</div>

**page cache是内存管理的内存，属于内核不属于用户。**

### 在Linux可以通过以下方式查看

1. /proc/meminfo
2. free命令
3. vmstat命令

### page cache指标说明

通过/proc/meminfo查看内存信息如下：

```sh
MemTotal:        2046920 kB
MemFree:          375284 kB
MemAvailable:    1013780 kB
Buffers:          142100 kB
Cached:           668196 kB
SwapCached:            0 kB
Active:           959184 kB
Inactive:         279700 kB
Active(anon):     491680 kB
Inactive(anon):    46776 kB
Active(file):     467504 kB
Inactive(file):   232924 kB
Unevictable:           0 kB
Mlocked:               0 kB
SwapTotal:             0 kB
SwapFree:              0 kB
Dirty:                 0 kB
Writeback:             0 kB
AnonPages:        428608 kB
Mapped:            37768 kB
Shmem:            109868 kB
Slab:             138120 kB
SReclaimable:     126188 kB
SUnreclaim:        11932 kB
KernelStack:        2480 kB
PageTables:         4572 kB
NFS_Unstable:          0 kB
Bounce:                0 kB
WritebackTmp:          0 kB
CommitLimit:      891364 kB
Committed_AS:    1419192 kB
VmallocTotal:   34359738367 kB
VmallocUsed:        9136 kB
VmallocChunk:   34359724540 kB
HardwareCorrupted:     0 kB
AnonHugePages:    169984 kB
CmaTotal:              0 kB
CmaFree:               0 kB
HugePages_Total:     129
HugePages_Free:      129
HugePages_Rsvd:        0
HugePages_Surp:        0
Hugepagesize:       2048 kB
DirectMap4k:       53104 kB
DirectMap2M:     2043904 kB
DirectMap1G:           0 kB
```

通过计算发现：

**Buffers + Cached + SwapCached = Active(file) + Inactive(file) + Shmem + SwapCached**

在 Page Cache 中，Active(file)+Inactive(file) 是 File-backed page（与文件对应的内存页），平时用的 mmap() 内存映射方式和 buffered I/O 来消耗的内存就属于这部分，这部分在真实的生产环境上也最容易产生问题。

### SwapCached说明(生产环境中不建议开启，防止IO引起性能抖动)

SwapCached 是在打开了 **Swap** 分区后，把 Inactive(anon)+Active(anon) 这两项里的匿名页给交换到磁盘（swap out），然后再读入到内存（swap in）后分配的内存。由于读入到内存后原来的 Swap File 还在，所以 SwapCached 也可以认为是 File-backed page，即属于 Page Cache。

<div align=center>

![1597731780142.png](..\images\1597731780142.png)

</div>

### Shmem

Shmem 是指匿名共享映射这种方式分配的内存（free 命令中 shared 这一项），比如 tmpfs（临时文件系统）

### frem命令的说明

**数据来源于/proc/meminfo**

```bash
free -k
              total        used        free      shared  buff/cache   available
Mem:        2046920      732896      377464      109860      936560     1016044
Swap:             0           0           0
```

通过源码可知：**buff/cache = Buffers + Cached + SReclaimable**

SReclaimable 是指可以被回收的内核内存，包括 dentry 和 inode。

#### 缓存的具体含义

##### 官方定义

```bash
Buffers %lu
    Relatively temporary storage for raw disk blocks that shouldn't get tremendously large (20MB or so).
Cached %lu
   In-memory cache for files read from the disk (the page cache).  Doesn't include SwapCached.
SReclaimable %lu (since Linux 2.6.19)
    Part of Slab, that might be reclaimed, such as caches.
    
SUnreclaim %lu (since Linux 2.6.19)
    Part of Slab, that cannot be reclaimed on memory pressure.
```

##### 具体解释

1. Buffers 是对原始磁盘块的临时存储，也就是用来**缓存磁盘的数据**，通常不会特别大（20MB 左右）。这样，内核就可以把分散的写集中起来，统一优化磁盘的写入，比如可以把多次小的写合并成单次大的写等等。
2. Cached 是从磁盘读取文件的页缓存，也就是用来**缓存从文件读取的数据**。这样，下次访问这些文件数据时，就可以直接从内存中快速获取，而不需要再次访问缓慢的磁盘。
3. SReclaimable 是 Slab 的一部分。Slab 包括两部分，其中的可回收部分，用 SReclaimable 记录；而不可回收部分，用 SUnreclaim 记录。
4. **Buffer 是对磁盘数据的缓存，而 Cache 是文件数据的缓存，它们既会用在读请求中，也会用在写请求中**

## 为什么使用page cache

**减少 I/O，提升应用的 I/O 速度**

## page cache的产生

### 产生方式

1. Buffered I/O（标准 I/O）；
2. Memory-Mapped I/O（存储映射 I/O）。

<div align=center>

![1597749811737.png](..\images\1597749811737.png)

</div>

### 产生方式的区别

1. 标准 I/O 是写的 (write(2)) 用户缓冲区 (Userpace Page 对应的内存)，然后再将用户缓冲区里的数据拷贝到内核缓冲区 (Pagecache Page 对应的内存)；如果是读的 (read(2)) 话则是先从内核缓冲区拷贝到用户缓冲区，再从用户缓冲区读数据，也就是 buffer 和文件内容不存在任何映射关系。
2. 对于存储映射 I/O 而言，则是直接将 Pagecache Page 给映射到用户地址空间，用户直接读写 Pagecache Page 中内容。

### 创建文件的过程

<div align=center>

![1597750033559.png](..\images\1597750033559.png)

</div>

#### 脏页

```bash
cat /proc/vmstat | egrep "dirty|writeback"
nr_dirty 25
nr_writeback 0
nr_writeback_temp 0
nr_dirty_threshold 94000
nr_dirty_background_threshold 31333
```

nr_dirty 表示当前系统中积压了多少脏页，nr_writeback 则表示有多少脏页正在回写到磁盘中，他们两个的单位都是 Page(4KB)。

## page cache的消亡

page cache的回收主要是针对free 命令中的 buff/cache 中的这些就是“活着”的 Page Cache。回收的过程如下图所示：

<div align=center>

![1597750451415.png](..\images\1597750451415.png)

</div>

回收的方式主要是两种：直接回收和后台回收,具体的回收行为，可以使用以下命令查看：

```bash
sar -B 1
Linux 3.10.0-1062.9.1.el7.x86_64 (instance-gctg007a) 	08/18/2020 	_x86_64_	(1 CPU)

07:34:59 PM  pgpgin/s pgpgout/s   fault/s  majflt/s  pgfree/s pgscank/s pgscand/s pgsteal/s    %vmeff
07:35:00 PM      0.00      0.00     66.00      0.00     58.00      0.00      0.00      0.00      0.00
07:35:01 PM      0.00     25.53   1893.62      0.00    608.51      0.00      0.00      0.00      0.00
07:35:02 PM      0.00      0.00    648.48      0.00    280.81      0.00      0.00      0.00      0.00
07:35:03 PM      0.00      0.00     17.00      0.00     19.00      0.00      0.00      0.00      0.00
07:35:04 PM      0.00      0.00   1096.04      0.00    295.05      0.00      0.00      0.00      0.00
07:35:05 PM      0.00      0.00     17.00      0.00     23.00      0.00      0.00      0.00      0.00
07:35:06 PM      0.00     52.00     17.00      0.00     19.00      0.00      0.00      0.00      0.00
07:35:07 PM      0.00      0.00     17.00      0.00     44.00      0.00      0.00      0.00      0.00
07:35:08 PM      0.00      0.00     18.00      0.00     19.00      0.00      0.00      0.00      0.00
07:35:09 PM      0.00      0.00     18.18      0.00     19.19      0.00      0.00      0.00      0.00
07:35:10 PM      0.00      0.00     17.00      0.00     19.00      0.00      0.00      0.00      0.00
07:35:11 PM      0.00      0.00     17.00      0.00     19.00      0.00      0.00      0.00      0.00
```

1. pgscank/s : kswapd(后台回收线程) 每秒扫描的 page 个数。
2. pgscand/s: Application 在内存申请过程中每秒直接扫描的 page 个数。
3. pgsteal/s: 扫描的 page 中每秒被回收的个数。
4. %vmeff: pgsteal/(pgscank+pgscand), 回收效率，越接近 100 说明系统越安全，越接近 0 说明系统内存压力越大。

具体数据来源于/proc/vmstat。

<div align=center>

![1597750689812.png](..\images\1597750689812.png)

</div>

## 其他



## 参考

1. [proc帮助手册](https://www.kernel.org/doc/Documentation/filesystems/proc.rst)
2. [常用性能分析工具](linux-performance-analysis-tools.md)
