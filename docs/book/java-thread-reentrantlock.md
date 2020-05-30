<!-- TOC -->

- [ReentrantLock详解](#reentrantlock详解)
  - [特点](#特点)
  - [类结构](#类结构)
  - [源码分析](#源码分析)
    - [Lock](#lock)
    - [ReentrantLock](#reentrantlock)
      - [tryLock](#trylock)
      - [lock](#lock-1)
      - [lockInterruptibly](#lockinterruptibly)
      - [lock、trylock、lockInterruptibly对比](#locktrylocklockinterruptibly对比)
  - [示例](#示例)
    - [基本语法](#基本语法)
    - [可重入](#可重入)
    - [可中断（lockInterruptibly、lock）](#可中断lockinterruptiblylock)
      - [可中断结果](#可中断结果)
      - [不可中断结果](#不可中断结果)
    - [超时锁（tryLock）](#超时锁trylock)
      - [立即返回](#立即返回)
      - [超时返回](#超时返回)
    - [公平锁](#公平锁)
    - [条件变量(Condition)](#条件变量condition)
  - [synchronized 和 ReentrantLock 的关系](#synchronized-和-reentrantlock-的关系)
    - [两者的共同点](#两者的共同点)
    - [两者的不同点](#两者的不同点)
  - [参考](#参考)

<!-- /TOC -->
# ReentrantLock详解

## 特点

1. 继承接口Lock，一种可重入锁
2. 完成synchronized所能的工作
3. 提供诸如可**响应中断锁、可轮询锁请求、定时锁**等避免多线程死锁的方法
4. **等待可中断（可设置超时时间）**：当持有锁的线程长期不释放锁的时候，正在等待的线程可以选择放弃等待，改为处理其他事情。可中断特性对处理执行时间非常长的同步块很有帮助。
5. **公平锁**：多个线程在等待同一个锁时，必须按照申请锁的时间顺序来依次获得锁；而非公平锁则不保证这一点，在锁被释放时，任何一个等待锁的线程都有机会获得锁。synchronized中的锁是非公平的，ReentrantLock在默认情况下也是非公平的，但可以通过带布尔值的构造函数要求使用公平锁。不过一旦使用了公平锁，将会导致ReentrantLock的性能急剧下降，会明显影响吞吐量。
6. **锁绑定多个条件**：一个ReentrantLock对象可以同时绑定多个Condition对象。在synchronized中，锁对象的wait()跟它的notify()或者notifyAll()方法配合可以实现一个隐含的条件，如果要和多于一个的条件关联的时候，就不得不额外添加一个锁；而ReentrantLock则无须这样做，多次调用newCondition()方法即可。

## 类结构

<div align=center>

![ReentrantLock类图](..\images\ReentrantLock-Class.png)

</div>

## 源码分析


### Lock

```java
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
```

### ReentrantLock

```java
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
```

#### tryLock

```java
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
```

#### lock

```java
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
```

#### lockInterruptibly

```java
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
```

#### lock、trylock、lockInterruptibly对比

1. lock(), 拿不到lock就不罢休，不然线程就一直block。 比较无赖的做法。
2. tryLock()，马上返回，拿到lock就返回true，不然返回false。 比较潇洒的做法。带时间限制的tryLock()，拿不到lock，就等一段时间，超时返回false。比较聪明的做法。
3. lockInterruptibly()：打扰机制，每个线程都有一个 打扰标志。这里分两种情况，
   - 线程在sleep或wait,join， 此时如果别的进程调用此进程的 interrupt（）方法，此线程会被唤醒并被要求处理InterruptedException；(thread在做IO操作时也可能有类似行为，见java thread api)此线程在运行中， 则不会收到提醒。但是 此线程的 “打扰标志”会被设置， 可以通过isInterrupted()查看并 作出处理。lockInterruptibly()和上面的第一种情况是一样的， 线程在请求lock并被阻塞时，如果被interrupt，则“此线程会被唤醒并被要求处理InterruptedException”。并且如果线程已经被interrupt，再使用lockInterruptibly的时候，此线程也会被要求处理interruptedException
4. lock 和 lockInterruptibly，如果两个线程分别执行这两个方法，但此时中断这两个线程， lock 不会抛出异常，而 lockInterruptibly 会抛出异常。


## 示例

### 基本语法

```java
// 获取锁
reentrantLock.lock();
try {
    // 临界区
} finally {
    // 释放锁
    reentrantLock.unlock();
}
```

### 可重入

```java
package com.sunld.thread.lock;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author : sunliaodong
 * @version : V1.0.0
 * @description:
 * 可重入是指同一个线程如果首次获得了这把锁，那么因为它是这把锁的拥有者，因此有权利再次获取这把锁
 * 如果是不可重入锁，那么第二次获得锁时，自己也会被锁挡住
 * @date : 2020/5/30 20:41
 */
public class ReentrantLockReentryTest {
    private static final ReentrantLock lock = new ReentrantLock();

    public static void test1(){
        lock.lock();
        try {
            System.out.println("test1----");
            test2();
        }finally {
            lock.unlock();
        }
    }

    private static void test2() {
        lock.lock();
        try {
            System.out.println("test2----");
        }finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        ReentrantLockReentryTest.test1();
    }
}
//执行结果。。。。
//test1----
//test2----
```

### 可中断（lockInterruptibly、lock）

```java
package com.sunld.thread.lock;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author : sunliaodong
 * @version : V1.0.0
 * @description: 可中断测试
 * @date : 2020/5/30 20:54
 */
public class ReentrantLockInterruptedTest {
    private static final ReentrantLock lock = new ReentrantLock();

    public static void testInterruptedEnable(){
        Thread thread = new Thread(() -> {
            System.out.println("testInterruptedEnable:子线程开始：。。。");
            try {
                lock.lockInterruptibly();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("testInterruptedEnable:子线程等待锁的过程被中断");
            }
            try{
                System.out.println("testInterruptedEnable:子线程获得了锁。。");
            }finally {
                lock.unlock();
            }
        },"可中断线程！！");

        lock.lock();
        System.out.println("testInterruptedEnable:主线程获得了锁。。");
        thread.start();
        try {
            Thread.sleep(1000);
            thread.interrupt();
            System.out.println("testInterruptedEnable:子线程thread开始中断。。");
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("testInterruptedEnable:主线程等待锁的过程被中断");
        } finally {
            lock.unlock();
        }
    }

    public static void testInterruptedUnable(){
        Thread thread = new Thread(() -> {
            System.out.println("testInterruptedEnable:子线程开始：。。。");
            lock.lock();

            try{
                System.out.println("testInterruptedEnable:子线程获得了锁。。");
            }finally {
                lock.unlock();
            }
        },"可中断线程！！");

        lock.lock();
        System.out.println("testInterruptedEnable:主线程获得了锁。。");
        thread.start();
        try {
            Thread.sleep(1000);
            thread.interrupt();
            System.out.println("testInterruptedEnable:子线程thread开始中断。。");
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("testInterruptedEnable:主线程等待锁的过程被中断");
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
//        ReentrantLockInterruptedTest.testInterruptedEnable();
        ReentrantLockInterruptedTest.testInterruptedUnable();
    }
}
```

#### 可中断结果

```java
testInterruptedEnable:主线程获得了锁。。
testInterruptedEnable:子线程开始：。。。
testInterruptedEnable:子线程thread开始中断。。
testInterruptedEnable:子线程等待锁的过程被中断
testInterruptedEnable:子线程获得了锁。。
java.lang.InterruptedException
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.doAcquireInterruptibly(AbstractQueuedSynchronizer.java:898)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquireInterruptibly(AbstractQueuedSynchronizer.java:1222)
	at java.util.concurrent.locks.ReentrantLock.lockInterruptibly(ReentrantLock.java:335)
	at com.sunld.thread.lock.ReentrantLockInterruptedTest.lambda$testInterruptedEnable$0(ReentrantLockInterruptedTest.java:18)
	at java.lang.Thread.run(Thread.java:748)
Exception in thread "可中断线程！！" java.lang.IllegalMonitorStateException
	at java.util.concurrent.locks.ReentrantLock$Sync.tryRelease(ReentrantLock.java:151)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.release(AbstractQueuedSynchronizer.java:1261)
	at java.util.concurrent.locks.ReentrantLock.unlock(ReentrantLock.java:457)
	at com.sunld.thread.lock.ReentrantLockInterruptedTest.lambda$testInterruptedEnable$0(ReentrantLockInterruptedTest.java:26)
	at java.lang.Thread.run(Thread.java:748)

Process finished with exit code 0
```

#### 不可中断结果

```java
testInterruptedEnable:主线程获得了锁。。
testInterruptedEnable:子线程开始：。。。
testInterruptedEnable:子线程thread开始中断。。
testInterruptedEnable:子线程获得了锁。。
```

### 超时锁（tryLock）

```java
package com.sunld.thread.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author : sunliaodong
 * @version : V1.0.0
 * @description: 锁超时
 * @date : 2020/5/30 21:36
 */
public class ReentrantLockTimeOutTest {
    private static final ReentrantLock lock = new ReentrantLock();

    public static void timeOut0(){
        Thread thread = new Thread(() -> {
            System.out.println(" thread start ...");
            if(!lock.tryLock()){
                System.out.println(" thread get lock by 0s failed ...");
                return;
            }
            try {
                System.out.println(" thread get lock success..");
            }finally {
                lock.unlock();
                System.out.println(" thread release lock success..");
            }
        },"子线程！！！");
        lock.lock();
        System.out.println(" main thread get lock success..");
        thread.start();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println(" main thread interrupted..");
        } finally {
            lock.unlock();
            System.out.println(" main thread release lock success..");
        }
    }

    public static void timeOut1(){
        Thread thread = new Thread(() -> {
            System.out.println(" thread start ...");
            try {
                if(!lock.tryLock(1, TimeUnit.SECONDS)){
                    System.out.println(" thread get lock by 1s failed ...");
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println(" thread get lock interrupted ....");
            }
            try {
                System.out.println(" thread get lock success..");
            }finally {
                lock.unlock();
                System.out.println(" thread release lock success..");
            }
        },"子线程！！！");
        lock.lock();
        System.out.println(" main thread get lock success..");
        thread.start();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println(" main thread interrupted..");
        } finally {
            lock.unlock();
            System.out.println(" main thread release lock success..");
        }
    }

    public static void main(String[] args) {
        ReentrantLockTimeOutTest.timeOut0();
//        ReentrantLockTimeOutTest.timeOut1();
    }
}
```

#### 立即返回

```java
 main thread get lock success..
 thread start ...
 thread get lock by 0s failed ...
 main thread release lock success..
```

#### 超时返回

```java
 main thread get lock success..
 thread start ...
 thread get lock by 1s failed ...
 main thread release lock success..
```

### 公平锁

```java
package com.sunld.thread.lock;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author : sunliaodong
 * @version : V1.0.0
 * @description: 公平锁测试
 * 公平锁一般没有必要，会降低并发度，后面分析原理时会讲解
 * @date : 2020/5/30 22:02
 */
public class ReentrantLockFairTest {

    public static void testFair(boolean fair){
        ReentrantLock lock = new ReentrantLock(fair);
        lock.lock();
        for (int i = 0; i < 500; i++) {
            new Thread(() -> {
                lock.lock();
                try {
                    System.out.println(Thread.currentThread().getName() + " running...");
                } finally {
                    lock.unlock();
                }
            }, "t" + i).start();
        }
        // 1s 之后去争抢锁
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " start...");
            lock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + " running...");
            } finally {
                lock.unlock();
            }
        }, "强行写入").start();
        lock.unlock();
    }

    public static void main(String[] args) {
        //公平
        ReentrantLockFairTest.testFair(true);
        //非公平
        ReentrantLockFairTest.testFair(false);
    }
}
```

### 条件变量(Condition)

Condition的详细使用，参考：[Condition](book/java-condition.md)  

1. synchronized 中也有条件变量，就是waitSet，当条件不满足时进入waitSet 等待
2. ReentrantLock 的条件变量比 synchronized 强大之处在于，它是支持多个条件变量的
   - synchronized 是那些不满足条件的线程都在一间休息室等消息
   - ReentrantLock 支持多间休息室，有专门等烟的休息室、专门等早餐的休息室、唤醒时也是按休息室来唤醒
3. 使用要点
   - await 前需要获得锁
   - await 执行后，会释放锁，进入 conditionObject 等待
   - await 的线程被唤醒（或打断、或超时）取重新竞争 lock 锁
   - 竞争 lock 锁成功后，从 await 后继续执行

```java
package com.sunld.thread.lock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author : sunliaodong
 * @version : V1.0.0
 * @description: TODO
 * @date : 2020/5/30 22:08
 */
public class ReentrantLockConditionTest {
    private static final Lock lock = new ReentrantLock();
    //Lock lock=new ReentrantLock(true);//公平锁
    //Lock lock=new ReentrantLock(false);//非公平锁
    private static final Condition condition = lock.newCondition();//创建 Condition

    public static void testWait() {
        lock.lock();//lock 加锁
        try {
            //1：wait 方法等待：
            System.out.println("开始 wait");
            condition.await();
            //通过创建 Condition 对象来使线程 wait，必须先执行 lock.lock 方法获得锁
            for (int i = 0; i < 5; i++) {
                System.out.println("ThreadName=" + Thread.currentThread().getName()+ (" " + (i + 1)));
            }
        }catch(InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
            System.out.println("释放锁");
        }
    }

    public static void testNotify() {
        lock.lock();//lock 加锁
        try {
            //1：开始唤醒：
            System.out.println("开始 signal");
            //2：signal 方法唤醒
            //condition 对象的 signal 方法可以唤醒 wait 线程
            condition.signalAll();
        }finally {
            lock.unlock();
            System.out.println("唤醒释放锁");
        }
    }

    public static void main(String[] args) {
        new Thread(() -> {
            ReentrantLockConditionTest.testWait();
        }, "等待通知").start();

        new Thread(() -> {
            ReentrantLockConditionTest.testNotify();
        },"启动通知").start();
    }
}
/**
 * TODO:
 * 开始 wait
 * 开始 signal
 * 唤醒释放锁
 * ThreadName=等待通知 1
 * ThreadName=等待通知 2
 * ThreadName=等待通知 3
 * ThreadName=等待通知 4
 * ThreadName=等待通知 5
 * 释放锁
 */
```


## synchronized 和 ReentrantLock 的关系

不要在Lock和Condition上使用wait、notiffy、notifyAll方法！
<div align=center>

![1589108538300.png](..\images\1589108538300.png)

</div>


### 两者的共同点

1.	都是用来协调多线程对共享对象、变量的访问 
2.	都是可重入锁，同一线程可以多次获得同一个锁 
3.	都保证了可见性和互斥性 

### 两者的不同点

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

## 参考

