<!-- TOC -->

- [Hadoop说明](#hadoop%e8%af%b4%e6%98%8e)
  - [概念](#%e6%a6%82%e5%bf%b5)
  - [HDFS](#hdfs)
  - [MapReduce](#mapreduce)
  - [Hadoop MapReduce 作业的生命周期](#hadoop-mapreduce-%e4%bd%9c%e4%b8%9a%e7%9a%84%e7%94%9f%e5%91%bd%e5%91%a8%e6%9c%9f)

<!-- /TOC -->
# Hadoop说明

## 概念

就是一个大数据解决方案。它提供了一套分布式系统基础架构。 核心内容包含 hdfs 和 mapreduce。hadoop2.0 以后引入 yarn.  
 hdfs 是提供数据存储的，mapreduce 是方便数据计算的。 
1.	hdfs 又对应 namenode 和 datanode. namenode 负责保存元数据的基本信息， datanode 直接存放数据本身； 
2.	mapreduce 对应 jobtracker 和 tasktracker. jobtracker 负责分发任务，tasktracker 负责执行具体任务； 
3.	对应到 master/slave 架构，namenode 和 jobtracker 就应该对应到 master, datanode 和 tasktracker 就应该对应到 slave. 


## HDFS 
1.1.1	Client 
Client（代表用 户） 通过与 NameNode 和 DataNode 交互访问 HDFS 中 的文件。 Client 提供了一个类似 POSIX 的文件系统接口供用户调用。 
1.1.2	NameNode 
整个 Hadoop 集群中只有一个 NameNode。 它是整个系统的“ 总管”， 负责管理 HDFS 的目录树和相关的文件元数据信息。 这些信息是以“ fsimage”（ HDFS 元数据镜像文件）和 “ editlog”（HDFS 文件改动日志）两个文件形式存放在本地磁盘，当 HDFS 重启时重新构造出来的。此外， NameNode 还负责监控各个 DataNode 的健康状态， 一旦发现某个 DataNode 宕掉，则将该 DataNode 移出 HDFS 并重新备份其上面的数据。 
1.1.3	Secondary NameNode 
Secondary NameNode 最重要的任务并不是为 NameNode 元数据进行热备份， 而是定期合并 fsimage 和 edits 日志， 并传输给 NameNode。 这里需要注意的是，为了减小 NameNode 压力， NameNode 自己并不会合并 fsimage 和 edits， 并将文件存储到磁盘上， 而是交由
Secondary NameNode 完成。 
1.1.4	DataNode 
一般而言， 每个 Slave 节点上安装一个 DataNode， 它负责实际的数据存储， 并将数据信息定期汇报给 NameNode。 DataNode 以固定大小的 block 为基本单位组织文件内容， 默认情况下 block 大小为 64MB。 当用户上传一个大的文件到 HDFS 上时， 该文件会被切分成若干个 block，分别存储到不同的 DataNode ； 同时，为了保证数据可靠， 会将同一个 block 以流水线方式写到若干个（默认是 3，该参数可配置）不同的 DataNode 上。 这种文件切割后存储的过程是对用户透明的。

## MapReduce 
同 HDFS 一样，Hadoop MapReduce 也采用了 Master/Slave（M/S）架构，具体如图所示。它主要由以下几个组件组成：Client、JobTracker、TaskTracker 和 Task。 下面分别对这几个组件进行介绍。 

<div align=center>

![1589029021510.png](..\images\1589029021510.png)

</div>

1.1.1	Client 
用户编写的 MapReduce 程序通过 Client 提交到 JobTracker 端； 同时， 用户可通过 Client 提供的一些接口查看作业运行状态。 在 Hadoop 内部用“作业”（Job） 表示 MapReduce 程序。一个 MapReduce 程序可对应若干个作业，而每个作业会被分解成若干个 Map/Reduce 任务
（Task）。 
1.1.2	JobTracker 
JobTracker 主要负责资源监控和作业调度。JobTracker 监控所有 TaskTracker 与作业的健康状况，一旦发现失败情况后，其会将相应的任务转移到其他节点；同时 JobTracker 会跟踪任务的执行进度、资源使用量等信息，并将这些信息告诉任务调度器，而调度器会在资源出现空闲时，选择合适的任务使用这些资源。在 Hadoop 中，任务调度器是一个可插拔的模块，用户可以根据自己的需要设计相应的调度器。 
 
1.1.3	TaskTracker 
TaskTracker 会周期性地通过 Heartbeat 将本节点上资源的使用情况和任务的运行进度汇报给 JobTracker， 同时接收 JobTracker 发送过来的命令并执行相应的操作（如启动新任务、 杀死任务等）。TaskTracker 使用“slot” 等量划分本节点上的资源量。“slot” 代表计算资源（CPU、内存等）。一个 Task 获取到一个 slot 后才有机会运行，而 Hadoop 调度器的作用就是将各个
TaskTracker 上的空闲 slot 分配给 Task 使用。 slot 分为 Map slot 和 Reduce slot 两种，分别供 MapTask 和 Reduce Task 使用。 TaskTracker 通过 slot 数目（可配置参数）限定 Task 的并发度。 
1.1.4	Task 
Task 分为 Map Task 和 Reduce Task 两种， 均由 TaskTracker 启动。 HDFS 以固定大小的 block 为基本单位存储数据， 而对于 MapReduce 而言， 其处理单位是 split。split 与 block 的对应关系如图所示。 split 是一个逻辑概念， 它只包含一些元数据信息， 比如数据起始位置、数据长度、数据所在节点等。它的划分方法完全由用户自己决定。 但需要注意的是，split 的多少决定了 Map 
Task 的数目 ，因为每个 split 会交由一个 Map Task 处理。 
Map Task 执行过程如图所示。 由该图可知，Map Task 先将对应的 split 迭代解析成一个个 key/value 对，依次调用用户自定义的 map() 函数进行处理，最终将临时结果存放到本地磁盘上，其中临时数据被分成若干个 partition，每个 partition 将被一个 Reduce Task 处理。 

<div align=center>

![1589029065613.png](..\images\1589029065613.png)

</div>

1.1.1	Reduce Task 执行过程 
该过程分为三个阶段 
1.	从远程节点上读取 MapTask 中间结果（称为“Shuffle 阶段”）； 
2.	按照 key 对 key/value 对进行排序（称为“ Sort 阶段”）； 
3.	依次读取<key, value list>，调用用户自定义的 reduce() 函数处理，并将最终结果存到 HDFS 上（称为“ Reduce 阶段”）。 

## Hadoop MapReduce 作业的生命周期 
1.作业提交与初始化 
1.	用户提交作业后， 首先由 JobClient 实例将作业相关信息， 比如将程序 jar 包、作业配置文件、 分片元信息文件等上传到分布式文件系统（ 一般为 HDFS）上，其中，分片元信息文件记录了每个输入分片的逻辑位置信息。 然后 JobClient 通过 RPC 通知 JobTracker。 JobTracker 收到新作业提交请求后， 由 作业调度模块对作业进行初始化：为作业创建一个 JobInProgress 对象以跟踪作业运行状况， 而 JobInProgress 则会为每个 Task 创建一个 TaskInProgress 对象以跟踪每个任务的运行状态， TaskInProgress 可能需要管理多个
“ Task 运行尝试”（ 称为“ Task Attempt”）。 
2.任务调度与监控。 
2.	前面提到，任务调度和监控的功能均由 JobTracker 完成。TaskTracker 周期性地通过 
Heartbeat 向 JobTracker 汇报本节点的资源使用 情况， 一旦出 现空闲资源， JobTracker 会按照一定的策略选择一个合适的任务使用该空闲资源， 这由任务调度器完成。 任务调度器是一个可插拔的独立模块， 且为双层架构， 即首先选择作业， 然后从该作业中选择任务， 其中，选择任务时需要重点考虑数据本地性。 此外，JobTracker 跟踪作业的整个运行过程，并为作业的成功运行提供全方位的保障。 首先， 当 TaskTracker 或者 Task 失败时， 转移计算任务 ； 其次， 当某个 Task 执行进度远落后于同一作业的其他 Task 时，为之启动一个相同 
Task， 并选取计算快的 Task 结果作为最终结果。 
3.任务运行环境准备 
3.	运行环境准备包括 JVM 启动和资源隔 离， 均由 TaskTracker 实现。 TaskTracker 为每个 
Task 启动一个独立的 JVM 以避免不同 Task 在运行过程中相互影响 ； 同时，TaskTracker 使用了操作系统进程实现资源隔离以防止 Task 滥用资源。  
4.任务执行 
4.	TaskTracker 为 Task 准备好运行环境后， 便会启动 Task。 在运行过程中， 每个 Task 的最
新进度首先由 Task 通过 RPC 汇报给 TaskTracker， 再由 TaskTracker 汇报给 JobTracker。  
5.作业完成。 
5.	待所有 Task 执行完毕后， 整个作业执行成功。 
