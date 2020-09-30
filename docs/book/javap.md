<!-- TOC -->

- [javap详解](#javap详解)
  - [命令](#命令)
  - [分析代码](#分析代码)
  - [javap分析](#javap分析)
  - [参考](#参考)

<!-- /TOC -->
# javap详解

## 命令

javap 命令用于解析类文件。其输出取决于所用的选项。若没有使用选项，javap 将输出传递给它的类的 public 域及方法。javap 将其输出到标准输出设备上。

```java
用法: javap <options> <classes>
其中, 可能的选项包括:
  -help  --help  -?        输出此用法消息
  -version                 版本信息
  -v  -verbose             输出附加信息,输出堆栈大小、各方法的 locals 及 args 数。
  -l                       输出行号和本地变量表
  -public                  仅显示公共类和成员
  -protected               显示受保护的/公共类和成员
  -package                 显示程序包/受保护的/公共类和成员 (默认)
  -p  -private             显示所有类和成员
  -c                       输出类中各方法的未解析的代码，即构成 Java 字节码的指令。
  -s                       输出内部类型签名
  -sysinfo                 显示正在处理的类的
                           系统信息 (路径, 大小, 日期, MD5 散列)
  -constants               显示最终常量
  -classpath <path>        指定查找用户类文件的位置,如果设置了该选项，则它将覆盖缺省值或 CLASSPATH 环境变量。目录用冒号分隔。
  -cp <path>               指定查找用户类文件的位置
  -bootclasspath <path>    覆盖引导类文件的位置,缺省情况下，自举类是实现核心 Java 平台的类，位于 jrelibt.jar 和 jrelibi18n.jar 中。
```

## 分析代码

```java
package com.sunld;

public class TestPassByValue {
	
	private int temp1 = 10;
	
	public void doPassInt() {
		int temp = 100;
		this.passInt(temp);
	}
	
	public void passInt(int abc) { }
	
	public void doPassObject() {
		String temp = "abcddd";
		PassByValue val = new PassByValue();
		this.passObject(val, temp);
	}
	
	public void passObject(PassByValue passByValue, String value) { 
		passByValue = null;
		value = "value";
	}
}
class PassByValue{}

```

## javap分析

```java
警告: 二进制文件Test/bin/com.sunld.TestPassByValue包含com.sunld.TestPassByValue
Classfile /D:/Workspaces/java/TestJVM/Test/bin/com/sunld/TestPassByValue.class
  Last modified 2020-4-29; size 939 bytes
  MD5 checksum ae6cce5733ce01797de9d8ae30359bdd
  Compiled from "TestPassByValue.java"
public class com.sunld.TestPassByValue
  minor version: 0
  major version: 52
  flags: ACC_PUBLIC, ACC_SUPER
Constant pool:
   #1 = Class              #2             // com/sunld/TestPassByValue
   #2 = Utf8               com/sunld/TestPassByValue
   #3 = Class              #4             // java/lang/Object
   #4 = Utf8               java/lang/Object
   #5 = Utf8               temp1
   #6 = Utf8               I
   #7 = Utf8               <init>
   #8 = Utf8               ()V
   #9 = Utf8               Code
  #10 = Methodref          #3.#11         // java/lang/Object."<init>":()V
  #11 = NameAndType        #7:#8          // "<init>":()V
  #12 = Fieldref           #1.#13         // com/sunld/TestPassByValue.temp1:I
  #13 = NameAndType        #5:#6          // temp1:I
  #14 = Utf8               LineNumberTable
  #15 = Utf8               LocalVariableTable
  #16 = Utf8               this
  #17 = Utf8               Lcom/sunld/TestPassByValue;
  #18 = Utf8               doPassInt
  #19 = Methodref          #1.#20         // com/sunld/TestPassByValue.passInt:(I)V
  #20 = NameAndType        #21:#22        // passInt:(I)V
  #21 = Utf8               passInt
  #22 = Utf8               (I)V
  #23 = Utf8               temp
  #24 = Utf8               abc
  #25 = Utf8               doPassObject
  #26 = String             #27            // abcddd
  #27 = Utf8               abcddd
  #28 = Class              #29            // com/sunld/PassByValue
  #29 = Utf8               com/sunld/PassByValue
  #30 = Methodref          #28.#11        // com/sunld/PassByValue."<init>":()V
  #31 = Methodref          #1.#32         // com/sunld/TestPassByValue.passObject:(Lcom/sunld/PassByValue;Ljava/lang/String;)V
  #32 = NameAndType        #33:#34        // passObject:(Lcom/sunld/PassByValue;Ljava/lang/String;)V
  #33 = Utf8               passObject
  #34 = Utf8               (Lcom/sunld/PassByValue;Ljava/lang/String;)V
  #35 = Utf8               Ljava/lang/String;
  #36 = Utf8               val
  #37 = Utf8               Lcom/sunld/PassByValue;
  #38 = String             #39            // value
  #39 = Utf8               value
  #40 = Utf8               passByValue
  #41 = Utf8               SourceFile
  #42 = Utf8               TestPassByValue.java
{
  //默认的构造方法，在构造方法执行时主要完成一些初始化操作，包括一些成员变量的初始化赋值等操作
  public com.sunld.TestPassByValue();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=2, locals=1, args_size=1
         //*load_*：将本地变量表中索引为*的局部变量加载到操作栈
         //从本地变量表中加载索引为0的变量的值到操作栈，也即this的引用，压入栈
         0: aload_0
         //invokespecial：用于调用对象的实例方法，根据对象的实际类型进行分派（虚方法分派）
         //出栈，调用java/lang/Object."<init>":()V 初始化对象，就是this指定的对象的init()方法完成初始化
         1: invokespecial #10                 // Method java/lang/Object."<init>":()V
         // 4到7表示，完成this.temp1 = 10的赋值。这里this引用入栈
         4: aload_0
         //将常量10加入到操作数栈
         5: bipush        10
         //出栈前面压入的两个值（this引用，常量值10）， 将10取出，并赋值给temp1
         7: putfield      #12                 // Field temp1:I
        10: return
      //指令与代码行数的偏移对应关系，每一行第一个数字对应代码行数，第二个数字对应前面code中指令前面的数字
      LineNumberTable:
        line 3: 0
        line 5: 4
        line 3: 10
      //局部变量表，start+length表示这个变量在字节码中的生命周期起始和结束的偏移位置（this生命周期从头0到结尾10）
      //slot就是这个变量在局部变量表中的槽位（槽位可复用），name就是变量名称，Signatur局部变量类型描述
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      11     0  this   Lcom/sunld/TestPassByValue;

  public void doPassInt();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=2, locals=2, args_size=1
         // 常量100入栈
         0: bipush        100
         // 将一个数值从操作数栈存储到局部变量表,存储100给temp
         2: istore_1
         // 加载this入栈
         3: aload_0
         // temp入栈
         4: iload_1
         5: invokevirtual #19                 // Method passInt:(I)V
         8: return
      LineNumberTable:
        line 8: 0
        line 9: 3
        line 10: 8
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       9     0  this   Lcom/sunld/TestPassByValue;
            3       6     1  temp   I

  public void passInt(int);
    descriptor: (I)V
    flags: ACC_PUBLIC
    Code:
      stack=0, locals=2, args_size=2
         0: return
      LineNumberTable:
        line 12: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       1     0  this   Lcom/sunld/TestPassByValue;
            0       1     1   abc   I

  public void doPassObject();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=3, locals=3, args_size=1
         // 从常量池取出abcddd入栈
         0: ldc           #26                 // String abcddd
         // 出栈赋值给temp
         2: astore_1
         // 创建PassByValue对象，将引用压入栈
         3: new           #28                 // class com/sunld/PassByValue
         // 将操作数栈顶的数据复制一份，并压入栈，此时栈中有两个引用值（目前有两个PassByValue的引用）
         6: dup
         7: invokespecial #30                 // Method com/sunld/PassByValue."<init>":()V
         // 出栈栈顶数据（对象PassByValue）复制给val
        10: astore_2
        // 入栈this
        11: aload_0
        // 入栈val
        12: aload_2
        // 入栈temp
        13: aload_1
        // 执行方法
        14: invokevirtual #31                 // Method passObject:(Lcom/sunld/PassByValue;Ljava/lang/String;)V
        17: return
      LineNumberTable:
        line 15: 0
        line 16: 3
        line 17: 11
        line 18: 17
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      18     0  this   Lcom/sunld/TestPassByValue;
            3      15     1  temp   Ljava/lang/String;
           11       7     2   val   Lcom/sunld/PassByValue;

  public void passObject(com.sunld.PassByValue, java.lang.String);
    descriptor: (Lcom/sunld/PassByValue;Ljava/lang/String;)V
    flags: ACC_PUBLIC
    Code:
      stack=1, locals=3, args_size=3
         0: aconst_null
         1: astore_1
         2: ldc           #38                 // String value
         4: astore_2
         5: return
      LineNumberTable:
        line 21: 0
        line 22: 2
        line 23: 5
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       6     0  this   Lcom/sunld/TestPassByValue;
            0       6     1 passByValue   Lcom/sunld/PassByValue;
            0       6     2 value   Ljava/lang/String;
}
SourceFile: "TestPassByValue.java"
```

## 参考

1. [通过javap命令分析java汇编指令](https://www.jianshu.com/p/6a8997560b05)
