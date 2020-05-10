1.1.4	互斥同步
互斥同步（Mutual Exclusion & Synchronization）是一种最常见也是最主要的并发正确性保障手段。同步是指在多个线程并发访问共享数据时，保证共享数据在同一个时刻只被一条（或者是一些，当使用信号量的时候）线程使用。而互斥是实现同步的一种手段，临界区（CriticalSection）、互斥量（Mutex）和信号量（Semaphore）都是常见的互斥实现方式。因此在“互斥同步”这四个字里面，互斥是因，同步是果；互斥是方法，同步是目的。
1.1.1.1	Synchronized
synchronized 它可以把任意一个非 NULL 的对象当作锁。他属于独占式的悲观锁，同时属于可重入锁。 
Synchronized 语义
	一种块结构同步语法，javac编译之后会在同步块的前后分别形成monitorenter和monitorexit这两个字节码指令，使用reference类型指明加锁和加锁的对象（用户自定义对象、当前this对象、当前class对象）
	通过锁的计数器来判断是否释放锁，在执行monitorenter指令时，首先要去尝试获取对象的锁，获取锁或当前拥有锁则计数器+1；执行monitorexit指令时会将锁计数器的值减一；一旦计数器的值为零，锁随即就被释放了
	如果获取对象锁失败，那当前线程就应当被阻塞等待，直到请求锁定的对象被持有它的线程释放为止
	被synchronized修饰的同步块对同一条线程来说是可重入的。这意味着同一线程反复进入同步块也不会出现自己把自己锁死的情况
	被synchronized修饰的同步块在持有锁的线程执行完毕并释放锁之前，会无条件地阻塞后面其他线程的进入。这意味着无法像处理某些数据库中的锁那样，强制已获取锁的线程释放锁；也无法强制正在等待锁的线程中断等待或超时退出。
Synchronized 作用范围 
1.	作用于方法时，锁住的是对象的实例(this)； 
2.	当作用于静态方法时，锁住的是Class实例，又因为Class的相关数据存储在永久带PermGen
（jdk1.8 则是 metaspace），永久带是全局共享的，因此静态方法锁相当于类的一个全局锁，会锁所有调用该方法的线程； 
3.	synchronized 作用于一个对象实例时，锁住的是所有以该对象为锁的代码块。它有多个队列，
当多个线程一起访问某个对象监视器的时候，对象监视器会将这些线程存储在不同的容器中。 
Synchronized 核心组件 
1)	Wait Set：调用 wait 方法被阻塞的线程被放置在这里； 
2)	Contention List：竞争队列，所有请求锁的线程首先被放在这个竞争队列中； 
3)	Entry List：Contention List 中那些有资格成为候选资源的线程被移动到 Entry List 中； 
4)	OnDeck：任意时刻，最多只有一个线程正在竞争锁资源，该线程被成为 OnDeck； 
5)	Owner：当前已经获取到所资源的线程被称为 Owner； 
6)	!Owner：当前释放锁的线程。 
Synchronized 实现 
<div align=center>

![1589108463996.png](..\images\1589108463996.png)

</div>

1.	JVM 每次从队列的尾部取出一个数据用于锁竞争候选者（OnDeck），但是并发情况下，
ContentionList 会被大量的并发线程进行 CAS 访问，为了降低对尾部元素的竞争，JVM 会将一部分线程移动到 EntryList 中作为候选竞争线程。 
2.	Owner 线程会在 unlock 时，将 ContentionList 中的部分线程迁移到 EntryList 中，并指定
EntryList 中的某个线程为 OnDeck 线程（一般是最先进去的那个线程）。 
3.	Owner 线程并不直接把锁传递给 OnDeck 线程，而是把锁竞争的权利交给 OnDeck，
OnDeck需要重新竞争锁。这样虽然牺牲了一些公平性，但是能极大的提升系统的吞吐量，在
JVM 中，也把这种选择行为称之为“竞争切换”。 
4.	OnDeck 线程获取到锁资源后会变为 Owner 线程，而没有得到锁资源的仍然停留在 EntryList 中。如果Owner线程被wait方法阻塞，则转移到WaitSet队列中，直到某个时刻通过notify 或者 notifyAll 唤醒，会重新进去 EntryList 中。 
5.	处于 ContentionList、EntryList、WaitSet 中的线程都处于阻塞状态，该阻塞是由操作系统来完成的（Linux 内核下采用 pthread_mutex_lock 内核函数实现的）。 
6.	Synchronized 是非公平锁。 Synchronized 在线程进入 ContentionList 时，等待的线程会先尝试自旋获取锁，如果获取不到就进入 ContentionList，这明显对于已经进入队列的线程是不公平的，还有一个不公平的事情就是自旋获取锁的线程还可能直接抢占 OnDeck 线程的锁资源。 
参考：https://blog.csdn.net/zqz_zqz/article/details/70233767 
7.	每个对象都有个 monitor 对象，加锁就是在竞争 monitor 对象，代码块加锁是在前后分别加上 monitorenter 和 monitorexit 指令来实现的，方法加锁是通过一个标记位来判断的 
8.	synchronized 是一个重量级操作，需要调用操作系统相关接口，性能是低效的，有可能给线程加锁消耗的时间比有用操作消耗的时间更多。 
9.	Java1.6，synchronized进行了很多的优化，有适应自旋、锁消除、锁粗化、轻量级锁及偏向锁等，效率有了本质上的提高。在之后推出的 Java1.7 与 1.8 中，均对该关键字的实现机理做了优化。引入了偏向锁和轻量级锁。都是在对象头中有标记位，不需要经过操作系统加锁。 
10.	锁可以从偏向锁升级到轻量级锁，再升级到重量级锁。这种升级过程叫做锁膨胀； 
11.	JDK 1.6 中默认是开启偏向锁和轻量级锁，可以通过-XX:-UseBiasedLocking 来禁用偏向锁。 
1.1.1.1	ReentrantLock 
 ReentantLock 继承接口 Lock 并实现了接口中定义的方法，他是一种可重入锁，除了能完成 synchronized 所能完成的所有工作外，还提供了诸如可响应中断锁、可轮询锁请求、定时锁等避免多线程死锁的方法。 （等待可中断、可实现公平锁及锁可以绑定多个条件。）
