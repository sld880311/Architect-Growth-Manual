1.1	Netty 原理：NIO，异步非阻塞
Netty 是一个高性能、异步事件驱动的 NIO 框架，基于 JAVA NIO 提供的 API 实现。它提供了对TCP、UDP 和文件传输的支持，作为一个异步 NIO 框架，Netty 的所有 IO 操作都是异步非阻塞的，通过 Future-Listener 机制，用户可以方便的主动获取或者通过通知机制获得 IO 操作结果。 
1.2	Netty 高性能 
在IO编程过程中，当需要同时处理多个客户端接入请求时，可以利用多线程或者IO多路复用技术进行处理。IO多路复用技术通过把多个IO的阻塞复用到同一个select的阻塞上，从而使得系统在单线程的情况下可以同时处理多个客户端请求。与传统的多线程/多进程模型比，I/O 多路复用的最大优势是系统开销小，系统不需要创建新的额外进程或者线程，也不需要维护这些进程和线程的运行，降低了系统的维护工作量，节省了系统资源。 
与Socket类和ServerSocket类相对应，NIO也提供了SocketChannel和ServerSocketChannel 两种不同的套接字通道实现。 
1.2.1	多路复用通讯方式 
Netty 架构按照 Reactor 模式设计和实现，它的服务端通信序列图如下： 
<div align=center>

![1589097340388.png](..\images\1589097340388.png)

</div>
客户端通信序列图如下：
<div align=center>

![1589097364309.png](..\images\1589097364309.png)

</div>
Netty 的 IO 线程 NioEventLoop 由于聚合了多路复用器 Selector，可以同时并发处理成百上千个客户端 Channel，由于读写操作都是非阻塞的，这就可以充分提升 IO 线程的运行效率，避免由于频繁 IO 阻塞导致的线程挂起。 
1.1.1	异步通讯NIO 
由于 Netty 采用了异步通信模式，一个 IO 线程可以并发处理 N 个客户端连接和读写操作，这从根本上解决了传统同步阻塞 IO 一连接一线程模型，架构的性能、弹性伸缩能力和可靠性都得到了极大的提升。 
1.1.2	零拷贝（DIRECT BUFFERS 使用堆外直接内存） 
1.	Netty 的接收和发送 ByteBuffer 采用 DIRECT BUFFERS，使用堆外直接内存进行 Socket 读写，不需要进行字节缓冲区的二次拷贝。如果使用传统的堆内存（HEAP BUFFERS）进行 Socket 读写，
JVM 会将堆内存 Buffer 拷贝一份到直接内存中，然后才写入 Socket 中。相比于堆外直接内存，消息在发送过程中多了一次缓冲区的内存拷贝。 
2.	Netty 提供了组合 Buffer 对象，可以聚合多个 ByteBuffer 对象，用户可以像操作一个 Buffer 那样方便的对组合 Buffer 进行操作，避免了传统通过内存拷贝的方式将几个小 Buffer 合并成一个大的
Buffer。 
3.	Netty的文件传输采用了transferTo方法，它可以直接将文件缓冲区的数据发送到目标Channel，避免了传统通过循环 write 方式导致的内存拷贝问题 
1.1.3	内存池（基于内存池的缓冲区重用机制）
随着 JVM 虚拟机和 JIT 即时编译技术的发展，对象的分配和回收是个非常轻量级的工作。但是对于缓冲区 Buffer，情况却稍有不同，特别是对于堆外直接内存的分配和回收，是一件耗时的操作。为了尽量重用缓冲区，Netty 提供了基于内存池的缓冲区重用机制。 
1.1.4	高效的 Reactor 线程模型 
常用的 Reactor 线程模型有三种，Reactor 单线程模型, Reactor 多线程模型, 主从 Reactor 多线程模型。 
1.1.4.1	Reactor单线程模型 
Reactor 单线程模型，指的是所有的 IO 操作都在同一个 NIO 线程上面完成，NIO 线程的职责如下： 
1.	作为 NIO 服务端，接收客户端的 TCP 连接； 
2.	作为 NIO 客户端，向服务端发起 TCP 连接； 
3.	读取通信对端的请求或者应答消息； 
4.	向通信对端发送消息请求或者应答消息。 
<div align=center>

![1589097392341.png](..\images\1589097392341.png)

