<!-- TOC -->

- [Java中的cas](#java中的cas)
  - [无锁的优点（效率高的原因）](#无锁的优点效率高的原因)
  - [CAS特点](#cas特点)
  - [具体实现](#具体实现)
    - [原子类操作](#原子类操作)
      - [AtomicInteger](#atomicinteger)
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