# ScheduledThreadPoolExecutor详解

1）当调用ScheduledThreadPoolExecutor的scheduleAtFixedRate()方法或者scheduleWith-FixedDelay()方法时，会向ScheduledThreadPoolExecutor的DelayQueue添加一个实现了RunnableScheduledFuture接口的ScheduledFutureTask。
/**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    public ScheduledFuture<?> schedule(Runnable command,
                                       long delay,
                                       TimeUnit unit) {
        if (command == null || unit == null)
            throw new NullPointerException();
        RunnableScheduledFuture<?> t = decorateTask(command,
            new ScheduledFutureTask<Void>(command, null,
                                          triggerTime(delay, unit)));
        delayedExecute(t);
        return t;
    }

/**
     * Main execution method for delayed or periodic tasks.  If pool
     * is shut down, rejects the task. Otherwise adds task to queue
     * and starts a thread, if necessary, to run it.  (We cannot
     * prestart the thread to run the task because the task (probably)
     * shouldn't be run yet.)  If the pool is shut down while the task
     * is being added, cancel and remove it if required by state and
     * run-after-shutdown parameters.
     *
     * @param task the task
     */
    private void delayedExecute(RunnableScheduledFuture<?> task) {
        if (isShutdown())
            reject(task);
        else {
            super.getQueue().add(task);
            if (isShutdown() &&
                !canRunInCurrentRunState(task.isPeriodic()) &&
                remove(task))
                task.cancel(false);
            else
                ensurePrestart();
        }
    }
2）线程池中的线程从DelayQueue中获取ScheduledFutureTask，然后执行任务。


<div align=center>

![1589104571948.png](..\images\1589104571948.png)

</div>

ScheduledThreadPoolExecutor为了实现周期性的执行任务，对ThreadPoolExecutor做了如下的修改。
❑ 使用DelayQueue作为任务队列。
❑ 获取任务的方式不同（后文会说明）。
❑ 执行周期任务后，增加了额外的处理。
1.1.1.1.1	执行过程
<div align=center>

![1589104596328.png](..\images\1589104596328.png)

</div>

1）线程1从DelayQueue中获取已到期的ScheduledFutureTask（DelayQueue. take()）。到期任务是指ScheduledFutureTask的time大于等于当前时间。

<div align=center>

![1589104627768.png](..\images\1589104627768.png)

</div>

/**
     * Retrieves and removes the head of this queue, waiting if necessary
     * until an element with an expired delay is available on this queue.
     *
     * @return the head of this queue
     * @throws InterruptedException {@inheritDoc}
     */
    public E take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        // 1 获取lock锁
        lock.lockInterruptibly();
        try {
            for (;;) {
                E first = q.peek();
                if (first == null)
                	// 2.1：如果PriorityQueue为空，则当前Condition available等待
                    available.await();
                else {
                    long delay = first.getDelay(NANOSECONDS);
                    if (delay <= 0)
                    	// 2.3.1 返回PriorityQueue中的头元素
                        return q.poll();
                    first = null; // don't retain ref while waiting
                    if (leader != null)
                        available.await();
                    else {
                        Thread thisThread = Thread.currentThread();
                        leader = thisThread;
                        try {
                        	// 2.2
                            available.awaitNanos(delay);
                        } finally {
                            if (leader == thisThread)
                                leader = null;
                        }
                    }
                }
            }
        } finally {
            if (leader == null && q.peek() != null)
            	//2.3.2 唤醒Condition available等待的线程
                available.signal();
            //3
            lock.unlock();
        }
    }
2）线程1执行这个ScheduledFutureTask。
3）线程1修改ScheduledFutureTask的time变量为下次将要被执行的时间。
4）线程1把这个修改time之后的ScheduledFutureTask放回DelayQueue中（Delay-Queue.add()）。
<div align=center>

![1589104656087.png](..\images\1589104656087.png)

</div>



/**
     * Inserts the specified element into this delay queue.
     *
     * @param e the element to add
     * @return {@code true}
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e) {
        final ReentrantLock lock = this.lock;
        lock.lock(); //1
        try {
            q.offer(e); //2.1
            if (q.peek() == e) {
                leader = null;
                // 2.2如果在上面2.1中添加的任务是PriorityQueue的头元素，唤醒在Condition中等待的所有线程。
                available.signal();
            }
            return true;
        } finally {
            lock.unlock();//3
        }
    }
1.1.1.1.1	实例
package com.sunld.thread;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledExecutorService1 {

	public static void main(String[] args) {
		ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(3);
		scheduledThreadPool.schedule(new Runnable() {
			@Override
			public void run() {
				System.out.println("延迟三秒");
			}
		}, 3, TimeUnit.SECONDS);
		scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				System.out.println("延迟 1 秒后每三秒执行一次");
			}
		}, 1, 3, TimeUnit.SECONDS);
	}
}
