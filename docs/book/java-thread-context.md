<!-- TOC -->

- [并发编程面临的问题](#并发编程面临的问题)
  - [上线文切换](#上线文切换)
    - [扩展知识](#扩展知识)
      - [栈与栈帧](#栈与栈帧)
      - [进程](#进程)
      - [上下文](#上下文)
      - [寄存器](#寄存器)
      - [程序计数器](#程序计数器)
      - [PCB-“切换桢”](#pcb-切换桢)
    - [上下文切换的活动](#上下文切换的活动)
      - [切换的原因](#切换的原因)
      - [切换过程的动作](#切换过程的动作)
    - [引起线程上下文切换的原因](#引起线程上下文切换的原因)
    - [上线文切换次数监测](#上线文切换次数监测)
    - [减少上下文切换的方式](#减少上下文切换的方式)
  - [死锁问题](#死锁问题)
    - [避免死锁的方式](#避免死锁的方式)
    - [死锁定位](#死锁定位)
  - [活锁问题](#活锁问题)
  - [饥饿问题](#饥饿问题)
  - [资源限制问题](#资源限制问题)
  - [变量线程安全分析](#变量线程安全分析)
    - [成员变量、类变量](#成员变量类变量)
    - [局部变量](#局部变量)
      - [基本类型](#基本类型)
      - [引用类型--成员变量](#引用类型--成员变量)
      - [引用类型--局部变量](#引用类型--局部变量)
    - [常见线程安全类](#常见线程安全类)
  - [参考](#参考)

<!-- /TOC -->
# 并发编程面临的问题

## 上线文切换

巧妙地利用了时间片轮转的方式, CPU 给每个任务都服务一定的时间，然后把当前任务的状态保存下来，在加载下一任务的状态后，继续服务下一任务，任务的状态保存及再加载, 这段过程就叫做**上下文切换**。时间片轮转的方式使多个任务在同一颗 CPU 上执行变成了可能。
<div align=center>

![1587898800503.png](..\images\1587898800503.png)

</div>

### 扩展知识

#### 栈与栈帧

**Java Virtual Machine Stacks （Java 虚拟机栈）**

1. 每个线程启动后，虚拟机就会为其分配一块栈内存。
2. 每个栈由多个栈帧（Frame）组成，对应着每次方法调用时所占用的内存
3. 每个线程只能有一个活动栈帧，对应着当前正在执行的那个方法

#### 进程

（有时候也称做任务）是指一个程序运行的实例。在 Linux 系统中，线程就是能并行运行并且与他们的父进程（创建他们的进程）共享同一地址空间（一段内存区域）和其他资源的轻量级的进程。

#### 上下文

是指某一时间点 CPU 寄存器和程序计数器的内容。

#### 寄存器

是 CPU 内部的数量较少但是速度很快的内存（与之对应的是 CPU 外部相对较慢的 RAM 主内存）。寄存器通过对常用值（通常是运算的中间值）的快速访问来提高计算机程序运行的速度。

#### 程序计数器

是一个专用的寄存器，用于表明指令序列中 CPU 正在执行的位置，存的值为正在执行的指令的位置或者下一个将要被执行的指令的位置，具体依赖于特定的系统。

#### PCB-“切换桢”

上下文切换可以认为是内核（操作系统的核心）在 CPU 上对于进程（包括线程）进行切换，上下文切换过程中的信息是保存在进程控制块（PCB, process control block）中的。PCB 还经常被称作“切换桢”（switchframe）。信息会一直保存到 CPU 的内存中，直到他们被再次使用。

### 上下文切换的活动

#### 切换的原因

1. 当前线程的时间片耗尽
2. 垃圾回收
3. 有更高优先级的线程需要执行
4. 线程自己调用 sleep、yield、wait、join、park、synchronized、lock 等方法

#### 切换过程的动作

1. 由操作系统保存当前线程的状态，并恢复另一个线程的状态（通过程序计数器完成）
   - 挂起一个进程，将这个进程在 CPU 中的状态（上下文）存储于内存中的某处。
   - 在内存中检索下一个进程的上下文并将其在 CPU 的寄存器中恢复。
   - 跳转到程序计数器所指向的位置（即跳转到进程被中断时的代码行），以恢复该进程在程序中。
2. 状态包括程序计数器、虚拟机栈中每个栈帧的信息，如局部变量、操作数栈、返回地址等
3. Context Switch 频繁发生会影响性能



### 引起线程上下文切换的原因

1. 当前执行任务的时间片用完之后，系统 CPU 正常调度下一个任务；
2. 当前执行任务碰到 IO 阻塞，调度器将此任务挂起，继续下一任务；
3. 多个任务抢占锁资源，当前任务没有抢到锁资源，被调度器挂起，继续下一任务；
4. 用户代码挂起当前任务，让出 CPU 时间；
5. 硬件中断；

### 上线文切换次数监测

1. 使用`Lmbench`可以测量上线文切换的时长
2. 使用`vmstat`可以查看上下文切换的次数，《参考[vmstat](book/linux-vmstat.md)》

### 减少上下文切换的方式

1. ❑ 无锁并发编程。多线程竞争锁时，会引起上下文切换，所以多线程处理数据时，可以用一些办法来避免使用锁，如将数据的ID按照Hash算法取模分段，不同的线程处理不同段的数据。
2. ❑ CAS算法。Java的Atomic包使用CAS算法来更新数据，而不需要加锁。
3. ❑ 使用最少线程。合理使用线程池。
4. ❑ 协程：在单线程里实现多任务的调度，并在单线程里维持多个任务间的切换。

## 死锁问题

多线程互相需要对方的锁，而且都不释放就会出现死锁。

### 避免死锁的方式

1. ❑ 避免一个线程同时获取多个锁。
2. ❑ 避免一个线程在锁内同时占用多个资源，尽量保证每个锁只占用一个资源。
3. ❑ 尝试使用定时锁，使用lock.tryLock（timeout）来替代使用内部锁机制。
4. ❑ 对于数据库锁，加锁和解锁必须在一个数据库连接里，否则会出现解锁失败的情况。
5. ❑ 避免死锁要注意加锁顺序
6. ❑ 如果由于某个线程进入了死循环，导致其它线程一直等待，对于这种情况 linux 下可以通过 top 先定位到CPU 占用高的 Java 进程，再利用 top -Hp 进程id 来定位是哪个线程，最后再用 jstack 排查


### 死锁定位

1. 可用工具jconsole工具，或者jps获取线程id在使用jstack定位

## 活锁问题

活锁出现在两个线程互相改变对方的结束条件，最后谁也无法结束。

```java
package com.sunld.thread;

/**
 * @author : sunliaodong
 * @version : V1.0.0
 * @description: TODO
 * @date : 2020/5/29 14:01
 */
public class TestLiveLock {
    static volatile  int count = 10;
    static final Object lock = new Object();

    public static void main(String[] args) {
        new Thread(() -> {
            while(count > 0){// 期望0时退出
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count--;
                System.out.println("11 count: " + count);
            }
        }).start();

        new Thread(() -> {
            // 期望超过 20 退出循环
            while (count < 20) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count++;
                System.out.println("22 count: " + count);
            }

        }).start();
    }
}
```

## 饥饿问题

<div align=center>

![1590736135292.png](..\images\1590736135292.png)

![顺序加锁解决方案](..\images\1590736163727.png)

顺序加锁解决方案

</div>

## 资源限制问题

1. 资源限制：在并发编程过程中，程序的执行速度受限于物理硬件资源或软件资源，比如：节点网络、磁盘读写、CPU处理速度、数据库连接
2. 由于资源组不足，在多线程并发过程中会降低程序的运行效率，可能比串行还慢（由于上线文切换）
3. 解决方案：合理使用线程池、复用socket连接、多路复用

## 变量线程安全分析

### 成员变量、类变量

1. 如果它们没有共享，则线程安全
2. 如果它们被共享了，根据它们的状态是否能够改变，又分两种情况
   - 如果只有读操作，则线程安全
   - 如果有读写操作，则这段代码是临界区，需要考虑线程安全

### 局部变量

1. 局部变量是线程安全的
2. 但局部变量引用的对象则未必
   - 如果该对象没有逃离方法的作用访问，它是线程安全的
   - 如果该对象逃离方法的作用范围，需要考虑线程安全

#### 基本类型

```java
package com.sunld.thread;

/**
 * @author : sunliaodong
 * @version : V1.0.0
 * @description: TODO
 * @date : 2020/5/28 19:48
 */
public class TestSafe {

    public static void testSafe1(){
        int i = 10;
        i++;
        System.out.println(i);
    }

    public static void main(String[] args) {

    }
}
```

```java
  public static void testSafe1();
    descriptor: ()V
    flags: ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=1, args_size=0
         0: bipush        10
         2: istore_0
         3: iinc          0, 1
         6: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
         9: iload_0
        10: invokevirtual #3                  // Method java/io/PrintStream.println:(I)V
        13: return
      LineNumberTable:
        line 12: 0
        line 13: 3
        line 14: 6
        line 15: 13
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            3      11     0     i   I
```

<div align=center>

![1590666789177.png](..\images\1590666789177.png)

</div>

#### 引用类型--成员变量

<div align=center>

![1590667021020.png](..\images\1590667021020.png)

</div>

#### 引用类型--局部变量

<div align=center>

![1590667066179.png](..\images\1590667066179.png)

</div>

### 常见线程安全类

1. String
2. Integer
3. StringBuffer
4. Random
5. Vector
6. Hashtable
7. java.util.concurrent 包下的类

## 参考

1. 《Java并发编程的艺术》
