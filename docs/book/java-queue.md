<!-- TOC -->

- [Java中的阻塞队列](#java%e4%b8%ad%e7%9a%84%e9%98%bb%e5%a1%9e%e9%98%9f%e5%88%97)

<!-- /TOC -->
# Java中的阻塞队列

阻塞队列，关键字是阻塞，先理解阻塞的含义，在阻塞队列中，线程阻塞有这样的两种情况： 
	当队列中没有数据的情况下，消费者端的所有线程都会被自动阻塞（挂起），直到有数据放入队列。 
<div align=center>

![1589104874399.png](..\images\1589104874399.png)

</div>

	当队列中填满数据的情况下，生产者端的所有线程都会被自动阻塞（挂起），直到队列中有空的位置，线程被自动唤醒。 
<div align=center>

![1589104894314.png](..\images\1589104894314.png)

</div>

<div align=center>

![1589104912351.png](..\images\1589104912351.png)

</div>

1.1.1.1	阻塞队列的主要方法 

<div align=center>

![1589104934306.png](..\images\1589104934306.png)

</div>

	抛出异常：抛出一个异常； 
	特殊值：返回一个特殊值（null 或 false,视情况而定） 
	则塞：在成功操作之前，一直阻塞线程 
	超时：放弃前只在最大的时间内阻塞 
<div align=center>

![1589104955872.png](..\images\1589104955872.png)

</div>


1.1.1.1.1	插入操作
1：public abstract boolean add(E paramE)：将指定元素插入此队列中（如果立即可行
且不会违反容量限制），成功时返回 true，如果当前没有可用的空间，则抛出 IllegalStateException。如果该元素是 NULL，则会抛出 NullPointerException 异常。 
2：public abstract boolean offer(E paramE)：将指定元素插入此队列中（如果立即可行且不会违反容量限制），成功时返回 true，如果当前没有可用的空间，则返回 false。  
3：public abstract void put(E paramE) throws InterruptedException： 将指定元素插入此队列中，将等待可用的空间（如果有必要） 
    /**
     * Inserts the specified element at the tail of this queue, waiting
     * for space to become available if the queue is full.
     *
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public void put(E e) throws InterruptedException {
        checkNotNull(e);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == items.length)
                notFull.await();//如果队列满了，则线程阻塞等待
            enqueue(e);
        } finally {
            lock.unlock();
        }
    }
4：offer(E o, long timeout, TimeUnit unit)：可以设定等待的时间，如果在指定的时间内，还不能往队列中加入 BlockingQueue，则返回失败。
1.1.1.1.2	获取数据操作
1：poll(time):取走 BlockingQueue 里排在首位的对象,若不能立即取出,则可以等 time 参数规定的时间,取不到时返回 null; 
2：poll(long timeout, TimeUnit unit)：从 BlockingQueue 取出一个队首的对象，如果在
指定时间内，队列一旦有数据可取，则立即返回队列中的数据。否则直到时间超时还没有数据可取，返回失败。 
3：take():取走 BlockingQueue 里排在首位的对象,若 BlockingQueue 为空,阻断进入等待状态直到 BlockingQueue 有新的数据被加入。 
4.drainTo():一次性从 BlockingQueue 获取所有可用的数据对象（还可以指定获取数据的个数），通过该方法，可以提升获取数据效率；不需要多次分批加锁或释放锁。  
1.1.1.1.3	示例
package com.sunld.block;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class BlockQueueTest {

	public static void main(String[] args) {
		final BlockingQueue<Object> bq = new ArrayBlockingQueue<Object>(10);
		new Thread("错误使用") {
			@Override
			public void run() {
				for(;;) {
					Object o = bq.poll();//不等待直接返回数据
					// TODO
				}
			}
		};
		new Thread("正确使用") {
			@Override
			public void run() {
				for(;;) {
					try {
						Object o = bq.take();//等待数据
						// TODO
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					}
				}
			}
		};
		new Thread("正确使用") {
			@Override
			public void run() {
				for(;;) {
					try {
						Object o = bq.poll(1, TimeUnit.SECONDS);//防止死等
						// TODO
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					}
				}
			}
		};
	}

}

1.1.1.2	自定义阻塞队列

package com.sunld.block;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyDefineBlockQueue {

	class BlockingQ1 {
		private Object notEmpty = new Object();
		private Queue<Object> linkedList = new LinkedList<>();

		// 未取得锁就直接执行wait、notfiy、notifyAll会抛异常
		public Object take() throws InterruptedException {
			synchronized (notEmpty) {
				if (linkedList.size() == 0) {// 没有数据需要等待
					/**
					 * 1.执行wait之前需要获得当前对象的锁 2.执行wait之后释放锁 3.被唤醒之前，需要先获得锁
					 */
					notEmpty.wait();
				}
				return linkedList.poll();
			}
		}

		public void offer(Object o) {
			synchronized (notEmpty) {
				System.out.println(linkedList.size());
				if (linkedList.size() == 0) {
					/**
					 * 要执行notify和notifyAll操作，都必须先取得该对象的锁。
					 */
					notEmpty.notifyAll();
				}
				linkedList.add(o);
			}
		}
	}

	/**
	 * 分别需要对notEmpty和notFull加锁
	 * 
	 * @author Theodore SUN
	 */
	class BlockingQ2 {
		private Object notEmpty = new Object();
		private Object notFull = new Object();
		private Queue<Object> linkedList = new LinkedList<Object>();
		private int maxLength = 10;

		public Object take() throws InterruptedException {
			synchronized (notEmpty) {
				if (linkedList.size() == 0) {
					notEmpty.wait();
				}
				synchronized (notFull) {
					if (linkedList.size() == maxLength) {
						notFull.notifyAll();
					}
					return linkedList.poll();
				}
			}
		}

		public void offer(Object object) throws InterruptedException {
			synchronized (notEmpty) {
				if (linkedList.size() == 0) {
					notEmpty.notifyAll();
				}
				synchronized (notFull) {
					if (linkedList.size() == maxLength) {
						notFull.wait();
					}
					linkedList.add(object);
				}
			}
		}
	}

	//未锁就直接执行await、signal、siganlAll会抛异常
	class BlockingQ3 {
		//一个锁创建多个condition
		private Lock lock = new ReentrantLock();
		private Condition notEmpty = lock.newCondition();
		private Condition notFull = lock.newCondition();
		private Queue<Object> linkedList = new LinkedList<Object>();
		private int maxLength = 10;

		public Object take() throws InterruptedException {
			lock.lock();
			try {
				if (linkedList.size() == 0) {
					/**
					 * 要执行await操作，必须先取得该Condition的锁。
					 * 执行await操作之后，锁会释放。
					 * 被唤醒之前，需要先获得锁
					 */
					notEmpty.await();
				}
				if (linkedList.size() == maxLength) {
					notFull.signalAll();
				}
				return linkedList.poll();
			} finally {
				lock.unlock();
			}
		}

		public void offer(Object object) throws InterruptedException {
			lock.lock();
			try {
				if (linkedList.size() == 0) {
					// 要执行signal和signalAll操作，都必须先取得该对象的锁。
					notEmpty.signalAll();
				}
				if (linkedList.size() == maxLength) {
					notFull.await();
				}
				linkedList.add(object);
			} finally {
				lock.unlock();
			}
		}
	}

	public static void main(String[] args) throws InterruptedException {
		MyDefineBlockQueue a = new MyDefineBlockQueue();
		BlockingQ1 bq1 = a.new BlockingQ1();
//		bq1.take();//没有数据会阻塞
		bq1.offer(new Object());
		System.out.println(bq1.take());
	}
}


