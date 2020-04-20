# 导航

<!-- TOC -->

- [导航](#导航)
- [语法](#语法)
- [常用语法](#常用语法)
- [显示列](#显示列)
- [作用](#作用)
- [VIRT RES SHR的准确含义](#virt-res-shr的准确含义)
- [参考](#参考)

<!-- /TOC -->

# 语法

```bash
[root@incloudos logs]# top -h
  procps-ng version 3.3.10
Usage:
  top -hv | -bcHiOSs -d secs -n max -u|U user -p pid(s) -o field -w [cols]
s – 改变画面更新频率
l – 关闭或开启第一部分第一行 top 信息的表示
t – 关闭或开启第一部分第二行 Tasks 和第三行 Cpus 信息的表示
m – 关闭或开启第一部分第四行 Mem 和 第五行 Swap 信息的表示
N – 以 PID 的大小的顺序排列表示进程列表
P – 以 CPU 占用率大小的顺序排列进程列表
M – 以内存占用率大小的顺序排列进程列表
h – 显示帮助
n – 设置在进程列表所显示进程的数量
q – 退出 top
s – 改变画面更新周期
请在top里面按下E，注意是大写。你会发现内存那一行的最左侧，
也就是Mem前面，会有Kib、MiB、GiB等单位变化，
但是数字后面不会直接写明单位。
```
# 常用语法
* top查看进程使用资源情况
* top -c显示详细的进程信息
* top -bn1静态显示所有进程
* q退出，数值1显示所有核cpu，大写字母M按内存使用排序，大写字母P按照cpu使用排序

```bash
top - 19:40:49 up 104 days, 11:55,  1 user,  load average: 0.00, 0.01, 0.05
Tasks:  87 total,   2 running,  85 sleeping,   0 stopped,   0 zombie
%Cpu(s):  0.0 us,  0.0 sy,  0.0 ni,100.0 id,  0.0 wa,  0.0 hi,  0.0 si,  0.0 st
KiB Mem :  2046936 total,   148592 free,   282184 used,  1616160 buff/cache
KiB Swap:        0 total,        0 free,        0 used.  1461432 avail Mem

  PID USER      PR  NI    VIRT    RES    SHR S %CPU %MEM     TIME+ COMMAND
    1 root      20   0  125496   3980   2516 S  0.0  0.2   5:54.39 systemd
    2 root      20   0       0      0      0 S  0.0  0.0   0:00.25 kthreadd
    4 root       0 -20       0      0      0 S  0.0  0.0   0:00.00 kworker/0:0H
```

# 显示列
默认情况下仅显示比较重要的 PID、USER、PR、NI、VIRT、RES、SHR、S、%CPU、%MEM、TIME+、COMMAND 列。可以通过下面的快捷键来更改显示内容。通过 f 键可以选择显示的内容。按 f 键之后会显示列的列表，按 a-z 即可显示或隐藏对应的列，最后按回车键确定。按 o 键可以改变列的显示顺序。按小写的 a-z 可以将相应的列向右移动，而大写的 A-Z 可以将相应的列向左移动。最后按回车键确定。按大写的 F 或 O 键，然后按 a-z 可以将进程按照相应的列进行排序。而大写的 R 键可以将当前的排序倒转。
<style type="text/css">
.tg  {border-collapse:collapse;border-spacing:0;}
.tg td{font-family:Arial, sans-serif;font-size:14px;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:black;}
.tg th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:black;}
.tg .tg-0lax{text-align:left;vertical-align:top}
</style>
<table class="tg">
  <tr>
    <th class="tg-0lax">序号</th>
    <th class="tg-0lax">列名</th>
    <th class="tg-0lax">解释</th>
  </tr>
  <tr>
    <td class="tg-0lax">a</td>
    <td class="tg-0lax">PID</td>
    <td class="tg-0lax">进程id</td>
  </tr>
  <tr>
    <td class="tg-0lax">b</td>
    <td class="tg-0lax">PPID</td>
    <td class="tg-0lax">父进程id</td>
  </tr>
  <tr>
    <td class="tg-0lax">c</td>
    <td class="tg-0lax">RUSER</td>
    <td class="tg-0lax">Real user name</td>
  </tr>
  <tr>
    <td class="tg-0lax">d</td>
    <td class="tg-0lax">UID</td>
    <td class="tg-0lax">进程所有者的用户id</td>
  </tr>
  <tr>
    <td class="tg-0lax">e</td>
    <td class="tg-0lax">USER</td>
    <td class="tg-0lax">进程所有者的用户名</td>
  </tr>
  <tr>
    <td class="tg-0lax">f</td>
    <td class="tg-0lax">GROUP</td>
    <td class="tg-0lax">进程所有者的组名</td>
  </tr>
  <tr>
    <td class="tg-0lax">g</td>
    <td class="tg-0lax">TTY</td>
    <td class="tg-0lax">启动进程的终端名。不是从终端启动的进程则显示为 ?</td>
  </tr>
  <tr>
    <td class="tg-0lax">h</td>
    <td class="tg-0lax">PR</td>
    <td class="tg-0lax">优先级</td>
  </tr>
  <tr>
    <td class="tg-0lax">i</td>
    <td class="tg-0lax">NI</td>
    <td class="tg-0lax">nice值。负值表示高优先级，正值表示低优先级</td>
  </tr>
  <tr>
    <td class="tg-0lax">j</td>
    <td class="tg-0lax">P</td>
    <td class="tg-0lax">最后使用的CPU，仅在多CPU环境下有意义</td>
  </tr>
  <tr>
    <td class="tg-0lax">k</td>
    <td class="tg-0lax">%CPU</td>
    <td class="tg-0lax">上次更新到现在的CPU时间占用百分比</td>
  </tr>
  <tr>
    <td class="tg-0lax">l</td>
    <td class="tg-0lax">TIME</td>
    <td class="tg-0lax">进程使用的CPU时间总计，单位秒</td>
  </tr>
  <tr>
    <td class="tg-0lax">m</td>
    <td class="tg-0lax">TIME+</td>
    <td class="tg-0lax">进程使用的CPU时间总计，单位1/100秒</td>
  </tr>
  <tr>
    <td class="tg-0lax">n</td>
    <td class="tg-0lax">%MEM</td>
    <td class="tg-0lax">进程使用的物理内存百分比，simply RES divided by total physical memory</td>
  </tr>
  <tr>
    <td class="tg-0lax">o</td>
    <td class="tg-0lax">VIRT</td>
    <td class="tg-0lax">
进程使用的虚拟内存总量，单位kb。</br>
VIRT：virtual memory usage 虚拟内存</br>
1、进程“需要的”虚拟内存大小，包括进程使用的库、代码、数据等</br>
2、假如进程申请100m的内存，但实际只使用了10m，那么它会增长100m，而不是实际的使用量</br>
</td>
  </tr>
  
  <tr>
    <td class="tg-0lax">p</td>
    <td class="tg-0lax">SWAP</td>
    <td class="tg-0lax">进程使用的虚拟内存中，被换出的大小，单位kb。</td>
  </tr>
  <tr>
    <td class="tg-0lax">q</td>
    <td class="tg-0lax">RES</td>
    <td class="tg-0lax">
进程使用的、未被换出的物理内存大小，单位kb。</br>
RES：resident memory usage 常驻内存
</td>
  </tr>
  <tr>
    <td class="tg-0lax">r</td>
    <td class="tg-0lax">CODE</td>
    <td class="tg-0lax">可执行代码占用的物理内存大小，单位kb</td>
  </tr>
  <tr>
    <td class="tg-0lax">s</td>
    <td class="tg-0lax">DATA</td>
    <td class="tg-0lax">
可执行代码以外的部分(数据段+栈)占用的物理内存大小，单位kb</br>
1、数据占用的内存。如果top没有显示，按f键可以显示出来。</br>
2、真正的该程序要求的数据空间，是真正在运行中要使用的。</br>
</td>
  </tr>
  <tr>
    <td class="tg-0lax">t</td>
    <td class="tg-0lax">SHR</td>
    <td class="tg-0lax">
共享内存大小，单位kb</br>
SHR：shared memory 共享内存</br>
1、除了自身进程的共享内存，也包括其他进程的共享内存</br>
2、虽然进程只使用了几个共享库的函数，但它包含了整个共享库的大小</br>
3、swap out后，它将会降下来</br>
</td>
  </tr>
  <tr>
    <td class="tg-0lax">u</td>
    <td class="tg-0lax">nFLT</td>
    <td class="tg-0lax">页面错误次数</td>
  </tr>
  <tr>
    <td class="tg-0lax">v</td>
    <td class="tg-0lax">nDRT</td>
    <td class="tg-0lax">最后一次写入到现在，被修改过的页面数。</td>
  </tr>
  <tr>
    <td class="tg-0lax">w</td>
    <td class="tg-0lax">S</td>
    <td class="tg-0lax">进程状态。（D=不可中断的睡眠状态，R=运行，S=睡眠，T=跟踪/停止，Z=僵尸进程）</td>
  </tr>
  <tr>
    <td class="tg-0lax">x</td>
    <td class="tg-0lax">COMMAND</td>
    <td class="tg-0lax">命令名/命令行</td>
  </tr>
  <tr>
    <td class="tg-0lax">y</td>
    <td class="tg-0lax">WCHAN</td>
    <td class="tg-0lax">若该进程在睡眠，则显示睡眠中的系统函数名</td>
  </tr>
  <tr>
    <td class="tg-0lax">z</td>
    <td class="tg-0lax">Flags</td>
    <td class="tg-0lax">任务标志，参考 sched.h</td>
  </tr>
</table>

# 作用
top命令作为Linux下最常用的性能分析工具之一，可以监控、收集进程的CPU、IO、内存使用情况。比如我们可以通过top命令获得一个进程使用了多少虚拟内存（VIRT）、物理内存（RES）、共享内存（SHR）。
# VIRT RES SHR的准确含义

1. top命令通过解析/proc/<pid>/statm统计VIRT和RES和SHR字段值。
2. VIRT是申请的虚拟内存总量。
3. RES是进程使用的物理内存总和。
4. SHR是RES中”映射至文件”的物理内存总和。包括：
   - 程序的代码段。
   - 动态库的代码段。
   - 通过mmap做的文件映射。
   - 通过mmap做的匿名映射，但指明了MAP_SHARED属性。
   - 通过shmget申请的共享内存。
5. /proc/<pid>/smaps内Shared_*统计的是RES中映射数量>=2的物理内存。
6. /proc/<pid>/smaps内Private_*统计的是RES中映射数量=1的物理内存。

# 参考            
- [剖析top命令显示的VIRT RES SHR值](http://blog.sina.com.cn/s/blog_4e41487001016eio.html)
- [top命令查看线程信息和jstack使用介绍](https://www.cnblogs.com/shengulong/p/8513652.html)
- [10.1-10.5 w查看系统负载 vmstat , top, sar, nload](http://blog.51cto.com/13578154/2088684)
- [Linux TOP官方帮助文档](http://man7.org/linux/man-pages/man1/top.1.html)

