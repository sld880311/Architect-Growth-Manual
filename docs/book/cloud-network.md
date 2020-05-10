1.1	网络
1.1.1	网络层架构 
7 层模型主要包括： 
1.	物理层：主要定义物理设备标准，如网线的接口类型、光纤的接口类型、各种传输介质的传输速率等。它的主要作用是传输比特流（就是由 1、0 转化为电流强弱来进行传输,到达目的地后在转化为
1、0，也就是我们常说的模数转换与数模转换）。这一层的数据叫做比特。    
2.	数据链路层：主要将从物理层接收的数据进行 MAC 地址（网卡的地址）的封装与解封装。常把这一层的数据叫做帧。在这一层工作的设备是交换机，数据通过交换机来传输。    
3.	网络层：主要将从下层接收到的数据进行 IP 地址（例 192.168.0.1)的封装与解封装。在这一层工作的设备是路由器，常把这一层的数据叫做数据包。 
4.	传输层：定义了一些传输数据的协议和端口号（WWW 端口 80 等），如：TCP（传输控制协议，传输效率低，可靠性强，用于传输可靠性要求高，数据量大的数据），UDP（用户数据报协议，与 TCP 特性恰恰相反，用于传输可靠性要求不高，数据量小的数据，如 QQ 聊天数据就是通过这种方式传输的）。 主要是将从下层接收的数据进行分段进行传输，到达目的地址后在进行重组。常常把这一层数据叫做段。    
5.	会话层：通过传输层（端口号：传输端口与接收端口）建立数据传输的通路。主要在你的系统之间
发起会话或或者接受会话请求（设备之间需要互相认识可以是 IP 也可以是 MAC 或者是主机名）    
6.	表示层：主要是进行对接收的数据进行解释、加密与解密、压缩与解压缩等（也就是把计算机能够识别的东西转换成人能够能识别的东西（如图片、声音等））
<div align=center>

![1589091609856.png](..\images\1589091609856.png)

</div>

1.1.1	TCP/IP 协议
1.1.1.1	分层
TCP/IP 协议不是 TCP 和 IP 这两个协议的合称，而是指因特网整个 TCP/IP 协议族。从协议分层模型方面来讲，TCP/IP 由四个层次组成：网络接口层、网络层、传输层、应用层。
<div align=center>

![1589091638727.png](..\images\1589091638727.png)

</div>

以太网（Ethernet）的数据帧在链路层 
IP包在网络层 
TCP或UDP包在传输层 
TCP或UDP中的数据（Data)在应用层 
它们的关系是 数据帧｛IP包｛TCP或UDP包｛Data｝｝｝

<div align=center>

![1589091663796.png](..\images\1589091663796.png)

</div>

