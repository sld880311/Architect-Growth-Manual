

1.1	JAVA 集合 
1.1.1	集合和数组的区别
1.	数组和集合都是Java中的容器
2.	数组的长度是固定的，集合的长度是可变的
3.	数组只能存储相同数据类型的数据，这里的数据类型可以是基本数据类型，也可以是引用类型
4.	集合可以存储不同数据类型的对象的引用(但一般情况下，我们会使用泛型来约定只使用1种数据类型)，但不能存储基本数据类型
1.1.2	接口继承关系和实现 
集合类存放于 Java.util 包中，主要有 3 种：set(集）、list(列表包含 Queue）和 map(映射)。 
1.	Collection：Collection 是集合 List、Set、Queue 的最基本的接口。 
2.	Iterator：迭代器，可以通过迭代器遍历集合中的数据 
3.	Map：是映射表的基础接口 
<div align=center>

![1589101729615.png](..\images\1589101729615.png)

</div>

<div align=center>

![1589101745961.png](..\images\1589101745961.png)

</div>


从上图可以总结出如下几点：
1.	Java集合的根接口是Collection，它又继承了迭代接口Iterable
2.	List接口和Set接口继承了Collection接口
3.	Map接口是独立的接口，并没有继承Collection接口 （这里是重点，面试可能问的比较多）
4.	List接口常用的实现类有：ArrayList、LinkedList、Vector
5.	Set接口常用的实现类有：HashSet、LinkedHashSet、TreeSet
6.	Map接口常用的实现类有：HashMap、HashTable、TreeMap
<div align=center>

![1589101774172.png](..\images\1589101774172.png)

</div>

1.1.1	List 
Java 的 List 是非常常用的数据类型。List 是有序的 Collection。Java List 一共三个实现类：分别是 ArrayList、Vector 和 LinkedList。 
<div align=center>

![1589101801968.png](..\images\1589101801968.png)

</div>

List集合包括List接口以及List接口的所有实现类。List集合具有以下特点：
1.	集合中的元素允许重复
2.	集合中的元素是有顺序的，各元素插入的顺序就是各元素的顺序
3.	集合中的元素可以通过索引来访问或者设置
1.1.1.1	ArrayList（数组） 
ArrayList 是最常用的 List 实现类，内部是通过数组实现的，它允许对元素进行快速随机访问。数组的缺点是每个元素之间不能有间隔，当数组大小不满足时需要增加存储能力，就要将已经有数组的数据复制到新的存储空间中。当从 ArrayList 的中间位置插入或者删除元素时，需要对数组进行复制、移动、代价比较高。因此，它适合随机查找和遍历，不适合插入和删除。 
1.1.1.2	Vector（数组实现、线程同步） 
Vector 与 ArrayList 一样，也是通过数组实现的，不同的是它支持线程的同步，即某一时刻只有一
个线程能够写 Vector，避免多线程同时写而引起的不一致性，但实现同步需要很高的花费，因此，访问它比访问 ArrayList 慢。 
1.1.1.3	LinkList（链表） 
LinkedList 是用链表结构存储数据的，很适合数据的动态插入和删除，随机访问和遍历速度比较慢。另外，他还提供了 List 接口中没有定义的方法，专门用于操作表头和表尾元素，可以当作堆栈、队列和双向队列使用。 
 
1.1.2	Set 
Set 注重独一无二的性质,该体系集合用于存储无序(存入和取出的顺序不一定相同)元素，值不能重复。对象的相等性本质是对象 hashCode 值（java 是依据对象的内存地址计算出的此序号）判断的，如果想要让两个不同的对象视为相等的，就必须覆盖 Object 的 hashCode 方法和 equals 方法。 
<div align=center>

![1589101829777.png](..\images\1589101829777.png)

</div>

Set集合包括Set接口以及Set接口的所有实现类。Set集合具有以下特点：
1.	集合中不包含重复元素(你可以重复添加，但只会保留第1个)
2.	集合中的元素不一定保证有序

1.1.1.1	HashSet（Hash 表） 
哈希表边存放的是哈希值。HashSet 存储元素的顺序并不是按照存入时的顺序（和 List 显然不
同） 而是按照哈希值来存的所以取数据也是按照哈希值取得。元素的哈希值是通过元素的
hashcode 方法来获取的, HashSet 首先判断两个元素的哈希值，如果哈希值一样，接着会比较
equals 方法 如果 equls 结果为 true ，HashSet 就视为同一个元素。如果 equals 为 false 就不是同一个元素。 
哈希值相同 equals 为 false 的元素是怎么存储呢,就是在同样的哈希值下顺延（可以认为哈希值相同的元素放在一个哈希桶中）。也就是哈希一样的存一列。如图 1 表示 hashCode 值不相同的情况；图 2 表示 hashCode 值相同，但 equals 不相同的情况。 
<div align=center>

![1589101856833.png](..\images\1589101856833.png)

</div>

HashSet 通过 hashCode 值来确定元素在内存中的位置。一个 hashCode 位置上可以存放多个元素。 
1.1.1.1	TreeSet（二叉树） 
1.	TreeSet()是使用二叉树的原理对新 add()的对象按照指定的顺序排序（升序、降序），每增加一个对象都会进行排序，将对象插入的二叉树指定的位置。 
2.	Integer 和 String 对象都可以进行默认的 TreeSet 排序，而自定义类的对象是不可以的，自己定义的类必须实现 Comparable 接口，并且覆写相应的 compareTo()函数，才可以正常使用。 
3.	在覆写 compare()函数时，要返回相应的值才能使 TreeSet 按照一定的规则来排序 
4.	比较此对象与指定对象的顺序。如果该对象小于、等于或大于指定对象，则分别返回负整数、零或正整数。 
1.1.1.2	LinkHashSet（HashSet+LinkedHashMap） 
对于 LinkedHashSet 而言，它继承与 HashSet、又基于 LinkedHashMap 来实现的。
LinkedHashSet 底层使用 LinkedHashMap 来保存所有元素，它继承与 HashSet，其所有的方法操作上又与 HashSet 相同，因此 LinkedHashSet 的实现上非常简单，只提供了四个构造方法，并通过传递一个标识参数，调用父类的构造器，底层构造一个 LinkedHashMap 来实现，在相关操作上与父类 HashSet 的操作相同，直接调用父类 HashSet 的方法即可。 
1.1.2	Map 
<div align=center>

![1589101882581.png](..\images\1589101882581.png)

</div>


Map集合包括Map接口以及Map接口的所有实现类。Map集合具有以下特点：
1.	Map接口并没有继承Collection接口，提供的是key到value的映射
2.	Map中不能包含相同的key



1.1.1.1.1	JAVA7


 












1.1.1.1.2	Hashmap与hashtable的区别

	HashMap 允许 key 和 value 为 null，Hashtable 不允许。
	HashMap 的默认初始容量为 16，Hashtable 为 11。
	HashMap 的扩容为原来的 2 倍，Hashtable 的扩容为原来的 2 倍加 1。
	HashMap 是非线程安全的，Hashtable是线程安全的。
	HashMap 的 hash 值重新计算过，Hashtable 直接使用 hashCode。
	HashMap 去掉了 Hashtable 中的 contains 方法。
	HashMap 继承自 AbstractMap 类，Hashtable 继承自 Dictionary 类。
1.1.1.1.3	总结

	HashMap 的底层是个 Node 数组（Node<K,V>[] table），在数组的具体索引位置，如果存在多个节点，则可能是以链表或红黑树的形式存在。
	增加、删除、查找键值对时，定位到哈希桶数组的位置是很关键的一步，源码中是通过下面3个操作来完成这一步：1）拿到 key 的 hashCode 值；2）将 hashCode 的高位参与运算，重新计算 hash 值；3）将计算出来的 hash 值与 “table.length - 1” 进行 & 运算。
	HashMap 的默认初始容量（capacity）是 16，capacity 必须为 2 的幂次方；默认负载因子（load factor）是 0.75；实际能存放的节点个数（threshold，即触发扩容的阈值）= capacity * load factor。
	HashMap 在触发扩容后，阈值会变为原来的 2 倍，并且会对所有节点进行重 hash 分布，重 hash 分布后节点的新分布位置只可能有两个：“原索引位置” 或 “原索引+oldCap位置”。例如 capacity 为16，索引位置 5 的节点扩容后，只可能分布在新表 “索引位置5” 和 “索引位置21（5+16）”。
	导致 HashMap 扩容后，同一个索引位置的节点重 hash 最多分布在两个位置的根本原因是：1）table的长度始终为 2 的 n 次方；2）索引位置的计算方法为 “(table.length - 1) & hash”。HashMap 扩容是一个比较耗时的操作，定义 HashMap 时尽量给个接近的初始容量值。
	HashMap 有 threshold 属性和 loadFactor 属性，但是没有 capacity 属性。初始化时，如果传了初始化容量值，该值是存在 threshold 变量，并且 Node 数组是在第一次 put 时才会进行初始化，初始化时会将此时的 threshold 值作为新表的 capacity 值，然后用 capacity 和 loadFactor 计算新表的真正 threshold 值。
	当同一个索引位置的节点在增加后达到 9 个时，并且此时数组的长度大于等于 64，则会触发链表节点（Node）转红黑树节点（TreeNode），转成红黑树节点后，其实链表的结构还存在，通过 next 属性维持。链表节点转红黑树节点的具体方法为源码中的 treeifyBin 方法。而如果数组长度小于64，则不会触发链表转红黑树，而是会进行扩容。
	当同一个索引位置的节点在移除后达到 6 个时，并且该索引位置的节点为红黑树节点，会触发红黑树节点转链表节点。红黑树节点转链表节点的具体方法为源码中的 untreeify 方法。
	HashMap 在 JDK 1.8 之后不再有死循环的问题，JDK 1.8 之前存在死循环的根本原因是在扩容后同一索引位置的节点顺序会反掉。
	HashMap 是非线程安全的，在并发场景下使用 ConcurrentHashMap 来代替。
1.1.1.1	ConcurrentHashMap 
1.1.1.1.1	Segment 段 
ConcurrentHashMap 和 HashMap 思路是差不多的，但是因为它支持并发操作，所以要复杂一
些。整个 ConcurrentHashMap 由一个个 Segment 组成，Segment 代表”部分“或”一段“的意思，所以很多地方都会将其描述为分段锁。注意，行文中，我很多地方用了“槽”来代表一个 segment。 
1.1.1.1.2	线程安全（Segment 继承 ReentrantLock 加锁）
简单理解就是，ConcurrentHashMap 是一个 Segment 数组，Segment 通过继承 
ReentrantLock 来进行加锁，所以每次需要加锁的操作锁住的是一个 segment，这样只要保证每个 Segment 是线程安全的，也就实现了全局的线程安全。 
<div align=center>

![1589102309296.png](..\images\1589102309296.png)

</div>

 
1.1.1.1.1	并行度（默认 16） 
concurrencyLevel：并行级别、并发数、Segment 数，怎么翻译不重要，理解它。默认是 16，也就是说 ConcurrentHashMap 有 16 个 Segments，所以理论上，这个时候，最多可以同时支持 16 个线程并发写，只要它们的操作分别分布在不同的 Segment 上。这个值可以在初始化的时候设置为其他值，但是一旦初始化以后，它是不可以扩容的。再具体到每个 Segment 内部，其实每个 Segment 很像之前介绍的 HashMap，不过它要保证线程安全，所以处理起来要麻烦些。 
1.1.1.1.2	Java8 实现 （引入了红黑树） 
Java8 对 ConcurrentHashMap 进行了比较大的改动,Java8 也引入了红黑树。 
<div align=center>

![1589102332640.png](..\images\1589102332640.png)

</div>

1.1.1.1	HashTable（线程安全） 
Hashtable 是遗留类，很多映射的常用功能与 HashMap 类似，不同的是它承自 Dictionary 类，并且是线程安全的，任一时间只有一个线程能写 Hashtable，并发性不如 ConcurrentHashMap，因为 ConcurrentHashMap 引入了分段锁。Hashtable 不建议在新代码中使用，不需要线程安全的场合可以用 HashMap 替换，需要线程安全的场合可以用 ConcurrentHashMap 替换。 
1.1.1.2	TreeMap（可排序） 
 
TreeMap 实现 SortedMap 接口，能够把它保存的记录根据键排序，默认是按键值的升序排序，也可以指定排序的比较器，当用 Iterator 遍历 TreeMap 时，得到的记录是排过序的。如果使用排序的映射，建议使用 TreeMap。 
在使用 TreeMap 时，key 必须实现 Comparable 接口或者在构造 TreeMap 传入自定义的
Comparator，否则会在运行时抛出 java.lang.ClassCastException 类型的异常。 
参考：https://www.ibm.com/developerworks/cn/java/j-lo-tree/index.html 
 
1.1.1.3	LinkHashMap（记录插入顺序） 
LinkedHashMap 是 HashMap 的一个子类，保存了记录的插入顺序，在用 Iterator 遍历 LinkedHashMap 时，先得到的记录肯定是先插入的，也可以在构造时带参数，按照访问次序排序。参考 1：http://www.importnew.com/28263.html 
参考 2：http://www.importnew.com/20386.html#comment-648123 
