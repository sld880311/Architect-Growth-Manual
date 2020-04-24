# Java中异常详解

## 概述

在Java中任务或方法的运行，要不就是正常执行完成(包括虚拟机退出，比如`System.exit()`)，要不就是出现异常终止(`Throwable`)。本章节重点讲解在Java中对异常的处理。当程序出现异常之后，Java会抛出一个封装好的异常堆栈信息，并且终止当前的方法，异常处理机制会将代码执行交给异常处理器。整体结构如下图所示：

<div align=center>

![1587714125412.png](..\images\1587714125412.png)

</div>

## 异常分类

### Throwable

```java
 * The {@code Throwable} class is the superclass of all errors and
 * exceptions in the Java language. Only objects that are instances of this
 * class (or one of its subclasses) are thrown by the Java Virtual Machine or
 * can be thrown by the Java {@code throw} statement. Similarly, only
 * this class or one of its subclasses can be the argument type in a
 * {@code catch} clause.
```

在Java中`Throwable`是所有异常类的父类，只有该类的子类才能被用于Java异常处理。该类的唯一两个子类是`Error`和`Exception`。

### Error

```java
 * An {@code Error} is a subclass of {@code Throwable}
 * that indicates serious problems that a reasonable application
 * should not try to catch. Most such errors are abnormal conditions.
 * The {@code ThreadDeath} error, though a "normal" condition,
 * is also a subclass of {@code Error} because most applications
 * should not try to catch it.
 * <p>
 * A method is not required to declare in its {@code throws}
 * clause any subclasses of {@code Error} that might be thrown
 * during the execution of the method but not caught, since these
 * errors are abnormal conditions that should never occur.
 *
 * That is, {@code Error} and its subclasses are regarded as unchecked
 * exceptions for the purposes of compile-time checking of exceptions.
```

Error在正常情况下不应该出现的异常（一般是JVM本身产生的异常，比如JVM运行错误、`NoClassDefFoundError`或`OutOfMemoryError`），而且不建议应用程序对其进行捕获。Error被定义为非检查异常。Error 类是指 java 运行时系统的内部错误和资源耗尽错误。应用程序不会抛出该类对象。如果出现了这样的错误，除了告知用户，剩下的就是尽力使程序安全的终止。  包括以下类：

<div align=center>

![1587719292555.png](..\images\1587719292555.png)

</div>

### Exception（RuntimeException、CheckedException）

```java
 * <p>The class {@code Exception} and any subclasses that are not also
 * subclasses of {@link RuntimeException} are <em>checked
 * exceptions</em>.  Checked exceptions need to be declared in a
 * method or constructor's {@code throws} clause if they can be thrown
 * by the execution of the method or constructor and propagate outside
 * the method or constructor boundary.
```

`Exception`用于处理应用程序方面的异常定义和处理。分为`RuntimeException`和`CheckedException`(非`RuntimeException`)。检查类异常需要在方法或者构造器中明确的进行处理（throws）。

#### RuntimeException

```java
 * {@code RuntimeException} is the superclass of those
 * exceptions that can be thrown during the normal operation of the
 * Java Virtual Machine.
 *
 * <p>{@code RuntimeException} and its subclasses are <em>unchecked
 * exceptions</em>.  Unchecked exceptions do <em>not</em> need to be
 * declared in a method or constructor's {@code throws} clause if they
 * can be thrown by the execution of the method or constructor and
 * propagate outside the method or constructor boundary.
```

`RuntimeException`是在JVM的正常操作期间可以抛出异常的超类。该类都是未检查异常类，未检查异常不需要在方法或构造函数中进行throws，如果他们可以通过该方法或构造函数的执行被抛出和方法或构造边界之外传播。所以通常用不着捕获RuntimeException，但在自己的封装里，也许仍然要选择抛出一部分RuntimeException。常见的类包括：

<div align=center>

![1587718395409.png](..\images\1587718395409.png)

</div>

#### CheckedException