1.1.1.1.1	网络访问层(Network Access Layer) 
网络访问层(Network Access Layer)在 TCP/IP 参考模型中并没有详细描述，只是指出主机必须使用某种协议与网络相连。 
1.1.1.1.2	网络层(Internet Layer) 
网络层(Internet Layer)是整个体系结构的关键部分，其功能是使主机可以把分组发往任何网络，并使分组独立地传向目标。这些分组可能经由不同的网络，到达的顺序和发送的顺序也可能不同。高层如果需要顺序收发，那么就必须自行处理对分组的排序。互联网层使用因特网协议(IP，Internet Protocol)。 
1.1.1.1.3	传输层(Tramsport Layer-TCP/UDP) 
传输层(Tramsport Layer)使源端和目的端机器上的对等实体可以进行会话。在这一层定义了两个端到端的协议：传输控制协议(TCP，Transmission Control Protocol)和用户数据报协议(UDP，User Datagram Protocol)。TCP 是面向连接的协议，它提供可靠的报文传输和对上层应用的连接服务。为此，除了基本的数据传输外，它还有可靠性保证、流量控制、多路复用、优先权和安全性控制等功能。UDP 是面向无连接的不可靠传输的协议，主要用于不需要 TCP 的排序和流量控制等功能的应用程序。 
1.1.1.1.4	应用层(Application Layer) 
应用层(Application Layer)包含所有的高层协议，包括：虚拟终端协议(TELNET，
TELecommunications NETwork)、文件传输协议(FTP，File Transfer Protocol)、电子邮件传输协议(SMTP，Simple Mail Transfer Protocol)、域名服务(DNS，Domain Name Service)、网上新闻传输协议(NNTP，Net News Transfer Protocol)和超文本传送协议
(HTTP，HyperText Transfer Protocol)等。 
1.1.1.2	数据包
1.1.1.2.1	说明
不同的协议层对数据包有不同的称谓，在传输层叫做段(segment)，在网络层叫做数据报(datagram)，在链路层叫做帧(frame)。数据封装成帧后发到传输介质上，到达目的主机后每层协议再剥掉相应的首部，最后将应用层数据交给应用程序处理。
在应用程序中我们用到的Data的长度最大是多少，直接取决于底层的限制。我们从下到上分析一下：
1.在链路层，由以太网的物理特性决定了数据帧的长度为(46＋18)－(1500＋18)，其中的18是数据帧的头和尾，也就是说数据帧的内容最大为1500(不包括帧头和帧尾)，即MTU(Maximum Transmission Unit)为1500；
2.在网络层，因为IP包的首部要占用20字节，所以这的MTU为1500－20＝1480；
3.在传输层，对于UDP包的首部要占用8字节，所以这的MTU为1480－8＝1472；
所以，在应用层，你的Data最大长度为1472。当我们的UDP包中的数据多于MTU(1472)时，发送方的IP层需要分片fragmentation进行传输，而在接收方IP层则需要进行数据报重组，由于UDP是不可靠的传输协议，如果分片丢失导致重组失败，将导致UDP数据包被丢弃。从上面的分析来看，在普通的局域网环境下，UDP的数据最大为1472字节最好(避免分片重组)。但在网络编程中，Internet中的路由器可能有设置成不同的值(小于默认值)，Internet上的标准MTU值为576，所以Internet的UDP编程时数据长度最好在576－20－8＝548字节以内。
1.1.1.2.2	TCP、UDP数据包最大值的确定
UDP和TCP协议利用端口号实现多项应用同时发送和接收数据。数据通过源端口发送出去，通过目标端口接收。有的网络应用只能使用预留或注册的静态端口；而另外一些网络应用则可以使用未被注册的动态端口。因为UDP和TCP报头使用两个字节存放端口号，所以端口号的有效范围是从0到65535。动态端口的范围是从1024到65535。
MTU最大传输单元，这个最大传输单元实际上和链路层协议有着密切的关系，EthernetII帧的结构DMAC+SMAC+Type+Data+CRC由于以太网传输电气方面的限制，每个以太网帧都有最小的大小64Bytes最大不能超过1518Bytes，对于小于或者大于这个限制的以太网帧我们都可以视之为错误的数据帧，一般的以太网转发设备会丢弃这些数据帧。
<div align=center>

![1589091703249.png](..\images\1589091703249.png)

</div>
由于以太网EthernetII最大的数据帧是1518Bytes这样，刨去以太网帧的帧头（DMAC目的MAC地址48bits=6Bytes+SMAC源MAC地址48bits=6Bytes+Type域2Bytes）14Bytes和帧尾CRC校验部分4Bytes那么剩下承载上层协议的地方也就是Data域最大就只能有1500Bytes这个值我们就把它称之为MTU。
UDP包的大小就应该是 1500 - IP头(20) - UDP头(8) = 1472(Bytes)
TCP包的大小就应该是 1500 - IP头(20) - TCP头(20) = 1460 (Bytes)
注*PPPoE所谓PPPoE就是在以太网上面跑“PPP”。随着宽带接入（这种宽带接入一般为Cable Modem或者xDSL或者以太网的接入），因为以太网缺乏认证计费机制而传统运营商是通过PPP协议来对拨号等接入服务进行认证计费的，所以引入PPPoE。PPPoE导致MTU变小了以太网的MTU是1500，再减去PPP的包头包尾的开销（8Bytes），就变成1492。不过目前大多数的路由设备的MTU都为1500。
如果我们定义的TCP和UDP包没有超过范围，那么我们的包在IP层就不用分包了，这样传输过程中就避免了在IP层组包发生的错误；如果超过范围，既IP数据报大于1500字节，发送方IP层就需要将数据包分成若干片，而接收方IP层就需要进行数据报的重组。更严重的是，如果使用UDP协议，当IP层组包发生错误，那么包就会被丢弃。接收方无法重组数据报，将导致丢弃整个IP数据报。UDP不保证可靠传输；但是TCP发生组包错误时，该包会被重传，保证可靠传输。
UDP数据报的长度是指包括报头和数据部分在内的总字节数，其中报头长度固定，数据部分可变。数据报的最大长度根据操作环境的不同而各异。从理论上说，包含报头在内的数据报的最大长度为65535字节(64K)。
我们在用Socket编程时，UDP协议要求包小于64K。TCP没有限定，TCP包头中就没有“包长度”字段，而完全依靠IP层去处理分帧。这就是为什么TCP常常被称作一种“流协议”的原因，开发者在使用TCP服务的时候，不必去关心数据包的大小，只需讲SOCKET看作一条数据流的入口，往里面放数据就是了，TCP协议本身会进行拥塞/流量控制。 
 不过鉴于Internet(非局域网)上的标准MTU值为576字节，所以建议在进行Internet的UDP编程时，最好将UDP的数据长度控制在548字节 (576-8-20)以内。
