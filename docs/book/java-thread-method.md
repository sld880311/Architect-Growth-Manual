<!-- TOC -->

- [线程常用方法说明](#线程常用方法说明)
  - [常用方法汇总](#常用方法汇总)
  - [常用方法详解](#常用方法详解)
    - [线程中断（interrupt）](#线程中断interrupt)
    - [Join 等待其他线程终止](#join-等待其他线程终止)
      - [为什么要用 join()方法](#为什么要用-join方法)
    - [wait/notify](#waitnotify)
      - [正确使用方式](#正确使用方式)
    - [sleep、yield、wait](#sleepyieldwait)
      - [wait](#wait)
        - [wait为什么需要配合synchronized使用](#wait为什么需要配合synchronized使用)
      - [sleep](#sleep)
      - [yield](#yield)
      - [线程优先级](#线程优先级)
    - [start 与 run 区别](#start-与-run-区别)
    - [park&unpark](#parkunpark)

<!-- /TOC -->
# 线程常用方法说明

<div align=center>

![1590711795506.png](..\images\1590711795506.png)

</div>

## 常用方法汇总

<style type="text/css">
.tg  {border-collapse:collapse;border-color:#bbb;border-spacing:0;}
.tg td{background-color:#E0FFEB;border-color:#bbb;border-style:solid;border-width:1px;color:#594F4F;
  font-family:Arial, sans-serif;font-size:14px;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg th{background-color:#9DE0AD;border-color:#bbb;border-style:solid;border-width:1px;color:#493F3F;
  font-family:Arial, sans-serif;font-size:14px;font-weight:normal;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg .tg-0lax{text-align:left;vertical-align:top}
.tg .tg-sjuo{background-color:#C2FFD6;text-align:left;vertical-align:top}
</style>
<table class="tg">
<thead>
  <tr>
    <th class="tg-0lax">方法名</th>
    <th class="tg-0lax">static</th>
    <th class="tg-0lax">功能说明</th>
    <th class="tg-0lax">注意</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td class="tg-sjuo">start()</td>
    <td class="tg-sjuo"></td>
    <td class="tg-sjuo">启动一个新线程，在新的线程运行 run 方法中的代码</td>
    <td class="tg-sjuo">start 方法只是让线程进入就绪，里面代码不一定立刻运行（CPU的时间片还没分给它）。每个线程对象的start方法只能调用一次，如果调用了多次会出现IllegalThreadStateException</td>
  </tr>
  <tr>
    <td class="tg-0lax">run()</td>
    <td class="tg-0lax"></td>
    <td class="tg-0lax">新线程启动后会调用的方法</td>
    <td class="tg-0lax">如果在构造 Thread 对象时传递了 Runnable 参数，则线程启动后会调用 Runnable 中的 run 方法，否则默认不执行任何操作。但可以创建Thread 的子类对象，来覆盖默认行为</td>
  </tr>
  <tr>
    <td class="tg-sjuo">join()</td>
    <td class="tg-sjuo"></td>
    <td class="tg-sjuo">等待线程运行结束</td>
    <td class="tg-sjuo"></td>
  </tr>
  <tr>
    <td class="tg-0lax">join(long n)</td>
    <td class="tg-0lax"></td>
    <td class="tg-0lax">等待线程运行结束,多等待n 毫秒</td>
    <td class="tg-0lax"></td>
  </tr>
  <tr>
    <td class="tg-sjuo">getId()</td>
    <td class="tg-sjuo"></td>
    <td class="tg-sjuo">获取线程长整型的 id</td>
    <td class="tg-sjuo">id唯一</td>
  </tr>
  <tr>
    <td class="tg-0lax">getName()</td>
    <td class="tg-0lax"></td>
    <td class="tg-0lax">获取线程名</td>
    <td class="tg-0lax"></td>
  </tr>
  <tr>
    <td class="tg-sjuo">setName(String)</td>
    <td class="tg-sjuo"></td>
    <td class="tg-sjuo">修改线程名</td>
    <td class="tg-sjuo"></td>
  </tr>
  <tr>
    <td class="tg-0lax">getPriority()</td>
    <td class="tg-0lax"></td>
    <td class="tg-0lax">获取线程优先级</td>
    <td class="tg-0lax"></td>
  </tr>
  <tr>
    <td class="tg-sjuo">setPriority(int)</td>
    <td class="tg-sjuo"></td>
    <td class="tg-sjuo">修改线程优先级</td>
    <td class="tg-sjuo">java中规定线程优先级是1~10 的整数，较大的优先级能提高该线程被CPU 调度的机率</td>
  </tr>
  <tr>
    <td class="tg-0lax">getState()</td>
    <td class="tg-0lax"></td>
    <td class="tg-0lax">获取线程状态</td>
    <td class="tg-0lax">Java中线程状态是用 6 个enum 表示，分别为：NEW,RUNNABLE, BLOCKED, WAITING,TIMED_WAITING,TERMINATED</td>
  </tr>
  <tr>
    <td class="tg-sjuo">isInterrupted()</td>
    <td class="tg-sjuo"></td>
    <td class="tg-sjuo">判断是否被打断，</td>
    <td class="tg-sjuo">不会清除 打断标记</td>
  </tr>
  <tr>
    <td class="tg-0lax">isAlive()</td>
    <td class="tg-0lax"></td>
    <td class="tg-0lax">线程是否存活（还没有运行完毕)
	</td>
    <td class="tg-0lax"></td>
  </tr>
  <tr>
    <td class="tg-sjuo">interrupt()</td>
    <td class="tg-sjuo"></td>
    <td class="tg-sjuo">打断线程</td>
    <td class="tg-sjuo">如果被打断线程正在 sleep，wait，join 会导致被打断的线程抛出InterruptedException，并清除打断标记；如果打断的正在运行的线程，则会设置 打断标记；park的线程被打断，也会设置打断标记</td>
  </tr>
  <tr>
    <td class="tg-0lax">interrupted()</td>
    <td class="tg-0lax">static</td>
    <td class="tg-0lax">判断当前线程是否被打断</td>
    <td class="tg-0lax">会清除 打断标记</td>
  </tr>
  <tr>
    <td class="tg-sjuo">currentThread()</td>
    <td class="tg-sjuo">static</td>
    <td class="tg-sjuo">获取当前正在执行的线程</td>
    <td class="tg-sjuo"></td>
  </tr>
  <tr>
    <td class="tg-sjuo">sleep(long n)</td>
    <td class="tg-sjuo">static</td>
    <td class="tg-sjuo">让当前执行的线程休眠n毫秒，休眠时让出 cpu 的时间片给其它线程</td>
    <td class="tg-sjuo"></td>
  </tr>
  <tr>
    <td class="tg-0lax">yield()</td>
    <td class="tg-0lax">static</td>
    <td class="tg-0lax">提示线程调度器让出当前线程对CPU的使用</td>
    <td class="tg-0lax">主要是为了测试和调试x</td>
  </tr>
  <tr>
    <td class="tg-0lax">activeCount</td>
    <td class="tg-0lax">static</td>
    <td class="tg-0lax">程序中活跃的线程数</td>
    <td class="tg-0lax"></td>
  </tr>
   <tr>
    <td class="tg-0lax">enumerate</td>
    <td class="tg-0lax">static</td>
    <td class="tg-0lax">枚举程序中的线程</td>
    <td class="tg-0lax"></td>
  </tr>
  <tr>
    <td class="tg-0lax">isDaemon()</td>
    <td class="tg-0lax"></td>
    <td class="tg-0lax">一个线程是否为守护线程</td>
    <td class="tg-0lax"></td>
  </tr>
  <tr>
    <td class="tg-0lax">setDaemon()</td>
    <td class="tg-0lax"></td>
    <td class="tg-0lax">设置一个线程为守护线程。(用户线程和守护线程的区别在于，是否等待主线程依赖于主线程结束而结束)</td>
    <td class="tg-0lax"></td>
  </tr>
  
</tbody>
</table>

<div align=center>

![1589109533903.png](..\images\1589109533903.png)

</div>

## 常用方法详解

### 线程中断（interrupt） 

中断一个线程，其本意是给这个线程一个通知信号，会影响这个线程内部的一个**中断标识位**。 **这个线程本身并不会因此而改变状态(如阻塞，终止等)。**

1. 调用 interrupt()方法并不会中断一个正在运行的线程。也就是说处于 Running 状态的线程并不会因为被中断而被终止，仅仅改变了内部维护的中断标识位而已。 
2. 若调用 sleep()而使线程处于 TIMED-WATING 状态，这时调用 interrupt()方法，会抛出InterruptedException,从而使线程提前结束 TIMED-WATING 状态。 
3. 许多声明抛出InterruptedException 的方法(如 Thread.sleep(long mills 方法))，抛出常前，都会清除中断标识位，所以抛出异常后，调用 isInterrupted()方法将会返回 false。 
4. 中断状态是线程固有的一个标识位，可以通过此标识位安全的终止线程。比如,你想终止一个线程thread的时候，可以调用thread.interrupt()方法，在线程的run方法内部可以根据 thread.isInterrupted()的值来优雅的终止线程。 
5. 打断sleep、wait、join的线程，会清空打断状态（isInterrupted方法为false）
6. 打断正常运行的线程，不会清空打断状态
7. 打断park线程，不会清空打断状态

### Join 等待其他线程终止 

join() 方法，等待其他线程终止，在当前线程中调用一个线程的 join() 方法，则当前线程转为阻塞状态，回到另一个线程结束，当前线程再由阻塞状态变为就绪状态，等待 cpu 的宠幸。 

#### 为什么要用 join()方法 

很多情况下，主线程生成并启动了子线程，需要用到子线程返回的结果，也就是需要主线程需要在子线程结束后再结束，这时候就要用到 join() 方法。

```java
package com.sunld.thread;

import java.util.concurrent.TimeUnit;

/**
 * @author : sunliaodong
 * @version : V1.0.0
 * @description: TODO
 * @date : 2020/5/28 14:26
 */
public class TestJoin {
    public static void main(String[] args) {
        Thread p = Thread.currentThread();
        for(int i = 0; i < 10; i++) {
            Thread t = new Thread(new D(p), String.valueOf(i));
            t.start();
            p = t;
        }
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + " ddd");
    }

    static class D implements Runnable{
        private Thread thread;
        public D(Thread thread) {
            this.thread = thread;
        }
        public void run() {
            try {
                thread.join();
            }catch(Exception e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " dd11d");
        }
    }
}
```

### wait/notify

1. obj.wait() 让进入 object 监视器的线程到 waitSet 等待，在对象的监视器上等待，直到当前的线程放弃此对象上的锁定（无限制等待），才能继续执行被唤醒的线程，被唤醒的线程将以常规方式与在该对象上主动同步的其他所有线程进行竞争
2. obj.notify() 在 object 上正在 waitSet 等待的线程中随意挑一个唤醒（发生在对实现做出决定时）
3. obj.notifyAll() 让 object 上正在 waitSet 等待的线程全部唤醒

```java
package com.sunld.thread;

/**
 * @author : sunliaodong
 * @version : V1.0.0
 * @description: TODO
 * @date : 2020/5/28 20:59
 */
public class TestWaitAndNotify {
    final static Object obj = new Object();

    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (obj) {
                System.out.println("1: 执行....");
                try {
                    obj.wait(); // 让线程在obj上一直等待下去
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("1: 其它代码....");
            }
        }).start();
        new Thread(() -> {
            synchronized (obj) {
                System.out.println("2: 执行....");
                try {
                    obj.wait(); // 让线程在obj上一直等待下去
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("2: 其它代码....");
            }
        }).start();
        // 主线程两秒后执行
        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("唤醒 obj 上其它线程");
        synchronized (obj) {
//            obj.notify(); // 唤醒obj上一个线程
             obj.notifyAll(); // 唤醒obj上所有等待线程
        }
    }
    /**
     notify:
     1: 执行....
     2: 执行....
     唤醒 obj 上其它线程
     1: 其它代码....
     */
    
    /**
     notifyal
     1: 执行....
     2: 执行....
     唤醒 obj 上其它线程
     2: 其它代码....
     1: 其它代码....
     */
}
```

#### 正确使用方式



### sleep、yield、wait

#### wait

1. Object对象的方法，调用之后进入WAITING 状态
2. 释放对象锁（wait 方法一般用在同步方法或同步代码块中）
3. 只有等待其他线程通知或被中断后才能返回
4. 调用 wait()方法的时候，线程会放弃对象锁，进入等待此对象的等待锁定池，只有针对此对象调用 notify()方法后本线程才进入对象锁定池准备获取对象锁进入运行状态。
5. 需要配合synchronized使用

##### wait为什么需要配合synchronized使用

**防止出现死锁。** 

在wait（）的内部，会先释放锁obj1，然后进入阻塞状态，之后，它被另外一个线程用notify（）唤醒，去重新拿锁！其次，wait（）调用完成后，执行后面的业务逻辑代码，然后退出synchronized同步块，再次释放锁。内部伪代码如下：

```java
wait(){
  //释放锁
  //阻塞，等待被其他线程notify
  //重新获取锁
  //处理后续逻辑
}
```

wait（）和notify（）所作用的对象和synchronized所作用的对象是同一个，只能有一个对象，无法区分队列空和列队满两个条件。可以通过**Condition**实现（参考：[Condition](book/java-condition.md)）。

#### sleep

1. 调用 sleep 会让当前线程从 Running 进入 Timed Waiting 状态（阻塞），到了时间之后进入等待队列
2. 不会释放锁（wait会）
3. 其它线程可以使用 interrupt 方法打断正在睡眠的线程，这时 sleep 方法会抛出 InterruptedException
4. 睡眠结束后的线程未必会立刻得到执行
5. 建议用 TimeUnit 的 sleep 代替 Thread 的 sleep 来获得更好的可读性
6. 属于Thread类
7. 不需要强制配额synchronized使用

#### yield

1. 调用 yield 会让当前线程从 Running 进入 Runnable 就绪状态，与其他线程一起重新竞争 CPU 时间片，然后调度执行其它线程
2. 一般优先级高的会可能有效被调度，但是具体的实现依赖于操作系统的任务调度器

#### 线程优先级

1. 线程优先级会提示（hint）调度器优先调度该线程，但它仅仅是一个提示，调度器可以忽略它
2. 如果 cpu 比较忙，那么优先级高的线程会获得更多的时间片，但 cpu 闲时，优先级几乎没作用

```java
package com.sunld.thread;

/**
 * @author : sunliaodong
 * @version : V1.0.0
 * @description: TODO
 * @date : 2020/5/28 8:45
 */
public class TestYield {
    public static void main(String[] args) {
        Runnable task1 = () -> {
            int count = 0;
            for(int i = 0; i < 1000; i++){
                System.out.println("----------->1---" + count++);
            }
        };
        Runnable task2 = () -> {
            int count = 0;
            for(int i = 0; i < 1000; i++){
                Thread.yield();
                System.out.println("----------->2---" + count++);
            }
        };

        Thread t1 = new Thread(task1, "thread1" );
        Thread t2 = new Thread(task2, "thread2" );
        t1.setPriority(Thread.MIN_PRIORITY);
        t2.setPriority(Thread.MAX_PRIORITY);
        t1.start();
        t2.start();
    }
}
```

### start 与 run 区别 

1. start方法启动线程，真正实现了多线程运行。
2. 使用start启动一个线程（处于就绪状态），然后线程会真正执行run方法  

```java
package com.sunld.thread;

/**
 * @author : sunliaodong
 * @version : V1.0.0
 * @description: 测试run方法和start方法的区别
 * @date : 2020/5/27 20:19
 */
public class TestThreadRunAndStart {

    public static void  testRun(Thread t1){
        t1.run();
        System.out.println("do other things ...");
    }

    public static void  testStart(Thread t1){
        t1.start();
        System.out.println("do other things ...");
    }

    public static void main(String[] args) {
        Thread t1 = new Thread("t1") {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + "start");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName()+ "end");
            }
        };
        //顺序执行
//        TestThreadRunAndStart.testRun(t1);
        //异步执行
        TestThreadRunAndStart.testStart(t1);
    }
}
```

### park&unpark

```java
// 暂停当前线程
LockSupport.park();
// 恢复某个线程的运行
LockSupport.unpark(暂停线程对象)
```

<div align=center>

![1590712088254.png](..\images\1590712088254.png)

</div>

1. wait，notify 和 notifyAll 必须配合 Object Monitor 一起使用，而 park，unpark 不必
2. park & unpark 是以线程为单位来【阻塞】和【唤醒】线程，而 notify 只能随机唤醒一个等待线程，notifyAll是唤醒所有等待线程，就不那么【精确】
3. park & unpark 可以先 unpark，而 wait & notify 不能先 notify

<div align=center>

![1590712868689.png](..\images\1590712868689.png)

</div>

具体参考：[LockSupport](book/java-locksupport.md)