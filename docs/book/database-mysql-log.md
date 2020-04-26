<!-- TOC -->

- [mysql日志详解](#mysql日志详解)
    - [Binlog日志](#binlog日志)
        - [作用](#作用)
            - [记录信息](#记录信息)
            - [特性](#特性)
        - [常用配置](#常用配置)
        - [格式](#格式)
        - [刷盘](#刷盘)
    - [事务日志](#事务日志)
        - [说明](#说明)
        - [原理](#原理)
        - [查看事务日志定义](#查看事务日志定义)
            - [参数说明](#参数说明)
                - [innodb_flush_log_at_trx_commit](#innodb_flush_log_at_trx_commit)
                - [innodb_mirrored_log_groups](#innodb_mirrored_log_groups)
            - [文件位置](#文件位置)
    - [慢查询日志](#慢查询日志)
    - [查询日志](#查询日志)
    - [错误日志](#错误日志)
    - [数据日志文件比对](#数据日志文件比对)
        - [ib_logfile与log-bin区别](#ib_logfile与log-bin区别)
        - [redo、undo、binlog的区别](#redoundobinlog的区别)
        - [ibdata](#ibdata)
            - [作用](#作用-1)
            - [导致该文件变大的原因](#导致该文件变大的原因)
            - [瘦身](#瘦身)
            - [扩容](#扩容)
                - [更多说明](#更多说明)
    - [其他](#其他)
    - [参考](#参考)

<!-- /TOC -->

# mysql日志详解

## Binlog日志

### 作用

#### 记录信息

1. 二进制日志记录 MySQL 数据库中所有与更新相关的操作，即二进制日志记录了所有的 DDL（数据定义语言）语句和 DML（数据操纵语言）语句，但是不包括数据查询语句。常用于恢复数据库和主从复制。
2. 二进制日志主要记录数据库的变化情况，因此可以用作主从库的同步。内容主要包括数据库所有的更新操作，use语句、insert语句、delete语句、update语句、create语句、alter语句、drop语句。用一句更简洁易懂的话概括就是：所有涉及数据变动的操作，都要记录进二进制日志中。

#### 特性

1. 只要重启了服务, binlog二进制日志就会自己滚动一个新的, 或者使用flush logs 手动滚动日志
2. 记录的信息 主要是记录修改数据或有可能引起数据改变的MySql语句, 记录时间,操作时长,等等信息
3. 日志格式: 基于(语句, row, mixed) 默认mixed
4. 每一个二进制日志叫做一个Binary log event(二进制日志事件), 每一个二进制日志事件都有自己的元数据(meta data)信息, 时间,操作时长….
5. 每个二进制日志的上限是1G

### 常用配置

<style type="text/css">
.tg  {border-collapse:collapse;border-spacing:0;}
.tg td{font-family:Arial, sans-serif;font-size:14px;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:black;}
.tg th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:black;}
.tg .tg-0lax{text-align:left;vertical-align:top}
</style>
<table class="tg">
  <tr>
    <th class="tg-0lax">参数</th>
    <th class="tg-0lax">说明</th>
  </tr>
  <tr>
    <td class="tg-0lax">sql_log_bin ={ON|OFF}</td>
    <td class="tg-0lax">用于控制会话级别二进制日志功能的开启或关闭。默认为ON，表示启用记录功能。用户可以在会话级别修改此变量的值，但其必须具有SUPER权限。</td>
  </tr>
  <tr>
    <td class="tg-0lax">binlog_cache_size =32768</td>
    <td class="tg-0lax">默认值32768 Binlog Cache用于在打开了二进制日志（binlog）记录功能的环境，是MySQL 用来提高binlog的记录效率而设计的一个用于短时间内临时缓存binlog数据的内存区域。一般来说，如果我们的数据库中没有什么大事务，写入也不是特别频繁，2MB～4MB是一个合适的选择。但是如果我们的数据库大事务较多，写入量比较大，可与适当调高binlog_cache_size。同时，我们可以通过binlog_cache_use 以及 binlog_cache_disk_use来分析设置的binlog_cache_size是否足够，是否有大量的binlog_cache由于内存大小不够而使用临时文件（binlog_cache_disk_use）来缓存了。</td>
  </tr>
  <tr>
    <td class="tg-0lax">binlog_stmt_cache_size= 32768</td>
    <td class="tg-0lax">当非事务语句使用二进制日志缓存，但是超出binlog_stmt_cache_size时，使用一个临时文件来存放这些语句。</td>
  </tr>
  <tr>
    <td class="tg-0lax">log_bin = mysql-bin</td>
    <td class="tg-0lax">指定binlog的位置，默认在数据目录下。</td>
  </tr>
  <tr>
    <td class="tg-0lax">binlog-format= {ROW|STATEMENT|MIXED}</td>
    <td class="tg-0lax">指定二进制日志的类型，默认为MIXED（row）。如果设定了二进制日志的格式，却没有启用二进制日志，则MySQL启动时会产生警告日志信息并记录于错误日志中</td>
  </tr>
  <tr>
    <td class="tg-0lax">sync_binlog = 10</td>
    <td class="tg-0lax">设定多久同步一次二进制日志至磁盘文件中，0表示不同步，任何正数值都表示对二进制每多少次写操作之后同步一次。当autocommit的值为1时，每条语句的执行都会引起二进制日志同步，否则，每个事务的提交会引起二进制日志同步</td>
  </tr>
  <tr>
    <td class="tg-0lax">max_binlog_cache_size= {4096 .. 18446744073709547520}</td>
    <td class="tg-0lax">二进制日志缓存空间大小，5.5.9及以后的版本仅应用于事务缓存，其上限由max_binlog_stmt_cache_size决定。</td>
  </tr>
  <tr>
    <td class="tg-0lax">max_binlog_stmt_cache_size= {4096 .. 18446744073709547520}</td>
    <td class="tg-0lax">二进制日志缓存空间大小，5.5.9及以后的版本仅应用于事务缓存</td>
  </tr>
  <tr>
    <td class="tg-0lax">expire_log_days ={0..99}</td>
    <td class="tg-0lax">设定二进制日志的过期天数，超出此天数的二进制日志文件将被自动删除。默认为0，表示不启用过期自动删除功能。如果启用此功能，自动删除工作通常发生在MySQL启动时或FLUSH日志时。</td>
  </tr>
  <tr>
    <td class="tg-0lax">log_slave_updates</td>
    <td class="tg-0lax">logslvaeupdates 用于主从复制</td>
  </tr>
</table>

### 格式

<style type="text/css">
.tg  {border-collapse:collapse;border-spacing:0;border-color:#bbb;}
.tg td{font-family:Arial, sans-serif;font-size:14px;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#bbb;color:#594F4F;background-color:#E0FFEB;}
.tg th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#bbb;color:#493F3F;background-color:#9DE0AD;}
.tg .tg-cly1{text-align:left;vertical-align:middle}
.tg .tg-0lax{text-align:left;vertical-align:top}
</style>
<table class="tg">
  <tr>
    <th class="tg-cly1">模式</th>
    <th class="tg-cly1">说明</th>
    <th class="tg-0lax">优点</th>
    <th class="tg-0lax">缺点</th>
  </tr>
  <tr>
    <td class="tg-cly1">STATEMENT模式（SBR）</td>
    <td class="tg-cly1">
        基于SQL语句的复制(statement-based replication, SBR)<br>
        每一条会修改数据的sql语句会记录到binlog中。
    </td>
    <td class="tg-0lax">
        不需要记录每一条sql语句和每一行的数据变化，减少了binlog日志量，节约IO，提高性能。<br>
        binlog中包含了所有数据库更改信息，可以据此来审核数据库的安全等情况。<br>
        binlog可以用于实时的还原，而不仅仅用于复制；主从版本可以不一样，从服务器版本可以比主服务器版本高
    </td>
    <td class="tg-0lax">
        在某些情况下会导致master-slave中的数据不一致(如sleep()函数， last_insert_id()，以及user-defined functions(udf)等会出现问题)<br>
        不是所有的UPDATE语句都能被复制，尤其是包含不确定操作的时候。<br>
        调用具有不确定因素的 UDF 时复制也可能出问题<br>
        INSERT ... SELECT 会产生比 RBR 更多的行级锁<br>
        使用以下函数的语句也无法被复制：<br>
        * LOAD_FILE()<br>
        * UUID()<br>
        * USER()<br>
        * FOUND_ROWS()<br>
        * SYSDATE() (除非启动时启用了 --sysdate-is-now 选项)<br>
        复制需要进行全表扫描(WHERE 语句中没有使用到索引)的 UPDATE 时，需要比 RBR 请求更多的行级锁<br>
        对于有 AUTO_INCREMENT 字段的 InnoDB表而言，INSERT 语句会阻塞其他 INSERT 语句<br>
        对于一些复杂的语句，在从服务器上的耗资源情况会更严重，而 RBR 模式下，只会对那个发生变化的记录产生影响<br>
        存储函数(不是存储过程)在被调用的同时也会执行一次 NOW() 函数，这个可以说是坏事也可能是好事<br>
        确定了的 UDF 也需要在从服务器上执行<br>
        数据表必须几乎和主服务器保持一致才行，否则可能会导致复制出错<br>
        执行复杂语句如果出错的话，会消耗更多资源
    </td>
  </tr>
  <tr>
    <td class="tg-cly1">ROW模式（RBR）</td>
    <td class="tg-cly1">基于行的复制(row-based replication, RBR)</td>
    <td class="tg-0lax">
        不记录每条sql语句的上下文信息，仅需记录哪条数据被修改了，修改成什么样了。而且不会出现某些特定情况下的存储过程、或function、或trigger的调用和触发无法被正确复制的问题。<br>
        任何情况都可以被复制，这对复制来说是最安全可靠的<br>
        和其他大多数数据库系统的复制技术一样<br>
        多数情况下，从服务器上的表如果有主键的话，复制就会快了很多<br>
        复制以下几种语句时的行锁更少：<br>
        * INSERT ... SELECT<br>
        * 包含 AUTO_INCREMENT 字段的 INSERT<br>
        * 没有附带条件或者并没有修改很多记录的 UPDATE 或 DELETE 语句<br>
        执行 INSERT，UPDATE，DELETE 语句时锁更少<br>
        从服务器上采用多线程来执行复制成为可能
    </td>
    <td class="tg-0lax">
        会产生大量的日志（binlog 大了很多），尤其是alter table的时候会让日志暴涨。<br>
        复杂的回滚时 binlog 中会包含大量的数据<br>
        主服务器上执行 UPDATE 语句时，所有发生变化的记录都会写到 binlog 中，而 SBR 只会写一次，这会导致频繁发生 binlog 的并发写问题<br>
        UDF 产生的大 BLOB 值会导致复制变慢<br>
        无法从 binlog 中看到都复制了写什么语句<br>
        当在非事务表上执行一段堆积的SQL语句时，最好采用 SBR 模式，否则很容易导致主从服务器的数据不一致情况发生<br>
        另外，针对系统库 mysql 里面的表发生变化时的处理规则如下：<br>
        如果是采用 INSERT，UPDATE，DELETE 直接操作表的情况，则日志格式根据 binlog_format 的设定而记录<br>
        如果是采用 GRANT，REVOKE，SET PASSWORD 等管理语句来做的话，那么无论如何都采用 SBR 模式记录<br>
        注：采用 RBR 模式后，能解决很多原先出现的主键重复问题。
    </td>
  </tr>
  <tr>
    <td class="tg-0lax">MIXED模式（MBR）</td>
    <td class="tg-0lax" colspan="3">
        混合模式复制(mixed-based replication, MBR)<br>
        一般的复制使用STATEMENT模式保存binlog，对于STATEMENT模式无法复制的操作使用ROW模式保存binlog，MySQL会根据执行的SQL语句选择日志保存方式。<br>
        对于执行的SQL语句中包含now()这样的时间函数，会在日志中产生对应的unix_timestamp()*1000的时间字符串，slave在完成同步时，取用的是sqlEvent发生的时间来保证数据的准确性。另外对于一些功能性函数slave能完成相应的数据同步，而对于上面指定的一些类似于UDF函数，导致Slave无法知晓的情况，则会采用ROW格式存储这些Binlog，以保证产生的Binlog可以供Slave完成数据同步。
    </td>
  </tr>
</table>

### 刷盘

<div align=center>

![1587543929052.png](..\images\1587543929052.png)

</div>

## 事务日志

MySQL会最大程度的利用缓存，从而提高数据的访问效率。那么换一句话来说，任何高性能的系统都必须利用到缓存，从各个层面来讲，缓存都发挥了巨大的作用。再上升到一个高度提炼一下：缓存和队列是实现高性能的必走之路。那么对于数据库来说这个却是个很棘手的问题，要保证数据更高效率的读取和存储，所以要利用到缓存。但是要保证数据的一致性，则必须保证所有的数据都必须准确无误的存储到数据库中，及时发生意外，也要保证数据可恢复。我们知道InnoDB是一个事务安全的存储引擎，而一致性是事务ACID中的一个重要特性。InnoDB存储引擎主要是通过InnoDB事务日志实现数据一致性的，InnoDB事务日志包括重做（redo、循环写入）日志，以及回滚（undo）日志。

### 说明

事务日志（InnoDB特有的日志）可以帮助提高事务的效率。
事物日志是innodb专用功能,这里只考虑innod存储引擎。
出于性能和故障恢复的考虑，MySQL 服务器不会立即执行事务，而是先将事务记录在日志里面，这样可以将随机IO转换成顺序IO，从而提高IO性能。
事物日志默认情况下会有两个文件，名称分别为ib_logfile0和ib_logfile1。当其中一个写满时，MySQL会将事务日志写入另一个日志文件(先清空原有内容)。当 MySQL 从崩溃中恢复时，会读取事务日志，将其中已经 commit 的事务写入数据库，没有 commit 的事务 rollback 。
在事物提交时，innodb是否将缓冲到文件中同步，只要提交则立刻同步，同时又不会保证每个语句都同步，因此性能不会有特别大的影响。
使用事务日志，存储引擎在修改表的数据时只需要修改其内存拷贝，再把该修改行为记录到持久在硬盘上的事务日志中，而不用每次都将修改的数据本身持久到磁盘。
事务日志采用追加的方式，因此写日志的操作是磁盘上一小块区域内的顺序I/O，而不像随机I/O需要在磁盘的多个地方移动磁头，所以采用事务日志的方式相对来说要快得多。
事务日志持久以后，内存中被修改的数据在后台可以慢慢的刷回到磁盘。目前大多数的存储引擎都是这样实现的，我们通常称之为预写式日志，修改数据需要写两次磁盘。
如果数据的修改已经记录到事务日志并持久化，但数据本身还没有写回磁盘，此时系统崩溃，存储引擎在重启时能够自动恢复这部分修改的数据。具有的恢复方式则视存储引擎而定。
事务日志是被轮转的,一启动就分配完毕了, 并且是连续的存储空间,默认每个文件的大小为5M
主要功能: 将随机I/O转换为顺序I/O
ib_logfile0, ib_logfile1, 这两个文件如果坏了那么对于mysql来说是致命的, 所以建议事务log使用raid
会把一些相关信息记录事务日志中(记录对数据文件数据修改的物理位置或叫做偏移量);
作用:在系统崩溃重启时，作事务重做；在系统正常时，每次checkpoint时间点，会将之前写入事务应用到数据文件中。 
有了 redo log，InnoDB 就可以保证即使数据库发生异常重启，之前提交的记录都不会丢失，这个能力称为 crash-safe。

### 原理

<div align=center>

![1587544132375.png](..\images\1587544132375.png)

</div>

<div align=center>

![redo log循环写入](..\images\1587544168998.png)

</div>

### 查看事务日志定义

```sql
show global variables like 'innodb_log%';
```

<div align=center>

![1587544274091.png](..\images\1587544274091.png)

</div>

#### 参数说明

```sql
mysql> show global variables like 'innodb_log%';
+-----------------------------+----------+
| Variable_name               | Value    |
+-----------------------------+----------+
# 定义内存空间的大小，万一都写在buffer里面，如果进程崩溃，也会丢失事物，
# 因此避免这种情况，一旦事物提交了，那么需要立即同步到磁盘中，而不是间断同步
# 事务日志缓存区,可设置1M~8M,默认8M,延迟事务日志写入磁盘,
# 启用大的事务日志缓存,可以将完整运行大事务日志，暂时存放在事务缓存区中,
# 不必(事务提交前)写入磁盘保存,同时也起到节约磁盘空间占用;
| innodb_log_buffer_size      | 16777216 |
| innodb_log_checksums        | ON       |
| innodb_log_compressed_pages | ON       |

# 控制事务日志ib_logfile的大小,范围5MB~4G；
# 所有事务日志ib_logfile0+ib_logfile1+..累加大小不能超过4G，
# 事务日志大，checkpoint会少,节省磁盘IO，
# 但是大的事务日志意味着数据库crash时，恢复起来较慢.
# 引入问题:修改该参数大小，导致ib_logfile文件的大小和之前存在的文件大小不匹配
# 解决方式：在干净关闭数据库情况下，删除ib_logfile，而后重启数据库，会自行创建该文件;
#每个日志的单位大小为5MB，如果有些大数据的话，则需要将其调大，
#否则恢复起来会比较慢，但是太大了也会导致恢复比较慢
| innodb_log_file_size        | 50331648 |

# 至少有两个
| innodb_log_files_in_group   | 2        |

# 定义innodb事务日志组的位置，
# 定义事物日志组的位置，一般来讲会有2个日志，
# 一个写满后会重建文件（达到轮询功能，写满后会同步到磁盘并将其清空）
# 一般来讲，日志文件大小是固定的，凡是mysql已启动日志空间会在磁盘上立即分配，
# 因为他们的主要功能是将随机IO转为顺序IO ，默认大小是每个文件为5MB，
# 明确说明事物日志的路径保存在./ 表示在当前路径下
| innodb_log_group_home_dir   | ./       |
| innodb_log_write_ahead_size | 8192     |
+-----------------------------+----------+
7 rows in set (0.00 sec)

```

##### innodb_flush_log_at_trx_commit

在事务提交时innodb是否同步日志从缓冲到文件中（1表示事务一提交就同步不提交每隔一秒同步一次，性能会很差造成大量的磁盘I/O；定义为2表示只有在事务提交时才会同步但是可能会丢失整个事务 ）
innodb_flush_log_at_trx_commit：控制事务日志何时写盘和刷盘，安全递增：0,2,1；事务缓存区:log_buffer;
如果innodb_flush_log_at_trx_commit设置为0，log buffer将每秒一次地写入log file中，并且log file的flush(刷到磁盘)操作同时进行.该模式下，在事务提交的时候，不会主动触发写入磁盘的操作。 
如果innodb_flush_log_at_trx_commit设置为1，每次事务提交时MySQL都会把log buffer的数据写入log file，并且flush(刷到磁盘)中去. （最安全）
如果innodb_flush_log_at_trx_commit设置为2，每次事务提交时MySQL都会把log buffer的数据写入log file.但是flush(刷到磁盘)操作并不会同时进行。该模式下,MySQL会每秒执行一次 flush(刷到磁盘)操作。
适用环境:

- 0:磁盘IO能力有限,安全方便较差,无复制或复制延迟可以接受，如日志性业务，mysql损坏丢失1s事务数据;
- 2:数据安全性有要求，可以丢失一点事务日志，复制延迟也可以接受，OS损坏时才可能丢失数据;
- 1:数据安全性要求非常高，且磁盘IO能力足够支持业务，如充值消费，敏感业务;

##### innodb_mirrored_log_groups

表示对日志组做镜像

#### 文件位置

<div align=center>

![1587544946386.png](..\images\1587544946386.png)

</div>

## 慢查询日志

## 查询日志

## 错误日志

## 数据日志文件比对

### ib_logfile与log-bin区别

<style type="text/css">
.tg  {border-collapse:collapse;border-spacing:0;}
.tg td{font-family:Arial, sans-serif;font-size:14px;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:black;}
.tg th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:black;}
.tg .tg-0lax{text-align:left;vertical-align:top}
</style>
<table class="tg">
  <tr>
    <td class="tg-0lax">ib_logfile</td>
    <td class="tg-0lax">
        记录系统的回滚，重做日志（在你修改数据之前，会先把 修改的操作 作为日志先记录下来）<br>
        记录文件的物理更改<br>
        记录的是redo log和undo log（应该记录在ibdata1中）的信息，这里记录的基本是commit之前的数据
    </td>
  </tr>
  <tr>
    <td class="tg-0lax">mysql-bin.******</td>
    <td class="tg-0lax">
        记录系统的所有更新记录，数据库的更细日志，记录的是逻辑更改<br>
        主从：mysql会把日志发送到slave，salve会接收日志，然后解析日志，把里面的sql语句重新应用到数据库里，于是就能同步数据了<br>
        记录的是已经执行完毕的对数据库的dml和ddl信息，这里记录的基本是commit之后的数据信息。
    </td>
  </tr>
</table>

### redo、undo、binlog的区别

<style type="text/css">
.tg  {border-collapse:collapse;border-spacing:0;}
.tg td{font-family:Arial, sans-serif;font-size:14px;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:black;}
.tg th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:black;}
.tg .tg-0lax{text-align:left;vertical-align:top}
</style>
<table class="tg">
  <tr>
    <th class="tg-0lax"></th>
    <th class="tg-0lax">redo</th>
    <th class="tg-0lax">undo</th>
    <th class="tg-0lax">binlog</th>
  </tr>
  <tr>
    <td class="tg-0lax">作用</td>
    <td class="tg-0lax">保持事务的持久性</td>
    <td class="tg-0lax">帮助事务回滚及MVCC的功能</td>
    <td class="tg-0lax">进行Point-In_Time的恢复及主从复制的建立</td>
  </tr>
  <tr>
    <td class="tg-0lax">产生主体</td>
    <td class="tg-0lax">InnoDB</td>
    <td class="tg-0lax">MySql</td>
    <td class="tg-0lax">MySql</td>
  </tr>
  <tr>
    <td class="tg-0lax">类型</td>
    <td class="tg-0lax">物理日志，只记录有关InnoDB引擎本身的事务日志。</td>
    <td class="tg-0lax">逻辑日志</td>
    <td class="tg-0lax">
        记录的都是关于一个事务的具体操作内容,逻辑日志。<br>
        记录所有与MySQL数据库有关的日志记录，包括InnoDB、MyISAM、Heap等其他存储引擎的日志。
    </td>
  </tr>

  <tr>
    <td class="tg-0lax">内容</td>
    <td class="tg-0lax">每个页的修改，重做日志主要是记录已经全部完成的事务,即执行了commit的日志，在默认情况下重做日志的值记录在iblogfile0 以及iblogfile1重做日志中。</td>
    <td class="tg-0lax">修改前的行数据，回滚日志主要记录已经部分完成并且写入硬盘的未完成事务，默认情况情况下，回滚日志的信息记录在表空间文件，共享表空间文件ibdata1或者独享表空间未见ibd中。</td>
    <td class="tg-0lax">执行的SQL语句</td>
  </tr>
  <tr>
    <td class="tg-0lax">每个事务的日志数量</td>
    <td class="tg-0lax">事务执行中不断写入，多事务可并发写入</td>
    <td class="tg-0lax">看修改的行数据量</td>
    <td class="tg-0lax">事务提交后记录一条SQL语句,根据配置执行</td>
  </tr>
  <tr>
    <td class="tg-0lax">幂等性</td>
    <td class="tg-0lax">是</td>
    <td class="tg-0lax"></td>
    <td class="tg-0lax">否</td>
  </tr>
  <tr>
    <td class="tg-0lax">日志文件</td>
    <td class="tg-0lax">ib_logfile</td>
    <td class="tg-0lax">Ibdata</td>
    <td class="tg-0lax">mysql-bin.******</td>
  </tr>
  <tr>
    <td class="tg-0lax">事务性</td>
    <td class="tg-0lax">INNODB存储引擎的重做日志，由于其记录是物理操作日志，因此每个事务对应多个日志条目，并且事务的重做日志写入是并发的，并非在事务提交时写入，做其在文件中记录的顺序并非是事务开始的顺序。</td>
    <td class="tg-0lax"></td>
    <td class="tg-0lax">二进制日志仅在事务提交时记录，并且对于每一个事务，仅在事务提交时记录，并且对于每一个事务，仅包含对应事务的一个日志。</td>
  </tr>
</table>

1. redo log 是 InnoDB 引擎特有的；binlog 是 MySQL 的 Server 层实现的，所有引擎都可以使用。
2. redo log 是物理日志，记录的是“在某个数据页上做了什么修改”；binlog 是逻辑日志，记录的是这个语句的原始逻辑，比如“给 ID=2 这一行的 c 字段加 1 ”。
3. redo log 是循环写的，空间固定会用完；binlog 是可以追加写入的。“追加写”是指 binlog 文件写到一定大小后会切换到下一个，并不会覆盖以前的日志（但是可以设置清理策略）。

### ibdata

#### 作用

ibdata1是一个用来构建innodb系统表空间的文件，这个文件包含了innodb表的
元数据（数据字典）、
撤销记录（FIL_PAGE_UNDO_LOG）********出现问题的可能性比较大、
修改buffer（innodb_ibuf_max_size 设置最大变更缓冲区
双写buffer（innodb_doublewrite_file 来将双写缓冲区存储到一个分离的文件）
如果file-per-table选项打开的话，该文件则不一定包含所有表的数据。当innodb_file_per_table选项打开的话，新创建表的数据和索引则不会存在系统表空间中，而是存放在各自表的.ibd文件中。
使用命令：SHOW ENGINE INNODB STATUS \g;查看当前二进制的状态。
显然这个文件会越来越大，innodb_autoextend_increment选项则指定了该文件每次自动增长的步进，默认是8M。

<div align=center>

![1587545996242.png](..\images\1587545996242.png)

</div>

#### 导致该文件变大的原因

显然ibdata文件存的是数据库的表数据，如缓存，索引等。所以随着数据库越来越大，表也会越大，这个无法避免的。

#### 瘦身

1. 按照类型分类
   - 数据文件与日志信息分文件保存，对于日志文件可以定期执行清理。
2. 或者定期数据备份和恢复
   - 先把数据库文件备份下来，然后直接删除ibdata文件，重新导入数据库文件即可。这样就可以把ibdata中的日志和缓存都删除掉了。

```sql
# 备份全部数据库，执行命令
mysqldump -q -uroot -ppassword --add-drop-table --all-databases >/var/lib/mysql/backall.sql
# 做完此步后，停止数据库服务。
service mysqld stop
# 在配置文件中设置如下参数
innodb_file_per_table=1
# 验证配置是否生效，可以重启mysql后,执行
service mysqld restart
# 登陆之后使用命令查询 show variables like '%per_table%';
# 还原数据库
mysql -uroot -p < /var/lib/mysql/backall.sql
```

#### 扩容

为了添加一个数据文件到表空间中，首先要关闭 MySQL 数据库，编辑 my.cnf 文件，确认innodb ibdata文件的实际情况和my.cnf的配置是否一致，这里有两种情况：

1. my.cnf的配置  
`innodb_data_file_path=ibdata1:10G;ibdata2:10G:autoextend`  
如果当前数据库正在使用ibdata1，或者使用ibdata2，但ibdata2没有超过10G，则对my.cnf配置直接改成：  
`innodb_data_file_path=ibdata1:10G;ibdata2:10G;ibdata3:10G:autoextend`
2. 如果设置了最后一个ibdata自动扩展时，有可能最后一个ibdata的占用空间大于my.cnf的配置空间。例如：

```sql
 mysql@test:/data1/mysqldata/innodb/data> ls -lh
-rw-rw---- 1 mysql mysql 10737418240 2010-01-26 16:34 ibdata1
-rw-rw---- 1 mysql mysql 16106127360 2010-01-26 16:34 ibdata2
```

这时，需要精确的计算ibdata2的大小 15360M，修改：

`innodb_data_file_path=ibdata1:10G;ibdata2:15360M;ibdata3:10G:autoextend`

重启mysql。

注意：  
1、扩容前注意磁盘空间是否足够。  
2、restart后关注是否生成了新的ibdata。

##### 更多说明

如果，最后一个文件以关键字 autoextend 来描述，那么编辑 my.cnf 的过程中，必须检查最后一个文件的尺寸，并使它向下接近于 1024 * 1024 bytes (= 1 MB) 的倍数（比方说现在autoextend 的/ibdata/ibdata1为18.5M，而在旧的my.ini中为10M，则需要修改为`innodb_data_file_path = /ibdata/ibdata1:19M;` 且必须是19M，如果指定20M，就会报错。），并在 innodb_data_file_path 中明确指定它的尺寸。然后你可以添加另一个数据文件。记住只有 innodb_data_file_path 中的最后一个文件可以被指定为 auto-extending。
一个例子：假设起先仅仅只有一个 auto-extending 数据文件 ibdata1 ，这个文件接近于 988 MB。下面是添加了另一个 auto-extending 数据文件后的可能示例 。

```sql
innodb_data_home_dir =
innodb_data_file_path = /ibdata/ibdata1:988M;/disk2/ibdata2:50M:autoextend
```

## 其他

## 参考