等待可中断：是指当持有锁的线程长期不释放锁的时候，正在等待的线程可以选择放弃等待，改为处理其他事情。可中断特性对处理执行时间非常长的同步块很有帮助。
公平锁：是指多个线程在等待同一个锁时，必须按照申请锁的时间顺序来依次获得锁；而非公平锁则不保证这一点，在锁被释放时，任何一个等待锁的线程都有机会获得锁。synchronized中的锁是非公平的，ReentrantLock在默认情况下也是非公平的，但可以通过带布尔值的构造函数要求使用公平锁。不过一旦使用了公平锁，将会导致ReentrantLock的性能急剧下降，会明显影响吞吐量。·
锁绑定多个条件：是指一个ReentrantLock对象可以同时绑定多个Condition对象。在synchronized中，锁对象的wait()跟它的notify()或者notifyAll()方法配合可以实现一个隐含的条件，如果要和多于一个的条件关联的时候，就不得不额外添加一个锁；而ReentrantLock则无须这样做，多次调用newCondition()方法即可。
1.1.1.1.1	java.util.concurrent.locks.Lock
    /**
     * Acquires the lock.
     *
     * <p>If the lock is not available then the current thread becomes
     * disabled for thread scheduling purposes and lies dormant until the
     * lock has been acquired.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>A {@code Lock} implementation may be able to detect erroneous use
     * of the lock, such as an invocation that would cause deadlock, and
     * may throw an (unchecked) exception in such circumstances.  The
     * circumstances and the exception type must be documented by that
     * {@code Lock} implementation.
     * - 执行此方法时, 如果锁处于空闲状态, 当前线程将获取到锁. 相反, 如果锁已经被其他线程持有, 
     * - 将禁用当前线程, 直到当前线程获取到锁
     */
    void lock();

    /**
     * Acquires the lock only if it is free at the time of invocation.
     *
     * <p>Acquires the lock if it is available and returns immediately
     * with the value {@code true}.
     * If the lock is not available then this method will return
     * immediately with the value {@code false}.
     *
     * <p>A typical usage idiom for this method would be:
     *  <pre> {@code
     * Lock lock = ...;
     * if (lock.tryLock()) {
     *   try {
     *     // manipulate protected state
     *   } finally {
     *     lock.unlock();
     *   }
     * } else {
     *   // perform alternative actions
     * }}</pre>
     *
     * This usage ensures that the lock is unlocked if it was acquired, and
     * doesn't try to unlock if the lock was not acquired.
     *
     * @return {@code true} if the lock was acquired and
     *         {@code false} otherwise
     * - 如果锁可用, 则获取锁, 并立即返回 true, 否则返回 false. 
     * - 与lock()的区别在于, tryLock()只是"试图"获取锁, 如果锁不可用, 不会导致当前线程被禁用, 
     * - 当前线程仍然继续往下执行代码. 
     * - 而 lock()方法则是一定要获取到锁, 如果锁不可用, 就一直等待, 在未获得锁之前,当前线程并不继续向下执行.  
     */
    boolean tryLock();


    /**
     * Releases the lock.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>A {@code Lock} implementation will usually impose
     * restrictions on which thread can release a lock (typically only the
     * holder of the lock can release it) and may throw
     * an (unchecked) exception if the restriction is violated.
     * Any restrictions and the exception
     * type must be documented by that {@code Lock} implementation.
     * - 当前线程将释放持有的锁. 锁只能由持有者释放, 如果线程并不持有锁, 却执行该方法, 可能导致异常的发生
     */
    void unlock();


    /**
     * Returns a new {@link Condition} instance that is bound to this
     * {@code Lock} instance.
     *
     * <p>Before waiting on the condition the lock must be held by the
     * current thread.
     * A call to {@link Condition#await()} will atomically release the lock
     * before waiting and re-acquire the lock before the wait returns.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>The exact operation of the {@link Condition} instance depends on
     * the {@code Lock} implementation and must be documented by that
     * implementation.
     *
     * @return A new {@link Condition} instance for this {@code Lock} instance
     * @throws UnsupportedOperationException if this {@code Lock}
     *         implementation does not support conditions
     * - 条件对象，获取等待通知组件。该组件和当前的锁绑定，
     * - 当前线程只有获取了锁，才能调用该组件的 await()方法，而调用后，当前线程将释放锁
     */
