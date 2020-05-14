<!-- TOC -->

- [慢查询日志](#慢查询日志)
    - [说明](#说明)
    - [查看慢查询日志](#查看慢查询日志)
    - [启动和设置慢查询日志](#启动和设置慢查询日志)
        - [修改配置文件](#修改配置文件)
        - [直接设置属性（通过登录mysql服务器直接定义）](#直接设置属性通过登录mysql服务器直接定义)
        - [超时配置](#超时配置)
            - [log_quries_not_using_indexes](#log_quries_not_using_indexes)
    - [分析慢查询日志](#分析慢查询日志)
        - [分析工具](#分析工具)
            - [表](#表)
            - [mysqldumpslow](#mysqldumpslow)
                - [语法](#语法)
                - [示例](#示例)
    - [参考](#参考)

<!-- /TOC -->
# 慢查询日志

## 说明

慢查询日志是用来记录执行时间超过指定时间的查询语句（包括dml和ddl等）。通过慢查询日志，可以查找出哪些查询语句的执行效率很低，以便进行优化。一般建议开启，它对服务器性能的影响微乎其微，但是可以记录mysql服务器上执行了很长时间的查询语句。可以帮助我们定位性能问题的。使用MySQL慢查询日志可以有效的跟踪 执行时间过长 或者 没有使用索引的查询语句。这种包括select 语句，update语句，delete语句，以及insert语句，为优化查询提供帮助。与普通查询日志不同的另一个区别在于，慢查询日志只包含成功执行过的查询语句。

## 查看慢查询日志

```sql
show global variables like '%slow%';
```

<div align=center>

![1589444344246.png](..\images\1589444344246.png)

</div>
输出方式如果为file，需要定义日志路径如上图所示。

## 启动和设置慢查询日志

### 修改配置文件

通过配置文件my.cnf中的log-slow-queries选项可以开启慢查询日志；形式如下：
<div align=center>

![1589444455739.png](..\images\1589444455739.png)

</div>
文件定义说明：log-slow-queries [=  DIR/[filename] ]  

其中，DIR参数指定慢查询日志的存储路径；filename参数指定日志的文件名，生成日志文件的完成名称为filename-slow.log。如果不指定存储路径，慢查询日志默认存储到mysql数据库的数据文件下，如果不指定文件名，默认文件名为hostname-slow.log。

### 直接设置属性（通过登录mysql服务器直接定义）

首先要有全局权限；然后执行mysql>set global（session） slow_query_log=1;

### 超时配置

一般都是通过long_query_time选项来设置这个时间值，时间以秒为单位，可以精确到微秒（可以精确到小数点后6位(微秒)）。如果查询时间超过了这个时间值（默认为10秒），这个查询语句将被记录到慢查询日志中。查看服务器默认时间值方式如下：
<div align=center>

![1589444539188.png](..\images\1589444539188.png)

</div>

注意：其中这个慢查询时间并不是只表示语句自身执行超过10秒还包含由于其他资源被征用造成阻塞的查询执行时间或其他原因等都被记录到慢查询中。所以这个慢查的时长表示从查询开始到查询结束中间包含可能的任何原因所经历的所有时间。

#### log_quries_not_using_indexes

log_quries_not_using_indexes 是否将不使用索引的查询语句记录到慢查询日志中，无论查询速度有多快。

```sql
mysql> set @@global.log_queries_not_using_indexes=1;

mysql> show variables like 'log_queries_not_using_indexes';
+-------------------------------+-------+
| Variable_name                 | Value |
+-------------------------------+-------+
| log_queries_not_using_indexes | ON    |
+-------------------------------+-------+
```

## 分析慢查询日志

```sql
mysql> system more /var/lib/mysql/mysql-slow-query.log
```

<div align=center>

![1589444635250.png](..\images\1589444635250.png)

</div>

### 分析工具

#### 表

log_output参数可以设置慢查询日志的输出形式。默认为FILE,可以设置为TABLE;

```sql
mysql> desc mysql.slow_log;
+----------------+---------------------+------+-----+----------------------+--------------------------------+
| Field          | Type                | Null | Key | Default              | Extra                          |
+----------------+---------------------+------+-----+----------------------+--------------------------------+
| start_time     | timestamp(6)        | NO   |     | CURRENT_TIMESTAMP(6) | on update CURRENT_TIMESTAMP(6) |
| user_host      | mediumtext          | NO   |     | NULL                 |                                |
| query_time     | time(6)             | NO   |     | NULL                 |                                |
| lock_time      | time(6)             | NO   |     | NULL                 |                                |
| rows_sent      | int(11)             | NO   |     | NULL                 |                                |
| rows_examined  | int(11)             | NO   |     | NULL                 |                                |
| db             | varchar(512)        | NO   |     | NULL                 |                                |
| last_insert_id | int(11)             | NO   |     | NULL                 |                                |
| insert_id      | int(11)             | NO   |     | NULL                 |                                |
| server_id      | int(10) unsigned    | NO   |     | NULL                 |                                |
| sql_text       | mediumblob          | NO   |     | NULL                 |                                |
| thread_id      | bigint(21) unsigned | NO   |     | NULL                 |                                |
+----------------+---------------------+------+-----+----------------------+--------------------------------+
12 rows in set (0.00 sec)
```

lock_time表示该SQL执行时被锁阻塞的时间。  
rows_send表示执行SQL后返回的内容行数。  
rows_examined表示该SQL执行时实际扫描的记录条数。  

#### mysqldumpslow

##### 语法

```sql
-s 表示按照何种方式排序
子选项: c、t、l、r
c : SQL执行的次数
t : 执行时间
l : 锁等待时间
r : 返回数据条数
at、al、ar 是对应 t l r 的平均值。
-t :表示返回前 N 条记录。
-g： grep 缩写。包含模糊匹配
```

##### 示例

```bash
//返回访问次数最多的20条SQL语句
mysqldumpslow -s c -t 20 mysql-slow-query.log
//返回return记录数最多的20条SQL语句
mysqldumpslow -s r -t 20 mysql-slow-query.log
//返回含有like的SQL语句
mysqldumpslow -g 'like' -t 20 mysql-slow-query.log
explain sql statements
show profile for query num 需要开启profiling
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
