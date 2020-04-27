<!-- TOC -->

- [线程中断](#线程中断)
    - [正常结束(执行完成)](#正常结束执行完成)
    - [使用退出标志退出线程](#使用退出标志退出线程)
    - [interrupt方法结束线程](#interrupt方法结束线程)
        - [Thread中断方法说明](#thread中断方法说明)
        - [类库中的中断](#类库中的中断)
        - [中断处理](#中断处理)
        - [中断响应](#中断响应)
        - [中断示例](#中断示例)
    - [stop 方法终止线程（线程不安全）](#stop-方法终止线程线程不安全)
    - [中断的使用场景](#中断的使用场景)
    - [参考](#参考)

<!-- /TOC -->

# 线程中断

## 正常结束(执行完成)

## 使用退出标志退出线程

一般 run()方法执行完，线程就会正常结束，然而，常常有些线程是**伺服线程**。它们需要长时间的运行，只有在外部某些条件满足的情况下，才能关闭这些线程。使用一个变量来控制循环，例如：最直接的方法就是设一个boolean类型的标志，并通过设置这个标志为true或false 来控制while 循环是否退出，代码示例：

```java
package com.sunld.thread;
public class ThreadSafe extends Thread{
	public volatile boolean exit = false;
	public void run() {
		while (!exit){
			//do something
		} 
	}  
}
```

定义了一个退出标志 exit，当 exit 为 true 时，while 循环退出，exit 的默认值为 false.在定义 exit 时，**使用了一个 Java 关键字 volatile，这个关键字的目的是使 exit 同步**，也就是说在同一时刻只能由一个线程来修改 exit 的值。

## interrupt方法结束线程

### Thread中断方法说明

```java
/**
* 1. 测试当前线程是否被中断
* 2. 执行该方法之后会清楚当前线程的重点状态。
* 3. 如果中断一个已经不活跃的线程，则返回false
*
* @return  <code>true</code> if the current thread has been interrupted;
*          <code>false</code> otherwise.
* @see #isInterrupted()
* @revised 6.0
*/
public static boolean interrupted() {
    return currentThread().isInterrupted(true);
}



/**
* 1. 测试线程是否被中断，该方法不会清除中断状态
*
* 2. 如果中断一个已经不活跃的线程，则返回false
*
* @return  <code>true</code> if this thread has been interrupted;
*          <code>false</code> otherwise.
* @see     #interrupted()
* @revised 6.0
*/
public boolean isInterrupted() {
    return isInterrupted(false);
}

/**
* Tests if some Thread has been interrupted.  The interrupted state
* is reset or not based on the value of ClearInterrupted that is
* passed.
*/
private native boolean isInterrupted(boolean ClearInterrupted);

/**
* Interrupts this thread.
*
* <p> Unless the current thread is interrupting itself, which is
* always permitted, the {@link #checkAccess() checkAccess} method
* of this thread is invoked, which may cause a {@link
* SecurityException} to be thrown.
*
* <p> If this thread is blocked in an invocation of the {@link
* Object#wait() wait()}, {@link Object#wait(long) wait(long)}, or {@link
* Object#wait(long, int) wait(long, int)} methods of the {@link Object}
* class, or of the {@link #join()}, {@link #join(long)}, {@link
* #join(long, int)}, {@link #sleep(long)}, or {@link #sleep(long, int)},
* methods of this class, then its interrupt status will be cleared and it
* will receive an {@link InterruptedException}.
* 
*
* <p> If this thread is blocked in an I/O operation upon an {@link
* java.nio.channels.InterruptibleChannel InterruptibleChannel}
* then the channel will be closed, the thread's interrupt
* status will be set, and the thread will receive a {@link
* java.nio.channels.ClosedByInterruptException}.
*
* <p> If this thread is blocked in a {@link java.nio.channels.Selector}
* then the thread's interrupt status will be set and it will return
* immediately from the selection operation, possibly with a non-zero
* value, just as if the selector's {@link
* java.nio.channels.Selector#wakeup wakeup} method were invoked.
*
* <p> If none of the previous conditions hold then this thread's interrupt
* status will be set. </p>
*
* <p> Interrupting a thread that is not alive need not have any effect.
*
* @throws  SecurityException
*          if the current thread cannot modify this thread
*
* @revised 6.0
* @spec JSR-51
*/
public void interrupt() {
    if (this != Thread.currentThread())
        checkAccess();

    synchronized (blockerLock) {
        Interruptible b = blocker;
        if (b != null) {
            interrupt0();           // Just to set the interrupt flag
            b.interrupt(this);
            return;
        }
    }
    interrupt0();
}
```

### 类库中的中断

1. FutureTask中的cancel方法，如果传入的参数为true，它将会在正在运行异步任务的线程上调用interrupt方法，如果正在执行的异步任务中的代码没有对中断做出响应，那么cancel方法中的参数将不会起到什么效果；
2. ThreadPoolExecutor中的shutdownNow方法会遍历线程池中的工作线程并调用线程的interrupt方法来中断线程，如果工作线程中正在执行的任务没有对中断做出响应，任务将一直执行直到正常结束。

### 中断处理

1. interrupt()方法仅仅是在当前线程中打了一个停止的标识将中断标志修改为true，并没有真正的停止线程：如果在此基础上进入堵塞状态（sleep(),wait(),join()）,马上就会抛出一个InterruptedException，且中断标志被清除，重新设置为false，线程退出。
2. 如果遇到的是可中断的阻塞方法抛出InterruptedException：可以继续向上层抛出该异常。若有时候不太方便在方法上抛出InterruptedException，比如要实现的某个接口中的方法签名上没有throws InterruptedException，这时就可以捕获可中断方法的InterruptedException并且恢复异常（重新设置中断）。
3. 阻塞中的那个方法抛出这个异常，通过代码捕获该异常，然后 break 跳出循环状态，从而让我们有机会结束这个线程的执行。通常很多人认为只要调用 interrupt 方法线程就会结束，实际上是错的， 一定要先捕获 InterruptedException 异常之后通过 break 来跳出循环，才能正常结束 run 方法。
4. 线程未处于阻塞状态：使用 isInterrupted()判断线程的中断标志来退出循环。当使用interrupt()方法时，中断标志就会置 true，和使用自定义的标志来控制循环是一样的道理。

### 中断响应

有些程序可能一检测到中断就立马将线程终止，有些可能是退出当前执行的任务，继续执行下一个任务……作为一种协作机制，这要与中断方协商好，当调用interrupt会发生些什么都是事先知道的，如做一些事务回滚操作，一些清理工作，一些补偿操作等。若不确定调用某个线程的interrupt后该线程会做出什么样的响应，那就不应当中断该线程。

**程序应该对线程中断作出恰当的响应。只有正确响应中断才能更好的结束线程。**

<div align=center>

![1587984426042.png](..\images\1587984426042.png)

</div>

### 中断示例

```java
package com.sunld.thread;
public class ThreadSafe2 extends Thread {
	public void run() {
		while (!isInterrupted()) { // 非阻塞过程中通过判断中断标志来退出
			try {
				Thread.sleep(5 * 1000);// 阻塞过程捕获中断异常来退出
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;// 捕获到异常之后，执行 break 跳出循环
			}
		}
	}
}
```

## stop 方法终止线程（线程不安全）

程序中可以直接使用 thread.stop()来强行终止线程，但是 stop 方法是很危险的，就象突然关闭计算机电源，而不是按正常程序关机一样，可能会产生不可预料的结果，不安全主要是： thread.stop()调用之后，创建子线程的线程就会抛出 ThreadDeatherror 的错误，并且会释放子线程所持有的所有锁。一般任何进行加锁的代码块，都是为了保护数据的一致性，如果在调用**thread.stop()后导致了该线程所持有的所有锁的突然释放(不可控制)，**那么被保护数据就有可能呈现不一致性，其他线程在使用这些被破坏的数据时，有可能导致一些很奇怪的应用程序错误。因此，并不推荐使用 stop 方法来终止线程。

## 中断的使用场景

1. 点击某个桌面应用中的取消按钮时；
2. 某个操作超过了一定的执行时间限制需要中止时；
3. 多个线程做相同的事情，只要一个线程成功其它线程都可以取消时；
4. 一组线程中的一个或多个出现错误导致整组都无法继续时；
5. 当一个应用或服务需要停止时。

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
