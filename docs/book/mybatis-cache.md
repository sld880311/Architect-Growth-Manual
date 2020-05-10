<!-- TOC -->

- [MyBatis缓存](#mybatis%e7%bc%93%e5%ad%98)
  - [Mybatis 的一级缓存原理（sqlsession 级别）](#mybatis-%e7%9a%84%e4%b8%80%e7%ba%a7%e7%bc%93%e5%ad%98%e5%8e%9f%e7%90%86sqlsession-%e7%ba%a7%e5%88%ab)
  - [二级缓存原理（mapper 基本）](#%e4%ba%8c%e7%ba%a7%e7%bc%93%e5%ad%98%e5%8e%9f%e7%90%86mapper-%e5%9f%ba%e6%9c%ac)

<!-- /TOC -->
# MyBatis缓存

Mybatis中有一级缓存和二级缓存，默认情况下一级缓存是开启的，而且是不能关闭的。一级缓存是指 SqlSession 级别的缓存，当在同一个 SqlSession 中进行相同的 SQL 语句查询时，第二次以后的查询不会从数据库查询，而是直接从缓存中获取，一级缓存最多缓存 1024 条 SQL。二级缓存是指可以跨 SqlSession 的缓存。是 mapper 级别的缓存，对于 mapper 级别的缓存不同的 sqlsession 是可以共享的。

<div align=center>

![1589028256048.png](..\images\1589028256048.png)

</div>

## Mybatis 的一级缓存原理（sqlsession 级别）

第一次发出一个查询 sql，sql 查询结果写入 sqlsession 的一级缓存中，缓存使用的数据结构是一个 map。 
key：MapperID+offset+limit+Sql+所有的入参 
value：用户信息 
同一个 sqlsession 再次发出相同的 sql，就从缓存中取出数据。如果两次中间出现 commit 操作（修改、添加、删除），本 sqlsession 中的一级缓存区域全部清空，下次再去缓存中查询不到所以要从数据库查询，从数据库查询到再写入缓存。 

## 二级缓存原理（mapper 基本）
二级缓存的范围是mapper 级别（mapper同一个命名空间），mapper以命名空间为单位创建缓存数据结构，结构是 map。mybatis 的二级缓存是通过 CacheExecutor 实现的。CacheExecutor 其实是 Executor 的代理对象。所有的查询操作，在 CacheExecutor 中都会先匹配缓存中是否存在，不存在则查询数据库。 
key：MapperID+offset+limit+Sql+所有的入参 
具体使用需要配置： 
1.	Mybatis 全局配置中启用二级缓存配置 
2.	在对应的 Mapper.xml 中配置 cache 节点 
3.	在对应的 select 查询节点中添加 useCache=true 
