<!-- TOC -->

- [HashMap详解](#hashmap%e8%af%a6%e8%a7%a3)
  - [概述](#%e6%a6%82%e8%bf%b0)
  - [Java7的实现方式](#java7%e7%9a%84%e5%ae%9e%e7%8e%b0%e6%96%b9%e5%bc%8f)
    - [核心数据结构](#%e6%a0%b8%e5%bf%83%e6%95%b0%e6%8d%ae%e7%bb%93%e6%9e%84)
    - [Entry静态内部类](#entry%e9%9d%99%e6%80%81%e5%86%85%e9%83%a8%e7%b1%bb)
    - [整体结构](#%e6%95%b4%e4%bd%93%e7%bb%93%e6%9e%84)
    - [关键属性](#%e5%85%b3%e9%94%ae%e5%b1%9e%e6%80%a7)
    - [构造函数](#%e6%9e%84%e9%80%a0%e5%87%bd%e6%95%b0)
    - [Put](#put)
    - [inflateTable、roundUpToPowerOf2](#inflatetablerounduptopowerof2)
    - [Hash函数](#hash%e5%87%bd%e6%95%b0)
    - [添加到链表中](#%e6%b7%bb%e5%8a%a0%e5%88%b0%e9%93%be%e8%a1%a8%e4%b8%ad)
    - [扩容](#%e6%89%a9%e5%ae%b9)
      - [transfer](#transfer)
    - [Get](#get)

<!-- /TOC -->

# HashMap详解

## 概述

1. HashMap是一种使用**数组+链表+红黑树**数据结构实现的Map
2. HashMap 根据键的 hashCode 值存储数据，大多数情况下可以直接定位到它的值，因而具有很快的访问速度，但遍历顺序不确定。
3. HashMap只允许一个key为null，允许多个value为null
4. HashMap 非线程安全，可以用 Collections 的 synchronizedMap 方法使 HashMap 具有线程安全的能力，或者使用 ConcurrentHashMap。

## Java7的实现方式

<div align=center>

![1589101907833.png](..\images\1589101907833.png)

</div>
数组+单向链表的方式实现，上图中，每个绿色的实体是嵌套类 Entry 的实例，Entry 包含四个属性：key, value, hash 值和用于单向链表的 next。

1. capacity：当前数组容量，始终保持 2^n，可以扩容，扩容后数组大小为当前的 2 倍。
2. loadFactor：负载因子，默认为 0.75。
3. threshold：扩容的阈值，等于 capacity * loadFactor

### 核心数据结构

```java
//主干数组，是一个Entry数组，初始值为空数组{}，主干数组的长度一定是2的次幂。
transient Entry<K,V>[] table = (Entry<K,V>[]) EMPTY_TABLE;
```

### Entry静态内部类

```java
static class Entry<K,V> implements Map.Entry<K,V> {
      final K key;
      V value;
      //存储指向下一个Entry的引用，单链表结构
      Entry<K,V> next;
      //对key的hashcode值进行hash运算后得到的值，存储在Entry，避免重复计算
      int hash;
      /**
      * Creates new entry.
      */
      Entry(int h, K k, V v, Entry<K,V> n) {
          value = v;
          next = n;
          key = k;
          hash = h;
      }
}
```

### 整体结构

<div align=center>

![1589101953955.png](..\images\1589101953955.png)

</div>

HashMap由数组+链表组成的，数组是HashMap的主体，链表则是主要为了解决哈希冲突而存在的，如果定位到的数组位置不含链表（当前entry的next指向null）,那么查找，添加等操作很快，仅需一次寻址即可；如果定位到的数组包含链表，对于添加操作，其时间复杂度为O(n)，首先遍历链表，存在即覆盖，否则新增；对于查找操作来讲，仍需遍历链表，然后通过key对象的equals方法逐一比对查找。所以，性能考虑，**HashMap中的链表出现越少，性能才会越好**。

### 关键属性

```java
/**实际存储的key-value键值对的个数*/
transient int size;

/**阈值，当table == {}时，该值为初始容量（初始容量默认为16）；
* 当table被填充时，为table分配内存空间后，threshold一般为 capacity*loadFactory。
* HashMap在进行扩容时需要参考threshold
*/
int threshold;

/**
* 负载因子，代表了table的填充度有多少，默认是0.75
* 为了减缓哈希冲突和自动扩容的临界值
* 当初始桶为16，等到满16个元素才扩容，某些桶里可能就有不止一个元素了。
* 所以加载因子默认为0.75，也就是说大小为16的HashMap，到了第13个元素，就会扩容成32。
*/
final float loadFactor;

/**HashMap被改变的次数，
* 由于HashMap非线程安全，在对HashMap进行迭代时，如果数据发生变化
* 抛出ConcurrentModificationException
*/
transient int modCount;
```

### 构造函数

在常规构造函数中没有为数组table分配内存空间（有一个入参为指定Map的构造器例外），而是在执行put操作的时候才真正构建table数组（懒加载）。

```java
public HashMap(int initialCapacity, float loadFactor) {
    //此处对传入的初始容量进行校验，最大不能超过MAXIMUM_CAPACITY = 1<<30(230)
    if (initialCapacity < 0)
        throw new IllegalArgumentException("Illegal initial capacity: " +
                                            initialCapacity);
    if (initialCapacity > MAXIMUM_CAPACITY)
        initialCapacity = MAXIMUM_CAPACITY;
    if (loadFactor <= 0 || Float.isNaN(loadFactor))
        throw new IllegalArgumentException("Illegal load factor: " +
                                            loadFactor);
    this.loadFactor = loadFactor;
    threshold = initialCapacity;
    //init方法在HashMap中没有实际实现，不过在其子类如 linkedHashMap中就会有对应实现
    init();
}
```

### Put

```java
public V put(K key, V value) {
    //table数组为空数组{}，进行数组填充（为table分配实际内存空间,入参是threshold）
    //此时threshold为initialCapacity 默认是1<<4(2^4=16)
    if (table == EMPTY_TABLE) {
        inflateTable(threshold);
    }
    //如果key为null，存储位置为table[0]或table[0]的冲突链上
    if (key == null)
        return putForNullKey(value);
    //对key的hashcode进一步计算，确保散列均匀
    int hash = hash(key);
    //获取在table中的实际位置
    int i = indexFor(hash, table.length);
    for (Entry<K,V> e = table[i]; e != null; e = e.next) {
        //如果该对应数据已存在，执行覆盖操作。用新value替换旧value，并返回旧value
        Object k;
        if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
            V oldValue = e.value;
            e.value = value;
            e.recordAccess(this);
            return oldValue;
        }
    }
    //保证并发访问时，若HashMap内部结构发生变化，快速响应失败
    modCount++;
    //新增一个entry
    addEntry(hash, key, value, i);
    return null;
}
```

### inflateTable、roundUpToPowerOf2

inflateTable这个方法用于为主干数组table在内存中分配存储空间，通过roundUpToPowerOf2(toSize)可以确保capacity为大于或等于toSize的最接近toSize的二次幂，比如toSize=13,则capacity=16;to_size=16,capacity=16;to_size=17,capacity=32.

```java
private void inflateTable(int toSize) {
    //capacity一定是2的次幂
    int capacity = roundUpToPowerOf2(toSize);
    /**计算threshold
     * 取capacity*loadFactor和MAXIMUM_CAPACITY+1的最小值，
     * capaticy一定不会超过MAXIMUM_CAPACITY，除非loadFactor大于1
    */
    threshold = (int) Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);
    table = new Entry[capacity];
    initHashSeedAsNeeded(capacity);
}
```

roundUpToPowerOf2中的这段处理使得数组长度一定为2的次幂，Integer.highestOneBit是用来获取最左边的bit（其他bit位为0）所代表的数值.

```java
private static int roundUpToPowerOf2(int number) {
    // assert number >= 0 : "number must be non-negative";
    return number >= MAXIMUM_CAPACITY
            ? MAXIMUM_CAPACITY
            : (number > 1) ? Integer.highestOneBit((number - 1) << 1) : 1;
}
```

### Hash函数

```java
/**这是一个神奇的函数，用了很多的异或，移位等运算
* 对key的hashcode进一步进行计算以及二进制位的调整等
* 来保证最终获取的存储位置尽量分布均匀
*/
final int hash(Object k) {
    int h = hashSeed;
    if (0 != h && k instanceof String) {
        return sun.misc.Hashing.stringHash32((String) k);
    }

    h ^= k.hashCode();

    h ^= (h >>> 20) ^ (h >>> 12);
    return h ^ (h >>> 7) ^ (h >>> 4);
}
```

以上hash函数计算出的值，通过indexFor进一步处理来获取实际的存储位置

```java
/**
* 返回数组下标
*/
static int indexFor(int h, int length) {
    return h & (length-1);
}
```

**h&（length-1）**保证获取的index一定在数组范围内，举个例子，默认容量16，length-1=15，h=18,转换成二进制计算为index=2。位运算对计算机来说，性能更高一些（HashMap中有大量位运算）  
所以最终存储位置的确定流程是这样的：
<div align=center>

![1589102000853.png](..\images\1589102000853.png)

</div>

### 添加到链表中

```java
void addEntry(int hash, K key, V value, int bucketIndex) {
    if ((size >= threshold) && (null != table[bucketIndex])) {
        //当size超过临界阈值threshold，并且即将发生哈希冲突时进行扩容
        resize(2 * table.length);
        hash = (null != key) ? hash(key) : 0;
        bucketIndex = indexFor(hash, table.length);
    }

    createEntry(hash, key, value, bucketIndex);
}
```

通过以上代码能够得知，当发生哈希冲突并且size大于阈值的时候，需要进行数组扩容，扩容时，需要新建一个长度为之前数组2倍的新的数组，然后将当前的Entry数组中的元素全部传输过去，扩容后的新数组长度为之前的2倍，所以扩容相对来说是个耗资源的操作。

### 扩容

```java
void resize(int newCapacity) {
    Entry[] oldTable = table;
    int oldCapacity = oldTable.length;
    if (oldCapacity == MAXIMUM_CAPACITY) {
        threshold = Integer.MAX_VALUE;
        return;
    }

    Entry[] newTable = new Entry[newCapacity];
    transfer(newTable, initHashSeedAsNeeded(newCapacity));
    table = newTable;
    threshold = (int)Math.min(newCapacity * loadFactor, MAXIMUM_CAPACITY + 1);
}
```

如果数组进行扩容，数组长度发生变化，而存储位置 index = h&(length-1),index也可能会发生变化，需要重新计算index，我们先来看看transfer这个方法。

#### transfer

```java
void transfer(Entry[] newTable, boolean rehash) {
    int newCapacity = newTable.length;
    //for循环中的代码，逐个遍历链表，重新计算索引位置，
    //将老数组数据复制到新数组中去（数组不存储实际数据，所以仅仅是拷贝引用而已）
    for (Entry<K,V> e : table) {
        while(null != e) {
            Entry<K,V> next = e.next;
            if (rehash) {
                e.hash = null == e.key ? 0 : hash(e.key);
            }
            int i = indexFor(e.hash, newCapacity);
            //将当前entry的next链指向新的索引位置,newTable[i]有可能为空，
            //有可能也是个entry链，如果是entry链，直接在链表头部插入。
            e.next = newTable[i];
            newTable[i] = e;
            e = next;
        }
    }
}
```

这个方法将老数组中的数据逐个链表地遍历，扔到新的扩容后的数组中，我们的数组索引位置的计算是通过 对key值的hashcode进行hash扰乱运算后，再通过和 length-1进行位运算得到最终数组索引位置。  

HashMap的数组长度一定保持2的次幂，比如16的二进制表示为 10000，那么length-1就是15，二进制为01111，同理扩容后的数组长度为32，二进制表示为100000，length-1为31，二进制表示为011111。从下图可以我们也能看到这样会保证低位全为1，而扩容后只有一位差异，也就是多出了最左位的1，这样在通过 h&(length-1)的时候，只要h对应的最左边的那一个差异位为0，就能保证得到的新的数组索引和老数组索引一致(大大减少了之前已经散列良好的老数组的数据位置重新调换)。
<div align=center>

![1589102034044.png](..\images\1589102034044.png)

</div>
还有，数组长度保持2的次幂，length-1的低位都为1，会使得获得的数组索引index更加均匀
<div align=center>

![1589102056292.png](..\images\1589102056292.png)

</div>

我们看到，上面的&运算，高位是不会对结果产生影响的（hash函数采用各种位运算可能也是为了使得低位更加散列），我们只关注低位bit，如果低位全部为1，那么对于h低位部分来说，任何一位的变化都会对结果产生影响，也就是说，要得到index=21这个存储位置，h的低位只有这一种组合。这也是数组长度设计为必须为2的次幂的原因。

<div align=center>

![1589102083089.png](..\images\1589102083089.png)

</div>

如果不是2的次幂，也就是低位不是全为1此时，要使得index=21，h的低位部分不再具有唯一性了，哈希冲突的几率会变的更大，同时，index对应的这个bit位无论如何不会等于1了，而对应的那些数组位置也就被白白浪费了。

### Get

public V get(Object key) {
//如果key为null,则直接去table[0]处去检索即可。
        if (key == null)
            return getForNullKey();
        Entry<K,V> entry = getEntry(key);
        return null == entry ? null : entry.getValue();
 }
get方法通过key值返回对应value，如果key为null，直接去table[0]处检索。我们再看一下getEntry这个方法。
final Entry<K,V> getEntry(Object key) {
            
        if (size == 0) {
            return null;
        }
        //通过key的hashcode值计算hash值
        int hash = (key == null) ? 0 : hash(key);
        //indexFor (hash&length-1) 获取最终数组索引，然后遍历链表，通过equals方法比对找出对应记录
        for (Entry<K,V> e = table[indexFor(hash, table.length)];
             e != null;
             e = e.next) {
            Object k;
            if (e.hash == hash && 
                ((k = e.key) == key || (key != null && key.equals(k))))
                return e;
        }
        return null;
    }    

可以看出，get方法的实现相对简单，key(hashcode)–>hash–>indexFor–>最终索引位置，找到对应位置table[i]，再查看是否有链表，遍历链表，通过key的equals方法比对查找对应的记录。要注意的是，有人觉得上面在定位到数组位置之后然后遍历链表的时候，e.hash == hash这个判断没必要，仅通过equals判断就可以。其实不然，试想一下，如果传入的key对象重写了equals方法却没有重写hashCode，而恰巧此对象定位到这个数组位置，如果仅仅用equals判断可能是相等的，但其hashCode和当前对象不一致，这种情况，根据Object的hashCode的约定，不能返回当前对象，而应该返回null。

在重写equals的方法的时候，必须注意重写hashCode方法，同时还要保证通过equals判断相等的两个对象，调用hashCode方法要返回同样的整数值。而如果equals判断不相等的两个对象，其hashCode可以相同（只不过会发生哈希冲突，应尽量避免）。

1.1.1.1.2	JAVA8
Java8 对 HashMap 进行了一些修改，最大的不同就是利用了红黑树，所以其由 数组+链表+红黑树 组成。 
根据 Java7 HashMap 的介绍，我们知道，查找的时候，根据 hash 值我们能够快速定位到数组的具体下标，但是之后的话，需要顺着链表一个个比较下去才能找到我们需要的，时间复杂度取决
于链表的长度，为 O(n)。为了降低这部分的开销，在 Java8 中，当链表中的元素超过了 8 个以后，会将链表转换为红黑树，在这些位置进行查找的时候可以降低时间复杂度为 O(logN)。 
<div align=center>

![1589102119067.png](..\images\1589102119067.png)

</div>

1.1.1.1.1.1	特殊说明
	源码中的头节点一般是指table表上索引位置的节点，也就是链表的头节点
	红黑树中的root节点指最上面的节点（没有父节点的节点），但是根节点不一定是索引位置的头节点（也就是链表的头节点），HashMap 通过 moveRootToFront 方法来维持红黑树的根结点就是索引位置的头结点，但是在 removeTreeNode 方法中，当 movable 为 false 时，不会调用 moveRootToFront 方法，此时红黑树的根节点不一定是索引位置的头节点，该场景发生在 HashIterator 的 remove 方法中。
	转为红黑树节点后，链表的结构还存在，通过 next 属性维持，红黑树节点在进行操作时都会维护链表的结构，
	在红黑树上，叶子节点也可能有 next 节点，因为红黑树的结构跟链表的结构是互不影响的
	链表移除操作
<div align=center>

![1589102145189.png](..\images\1589102145189.png)

</div>

	红黑链表维护结构

<div align=center>

![1589102168356.png](..\images\1589102168356.png)

</div>

 


	源码中进行红黑树的查找时，会反复用到以下两条规则：1）如果目标节点的 hash 值小于 p 节点的 hash 值，则向 p 节点的左边遍历；否则向 p 节点的右边遍历。2）如果目标节点的 key 值小于 p 节点的 key 值，则向 p 节点的左边遍历；否则向 p 节点的右边遍历。这两条规则是利用了红黑树的特性（左节点 < 根节点 < 右节点）
	源码中进行红黑树的查找时，会用 dir（direction）来表示向左还是向右查找，dir 存储的值是目标节点的 hash/key 与 p 节点的 hash/key 的比较结果
1.1.1.1.1.1	基本属性
/**
     * The default initial capacity - MUST be a power of two.
     * 默认容量，1向左移位4个，00000001变成00010000，
     * 也就是2的4次方为16，使用移位是因为移位是计算机基础运算，效率比加减乘除快。
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The load factor used when none specified in constructor.
     * 用于扩容的加载因子
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * The bin count threshold for using a tree rather than list for a
     * bin.  Bins are converted to trees when adding an element to a
     * bin with at least this many nodes. The value must be greater
     * than 2 and should be at least 8 to mesh with assumptions in
     * tree removal about conversion back to plain bins upon
     * shrinkage.
     * 当桶的数量大于该值时，会把链表结构转换成红黑树结构
     */
    static final int TREEIFY_THRESHOLD = 8;

    /**
     * The bin count threshold for untreeifying a (split) bin during a
     * resize operation. Should be less than TREEIFY_THRESHOLD, and at
     * most 6 to mesh with shrinkage detection under removal.
     * 当桶的节点数量小于该值时，会自动转换成链表结构，前天是当前结构为红黑树
     */
    static final int UNTREEIFY_THRESHOLD = 6;

    /**
     * The smallest table capacity for which bins may be treeified.
     * (Otherwise the table is resized if too many nodes in a bin.)
     * Should be at least 4 * TREEIFY_THRESHOLD to avoid conflicts
     * between resizing and treeification thresholds.
     * 当hashmap中元素的数量大于该值时，桶的存储结构也会转换成红黑树
     */
    static final int MIN_TREEIFY_CAPACITY = 64;

    /**
     * Basic hash bin node, used for most entries.  (See below for
     * TreeNode subclass, and in LinkedHashMap for its Entry subclass.)
     * 链表结构定义
     */
    static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        V value;
        Node<K,V> next;

        Node(int hash, K key, V value, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey()        { return key; }
        public final V getValue()      { return value; }
        public final String toString() { return key + "=" + value; }

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                if (Objects.equals(key, e.getKey()) &&
                    Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
    }

    /* ---------------- Fields -------------- */

    /**
     * The table, initialized on first use, and resized as
     * necessary. When allocated, length is always a power of two.
     * (We also tolerate length zero in some operations to allow
     * bootstrapping mechanics that are currently not needed.)
     * 存储实际元素的数组，被transient修饰，表示不被序列化
     */
    transient Node<K,V>[] table;

    /**
     * Holds cached entrySet(). Note that AbstractMap fields are used
     * for keySet() and values().
     * 将数据转换成set的另一种存储形式，这个变量主要用于迭代功能
     */
    transient Set<Map.Entry<K,V>> entrySet;

    /**
     * The number of key-value mappings contained in this map.
     * 元素数量,实际存储key-value键值对的数量
     */
    transient int size;

    /**
     * The number of times this HashMap has been structurally modified
     * Structural modifications are those that change the number of mappings in
     * the HashMap or otherwise modify its internal structure (e.g.,
     * rehash).  This field is used to make iterators on Collection-views of
     * the HashMap fail-fast.  (See ConcurrentModificationException).
     * 统计map的结构化修改次数
     */
    transient int modCount;

    /**
     * The next size value at which to resize (capacity * load factor).
     * 扩容的临界值
     * @serial
     */
    // (The javadoc description is true upon serialization.
    // Additionally, if the table array has not been allocated, this
    // field holds the initial array capacity, or zero signifying
    // DEFAULT_INITIAL_CAPACITY.)
    int threshold;

    /**
     * The load factor for the hash table.
     * 加载因子，定义为可使用的变量
     * @serial
     */
final float loadFactor;
 /**
     * Entry for Tree bins. Extends LinkedHashMap.Entry (which in turn
     * extends Node) so can be used as extension of either regular or
     * linked node.
     * -- 红黑树结构
     */
    static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
        TreeNode<K,V> parent;  // red-black tree links
        TreeNode<K,V> left;
        TreeNode<K,V> right;
        TreeNode<K,V> prev;    // needed to unlink next upon deletion
        boolean red;
        TreeNode(int hash, K key, V val, Node<K,V> next) {
            super(hash, key, val, next);
        }
1.1.1.1.1.2	Hash
如何在不遍历链表/红黑树的情况下快速定位原始，可以大大优化查询效率，并且通过hash算法可以使数据均匀分布，尽量减少哈希碰撞。
/**
     * Computes key.hashCode() and spreads (XORs) higher bits of hash
     * to lower.  Because the table uses power-of-two masking, sets of
     * hashes that vary only in bits above the current mask will
     * always collide. (Among known examples are sets of Float keys
     * holding consecutive whole numbers in small tables.)  So we
     * apply a transform that spreads the impact of higher bits
     * downward. There is a tradeoff between speed, utility, and
     * quality of bit-spreading. Because many common sets of hashes
     * are already reasonably distributed (so don't benefit from
     * spreading), and because we use trees to handle large sets of
     * collisions in bins, we just XOR some shifted bits in the
     * cheapest possible way to reduce systematic lossage, as well as
     * to incorporate impact of the highest bits that would otherwise
     * never be used in index calculations because of table bounds.
     * 使用key的hashCode进行移位异或运算，尽量避免hash碰撞
     * 如果在修改某个对象的hashCode方法时需要尽量保障唯一性，
     * 否则在使用map数据结构存储时会出现数据覆盖的问题
     */
    static final int hash(Object key) {
        int h;
        /**
         * 1.首先获取key的hashCode值
         * 2.将hashCode值的高16位参与运算
         */
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
拿到元素的hash值之后通过以下方式完成索引定位
int n = tab.length;
int index = (n - 1) & hash
hashmap使用模运算算法均匀分布数据，由于模运算比较消耗系统性能，JDK团队使用位与运算（(table.length -1) & h）来替代模运算。这个优化是基于以下公式：x mod 2^n = x & (2^n - 1)。由于 HashMap 底层数组的长度总是 2 的 n 次方，并且取模运算为 “h mod table.length”，对应上面的公式，可以得到该运算等同于“h & (table.length - 1)”。这是 HashMap 在速度上的优化，因为 & 比 % 具有更高的效率。在 JDK1.8 的实现中，还优化了高位运算的算法，将 hashCode 的高 16 位与 hashCode 进行异或运算，主要是为了在 table 的 length 较小的时候，让高位也参与运算，并且不会有太大的开销。
举例说明：
当 table 长度为 16 时，table.length - 1 = 15 ，用二进制来看，此时低 4 位全是 1，高 28 位全是 0，与 0 进行 & 运算必然为 0，因此此时 hashCode 与 “table.length - 1” 的 & 运算结果只取决于 hashCode 的低 4 位，在这种情况下，hashCode 的高 28 位就没有任何作用，并且由于 hash 结果只取决于 hashCode 的低 4 位，hash 冲突的概率也会增加。因此，在 JDK 1.8 中，将高位也参与计算，目的是为了降低 hash 冲突的概率。

<div align=center>

![1589102211003.png](..\images\1589102211003.png)

</div>



1.1.1.1.1.1	查询
    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
     * key.equals(k))}, then this method returns {@code v}; otherwise
     * it returns {@code null}.  (There can be at most one such mapping.)
     *
     * <p>A return value of {@code null} does not <i>necessarily</i>
     * indicate that the map contains no mapping for the key; it's also
     * possible that the map explicitly maps the key to {@code null}.
     * The {@link #containsKey containsKey} operation may be used to
     * distinguish these two cases.
     *
     * @see #put(Object, Object)
     */
    public V get(Object key) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }

    /**
     * Implements Map.get and related methods.
     *
     * @param hash hash for key
     * @param key the key
     * @return the node, or null if none
     */
    final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
        /**
         * 只有当table不为空，且table长度大于0，
         * 且table索引位置(使用table.length - 1和hash值进行位与运算)的节点不为空
         */
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (first = tab[(n - 1) & hash]) != null) {
        	/**
        	 * 2.检查first节点的hash值和key是否和入参的一样，
        	 * 如果一样则first即为目标节点，直接返回first节点
        	 */
            if (first.hash == hash && // always check first node
                ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            /**
             * 3.如果first不是目标节点，并且first的next节点不为空则继续遍历
             */
            if ((e = first.next) != null) {
                if (first instanceof TreeNode)
                	/**
                	 * 3.如果first不是目标节点，并且first的next节点不为空则继续遍历
                	 */
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                do {
                	/**
                	 * 5.执行链表节点的查找，向下遍历链表, 直至找到节点的key和入参的key相等时,返回该节点
                	 */
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        // 没有查询到数据
        return null;
    }
红黑树查找：
        /**
         * Calls find for root node.
         */
        final TreeNode<K,V> getTreeNode(int h, Object k) {
        	// 首先获取root节点，然后根据root节点进行find
            return ((parent != null) ? root() : this).find(h, k, null);
        }
定位根节点：
        /**
         * Returns root of tree containing this node.
         * -- 没有父节点的节点为根节点
         */
        final TreeNode<K,V> root() {
            for (TreeNode<K,V> r = this, p;;) {
                if ((p = r.parent) == null)
                    return r;
                r = p;
            }
        }
使用根节点进行find：
        /**
         * Finds the node starting at root p with the given hash and key.
         * The kc argument caches comparableClassFor(key) upon first use
         * comparing keys.
         * 从调用此方法的节点开始查找, 通过hash值和key找到对应的节点
         * 此方法是红黑树节点的查找, 红黑树是特殊的自平衡二叉查找树
         * 平衡二叉查找树的特点：左节点<根节点<右节点
         */
        final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
        	// 1.将p节点赋值为调用此方法的节点，即为红黑树根节点
            TreeNode<K,V> p = this;
            do {
                int ph, dir; K pk;
                TreeNode<K,V> pl = p.left, pr = p.right, q;
                // 3.如果传入的hash值小于p节点的hash值，则往p节点的左边遍历
                if ((ph = p.hash) > h)
                    p = pl;
                // 4.如果传入的hash值大于p节点的hash值，则往p节点的右边遍历
                else if (ph < h)
                    p = pr;
                // 5.如果传入的hash值和key值等于p节点的hash值和key值,则p节点为目标节点,返回p节点
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                // 6.p节点的左节点为空则将向右遍历
                else if (pl == null)
                    p = pr;
                // 7.p节点的右节点为空则向左遍历
                else if (pr == null)
                    p = pl;
                // 8.将p节点与k进行比较
                else if ((kc != null ||
                          (kc = comparableClassFor(k)) != null) && // 8.1 kc不为空代表k实现了Comparable
                         (dir = compareComparables(kc, k, pk)) != 0) // 8.2 k<pk则dir<0, k>pk则dir>0
                	// 8.3 k<pk则向左遍历(p赋值为p的左节点), 否则向右遍历
                	p = (dir < 0) ? pl : pr;
                // 9.代码走到此处, 代表key所属类没有实现Comparable, 直接指定向p的右边遍历
                else if ((q = pr.find(h, k, kc)) != null)
                    return q;
                // 10.代码走到此处代表“pr.find(h, k, kc)”为空, 因此直接向左遍历
                else
                    p = pl;
            } while (p != null);
            return null;
        }

    /**
     * Returns x's Class if it is of the form "class C implements
     * Comparable<C>", else null.
     */
    static Class<?> comparableClassFor(Object x) {
    	// 1.判断x是否实现了Comparable接口
    	if (x instanceof Comparable) {
            Class<?> c; Type[] ts, as; Type t; ParameterizedType p;
            // 2.校验x是否为String类型
            if ((c = x.getClass()) == String.class) // bypass checks
                return c;
            if ((ts = c.getGenericInterfaces()) != null) {
            	// 3.遍历x实现的所有接口
            	for (int i = 0; i < ts.length; ++i) {
            		// 4.如果x实现了Comparable接口，则返回x的Class
                    if (((t = ts[i]) instanceof ParameterizedType) &&
                        ((p = (ParameterizedType)t).getRawType() ==
                         Comparable.class) &&
                        (as = p.getActualTypeArguments()) != null &&
                        as.length == 1 && as[0] == c) // type arg is c
                        return c;
                }
            }
        }
        return null;
    }


1.1.1.1.1.2	Put逻辑
<div align=center>

![1589102248734.png](..\images\1589102248734.png)

</div>




    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }

    /**
     * Implements Map.put and related methods.
     *
     * @param hash hash for key
     * @param key the key
     * @param value the value to put
     * @param onlyIfAbsent if true, don't change existing value
     * @param evict if false, the table is in creation mode.（初始化时使用false）
     * @return previous value, or null if none
     */
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
    	/**
    	 * tab 哈希数组，
    	 * p 该哈希桶的首节点，
    	 * n hashMap的长度，
    	 * i 计算出的数组下标
    	 */
        Node<K,V>[] tab; Node<K,V> p; int n, i;
        /**
         * 1.使用懒加载的方式完成table的初始化（通过扩容完成：resize方法）
         * 2.如果table为空或者长度为0，则调用resize完成初始化
         */
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
        /**
         * 通过hash值计算索引位置，如果计算出的该哈希桶的位置没有值，
         * 则把新插入的key-value放到此处，并且赋值给p（首节点）
         */
        if ((p = tab[i = (n - 1) & hash]) == null)
        	//如果p（首节点）为空，则在该索引位置新增一个节点
            tab[i] = newNode(hash, key, value, null);
        else {
        	/**
        	 * table索引位置不为空，及首节点不为空，则进行查找
        	 */
        	// e 临时节点的作用， k 存放该当前节点的key
            Node<K,V> e; K k;
            /**
             * 第一种：插入的key-value的hash值，key都与当前节点的相等，e = p，
             * 则表示为首节点
             */
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
            /**
             * 第二种：hash值不等于首节点，判断该p是否属于红黑树的节点
             */
            else if (p instanceof TreeNode)
            	/**
            	 * 为红黑树的节点，则在红黑树中进行添加，
            	 * 如果该节点已经存在，则返回该节点（不为null），
            	 * 该值很重要，用来判断put操作是否成功，如果添加成功返回null
            	 */
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            /**
             * 第三种，hash值不等于首节点，不为红黑树的节点，则为链表的节点，使用binCount统计链表数
             */
            else {
                for (int binCount = 0; ; ++binCount) {// 遍历链表
                	/**
                	 * 如果找到尾部，则表明添加的key-value没有重复，在尾部进行添加
                	 */
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
                        // 判断是否转换成红黑树结构，减1，是由于循环从p的下一个节点开始
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    // 如果链表中有重复的key，e则为当前重复的节点，结束循环
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e; // 将p指向下一个节点
                }
            }
            /**
             * 如果存在重复key，则使用新值插入，并且返回旧值
             */
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
        }
        /**
         * 在没有重复值的情况下，完成以下操作
         * 1.modCount + 1
         * 2.实际长度size + 1
         * 3.根据实际情况完成扩容
         * 4.返回null，表示添加成功
         */
        ++modCount;
        if (++size > threshold)
            resize();
        // 该方法目前未具体实现，在LinkedHashMap中有实现
        afterNodeInsertion(evict);
        return null;
    }
红黑树结构维护数据
        /**
         * Tree version of putVal.
         * -- 红黑树的put操作，红黑树插入会同时维护原来的链表属性, 即原来的next属性
         */
        final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab,
                                       int h, K k, V v) {
            Class<?> kc = null;
            boolean searched = false;
            /**
             * 获取根节点，索引位置的头节点不一定是根节点
             */
            TreeNode<K,V> root = (parent != null) ? root() : this;
            /**
             * 将根节点赋值给p，然后进行遍历查找
             */
            for (TreeNode<K,V> p = root;;) {
                int dir, ph; K pk;
                // 如果传入的hash值小于p节点的hash值，将dir赋值为-1，代表向p的左边查找树
                if ((ph = p.hash) > h)
                    dir = -1;
                // 如果传入的hash值大于p节点的hash值， 将dir赋值为1，代表向p的右边查找树
                else if (ph < h)
                    dir = 1;
                // 如果传入的hash值和key值等于p节点的hash值和key值, 则p节点即为目标节点, 返回p节点
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                /**
                 * 如果k所属的类没有实现Comparable接口 或者 k和p节点的key相等
                 */
                else if ((kc == null &&
                          (kc = comparableClassFor(k)) == null) ||
                         (dir = compareComparables(kc, k, pk)) == 0) {
                	/**
                	 * 第一次符合条件, 从p节点的左节点和右节点分别调用find方法进行查找, 
                	 * 如果查找到目标节点则返回
                	 */
                    if (!searched) {
                        TreeNode<K,V> q, ch;
                        searched = true;
                        if (((ch = p.left) != null &&
                             (q = ch.find(h, k, kc)) != null) ||
                            ((ch = p.right) != null &&
                             (q = ch.find(h, k, kc)) != null))
                            return q;
                    }
                    /**
                     * 否则使用定义的一套规则来比较k和p节点的key的大小, 用来决定向左还是向右查找
                     * dir<0则代表k<pk，则向p左边查找；反之亦然
                     */
                    dir = tieBreakOrder(k, pk);
                }

                // xp赋值为x的父节点,中间变量,用于下面给x的父节点赋值
                TreeNode<K,V> xp = p;
                /**
                 * dir<=0则向p左边查找,否则向p右边查找,如果为null,则代表该位置即为x的目标位置
                 */
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                	// 表示已经找到x的位置，只需要将x放到该位置即可
                    Node<K,V> xpn = xp.next;
                    // 创建新的节点, 其中x的next节点为xpn, 即将x节点插入xp与xpn之间
                    TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);
                    /**
                     * 调整x、xp、xpn之间的属性关系
                     * 如果时dir <= 0, 则代表x节点为xp的左节点
                     * 如果时dir> 0, 则代表x节点为xp的右节点
                     */
                    if (dir <= 0)
                        xp.left = x;
                    else
                        xp.right = x;
                    xp.next = x;// 将xp的next节点设置为x
                    x.parent = x.prev = xp;// 将x的parent和prev节点设置为xp
                    // 如果xpn不为空,则将xpn的prev节点设置为x节点,与上文的x节点的next节点对应
                    if (xpn != null)
                        ((TreeNode<K,V>)xpn).prev = x;
                    // 进行红黑树的插入平衡调整
                    moveRootToFront(tab, balanceInsertion(root, x));
                    return null;
                }
            }
        }

        /**
         * Tie-breaking utility for ordering insertions when equal
         * hashCodes and non-comparable. We don't require a total
         * order, just a consistent insertion rule to maintain
         * equivalence across rebalancings. Tie-breaking further than
         * necessary simplifies testing a bit.
         * - 用于不可比较或者hashCode相同时进行比较的方法, 
         * - 只是一个一致的插入规则，用来维护重定位的等价性。
         */
        static int tieBreakOrder(Object a, Object b) {
            int d;
            if (a == null || b == null ||
                (d = a.getClass().getName().
                 compareTo(b.getClass().getName())) == 0)
                d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
                     -1 : 1);
            return d;
        }

1.1.1.1.1.1	链表转红黑树
    /**
     * Replaces all linked nodes in bin at index for given hash unless
     * table is too small, in which case resizes instead.
     * -- 链表转换成红黑树
     */
    final void treeifyBin(Node<K,V>[] tab, int hash) {
        int n, index; Node<K,V> e;
        /**
         * 如果tab为空，或者长度小于64，则调用resize方法进行扩容
         */
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            resize();
        /**
         * 根据hash值计算索引值，将该索引位置的节点赋值给e，从e开始遍历该索引位置的链表
         */
        else if ((e = tab[index = (n - 1) & hash]) != null) {
            TreeNode<K,V> hd = null, tl = null;
            do {
            	/**
            	 * 将链表转换成红黑树
            	 */
                TreeNode<K,V> p = replacementTreeNode(e, null);
                if (tl == null)// 第一次遍历，将节点赋值给hd
                    hd = p;
                else {
                	/**
                	 * 如果不是第一次遍历，则处理当前节点的prev属性和上一个节点的next属性
                	 */
                    p.prev = tl; // 当前节点的prev属性设为上一个节点
                    tl.next = p; // 上一个节点的next属性设置为当前节点
                }
                /**
                 * 将p节点赋值给tl，用于在下一次循环中作为上一个节点进行一些链表的关联操作
                 * （p.prev = tl 和 tl.next = p）
                 */
                tl = p;
            } while ((e = e.next) != null);
            if ((tab[index] = hd) != null)
            	/**
            	 * 将table该索引位置赋值为新转的TreeNode的头节点，
            	 * 如果该节点不为空，则以以头节点(hd)为根节点, 构建红黑树
            	 */
                hd.treeify(tab);
        }
    }

    // For treeifyBin
    TreeNode<K,V> replacementTreeNode(Node<K,V> p, Node<K,V> next) {
        return new TreeNode<>(p.hash, p.key, p.value, next);
    }
