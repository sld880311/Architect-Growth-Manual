<!-- TOC -->

- [eclipse使用技巧](#eclipse%e4%bd%bf%e7%94%a8%e6%8a%80%e5%b7%a7)
  - [集成javap命令](#%e9%9b%86%e6%88%90javap%e5%91%bd%e4%bb%a4)
    - [配置](#%e9%85%8d%e7%bd%ae)
    - [使用](#%e4%bd%bf%e7%94%a8)
    - [javap的命令说明](#javap%e7%9a%84%e5%91%bd%e4%bb%a4%e8%af%b4%e6%98%8e)

<!-- /TOC -->
# eclipse使用技巧

## 集成javap命令

### 配置

1. eclipse中点击工具栏: Run > External Tools > External Tools Configuration
2. 双击”程序”
3. 修改配置

<div align=center>

![1588039059895.png](..\images\1588039059895.png)

</div>

```java
Name: javap(随意)
location: jdk实际路径
Working Directory:${workspace_loc}\${project_name} （不要改）
Arguments:-c -verbose  -classpath  ${workspace_loc}/ ${project_name}/bin/${java_type_name}
```

### 使用

<div align=center>

![1588039225454.png](..\images\1588039225454.png)

</div>

### javap的命令说明

javap 命令用于解析类文件。其输出取决于所用的选项。若没有使用选项，javap 将输出传递给它的类的 public 域及方法。javap 将其输出到标准输出设备上。

```java
    语法：
　　javap [ 命令选项 ] class. . .
　　命令选项
　　-help 输出 javap 的帮助信息。
　　-l 输出行及局部变量表。
　　-b 确保与 JDK 1.1 javap 的向后兼容性。
　　-public 只显示 public 类及成员。
　　-protected 只显示 protected 和 public 类及成员。
　　-package 只显示包、protected 和 public 类及成员。这是缺省设置。
　　-private 显示所有类和成员。
　　-J[flag] 直接将 flag 传给运行时系统。
　　-s 输出内部类型签名。
　　-c 输出类中各方法的未解析的代码，即构成 Java 字节码的指令。
　　-verbose 输出堆栈大小、各方法的 locals 及 args 数。
　　-classpath[路径] 指定 javap 用来查找类的路径。如果设置了该选项，则它将覆盖缺省值或 CLASSPATH 环境变量。目录用冒号分隔。
　　- bootclasspath[路径] 指定加载自举类所用的路径。缺省情况下，自举类是实现核心 Java 平台的类，位于 jrelib
　t.jar 和 jrelibi18n.jar 中。
　　-extdirs[dirs] 覆盖搜索安装方式扩展的位置。扩展的缺省位置是 jrelibex
```
