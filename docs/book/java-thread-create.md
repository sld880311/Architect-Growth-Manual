<!-- TOC -->

- [线程的创建](#线程的创建)
	- [通过Thread构建](#通过thread构建)
		- [Thread定义](#thread定义)
		- [Thread原理（init方法）](#thread原理init方法)
		- [示例](#示例)
	- [通过Runnable构建](#通过runnable构建)
	- [通过ExecutorService、Callable、Future构建](#通过executorservicecallablefuture构建)
		- [Callable JDK定义（泛型返回值V）](#callable-jdk定义泛型返回值v)
		- [Callable在JDK中的定义使用(ExecutorService)](#callable在jdk中的定义使用executorservice)
		- [Future说明](#future说明)
		- [示例](#示例-1)
	- [基于线程池构建](#基于线程池构建)
	- [启动线程](#启动线程)
		- [start源码](#start源码)
		- [线程启动的注意事项](#线程启动的注意事项)
	- [其他](#其他)
		- [Thread与Runnable](#thread与runnable)
			- [关系](#关系)
			- [区别](#区别)
	- [参考](#参考)

<!-- /TOC -->

# 线程的创建

## 通过Thread构建

Thread 类本质上是实现了 Runnable 接口的一个实例，代表一个线程的实例。启动线程的唯一方法就是通过 Thread 类的 start()实例方法。start()方法是一个 native 方法，它将启动一个新线程。

### Thread定义

```java
/**
 * A <i>thread</i> is a thread of execution in a program. The Java
 * Virtual Machine allows an application to have multiple threads of
 * execution running concurrently.
 * <p>
 * Every thread has a priority. Threads with higher priority are
 * executed in preference to threads with lower priority. Each thread
 * may or may not also be marked as a daemon. When code running in
 * some thread creates a new <code>Thread</code> object, the new
 * thread has its priority initially set equal to the priority of the
 * creating thread, and is a daemon thread if and only if the
 * creating thread is a daemon.
 * <p>
 * When a Java Virtual Machine starts up, there is usually a single
 * non-daemon thread (which typically calls the method named
 * <code>main</code> of some designated class). The Java Virtual
 * Machine continues to execute threads until either of the following
 * occurs:
 * <ul>
 * <li>The <code>exit</code> method of class <code>Runtime</code> has been
 *     called and the security manager has permitted the exit operation
 *     to take place.
 * <li>All threads that are not daemon threads have died, either by
 *     returning from the call to the <code>run</code> method or by
 *     throwing an exception that propagates beyond the <code>run</code>
 *     method.
 * </ul>
 * <p>
 * There are two ways to create a new thread of execution. One is to
 * declare a class to be a subclass of <code>Thread</code>. This
 * subclass should override the <code>run</code> method of class
 * <code>Thread</code>. An instance of the subclass can then be
 * allocated and started. For example, a thread that computes primes
 * larger than a stated value could be written as follows:
 * <hr><blockquote><pre>
 *     class PrimeThread extends Thread {
 *         long minPrime;
 *         PrimeThread(long minPrime) {
 *             this.minPrime = minPrime;
 *         }
 *
 *         public void run() {
 *             // compute primes larger than minPrime
 *             &nbsp;.&nbsp;.&nbsp;.
 *         }
 *     }
 * </pre></blockquote><hr>
 * <p>
 * The following code would then create a thread and start it running:
 * <blockquote><pre>
 *     PrimeThread p = new PrimeThread(143);
 *     p.start();
 * </pre></blockquote>
 * <p>
 * The other way to create a thread is to declare a class that
 * implements the <code>Runnable</code> interface. That class then
 * implements the <code>run</code> method. An instance of the class can
 * then be allocated, passed as an argument when creating
 * <code>Thread</code>, and started. The same example in this other
 * style looks like the following:
 * <hr><blockquote><pre>
 *     class PrimeRun implements Runnable {
 *         long minPrime;
 *         PrimeRun(long minPrime) {
 *             this.minPrime = minPrime;
 *         }
 *
 *         public void run() {
 *             // compute primes larger than minPrime
 *             &nbsp;.&nbsp;.&nbsp;.
 *         }
 *     }
 * </pre></blockquote><hr>
 * <p>
 * The following code would then create a thread and start it running:
 * <blockquote><pre>
 *     PrimeRun p = new PrimeRun(143);
 *     new Thread(p).start();
 * </pre></blockquote>
 * <p>
 * Every thread has a name for identification purposes. More than
 * one thread may have the same name. If a name is not specified when
 * a thread is created, a new name is generated for it.
 * <p>
 * Unless otherwise noted, passing a {@code null} argument to a constructor
 * or method in this class will cause a {@link NullPointerException} to be
 * thrown.
 *
 * @author  unascribed
 * @see     Runnable
 * @see     Runtime#exit(int)
 * @see     #run()
 * @see     #stop()
 * @since   JDK1.0
 */
public class Thread implements Runnable {
}
```

通过JDK官方定义可以得出以下结论：

1. 线程是程序中的执行线程，java虚拟机允许应用程序并发的运行多个线程。
2. 每个线程都有一个优先级，高优先级线程的执行优先于低优先级线程。每个线程都可以或不可以标记为一个守护程序。当某个线程中运行的代码创建一个新 Thread 对象时，该新线程的初始优先级被设定为创建线程的优先级，并且当且仅当创建线程是守护线程时，新线程才是守护程序。
3. 当 Java 虚拟机启动时，通常都会有单个非守护线程（它通常会调用某个指定类的 main 方法）。Java 虚拟机会继续执行线程，直到下列任一情况出现时为止：
   1. 调用了 Runtime 类的 exit 方法，并且安全管理器允许退出操作发生。
   2. 非守护线程的所有线程都已停止运行，无论是通过从对 run 方法的调用中返回，还是通过抛出一个传播到 run 方法之外的异常。

### Thread原理（init方法）

在运行线程之前首先要构造一个线程对象，线程对象在构造的时候需要提供线程所需要的属性，如**线程所属的线程组、线程优先级、是否是Daemon线程等信息**。在new Thread时会调用以下方法进行实例化Thread对象。  
初始化代码如下：

```java
/**
* Initializes a Thread.
*
* @param g the Thread group
* @param target the object whose run() method gets called
* @param name the name of the new Thread
* @param stackSize the desired stack size for the new thread, or
*        zero to indicate that this parameter is to be ignored.
* @param acc the AccessControlContext to inherit, or
*            AccessController.getContext() if null
* @param inheritThreadLocals if {@code true}, inherit initial values for
*            inheritable thread-locals from the constructing thread
*/
private void init(ThreadGroup g, Runnable target, String name,
					long stackSize, AccessControlContext acc,
					boolean inheritThreadLocals) {
	if (name == null) {
		throw new NullPointerException("name cannot be null");
	}

	this.name = name;
	//当前线程作为该线程的父线程
	Thread parent = currentThread();
	SecurityManager security = System.getSecurityManager();
	//线程组的获取：如果传入的参数为空首先获取系统默认的安全组，如果为空获取父线程的安全组
	if (g == null) {
		/* Determine if it's an applet or not */

		/* If there is a security manager, ask the security manager
			what to do. */
		if (security != null) {
			g = security.getThreadGroup();
		}

		/* If the security doesn't have a strong opinion of the matter
			use the parent thread group. */
		if (g == null) {
			g = parent.getThreadGroup();
		}
	}

	/* checkAccess regardless of whether or not threadgroup is
		explicitly passed in. */
	g.checkAccess();

	/*
		* Do we have the required permissions?
		*/
	if (security != null) {
		if (isCCLOverridden(getClass())) {
			security.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
		}
	}

	g.addUnstarted();

	this.group = g;
	//设置daemon 、priority 属性为父线程对应的值
	this.daemon = parent.isDaemon();
	this.priority = parent.getPriority();
	if (security == null || isCCLOverridden(parent.getClass()))
		this.contextClassLoader = parent.getContextClassLoader();
	else
		this.contextClassLoader = parent.contextClassLoader;
	this.inheritedAccessControlContext =
			acc != null ? acc : AccessController.getContext();
	this.target = target;
	setPriority(priority);
	//将父线程的InheritableThreadLocal复制过来
	if (inheritThreadLocals && parent.inheritableThreadLocals != null)
		this.inheritableThreadLocals =
			ThreadLocal.createInheritedMap(parent.inheritableThreadLocals);
	/* Stash the specified stack size in case the VM cares */
	this.stackSize = stackSize;

	/* Set thread ID */
	//生成线程id（一个long型的字段threadSeqNumber）
	tid = nextThreadID();
}
```

> 通过代码总结如下：
> 一个新构建的Thread对象（new Thread()）,是由其父线程（当前线程）进行空间分配，而子线程继承了父线程的Daemon、优先级和加载资源的contextClassLoader，以及可继承的ThreadLocal，同时会为子线程分配一个线程id。一个可以运行的线程对象完成初始化工作，并且在堆内存中等待运行。

### 示例

```java
package com.sunld.thread;

/**
 * @author : sunliaodong
 * @version : V1.0.0
 * @description: 基于Thread实现线程的创建
 * @date : 2020/5/27 18:50
 */
public class CreateThreadByThread extends Thread{
    @Override
    public void run() {
        // 需要实现的业务
        System.out.println("CreateThreadByThread.run() by extends Thread");
    }
    public static void main(String[] args) {
        // 多态创建线程
        Thread o = new CreateThreadByThread();
        // 启动线程
        o.start();
    }
}
// CreateThreadByThread.run() by extends Thread
```

1. 通过Thread源码发现（Thread implements Runnable）发现thread其实也是一个实现了runnable接口的一个实例，它代表一个线程的实例，并且，启动线程的唯一方法就是通过Thread类的start()实例方法。start()方法是一个native方法，它将启动一个新线程，并执行run()方法。这种方式实现多线程很简单，通过自己的类直接extend Thread，并复写run()方法，就可以启动新线程并执行自己定义的run()方法。
2. 其中run()方法的方法体代表了线程需要完成的任务，称之为线程执行体。当创建此线程类对象时一个新的线程得以创建，并进入到线程新建状态。通过调用线程对象引用的start()方法，使得该线程进入到就绪状态，此时此线程并不一定会马上得以执行，这取决于CPU调度时机。

## 通过Runnable构建

如果自己的类已经 extends 另一个类，就无法直接 extends Thread，此时，可以实现一个 Runnable 接口。

**把【线程】和【任务】（要执行的代码）分开**  

1. Thread 代表线程
2. Runnable 可运行的任务（线程要执行的代码）

```java
package com.sunld.thread;

/**
 * @author : sunliaodong
 * @version : V1.0.0
 * @description: 通过Runnable创建线程，实现业务与线程的解耦
 * @date : 2020/5/27 18:55
 */
public class CreateThreadByRunnable implements Runnable{
    @Override
    public void run() {
        // 实际执行的业务
        System.out.println("====CreateThreadByRunnable=====Runnable====");
    }
    public static void main(String[] args) {
        // 实例化线程
        Thread t = new Thread(new CreateThreadByRunnable());
        // 启动线程
        t.start();

        // JDK1.8之后的实现
        Runnable task2 = () -> System.out.println("task2 jdk8");
        new Thread(task2, "task2").start();
    }
}
//====CreateThreadByRunnable=====Runnable====
//task2 jdk8
```

## 通过ExecutorService、Callable、Future构建

**解决Thread和Runnable无法返回值的问题（可以通过共享变量实现）。** 有返回值的任务必须实现 Callable 接口，类似的，无返回值的任务必须 Runnable 接口。执行Callable 任务后，可以获取一个 Future 的对象，在该对象上调用 get 就可以获取到 Callable 任务返回的 Object 了，再结合线程池接口 ExecutorService 就可以实现传说中有返回结果的多线程了。

### Callable JDK定义（泛型返回值V）

```java
package java.util.concurrent;

/**
 * A task that returns a result and may throw an exception.
 * Implementors define a single method with no arguments called
 * {@code call}.
 *
 * <p>The {@code Callable} interface is similar to {@link
 * java.lang.Runnable}, in that both are designed for classes whose
 * instances are potentially executed by another thread.  A
 * {@code Runnable}, however, does not return a result and cannot
 * throw a checked exception.
 *
 * <p>The {@link Executors} class contains utility methods to
 * convert from other common forms to {@code Callable} classes.
 *
 * @see Executor
 * @since 1.5
 * @author Doug Lea
 * @param <V> the result type of method {@code call}
 */
@FunctionalInterface
public interface Callable<V> {
    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    V call() throws Exception;
}
```

### Callable在JDK中的定义使用(ExecutorService)

```java
<T> Future<T> submit(Callable<T> task);
<T> Future<T> submit(Runnable task, T result);
Future<?> submit(Runnable task);
```

### Future说明

《[Future与FutureTask（任务执行结果处理）](book/java-future-futuretask.md)》

### 示例

```java
package com.sunld.thread;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
public class ExecutorsThread {
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		int taskSize = 10;
		// 创建线程池
		ExecutorService pool = Executors.newFixedThreadPool(taskSize);
		// 创建有返回值的任务
		List<Future<String>> list = new ArrayList<>();
		for(int i = 0; i < taskSize; i++) {
			list.add(pool.submit(new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "ddd:111  " + Thread.currentThread().getName();
				}
			}));
		}
		//关闭线程池
		pool.shutdown();
		// 获取结果
		for(Future<String> f : list) {
			System.out.println(f.get());
		}
	}
}
```

```java
package com.sunld.thread;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author : sunliaodong
 * @version : V1.0.0
 * @description: TODO
 * @date : 2020/5/27 19:31
 */
public class CreateThreadByFuture {

    public static void main(String[] args) {

//      创建任务对象
        FutureTask<Integer> thread = new FutureTask<Integer>(() -> {
            System.out.println("FutureTask====");
            return 100;
        });

//      创建并且启动线程
        new Thread(thread).start();
        try {
            // 处理返回值信息
            System.out.println(thread.get() + "===============");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
```

## 基于线程池构建

线程和数据库连接这些资源都是非常宝贵的资源。那么每次需要的时候创建，不需要的时候销毁，是非常浪费资源的。那么我们就可以使用缓存的策略，也就是使用线程池。

```java
package com.sunld.thread;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class ExecutorsThread2 {
	public static void main(String[] args) {
		ExecutorService pool = Executors.newFixedThreadPool(10);
		while(true) {
			pool.execute(new Runnable() {
				@Override
				public void run() {
					System.out.println(Thread.currentThread().getName() + ": is running");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
}
```

## 启动线程

### start源码

```java
/**
* Causes this thread to begin execution; the Java Virtual Machine
* calls the <code>run</code> method of this thread.
* <p>
* The result is that two threads are running concurrently: the
* current thread (which returns from the call to the
* <code>start</code> method) and the other thread (which executes its
* <code>run</code> method).
* <p>
* It is never legal to start a thread more than once.
* In particular, a thread may not be restarted once it has completed
* execution.
*
* @exception  IllegalThreadStateException  if the thread was already
*               started.
* @see        #run()
* @see        #stop()
*/
public synchronized void start() {
	/**
	* This method is not invoked for the main method thread or "system"
	* group threads created/set up by the VM. Any new functionality added
	* to this method in the future may have to also be added to the VM.
	*
	* A zero status value corresponds to state "NEW".
	*/
	if (threadStatus != 0)
		throw new IllegalThreadStateException();

	/* Notify the group that this thread is about to be started
		* so that it can be added to the group's list of threads
		* and the group's unstarted count can be decremented. */
	group.add(this);

	boolean started = false;
	try {
		start0();
		started = true;
	} finally {
		try {
			if (!started) {
				group.threadStartFailed(this);
			}
		} catch (Throwable ignore) {
			/* do nothing. If start0 threw a Throwable then
				it will be passed up the call stack */
		}
	}
}

private native void start0();
```

通过源码得出以下结论：

1. 对象初始化完成之后，通过执行start方法来执行这个线程，并且java虚拟机会调用该线程的run方法执行线程的业务逻辑；
2. 调用start方法之后发现会同时有两个线程在执行：当前线程（parent线程【同步告知java虚拟机，只要线程规划器空闲，应立即启动调用start方法的线程】，从调用返回给start方法）和另一个线程（执行其run方法）。
3. 并且多次启动一个线程是非法的。特别是当线程已经结束执行后，不能再重新启动。 

### 线程启动的注意事项

无论何种方式，启动一个线程，就要给它一个名字！这对排错诊断 系统监控有帮助。否则诊断问题时，无法直观知道某个线程的用途。

<div align=center>

![1587983785160.png](..\images\1587983785160.png)

</div>

## 其他

### Thread与Runnable

#### 关系
Thread实现接口Runnable，并且实现了run方法，代码参考如下:

```java
public class Thread implements Runnable {
		//如果该线程是使用独立的 Runnable 运行对象构造的，则调用该 Runnable 对象的 run 方法；
		//否则，该方法不执行任何操作并返回。
		//Thread 的子类应该重写该方法。
	    /**
	     * If this thread was constructed using a separate
	     * <code>Runnable</code> run object, then that
	     * <code>Runnable</code> object's <code>run</code> method is called;
	     * otherwise, this method does nothing and returns.
	     * <p>
	     * Subclasses of <code>Thread</code> should override this method.
	     *
	     * @see     #start()
	     * @see     #stop()
	     * @see     #Thread(ThreadGroup, Runnable, String)
	     */
	    @Override
	    public void run() {
	        if (target != null) {
	            target.run();
	        }
	    }
	
}
```

#### 区别

 当执行到Thread类中的run()方法时，会首先判断target是否存在，存在则执行target中的run()方法，也就是实现了Runnable接口并重写了run()方法的类中的run()方法。当时如果该Runnable的子类是通过一个继承Thread的子类（该且重写了run方法），则真正执行的是Thread子类重写的run方法（由于多态的原因）。  

实现Runnable接口相比继承Thread类有如下优势：

1. 可以避免由于Java的单继承特性而带来的局限；
2. 增强程序的健壮性，代码能够被多个线程共享，代码与数据是独立的；
3. 适合多个相同程序代码的线程区处理同一资源的情况。
4. 用 Runnable 更容易与线程池等高级 API 配合
5. 用 Runnable 让任务类脱离了 Thread 继承体系，更灵活
6. Thread是把线程和任务合并在了一起，Runnable是把线程和任务分开了

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
11. [JAVA多线程实现的三种方式 ](https://blog.csdn.net/aboy123/article/details/38307539/)
