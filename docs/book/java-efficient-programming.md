<!-- TOC -->

- [JAVA高效编程技巧总结](#java%e9%ab%98%e6%95%88%e7%bc%96%e7%a8%8b%e6%8a%80%e5%b7%a7%e6%80%bb%e7%bb%93)
  - [高效遍历MAP](#%e9%ab%98%e6%95%88%e9%81%8d%e5%8e%86map)

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
