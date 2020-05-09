<!-- TOC -->

- [Hibernate常用方法说明](#hibernate%e5%b8%b8%e7%94%a8%e6%96%b9%e6%b3%95%e8%af%b4%e6%98%8e)
  - [session.find/load](#sessionfindload)
  - [Session.get/load](#sessiongetload)
  - [参考](#%e5%8f%82%e8%80%83)

<!-- /TOC -->
# Hibernate常用方法说明

## session.find/load

hibernate中session.find/load方法都能通过指定实体类名和id从数据库中读取指定的记录，并且返回与之对映的实体对象。但是它们也有很大的区别 ,find()和load()方法在执行检索时的区别：

1. find()在类检索级别时总是执行立即检索而且如果检索不到相关的对象的话会返回null,load()方法则会抛出一个ObjectNotException
2. load()方法可返回一个实体代理类类型，而find()方法直接返回的是实体类对象。
3. load()方法可以充分利用内部缓存和二级缓存，而find()方法会忽略二级缓存，若内部缓存没有查询到会到数据库中去查询。

## Session.get/load

1. 如果未能发现符合条件的记录，get方法返回null，而load方法会抛出一个ObejctNotFoundException。
2. Load方法可返回实体的代理类类型，而get方法永远直接返回实体类。
3. Load方法可以充分利用内部缓存和二级缓存中现有数据，而get方法则仅仅在内部缓存中进行数据查找，如没有发现对应数据，将越过二级缓存，直接调用SQL完成数据读取。

Session 的load(), get() , find() 方法都可实现对业务数据的查询，其中load()会因所要查询的对象类的映射文件（比如Customer.hbm.xml文件）中设置的检索策略，而实现立即检索或延迟检索。get()方法和find()方法却不受这种控制，它们总是会执行立即检索。

那么象load()这样的方法，当其被设置为使用延迟检索时，它会返回什么样的实例呢？当使用延迟检索时返回为这一对象的代理类实例，代理类实例也有一个标志性的OID, 但没有被实例化，其所有属性为默认初始值或为空。提到检索，load() 和get()方法都是用指定了OID的方式检索并返回一个与数据库中记录相对应的实例对象。如Customer customer = (Customer)session.get(Customer.class, new Long(1)) 将会返回一个Customer实例对象customer. 而 find()可以实现批量的检索，其返回类型为List的对象集合。例如：
List customerList = session.find( “from Customer as c”);

Load(): 另一种读取数据的方法,和get的区别是: 1.异常处理: load有异常处理,get没有,它返回null,2.get从数据库读数据,load可能去读缓冲区。

## 参考
