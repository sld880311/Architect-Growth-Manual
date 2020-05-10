<!-- TOC -->

- [线程常用方法说明](#%e7%ba%bf%e7%a8%8b%e5%b8%b8%e7%94%a8%e6%96%b9%e6%b3%95%e8%af%b4%e6%98%8e)

<!-- /TOC -->
# 线程常用方法说明


1.1.1	线程基本方法
线程相关的基本方法有 wait，notify，notifyAll，sleep，join，yield 等。 
<div align=center>

![1589109533903.png](..\images\1589109533903.png)

</div>


1.1.1.1	线程等待（wait） 
调用该方法的线程进入 WAITING 状态，只有等待另外线程的通知或被中断才会返回，需要注意的是调用 wait()方法后，会释放对象的锁。因此，wait 方法一般用在同步方法或同步代码块中。 
1.1.1.2	线程睡眠（sleep） 
sleep 导致当前线程休眠，与 wait 方法不同的是 sleep 不会释放当前占有的锁,sleep(long)会导致线程进入 TIMED-WATING 状态，而 wait()方法会导致当前线程进入 WATING 状态 
1.1.1.3	线程让步（yield） 
yield 会使当前线程让出 CPU 执行时间片，与其他线程一起重新竞争 CPU 时间片。一般情况下，优先级高的线程有更大的可能性成功竞争得到 CPU 时间片，但这又不是绝对的，有的操作系统对线程优先级并不敏感。 
1.1.1.4	线程中断（interrupt） 
中断一个线程，其本意是给这个线程一个通知信号，会影响这个线程内部的一个中断标识位。这个线程本身并不会因此而改变状态(如阻塞，终止等)。 
1.	调用 interrupt()方法并不会中断一个正在运行的线程。也就是说处于 Running 状态的线程并不会因为被中断而被终止，仅仅改变了内部维护的中断标识位而已。 
2.	若调用 sleep()而使线程处于 TIMED-WATING 状态，这时调用 interrupt()方法，会抛出
InterruptedException,从而使线程提前结束 TIMED-WATING 状态。 
3.	许多声明抛出InterruptedException 的方法(如 Thread.sleep(long mills 方法))，抛出异
常前，都会清除中断标识位，所以抛出异常后，调用 isInterrupted()方法将会返回 false。 
4.	中断状态是线程固有的一个标识位，可以通过此标识位安全的终止线程。比如,你想终止一个线程thread的时候，可以调用thread.interrupt()方法，在线程的run方法内部可以根据 thread.isInterrupted()的值来优雅的终止线程。 
1.1.1.5	Join 等待其他线程终止 
join() 方法，等待其他线程终止，在当前线程中调用一个线程的 join() 方法，则当前线程转为阻塞状态，回到另一个线程结束，当前线程再由阻塞状态变为就绪状态，等待 cpu 的宠幸。 
1.1.1.5.1	为什么要用 join()方法 
很多情况下，主线程生成并启动了子线程，需要用到子线程返回的结果，也就是需要主线程需要在子线程结束后再结束，这时候就要用到 join() 方法。 

package com.sunld;

import java.util.concurrent.TimeUnit;

public class Join {

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

1.1.1.6	线程唤醒（notify） 
Object 类中的 notify() 方法，唤醒在此对象监视器上等待的单个线程，如果所有线程都在此对象上等待，则会选择唤醒其中一个线程，选择是任意的，并在对实现做出决定时发生，线程通过调用其中一个 wait() 方法，在对象的监视器上等待，直到当前的线程放弃此对象上的锁定，才能继续执行被唤醒的线程，被唤醒的线程将以常规方式与在该对象上主动同步的其他所有线程进行竞争。类似的方法还有 notifyAll() ，唤醒再次监视器上等待的所有线程。 
1.1.1.7	其他方法
1.	isAlive()： 判断一个线程是否存活。  
2.	activeCount()： 程序中活跃的线程数。  
3.	enumerate()： 枚举程序中的线程。  
4.	currentThread()： 得到当前线程。  
5.	isDaemon()： 一个线程是否为守护线程。  
6.	setDaemon()： 设置一个线程为守护线程。(用户线程和守护线程的区别在于，是否等待主线程依赖于主线程结束而结束)  
7.	setName()： 为线程设置一个名称。  
8.	setPriority()： 设置一个线程的优先级。  
9.	getPriority():：获得一个线程的优先级。 

1.1.1.8	sleep 与 wait 区别 
1.	对于 sleep()方法，我们首先要知道该方法是属于 Thread 类中的。而 wait()方法，则是属于
Object 类中的。 
2.	sleep()方法导致了程序暂停执行指定的时间，让出 cpu 该其他线程，但是他的监控状态依然
保持者，当指定的时间到了又会自动恢复运行状态。 
3.	在调用 sleep()方法的过程中，线程不会释放对象锁。 
4.	而当调用 wait()方法的时候，线程会放弃对象锁，进入等待此对象的等待锁定池，只有针对此对象调用 notify()方法后本线程才进入对象锁定池准备获取对象锁进入运行状态。 
 
1.1.1.9	start 与 run 区别 
1.	start（）方法来启动线程，真正实现了多线程运行。这时无需等待 run 方法体代码执行完毕，可以直接继续执行下面的代码。 
2.	通过调用 Thread 类的 start()方法来启动一个线程， 这时此线程是处于就绪状态， 并没有运行。 
3.	方法 run()称为线程体，它包含了要执行的这个线程的内容，线程就进入了运行状态，开始运行 run 函数当中的代码。 Run 方法运行结束， 此线程终止。然后 CPU 再调度其它线程。 