Exception下除`RuntimeException`和`Error`之外的异常类都是`CheckedException`。它们都在java.lang库内部定义。Java编译器要求程序必须捕获或声明抛出这种异常。常用的类包括： I/O 错误导致的 IOException、SQLException。这类异常的表现形式一般为：

1. 试图在文件尾部读取数据  
2. 试图打开一个错误格式的 URL  
3. 试图根据给定的字符串查找 class 对象，而这个字符串表示的类并不存在

#### CheckedException与RuntimeException的区别

1. CheckedException需要显示的处理，throws；RuntimeException不需要。
2. RuntimeException运行期间的错误，一般都是代码bug；CheckedException编译期间的错误，一般是外部错误。Java 编译器会强制程序去捕获此类异常（ try catch）。

## 异常的处理方式

### 抛出

当程序中出现异常时，如果不进行具体处理，可以使用`throw`、`throws` 、系统自动抛出三种方式进行异常抛出处理。

#### throw与throws的区别

1. 位置不同：throws在函数或构造器定义中，throw是函数或构造器内
2. 功能不同：throws用于异常声明，让调用者知道可能出现的异常；throw抛出异常，业务处理终止，抛出到上层业务
3. 含义不同：throws 表示出现异常的一种可能性，并不一定会发生这些异常；throw 则是抛出了异常，执行 throw 则一定抛出了某种异常对象。  throw需要配合throws使用。
4. 两者都是消极处理异常的方式，只是抛出或者可能抛出异常，但是不会由函数去处理异常，真正的处理异常由函数的上层调用处理。

### 捕获

如果程序中需要进行异常的特殊处理或者进行异常转换，则需要使用try、catch进行处理。

### 处理的伪代码

```java
try{
 //代码块
}
catch(ExceptionType e){
 //此违例类型的控制代码
}finally{
 //清除回收等工作
}
```

首先执行try中包含的代码块，如果遇到执行错误，程序掷出（throw）一特定类型的违例，你捕捉到此违例并转而执行catch中的违例控制代码。最后，无论程序是否产生违例都必须执行finally中的代码，其主要为一些变量清除、资源回收（1）等工作。

## 异常的限制

1. 重写一个方法时，只能产生已在方法的基础类版本中定义的异常。
2. 重写的方法可以抛出父类方法所抛出的异常或它的子类型
3. 重写的方法可以不用抛出父类方法所抛出的异常
4. 重写的方法不可以抛出异常如果父类方法没有抛出异常
5. 对异常的限制并不适用于构建器。

## 异常匹配

掷”出一个异常后，异常控制系统会按当初编写的顺序搜索“最接近”的控制器。一旦找到相符的控制器，就认为异常已得到控制，不再进行更多的搜索工作。在异常和它的控制器之间，并不需要非常精确的匹配。一个衍生类对象可与基础类的一个异常控制器相配，**即我们在写代码时，将子类写在前面**。

## JVM中处理异常的原理

### 异常的执行顺序

1、new一个异常对象  
2、终止当前的执行程序。  
3、弹出异常对象的引用。  
4、异常处理机制接管被终止的执行程序。  
5、寻找一个恰当的地点（异常处理程序）继续执行程序。  

### 异常处理的理论模型

1. **终止模型**：这种模型将假设错误非常关键，以至于程序无法返回到异常发生的地方继续执行，一旦异常抛出错误就意味着世界末日，意味着死亡，意味着GG
2. **恢复模型**：异常处理程序发现了错误，并且修复了错误然后重新调用出问题的方法，并且认为第二次调用该方法会成功。通常可以将try块放入while循环中，不断执行方法，直到得到满意的结果。

### 源码分析

#### 异常信息构建：Throwable

```java
/**
* Constructs a new throwable with {@code null} as its detail message.
* The cause is not initialized, and may subsequently be initialized by a
* call to {@link #initCause}.
*
* <p>The {@link #fillInStackTrace()} method is called to initialize
* the stack trace data in the newly created throwable.
*/
public Throwable() {
    /**
    * 填充执行过程中的堆栈信息，
    * 此方法在Throwable对象中记录当前线程的栈帧的状态信息
    */
    fillInStackTrace();
}
```

#### 核心代码：fillInStackTrace