Condition newCondition();


1.1.1.1.2	java.util.concurrent.locks.ReentrantLock
    /**
     * Queries the number of holds on this lock by the current thread.
     *
     * <p>A thread has a hold on a lock for each lock action that is not
     * matched by an unlock action.
     *
     * <p>The hold count information is typically only used for testing and
     * debugging purposes. For example, if a certain section of code should
     * not be entered with the lock already held then we can assert that
     * fact:
     *
     *  <pre> {@code
     * class X {
     *   ReentrantLock lock = new ReentrantLock();
     *   // ...
     *   public void m() {
     *     assert lock.getHoldCount() == 0;
     *     lock.lock();
     *     try {
     *       // ... method body
     *     } finally {
     *       lock.unlock();
     *     }
     *   }
     * }}</pre>
     *
     * @return the number of holds on this lock by the current thread,
     *         or zero if this lock is not held by the current thread
     * - 查询当前线程保持此锁的次数，也就是执行此线程执行lock方法的次数
     */
    public int getHoldCount() {
        return sync.getHoldCount();
    }

    /**
     * Returns an estimate of the number of threads waiting to
     * acquire this lock.  The value is only an estimate because the number of
     * threads may change dynamically while this method traverses
     * internal data structures.  This method is designed for use in
     * monitoring of the system state, not for synchronization
     * control.
     * -- 返回正等待获取此锁的线程估计数，比如启动 10 个线程，1 个线程获得锁，此时返回的是 9
     *
     * @return the estimated number of threads waiting for this lock
     */
    public final int getQueueLength() {
        return sync.getQueueLength();
    }

    /**
     * Returns an estimate of the number of threads waiting on the
     * given condition associated with this lock. Note that because
     * timeouts and interrupts may occur at any time, the estimate
     * serves only as an upper bound on the actual number of waiters.
     * This method is designed for use in monitoring of the system
     * state, not for synchronization control.
     *
     * @param condition the condition
     * @return the estimated number of waiting threads
     * @throws IllegalMonitorStateException if this lock is not held
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this lock
     * @throws NullPointerException if the condition is null
     * -- 返回等待与此锁相关的给定条件的线程估计数。
     * -- 比如 10 个线程，用同一个 condition 对象，
     * -- 并且此时这 10 个线程都执行了 condition 对象的 await 方法，那么此时执行此方法返回 10 
     */
    public int getWaitQueueLength(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.getWaitQueueLength((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    /**
     * Queries whether any threads are waiting on the given condition
     * associated with this lock. Note that because timeouts and
     * interrupts may occur at any time, a {@code true} return does
     * not guarantee that a future {@code signal} will awaken any
     * threads.  This method is designed primarily for use in
     * monitoring of the system state.
     *
     * @param condition the condition
     * @return {@code true} if there are any waiting threads
     * @throws IllegalMonitorStateException if this lock is not held
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this lock
     * @throws NullPointerException if the condition is null
     * -- 查询是否有线程等待与此锁有关的给定条件
     * -- (condition)，对于指定 contidion 对象，有多少线程执行了 condition.await 方法

     */
    public boolean hasWaiters(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.hasWaiters((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    /**
     * Queries whether the given thread is waiting to acquire this
     * lock. Note that because cancellations may occur at any time, a
     * {@code true} return does not guarantee that this thread
     * will ever acquire this lock.  This method is designed primarily for use
     * in monitoring of the system state.
     *
     * @param thread the thread
     * @return {@code true} if the given thread is queued waiting for this lock
     * @throws NullPointerException if the thread is null
     * -- 查询给定线程是否等待获取此锁
     */
    public final boolean hasQueuedThread(Thread thread) {
        return sync.isQueued(thread);
    }

    /**
     * Queries whether any threads are waiting to acquire this lock. Note that
     * because cancellations may occur at any time, a {@code true}
     * return does not guarantee that any other thread will ever
     * acquire this lock.  This method is designed primarily for use in
     * monitoring of the system state.
     *
     * @return {@code true} if there may be other threads waiting to
     *         acquire the lock
     * -- 是否有线程等待锁
     */
    public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }


    /**
     * Returns {@code true} if this lock has fairness set true.
     *
     * @return {@code true} if this lock has fairness set true
     * -- 该锁是否是公平锁
     */
    public final boolean isFair() {
        return sync instanceof FairSync;
    }

    /**
     * Queries if this lock is held by the current thread.
     *
     * <p>Analogous to the {@link Thread#holdsLock(Object)} method for
     * built-in monitor locks, this method is typically used for
     * debugging and testing. For example, a method that should only be
     * called while a lock is held can assert that this is the case:
     *
     *  <pre> {@code
     * class X {
     *   ReentrantLock lock = new ReentrantLock();
     *   // ...
     *
     *   public void m() {
     *       assert lock.isHeldByCurrentThread();
     *       // ... method body
     *   }
     * }}</pre>
     *
     * <p>It can also be used to ensure that a reentrant lock is used
     * in a non-reentrant manner, for example:
     *
     *  <pre> {@code
     * class X {
     *   ReentrantLock lock = new ReentrantLock();
     *   // ...
     *
     *   public void m() {
     *       assert !lock.isHeldByCurrentThread();
     *       lock.lock();
     *       try {
     *           // ... method body
     *       } finally {
     *           lock.unlock();
     *       }
     *   }
     * }}</pre>
     *
     * @return {@code true} if current thread holds this lock and
     *         {@code false} otherwise
     * -- 当前线程是否保持锁锁定，线程的执行 lock 方法的前后分别是 false 和 true 
     */
    public boolean isHeldByCurrentThread() {
        return sync.isHeldExclusively();
    }

    /**
     * Queries if this lock is held by any thread. This method is
     * designed for use in monitoring of the system state,
     * not for synchronization control.
     *
     * @return {@code true} if any thread holds this lock and
     *         {@code false} otherwise
     * --- 此锁是否有任意线程占用
     */
    public boolean isLocked() {
        return sync.isLocked();
    }


1.1.1.1.2.1	tryLock

    /**
     * 
     * Acquires the lock only if it is not held by another thread at the time
     * of invocation.仅在调用时锁未被另一个线程保持的情况下，才获取该锁。 
     * 1）如果该锁没有被另一个线程保持，并且立即返回 true 值，则将锁的保持计数设置为 1。
     * 即使已将此锁设置为使用公平排序策略，但是调用 tryLock() 仍将 立即获取锁（如果有可用的），
     * 而不管其他线程当前是否正在等待该锁。在某些情况下，此“闯入”行为可能很有用，即使它会打破公
     * 平性也如此。如果希望遵守此锁的公平设置，则使用 tryLock(0, TimeUnit.SECONDS) 
     * ，它几乎是等效的（也检测中断）
     * <p>Acquires the lock if it is not held by another thread and
     * returns immediately with the value {@code true}, setting the
     * lock hold count to one. Even when this lock has been set to use a
     * fair ordering policy, a call to {@code tryLock()} <em>will</em>
     * immediately acquire the lock if it is available, whether or not
     * other threads are currently waiting for the lock.
     * This &quot;barging&quot; behavior can be useful in certain
     * circumstances, even though it breaks fairness. If you want to honor
     * the fairness setting for this lock, then use
     * {@link #tryLock(long, TimeUnit) tryLock(0, TimeUnit.SECONDS) }
     * which is almost equivalent (it also detects interruption).
     * 2）如果当前线程已经保持此锁，则将保持计数加 1，该方法将返回 true。 
     * <p>If the current thread already holds this lock then the hold
     * count is incremented by one and the method returns {@code true}.
     * 3）如果锁被另一个线程保持，则此方法将立即返回 false 值。 
     * <p>If the lock is held by another thread then this method will return
     * immediately with the value {@code false}.
     * return：如果锁是自由的并且被当前线程获取，或者当前线程已经保持该锁，则返回 true；否则返回 false
     * @return {@code true} if the lock was free and was acquired by the
     *         current thread, or the lock was already held by the current
     *         thread; and {@code false} otherwise
     */
    public boolean tryLock() {
        return sync.nonfairTryAcquire(1);
    }

1.1.1.1.2.2	lock
    /**
     * Acquires the lock.
     * 1.如果该锁没有被另一个线程保持，则获取该锁并立即返回，将锁的保持计数设置为 1
     * <p>Acquires the lock if it is not held by another thread and returns
     * immediately, setting the lock hold count to one.
     * 2.如果当前线程已经保持该锁，则将保持计数加 1，并且该方法立即返回
     * <p>If the current thread already holds the lock then the hold
     * count is incremented by one and the method returns immediately.
     * 3.如果该锁被另一个线程保持，则出于线程调度的目的，禁用当前线程，
     *            并且在获得锁之前，该线程将一直处于休眠状态，此时锁保持计数被设置为 1
     * <p>If the lock is held by another thread then the
     * current thread becomes disabled for thread scheduling
     * purposes and lies dormant until the lock has been acquired,
     * at which time the lock hold count is set to one.
     */
    public void lock() {
        sync.lock();
    }

1.1.1.1.2.3	lockInterruptibly

    /**
     * 1.如果当前线程未被中断，则获取锁。 
     * Acquires the lock unless the current thread is
     * {@linkplain Thread#interrupt interrupted}.
     *
     * 2.如果该锁没有被另一个线程保持，则获取该锁并立即返回，将锁的保持计数设置为 1。 
     * <p>Acquires the lock if it is not held by another thread and returns
     * immediately, setting the lock hold count to one.
     *
     * 3.如果当前线程已经保持此锁，则将保持计数加 1，并且该方法立即返回。 
     * <p>If the current thread already holds this lock then the hold count
     * is incremented by one and the method returns immediately.
     *
     * 4.如果锁被另一个线程保持，则出于线程调度目的，禁用当前线程，并且在发生以下两种情况之一以前，该线程将一直处于休眠状态： 
     * <p>If the lock is held by another thread then the
     * current thread becomes disabled for thread scheduling
     * purposes and lies dormant until one of two things happens:
     *
     * <ul>
     * 4.1锁由当前线程获得
     * <li>The lock is acquired by the current thread; or
     * 4.2其他某个线程中断当前线程
     * <li>Some other thread {@linkplain Thread#interrupt interrupts} the
     * current thread.
     *
     * </ul>
     * 5.如果当前线程获得该锁，则将锁保持计数设置为 1,如果当前线程出现两种情况则抛出 InterruptedException，并且清除当前线程的已中断状态。
     * <p>If the lock is acquired by the current thread then the lock hold
     * count is set to one.
     *
     * <p>If the current thread:
     *
     * <ul>
     * 5.1在进入此方法时已经设置了该线程的中断状态
     * <li>has its interrupted status set on entry to this method; or
     * 5.2在等待获取锁的同时被中断
     * <li>is {@linkplain Thread#interrupt interrupted} while acquiring
     * the lock,
     *
     * </ul>
     *
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     * 6.在此实现中，因为此方法是一个显式中断点，所以要优先考虑响应中断，而不是响应锁的普通获取或重入获取
     * <p>In this implementation, as this method is an explicit
     * interruption point, preference is given to responding to the
     * interrupt over normal or reentrant acquisition of the lock.
     *
     * @throws InterruptedException if the current thread is interrupted
     * -- 如果当前线程未被中断，获取锁
     */
	/**
	 * lockInterruptibly()方法能够中断等待获取锁的线程。
	 * 当两个线程同时通过lock.lockInterruptibly()获取某个锁时，假若此时线程A获取到了锁，而线程B只有等待，
	 * 那么对线程B调用threadB.interrupt()方法能够中断线程B的等待过程。
	 */
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }
1.1.1.1.2.4	lock、trylock、lockInterruptibly
1）lock(), 拿不到lock就不罢休，不然线程就一直block。 比较无赖的做法。
2）tryLock()，马上返回，拿到lock就返回true，不然返回false。 比较潇洒的做法。    带时间限制的tryLock()，拿不到lock，就等一段时间，超时返回false。比较聪明的做法。
3）lockInterruptibly()就稍微难理解一些。先说说线程的打扰机制，每个线程都有一个 打扰标志。这里分两种情况，
1. 线程在sleep或wait,join， 此时如果别的进程调用此进程的 interrupt（）方法，此线程会被唤醒并被要求处理InterruptedException；(thread在做IO操作时也可能有类似行为，见java thread api)
2. 此线程在运行中， 则不会收到提醒。但是 此线程的 “打扰标志”会被设置， 可以通过isInterrupted()查看并 作出处理。lockInterruptibly()和上面的第一种情况是一样的， 线程在请求lock并被阻塞时，如果被interrupt，则“此线程会被唤醒并被要求处理InterruptedException”。并且如果线程已经被interrupt，再使用lockInterruptibly的时候，此线程也会被要求处理interruptedException
4）lock 和 lockInterruptibly，如果两个线程分别执行这两个方法，但此时中断这两个线程， lock 不会抛出异常，而 lockInterruptibly 会抛出异常。


1.1.1.1.3	示例

package com.sunld.thread;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockTest {
	
	private Lock lock = new ReentrantLock();
	//Lock lock=new ReentrantLock(true);//公平锁 
	//Lock lock=new ReentrantLock(false);//非公平锁
	private Condition condition = lock.newCondition();//创建 Condition
		 
	public void testMethod() { 
		try {
			lock.lock();//lock 加锁 
			//1：wait 方法等待： 
			//System.out.println("开始 wait");             
			condition.await(); 
			//通过创建 Condition 对象来使线程 wait，必须先执行 lock.lock 方法获得锁 
			//:2：signal 方法唤醒 
			condition.signal();//condition 对象的 signal 方法可以唤醒 wait 线程 
			for (int i = 0; i < 5; i++) { 
				System.out.println("ThreadName=" + Thread.currentThread().getName()+ (" " + (i + 1))); 
			}
		}catch(InterruptedException e) {
			e.printStackTrace(); 
		}finally {
			lock.unlock(); 
		}
	}
}


1.1.1.2	synchronized 和 ReentrantLock 的区别 
不要在Lock和Condition上使用wait、notiffy、notifyAll方法！
<div align=center>

![1589108538300.png](..\images\1589108538300.png)

</div>


1.1.1.1.1	两者的共同点
1.	都是用来协调多线程对共享对象、变量的访问 
2.	都是可重入锁，同一线程可以多次获得同一个锁 
3.	都保证了可见性和互斥性 
1.1.1.1.2	两者的不同点
1.	ReentrantLock 显示的获得、释放锁，synchronized 隐式获得释放锁 
2.	ReentrantLock 可响应中断、可轮回，synchronized 是不可以响应中断的，为处理锁的不可用性提供了更高的灵活性 
3.	ReentrantLock 是 API 级别的，synchronized 是 JVM 级别的 
4.	ReentrantLock 可以实现公平锁 
5.	ReentrantLock 通过 Condition 可以绑定多个条件 
6.	底层实现不一样， synchronized 是同步阻塞，使用的是悲观并发策略，lock 是同步非阻塞，采用的是乐观并发策略 
7.	Lock 是一个接口，而 synchronized 是 Java 中的关键字，synchronized 是内置的语言实现。 
8.	synchronized 在发生异常时，会自动释放线程占有的锁，因此不会导致死锁现象发生；而 Lock 在发生异常时，如果没有主动通过 unLock()去释放锁，则很可能造成死锁现象，因此使用 Lock 时需要在 finally 块中释放锁。 
9.	Lock 可以让等待锁的线程响应中断，而 synchronized 却不行，使用 synchronized 时，等待的线程会一直等待下去，不能够响应中断。 
10.	通过 Lock 可以知道有没有成功获取锁，而 synchronized 却无法办到。 
11.	Lock 可以提高多个线程进行读操作的效率，既就是实现读写锁等。  



1.1.1.2	分段锁
1.1.1.2.1	ConcurrentHashMap
java中的ConcurrentHashMap在jdk1.8之前的版本，使用一个Segment 数组
Segment< K,V >[] segments
Segment继承自ReenTrantLock，所以每个Segment就是个可重入锁，每个Segment 有一个HashEntry< K,V >数组用来存放数据，put操作时，先确定往哪个Segment放数据，只需要锁定这个Segment，执行put，其它的Segment不会被锁定；所以数组中有多少个Segment就允许同一时刻多少个线程存放数据，这样增加了并发能力。

1.1.1	减小锁粒度 
 
减小锁粒度是指缩小锁定对象的范围，从而减小锁冲突的可能性，从而提高系统的并发能力。减小锁粒度是一种削弱多线程锁竞争的有效手段，这种技术典型的应用是 ConcurrentHashMap(高性能的 HashMap)类的实现。对于 HashMap 而言，最重要的两个方法是 get 与 set 方法，如果我们对整个 HashMap 加锁，可以得到线程安全的对象，但是加锁粒度太大。Segment 的大小也被称为 ConcurrentHashMap 的并发度。 
1.1.2	ConcurrentHashMap 分段锁 
 
ConcurrentHashMap，它内部细分了若干个小的 HashMap，称之为段(Segment)。默认情况下一个 ConcurrentHashMap 被进一步细分为 16 个段，既就是锁的并发度。 
如果需要在 ConcurrentHashMap 中添加一个新的表项，并不是将整个 HashMap 加锁，而是首先根据hashcode得到该表项应该存放在哪个段中，然后对该段加锁，并完成put操作。在多线程环境中，如果多个线程同时进行put操作，只要被加入的表项不存放在同一个段中，则线程间可以做到真正的并行。 
ConcurrentHashMap 是由Segment 数组结构和HashEntry 数组结构组成 
ConcurrentHashMap 是由 Segment 数组结构和 HashEntry 数组结构组成。Segment 是一种可重入锁 ReentrantLock，在 ConcurrentHashMap 里扮演锁的角色，HashEntry 则用于存储键值对数据。一个 ConcurrentHashMap 里包含一个 Segment 数组，Segment 的结构和 HashMap 类似，是一种数组和链表结构， 一个 Segment 里包含一个 HashEntry 数组，每个 HashEntry 是一个链表结构的元素， 每个 Segment 守护一个 HashEntry 数组里的元素,当对 HashEntry 数组的数据进行修改时，必须首先获得它对应的 Segment 锁。 
<div align=center>

![1589108702724.png](..\images\1589108702724.png)

</div>

1.1.1.2.2	LongAdder
LongAdder 实现思路也类似ConcurrentHashMap，LongAdder有一个根据当前并发状况动态改变的Cell数组，Cell对象里面有一个long类型的value用来存储值; 
开始没有并发争用的时候或者是cells数组正在初始化的时候，会使用cas来将值累加到成员变量的base上，在并发争用的情况下，LongAdder会初始化cells数组，在Cell数组中选定一个Cell加锁，数组有多少个cell，就允许同时有多少线程进行修改，最后将数组中每个Cell中的value相加，在加上base的值，就是最终的值；cell数组还能根据当前线程争用情况进行扩容，初始长度为2，每次扩容会增长一倍，直到扩容到大于等于cpu数量就不再扩容，这也就是为什么LongAdder比cas和AtomicInteger效率要高的原因，后面两者都是volatile+cas实现的，他们的竞争维度是1，LongAdder的竞争维度为“Cell个数+1”为什么要+1？因为它还有一个base，如果竞争不到锁还会尝试将数值加到base上；
1.1.1.2.3	LinkedBlockingQueue
LinkedBlockingQueue也体现了这样的思想，在队列头入队，在队列尾出队，入队和出队使用不同的锁，相对于LinkedBlockingArray只有一个锁效率要高；
拆锁的粒度不能无限拆，最多可以将一个锁拆为当前cup数量个锁即可；
1.1.1.3	使用读写锁
ReentrantReadWriteLock 是一个读写锁，读操作加读锁，可以并发读，写操作使用写锁，只能单线程写；
1.1.1.4	读写分离
CopyOnWriteArrayList 、CopyOnWriteArraySet 
CopyOnWrite容器即写时复制的容器。通俗的理解是当我们往一个容器添加元素的时候，不直接往当前容器添加，而是先将当前容器进行Copy，复制出一个新的容器，然后新的容器里添加元素，添加完元素之后，再将原容器的引用指向新的容器。这样做的好处是我们可以对CopyOnWrite容器进行并发的读，而不需要加锁，因为当前容器不会添加任何元素。所以CopyOnWrite容器也是一种读写分离的思想，读和写不同的容器。 
　CopyOnWrite并发容器用于读多写少的并发场景，因为，读的时候没有锁，但是对其进行更改的时候是会加锁的，否则会导致多个线程同时复制出多个副本，各自修改各自的；
1.1.1.5	使用CAS（比较并交换-乐观锁机制-锁自旋）
如果需要同步的操作执行速度非常快，并且线程竞争并不激烈，这时候使用cas效率会更高，因为加锁会导致线程的上下文切换，如果上下文切换的耗时比同步操作本身更耗时，且线程对资源的竞争不激烈，使用volatiled+cas操作会是非常高效的选择；
1.1.1.5.1	概念及特性 
CAS（Compare And Swap/Set）比较并交换，CAS 算法的过程是这样：它包含 3 个参数 CAS(V,E,N)。V 表示要更新的变量(内存值)，E 表示预期值(旧的)，N 表示新值。当且仅当 V 值等于 E 值时，才会将 V 的值设为 N，如果 V 值和 E 值不同，则说明已经有其他线程做了更新，则当前线程什么都不做。最后，CAS 返回当前 V 的真实值。 
CAS 操作是抱着乐观的态度进行的(乐观锁)，它总是认为自己可以成功完成操作。当多个线程同时使用 CAS 操作一个变量时，只有一个会胜出，并成功更新，其余均会失败。失败的线程不会被挂起，仅是被告知失败，并且允许再次尝试，当然也允许失败的线程放弃操作。基于这样的原理，
CAS 操作即使没有锁，也可以发现其他线程对当前线程的干扰，并进行恰当的处理。 
1.1.1.5.2	原子包 java.util.concurrent.atomic（锁自旋） 
JDK1.5 的原子包：java.util.concurrent.atomic 这个包里面提供了一组原子类。其基本的特性就是在多线程环境下，当有多个线程同时执行这些类的实例包含的方法时，具有排他性，即当某个线程进入方法，执行其中的指令时，不会被其他线程打断，而别的线程就像自旋锁一样，一直等
到该方法执行完成，才由 JVM 从等待队列中选择一个另一个线程进入，这只是一种逻辑上的理解。 
相对于对于 synchronized 这种阻塞算法，CAS 是非阻塞算法的一种常见实现。由于一般 CPU 切换时间比 CPU 指令集操作更加长， 所以 J.U.C 在性能上有了很大的提升。如下代码： 
 public class AtomicInteger extends Number implements java.io.Serializable {   
        private volatile int value;   public final int get() {              return value;   
     }   
     public final int getAndIncrement() {   
        for (;;) {  //CAS 自旋，一直尝试，直达成功 
           int current = get();               int next = current + 1;               if (compareAndSet(current, next))                   return current;   
        }   
    }   
    public final boolean compareAndSet(int expect, int update) {           return unsafe.compareAndSwapInt(this, valueOffset, expect, update);   
    }   
} 
getAndIncrement 采用了 CAS 操作，每次从内存中读取数据然后将此数据和+1 后的结果进行 CAS 操作，如果成功就返回结果，否则重试直到成功为止。而 compareAndSet 利用 JNI 来完成
CPU 指令的操作。 
<div align=center>

![1589108594441.png](..\images\1589108594441.png)

</div>

 
 
1.1.1.1.1	ABA 问题 
CAS 会导致“ABA 问题”。CAS 算法实现一个重要前提需要取出内存中某时刻的数据，而在下时刻比较并替换，那么在这个时间差类会导致数据的变化。 
比如说一个线程 one 从内存位置 V 中取出 A，这时候另一个线程 two 也从内存中取出 A，并且 two 进行了一些操作变成了 B，然后 two 又将 V 位置的数据变成 A，这时候线程 one 进行 CAS 操作发现内存中仍然是 A，然后 one 操作成功。尽管线程 one 的 CAS 操作成功，但是不代表这个过程就是没有问题的。 
部分乐观锁的实现是通过版本号（version）的方式来解决 ABA 问题，乐观锁每次在执行数据的修改操作时，都会带上一个版本号，一旦版本号和数据的版本号一致就可以执行修改操作并对版本号执行+1 操作，否则就执行失败。因为每次操作的版本号都会随之增加，所以不会出现 ABA 问题，因为版本号只会增加不会减少。
1.1.1.2	消除缓存行的伪共享
除了我们在代码中使用的同步锁和jvm自己内置的同步锁外，还有一种隐藏的锁就是缓存行，它也被称为性能杀手。 
在多核cup的处理器中，每个cup都有自己独占的一级缓存、二级缓存，甚至还有一个共享的三级缓存，为了提高性能，cpu读写数据是以缓存行为最小单元读写的；32位的cpu缓存行为32字节，64位cup的缓存行为64字节，这就导致了一些问题。 
例如，多个不需要同步的变量因为存储在连续的32字节或64字节里面，当需要其中的一个变量时，就将它们作为一个缓存行一起加载到某个cup-1私有的缓存中（虽然只需要一个变量，但是cpu读取会以缓存行为最小单位，将其相邻的变量一起读入），被读入cpu缓存的变量相当于是对主内存变量的一个拷贝，也相当于变相的将在同一个缓存行中的几个变量加了一把锁，这个缓存行中任何一个变量发生了变化，当cup-2需要读取这个缓存行时，就需要先将cup-1中被改变了的整个缓存行更新回主存（即使其它变量没有更改），然后cup-2才能够读取，而cup-2可能需要更改这个缓存行的变量与cpu-1已经更改的缓存行中的变量是不一样的，所以这相当于给几个毫不相关的变量加了一把同步锁； 
为了防止伪共享，不同jdk版本实现方式是不一样的： 
1. 在jdk1.7之前会 将需要独占缓存行的变量前后添加一组long类型的变量，依靠这些无意义的数组的填充做到一个变量自己独占一个缓存行； 
2. 在jdk1.7因为jvm会将这些没有用到的变量优化掉，所以采用继承一个声明了好多long变量的类的方式来实现； 
3. 在jdk1.8中通过添加sun.misc.Contended注解来解决这个问题，若要使该注解有效必须在jvm中添加以下参数： 
-XX:-RestrictContended
sun.misc.Contended注解会在变量前面添加128字节的padding将当前变量与其他变量进行隔离； 


1.1.1.3	总结
<div align=center>

![1589108624691.png](..\images\1589108624691.png)

</div>

synchronized的执行过程： 
1. 检测Mark Word里面是不是当前线程的ID，如果是，表示当前线程处于偏向锁 
2. 如果不是，则使用CAS将当前线程的ID替换Mard Word，如果成功则表示当前线程获得偏向锁，置偏向标志位1 
3. 如果失败，则说明发生竞争，撤销偏向锁，进而升级为轻量级锁。 
4. 当前线程使用CAS将对象头的Mark Word替换为锁记录指针，如果成功，当前线程获得锁 
5. 如果失败，表示其他线程竞争锁，当前线程便尝试使用自旋来获取锁。 
6. 如果自旋成功则依然处于轻量级状态。 
7. 如果自旋失败，则升级为重量级锁。
上面几种锁都是JVM自己内部实现，当我们执行synchronized同步块的时候jvm会根据启用的锁和当前线程的争用情况，决定如何执行同步操作；
在所有的锁都启用的情况下线程进入临界区时会先去获取偏向锁，如果已经存在偏向锁了，则会尝试获取轻量级锁，启用自旋锁，如果自旋也没有获取到锁，则使用重量级锁，没有获取到锁的线程阻塞挂起，直到持有锁的线程执行完同步块唤醒他们；
偏向锁是在无锁争用的情况下使用的，也就是同步开在当前线程没有执行完之前，没有其它线程会执行该同步块，一旦有了第二个线程的争用，偏向锁就会升级为轻量级锁，如果轻量级锁自旋到达阈值后，没有获取到锁，就会升级为重量级锁；
如果线程争用激烈，那么应该禁用偏向锁。
