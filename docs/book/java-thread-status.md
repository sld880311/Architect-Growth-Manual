<!-- TOC -->

- [线程状态](#线程状态)
  - [操作系统中的状态](#操作系统中的状态)
  - [Java中的线程状态](#java中的线程状态)
    - [源码定义](#源码定义)
    - [状态转换](#状态转换)
    - [状态说明](#状态说明)
      - [特殊说明](#特殊说明)
      - [Java多线程的就绪、运行和死亡状态（DEAD）](#java多线程的就绪运行和死亡状态dead)
  - [状态分析-jvisualvm](#状态分析-jvisualvm)
    - [参考代码（java并发编程艺术）](#参考代码java并发编程艺术)
    - [dump信息](#dump信息)
  - [参考](#参考)

<!-- /TOC -->
# 线程状态

## 操作系统中的状态

<div align=center>

![1590649906474.png](..\images\1590649906474.png)

</div>

1. 【初始状态】仅是在语言层面创建了线程对象，还未与操作系统线程关联
2. 【可运行状态】（就绪状态）指该线程已经被创建（与操作系统线程关联），可以由 CPU 调度执行
3. 【运行状态】指获取了 CPU 时间片运行中的状态
   - 当 CPU 时间片用完，会从【运行状态】转换至【可运行状态】，会导致线程的上下文切换
4. 【阻塞状态】
   - 如果调用了阻塞 API，如 BIO 读写文件，这时该线程实际不会用到 CPU，会导致线程上下文切换，进入【阻塞状态】
   - 等 BIO 操作完毕，会由操作系统唤醒阻塞的线程，转换至【可运行状态】
   - 与【可运行状态】的区别是，对【阻塞状态】的线程来说只要它们一直不唤醒，调度器就一直不会考虑调度它们
5. 【终止状态】表示线程已经执行完毕，生命周期已经结束，不会再转换为其它状态

## Java中的线程状态

### 源码定义

```java
/**
* A thread state.  A thread can be in one of the following states:
* <ul>
* <li>{@link #NEW}<br>
*     A thread that has not yet started is in this state.
*     </li>
* <li>{@link #RUNNABLE}<br>
*     A thread executing in the Java virtual machine is in this state.
*     </li>
* <li>{@link #BLOCKED}<br>
*     A thread that is blocked waiting for a monitor lock
*     is in this state.
*     </li>
* <li>{@link #WAITING}<br>
*     A thread that is waiting indefinitely for another thread to
*     perform a particular action is in this state.
*     </li>
* <li>{@link #TIMED_WAITING}<br>
*     A thread that is waiting for another thread to perform an action
*     for up to a specified waiting time is in this state.
*     </li>
* <li>{@link #TERMINATED}<br>
*     A thread that has exited is in this state.
*     </li>
* </ul>
*
* <p>
* A thread can be in only one state at a given point in time.
* These states are virtual machine states which do not reflect
* any operating system thread states.
*
* @since   1.5
* @see #getState
*/
public enum State {
    /**
    * Thread state for a thread which has not yet started.
    */
    NEW,

    /**
    * Thread state for a runnable thread.  A thread in the runnable
    * state is executing in the Java virtual machine but it may
    * be waiting for other resources from the operating system
    * such as processor.
    */
    RUNNABLE,

    /**
    * Thread state for a thread blocked waiting for a monitor lock.
    * A thread in the blocked state is waiting for a monitor lock
    * to enter a synchronized block/method or
    * reenter a synchronized block/method after calling
    * {@link Object#wait() Object.wait}.
    */
    BLOCKED,

    /**
    * Thread state for a waiting thread.
    * A thread is in the waiting state due to calling one of the
    * following methods:
    * <ul>
    *   <li>{@link Object#wait() Object.wait} with no timeout</li>
    *   <li>{@link #join() Thread.join} with no timeout</li>
    *   <li>{@link LockSupport#park() LockSupport.park}</li>
    * </ul>
    *
    * <p>A thread in the waiting state is waiting for another thread to
    * perform a particular action.
    *
    * For example, a thread that has called <tt>Object.wait()</tt>
    * on an object is waiting for another thread to call
    * <tt>Object.notify()</tt> or <tt>Object.notifyAll()</tt> on
    * that object. A thread that has called <tt>Thread.join()</tt>
    * is waiting for a specified thread to terminate.
    */
    WAITING,

    /**
    * Thread state for a waiting thread with a specified waiting time.
    * A thread is in the timed waiting state due to calling one of
    * the following methods with a specified positive waiting time:
    * <ul>
    *   <li>{@link #sleep Thread.sleep}</li>
    *   <li>{@link Object#wait(long) Object.wait} with timeout</li>
    *   <li>{@link #join(long) Thread.join} with timeout</li>
    *   <li>{@link LockSupport#parkNanos LockSupport.parkNanos}</li>
    *   <li>{@link LockSupport#parkUntil LockSupport.parkUntil}</li>
    * </ul>
    */
    TIMED_WAITING,

    /**
    * Thread state for a terminated thread.
    * The thread has completed execution.
    */
    TERMINATED;
}
```

### 状态转换

<div align=center>

![1587977737466.png](..\images\1587977737466.png)

![1587983275233.png](..\images\1587983275233.png)


</div>

1. NEW --> RUNNABLE
   - 当调用 t.start() 方法时，由 NEW --> RUNNABLE
2. RUNNABLE <--> WAITING
   -  wait方式（需要synchronized(obj)获取锁）
      -  释放（obj.notify() ， obj.notifyAll() ， t.interrupt()）
         -  竞争锁成功，t 线程从 WAITING --> RUNNABLE
         -  竞争锁失败，t 线程从 WAITING --> BLOCKED  
   -  join方式（当前线程在t 线程对象的监视器上等待）
      -  释放：运行结束或调用当前线程的interrupt方法：WAITING --> RUNNABLE
   -  LockSupport.park方式
      -  调用unpark(目标线程)或interrupt方法：WAITING --> RUNNABLE
3. RUNNABLE <--> TIMED_WAITING
   -  wait(时间)方式（需要synchronized(obj)获取锁）
      -  释放（obj.notify() ， obj.notifyAll() ， t.interrupt()、或超时）
         -  竞争锁成功，t 线程从 TIMED_WAITING --> RUNNABLE
         -  竞争锁失败，t 线程从 TIMED_WAITING --> BLOCKED  
   -  join(时间)方式（当前线程在t 线程对象的监视器上等待）
      -  释放：运行结束或调用当前线程的interrupt方法、或超时：TIMED_WAITING --> RUNNABLE
   -  LockSupport.parkNanos(long nanos) 或 LockSupport.parkUntil(long millis)方式
      -  调用unpark(目标线程)或interrupt方法、或超时：TIMED_WAITING --> RUNNABLE
   -  sleep（时间）
      -  当前线程超时： TIMED_WAITING --> RUNNABLE
4. RUNNABLE <--> BLOCKED
   -  t 线程用 synchronized(obj) 获取了对象锁时如果竞争失败，从 RUNNABLE --> BLOCKED
   -  持 obj 锁线程的同步代码块执行完毕，会唤醒该对象上所有 BLOCKED 的线程重新竞争，如果其中 t 线程竞争成功，从 BLOCKED --> RUNNABLE ，其它失败的线程仍然 BLOCKED
5. RUNNABLE <--> TERMINATED
   -  当前线程所有代码运行完毕，进入 TERMINATED

### 状态说明

#### 特殊说明

1. 创建并启动线程后，不会立即进入就绪状态（**需要经过经过新建(New)、就绪（Runnable）、运行（Running）、阻塞 (Blocked)和死亡(Dead)5 种状态。**），而且线程的运行需要一些条件（比如内存资源，程序计数器、Java栈、本地方法栈都是线程私有的，所以需要为线程分配一定的内存空间），只有线程运行需要的所有条件满足了，才进入就绪状态。
2. 当线程进入就绪状态后，不代表立刻就能获取CPU执行时间，也许此时CPU正在执行其他的事情，因此它要等待。当得到CPU执行时间之后，线程便真正进入运行状态。
3. 线程在运行状态过程中，可能有多个原因导致当前线程不继续运行下去，比如用户主动让线程睡眠（睡眠一定的时间之后再重新执行）、用户主动让线程等待，或者被同步块给阻塞，此时就对应着多个状态：time waiting（睡眠或等待一定的事件）、waiting（等待被唤醒）、blocked（阻塞）。
4. 当由于突然中断或者子任务执行完毕，线程就会被消亡。

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
  <tr>
    <th class="tg-0lax">状态名称</th>
    <th class="tg-0lax">说明</th>
  </tr>
  <tr>
    <td class="tg-sjuo">NEW</td>
    <td class="tg-sjuo">
        初始状态，线程在被构建时的状态，如果Thread t = new MyThread();还没有调用start方法<br>
        此时仅由 JVM 为其分配内存，并初始化其成员变量的值<br>
    </td>
  </tr>
  <tr>
    <td class="tg-0lax">RUNNABLE</td>
    <td class="tg-0lax">
        运行状态，java线程将操作系统中的就绪状态和运行状态笼统地称为“运行中”。<br>
        <font color=red>ready</font>：当调用线程对象的start()方法（t.start();），线程即进入就绪状态。处于就绪状态的线程，只是说明此线程已经做好了准备，随时等待CPU调度执行，并不是说执行了t.start()此线程立即就会执行；此时Java 虚拟机会为其创建方法调用栈和程序计数器，等待调度运行<br>
        <font color=red>running</font>：当CPU开始调度处于就绪状态的线程时，此时线程才得以真正执行，即进入到运行状态（开始执行run方法）。注：就绪状态是进入到运行状态的唯一入口，也就是说，线程要想进入运行状态执行，首先必须处于就绪状态中；<br>
    </td>
  </tr>
  <tr>
    <td class="tg-sjuo">BLOCKED</td>
    <td class="tg-sjuo">
        阻塞状态，表示线程阻塞于锁，释放出cpu使用权，让出了cpu timeslice，暂时停止运行<br>
        此时进入阻塞状态，直到其进入到就绪状态，才有机会再次被CPU调用以进入到运行状态。<br>
        根据阻塞产生的原因不同，阻塞状态又可以分为三种：<br>
        1.等待阻塞（o.wait->等待对列）：运行状态中的线程执行wait()方法，JVM 会把该线程放入等待队列(waitting queue) 中，使本线程进入到等待阻塞状态；<br>
        2.同步阻塞(lock->锁池) ：运行(running)的线程在获取对象的同步锁时，若该同步锁被别的线程占用（获取synchronized同步锁失败），则 JVM 会把该线程放入锁池(lock pool)中，进入同步阻塞状态；<br>
        3.其他阻塞(sleep/join)： 运行(running)的线程执行 Thread.sleep(long ms)或 t.join()方法，或者发出了 I/O 请求时， JVM 会把该线程置为阻塞状态。当 sleep()状态超时、join()等待线程终止或者超时、或者 I/O 处理完毕时，线程重新转入可运行(runnable)状态。<br>
    </td>
  </tr>
  
  <tr>
    <td class="tg-0lax">WAITING</td>
    <td class="tg-0lax">等待状态，表示线程进入等待状态，进入该状态表示当前线程需要等待其他线程做出一些特定的动作（通知或中断）</td>
  </tr>
  <tr>
    <td class="tg-sjuo">TIMED_WAITING</td>
    <td class="tg-sjuo">超时等待状态，改状态不同于waiting，它是可以在指定时间内自动返回的</td>
  </tr>
  <tr>
    <td class="tg-0lax">TERMINATED</td>
    <td class="tg-0lax">线程执行完了或者因异常退出了run()方法，该线程结束生命周期</td>
  </tr>
</table>

#### Java多线程的就绪、运行和死亡状态（DEAD）

1. 就绪状态转换为运行状态：当此线程得到处理器资源；
2. 运行状态转换为就绪状态：当此线程主动调用yield()方法或在运行过程中失去处理器资源。
3. 运行状态转换为死亡状态：当此线程线程执行体执行完毕或发生了异常。
   1. 正常结束 ：run()或 call()方法执行完成，线程正常结束。
   2. 异常结束：线程抛出一个未捕获的 Exception 或 Error。
   3. 调用stop：直接调用该线程的 stop()方法来结束该线程—该方法通常容易导致死锁，不推荐使用

> 此处需要特别注意的是：当调用线程的yield()方法时，线程从运行状态转换为就绪状态，但接下来CPU调度就绪状态中的哪个线程具有一定的随机性，因此，可能会出现A线程调用了yield()方法后，接下来CPU仍然调度了A线程的情况。
> 由于实际的业务需要，常常会遇到需要在特定时机终止某一线程的运行，使其进入到死亡状态。目前最通用的做法是设置一boolean型的变量，当条件满足时，使线程执行体快速执行完毕（**不在执行run方法**）。

## 状态分析-jvisualvm

### 参考代码（java并发编程艺术）

```java
package com.sunld;
import java.util.concurrent.TimeUnit;
/**
 * @Title: TestThreadState.java
 * @Package com.sunld
 * <p>Description:</p>
 * @author sunld
 * @version V1.0.0 
 * <p>CreateDate:2017年9月28日 下午5:14:27</p>
*/
public class TestThreadState {
	public static void main(String[] args) {
		new Thread(new TimeWaiting (), "TimeWaitingThread").start();
		new Thread(new Waiting(), "WaitingThread").start();
		// 使用两个Blocked线程，一个获取锁成功，另一个被阻塞
		new Thread(new Blocked(), "BlockedThread-1").start();
		new Thread(new Blocked(), "BlockedThread-2").start();
	}
	//该线程不断地进行睡眠
	static class TimeWaiting implements Runnable{
		@Override
		public void run() {
			SleepUtils.second(100);
		}
	}
	//该线程在Waiting.class实例上等待
	static class Waiting implements Runnable{
		@Override
		public void run() {
			while (true) {
				synchronized (Waiting.class) {
					try {
						Waiting.class.wait();
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	//该线程在Blocked.class实例上加锁后，不会释放该锁
	static class Blocked implements Runnable {
		@Override
		public void run() {
			synchronized (Blocked.class) {
				while (true) {
					SleepUtils.second(100);
				}
			}
		}
	}
}
class SleepUtils{
	public static final void second(long seconds) {
		try {
			TimeUnit.SECONDS.sleep(seconds);
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
}
```

### dump信息

```java
2017-09-28 17:26:47
Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.112-b15 mixed mode):
//BlockedThread-2线程获取到了Blocked.class的锁
"BlockedThread-2" #13 prio=5 os_prio=0 tid=0x000000001f268000 nid=0x3754 waiting on condition [0x000000002009f000]
   java.lang.Thread.State: TIMED_WAITING (sleeping)
//BlockedThread-1线程阻塞在获取Blocked.class示例的锁上
"BlockedThread-1" #12 prio=5 os_prio=0 tid=0x000000001f266800 nid=0x89c waiting for monitor entry [0x000000001ff9f000]
   java.lang.Thread.State: BLOCKED (on object monitor)
//WaitingThread线程在Waiting实例上等待
"WaitingThread" #11 prio=5 os_prio=0 tid=0x000000001f260800 nid=0x4d08 in Object.wait() [0x000000001fe9f000]
   java.lang.Thread.State: WAITING (on object monitor)
//TimeWaitingThread线程处于超时等待
"TimeWaitingThread" #10 prio=5 os_prio=0 tid=0x000000001f25f000 nid=0x42ac waiting on condition [0x000000001fd9e000]
   java.lang.Thread.State: TIMED_WAITING (sleeping)
```

## 参考

1. 《Java并发编程的艺术》
2. [Java中的多线程你只要看这一篇就够了](https://www.cnblogs.com/wxd0108/p/5479442.html)
3. [线程](https://baike.baidu.com/item/%E7%BA%BF%E7%A8%8B/103101?fr=aladdin)
4. [Java总结篇系列：Java多线程（一）](https://www.cnblogs.com/lwbqqyumidi/p/3804883.html)
5. 《java并发编程实战》
6. [多线程01：《疯狂Java讲义》学习笔记——线程概述](https://blog.csdn.net/hanhaiyinheguxing/article/details/51366541)
7. [java并发编程---如何创建线程以及Thread类的使用](https://blog.csdn.net/hla199106/article/details/47840505)
8. [Java并发编程：Thread类的使用](https://www.cnblogs.com/dolphin0520/p/3920357.html)
9. [Java中断机制](https://www.cnblogs.com/loveer/p/11518402.html)
10. [JAVA多线程之中断机制(如何处理中断？)](https://www.cnblogs.com/hapjin/p/5450779.html)
