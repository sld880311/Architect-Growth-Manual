<!-- TOC -->

- [慢查询日志](#慢查询日志)
    - [说明](#说明)
        - [官方说明](#官方说明)
    - [慢查询日志相关参数](#慢查询日志相关参数)
    - [查看慢查询日志](#查看慢查询日志)
    - [启动和设置慢查询日志](#启动和设置慢查询日志)
        - [修改配置文件](#修改配置文件)
        - [直接设置属性（通过登录mysql服务器直接定义）](#直接设置属性通过登录mysql服务器直接定义)
        - [超时配置](#超时配置)
        - [log_quries_not_using_indexes](#log_quries_not_using_indexes)
        - [log_slow_admin_statements](#log_slow_admin_statements)
        - [log_slow_slave_statements](#log_slow_slave_statements)
        - [参数--log-short-format](#参数--log-short-format)
        - [查询有多少条慢查询记录](#查询有多少条慢查询记录)
        - [日志记录方式](#日志记录方式)
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

1. 记录执行时间超过指定时间（long_query_time，默认10s）的查询语句（包括dml和ddl等）
2. 慢查询日志默认不启动
3. 慢查询日志支持将日志写入文件或数据库表中
4. 优点：可以有效的跟踪 执行时间过长 或者 没有使用索引的查询语句（包括select 语句，update语句，delete语句，以及insert语句）
5. 缺点：存在性能损耗，不建议默认开启
6. 慢查询日志只包含成功执行过的查询语句

### 官方说明

The slow query log consists of SQL statements that took more than long_query_time seconds to execute and required at least min_examined_row_limit rows to be examined. The minimum and default values of long_query_time are 0 and 10, respectively. The value can be specified to a resolution of microseconds. For logging to a file, times are written including the microseconds part. For logging to tables, only integer times are written; the microseconds part is ignored.  

By default, administrative statements are not logged, nor are queries that do not use indexes for lookups. This behavior can be changed usinglog_slow_admin_statements and log_queries_not_using_indexes, as described later.  

## 慢查询日志相关参数

<style type="text/css">
.tg  {border-collapse:collapse;border-color:#bbb;border-spacing:0;}
.tg td{background-color:#E0FFEB;border-color:#bbb;border-style:solid;border-width:1px;color:#594F4F;
  font-family:Arial, sans-serif;font-size:14px;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg th{background-color:#9DE0AD;border-color:#bbb;border-style:solid;border-width:1px;color:#493F3F;
  font-family:Arial, sans-serif;font-size:14px;font-weight:normal;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg .tg-0lax{text-align:left;vertical-align:top}
</style>
<table class="tg">
<thead>
  <tr>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;属性&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;说明&nbsp;&nbsp;&nbsp;</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;slow_query_log&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;是否开启慢查询日志，1表示开启，0表示关闭。&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;log-slow-queries&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;旧版（5.6以下版本）MySQL数据库慢查询日志存储路径。可以不设置该参数，系统则会默认给一个缺省的文件host_name-slow.log&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;slow-query-log-file&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;新版（5.6及以上版本）MySQL数据库慢查询日志存储路径。可以不设置该参数，系统则会默认给一个缺省的文件host_name-slow.log&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;long_query_time&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;慢查询阈值，当查询时间多于设定的阈值时，记录日志&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;log_queries_not_using_indexes&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;未使用索引的查询也被记录到慢查询日志中（可选项）&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;log_output&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">
        日志存储方式。<br>
        log_output='FILE'表示将日志存入文件，默认值是'FILE'。<br>
        log_output='TABLE'表示将日志存入数据库，这样日志信息就会被写入到mysql.slow_log表中。<br>
        MySQL数据库支持同时两种日志存储方式，配置的时候以逗号隔开即可，如：log_output='FILE,TABLE'。日志记录到系统的专用日志表中，要比记录到文件耗费更多的系统资源，因此对于需要启用慢查询日志，又需要能够获得更高的系统性能，那么建议优先记录到文件。<br>
    </td>
  </tr>
</tbody>
</table>

## 查看慢查询日志

```sql
mysql> show variables like '%slow%';
+---------------------------+----------------+
| Variable_name             | Value          |
+---------------------------+----------------+
| log_slow_admin_statements | OFF            |
| log_slow_slave_statements | OFF            |
| slow_launch_time          | 2              |
| slow_query_log            | ON             |
| slow_query_log_file       | SUNLD-slow.log |
+---------------------------+----------------+
5 rows in set, 1 warning (0.03 sec)
```

## 启动和设置慢查询日志

### 修改配置文件

通过配置文件my.cnf中的log-slow-queries选项可以开启慢查询日志；形式如下：
<div align=center>

![1589444455739.png](..\images\1589444455739.png)

</div>

```conf
#修改配置文件my.cnf
slow_query_log =1
slow_query_log_file=/var/lib/mysql/mysql-slow-query-new.log
```

文件定义说明：log-slow-queries [=  DIR/[filename] ]  

其中，DIR参数指定慢查询日志的存储路径；filename参数指定日志的文件名，生成日志文件的完成名称为filename-slow.log。如果不指定存储路径，慢查询日志默认存储到mysql数据库的数据文件下，如果不指定文件名，默认文件名为hostname-slow.log。

### 直接设置属性（通过登录mysql服务器直接定义）

```bash
set global slow_query_log=0;
set global slow_query_log=1;
set global slow_query_log_file='/var/lib/mysql/mysql-slow-query-new.log';
```

### 超时配置

一般都是通过long_query_time选项来设置这个时间值，时间以秒为单位，可以精确到微秒（可以精确到小数点后6位(微秒)）。如果查询时间超过了这个时间值（默认为10秒），这个查询语句将被记录到慢查询日志中。查看服务器默认时间值方式如下：

```sql
mysql> show variables like '%long_query_time%';
+-----------------+-----------+
| Variable_name   | Value     |
+-----------------+-----------+
| long_query_time | 10.000000 |
+-----------------+-----------+
1 row in set, 1 warning (0.00 sec)
```

> 1. 注意：其中这个慢查询时间并不是只表示语句自身执行超过10秒还包含由于其他资源被征用造成阻塞的查询执行时间或其他原因等都被记录到慢查询中。所以这个慢查的时长表示从查询开始到查询结束中间包含可能的任何原因所经历的所有时间。  
> 2. 从MySQL 5.1开始，long_query_time开始以微秒记录SQL语句运行时间，之前仅用秒为单位记录。如果记录到表里面，只会记录整数部分，不会记录微秒部分。  
> 3. 注意：使用命令 set global long_query_time=4修改后，需要重新连接或新开一个会话才能看到修改值。你用show variables like 'long_query_time'查看是当前会话的变量值，你也可以不用重新连接会话，而是用show global variables like 'long_query_time';  

### log_quries_not_using_indexes

系统变量log-queries-not-using-indexes：未使用索引的查询也被记录到慢查询日志中（可选项）。如果调优的话，建议开启这个选项。另外，开启了这个参数，其实使用full index scan的sql也会被记录到慢查询日志。  

This option does not necessarily mean that no index is used. For example, a query that uses a full index scan uses an index but would be logged because the index would not limit the number of rows.

```sql
mysql> set @@global.log_queries_not_using_indexes=1;

mysql> show variables like 'log_queries_not_using_indexes';
+-------------------------------+-------+
| Variable_name                 | Value |
+-------------------------------+-------+
| log_queries_not_using_indexes | ON    |
+-------------------------------+-------+
```

### log_slow_admin_statements

系统变量log_slow_admin_statements表示是否将慢管理语句例如ANALYZE TABLE和ALTER TABLE等记入慢查询日志。

```sql
mysql> show variables like '%log_slow_admin%';
+---------------------------+-------+
| Variable_name             | Value |
+---------------------------+-------+
| log_slow_admin_statements | OFF   |
+---------------------------+-------+
1 row in set, 1 warning (0.02 sec)
```

### log_slow_slave_statements

By default, a replication slave does not write replicated queries to the slow query log. To change this, use the log_slow_slave_statements system variable.  

When the slow query log is enabled, this variable enables logging for queries that have taken more than long_query_time seconds to execute on the slave. This variable was added in MySQL 5.7.1. Setting this variable has no immediate effect. The state of the variable applies on all subsequent START SLAVE statements.  

```sql
mysql> show variables like '%log_slow_slave%';
+---------------------------+-------+
| Variable_name             | Value |
+---------------------------+-------+
| log_slow_slave_statements | OFF   |
+---------------------------+-------+
1 row in set, 1 warning (0.02 sec)
```

### 参数--log-short-format

The server writes less information to the slow query log if you use the --log-short-format option.  

<style type="text/css">
.tg  {border-collapse:collapse;border-spacing:0;}
.tg td{border-color:black;border-style:solid;border-width:1px;font-family:Arial, sans-serif;font-size:14px;
  overflow:hidden;padding:10px 5px;word-break:normal;}
.tg th{border-color:black;border-style:solid;border-width:1px;font-family:Arial, sans-serif;font-size:14px;
  font-weight:normal;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg .tg-0lax{text-align:left;vertical-align:top}
</style>
<table class="tg">
<thead>
  <tr>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;Command-Line Format&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;--log-short-format&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;Permitted Values&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;Type&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;boolean&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;Default&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;FALSE&nbsp;&nbsp;&nbsp;</td>
  </tr>
</tbody>
</table>

### 查询有多少条慢查询记录

```sql
mysql> show global status like '%slow_queries%';
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| Slow_queries  | 0     |
+---------------+-------+
1 row in set (0.01 sec)
```

### 日志记录方式

```sql
mysql> show variables like '%log_output%';
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| log_output    | FILE  |
+---------------+-------+
1 row in set, 1 warning (0.00 sec)
#设置存储方式
set global log_output='TABLE';
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

```bash
mysqldumpslow --help
Usage: mysqldumpslow [ OPTS... ] [ LOGS... ]
Parse and summarize the MySQL slow query log. Options are
  --verbose    verbose
  --debug      debug
  --help       write this text to standard output
  -v           verbose
  -d           debug
  -s ORDER     what to sort by (al, at, ar, c, l, r, t), 'at' is default
                al: average lock time 平均锁定时间
                ar: average rows sent 平均返回记录
                at: average query time 平均查询时间
                 c: count 访问次数
                 l: lock time 锁定时间
                 r: rows sent 返回记录
                 t: query time  查询时间
  -r           reverse the sort order (largest last instead of first)
  -t NUM       just show the top n queries
  -a           don't abstract all numbers to N and strings to 'S'
  -n NUM       abstract numbers with at least n digits within names
  -g PATTERN   grep: only consider stmts that include this string
  -h HOSTNAME  hostname of db server for *-slow.log filename (can be wildcard),
               default is '*', i.e. match all
  -i NAME      name of server instance (if using mysql.server startup script)
  -l           don't subtract lock time from total time
```

##### 示例

```bash
#返回访问次数最多的20条SQL语句
mysqldumpslow -s c -t 20 mysql-slow-query.log
#返回return记录数最多的20条SQL语句
mysqldumpslow -s r -t 20 mysql-slow-query.log
#返回含有like的SQL语句
mysqldumpslow -g 'like' -t 20 mysql-slow-query.log
explain sql statements
show profile for query num 需要开启profiling
#得到返回记录集最多的10个SQL。
mysqldumpslow -s r -t 10 /database/mysql/mysql06_slow.log
#得到访问次数最多的10个SQL
mysqldumpslow -s c -t 10 /database/mysql/mysql06_slow.log
#得到按照时间排序的前10条里面含有左连接的查询语句。
mysqldumpslow -s t -t 10 -g “left join” /database/mysql/mysql06_slow.log
#另外建议在使用这些命令时结合 | 和more 使用 ，否则有可能出现刷屏的情况。
mysqldumpslow -s r -t 20 /mysqldata/mysql/mysql06-slow.log | more
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
