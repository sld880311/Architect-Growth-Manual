<!-- TOC -->

- [JAVA克隆（Clone）](#java%e5%85%8b%e9%9a%86clone)
	- [java.lang.Cloneable](#javalangcloneable)
	- [java.lang.Object.clone()](#javalangobjectclone)
	- [实现方式](#%e5%ae%9e%e7%8e%b0%e6%96%b9%e5%bc%8f)
		- [测试代码准备](#%e6%b5%8b%e8%af%95%e4%bb%a3%e7%a0%81%e5%87%86%e5%a4%87)
		- [直接赋值](#%e7%9b%b4%e6%8e%a5%e8%b5%8b%e5%80%bc)
		- [浅复制（复制引用但不复制引用的对象）](#%e6%b5%85%e5%a4%8d%e5%88%b6%e5%a4%8d%e5%88%b6%e5%bc%95%e7%94%a8%e4%bd%86%e4%b8%8d%e5%a4%8d%e5%88%b6%e5%bc%95%e7%94%a8%e7%9a%84%e5%af%b9%e8%b1%a1)
		- [深复制（复制对象和其应用对象）](#%e6%b7%b1%e5%a4%8d%e5%88%b6%e5%a4%8d%e5%88%b6%e5%af%b9%e8%b1%a1%e5%92%8c%e5%85%b6%e5%ba%94%e7%94%a8%e5%af%b9%e8%b1%a1)
		- [序列化（深 clone 一中实现）](#%e5%ba%8f%e5%88%97%e5%8c%96%e6%b7%b1-clone-%e4%b8%80%e4%b8%ad%e5%ae%9e%e7%8e%b0)
			- [实现deepclone](#%e5%ae%9e%e7%8e%b0deepclone)
		- [反射（深复制一种）](#%e5%8f%8d%e5%b0%84%e6%b7%b1%e5%a4%8d%e5%88%b6%e4%b8%80%e7%a7%8d)
			- [org.springframework.beans.BeanUtils](#orgspringframeworkbeansbeanutils)
	- [参考](#%e5%8f%82%e8%80%83)

<!-- /TOC -->
# JAVA克隆（Clone）

所谓克隆就是对原有对象复用的复用，**Java语言中克隆针对的是类的实例（对象）**，在Java中一共有三种方式实现。

1. 直接赋值
2. 浅拷贝
3. 深拷贝

## java.lang.Cloneable

```java
/**
 * A class implements the <code>Cloneable</code> interface to
 * indicate to the {@link java.lang.Object#clone()} method that it
 * is legal for that method to make a
 * field-for-field copy of instances of that class.
 * <p>
 * Invoking Object's clone method on an instance that does not implement the
 * <code>Cloneable</code> interface results in the exception
 * <code>CloneNotSupportedException</code> being thrown.
 * <p>
 * By convention, classes that implement this interface should override
 * <tt>Object.clone</tt> (which is protected) with a public method.
 * See {@link java.lang.Object#clone()} for details on overriding this
 * method.
 * <p>
 * Note that this interface does <i>not</i> contain the <tt>clone</tt> method.
 * Therefore, it is not possible to clone an object merely by virtue of the
 * fact that it implements this interface.  Even if the clone method is invoked
 * reflectively, there is no guarantee that it will succeed.
 * 
 * 实现该接口类，可以通过java.lang.Object.clone()方法完成对象中属性的赋值
 *
 * @author  unascribed
 * @see     java.lang.CloneNotSupportedException
 * @see     java.lang.Object#clone()
 * @since   JDK1.0
 */
public interface Cloneable {
}
```

通过源码分析可知：  

1. 必须实现Cloneable接口，否则抛出CloneNotSupportedException异常
2. 实现Cloneable的类应该重写clone()，重写时该方法的修饰符为public。

## java.lang.Object.clone()

```java
/**
* Creates and returns a copy of this object.  The precise meaning
* of "copy" may depend on the class of the object. The general
* intent is that, for any object {@code x}, the expression:
* <blockquote>
* <pre>
* x.clone() != x</pre></blockquote>
* will be true, and that the expression:
* <blockquote>
* <pre>
* x.clone().getClass() == x.getClass()</pre></blockquote>
* will be {@code true}, but these are not absolute requirements.
* While it is typically the case that:
* <blockquote>
* <pre>
* x.clone().equals(x)</pre></blockquote>
* will be {@code true}, this is not an absolute requirement.
* <p>
* By convention, the returned object should be obtained by calling
* {@code super.clone}.  If a class and all of its superclasses (except
* {@code Object}) obey this convention, it will be the case that
* {@code x.clone().getClass() == x.getClass()}.
* <p>
* By convention, the object returned by this method should be independent
* of this object (which is being cloned).  To achieve this independence,
* it may be necessary to modify one or more fields of the object returned
* by {@code super.clone} before returning it.  Typically, this means
* copying any mutable objects that comprise the internal "deep structure"
* of the object being cloned and replacing the references to these
* objects with references to the copies.  If a class contains only
* primitive fields or references to immutable objects, then it is usually
* the case that no fields in the object returned by {@code super.clone}
* need to be modified.
* <p>
* The method {@code clone} for class {@code Object} performs a
* specific cloning operation. First, if the class of this object does
* not implement the interface {@code Cloneable}, then a
* {@code CloneNotSupportedException} is thrown. Note that all arrays
* are considered to implement the interface {@code Cloneable} and that
* the return type of the {@code clone} method of an array type {@code T[]}
* is {@code T[]} where T is any reference or primitive type.
* Otherwise, this method creates a new instance of the class of this
* object and initializes all its fields with exactly the contents of
* the corresponding fields of this object, as if by assignment; the
* contents of the fields are not themselves cloned. Thus, this method
* performs a "shallow copy" of this object, not a "deep copy" operation.
* <p>
* The class {@code Object} does not itself implement the interface
* {@code Cloneable}, so calling the {@code clone} method on an object
* whose class is {@code Object} will result in throwing an
* exception at run time.
*
* @return     a clone of this instance.
* @throws  CloneNotSupportedException  if the object's class does not
*               support the {@code Cloneable} interface. Subclasses
*               that override the {@code clone} method can also
*               throw this exception to indicate that an instance cannot
*               be cloned.
* @see java.lang.Cloneable
*/
protected native Object clone() throws CloneNotSupportedException;
```

## 实现方式

### 测试代码准备  

```java
package com.sunld.clone;

public class CloneTest {

	public static void main(String[] args) {
		CloneObject co1 = new CloneObject();
		co1.setName("name1");
		co1.setAge(10);
		CloneObject2 c021 = new CloneObject2();
		c021.setName2("name21");
		co1.setCloneObject2(c021);
		
	}

}
class CloneObject{
	private String name;
	private int age;
	private CloneObject2 cloneObject2;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public CloneObject2 getCloneObject2() {
		return cloneObject2;
	}
	public void setCloneObject2(CloneObject2 cloneObject2) {
		this.cloneObject2 = cloneObject2;
	}
	
}
class CloneObject2{
	private String name2;

	public String getName2() {
		return name2;
	}

	public void setName2(String name2) {
		this.name2 = name2;
	}
	
}

```

### 直接赋值

```java
/**
* 1.直接赋值
*/
CloneObject co2 = co1;
System.out.println(co2.getName());
System.out.println(co1.getName());
```

对象co2和co1在堆内存中的地址一致，所以**两个对象之间会互相影响**。

### 浅复制（复制引用但不复制引用的对象）

需要通过实现接口`java.lang.Cloneable`，并且重新`java.lang.Object.clone()`的方法进行实现。实现原理：创建一个新对象，然后将当前对象的非静态字段复制到该新对象，如果字段是值类型的，那么对该字段执行复制；如果该字段是引用类型的话，则复制引用但不复制引用的对象。**因此，原始对象及其副本引用同一个对象。（对象中的对象）**

参考代码如下：  

```java
class CloneObject implements Cloneable{
	private String name;
	private int age;
	private CloneObject2 cloneObject2;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public CloneObject2 getCloneObject2() {
		return cloneObject2;
	}
	public void setCloneObject2(CloneObject2 cloneObject2) {
		this.cloneObject2 = cloneObject2;
	}
	/**
	 * (1) 实现 Cloneable 接口
	 * (2) 覆盖 clone()
	 * (3) 在自己的 clone()中调用 super.clone()
	 * (4) 在自己的 clone()中捕获违例
	 */
	@Override
	public CloneObject clone()  {
		try {
			return (CloneObject) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
```

```java
/**
* 2.浅复制
*/
CloneObject co2 = co1.clone();
co2.setName("cloneName");
co2.getCloneObject2().setName2("cloneName2222");
System.out.println(co2.getName());
System.out.println(co1.getName());

System.out.println(co1.getCloneObject2().getName2());
System.out.println(co2.getCloneObject2().getName2());
```

### 深复制（复制对象和其应用对象）

深拷贝不仅复制对象本身，而且复制对象包含的引用指向的所有对象。

```java
class CloneObject implements Cloneable{
	private String name;
	private int age;
	private CloneObject2 cloneObject2;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public CloneObject2 getCloneObject2() {
		return cloneObject2;
	}
	public void setCloneObject2(CloneObject2 cloneObject2) {
		this.cloneObject2 = cloneObject2;
	}
	/**
	 * (1) 实现 Cloneable 接口
	 * (2) 覆盖 clone()
	 * (3) 在自己的 clone()中调用 super.clone()
	 * (4) 在自己的 clone()中捕获违例
	 */
	@Override
	public CloneObject clone()  {
		try {
			CloneObject cloneObject = (CloneObject) super.clone();
			cloneObject.cloneObject2 = this.cloneObject2.clone();
			return cloneObject;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
class CloneObject2 implements Cloneable{
	private String name2;

	public String getName2() {
		return name2;
	}

	public void setName2(String name2) {
		this.name2 = name2;
	}
	
	@Override
	public CloneObject2 clone()  {
		try {
			return (CloneObject2) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
```

### 序列化（深 clone 一中实现）

在Java 语言里深复制一个对象，常常可以先使对象实现 `Serializable 接口`(必须)，然后把对象（实际上只是对象的一个拷贝）写到一个流里（**可以实现对象持久化到内存或磁盘中，提高对象的生命周期**），再从流里读出来，便可以重建对象。特点：

1. 序列化对象以字节数组保持-静态成员不保存，保存到是对象的“状态”
2. 序列化用户远程对象传输，比如：RMI(远程方法调用)
3. 实现接口`java.io.Serializable`
4. 序列化和反序列化：`ObjectOutputStream` 和`ObjectInputStream`
5. `writeObject` 和 `readObject` 自定义序列化策略，可以实现序列化信息和反序列化的控制
6. 序列化 ID（`private static final long serialVersionUID`） ：需要保证序列化和反序列化类的ID一致性
7. Transient  关键字阻止该变量被序列化到文件中
   - 阻止该变量被序列化到文件中，在被反序列化后，transient 变量的值被设为初始值，如 int 型的是 0，对象型的是 null。
   - writeExternal() 使用该方法也可以实现序列信息的控制

#### 实现deepclone

```java
package com.sunld.clone;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerCloneTest2 {

	public static void main(String[] args) {
		SerCloneObject co1 = new SerCloneObject();
		co1.setName("name1");
		co1.setAge(10);
		SerCloneObject2 c021 = new SerCloneObject2();
		c021.setName2("name21");
		co1.setCloneObject2(c021);
		
		SerCloneObject co2 = co1.deepClone();
		co2.setName("cloneName");
		co2.getCloneObject2().setName2("cloneName2222");
		System.out.println(co2.getName());
		System.out.println(co1.getName());
		
		System.out.println(co1.getCloneObject2().getName2());
		System.out.println(co2.getCloneObject2().getName2());
	}

}
class SerCloneObject implements Serializable{
	private static final long serialVersionUID = 1L;
	private String name;
	private int age;
	private SerCloneObject2 cloneObject2;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	
	public SerCloneObject2 getCloneObject2() {
		return cloneObject2;
	}
	public void setCloneObject2(SerCloneObject2 cloneObject2) {
		this.cloneObject2 = cloneObject2;
	}
	
	public SerCloneObject deepClone() {
		//将该对象序列化成流,因为写在流里的是对象的一个拷贝，
		//而原对象仍然存在于JVM里面。所以利用这个特性可以实现对象的深拷贝  
		ByteArrayOutputStream bos = new ByteArrayOutputStream();  
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(this);  
			//将流序列化成对象  
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());  
			ObjectInputStream ois = new ObjectInputStream(bis);  
			return (SerCloneObject) ois.readObject();  
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}  
	}
}
class SerCloneObject2 implements Serializable{
	private static final long serialVersionUID = 1L;
	private String name2;

	public String getName2() {
		return name2;
	}

	public void setName2(String name2) {
		this.name2 = name2;
	}
}
```

### 反射（深复制一种）

#### org.springframework.beans.BeanUtils

```java
	/**
	 * Copy the property values of the given source bean into the given target bean.
	 * <p>Note: The source and target classes do not have to match or even be derived
	 * from each other, as long as the properties match. Any bean properties that the
	 * source bean exposes but the target bean does not will silently be ignored.
	 * @param source the source bean
	 * @param target the target bean
	 * @param editable the class (or interface) to restrict property setting to
	 * @param ignoreProperties array of property names to ignore
	 * @throws BeansException if the copying failed
	 * @see BeanWrapper
	 */
	private static void copyProperties(Object source, Object target, @Nullable Class<?> editable,
			@Nullable String... ignoreProperties) throws BeansException {

		Assert.notNull(source, "Source must not be null");
		Assert.notNull(target, "Target must not be null");

		/**
		 * 获取目标对象的class
		 */
		Class<?> actualEditable = target.getClass();
		if (editable != null) {
			if (!editable.isInstance(target)) {
				throw new IllegalArgumentException("Target class [" + target.getClass().getName() +
						"] not assignable to Editable class [" + editable.getName() + "]");
			}
			actualEditable = editable;
		}
		/**
		 * 获取目标对象的属性信息
		 */
		PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
		/**
		 * 处理忽略属性
		 */
		List<String> ignoreList = (ignoreProperties != null ? Arrays.asList(ignoreProperties) : null);

		for (PropertyDescriptor targetPd : targetPds) {
			Method writeMethod = targetPd.getWriteMethod();
			if (writeMethod != null && (ignoreList == null || !ignoreList.contains(targetPd.getName()))) {
				PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
				if (sourcePd != null) {
					Method readMethod = sourcePd.getReadMethod();
					if (readMethod != null &&
							ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType())) {
						try {
							if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
								readMethod.setAccessible(true);
							}
							Object value = readMethod.invoke(source);
							if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
								writeMethod.setAccessible(true);
							}
							writeMethod.invoke(target, value);
						}
						catch (Throwable ex) {
							throw new FatalBeanException(
									"Could not copy property '" + targetPd.getName() + "' from source to target", ex);
						}
					}
				}
			}
		}
	}
```

## 参考

1. [Java提高篇——对象克隆（复制）](https://www.cnblogs.com/Qian123/p/5710533.html)
2. [编程思想](https://lingcoder.github.io/OnJava8/#/book/Appendix-Object-Serialization)
