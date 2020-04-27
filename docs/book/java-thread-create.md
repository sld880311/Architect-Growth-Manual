<!-- TOC -->

- [线程的创建](#%e7%ba%bf%e7%a8%8b%e7%9a%84%e5%88%9b%e5%bb%ba)
  - [通过Thread构建](#%e9%80%9a%e8%bf%87thread%e6%9e%84%e5%bb%ba)
    - [Thread定义](#thread%e5%ae%9a%e4%b9%89)
    - [示例](#%e7%a4%ba%e4%be%8b)
  - [通过Runnable构建](#%e9%80%9a%e8%bf%87runnable%e6%9e%84%e5%bb%ba)
  - [通过ExecutorService、Callable、Future构建](#%e9%80%9a%e8%bf%87executorservicecallablefuture%e6%9e%84%e5%bb%ba)
  - [基于线程池构建](#%e5%9f%ba%e4%ba%8e%e7%ba%bf%e7%a8%8b%e6%b1%a0%e6%9e%84%e5%bb%ba)
  - [线程启动的注意事项](#%e7%ba%bf%e7%a8%8b%e5%90%af%e5%8a%a8%e7%9a%84%e6%b3%a8%e6%84%8f%e4%ba%8b%e9%a1%b9)
  - [参考](#%e5%8f%82%e8%80%83)

<!-- /TOC -->

# 线程的创建

## 通过Thread构建

Thread 类本质上是实现了 Runnable 接口的一个实例，代表一个线程的实例。启动线程的唯一方法就是通过 Thread 类的 start()实例方法。start()方法是一个 native 方法，它将启动一个新线程。

### Thread定义

### 示例

```java
package com.sunld.thread;
public class OneThread extends Thread{
	@Override
	public void run() {
		System.out.println("OneThread.run() by extends Thread");
	}
	public static void main(String[] args) {
		Thread o = new OneThread();
		o.start();
	}
}
```

## 通过Runnable构建

如果自己的类已经 extends 另一个类，就无法直接 extends Thread，此时，可以实现一个 Runnable 接口。

```java
package com.sunld.thread;
public class ThreadRunnable implements Runnable{
	@Override
	public void run() {
		System.out.println("====ThreadRunnable=====Runnable====");
	}
	public static void main(String[] args) {
		//实例化线程
		Thread t = new Thread(new ThreadRunnable());
		t.start();
	}
}
```

## 通过ExecutorService、Callable、Future构建

有返回值的任务必须实现 Callable 接口，类似的，无返回值的任务必须 Runnable 接口。执行Callable 任务后，可以获取一个 Future 的对象，在该对象上调用 get 就可以获取到 Callable 任务返回的 Object 了，再结合线程池接口 ExecutorService 就可以实现传说中有返回结果的多线程了。

```java
package com.sunld.thread;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
public class ExecutorsThread {
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		int taskSize = 10;
		// 创建线程池
		ExecutorService pool = Executors.newFixedThreadPool(taskSize);
		// 创建有返回值的任务
		List<Future<String>> list = new ArrayList<>();
		for(int i = 0; i < taskSize; i++) {
			list.add(pool.submit(new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "ddd:111  " + Thread.currentThread().getName();
				}
			}));
		}
		//关闭线程池
		pool.shutdown();
		// 获取结果
		for(Future<String> f : list) {
			System.out.println(f.get());
		}
	}
}
```

## 基于线程池构建

线程和数据库连接这些资源都是非常宝贵的资源。那么每次需要的时候创建，不需要的时候销毁，是非常浪费资源的。那么我们就可以使用缓存的策略，也就是使用线程池。

```java
package com.sunld.thread;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class ExecutorsThread2 {
	public static void main(String[] args) {
		ExecutorService pool = Executors.newFixedThreadPool(10);
		while(true) {
			pool.execute(new Runnable() {
				@Override
				public void run() {
					System.out.println(Thread.currentThread().getName() + ": is running");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
}
```

## 线程启动的注意事项

无论何种方式，启动一个线程，就要给它一个名字！这对排错诊断 系统监控有帮助。否则诊断问题时，无法直观知道某个线程的用途。

<div align=center>

![1587983785160.png](..\images\1587983785160.png)

</div>

## 参考

1. 《Java并发编程的艺术》
2. [Java中的多线程你只要看这一篇就够了](https://www.cnblogs.com/wxd0108/p/5479442.html)
3. [线程](https://baike.baidu.com/item/%E7%BA%BF%E7%A8%8B/103101?fr=aladdin)
4. [Java总结篇系列：Java多线程（一）](https://www.cnblogs.com/lwbqqyumidi/p/3804883.html)
5. 《java并发编程实战》
6. [多线程01：《疯狂Java讲义》学习笔记——线程概述](https://blog.csdn.net/hanhaiyinheguxing/article/details/51366541)
7. [java并发编程---如何创建线程以及Thread类的使用](https://blog.csdn.net/hla199106/article/details/47840505)
8. [Java并发编程：Thread类的使用](https://www.cnblogs.com/dolphin0520/p/3920357.html)
9. [Java中断机制](https://www.cnblogs.com/loveer/p/11518402.html)
10. [JAVA多线程之中断机制(如何处理中断？)](https://www.cnblogs.com/hapjin/p/5450779.html)
