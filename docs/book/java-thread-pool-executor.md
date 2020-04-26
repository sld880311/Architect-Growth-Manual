<!-- TOC -->

- [线程池ThreadPoolExecutor详解](#线程池threadpoolexecutor详解)
    - [构建函数](#构建函数)
        - [参数说明](#参数说明)
    - [任务封装](#任务封装)
        - [工作线程定义](#工作线程定义)
        - [工作线程任务的真正执行](#工作线程任务的真正执行)
    - [任务提交](#任务提交)
    - [任务执行过程](#任务执行过程)
    - [任务执行分析](#任务执行分析)
        - [任务提交执行代码](#任务提交执行代码)
    - [线程池关闭](#线程池关闭)
        - [shutdown](#shutdown)
        - [shutdownNow](#shutdownnow)
    - [线程池监控](#线程池监控)
    - [参考](#参考)

<!-- /TOC -->

# 线程池ThreadPoolExecutor详解

## 构建函数

```java
    /**
     * Creates a new {@code ThreadPoolExecutor} with the given initial
     * parameters.
     *
     * @param corePoolSize the number of threads to keep in the pool, even
     *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     *        -- 当提交一个任务到线程池时，线程池会创建一个线程来执行任务，
     *        -- 即使其他空闲的基本线程能够执行新任务也会创建线程，
     *        -- 等到需要执行的任务数大于线程池基本大小时就不再创建。
     *        -- 调用prestartAllCoreThreads()后，线程池会提前创建并启动所有基本线程。
     * @param maximumPoolSize the maximum number of threads to allow in the
     *        pool
     *        -- 允许创建的最大线程数。如果队列满了，并且已创建的线程数小于最大线程数，
     *        -- 则线程池会再创建新的线程执行任务（无线队列会使该参数失效）。
     * @param keepAliveTime when the number of threads is greater than
     *        the core, this is the maximum time that excess idle threads
     *        will wait for new tasks before terminating.
     *        -- 线程池的工作线程空闲后，保持存活的时间。
     *        -- 如果任务很多，并且每个任务执行的时间比较短，可以调大时间，提高线程的利用率。
     * @param unit the time unit for the {@code keepAliveTime} argument
     * 		  -- 天（DAYS）、小时（HOURS）、分钟（MINUTES）、毫秒（MILLISECONDS）、
     *        -- 微秒（MICROSECONDS，千分之一毫秒）和纳秒（NANOSECONDS，千分之一微秒）
     * @param workQueue the queue to use for holding tasks before they are
     *        executed.  This queue will hold only the {@code Runnable}
     *        tasks submitted by the {@code execute} method.
     *        -- 用于保存等待执行的任务的阻塞队列
     *        -- ❑ ArrayBlockingQueue：基于数组结构的有界阻塞队列，排序规则：FIFO
     *        -- ❑ LinkedBlockingQueue：基于链表结构的阻塞队列，排序规则：FIFO，
                                        吞吐量通常要高于ArrayBlockingQueue。
                                        Executors.newFixedThreadPool()使用了这个队列。
     *        -- ❑ SynchronousQueue：一个不存储元素的阻塞队列（读写交换执行，否则会阻塞。）
                                      吞吐量通常要高于Linked-BlockingQueue，
                                      Executors.newCachedThreadPool使用了这个队列。
     *        -- ❑ PriorityBlockingQueue：一个具有优先级的无限阻塞队列。
     * @param threadFactory the factory to use when the executor
     *        creates a new thread
     *        -- 设置创建线程的工厂，可以通过线程工厂给每个创建出来的线程设置更有意义的名字。
     *        -- 使用开源框架guava提供的ThreadFactoryBuilder
                 可以快速给线程池里的线程设置有意义的名字
     * @param handler the handler to use when execution is blocked
     *        because the thread bounds and queue capacities are reached
     *        -- 当队列和线程池都满了（饱和状态），那么必须采取一种策略处理提交的新任务。
     *        -- 这个策略默认情况下是AbortPolicy，表示无法处理新任务时抛出异常
     *        -- ❑ AbortPolicy：直接抛出异常。
     *        -- ❑ CallerRunsPolicy：只用调用者所在线程来运行任务。
     *        -- ❑ DiscardOldestPolicy：丢弃队列里最近的一个任务，并执行当前任务。
     *        -- ❑ DiscardPolicy：不处理，丢弃掉。
     *        -- 实现RejectedExecutionHandler接口自定义策略
     * @throws IllegalArgumentException if one of the following holds:<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveTime < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code workQueue}
     *         or {@code threadFactory} or {@code handler} is null
     */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory,
                              RejectedExecutionHandler handler) {
        if (corePoolSize < 0 ||
            maximumPoolSize <= 0 ||
            maximumPoolSize < corePoolSize ||
            keepAliveTime < 0)
            throw new IllegalArgumentException();
        if (workQueue == null || threadFactory == null || handler == null)
            throw new NullPointerException();
        this.acc = System.getSecurityManager() == null ?
                null :
                AccessController.getContext();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.threadFactory = threadFactory;
        this.handler = handler;
    }
```

### 参数说明

1. corePoolSize（线程池的基本大小）
   - 当提交一个任务到线程池时，线程池会创建一个线程来执行任务
   - 即使其他空闲的基本线程能够执行新任务也会创建线程
   - 等到需要执行的任务数大于线程池基本大小时就不再创建
   - 调用prestartAllCoreThreads()后，线程池会提前创建并启动所有基本线程
2. maximumPoolSize（线程池最大数量）
   - 允许创建的最大线程数。如果队列满了，并且已创建的线程数小于最大线程数
   - 则线程池会再创建新的线程执行任务
   - 无线队列会使该参数失效
3. keepAliveTime（线程活动保持时间）
   - 线程池的工作线程空闲后，保持存活的时间
   - 如果任务很多，并且每个任务执行的时间比较短，可以调大时间，提高线程的利用率
4. unit（线程活动保持的时间单位）
   - 天（DAYS）
   - 小时（HOURS）
   - 分钟（MINUTES）
   - 毫秒（MILLISECONDS）、
   - 微秒（MICROSECONDS，千分之一毫秒）
   - 纳秒（NANOSECONDS，千分之一微秒）
5. workQueue（任务队列）：用于保存等待执行的任务的阻塞队列
   - ArrayBlockingQueue：
     - 基于数组结构的有界阻塞队列，排序规则：FIFO
   - LinkedBlockingQueue：
     - 基于链表结构的阻塞队列，排序规则：FIFO，
     - 吞吐量通常要高于ArrayBlockingQueue。
     - Executors.newFixedThreadPool()使用了这个队列。
   - SynchronousQueue：
     - 一个不存储元素的阻塞队列（读写交换执行，否则会阻塞。）
     - 吞吐量通常要高于Linked-BlockingQueue，
     - Executors.newCachedThreadPool使用了这个队列。
   - PriorityBlockingQueue：
     - 一个具有优先级的无限阻塞队列。
6. threadFactory（线程工厂）
   - 设置创建线程的工厂，可以通过线程工厂给每个创建出来的线程设置更有意义的名字。
   - 使用开源框架guava提供的ThreadFactoryBuilder可以快速给线程池里的线程设置有意义的名字
7. handler（拒绝策略）
   - 当队列和线程池都满了（饱和状态）或关闭时，那么必须采取一种策略处理提交的新任务
   - 这个策略默认情况下是AbortPolicy，表示无法处理新任务时抛出异常
   - 具体策略包括：
     - AbortPolicy：直接抛出异常
     - CallerRunsPolicy：只用调用者所在线程来运行任务
     - DiscardOldestPolicy：丢弃队列里最近的一个任务，并执行当前任务
     - DiscardPolicy：不处理，丢弃掉
     - 实现RejectedExecutionHandler接口自定义策略

## 任务封装

线程池创建线程时，会将线程封装成工作线程Worker，Worker在执行完任务后，还会循环获取工作队列里的任务来执行。我们可以从Worker类的run()方法里看到这点。

### 工作线程定义

```java
/**
* Class Worker mainly maintains interrupt control state for
* threads running tasks, along with other minor bookkeeping.
* This class opportunistically extends AbstractQueuedSynchronizer
* to simplify acquiring and releasing a lock surrounding each
* task execution.  This protects against interrupts that are
* intended to wake up a worker thread waiting for a task from
* instead interrupting a task being run.  We implement a simple
* non-reentrant mutual exclusion lock rather than use
* ReentrantLock because we do not want worker tasks to be able to
* reacquire the lock when they invoke pool control methods like
* setCorePoolSize.  Additionally, to suppress interrupts until
* the thread actually starts running tasks, we initialize lock
* state to a negative value, and clear it upon start (in
* runWorker).
* -Worker继承了AQS抽象类，其重写了AQS的一些方法，
* -并且其也可作为一个Runnable对象，从而可以创建线程Thread
*/
private final class Worker
    extends AbstractQueuedSynchronizer
    implements Runnable
{
    /**
    * This class will never be serialized, but we provide a
    * serialVersionUID to suppress a javac warning.
    */
    private static final long serialVersionUID = 6138294804551838833L;

    /** Thread this worker is running in.  Null if factory fails. */
    // worker 所对应的线程，用来封装worker（因为worker为Runnable对象），表示一个线程
    final Thread thread;
    /** Initial task to run.  Possibly null. */
    // worker所对应的第一个任务
    // 表示该worker所包含的Runnable对象，即用户自定义的Runnable对象，
    // 完成用户自定义的逻辑的Runnable对象
    Runnable firstTask;
    /** Per-thread task counter */
    // 已完成任务数量
    volatile long completedTasks;

    /**
        * Creates with given first task and thread from ThreadFactory.
        * @param firstTask the first task (null if none)
        */
    Worker(Runnable firstTask) {
        //并设置AQS的state为-1
        setState(-1); // inhibit interrupts until runWorker
        // 初始化第一个任务
        this.firstTask = firstTask;
        // 根据当前worker，初始化线程
        this.thread = getThreadFactory().newThread(this);
    }

    /** Delegates main run loop to outer runWorker  */
    /** 重写Runnable中的run方法 */
    public void run() {
        runWorker(this);
    }

    // Lock methods
    //
    // The value 0 represents the unlocked state.
    // The value 1 represents the locked state.

    protected boolean isHeldExclusively() {
        return getState() != 0;
    }

    protected boolean tryAcquire(int unused) {
        if (compareAndSetState(0, 1)) {
            setExclusiveOwnerThread(Thread.currentThread());
            return true;
        }
        return false;
    }

    protected boolean tryRelease(int unused) {
        setExclusiveOwnerThread(null);
        setState(0);
        return true;
    }

    public void lock()        { acquire(1); }
    public boolean tryLock()  { return tryAcquire(1); }
    public void unlock()      { release(1); }
    public boolean isLocked() { return isHeldExclusively(); }

    void interruptIfStarted() {
        Thread t;
        if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
            try {
                t.interrupt();
            } catch (SecurityException ignore) {
            }
        }
    }
}
```

### 工作线程任务的真正执行

```java
/**
* - 真正执行任务的方法
* 1.线程开始执行前，需要对worker加锁，完成一个任务后执行unlock()
* 2.在任务执行前后，执行beforeExecute()和afterExecute()方法
* 3.记录任务执行中的异常后，继续抛出
* 4.每个任务完成后，会记录当前线程完成的任务数
* 5.当worker执行完一个任务的时候，包括初始任务firstTask，
    会调用getTask()继续获取任务，这个方法调用
*/
final void runWorker(Worker w) {
    Thread wt = Thread.currentThread();
    Runnable task = w.firstTask;
    w.firstTask = null;
    w.unlock(); // allow interrupts
    boolean completedAbruptly = true;
    try {
        // 循环执行，直到阻塞队列为空
        while (task != null || (task = getTask()) != null) {
            w.lock();
            // If pool is stopping, ensure thread is interrupted;
            // if not, ensure thread is not interrupted.  This
            // requires a recheck in second case to deal with
            // shutdownNow race while clearing interrupt
            if ((runStateAtLeast(ctl.get(), STOP) ||
                    (Thread.interrupted() &&
                    runStateAtLeast(ctl.get(), STOP))) &&
                !wt.isInterrupted())
                wt.interrupt();
            try {
                // 在执行之前调用钩子函数
                beforeExecute(wt, task);
                Throwable thrown = null;
                try {
                    // 运行给定的任务
                    task.run();
                } catch (RuntimeException x) {
                    thrown = x; throw x;
                } catch (Error x) {
                    thrown = x; throw x;
                } catch (Throwable x) {
                    thrown = x; throw new Error(x);
                } finally {
                    // 执行完后调用钩子函数
                    afterExecute(task, thrown);
                }
            } finally {
                task = null;
                w.completedTasks++;
                w.unlock();
            }
        }
        completedAbruptly = false;
    } finally {
        // 处理完成后，调用钩子函数
        processWorkerExit(w, completedAbruptly);
    }
}
```

```java
/**
* Performs cleanup and bookkeeping for a dying worker. Called
* only from worker threads. Unless completedAbruptly is set,
* assumes that workerCount has already been adjusted to account
* for exit.  This method removes thread from worker set, and
* possibly terminates the pool or replaces the worker if either
* it exited due to user task exception or if fewer than
* corePoolSize workers are running or queue is non-empty but
* there are no workers.
* @param w the worker
* @param completedAbruptly if the worker died due to user exception
* processWorkerExit函数是在worker退出时调用到的钩子函数，而引起worker退出的主要因素如下
* ① 阻塞队列已经为空，即没有任务可以运行了。
* ② 调用了shutDown或shutDownNow函数
* 此函数会根据是否中断了空闲线程来确定是否减少workerCount的值，
* 并且将worker从workers集合中移除并且会尝试终止线程池。
*/
private void processWorkerExit(Worker w, boolean completedAbruptly) {
    // 如果被中断，则需要减少workCount
    if (completedAbruptly) // If abrupt, then workerCount wasn't adjusted
        decrementWorkerCount();

    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        // 将worker完成的任务添加到总的完成任务中
        completedTaskCount += w.completedTasks;
        // 从workers集合中移除该worker
        workers.remove(w);
    } finally {
        mainLock.unlock();
    }
    // 尝试终止
    tryTerminate();

    int c = ctl.get();
    if (runStateLessThan(c, STOP)) {// 小于STOP的运行状态
        if (!completedAbruptly) {
            // 允许核心超时并且workQueue阻塞队列不为空
            int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
            if (min == 0 && ! workQueue.isEmpty())
                min = 1;
            if (workerCountOf(c) >= min)// workerCount大于等于min
                return; // replacement not needed
        }
        // 添加worker
        addWorker(null, false);
    }
}


```

```java
/**
* Performs blocking or timed wait for a task, depending on
* current configuration settings, or returns null if this worker
* must exit because of any of:
* 1. There are more than maximumPoolSize workers (due to
*    a call to setMaximumPoolSize).
* 2. The pool is stopped.
* 3. The pool is shutdown and the queue is empty.
* 4. This worker timed out waiting for a task, and timed-out
*    workers are subject to termination (that is,
*    {@code allowCoreThreadTimeOut || workerCount > corePoolSize})
*    both before and after the timed wait, and if the queue is
*    non-empty, this worker is not the last thread in the pool.
*
* @return task, or null if the worker must exit, in which case
*         workerCount is decremented
* -此函数用于从workerQueue阻塞队列中获取Runnable对象，由于是阻塞队列，
* -所以支持有限时间等待（poll）和无限时间等待（take）。
* -在该函数中还会响应shutDown和、shutDownNow函数的操作，若检测到线程池处于SHUTDOWN或STOP状态，
* -则会返回null，而不再返回阻塞队列中的Runnalbe对象。
*/

private Runnable getTask() {
    boolean timedOut = false; // Did the last poll() time out?

    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);

        // Check if queue empty only if necessary.
        // 大于等于SHUTDOWN（表示调用了shutDown）并且（大于等于STOP（调用了shutDownNow）或者worker阻塞队列为空）
        if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
            // 减少worker的数量
            decrementWorkerCount();
            return null;// 返回null，不执行任务
        }

        int wc = workerCountOf(c);

        // Are workers subject to culling?
        // 是否允许coreThread超时或者workerCount大于核心大小
        boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;

        if ((wc > maximumPoolSize || (timed && timedOut))// worker数量大于maximumPoolSize
            && (wc > 1 || workQueue.isEmpty())) {// workerCount大于1或者worker阻塞队列为空（在阻塞队列不为空时，需要保证至少有一个wc）
            if (compareAndDecrementWorkerCount(c))// 比较并减少workerCount
                return null;// 返回null，不执行任务，该worker会退出
            continue;// 跳过剩余部分，继续循环
        }

        try {
            Runnable r = timed ?
                workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                workQueue.take();
            if (r != null)
                return r;
            timedOut = true;
        } catch (InterruptedException retry) {
            timedOut = false;
        }
    }
}
```

## 任务提交

<div align=center>

![1587871132076.png](..\images\1587871132076.png)

</div>

1. 主线程首先要创建实现Runnable或者Callable接口的任务对象。工具类Executors可以把一个Runnable对象封装为一个Callable对象（Executors.callable（Runnable task）或Executors.callable（Runnable task，Object resule））。
2. 然后可以把Runnable对象直接交给ExecutorService执行（ExecutorService.execute（Runnable command））；或者也可以把Runnable对象或Callable对象提交给ExecutorService执行（Executor-Service.submit（Runnable task）或ExecutorService.submit（Callable<T>task））。
3. 如果执行ExecutorService.submit（…），ExecutorService将返回一个实现Future接口的对象（到目前为止的JDK中，返回的是FutureTask对象）。由于FutureTask实现了Runnable，程序员也可以创建FutureTask，然后直接交给ExecutorService执行。
4. 最后，主线程可以执行FutureTask.get()方法来等待任务执行完成。主线程也可以执行FutureTask.cancel（boolean mayInterruptIfRunning）来取消此任务的执行。

> 说明：  
> execute()方法用于提交不需要返回值的任务，所以无法判断任务是否被线程池执行成功  
> submit()方法用于提交需要返回值的任务。线程池会返回一个future类型的对象，通过这个future对象可以判断任务是否执行成功，并且可以通过future的get()方法来获取返回值，get()方法会阻塞当前线程直到任务完成，而使用get（long timeout，TimeUnit unit）方法则会阻塞当前线程一段时间后立即返回，这时候有可能任务没有执行完。

## 任务执行过程

1. 线程池刚创建时，里面没有一个线程。任务队列是作为参数传进来的。不过，就算队列里面有任务，线程池也不会马上执行它们。
2. 当调用 execute() 方法添加一个任务时，线程池会做如下判断：
    - 如果正在运行的线程数量小于 corePoolSize，那么马上创建线程运行这个任务，即使此时线程池中存在空闲线程；**（需要使用全局锁处理）**
    - 如果正在运行的线程数量大于或等于 corePoolSize，那么将这个任务放入workQueue队列，等待线程池中任务调度执行；
    - 如果这时候workQueue队列满了（无法加入队列），而且正在运行的线程数量小于 maximumPoolSize，那么还是要创建非核心线程立刻运行这个任务；**（需要使用全局锁处理）**
    - 如果队列满了，而且正在运行的线程数量大于或等于 maximumPoolSize，会出行对应的拒绝策略。
3. 当一个线程完成任务时，它会从队列中取下一个任务来执行。
4. 当一个线程无事可做，超过一定的时间（keepAliveTime）时，线程池会判断，如果当前运行的线程数大于 corePoolSize，那么这个线程就被停掉。所以线程池的所有任务完成后，它最终会收缩到 corePoolSize 的大小，当设置allowCoreThreadTimeOut(true)时，线程池中corePoolSize线程空闲时间达到keepAliveTime也将关闭

<div align=center>

![1587867917277.png](..\images\1587867917277.png)

![1587867935740.png](..\images\1587867935740.png)

</div>

> ThreadPoolExecutor采取上述步骤的总体设计思路，是为了在执行execute()方法时，尽可能地避免获取全局锁（那将会是一个严重的可伸缩瓶颈）。在ThreadPoolExecutor完成预热之后（当前运行的线程数大于等于corePoolSize），基本上都是加入到等待队列中处理。

## 任务执行分析

<div align=center>

![1587868053700.png](..\images\1587868053700.png)

</div>

### 任务提交执行代码

```java
/**
* Executes the given task sometime in the future.  The task
* may execute in a new thread or in an existing pooled thread.
*
* If the task cannot be submitted for execution, either because this
* executor has been shutdown or because its capacity has been reached,
* the task is handled by the current {@code RejectedExecutionHandler}.
*
* @param command the task to execute
* @throws RejectedExecutionException at discretion of
*         {@code RejectedExecutionHandler}, if the task
*         cannot be accepted for execution
* @throws NullPointerException if {@code command} is null
*/
public void execute(Runnable command) {
    if (command == null)
        throw new NullPointerException();
    /**
    * - 进行下面三步
    * - 1. 如果运行的线程小于corePoolSize,则尝试使用用户定义的Runnalbe对象创建一个新的线程
    * -    调用addWorker函数会原子性的检查runState和workCount，通过返回false来防止在不应
    * -    该添加线程时添加了线程
    * -
    * - 2. 如果一个任务能够成功入队列，在添加一个线城时仍需要进行双重检查（因为在前一次检查后
    * -    该线程死亡了），或者当进入到此方法时，线程池已经shutdown了，所以需要再次检查状态，
    * -    若有必要，当停止时还需要回滚入队列操作，或者当线程池没有线程时需要创建一个新线程
    * -
    * - 3. 如果无法入队列，那么需要增加一个新线程，如果此操作失败，
    * - 那么就意味着线程池已经shutdown或者已经饱和了，所以拒绝任务
    */
    int c = ctl.get(); // 获取线程池控制状态
    if (workerCountOf(c) < corePoolSize) {
        if (addWorker(command, true)) // 添加成功后则返回，否则继续执行
            return;
        c = ctl.get(); // 不成功则再次获取线程池控制状态
    }
    // 线程池处于RUNNING状态，将命令（用户自定义的Runnable对象）添加进workQueue队列
    if (isRunning(c) && workQueue.offer(command)) {
        // 再次检查，获取线程池控制状态
        int recheck = ctl.get();
        // 线程池不处于RUNNING状态，将命令从workQueue队列中移除
        if (! isRunning(recheck) && remove(command))
            reject(command); // 拒绝执行命令
        else if (workerCountOf(recheck) == 0)
            addWorker(null, false);
    }
    else if (!addWorker(command, false)) // 添加worker失败
        reject(command); // 拒绝执行命令
}

```

```java
/**
* Checks if a new worker can be added with respect to current
* pool state and the given bound (either core or maximum). If so,
* the worker count is adjusted accordingly, and, if possible, a
* new worker is created and started, running firstTask as its
* first task. This method returns false if the pool is stopped or
* eligible to shut down. It also returns false if the thread
* factory fails to create a thread when asked.  If the thread
* creation fails, either due to the thread factory returning
* null, or due to an exception (typically OutOfMemoryError in
* Thread.start()), we roll back cleanly.
*
* @param firstTask the task the new thread should run first (or
* null if none). Workers are created with an initial first task
* (in method execute()) to bypass queuing when there are fewer
* than corePoolSize threads (in which case we always start one),
* or when the queue is full (in which case we must bypass queue).
* Initially idle threads are usually created via
* prestartCoreThread or to replace other dying workers.
*
* @param core if true use corePoolSize as bound, else
* maximumPoolSize. (A boolean indicator is used here rather than a
* value to ensure reads of fresh values after checking other pool
* state).
* @return true if successful
*/
/**
    * -① 原子性的增加workerCount。
    * -② 将用户给定的任务封装成为一个worker，并将此worker添加进workers集合中。
    * -③ 启动worker对应的线程，并启动该线程，运行worker的run方法。
    * -④ 回滚worker的创建动作，即将worker从workers集合中删除，并原子性的减少workerCount。
    */
private boolean addWorker(Runnable firstTask, boolean core) {
    /**
    * 双层无限循环，尝试增加线程数到ctl变量，并且做一些比较判断，
    * 如果超出线程数限定或者ThreadPoolExecutor的状态不符合要求，则直接返回false，增加worker失败。
    * 第二部分：从第28行开始到结尾，把firstTask这个Runnable对象传给Worker构造方法，赋值给Worker对象的task属性。Worker对象把自身（也是一个Runnable）封装成一个Thread对象赋予Worker对象的thread属性。锁住整个线程池并实际增加worker到workers的HashSet对象当中。成功增加后开始执行t.start()，就是worker的thread属性开始运行，实际上就是运行Worker对象的run方法。Worker的run()方法实际上调用了ThreadPoolExecutor的runWorker()方法。在看runWorker()之前先看一下Worker对象。
    */
    retry:
    for (;;) {// 无限循环
        int c = ctl.get();// 获取线程池控制状态
        // 获取状态
        int rs = runStateOf(c);

        // Check if queue empty only if necessary.
        if (rs >= SHUTDOWN &&
            ! (rs == SHUTDOWN &&
                firstTask == null &&
                ! workQueue.isEmpty()))
            return false;

        for (;;) {
            // worker数量
            int wc = workerCountOf(c);
            if (wc >= CAPACITY ||
                wc >= (core ? corePoolSize : maximumPoolSize))
                return false;
            if (compareAndIncrementWorkerCount(c))
                break retry; // 调出外循环
            // 获取线程池控制状态
            c = ctl.get();  // Re-read ctl
            // 此次的状态与上次获取的状态不相同
            if (runStateOf(c) != rs)
                // 跳过剩余部分，继续循环
                continue retry;
            // else CAS failed due to workerCount change; retry inner loop
        }
    }
    /**
        * 把firstTask这个Runnable对象传给Worker构造方法，赋值给Worker对象的task属性。
        * Worker对象把自身（也是一个Runnable）封装成一个Thread对象赋予Worker对象的thread属性。
        * 锁住整个线程池并实际增加worker到workers的HashSet对象当中。
        * 成功增加后开始执行t.start()，就是worker的thread属性开始运行，实际上就是运行Worker对象的run方法。
        * Worker的run()方法实际上调用了ThreadPoolExecutor的runWorker()方法。
        */
    // worker开始标识
    boolean workerStarted = false;
    // worker被添加标识
    boolean workerAdded = false;
    Worker w = null;
    try {
        // 使用用户任务初始化worker
        w = new Worker(firstTask);
        // 获取worker对应的线程
        final Thread t = w.thread;
        if (t != null) {
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                // Recheck while holding lock.
                // Back out on ThreadFactory failure or if
                // shut down before lock acquired.
                // 线程池的运行状态
                int rs = runStateOf(ctl.get());

                if (rs < SHUTDOWN ||
                    (rs == SHUTDOWN && firstTask == null)) {
                    if (t.isAlive()) // precheck that t is startable
                        throw new IllegalThreadStateException();
                    workers.add(w);
                    int s = workers.size();
                    if (s > largestPoolSize)
                        largestPoolSize = s;
                    workerAdded = true;
                }
            } finally {
                mainLock.unlock();
            }
            if (workerAdded) {
                t.start();
                workerStarted = true;
            }
        }
    } finally {
        if (! workerStarted)
            addWorkerFailed(w);
    }
    return workerStarted;
}
```

## 线程池关闭

使用`shutdown`或`shutdownNow`方法关闭线程池。它原理是遍历线程池中的工作线程，然后逐个调用线程的`interrupt`方法来中断线程，所以无法响应中断的任务可能永远无法终止。

1. shutdownNow:任务要不求执行完成
   - 将线程池的状态设置成STOP，
   - 尝试停止所有的正在执行或暂停任务的线程，
   - 返回等待执行任务的列表
2. shutdown:任务需要执行完成
   - 将线程池的状态设置成SHUTDOWN状态
   - 中断所有没有正在执行任务的线程。

> 注意事项  
> `isShutdown`方法在调用`shutdown`或`shutdownNow`之后立即返回true  
> `isTerminaed`当所有任务都关闭后，表示线程池关闭成功，并且返回true  

### shutdown

```java
/**
* Initiates an orderly shutdown in which previously submitted
* tasks are executed, but no new tasks will be accepted.
* Invocation has no additional effect if already shut down.
*
* <p>This method does not wait for previously submitted tasks to
* complete execution.  Use {@link #awaitTermination awaitTermination}
* to do that.
*
* @throws SecurityException {@inheritDoc}
*/
public void shutdown() {
    // 此函数会按过去执行已提交任务的顺序发起一个有序的关闭，但是不接受新任务。
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        // 检查shutdown权限
        checkShutdownAccess();
        /**
        * 尝试将状态切换到SHUTDOWN，这样就不会再接收新的任务提交。
        * 对空闲线程进行中断调用。最后检查线程池线程是否为0，并尝试切换到TERMINATED状态。
        */
        advanceRunState(SHUTDOWN);
        // 中断空闲worker
        interruptIdleWorkers();
        // 调用shutdown钩子函数
        onShutdown(); // hook for ScheduledThreadPoolExecutor
    } finally {
        mainLock.unlock();
    }
    // 尝试终止
    tryTerminate();
}
```

### shutdownNow

```java
/**
* Attempts to stop all actively executing tasks, halts the
* processing of waiting tasks, and returns a list of the tasks
* that were awaiting execution. These tasks are drained (removed)
* from the task queue upon return from this method.
*
* <p>This method does not wait for actively executing tasks to
* terminate.  Use {@link #awaitTermination awaitTermination} to
* do that.
*
* <p>There are no guarantees beyond best-effort attempts to stop
* processing actively executing tasks.  This implementation
* cancels tasks via {@link Thread#interrupt}, so any task that
* fails to respond to interrupts may never terminate.
*
* @throws SecurityException {@inheritDoc}
*/
public List<Runnable> shutdownNow() {
    List<Runnable> tasks;
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        checkShutdownAccess();
        advanceRunState(STOP);
        interruptWorkers();
        tasks = drainQueue();
    } finally {
        mainLock.unlock();
    }
    tryTerminate();
    return tasks;
}
```

## 线程池监控

1. taskCount：线程池需要执行的任务数量。
   
   ```java
    /**
     * -- 返回曾经计划执行的任务总数的近似值（由于任务和线程的状态可能在计算期间发生变化）
     *
     * @return the number of tasks
     */
    public long getTaskCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
        	// 已经执行完成的任务数
            long n = completedTaskCount;
            // 加上任务中执行完成的数量
            for (Worker w : workers) {
                n += w.completedTasks;
                if (w.isLocked())
                    ++n;
            }
            // 加上阻塞队列的大小
            return n + workQueue.size();
        } finally {
            mainLock.unlock();
        }
    }
   ```
   
2. completedTaskCount:线程池在运行过程中已完成的任务数量，小于或等于taskCount。
   
   ```java
    /**
     * Counter for completed tasks. Updated only on termination of
     * worker threads. Accessed only under mainLock.
     * - 完成的任务数，只有在工作线程完成之后才更新该值，需要通过mainLock下获取
     */
    private long completedTaskCount;

    /**
     * Returns the approximate total number of tasks that have
     * completed execution. Because the states of tasks and threads
     * may change dynamically during computation, the returned value
     * is only an approximation, but one that does not ever decrease
     * across successive calls.
     *
     * @return the number of tasks
     */
    public long getCompletedTaskCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = completedTaskCount;
            for (Worker w : workers)
                n += w.completedTasks;
            return n;
        } finally {
            mainLock.unlock();
        }
    }

   ```

3. largestPoolSize：线程池里曾经创建过的最大线程数量。通过这个数据可以知道线程池是否曾经满过。如该数值等于线程池的最大大小，则表示线程池曾经满过。
   
   ```java
    /**
     * Tracks largest attained pool size. Accessed only under
     * mainLock.
     */
    private int largestPoolSize;

    /**
     * Returns the largest number of threads that have ever
     * simultaneously been in the pool.
     *
     * @return the number of threads
     */
    public int getLargestPoolSize() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            return largestPoolSize;
        } finally {
            mainLock.unlock();
        }
    }
   ```

4. getPoolSize：线程池的线程数量。如果线程池不销毁的话，线程池里的线程不会自动销毁，所以这个大小只增不减。
   
   ```java
    /**
     * Returns the current number of threads in the pool.
     *
     * @return the number of threads
     */
    public int getPoolSize() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            // Remove rare and surprising possibility of
            // isTerminated() && getPoolSize() > 0
            return runStateAtLeast(ctl.get(), TIDYING) ? 0
                : workers.size();
        } finally {
            mainLock.unlock();
        }
    }
   ```

5. getActiveCount：获取活动的线程数。
   
   ```java
    /**
     * Returns the approximate number of threads that are actively
     * executing tasks.
     *
     * @return the number of threads
     */
    public int getActiveCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            int n = 0;
            for (Worker w : workers)
                if (w.isLocked())
                    ++n;
            return n;
        } finally {
            mainLock.unlock();
        }
    }
   ```

> 可以通过继承线程池来自定义线程池，重写线程池的beforeExecute、afterExecute和terminated方法，也可以在任务执行前、执行后和线程池关闭前执行一些代码来进行监控。例如，监控任务的平均执行时间、最大执行时间和最小执行时间等。这几个方法在线程池里是空方法

## 参考

1. 《Java并发编程的艺术》
2. 《Java并发编程》
3. 《Java多线程编程核心技术》