</div>
由于 Reactor 模式使用的是异步非阻塞 IO，所有的 IO 操作都不会导致阻塞，理论上一个线程可以独立处理所有 IO 相关的操作。从架构层面看，一个 NIO 线程确实可以完成其承担的职责。例如，通过Acceptor 接收客户端的 TCP 连接请求消息，链路建立成功之后，通过 Dispatch 将对应的 ByteBuffer 派发到指定的 Handler 上进行消息解码。用户 Handler 可以通过 NIO 线程将消息发送给客户端。 
1.1.1.1	Reactor多线程模型 
Rector 多线程模型与单线程模型最大的区别就是有一组 NIO 线程处理 IO 操作。有专门一个
NIO 线程-Acceptor 线程用于监听服务端，接收客户端的 TCP 连接请求； 网络 IO 操作-读、写等由一个 NIO 线程池负责，线程池可以采用标准的 JDK 线程池实现，它包含一个任务队列和 N 个可用的线程，由这些 NIO 线程负责消息的读取、解码、编码和发送； 

<div align=center>

![1589097415505.png](..\images\1589097415505.png)

</div>


1.1.1.1	主从Reactor多线程模型
服务端用于接收客户端连接的不再是个 1 个单独的 NIO 线程，而是一个独立的 NIO 线程池。
Acceptor 接收到客户端 TCP 连接请求处理完成后（可能包含接入认证等），将新创建的
SocketChannel 注册到 IO 线程池（sub reactor 线程池）的某个 IO 线程上，由它负责
SocketChannel 的读写和编解码工作。Acceptor 线程池仅仅只用于客户端的登陆、握手和安全认证，一旦链路建立成功，就将链路注册到后端 subReactor 线程池的 IO 线程上，由 IO 线程负责后续的 IO 操作。 

<div align=center>

![1589097440199.png](..\images\1589097440199.png)

</div>

1.1.1	无锁设计、线程绑定 
Netty 采用了串行无锁化设计，在 IO 线程内部进行串行操作，避免多线程竞争导致的性能下降。表面上看，串行化设计似乎 CPU 利用率不高，并发程度不够。但是，通过调整 NIO 线程池的线程参数，可以同时启动多个串行化的线程并行运行，这种局部无锁化的串行线程设计相比一个队列多个工作线程模型性能更优。 

<div align=center>

![1589097463316.png](..\images\1589097463316.png)

</div>

 
Netty 的 NioEventLoop 读取到消息之后，直接调用 ChannelPipeline 的
fireChannelRead(Object msg)，只要用户不主动切换线程，一直会由 NioEventLoop 调用到用户的 Handler，期间不进行线程切换，这种串行化处理方式避免了多线程操作导致的锁的竞争，从性能角度看是最优的。 
1.1.1	高性能的序列化框架 
Netty 默认提供了对 Google Protobuf 的支持，通过扩展 Netty 的编解码接口，用户可以实现其它的高性能序列化框架，例如 Thrift 的压缩二进制编解码框架。 
1.	SO_RCVBUF 和 SO_SNDBUF：通常建议值为 128K 或者 256K。 
小包封大包，防止网络阻塞 
2.	SO_TCPNODELAY：NAGLE 算法通过将缓冲区内的小封包自动相连，组成较大的封包，阻止大量小封包的发送阻塞网络，从而提高网络应用效率。但是对于时延敏感的应用场景需要关闭该优化算法。 
软中断Hash 值和CPU绑定 
3.	软中断：开启 RPS 后可以实现软中断，提升网络吞吐量。RPS 根据数据包的源地址，目的地址以及目的和源端口，计算出一个 hash 值，然后根据这个 hash 值来选择软中断运行的 cpu，从上层来看，也就是说将每个连接和 cpu 绑定，并通过这个 hash 值，来均衡软中断在多个 cpu 上，提升网络并行处理性能。 
1.2	Netty RPC 实现 
1.2.1	概念 
RPC，即 Remote Procedure Call（远程过程调用），调用远程计算机上的服务，就像调用本地服务一样。RPC 可以很好的解耦系统，如 WebService 就是一种基于 Http 协议的 RPC。这个 RPC 整体框架如下： 

<div align=center>

![1589097494747.png](..\images\1589097494747.png)

</div>

1.1.1	关键技术 
 