1.1.1.1.1	TCP、UDP数据包最小值的确定
在用UDP局域网通信时，经常发生“Hello World”来进行测试，但是“Hello World”并不满足最小有效数据(64-46)的要求，为什么小于18个字节，对方仍然可用收到呢？因为在链路层的MAC子层中会进行数据补齐，不足18个字节的用0补齐。但当服务器在公网，客户端在内网，发生小于18个字节的数据，就会出现接收端收不到数据的情况。
以太网EthernetII规定，以太网帧数据域部分最小为46字节，也就是以太网帧最小是6＋6＋2＋46＋4＝64。除去4个字节的FCS，因此，抓包时就是60字节。当数据字段的长度小于46字节时，MAC子层就会在数据字段的后面填充以满足数据帧长不小于64字节。由于填充数据是由MAC子层负责，也就是设备驱动程序。不同的抓包程序和设备驱动程序所处的优先层次可能不同，抓包程序的优先级可能比设备驱动程序更高，也就是说，我们的抓包程序可能在设备驱动程序还没有填充不到64字节的帧的时候，抓包程序已经捕获了数据。因此不同的抓包工具抓到的数据帧的大小可能不同。下列是本人分别用wireshark和sniffer抓包的结果，对于TCP 的ACK确认帧的大小一个是54字节，一个是60字节，wireshark抓取时没有填充数据段，sniffer抓取时有填充数据段。
1.1.1.1.2	实际应用
用UDP协议发送时，用sendto函数最大能发送数据的长度为：65535- IP头(20) - UDP头(8)＝65507字节。用sendto函数发送数据时，如果发送数据长度大于该值，则函数会返回错误。  
用TCP协议发送时，由于TCP是数据流协议，因此不存在包大小的限制（暂不考虑缓冲区的大小），这是指在用send函数时，数据长度参数不受限制。而实际上，所指定的这段数据并不一定会一次性发送出去，如果这段数据比较长，会被分段发送，如果比较短，可能会等待和下一次数据一起发送。
1.1.2	TCP 三次握手/四次挥手 
TCP 在传输之前会进行三次沟通，一般称为“三次握手”，传完数据断开的时候要进行四次沟通，一般称为“四次挥手”。 
1.1.2.1	数据包说明 
	源端口号（ 16 位）：它（连同源主机 IP 地址）标识源主机的一个应用进程。 
	目的端口号（ 16 位）：它（连同目的主机 IP 地址）标识目的主机的一个应用进程。这两个值加上 IP 报头中的源主机 IP 地址和目的主机 IP 地址唯一确定一个 TCP 连接。 
	顺序号 seq（ 32 位）：用来标识从 TCP 源端向 TCP 目的端发送的数据字节流，它表示在这个报文段中的第一个数据字节的顺序号。如果将字节流看作在两个应用程序间的单向流动，则 TCP 用顺序号对每个字节进行计数。序号是 32bit 的无符号数，序号到达 2 的 32 次方 － 1 后又从 0 开始。当建立一个新的连接时， SYN 标志变 1 ，顺序号字段包含由这个主机选择的该连接的初始顺序号 ISN （ Initial Sequence Number ）。 
	确认号 ack（ 32 位）：包含发送确认的一端所期望收到的下一个顺序号。因此，确认序号应当是上次已成功收到数据字节顺序号加 1 。只有 ACK 标志为 1 时确认序号字段才有效。 TCP 为应用层提供全双工服务，这意味数据能在两个方向上独立地进行传输。因此，连接的每一端必须保持每个方向上的传输数据顺序号。 
	TCP 报头长度（ 4 位）：给出报头中 32bit 字的数目，它实际上指明数据从哪里开始。需要这个值是因为任选字段的长度是可变的。这个字段占 4bit ，因此 TCP 最多有 60 字节的首部。然而，没有任选字段，正常的长度是 20 字节。 
	保留位（ 6 位）：保留给将来使用，目前必须置为 0 。 
	控制位（ control flags ， 6 位）：在 TCP 报头中有 6 个标志比特，它们中的多个可同时被设置为 1 。依次为： 
