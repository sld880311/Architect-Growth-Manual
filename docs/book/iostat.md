# 导航

# 概述
iostat 主要用于输出磁盘IO 和 CPU的统计信息。
iostat属于sysstat软件包。可以用yum install sysstat 直接安装。
# 用法
```bash
Usage: iostat [ options ] [ <interval> [ <count> ] ]
Options are:
[ -c ] [ -d ] [ -h ] [ -k | -m ] [ -N ] [ -t ] [ -V ] [ -x ] [ -y ] [ -z ]
[ -j { ID | LABEL | PATH | UUID | ... } ]
[ [ -T ] -g <group_name> ] [ -p [ <device> [,...] | ALL ] ]
[ <device> [...] | ALL ]
```
# 参数说明

* -c： 显示CPU使用情况
* -d： 显示磁盘使用情况
* -N： 显示磁盘阵列(LVM) 信息
* -n： 显示NFS 使用情况
* -k： 以 KB 为单位显示
* -m： 以 M 为单位显示
* -t： 报告每秒向终端读取和写入的字符数和CPU的信息
* -V： 显示版本信息
* -x： 显示详细信息
* -p：[磁盘] 显示磁盘和分区的情况

# 场景说明
## 显示所有设备负载情况

```bash
Linux 3.10.0-1062.4.1.el7.x86_64 (instance-gctg007a) 	04/20/2020 	_x86_64_	(1 CPU)

avg-cpu:  %user   %nice %system %iowait  %steal   %idle
           0.60    0.05    0.46    0.04    0.01   98.85

Device:            tps    kB_read/s    kB_wrtn/s    kB_read    kB_wrtn
vda               1.33         0.07        11.68     657419  105232080

```
| 选项    | 说明                                                        |
|---------|-------------------------------------------------------------|
| 第一行  | 最上面指示系统版本、主机名和当前日期                        |
| avg-cpu | 总体cpu使用情况统计信息，对于多核cpu，这里为所有cpu的平均值 |
| Device  | 各磁盘设备的IO统计信息                                      |

### cpu属性值说明
| 选项    | 说明                                                              |
|---------|-------------------------------------------------------------------|
| %user   | CPU在用户态执行进程的时间百分比。                                 |
| %nice   | CPU在用户态模式下，用于nice操作，所占用CPU总时间的百分比          |
| %system | CPU处在内核态执行进程的时间百分比                                 |
| %iowait | CPU用于等待I/O操作占用CPU总时间的百分比                           |
| %steal  | 管理程序(hypervisor)为另一个虚拟进程提供服务而等待虚拟CPU的百分比 |
| %idle   | CPU空闲时间百分比                                                 |

```bash
1. 若 %iowait 的值过高，表示硬盘存在I/O瓶颈 
2. 若 %idle 的值高但系统响应慢时，有可能是CPU等待分配内存，此时应加大内存容量 
3. 若 %idle 的值持续低于1，则系统的CPU处理能力相对较低，表明系统中最需要解决的资源是 CPU
```

### disk属性值说明
| 选项       | 说明                                                                                                                                                                                                                                                   |
|------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Device     | 设备名称                                                                                                                                                                                                                                               |
| tps        | 每秒向磁盘设备请求数据的次数，包括读、写请求，为rtps与wtps的和。出于效率考虑，每一次IO下发后并不是立即处理请求，而是将请求合并(merge)，这里tps指请求合并后的请求计数。                                                                                 |
| Blk_read/s | Indicate the amount of data read from the device expressed in a number of blocks per second. Blocks are equivalent to sectors with kernels 2.4 and later and therefore have a size of 512 bytes. With older kernels, a block is of indeterminate size. |
| Blk_wrtn/s | Indicate the amount of data written to the device expressed in a number of blocks per second.                                                                                                                                                          |
| Blk_read   | 取样时间间隔内读扇区总数量                                                                                                                                                                                                                             |
| Blk_wrtn   | 取样时间间隔内写扇区总数量                                                                                                                                                                                                                             |
         
