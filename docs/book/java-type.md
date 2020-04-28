<!-- TOC -->

- [Java中Type解析](#java%e4%b8%adtype%e8%a7%a3%e6%9e%90)
  - [概述](#%e6%a6%82%e8%bf%b0)
  - [JDK定义](#jdk%e5%ae%9a%e4%b9%89)
  - [类结构](#%e7%b1%bb%e7%bb%93%e6%9e%84)
    - [ParameterizedType](#parameterizedtype)
  - [参考](#%e5%8f%82%e8%80%83)

<!-- /TOC -->
# Java中Type解析

## 概述

这里的Type指java.lang.reflect.Type, 是Java中所有类型的公共高级接口, 代表了Java中的所有类型. Type体系中类型的包括：数组类型(GenericArrayType)、参数化类型(ParameterizedType)、类型变量(TypeVariable)、通配符类型(WildcardType)、原始类型(Class)、基本类型(Class), 以上这些类型都实现Type接口.  

Type 表示的全部类型而每个Class对象表示一个具体类型的实例，如String.class仅代表String类型。由此看来Type与 Class 表示类型几乎是相同的（Class实现接口Type），只不过 Type表示的范围比Class要广得多而已。当然Type还有其他子类。

1. 参数化类型（ParameterizedType）：有原始类型和具体的类型参数，泛型List<String>、Map；
2. 数组类型（GenericArrayType）：不是数组String[] 、byte[]，而是带有泛型的数组，即T[] ；
3. 通配符类型（WildcardType）： 指的是<?>, <? extends T>等等
4. 原始类型（Class）： 不仅仅包含我们平常所指的类，还包括枚举、数组、注解等；
5. 基本类型（Class）, 也就是我们所说的java的基本类型，即int,float,double等
6. 类型变量(TypeVariable)：表示类型参数，可以有上界，比如：T extends Number

## JDK定义

```java
package java.lang.reflect;
/**
 * Type is the common superinterface for all types in the Java
 * programming language. These include raw types, parameterized types,
 * array types, type variables and primitive types.
 *
 * @since 1.5
 */
public interface Type {
    /**
     * Returns a string describing this type, including information
     * about any type parameters.
     *
     * @implSpec The default implementation calls {@code toString}.
     *
     * @return a string describing this type
     * @since 1.8
     */
    default String getTypeName() {
        return toString();
    }
}
```

## 类结构

<div align=center>

![1588062825641.png](..\images\1588062825641.png)

</div>

### ParameterizedType

```java
package java.lang.reflect;
/**
 * ParameterizedType represents a parameterized type such as
 * Collection&lt;String&gt;.
 * 
 * 1. 表示一种参数化类型，比如：Collection<String>
 *
 * <p>A parameterized type is created the first time it is needed by a
 * reflective method, as specified in this package. When a
 * parameterized type p is created, the generic type declaration that
 * p instantiates is resolved, and all type arguments of p are created
 * recursively. See {@link java.lang.reflect.TypeVariable
 * TypeVariable} for details on the creation process for type
 * variables. Repeated creation of a parameterized type has no effect.
 * 
 * 2. 参数化类型在反射方法第一次使用的时候被创建。
 * 3. 当参数化类型p被创建之后，被p实例化的泛型会被解析，并且递归创建p的所有参数化类型
 * 4. 重复创建一个参数化类型不会有影响
 *
 * <p>Instances of classes that implement this interface must implement
 * an equals() method that equates any two instances that share the
 * same generic type declaration and have equal type parameters.
 *
 * @since 1.5
 */
public interface ParameterizedType extends Type {
    /**
     * Returns an array of {@code Type} objects representing the actual type
     * arguments to this type.
     * 
     * 1. 返回确切的泛型参数, 如Map<String, Integer>返回[String, Integer]
     *
     * <p>Note that in some cases, the returned array be empty. This can occur
     * if this type represents a non-parameterized type nested within
     * a parameterized type.
     *
     * @return an array of {@code Type} objects representing the actual type
     *     arguments to this type
     * @throws TypeNotPresentException if any of the
     *     actual type arguments refers to a non-existent type declaration
     * @throws MalformedParameterizedTypeException if any of the
     *     actual type parameters refer to a parameterized type that cannot
     *     be instantiated for any reason
     * @since 1.5
     */
    Type[] getActualTypeArguments();

    /**
     * Returns the {@code Type} object representing the class or interface
     * that declared this type.
     * 1. 返回当前class或interface声明的类型, 如List<?>返回List
     *
     * @return the {@code Type} object representing the class or interface
     *     that declared this type
     * @since 1.5
     */
    Type getRawType();

    /**
     * Returns a {@code Type} object representing the type that this type
     * is a member of.  For example, if this type is {@code O<T>.I<S>},
     * return a representation of {@code O<T>}.
     * 
     * 1. 返回所属类型. 如,当前类型为O<T>.I<S>, 则返回O<T>. 顶级类型将返回null 
     *
     * <p>If this type is a top-level type, {@code null} is returned.
     *
     * @return a {@code Type} object representing the type that
     *     this type is a member of. If this type is a top-level type,
     *     {@code null} is returned
     * @throws TypeNotPresentException if the owner type
     *     refers to a non-existent type declaration
     * @throws MalformedParameterizedTypeException if the owner type
     *     refers to a parameterized type that cannot be instantiated
     *     for any reason
     * @since 1.5
     */
    Type getOwnerType();
}
```

## 参考

1. [ParameterizedType详解](https://blog.csdn.net/JustBeauty/article/details/81116144)
2. [Java中与泛型相关的接口 之 ParameterizedType](https://www.jianshu.com/p/cfa74c980b25)