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
    - [PageCache数据结构](#pagecache数据结构)
  - [为什么使用page cache](#为什么使用page-cache)
  - [page cache的产生](#page-cache的产生)
    - [产生方式](#产生方式)
    - [产生方式的区别](#产生方式的区别)
    - [常规文件读写](#常规文件读写)
      - [脏页](#脏页)
    - [mmap](#mmap)
      - [memory map具体步骤如下](#memory-map具体步骤如下)
    - [sendfile](#sendfile)
    - [顺序读写](#顺序读写)
      - [文件预读](#文件预读)
      - [顺序读写高效的原因](#顺序读写高效的原因)
  - [page cache的消亡](#page-cache的消亡)
  - [其他](#其他)
    - [DMA（Direct Memory Access,直接存储器访问）](#dmadirect-memory-access直接存储器访问)
    - [堆外内存](#堆外内存)
      - [堆内存与堆外内存的关系](#堆内存与堆外内存的关系)
      - [最佳实践](#最佳实践)
  - [PageCache内存回收](#pagecache内存回收)
    - [回收过程](#回收过程)
    - [避免PageCache回收出现的性能问题](#避免pagecache回收出现的性能问题)
      - [memory cgroup protection](#memory-cgroup-protection)
    - [出现load过高的原因](#出现load过高的原因)
      - [直接内存回收引起](#直接内存回收引起)
        - [内存回收过程](#内存回收过程)
      - [系统中脏页积压过多](#系统中脏页积压过多)
        - [内存申请过程](#内存申请过程)
        - [解决方法](#解决方法)
      - [系统numa策略配置不当](#系统numa策略配置不当)
  - [内存泄漏](#内存泄漏)
    - [OOM KILL逻辑](#oom-kill逻辑)
    - [如何观察内核内存泄漏](#如何观察内核内存泄漏)
    - [排查思路](#排查思路)
  - [其他](#其他-1)
    - [清理缓存buffer/cache](#清理缓存buffercache)
      - [运行sync将dirty的内容写回硬盘](#运行sync将dirty的内容写回硬盘)
      - [通过修改proc系统的drop_caches清理free的cache](#通过修改proc系统的drop_caches清理free的cache)
      - [可以调用crond定时任务：每10分钟执行一次](#可以调用crond定时任务每10分钟执行一次)
    - [重要配置参数](#重要配置参数)
      - [/proc/sys/vm/dirty_ratio（同步刷盘）](#procsysvmdirty_ratio同步刷盘)
      - [/proc/sys/vm/dirty_background_ratio（异步刷盘）](#procsysvmdirty_background_ratio异步刷盘)
      - [/proc/sys/vm/dirty_writeback_centisecs](#procsysvmdirty_writeback_centisecs)
      - [/proc/sys/vm/dirty_expire_centisecs](#procsysvmdirty_expire_centisecs)
      - [/proc/sys/vm/drop_caches](#procsysvmdrop_caches)
      - [/proc/sys/vm/page_cluster](#procsysvmpage_cluster)
      - [/proc/sys/vm/swapiness](#procsysvmswapiness)
      - [/proc/sys/vm/vfs_cache_pressure](#procsysvmvfs_cache_pressure)
  - [参考](#参考)

<!-- /TOC -->
# page cache详解

应用程序要存储或访问数据时，只需读或者写"文件"的一维地址空间即可，而这个地址空间与存储设备上存储块之间的对应关系则由操作系统维护。说白了，文件就是基于内核态Page Cache的一层抽象。

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

1. Shmem 是指匿名共享映射这种方式分配的内存（free 命令中 shared 这一项）
2. 进程使用mmap(MAP_ANON|MAP_SHARED)的方式申请内存
3. tmpfs： 磁盘的速度是远远低于内存的，有些应用程序为了提升性能，会避免将一些无需持续化存储的数据写入到磁盘，而是把这部分临时数据写入到内存中，然后定期或者在不需要这部分数据时，清理掉这部分内容来释放出内存。在这种需求下，就产生了一种特殊的 Shmem：tmpfs
4. 

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

### PageCache数据结构

1. 内存管理系统与Page Cache交互，负责维护每项 Page Cache 的分配和回收，同时在使用 memory map 方式访问时负责建立映射；
2. VFS 与Page Cache交互，负责 Page Cache 与用户空间的数据交换，即文件读写；
3. 具体文件系统则一般只与 Buffer Cache 交互，它们负责在外围存储设备和 Buffer Cache 之间交换数据。
4. 一个Page Cache包含多个Buffer Cache，一个Buffer Cache与一个磁盘块一一对应；假定了 Page 的大小是 4K，则文件的每个4K的数据块最多只能对应一个 Page Cache 项，它通过一个是 radix tree来管理文件块和page cache的映射关系，Radix tree 是一种搜索树，Linux 内核利用这个数据结构来通过文件内偏移快速定位 Cache 项。

<div align=center>

![1598412861987.png](..\images\1598412861987.png)

![1598412872367.png](..\images\1598412872367.png)

</div>

## 为什么使用page cache

**减少 I/O，提升应用的 I/O 速度**

<div align=center>

![1598412292674.png](..\images\1598412292674.png)
Linux 操作系统中文件 Cache 管理与内存管理以及文件系统的关系示意图

</div>

1. **具体文件系统：**，如 ext2/ext3、jfs、ntfs 等，负责在文件 Cache和存储设备之间交换数据
2. **虚拟文件系统VFS：** 负责在应用程序和文件 Cache 之间通过 read/write 等接口交换数据
3. **内存管理系统：** 负责文件 Cache 的分配和回收
4. **虚拟内存管理系统(VMM)：** 则允许应用程序和文件 Cache 之间通过 memory map的方式交换数据
5. **在 Linux 系统中，文件 Cache 是内存管理系统、文件系统以及应用程序之间的一个联系枢纽**。

## page cache的产生

### 产生方式

1. Buffered I/O（标准 I/O）如：read/write/sendfile等；
2. Memory-Mapped I/O（存储映射 I/O）如：mmap；
3. sendfile和mmap都是零拷贝的实现方案。

<div align=center>

![1597749811737.png](..\images\1597749811737.png)

</div>

### 产生方式的区别

1. 标准 I/O 是写的 (write(2)) 用户缓冲区 (Userpace Page 对应的内存)，然后再将用户缓冲区里的数据拷贝到内核缓冲区 (Pagecache Page 对应的内存)；如果是读的 (read(2)) 话则是先从内核缓冲区拷贝到用户缓冲区，再从用户缓冲区读数据，也就是 buffer 和文件内容不存在任何映射关系。
2. 对于存储映射 I/O 而言，则是直接将 Pagecache Page 给映射到用户地址空间，用户直接读写 Pagecache Page 中内容。

### 常规文件读写

FileChannel#read，FileChannel#write，共涉及四次上下文切换（内核态和用户态的切换，包括read调用，read返回，write调用，write返回）和四次数据拷贝。

<div align=center>

![1598421055864.png](..\images\1598421055864.png)

![1598421062853.png](..\images\1598421062853.png)

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

### mmap

1. 文件（page cache）直接映射到用户虚拟地址空间，内核态和用户态共享一片page cache，避免了一次数据拷贝
2. 建立mmap之后，并不会立马加载数据到内存，只有真正使用数据时，才会引发缺页异常并加载数据到内存

<div align=center>

![1598421396771.png](..\images\1598421396771.png)

![1598421406978.png](..\images\1598421406978.png)

</div>

#### memory map具体步骤如下

1. 应用程序调用mmap（图中1），先到内核中
2. 后调用do_mmap_pgoff（图中2），该函数从应用程序的地址空间中**分配一段区域作为映射的内存地址**，并使用一个VMA（vm_area_struct）结构代表该区域，
3. 之后就返回到应用程序（图中3）
4. 当应用程序访问mmap所返回的地址指针时（图中4），由于虚实映射尚未建立，会触发缺页中断（图中5）。之后系统会调用缺页中断处理函数（图中6），在缺页中断处理函数中，内核通过相应区域的VMA结构判断出该区域属于文件映射，于是调用具体文件系统的接口读入相应的Page Cache项（图中7、8、9），并填写相应的虚实映射表。
5. 经过这些步骤之后，应用程序就可以正常访问相应的内存区域了。

### sendfile

1. 使用sendfile的方式避免了用户空间与内核空间的交互，复制次数减少到三次，内核态与用户态切换减少到两次。
2. 在 Linux 内核 2.4 及后期版本中，针对套接字缓冲区描述符做了相应调整，DMA自带了收集功能，对于用户方面，用法还是一样。内部只把包含数据位置和长度信息的描述符追加到套接字缓冲区，DMA 引擎直接把数据从内核缓冲区传到协议引擎，从而消除了最后一次 CPU参与的拷贝动作。

<div align=center>

![1598433791064.png](..\images\1598433791064.png)

![1598433800506.png](..\images\1598433800506.png)

</div>

### 顺序读写

#### 文件预读

文件的预读机制，它是一种将磁盘块预读到page cache的机制,执行步骤如下：
1. 对于每个文件的**第一个读请求**，系统读入所请求的页面并读入紧随其后的少数几个页面(不少于一个页面，通常是三个页面)，这时的预读称为**同步预读**。
2. 对于**第二次读请求**，如果所读页面不在Cache中，即不在前次预读的group中，则表明文件访问不是顺序访问，系统继续采用同步预读；如果所读页面在Cache中，则表明前次预读命中，操作系统把预读group扩大一倍，并让底层文件系统读入group中剩下尚不在Cache中的文件数据块，这时的预读称为**异步预读**。
3. 无论第二次读请求是否命中，系统都要更新当前预读group的大小。
4. 系统中定义了一个window，它包括前一次预读的group和本次预读的group。任何接下来的读请求都会处于两种情况之一：
   - 第一种情况是所请求的页面处于预读window中，这时继续进行异步预读并更新相应的window和group；
   - 第二种情况是所请求的页面处于预读window之外，这时系统就要进行同步预读并重置相应的window和group。

<div align=center>

![1598434976482.png](..\images\1598434976482.png)

</div>

图中group指一次读入page cached的集合；window包括前一次预读的group和本次预读的group；浅灰色代表要用户想要查找的page cache，深灰色代表命中的page。

#### 顺序读写高效的原因

<div align=center>

![1598435243913.png](..\images\1598435243913.png)

</div>

以顺序读为例，当用户发起一个 fileChannel.read(4kb) 之后，实际发生了两件事

1. 操作系统从磁盘加载了 16kb 进入 PageCache，这被称为预读
2. 操作通从 PageCache 拷贝 4kb 进入用户内存
3. 当用户继续访问接下来的 [4kb,16kb] 的磁盘内容时，便是直接从 PageCache 去访问了

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

### DMA（Direct Memory Access,直接存储器访问）

1. DMA的出现就是为了解决批量数据的输入/输出问题。DMA是指外部设备不通过CPU而直接与系统内存交换数据的接口技术
2. DMA控制器需要具备的功能：
   - 能向CPU发出系统保持信号，提出总线接管请求
   - 当CPU同意接管请求之后，对总线的控制交给DMA
   - **能对存储器寻址及能修改地址指针，实现对内存的读写**
   - 能决定本次DMA传送的字节数，判断DMA传送是否借宿
   - 发送DMA结束信号，使CPU恢复正常工作状态

<div align=center>

![1598409974379.png](..\images\1598409974379.png)

![1598409984707.png](..\images\1598409984707.png)

</div>

### 堆外内存

#### 堆内存与堆外内存的关系

<div align=center>

![1598435602865.png](..\images\1598435602865.png)

</div>

#### 最佳实践

1. 当需要申请大块的内存时，堆内内存会受到限制，只能分配堆外内存。
2. 堆外内存适用于生命周期中等或较长的对象。(如果是生命周期较短的对象，在 YGC 的时候就被回收了，就不存在大内存且生命周期较长的对象在 FGC 对应用造成的性能影响)。
3. 堆内内存刷盘的过程中，还需要复制一份到堆外内存，这部分内容可以在 FileChannel 的实现源码中看到细节
   -  使用 HeapByteBuffer 读写都会经过 DirectByteBuffer，写入数据的流转方式其实是：HeapByteBuffer -> DirectByteBuffer -> PageCache -> Disk，读取数据的流转方式正好相反。
   -  使用 HeapByteBuffer 读写会申请一块跟线程绑定的 DirectByteBuffer。这意味着，线程越多，临时 DirectByteBuffer 就越会占用越多的空间。
4. 堆外内存就是把内存对象分配在Java虚拟机堆以外的内存，这些内存直接受操作系统管理（而不是虚拟机），这样做的结果就是能够在一定程度上减少垃圾回收对应用程序造成的影响。
5. 内存回收流程

<div align=center>

![1598440617394.png](..\images\1598440617394.png)

</div>

## PageCache内存回收

### 回收过程

在内存紧张的时候会触发内存回收，内存回收会尝试去回收reclaimable（可被回收）的内存。包括**PageCache以及reclaimable kernel memory（比如slab）**。

<div align=center>

![1598493313084.png](..\images\1598493313084.png)

</div>

### 避免PageCache回收出现的性能问题

#### memory cgroup protection

<div align=center>

![1598493920439.png](..\images\1598493920439.png)

</div>

1. **memory.max**：memory cgroup 内的进程最多能够分配的内存，如果不设置的话，就默认不做内存大小的限制
2. **memory.high**：当 memory cgroup 内进程的内存使用量超过了该值后就会立即被回收掉，目的是为了**尽快的回收掉不活跃的 Page Cache**。
3. **memory.low**：用来保护重要数据的，当 memory cgroup 内进程的内存使用量低于了该值后，在内存紧张触发回收后就会先去回收不属于该 memory cgroup 的 Page Cache，等到其他的 Page Cache 都被回收掉后再来回收这些 Page Cache。
4. **memory.min**：用来保护重要数据的，只不过与 memoy.low 有所不同的是，当 memory cgroup 内进程的内存使用量低于该值后，即使其他不在该 memory cgroup 内的 Page Cache 都被回收完了也不会去回收这些 Page Cache。
5. **总结：如果你想要保护你的 Page Cache 不被回收，你就可以考虑将你的业务进程放在一个 memory cgroup 中，然后设置 memory.{min,low} 来进行保护；与之相反，如果你想要尽快释放你的 Page Cache，那你可以考虑设置 memory.high 来及时的释放掉不活跃的 Page Cache。**

### 出现load过高的原因

#### 直接内存回收引起

##### 内存回收过程

<div align=center>

![1602664770697.png](..\images\1602664770697.png)

</div>

后台回收原理：

<div align=center>

![1602664859410.png](..\images\1602664859410.png)

</div>

通过调整参数vm.min_free_kbytes来提高后台进程回收频率。

```bash
cat /proc/sys/vm/min_free_kbytes
vi /etc/sysctl.conf vm.min_free_kbytes=524288 
sysctl -p
```

通过调整内存水位，在一定程度上保障了应用的内存申请，但是同时也带来了一定的内存浪费，因为系统始终要保障有这么多的 free 内存，这就压缩了 Page Cache 的空间。调整的效果你可以通过 /proc/zoneinfo 来观察

#### 系统中脏页积压过多

##### 内存申请过程

<div align=center>

![1602665372291.png](..\images\1602665372291.png)

</div>

##### 解决方法

设置配置：/proc/vmstat
```bash
vm.dirty_background_bytes = 0
vm.dirty_background_ratio = 10
vm.dirty_bytes = 0
vm.dirty_expire_centisecs = 3000
vm.dirty_ratio = 20
```

#### 系统numa策略配置不当

## 内存泄漏

### OOM KILL逻辑

<div align=center>

![1602661306900.png](..\images\1602661306900.png)

</div>

可以调整oom_score_adj来防止进程被杀掉（不建议配置）

### 如何观察内核内存泄漏

1. 如果 /proc/meminfo 中内核内存（比如 VmallocUsed 和 SUnreclaim）太大，那很有可能发生了内核内存泄漏
2. 周期性地观察 VmallocUsed 和 SUnreclaim 的变化，如果它们持续增长而不下降，也可能是发生了内核内存泄漏
3. 通过 /proc/vmallocinfo 来看到该模块的内存使用情况
4. kmemleak 内核内存分析工具

### 排查思路

<div align=center>

![1602663224947.png](..\images\1602663224947.png)

</div>

1. 应用程序可以通过 malloc() 和 free() 在用户态申请和释放内存，与之对应，可以通过 kmalloc()/kfree() 以及 vmalloc()/vfree() 在内核态申请和释放内存
2. vmalloc 申请的内存会体现在 VmallocUsed 这一项中，即已使用的 Vmalloc 区大小；而 kmalloc 申请的内存则是体现在 Slab 这一项中，它又分为两部分，其中 SReclaimable 是指在内存紧张的时候可以被回收的内存，而 SUnreclaim 则是不可以被回收只能主动释放的内存。

## 其他

### 清理缓存buffer/cache

#### 运行sync将dirty的内容写回硬盘

#### 通过修改proc系统的drop_caches清理free的cache

```bash
echo 3 > /proc/sys/vm/drop_caches
说明
echo 1 > /proc/sys/vm/drop_caches:表示清除pagecache。 
echo 2 > /proc/sys/vm/drop_caches:表示清除回收slab分配器中的对象（包括目录项缓存和inode缓存）。slab分配器是内核中管理内存的一种机制，其中很多缓存数据实现都是用的pagecache。 
echo 3 > /proc/sys/vm/drop_caches:表示清除pagecache和slab分配器中的缓存对象。
```

可以通过`/proc/vmstat`文件判断是否执行过drop_caches：

```bash
[root@instance-gctg007a ~]# cat /proc/vmstat | grep drop
drop_pagecache 0
drop_slab 0
```

#### 可以调用crond定时任务：每10分钟执行一次

```bash
*/10 * * * * sync;echo 3 > /proc/sys/vm/drop_caches;  
```

### 重要配置参数

#### /proc/sys/vm/dirty_ratio（同步刷盘）

这个参数控制文件系统的文件系统写缓冲区的大小，单位是百分比，表示系统内存的百分比，**表示当写缓冲使用到系统内存多少的时候，开始向磁盘写出数据**。增大之会使用更多系统内存用于磁盘写缓冲，也可以极大提高系统的写性能。但是，当你需要持续、恒定的写入场合时，应该降低其数值，一般启动上缺省是 10。设1加速程序速度

#### /proc/sys/vm/dirty_background_ratio（异步刷盘）

这个参数控制文件系统的pdflush进程，在何时刷新磁盘。单位是百分比，**表示系统内存的百分比，意思是当写缓冲使用到系统内存多少的时 候，pdflush开始向磁盘写出数据**。增大之会使用更多系统内存用于磁盘写缓冲，也可以极大提高系统的写性能。但是，当你需要持续、恒定的写入场合时， 应该降低其数值，一般启动上缺省是 5

#### /proc/sys/vm/dirty_writeback_centisecs

这个参数控制内核的脏数据刷新进程pdflush的运行间隔。单位是 1/100 秒。缺省数值是500，也就是 5 秒。如果你的系统是持续地写入动作，那么实际上还是降低这个数值比较好，这样可以把尖峰的写操作削平成多次写操

#### /proc/sys/vm/dirty_expire_centisecs

这个参数声明Linux内核写缓冲区里面的数据多“旧”了之后，pdflush进程就开始考虑写到磁盘中去。单位是 1/100秒。缺省是 30000，也就是 30 秒的数据就算旧了，将会刷新磁盘。对于特别重载的写操作来说，这个值适当缩小也是好的，但也不能缩小太多，因为缩小太多也会导致IO提高太快。建议设置为 1500，也就是15秒算旧。 

#### /proc/sys/vm/drop_caches

释放已经使用的cache

#### /proc/sys/vm/page_cluster

该文件表示在写一次到swap区的时候写入的页面数量，0表示1页，1表示2页，2表示4页。

#### /proc/sys/vm/swapiness

该文件表示系统进行交换行为的程度，数值（0-100）越高，越可能发生磁盘交换。

#### /proc/sys/vm/vfs_cache_pressure

该文件表示内核回收用于directory和inode cache内存的倾向

## 参考

1. [proc帮助手册](https://www.kernel.org/doc/Documentation/filesystems/proc.rst)
2. [常用性能分析工具](linux-performance-analysis-tools.md)
3. [NIO进阶篇：Page Cache、零拷贝、顺序读写、堆外内存](https://blog.csdn.net/a1240466196/article/details/106456200)
4. [面试官：RocketMQ 如何基于mmap+page cache实现磁盘文件的高性能读写？](https://www.imooc.com/article/301624)
5. [文件系统缓存dirty_ratio与dirty_background_ratio两个参数区别](http://blog.sina.com.cn/s/blog_448574810101k1va.html)
6. [PageCache系列之五 统一缓存之PageCache](https://zhuanlan.zhihu.com/p/42364591)
