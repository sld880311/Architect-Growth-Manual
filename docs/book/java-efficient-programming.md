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

