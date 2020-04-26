<!-- TOC -->

- [CPU使用率过高](#cpu%e4%bd%bf%e7%94%a8%e7%8e%87%e8%bf%87%e9%ab%98)
  - [计算方式](#%e8%ae%a1%e7%ae%97%e6%96%b9%e5%bc%8f)

<!-- /TOC -->

# CPU使用率过高

## 计算方式

<div align=center>

![1587429499354.png](..\images\1587429499354.png)

</div>

1. 登录服务器之后使用[top](#_Top)命令，并且输入1按照cpu使用率降序排序获取使用率最高的线程
2. perf top -g -p pid查看调用关系（\-g开启调用关系分析，\-p指定线程号）
3. top -Hp pid可以查看某个进程的线程信息
4. jstack 线程ID 可以查看某个线程的堆栈情况，特别对于hung挂死的线程，可以使用选项\-F强制打印dump信息jstack -F pid
5. 执行 jstack pid|grep -A 10 pid的16进制，得到线程堆栈信息中1371这个线程所在行的后面10行