1.	URG ：为 1 表示紧急指针有效，为 0 则忽略紧急指针值。 
2.	ACK ：为 1 表示确认号有效，为 0 表示报文中不包含确认信息，忽略确认号字段。 
3.	PSH ：为 1 表示是带有 PUSH 标志的数据，指示接收方应该尽快将这个报文段交给应用层而不用等待缓冲区装满。 
4.	RST ：用于复位由于主机崩溃或其他原因而出现错误的连接。它还可以用于拒绝非法的报文段和拒绝连接请求。一般情况下，如果收到一个 RST 为 1 的报文，那么一定发生了某些问题。 
5.	SYN ：同步序号，为 1 表示连接请求，用于建立连接和使顺序号同步（ synchronize ）。 
6.	FIN ：用于释放连接，为 1 表示发送方已经没有数据发送了，即关闭本方数据流。 
	窗口大小（ 16 位）：数据字节数，表示从确认号开始，本报文的源方可以接收的字节数，即源方接收窗口大小。窗口大小是一个 16bit 字段，因而窗口大小最大为 65535 字节。 
	校验和（ 16 位）：此校验和是对整个的 TCP 报文段，包括 TCP 头部和 TCP 数据，以 16 位字进行计算所得。这是一个强制性的字段，一定是由发送端计算和存储，并由接收端进行验证。 
	紧急指针（ 16 位）：只有当 URG 标志置 1 时紧急指针才有效。TCP 的紧急方式是发送端向另一端发送紧急数据的一种方式。 
	选项：最常见的可选字段是最长报文大小，又称为 MSS(Maximum Segment Size) 。每个连接方通常都在通信的第一个报文段（为建立连接而设置 SYN 标志的那个段）中指明这个选项，它指明本端所能接收的最大长度的报文段。选项长度不一定是 32 位字的整数倍，所以要加填充位，使得报头长度成为整字数。 
	数据： TCP 报文段中的数据部分是可选的。在一个连接建立和一个连接终止时，双方交换的报文段仅有 TCP 首部。如果一方没有数据要发送，也使用没有任何数据的首部来确认收到的数据。在处理超时的许多情况中，也会发送不带任何数据的报文段。
<div align=center>

![1589091747463.png](..\images\1589091747463.png)

</div>

1.1.1.1	三次握手 
第一次握手：主机 A 发送位码为 syn＝1,随机产生 seq number=1234567 的数据包到服务器，主机 B 由 SYN=1 知道，A 要求建立联机； 
第二次握手：主机 B 收到请求后要确认联机信息，向 A 发送 ack number=(主机 A 的 seq+1),syn=1,ack=1,随机产生 seq=7654321 的包 
第三次握手：主机 A 收到后检查 ack number 是否正确，即第一次发送的 seq number+1,以及位码
ack 是否为 1，若正确，主机 A 会再发送 ack number=(主机 B 的 seq+1),ack=1，主机 B 收到后确认 seq 值与 ack=1 则连接建立成功。
<div align=center>

![1589091772188.png](..\images\1589091772188.png)

</div>

