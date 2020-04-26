# Java中线程池的总体架构

## Executor

为了方便并发执行任务，出现了一种专门用来执行任务的实现，也就是`Executor`。  
由此，任务提交者不需要再创建管理线程，使用更方便，也减少了开销。Java 里面线程池的顶级接口是 Executor，但是严格意义上讲 Executor 并不是一个线程池，而只是一个执行线程的工具。真正的线程池接口是 `ExecutorService`。Executor定义规范。

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