1.1.1.3	Java 中的阻塞队列 
 
1.	ArrayBlockingQueue ：由数组结构组成的有界阻塞队列。 
2.	LinkedBlockingQueue ：由链表结构组成的有界阻塞队列。 
3.	PriorityBlockingQueue ：支持优先级排序的无界阻塞队列。 
4.	DelayQueue：使用优先级队列实现的无界阻塞队列。 
5.	SynchronousQueue：不存储元素的阻塞队列。 
6.	LinkedTransferQueue：由链表结构组成的无界阻塞队列。 
7.	LinkedBlockingDeque：由链表结构组成的双向阻塞队列 
<div align=center>

![1589105003217.png](..\images\1589105003217.png)

</div>
1.1.1.1.1	ArrayBlockingQueue（公平、非公平） 
用数组实现的有界阻塞队列。此队列按照先进先出（FIFO）的原则对元素进行排序。默认情况下不保证访问者公平的访问队列，所谓公平访问队列是指阻塞的所有生产者线程或消费者线程，当队列可用时，可以按照阻塞的先后顺序访问队列，即先阻塞的生产者线程，可以先往队列里插入元素，先阻塞的消费者线程，可以先从队列里获取元素。通常情况下为了保证公平性会降低吞吐量。我们可以使用以下代码创建一个公平的阻塞队列： 
ArrayBlockingQueue fairQueue = new  ArrayBlockingQueue(1000,true); 
1.1.1.1.2	LinkedBlockingQueue（两个独立锁提高并发） 
基于链表的阻塞队列，同ArrayListBlockingQueue类似，此队列按照先进先出（FIFO）的原则对元素进行排序。而 LinkedBlockingQueue 之所以能够高效的处理并发数据，还因为其对于生产者端和消费者端分别采用了独立的锁来控制数据同步，这也意味着在高并发的情况下生产者和消费者可以并行地操作队列中的数据，以此来提高整个队列的并发性能。 
LinkedBlockingQueue 会默认一个类似无限大小的容量（Integer.MAX_VALUE）。 
1.1.1.1.3	PriorityBlockingQueue（compareTo 排序实现优先） 
是一个支持优先级的无界队列。默认情况下元素采取自然顺序升序排列。可以自定义实现 compareTo()方法来指定元素进行排序规则，或者初始化 PriorityBlockingQueue 时，指定构造参数 Comparator 来对元素进行排序。需要注意的是不能保证同优先级元素的顺序。 
1.1.1.1.4	DelayQueue（缓存失效、定时任务 ） 
是一个支持延时获取元素的无界阻塞队列。队列使用PriorityQueue来实现。队列中的元素必须实现 Delayed 接口，在创建元素时可以指定多久才能从队列中获取当前元素。只有在延迟期满时才能从队列中提取元素。我们可以将 DelayQueue 运用在以下应用场景： 
1.	缓存系统的设计：可以用 DelayQueue 保存缓存元素的有效期，使用一个线程循环查询
DelayQueue，一旦能从 DelayQueue 中获取元素时，表示缓存有效期到了。 
2.	定时任务调度：使用 DelayQueue 保存当天将会执行的任务和执行时间，一旦从
DelayQueue 中获取到任务就开始执行，从比如 TimerQueue 就是使用 DelayQueue 实现的。 
1.1.1.1.5	SynchronousQueue（不存储数据、可用于传递数据） 
是一个不存储元素的阻塞队列。每一个 put 操作必须等待一个 take 操作，否则不能继续添加元素。
SynchronousQueue 可以看成是一个传球手，负责把生产者线程处理的数据直接传递给消费者线程。队列本身并不存储任何元素，非常适合于传递性场景,比如在一个线程中使用的数据，传递给另 外 一 个 线 程 使 用 ， SynchronousQueue 的 吞 吐 量 高 于 LinkedBlockingQueue 和 
ArrayBlockingQueue。 
1.1.1.1.6	LinkedTransferQueue 
是 一 个 由 链 表 结 构 组 成 的 无 界 阻 塞 TransferQueue 队 列 。 相 对 于 其 他 阻 塞 队 列 ，
LinkedTransferQueue 多了 tryTransfer 和 transfer 方法。 
1.	transfer 方法：如果当前有消费者正在等待接收元素（消费者使用 take()方法或带时间限制的 poll()方法时），transfer 方法可以把生产者传入的元素立刻 transfer（传输）给消费者。如果没有消费者在等待接收元素，transfer 方法会将元素存放在队列的 tail 节点，并等到该元素被消费者消费了才返回。 
2.	tryTransfer 方法。则是用来试探下生产者传入的元素是否能直接传给消费者。如果没有消费者等待接收元素，则返回 false。和 transfer 方法的区别是 tryTransfer 方法无论消费者是否接收，方法立即返回。而 transfer 方法是必须等到消费者消费了才返回。 
对于带有时间限制的 tryTransfer(E e, long timeout, TimeUnit unit)方法，则是试图把生产者传入的元素直接传给消费者，但是如果没有消费者消费该元素则等待指定的时间再返回，如果超时还没消费元素，则返回 false，如果在超时时间内消费了元素，则返回 true。 
1.1.1.1.7	LinkedBlockingDeque 
是一个由链表结构组成的双向阻塞队列。所谓双向队列指的你可以从队列的两端插入和移出元素。
双端队列因为多了一个操作队列的入口，在多线程同时入队时，也就减少了一半的竞争。相比其他的阻塞队列，LinkedBlockingDeque 多了 addFirst，addLast，offerFirst，offerLast， peekFirst，peekLast 等方法，以 First 单词结尾的方法，表示插入，获取（peek）或移除双端队列的第一个元素。以 Last 单词结尾的方法，表示插入，获取或移除双端队列的最后一个元素。另外插入方法 add 等同于 addLast，移除方法 remove 等效于 removeFirst。但是 take 方法却等同于 takeFirst，不知道是不是 Jdk 的 bug，使用时还是用带有 First 和 Last 后缀的方法更清楚。 
在初始化 LinkedBlockingDeque 时可以设置容量防止其过渡膨胀。另外双向阻塞队列可以运用在
“工作窃取”模式中。 