1.1.1.1	四次挥手 
TCP 建立连接要进行三次握手，而断开连接要进行四次。这是由于 TCP 的半关闭造成的。因为 TCP 连接是全双工的(即数据可在两个方向上同时传递)所以进行关闭时每个方向上都要单独进行关闭。这个单方向的关闭就叫半关闭。当一方完成它的数据发送任务，就发送一个 FIN 来向另一方通告将要终止这个方向的连接。 
1）	关闭客户端到服务器的连接：首先客户端 A 发送一个 FIN，用来关闭客户到服务器的数据传送，然后等待服务器的确认。其中终止标志位 FIN=1，序列号 seq=u 
2）	服务器收到这个 FIN，它发回一个 ACK，确认号 ack 为收到的序号加 1。 
3）	关闭服务器到客户端的连接：也是发送一个 FIN 给客户端。 
4）	客户段收到 FIN 后，并发回一个 ACK 报文确认，并将确认序号 seq 设置为收到序号加 1。 
     首先进行关闭的一方将执行主动关闭，而另一方执行被动关闭。 
<div align=center>

![1589091798311.png](..\images\1589091798311.png)

</div>
 
 主机 A 发送 FIN 后，进入终止等待状态， 服务器 B 收到主机 A 连接释放报文段后，就立即给主机 A 发送确认，然后服务器 B 就进入 close-wait 状态，此时 TCP 服务器进程就通知高层应用进程，因而从 A 到 B 的连接就释放了。此时是“半关闭”状态。即 A 不可以发送给
B，但是 B 可以发送给 A。此时，若 B 没有数据报要发送给 A 了，其应用进程就通知 TCP 释放连接，然后发送给 A 连接释放报文段，并等待确认。A 发送确认后，进入 time-wait，注意，此时 TCP 连接还没有释放掉，然后经过时间等待计时器设置的 2MSL 后，A 才进入到 close 状态。 
1.1.1	HTTP 原理 
HTTP是一个无状态的协议。无状态是指客户机（Web浏览器）和服务器之间不需要建立持久的连接，这意味着当一个客户端向服务器端发出请求，然后服务器返回响应(response)，连接就被关闭了，在服务器端不保留连接的有关信息.HTTP 遵循请求(Request)/应答(Response)模型。客户机（浏览器）向服务器发送请求，服务器处理请求并返回适当的应答。所有 HTTP 连接都被构造成一套请求和应答。 
1.1.1.1	传输流程 
1：地址解析 
 如用客户端浏览器请求这个页面：http://localhost.com:8080/index.htm 从中分解出协议名、主机名、端口、对象路径等部分，对于我们的这个地址，解析得到的结果如下：      协议名：http 
     主机名：localhost.com      端口：8080 
     对象路径：/index.htm 
      在这一步，需要域名系统 DNS 解析域名 localhost.com,得主机的 IP 地址。 
2：封装HTTP请求数据包 
把以上部分结合本机自己的信息，封装成一个 HTTP 请求数据包 
3：封装成TCP包并建立连接 
     封装成 TCP 包，建立 TCP 连接（TCP 的三次握手） 
4：客户机发送请求命 
     4）客户机发送请求命令：建立连接后，客户机发送一个请求给服务器，请求方式的格式为：统一资源标识符（URL）、协议版本号，后边是 MIME 信息包括请求修饰符、客户机信息和可内容。 
 5：服务器响应 
服务器接到请求后，给予相应的响应信息，其格式为一个状态行，包括信息的协议版本号、一个成功或错误的代码，后边是 MIME 信息包括服务器信息、实体信息和可能的内容。 
6：服务器关闭TCP连接 
  服务器关闭 TCP 连接：一般情况下，一旦 Web 服务器向浏览器发送了请求数据，它就要关闭 TCP 连接，然后如果浏览器或者服务器在其头信息加入了这行代码 Connection:keep-alive，TCP 连接在发送后将仍然保持打开状态，于是，浏览器可以继续通过相同的连接发送请求。保持连接节省了为每个请求建立新连接所需的时间，还节约了网络带宽。 
<div align=center>

![1589091838677.png](..\images\1589091838677.png)

</div>

HTTP 状态 

