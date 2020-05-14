
<!-- TOC -->

- [MYSQL日志整体概述](#mysql日志整体概述)
    - [简单介绍](#简单介绍)
        - [错误日志](#错误日志)
        - [通用查询日志](#通用查询日志)
        - [慢速查询日志](#慢速查询日志)
        - [更新日志](#更新日志)
        - [mysql日志缓存](#mysql日志缓存)
    - [日志相关配置参数](#日志相关配置参数)
    - [配置文件my.cnf参考](#配置文件mycnf参考)
    - [参考](#参考)

<!-- /TOC -->
# MYSQL日志整体概述

日志是mysql数据库的重要组成部分。日志文件中记录着mysql数据库运行期间发生的变化；也就是说用来记录mysql数据库的客户端连接状况、SQL语句的执行情况和错误信息等。当数据库遭到意外的损坏时，可以通过日志查看文件出错的原因，并且可以通过日志文件进行数据恢复。  

默认情况下，所有日志创建于mysqld数据目录中。通过刷新日志，你可以强制 mysqld来关闭和重新打开日志文件（或者在某些情况下切换到一个新的日志）。当你执行一个FLUSH LOGS语句或执行mysqladmin flush-logs或mysqladmin refresh时，出现日志刷新  

mysql日志分类：错误日志、查询日志、慢查询日志、事务日志、二进制日志；  

官网：https://dev.mysql.com/doc/refman/5.7/en/server-logs.html

## 简单介绍

### 错误日志

用--log-error[=file_name]选项来指定mysqld保存错误日志文件的位置。如果没有给定file_name值，mysqld使用错误日志名 host_name.err并在数据目录中写入日志文件。如果你执行FLUSH LOGS，错误日志用-old重新命名后缀并且mysqld创建一个新的空日志文件。(如果未给出--log-error选项，则不会重新命名）。
如果不指定--log-error，或者(在Windows中)如果你使用--console选项，错误被写入标准错误输出stderr。通常标准输出为你的终端。

### 通用查询日志

用--log[=file_name]或-l [file_name]选项启动它。如果没有给定file_name的值，默认名是host_name.log。

### 慢速查询日志

用--log-slow-queries[=file_name]选项启动时，mysqld 写一个包含所有执行时间超过long_query_time秒的SQL语句的日志文件.如果没有给出file_name值，默认未主机名，后缀为 -slow.log。如果给出了文件名，但不是绝对路径名，文件则写入数据目录。或者没有使用缩影的语句。

### 更新日志

用--log-update[=file_name]选项启动,不推荐使用

### mysql日志缓存

一个高速、稳定、可靠的系统，缓存在其中必定起着至关重要的作用。MySQL日志处理也使用了缓存机制。MySQL日志最初存放在MySQL服务器的内存中，若超过指定的存储容量，内存中的日志则写（或者刷新flush）到外存中，以数据库表或者以文件的方式永远的保存在硬盘中。

## 日志相关配置参数

```sql
mysql> show  variables like '%log%';
+-----------------------------------------+------------------------------------------+
| Variable_name                           | Value                                    |
+-----------------------------------------+------------------------------------------+
| back_log                                | 450                                      |

# 默认启动的时候二进制的大小
# 默认值32768 Binlog Cache用于在打开了二进制日志（binlog）记录功能的环境，
# 是MySQL用来提高binlog的记录效率而设计的一个用于短时间内临时缓存binlog数据的内存区域。
# 一般来说，如果我们的数据库中没有什么大事务，写入也不是特别频繁，2MB～4MB是一个合适的选择
# 但是如果我们的数据库大事务较多，写入量比较大，可与适当调高binlog_cache_size。
# 同时，我们可以通过binlog_cache_use以及 binlog_cache_disk_use来分析设置的
# binlog_cache_size是否足够，是否有大量的
# binlog_cache由于内存大小不够而使用临时文件（binlog_cache_disk_use）来缓存了
| binlog_cache_size                       | 32768

| binlog_checksum                         | CRC32                                    |
| binlog_direct_non_transactional_updates | OFF                                      |
| binlog_error_action                     | ABORT_SERVER                             |

# 指定二进制日志的类型，默认为MIXED。如果设定了二进制日志的格式，却没有启用二进制日志，
# 则MySQL启动时会产生警告日志信息并记录于错误日志中。
| binlog_format                           | ROW

| binlog_group_commit_sync_delay          | 0                                        |
| binlog_group_commit_sync_no_delay_count | 0                                        |
| binlog_gtid_simple_recovery             | ON                                       |
| binlog_max_flush_queue_time             | 0                                        |
| binlog_order_commits                    | ON                                       |
| binlog_row_image                        | FULL                                     |
| binlog_rows_query_log_events            | OFF                                      |

# 当非事务语句使用二进制日志缓存，但是超出binlog_stmt_cache_size时，
# 使用一个临时文件来存放这些语句。基于语句格式的二进制日志缓存的大小
| binlog_stmt_cache_size                  | 32768

# 设定二进制日志的过期天数，超出此天数的二进制日志文件将被自动删除。
# 默认为0，表示不启用过期自动删除功能。如果启用此功能，
#自动删除工作通常发生在MySQL启动时或FLUSH日志时。建议别设置,还是自己手动purge最好
| expire_logs_days                        | 0

# 定义查询日志是否开启
| general_log                             | OFF                                      |
# 定义查询日志的文件地址名称
| general_log_file                        | /var/lib/mysql/incloudos.log             |

| innodb_api_enable_binlog                | OFF                                      |
| innodb_flush_log_at_timeout             | 1                                        |
| innodb_flush_log_at_trx_commit          | 1                                        |
| innodb_locks_unsafe_for_binlog          | OFF                                      |

# 定义内存空间的大小，万一都写在buffer里面，如果进程崩溃，也会丢失事物，
# 因此避免这种情况，一旦事物提交了，那么需要立即同步到磁盘中，而不是间断同步
| innodb_log_buffer_size                  | 16777216
| innodb_log_checksums                    | ON                                       |
| innodb_log_compressed_pages             | ON                                       |

# 每个日志的单位大小为5MB，如果有些大数据的话，则需要将其调大，
# 否则恢复起来会比较慢，但是太大了也会导致恢复比较慢
| innodb_log_file_size                    | 50331648
# 设置了日志文件组中重做日志（redo）日志的数量
| innodb_log_files_in_group               | 2                                        |

# 定义事物日志组的位置，一般来讲会有2个日志，一个写满后会重建文件（达到轮询功能，写满后会同
# 步到磁盘并将其清空）一般来讲，日志文件大小是固定的，凡是mysql已启动日志空间会在磁盘上立即
# 分配，因为他们的主要功能是将随机IO转为顺序IO ，默认大小是每个文件为5MB，
# 明确说明事物日志的路径保存在./ 表示在当前路径下
| innodb_log_group_home_dir               | ./

| innodb_log_write_ahead_size             | 8192                                     |
| innodb_max_undo_log_size                | 1073741824                               |
| innodb_online_alter_log_max_size        | 134217728                                |
| innodb_undo_log_truncate                | OFF                                      |
# 设置回滚日志的回滚段大小，默认为128k
| innodb_undo_logs                        | 128                                      |

# 设置回滚日志存放的目录。
| innodb_undo_directory    | ./         |
# 设置了回滚日志由多少个回滚日志文件组成，默认为0.
# Warning 特别注意：安装MySQL后需要在my.cnf中设置回滚日志的参数，
# 如果创建数据库以后再设置回滚日志的参数，MySQL就会报错，并且回滚日志建好后，就不能再次修改或者增加。
| innodb_undo_tablespaces  | 0          |


# on|文件路径 是否启用二进制日志,on表示开启,文件路径表示指定自定义日志路径,
# 默认路径在datadir指定的路径下
| log_bin                                 | OFF|

| log_bin_basename                        |                                          |
| log_bin_index                           |                                          |
| log_bin_trust_function_creators         | OFF                                      |
| log_bin_use_v1_row_events               | OFF                                      |
| log_builtin_as_identified_by_password   | OFF                                      |
# 定义错误日志
| log_error                               | /var/lib/mysql/mysqld-error.log          |
| log_error_verbosity                     | 3                                        |
# 日志的输出的位置，对慢查询和普通查询有效
| log_output                              | FILE                                     |
| log_queries_not_using_indexes           | OFF                                      |
| log_slave_updates                       | ON                                       |
| log_slow_admin_statements               | OFF                                      |
| log_slow_slave_statements               | OFF                                      |
| log_statements_unsafe_for_binlog        | ON                                       |
| log_syslog                              | OFF                                      |
| log_syslog_facility                     | daemon                                   |
| log_syslog_include_pid                  | ON                                       |
| log_syslog_tag                          |                                          |
| log_throttle_queries_not_using_indexes  | 0                                        |
| log_timestamps                          | UTC                                      |
# 是否把警告信息写入错误日志中
| log_warnings                            | 2                                        |
# 二进定日志缓存空间大小，5.5.9及以后的版本仅应用于事务缓存，
# 其上限由max_binlog_stmt_cache_size决定。最大二进制缓存大小
| max_binlog_cache_size                   | 1844674407370954752                      |
# 最大二进制日志大小,默认1G,当达到这个值以后会自动滚动的
| max_binlog_size                         | 1073741824
# 二进定日志缓存空间大小，5.5.9及以后的版本仅应用于事务缓存
# 最大基于语句二进制缓存大小
| max_binlog_stmt_cache_size              | 18446744073709547520
# 最大中继日志大小
| max_relay_log_size                      | 0                                        |
# 中继日志
| relay_log                               |                                          |
| relay_log_basename                      | /var/lib/mysql/incloudos-relay-bin       |
| relay_log_index                         | /var/lib/mysql/incloudos-relay-bin.index |
# 中继日志存放的文件 
| relay_log_info_file                     | relay-log.info                           |
| relay_log_info_repository               | FILE                                     |
| relay_log_purge                         | ON                                       |
| relay_log_recovery                      | OFF                                      |
| relay_log_space_limit                   | 0                                        |

# 定义慢查询日志的
| slow_query_log                          | ON                                       |

# 输出方式为file（文件）时定义慢查询日志的位置
| slow_query_log_file                     | /var/lib/mysql/mysql-slow-query.log      |

# 用于控制会话级别二进制日志功能的开启或关闭。默认为ON，表示启用记录功能。
# 用户可以在会话级别修改此变量的值，但其必须具有SUPER权限。
| sql_log_bin                             | ON
| sql_log_off                             | OFF                                      |
# 设定多久同步一次二进制日志至磁盘文件中，0表示不同步，任何正数值都表示对二进制每多少次写操
# 作之后同步一次。当autocommit的值为1时，每条语句的执行都会引起二进制日志同步，
# 否则，每个事务的提交会引起二进制日志同步
| sync_binlog                             | 1
| sync_relay_log                          | 10000                                    |
| sync_relay_log_info                     | 10000                                    |
+-----------------------------------------+------------------------------------------+
72 rows in set (0.00 sec)
```

## 配置文件my.cnf参考

```sql
# Example MySQL config file for medium systems.
#
# This is for a system with little memory (32M - 64M) where MySQL plays
# an important part, or systems up to 128M where MySQL is used together with
# other programs (such as a web server)
#
# You can copy this file to
# /etc/my.cnf to set global options,
# mysql-data-dir/my.cnf to set server-specific options (in this
# installation this directory is /var/lib/mysql) or
# ~/.my.cnf to set user-specific options.
#
# In this file, you can use all long options that a program supports.
# If you want to know which options a program supports, run the program
# with the "--help" option.

# The following options will be passed to all MySQL clients
[client]
port                = 3306
socket                = /var/lib/mysql/mysql.sock
default-character-set = utf8

# Here follows entries for some specific programs

# The MySQL server
[mysqld]
event_scheduler=ON
skip-name-resolve
port                = 3306
# explicit_defaults_for_timestamp = true
socket                = /var/lib/mysql/mysql.sock
default-storage-engine = INNODB

sql_mode='STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION'

# 1> interactive_timeout针对交互式连接，wait_timeout针对非交互式连接。
# 所谓的交互式连接，即在mysql_real_connect()函数中使用了CLIENT_INTERACTIVE选项。
# 说得直白一点，通过mysql客户端连接数据库是交互式连接，通过jdbc连接数据库是非交互式连接。

#1. 控制连接最大空闲时长是wait_timeout参数。
#2. 对于非交互式连接，类似于jdbc连接，wait_timeout的值继承自服务器端全局变量wait_timeout。
#   对于交互式连接，类似于mysql客户单连接，wait_timeout的值继承自服务器端全局变量interactive_timeout。
#3. 判断一个连接的空闲时间，可通过show processlist输出中Sleep状态的时间


wait_timeout=1800
interactive_timeout=1800

#数据文件单独存放
innodb_file_per_table
max_connections = 2000

# 这个是定义mysql服务器端和客户端在一次传送数据包的过程当中数据包的大小
# 定义过大，8092，有可能服务器端太忙，来不及接收，或者网络太差，会容易造成丢包
# 定义过小，会因为客户端可能无法快速接收服务器端发过来的包，一般推荐是4096
# 用来控制其通信缓冲区的最大长度。
max_allowed_packet = 4M

sort_buffer_size = 512K
net_buffer_length = 8K
read_buffer_size = 256K
read_rnd_buffer_size = 512K
character_set_server=utf8
thread_cache_size=300
tmp_table_size=500M
max_heap_table_size=300M

#open slow query
slow-query-log=1
slow-query-log-file=/var/lib/mysql/mysql-slow-query.log
#set slow time to 1 second
long_query_time = 1

log-error=/var/lib/mysql/mysqld-error.log

local-infile=0

# binary logging is required for replication
####log-bin=mysql-bin

#master - slave syncronized setting
log_slave_updates=1

# required unique id between 1 and 2^32 - 1
# defaults to 1 if master-host is not set
# but will not function as a master if omitted
server-id        = 1

# Replication Slave (comment out master section to use this)
#
# To configure this host as a replication slave, you can choose between
# two methods :
#
# 1) Use the CHANGE MASTER TO command (fully described in our manual) -
#    the syntax is:
#
#    CHANGE MASTER TO MASTER_HOST=<host>, MASTER_PORT=<port>,
#    MASTER_USER=<user>, MASTER_PASSWORD=<password> ;
#
#    where you replace <host>, <user>, <password> by quoted strings and
#    <port> by the master's port number (3306 by default).
#
#    Example:
#
#    CHANGE MASTER TO MASTER_HOST='125.564.12.1', MASTER_PORT=3306,
#    MASTER_USER='joe', MASTER_PASSWORD='secret';
#
# OR
#
# 2) Set the variables below. However, in case you choose this method, then
#    start replication for the first time (even unsuccessfully, for example
#    if you mistyped the password in master-password and the slave fails to
#    connect), the slave will create a master.info file, and any later
#    change in this file to the variables' values below will be ignored and
#    overridden by the content of the master.info file, unless you shutdown
#    the slave server, delete master.info and restart the slaver server.
#    For that reason, you may want to leave the lines below untouched
#    (commented) and instead use CHANGE MASTER TO (see above)
#
# required unique id between 2 and 2^32 - 1
# (and different from the master)
# defaults to 2 if master-host is set
# but will not function as a slave if omitted
#server-id       = 2
#
# The replication master for this slave - required
#master-host     =   <hostname>
#
# The username the slave will use for authentication when connecting
# to the master - required
#master-user     =   <username>
#
# The password the slave will authenticate with when connecting to
# the master - required
#master-password =   <password>
#
# The port the master is listening on.
# optional - defaults to 3306
#master-port     =  <port>
#
# binary logging - not required for slaves, but recommended
#log-bin=mysql-bin

# Point the following paths to different dedicated disks
#tmpdir                = /tmp/                
#log-update         = /path-to-dedicated-directory/hostname

# Uncomment the following if you are using BDB tables
#bdb_cache_size = 4M
#bdb_max_lock = 10000

# Uncomment the following if you are using InnoDB tables
#innodb_data_home_dir = /var/lib/mysql/
#innodb_data_file_path = ibdata1:10M:autoextend
#innodb_log_group_home_dir = /var/lib/mysql/
#innodb_log_arch_dir = /var/lib/mysql/
# You can set .._buffer_pool_size up to 50 - 80 %
# of RAM but beware of setting memory usage too high
#innodb_buffer_pool_size = 16M
#innodb_additional_mem_pool_size = 2M
# Set .._log_file_size to 25 % of buffer pool size
#innodb_log_file_size = 5M
#innodb_log_buffer_size = 8M
#innodb_flush_log_at_trx_commit = 1
#innodb_lock_wait_timeout = 50

[mysqldump]
quick
max_allowed_packet = 16M

[mysql]
no-auto-rehash
# Remove the next comment character if you are not familiar with SQL
#safe-updates

#[isamchk]
#key_buffer = 20M
#sort_buffer_size = 20M
#read_buffer = 2M
#write_buffer = 2M

#[myisamchk]
#key_buffer = 20M
#sort_buffer_size = 20M
#read_buffer = 2M
#write_buffer = 2M

[mysqlhotcopy]
interactive-timeout
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
