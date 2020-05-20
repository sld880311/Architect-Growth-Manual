<!-- TOC -->

- [Mysql数据备份与恢复](#mysql数据备份与恢复)
    - [说明](#说明)
        - [备份的目的](#备份的目的)
        - [备份需要考虑的问题](#备份需要考虑的问题)
        - [备份的类型](#备份的类型)
            - [根据是否需要数据库离线](#根据是否需要数据库离线)
            - [根据要备份的数据集合的范围](#根据要备份的数据集合的范围)
            - [根据备份数据或文件](#根据备份数据或文件)
        - [备份对象](#备份对象)
    - [数据库备份与还原](#数据库备份与还原)
        - [利用select into outfile实现数据的备份与还原](#利用select-into-outfile实现数据的备份与还原)
            - [备份数据](#备份数据)
                - [错误分析](#错误分析)
                - [文件内容](#文件内容)
            - [数据恢复](#数据恢复)
            - [其他例子](#其他例子)
        - [使用mysqldump备份数据和还原数据](#使用mysqldump备份数据和还原数据)
            - [说明](#说明-1)
            - [语法](#语法)
                - [备份一个数据库](#备份一个数据库)
                - [备份多个数据库](#备份多个数据库)
                - [备份全部数据库](#备份全部数据库)
            - [施加读锁的方式](#施加读锁的方式)
                - [直接在备份的时候添加选项](#直接在备份的时候添加选项)
                - [在服务器端书写命令](#在服务器端书写命令)
            - [备份策略](#备份策略)
            - [完整备份数据库](#完整备份数据库)
                - [文件信息](#文件信息)
                - [文件内容](#文件内容-1)
            - [日志文件内容general log](#日志文件内容general-log)
                - [FLUSH /*!40101 LOCAL */ TABLES](#flush-40101-local--tables)
                - [FLUSH TABLES WITH READ LOCK](#flush-tables-with-read-lock)
                - [SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ](#set-session-transaction-isolation-level-repeatable-read)
                - [START TRANSACTION /*!40100 WITH CONSISTENT SNAPSHOT */](#start-transaction-40100-with-consistent-snapshot-)
                - [SHOW MASTER STATUS](#show-master-status)
                - [UNLOCK TABLES](#unlock-tables)
            - [在服务器端执行相关sql，然后查看二进制日志的记录位置](#在服务器端执行相关sql然后查看二进制日志的记录位置)
            - [增量备份](#增量备份)
            - [恢复数据](#恢复数据)
            - [注意事项](#注意事项)
        - [直接复制整个数据库目录](#直接复制整个数据库目录)
        - [使用mysqlhotcopy工具快速备份](#使用mysqlhotcopy工具快速备份)
    - [数据库恢复](#数据库恢复)
        - [还原直接复制目录的备份](#还原直接复制目录的备份)
    - [其他](#其他)
        - [mysql查询表的数据大小](#mysql查询表的数据大小)
            - [information_schema](#information_schema)
        - [要查询表所占的容量，就是把表的数据和索引加起来就可以了](#要查询表所占的容量就是把表的数据和索引加起来就可以了)
        - [查询所有的数据大小](#查询所有的数据大小)
        - [查询某个表的数据](#查询某个表的数据)
        - [mysqldump参数汇总](#mysqldump参数汇总)
    - [参考](#参考)

<!-- /TOC -->
# Mysql数据备份与恢复

## 说明

### 备份的目的

1. 做灾难恢复：对损坏的数据进行恢复和还原
2. 需求改变：因需求改变而需要把数据还原到改变以前
3. 测试：测试新功能是否可用

### 备份需要考虑的问题

1. 可以容忍丢失多长时间的数据；
2. 恢复数据要在多长时间内完；
3. 恢复的时候是否需要持续提供服务；
4. 恢复的对象，是整个库，多个表，还是单个库，单个表。

### 备份的类型

#### 根据是否需要数据库离线

1. 冷备（cold backup）：需要关mysql服务，读写请求均不允许状态下进行；
2. 温备（warm backup）： 服务在线，但仅支持读请求，不允许写请求；
3. 热备（hot backup）：备份的同时，业务不受影响。

> 注：
>> 1、这种类型的备份，取决于业务的需求，而不是备份工具  
>> 2、MyISAM不支持热备，InnoDB支持热备，但是需要专门的工具  

#### 根据要备份的数据集合的范围

1. 完全备份：full backup，备份全部字符集。
2. 增量备份: incremental backup 上次完全备份或增量备份以来改变了的数据，不能单独使用，要借助完全备份，备份的频率取决于数据的更新频率。
3. 差异备份：differential backup 上次完全备份以来改变了的数据。

建议的恢复策略：  

1. 完全+增量+二进制日志
2. 完全+差异+二进制日志

#### 根据备份数据或文件

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
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;类型&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;优点&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;缺点&nbsp;&nbsp;&nbsp;</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;物理备份&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;直接备份数据文件&nbsp;&nbsp;&nbsp;备份和恢复操作都比较简单，能够跨mysql的版本，&nbsp;&nbsp;&nbsp;恢复速度快，属于文件系统级别的&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;不要假设备份一定可用，要测试&nbsp;&nbsp;&nbsp;mysql&gt;check&nbsp;&nbsp;&nbsp;tables；检测表是否可用&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;逻辑备份&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;备份表中的数据和代码&nbsp;&nbsp;&nbsp;恢复简单、备份的结果为ASCII文件，可以编辑&nbsp;&nbsp;&nbsp;与存储引擎无关&nbsp;&nbsp;&nbsp;可以通过网络备份和恢复&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;备份或恢复都需要mysql服务器进程参与&nbsp;&nbsp;&nbsp;备份结果占据更多的空间，&nbsp;&nbsp;&nbsp;浮点数可能会丢失精度&nbsp;&nbsp;&nbsp;还原之后，缩影需要重建&nbsp;&nbsp;&nbsp;</td>
  </tr>
</tbody>
</table>

### 备份对象

1. 数据；
2. 配置文件；
3. 代码：存储过程、存储函数、触发器
4. os相关的配置文件
5. 复制相关的配置
6. 二进制日志

## 数据库备份与还原

### 利用select into outfile实现数据的备份与还原

#### 备份数据

```sql
mysql> show databases;
+--------------------+
| Database           |
+--------------------+
| information_schema |
| mysql              |
| performance_schema |
| sunld_db           |
| sys                |
+--------------------+
6 rows in set (0.00 sec)

mysql> use sunld_db;
Database changed
mysql> select * from t_sunld_user;
+------+------+
| id   | name |
+------+------+
|    1 | a    |
|    2 | 啊   |
+------+------+
2 rows in set (0.01 sec)

mysql> select * from t_sunld_user into outfile '/var/lib/mysql/t_sunld_user_01.txt';
ERROR 1290 (HY000): The MySQL server is running with the --secure-file-priv option so it cannot execute this statement
```

##### 错误分析

出现这个问题的原因是因为启动MySQL的时候使用了--secure-file-priv这个参数，这个参数的主要目的就是限制LOAD DATA INFILE或者SELECT INTO OUTFILE之类文件的目录位置，我们可以使用。

```sql
mysql> show variables like 'secure_file_priv';
+------------------+-----------------------+
| Variable_name    | Value                 |
+------------------+-----------------------+
| secure_file_priv | /var/lib/mysql-files/ |
+------------------+-----------------------+
1 row in set (0.01 sec)
```

如果要解决这个问题，我们可以通过下面2种方式：

1. 将你要导入或导出的文件位置指定到你设置的路径里
2. 由于不能动态修改，我们可以修改my.cnf里关于这个选项的配置，然后重启即可。

```sql
mysql> select * from t_sunld_user into outfile '/var/lib/mysql-files/t_sunld_user_01.txt';
Query OK, 2 rows affected (0.08 sec)
```

##### 文件内容

```sql
[root@]# pwd
/var/lib/mysql-files
[root@]# cat t_sunld_user_01.txt
1	a
2	啊
```

发现文件只是一个文本文件，如果导入的话需要使用load data infile进行数据恢复

#### 数据恢复

```sql
mysql> delete from t_sunld_user;
Query OK, 2 rows affected (0.01 sec)

mysql> select * from t_sunld_user;
Empty set (0.00 sec)

mysql> load data infile '/var/lib/mysql-files/t_sunld_user_01.txt' into table t_sunld_user;
Query OK, 2 rows affected (0.02 sec)
Records: 2  Deleted: 0  Skipped: 0  Warnings: 0

mysql> select * from t_sunld_user;
+------+------+
| id   | name |
+------+------+
|    1 | a    |
|    2 | 啊   |
+------+------+
2 rows in set (0.00 sec)
```

#### 其他例子

```sql
SELECT * FROM `metadatakeys` INTO OUTFILE 'D:/outfile.txt' FIELDS TERMINATED BY '|' ENCLOSED BY '"' LINES TERMINATED BY '\r\n'
LOAD DATA INFILE 'D:/outfile.txt' INTO TABLE `metadatakeys`FIELDS TERMINATED BY '|' ENCLOSED BY '"' LINES TERMINATED BY '\r\n'
```

### 使用mysqldump备份数据和还原数据

#### 说明

1. mysqldump命令将数据库中的数据备份成一个文本文件。表的结构和表中的数据将存储在生成的文本文件中。
2. mysqldump命令的工作原理很简单。它先查出需要备份的表的结构，再在文本文件中生成一个CREATE语句。然后，将表中的所有记录转换成一条INSERT语句。然后通过这些语句，就能够创建表并插入数据。
3. 抛开源码不谈，其实我们可以通过打开general log，查看mysqldump全库备份时执行的命令来了解mysqldump背后的原理。

```sql
打开general log
mysql> set global general_log=on;
其中，general log的存放路径可通过以下命令查看
mysql> show variables like '%general_log_file%';
```

#### 语法

```bash
Usage: mysqldump [OPTIONS] database [tables]
OR     mysqldump [OPTIONS] --databases [OPTIONS] DB1 [DB2 DB3...]
OR     mysqldump [OPTIONS] --all-databases [OPTIONS]
mysqldump -u username -p dbname table1 table2 ...-> BackupName.sql
其中：
dbname参数表示数据库的名称；
table1和table2参数表示需要备份的表的名称，为空则整个数据库备份；
BackupName.sql参数表设计备份文件的名称，文件名前面可以加上一个绝对路径。通常将数据库被分成一个后缀名为sql的文件；

```

##### 备份一个数据库

```bash
mysqldump -uroot -p123456a? cloudos_db t_sunld_user > back001.sql
```

##### 备份多个数据库

```bash
语法：
mysqldump -u username -p --databases dbname2 dbname2 > Backup.sql
加上了--databases选项，然后后面跟多个数据库
mysqldump -u root -p --databases test mysql > backup.sql
```

##### 备份全部数据库

```bash
mysqldump命令备份所有数据库的语法如下：
mysqldump -u username -p -all-databases > BackupName.sql
示例：
mysqldump -u -root -p -all-databases > all.sql
```

#### 施加读锁的方式

mysqldump 常用来做温备，所以我们首先需要对想备份的数据施加读锁

##### 直接在备份的时候添加选项

1. --lock-all-tables 是对要备份的数据库的所有表施加读锁
2. --lock-table 仅对单张表施加读锁，即使是备份整个数据库，它也是在我们备份某张表的时候才对该表施加读锁，因此适用于备份单张表

##### 在服务器端书写命令

```bash
mysql> flush tables with read lock; 施加锁，表示把位于内存上的表统统都同步到磁盘上去，然后施加读锁
mysql> flush tables with read lock;释放读锁
```

但这对于InnoDB存储引擎来讲，虽然你也能够请求道读锁，但是不代表它的所有数据都已经同步到磁盘上，因此当面对InnoDB的时候，我们要使用mysql> show engine innodb status; 看看InnoDB所有的数据都已经同步到磁盘上去了，才进行备份操作。

#### 备份策略

完全备份+增量备份+二进制日志

#### 完整备份数据库

```bash
mysqldump -uroot -p123456a? --single-transaction --master-data=2 --databases sunld_db > /var/lib/mysql/sunld_db_`date +%F`.sql
mysqldump: [Warning] Using a password on the command line interface can be insecure.

--single-transaction: 基于此选项能实现热备InnoDB表；因此，不需要同时使用--lock-all-tables；
--master-data=2  记录备份那一时刻的二进制日志的位置，并且注释掉，1是不注释的
--databases hellodb 指定备份的数据库
```

##### 文件信息

```bash
[root@ mysql]# pwd
/var/lib/mysql
[root@ mysql]# ll|grep sunld_db
drwxr-x--- 2 mysql mysql         65 Dec 19 21:57 sunld_db
-rw-r--r-- 1 root  root        2232 Dec 19 22:03 sunld_db_2017-12-19.sql
```

##### 文件内容

```sql
#文件中以“--”开头的都是SQL语言的注释
#以"/*!40101"等形式开头的是与MySQL有关的注释。
#40101是MySQL数据库的版本号，如果MySQL的版本比1.11高，则/*!40101和*/之间的内容就被当做SQL命令来执行，如果比4.1.1低就会被当做注释。

-- MySQL dump 10.13  Distrib 5.7.17, for Linux (x86_64)#文件的开头会记录MySQL的版本、备份的主机名和数据库名。
--
-- Host: localhost    Database: sunld_db
-- ------------------------------------------------------
-- Server version	5.7.17-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
--
-- Position to start replication or point-in-time recovery from
--
-- CHANGE MASTER TO MASTER_LOG_FILE='mysql-bin.000016', MASTER_LOG_POS=143358362;#记录了二进制日志的位置 
--
-- Current Database: `sunld_db`
--
CREATE DATABASE /*!32312 IF NOT EXISTS*/ `sunld_db` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `sunld_db`;
--
-- Table structure for table `t_sunld_user`
--
DROP TABLE IF EXISTS `t_sunld_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_sunld_user` (
  `id` int(11) DEFAULT NULL,
  `name` varchar(255) COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
--
-- Dumping data for table `t_sunld_user`
--
LOCK TABLES `t_sunld_user` WRITE;
/*!40000 ALTER TABLE `t_sunld_user` DISABLE KEYS */;
INSERT INTO `t_sunld_user` VALUES (1,'a'),(2,'啊');
/*!40000 ALTER TABLE `t_sunld_user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
-- Dump completed on 2017-12-19 22:03:10
```

#### 日志文件内容general log

```sql
11:00:59    14 Connect   root@localhost on
Query     /*!40100 SET @@SQL_MODE='' */
Query     /*!40103 SET TIME_ZONE='+00:00' */
Query     FLUSH /*!40101 LOCAL */ TABLES
Query     FLUSH TABLES WITH READ LOCK
Query     SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ
Query     START TRANSACTION /*!40100 WITH CONSISTENT SNAPSHOT */
Query     SHOW VARIABLES LIKE 'gtid\_mode'
Query     SHOW MASTER STATUS
Query     UNLOCK TABLES
Query     SELECT LOGFILE_GROUP_NAME, FILE_NAME, TOTAL_EXTENTS, INITIAL_SIZE, ENGINE, EXTRA FROM INFORMATION_SCHEMA.FILES WHERE FILE_TYPE = 'UNDO LOG' AND FILE_NAME IS NOT NULL GROUP BY LOGFILE_GROUP_NAME, FILE_NAME, ENGINE ORDER BY LOGFILE_GROUP_NAME
Query     SELECT DISTINCT TABLESPACE_NAME, FILE_NAME, LOGFILE_GROUP_NAME, EXTENT_SIZE, INITIAL_SIZE, ENGINE FROM INFORMATION_SCHEMA.FILES WHERE FILE_TYPE = 'DATAFILE' ORDER BY TABLESPACE_NAME, LOGFILE_GROUP_NAME
Query     SHOW DATABASES
Query     SHOW VARIABLES LIKE 'ndbinfo\_version'
```

##### FLUSH /*!40101 LOCAL */ TABLES

Closes all open tables, forces all tables in use to be closed, and flushes the query cache.

##### FLUSH TABLES WITH READ LOCK

执行flush tables操作，并加一个全局读锁，很多童鞋可能会好奇，这两个命令貌似是重复的，为什么不在第一次执行flush tables操作的时候加上锁呢？  

下面看看源码中的解释：

> We do first a FLUSH TABLES. If a long update is running, the FLUSH TABLES,will wait but will not stall the whole mysqld, and when the long update is done the FLUSH TABLES WITH READ LOCK will start and succeed quickly. So,FLUSH TABLES is to lower the probability of a stage where both mysqldump and most client connections are stalled. Of course, if a second long update starts between the two FLUSHes, we have that bad stall.

简而言之，是为了避免较长的事务操作造成FLUSH TABLES WITH READ LOCK操作迟迟得不到锁，但同时又阻塞了其它客户端操作。

##### SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ

设置当前会话的事务隔离等级为RR，RR可避免不可重复读和幻读。

##### START TRANSACTION /*!40100 WITH CONSISTENT SNAPSHOT */

1. 获取当前数据库的快照，这个是由mysqldump中--single-transaction决定的。
2. 这个只适用于支持事务的表，在MySQL中，只有Innodb。

> 注意：  
> > START TRANSACTION和START TRANSACTION WITH CONSISTENT SNAPSHOT并不一样，  
> > START TRANSACTION WITH CONSISTENT SNAPSHOT是开启事务的一致性快照。  

下面看看官方的说法，  

The WITH CONSISTENT SNAPSHOT modifier starts a consistent read for storage engines that are capable of it. This applies only to InnoDB. The effect is the same as issuing a START TRANSACTION followed by a SELECT from any InnoDB table.  

简而言之，就是开启事务并对所有表执行了一次SELECT操作，这样可保证备份时，在任意时间点执行select * from table得到的数据和执行START TRANSACTION WITH CONSISTENT SNAPSHOT时的数据一致。  

> 注意，WITH CONSISTENT SNAPSHOT只在RR隔离级别下有效。  

##### SHOW MASTER STATUS

这个是由--master-data决定的，记录了开始备份时，binlog的状态信息，包括MASTER_LOG_FILE和MASTER_LOG_POS

##### UNLOCK TABLES

释放锁。

#### 在服务器端执行相关sql，然后查看二进制日志的记录位置

```sql
mysql> insert into test1 values(1,2,3);#准备数据
ERROR 1136 (21S01): Column count doesn't match value count at row 1
mysql> insert into test1 values (1),(2),(3);
Query OK, 3 rows affected (0.01 sec)
Records: 3  Duplicates: 0  Warnings: 0

mysql> select * from test1;
+------+
| id   |
+------+
|    1 |
|    2 |
|    3 |
+------+
3 rows in set (0.00 sec)

#查看二进制的记录位置，从备份文件里边记录的位置到我们此时的位置，即为增量的部分
mysql> show master status;
+------------------+-----------+--------------+------------------+-------------------+
| File             | Position  | Binlog_Do_DB | Binlog_Ignore_DB | Executed_Gtid_Set |
+------------------+-----------+--------------+------------------+-------------------+
| mysql-bin.000016 | 168329475 |              |                  |                   |
+------------------+-----------+--------------+------------------+-------------------+
1 row in set (0.00 sec)
```

#### 增量备份

```bash
mysqlbinlog --no-defaults --start-position=143358362 --stop-position=168329475  /var/lib/mysql/mysql-bin.000016 > /var/lib/mysql/sunld_db_`date +%F_%H`.sql
```

```sql
mysql> set sql_log_bin=0;  关闭二进制日志
mysql> flush logs; 滚动下日志
```

#### 恢复数据

```bash
mysql -u root -p [dbname] < backup.sql
示例：
mysql -u root -p < backup.sql
[root@www ]# mysql < /var/lib/mysql/sunld_db_2017-12-19.sql  //导入完全备份文件
[root@www ]# mysql < /var/lib/mysql/sunld_db_2017-12-19_22.sql //导入增量备份文件
```

#### 注意事项

1. 真正在生产环境中，我们应该导出的是整个mysql服务器中的数据，而不是单个库，因此应该使用--all-databases
2. 在导出二进制日志的时候，可以直接复制文件即可，但是要注意的是，备份之前滚动下日志。

### 直接复制整个数据库目录

MySQL有一种非常简单的备份方法，就是将MySQL中的数据库文件直接复制出来。这是最简单，速度最快的方法。
不过在此之前，要先将服务器停止，这样才可以保证在复制期间数据库的数据不会发生变化。如果在复制数据库的过程中还有数据写入，就会造成数据不一致。这种情况在开发环境可以，但是在生产环境中很难允许备份服务器。
> 注意：这种方法不适用于InnoDB存储引擎的表，而对于MyISAM存储引擎的表很方便。同时，还原时MySQL的版本最好相同。

### 使用mysqlhotcopy工具快速备份

mysqlhotcopy支持不停止MySQL服务器备份。而且，mysqlhotcopy的备份方式比mysqldump快。mysqlhotcopy是一个perl脚本，主要在Linux系统下使用。其使用LOCK TABLES、FLUSH TABLES和cp来进行快速备份。
原理：先将需要备份的数据库加上一个读锁，然后用FLUSH TABLES将内存中的数据写回到硬盘上的数据库，最后，把需要备份的数据库文件复制到目标目录。  
命令格式如下：

```bash
mysqlhotcopy [option] dbname1 dbname2 backupDir/
dbname：数据库名称；
backupDir：备份到哪个文件夹下；
常用选项：
--help：查看mysqlhotcopy帮助；
--allowold：如果备份目录下存在相同的备份文件，将旧的备份文件加上_old；
--keepold：如果备份目录下存在相同的备份文件，不删除旧的备份文件，而是将旧的文件更名；
--flushlog：本次辈分之后，将对数据库的更新记录到日志中；
--noindices：只备份数据文件，不备份索引文件；
--user=用户名：用来指定用户名，可以用-u代替；
--password=密码：用来指定密码，可以用-p代替。使用-p时，密码与-p之间没有空格；
--port=端口号：用来指定访问端口，可以用-P代替；
--socket=socket文件：用来指定socket文件，可以用-S代替；
```

mysqlhotcopy并非mysql自带，需要安装Perl的数据库接口包；下载地址为:http://dev.mysql.com/downloads/dbi.html  

目前，该工具也仅仅能够备份MyISAM类型的表  

## 数据库恢复

### 还原直接复制目录的备份

通过这种方式还原时，必须保证两个MySQL数据库的版本号是相同的。MyISAM类型的表有效，对于InnoDB类型的表不可用，InnoDB表的表空间不能直接复制。

## 其他

### mysql查询表的数据大小

#### information_schema

在mysql中有一个information_schema数据库，这个数据库中装的是mysql的元数据，包括数据库信息、数据库中表的信息等。所以要想查询数据库占用磁盘的空间大小可以通过对information_schema数据库进行操作。
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
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;关键表&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;作用&nbsp;&nbsp;&nbsp;</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;schemata&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;这个表里面主要是存储在mysql中的所有的数据库的信息&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;tables&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;这个表里存储了所有数据库中的表的信息，包括每个表有多少个列等信息&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;columns&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;这个表存储了所有表中的表字段信息&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;statistics&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;存储了表中索引的信息&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;user_privileges&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;存储了用户的权限信息&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;schema_privileges&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;存储了数据库权限&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;table_privileges&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;存储了表的权限&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;column_privileges&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;存储了列的权限信息&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;character_sets&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;存储了mysql可以用的字符集的信息&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;collations&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;提供各个字符集的对照信息&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;collation_character_set_applicability&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;相当于collations表和character_sets表的前两个字段的一个对比，记录了字符集之间的对照信息。&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;table_constraints&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;这个表主要是用于记录表的描述存在约束的表和约束类型&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;key_column_usage&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;记录具有约束的列&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;routines&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;记录了存储过程和函数的信息，不包含自定义的过程或函数信息。&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;views&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;记录了视图信息，需要有show view权限&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;triggers&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;存储了触发器的信息，需要有super权限&nbsp;&nbsp;&nbsp;</td>
  </tr>
</tbody>
</table>

### 要查询表所占的容量，就是把表的数据和索引加起来就可以了

```sql
select sum(DATA_LENGTH)+sum(INDEX_LENGTH) from information_schema.tables  where table_schema='数据库名';
```

### 查询所有的数据大小

```sql
select concat(round(sum(DATA_LENGTH/1024/1024),2),'M') from tables; -- 查询所有的数据大小
```

### 查询某个表的数据

```sql
select concat(round(sum(DATA_LENGTH/1024/1024),2),'M') from tables where table_schema=’数据库名’ AND table_name=’表名’;
```

### mysqldump参数汇总

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
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;参数&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;说明&nbsp;&nbsp;&nbsp;</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--all-databases ,&nbsp;&nbsp;&nbsp;-A&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;导出全部数据库。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--all-tablespaces&nbsp;&nbsp;&nbsp;, -Y&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;导出全部表空间。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases --all-tablespaces&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--no-tablespaces ,&nbsp;&nbsp;&nbsp;-y&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;不导出任何表空间信息。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases --no-tablespaces&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--add-drop-database&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;每个数据库创建之前添加drop数据库语句。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases&nbsp;&nbsp;&nbsp;--add-drop-database&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--add-drop-table&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;每个数据表创建之前添加drop数据表语句。(默认为打开状态，使用--skip-add-drop-table取消选项)&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p&nbsp;&nbsp;&nbsp;--all-databases  (默认添加drop语句)&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p&nbsp;&nbsp;&nbsp;--all-databases --skip-add-drop-table  (取消drop语句)&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--add-locks&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;在每个表导出之前增加LOCK TABLES并且之后UNLOCK  TABLE。(默认为打开状态，使用--skip-add-locks取消选项)&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases  (默认添加LOCK语句)&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases --skip-add-locks   (取消LOCK语句)&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--allow-keywords&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;允许创建是关键词的列名字。这由表名前缀于每个列名做到。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases --allow-keywords&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--apply-slave-statements&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;在'CHANGE MASTER'前添加'STOP&nbsp;&nbsp;&nbsp;SLAVE'，并且在导出的最后添加'START SLAVE'。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases&nbsp;&nbsp;&nbsp;--apply-slave-statements&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--character-sets-dir&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;字符集文件的目录&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases &nbsp;&nbsp;&nbsp;--character-sets-dir=/usr/local/mysql/share/mysql/charsets&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--comments&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;附加注释信息。默认为打开，可以用--skip-comments取消&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p&nbsp;&nbsp;&nbsp;--all-databases  (默认记录注释)&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p&nbsp;&nbsp;&nbsp;--all-databases --skip-comments   (取消注释)&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--compatible&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;导出的数据将和其它数据库或旧版本的MySQL&nbsp;&nbsp;&nbsp;相兼容。值可以为ansi、mysql323、mysql40、postgresql、oracle、mssql、db2、maxdb、no_key_options、no_tables_options、no_field_options等，&nbsp;&nbsp;&nbsp;要使用几个值，用逗号将它们隔开。它并不保证能完全兼容，而是尽量兼容。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases --compatible=ansi&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--compact&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;导出更少的输出信息(用于调试)。去掉注释和头尾等结构。可以使用选项：--skip-add-drop-table  --skip-add-locks --skip-comments&nbsp;&nbsp;&nbsp;--skip-disable-keys&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases --compact&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--complete-insert,&nbsp;&nbsp;&nbsp;-c&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;使用完整的insert语句(包含列名称)。这么做能提高插入效率，但是可能会受到max_allowed_packet参数的影响而导致插入失败。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases --complete-insert&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--compress, -C&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;在客户端和服务器之间启用压缩传递所有信息&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases --compress&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--create-options,&nbsp;&nbsp;&nbsp;-a&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;在CREATE TABLE语句中包括所有MySQL特性选项。(默认为打开状态)&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--databases, -B&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;导出几个数据库。参数后面所有名字参量都被看作数据库名。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --databases test mysql&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--debug&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;输出debug信息，用于调试。默认值为：d:t:o,/tmp/mysqldump.trace&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases --debug&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases --debug=”&nbsp;&nbsp;&nbsp;d:t:o,/tmp/debug.trace”&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--debug-check&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;检查内存和打开文件使用说明并退出。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases --debug-check&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--debug-info&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;输出调试信息并退出&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases --debug-info&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--default-character-set&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;设置默认字符集，默认值为utf8&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases&nbsp;&nbsp;&nbsp;--default-character-set=latin1&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--delayed-insert&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;采用延时插入方式（INSERT DELAYED）导出数据&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases --delayed-insert&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--delete-master-logs&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;master备份后删除日志. 这个参数将自动激活--master-data。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases&nbsp;&nbsp;&nbsp;--delete-master-logs&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--disable-keys&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;对于每个表，用/*!40000 ALTER TABLE tbl_name&nbsp;&nbsp;&nbsp;DISABLE KEYS */;和/*!40000 ALTER TABLE tbl_name ENABLE KEYS&nbsp;&nbsp;&nbsp;*/;语句引用INSERT语句。这样可以更快地导入dump出来的文件，因为它是在插入所有行后创建索引的。该选项只适合MyISAM表，默认为打开状态。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--dump-slave&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;该选项将导致主的binlog位置和文件名追加到导出数据的文件中。设置为1时，将会以CHANGE&nbsp;&nbsp;&nbsp;MASTER命令输出到数据文件；设置为2时，在命令前增加说明信息。该选项将会打开--lock-all-tables，除非--single-transaction被指定。该选项会自动关闭--lock-tables选项。默认值为0。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases --dump-slave=1&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases --dump-slave=2&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--events, -E&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;导出事件。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases --events&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--extended-insert,&nbsp;&nbsp;&nbsp;-e&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;使用具有多个VALUES列的INSERT语法。这样使导出文件更小，并加速导入时的速度。默认为打开状态，使用--skip-extended-insert取消选项。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p&nbsp;&nbsp;&nbsp;--all-databases--skip-extended-insert  &nbsp;&nbsp;&nbsp;(取消选项)&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--fields-terminated-by&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;导出文件中忽略给定字段。与--tab选项一起使用，不能用于--databases和--all-databases选项&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p test test --tab=”/home/mysql”&nbsp;&nbsp;&nbsp;--fields-terminated-by=”#”&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--fields-enclosed-by&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;输出文件中的各个字段用给定字符包裹。与--tab选项一起使用，不能用于--databases和--all-databases选项&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p test test --tab=”/home/mysql”&nbsp;&nbsp;&nbsp;--fields-enclosed-by=”#”&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--fields-optionally-enclosed-by&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;输出文件中的各个字段用给定字符选择性包裹。与--tab选项一起使用，不能用于--databases和--all-databases选项&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p test test&nbsp;&nbsp;&nbsp;--tab=”/home/mysql” &nbsp;&nbsp;&nbsp;--fields-enclosed-by=”#” --fields-optionally-enclosed =”#”&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--fields-escaped-by&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;输出文件中的各个字段忽略给定字符。与--tab选项一起使用，不能用于--databases和--all-databases选项&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p mysql user --tab=”/home/mysql”&nbsp;&nbsp;&nbsp;--fields-escaped-by=”#”&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--flush-logs&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;开始导出之前刷新日志。&nbsp;&nbsp;&nbsp;请注意：假如一次导出多个数据库(使用选项--databases或者--all-databases)，将会逐个数据库刷新日志。除使用--lock-all-tables或者--master-data外。在这种情况下，日志将会被刷新一次，相应的所以表同时被锁定。因此，如果打算同时导出和刷新日志应该使用--lock-all-tables&nbsp;&nbsp;&nbsp;或者--master-data 和--flush-logs。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases --flush-logs&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--flush-privileges&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;在导出mysql数据库之后，发出一条FLUSH  PRIVILEGES&nbsp;&nbsp;&nbsp;语句。为了正确恢复，该选项应该用于导出mysql数据库和依赖mysql数据库数据的任何时候。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases&nbsp;&nbsp;&nbsp;--flush-privileges&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--force&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;在导出过程中忽略出现的SQL错误。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases --force&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--help&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;显示帮助信息并退出。&nbsp;&nbsp;&nbsp;mysqldump  --help&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--hex-blob&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;使用十六进制格式导出二进制字符串字段。如果有二进制数据就必须使用该选项。影响到的字段类型有BINARY、VARBINARY、BLOB。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --all-databases --hex-blob&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--host, -h&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;需要导出的主机信息&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--ignore-table&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;不导出指定表。指定忽略多个表时，需要重复多次，每次一个表。每个表必须同时指定数据库和表名。例如：--ignore-table=database.table1 --ignore-table=database.table2 ……&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--ignore-table=mysql.user&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--include-master-host-port&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;在--dump-slave产生的'CHANGE  MASTER&nbsp;&nbsp;&nbsp;TO..'语句中增加'MASTER_HOST=&lt;host&gt;，MASTER_PORT=&lt;port&gt;' &nbsp;&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--include-master-host-port&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--insert-ignore&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;在插入行时使用INSERT IGNORE语句.&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--insert-ignore&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--lines-terminated-by&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;输出文件的每行用给定字符串划分。与--tab选项一起使用，不能用于--databases和--all-databases选项。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost test test&nbsp;&nbsp;&nbsp;--tab=”/tmp/mysql” &nbsp;&nbsp;&nbsp;--lines-terminated-by=”##”&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--lock-all-tables,&nbsp;&nbsp;&nbsp;-x&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;提交请求锁定所有数据库中的所有表，以保证数据的一致性。这是一个全局读锁，并且自动关闭--single-transaction&nbsp;&nbsp;&nbsp;和--lock-tables 选项。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--lock-all-tables&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--lock-tables, -l&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;开始导出前，锁定所有表。用READ &nbsp;&nbsp;&nbsp;LOCAL锁定表以允许MyISAM表并行插入。对于支持事务的表例如InnoDB和BDB，--single-transaction是一个更好的选择，因为它根本不需要锁定表。&nbsp;&nbsp;&nbsp;请注意当导出多个数据库时，--lock-tables分别为每个数据库锁定表。因此，该选项不能保证导出文件中的表在数据库之间的逻辑一致性。不同数据库表的导出状态可以完全不同。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--lock-tables&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--log-error&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;附加警告和错误信息到给定文件&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost&nbsp;&nbsp;&nbsp;--all-databases &nbsp;&nbsp;&nbsp;--log-error=/tmp/mysqldump_error_log.err&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--master-data&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;该选项将binlog的位置和文件名追加到输出文件中。如果为1，将会输出CHANGE&nbsp;&nbsp;&nbsp;MASTER 命令；如果为2，输出的CHANGE &nbsp;&nbsp;&nbsp;MASTER命令前添加注释信息。该选项将打开--lock-all-tables&nbsp;&nbsp;&nbsp;选项，除非--single-transaction也被指定（在这种情况下，全局读锁在开始导出时获得很短的时间；其他内容参考下面的--single-transaction选项）。该选项自动关闭--lock-tables选项。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--master-data=1;&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--master-data=2;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--max_allowed_packet&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;服务器发送和接受的最大包长度。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--max_allowed_packet=10240&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--net_buffer_length&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;TCP/IP和socket连接的缓存大小。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--net_buffer_length=1024&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--no-autocommit&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;使用autocommit/commit 语句包裹表。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--no-autocommit&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--no-create-db, -n&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;只导出数据，而不添加CREATE DATABASE 语句。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--no-create-db&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--no-create-info,&nbsp;&nbsp;&nbsp;-t&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;只导出数据，而不添加CREATE TABLE 语句。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--no-create-info&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--no-data, -d&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;不导出任何数据，只导出数据库表结构。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--no-data&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--no-set-names, -N&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;等同于--skip-set-charset&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--no-set-names&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--opt&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;等同于--add-drop-table,  --add-locks, --create-options, --quick,&nbsp;&nbsp;&nbsp;--extended-insert, --lock-tables, &nbsp;&nbsp;&nbsp;--set-charset, --disable-keys 该选项默认开启, &nbsp;&nbsp;&nbsp;可以用--skip-opt禁用.&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--opt&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--order-by-primary&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;如果存在主键，或者第一个唯一键，对每个表的记录进行排序。在导出MyISAM表到InnoDB表时有效，但会使得导出工作花费很长时间。&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--order-by-primary&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--password, -p&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;连接数据库密码&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--pipe(windows系统可用)&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;使用命名管道连接mysql&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--pipe&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--port, -P&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;连接数据库端口号&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--protocol&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;使用的连接协议，包括：tcp, socket, pipe, memory.&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--protocol=tcp&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--quick, -q&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;不缓冲查询，直接导出到标准输出。默认为打开状态，使用--skip-quick取消该选项。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--skip-quick&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--quote-names,-Q&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;使用（`）引起表和列名。默认为打开状态，使用--skip-quote-names取消该选项。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--skip-quote-names&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--replace&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;使用REPLACE INTO 取代INSERT INTO.&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--replace&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--result-file, -r&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;直接输出到指定文件中。该选项应该用在使用回车换行对（\\r\\n）换行的系统上（例如：DOS，Windows）。该选项确保只有一行被使用。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--result-file=/tmp/mysqldump_result_file.txt&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--routines, -R&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;导出存储过程以及自定义函数。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--routines&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--set-charset&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;添加'SET NAMES &nbsp;&nbsp;&nbsp;default_character_set'到输出文件。默认为打开状态，使用--skip-set-charset关闭选项。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--skip-set-charset&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--single-transaction&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;该选项在导出数据之前提交一个BEGIN SQL语句，BEGIN&nbsp;&nbsp;&nbsp;不会阻塞任何应用程序且能保证导出时数据库的一致性状态。它只适用于多版本存储引擎，仅InnoDB。本选项和--lock-tables 选项是互斥的，因为LOCK &nbsp;&nbsp;&nbsp;TABLES 会使任何挂起的事务隐含提交。要想导出大表的话，应结合使用--quick 选项。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--single-transaction&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--dump-date&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;将导出时间添加到输出文件中。默认为打开状态，使用--skip-dump-date关闭选项。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--skip-dump-date&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--skip-opt&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;禁用–opt选项.&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--skip-opt&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--socket,-S&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;指定连接mysql的socket文件位置，默认路径/tmp/mysql.sock&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--socket=/tmp/mysqld.sock&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--tab,-T&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;为每个表在给定路径创建tab分割的文本文件。注意：仅仅用于mysqldump和mysqld服务器运行在相同机器上。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost test test&nbsp;&nbsp;&nbsp;--tab="/home/mysql"&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--tables&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;覆盖--databases (-B)参数，指定需要导出的表名。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --databases test&nbsp;&nbsp;&nbsp;--tables test&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--triggers&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;导出触发器。该选项默认启用，用--skip-triggers禁用它。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--triggers&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--tz-utc&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;在导出顶部设置时区TIME_ZONE='+00:00'&nbsp;&nbsp;&nbsp;，以保证在不同时区导出的TIMESTAMP 数据或者数据被移动其他时区时的正确性。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--tz-utc&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--user, -u&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;指定连接的用户名。&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--verbose, --v&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;输出多种平台信息。&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--version, -V&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;输出mysqldump版本信息并退出&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;-where, -w&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;只转储给定的WHERE条件选择的记录。请注意如果条件包含命令解释符专用空格或字符，一定要将条件引用起来。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--where=” user=’root’”&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--xml, -X&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;导出XML格式.&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--xml&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--plugin_dir&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;客户端插件的目录，用于兼容不同的插件版本。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--plugin_dir=”/usr/local/lib/plugin”&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;--default_auth&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;客户端插件默认使用权限。&nbsp;&nbsp;&nbsp;mysqldump  -uroot -p --host=localhost --all-databases&nbsp;&nbsp;&nbsp;--default-auth=”/usr/local/lib/plugin/&lt;PLUGIN&gt;”&nbsp;&nbsp;&nbsp;</td>
  </tr>
</tbody>
</table>

## 参考

1. [mysql备份的三种方式详解](https://www.jb51.net/article/41570.htm)
2. [MySQL备份与恢复](https://www.cnblogs.com/roverliang/p/6436140.html)
3. [mysqldump的实现原理](https://www.cnblogs.com/ivictor/p/5505307.html)
