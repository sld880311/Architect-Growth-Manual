<!-- TOC -->

- [线程的优先级](#%e7%ba%bf%e7%a8%8b%e7%9a%84%e4%bc%98%e5%85%88%e7%ba%a7)
  - [Java中的设置方式setPriority(int)](#java%e4%b8%ad%e7%9a%84%e8%ae%be%e7%bd%ae%e6%96%b9%e5%bc%8fsetpriorityint)
  - [示例（java并发编程艺术）](#%e7%a4%ba%e4%be%8bjava%e5%b9%b6%e5%8f%91%e7%bc%96%e7%a8%8b%e8%89%ba%e6%9c%af)

<!-- /TOC -->
# 线程的优先级

现代操作系统基本采用时分的形式调度运行的线程，操作系统会分出一个个时间片，线程会分配到若干时间片，当线程的时间片用完了就会发生线程调度，并等待着下次分配。线程分配到的时间片多少也就决定了线程使用处理器资源的多少，而线程优先级就是决定线程需要多或者少分配一些处理器资源的线程属性。优先级高的线程分配时间片的数量要多于优先级低的线程。

## Java中的设置方式setPriority(int)

```java
// 属性定义
private int            priority;

/**
* The minimum priority that a thread can have.
*/
public final static int MIN_PRIORITY = 1;

/**
* The default priority that is assigned to a thread.
*/
public final static int NORM_PRIORITY = 5;

/**
* The maximum priority that a thread can have.
*/
public final static int MAX_PRIORITY = 10;


/**
* Changes the priority of this thread.
* <p>
* First the <code>checkAccess</code> method of this thread is called
* with no arguments. This may result in throwing a
* <code>SecurityException</code>.
* <p>
* Otherwise, the priority of this thread is set to the smaller of
* the specified <code>newPriority</code> and the maximum permitted
* priority of the thread's thread group.
*
* @param newPriority priority to set this thread to
* @exception  IllegalArgumentException  If the priority is not in the
*               range <code>MIN_PRIORITY</code> to
*               <code>MAX_PRIORITY</code>.
* @exception  SecurityException  if the current thread cannot modify
*               this thread.
* @see        #getPriority
* @see        #checkAccess()
* @see        #getThreadGroup()
* @see        #MAX_PRIORITY
* @see        #MIN_PRIORITY
* @see        ThreadGroup#getMaxPriority()
*/
public final void setPriority(int newPriority) {
    ThreadGroup g;
    checkAccess();
    if (newPriority > MAX_PRIORITY || newPriority < MIN_PRIORITY) {
        throw new IllegalArgumentException();
    }
    if((g = getThreadGroup()) != null) {
        if (newPriority > g.getMaxPriority()) {
            newPriority = g.getMaxPriority();
        }
        setPriority0(priority = newPriority);
    }
}
```

## 示例（java并发编程艺术）

设置线程优先级时，针对频繁阻塞（休眠或者I/O操作）的线程需要设置较高优先级，而偏重计算（需要较多CPU时间或者偏运算）的线程则设置较低的优先级，确保处理器不会被独占。在不同的JVM以及操作系统上，线程规划会存在差异，有些操作系统甚至会忽略对线程优先级的设定。

```java
package com.sunld;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Title: Priority.java
 * @Package com.sunld
 * <p>Description:</p>
 * @author sunld
 * @version V1.0.0 
 * <p>CreateDate:2017年10月9日 下午2:26:33</p>
*/

public class Priority {
	private static volatile boolean notStart = true;
	private static volatile boolean notEnd = true;
	
	public static void main(String[] args) throws InterruptedException {
		List<Job> jobs = new ArrayList<Job>();
		for(int i = 0; i < 10; i++) {
			int priority = i < 5 ? Thread.MIN_PRIORITY : Thread.MAX_PRIORITY;
			Job job = new Job(priority);
			jobs.add(job);
			Thread thread = new Thread(job, "Thread:" + i);
			thread.setPriority(priority);
			thread.start();
		}
		notStart = false;
		TimeUnit.SECONDS.sleep(10);
		notEnd = false;
		for (Job job : jobs) {
			System.out.println("Job Priority : " + job.priority + ","
					+ "Count : " + job.jobCount);
		}
	}
	static class Job implements Runnable{
		private int priority;
		private long jobCount;
		public Job(int priority) {
			this.priority = priority;
		}
		@Override
		public void run() {
			while(notStart) {
				Thread.yield();
			}
			while(notEnd) {
				Thread.yield();
				jobCount++;
			}
		}
	}
}
```

运行结果：

```java
Job Priority : 1,Count : 1034791
Job Priority : 1,Count : 1034727
Job Priority : 1,Count : 1033945
Job Priority : 1,Count : 1034445
Job Priority : 1,Count : 1034565
Job Priority : 10,Count : 4388204
Job Priority : 10,Count : 4374055
Job Priority : 10,Count : 4375073
Job Priority : 10,Count : 4350174
Job Priority : 10,Count : 4393224
```

> 结论：
> > 测试环境是win10+JDK1.8，级别越高执行的次数越多。但是**线程优先级不能作为程序正确性的依赖，因为操作系统可以完全不用理会Java线程对于优先级的设定。**