```java

/**
* A shared value for an empty stack.
*/
private static final StackTraceElement[] UNASSIGNED_STACK = new StackTraceElement[0];

/**
* The stack trace, as returned by {@link #getStackTrace()}.
*
* The field is initialized to a zero-length array.  A {@code
* null} value of this field indicates subsequent calls to {@link
* #setStackTrace(StackTraceElement[])} and {@link
* #fillInStackTrace()} will be be no-ops.
*
* @serial
* @since 1.4
*/
private StackTraceElement[] stackTrace = UNASSIGNED_STACK;


/**
* Fills in the execution stack trace. This method records within this
* {@code Throwable} object information about the current state of
* the stack frames for the current thread.
*
* <p>If the stack trace of this {@code Throwable} {@linkplain
* Throwable#Throwable(String, Throwable, boolean, boolean) is not
* writable}, calling this method has no effect.
*
* @return  a reference to this {@code Throwable} instance.
* @see     java.lang.Throwable#printStackTrace()
*/
public synchronized Throwable fillInStackTrace() {
    if (stackTrace != null ||
        backtrace != null /* Out of protocol state */ ) {
        fillInStackTrace(0);
        stackTrace = UNASSIGNED_STACK;
    }
    return this;
}


/**
* 1. 底层本地方法获取当前线程的堆栈信息
* 2. 执行非常耗时。
* 3.如果只是关系异常的传播性质，而不关心异常的堆栈信息，可重写fillInStackTrace()方法。
*/
private native Throwable fillInStackTrace(int dummy);
```

#### StackTraceElement

```java
/**
 * An element in a stack trace, as returned by {@link
 * Throwable#getStackTrace()}.  Each element represents a single stack frame.
 * All stack frames except for the one at the top of the stack represent
 * a method invocation.  The frame at the top of the stack represents the
 * execution point at which the stack trace was generated.  Typically,
 * this is the point at which the throwable corresponding to the stack trace
 * was created.
 *
 * @since  1.4
 * @author Josh Bloch
 */
public final class StackTraceElement implements java.io.Serializable {
    // Normally initialized by VM (public constructor added in 1.5)
    private String declaringClass;// 方法的类名
    private String methodName; //方法名
    private String fileName;//文件名
    private int    lineNumber;// 调用的行数

    /**
     * Creates a stack trace element representing the specified execution
     * point.
     *
     * @param declaringClass the fully qualified name of the class containing
     *        the execution point represented by the stack trace element
     * @param methodName the name of the method containing the execution point
     *        represented by the stack trace element
     * @param fileName the name of the file containing the execution point
     *         represented by the stack trace element, or {@code null} if
     *         this information is unavailable
     * @param lineNumber the line number of the source line containing the
     *         execution point represented by this stack trace element, or
     *         a negative number if this information is unavailable. A value
     *         of -2 indicates that the method containing the execution point
     *         is a native method
     * @throws NullPointerException if {@code declaringClass} or
     *         {@code methodName} is null
     * @since 1.5
     */
    public StackTraceElement(String declaringClass, String methodName,
                             String fileName, int lineNumber) {
        this.declaringClass = Objects.requireNonNull(declaringClass, "Declaring class is null");
        this.methodName     = Objects.requireNonNull(methodName, "Method name is null");
        this.fileName       = fileName;
        this.lineNumber     = lineNumber;
    }
}
```

## 其他

### 违例的作用

1)监视程序中的异常情况  
2)当异常情况发生时，将控制权交给你自己编写的违例控制代码  

### 使用准则

(1) 解决问题并再次调用造成违例的方法。  
(2) 平息事态的发展，并在不重新尝试方法的前提下继续。  
(3) 计算另一些结果，而不是希望方法产生的结果。  
(4) 在当前环境中尽可能解决问题，以及将相同的违例重新“掷”出一个更高级的环境。  
(5) 在当前环境中尽可能解决问题，以及将不同的违例重新“掷”出一个更高级的环境。  
(6) 中止程序执行。  
(7) 简化编码。若违例方案使事情变得更加复杂，那就会令人非常烦恼，不如不用。  
(8) 使自己的库和程序变得更加安全。这既是一种“短期投资”（便于调试），也是一种“长期投资”（改善应用程序的健壮性）  