## iostat 1 5：间隔1秒，总共显示5次
## iostat -d 2：每隔2秒,显示一次设备统计信息.
## iostat -d 2 3：每隔2秒,显示一次设备统计信息.总共输出3次.
## iostat -x sda sdb 2 3：每隔2秒显示一次sda, sdb两个设备的扩展统计信息,共输出3次.
## iostat -p sda 2 3：每隔2秒显示一次sda及上面所有分区的统计信息,共输出3次.
## iostat -m：以M为单位显示所有信息
## iostat -d sda：显示指定硬盘信息
## iostat -t：报告每秒向终端读取和写入的字符数。
## iostat -d -k 1 1：查看TPS和吞吐量信息            
## iostat -d -x -k 1 1：查看设备使用率（%util）、响应时间（await）

```bash
Linux 3.10.0-1062.4.1.el7.x86_64 (instance-gctg007a) 	04/20/2020 	_x86_64_	(1 CPU)

Device:         rrqm/s   wrqm/s     r/s     w/s    rkB/s    wkB/s avgrq-sz avgqu-sz   await r_await w_await  svctm  %util
vda               0.00     1.14    0.00    1.32     0.07    11.68    17.72     0.00    0.87    5.07    0.86   0.47   0.06

```

| 选项     | 说明                                                                                                                 |
|----------|----------------------------------------------------------------------------------------------------------------------|
| rrqm/s   | 每秒对该设备的读请求被合并次数，文件系统会对读取同块(block)的请求进行合并                                            |
| wrqm/s   | 每秒对该设备的写请求被合并次数                                                                                       |
| r/s      | 每秒完成的读 I/O 设备次数                                                                                            |
| w/s      | 每秒完成的写 I/O 设备次数                                                                                            |
| rkB/s    | 每秒读K字节数。是 rsect/s 的一半，因为每扇区大小为512字节                                                            |
| wkB/s    | 每秒写K字节数。是 wsect/s 的一半                                                                                     |
| avgrq-sz | 平均每次IO操作的数据量(扇区数为单位)                                                                                 |
| avgqu-sz | 平均等待处理的IO请求队列长度                                                                                         |
| rsec/s   | 每秒读扇区数。即 rsect/s                                                                                             |
| wsec/s   | 每秒写扇区数。即 wsect/s                                                                                             |
| r_await  | 每个读操作平均所需的时间，不仅包括硬盘设备读操作的时间，还包括了在kernel队列中等待的时间                             |
| w_await  | 每个写操作平均所需的时间，不仅包括硬盘设备写操作的时间，还包括了在kernel队列中等待的时间                             |
| await    | 平均每次IO请求等待时间(包括等待时间和处理时间，毫秒为单位)                                                           |
| svctm    | 平均每次IO请求的处理时间(毫秒为单位)                                                                                 |
| %util    | 采用周期内用于IO操作的时间比率，即IO队列非空的时间比率；一秒中有百分之多少的时间用于 I/O 操作，即被io消耗的cpu百分比 |

>  <font color="red">**备注：如果 %util 接近 100%，说明产生的I/O请求太多，I/O系统已经满负荷，该磁盘可能存在瓶颈。如果 svctm 比较接近 await，说明 I/O 几乎没有等待时间；如果 await 远大于 svctm，说明I/O 队列太长，io响应太慢，则需要进行必要优化。如果avgqu-sz比较大，也表示有当量io在等待。**
</font>

### 重点关注参数

```bash
1、iowait% 表示CPU等待IO时间占整个CPU周期的百分比，如果iowait值超过50%，或者明显大于%system、%user以及%idle，表示IO可能存在问题。
2、avgqu-sz 表示磁盘IO队列长度，即IO等待个数。
3、await 表示每次IO请求等待时间，包括等待时间和处理时间
4、svctm 表示每次IO请求处理的时间
5、%util 表示磁盘忙碌情况，一般该值超过80%表示该磁盘可能处于繁忙状态。
```

## iostat -c 1 2：间隔1秒显示一次，总共显示2次（查看CPU的状态）