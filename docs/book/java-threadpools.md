<!-- TOC -->

- [Java中的线程池](#java%e4%b8%ad%e7%9a%84%e7%ba%bf%e7%a8%8b%e6%b1%a0)
  - [实现原理](#%e5%ae%9e%e7%8e%b0%e5%8e%9f%e7%90%86)
    - [线程复用](#%e7%ba%bf%e7%a8%8b%e5%a4%8d%e7%94%a8)
  - [Executor](#executor)
    - [任务与线程执行器的关系](#%e4%bb%bb%e5%8a%a1%e4%b8%8e%e7%ba%bf%e7%a8%8b%e6%89%a7%e8%a1%8c%e5%99%a8%e7%9a%84%e5%85%b3%e7%b3%bb)
    - [Executor类图](#executor%e7%b1%bb%e5%9b%be)
    - [Executor两级调度模型](#executor%e4%b8%a4%e7%ba%a7%e8%b0%83%e5%ba%a6%e6%a8%a1%e5%9e%8b)
    - [Executor框架结构](#executor%e6%a1%86%e6%9e%b6%e7%bb%93%e6%9e%84)
      - [Executor结构组件](#executor%e7%bb%93%e6%9e%84%e7%bb%84%e4%bb%b6)
  - [ExecutorService](#executorservice)
    - [ThreadPoolExecutor](#threadpoolexecutor)
      - [构建](#%e6%9e%84%e5%bb%ba)
        - [参数说明](#%e5%8f%82%e6%95%b0%e8%af%b4%e6%98%8e)
      - [任务封装](#%e4%bb%bb%e5%8a%a1%e5%b0%81%e8%a3%85)
        - [工作线程定义](#%e5%b7%a5%e4%bd%9c%e7%ba%bf%e7%a8%8b%e5%ae%9a%e4%b9%89)
        - [工作线程任务的真正执行](#%e5%b7%a5%e4%bd%9c%e7%ba%bf%e7%a8%8b%e4%bb%bb%e5%8a%a1%e7%9a%84%e7%9c%9f%e6%ad%a3%e6%89%a7%e8%a1%8c)
      - [任务提交](#%e4%bb%bb%e5%8a%a1%e6%8f%90%e4%ba%a4)
      - [任务执行过程](#%e4%bb%bb%e5%8a%a1%e6%89%a7%e8%a1%8c%e8%bf%87%e7%a8%8b)
      - [任务执行分析](#%e4%bb%bb%e5%8a%a1%e6%89%a7%e8%a1%8c%e5%88%86%e6%9e%90)
        - [任务提交执行代码](#%e4%bb%bb%e5%8a%a1%e6%8f%90%e4%ba%a4%e6%89%a7%e8%a1%8c%e4%bb%a3%e7%a0%81)
      - [线程池关闭](#%e7%ba%bf%e7%a8%8b%e6%b1%a0%e5%85%b3%e9%97%ad)
        - [shutdown](#shutdown)
        - [shutdownNow](#shutdownnow)
      - [线程池监控](#%e7%ba%bf%e7%a8%8b%e6%b1%a0%e7%9b%91%e6%8e%a7)
    - [ScheduledThreadPoolExecutor](#scheduledthreadpoolexecutor)
    - [Executors](#executors)
      - [newCachedThreadPool](#newcachedthreadpool)
      - [newFixedThreadPool](#newfixedthreadpool)
      - [newScheduledThreadPool](#newscheduledthreadpool)
      - [newSingleThreadExecutor](#newsinglethreadexecutor)
  - [异步结果计算](#%e5%bc%82%e6%ad%a5%e7%bb%93%e6%9e%9c%e8%ae%a1%e7%ae%97)
  - [线程池使用注意事项](#%e7%ba%bf%e7%a8%8b%e6%b1%a0%e4%bd%bf%e7%94%a8%e6%b3%a8%e6%84%8f%e4%ba%8b%e9%a1%b9)
    - [配置合理线程池的参考项](#%e9%85%8d%e7%bd%ae%e5%90%88%e7%90%86%e7%ba%bf%e7%a8%8b%e6%b1%a0%e7%9a%84%e5%8f%82%e8%80%83%e9%a1%b9)
    - [使用技巧](#%e4%bd%bf%e7%94%a8%e6%8a%80%e5%b7%a7)
  - [参考](#%e5%8f%82%e8%80%83)

<!-- /TOC -->

# Java中的线程池

使用线程池可以有以下优点：  

1. 降低资源消耗。通过重复利用已创建的线程降低线程创建和销毁造成的消耗。
2. 提高响应速度。当任务到达时，任务可以不需要等到线程创建就能立即执行。
3. 提高线程的可管理性。线程是稀缺资源，如果无限制地创建，不仅会消耗系统资源，还会降低系统的稳定性，使用线程池可以进行统一分配、调优和监控。

## 实现原理

线程池做的工作主要是控制运行的线程的数量，处理过程中将任务放入队列，然后在线程创建后启动这些任务，如果线程数量超过了最大数量超出数量的线程排队等候，等其它线程执行完毕，再从队列中取出任务来执行。他的主要特点为：**线程复用；控制最大并发数；管理线程**。

### 线程复用

每一个 Thread 的类都有一个 start 方法。 当调用 start 启动线程时 Java 虚拟机会调用该类的 run 方法。 那么该类的 run() 方法中就是调用了 Runnable 对象的 run() 方法。 我们可以继承重写 Thread 类，在其 start 方法中添加不断循环调用传递过来的 Runnable 对象。 这就是线程池的实现原理。循环方法中不断获取 Runnable 是用 Queue 实现的，在获取下一个 Runnable 之前可以是阻塞的。

## Executor

为了方便并发执行任务，出现了一种专门用来执行任务的实现，也就是`Executor`。  
由此，任务提交者不需要再创建管理线程，使用更方便，也减少了开销。Java 里面线程池的顶级接口是 Executor，但是严格意义上讲 Executor 并不是一个线程池，而只是一个执行线程的工具。真正的线程池接口是 `ExecutorService`。

### 任务与线程执行器的关系

<div align=center>

![1587825891275.png](..\images\1587825891275.png)

</div>

### Executor类图

<div align=center>

![1587825952037.png](..\images\1587825952037.png)

</div>

### Executor两级调度模型

在上层，Java多线程程序通常把应用分解为若干个任务，然后使用用户级的调度器（Executor框架）将这些任务映射为固定数量的线程；在底层，操作系统内核将这些线程映射到硬件处理器上。

<div align=center>

![1587826068684.png](..\images\1587826068684.png)

</div>

### Executor框架结构

<div align=center>

![1587826248237.png](..\images\1587826248237.png)

</div>

一般的线程池主要分为以下 4 个组成部分：  

1. 线程池管理器：用于创建并管理线程池
2. 工作线程：线程池中的线程
3. 任务接口：每个任务必须实现的接口，用于工作线程调度其运行
4. 任务队列：用于存放待处理的任务，提供一种缓冲机制

Java 中的线程池是通过 Executor 框架实现的，该框架中用到了 `Executor、Executors、ExecutorService、ThreadPoolExecutor 、Callable、Future、FutureTask` 这几个类。

#### Executor结构组件

1. 任务。包括被执行任务需要实现的接口：Runnable接口或Callable接口。Runnable接口和Callable接口的实现类，都可以被ThreadPoolExecutor或Scheduled-ThreadPoolExecutor执行
2. 任务的执行。包括任务执行机制的核心接口Executor，以及继承自Executor的ExecutorService接口。Executor框架有两个关键类实现了ExecutorService接口（ThreadPoolExecutor和ScheduledThreadPoolExecutor）。
3. Executor：Executor框架的基础接口，它将任务的提交与任务的执行分离开来

```java
/**
 * Executor是一个可以提交任务的对象，Executor接口提供一种解耦的任务提交方式，
 * 这种提交方式源于每个任务如何被执行的机制，包括线程的使用细节和调度。
 * 这个接口通常用于明确创建线程的替代方式
 * For example, rather than
 * invoking {@code new Thread(new(RunnableTask())).start()} for each
 * of a set of tasks, you might use:
 * <pre>
 * Executor executor = <em>anExecutor</em>;
 * executor.execute(new RunnableTask1());
 * executor.execute(new RunnableTask2());
 * ...
 * </pre>
 * Executor接口并不严格要求任务被异步执行，有时候能在当前调用的线程中立即执行。
 *  <pre> {@code
 * class DirectExecutor implements Executor {
 *   public void execute(Runnable r) {
 *     r.run();
 *   }
 * }}</pre>
 * 更多典型的场景下,任务被一些线程执行，
 * 而并不在当前线程下,下面的例子中，为每一个任务就产生了一个新的线程。
 *  <pre> {@code
 * class ThreadPerTaskExecutor implements Executor {
 *   public void execute(Runnable r) {
 *     new Thread(r).start();
 *   }
 * }}</pre>
 *
 *一些Executor的实现强加某种关于任务何时和如何被调度的限制，
 *下边的串行化的任务交给第二个executor去执行
 *  <pre> {@code
 * class SerialExecutor implements Executor {
 *   final Queue<Runnable> tasks = new ArrayDeque<Runnable>();
 *   final Executor executor;
 *   Runnable active;
 *   SerialExecutor(Executor executor) {
 *     this.executor = executor;
 *   }
 *   public synchronized void execute(final Runnable r) {
 *     tasks.offer(new Runnable() {
 *       public void run() {
 *         try {
 *           r.run();
 *         } finally {
 *           scheduleNext();
 *         }
 *       }
 *     });
 *     if (active == null) {
 *       scheduleNext();
 *     }
 *   }
 *   protected synchronized void scheduleNext() {
 *     if ((active = tasks.poll()) != null) {
 *       executor.execute(active);
 *     }
 *   }
 * }}</pre>
 *
 * 在当前包下提供了一些实现ExecutorService接口的实现，这个接口是一个相对比较广泛的接口，
 * ThreadPoolExecutor　该类提供了一个可扩展的线程池实现。
 * Execturos静态类提供一些方便的工厂方法去创建不同的Executor
 * 
 * 内存一致性影响：在当前线程的动作要优先于提交给Executor的任务对象，
 * 它们在真正开始执行的时侯，可能在另一个线程里了。
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface Executor {

    /**
     * Executes the given command at some time in the future.  The command
     * may execute in a new thread, in a pooled thread, or in the calling
     * thread, at the discretion of the {@code Executor} implementation.
     *执行给定的命令任务在未来的某个时间点，
     *这个命令可能在一个新的线程里，或者在一个线程池里，也有可能就在前调用线程中。
     * @param command the runnable task
     * @throws RejectedExecutionException if this task cannot be
     * accepted for execution
     * @throws NullPointerException if command is null
     */
    void execute(Runnable command);
}
```

4. ThreadPoolExecutor：线程池的核心实现类，用来执行被提交的任务
5. ScheduledThreadPoolExecutor：线程池实现类，可以在给定的延迟后运行命令，或者定期执行。比Timer更灵活，功能更强大
6. 异步计算的结果。包括接口Future和实现Future接口的FutureTask类。
7. Future接口和实现Future接口的FutureTask代表异步计算的结果


## ExecutorService

```java
/**
 * - 该接口提供了管理任务中断以及一个或多个异步任务执行的跟踪。
 * -
 * - ExecutorService可以被shutdown，执行之后会拒绝新任务的加入，包括两种关闭方式：
 * - shutdown：允许在中止之前提供的任务继续执行，
 * - shutdownNow：阻止等待中的任务，并试图阻止正在执行的任务,
 * - 当一个executor要中止的时侯，没有活动的任务执行，没有任务等待执行，没有新的任务被提交，
 * - 一个未使用的ExecutorService 将被shutdown,以允许资源被回收
 * -
 * - submit方法基于execute方法，
 * - 并且创建返回一个Future对象，该对象通常可以取消执行或等待直到任务完成
 * - invokeAny和invokeAll方法执行批量处理形式，执行一批任务然后等待一个或多个任务执行结束
 * - ExecutorCompletionService通常用于写这些方法的自定义变量。
 * -
 * - Executors 这个类提供工厂方法为了提供executor 服务
 *
 * <h3>Usage Examples</h3>
 *
 * Here is a sketch of a network service in which threads in a thread
 * pool service incoming requests. It uses the preconfigured {@link
 * Executors#newFixedThreadPool} factory method:
 *
 *  <pre> {@code
 * class NetworkService implements Runnable {
 *   private final ServerSocket serverSocket;
 *   private final ExecutorService pool;
 *
 *   public NetworkService(int port, int poolSize)
 *       throws IOException {
 *     serverSocket = new ServerSocket(port);
 *     pool = Executors.newFixedThreadPool(poolSize);
 *   }
 *
 *   public void run() { // run the service
 *     try {
 *       for (;;) {
 *         pool.execute(new Handler(serverSocket.accept()));
 *       }
 *     } catch (IOException ex) {
 *       pool.shutdown();
 *     }
 *   }
 * }
 *
 * class Handler implements Runnable {
 *   private final Socket socket;
 *   Handler(Socket socket) { this.socket = socket; }
 *   public void run() {
 *     // read and service request on socket
 *   }
 * }}</pre>
 *
 * The following method shuts down an {@code ExecutorService} in two phases,
 * first by calling {@code shutdown} to reject incoming tasks, and then
 * calling {@code shutdownNow}, if necessary, to cancel any lingering tasks:
 *
 *  <pre> {@code
 * void shutdownAndAwaitTermination(ExecutorService pool) {
 *   pool.shutdown(); // Disable new tasks from being submitted
 *   try {
 *     // Wait a while for existing tasks to terminate
 *     if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
 *       pool.shutdownNow(); // Cancel currently executing tasks
 *       // Wait a while for tasks to respond to being cancelled
 *       if (!pool.awaitTermination(60, TimeUnit.SECONDS))
 *           System.err.println("Pool did not terminate");
 *     }
 *   } catch (InterruptedException ie) {
 *     // (Re-)Cancel if current thread also interrupted
 *     pool.shutdownNow();
 *     // Preserve interrupt status
 *     Thread.currentThread().interrupt();
 *   }
 * }}</pre>
 *
 * <p>Memory consistency effects: Actions in a thread prior to the
 * submission of a {@code Runnable} or {@code Callable} task to an
 * {@code ExecutorService}
 * <a href="package-summary.html#MemoryVisibility"><i>happen-before</i></a>
 * any actions taken by that task, which in turn <i>happen-before</i> the
 * result is retrieved via {@code Future.get()}.
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface ExecutorService extends Executor {

    /**
     * - 启动一个有秩序的shutdown
     * - 调用该方法时，之前提交的任务都被执行，但是不接收新任务；
     * - 如果已关闭，再次调用则没有任何影响
     * -
     * - 该方法不会等待已提交任务被执行完成，可以使用awaitTermination来实现。
     *
     * @throws SecurityException if a security manager exists and
     *         shutting down this ExecutorService may manipulate
     *         threads that the caller is not permitted to modify
     *         because it does not hold {@link
     *         java.lang.RuntimePermission}{@code ("modifyThread")},
     *         or the security manager's {@code checkAccess} method
     *         denies access.
     */
    void shutdown();

    /**
     * - 试图停止所有活动的正在执行的任务，停止等待中的任务处理，
     * - 返回等待执行任务列表（未执行的任务列表）
     *
     * - 该方法不会等待活动的执行中的任务终止，可以使用awaitTermination处理。
     *
     * - 处理尽量尝试终止活动中执行的任务，但是没有任何保障。
     * - 尤其是无法响应中断的任务，则永远无法终止
     *
     * @return list of tasks that never commenced execution
     * @throws SecurityException if a security manager exists and
     *         shutting down this ExecutorService may manipulate
     *         threads that the caller is not permitted to modify
     *         because it does not hold {@link
     *         java.lang.RuntimePermission}{@code ("modifyThread")},
     *         or the security manager's {@code checkAccess} method
     *         denies access.
     */
    List<Runnable> shutdownNow();

    /**
     * Returns {@code true} if this executor has been shut down.
     * - 如果executor已经关闭则返回true
     *
     * @return {@code true} if this executor has been shut down
     */
    boolean isShutdown();

    /**
     * - 如果所有任务在shutdown后全部执行完成则返回true
     * - isTerminated 可能永远为true,除非调用了shutdown或shutdownNow
     *
     * @return {@code true} if all tasks have completed following shut down
     */
    boolean isTerminated();

    /**
     * -阻塞直到（任一）
     * -1.在shutdown 方法执行之后，所有任务完成执行
     * -2.时间超时
     * -3.当前线程interrupted
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return {@code true} if this executor terminated and
     *         {@code false} if the timeout elapsed before termination
     * @throws InterruptedException if interrupted while waiting
     */
    boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * - 提交一个有返回值的任务，执行并且返回一个Future对象，代表任务的执行结果
     * - future.get方法会返回成功完成任务的执行结果（阻塞效果）
     *
     * - 如果想立刻阻塞等待任务的结果，可以参考以下代码：
     * {@code result = exec.submit(aCallable).get();}
     *
     * - Executors中提供了一组方法，用于对象的转换（闭包对象），
     * - 比如PrivilegedAction转成Callable形式
     *
     * @param task the task to submit
     * @param <T> the type of the task's result
     * @return a Future representing pending completion of the task
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException if the task is null
     */
    <T> Future<T> submit(Callable<T> task);

    /**
     * Submits a Runnable task for execution and returns a Future
     * representing that task. The Future's {@code get} method will
     * return the given result upon successful completion.
     *
     * @param task the task to submit
     * @param result the result to return
     * @param <T> the type of the result
     * @return a Future representing pending completion of the task
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException if the task is null
     */
    <T> Future<T> submit(Runnable task, T result);

    /**
     * Submits a Runnable task for execution and returns a Future
     * representing that task. The Future's {@code get} method will
     * return {@code null} upon <em>successful</em> completion.
     *
     * @param task the task to submit
     * @return a Future representing pending completion of the task
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException if the task is null
     */
    Future<?> submit(Runnable task);

    /**
     * - 批量提交任务，当所有任务都执行完成时返回Future列表，这些Future保持任务的状态和结果。
     * - 返回列表中每个Future的isDone都为true
     * -
     * - 任务会在正常运行结束或者抛出异常时完成终止
     * - 如果在操作过程中任务队列别修改，则返回返回undefined
     *
     * @param tasks the collection of tasks
     * @param <T> the type of the values returned from the tasks
     * @return a list of Futures representing the tasks, in the same
     *         sequential order as produced by the iterator for the
     *         given task list, each of which has completed
     * @throws InterruptedException if interrupted while waiting, in
     *         which case unfinished tasks are cancelled
     * @throws NullPointerException if tasks or any of its elements are {@code null}
     * @throws RejectedExecutionException if any task cannot be
     *         scheduled for execution
     */
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
        throws InterruptedException;

    /**
     * @param tasks the collection of tasks
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @param <T> the type of the values returned from the tasks
     * @return a list of Futures representing the tasks, in the same
     *         sequential order as produced by the iterator for the
     *         given task list. If the operation did not time out,
     *         each task will have completed. If it did time out, some
     *         of these tasks will not have completed.
     * @throws InterruptedException if interrupted while waiting, in
     *         which case unfinished tasks are cancelled
     * @throws NullPointerException if tasks, any of its elements, or
     *         unit are {@code null}
     * @throws RejectedExecutionException if any task cannot be scheduled
     *         for execution
     */
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                  long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * - 1.返回任一个已经成功完成任务的结果，也就是说没有抛出异常。
     * - 2.在正常或者异常返回时，未完成的任务将被取消。
     * - 3.在执行过程中如果任务列表被修改，则方法的返回结果为undefined
     *
     * @param tasks the collection of tasks
     * @param <T> the type of the values returned from the tasks
     * @return the result returned by one of the tasks
     * @throws InterruptedException if interrupted while waiting
     * @throws NullPointerException if tasks or any element task
     *         subject to execution is {@code null}
     * @throws IllegalArgumentException if tasks is empty
     * @throws ExecutionException if no task successfully completes
     * @throws RejectedExecutionException if tasks cannot be scheduled
     *         for execution
     */
    <T> T invokeAny(Collection<? extends Callable<T>> tasks)
        throws InterruptedException, ExecutionException;

    /**
     * @param tasks the collection of tasks
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @param <T> the type of the values returned from the tasks
     * @return the result returned by one of the tasks
     * @throws InterruptedException if interrupted while waiting
     * @throws NullPointerException if tasks, or unit, or any element
     *         task subject to execution is {@code null}
     * @throws TimeoutException if the given timeout elapses before
     *         any task successfully completes
     * @throws ExecutionException if no task successfully completes
     * @throws RejectedExecutionException if tasks cannot be scheduled
     *         for execution
     */
    <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                    long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
}
```

### ThreadPoolExecutor

#### 构建

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

##### 参数说明

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

#### 任务封装

线程池创建线程时，会将线程封装成工作线程Worker，Worker在执行完任务后，还会循环获取工作队列里的任务来执行。我们可以从Worker类的run()方法里看到这点。

##### 工作线程定义

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

##### 工作线程任务的真正执行

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

#### 任务提交

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

#### 任务执行过程

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

#### 任务执行分析

<div align=center>

![1587868053700.png](..\images\1587868053700.png)

</div>

##### 任务提交执行代码

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

#### 线程池关闭

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

##### shutdown

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

##### shutdownNow

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

#### 线程池监控

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

### ScheduledThreadPoolExecutor

### Executors

#### newCachedThreadPool

#### newFixedThreadPool

#### newScheduledThreadPool

#### newSingleThreadExecutor

## 异步结果计算

## 线程池使用注意事项

### 配置合理线程池的参考项

1. ❑ 任务的性质：CPU密集型任务、IO密集型任务和混合型任务。
   1. CPU密集型任务：配置可能小的线程，如配置Ncpu+1个线程的线程池。
   2. IO密集型任务：配置尽可能多的线程，如2*Ncpu。
2. ❑ 任务的优先级：高、中和低。
   1. 使用优先级队列：`PriorityBlockingQueue`
3. ❑ 任务的执行时间：长、中和短。
   1. 使用不同规模的线程池处理
   2. 或者使用优先级队列
4. ❑ 任务的依赖性：是否依赖其他系统资源，如数据库连接。
   1. 线程数配置尽量大，充分利用CPU

### 使用技巧

1. 避免是有无限队列


## 参考

1. 《Java并发编程的艺术》
2. 《Java并发编程》
3. 《Java多线程编程核心技术》
