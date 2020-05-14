<!-- TOC -->

- [查询日志](#查询日志)
    - [说明](#说明)
    - [开启](#开启)
    - [分析](#分析)
    - [参考](#参考)

<!-- /TOC -->
# 查询日志

## 说明

默认情况下查询日志是关闭的。由于查询日志会记录用户的所有操作，其中还包含增删查改等信息，

1. insert查询为了避免数据冲突，如果此前插入过数据，而如果跟主键或唯一键的数据重复那肯定会报错
2. update时也会查询因为更新的时候很可能会更新某一块数据
3. delete查询，只删除符合条件的数据

MySQL普通查询日志记录MySQL服务实例所有的操作，如select、update、insert、delete等操作，无论该操作是否成功执行。还有MySQL客户机与MySQL服务端连接及断开的相关信息，无论连接成功还是失败。
注意：由于普通查询日志几乎记录了MySQL的所有操作，对于数据访问频繁的数据库服务器而言，如果开启MySQL的普通查询日志将会大幅度的降低数据库的性能，因此建议关闭普通查询日志。只有在特殊时期，如需要追踪某些特殊的查询日志，可以临时打开普通的查询日志。在并发操作大的环境下会产生大量的信息从而导致不必要的磁盘IO，会影响mysql的性能的。如若不是为了调试数据库的目的建议不要开启查询日志。

1. log = on|off 是否开启日志功能, 这个设置包含了查询日志和慢查询日志
2. general_log = on|off 是否开启查询日志
3. general_log_file = /path/to/file 指定查询日志的位置
4. log_output = FILE|table 日志输出的位置, 这个指令对慢查询日志也起作用

## 开启

```sql
mysql> show  variables like '%general_log%';
+------------------+------------------------------+
| Variable_name    | Value                        |
+------------------+------------------------------+
| general_log      | OFF                          |
| general_log_file | /var/lib/mysql/incloudos.log |
+------------------+------------------------------+
2 rows in set (0.00 sec)
```

<div align=center>

![1589443754829.png](..\images\1589443754829.png)

</div>

## 分析

拓展解析：日志的输出位置一般有三种方式：file(文件)，table(表)，none(不保存)；其中前两个输出位置可以同时定义，none表示是开启日志功能但是不记录日志信息。file就是通过general_log_file  等方式定义的，而输出位置定义为表时查看日志的内容方式为：

```sql
mysql> use mysql;#所在数据库
Database changed
mysql> show tables;
+---------------------------+
| Tables_in_mysql           |
+---------------------------+
| columns_priv              |
| db                        |
| engine_cost               |
| event                     |
| func                      |
| general_log               |查询日志表
| gtid_executed             |
| help_category             |
| help_keyword              |
| help_relation             |
| help_topic                |
| innodb_index_stats        |
| innodb_table_stats        |
| ndb_binlog_index          |
| plugin                    |
| proc                      |
| procs_priv                |
| proxies_priv              |
| server_cost               |
| servers                   |
| slave_master_info         |
| slave_relay_log_info      |
| slave_worker_info         |
| slow_log                  |
| tables_priv               |
| time_zone                 |
| time_zone_leap_second     |
| time_zone_name            |
| time_zone_transition      |
| time_zone_transition_type |
| user                      |
+---------------------------+
31 rows in set (0.00 sec)
```

## 参考

1. [数据库日志文件-- undo log 、redo log、 undo/redo log](https://blog.csdn.net/ggxxkkll/article/details/7616739)
2. [InnoDB事务日志（redo log 和 undo log）详解](https://blog.csdn.net/leonpenn/article/details/72778901)
3. [MySQL源码学习：ib_logfile、bin-log与主从同步](https://www.iteye.com/blog/dinglin-907123)
4. [Mysql日志抽取与解析](https://blog.csdn.net/hackerwin7/article/details/39896173)
5. [mysql data文件夹下的ibdata1 文件作用](https://blog.csdn.net/u010440155/article/details/54914353)
6. [mysql——innodb事务日志详解](https://blog.csdn.net/donghaixiaolongwang/article/details/60961603)
7. [inlog，redo log，undo log区别](https://blog.csdn.net/mydriverc2/article/details/50629599)
8. [高性能MySQL学习笔记（3） --- InnoDB事务日志（redo log 和 undo log）详解](http://www.itwendao.com/article/detail/450198.html)
