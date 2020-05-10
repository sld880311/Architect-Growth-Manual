<!-- TOC -->

- [YARN](#yarn)

<!-- /TOC -->

# YARN

1.1	概念 
YARN 是一个资源管理、任务调度的框架，主要包含三大模块：ResourceManager（RM）、 NodeManager（NM）、ApplicationMaster（AM）。其中，ResourceManager 负责所有资源的监控、分配和管理；ApplicationMaster 负责每一个具体应用程序的调度和协调； NodeManager 负责每一个节点的维护。对于所有的applications，RM拥有绝对的控制权和对资源的分配权。而每个 AM 则会和 RM 协商资源，同时和 NodeManager 通信来执行和监控 task。几个模块之间的关系如图所示。 

<div align=center>

![1589029394815.png](..\images\1589029394815.png)

</div>

1.1	ResourceManager 
1.	ResourceManager 负责整个集群的资源管理和分配，是一个全局的资源管理系统。 
2.	NodeManager 以心跳的方式向 ResourceManager 汇报资源使用情况（目前主要是 CPU 和内存的使用情况）。RM 只接受 NM 的资源回报信息，对于具体的资源处理则交给 NM 自己处理。 
3.	YARN Scheduler 根据 application 的请求为其分配资源，不负责 application job 的监控、追踪、运行状态反馈、启动等工作。 
1.2	NodeManager 
1.	NodeManager 是每个节点上的资源和任务管理器，它是管理这台机器的代理，负责该节点程序的运行，以及该节点资源的管理和监控。YARN集群每个节点都运行一个NodeManager。 
2.	NodeManager 定时向 ResourceManager 汇报本节点资源（CPU、内存）的使用情况和 Container 的运行状态。当 ResourceManager 宕机时 NodeManager 自动连接 RM 备用节点。 
3.	NodeManager 接收并处理来自 ApplicationMaster 的 Container 启动、停止等各种请求。 
1.3	ApplicationMaster 
用户提交的每个应用程序均包含一个ApplicationMaster，它可以运行在ResourceManager以外的机器上。 
1.	负责与 RM 调度器协商以获取资源（用 Container 表示）。 
2.	将得到的任务进一步分配给内部的任务(资源的二次分配)。 
3.	与 NM 通信以启动/停止任务。 
4.	监控所有任务运行状态，并在任务运行失败时重新为任务申请资源以重启任务。 
5.	当前 YARN 自带了两个 ApplicationMaster 实现，一个是用于演示 AM 编写方法的实例程序 DistributedShell，它可以申请一定数目的 Container 以并行运行一个 Shell 命令或者 Shell 脚本；另一个是运行 MapReduce 应用程序的 AM—MRAppMaster。 
注：RM 只负责监控 AM，并在 AM 运行失败时候启动它。RM 不负责 AM 内部任务的容错，任务的容错由 AM 完成。  
1.4	YARN 运行流程 

<div align=center>

![1589029440365.png](..\images\1589029440365.png)

</div>

1.	client 向 RM 提交应用程序，其中包括启动该应用的 ApplicationMaster 的必须信息，例如 ApplicationMaster 程序、启动 ApplicationMaster 的命令、用户程序等。 
2.	ResourceManager 启动一个 container 用于运行 ApplicationMaster。 
3.	启动中的ApplicationMaster向ResourceManager注册自己，启动成功后与RM保持心跳。 
4.	ApplicationMaster 向 ResourceManager 发送请求，申请相应数目的 container。 
5.	ResourceManager 返回 ApplicationMaster 的申请的 containers 信息。申请成功的 container，由 ApplicationMaster 进行初始化。container 的启动信息初始化后，AM 与对应的 NodeManager 通信，要求 NM 启动 container。AM 与 NM 保持心跳，从而对 NM 上运行的任务进行监控和管理。 
6.	container 运行期间，ApplicationMaster 对 container 进行监控。container 通过 RPC 协议向对应的 AM 汇报自己的进度和状态等信息。 
7.	应用运行期间，client 直接与 AM 通信获取应用的状态、进度更新等信息。 
8.	应用运行结束后，ApplicationMaster 向 ResourceManager 注销自己，并允许属于它的 container 被收回。 
