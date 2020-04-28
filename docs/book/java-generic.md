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
    - [JVM中的实现](#jvm中的实现)
    - [参考](#参考)

<!-- /TOC -->
# Java泛型详解

## 背景

泛型在JDK1.5引入，其本质是一种**参数化类型，在使用时传入实际类型即可**，可以作用在类、接口、方法中。泛型是编译期的一种概念，主要是用于编译期类型安全检查（编译之后泛型会被擦除）。

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

泛型是编译期的概念，在编译之后的字节码中不包含泛型的信息。使用泛型的时候加上的类型参数，会被编译器在编译的时候去掉。这个过程就称为**类型擦除**。比如：`List<Object>`和 `List<String>`等类型，在编译之后都会变成 `List`。JVM 看到的只是 List，而由泛型附加的类型信息对JVM来说是不可见的。

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

## JVM中的实现

## 参考

1. [Java 泛型在实际开发中的应用](https://www.cnblogs.com/ldh-better/p/7127308.html#_label0)
2. [Java泛型详解](https://www.jianshu.com/p/986f732ed2f1)