**异常的处理包括业务类处理（给于用户更好的友好提示）和bug类处理（链条式异常信息输出，方便运维人员或研发人员快速定位问题）。**

### 异常相关的关键字

`try，catch，throw，throws，finally`

### finally的使用总结

#### finally不被执行的场景

1. 与try配套使用，所以只有try执行finally才会执行
2. 如果try中执行System.exit(0);或jvm异常终止，则否finally不会被执行

#### finally语句在return语句执行之后return返回之前执行

```java
package com.sunld.finally1;
public class FinallyTest1 {
	public static void main(String[] args) {
		System.out.println(test1());//4.输出100
	}
	public static int test1() {
        int b = 20;
        try {
            System.out.println("try block");//1
            return b += 80; 
        }catch (Exception e) {
            System.out.println("catch block");
        }finally {
            System.out.println("finally block");//2
            if (b > 25) {
                System.out.println("b>25, b = " + b);//3
            }
        }
        return b;
    }
}
```

```java
package com.sunld.finally1;
public class FinallyTest2 {
	public static void main(String[] args) {
		System.out.println(test11());// 4.after return
	}
	public static String test11() {
        try {
            System.out.println("try block");//1
           return test12();
      } finally {
           System.out.println("finally block");//3
       }
	}
	public static String test12() {
       System.out.println("return statement");//2
       return "after return";
	}
}
```

#### finally块中的return语句会覆盖try块中的return返回

```java
package com.sunld.finally1;
public class FinallyTest3 {
	public static void main(String[] args) {
		System.out.println(test2());//200
	}
	public static int test2() {
        int b = 20;
        try {
            System.out.println("try block");//1
            return b += 80;
        } catch (Exception e) {
            System.out.println("catch block");
        } finally {
            System.out.println("finally block");//2
            if (b > 25) {
                System.out.println("b>25, b = " + b);//3
            }
            return 200;
        }
    }
}
```

这说明finally里的return直接返回了，就不管try中是否还有返回语句.

#### finally语句中没有return语句覆盖返回值，返回值的变化

用例1：
```java
package com.sunld.finally1;
public class FinallyTest4 {
	public static void main(String[] args) {
		System.out.println(test3());//4:100
	}
	public static int test3() {
        int b = 20;
        try {
            System.out.println("try block");//1
            return b += 80;
        } catch (Exception e) {
            System.out.println("catch block");
        } finally {
            System.out.println("finally block");//2
            if (b > 25) {
                System.out.println("b>25, b = " + b);//3
            }
            b = 150;
        }
        return 2000;
    }
}
```

用例2：

```java
package com.sunld.finally1;
import java.util.HashMap;
import java.util.Map;
public class FinallyTest6 {
	public static void main(String[] args) {
		System.out.println(getMap().get("KEY").toString());//FINALLY
	}
	public static Map<String, String> getMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("KEY", "INIT");
        try {
            map.put("KEY", "TRY");
            return map;
        }
        catch (Exception e) {
            map.put("KEY", "CATCH");
        }
        finally {
            map.put("KEY", "FINALLY");
            map = null;
        }
        return map;
    }
}
```

#### try块里的return语句在异常的情况下不会被执行

```java
package com.sunld.finally1;
public class FinallyTest5 {
	public static void main(String[] args) {
		System.out.println(test4());//5:204
	}
	public static int test4() {
        int b = 20;
        try {
            System.out.println("try block");//1
            b = b / 0;
            return b += 80;
        } catch (Exception e) {
            b += 15;
            System.out.println("catch block");//2
        } finally {
            System.out.println("finally block");//3
            if (b > 25) {
                System.out.println("b>25, b = " + b);//4
            }
            b += 50;
        }
        return 204;
    }
}
```

#### 当发生异常后，catch中的return执行情况与未发生异常时try中return的执行情况完全一样

