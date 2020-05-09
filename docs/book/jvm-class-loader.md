<!-- TOC -->

- [Jvm类加载机制](#jvm类加载机制)
    - [类加载时机](#类加载时机)
    - [加载（Loading）](#加载loading)
        - [数组的加载](#数组的加载)
    - [验证](#验证)
        - [文件格式验证](#文件格式验证)
        - [元数据验证](#元数据验证)
        - [字节码验证](#字节码验证)
            - [StackMapTable](#stackmaptable)
        - [符号引用验证](#符号引用验证)
    - [准备](#准备)
        - [初始值的概念](#初始值的概念)
    - [解析](#解析)
        - [符号引用（Symbolic References）](#符号引用symbolic-references)
        - [直接引用（Direct References）](#直接引用direct-references)
        - [解析范围](#解析范围)
            - [类或接口的解析](#类或接口的解析)
            - [字段解析](#字段解析)
            - [方法解析](#方法解析)
            - [接口方法解析](#接口方法解析)
    - [初始化](#初始化)
    - [类构造器<client>](#类构造器client)
    - [类加载器](#类加载器)
    - [双亲委派原则](#双亲委派原则)
    - [OSGI（动态模型系统）](#osgi动态模型系统)
    - [参考](#参考)

<!-- /TOC -->
# Jvm类加载机制

Java虚拟机把描述类的数据从class文件加载到内存，并且对数据进行校验、转换解析和初始化，最终形成可以被虚拟机直接使用的Java类型，这个过程称为**Java虚拟机的类加载机制**，Java天生可以动态扩展的语言特性就是依赖运行期动态加载和动态连接这个特点实现的，JVM 类加载机制分为五个部分：加载，验证，准备，解析，初始化，这五部分是在程序**运行期间**完成；还有使用和卸载，共七个阶段，参考下图所示：

<div align=center>

![1588727499000.png](..\images\1588727499000.png)

</div>

## 类加载时机

1. new、getstatic、putstatic或invokestatic这四条字节码指令时，如果类型没有进行过初始化，则需要先触发其初始化阶段。具体场景：
   - 使用new关键字实例化对象的时候
   - 读取或设置一个类型的静态字段（被final修饰、已在编译期把结果放入常量池的静态字段除外）的时候。
   - 调用一个类型的静态方法的时候。
2. 使用java.lang.reflect包的方法对类型进行反射调用的时候，如果类型没有进行过初始化，则需要先触发其初始化。
3. 当初始化类的时候，如果发现其父类还没有进行过初始化，则需要先触发其父类的初始化。
4. 当虚拟机启动时，用户需要指定一个要执行的主类（包含main()方法的那个类），虚拟机会先初始化这个主类。
5. 当使用JDK 7新加入的动态语言支持时，如果一个java.lang.invoke.MethodHandle实例最后的解析结果为REF_getStatic、REF_putStatic、REF_invokeStatic、REF_newInvokeSpecial四种类型的方法句柄，并且这个方法句柄对应的类没有进行过初始化，则需要先触发其初始化。
6. 当一个接口中定义了JDK 8新加入的默认方法（被default关键字修饰的接口方法）时，如果有这个接口的实现类发生了初始化，那该接口要在其之前被初始化。

> 说明
> > 对于静态字段，只有直接定义这个字段的类才会被初始化
> > 接口与类真正有所区别的是前面讲述的六种“有且仅有”需要触发初始化场景中的第三种：当一个类在初始化时，要求其父类全部都已经初始化过了，但是一个接口在初始化时，并不要求其父接口全部都完成了初始化，只有在真正使用到父接口的时候（如引用接口中定义的常量）才会初始化。

## 加载（Loading）

类加载（Class Loading）的第一个阶段，在`加载`过程中主要完成以下操作：

1. 通过一个类的全限定名来获取定义此类的二进制字节流，比如`java.lang.String`，二进制字节流的形式可以是：
   -  ZIP中获取：jar、ear、war
   -  网络获取：Web Applet
   -  动态代理：**java.lang.reflect.Proxy中的ProxyGenerator.generateProxyClass()来为特定接口生成形式为“*$Proxy”的代理类的二进制字节流**
   -  JSP编译后的class文件
   -  加密后的class文件，然后使用时解密
2. 将这个字节流所代表的静态存储结构转化为方法区的运行时数据结构。
3. 在内存**堆**中生成一个代表这个类的`java.lang.Class`对象，作为方法区这个类的各种数据的访问入口。

### 数组的加载

1. 数组本身不通过类加载创建，由java虚拟机直接在内存中动态构建。
2. 数组类的元素类型（Element Type，指的是数组去掉所有维度的类型）最终还是要靠类加载器来完成加载

如果一个数组类（比如：C）的**组件类型**（Component Type，指的是数组去掉一个维度的类型）是：

1. **引用类型**，就会使用类加载器来递归完成组件类型的加载。**数组C将被标识在加载该组件类型的类加载器的类名称空间上**；
2. 非引用类型（例如int[]数组的组件类型为int），**Java虚拟机将会把数组C标记为与引导类加载器关联**。
3. 数组类的可访问性与它的组件类型的可访问性一致，如果组件类型不是引用类型，它的数组类的可访问性将默认为public，可被所有的类和接口访问到。

## 验证

验证是连接阶段的第一步，这一阶段的目的是**确保Class文件的字节流中包含的信息符合《Java虚拟机规范》的全部约束要求**，保证这些信息被当作代码运行后不会危害虚拟机自身的安全。主要完成以下四个阶段的校验的工作：**文件格式验证、元数据验证、字节码验证和符号引用验证**。

### 文件格式验证

基于二进制流，判断字节流是否符合Class文件格式的规范，并且能被当前版本的虚拟机处理，解析完成之后可以正确的保存到方法区中，包括但不限于：

1. 是否以魔数0xCAFEBABE开头
2. 主、次版本号
3. 常量池中非法常量类型检查（检查常量tag标志）
4. 指向常量的各种索引值中是否有指向不存在的常量或不符合类型的常量
5. CONSTANT_Utf8_info型的常量中是否有不符合UTF-8编码的数据。
6. Class文件中各个部分及文件本身是否有被删除的或附加的其他信息

### 元数据验证

元数据验证主要完成字节码描述信息的**语义分析（面向元数据信息中的数据类型）**，符合《Java语言规范》的要求，主要完成以下信息：

1. 这个类是否有父类（除了java.lang.Object之外，所有的类都应当有父类）。
2. 这个类的父类是否继承了不允许被继承的类（被final修饰的类）。
3. 如果这个类不是抽象类，是否实现了其父类或接口之中要求实现的所有方法。
4. 类中的字段、方法是否与父类产生矛盾（例如覆盖了父类的final字段，或者出现不符合规则的方法重载）

### 字节码验证

通过**数据流分析和控制流分析**，确定程序语义是合法的、符合逻辑的。面向**类的方法体（Class文件中的Code属性）**进行校验分析，具体如下：

1. 保证任意时刻操作数栈的数据类型与指令代码序列都能配合工作（入栈的类型与出栈后的使用类型一致）
2. 保证任何跳转指令都不会跳转到方法体以外的字节码指令上。
3. 保证方法体中的类型转换总是有效的（合法的类型转换）

#### StackMapTable

为了提高字节码验证阶段的时间，JDK6之后在javac编译期阶段的方法体code中增加了一个`StackMapTable`的新属性，这项属性描述了**方法体所有的基本块**（Basic Block，指按照控制流拆分的代码块）开始时本地变量表和操作栈应有的状态，在字节码验证期间，Java虚拟机就不需要根据程序推导这些状态的合法性，只需要检查StackMapTable属性中的记录是否合法即可。StackMapTable属性也存在错误或被篡改的可能，所以是否有可能在恶意篡改了Code属性的同时，也生成相应的StackMapTable属性来骗过虚拟机的类型校验。

> 扩展知识
> > 使用`-XX：-UseSplitVerifier`关闭该优化，或使用`-XX：+FailOverToOldVerifier`要求在类型校验失败的时候退回到旧的类型推导方式进行校验。
> > JDK 7之后，尽管虚拟机中仍然保留着类型推导验证器的代码，但是对于主版本号大于50（对应JDK 6）的Class文件，使用类型检查来完成数据流分析校验则是唯一的选择，不允许再退回到原来的类型推导的校验方式。

### 符号引用验证

发生在虚拟机将符号引用转化为直接引用时（**解析阶段**），**符号引用验证可以看作是对类自身以外（常量池中的各种符号引用）的各类信息进行匹配性校验，该类是否缺少或者被禁止访问它依赖的某些外部类、方法、字段等资源。**具体如下：

1. 符号引用中通过字符串描述的全限定名是否能找到对应的类。
2. 在指定类中是否存在符合方法的字段描述符及简单名称所描述的方法和字段。
3. 符号引用中的类、字段、方法的可访问性（private、protected、public、<package>）是否可被当前类访问。

符号引用验证的主要目的是确保解析行为能正常执行，如果无法通过符号引用验证，Java虚拟机将会抛出一个java.lang.IncompatibleClassChangeError的子类异常，典型的如：java.lang.IllegalAccessError、java.lang.NoSuchFieldError、java.lang.NoSuchMethodError等。

## 准备

该阶段是正式为**类变量（static修饰）分配内存并且完成初始化赋值的阶段（即在方法区分配内存空间）**。

> 关于方法区的特殊说明
> > JDK 7及之前，HotSpot使用永久代来实现方法区
> > JDK 8及之后，类变量则会随着Class对象一起存放在Java堆中

### 初始值的概念

该阶段主要完成“0”值的处理，具体如下：

`public static int v = 8080;`,则为0，将 v 赋值为 8080 的 put static 指令是程序被编译后，存放于static方法(`<clinit>()`)之中。`public static final int v = 8080;`在编译阶段会为 v 生成 ConstantValue 属性，在准备阶段虚拟机会根据 ConstantValue 属性将 v 赋值为 8080。

```java
public class TestStatic {

	public static int a = 10;
	
	public final static int b = 20;
}
```

```java
警告: 二进制文件Test/bin/com.sunld.TestStatic包含com.sunld.TestStatic
Classfile /D:/Workspaces/java/TestJVM/Test/bin/com/sunld/TestStatic.class
  Last modified 2020-5-9; size 409 bytes
  MD5 checksum 7f77187644282a651264bddec7487130
  Compiled from "TestStatic.java"
public class com.sunld.TestStatic
  minor version: 0
  major version: 52
  flags: ACC_PUBLIC, ACC_SUPER
Constant pool:
   #1 = Class              #2             // com/sunld/TestStatic
   #2 = Utf8               com/sunld/TestStatic
   #3 = Class              #4             // java/lang/Object
   #4 = Utf8               java/lang/Object
   #5 = Utf8               a
   #6 = Utf8               I
   #7 = Utf8               b
   #8 = Utf8               ConstantValue
   #9 = Integer            20
  #10 = Utf8               <clinit>
  #11 = Utf8               ()V
  #12 = Utf8               Code
  #13 = Fieldref           #1.#14         // com/sunld/TestStatic.a:I
  #14 = NameAndType        #5:#6          // a:I
  #15 = Utf8               LineNumberTable
  #16 = Utf8               LocalVariableTable
  #17 = Utf8               <init>
  #18 = Methodref          #3.#19         // java/lang/Object."<init>":()V
  #19 = NameAndType        #17:#11        // "<init>":()V
  #20 = Utf8               this
  #21 = Utf8               Lcom/sunld/TestStatic;
  #22 = Utf8               SourceFile
  #23 = Utf8               TestStatic.java
{
  public static int a;
    descriptor: I
    flags: ACC_PUBLIC, ACC_STATIC

  public static final int b;
    descriptor: I
    flags: ACC_PUBLIC, ACC_STATIC, ACC_FINAL
    ConstantValue: int 20

  static {};
    descriptor: ()V
    flags: ACC_STATIC
    Code:
      stack=1, locals=0, args_size=0
         0: bipush        10
         2: putstatic     #13                 // Field a:I
         5: return
      LineNumberTable:
        line 5: 0
        line 7: 5
      LocalVariableTable:
        Start  Length  Slot  Name   Signature

  public com.sunld.TestStatic();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #18                 // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 3: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   Lcom/sunld/TestStatic;
}
SourceFile: "TestStatic.java"
```

**零值参考表**：  

<div align=center>

![1589021066885.png](..\images\1589021066885.png)

</div>

## 解析

该阶段主要是完成Java虚拟机中常量池中符号引用替换为直接引用的过程。符号引用主要是指class文件中的：

1. CONSTANT_Class_info
2. CONSTANT_Field_info
3. CONSTANT_Method_info等

### 符号引用（Symbolic References）

1. 一组描述引用目标的符号，该符号可以是任意字面量（符合《Java虚拟机规范》）
2. 与虚拟机实现的内存布局无关，引用的目标并不一定是已经加载到虚拟机内存当中的内容

### 直接引用（Direct References）

1. 直接指向目标的指针、相对偏移量或者是一个能间接定位到目标的句柄。
2. 直接引用是和虚拟机实现的内存布局直接相关的，同一个符号引用在不同虚拟机实例上翻译出来的直接引用一般不会相同。如果有了直接引用，那引用的目标必定已经在虚拟机的内存中存在。

### 解析范围

主要针对类或接口、字段、类方法、接口方法、方法类型、方法句柄和调用点限定符这7类符号引用进行，分别对应于常量池的**CONSTANT_Class_info、CON-STANT_Fieldref_info、CONSTANT_Methodref_info、CONSTANT_InterfaceMethodref_info、CONSTANT_MethodType_info、CONSTANT_MethodHandle_info、CONSTANT_Dyna-mic_info和CONSTANT_InvokeDynamic_info** 8种常量类型。

#### 类或接口的解析

举例：

```java
public class D{
    private C N;
}
```

JVM的解析过程：

1. C `!=`数组类型，D类会使用N的全限定名去加载这个类C。在加载过程中，由于元数据验证、字节码验证的需要，又可能触发其他相关类的加载动作，例如加载这个类的父类或实现的接口。一旦这个加载过程出现了任何异常，解析过程就将宣告失败。
2. C`=`数组类型，并且数组的元素类型为对象（如：`[Ljava/lang/Integer`），那将会按照第一点的规则加载数组元素类型。如果N的描述符如前面所假设的形式，需要加载的元素类型就是`java.lang.Integer`，**接着由虚拟机生成一个代表该数组维度和元素的数组对象**。
3. 以上通过之后，C在虚拟机中实际上已经成为一个有效的类或接口了，但在解析完成前还要进行符号引用验证，确认D是否具备对C的**访问权限**。如果发现不具备访问权限，将抛出java.lang.IllegalAccessError异常。如果我们说一个D拥有C的访问权限，那就意味着以下3条规则中至少有其中一条成立：
   - 被访问类C是public的，并且与访问类D处于同一个模块。
   - 被访问类C是public的，不与访问类D处于同一个模块，但是被访问类C的模块允许被访问类D的模块进行访问。
   - 被访问类C不是public的，但是它与访问类D处于同一个包中。

#### 字段解析

#### 方法解析

#### 接口方法解析

## 初始化

## 类构造器<client>

## 类加载器

## 双亲委派原则

## OSGI（动态模型系统）

## 参考

1. 《深入理解Java虚拟机》