<style type="text/css">
.tg  {border-collapse:collapse;border-color:#bbb;border-spacing:0;}
.tg td{background-color:#E0FFEB;border-color:#bbb;border-style:solid;border-width:0px;color:#594F4F;
  font-family:Arial, sans-serif;font-size:14px;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg th{background-color:#9DE0AD;border-color:#bbb;border-style:solid;border-width:0px;color:#493F3F;
  font-family:Arial, sans-serif;font-size:14px;font-weight:normal;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg .tg-0lax{text-align:left;vertical-align:top}
.tg .tg-sjuo{background-color:#C2FFD6;text-align:left;vertical-align:top}
</style>
<table class="tg">
<thead>
  <tr>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;状态码&nbsp;&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;原因短语&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;消息响应&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;100&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;Continue(继续)&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;101&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;Switching Protocol(切换协议)&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;成功响应&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;200&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;OK(成功)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;201&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;Created(已创建)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;202&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;Accepted(已创建)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;203&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;Non-Authoritative Information(未授权信息)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;204&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;No Content(无内容)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;205&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;Reset Content(重置内容)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;206&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;Partial Content(部分内容)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;重定向&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;300&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;Multiple Choice(多种选择)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;301&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;Moved Permanently(永久移动)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;302&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;Found(临时移动)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;303&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;See Other(查看其他位置)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;304&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;Not Modified(未修改)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;305&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;Use Proxy(使用代理)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;306&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;unused(未使用)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;307&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;Temporary Redirect(临时重定向)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;308&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;Permanent Redirect(永久重定向)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;客户端错误&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;400&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;Bad Request(错误请求)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;401&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;Unauthorized(未授权)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;402&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;Payment Required(需要付款)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;403&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;Forbidden(禁止访问)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;404&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;Not Found(未找到)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;405&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;Method Not Allowed(不允许使用该方法)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;406&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;Not Acceptable(无法接受)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;407&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;Proxy Authentication Required(要求代理身份验证)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;408&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;Request Timeout(请求超时)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;409&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;Conflict(冲突)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;410&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;Gone(已失效)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;411&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;Length Required(需要内容长度头)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;412&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;Precondition Failed(预处理失败)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;413&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;Request Entity Too Large(请求实体过长)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;414&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;Request-URI Too Long(请求网址过长)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;415&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;Unsupported Media Type(媒体类型不支持)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;416&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;Requested Range Not Satisfiable(请求范围不合要求)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;417&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;Expectation Failed(预期结果失败)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;服务器端错误&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;500&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;Internal Server Error(内部服务器错误)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;501&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;Implemented(未实现)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;502&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;Bad Gateway(网关错误)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;503&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;Service Unavailable(服务不可用)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;504&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax" colspan="2">&nbsp;&nbsp;&nbsp;Gateway Timeout (网关超时)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;505&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo" colspan="2">&nbsp;&nbsp;&nbsp;HTTP Version Not Supported(HTTP 版本不受支持)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
</tbody>
</table>

 
1.1.1.1	HTTPS 
        HTTPS（全称：Hypertext Transfer Protocol over Secure Socket Layer），是以安全为目标的
HTTP 通道，简单讲是 HTTP 的安全版。即 HTTP 下加入 SSL 层，HTTPS 的安全基础是 SSL。其所用的端口号是 443。 过程大致如下： 
建立连接获取证书 
1）	SSL 客户端通过 TCP 和服务器建立连接之后（443 端口），并且在一般的 tcp 连接协商（握手）过程中请求证书。即客户端发出一个消息给服务器，这个消息里面包含了自己可实现的算法列表和其它一些需要的消息，SSL 的服务器端会回应一个数据包，这里面确定了这次通信所需要的算法，然后服务器向客户端返回证书。（证书里面包含了服务器信息：域名。申请证书的公司，公共秘钥）。                 
证书验证  
2）	Client 在收到服务器返回的证书后，判断签发这个证书的公共签发机构，并使用这个机构的公共秘钥确认签名是否有效，客户端还会确保证书中列出的域名就是它正在连接的域名。 
数据加密和传输 
3）	如果确认证书有效，那么生成对称秘钥并使用服务器的公共秘钥进行加密。然后发送给服务
器，服务器使用它的私钥对它进行解密，这样两台计算机可以开始进行对称加密进行通信。 
<div align=center>

![1589091956418.png](..\images\1589091956418.png)

</div>