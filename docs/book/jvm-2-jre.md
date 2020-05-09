<!-- TOC -->

- [两套JRE](#两套jre)
    - [JDK、JRE、JVM之间的关系](#jdkjrejvm之间的关系)
        - [使用两套JDK的原因](#使用两套jdk的原因)
        - [如何选择使用那套JRE](#如何选择使用那套jre)
        - [JVM](#jvm)
    - [名称解释](#名称解释)
    - [其他](#其他)
        - [jdk下bin目录里的java.exe与外部jre中的java.exe的秘密](#jdk下bin目录里的javaexe与外部jre中的javaexe的秘密)
    - [总结](#总结)

<!-- /TOC -->
# 两套JRE

JDK在安装过程中如果勾选安装JRE最终会在安装目录中有两套JRE。简单来说一套用于Java本身的运行（比如java本身的javac、还有监控套件javap等），一套用于程序的运行。两套JRE的路径默认为：

1. C:\Program Files\Java\jdk1.8.0_192\jre
2. C:\Program Files\Java\jre1.8.0_192

## JDK、JRE、JVM之间的关系

JDK本身运行环境中的jre比jre目录下多了Server端的Java虚拟机。  

JRE：Java编译后的可运行环境。
JVM：Java虚拟机

### 使用两套JDK的原因

JDK里面有很多用Java所编写的开发工具（如javac.exe、jar.exe等），而且都放置在 <JDK安装目录>\lib\tools.jar 里。`javac.exe与输入
java -cp c:\jdk\lib\tools.jar com.sun.tools.javac.Main`是一样的，会得到相同的结果。

### 如何选择使用那套JRE

由`java.exe`决定，执行顺序如下：

1. 自己的目录下有没有JRE；
2. 父目录有没有JRE；
3. 查询注册表：
[HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment]

### JVM

**JRE目录下的Bin目录有两个目录：server与client。这就是真正的jvm.dll所在。**
jvm.dll 无法单独工作，当jvm.dll启动后，会使用explicit的方法（就是使用Win32 API之中的LoadLibrary()与GetProcAddress()来载入辅助用的动态链接库），而这些辅助用的动态链接库（.dll）都必须位于jvm.dll所在目录的父目录之中。因此想使用哪个JVM，只需要设置PATH，指向JRE所在目录底下的jvm.dll。

## 名称解释

1. **JAVA IDE**:JAVA集成开发环境，如JBuilder Eclipse NetBeans,JEdit,TaxPad等等。
2. **JDK Java开发包**:java development kit ，它是使用JAVA进行开发的基础，包含：JVM（JAVA虚拟机）,JAVA 类库，JAVA运行环境。简单的说就是个开发平台：包含开发平台，和JAVA运行平台（即JRE或J2RE是Java2 Runtime Environment，即Java运行环境，有时简称JRE。如果你只需要运行Java程序或Applet，下载并安装它即可。）
3. **SDK（Software Development Kit）**一般指软件开发包，可以包括函数库、编译程序等。
4. **JRE（Java Runtime Enviroment）**是指Java的运行环境，是面向Java程序的使用者，而不是开发者。其内部含有JVM(JAVA虚拟机）

## 其他

### jdk下bin目录里的java.exe与外部jre中的java.exe的秘密

jdk里的java.exe和jre中的java.exe其实是一样的，但我们在运行的时候用的却是优先使用外部jre中的java.exe（即使我们安装了JDK且也配置了环境变量）。

## 总结

1. 安装JDK时一定会在其子目录下面安装一个JRE，同时在安装的过程也会询问你是否要安装一个外部的JRE。
2. 如果我们选择安装则就同时拥有了两个jre。这两个JRE其实本质上是没有任何分别的。最主要的区别在于：JDK目录里面的JRE主要是设计用来运行JDK自带的那些工具的（Bin目录下）。而外部的JRE在安装的时候会自动注册到操作系统的path（但在我的电脑\属性\高级系统设置\环境变量\系统变量中的 path中并没有包含该命令的路径值）里面一般是：System32文件夹下（该文件夹下包含：java.exe javaw.exe  javaws.exe 三个文件）面。
3. 因此我们只要安装了外部的JRE（即使JDK没有安装，环境变量没有配置）则运行Java程序时都是用的外部JRE的java.exe程序来运行的（即使安装了JDK且配置了环境变量）（系统的默认path具有优先）。
