<!-- TOC -->

- [Hibernate对象状态详解](#hibernate%e5%af%b9%e8%b1%a1%e7%8a%b6%e6%80%81%e8%af%a6%e8%a7%a3)
  - [概述](#%e6%a6%82%e8%bf%b0)
  - [状态转换](#%e7%8a%b6%e6%80%81%e8%bd%ac%e6%8d%a2)
    - [相关方法说明](#%e7%9b%b8%e5%85%b3%e6%96%b9%e6%b3%95%e8%af%b4%e6%98%8e)
  - [实例说明](#%e5%ae%9e%e4%be%8b%e8%af%b4%e6%98%8e)
  - [参考](#%e5%8f%82%e8%80%83)

<!-- /TOC -->
# Hibernate对象状态详解

## 概述

<style type="text/css">
.tg  {border-collapse:collapse;border-color:#C44D58;border-spacing:0;}
.tg td{background-color:#F9CDAD;border-color:#C44D58;border-style:solid;border-width:1px;color:#002b36;
  font-family:Arial, sans-serif;font-size:14px;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg th{background-color:#FE4365;border-color:#C44D58;border-style:solid;border-width:1px;color:#fdf6e3;
  font-family:Arial, sans-serif;font-size:14px;font-weight:normal;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg .tg-0lax{text-align:left;vertical-align:top}
</style>
<table class="tg">
<thead>
  <tr>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;状态&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;说明&nbsp;&nbsp;&nbsp;</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;临时状态（Transient）&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;刚创建的对象（new）还没有被Session持久化、缓存中不存在这个对象的数据并且数据库中没有这个对象对应的数据为瞬时状态这个时候是没有OID。&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;持久状态（Persistent）&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;对象经过Session持久化操作，缓存中存在这个对象的数据为持久状态并且数据库中存在这个对象对应的数据为持久状态这个时候有OID。&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;游离状态（Detached）&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;已经被持久化，但不处于session的缓存中。&nbsp;&nbsp;&nbsp;当Session关闭，缓存中不存在这个对象数据而数据库中有这个对象的数据并且有OID为游离状态。&nbsp;&nbsp;&nbsp;</td>
  </tr>
</tbody>
</table>

## 状态转换

<div align=center>

![1588843982557.png](..\images\1588843982557.png)

</div>

当对象在持久化状态时，它一直位于 Session 的缓存中，对它的任何操作在事务提交时都将同步到数据库，因此，对一个已经持久的对象调用 save() 或 update() 方法是没有意义的。

> save()和persist()将会引发SQL的INSERT，delete()会引发SQLDELETE， 而update()或merge()会引发SQLUPDATE。对持久化（persistent）实例的修改在刷新提交的时候会被检测到， 它也会引起SQLUPDATE。saveOrUpdate()或者replicate()会引发SQLINSERT或者UPDATE

### 相关方法说明

1. get、load、find: 方法的使用上较为类似，他们都是将数据库中对应Id的数据映射为Java对象，此时对象变为持久化状态。
2. save: 保存，此时Java对象已经与数据库记录建立的关系。将对象从临时状态的变为持久化状态或者将游离状态的数据变为持久状态。
3. saveOrUpdate: 保存或者更新，如果没有与数据库记录所对应的oid，则执行保存，如果有，则执行更新。将对象从临时状态的变为持久化状态或者将游离状态的数据变为持久状态。
4. delete: 删除对象，将对象从持久化状态或者游离状态变为临时状态。
5. close: 关闭session, 先将session清空，然后再关闭。将对象从持久状态变为临时状态。
6. clear: 清空session缓存。将对象从持久状态变为临时状态。
7. evict: 清除指定的对象。将对象从持久状态变为临时状态.

## 实例说明

## 参考

1. [hibernate的各种保存方式的区别 (save,persist,update,saveOrUpdte,merge,flush,lock)](https://www.cnblogs.com/flqcchblog/p/4619022.html)
2. [Hibernate一级缓存](https://blog.csdn.net/pangqiandou/article/details/53386728)
3. [hibernate session的flushMode的区别](https://blog.csdn.net/looyo/article/details/6309136)
4. [Hibernate的Session_flush与隔离级别](https://blog.csdn.net/lzm1340458776/article/details/32729127)
5. [Hibernate 中的session 的flush、reflush 和clear 方法 ，及数据库的隔离级别](https://blog.csdn.net/chuck_kui/article/details/51531089)
6. [Hibernate深入理解----03操作Session缓存方法（flush、refresh、clear，事务隔离级别）](https://blog.csdn.net/oChangWen/article/details/52582958)
7. [Hibernate中对象的三种状态及相互转化](http://blog.csdn.net/fg2006/article/details/6436517)
8. [深入hibernate的三种状态](https://www.cnblogs.com/xiaoluo501395377/p/3380270.html)
