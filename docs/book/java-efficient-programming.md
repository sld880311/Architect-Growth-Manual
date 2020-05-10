<!-- TOC -->

- [JAVA高效编程技巧总结](#java高效编程技巧总结)
    - [高效遍历MAP](#高效遍历map)
    - [方法参数要求](#方法参数要求)
    - [尽量使用同步代码块替代同步方法，提高代码执行效率](#尽量使用同步代码块替代同步方法提高代码执行效率)
    - [常量定义为static final，并且名称使用大写，多个字符使用下划线拼接，比如：USER_NAME](#常量定义为static-final并且名称使用大写多个字符使用下划线拼接比如user_name)
    - [及时关闭资源](#及时关闭资源)
    - [关闭多个资源时，需要分开执行，防止由于异常无法关闭所有的资源](#关闭多个资源时需要分开执行防止由于异常无法关闭所有的资源)
    - [使用带缓冲的输入输出流进行IO操作](#使用带缓冲的输入输出流进行io操作)
    - [避免未使用信息的定义](#避免未使用信息的定义)
    - [字符串比对相等时，字符串常量写在字符串变量前面，比如"abc".equals(abc)](#字符串比对相等时字符串常量写在字符串变量前面比如abcequalsabc)
    - [尽量使用局部变量](#尽量使用局部变量)
    - [循环内不要创建对象的引用](#循环内不要创建对象的引用)
    - [乘法和除法使用移位操作](#乘法和除法使用移位操作)

<!-- /TOC -->
# JAVA高效编程技巧总结

## 高效遍历MAP

```java
package com.sunld;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class MapTest {
	/**
	 * keySet的for循环方式：
	 */
	//只获取key
	public static void keySetForGetKey(Map<String, String> map){
	    long startTime = System.currentTimeMillis();
	    for (String key : map.keySet()) {
	    }
	    long endTime = System.currentTimeMillis();
	    System.out.println("keySetForGetKey运行时间" + (endTime - startTime));
	}
	//获取key和value
	public static void keySetForGetKeyAndValue(Map<String, String> map){
	    long startTime = System.currentTimeMillis();
	    for (String key : map.keySet()) {
	        String value = map.get(key);
	    }
	    long endTime = System.currentTimeMillis();
	    System.out.println("keySetForGetKeyAndValue运行时间" + (endTime - startTime));
	}
	
	/**
	 * keySet的iterator迭代器方式：
	 * @param args
	 */
	
	//只获取key
	public static void keySetIteratorGetKey(Map<String, String> map){
	    long startTime = System.currentTimeMillis();
	    Iterator<String> iterator = map.keySet().iterator();
	    while (iterator.hasNext()) {
	        String key = iterator.next();
	    }
	    long endTime = System.currentTimeMillis();
	    System.out.println("keySetIteratorGetKey运行时间" + (endTime - startTime));
	}
	//获取key和value
	public static void keySetIteratorGetKeyAndValue(Map<String, String> map){
	    long startTime = System.currentTimeMillis();
	    Iterator<String> iterator = map.keySet().iterator();
	    while (iterator.hasNext()) {
	        String key = iterator.next();
	        String value = map.get(iterator.next());
	    }
	    long endTime = System.currentTimeMillis();
	    System.out.println("keySetIteratorGetKeyAndValue运行时间" + (endTime - startTime));
	}
	
	/**
	 * entrySet的for循环方式：
	 * @param args
	 */
	//只获取key
	public static void entrySetForGetKey(Map<String, String> map){
	    long startTime = System.currentTimeMillis();
	    for (Entry<String, String> entry : map.entrySet()) {
	        String key = entry.getKey();
	    }
	    long endTime = System.currentTimeMillis();
	    System.out.println("entrySetForGetKey运行时间" + (endTime - startTime));
	}
	//获取key和value
	public static void entrySetForGetKeyAndValue(Map<String, String> map){
	    long startTime = System.currentTimeMillis();
	    for (Entry<String, String> entry : map.entrySet()) {
	        String key = entry.getKey();
	        String value = entry.getValue();
	    }
	    long endTime = System.currentTimeMillis();
	    System.out.println("entrySetForGetKeyAndValue运行时间" + (endTime - startTime));
	}
	
	/**
	 * entrySet的iterator迭代器方式：
	 * @param args
	 */
	//只获取key
	public static void entrySetIteratorGetKey(Map<String, String> map){
	    long startTime = System.currentTimeMillis();
	    Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
	    while (iterator.hasNext()) {
	        String key = iterator.next().getKey();
	    }
	    long endTime = System.currentTimeMillis();
	    System.out.println("entrySetIteratorGetKey运行时间" + (endTime - startTime));
	}
	//获取key和value
	public static void entrySetIteratorGetKeyAndValue(Map<String, String> map){
	    long startTime = System.currentTimeMillis();
	    Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
	    while (iterator.hasNext()) {
	        String key = iterator.next().getKey();
	        String value = iterator.next().getValue();
	    }
	    long endTime = System.currentTimeMillis();
	    System.out.println("entrySetIteratorGetKeyAndValue运行时间" + (endTime - startTime));
	}
	
	public static void main(String[] args) {
		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < 1000000; i++) {
		    map.put(i + "", i + "AA");
		}
		
		MapTest.keySetForGetKey(map);
		MapTest.keySetIteratorGetKey(map);
		MapTest.entrySetForGetKey(map);
		MapTest.entrySetIteratorGetKey(map);
		
		MapTest.keySetForGetKeyAndValue(map);
		MapTest.keySetIteratorGetKeyAndValue(map);
		MapTest.entrySetForGetKeyAndValue(map);
		MapTest.entrySetIteratorGetKeyAndValue(map);
	}

}
```

运行结果：

```java
keySetForGetKey运行时间96
keySetIteratorGetKey运行时间107
entrySetForGetKey运行时间112
entrySetIteratorGetKey运行时间153
keySetForGetKeyAndValue运行时间169
keySetIteratorGetKeyAndValue运行时间283
entrySetForGetKeyAndValue运行时间109
entrySetIteratorGetKeyAndValue运行时间138
```

>总结：
> entrySet的方式整体都是比keySet方式要高一些；  
> 单纯的获取key来说，两者的差别并不大，但是如果要获取value，还是entrySet的效率会更好，因为keySet需要从map中再次根据key获取value，而entrySet一次都全部获取出来；  
> iterator的迭代器方式比foreach的效率高。  

## 方法参数要求

在Java编程中，要尽量保证面向对象编程，并且达到高内聚，低耦合，实现动态扩展的特性。如果定义参数太多，会有以下缺点：

1. 违背面向对象编程
2. 可扩展性低
3. 方法调用出错概率大

建议参数保证在3~4个之内，尽量使用有明确意义的对象传参（**减少类似Map对象的使用**）。

## 尽量使用同步代码块替代同步方法，提高代码执行效率

## 常量定义为static final，并且名称使用大写，多个字符使用下划线拼接，比如：USER_NAME

## 及时关闭资源

使用资源一般都会建立流的连接，比如文件流，数据库连接等，在使用完成之后要及时关闭打开的连接，防止内存泄露。一般在finally中处理。

## 关闭多个资源时，需要分开执行，防止由于异常无法关闭所有的资源

## 使用带缓冲的输入输出流进行IO操作

## 避免未使用信息的定义

1. 不要定义、创建不使用的对象，变量；
2. 不要导入不需要的依赖包

## 字符串比对相等时，字符串常量写在字符串变量前面，比如"abc".equals(abc)

## 尽量使用局部变量

1. 方法参数和临时临时变量都在栈中分配，速度快
2. 类变量、实例变量存储在堆中速度较慢
3. 栈中的变量随时方法的结束而结束，不需要额外的垃圾回收

## 循环内不要创建对象的引用

减少堆栈中的使用，避免出现栈内存溢出或出现栈越界。

## 乘法和除法使用移位操作


1.1.1	尽量指定类、方法的final修饰符
带有final修饰符的类是不可派生的。在Java核心API中，有许多应用final的例子，例如java.lang.String，整个类都是final的。为类指定final修饰符可以让类不可以被继承，为方法指定final修饰符可以让方法不可以被重写。如果指定了一个类为final，则该类所有的方法都是final的。Java编译器会寻找机会内联所有的final方法，内联对于提升Java运行效率作用重大，具体参见Java运行期优化。此举能够使性能平均提高50% 。
1.1.2	尽量重用对象
特别是String对象的使用，出现字符串连接时应该使用StringBuilder/StringBuffer代替。由于Java虚拟机不仅要花时间生成对象，以后可能还需要花时间对这些对象进行垃圾回收和处理，因此，生成过多的对象将会给程序的性能带来很大的影响。
1.1.3	尽量减少对变量的重复计算
明确一个概念，对方法的调用，即使方法中只有一句语句，也是有消耗的，包括创建栈帧、调用方法时保护现场、调用方法完毕时恢复现场等。所以例如下面的操作：
<div align=center>

![1589105666035.png](..\images\1589105666035.png)

</div>
建议替换为：
<div align=center>

![1589105688607.png](..\images\1589105688607.png)

</div>


这样，在list.size很大的时候，就减少了很多的消耗
1.1.1	尽量采用懒加载的策略，即在需要的时候才创建
例如：
<div align=center>

![1589105713071.png](..\images\1589105713071.png)

</div>

建议替换为：
<div align=center>

![1589105738506.png](..\images\1589105738506.png)

</div>



1.1.1	慎用异常
异常对性能不利。抛出异常首先要创建一个新的对象，Throwable接口的构造函数调用名为fillInStackTrace的本地同步方法，fillInStackTrace方法检查堆栈，收集调用跟踪信息。只要有异常被抛出，Java虚拟机就必须调整调用堆栈，因为在处理过程中创建了一个新的对象。异常只能用于错误处理，不应该用来控制程序流程。
1.1.2	不要在循环中使用try…catch…，应该把其放在最外层
除非不得已。如果毫无理由地这么写了，只要你的领导资深一点、有强迫症一点，八成就要骂你为什么写出这种垃圾代码来了。
1.1.3	如果能估计到待添加的内容长度，为底层以数组方式实现的集合、工具类指定初始长度
比如ArrayList、LinkedLlist、StringBuilder、StringBuffer、HashMap、HashSet等等，以StringBuilder为例：
（1）StringBuilder // 默认分配16个字符的空间
（2）StringBuilder(int size) // 默认分配size个字符的空间
（3）StringBuilder(String str) // 默认分配16个字符+str.length个字符空间
可以通过类（这里指的不仅仅是上面的StringBuilder）的来设定它的初始化容量，这样可以明显地提升性能。比如StringBuilder吧，length表示当前的StringBuilder能保持的字符数量。因为当StringBuilder达到最大容量的时候，它会将自身容量增加到当前的2倍再加2，无论何时只要StringBuilder达到它的最大容量，它就不得不创建一个新的字符数组然后将旧的字符数组内容拷贝到新字符数组中—-这是十分耗费性能的一个操作。试想，如果能预估到字符数组中大概要存放5000个字符而不指定长度，最接近5000的2次幂是4096，每次扩容加的2不管，那么：
（1）在4096 的基础上，再申请8194个大小的字符数组，加起来相当于一次申请了12290个大小的字符数组，如果一开始能指定5000个大小的字符数组，就节省了一倍以上的空间；
（2）把原来的4096个字符拷贝到新的的字符数组中去。
这样，既浪费内存空间又降低代码运行效率。所以，给底层以数组实现的集合、工具类设置一个合理的初始化容量是错不了的，这会带来立竿见影的效果。但是，注意，像HashMap这种是以数组+链表实现的集合，别把初始大小和你估计的大小设置得一样，因为一个table上只连接一个对象的可能性几乎为0。初始大小建议设置为2的N次幂，如果能估计到有2000个元素，设置成new HashMap(128)、new HashMap(256)都可以。
1.1.4	当复制大量数据时，使用System.arraycopy命令
1.1.5	基于效率和类型检查的考虑，应该尽可能使用array，无法确定数组大小时才使用ArrayList
1.1.6	尽量使用HashMap、ArrayList、StringBuilder，除非线程安全需要，否则不推荐使用Hashtable、Vector、StringBuffer，后三者由于使用同步机制而导致了性能开销
1.1.7	不要将数组声明为public static final
因为这毫无意义，这样只是定义了引用为static final，数组的内容还是可以随意改变的，将数组声明为public更是一个安全漏洞，这意味着这个数组可以被外部类所改变。
1.1.8	尽量在合适的场合使用单例
使用单例可以减轻加载的负担、缩短加载的时间、提高加载的效率，但并不是所有地方都适用于单例，简单来说，单例主要适用于以下三个方面：
（1）控制资源的使用，通过线程同步来控制资源的并发访问
（2）控制实例的产生，以达到节约资源的目的
（3）控制数据的共享，在不建立直接关联的条件下，让多个不相关的进程或线程之间实现通信
1.1.9	尽量避免随意使用静态变量
要知道，当某个对象被定义为static的变量所引用，那么gc通常是不会回收这个对象所占有的堆内存的，如：
<div align=center>

![1589105773318.png](..\images\1589105773318.png)

</div>
此时静态变量b的生命周期与A类相同，如果A类不被卸载，那么引用B指向的B对象会常驻内存，直到程序终止
1.1.1	及时清除不再需要的会话
为了清除不再活动的会话，许多应用服务器都有默认的会话超时时间，一般为30分钟。当应用服务器需要保存更多的会话时，如果内存不足，那么操作系统会把部分数据转移到磁盘，应用服务器也可能根据MRU（最近最频繁使用）算法把部分不活跃的会话转储到磁盘，甚至可能抛出内存不足的异常。如果会话要被转储到磁盘，那么必须要先被序列化，在大规模集群中，对对象进行序列化的代价是很昂贵的。因此，当会话不再需要时，应当及时调用HttpSession的invalidate方法清除会话。
1.1.2	实现RandomAccess接口的集合比如ArrayList，应当使用最普通的for循环而不是foreach循环来遍历
这是JDK推荐给用户的。JDK API对于RandomAccess接口的解释是：实现RandomAccess接口用来表明其支持快速随机访问，此接口的主要目的是允许一般的算法更改其行为，从而将其应用到随机或连续访问列表时能提供良好的性能。实际经验表明，实现RandomAccess接口的类实例，假如是随机访问的，使用普通for循环效率将高于使用foreach循环；反过来，如果是顺序访问的，则使用Iterator会效率更高。可以使用类似如下的代码作判断：
<div align=center>

![1589105799147.png](..\images\1589105799147.png)

</div>

foreach循环的底层实现原理就是迭代器Iterator，参见Java语法糖1：可变长度参数以及foreach循环原理。所以后半句”反过来，如果是顺序访问的，则使用Iterator会效率更高”的意思就是顺序访问的那些类实例，使用foreach循环去遍历。
1.1.1	程序运行过程中避免使用反射
关于，请参见反射。反射是Java提供给用户一个很强大的功能，功能强大往往意味着效率不高。不建议在程序运行过程中使用尤其是频繁使用反射机制，特别是Method的invoke方法，如果确实有必要，一种建议性的做法是将那些需要通过反射加载的类在项目启动的时候通过反射实例化出一个对象并放入内存—-用户只关心和对端交互的时候获取最快的响应速度，并不关心对端的项目启动花多久时间。
1.1.2	使用数据库连接池和线程池
这两个池都是用于重用对象的，前者可以避免频繁地打开和关闭连接，后者可以避免频繁地创建和销毁线程
1.1.3	顺序插入和随机访问比较多的场景使用ArrayList，元素删除和中间插入比较多的场景使用LinkedList这个，理解ArrayList和LinkedList的原理就知道了
1.1.4	请知道，在java中if (i == 1)和if (1 == i)是没有区别的，但从阅读习惯上讲，建议使用前者
平时有人问，”if (i == 1)”和”if (1== i)”有没有区别，这就要从C/C++讲起。
在C/C++中，”if (i == 1)”判断条件成立，是以0与非0为基准的，0表示false，非0表示true，如果有这么一段代码：
<div align=center>

![1589105828461.png](..\images\1589105828461.png)

</div>

C/C++判断”i==1″不成立，所以以0表示，即false。但是如果：
<div align=center>

![1589105851862.png](..\images\1589105851862.png)

</div>
万一程序员一个不小心，把”if (i == 1)”写成”if (i = 1)”，这样就有问题了。在if之内将i赋值为1，if判断里面的内容非0，返回的就是true了，但是明明i为2，比较的值是1，应该返回的false。这种情况在C/C++的开发中是很可能发生的并且会导致一些难以理解的错误产生，所以，为了避免开发者在if语句中不正确的赋值操作，建议将if语句写为：
<div align=center>

![1589105871911.png](..\images\1589105871911.png)

</div>
这样，即使开发者不小心写成了”1 = i”，C/C++编译器也可以第一时间检查出来，因为我们可以对一个变量赋值i为1，但是不能对一个常量赋值1为i。
但是，在Java中，C/C++这种”if (i = 1)”的语法是不可能出现的，因为一旦写了这种语法，Java就会编译报错”Type mismatch: cannot convert from int to boolean”。但是，尽管Java的”if (i == 1)”和”if (1 == i)”在语义上没有任何区别，但是从阅读习惯上讲，建议使用前者会更好些。
1.1.1	不要对数组使用toString方法
看一下对数组使用toString打印出来的是什么：
<div align=center>

![1589105894320.png](..\images\1589105894320.png)

</div>

结果是：
<div align=center>

![1589105913035.png](..\images\1589105913035.png)

</div>

本意是想打印出数组内容，却有可能因为数组引用is为空而导致空指针异常。不过虽然对数组toString没有意义，但是对集合toString是可以打印出集合里面的内容的，因为集合的父类AbstractCollections重写了Object的toString方法。

1.1.1	不要对超出范围的基本数据类型做向下强制转型
这绝不会得到想要的结果：
<div align=center>

![1589105935359.png](..\images\1589105935359.png)

</div>

我们可能期望得到其中的某几位，但是结果却是：
1942892530
解释一下。Java中long是8个字节64位的，所以12345678901234在计算机中的表示应该是：
0000 0000 0000 0000 0000 1011 0011 1010 0111 0011 1100 1110 0010 1111 1111 0010
一个int型数据是4个字节32位的，从低位取出上面这串二进制数据的前32位是：
0111 0011 1100 1110 0010 1111 1111 0010
这串二进制表示为十进制1942892530，所以就是我们上面的控制台上输出的内容。从这个例子上还能顺便得到两个结论：
1、整型默认的数据类型是int，long l = 12345678901234L，这个数字已经超出了int的范围了，所以最后有一个L，表示这是一个long型数。顺便，浮点型的默认类型是double，所以定义float的时候要写成””float f = 3.5f”
2、接下来再写一句”int ii = l + i;”会报错，因为long + int是一个long，不能赋值给int
1.1.1	公用的集合类中不使用的数据一定要及时remove掉
如果一个集合类是公用的（也就是说不是方法里面的属性），那么这个集合里面的元素是不会自动释放的，因为始终有引用指向它们。所以，如果公用集合里面的某些数据不使用而不去remove掉它们，那么将会造成这个公用集合不断增大，使得系统有内存泄露的隐患。
1.1.2	把一个基本数据类型转为字符串，基本数据类型.toString是最快的方式、String.valueOf次之、数据+””最慢
把一个基本数据类型转为一般有三种方式，我有一个Integer型数据i，可以使用i.toString、String.valueOf(i)、i+””三种方式，三种方式的效率如何，看一个测试：
<div align=center>

![1589105969178.png](..\images\1589105969178.png)

</div>
运行结果为：
<div align=center>

![1589105992058.png](..\images\1589105992058.png)

</div>



所以以后遇到把一个基本数据类型转为String的时候，优先考虑使用toString方法。至于为什么，很简单：
1、String.valueOf方法底层调用了Integer.toString方法，但是会在调用前做空判断
2、Integer.toString方法就不说了，直接调用了
3、i + “”底层使用了StringBuilder实现，先用append方法拼接，再用toString方法获取字符串
三者对比下来，明显是2最快、1次之、3最慢
