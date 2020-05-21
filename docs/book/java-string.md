<!-- TOC -->

- [Java字符串深入分析](#java%e5%ad%97%e7%ac%a6%e4%b8%b2%e6%b7%b1%e5%85%a5%e5%88%86%e6%9e%90)
  - [String介绍](#string%e4%bb%8b%e7%bb%8d)
    - [API描述](#api%e6%8f%8f%e8%bf%b0)
  - [如何创建字符串](#%e5%a6%82%e4%bd%95%e5%88%9b%e5%bb%ba%e5%ad%97%e7%ac%a6%e4%b8%b2)
    - [直接赋值](#%e7%9b%b4%e6%8e%a5%e8%b5%8b%e5%80%bc)
    - [使用new](#%e4%bd%bf%e7%94%a8new)
    - [字符串拼接](#%e5%ad%97%e7%ac%a6%e4%b8%b2%e6%8b%bc%e6%8e%a5)
    - [分析](#%e5%88%86%e6%9e%90)
      - [参考代码](#%e5%8f%82%e8%80%83%e4%bb%a3%e7%a0%81)
      - [内存分配](#%e5%86%85%e5%ad%98%e5%88%86%e9%85%8d)
  - [源码分析](#%e6%ba%90%e7%a0%81%e5%88%86%e6%9e%90)
    - [成员变量](#%e6%88%90%e5%91%98%e5%8f%98%e9%87%8f)
      - [字符串长度](#%e5%ad%97%e7%ac%a6%e4%b8%b2%e9%95%bf%e5%ba%a6)
    - [构造函数](#%e6%9e%84%e9%80%a0%e5%87%bd%e6%95%b0)
    - [判断功能](#%e5%88%a4%e6%96%ad%e5%8a%9f%e8%83%bd)
      - [equals](#equals)
      - [equalsIgnoreCase](#equalsignorecase)
      - [startsWith](#startswith)
      - [endsWith](#endswith)
    - [获取功能](#%e8%8e%b7%e5%8f%96%e5%8a%9f%e8%83%bd)
      - [length](#length)
      - [charAt](#charat)
      - [indexOf](#indexof)
      - [substring(int start,int end)](#substringint-startint-end)
    - [转换功能](#%e8%bd%ac%e6%8d%a2%e5%8a%9f%e8%83%bd)
      - [toCharArray](#tochararray)
      - [toLowerCase](#tolowercase)
      - [toUpperCase](#touppercase)
    - [其他常用](#%e5%85%b6%e4%bb%96%e5%b8%b8%e7%94%a8)
      - [trim](#trim)
      - [split](#split)
      - [compareTo](#compareto)
  - [String的不可变性](#string%e7%9a%84%e4%b8%8d%e5%8f%af%e5%8f%98%e6%80%a7)
    - [原因](#%e5%8e%9f%e5%9b%a0)
    - [优点](#%e4%bc%98%e7%82%b9)
  - [字符串常量池](#%e5%ad%97%e7%ac%a6%e4%b8%b2%e5%b8%b8%e9%87%8f%e6%b1%a0)
    - [字符串常量池概述](#%e5%ad%97%e7%ac%a6%e4%b8%b2%e5%b8%b8%e9%87%8f%e6%b1%a0%e6%a6%82%e8%bf%b0)
      - [常量池表（Constant_Pool table）](#%e5%b8%b8%e9%87%8f%e6%b1%a0%e8%a1%a8constantpool-table)
      - [运行时常量池（Runtime Constant Pool）](#%e8%bf%90%e8%a1%8c%e6%97%b6%e5%b8%b8%e9%87%8f%e6%b1%a0runtime-constant-pool)
      - [字符串常量池（String Pool）](#%e5%ad%97%e7%ac%a6%e4%b8%b2%e5%b8%b8%e9%87%8f%e6%b1%a0string-pool)
    - [亨元模式](#%e4%ba%a8%e5%85%83%e6%a8%a1%e5%bc%8f)
    - [详细分析](#%e8%af%a6%e7%bb%86%e5%88%86%e6%9e%90)
  - [其他](#%e5%85%b6%e4%bb%96)
    - [String与StringBuffer、StringBuilder](#string%e4%b8%8estringbufferstringbuilder)
      - [String循环拼接对象](#string%e5%be%aa%e7%8e%af%e6%8b%bc%e6%8e%a5%e5%af%b9%e8%b1%a1)
      - [使用StringBuilder](#%e4%bd%bf%e7%94%a8stringbuilder)
    - [==与equals](#%e4%b8%8eequals)
    - [编码问题](#%e7%bc%96%e7%a0%81%e9%97%ae%e9%a2%98)
      - [示例](#%e7%a4%ba%e4%be%8b)
      - [说明](#%e8%af%b4%e6%98%8e)
      - [替代方案：codePointCount](#%e6%9b%bf%e4%bb%a3%e6%96%b9%e6%a1%88codepointcount)
  - [参考](#%e5%8f%82%e8%80%83)

<!-- /TOC -->
# Java字符串深入分析

## String介绍

### API描述

> 在`java.lang.String`的源码中明确说明，`String`类表示字符串，Java中所有的字符串传字面值（如："abc"）都是该类的实例；并且字符串是常量，一旦创建之后则不可以改变，String buffers支持可变的字符串。因为`String`对象是不可变的，但是可以共享。

```java
 * The {@code String} class represents character strings. All
 * string literals in Java programs, such as {@code "abc"}, are
 * implemented as instances of this class.
 * <p>
 * Strings are constant; their values cannot be changed after they
 * are created. String buffers support mutable strings.
 * Because String objects are immutable they can be shared.

public final class String
    implements java.io.Serializable,
    Comparable<String>, CharSequence {
 }
```

通过API定义总结以下信息：

1. 使用final修饰，则不能被继承（该类中的成员方法默认都是final）
2. 实现接口`Serializable`，说明可以进行序列化
3. 实现接口`Comparable`,说明可以大小比较
4. 实现接口`CharSequence`,说明String本身就是char类型的数组，而且通过成员变量定义可以进行佐证
5. 通过char字符数组实现

## 如何创建字符串

### 直接赋值

直接赋值方式创建对象是在<font color="red">方法区的常量池。</font>  
`String str = "abc";`

### 使用new

通过构造方法创建字符串对象是在<font color="red">堆内存.  </font>
`String str = new String("abc");`  

### 字符串拼接

`String str = "abc" + "bdc";`

### 分析

#### 参考代码

```java
package com.sunld.string;

public class StringTest {

	public static void main(String[] args) {
		String str1 = "sunld";
        String str2 = new String("sunld");
        //引用传递，str3直接指向st2的堆内存地址
        String str3 = str2; 
        String str4 = "sunld";
        /**
         *  ==:
         * 1. 基本数据类型：比较的是基本数据类型的值是否相同
         * 2. 引用数据类型：比较的是引用数据类型的地址值是否相同
         * 3. 所以在这里的话：String类对象==比较，比较的是地址，而不是内容
         */
         System.out.println(str1==str2);//false
         System.out.println(str1==str3);//false
         System.out.println(str3==str2);//true
         System.out.println(str1==str4);//true
	}

}

```

#### 内存分配

<div align=center>

![1587630935534.png](..\images\1587630935534.png)

</div>

> 特殊说明
>> 1. 在使用new创建对象时，首先会在堆中的对象区域创建一个区域分配空间和完成初始化  
>> 2. 在使用字符串时，首先会判断字符串常量池中是否存在，否则则创建，然后堆对象会指向该字符串  
>> 3. 直接赋值：只开辟一块堆内存空间，并且会自动入池，不会产生垃圾。  
>> 4. 构造方法:会开辟两块堆内存空间，其中一块堆内存会变成垃圾被系统回收，而且不能够自动入池，需要通过public  String intern();方法进行手工入池。  
>> 5. String类对象一旦声明则不可以改变；而改变的只是地址，原来的字符串还是存在的，并且产生垃圾  
>> 6. 当使用字符串拼接生成字符串时，如果在编译器就可以确定字符串，则使用==判断时则返回true  

```java
public class TestString {
    public static void main(String args[]){
    //对匿名对象"hello"进行手工入池操作
     String str =new String("sunld").intern();
     String str1="sunld";
     System.out.println(str==str1);//true
    }
}
```

## 源码分析

### 成员变量

```java
/**
* String 使用final 修饰的字符数组进行存储，
* 并且字符串的长度和是否为空都是通过该数组的长度判断
*/
private final char value[];

/** Cache the hash code for the string */
private int hash; // Default to 0

/** use serialVersionUID from JDK 1.0.2 for interoperability */
private static final long serialVersionUID = -6849794470754667710L;

/**
* Class String is special cased within the Serialization Stream Protocol.
*
* A String instance is written into an ObjectOutputStream according to
* <a href="{@docRoot}/../platform/serialization/spec/output.html">
* Object Serialization Specification, Section 6.2, "Stream Elements"</a>
*/
private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[0];
```

#### 字符串长度

String内部是以char数组的形式存储，数组的长度是int类型，那么String允许的最大长度就是Integer.MAX_VALUE了。又由于java中的字符是以16位存储的，因此大概需要4GB的内存才能存储最大长度的字符串。不过这仅仅是对字符串变量而言，如果是字符串字面量(string literals)，如“abc"、"1a2b"之类写在代码中的字符串literals，那么允许的最大长度取决于字符串在常量池中的存储大小，也就是字符串在class格式文件中的存储格式：

``` java
CONSTANT_Utf8_info {
        u1 tag;
        u2 length;
        u1 bytes[length];
}
```

u2是无符号的16位整数，因此理论上允许的string literal的最大长度是2^16-1=65535。然而实际测试表明，允许的最大长度仅为65534，超过就编译错误了，有兴趣可以写段代码试试，估计是length还不能为0。

### 构造函数

<style type="text/css">
.tg  {border-collapse:collapse;border-spacing:0;border-color:#bbb;}
.tg td{font-family:Arial, sans-serif;font-size:14px;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#bbb;color:#594F4F;background-color:#E0FFEB;}
.tg th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#bbb;color:#493F3F;background-color:#9DE0AD;}
.tg .tg-0lax{text-align:left;vertical-align:top}
</style>
<table class="tg">
  <tr>
    <th class="tg-0lax">Constructor</th>
    <th class="tg-0lax"><span style="font-weight:400;font-style:normal">Description</span><br></th>
  </tr>
  <tr>
    <td class="tg-0lax">String()</td>
    <td class="tg-0lax">初始化新创建的 String对象，使其表示空字符序列。</td>
  </tr>
  <tr>
    <td class="tg-0lax">String(byte[] bytes)</td>
    <td class="tg-0lax">通过使用平台的默认字符集解码指定的字节数组来构造新的 String 。</td>
  </tr>
  <tr>
    <td class="tg-0lax">String(byte[] bytes, Charset charset)</td>
    <td class="tg-0lax">构造一个新的String由指定用指定的字节的数组解码charset 。</td>
  </tr>
  <tr>
    <td class="tg-0lax">String(byte[] bytes, int offset, int length)</td>
    <td class="tg-0lax">通过使用平台的默认字符集解码指定的字节子阵列来构造新的 String 。</td>
  </tr>
  <tr>
    <td class="tg-0lax">String(byte[] bytes, int offset, int length, Charset charset)</td>
    <td class="tg-0lax">构造一个新的String通过使用指定的指定字节子阵列解码charset 。</td>
  </tr>
  <tr>
    <td class="tg-0lax">String(byte[] bytes, int offset, int length, String charsetName)</td>
    <td class="tg-0lax">构造一个新的 String通过使用指定的字符集解码指定的字节子阵列。</td>
  </tr>
  <tr>
    <td class="tg-0lax">String(byte[] bytes, String charsetName)</td>
    <td class="tg-0lax">构造一个新的String由指定用指定的字节的数组解码charset 。</td>
  </tr>
  <tr>
    <td class="tg-0lax">String(char[] value)</td>
    <td class="tg-0lax">分配一个新的 String ，以便它表示当前包含在字符数组参数中的字符序列。</td>
  </tr>
  <tr>
    <td class="tg-0lax">String(char[] value, int offset, int count)</td>
    <td class="tg-0lax">分配一个新的 String ，其中包含字符数组参数的子阵列中的字符。</td>
  </tr>
  <tr>
    <td class="tg-0lax">String(int[] codePoints, int offset, int count)</td>
    <td class="tg-0lax">分配一个新的 String ，其中包含 Unicode code point数组参数的子阵列中的 字符 。</td>
  </tr>
  <tr>
    <td class="tg-0lax">String(String original)</td>
    <td class="tg-0lax">初始化新创建的String对象，使其表示与参数相同的字符序列<br>
    换句话说，新创建的字符串是参数字符串的副本。
    </td>
  </tr>
  <tr>
    <td class="tg-0lax">String(StringBuffer buffer)</td>
    <td class="tg-0lax">分配一个新的字符串，其中包含当前包含在字符串缓冲区参数中的字符序列。</td>
  </tr>
  <tr>
    <td class="tg-0lax">String(StringBuilder builder)</td>
    <td class="tg-0lax">分配一个新的字符串，其中包含当前包含在字符串构建器参数中的字符序列。</td>
  </tr>
</table>

### 判断功能

#### equals

比较字符串内容是否相同。重写Object类中的`equals`方法。

```java
/**
    * Compares this string to the specified object.  The result is {@code
    * true} if and only if the argument is not {@code null} and is a {@code
    * String} object that represents the same sequence of characters as this
    * object.
    *
    * @param  anObject
    *         The object to compare this {@code String} against
    *
    * @return  {@code true} if the given object represents a {@code String}
    *          equivalent to this string, {@code false} otherwise
    *
    * @see  #compareTo(String)
    * @see  #equalsIgnoreCase(String)
    * 判断对象存储内容是否相同
    */
public boolean equals(Object anObject) {
    if (this == anObject) {// 首先判断是否属于同一对象
        return true;
    }
    if (anObject instanceof String) {
        // 类型String----长度---数组中的字符
        String anotherString = (String)anObject;
        int n = value.length;
        if (n == anotherString.value.length) {
            char v1[] = value;
            char v2[] = anotherString.value;
            int i = 0;
            while (n-- != 0) {// 循环判断不等
                if (v1[i] != v2[i])
                    return false;
                i++;
            }
            return true;
        }
    }
    return false;
}
```

#### equalsIgnoreCase

比较字符串的内容是否相同,忽略大小写。

```java
/**
    * Compares this {@code String} to another {@code String}, ignoring case
    * considerations.  Two strings are considered equal ignoring case if they
    * are of the same length and corresponding characters in the two strings
    * are equal ignoring case.
    *
    * <p> Two characters {@code c1} and {@code c2} are considered the same
    * ignoring case if at least one of the following is true:
    * <ul>
    *   <li> The two characters are the same (as compared by the
    *        {@code ==} operator)
    *   <li> Applying the method {@link
    *        java.lang.Character#toUpperCase(char)} to each character
    *        produces the same result
    *   <li> Applying the method {@link
    *        java.lang.Character#toLowerCase(char)} to each character
    *        produces the same result
    * </ul>
    *
    * @param  anotherString
    *         The {@code String} to compare this {@code String} against
    *
    * @return  {@code true} if the argument is not {@code null} and it
    *          represents an equivalent {@code String} ignoring case; {@code
    *          false} otherwise
    *
    * @see  #equals(Object)
    */
public boolean equalsIgnoreCase(String anotherString) {
    return (this == anotherString) ? true
            : (anotherString != null)
            && (anotherString.value.length == value.length)
            && regionMatches(true, 0, anotherString, 0, value.length);
}
```

```java
/**
    * Tests if two string regions are equal.
    * <p>
    * A substring of this {@code String} object is compared to a substring
    * of the argument {@code other}. The result is {@code true} if these
    * substrings represent character sequences that are the same, ignoring
    * case if and only if {@code ignoreCase} is true. The substring of
    * this {@code String} object to be compared begins at index
    * {@code toffset} and has length {@code len}. The substring of
    * {@code other} to be compared begins at index {@code ooffset} and
    * has length {@code len}. The result is {@code false} if and only if
    * at least one of the following is true:
    * <ul><li>{@code toffset} is negative.
    * <li>{@code ooffset} is negative.
    * <li>{@code toffset+len} is greater than the length of this
    * {@code String} object.
    * <li>{@code ooffset+len} is greater than the length of the other
    * argument.
    * <li>{@code ignoreCase} is {@code false} and there is some nonnegative
    * integer <i>k</i> less than {@code len} such that:
    * <blockquote><pre>
    * this.charAt(toffset+k) != other.charAt(ooffset+k)
    * </pre></blockquote>
    * <li>{@code ignoreCase} is {@code true} and there is some nonnegative
    * integer <i>k</i> less than {@code len} such that:
    * <blockquote><pre>
    * Character.toLowerCase(this.charAt(toffset+k)) !=
    Character.toLowerCase(other.charAt(ooffset+k))
    * </pre></blockquote>
    * and:
    * <blockquote><pre>
    * Character.toUpperCase(this.charAt(toffset+k)) !=
    *         Character.toUpperCase(other.charAt(ooffset+k))
    * </pre></blockquote>
    * </ul>
    *
    * @param   ignoreCase   if {@code true}, ignore case when comparing
    *                       characters.
    * @param   toffset      the starting offset of the subregion in this
    *                       string.
    * @param   other        the string argument.
    * @param   ooffset      the starting offset of the subregion in the string
    *                       argument.
    * @param   len          the number of characters to compare.
    * @return  {@code true} if the specified subregion of this string
    *          matches the specified subregion of the string argument;
    *          {@code false} otherwise. Whether the matching is exact
    *          or case insensitive depends on the {@code ignoreCase}
    *          argument.
    */
public boolean regionMatches(boolean ignoreCase, int toffset,
        String other, int ooffset, int len) {
    char ta[] = value;
    int to = toffset;
    char pa[] = other.value;
    int po = ooffset;
    // Note: toffset, ooffset, or len might be near -1>>>1.
    if ((ooffset < 0) || (toffset < 0)
            || (toffset > (long)value.length - len)
            || (ooffset > (long)other.value.length - len)) {
        return false;
    }
    while (len-- > 0) {
        char c1 = ta[to++];
        char c2 = pa[po++];
        if (c1 == c2) {
            continue;
        }
        if (ignoreCase) {
            // If characters don't match but case may be ignored,
            // try converting both characters to uppercase.
            // If the results match, then the comparison scan should
            // continue.
            char u1 = Character.toUpperCase(c1);
            char u2 = Character.toUpperCase(c2);
            if (u1 == u2) {
                continue;
            }
            // Unfortunately, conversion to uppercase does not work properly
            // for the Georgian alphabet, which has strange rules about case
            // conversion.  So we need to make one last check before
            // exiting.
            if (Character.toLowerCase(u1) == Character.toLowerCase(u2)) {
                continue;
            }
        }
        return false;
    }
    return true;
}
```

#### startsWith

判断字符串对象是否以指定的str开头。源码中表现为按照长度截断之后进行循环比对

```java
/**
    * Tests if the substring of this string beginning at the
    * specified index starts with the specified prefix.
    *
    * @param   prefix    the prefix.
    * @param   toffset   where to begin looking in this string.
    * @return  {@code true} if the character sequence represented by the
    *          argument is a prefix of the substring of this object starting
    *          at index {@code toffset}; {@code false} otherwise.
    *          The result is {@code false} if {@code toffset} is
    *          negative or greater than the length of this
    *          {@code String} object; otherwise the result is the same
    *          as the result of the expression
    *          <pre>
    *          this.substring(toffset).startsWith(prefix)
    *          </pre>
    */
public boolean startsWith(String prefix, int toffset) {
    char ta[] = value;
    int to = toffset;
    char pa[] = prefix.value;
    int po = 0;
    int pc = prefix.value.length;
    // Note: toffset might be near -1>>>1.
    if ((toffset < 0) || (toffset > value.length - pc)) {
        return false;
    }
    while (--pc >= 0) {
        if (ta[to++] != pa[po++]) {
            return false;
        }
    }
    return true;
}
```

#### endsWith

判断字符串对象是否以指定的str结尾,源码中转换成`startsWith`处理。

```java
/**
    * Tests if this string ends with the specified suffix.
    *
    * @param   suffix   the suffix.
    * @return  {@code true} if the character sequence represented by the
    *          argument is a suffix of the character sequence represented by
    *          this object; {@code false} otherwise. Note that the
    *          result will be {@code true} if the argument is the
    *          empty string or is equal to this {@code String} object
    *          as determined by the {@link #equals(Object)} method.
    */
public boolean endsWith(String suffix) {
    return startsWith(suffix, value.length - suffix.value.length);
}
```

### 获取功能

#### length

获取字符串的长度，其实也就是字符个数

```java
/**
    * Returns the length of this string.
    * The length is equal to the number of <a href="Character.html#unicode">Unicode
    * code units</a> in the string.
    * 返回字符串的长度，长度等于字符串中的Unicode代码单元的数目（UTF-16的代码单元的数目）
    *
    * @return  the length of the sequence of characters represented by this
    *          object.
    */
public int length() {
    return value.length;
}
```

#### charAt

获取指定索引处的字符

#### indexOf

获取str在字符串对象中第一次出现的索引

#### substring(int start,int end)

从start开始，到end结束截取字符串。包括start，不包括end

### 转换功能

#### toCharArray

把字符串转换为字符数组

```java
/**
    * Converts this string to a new character array.
    *
    * @return  a newly allocated character array whose length is the length
    *          of this string and whose contents are initialized to contain
    *          the character sequence represented by this string.
    */
public char[] toCharArray() {
    // Cannot use Arrays.copyOf because of class initialization order issues
    /**
        * 1. String 和Arrays 都属于rt.jar中的类，但是BootstrapClassloader在加载这两个类的顺序是不同的。
        * 2. 所以当String.class被加载进内存的时候,Arrays此时没有被加载，所以直接使用肯定会抛异常。
        * 3. 而System.arrayCopy是使用native代码，则不会有这个问题。
        */
    char result[] = new char[value.length];
    System.arraycopy(value, 0, result, 0, value.length);
    return result;
}
```

#### toLowerCase

把字符串转换为小写字符串

#### toUpperCase

把字符串转换为大写字符串

### 其他常用

#### trim

去除字符串两端空格

```java
/**
    * Returns a string whose value is this string, with any leading and trailing
    * whitespace removed.
    * <p>
    * If this {@code String} object represents an empty character
    * sequence, or the first and last characters of character sequence
    * represented by this {@code String} object both have codes
    * greater than {@code '\u005Cu0020'} (the space character), then a
    * reference to this {@code String} object is returned.
    * <p>
    * Otherwise, if there is no character with a code greater than
    * {@code '\u005Cu0020'} in the string, then a
    * {@code String} object representing an empty string is
    * returned.
    * <p>
    * Otherwise, let <i>k</i> be the index of the first character in the
    * string whose code is greater than {@code '\u005Cu0020'}, and let
    * <i>m</i> be the index of the last character in the string whose code
    * is greater than {@code '\u005Cu0020'}. A {@code String}
    * object is returned, representing the substring of this string that
    * begins with the character at index <i>k</i> and ends with the
    * character at index <i>m</i>-that is, the result of
    * {@code this.substring(k, m + 1)}.
    * <p>
    * This method may be used to trim whitespace (as defined above) from
    * the beginning and end of a string.
    *
    * @return  A string whose value is this string, with any leading and trailing white
    *          space removed, or this string if it has no leading or
    *          trailing white space.
    */
public String trim() {
    int len = value.length;
    int st = 0;
    char[] val = value;    /* avoid getfield opcode */

    while ((st < len) && (val[st] <= ' ')) {
        st++;
    }
    while ((st < len) && (val[len - 1] <= ' ')) {
        len--;
    }
    return ((st > 0) || (len < value.length)) ? substring(st, len) : this;
}

```

#### split

按照指定符号分割字符串

#### compareTo

```java
/**
    * Compares two strings lexicographically.
    * The comparison is based on the Unicode value of each character in
    * the strings. The character sequence represented by this
    * {@code String} object is compared lexicographically to the
    * character sequence represented by the argument string. The result is
    * a negative integer if this {@code String} object
    * lexicographically precedes the argument string. The result is a
    * positive integer if this {@code String} object lexicographically
    * follows the argument string. The result is zero if the strings
    * are equal; {@code compareTo} returns {@code 0} exactly when
    * the {@link #equals(Object)} method would return {@code true}.
    * <p>
    * This is the definition of lexicographic ordering. If two strings are
    * different, then either they have different characters at some index
    * that is a valid index for both strings, or their lengths are different,
    * or both. If they have different characters at one or more index
    * positions, let <i>k</i> be the smallest such index; then the string
    * whose character at position <i>k</i> has the smaller value, as
    * determined by using the &lt; operator, lexicographically precedes the
    * other string. In this case, {@code compareTo} returns the
    * difference of the two character values at position {@code k} in
    * the two string -- that is, the value:
    * <blockquote><pre>
    * this.charAt(k)-anotherString.charAt(k)
    * </pre></blockquote>
    * If there is no index position at which they differ, then the shorter
    * string lexicographically precedes the longer string. In this case,
    * {@code compareTo} returns the difference of the lengths of the
    * strings -- that is, the value:
    * <blockquote><pre>
    * this.length()-anotherString.length()
    * </pre></blockquote>
    *
    * @param   anotherString   the {@code String} to be compared.
    * @return  the value {@code 0} if the argument string is equal to
    *          this string; a value less than {@code 0} if this string
    *          is lexicographically less than the string argument; and a
    *          value greater than {@code 0} if this string is
    *          lexicographically greater than the string argument.
    */
/**
    * 1. 比较字符串大小，因为String实现了Comparable<String>接口，所有重写了compareTo方法
    * 2. 返回int类型，正数为大，负数为小，基于字符的ASSIC码比较
    */
public int compareTo(String anotherString) {
    int len1 = value.length;
    int len2 = anotherString.value.length;
    int lim = Math.min(len1, len2); //获取长度比较小的字符串长度
    char v1[] = value;
    char v2[] = anotherString.value;

    int k = 0;
    while (k < lim) { //当前索引小于两个字符串中长度较小的字符串长度时，循环继续
        char c1 = v1[k];
        char c2 = v2[k];
        if (c1 != c2) {
            return c1 - c2; //从前向后遍历，有一个字符不相同，返回差值
        }
        k++;
    }
    return len1 - len2; //如果遍历结束，都相同，比较两个字符串长度
}
```

## String的不可变性

在定义String时增加了`final`标记，并且存储数据的数组也被定义为`final`，则表示String一但创建就注定不可变。**对String对象的任何改变都不影响到原对象，相关的任何change操作都会生成新的对象**

### 原因

因为String太过常用，JAVA类库的设计者在实现时做了个小小的变化，即采用了享元模式,每当生成一个新内容的字符串时，他们都被添加到一个共享池中，当第二次再次生成同样内容的字符串实例时，就共享此对象，而不是创建一个新对象，但是这样的做法仅仅适合于通过=符号进行的初始化。需要说明一点的是，在object中，equals()是用来比较内存地址的，但是String重写了equals()方法，用来比较内容的，即使是不同地址，只要内容一致，也会返回true，这也就是为什么a.equals(c)返回true的原因了。

### 优点

1. 可以实现多个变量引用堆内存中的同一个字符串实例，避免创建的开销。
2. 安全性更高
3. 传参与基础类型一样，更加直观

## 字符串常量池

后续会在JVM中进行详细的讲解。

### 字符串常量池概述

#### 常量池表（Constant_Pool table）

1. Class文件中存储所有常量（包括字符串）的table。
2. Class文件中的内容，不是运行内容，表示Class文件中的字节码指令

#### 运行时常量池（Runtime Constant Pool）

1. JVM内存中方法区的一部分，这是运行时的内容
2. 这部分内容（绝大部分）是随着JVM运行时候，从常量池转化而来，每个Class对应一个运行时常量池
3. 除了 Class中常量池内容，还可能包括动态生成并加入这里的内容

#### 字符串常量池（String Pool）

1. 这部分也在方法区中，但与Runtime Constant Pool不是一个概念，String Pool是JVM实例全局共享的，全局只有一个
2. JVM规范要求进入这里的String实例叫“被驻留的interned string”，各个JVM可以有不同的实现，HotSpot是设置了一个哈希表StringTable来引用堆中的字符串实例，被引用就是被驻留。

### 亨元模式

字符串在使用过程中使用到了享元模式（一个系统中如果有多处用到了相同的一个元素，那么我们应该只存储一份此元素，而让所有地方都引用这一个元素），而那个存储元素的地方就叫做“字符串常量池 - String Pool”

### 详细分析

`String str = "hello";`的执行过程：

<div align=center>

![1587636643511.png](..\images\1587636643511.png)

</div>

## 其他

### String与StringBuffer、StringBuilder

1. String是不可变的字符序列
2. StringBuffer和StringBuilder是可变的字符序列
3. StringBuffer是线程安全的，效率低
4. StringBuilder和String是线程不安全的，效率高一些，
5. 效率从高到低：StringBuilder>String>StringBuffer

#### String循环拼接对象

```java
package com.sunld.string;
public class Test1 {
	public static void main(String[] args) {
		String string = "";
		for(int i=0;i<10000;i++) {
			string += "hello";
		}
		System.out.println(string);
	}
}
```

```java
Compiled from "Test1.java"
public class com.sunld.string.Test1 {
  public com.sunld.string.Test1();
    Code:
       0: aload_0
       1: invokespecial #8                  // Method java/lang/Object."<init>":()V
       4: return

  public static void main(java.lang.String[]);
    Code:
       0: ldc           #16                 // String
       2: astore_1
       3: iconst_0
       4: istore_2
       5: goto          31
       8: new           #18                 // class java/lang/StringBuilder
      11: dup
      12: aload_1
      13: invokestatic  #20                 // Method java/lang/String.valueOf:(Ljava/lang/Object;)Ljava/lang/String;
      16: invokespecial #26                 // Method java/lang/StringBuilder."<init>":(Ljava/lang/String;)V
      19: ldc           #29                 // String hello
      21: invokevirtual #31                 // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
      24: invokevirtual #35                 // Method java/lang/StringBuilder.toString:()Ljava/lang/String;
      27: astore_1
      28: iinc          2, 1
      31: iload_2
      32: sipush        10000
      35: if_icmplt     8
      38: getstatic     #39                 // Field java/lang/System.out:Ljava/io/PrintStream;
      41: aload_1
      42: invokevirtual #45                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
      45: return
}
```

8~35行是整个循环过程，并且每次都会new一个StringBuilder（String拼接被jvm优化为使用StringBuilder处理）

#### 使用StringBuilder

```java
package com.sunld.string;

public class Test2 {

	public static void main(String[] args) {
		StringBuilder string = new StringBuilder();
		for(int i=0;i<10000;i++) {
			string.append("hello");
		}
		System.out.println(string.toString());
	}

}
```

```java
Compiled from "Test2.java"
public class com.sunld.string.Test2 {
  public com.sunld.string.Test2();
    Code:
       0: aload_0
       1: invokespecial #8                  // Method java/lang/Object."<init>":()V
       4: return

  public static void main(java.lang.String[]);
    Code:
       0: new           #16                 // class java/lang/StringBuilder
       3: dup
       4: invokespecial #18                 // Method java/lang/StringBuilder."<init>":()V
       7: astore_1
       8: iconst_0
       9: istore_2
      10: goto          23
      13: aload_1
      14: ldc           #19                 // String hello
      16: invokevirtual #21                 // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
      19: pop
      20: iinc          2, 1
      23: iload_2
      24: sipush        10000
      27: if_icmplt     13
      30: getstatic     #25                 // Field java/lang/System.out:Ljava/io/PrintStream;
      33: aload_1
      34: invokevirtual #31                 // Method java/lang/StringBuilder.toString:()Ljava/lang/String;
      37: invokevirtual #35                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
      40: return
}

```

13~27整个循环处理过程，只创建了一次对象。

### ==与equals

==在对字符串比较的时候，对比的是内存地址，而equals比较的是字符串内容，在开发的过程中，equals()通过接受参数，可以避免空指向`"hello".equals(str)`。

### 编码问题

java中的String类是按照Unicode的方式进行编码，并且默认编码方式为UTF-16。使用字节的方式可以指定编码方式，以及编码方式的转换（**转换不对会出现乱码问题**）。并且不能的编码方式对应的数组长度不同。

#### 示例

```java
package com.sunld.string;

public class TestString2 {
	public static void main(String[] args) {
		String b = "𝄞";
		System.out.println(b.length()); 	//输出为2
	}
}
```

通过length的描述:知道返回的字符串长度时Unicode代码单元的数目

```java
/**
    * Returns the length of this string.
    * The length is equal to the number of <a href="Character.html#unicode">Unicode
    * code units</a> in the string.
    * 返回字符串的长度，长度等于字符串中的Unicode代码单元的数目（UTF-16的代码单元的数目）
    *
    * @return  the length of the sequence of characters represented by this
    *          object.
    */
```

#### 说明

在java中的编码方式分为内码和外码：

1. 内码：char或string在内存中使用的编码方式，java中的使用utf16作为内码
2. 外码：除内码都可以认为是外码，包括class文件的编码

> 代码单元：一种转换格式（UTF）中最小的一个分隔，称为一个代码单元（code unit），一个转换公式只会包含整数个单元。UTF-X中的X表示各自码单元的位数。
> UTF-16，可以包含一个单元和两个单元，也就是两个字节和四个字节

#### 替代方案：codePointCount

```java
/**
    * Returns the number of Unicode code points in the specified text
    * range of this {@code String}. The text range begins at the
    * specified {@code beginIndex} and extends to the
    * {@code char} at index {@code endIndex - 1}. Thus the
    * length (in {@code char}s) of the text range is
    * {@code endIndex-beginIndex}. Unpaired surrogates within
    * the text range count as one code point each.
    *
    * @param beginIndex the index to the first {@code char} of
    * the text range.
    * @param endIndex the index after the last {@code char} of
    * the text range.
    * @return the number of Unicode code points in the specified text
    * range
    * @exception IndexOutOfBoundsException if the
    * {@code beginIndex} is negative, or {@code endIndex}
    * is larger than the length of this {@code String}, or
    * {@code beginIndex} is larger than {@code endIndex}.
    * @since  1.5
    * 可以使用该方法返回字符串的准确长度（相对于length方法）
    */
public int codePointCount(int beginIndex, int endIndex) {
    if (beginIndex < 0 || endIndex > value.length || beginIndex > endIndex) {
        throw new IndexOutOfBoundsException();
    }
    return Character.codePointCountImpl(value, beginIndex, endIndex - beginIndex);
}
```

## 参考

1. [Java常用类（二）String类详解](https://www.cnblogs.com/zhangyinhua/p/7689974.html#_lab2_0_1)
2. [String源码分析（基于JDK1.8)](https://blog.csdn.net/qq_34691713/article/details/92572843)
3. [刨根究底字符编码之十四——UTF-16究竟是怎么编码的](https://www.cnblogs.com/benbenalin/p/7152570.html)
