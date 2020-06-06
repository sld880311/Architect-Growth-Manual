<!-- TOC -->

- [Java中的cas](#java中的cas)
  - [无锁的优点（效率高的原因）](#无锁的优点效率高的原因)
  - [CAS特点](#cas特点)
  - [具体实现](#具体实现)
    - [原子类操作](#原子类操作)
      - [AtomicInteger](#atomicinteger)
      - [AtomicLong](#atomiclong)
      - [AtomicBoolean](#atomicboolean)
      - [AtomicStampedReference和AtomicMarkableReference](#atomicstampedreference和atomicmarkablereference)
        - [AtomicStampedReference](#atomicstampedreference)
        - [AtomicMarkableReference](#atomicmarkablereference)
      - [AtomicXXXFieldUpdater](#atomicxxxfieldupdater)
      - [Striped64与LongAdder](#striped64与longadder)
        - [原理](#原理)
        - [Striped64](#striped64)
        - [添加数据](#添加数据)
        - [聚合数据](#聚合数据)
  - [其他](#其他)
    - [ABA 问题](#aba-问题)
    - [消除缓存行的伪共享](#消除缓存行的伪共享)
  - [参考](#参考)

<!-- /TOC -->
# Java中的cas

## 无锁的优点（效率高的原因）

1. 不会出现上下文切换
2. 需要额外CPU的支持


## CAS特点

**结合CAS+volatile可以实现无锁并发。**

1. 场景：线程数少、多核 CPU 的场景下
2. CAS基于乐观锁实现思路，synchronized基于悲观锁的思路
3. CAS 体现的是无锁并发、无阻塞并发（竞争不激烈的前提下）
4. 概念：Compare And Swap/Set，3 个参数 CAS(V,E,N)。V 表示要更新的变量(内存值)，E 表示预期值(旧的)，N 表示新值。当且仅当 V 值等于 E 值时，才会将 V 的值设为 N，如果 V 值和 E 值不同，则说明已经有其他线程做了更新，则当前线程什么都不做。最后，CAS 返回当前 V 的真实值。 

## 具体实现

### 原子类操作

1. 引入时间：JDK1.5后
2. 包路径：java.util.concurrent.atomic
3. 实现原理（锁自旋）：在多线程环境下，当有多个线程同时执行这些类的实例包含的方法时，**具有排他性**，即当某个线程进入方法，执行其中的指令时，不会被其他线程打断，而别的线程就像自旋锁一样，一直等到该方法执行完成，才由 JVM 从等待队列中选择一个另一个线程进入，这只是一种逻辑上的理解。 

#### AtomicInteger

在JDK1.8中使用Unsafe类进行数据处理的实现。

```java
public class AtomicInteger extends Number implements java.io.Serializable {
    private static final long serialVersionUID = 6214790243416807050L;

    // setup to use Unsafe.compareAndSwapInt for updates
    /**
     * 使用Unsafe.compareAndSwapInt处理数据
     */
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long valueOffset;

    static {
        try {
            // 使用unsafe设置数据value的偏移地址
            valueOffset = unsafe.objectFieldOffset
                (AtomicInteger.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }

    // 保证内存可见性和禁止指令重排序
    private volatile int value;

   /**
     * Atomically increments by one the current value.
     *
     * @return the previous value
     */
    public final int getAndIncrement() {
        return unsafe.getAndAddInt(this, valueOffset, 1);
    }
}
```

在转化的时候，先通过反射（getDeclaredField）获取value成员变量对应的Field对象，再通过objectFieldOffset函数转化成valueOffset。此处的valueOffset就代表了value变量本身，后面执行CAS操作的时候，不是直接操作value，而是操作valueOffset。

#### AtomicLong

与AtomicInteger的实现方式类似。

#### AtomicBoolean

1. 背景：处理compareAndSet(true, false)问题
2. 实现方式：基于unsafe.compareAndSwapInt

```java
/**
  * Atomically sets the value to the given updated value
  * if the current value {@code ==} the expected value.
  *
  * @param expect the expected value
  * @param update the new value
  * @return {@code true} if successful. False return indicates that
  * the actual value was not equal to the expected value.
  */
public final boolean compareAndSet(boolean expect, boolean update) {
    int e = expect ? 1 : 0;
    int u = update ? 1 : 0;
    return unsafe.compareAndSwapInt(this, valueOffset, e, u);
}
```

#### AtomicStampedReference和AtomicMarkableReference

1. 处理ABA问题
2. 通过增加版本的概念处理
3. AtomicStampedReference版本是int类型
4. AtomicMarkableReference版本是boolean类型，还可能会出现ABA问题
5. 原理：通过“值”和“版本号”比对确认是否可以更新，可解决Integer型或者Long型的ABA问题

##### AtomicStampedReference

```java

package java.util.concurrent.atomic;

public class AtomicStampedReference<V> {

    private static class Pair<T> {
        final T reference;
        // 维护的版本
        final int stamp;
        private Pair(T reference, int stamp) {
            this.reference = reference;
            this.stamp = stamp;
        }
        static <T> Pair<T> of(T reference, int stamp) {
            return new Pair<T>(reference, stamp);
        }
    }

    private volatile Pair<V> pair;

    /**
     * Creates a new {@code AtomicStampedReference} with the given
     * initial values.
     *
     * @param initialRef the initial reference
     * @param initialStamp the initial stamp
     */
    public AtomicStampedReference(V initialRef, int initialStamp) {
        pair = Pair.of(initialRef, initialStamp);
    }

    

    /**
     * Atomically sets the value of both the reference and stamp
     * to the given update values if the
     * current reference is {@code ==} to the expected reference
     * and the current stamp is equal to the expected stamp.
     *
     * @param expectedReference the expected value of the reference
     * @param newReference the new value for the reference
     * @param expectedStamp the expected value of the stamp
     * @param newStamp the new value for the stamp
     * @return {@code true} if successful
     */
    public boolean compareAndSet(V   expectedReference,
                                 V   newReference,
                                 int expectedStamp,
                                 int newStamp) {
        Pair<V> current = pair;
        return
            expectedReference == current.reference &&
            expectedStamp == current.stamp &&
            ((newReference == current.reference &&
              newStamp == current.stamp) ||
             casPair(current, Pair.of(newReference, newStamp)));
    }
}

```

##### AtomicMarkableReference

```java
public class AtomicMarkableReference<V> {

    private static class Pair<T> {
        final T reference;
        // 维护的版本
        final boolean mark;
        private Pair(T reference, boolean mark) {
            this.reference = reference;
            this.mark = mark;
        }
        static <T> Pair<T> of(T reference, boolean mark) {
            return new Pair<T>(reference, mark);
        }
    }

    private volatile Pair<V> pair;

    /**
     * Creates a new {@code AtomicMarkableReference} with the given
     * initial values.
     *
     * @param initialRef the initial reference
     * @param initialMark the initial mark
     */
    public AtomicMarkableReference(V initialRef, boolean initialMark) {
        pair = Pair.of(initialRef, initialMark);
    }

    /**
     * Atomically sets the value of both the reference and mark
     * to the given update values if the
     * current reference is {@code ==} to the expected reference
     * and the current mark is equal to the expected mark.
     *
     * @param expectedReference the expected value of the reference
     * @param newReference the new value for the reference
     * @param expectedMark the expected value of the mark
     * @param newMark the new value for the mark
     * @return {@code true} if successful
     */
    public boolean compareAndSet(V       expectedReference,
                                 V       newReference,
                                 boolean expectedMark,
                                 boolean newMark) {
        Pair<V> current = pair;
        return
            expectedReference == current.reference &&
            expectedMark == current.mark &&
            ((newReference == current.reference &&
              newMark == current.mark) ||
             casPair(current, Pair.of(newReference, newMark)));
    }
}

```

#### AtomicXXXFieldUpdater

1. 处理类似类中的属性原始操作
2. 条件：成员变量必须是volatile的int类型（不能是Integer包装类）

#### Striped64与LongAdder

在jdk1.8中针对long和double(具体实现基于long)分别增加了XXXAdder、XXXAccumulator来处理原则操作。具体类图如下：

<div align=center>

![1591450381138.png](..\images\1591450381138.png)

</div>

##### 原理

与ConcurrentHashMap类似。

1. 拆分：提高并发，把一个long拆分成多个long
2. 最终一致性：最终对数据进行汇总处理

<div align=center>

![1591450602899.png](..\images\1591450602899.png)

</div>

##### Striped64

```java

package java.util.concurrent.atomic;
import java.util.function.LongBinaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("serial")
abstract class Striped64 extends Number {
    @sun.misc.Contended static final class Cell {
        volatile long value;
        Cell(long x) { value = x; }
        final boolean cas(long cmp, long val) {
            return UNSAFE.compareAndSwapLong(this, valueOffset, cmp, val);
        }

        // Unsafe mechanics
        private static final sun.misc.Unsafe UNSAFE;
        private static final long valueOffset;
        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> ak = Cell.class;
                valueOffset = UNSAFE.objectFieldOffset
                    (ak.getDeclaredField("value"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /** Number of CPUS, to place bound on table size */
    static final int NCPU = Runtime.getRuntime().availableProcessors();

    /**
     * Table of cells. When non-null, size is a power of 2.
     */
    transient volatile Cell[] cells;

    /**
     * Base value, used mainly when there is no contention, but also as
     * a fallback during table initialization races. Updated via CAS.
     */
    transient volatile long base;

    /**
     * Spinlock (locked via CAS) used when resizing and/or creating Cells.
     */
    transient volatile int cellsBusy;

    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long BASE;
    private static final long CELLSBUSY;
    private static final long PROBE;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> sk = Striped64.class;
            BASE = UNSAFE.objectFieldOffset
                (sk.getDeclaredField("base"));
            CELLSBUSY = UNSAFE.objectFieldOffset
                (sk.getDeclaredField("cellsBusy"));
            Class<?> tk = Thread.class;
            PROBE = UNSAFE.objectFieldOffset
                (tk.getDeclaredField("threadLocalRandomProbe"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}

```

##### 添加数据

```java
/**
  * Adds the given value.
  *
  * @param x the value to add
  */
public void add(long x) {
    Cell[] as; long b, v; int m; Cell a;
    if ((as = cells) != null || !casBase(b = base, b + x)) {
        boolean uncontended = true;
        if (as == null || (m = as.length - 1) < 0 ||
            (a = as[getProbe() & m]) == null ||
            !(uncontended = a.cas(v = a.value, v + x)))
            longAccumulate(x, null, uncontended);
    }
}
```

##### 聚合数据

```java
/**
  * Returns the current sum.  The returned value is <em>NOT</em> an
  * atomic snapshot; invocation in the absence of concurrent
  * updates returns an accurate result, but concurrent updates that
  * occur while the sum is being calculated might not be
  * incorporated.
  *
  * @return the sum
  */
public long sum() {
    Cell[] as = cells; Cell a;
    long sum = base;
    if (as != null) {
        for (int i = 0; i < as.length; ++i) {
            if ((a = as[i]) != null)
                sum += a.value;
        }
    }
    return sum;
}
```

## 其他

### ABA 问题

1. 多个线程操作同一个数据时，数据被修改为A-B-A
2. 解决方式：使用版本号解决 

### 消除缓存行的伪共享

1. cpu中的一级缓存、二级缓存、三级缓存
2. 禁用 
   - jdk1.7前会将需要独占缓存行的变量前后添加一组long类型的变量，依靠这些无意义的数组的填充做到一个变量自己独占一个缓存行； 
   - 在jdk1.7因为jvm会将这些没有用到的变量优化掉，所以采用继承一个声明了好多long变量的类的方式来实现； 
   - 在jdk1.8中通过添加sun.misc.Contended注解来解决这个问题，若要使该注解有效必须在jvm中添加以下参数： -XX:-RestrictContended
   - sun.misc.Contended注解会在变量前面添加128字节的padding将当前变量与其他变量进行隔离； 

## 参考