1.	服务发布与订阅：服务端使用 Zookeeper 注册服务地址，客户端从 Zookeeper 获取可用的服务地址。 
2.	通信：使用 Netty 作为通信框架。 
3.	Spring：使用 Spring 配置服务，加载 Bean，扫描注解。 
4.	动态代理：客户端使用代理模式透明化服务调用。 
5.	消息编解码：使用 Protostuff 序列化和反序列化消息。 
1.1.2	核心流程 
1.	服务消费方（client）调用以本地调用方式调用服务； 
2.	client stub 接收到调用后负责将方法、参数等组装成能够进行网络传输的消息体； 
3.	client stub 找到服务地址，并将消息发送到服务端； 
4.	server stub 收到消息后进行解码； 
5.	server stub 根据解码结果调用本地的服务； 
6.	本地服务执行并将结果返回给 server stub； 
7.	server stub 将返回结果打包成消息并发送至消费方； 
8.	client stub 接收到消息，并进行解码； 
9.	服务消费方得到最终结果。 
 RPC 的目标就是要 2~8 这些步骤都封装起来，让用户对这些细节透明。JAVA 一般使用动态代理方式实现远程调用。 

<div align=center>

![1589097525663.png](..\images\1589097525663.png)

</div>

 
1.1.1	消息编解码 
息数据结构（接口名称+方法名+参数类型和参数值+超时时间+ requestID）   客户端的请求消息结构一般需要包括以下内容： 
1.	接口名称：在我们的例子里接口名是“HelloWorldService”，如果不传，服务端就不知道调用哪个接口了； 
2.	方法名：一个接口内可能有很多方法，如果不传方法名服务端也就不知道调用哪个方法； 
3.	参数类型和参数值：参数类型有很多，比如有 bool、int、long、double、string、map、list，甚至如 struct（class）；以及相应的参数值； 
4.	超时时间： 
5.	requestID，标识唯一请求 id，在下面一节会详细描述 requestID 的用处。 
6.	服务端返回的消息 ： 一般包括以下内容。返回值+状态 code+requestID 
序列化 
目前互联网公司广泛使用 Protobuf、Thrift、Avro 等成熟的序列化解决方案来搭建 RPC 框架，这些都是久经考验的解决方案。 
1.1.2	通讯过程 
核心问题(线程暂停、消息乱序) 
  如果使用 netty 的话，一般会用 channel.writeAndFlush()方法来发送消息二进制串，这个方法调用后对于整个远程调用(从发出请求到接收到结果)来说是一个异步的，即对于当前线程来说，将请求发送出来后，线程就可以往后执行了，至于服务端的结果，是服务端处理完成后，再以消息的形式发送给客户端的。于是这里出现以下两个问题： 
1.	怎么让当前线程“暂停”，等结果回来后，再向后执行？ 
2.	如果有多个线程同时进行远程方法调用，这时建立在 client server 之间的 socket 连接上会有很多双方发送的消息传递，前后顺序也可能是随机的，server 处理完结果后，将结果消息发送给 client，client 收到很多消息，怎么知道哪个消息结果是原先哪个线程调用
的？如下图所示，线程 A 和线程 B 同时向 client socket 发送请求 requestA 和 requestB，
socket 先后将 requestB 和 requestA 发送至 server，而 server 可能将 responseB 先返回，尽管 requestB 请求到达时间更晚。我们需要一种机制保证 responseA 丢给
ThreadA，responseB 丢给 ThreadB。 

<div align=center>

![1589097554075.png](..\images\1589097554075.png)

</div>


 通讯流程 
