<!-- TOC -->

- [JAVA高效编程技巧总结](#java高效编程技巧总结)
	- [Java代码规范](#java代码规范)
		- [命名规范（阿里规范）](#命名规范阿里规范)
		- [常量定义](#常量定义)
	- [高效能开发的原则](#高效能开发的原则)
		- [优化代码的注意事项](#优化代码的注意事项)
		- [Java编码原则](#java编码原则)
	- [集合遍历效率问题](#集合遍历效率问题)
		- [高效遍历MAP](#高效遍历map)
		- [实现RandomAccess接口的集合使用for循环而不是foreach来遍历](#实现randomaccess接口的集合使用for循环而不是foreach来遍历)
	- [线程问题](#线程问题)
	- [资源处理注意事项](#资源处理注意事项)
	- [垃圾信息处理](#垃圾信息处理)
	- [尽量使用局部变量](#尽量使用局部变量)
	- [提高效率，减少内存](#提高效率减少内存)
	- [关键字和方法的说明](#关键字和方法的说明)
		- [equals的正确使用](#equals的正确使用)
		- [final的正确使用](#final的正确使用)
		- [方法参数要求](#方法参数要求)
		- [不要对数组使用toString方法](#不要对数组使用tostring方法)
	- [异常使用注意事项](#异常使用注意事项)
	- [合理使用初始化长度](#合理使用初始化长度)
	- [类型转换](#类型转换)
	- [日志规范](#日志规范)

<!-- /TOC -->
# JAVA高效编程技巧总结

## Java代码规范

### 命名规范（阿里规范）

1. 名称只能是有含义的英文，不能出现下划线或美元符号
2. 方法名、参数名、成员变量、局部变量统一使用驼峰方式命名，形如lowerCamelCase
3. 类名使用UpperCamelCase命名，DO/BO/DTO/VO/AO/PO/UID除外
4. 常量或类变量都需要使用大写，并且中间使用下划线分割
5. 抽象类必须以Abstract或Base开头，异常类，必须以Exception结果，测试类必须以待测试代码_Test
6. 类型与中括号紧挨相连来表示数组,形如`int[] arrayDemo`
7. 成员变量不能以is开头
8. 包使用小写定义，并且使用单数，中间使用英文句号分割，并且定义必须有意义和唯一性
9. 子类与父类之间不要使用相同的成员变量命名
10. 完全杜绝不规范的缩写，尽量使用完整的单词
11. 在常量与变量命名时，表示类型的名称放在词尾
12. 如果模块、接口、类、方法使用了设计模式，在命名时需体现出具体模式
13. 接口中不要定义变量，方式不需要增加任何修饰符，都必须有注释信息
14. 接口与实现类的命名规范是**Service/**ServiceImpl、**DAO/**DAOImpl
15. 枚举类名带上 Enum 后缀，枚举成员名称需要全大写，单词间用下划线隔开
16. 各层命名规范：
    - Service/DAO 层方法命名规约
      - 获取单个对象的方法用 get 做前缀。
      - 获取多个对象的方法用 list 做前缀，复数形式结尾如：listObjects。
      - 获取统计值的方法用 count 做前缀。
      - 插入的方法用 save/insert 做前缀。
      - 删除的方法用 remove/delete 做前缀。
      - 修改的方法用 update 做前缀。
    - 领域模型命名规约
      - 数据对象：xxxDO，xxx 即为数据表名。
      - 数据传输对象：xxxDTO，xxx 为业务领域相关的名称。
      - 展示对象：xxxVO，xxx 一般为网页名称。
      - POJO 是 DO/DTO/BO/VO 的统称，禁止命名成 xxxPOJO。

### 常量定义

1. 不允许任何魔法值（即未经预先定义的常量）直接出现在代码中，使用常量替换
2. 定义类型为long或Long的属性时，需要用大写L结尾
3. 常量定义要分类维护，一般分为：跨应用共享常量、应用内共享常量、子工程内共享常量、包内共享常量、类内共享常量
4. 如果变量值仅在一个固定范围内变化用 enum 类型来定义

## 高效能开发的原则

### 优化代码的注意事项

1. 除非必须优化，否则不要轻易改动
2. 改动之后要进行仔细的测试
3. 在各个JVM产品中不存在一劳永逸的成本模型

### Java编码原则

1. 注重设计、数据结构、算法选择
2. 不要过分依赖编译器编译期的优化技术，正确理解Java运行期的实际效果
3. 将对象的创建和使用降到最低：
   - StringBuilder的使用
   - for循环中对象的索引使用
   - 线程或连接的复用
   - 合理设计对象的大小（在类加载过程会完成内存大小的计算）
   - 合理设计继承关系（不多于3层）
   - 构造函数尽可能短小精干
   - 对象创建使用懒加载思想
4. 降低同步的影响范围
   - 是否需要使用同步控制或者线程安全的类
   - 控制同步范围
   - 合理使用锁变量，做到线程分离，提高并发度和吞吐量
   - 减少锁的使用，避免出现死锁
5. 尽量在栈中完成业务处理
6. 使用static、final、private函数促成inlining
7. 实例变量初始化合适就好，比如单例模式
8. 注意集合的使用
   - 选择合适的遍历方式
   - 使用大小的控制
   - 数据复制的选择
   - 选择合适的集合类
9. 尽可能重用对象
10. 增加缓存概念

## 集合遍历效率问题

### 高效遍历MAP

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
> entrySet的方式整体都是比keySet方式要高一些  
> 单纯的获取key来说，两者的差别并不大，但是如果要获取value，还是entrySet的效率会更好，因为keySet需要从map中再次根据key获取value，而entrySet一次都全部获取出来  
> iterator的迭代器方式比foreach的效率高  

### 实现RandomAccess接口的集合使用for循环而不是foreach来遍历

这是JDK推荐给用户的。JDK API对于RandomAccess接口的解释是：实现RandomAccess接口用来表明其：色nu支持快速随机访问，此接口的主要目的是允许一般的算法更改其行为，从而将其应用到随机或连续访问列表时能提供良好的性能。实际经验表明，实现RandomAccess接口的类实例，假如是随机访问的，使用普通for循环效率将高于使用foreach循环；反过来，如果是顺序访问的，则使用Iterator会效率更高。可以使用类似如下的代码作判断：
<div align=center>

![1589105799147.png](..\images\1589105799147.png)

</div>

foreach循环的底层实现原理就是迭代器Iterator，参见Java语法糖1：可变长度参数以及foreach循环原理。所以后半句”反过来，如果是顺序访问的，则使用Iterator会效率更高”的意思就是顺序访问的那些类实例，使用foreach循环去遍历。

## 线程问题

1. 尽量使用同步代码块替代同步方法，提高代码执行效率
2. 尽量使用HashMap、ArrayList、StringBuilder，除非线程安全需要，否则不推荐使用Hashtable、Vector、StringBuffer，后三者由于使用同步机制而导致了性能开销

## 资源处理注意事项

1. 及时关闭资源：使用资源一般都会建立流的连接，比如文件流，数据库连接等，在使用完成之后要及时关闭打开的连接，防止内存泄露。一般在finally中处理。
2. 关闭多个资源时，需要分开执行，防止由于异常无法关闭所有的资源

## 垃圾信息处理

1. 不要定义、创建不使用的对象，变量；
2. 不要导入不需要的依赖包
3. 公用的集合类中不使用的数据一定要及时remove掉
4. ThreadLocal中不使用的数据要及时处理掉，防止内存溢出
5. 及时清除不再需要的会话（会话超时设置），防止出现内存不足或内存磁盘之间交互频繁，当会话不再需要时，应当及时调用HttpSession的invalidate方法清除会话。

## 尽量使用局部变量

1. 方法参数和临时临时变量都在栈中分配，速度快
2. 类变量、实例变量存储在堆中速度较慢
3. 栈中的变量随时方法的结束而结束，不需要额外的垃圾回收

## 提高效率，减少内存

1. 使用StringBuilder/StringBuffer替代String
2. 循环内不要创建对象的引用：减少堆栈中的使用，避免出现栈内存溢出或出现栈越界。
3. 尽量采用懒加载的策略，即在需要的时候才创建
4. 尽量避免随意使用静态变量：当某个对象被定义为static的变量所引用，那么gc通常是不会回收这个对象所占有的堆内存的
5. 尽量在合适的场合使用单例（减轻加载的负担、缩短加载的时间、提高加载的效率）
   - 控制资源的使用，通过线程同步来控制资源的并发访问
   - 控制实例的产生，以达到节约资源的目的
   - 控制数据的共享，在不建立直接关联的条件下，让多个不相关的进程或线程之间实现通信
6. 使用数据库连接池和线程池：前者可以避免频繁地打开和关闭连接，后者可以避免频繁地创建和销毁线程
7. 使用带缓冲的输入输出流进行IO操作
8. 乘法和除法使用移位操作
9. 减少对变量的重复计算：对方法的调用，即使方法中只有一句语句，也是有消耗的，包括创建栈帧、调用方法时保护现场、调用方法完毕时恢复现场等。集合中大小的重复获取会消耗内存和时间。
10. 当复制大量数据时，使用System.arraycopy命令
11. 基于效率和类型检查的考虑，应该尽可能使用array，无法确定数组大小时才使用ArrayList
12. 程序运行过程中避免使用反射（根据实际情况定）：反射是Java提供给用户一个很强大的功能，功能强大往往意味着效率不高。特别是Method的invoke方法，如果确实有必要，一种建议性的做法是将那些需要通过反射加载的类在项目启动的时候通过反射实例化出一个对象并放入内存—-用户只关心和对端交互的时候获取最快的响应速度，并不关心对端的项目启动花多久时间。
13. 顺序插入和随机访问比较多的场景使用ArrayList，元素删除和中间插入比较多的场景使用LinkedList这个

## 关键字和方法的说明

### equals的正确使用

1. 重写equals后需要同时完成hashcode方法的重写
2. equals中初始代码比对的是对象地址
3. 字符串比对相等时，字符串常量写在字符串变量前面，比如"abc".equals(abc)

### final的正确使用

1. 如果类不能被派生，则必须定义为final（该类中的方法都是final的）
2. 如果方法不能被重写，则必须定义为final
3. 常量定义为static final，并且名称使用大写，多个字符使用下划线拼接，比如：USER_NAME
4. 不要将数组声明为public static final（final只是表示引用不变，但是内容还是可以变）

### 方法参数要求

在Java编程中，要尽量保证面向对象编程，并且达到高内聚，低耦合，实现动态扩展的特性。如果定义参数太多，会有以下缺点：

1. 违背面向对象编程
2. 可扩展性低
3. 方法调用出错概率大

建议参数保证在3~4个之内，尽量使用有明确意义的对象传参（**减少类似Map对象的使用**）。

### 不要对数组使用toString方法

1. 数组为空会出现空指针
2. 打印的数据是地址信息，与预期不一致
3. 对集合toString是可以打印出集合里面的内容的，因为集合的父类AbstractCollections重写了Object的toString方法。

## 异常使用注意事项

1. 慎用异常：异常对性能不利。抛出异常首先要创建一个新的对象，Throwable接口的构造函数调用名为fillInStackTrace的本地同步方法，fillInStackTrace方法检查堆栈，收集调用跟踪信息。只要有异常被抛出，Java虚拟机就必须调整调用堆栈，因为在处理过程中创建了一个新的对象。异常只能用于错误处理，不应该用来控制程序流程。
2. 不要在循环中使用try…catch…，应该把其放在最外层，除非不得已

## 合理使用初始化长度

1. 集合：ArrayList、LinkedLlist等
2. 字符串：StringBuilder、StringBuffer等
3. Map：HashMap等
4. Set：HashSet等

## 类型转换

1. 不要对超出范围的基本数据类型做向下强制转型
   - 整型默认的数据类型是int，long需要在结尾增加`L`
   - 浮点型的默认类型是double，所以定义float的时候要写成`float f = 3.5f`
   - long+int会自动转型为long
2. 基本类型（包装类）转String：`toString > String.valueOf > +`，可以通过源码得到原因：
   - String.valueOf方法底层调用了Integer.toString方法，但是会在调用前做空判断
   - Integer.toString，直接调用了
   - i + “”底层使用了StringBuilder实现，先用append方法拼接，再用toString方法获取字符串

## 日志规范

1. 日志级别：OFF、FATAL、ERROR、WARN、INFO、DEBUG、ALL，常用级别**ERROR、WARN、INFO和DEBUG**
2. ERROR
   - 表示不能自己恢复的错误，需要立即关注和解决
   - 比如：数据库连接错误、网络错误、未知系统错误
   - 需要接入监控和报警系统 
3. WARN
   - 表示可预知的错误，业务场景类错误
   - 比如：参数验证、权限认证 
4. INFO
   - 记录系统的基本运行过程和运行状态
   - 包括：系统状态变化、业务流程的核心处理、关键动作、业务流状态的变化 
5. DEBUG
   - 调试信息 