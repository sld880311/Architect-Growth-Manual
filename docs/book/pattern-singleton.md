<!-- TOC -->

- [单例模式详解](#单例模式详解)
    - [意图](#意图)
    - [适用性](#适用性)
    - [结构](#结构)
    - [参与者](#参与者)
        - [Singleton](#singleton)
    - [实现方式](#实现方式)
        - [懒汉模式（延迟加载）](#懒汉模式延迟加载)
            - [普通实现-非线程安全](#普通实现-非线程安全)
            - [锁在方法上(synchronized)-线程安全](#锁在方法上synchronized-线程安全)
            - [同步代码块（synchronized）-线程不安全](#同步代码块synchronized-线程不安全)
            - [DCL模式实现-线程安全](#dcl模式实现-线程安全)
                - [为什么需要使用volatile](#为什么需要使用volatile)
        - [饿汉模式（立即加载）](#饿汉模式立即加载)
        - [静态内部类实现](#静态内部类实现)
        - [静态代码块](#静态代码块)
        - [枚举实现单例](#枚举实现单例)
        - [单例注册工厂](#单例注册工厂)
    - [协作](#协作)
    - [效果](#效果)
        - [对唯一实例的受控访问](#对唯一实例的受控访问)
        - [缩小名空间](#缩小名空间)
        - [允许对操作和表示的精化](#允许对操作和表示的精化)
        - [允许可变数目的实例](#允许可变数目的实例)
        - [比类操作更灵活](#比类操作更灵活)
        - [优点](#优点)
        - [缺点](#缺点)
    - [实现原理](#实现原理)
        - [保证一个唯一的实例](#保证一个唯一的实例)
        - [创建Singleton类的子类](#创建singleton类的子类)
    - [经典例子](#经典例子)
        - [Spring框架中实现的例子](#spring框架中实现的例子)
    - [相关模式](#相关模式)
    - [参考](#参考)

<!-- /TOC -->
# 单例模式详解

（Singleton Pattern、单态模式、单件模式、对象创建型模式），单例模式的特点：

1. 构造方法私有化；(不能被实例化和继承)
2. 实例化的变量引用私有化；
3. 获取实例的方法共有。

## 意图

1. 唯一的对象实例
2. 保证一个类仅有一个实例，并提供一个访问他的全局访问点
3. 有状态（可变的单例对象，可以组成状态仓库）、无状态（工具类）

## 适用性

在下面的情况下可以使用Singleton模式：

1. 当类只能有一个实例而且客户可以从一个总所周知的访问点访问它时。
2. 当这个唯一实例应该是通过子类可扩展的，并且客户应该无需更改代码就能使用一个扩展的实例时。
3. 系统只需要一个实例的对象，而这个对象又会被经常创建。

## 结构

<div align=center>

![1588206615300.png](..\images\1588206615300.png)

</div>

## 参与者

### Singleton

1. 定义一个Instance操作，允许客户访问它的唯一实例。Instance是一个类操作。
2. 可能负责创建它自己的唯一实例。

## 实现方式

### 懒汉模式（延迟加载）

#### 普通实现-非线程安全

```java
public class Singleton1 {
	private Singleton1(){
	}
	private static Singleton1 instance=null;
	public static Singleton1 getInstance(){
		if(instance==null){
			instance = new Singleton1();
		}
		return instance;
	}
}
```

> 总结：非线程安全

#### 锁在方法上(synchronized)-线程安全

```java
public class Singleton1 {
	private Singleton1(){
	}
	private static Singleton1 instance=null;
	public static synchronized Singleton1 getInstance(){
		if(instance==null){
			instance = new Singleton1();
		}
		return instance;
	}
}
```

> 总结：线程安全，效率低

#### 同步代码块（synchronized）-线程不安全

```java
public class Singleton1 {
	private Singleton1(){
	}
	private static Singleton1 instance=null;
	public static Singleton1 getInstance(){
        synchronized(Singleton1.class){
            if(instance==null){
                instance = new Singleton1();
            }
        }
		return instance;
	}
}
```

> 总结：线程安全，效率低

#### DCL模式实现-线程安全

```java
package com.sunld;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Singleton1 {
	private Singleton1(){
	}
	// 防止重排序
	private static volatile Singleton1 instance = null;
	public static Singleton1 getInstance(){
		if(instance == null) {
			synchronized(Singleton1.class){
				if(instance == null){
					instance = new Singleton1();
				}
			}
		}
		return instance;
	}
	
	public static void main(String[] args) {
		int size = 10;
		ExecutorService es = Executors.newFixedThreadPool(10);
		for(int i = 0; i <= size; i++ ) {
			es.execute(new Runnable() {
				@Override
				public void run() {
					System.out.println(Thread.currentThread().getName() 
							+  Singleton1.getInstance());
				}
			});
		}
	}
}
```

##### 为什么需要使用volatile

`instance = new Singleton()`主要是完成了以下三个事情

1. 给instance实例分配内存；
2. 初始化instance的构造器；
3. 将instance对象指向分配的内存空间（注意到这步时instance就非null了）

JVM会进行指令优化为：  

1. 给instance实例分配内存；
2. 将instance对象指向分配的内存空间；---会出现线程安全问题
3. 初始化instance的构造器；

```java
警告: 二进制文件Test/bin/com.sunld.Singleton1包含com.sunld.Singleton1
Classfile /D:/Workspaces/java/TestJVM/Test/bin/com/sunld/Singleton1.class
  Last modified 2020-4-30; size 620 bytes
  MD5 checksum 69d5cbecd81034e77060fee87c23526c
  Compiled from "Singleton1.java"
public class com.sunld.Singleton1
  minor version: 0
  major version: 52
  flags: ACC_PUBLIC, ACC_SUPER
Constant pool:
   #1 = Class              #2             // com/sunld/Singleton1
   #2 = Utf8               com/sunld/Singleton1
   #3 = Class              #4             // java/lang/Object
   #4 = Utf8               java/lang/Object
   #5 = Utf8               instance
   #6 = Utf8               Lcom/sunld/Singleton1;
   #7 = Utf8               <clinit>
   #8 = Utf8               ()V
   #9 = Utf8               Code
  #10 = Fieldref           #1.#11         // com/sunld/Singleton1.instance:Lcom/sunld/Singleton1;
  #11 = NameAndType        #5:#6          // instance:Lcom/sunld/Singleton1;
  #12 = Utf8               LineNumberTable
  #13 = Utf8               LocalVariableTable
  #14 = Utf8               <init>
  #15 = Methodref          #3.#16         // java/lang/Object."<init>":()V
  #16 = NameAndType        #14:#8         // "<init>":()V
  #17 = Utf8               this
  #18 = Utf8               getInstance
  #19 = Utf8               ()Lcom/sunld/Singleton1;
  #20 = Methodref          #1.#16         // com/sunld/Singleton1."<init>":()V
  #21 = Utf8               StackMapTable
  #22 = Class              #23            // java/lang/Class
  #23 = Utf8               java/lang/Class
  #24 = Class              #25            // java/lang/Throwable
  #25 = Utf8               java/lang/Throwable
  #26 = Utf8               SourceFile
  #27 = Utf8               Singleton1.java
{
  static {};
    descriptor: ()V
    flags: ACC_STATIC
    Code:
      stack=1, locals=0, args_size=0
         0: aconst_null
         1: putstatic     #10                 // Field instance:Lcom/sunld/Singleton1;
         4: return
      LineNumberTable:
        line 10: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature

  public static com.sunld.Singleton1 getInstance();
    descriptor: ()Lcom/sunld/Singleton1;
    flags: ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=1, args_size=0
         0: getstatic     #10                 // Field instance:Lcom/sunld/Singleton1;
         3: ifnonnull     35
         6: ldc           #1                  // class com/sunld/Singleton1
         8: dup
         9: astore_0
        10: monitorenter
        11: getstatic     #10                 // Field instance:Lcom/sunld/Singleton1;
        14: ifnonnull     27
        17: new           #1                  // class com/sunld/Singleton1
        20: dup
        21: invokespecial #20                 // Method "<init>":()V
        24: putstatic     #10                 // Field instance:Lcom/sunld/Singleton1;
        27: aload_0
        28: monitorexit
        29: goto          35
        32: aload_0
        33: monitorexit
        34: athrow
        35: getstatic     #10                 // Field instance:Lcom/sunld/Singleton1;
        38: areturn
      Exception table:
         from    to  target type
            11    29    32   any
            32    34    32   any
      LineNumberTable:
        line 12: 0
        line 13: 6
        line 14: 11
        line 15: 17
        line 13: 27
        line 19: 35
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
      StackMapTable: number_of_entries = 3
        frame_type = 252 /* append */
          offset_delta = 27
          locals = [ class java/lang/Class ]
        frame_type = 68 /* same_locals_1_stack_item */
          stack = [ class java/lang/Throwable ]
        frame_type = 250 /* chop */
          offset_delta = 2
}
SourceFile: "Singleton1.java"

```

### 饿汉模式（立即加载）

```java
public class Singleton {
	private Singleton(){
	}
	private static final Singleton instance = new Singleton();
	public static Singleton getInstance(){
		return instance;
	}
}
```

> 结论:
> 1. 线程安全
> 2. 在获取变量的过程中不能有其他操作，以防出现线程安全问题

### 静态内部类实现

```java
package com.sunld;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingletonInner implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private SingletonInner() {}
	
	private static class SingletonInner1{
		private static SingletonInner instance = new SingletonInner();
	}
	
	public static SingletonInner getInstance(){
		return SingletonInner1.instance;
	}
	
	/**
	 * 防止序列化之后变成多例
	 * @return
	 * @throws ObjectStreamException
	 */
	protected Object readResoObject() throws ObjectStreamException{
		return SingletonInner1.instance;
		
	}

	public static void main(String[] args) {
		int size = 10;
		ExecutorService es = Executors.newFixedThreadPool(10);
		for(int i = 0; i <= size; i++ ) {
			es.execute(new Runnable() {
				@Override
				public void run() {
					System.out.println(Thread.currentThread().getName() 
							+  SingletonInner.getInstance());
				}
			});
		}
	}

}
```

### 静态代码块

```java
package com.sunld;

public class Singleton2 {
	private Singleton2(){
	}
	private static Singleton2 instance = null;
	static {
		instance = new Singleton2();
	}
	public static Singleton2 getInstance(){
		return instance;
	}
}
```

### 枚举实现单例

```java
package com.sunld;

public class EnumSingleton {
	//私有化构造函数
	private EnumSingleton(){ }
	//定义一个静态枚举类
	static enum SingletonEnum{
		//创建一个枚举对象，该对象天生为单例
        INSTANCE;
		private EnumSingleton enumSingleton;
		//私有化枚举的构造函数
        private SingletonEnum(){
        	enumSingleton = new EnumSingleton();
        }
        public EnumSingleton getInstnce(){
            return enumSingleton;
        }
	}
	//对外暴露一个获取EnumSingleton对象的静态方法
    public static EnumSingleton getInstance(){
        return SingletonEnum.INSTANCE.getInstnce();
    }
}
```

> 总结：
> 枚举可以解决反序列化会破坏单例的问题
> > 在枚举序列化的时候，Java仅仅是将枚举对象的name属性输出到结果中，反序列化的时候则是通过java.lang.Enum的valueOf方法来根据名字查找枚举对象。同时，编译器是不允许任何对这种序列化机制的定制的，因此禁用了writeObject、readObject、readObjectNoData、writeReplace和readResolve等方法。

### 单例注册工厂

```java
public final class SingletonRegistryFactory implements FactoryInterface{
	private static final int maxCapacity = 50;
	private static final SingletonRegistryFactory singleRegistry = new SingletonRegistryFactory();
	//保证线程安全
	private Map<String,Object> singletonCache = Collections.synchronizedMap(new LRULinkedHashMap<String, Object>(maxCapacity));
	/**
	 * 私有构造方法
	 */
	private SingletonRegistryFactory() {
	}
	public static SingletonRegistryFactory getInstance() {
		return singleRegistry;
	}

	public Object getSingleton(String beanName) {
		return this.singletonCache.get(beanName);
	}

	public void addSingleton(String beanName, Object sharedBean) {
		synchronized (this.singletonCache) {
			this.singletonCache.put(beanName, sharedBean);
		}
	}

	public void removeSingleton(String beanName) {
		synchronized (this.singletonCache) {
			this.singletonCache.remove(beanName);
		}
	}

	public void destroySingletons() {
		synchronized (this.singletonCache) {
			this.singletonCache.clear();
		}
	}
}
```

## 协作

客户职能通过Singleton的Instance操作访问一个Singleton的实例。

## 效果

### 对唯一实例的受控访问

因为Singleton类封装它的唯一实例，所以它可以严格的控制客户怎样以及何时访问它。

### 缩小名空间

Singleton模式是对全局变量的一种改进。它避免了那些存储唯一实例的全局变量污染名空间。

### 允许对操作和表示的精化

Singleton类可以有子类，而且用这个扩展类的实例来配置一个应用是很容易的。你可以用你所需要的类的实例在运行时刻配置应用。

### 允许可变数目的实例

这个模式使得你易于改变你的想法，并允许Singleton类的多个实例。此外，你可以用相同的方法来控制应用所使用的实例的数目。只允许访问Singleton实例的操作需要改变。

### 比类操作更灵活

另一种封装单件功能的方式是使用类操作。但这种语言技术难以改变设计你允许一个类有多个实例。此外，静态成员函数不是虚函数，因此子类不能多态的重定义它们。

### 优点

1. 实例控制，保证实例的唯一性
2. 灵活性，因为类控制了实例化过程，所以类可以灵活更改实例化过程。

### 缺点

1. 需要提供详细文档提供开发者使用，防止开发混乱
2. 不能解决删除单个对象的问题，可以使用缓存管理技术管理单例对象

## 实现原理

### 保证一个唯一的实例

Singleton模式使得这个唯一实例是类的一般实例，但该类被写成只有一个实例能被创建。做到这一点的一个常用方法是将创建这个实例的操作隐藏在一个类操作后面，由它保证只有一个实例被创建。这个操作可以访问保存唯一实例的变量，而且它可以保证这个变量在返回值之前用这个唯一实例初始化。这种方法保证了单件在它的首次使用前被创建和使用。

### 创建Singleton类的子类

主要问题与其说是定义子类不如说是建立它的唯一实例，这样客户就可以使用它。事实上，指向单件实例的变量必须用子类的实例进行初始化。最简单的技术是在Singleton的Instance操作中决定你想使用的是哪一个单件。  

另一个选择Singleton的子类的方法是将Instance的实现从父类中分离出来，将它放入子类。
一个更灵活的方法是使用一个单件注册表（registry of singleton）。可能的Singleton类的集合不是由Instance定义的，Singleton类可以根据名字在一个众所周知的注册表中注册它们的单件实例。
这个注册表在字符串名字和单件之间建立映射。当Instance需要一个单件时，它参考注册表，根据名字请求单件。

## 经典例子

各种工具类的使用，建议使用缓存技术管理单例对象

### Spring框架中实现的例子

```java
package org.springframework.aop.framework.adapter;
/**
 * Singleton to publish a shared DefaultAdvisorAdapterRegistry instance.
 * 抽象化类使其不可实例化
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @see DefaultAdvisorAdapterRegistry
 */
public abstract class GlobalAdvisorAdapterRegistry {
	/**
	 * Keep track of a single instance so we can return it to classes that request it.
	 */
	private static AdvisorAdapterRegistry instance = new DefaultAdvisorAdapterRegistry();
	/**
	 * Return the singleton {@link DefaultAdvisorAdapterRegistry} instance.
	 */
	public static AdvisorAdapterRegistry getInstance() {
		return instance;
	}
	/**
	 * Reset the singleton {@link DefaultAdvisorAdapterRegistry}, removing any
	 * {@link AdvisorAdapterRegistry#registerAdvisorAdapter(AdvisorAdapter) registered}
	 * adapters.
	 */
	static void reset() {
		instance = new DefaultAdvisorAdapterRegistry();
	}
}

```

## 相关模式

1. Abstract Factory Pattern中的具体工厂
2. Builder Pattern中的指导者
3. Facade Pattern中的Facade参与者
4. Prototype Pattern中的原型管理器

## 参考

1. [DCL单例模式](https://www.cnblogs.com/codingmengmeng/p/9846131.html)
2. [被面试官虐过之后，他轻蔑的问我：你还说你了解单例模式吗?](https://www.toutiao.com/i6821004595079152141/)