```java
package com.sunld.finally1;
public class FinallyTest7 {
	public static void main(String[] args) {
		System.out.println(test5());//5:35
	}
	public static int test5() {
        int b = 20;
        try {
            System.out.println("try block");//1
            b = b /0;
            return b += 80;
        } catch (Exception e) {
            System.out.println("catch block");//2
            return b += 15;
        } finally {
            System.out.println("finally block");//3
            if (b > 25) {
                System.out.println("b>25, b = " + b);//4
            }
            b += 50;
        }
        //return b;
    }
}
```

#### 总结

1. finally语句在return语句执行之后return返回之前执行
2. finally块中的return语句会覆盖try块中的return返回
3. 如果finally语句中没有return语句,且覆盖了返回值，那么原来的返回值原始类型则不覆盖，对象类型则覆盖
4. try块里的return语句在异常的情况下不会被执行
5. 当发生异常后，catch中的return执行情况与未发生异常时try中return的执行情况完全一样。

### 自定义异常

#### 自定义异常的优点

1. 统一了对外异常展示的方式。
2. 方便框架统一处理`@ControllerAdvice`
3. 定义业务类异常
4. 隐藏底层的异常，这样更安全，异常信息也更加的直观

#### 自定义异常的注意事项

1. 所有异常都必须是 Throwable 的子类。
2. 如果希望写一个检查性异常类，则需要继承 Exception 类。
3. 如果你想写一个运行时异常类，那么需要继承 RuntimeException 类。

### 异常捕获的陷阱

#### 正确关闭资源的方式

对于物理资源（数据库连接、网络连接、磁盘文件），JVM是不会进行处理的，因为JVM属于Java内存管理的一部分，只负责回收堆内存中分配的空间。  
**关闭资源：**

1. 必须要保证一定执行，一次要放在finally中完成
2. 必须保证被关闭的资源不为空
3. 保证资源之间的关闭操作互不影响

#### finally块的陷阱

##### finally块的执行规则

1. 如果调用了System.exit(0);finally将不再执行，
2. 当System.exit(0)被执行时，虚拟机在退出之前要完成两项工作：
   1. 执行系统中注册的所有钩子
   2. 如果程序调用了System.runFinalizersOnExit(true);那么JVM会对所有未结束的对象调用Finalize
   
   ```java
    final FileOutputStream fos = new FileOutputStream("");
    Runtime.getRuntime().addShutdownHook(new Thread(){
        public void run(){
            if(fos!=null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });
    System.exit(0);
   ```

##### finally块和方法返回值

当Java程序执行try、catch遇到return语句时，return语句会导致该方法会立即结束；系统执行return语句之后并不会立即结束该方法，而是去寻找异常处理过程中是否有finally，如果有则会执行finally代码块，在执行finally块时如果该块中没有return则会直接返回到try中的return，结束该方法，如果有则会直接返回finally中的数据，而不会调用try中的return。

#### catch的用法

1. catch的顺序: 先处理小异常在处理大异常
2. 不要用catch代替流程控制
3. 只能catch可能抛出的异常(减少大范围catch异常)
4. 实际的修复
   1. 如果程序知道如何修复这个异常，应该在catch中修复这个异常，修复之后可以再次调用这个方法；
   2. 如果程序不知道如何修复并且系统也没有进行任何修复，千万不要再次调用可能导致该异常的方法。（造成内存溢出），不要在finally块中调用可能引起异常的方法，可能会导致无限递归、内存溢出

#### 继承得到的异常

1. 子类重写父类方法时，不能抛出比父类方法类型更多、范围更大的异常
2. 抛出的异常只能是父类异常中的交集，否则不能通过编译。

### 异常的处理流程

<div align=center>

![1587727204173.png](..\images\1587727204173.png)

</div>

### 异常拦截

系统的异常处理机制是衡量一个系统设计的关键因素，良好的异常处理机制能在系统出现异常时准确的找到问题的所在。spring aop对异常的处理有良好的支持。spring（spring全家桶中增加了很多异常统一处理的接口和AOP，比如`@ControllerAdvice`） 提供了一个接口 `ThrowsAdvice`，该接口里面没有任何方法，但是实现类里面必须的实现。

