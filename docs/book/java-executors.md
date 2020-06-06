<!-- TOC -->

- [Executors](#executors)
  - [newCachedThreadPool](#newcachedthreadpool)
    - [源码分析](#源码分析)
    - [execute执行流程](#execute执行流程)
    - [注意事项](#注意事项)
    - [示例](#示例)
  - [newFixedThreadPool](#newfixedthreadpool)
    - [源码分析](#源码分析-1)
    - [execute执行过程](#execute执行过程)
    - [注意事项](#注意事项-1)
    - [示例](#示例-1)
  - [newScheduledThreadPool](#newscheduledthreadpool)
    - [源码分析](#源码分析-2)
  - [newSingleThreadExecutor](#newsinglethreadexecutor)
    - [源码分析](#源码分析-3)
    - [execute执行过程](#execute执行过程-1)
  - [参考](#参考)

<!-- /TOC -->

# Executors

## newCachedThreadPool

创建一个可根据需要创建新线程的线程池，但是在以前构造的线程可用时将重用它们。对于执行**很多短期异步任务**的程序而言，这些线程池通常可提高程序性能。调用 execute 将重用以前构造的线程（如果线程可用）。如果现有线程没有可用的，则创建一个新线程并添加到池中。终止并从缓存中移除那些已有 60 秒钟未被使用的线程。创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程。因此，长时间保持空闲的线程池不会使用任何资源。

### 源码分析

```java
/**
* Creates a thread pool that creates new threads as needed, but
* will reuse previously constructed threads when they are
* available.  These pools will typically improve the performance
* of programs that execute many short-lived asynchronous tasks.
* Calls to {@code execute} will reuse previously constructed
* threads if available. If no existing thread is available, a new
* thread will be created and added to the pool. Threads that have
* not been used for sixty seconds are terminated and removed from
* the cache. Thus, a pool that remains idle for long enough will
* not consume any resources. Note that pools with similar
* properties but different details (for example, timeout parameters)
* may be created using {@link ThreadPoolExecutor} constructors.
* - 为每一个任务创建一个线程，并且也可以重用已有的线程，无核心线程数量，
* - 超过60s的空闲线程将弃用，但是会受到系统实际内存的线程
*
* - 当有新任务到来，则插入到SynchronousQueue中，由于SynchronousQueue是同步队列，
* - 因此会在池中寻找可用线程来执行，若有可以线程则执行，若没有可用线程则创建一个线程来执行该任务；
* - 若池中线程空闲时间超过指定大小，则该线程会被销毁。
*
* - 适用：大小无界线程池，执行很多短期异步的小程序或者负载较轻的服务器；或负载较轻的服务器
*
* @return the newly created thread pool
*/
public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                    60L, TimeUnit.SECONDS,
                                    new SynchronousQueue<Runnable>());
}

/**
* Creates a thread pool that creates new threads as needed, but
* will reuse previously constructed threads when they are
* available, and uses the provided
* ThreadFactory to create new threads when needed.
* @param threadFactory the factory to use when creating new threads
* @return the newly created thread pool
* @throws NullPointerException if threadFactory is null
*/
public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                    60L, TimeUnit.SECONDS,
                                    new SynchronousQueue<Runnable>(),
                                    threadFactory);
}
```

### execute执行流程

<div align=center>

![1587885350899.png](..\images\1587885350899.png)

</div>

1. 执行`SynchronousQueue.offer（Runnabletask）`;如果当前maximumPool中有空闲线程正在执行`SynchronousQueue.poll（keepAliveTime，TimeUnit.NANOSECONDS）`，那么主线程执行offer操作与空闲线程执行的poll操作配对成功，主线程把任务交给空闲线程执行，execute()方法执行完成
2. 当初始maximumPool为空，或者maximumPool中当前没有空闲线程时，将没有线程执行`SynchronousQueue.poll（keepAliveTime，TimeUnit.NANOSECONDS）`。此时CachedThreadPool会创建一个新线程执行任务，execute()方法执行完成。
3. 在步骤2）中新创建的线程将任务执行完后，会执行`SynchronousQueue. poll（keepAliveTime，TimeUnit.NANOSECONDS）`。这个poll操作会让空闲线程最多在SynchronousQueue中等待60秒钟。如果60秒钟内主线程提交了一个新任务（主线程执行步骤1）），那么这个空闲线程将执行主线程提交的新任务；否则，这个空闲线程将终止。由于空闲60秒的空闲线程会被终止，因此长时间保持空闲的CachedThreadPool不会使用任何资源。SynchronousQueue是一个没有容量的阻塞队列。每个插入操作必须等待另一个线程的对应移除操作，反之亦然。CachedThreadPool使用SynchronousQueue，把主线程提交的任务传递给空闲线程执行。
   
<div align=center>

![CachedThreadPool任务传递示意图](..\images\1587885884366.png)

</div>

### 注意事项

由于CachedThreadPool的maximumPool是无界的。这意味着，如果主线程提交任务的速度高于maximumPool中线程处理任务的速度时，CachedThreadPool会不断创建新线程。极端情况下，**CachedThreadPool会因为创建过多线程而耗尽CPU和内存资源**。

### 示例

```java
package com.sunld.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CachedThreadPoolTest1 {

	public static void main(String[] args) {
		ExecutorService es = Executors.newCachedThreadPool();
		for(int i = 0; i <= 10; i++) {
			es.execute(new Runnable() {

				@Override
				public void run() {
					System.out.println("run:::" + Thread.currentThread().getName());
				}
			});
		}
	}
}


----结果
run:::pool-1-thread-2
run:::pool-1-thread-1
run:::pool-1-thread-3
run:::pool-1-thread-5
run:::pool-1-thread-7
run:::pool-1-thread-6
run:::pool-1-thread-4
run:::pool-1-thread-6
run:::pool-1-thread-4
run:::pool-1-thread-5
run:::pool-1-thread-7
```

通过结果可以发现：

1. 根据线程名称判断使用到了线程池
2. 根据线程名称的编号可以判断线程被复用
3. 无限性可以通过调整循环次数和增加等待时间测试（由于内存原因未模拟）

## newFixedThreadPool

创建一个可重用固定线程数的线程池，可控制线程最大并发数，超出的线程会在队列中等待，以共享的无界队列方式来运行这些线程。在任意点，在大多数 nThreads 线程会处于处理任务的活动状态。如果在所有线程处于活动状态时提交附加任务，则在有可用线程之前，附加任务将在队列中等待。如果在关闭前的执行期间由于失败而导致任何线程终止，那么一个新线程将代替它执行后续的任务（如果需要）。在某个线程被显式地关闭之前，池中的线程将一直存在。

### 源码分析

```java
/**
* Creates a thread pool that reuses a fixed number of threads
* operating off a shared unbounded queue.  At any point, at most
* {@code nThreads} threads will be active processing tasks.
* If additional tasks are submitted when all threads are active,
* they will wait in the queue until a thread is available.
* If any thread terminates due to a failure during execution
* prior to shutdown, a new one will take its place if needed to
* execute subsequent tasks.  The threads in the pool will exist
* until it is explicitly {@link ExecutorService#shutdown shutdown}.
*
* @param nThreads the number of threads in the pool
* @return the newly created thread pool
* @throws IllegalArgumentException if {@code nThreads <= 0}
* 
* - 创建可容纳固定数量线程的池子，每隔线程的存活时间是无限的，当池子满了就不在添加线程了；
* - 如果池中的所有线程均在繁忙状态，对于新任务会进入阻塞队列中(无界的阻塞队列)
* 
* - 适用：执行长期的任务，性能好很多；为了满足资源管理的需求，而需要限制当前线程的数量；负载比较重的服务器
*/
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads, nThreads,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>());
}

/**
* Creates a thread pool that reuses a fixed number of threads
* operating off a shared unbounded queue, using the provided
* ThreadFactory to create new threads when needed.  At any point,
* at most {@code nThreads} threads will be active processing
* tasks.  If additional tasks are submitted when all threads are
* active, they will wait in the queue until a thread is
* available.  If any thread terminates due to a failure during
* execution prior to shutdown, a new one will take its place if
* needed to execute subsequent tasks.  The threads in the pool will
* exist until it is explicitly {@link ExecutorService#shutdown
* shutdown}.
*
* @param nThreads the number of threads in the pool
* @param threadFactory the factory to use when creating new threads
* @return the newly created thread pool
* @throws NullPointerException if threadFactory is null
* @throws IllegalArgumentException if {@code nThreads <= 0}
*/
public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
    return new ThreadPoolExecutor(nThreads, nThreads,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>(),
                                    threadFactory);
}
```

### execute执行过程

<div align=center>

![1587886474032.png](..\images\1587886474032.png)

</div>

- 1）如果当前运行的线程数少于corePoolSize，则创建新线程来执行任务。当线程池中的线程数大于corePoolSize时，keepAliveTime为多余的空闲线程等待新任务的最长时间，超过这个时间后多余的线程将被终止。这里把keepAliveTime设置为0L，意味着多余的空闲线程会被立即终止。
- 2）在线程池完成预热之后（当前运行的线程数等于corePoolSize），将任务加LinkedBlockingQueue。
- 3）线程执行完1中的任务后，会在循环中反复从LinkedBlockingQueue获取任务来执行。

### 注意事项

FixedThreadPool使用无界队列LinkedBlockingQueue作为线程池的工作队列（队列的容量为Integer.MAX_VALUE）。使用无界队列作为工作队列会对线程池带来如下影响。

- 1）当线程池中的线程数达到corePoolSize后，新任务将在无界队列中等待，因此线程池中的线程数不会超过corePoolSize。
- 2）由于1，使用无界队列时maximumPoolSize将是一个无效参数。
- 3）由于1和2，使用无界队列时keepAliveTime将是一个无效参数。
- 4）由于使用无界队列，运行中的FixedThreadPool（未执行方法shutdown()或shutdownNow()）不会拒绝任务（不会调用RejectedExecutionHandler.rejectedExecution方法）。

### 示例

```java
package com.sunld.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FixedThreadPoolTest1 {

	public static void main(String[] args) {
		ExecutorService es = Executors.newFixedThreadPool(3);
		for(int i = 0; i <= 10; i++) {
			es.execute(new Runnable() {

				@Override
				public void run() {
					System.out.println("run:::" + Thread.currentThread().getName());
				}
			});
		}

	}

}
-----结果
run:::pool-1-thread-2
run:::pool-1-thread-3
run:::pool-1-thread-1
run:::pool-1-thread-3
run:::pool-1-thread-2
run:::pool-1-thread-3
run:::pool-1-thread-1
run:::pool-1-thread-3
run:::pool-1-thread-2
run:::pool-1-thread-3
run:::pool-1-thread-1
```
通过结果发下：

1. 可以控制线程池的最大数

## newScheduledThreadPool

创建一个线程池，它可安排在给定延迟后运行命令或者定期地执行。

### 源码分析

```java
/**
* Creates a thread pool that can schedule commands to run after a
* given delay, or to execute periodically.
* @param corePoolSize the number of threads to keep in the pool,
* even if they are idle
* @return a newly created scheduled thread pool
* @throws IllegalArgumentException if {@code corePoolSize < 0}
* 
* 创建一个固定核心大小的线程池，最大线程数没有限制，
* 线程池内线程存活时间无限制，线程池可以支持定时及周期性任务执行，
* 如果所有线程均处于繁忙状态，对于新任务会进入DelayedWorkQueue队列中，
* 这是一种按照超时时间排序的队列结构
* 
* 适用：需要多个后台线程执行周期任务，同时为了满足资源管理的需求而需要限制后台线程的数量的应用场景
*/
public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
    return new ScheduledThreadPoolExecutor(corePoolSize);
}

/**
* Creates a thread pool that can schedule commands to run after a
* given delay, or to execute periodically.
* @param corePoolSize the number of threads to keep in the pool,
* even if they are idle
* @param threadFactory the factory to use when the executor
* creates a new thread
* @return a newly created scheduled thread pool
* @throws IllegalArgumentException if {@code corePoolSize < 0}
* @throws NullPointerException if threadFactory is null
*/
public static ScheduledExecutorService newScheduledThreadPool(
        int corePoolSize, ThreadFactory threadFactory) {
    return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
}

/**
* Creates a single-threaded executor that can schedule commands
* to run after a given delay, or to execute periodically.
* (Note however that if this single
* thread terminates due to a failure during execution prior to
* shutdown, a new one will take its place if needed to execute
* subsequent tasks.)  Tasks are guaranteed to execute
* sequentially, and no more than one task will be active at any
* given time. Unlike the otherwise equivalent
* {@code newScheduledThreadPool(1)} the returned executor is
* guaranteed not to be reconfigurable to use additional threads.
* @return the newly created scheduled executor
* - 适用于需要单个后台线程执行周期任务，同时需要保证顺序地执行各个任务的应用场景。
*/
public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
  return new DelegatedScheduledExecutorService
      (new ScheduledThreadPoolExecutor(1));
}

/**
* Creates a single-threaded executor that can schedule commands
* to run after a given delay, or to execute periodically.  (Note
* however that if this single thread terminates due to a failure
* during execution prior to shutdown, a new one will take its
* place if needed to execute subsequent tasks.)  Tasks are
* guaranteed to execute sequentially, and no more than one task
* will be active at any given time. Unlike the otherwise
* equivalent {@code newScheduledThreadPool(1, threadFactory)}
* the returned executor is guaranteed not to be reconfigurable to
* use additional threads.
* @param threadFactory the factory to use when creating new
* threads
* @return a newly created scheduled executor
* @throws NullPointerException if threadFactory is null
*/
public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
  return new DelegatedScheduledExecutorService
      (new ScheduledThreadPoolExecutor(1, threadFactory));
}
```

## newSingleThreadExecutor

Executors.newSingleThreadExecutor()返回一个线程池（这个线程池只有一个线程）,这个线程池可以在线程死后（或发生异常时）重新启动一个线程来替代原来的线程继续执行下去！

### 源码分析

```java
/**
* Creates an Executor that uses a single worker thread operating
* off an unbounded queue. (Note however that if this single
* thread terminates due to a failure during execution prior to
* shutdown, a new one will take its place if needed to execute
* subsequent tasks.)  Tasks are guaranteed to execute
* sequentially, and no more than one task will be active at any
* given time. Unlike the otherwise equivalent
* {@code newFixedThreadPool(1)} the returned executor is
* guaranteed not to be reconfigurable to use additional threads.
*
* @return the newly created single-threaded Executor
* 
* 创建只有一个线程的线程池，且线程的存活时间是无限的；
* 当该线程正繁忙时，对于新任务会进入阻塞队列中(无界的阻塞队列)
* 
* 适用：一个任务一个任务执行的场景（顺序执行）；并且在任意时间点，不会有多个线程是活动的场景
*/
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService
        (new ThreadPoolExecutor(1, 1,
                                0L, TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>()));
}

/**
* Creates an Executor that uses a single worker thread operating
* off an unbounded queue, and uses the provided ThreadFactory to
* create a new thread when needed. Unlike the otherwise
* equivalent {@code newFixedThreadPool(1, threadFactory)} the
* returned executor is guaranteed not to be reconfigurable to use
* additional threads.
*
* @param threadFactory the factory to use when creating new
* threads
*
* @return the newly created single-threaded Executor
* @throws NullPointerException if threadFactory is null
*/
public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory) {
    return new FinalizableDelegatedExecutorService
        (new ThreadPoolExecutor(1, 1,
                                0L, TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>(),
                                threadFactory));
}
```

### execute执行过程

<div align=center>

![1587887145755.png](..\images\1587887145755.png)

</div>

1. 如果当前运行的线程数少于corePoolSize（即线程池中无运行的线程），则创建一个新线程来执行任务。
2. 在线程池完成预热之后（当前线程池中有一个运行的线程），将任务加入Linked-BlockingQueue
3. 线程执行完1中的任务后，会在一个无限循环中反复从LinkedBlockingQueue获取任务来执行。


## 参考

1. 《Java并发编程的艺术》
2. 《Java并发编程》
3. 《Java多线程编程核心技术》
