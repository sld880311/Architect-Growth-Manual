<!-- TOC -->

- [TIME_WAIT问题分析](#time_wait问题分析)
  - [TCP协议说明](#tcp协议说明)
    - [TCP建立连接的过程](#tcp建立连接的过程)
    - [TCP断开连接](#tcp断开连接)
    - [常用配置参数](#常用配置参数)
  - [状态说明](#状态说明)
    - [LISTENING状态](#listening状态)
    - [ESTABLISHED状态](#established状态)
    - [CLOSE_WAIT](#close_wait)
    - [TIME_WAIT](#time_wait)
    - [SYN_SENT状态](#syn_sent状态)
  - [状态转换](#状态转换)
  - [TIME_WAIT配置](#time_wait配置)
    - [windows 机器设置](#windows-机器设置)
    - [ubuntu机器设置](#ubuntu机器设置)
  - [查看系统TCP连接资源命令](#查看系统tcp连接资源命令)
  - [TCP协议中有TIME_WAIT这个状态的原因](#tcp协议中有time_wait这个状态的原因)
  - [TIME_WAIT造成的影响](#time_wait造成的影响)
  - [参考](#参考)

<!-- /TOC -->

# TIME_WAIT问题分析

## TCP协议说明

TCP协议规定，对于已经建立的连接，网络双方要进行四次握手才能成功断开连接，如果缺少了其中某个步骤，将会使连接处于假死状态，连接本身占用的资源不会被释放。网络服务器程序要同时管理大量连接，所以很有必要保证无用连接完全断开，否则大量僵死的连接会浪费许多服务器资源。在众多TCP状态中，最值得注意的状态有两个：**CLOSE_WAIT和TIME_WAIT。**

### TCP建立连接的过程

<div align=center>

![1602666879773.png](..\images\1602666879773.png)

</div>

### TCP断开连接

<div align=center>

![1602667375240.png](..\images\1602667375240.png)

</div>

### 常用配置参数

<div align=center>

![1602667762547.png](..\images\1602667762547.png)


![1606352049779.png](..\images\1606352049779.png)


</div>

## 状态说明

### LISTENING状态

FTP服务启动后首先处于侦听（LISTENING）状态。

### ESTABLISHED状态

ESTABLISHED的意思是建立连接。表示两台机器正在通信。

### CLOSE_WAIT

对方主动关闭连接或者网络异常导致连接中断，这时我方的状态会变成CLOSE_WAIT 此时我方要调用close()来使得连接正确关闭

### TIME_WAIT

我方主动调用close()断开连接，收到对方确认后状态变为TIME_WAIT。TCP协议规定TIME_WAIT状态会一直持续2MSL(Max Segment Lifetime，即两倍的分段最大生存期，Windows下默认为4分钟)，以此来确保旧的连接状态不会对新连接产生影响。**处于TIME_WAIT状态的连接占用的资源不会被内核释放**，所以作为服务器，在可能的情况下，尽量不要主动断开连接，以减少TIME_WAIT状态造成的资源浪费。  

目前有一种避免TIME_WAIT资源浪费的方法，就是关闭socket的LINGER选项。但这种做法是TCP协议不推荐使用的，在某些情况下这个操作可能会带来错误。  
**TIME_WAIT是TCP协议用以保证被重新分配的socket不会受到之前残留的延迟重发报文影响的机制,是必要的逻辑保证.**

### SYN_SENT状态

SYN_SENT状态表示请求连接，当你要访问其它的计算机的服务时首先要发个同步信号给该端口，此时状态为SYN_SENT，如果连接成功了就变为 ESTABLISHED，此时SYN_SENT状态非常短暂。但如果发现SYN_SENT非常多且在向不同的机器发出，那你的机器可能中了冲击波或震荡波 之类的病毒了。这类病毒为了感染别的计算机，它就要扫描别的计算机，在扫描的过程中对每个要扫描的计算机都要发出了同步请求，这也是出现许多 SYN_SENT的原因。

## 状态转换

<div align=center>

![TCP状态转换图](..\images\1588043788007.png)


![1594448316079.png](..\images\1594448316079.png)


</div>

## TIME_WAIT配置

### windows 机器设置

在HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\Tcpip\Parameters,右键添加名为TcpTimedWaitDelay的
DWORD键,设置为60,以缩短TIME_WAIT的等待时间

### ubuntu机器设置

`vi /etc/sysctl.conf`

```bash
# 1:表示开启SYN Cookies。当出现SYN等待队列溢出时，启用cookies来处理，可防范少量SYN攻击
# 默认为0，表示关闭；
net.ipv4.tcp_syncookies = 1
# 表示开启重用。允许将TIME-WAIT sockets重新用于新的TCP连接，默认为0，表示关闭；
net.ipv4.tcp_tw_reuse = 1
# 表示开启TCP连接中TIME-WAIT sockets的快速回收，默认为0，表示关闭。
net.ipv4.tcp_tw_recycle = 1
# 修改系統默认的 TIMEOUT 时间
net.ipv4.tcp_fin_timeout = 30

# 表示当keepalive起用的时候，TCP发送keepalive消息的频度。缺省是2小时，改为20分钟。
net.ipv4.tcp_keepalive_time = 1200
# 表示用于向外连接的端口范围。缺省情况下很小：32768到61000，改为1024到65000。
net.ipv4.ip_local_port_range = 1024 65000
# 表示SYN队列的长度，默认为1024，加大队列长度为8192，可以容纳更多等待连接的网络连接数。
net.ipv4.tcp_max_syn_backlog = 8192
# 表示系统同时保持TIME_WAIT套接字的最大数量，如果超过这个数字，TIME_WAIT套接字将立刻被清除并打印警告信息。默认为180000，改为5000。
net.ipv4.tcp_max_tw_buckets = 5000
```

## 查看系统TCP连接资源命令

`netstat -n | awk '/^tcp/ {++S[$NF]} END {for(a in S) print a, S[a]}'`

一般情况下，系统的socket资源默认5000个。（非官方）

## TCP协议中有TIME_WAIT这个状态的原因

1. 防止上一次连接中的包，迷路后重新出现，影响新连接（经过2MSL，上一次连接中所有的重复包都会消失）
2. 可靠的关闭TCP连接。在主动关闭方发送的最后一个 ack(fin) ，有可能丢失，这时被动方会重新发
fin, 如果这时主动方处于 CLOSED 状态 ，就会响应 rst 而不是 ack。所以主动方要处于 TIME_WAIT 状态，而不能是 CLOSED 。

## TIME_WAIT造成的影响

1. 场景：在高并发短连接的TCP服务器上，当服务器处理完请求后立刻主动正常关闭连接，会出现大量socket处于TIME_WAIT状态
   1. 高并发可以让服务器在短时间范围内同时占用大量端口（0~65535）
   2. 短连接表示“业务处理+传输数据的时间 远远小于 TIMEWAIT超时的时间”的连接。

## 参考

1. 命令参考《[netstat/ss](book/linux-netstat-ss.md)》
2. [解决TIME_WAIT过多造成的问题](https://www.cnblogs.com/dadonggg/p/8778318.html)
