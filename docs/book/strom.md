1	Storm 
1.1	概念 
Storm 是一个免费并开源的分布式实时计算系统。利用 Storm 可以很容易做到可靠地处理无限的数据流，像 Hadoop 批量处理大数据一样，Storm 可以实时处理数据。 
1.2	集群架构 
<div align=center>

![1589092153899.png](..\images\1589092153899.png)

</div>

18.3	Nimbus（master-代码分发给 Supervisor） 
Storm 集群的 Master 节点，负责分发用户代码，指派给具体的 Supervisor 节点上的 Worker 节点，去运行 Topology 对应的组件（Spout/Bolt）的 Task。 
18.3.1	Supervisor（slave-管理 Worker 进程的启动和终止） 
Storm 集群的从节点，负责管理运行在 Supervisor 节点上的每一个 Worker 进程的启动和终止。通过 Storm 的配置文件中的 supervisor.slots.ports 配置项，可以指定在一个 Supervisor 上最大允许多少个 Slot，每个 Slot 通过端口号来唯一标识，一个端口号对应一个 Worker 进程（如果该 Worker 进程被启动）。 
18.3.2	Worker（具体处理组件逻辑的进程） 
运行具体处理组件逻辑的进程。Worker 运行的任务类型只有两种，一种是 Spout 任务，一种是
Bolt 任务。 
18.3.3	Task 
worker中每一个spout/bolt的线程称为一个task. 在storm0.8之后，task不再与物理线程对应，不同 spout/bolt 的 task 可能会共享一个物理线程，该线程称为 executor。 
18.3.4	ZooKeeper 
用来协调 Nimbus 和 Supervisor，如果 Supervisor 因故障出现问题而无法运行 Topology，
Nimbus 会第一时间感知到，并重新分配 Topology 到其它可用的 Supervisor 上运行 
 
18.4	编程模型（spout->tuple->bolt） 
strom 在运行中可分为 spout 与 bolt 两个组件，其中，数据源从 spout 开始，数据以 tuple 的方式发送到 bolt，多个 bolt 可以串连起来，一个 bolt 也可以接入多个 spot/bolt.运行时原理如下图： 
<div align=center>

![1589092196333.png](..\images\1589092196333.png)

</div>
18.4.1	Topology 
Storm 中运行的一个实时应用程序的名称。将 Spout、 Bolt 整合起来的拓扑图。定义了 Spout 和 Bolt 的结合关系、并发数量、配置等等。 
18.4.2	Spout 
在一个 topology 中获取源数据流的组件。通常情况下 spout 会从外部数据源中读取数据，然后转换为 topology 内部的源数据。 
18.4.3	Bolt 
接受数据然后执行处理的组件,用户可以在其中执行自己想要的操作。 
18.4.4	Tuple 
一次消息传递的基本单元，理解为一组消息就是一个 Tuple。 
18.4.5	Stream 
Tuple 的集合。表示数据的流向。 
18.5	Topology 运行 
在 Storm 中,一个实时应用的计算任务被打包作为 Topology 发布，这同 Hadoop MapReduce 任务相似。但是有一点不同的是:在 Hadoop 中，MapReduce 任务最终会执行完成后结束；而在 Storm 中，Topology 任务一旦提交后永远不会结束，除非你显示去停止任务。计算任务
Topology 是由不同的 Spouts 和 Bolts，通过数据流（Stream）连接起来的图｡一个 Storm 在集群上运行一个 Topology 时，主要通过以下 3 个实体来完成 Topology 的执行工作： 
 
(1). Worker（进程） 
(2). Executor（线程） 
(3). Task 
<div align=center>

![1589092241018.png](..\images\1589092241018.png)

</div>

18.5.1	Worker(1 个 worker 进程执行的是 1 个 topology 的子集) 
1 个 worker 进程执行的是 1 个 topology 的子集（注：不会出现 1 个 worker 为多个 topology 服务）。1 个 worker 进程会启动 1 个或多个 executor 线程来执行 1 个 topology 的 component(spout 或 bolt)。因此，1 个运行中的 topology 就是由集群中多台物理机上的多个 worker 进程组成的。 
18.5.2	Executor(executor 是 1 个被 worker 进程启动的单独线程) 
executor 是 1 个被 worker 进程启动的单独线程。每个 executor 只会运行 1 个 topology 的 1 个 component(spout 或 bolt)的 task（注：task 可以是 1 个或多个，storm 默认是 1 个 component 只生成 1 个 task，executor 线程里会在每次循环里顺序调用所有 task 实例）。 
 
18.5.3	Task(最终运行 spout 或 bolt 中代码的单元) 
是最终运行 spout 或 bolt 中代码的单元（注：1 个 task 即为 spout 或 bolt 的 1 个实例， executor 线程在执行期间会调用该 task 的 nextTuple 或 execute 方法）。topology 启动后，1 个 component(spout 或 bolt)的 task 数目是固定不变的，但该 component 使用的 executor 线程数可以动态调整（例如：1 个 executor 线程可以执行该 component 的 1 个或多个 task 实例）。这意味着，对于 1 个 component 存在这样的条件：#threads<=#tasks（即：线程数小于等于 task 数目）。默认情况下 task 的数目等于 executor 线程数目，即 1 个 executor 线程只运行 1 个 task。 
<div align=center>

![1589092278740.png](..\images\1589092278740.png)

</div>

18.5.4	Storm Streaming Grouping 
Storm 中最重要的抽象，应该就是 Stream grouping 了，它能够控制 Spot/Bolt 对应的 Task 以什么样的方式来分发 Tuple，将 Tuple 发射到目的 Spot/Bolt 对应的 Task. 
<div align=center>

![1589092307472.png](..\images\1589092307472.png)

</div>
目前，Storm Streaming Grouping 支持如下几种类型： 
18.5.4.1	huffle Grouping  
随机分组，尽量均匀分布到下游 Bolt 中将流分组定义为混排。这种混排分组意味着来自 Spout 的输入将混排，或随机分发给此 Bolt 中的任务。shuffle grouping 对各个 task 的 tuple 分配的比较均匀。 
18.5.4.2	Fields Grouping  
按字段分组，按数据中 field 值进行分组；相同 field 值的 Tuple 被发送到相同的 Task 这种 grouping 机制保证相同 field 值的 tuple 会去同一个 task。 
18.5.4.3	All grouping ：广播 
广播发送， 对于每一个 tuple 将会复制到每一个 bolt 中处理。 
18.5.4.4	Global grouping  
全局分组，Tuple 被分配到一个 Bolt 中的一个 Task，实现事务性的 Topology。Stream 中的所有的 tuple 都会发送给同一个 bolt 任务处理，所有的 tuple 将会发送给拥有最小 task_id 的 bolt 任务处理。 
18.5.4.5	None grouping ：不分组 
不关注并行处理负载均衡策略时使用该方式，目前等同于 shuffle grouping,另外 storm 将会把 bolt 任务和他的上游提供数据的任务安排在同一个线程下。 
18.5.4.6	Direct grouping ：直接分组 指定分组 
由 tuple 的发射单元直接决定 tuple 将发射给那个 bolt，一般情况下是由接收 tuple 的 bolt 决定接收哪个 bolt 发射的 Tuple。这是一种比较特别的分组方法，用这种分组意味着消息的发送者指定由消息接收者的哪个 task 处理这个消息。 只有被声明为 Direct Stream 的消息流可以声明这种分组方法。而且这种消息 tuple 必须使用 emitDirect 方法来发射。消息处理者可以通过
TopologyContext 来获取处理它的消息的 taskid (OutputCollector.emit 方法也会返回 taskid)。 
 

