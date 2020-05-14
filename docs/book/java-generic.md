<!-- TOC -->

- [Java泛型详解](#java泛型详解)
    - [背景](#背景)
        - [常用泛型类型常量](#常用泛型类型常量)
        - [泛型反例](#泛型反例)
    - [泛型接口](#泛型接口)
    - [泛型类](#泛型类)
    - [泛型方法](#泛型方法)
    - [类型擦除](#类型擦除)
        - [定义](#定义)
        - [擦除过程](#擦除过程)
        - [泛型擦除的问题](#泛型擦除的问题)
    - [泛型边界](#泛型边界)
        - [类型通配符?](#类型通配符)
            - [具体说明](#具体说明)
    - [其他](#其他)
        - [优点](#优点)
        - [局限性](#局限性)
        - [泛型继承规则](#泛型继承规则)
    - [泛型与反射](#泛型与反射)
    - [参考](#参考)

<!-- /TOC -->
# Java泛型详解

## 背景

泛型在JDK1.5引入，其本质是一种**参数化类型（Parameterized Type），在使用时传入实际类型即可**，即可以将操作的数据类型指定为方法签名中的一种特殊参数,可以作用在类、接口、方法中。泛型是编译期的一种概念，主要是用于编译期类型安全检查（编译之后泛型会被擦除）。

### 常用泛型类型常量

```java
E：元素（Element），多用于java集合框架
K：关键字（Key）
N：数字（Number）
T：类型（Type）
V：值（Value）
```

### 泛型反例

```java
public class Test1 {
	public static void main(String[] args) {
		List list = new ArrayList();
		list.add("abc");
		list.add(100);
		list.add(10.09);
		for(int i = 0; i < list.size(); i++) {
			System.out.println((String)list.get(i));
		}
	}
}
```

编译期通过，并且运行结果如下：

```java
abc
Exception in thread "main" java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.String
	at com.sunld.Test1.main(Test1.java:14)
```

**如何在编译期完成校验？**

## 泛型接口

与泛型类的定义类似，参考代码如下：

```java
//定义
public interface Callable<V> {
    V call() throws Exception;
}
//实现方式1
class MyGeneric1<T> implements Callable<T>{
	@Override
	public T call() throws Exception {
		return null;
	}
}
//实现方式2
class MyGeneric2 implements Callable<String>{
	@Override
	public String call() throws Exception {
		return null;
	}
}
```

## 泛型类

泛型类的声明与非泛型类的类似，在类的名称后面增加类型参数。语法如下：  

```java
class className<泛型标识 extends|super 上限|下限, ...>{
  private 泛型标识 /*（成员变量类型）*/ genericType;
}
```

泛型类的类型参数声明部分也包含一个或多个类型参数，参数间用逗号隔开。如果上限或下限有多个限制，可以使用`&`处理。一个泛型参数，也被称为一个类型变量，是用于指定一个泛型类型名称的标识符。简单示例：  

```java
class MyGeneric<T>{
	private T t;
	public T getT() {
		return t;
	}
	public void setT(T t) {
		this.t = t;
	}
}
```

## 泛型方法

一种可以接受不同参数类型的方法，并且可以根据入参进行参数返回，尤其是反射处理数据转换比较常用。
> 注意：方法上是否定义泛型和类上是否定义没有必然的联系

语法参考如下：

```java
/**
* 1. 第一个T表示泛型声明，只有声明之后才能使用
* 2. 第二个T表示返回值
* 3.第三个T限制入参的返回需要与第一个T一致
*/
public static <T> T executeGenericMethod(Class<T> cls){
    try {
        return cls.newInstance();
    } catch (InstantiationException e) {
        e.printStackTrace();
    } catch (IllegalAccessException e) {
        e.printStackTrace();
    }
    return null;
}
```

## 类型擦除

### 定义

泛型是编译期的概念，在编译之后的字节码中不包含泛型的信息（为了解决该问题，java在字节码中引入Signature、LocalVariableTypeTable）。使用泛型的时候加上的类型参数，会被编译器在编译的时候去掉。这个过程就称为**类型擦除**。比如：`List<Object>`和 `List<String>`等类型，在编译之后都会变成 `List`。JVM 看到的只是 List，而由泛型附加的类型信息对JVM来说是不可见的。

```java
package com.sunld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestGenericType2<T> {
	
	private T t;
	
	private List<String> list;
	
	private static int temp1 = 10;
	private final int temp2 =20;
	private final static int temp3=30;
	
	

//	public T getT() {
//		return t;
//	}
//
//
//
//	public void setT(T t) {
//		this.t = t;
//	}
//
//
//
//	public List<String> getList() {
//		return list;
//	}
//
//
//
//	public void setList(List<String> list) {
//		this.list = list;
//	}



	public static void main(String[] args) {

		Map<String, String> map = new HashMap<String,String>();
		map.put("a", "valueaa");
		String value = map.get("a");
//		TestGenericType2<Integer>[] array = new TestGenericType2<Integer>[10];
//		TestGenericType2<Integer>[] array1 = new TestGenericType2[10];
	}

}
```

编译后的结果：  

```java
警告: 二进制文件Test/bin/com.sunld.TestGenericType2包含com.sunld.TestGenericType2
Classfile /D:/Workspaces/java/TestJVM/Test/bin/com/sunld/TestGenericType2.class
  Last modified 2020-5-14; size 1287 bytes
  MD5 checksum 7ad53533c7723c97d0ec9bb863b415ca
  Compiled from "TestGenericType2.java"
public class com.sunld.TestGenericType2<T extends java.lang.Object> extends java.lang.Object
  minor version: 0
  major version: 52
  flags: ACC_PUBLIC, ACC_SUPER
Constant pool:
   #1 = Class              #2             // com/sunld/TestGenericType2
   #2 = Utf8               com/sunld/TestGenericType2
   #3 = Class              #4             // java/lang/Object
   #4 = Utf8               java/lang/Object
   #5 = Utf8               t
   #6 = Utf8               Ljava/lang/Object;
   #7 = Utf8               Signature
   #8 = Utf8               TT;
   #9 = Utf8               list
  #10 = Utf8               Ljava/util/List;
  #11 = Utf8               Ljava/util/List<Ljava/lang/String;>;
  #12 = Utf8               temp1
  #13 = Utf8               I
  #14 = Utf8               temp2
  #15 = Utf8               ConstantValue
  #16 = Integer            20
  #17 = Utf8               temp3
  #18 = Integer            30
  #19 = Utf8               <clinit>
  #20 = Utf8               ()V
  #21 = Utf8               Code
  #22 = Fieldref           #1.#23         // com/sunld/TestGenericType2.temp1:I
  #23 = NameAndType        #12:#13        // temp1:I
  #24 = Utf8               LineNumberTable
  #25 = Utf8               LocalVariableTable
  #26 = Utf8               <init>
  #27 = Methodref          #3.#28         // java/lang/Object."<init>":()V
  #28 = NameAndType        #26:#20        // "<init>":()V
  #29 = Fieldref           #1.#30         // com/sunld/TestGenericType2.temp2:I
  #30 = NameAndType        #14:#13        // temp2:I
  #31 = Utf8               this
  #32 = Utf8               Lcom/sunld/TestGenericType2;
  #33 = Utf8               LocalVariableTypeTable
  #34 = Utf8               Lcom/sunld/TestGenericType2<TT;>;
  #35 = Utf8               main
  #36 = Utf8               ([Ljava/lang/String;)V
  #37 = Class              #38            // java/util/HashMap
  #38 = Utf8               java/util/HashMap
  #39 = Methodref          #37.#28        // java/util/HashMap."<init>":()V
  #40 = String             #41            // a
  #41 = Utf8               a
  #42 = String             #43            // valueaa
  #43 = Utf8               valueaa
  #44 = InterfaceMethodref #45.#47        // java/util/Map.put:(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
  #45 = Class              #46            // java/util/Map
  #46 = Utf8               java/util/Map
  #47 = NameAndType        #48:#49        // put:(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
  #48 = Utf8               put
  #49 = Utf8               (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
  #50 = InterfaceMethodref #45.#51        // java/util/Map.get:(Ljava/lang/Object;)Ljava/lang/Object;
  #51 = NameAndType        #52:#53        // get:(Ljava/lang/Object;)Ljava/lang/Object;
  #52 = Utf8               get
  #53 = Utf8               (Ljava/lang/Object;)Ljava/lang/Object;
  #54 = Class              #55            // java/lang/String
  #55 = Utf8               java/lang/String
  #56 = Utf8               args
  #57 = Utf8               [Ljava/lang/String;
  #58 = Utf8               map
  #59 = Utf8               Ljava/util/Map;
  #60 = Utf8               value
  #61 = Utf8               Ljava/lang/String;
  #62 = Utf8               Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;
  #63 = Utf8               SourceFile
  #64 = Utf8               TestGenericType2.java
  #65 = Utf8               <T:Ljava/lang/Object;>Ljava/lang/Object;
{
  static {};
    descriptor: ()V
    flags: ACC_STATIC
    Code:
      stack=1, locals=0, args_size=0
         0: bipush        10
         2: putstatic     #22                 // Field temp1:I
         5: return
      LineNumberTable:
        line 13: 0
        line 15: 5
      LocalVariableTable:
        Start  Length  Slot  Name   Signature

  public com.sunld.TestGenericType2();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=2, locals=1, args_size=1
         0: aload_0
         1: invokespecial #27                 // Method java/lang/Object."<init>":()V
         4: aload_0
         5: bipush        20
         7: putfield      #29                 // Field temp2:I
        10: return
      LineNumberTable:
        line 7: 0
        line 14: 4
        line 7: 10
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      11     0  this   Lcom/sunld/TestGenericType2;
      LocalVariableTypeTable:
        Start  Length  Slot  Name   Signature
            0      11     0  this   Lcom/sunld/TestGenericType2<TT;>;

  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: ACC_PUBLIC, ACC_STATIC
    Code:
      stack=3, locals=3, args_size=1
         0: new           #37                 // class java/util/HashMap
         3: dup
         4: invokespecial #39                 // Method java/util/HashMap."<init>":()V
         7: astore_1
         8: aload_1
         9: ldc           #40                 // String a
        11: ldc           #42                 // String valueaa
        13: invokeinterface #44,  3           // InterfaceMethod java/util/Map.put:(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
        18: pop
        19: aload_1
        20: ldc           #40                 // String a
        22: invokeinterface #50,  2           // InterfaceMethod java/util/Map.get:(Ljava/lang/Object;)Ljava/lang/Object;
        27: checkcast     #54                 // class java/lang/String
        30: astore_2
        31: return
      LineNumberTable:
        line 45: 0
        line 46: 8
        line 47: 19
        line 50: 31
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      32     0  args   [Ljava/lang/String;
            8      24     1   map   Ljava/util/Map;
           31       1     2 value   Ljava/lang/String;
      LocalVariableTypeTable:
        Start  Length  Slot  Name   Signature
            8      24     1   map   Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;
}
SourceFile: "TestGenericType2.java"
Signature: #65                          // <T:Ljava/lang/Object;>Ljava/lang/Object;
```

从Signature属性的得出结论，擦除法所谓的擦除，仅仅是对方法的Code属性中的字节码进行擦除，实际上元数据中还是保留了泛型信息，这也是我们在编码时能通过反射手段取得参数化类型的根本依据。

### 擦除过程

首先是找到用来替换类型参数的具体类。这个具体类一般是 Object。如果指定了类型参数的上界的话，则使用这个上界。把代码中的类型参数都替换成具体的类。

### 泛型擦除的问题

1. 用泛型不可以区分方法签名
2. 泛型类的静态变量是共享

## 泛型边界

1. <? extends T>表示该通配符所代表的类型是 T 类型的子类。
2. <? super T>表示该通配符所代表的类型是 T 类型的父类。

### 类型通配符?

在java中如果使用泛型，例如`List<Interger>和List<Number>`其实是两种类型(**类型之间转换会出现转换异常**)，之间没有任务关系，如果想要接收不同类型的参数，则需要引入通配符的概念，`List<?>`（？表示所有泛型中的父类）**泛型内是不存在父子关系，但是利用通配符可以产生类似的效果**。

#### 具体说明

假设给定的泛型类型为G,两个具体的泛型参数X、Y，当中Y是X的子类

1. G<? extends Y> 是 G<? extends X>的子类型
2. G<X> 是 G<? extends X>的子类型
3. G<?> 与 G<? extends Object>等同

## 其他

### 优点

1. 适用于多种数据类型执行相同的代码（代码复用）
2. 泛型中的类型在使用时指定，不需要强制类型转换（类型安全，编译器会检查类型）

### 局限性

1. 不能实例化泛型类：`T t = new T();`
2. 静态变量不能引用泛型变量：`private static T t1;`；非静态变量可以引用：`private T t1;`
3. 未声明泛型的方法不能引用泛型变量：`public static T getT1(){return t1;}`；声明之后可以：`public static <T> T executeGenericMethod(Class<T> cls){return null;}`
4. 基本类型无法作为泛型类型：`List<int> list = new ArrayList<>();`
5. 无法使用instanceof关键字或==判断泛型类的类型：`list instanceof List<String>` 或者`list == List<String>`
6. 泛型类的原生类型与所传递的泛型无关，无论传递什么类型，原生类是一样的，即类型擦除之后的class信息相同
7. 泛型数组可以声明但无法实例化：`Test1<Integer>[] array = new Test1<Integer>[10];`，去掉泛型即可`Test1<Integer>[] array = new Test1[10];`
8. 泛型类不能继承Exception或者Throwable
9. 不能捕获泛型类型限定的异常但可以将泛型限定的异常抛出

### 泛型继承规则

1. 对于泛型参数是继承关系的泛型类之间是没有继承关系的：`List<Integer>与List<Number>`
2. 泛型类可以继承其它泛型类，例如: `public class ArrayList<E> extends AbstractList<E>`
3. 泛型类的继承关系在使用中同样会受到泛型类型的影响

## 泛型与反射

```java
package com.sunld;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TestGenericType1<T> {

	private T t;

	public T getT() {
		return t;
	}

	public void setT(T t) {
		this.t = t;
	}
	
	public static void main(String[] args) {
		TestGenericType1<Integer> a = new TestGenericType1<Integer>() {};
		Type superclass = a.getClass().getGenericSuperclass();
		System.out.println(superclass);
	    //getActualTypeArguments 返回确切的泛型参数
		Type type = ((ParameterizedType)superclass).getActualTypeArguments()[0]; 
	    System.out.println(type);//class java.lang.Integer
	}
}
```

## 参考

1. [Java 泛型在实际开发中的应用](https://www.cnblogs.com/ldh-better/p/7127308.html#_label0)
2. [Java泛型详解](https://www.jianshu.com/p/986f732ed2f1)
3. [Java总结篇系列：Java泛型](https://www.cnblogs.com/lwbqqyumidi/p/3837629.html)
4. [java 泛型详解-绝对是对泛型方法讲解最详细的，没有之一](https://www.cnblogs.com/coprince/p/8603492.html)
