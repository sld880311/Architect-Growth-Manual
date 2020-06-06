<!-- TOC -->

- [AbstractQueuedSynchronizer详解](#abstractqueuedsynchronizer详解)
  - [锁的实现要点](#锁的实现要点)
  - [公平锁与非公平锁的实现](#公平锁与非公平锁的实现)
    - [AbstractQueuedSynchronizer.acquire实现](#abstractqueuedsynchronizeracquire实现)
    - [Sync默认实现非公平锁](#sync默认实现非公平锁)
    - [FairSync.tryAcquire](#fairsynctryacquire)
  - [阻塞队列与唤醒机制](#阻塞队列与唤醒机制)

<!-- /TOC -->
# AbstractQueuedSynchronizer详解

<div align=center>

![1591455525399.png](..\images\1591455525399.png)

</div>

## 锁的实现要点

1. 需要一个state变量，标记该锁的状态,state变量至少有两个值：0、1。对state变量的操作，要确保线程安全，也就是会用到CAS
   
   ```java
    /**
     * The synchronization state.
     * 锁状态
     * 当state=0时，没有线程持有锁，exclusiveOwnerThread=null；
     * 当state=1时，有一个线程持有锁，exclusiveOwnerThread=该线程；
     * 当state>1时，说明该线程重入了该锁。
     */
    private volatile int state;
   ```

2. 需要记录当前是哪个线程持有锁。
   
   ```java
    /**
     * The current owner of exclusive mode synchronization.
     * 记录锁被哪个线程持有
     */
    private transient Thread exclusiveOwnerThread;
   ```

3. 需要底层支持对一个线程进行阻塞或唤醒操作,基于LockSupport中的park和Uunpark，实际调用的是Unsafe中的相关方法

    ```java
    public native void unpark(Object var1);
    public native void park(boolean var1, long var2);
    ```

    ```java
    public class LockSupport {
        
        public static void unpark(Thread thread) {
            if (thread != null)
                UNSAFE.unpark(thread);
        }

        public static void park() {
            UNSAFE.park(false, 0L);
        }
    }
    ```

4. 需要有一个队列维护所有阻塞的线程。这个队列也必须是线程安全的无锁队列，也需要用到CAS
   ```java
    /**
     * Head of the wait queue, lazily initialized.  Except for
     * initialization, it is modified only via method setHead.  Note:
     * If head exists, its waitStatus is guaranteed not to be
     * CANCELLED.
     */
    private transient volatile Node head;

    /**
     * Tail of the wait queue, lazily initialized.  Modified only via
     * method enq to add new wait node.
     */
    private transient volatile Node tail;
   ```

   <div align=center>

    ![1591456622328.png](..\images\1591456622328.png)

    ![1591456659535.png](..\images\1591456659535.png)

   </div>


## 公平锁与非公平锁的实现

<div align=center>

![1591456931424.png](..\images\1591456931424.png)

</div>

ReentrantLock中的实现：

```java
    /**
     * Sync object for non-fair locks
     */
    static final class NonfairSync extends Sync {

        /**
         * Performs lock.  Try immediate barge, backing up to normal
         * acquire on failure.
         */
        final void lock() {
            /**
             * 直接尝试获取锁，不考虑队列中是否存在其他线程
             */
            if (compareAndSetState(0, 1))
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);
        }
    }

    /**
     * Sync object for fair locks
     */
    static final class FairSync extends Sync {

        final void lock() {
            acquire(1);
        }
    }
```

### AbstractQueuedSynchronizer.acquire实现

```java
/**
* Acquires in exclusive mode, ignoring interrupts.  Implemented
* by invoking at least once {@link #tryAcquire},
* returning on success.  Otherwise the thread is queued, possibly
* repeatedly blocking and unblocking, invoking {@link
* #tryAcquire} until success.  This method can be used
* to implement method {@link Lock#lock}.
*
* @param arg the acquire argument.  This value is conveyed to
*        {@link #tryAcquire} but is otherwise uninterpreted and
*        can represent anything you like.
*/
public final void acquire(int arg) {
    if (!tryAcquire(arg) && 
            // 加入阻塞队列，并且阻塞该线程
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```

### Sync默认实现非公平锁

```java
    abstract static class Sync extends AbstractQueuedSynchronizer {

        /**
         * Performs non-fair tryLock.  tryAcquire is implemented in
         * subclasses, but both need nonfair try for trylock method.
         */
        final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {// 无锁情况下，进行抢锁
                if (compareAndSetState(0, acquires)) {
                    // 获取到锁，星期设置当前线程获取该锁
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            //重入，只需要更新state
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
    }
```

### FairSync.tryAcquire

```java
    static final class FairSync extends Sync {
        /**
         * Fair version of tryAcquire.  Don't grant access unless
         * recursive call or no waiters or is first.
         */
        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                // 只有当c==0（没有线程持有锁），并且排在队列的第1个时（即当队列中没有其他线程的时候），
                // 才去抢锁，否则继续排队
                if (!hasQueuedPredecessors() &&
                    compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
    }
```

## 阻塞队列与唤醒机制


