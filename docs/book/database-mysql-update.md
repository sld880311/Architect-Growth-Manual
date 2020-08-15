<!-- TOC -->

- [Update原理分析](#update原理分析)
  - [Update执行流程](#update执行流程)
  - [两阶段提交协议](#两阶段提交协议)

<!-- /TOC -->
# Update原理分析

## Update执行流程

```sql
update T set c=c+1 where ID=2;
```


1. 客户端通过tcp/ip发送一条sql语句到server层的SQL interface
2. SQL interface接到该请求后，先对该条语句进行解析，验证权限是否匹配
3. 验证通过以后，分析器会对该语句分析,是否语法有错误等
4. 接下来是优化器器生成相应的执行计划，选择最优的执行计划
5. 之后会是执行器根据执行计划执行这条语句。在这一步会去open table,如果该table上有MDL（metadata lock），则等待。如果没有，则加在该表上加短暂的MDL(S)(如果opend_table太大,表明open_table_cache太小。需要不停的去打开frm文件)
6. 进入到引擎层，首先会去**innodb_buffer_pool**里的data dictionary(元数据信息)得到表信息
7. 通过元数据信息,去lock info里查出是否会有相关的锁信息，并把这条update语句需要的锁信息写入到lock info里
8. 然后涉及到的老数据通过快照的方式存储到innodb_buffer_pool里的undo page里,并且记录undo log修改的redo(如果data page里有就直接载入到undo page里，如果没有，则需要去磁盘里取出相应page的数据，载入到undo page里)
9. 在innodb_buffer_pool的data page做update操作。并把操作的物理数据页修改记录到**redo log buffer**里，由于update这个事务会涉及到多个页面的修改，所以redo log buffer里会记录多条页面的修改信息。因为group commit的原因，这次事务所产生的redo log buffer可能会跟随其它事务一同flush并且sync到磁盘上
10. 同时修改的信息，会按照event的格式,记录到**binlog_cache**中。(这里注意binlog_cache_size是transaction级别的,不是session级别的参数,一但commit之后，dump线程会从binlog_cache里把event主动发送给slave的I/O线程)
11. 之后把这条sql,需要在二级索引上做的修改，写入到**change buffer page**，等到下次有其他sql需要读取该二级索引时，再去与二级索引做merge(随机I/O变为顺序I/O,但是由于现在的磁盘都是SSD,所以对于寻址来说,随机I/O和顺序I/O差距不大)
12. 此时update语句已经完成，需要commit（或者rollback），此时 redo log 处于 prepare 状态。然后告知执行器执行完成了，随时可以提交事务。
13. commit操作，由于存储引擎层与server层之间采用的是内部XA(保证两个事务的一致性,这里主要保证redo log和binlog的原子性),所以提交分为prepare阶段与commit阶段
14. prepare阶段,将事务的xid写入，将binlog_cache里的进行flush以及sync操作(大事务的话这步非常耗时)，写binlog
15. commit阶段，由于之前该事务产生的redo log已经sync到磁盘了。所以这步只是在redo log里标记commit
16. 当binlog和redo log都已经落盘以后，如果触发了刷新脏页的操作，先把该脏页复制到doublewrite buffer里，把doublewrite buffer里的刷新到共享表空间，然后才是通过page cleaner线程把脏页写入到磁盘中

<div align=center>

![1589106235941.png](..\images\1589106235941.png)

![1597475447641.png](..\images\1597475447641.png)

</div>

## 两阶段提交协议

binlog是MySQL内部实现二阶段提交的协调者，它为每个事务分配一个事务**ID: XID**
一阶段：
开启事务，redo log 和 undo log已经记录了对应的日志，此时事务状态为prepare
二阶段：
binlog 完成write和fsync后，成功，事务一定提交了，否则事务回滚 发送commit，清除undo信息，刷redo，设置事务状态为completed