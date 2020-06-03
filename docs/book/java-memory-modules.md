<!-- TOC -->

- [Java内存模型详解（JMM）](#java内存模型详解jmm)
  - [基本概念](#基本概念)
    - [Java内存模型的抽象结构](#java内存模型的抽象结构)
    - [happens-before](#happens-before)
  - [重排序](#重排序)
  - [顺序一致性](#顺序一致性)
  - [volatile的内存语义](#volatile的内存语义)
    - [volatile内存语义的实现](#volatile内存语义的实现)
      - [内存屏障的使用](#内存屏障的使用)
    - [死循环代码](#死循环代码)
  - [锁的内存语义](#锁的内存语义)
    - [锁的释放、获取](#锁的释放获取)
    - [锁内存语义实现](#锁内存语义实现)
  - [final的内存语义](#final的内存语义)
    - [final域的重排序规则](#final域的重排序规则)
    - [写final域的重排序规则](#写final域的重排序规则)
    - [读final域的重排序规则](#读final域的重排序规则)
    - [final域为引用类型](#final域为引用类型)
    - [final引用不能从构造函数内“溢出”](#final引用不能从构造函数内溢出)
  - [参考](#参考)

<!-- /TOC -->
# Java内存模型详解（JMM）

## 基本概念

Java内存模型，即java memory model，简称JMM，它定义了主存与工作内存的抽象概念，并且底层对应CPU的寄存器、缓存、硬件内存以及CPU指令优化等。

JMM 体现在以下几个方面：

1. **原子性** - 保证指令不会受到线程上下文切换的影响
2. **可见性** - 保证指令不会受 cpu 缓存的影响
3. **有序性** - 保证指令不会受 cpu 指令并行优化的影响

java中多线程通信基于共享内存的方式，并且JMM保证了共享内存的可见性和有序性的问题，锁解决了原子性的问题。

### Java内存模型的抽象结构

1. 共享数据：实例域、静态域、数组存储在堆中，线程间共享
2. 非共享数据：局部变量、方法参数、异常处理器参数

<div align=center>

![java内存模型抽象结构示意图](..\images\1590914067946.png)
java内存模型抽象结构示意图

![线程之间的通信](..\images\1590914160544.png)
线程之间的通信
</div>

### happens-before

**happens-before仅仅要求前一个操作（执行的结果）对后一个操作可见；规定了对共享变量的写操作对其它线程的读操作可见，它是可见性与有序性的一套规则总结**

1. ❑ 程序顺序规则：一个线程中的每个操作，happens-before于该线程中的任意后续操作。
2. ❑ 监视器锁规则：线程解锁 m 之前对变量的写，对于接下来对 m 加锁的其它线程对该变量的读可见
3. ❑ volatile变量规则：线程对 volatile 变量的写，对接下来其它线程对该变量的读可见
4. ❑ 线程 start 前对变量的写，对该线程开始后对该变量的读可见
5. ❑ 线程结束前对变量的写，对其它线程得知它结束后的读可见
6. ❑ 线程 t1 打断 t2（interrupt）前对变量的写，对于其他线程得知 t2 被打断后对变量的读可见
7. ❑ 对变量默认值（0，false，null）的写，对其它线程对该变量的读可见
8. ❑ 传递性：如果A happens-before B，且B happens-before C，那么A happens-before C。

## 重排序

## 顺序一致性

## volatile的内存语义

1. **同步**：一种弱的同步机制，用来确保将变量的更新操作通知到其他线程，Volatile修饰变量的单个读/写，可以看做使用同一个锁完成的同步操作。
2. 可见性：当一个线程修改了变量的值，那么新的值对于其他线程是可以**立即获取的。**被volatile修饰的变量总会返回最新写入主内存的值
3. **禁止指令重排序**
4. **场景**：一个变量被多个线程共享，线程直接给这个变量赋值。
5. **保证线程安全的前置条件**
   - 单个读/写
   - 不同的volatile变量之间，不能互相依赖
6. **原子性**：只能对原子性（单个读/写）操作具备线程安全性
7. **原理**：普通变量的获取首先把主内存的数据获取到CPU缓存中，然后通过缓存使用；被volatile修饰的变量JVM保证了每次读变量都从内存中读，跳过 CPU cache 这一步，直接通过主内存获取数据
8. **Volatile写语义**：当写一个volatile变量时，JMM会把该线程对应的本地内存中的共享变量值刷新到主内存。
9. **Volatile读语义**：当读一个volatile变量时，JMM会把该线程对应的本地内存置为无效。线程接下来将从主内存中读取共享变量。

### volatile内存语义的实现

<div align=center>

![volatile重排序规则表](..\images\1590929114372.png)
volatile重排序规则表
</div>


#### 内存屏障的使用

基于保守策略的JMM内存屏障插入策略。  
1. ❑ 在每个volatile写操作的前面插入一个StoreStore屏障。
2. ❑ 在每个volatile写操作的后面插入一个StoreLoad屏障。
3. ❑ 在每个volatile读操作的前面插入一个LoadLoad屏障。
4. ❑ 在每个volatile读操作的后面插入一个LoadStore屏障

<div align=center>

![volatile写：指令执行顺序示意图](..\images\1590929400626.png)
volatile写：指令执行顺序示意图

![volatile读：指令执行顺序示意图](..\images\1590929489469.png)
volatile读：指令执行顺序示意图
</div>

### 死循环代码

```java
package com.sunld.thread.jmm;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

/**
 * @author : sunliaodong
 * @version : V1.0.0
 * @description: 不适用Volatile导致的死循环
 * 不同的CPU可能无法达到演示效果
 * @date : 2020/5/31 16:05
 */
public class VolatileDeadCircleTest {
    private volatile boolean run = true;

    public static void main(String[] args) throws InterruptedException {
        VolatileDeadCircleTest t = new VolatileDeadCircleTest();
        AtomicInteger count = new AtomicInteger();
        new Thread(() -> {
            while(t.isRun()){
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("子线程一致执行:" + (count.getAndIncrement()));
            }
        }).start();
        sleep(10 * 1000L);
        // 计划停止子线程，但是和预期可能不一致，最终会停止
        t.setRun(false);
    }

    public boolean isRun() {
        return run;
    }

    public void setRun(boolean run) {
        this.run = run;
    }
}
```

## 锁的内存语义

1. 锁可以让临界区互斥执行
2. 重要的同步手段
3. 释放锁的线程向获取同一个锁的线程发送消息（唤醒）

### 锁的释放、获取

```java
package com.sunld.thread.jmm;

/**
 * @author : sunliaodong
 * @version : V1.0.0
 * @description: TODO
 * @date : 2020/6/3 17:34
 */
public class MonitorExample {
    int a = 0;

    public synchronized void writer(){ //1
        a++;                           //2
    }
                                       //3
    public synchronized void reader(){ //4
        System.out.println(a);         //5
    }
                                       //6
}
```

<div align=center>

![1591177153382.png](..\images\1591177153382.png)

</div>

1. 线程之间使用同一个锁存在先后顺序
2. 某个方法使用完成之后释放对应的锁，其他线程可竞争获取该锁
3. synchronized使用的是监视器锁
4. 锁释放与volatile写有相同的内存语义；锁获取与volatile读有相同的内存语义（释放锁之后会把共享数据刷入主存并且使其他线程的本地内存中的变量无效）

### 锁内存语义实现



## final的内存语义

对final域的读写更像是普通变量访问。

### final域的重排序规则

1. 在构造函数内对一个final域的写入，与随后把这个被构造对象的引用赋值给一个引用变量，这两个操作之间不能重排序。
2. 初次读一个包含final域的对象的引用，与随后初次读这个final域，这两个操作之间不能重排序。

```java
package com.sunld.thread.jmm;

/**
 * @author : sunliaodong
 * @version : V1.0.0
 * @description: TODO
 * @date : 2020/5/31 21:04
 */
public class FinalExample {
    int i;                         // 普通变量
    final int j;                   // final变量
    static FinalExample obj;
    public FinalExample(){         // 构造函数
        i = 1;                     // 普通变量赋值
        j = 2;                     // final变量赋值
    }
    public static void writer(){   // 写线程A执行
        obj = new FinalExample();
    }
    public static void reader(){   // 读线程B执行
        FinalExample object = obj; // 读对象引用
        int a = object.i;          // 读普通变量
        int b = object.j;          // 读final变量
    }
}
```

### 写final域的重排序规则

1. JMM禁止编译器把final域的写重排序到构造函数之外
2. 编译器会在final域的写之后，构造函数return之前，插入一个StoreStore屏障。这个屏障禁止处理器把final域的写重排序到构造函数之外。
3. 在对象引用为任意线程可见之前，对象的final域已经被正确初始化过了，而普通域不具有这个保障
4. 写final域的重排序规则会要求编译器在final域的写之后，构造函数return之前插入一个StoreStore障屏。读final域的重排序规则要求编译器在读final域的操作前面插入一个LoadLoad屏障。

<div align=center>

![写final域的重排序规则](..\images\1590930859921.png)
写final域的重排序规则
</div>

### 读final域的重排序规则

**编译器会在读final域操作的前面插入一个LoadLoad屏障，防止初次读对象引用与初次读该对象包含的final域出现重排序。**    

<div align=center>

![读final域的重排序规则](..\images\1590931029214.png)
读final域的重排序规则
</div>

### final域为引用类型

在构造函数内对一个final引用的对象的成员域的写入，与随后在构造函数外把这个被构造对象的引用赋值给一个引用变量，这两个操作之间不能重排序。  

### final引用不能从构造函数内“溢出”

在构造函数内部，不能让这个被构造对象的引用为其他线程所见，也就是对象引用不能在构造函数中“逸出”。

## 参考

1. 《Java并发编程的艺术》

