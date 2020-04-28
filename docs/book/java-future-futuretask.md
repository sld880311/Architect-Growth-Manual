# 异步结果计算

## 任务提交与执行者的关系

Task Submitter把任务提交给Executor执行，他们之间需要一种 通讯手段，这种手段的具体实现，通常叫做Future。Future通常 包括get（阻塞至任务完成）， cancel，get(timeout)（等待一 段时间）等等。Future也用于异步变同步的场景。

<div align=center>

![1587887264164.png](..\images\1587887264164.png)

</div>

Future就是对于具体的Runnable或者Callable任务的执行结果进行取消、查询是否完成、获取结果。必要时可以通过get方法获取执行结果，该方法会阻塞直到任务返回结果。
Future类位于java.util.concurrent包下，它是一个接口：
?
1	public interface Future<V> {
2	    boolean cancel(boolean mayInterruptIfRunning);
3	    boolean isCancelled();
4	    boolean isDone();
5	    V get() throws InterruptedException, ExecutionException;
6	    V get(long timeout, TimeUnit unit)
7	        throws InterruptedException, ExecutionException, TimeoutException;
8	}
 　　在Future接口中声明了5个方法，下面依次解释每个方法的作用：
	• cancel方法用来取消任务，如果取消任务成功则返回true，如果取消任务失败则返回false。参数mayInterruptIfRunning表示是否允许取消正在执行却没有执行完毕的任务，如果设置true，则表示可以取消正在执行过程中的任务。如果任务已经完成，则无论mayInterruptIfRunning为true还是false，此方法肯定返回false，即如果取消已经完成的任务会返回false；如果任务正在执行，若mayInterruptIfRunning设置为true，则返回true，若mayInterruptIfRunning设置为false，则返回false；如果任务还没有执行，则无论mayInterruptIfRunning为true还是false，肯定返回true。
	• isCancelled方法表示任务是否被取消成功，如果在任务正常完成前被取消成功，则返回 true。
	• isDone方法表示任务是否已经完成，若任务完成，则返回true；
	• get()方法用来获取执行结果，这个方法会产生阻塞，会一直等到任务执行完毕才返回；
	• get(long timeout, TimeUnit unit)用来获取执行结果，如果在指定时间内，还没获取到结果，就直接返回null。
也就是说Future提供了三种功能：
1）判断任务是否完成；
2）能够中断任务；
3）能够获取任务执行结果。
因为Future只是一个接口，所以是无法直接用来创建对象使用的，因此就有了下面的FutureTask。

我们先来看一下FutureTask的实现：
?
1	public class FutureTask<V> implements RunnableFuture<V>
 　　FutureTask类实现了RunnableFuture接口，我们看一下RunnableFuture接口的实现：
?
1	public interface RunnableFuture<V> extends Runnable, Future<V> {
2	    void run();
3	}
 　　可以看出RunnableFuture继承了Runnable接口和Future接口，而FutureTask实现了RunnableFuture接口。所以它既可以作为Runnable被线程执行，又可以作为Future得到Callable的返回值。
FutureTask提供了2个构造器：
?
1	public FutureTask(Callable<V> callable) {
2	}
3	public FutureTask(Runnable runnable, V result) {
4	}
事实上，FutureTask是Future接口的一个唯一实现类。