requestID 生成-AtomicLong 
1.	client 线程每次通过 socket 调用一次远程接口前，生成一个唯一的 ID，即 requestID
（requestID 必需保证在一个 Socket 连接里面是唯一的），一般常常使用 AtomicLong 从 0 开始累计数字生成唯一 ID； 
存放回调对象callback到全局ConcurrentHashMap 
2.	将 处 理 结 果 的 回 调 对 象 callback ， 存 放 到 全 局 ConcurrentHashMap 里 面 put(requestID, callback)； 
synchronized 获取回调对象callback的锁并自旋wait 
3.	当线程调用 channel.writeAndFlush()发送消息后，紧接着执行 callback 的 get()方法试图获取远程返回的结果。在 get()内部，则使用 synchronized 获取回调对象 callback 的锁，再先检测是否已经获取到结果，如果没有，然后调用 callback 的 wait()方法，释放 callback 上的锁，让当前线程处于等待状态。 
监听消息的线程收到消息，找到callback上的锁并唤醒 
4.	服务端接收到请求并处理后，将response结果（此结果中包含了前面的requestID）发送给客户端，客户端 socket 连接上专门监听消息的线程收到消息，分析结果，取到 requestID，再从前面的 ConcurrentHashMap 里面 get(requestID)，从而找到 callback 对象，再用 synchronized 获取 callback 上的锁，将方法调用结果设置到 callback 对象里，再调用 callback.notifyAll()唤醒前面处于等待状态的线程。 
 
  public Object get() {         synchronized (this) { // 旋锁             while (true) { // 是否有结果了 
If （!isDone）{ 
                           wait(); //没结果释放锁，让当前线程处于等待状态 
}else{//获取数据并处理 
} 
                     } 
        } 
    } 
 	private void setDone(Response res) { 
        this.res = res;         isDone = true; 
        synchronized (this) { //获取锁，因为前面 wait()已经释放了 callback 的锁了             notifyAll(); // 唤醒处于等待的线程 
        } 
    } 

1.1	RMI 实现方式 
  Java 远程方法调用，即 Java RMI（Java Remote Method Invocation）是 Java 编程语言里，一种用于实现远程过程调用的应用程序编程接口。它使客户机上运行的程序可以调用远程服务器上的对象。远程方法调用特性使 Java 编程人员能够在网络环境中分布操作。RMI 全部的宗旨就是尽可能简化远程接口对象的使用。 
   
1.1.1	实现步骤 
1.	编写远程服务接口，该接口必须继承 java.rmi.Remote 接口，方法必须抛出 java.rmi.RemoteException 异常； 
2.	编写远程接口实现类，该实现类必须继承 java.rmi.server.UnicastRemoteObject 类； 
3.	运行 RMI 编译器（rmic），创建客户端 stub 类和服务端 skeleton 类; 
4.	启动一个 RMI 注册表，以便驻留这些服务; 
5.	在 RMI 注册表中注册服务； 
6.	客户端查找远程对象，并调用远程方法； 
 1：创建远程接口，继承 java.rmi.Remote 接口 
 public interface GreetService extends java.rmi.Remote {     String sayHello(String name) throws RemoteException; 
} 
2：实现远程接口，继承 java.rmi.server.UnicastRemoteObject 类  
public class GreetServiceImpl extends java.rmi.server.UnicastRemoteObject implements GreetService {     private static final long serialVersionUID = 3434060152387200042L;     public GreetServiceImpl() throws RemoteException {         super(); 
    } 
    @Override     public String sayHello(String name) throws RemoteException {         return "Hello " + name; 
    } 
} 
  3：生成 Stub 和 Skeleton; 
  4：执行 rmiregistry 命令注册服务  
5：启动服务 
 LocateRegistry.createRegistry(1098); 
Naming.bind("rmi://10.108.1.138:1098/GreetService", new GreetServiceImpl()); 
6.客户端调用 
 GreetService 	greetService 	= 	(GreetService) 
Naming.lookup("rmi://10.108.1.138:1098/GreetService"); 
System.out.println(greetService.sayHello("Jobs")); 
 

1.1	Protoclol Buffer 
protocol buffer 是 google 的一个开源项目,它是用于结构化数据串行化的灵活、高效、自动的方法，例如 XML，不过它比 xml 更小、更快、也更简单。你可以定义自己的数据结构，然后使用代码生成器生成的代码来读写这个数据结构。你甚至可以在无需重新部署程序的情况下更新数据结构。 
1.1.1	特点 
<div align=center>

![1589097617941.png](..\images\1589097617941.png)

</div>

 
Protocol Buffer 的序列化 & 反序列化简单 & 速度快的原因是：  
1.	编码 / 解码 方式简单（只需要简单的数学运算 = 位移等等）  
2.	采用 Protocol Buffer 自身的框架代码 和 编译器 共同完成 
Protocol Buffer 的数据压缩效果好（即序列化后的数据量体积小）的原因是：  
1.	a. 采用了独特的编码方式，如 Varint、Zigzag 编码方式等等  
2.	b. 采用 T - L - V 的数据存储方式：减少了分隔符的使用 & 数据存储得紧凑 