1.1.1	Java线程阻塞的代价
java的线程是映射到操作系统原生线程之上的，如果要阻塞或唤醒一个线程就需要操作系统介入，需要在户态与核心态之间切换，这种切换会消耗大量的系统资源，因为用户态与内核态都有各自专用的内存空间，专用的寄存器等，用户态切换至内核态需要传递给许多变量、参数给内核，内核也需要保护好用户态在切换时的一些寄存器值、变量等，以便内核态调用结束后切换回用户态继续工作。
	如果线程状态切换是一个高频操作时，这将会消耗很多CPU处理时间；
	如果对于那些需要同步的简单的代码块，获取锁挂起操作消耗的时间比用户代码执行的时间还要长，这种同步策略显然非常糟糕的。
synchronized会导致争用不到锁的线程进入阻塞状态，所以说它是java语言中一个重量级的同步操纵，被称为重量级锁，为了缓解上述性能问题，JVM从1.5开始，引入了轻量锁与偏向锁，默认启用了自旋锁，他们都属于乐观锁。

1.1.3	什么是 AQS（抽象的队列同步器） 
AbstractQueuedSynchronizer 类如其名，抽象的队列式的同步器，AQS 定义了一套多线程访问共享资源的同步器框架，许多同步类实现都依赖于它，如常用的
ReentrantLock/Semaphore/CountDownLatch。 
<div align=center>

