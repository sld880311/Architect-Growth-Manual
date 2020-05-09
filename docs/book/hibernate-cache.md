<!-- TOC -->

- [hibernate缓存详解](#hibernate缓存详解)
    - [N+1问题](#n1问题)
        - [使用iterate（）方法](#使用iterate方法)
        - [使用查询缓存策略](#使用查询缓存策略)
    - [一级缓存，session级别缓存](#一级缓存session级别缓存)
    - [二级缓存（sessionFactory级别）](#二级缓存sessionfactory级别)
        - [下载ehcache相关包](#下载ehcache相关包)
        - [在hibernate.cfg.xml配置文件中配置我们二级缓存的一些属性](#在hibernatecfgxml配置文件中配置我们二级缓存的一些属性)
        - [配置ehcache.xml](#配置ehcachexml)
        - [开启缓存](#开启缓存)
        - [缓存策略](#缓存策略)
        - [二级缓存触发的方法](#二级缓存触发的方法)
        - [执行顺序](#执行顺序)
        - [其他](#其他)
        - [解决N+1问题](#解决n1问题)
    - [查询缓存（sessionFactory级别）](#查询缓存sessionfactory级别)
        - [开启缓存](#开启缓存-1)
        - [查询中需要调用方法](#查询中需要调用方法)
        - [其他](#其他-1)
    - [FlushMode与session.flush()](#flushmode与sessionflush)
        - [MANUAL](#manual)
        - [COMMIT](#commit)
        - [AUTO（默认）](#auto默认)
            - [缺点](#缺点)
        - [ALWAYS](#always)
    - [总结](#总结)
    - [缓存策略提供商](#缓存策略提供商)

<!-- /TOC -->

# hibernate缓存详解

## N+1问题

所谓N+1问题，使用hibernate查询数据，首先返回数据的id信息，并没有返回所有的对象信息，只有在真正使用的时候，在使用这个id对数据库中查询数据，一次查询多次调用数据库（缓存）的情况，就是所谓的N+1问题。以下方法或者策略会出现N+1问题：

### 使用iterate（）方法

存在iterator的原因是，有可能会在一个session中查询两次数据，如果使用list每一次都会把所有的对象查询上来，如果使用iterator仅仅只会查询id，此时所有的对象已经存储在一级缓存(session的缓存)中，可以直接获取

```java
/**
* Return the query results as an <tt>Iterator</tt>. If the query
* contains multiple results per row, the results are returned in
* an instance of <tt>Object[]</tt>.<br>
* <br>
* Entities returned as results are initialized on demand. The first
* SQL query returns identifiers only.<br>
*
* @return the result iterator
*/
Iterator<R> iterate();
```

### 使用查询缓存策略

## 一级缓存，session级别缓存

首次查询数据，会查询数据库返回数据，并且保存到缓存中；再次查询该数据时，直接从缓存中获取（同一个session）。
由于一级缓存是session级别的缓存，只有在同一个session中才能起到再次查询从缓存中获取数据。
比如：list()放入缓存，load查询从缓存中获取数据。
由于Session对象的生命周期通常对应一个数据库事务或者一个应用事务，因此它的缓存是事务范围的缓存。
Session级缓存是必需的，不允许而且事实上也无法卸除。在Session级缓存中，持久化类的每个实例都具有唯一的ID
**调用session的方法会加入一级缓存：save()、update()、savaeOrUpdate()、get()或load()；调用查询接口的list()、iterate()或filter()方法**

## 二级缓存（sessionFactory级别）

### 下载ehcache相关包

### 在hibernate.cfg.xml配置文件中配置我们二级缓存的一些属性

```xml
<!-- 开启二级缓存 -->
<property name="hibernate.cache.use_second_level_cache">true</property>
<!-- 二级缓存的提供类 在hibernate4.0版本以后我们都是配置这个属性来指定二级缓存的提供类-->
<property name="hibernate.cache.region.factory_class"org.hibernate.cache.ehcache.EhCacheRegionFactory</property>
<!-- 4.0之前版本的配置  <property name="hibernate.cache.provider_class">net.sf.ehcache.hibernate.EhCacheProvider</property> -->
<!-- 二级缓存配置文件的位置 -->
<property name="hibernate.cache.provider_configuration_file_resource_path">ehcache.xml</property>
```

### 配置ehcache.xml

```xml
<ehcache>
<!-- Sets the path to the directory where cache .data files are created.
If the path is a Java System Property it is replaced by
         its value in the running VM.
The following properties are translated:
         user.home - User's home directory
         user.dir - User's current working directory
         java.io.tmpdir - Default temp file path -->
 
<!--指定二级缓存存放在磁盘上的位置-->
    <diskStore path="user.dir"/>
<!--我们可以给每个实体类指定一个对应的缓存，如果没有匹配到该类，则使用这个默认的缓存配置-->
    <defaultCache
        maxElementsInMemory="10000"　　//在内存中存放的最大对象数
        eternal="false"　　　　　　　　　//是否永久保存缓存，设置成false
        timeToIdleSeconds="120"
        timeToLiveSeconds="120"
        overflowToDisk="true"　　　　　//如果对象数量超过内存中最大的数，是否将其保存到磁盘中，设置成true
        />
 
<!--
1、timeToLiveSeconds的定义是：以创建时间为基准开始计算的超时时长；
2、timeToIdleSeconds的定义是：在创建时间和最近访问时间中取出离现在最近的时间作为基准计算的超时时长；
3、如果仅设置了timeToLiveSeconds，则该对象的超时时间=创建时间+timeToLiveSeconds，假设为A；
4、如果没设置timeToLiveSeconds，则该对象的超时时间=max(创建时间，最近访问时间)+timeToIdleSeconds，假设为B；
5、如果两者都设置了，则取出A、B最少的值，即min(A,B)，表示只要有一个超时成立即算超时。
-->
<!--可以给每个实体类指定一个配置文件，通过name属性指定，要使用类的全名-->
    <cache name="com.sunld.bean.User"
        maxElementsInMemory="10000"
        eternal="false"
        timeToIdleSeconds="300"
        timeToLiveSeconds="600"
        overflowToDisk="true"
        />
<cache name="sampleCache2"
        maxElementsInMemory="1000"
        eternal="true"
        timeToIdleSeconds="0"
        timeToLiveSeconds="0"
        overflowToDisk="false"
        /> -->
</ehcache>
```

### 开启缓存

- ①如果使用xml配置，我们需要在 *.hbm.xml 中加上一下配置
`<cache usage="read-only"/> `
②如果使用annotation配置，我们需要在类上加上这样一个注解
`@Cache(usage=CacheConcurrencyStrategy.READ_ONLY)`

### 缓存策略

read-only、nonstrict-read-write、read-write、transactional

### 二级缓存触发的方法

save、update、saveOrupdate、load、get、list、query、Criteria方法都会填充二级缓存
get、load、iterate会从二级缓存中取数据
session.save(user)
如果user主键使用“native”生成，则不放入二级缓存

### 执行顺序

1. 条件查询的时候，总是发出一条`select * from table_name where` …. （选择所有字段）这样的SQL语句查询数据库，一次获得所有的数据对象。
2. 把获得的所有数据对象根据ID放入到第二级缓存中。
3. 当Hibernate根据ID访问数据对象的时候，首先从Session一级缓存中查；查不到，如果配置了二级缓存，那么从二级缓存中查；查不到，再查询数据库，把结果按照ID放入到缓存。
4. 删除、更新、增加数据的时候，同时更新缓存。

### 其他

二级缓存缓存的仅仅是对象，如果查询出来的是对象的一些属性，则不会被加到缓存中去
Hibernate的二级缓存策略，是针对于ID查询的缓存策略，对于条件查询则毫无作用。为此，Hibernate提供了针对条件查询的查询缓存（Query Cache）。

### 解决N+1问题

当我们如果需要查询出两次对象的时候，可以使用二级缓存来解决N+1的问题
先list在iterator

## 查询缓存（sessionFactory级别）

### 开启缓存

hibernate.cfg.xml

```xml
<!-- 开启查询缓存 -->
<property name="hibernate.cache.use_query_cache">true</property>
```

### 查询中需要调用方法

.setCacheable(true) //开启查询缓存，查询缓存也是SessionFactory级别的缓存
如果使用注解方式，需要在类上加入在类上加注解：@Cacheable

### 其他

1. 只有当 HQL 查询语句完全相同时，连参数设置都要相同，此时查询缓存才有效
2. 查询缓存也能引起 N+1 的问题，需要开启二级缓存。
3. 查询普通属性，会先到查询缓存中取，如果没有，则查询数据库；
4. 查询实体，会先到查询缓存中取id，如果有，则根据id到缓存(一级/二级)中取实体，如果缓存中取不到实体，再查询数据库。

## FlushMode与session.flush()

在Hibernate中，使用session来操作数据库，session中的存在缓存（一级缓存），当调用session.save或者session.update()等方法的时候，hibernate并不一定会将修改同步到数据库（要看具体的FlushMode），而是先将这些数据存储在session的缓存中，由hibernate自己决定何时同步刷新到数据中。正是由于hibernate的这种缓存机制，**在同一个session中多次修改一个记录**，最终只会向数据库发出一条update语句。由于session缓存以及脏数据检查机制，能够帮助我们尽可能少地发出SQL语句。

hibernate提供了FlushMode接口，能够让我们干预hibernate将脏数据同步到数据库的时机。Session.flush()会触发hibernate将数据同步到数据库。可以通过session.setFlushMode()来修改刷新模式。FlushMode提供了4种缓存模式：MANUAL、COMMIT、AUTO和ALWAYS。源码如下:

```java
/**
 * Enumeration extending javax.persistence flush modes.
 *
 * @author Carlos Gonz lez-Cadenas
 */
public enum FlushModeType {
	/**
	 * Corresponds to {@link org.hibernate.FlushMode#ALWAYS}.
	 */
	ALWAYS,
	/**
	 * Corresponds to  {@link org.hibernate.FlushMode#AUTO}.
	 */
	AUTO,
	/**
	 * Corresponds to  {@link org.hibernate.FlushMode#COMMIT}.
	 */
	COMMIT,
	/**
	 * Corresponds to  {@link org.hibernate.FlushMode#NEVER}.
	 *
	 * @deprecated use MANUAL, will be removed in a subsequent release
	 */
	@Deprecated
	NEVER,
	/**
	 * Corresponds to  {@link org.hibernate.FlushMode#MANUAL}.
	 */
	MANUAL,
	/**
	 * Current flush mode of the persistence context at the time the query is executed.
	 */
	PERSISTENCE_CONTEXT
}
```

### MANUAL

我们必须在代码中**手动调用session.flush()**，hibernate才会将脏数据同步到数据库。如果我们忘记了手动刷新，那么就算是通过session.getTransaction().commit()提交了事务，也不能将修改同步到数据库。

### COMMIT

当数据库事务提交的时候会刷新缓存，当然手动调用flush()肯定也是可以的，不过没有必要罢了。

### AUTO（默认）

事务提交或者手动刷新，都能将脏数据同步到数据库。除此之外，某些查询出现的时候也会导致缓存刷新。

#### 缺点

1. you don't control when Hibernate will decide to execute UPDATE/INSERT/DELETE.
2. potential performance issues because every object modification may lead to dirty checking + DML statement execution.
3. you are not taking advantage of batching and other optimizations that Hibernate can perform when it is not trying to avoid 'stale' state

### ALWAYS

只要有查询出现，或者事务提交，或者手动刷新，都会导致缓存刷新。这个策略性能比较差，实际中不会使用。

## 总结

默认hibernate不会开启查询缓存，这是因为查询缓存只有在hql/hql语句语义完全一致的时候，才能命中。而实际查询场景下，查询条件、分页、排序等构成的复杂查询sql语句很难完全一致。可能是hibernate觉得命中率低，所以默认关闭了查询缓存。我们可以根据实际使用情况，决定是否开启查询缓存，唯一的原则就是命中率要尽可能的高。如果针对A表的查询，查询sql语句基本都是完全一致的情况，就可以针对A使用查询缓存；如果B表的查询条件经常变化，很难命中，那么就不要对B表使用查询缓存。这可能就是hibernate使用查询缓存的时候，既要在hibernate.cfg.xml中进行配置，也需要query.setCacheable(true)的原因。查询缓存只对list有用，对iterate方式无用。iterate不会读也不会写查询缓存，list会读也会写查询缓存。查询缓存中的key是sql语句（这些sql语句会被hibernate解析，保证语义相同的sql，能够命中查询缓存），缓存的value是记录的主键值。

通过开启查询缓存和二级缓存，相同的sql查询可以直接使用查询缓存中的id和二级缓存中的实体对象，可以有效的降低反复的数据库查询，可以提高查询效率。也就是说：同一时候开启查询缓存和二级缓存是有意义的。也是实际使用hibernate的最佳配置。进一步的。我们也能够看出list和iterate方法的差别。list()会将实体对象的id放入查询缓存，将实体对象本身放入二级缓存。iterate不会将实体对象的id放入查询缓存。可是会将实体对象本身存入二级缓存。假设第二次查询可以命中的情况下：list全然不须要查询数据库，可以先从查询缓存中获取到id。再从二级缓存中获取实体对象。iterate一定会发出一条查id的sql，然后去二级缓存中获取实体对象。

## 缓存策略提供商

org.hibernate.cache.HashtableCacheProvider(内存)
org.hibernate.cache.EhCacheProvider(内存，硬盘)
org.hibernate.cache.OSCacheProvider(内存，硬盘)
org.hibernate.cache.SwarmCacheProvider(能用于集群环境)
org.hibernate.cache.TreeCacheProvider(能用于集群环境)
org.hibernate.cache.jbc.JBossCacheRegionFactory(能用于集群环境)