```java
//可以处理详细的异常信息
afterThrowing(Method method, Object[] args, Object target, RuntimeException  throwable)
//方便快速记录发生的异常
afterThrowing(RuntimeException  throwable)
```

### ClassNotFoundException和NoClassDefFoundError的区别

NoClassDefFoundError是一个错误(Error)，而ClassNOtFoundException是一个异常，在Java中错误和异常是有区别的，我们可以从异常中恢复程序但却不应该尝试从错误中恢复程序。

#### ClassNotFoundException的产生原因

1. 使用`Class.forName（ClassLoader.loadClass、ClassLOader.findSystemClass）`加载对象时，如果没有找到则会出现该异常
2. 当一个类已经某个类加载器加载到内存中了，此时另一个类加载器又尝试着动态地从同一个包中加载这个类。
3. ClassNotFoundException发生在装入阶段。
4. 加载时从外存储器找不到需要的class就出现ClassNotFoundException

#### NoClassDefFoundError产生的原因

1. JVM或者ClassLoader实例尝试加载（可以通过正常的方法调用，也可能是使用new来创建新的对象）类的时候却找不到类的定义。要查找的类在编译的时候是存在的，运行的时候却找不到了。
2. NoClassDefFoundError： 当目前执行的类已经编译，但是找不到它的定义时
3. 连接时从内存找不到需要的class就出现NoClassDefFoundError

#### NoClassDefFoundError 解决的三种方法

1. Simple example of NoClassDefFoundError is class belongs to a jar and jar was not added into classpath or sometime jar’s name has been changed by someone like in my case one of my colleague has changed tibco.jar into tibco_v3.jar and by program is failing with java.lang.NoClassDefFoundError and I was wondering what’s wrong. 
首先是类在运行的时候依赖于其它的一个jar包，但是该jar包没有加载到classpath中或者是该jar包的名字被其他人改了，就像我的一个例子tibo.jar改为了tibco_v3.jar……. 

2. Class is not in Classpath, there is no sure shot way of knowing it but many a times you can just have a look to print System.getproperty(”java.classpath“)and it will print the classpath from there you can at least get an idea of your actual runtime classpath. 
运行的类不在classpath中，这个问题没有一个确定的方法去知道，但是很多时候你可以通过System.getproperty(”java.classpath“)方法，该方法能让你至少可以领略到实际存在的运行期间的classpath。
 
3. Just try to run with explicitly -classpath option with the classpath you think will work and if its working then it’s sure short sign that some one is overriding java classpath. 
试着通过-classpath命令明确指出你认为正确的classpath，如果能够正常执行的话就说明你使用的classpath是正确的，而系统中的classpath已经被修该过了。

#### 类装载方式

##### 显示类装载

显式 类装入发生在使用以下方法调用装入的类的时候：

- cl.loadClass()（cl 是 java.lang.ClassLoader 的实例）
- Class.forName()（启动的类装入器是当前类定义的类装入器）

当调用其中一个方法的时候，指定的类（以类名为参数）由类装入器装入。如果类已经装入，那么只是返回一个引用；否则，装入器会通过委托模型装入类。

##### 隐式类装载

隐式 类装入发生在由于引用、实例化或继承导致装入类的时候（不是通过显式方法调用）。在每种情况下，装入都是在幕后启动的，JVM 会解析必要的引用并装入类。与显式类装入一样，如果类已经装入了，那么只是返回一个引用；否则，装入器会通过委托模型装入类。

## 参考

- [JAVA 异常详解](https://blog.csdn.net/jygqm/article/details/81364636)
- [JAVA基础——异常详解](https://www.cnblogs.com/hysum/p/7112011.html)
- [Java 异常基础详解(详细的使用方式，可参考)](https://www.cnblogs.com/nwgdk/p/8862353.html)
- [Java基础篇——异常详解](https://zhuanlan.zhihu.com/p/108423001)
- [Java异常实现及原理](https://blog.csdn.net/qq_31615049/article/details/80952216)
- 《疯狂Java》