![1589109265967.png](..\images\1589109265967.png)

</div>

它维护了一个 volatile int state（代表共享资源）和一个 FIFO 线程等待队列（多线程争用资源被阻塞时会进入此队列）。这里 volatile 是核心关键词，具体 volatile 的语义，在此不述。state 的访问方式有三种: 
getState() setState() 
compareAndSetState() 
AQS 定义两种资源共享方式 
Exclusive独占资源-ReentrantLock 
Exclusive（独占，只有一个线程能执行，如 ReentrantLock） 
Share共享资源-Semaphore/CountDownLatch 
Share（共享，多个线程可同时执行，如 Semaphore/CountDownLatch）。 
AQS只是一个框架，具体资源的获取/释放方式交由自定义同步器去实现，AQS这里只定义了一个接口，具体资源的获取交由自定义同步器去实现了（通过state的get/set/CAS)之所以没有定义成 abstract，是因为独占模式下只用实现 tryAcquire-tryRelease，而共享模式下只用实现 tryAcquireShared-tryReleaseShared。如果都定义成abstract，那么每个模式也要去实现另一模式下的接口。不同的自定义同步器争用共享资源的方式也不同。自定义同步器在实现时只需要实现共享资源 state 的获取与释放方式即可，至于具体线程等待队列的维护（如获取资源失败入队/ 唤醒出队等），AQS 已经在顶层实现好了。自定义同步器实现时主要实现以下几种方法： 
1． isHeldExclusively()：该线程是否正在独占资源。只有用到 condition 才需要去实现它。 
2． tryAcquire(int)：独占方式。尝试获取资源，成功则返回 true，失败则返回 false。 
3． tryRelease(int)：独占方式。尝试释放资源，成功则返回 true，失败则返回 false。 
4． tryAcquireShared(int)：共享方式。尝试获取资源。负数表示失败；0 表示成功，但没有剩余可用资源；正数表示成功，且有剩余资源。 
5． tryReleaseShared(int)：共享方式。尝试释放资源，如果释放后允许唤醒后续等待结点返回 true，否则返回 false。 
 
同步器的实现是ABS核心（state资源状态计数） 
同步器的实现是 ABS 核心，以 ReentrantLock 为例，state 初始化为 0，表示未锁定状态。A 线程 lock()时，会调用 tryAcquire()独占该锁并将 state+1。此后，其他线程再 tryAcquire()时就会失败，直到A线程unlock()到state=0（即释放锁）为止，其它线程才有机会获取该锁。当然，释放锁之前，A 线程自己是可以重复获取此锁的（state 会累加），这就是可重入的概念。但要注意，获取多少次就要释放多么次，这样才能保证 state 是能回到零态的。 
以 CountDownLatch 以例，任务分为 N 个子线程去执行，state 也初始化为 N（注意 N 要与线程个数一致）。这 N 个子线程是并行执行的，每个子线程执行完后 countDown()一次，state 会 CAS 减1。等到所有子线程都执行完后(即state=0)，会 unpark()主调用线程，然后主调用线程就会从 await()函数返回，继续后余动作。 
ReentrantReadWriteLock 实现独占和共享两种方式 
  一般来说，自定义同步器要么是独占方法，要么是共享方式，他们也只需实现 tryAcquiretryRelease、tryAcquireShared-tryReleaseShared 中的一种即可。但 AQS 也支持自定义同步器同时实现独占和共享两种方式，如 ReentrantReadWriteLock。 

