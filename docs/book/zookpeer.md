<!-- TOC -->

- [Zookeeper详解](#zookeeper详解)
  - [简介](#简介)
    - [文件系统](#文件系统)
      - [znode](#znode)
        - [znode分类](#znode分类)
        - [znode客户端操作](#znode客户端操作)
        - [znode状态信息](#znode状态信息)
        - [版本号](#版本号)
        - [事务](#事务)
        - [ACL](#acl)
    - [监听通知机制（watcher）](#监听通知机制watcher)
      - [特性](#特性)
      - [监听流程](#监听流程)
    - [特点](#特点)
    - [Zookeeper角色](#zookeeper角色)
      - [Leader](#leader)
      - [Follower](#follower)
      - [Observer](#observer)
  - [功能](#功能)
    - [数据发布/订阅](#数据发布订阅)
    - [分布式锁](#分布式锁)
    - [负载均衡](#负载均衡)
      - [ZooKeeper负载均衡和Nginx负载均衡区别](#zookeeper负载均衡和nginx负载均衡区别)
    - [命名服务](#命名服务)
    - [分布式协调/通知](#分布式协调通知)
    - [集群管理](#集群管理)
  - [Leader选举](#leader选举)
    - [节点状态](#节点状态)
    - [服务器ID](#服务器id)
    - [ZXID](#zxid)
    - [启动时选举](#启动时选举)
    - [运行时选举](#运行时选举)
    - [脑裂](#脑裂)
  - [一致性协议-ZAB](#一致性协议-zab)
    - [zab简介](#zab简介)
    - [原子广播模式（同步）](#原子广播模式同步)
    - [恢复模式（选主）](#恢复模式选主)
    - [zab特性](#zab特性)
    - [ZAB 协议4阶段](#zab-协议4阶段)
      - [Leader election（选举阶段-选出准Leader）](#leader-election选举阶段-选出准leader)
      - [Discovery（发现阶段-接受提议、生成epoch、接受epoch）](#discovery发现阶段-接受提议生成epoch接受epoch)
      - [Synchronization（同步阶段-同步follower副本）](#synchronization同步阶段-同步follower副本)
      - [Broadcast（广播阶段-leader消息广播）](#broadcast广播阶段-leader消息广播)
    - [ZAB 和 Paxos 对比](#zab-和-paxos-对比)
      - [相同点](#相同点)
      - [不同点](#不同点)
  - [其他](#其他)
    - [使用注意事项](#使用注意事项)
    - [部署模式](#部署模式)
    - [常用命令](#常用命令)
  - [参考](#参考)

<!-- /TOC -->
# Zookeeper详解

## 简介

1. 开源分布式协调框架
2. 为分布式应用提供一致性服务，作为整个大数据体系的管理员，包括服务发现、分布式锁、分布式领导选举、配置管理等
3. Zookeeper = 文件系统（类似于Linux文件系统的树形结构） + 监听通知机制

### 文件系统

<div align=center>

![1610767149963.png](..\images\1610767149963.png)

</div>

1. 每个znode存放数据的上线为1M
2. 能够自由的增减、删除znode
3. znode中可以存放数据
4. 节点以绝对路径表示，不存在相对路径，且路径最后不能以 / 结尾（根节点除外）
5. ZooKeeper保证读和写都是原子操作，且每次读写操作都是对数据的完整读取或完整写入，并不提供对数据进行部分读取或者写入的操作。

#### znode

##### znode分类

1. **持久化目录节点PERSISTENT**：客户端与zookeeper断开连接后，该节点依旧存在。
2. **持久化顺序编号目录节点PERSISTENT_SEQUENTIAL**：客户端与zookeeper断开连接后，该节点依旧存在，只是Zookeeper给该节点名称进行顺序编号。
3. **临时目录节点 EPHEMERAL**：客户端与zookeeper断开连接后，该节点被删除。
4. **临时顺序编号目录节点 EPHEMERAL_SEQUENTIAL**：客户端与zookeeper断开连接后，该节点被删除，只是Zookeeper给该节点名称进行顺序编号。

##### znode客户端操作

<div align=center>

![1610767796309.png](..\images\1610767796309.png)

</div>

##### znode状态信息

<div align=center>

![1610767865744.png](..\images\1610767865744.png)

</div>

##### 版本号

1. **dataVersion** ：数据版本号，每次对节点进行set操作，dataVersion的值都会增加1（即使设置的是相同的数据）。
2. **cversion** ：子节点的版本号。当znode的子节点有变化时，cversion 的值就会增加1。
3. **aclVersion** ：ACL的版本号，ACL（Access Control List，访问控制）

<div align=center>

![1610768088118.png](..\images\1610768088118.png)

</div>

##### 事务

对于zk来说，每次的变化都会产生一个唯一的事务id，zxid（ZooKeeper Transaction Id）。通过zxid，可以确定更新操作的先后顺序。例如，如果zxid1小于zxid2，说明zxid1操作先于zxid2发生。 需要指出的是，zxid对于整个zk都是唯一的，即使操作的是不同的znode。
1. **cZxid** :Znode创建的事务id。
2. **mZxid** :Znode被修改的事务id，即每次对znode的修改都会更新mZxid。

<div align=center>

![1610768235767.png](..\images\1610768235767.png)

</div>

##### ACL

1. 身份认证
<div align=center>

![1610768336326.png](..\images\1610768336326.png)

 </div>
2. 权限操作
<div align=center>

![1610768364549.png](..\images\1610768364549.png)

</div>

### 监听通知机制（watcher）

1. 基于zookeeper上创建的节点，可以进行监听事件的绑定（节点数据变更、节点删除、子节点状态变更）
2. 基于事件机制，实现分布式锁和集群管理功能

#### 特性

> 当数据发生变化的时候， Zookeeper 会产生一个 Watcher 事件，并且会发送到客户端。但是客户端只会收到一次通知。如果后续这个节点再次发生变化，那么之前设置 Watcher 的客户端不会再次收到消息。（Watcher 是一次性的操作）。可以通过循环监听去达到永久监听效果。

#### 监听流程

1. 客户端注册 Watcher，注册 watcher 有 3 种方式，getData、exists、getChildren。
2. 服务器处理 Watcher 。
3. 客户端回调 Watcher 客户端。

<div align=center>

![1610768831271.png](..\images\1610768831271.png)

</div>

1. 创建main()线程
2. 在main线程中创建Zookeeper客户端，这时就会创建两个线程，一个负责网络连接通信（connet），一个负责监听（listener）。
3. 通过connect线程将注册的监听事件发送给Zookeeper。
4. 在Zookeeper的注册监听器列表中将注册的监听事件添加到列表中。
5. Zookeeper监听到有数据或路径变化，就会将这个消息发送给listener线程。
6. listener线程内部调用了process()方法。

### 特点

1. **集群**：Zookeeper是一个领导者（Leader），多个跟随者（Follower）组成的集群。
2. **高可用性**：集群中只要有半数以上节点存活，Zookeeper集群就能正常服务。
3. **全局数据一致**：每个Server保存一份相同的数据副本，Client无论连接到哪个Server，数据都是一致的。
4. **更新请求顺序进行**：来自同一个Client的更新请求按其发送顺序依次执行。
5. **数据更新原子性**：一次数据更新要么成功，要么失败。
6. **实时性**：在一定时间范围内，Client能读到最新数据。
7. 从设计模式角度来看，zk是一个基于观察者设计模式的框架，它负责管理跟存储大家都关心的数据，然后接受观察者的注册，数据反生变化zk会通知在zk上注册的观察者做出反应。
8. Zookeeper是一个分布式协调系统，满足CP性，跟SpringCloud中的Eureka满足AP不一样。

> 分布式协调系统：Leader会同步数据到follower，用户请求可通过follower得到数据，这样不会出现单点故障，并且只要同步时间无限短，那这就是个好的 分布式协调系统。CAP原则又称CAP定理，指的是在一个分布式系统中，一致性（Consistency）、可用性（Availability）、分区容错性（Partition tolerance）。CAP 原则指的是，这三个要素最多只能同时实现两点，不可能三者兼顾。

### Zookeeper角色

Zookeeper 集群是一个基于主从复制的高可用集群，每个服务器承担如下三种角色中的一种
<div align=center>

![1589095424742.png](..\images\1589095424742.png)

</div>

#### Leader

1. 一个Zookeeper集群同一时间只会有一个实际工作的Leader，它会发起并维护与各 Follwer及Observer间的心跳。 
2. 所有的写操作必须要通过 Leader 完成再由 Leader 将写操作广播给其它服务器。只要有超过半数节点（不包括 observeer 节点）写入成功，该写请求就会被提交（类 2PC 协议）。 

#### Follower  

1. 一个 Zookeeper 集群可能同时存在多个 Follower，它会响应 Leader 的心跳， 
2. Follower 可直接处理并返回客户端的读请求，同时会将写请求转发给 Leader 处理，并且负责在 Leader 处理写请求时对请求进行投票。 

#### Observer 

角色与 Follower 类似，但是无投票权。Zookeeper 需保证高可用和强一致性，为了支持更多的客户端，需要增加更多 Server；Server 增多，投票阶段延迟增大，影响性能；引入 Observer， Observer 不参与投票； Observers 接受客户端的连接，并将写请求转发给 leader 节点； 加入更多 Observer 节点，提高伸缩性，同时不影响吞吐率。 

## 功能

### 数据发布/订阅

当某些数据由几个机器共享，且这些信息经常变化数据量还小的时候，这些数据就适合存储到ZK中。

1. **数据存储**：将数据存储到 Zookeeper 上的一个数据节点。
2. **数据获取**：应用在启动初始化节点从 Zookeeper 数据节点读取数据，并在该节点上注册一个数据变更 Watcher
3. **数据变更**：当变更数据时会更新 Zookeeper 对应节点数据，Zookeeper会将数据变更通知发到各客户端，客户端接到通知后重新读取变更后的数据即可。

### 分布式锁

1. **保持独占**：在zk中有**一个唯一的临时节点**，只有拿到节点的才可以操作数据，没拿到的线程就需要等待。缺点：可能引发羊群效应，第一个用完后瞬间有999个同时并发的线程向zk请求获得锁。
2. **控制时序**：主要是避免了羊群效应，临时节点已经预先存在，所有想要获得锁的线程在它下面创建临时顺序编号目录节点，编号最小的获得锁，用完删除，后面的依次排队获取。

<div align=center>

![1610778219171.png](..\images\1610778219171.png)

</div>

### 负载均衡

<div align=center>

![1610778458637.png](..\images\1610778458637.png)

</div>

1. 多个服务注册
2. 客户端获取中间件地址集合
3. 从集合中随机选一个服务执行任务

#### ZooKeeper负载均衡和Nginx负载均衡区别

> ZooKeeper不存在单点问题，zab机制保证单点故障可重新选举一个leader只负责服务的注册与发现，不负责转发，减少一次数据交换（消费方与服务方直接通信），需要自己实现相应的负载均衡算法。
> Nginx存在单点问题，单点负载高数据量大,需要通过 KeepAlived + LVS 备机实现高可用。每次负载，都充当一次中间人转发角色，增加网络负载量（消费方与服务方间接通信），自带负载均衡算法


### 命名服务

<div align=center>

![1610778627481.png](..\images\1610778627481.png)

</div>

命名服务是指**通过指定的名字来获取资源或者服务的地址**，利用 zk 创建一个全局唯一的路径，这个路径就可以作为一个名字，指向集群中的集群，提供的服务的地址，或者一个远程的对象等等。

### 分布式协调/通知

1. 对于系统调度来说，用户更改zk某个节点的value， ZooKeeper会将这些变化发送给注册了这个节点的 watcher 的所有客户端，进行通知。
2. 对于执行情况汇报来说，每个工作进程都在目录下创建一个携带工作进度的临时节点，那么汇总的进程可以监控目录子节点的变化获得工作进度的实时的全局情况。

### 集群管理

<div align=center>

![1610778799129.png](..\images\1610778799129.png)

</div>

1. 动态上下线：
> 比如在zookeeper服务器端有一个znode叫 /Configuration，那么集群中每一个机器启动的时候都去这个节点下创建一个EPHEMERAL类型的节点，比如server1 创建 /Configuration/Server1，server2创建**/Configuration /Server1**，然后Server1和Server2都watch /Configuration 这个父节点，那么也就是这个父节点下数据或者子节点变化都会通知到该节点进行watch的客户端。

2. Leader选举：

> 利用ZooKeeper的强一致性，能够保证在分布式高并发情况下节点创建的全局唯一性，即：同时有多个客户端请求创建 /Master 节点，最终一定只有一个客户端请求能够创建成功。利用这个特性，就能很轻易的在分布式环境中进行集群选举了。
> 动态Master选举。这就要用到 EPHEMERAL_SEQUENTIAL类型节点的特性了，这样每个节点会自动被编号。允许所有请求都能够创建成功，但是得有个创建顺序，每次选取序列号最小的那个机器作为Master 。

## Leader选举

### 节点状态

1. **LOOKING：寻 找 Leader 状态**。当服务器处于该状态时会认为当前集群中没有 Leader，因此需要进入 Leader 选举状态。
2. **FOLLOWING：跟随者状态**。处理客户端的非事务请求，转发事务请求给 Leader 服务器，参与事务请求 Proposal(提议) 的投票，参与 Leader 选举投票。
3. **LEADING：领导者状态**。事务请求的唯一调度和处理者，保证集群事务处理的顺序性，集群内部个服务器的调度者(管理follower,数据同步)。
4. **OBSERVING：观察者状态**。3.0 版本以后引入的一个服务器角色，在不影响集群事务处理能力的基础上提升集群的非事务处理能力，处理客户端的非事务请求，转发事务请求给 Leader 服务器，不参与任何形式的投票。

### 服务器ID

**Server id**，一般在搭建ZK集群时会在myid文件中给每个节点搞个唯一编号，**编号越大在Leader选择算法中的权重越大**，比如初始化启动时就是根据服务器ID进行比较。

### ZXID

<div align=center>

![1610779475884.png](..\images\1610779475884.png)

</div>

采用**全局递增的事务 Id** 来标识，所有 proposal(提议)在被提出的时候加上了ZooKeeper Transaction Id ，zxid是**64位**的Long类型，这是保证事务的顺序一致性的关键。**zxid中高32位表示纪元epoch，低32位表示事务标识xid**。

1. 每个leader都会具有**不同的epoch值**，表示一个纪元/朝代，用来标识 leader 周期。每个新的选举开启时都会生成一个新的epoch，新的leader产生的话epoch会自增，会将该值更新到所有的zkServer的zxid和epoch，
2. xid是一个依次递增的事务编号。数值越大说明数据越新，所有 proposal（提议）在被提出的时候加上了zxid，然后会依据数据库的两阶段过程，**首先会向其他的 server 发出事务执行请求，如果超过半数的机器都能执行并且能够成功，那么就会开始执行**。

### 启动时选举

<div align=center>

![1610768994211.png](..\images\1610768994211.png)

</div>

1. 服务器1启动，发起一次选举。服务器1投自己一票。此时服务器1票数一票，不够半数以上（3票），选举无法完成，**服务器1状态保持为LOOKING**。
2. 服务器2启动，再发起一次选举。服务器1和2分别投自己一票，此时服务器1发现服务器2的id比自己大，更改选票投给服务器2。此时服务器1票数0票，服务器2票数2票，不够半数以上（3票），选举无法完成。服务器1，2状态保持LOOKING。
3. 服务器3启动，发起一次选举。与上面过程一样，服务器1和2先投自己一票，然后因为服务器3id最大，两者更改选票投给为服务器3。此次投票结果：服务器1为0票，服务器2为0票，服务器3为3票。此时服务器3的票数已经超过半数（3票），**服务器3当选Leader。服务器1，2更改状态为FOLLOWING，服务器3更改状态为LEADING；**
4. 服务器4启动，发起一次选举。此时服务器1、2、3已经不是LOOKING状态，不会更改选票信息，交换选票信息结果。服务器3为3票，服务器4为1票。此时服务器4服从多数，更改选票信息为服务器3，服务器4并更改状态为FOLLOWING。
5. 服务器5启动，发起一次选举同4一样投票给3，此时服务器3一共5票，服务器5为0票。服务器5并更改状态为FOLLOWING；
6. 最终Leader是服务器3，状态为LEADING。其余服务器是Follower，状态为FOLLOWING。

### 运行时选举

运行时候如果Master节点崩溃了会走恢复模式，新Leader选出前会暂停对外服务，大致可以分为四个阶段 **选举、发现、同步、广播**。

<div align=center>

![1610780044797.png](..\images\1610780044797.png)

</div>

1. 每个Server会发出一个投票，第一次都是投自己，其中投票信息 = (myid，ZXID)
2. 收集来自各个服务器的投票
3. 处理投票并重新投票，处理逻辑：优先比较ZXID，然后比较myid。
4. 统计投票，只要超过半数的机器接收到同样的投票信息，就可以确定leader，注意epoch的增加跟同步。
5. 改变服务器状态Looking变为Following或Leading。
6. 当 Follower 链接上 Leader 之后，Leader 服务器会根据自己服务器上最后被提交的 ZXID 和 Follower 上的 ZXID 进行比对，比对结果要么回滚，要么和 Leader 同步，保证集群中各个节点的事务一致。
7. 集群恢复到广播模式，开始接受客户端的写请求。

### 脑裂

有了过半机制，并且有且只能一个leader

## 一致性协议-ZAB

### zab简介

ZAB (Zookeeper Atomic Broadcast **原子广播协议**) 协议是为分布式协调服务ZooKeeper专门设计的一种支持崩溃恢复的一致性协议。基于该协议，ZooKeeper 实现了一种**主从模式的系统架构来保持集群中各个副本之间的数据一致性**。

1. 原子广播模式：把数据更新到所有的follower。
2. 崩溃恢复模式：Leader发生崩溃时，如何恢复。

### 原子广播模式（同步）

**保证事务的顺序一致性**。

<div align=center>

![1610781102376.png](..\images\1610781102376.png)

</div>

1. leader从客户端收到一个写请求后生成一个新的事务并为这个事务生成一个唯一的ZXID，
2. leader将带有 zxid 的消息作为一个提案(proposal)分发给所有 FIFO队列。
3. FIFO队列取出队头proposal给follower节点。
4. 当 follower 接收到 proposal，**先将 proposal 写到硬盘，写硬盘成功后再向 leader 回一个 ACK。**
5. FIFO队列把ACK返回给Leader。
6. 当leader收到超过一半以上的follower的ack消息，leader会进行commit请求，然后再给FIFO发送commit请求。
7. 当follower收到commit请求时，会判断该事务的ZXID是不是比历史队列中的任何事务的ZXID都小，如果是则提交，如果不是则等待比它更小的事务的commit(保证顺序性)

### 恢复模式（选主）

当 Leader 崩溃会进入崩溃恢复模式。

1. ZAB 协议确保执行那些已经在 Leader 提交的事务最终会被所有服务器提交。
2. ZAB 协议确保丢弃那些只在 Leader 提出/复制，但没有提交的事务。

### zab特性

1. **一致性保证：可靠提交(Reliable delivery)** ，如果一个事务 A 被一个server提交(committed)了，那么它最终一定会被所有的server提交
2. **全局有序(Total order)**：假设有A、B两个事务，有一台server先执行A再执行B，那么可以保证所有server上A始终都被在B之前执行
3. **因果有序(Causal order)**：如果发送者在事务A提交之后再发送B,那么B必将在A之后执行
4. **高可用性**：只要大多数（法定数量）节点启动，系统就行正常运行
5. **可恢复性**：当节点下线后重启，它必须保证能恢复到当前正在执行的事务

### ZAB 协议4阶段 

#### Leader election（选举阶段-选出准Leader） 

节点在一开始都处于选举阶段，只要有一个节点得到超半数节点的票数，它就可以当选准 leader。只有到达 广播阶段（broadcast） 准 leader 才会成为真正的 leader。这一阶段的目的是就是为了选出一个准 leader，然后进入下一个阶段。 

#### Discovery（发现阶段-接受提议、生成epoch、接受epoch） 

在这个阶段，followers 跟准 leader 进行通信，同步 followers 最近接收的事务提议。这个一阶段的主要目的是发现当前大多数节点接收的最新提议，并且准 leader 生成新的 epoch，让 followers 接受，更新它们的 accepted Epoch ，一个 follower 只会连接一个 leader，如果有一个节点 f 认为另一个 follower p 是 leader，f 在尝试连接 p 时会被拒绝，f 被拒绝之后，就会进入重新选举阶段。 

#### Synchronization（同步阶段-同步follower副本） 

主要是利用 leader 前一阶段获得的最新提议历史，同步集群中所有的副本。只有当 大多数节点都同步完成，准 leader 才会成为真正的 leader。
follower 只会接收 zxid 比自己的 lastZxid 大的提议。 

#### Broadcast（广播阶段-leader消息广播） 

到了这个阶段，Zookeeper 集群才能正式对外提供事务服务，并且 leader 可以进行消息广播。同时如果有新的节点加入，还需要对新节点进行同步。 

### ZAB 和 Paxos 对比

#### 相同点
> 两者都存在一个类似于 Leader 进程的角色，由其负责协调多个 Follower 进程的运行.Leader 进程都会等待超过半数的 Follower 做出正确的反馈后，才会将一个提案进行提交.ZAB 协议中，每个 Proposal 中都包含一个 epoch 值来代表当前的 Leader周期，Paxos 中名字为 Ballot

#### 不同点
> ZAB 用来构建高可用的**分布式数据主备系统**（Zookeeper），Paxos 是用来构建分**布式一致性状态机**系统。

 
## 其他

### 使用注意事项

1. 集群中机器的数量并不是越多越好，一个写操作需要半数以上的节点ack，所以集群节点数越多，整个集群可以抗挂点的节点数越多(越可靠)，但是吞吐量越差。集群的数量必须为奇数。
2. zk是基于内存进行读写操作的，有时候会进行消息广播，因此不建议在节点存取容量比较大的数据。
3. dataDir目录、dataLogDir两个目录会随着时间推移变得庞大，容易造成硬盘满了。建议自己编写或使用自带的脚本保留最新的n个文件。
4. 默认最大连接数 默认为60，配置maxClientCnxns参数，配置单个客户端机器创建的最大连接数。

### 部署模式

1. 单机部署：一台机器上运行。
2. 集群部署：多台机器运行。
3. 伪集群部署：一台机器启动多个 Zookeeper 实例运行。

### 常用命令

<div align=center>

![1610781922501.png](..\images\1610781922501.png)

</div>

## 参考

1. [Zookeeper笔记（一）初识Zookeeper](https://yq.aliyun.com/articles/38098?spm=5176.100239.blogcont38274.9.Bx5zlv) 
2. [Zookeeper原理架构](https://blog.csdn.net/xuxiuning/article/details/51218941) 
3. [zookeeper原理（转）](https://www.iteye.com/blog/cailin-2014486)  
4. [Zookeeper的功能以及工作原理](https://www.cnblogs.com/felixzh/p/5869212.html) 
5. [zookeeper在Dubbo中的作用](https://blog.csdn.net/yzllz001/article/details/68946323)  
6. [zookeeper和dubbo的关系](https://blog.csdn.net/daiqinge/article/details/51282874) 
7. [hadoop系列：zookeeper（3）——zookeeper核心原理（事件）](https://blog.csdn.net/yinwenjie/article/details/47685077) 
8. [万字详解 Zookeeper 的五个核心知识点](https://www.toutiao.com/i6917882262764012043)