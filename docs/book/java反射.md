<!-- TOC -->

- [概述](#%e6%a6%82%e8%bf%b0)
  - [知识点](#%e7%9f%a5%e8%af%86%e7%82%b9)
  - [动态语言](#%e5%8a%a8%e6%80%81%e8%af%ad%e8%a8%80)
  - [定义](#%e5%ae%9a%e4%b9%89)
    - [场景](#%e5%9c%ba%e6%99%af)
- [使用](#%e4%bd%bf%e7%94%a8)
  - [API](#api)
    - [Class](#class)
    - [Field](#field)
    - [Method](#method)
    - [Constructor](#constructor)
    - [Annotation](#annotation)
  - [使用步骤（获取 Class 对象、调用对象方法）](#%e4%bd%bf%e7%94%a8%e6%ad%a5%e9%aa%a4%e8%8e%b7%e5%8f%96-class-%e5%af%b9%e8%b1%a1%e8%b0%83%e7%94%a8%e5%af%b9%e8%b1%a1%e6%96%b9%e6%b3%95)
  - [构建对象](#%e6%9e%84%e5%bb%ba%e5%af%b9%e8%b1%a1)
    - [Class对象的newInstance()](#class%e5%af%b9%e8%b1%a1%e7%9a%84newinstance)
    - [调用Constructor 对象的newInstance()](#%e8%b0%83%e7%94%a8constructor-%e5%af%b9%e8%b1%a1%e7%9a%84newinstance)

<!-- /TOC -->
# 概述
## 知识点
<div align=center>


![1587523711269.png](..\images\1587523711269.png)

</div>

## 动态语言
动态语言，是指程序在运行时可以改变其结构：新的函数可以引进，已有的函数可以被删除等结构上的变化。比如常见的 JavaScript 就是动态语言，除此之外 Ruby,Python 等也属于动态语言，而 C、C++则不属于动态语言。从反射角度说 JAVA 属于半动态语言。在Java中如果想获取到运行中对象的结构则需要引入反射的概念。

## 定义
在 Java 中的反射机制是指在运行状态中，对于任意一个类都能够知道这个类所有的属性和方法；并且对于任意一个对象，都能够调用它的任意一个方法；这种动态获取信息以及动态调用对象方法的功能成为 Java 语言的反射机制。
### 场景
在java中包括两种时期：编译期和运行期，对应的类型就是编译时类型和运行时类型。编译时类型由声明时的对象决定，运行时类型则由实际的对象类型决定（**主要表现为行为，对于成员变量则编译时确认**）。比如：
```java
//其中编译时类型为 Person，运行时类型为 Man。  
Person p=new Man();
```
由于编译时类型无法获取具体方法且程序运行过程中可能会接收外部传入的对象该对象的编译时类型为 Object,但是程序有需要调用该对象的运行时类型的方法。为了解决这些问题，程序需要在运行时发现对象和类的真实信息。然而，如果编译时根本无法预知该对象和类属于哪些类，程序只能依靠运行时信息来发现该对象和类的真实信息，此时就必须使用到反射了。

# 使用
## API
反射API用来生成JVM中的类、接口或则对象的信息。  
### Class
反射的核心类，可以获取类的属性，方法等信息。

### Field
Java.lang.reflec 包中的类，表示类的成员变量，可以用来获取和设置类之中的属性值。
### Method  
Java.lang.reflec 包中的类，表示类的方法，它可以用来获取类中的方法信息或者执行方法。
### Constructor
Java.lang.reflec 包中的类，表示类的构造方法。
### Annotation


## 使用步骤（获取 Class 对象、调用对象方法）

1. 获取想要操作的类的 Class 对象，他是反射的核心，通过 Class 对象我们可以任意调用类的方法。可以通过以下方式获取：
   - 调用对象的getClass()：(new Man()).getClass()
   - 调用类的class属性：Man.class
   - 使用Class.forName调用类的全路径（**最安全，性能最好**）：Class.forName("com.sunld.Man")
2. 调用 Class 类中的方法，既就是反射的使用阶段。
3. 使用反射 API 来操作这些信息。
   ```java
   package com.sunld;
    public class ReflectionTest {
        public static void main(String[] args) {
            // 获取 Man 类的 Class 对象
            Class<?> clazz = Class.forName("com.sunld.Man");
            // 获取 Man 类的所有方法信息
            Method[] method = clazz.getDeclaredMethods();
            for (Method m : method) {
                System.out.println(m.toString());
            }
            // 获取 Man 类的所有成员属性信息
            Field[] field = clazz.getDeclaredFields();
            for (Field f : field) {
                System.out.println(f.toString());
            }
            // 获取 Man 类的所有构造方法信息
            Constructor[] constructor = clazz.getDeclaredConstructors();
            for (Constructor c : constructor) {
                System.out.println(c.toString());
            }
        }
    }
   ```
## 构建对象
### Class对象的newInstance() 
使用 Class 对象的 newInstance()方法来创建该 Class 对象对应类的实例，但是这种方法要求该 Class 对象对应的类有默认的空构造器。
```java
//获取 Man 类的 Class 对象
Class<?> clazz = Class.forName("com.sunld.Man");
//使用.newInstane 方法创建对象
Man p=(Man) clazz.newInstance();
```
### 调用Constructor 对象的newInstance() 
先使用 Class 对象获取指定的 Constructor 对象，再调用 Constructor 对象的 newInstance() 方法来创建 Class 对象对应类的实例,通过这种方法可以选定构造方法创建实例。
```java
Class<?> clazz=Class.forName("com.sunld.Man");  
//获取构造方法并创建对象
Constructor c=clazz.getDeclaredConstructor(String.class);
//创建对象并设置属性
Man m=(Man) c.newInstance("男人");
```
