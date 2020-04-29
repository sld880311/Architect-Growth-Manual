<!-- TOC -->

- [Java中方法的传递方式](#java中方法的传递方式)
    - [形参与实参](#形参与实参)
    - [值传递与引用传递](#值传递与引用传递)
    - [代码分析](#代码分析)
    - [参考](#参考)

<!-- /TOC -->
# Java中方法的传递方式

在Java中的方法调用使用的都是**值传递**，Java 中的所有自变量或参数传递都是通过传递句柄进行的。也就是说，当我们传递“一个对象”时，**实际传递的只是指向位于方法外部的那个对象的“一个句柄”**。所以一旦要对那个句柄进行任何修改，便相当于修改外部对象。此外：

1. ■参数传递过程中会自动产生别名问题
2. ■不存在本地对象，只有本地句柄
3. ■句柄有自己的作用域，而对象没有
4. ■对象的“存在时间”在 Java 里不是个问题
5. ■没有语言上的支持（如常量）可防止对象被修改（以避免别名的副作用）

## 形参与实参

> 形式参数：是在定义函数名和函数体的时候使用的参数,目的是用来接收调用该函数时传入的参数。
> 实际参数：在调用有参函数时，主调函数和被调函数之间有数据传递关系。在主调函数中调用一个函数时，函数名后面括号中的参数称为“实际参数”。

## 值传递与引用传递

> 值传递（pass by value）是指在调用函数时将实际参数复制一份传递到函数中(**副本的概念**)，这样在函数中如果对参数进行修改，将不会影响到实际参数。
> 引用传递（pass by reference）是指在调用函数时将实际参数的地址直接传递到函数中，那么在函数中对参数所进行的修改，将影响到实际参数。

<style type="text/css">
.tg  {border-collapse:collapse;border-color:#bbb;border-spacing:0;}
.tg td{background-color:#E0FFEB;border-color:#bbb;border-style:solid;border-width:1px;color:#594F4F;
  font-family:Arial, sans-serif;font-size:14px;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg th{background-color:#9DE0AD;border-color:#bbb;border-style:solid;border-width:1px;color:#493F3F;
  font-family:Arial, sans-serif;font-size:14px;font-weight:normal;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg .tg-0lax{text-align:left;vertical-align:top}
.tg .tg-sjuo{background-color:#C2FFD6;text-align:left;vertical-align:top}
</style>
<table class="tg">
  <tr>
    <th class="tg-0lax"></th>
    <th class="tg-0lax">值传递</th>
    <th class="tg-0lax">引用传递</th>
  </tr>
  <tr>
    <td class="tg-0lax">根本区别</td>
    <td class="tg-sjuo">会创建副本，或者是直接句柄处理</td>
    <td class="tg-0lax">不创建副本</td>
  </tr>
  <tr>
    <td class="tg-0lax">结论</td>
    <td class="tg-sjuo">方法中无法改变原始对象</td>
    <td class="tg-0lax">方法中可以改变原始对象</td>
  </tr>
</table>

## 代码分析

参考《[javap详解](book/javap.md)》

## 参考

1. [Java 到底是值传递还是引用传递？](https://www.toutiao.com/i6813552507239793164/)