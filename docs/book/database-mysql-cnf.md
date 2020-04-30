# my.cnf配置文件详解

下面是72GB内存生产环境中my.cnf配置文件，读者可以作为一个优化参考：

```sql
############################缓存配置整体说明############################################
## global_buffers 在内存中缓存从数据文件中检索出来的数据块，可以大大提高查询和更新数据的性能
## 计算公式： Innodb_buffer_pool_size +
##           Innodb_additional_mem_pool_size +
##           Innodb_log_buffer_size +
##           key_buffer_size +
##           query_cache_size
##
##
## per_thread_buffers 线程独享内存大小
## 计算公式（ 
##             read_buffer_size +
##             read_rnd_buffer_size +
##             sort_buffer_size +
##             thread_stack +
##             join_buffer_size +
##             binlog_cache_size ）* max_connections

## 注意：global_buffers + per_thread_buffers不能大于实际物理内存，
## 否者并发量大时会造成内存溢出、系统死机 ！
############################缓存配置整体说明############################################
# MySQL configuration for 72G memory
#定义客户端连接信息，端口号、socket 存放位置
[client]
port    = 3306
socket  = /tmp/mysql.sock

# The MySQL server
#########Basic##################
# Mysql 基本信息，端口号、socket、安装目录、数据存放目录、临时目录
[mysqld]

# Mysql Server 唯一标识，用来做主同同步（ 主从时开启 ）
server-id= 22
port     = 3306
user     = mysql
basedir  = /usr/local/mysql
datadir  = /mysqlData/data
tmpdir   = /mysqlData/tmp
socket   = /tmp/mysql.sock

# 避免Mysql外部锁定，减少出错几率、增强稳定性
skip-external-locking

# 禁止 Mysql对外部连接进行DNS解析，加快连接速度。开启后所有远程连接主机只能使用IP的方式
skip-name-resolve

lower_case_table_names = 1

# 禁止 SQL 读取本地文件
local-infile = 0

# 默认存储引擎
default-storage-engine = INNODB

# 默认字符集 utf8
character-set-server = utf8

# wait_timeout默认是8小时，也就是说一个connection空闲超过8个小时，Mysql将自动断开该connection
# 如果并发很高，默认设置会导致最大连接被占满，出现 "too many connections" 错误
# 如果这个值很低，可能会导致出现 "ERROR 2006 (HY000) MySQL server has gone away" 的错误，
#     出现这个错误还有可能是max_allowed_packet设置过小

# 数据库连接池如果没有配置正确，会导致连接失效，wait_timeout与interactive_timeout要同时设置
# 数据库连接池中需要配置的存活时间小于数据库中的时间，或者通过定期检查链接的状态

# 服务器关闭非交互连接之前等待活动的秒数（Mysql处理完一条连接后所等待释放的时间）。
wait_timeout = 100

# 服务器关闭交互式连接前等待活动的秒数。
interactive_timeout = 100

# mysql客户端在尝试与mysql服务器建立连接时，
# mysql服务器返回错误握手协议前等待客户端数据包的最大时限。默认10秒。
connect_timeout = 20

# 在MYSQL暂时停止响应新请求之前，短时间内的多少个请求可以被存在堆栈中
# Mysql 连接请求队列存放数，当某一时刻客户端连接请求过多，
#       造成 Mysql Server 无法为其建立连接时存放的数量，
#  最大 65535（ 增大时需同时调整 OS 级别的网络监听队列限制 ），默认50
back_log = 500

myisam_recover
event_scheduler = ON

#########general_log，一般不开启##

# 开启查询日志，一般选择不开启，因为查询日志记录很详细，会增大磁盘 IO 开销，影响性能
# general_log = on

# 查询日志存放位置及文件名
# general_log_file = /usr/local/mysql/data/mysql.log


#########binlog##################
# 开启 binlog ( 二进制 ) 日志，主要用来做增量备份跟主从同步
log-bin = /mysqlLog/logs/mysql-bin

# Mysql binlog 的日志格式，Statement、ROW 跟 Mixed（ 混合模式 ）
binlog_format = row

max_binlog_size = 28M

# 二进制日志缓冲大小，此参数是为每 Session 单独分配的
# 当一个线程开始一个事务时，Mysql就会为此Session分配一个binlog cache，
# 当这个事务提交时，binlog cache 中的数据被写入 binlog 文件
# 通过 show status like 'binlog_cache%'; 查看使用 binlog cache 的次数及使用磁盘的次数
binlog_cache_size = 2M

# 重要参数，不仅影响到 binlog 对 Mysql 所带来的性能损耗，还影响到 Mysql 中数据的完整性。
# 0：代表事务提交后，Mysql 不做 fsync 之类的磁盘同步指令刷新 binlog_cache 中的信息到磁盘
#    而让 Filesystem 自行决定什么时候同步，或者 cache 满了之后才同步磁盘。
#
# n：代表进行 n 次事务提交后，Mysql 将进行一次 fsync 之类的磁盘同步指令来将
#
#    binlog_cache 中的数据强制写入磁盘。
# 默认0 ，即不做任何强制性的磁盘刷新指令，性能最好，但是风险也最大。
#         当系统崩溃时 binlog_cache 中的所有 binlog 信息都会丢失。
#
# 1，是最安全但是性能损耗最大。当系统崩溃时，
#    最多丢失 binlog_cache 中未完成的一个事务，对实际数据没有实质性的影响。
sync_binlog = 0

# 保留5天的binlog日志，系统重启、执行 flush logs或binlog日志文件大小达到上限时删除 binlog日志
expire-logs-days = 5

#########replication#############
# mysql从复制连结等待读取数据的最大时限，默认3600秒。
slave-net-timeout                  = 10
rpl_semi_sync_master_enabled       = 1
rpl_semi_sync_master_wait_no_slave = 1
rpl_semi_sync_master_timeout       = 1000
rpl_semi_sync_slave_enabled        = 1
skip-slave-start
log_slave_updates                  = 1
relay_log_recovery                 = 1

#########slow log#############

# 开启慢查询日志，开启后将会记录执行时间超过 long_query_time 参数值的 SQL 语句
#（ 一般临时开启即可 ）
slow_query_log = 1

# 定义慢查询日志存放位置
slow_query_log_file = /mysqlLog/logs/mysql.slow

# 定义执行时间超过多少秒为慢查询，默认 10s
long_query_time = 2

#########error log#############
# 错误日志位置跟文件名
log-error  = /mysqlLog/logs/error.log

# 如果此参数不开启，error_log 中会有警告信息
explicit_defaults_for_timestamp = 1


#######per_thread_buffers############

# 查看最大连接数：show variables like '%max_connections%';
# 修改最大连接数：set GLOBAL max_connections = 200;（重启后失效）或则直接修改配置文件
# 默认100，最大值16384
# 使用命令show processlist; 可以查询相关（root所有，其他账号只显示自己相关的）的连接信息，
#   显示100条，使用show full processlist; 可列出所有
#
# Mysql 最大连接数，直接影响 Mysql 应用的并发处理能力
# 500~1000 是个比较合适的值，注意每建立的连接都会占用一定的内存空间，直到连接被关闭才释放内存
max_connections=1024

max_user_connections=1000

# 最大连接失败次数，跟性能没有太大关系，主要跟安全方面有关
# 设置每个主机的连接请求异常中断的最大次数，当超过该次数，MYSQL服务器将禁止host的连接请求，
# 直到mysql服务器重启或通过flush hosts命令清空此host的相关信息，默认100
max_connect_errors=10000

# 用来缓存 MyISAM 存储引擎的索引（ 默认 8M ，如果使用 Innodb 存储引擎，此值设为 64M 或更小 ）
# 计算公式：key_reads / key_read_requests * 100% 的值小于 0.1%
# 增加它可以得到更好的索引处理性能
key_buffer_size = 64M

# 网络传输中一次消息量的最大值，默认 4M ，必须设为 1024 的整倍数
max_allowed_packet = 128M

# 指示表调整缓冲区大小。
# table_cache 参数设置表高速缓存的数目。每个连接进来，都会至少打开一个表缓存。
# 因此， table_cache 的大小应与 max_connections 的设置有关。
# 例如，对于 200 个并行运行的连接，应该让表的缓存至少有 200 × N ，这里 N 是应用可以执行的查询的一个联接中表的最大数量。
# 此外，还需要为临时表和文件保留一些额外的文件描述符。
# 当 Mysql 访问一个表时，如果该表在缓存中已经被打开，则可以直接访问缓存；
# 如果还没有被缓存，但是在 Mysql 表缓冲区中还有空间，那么这个表就被打开并放入表缓冲区；
# 如果表缓存满了，则会按照一定的规则将当前未用的表释放，或者临时扩大表缓存来存放，使用表缓存的好处是可以更快速地访问表中的内容。
# 执行 flush tables 会清空缓存的内容。
# 一般来说，可以通过查看数据库运行峰值时间的状态值 Open_tables 和 Opened_tables ，判断是否需要增加 table_cache 的值
#（其中 open_tables 是当前打开的表的数量， Opened_tables 则是已经打开的表的数量）
# 即如果open_tables接近table_cache的时候，并且Opened_tables这个值在逐步增加，那就要考虑增加这个值的大小了。
# 还有就是Table_locks_waited比较高的时候，也需要增加table_cache。
table_cache = 3096

# 打开文件描述符的缓存个数，防止系统频繁打开、关闭描述符而浪费资源（ 对性能有影响，默认 2000 ）
table_open_cache = 6144

table_definition_cache = 4096

# 系统中对数据进行排序时使用的 buffer ，如果系统中排序比较大，且内存充足、并发不大时，可以适当增大此值（ 默认 256K ，此参数为每线程分配独立的 buffer ）
# Sort_Buffer_Size 是一个connection级参数，在每个connection（session）第一次需要使用这个buffer的时候，一次性分配设置的内存。
# Sort_Buffer_Size 并不是越大越好，由于是connection级的参数，过大的设置+高并发可能会耗尽系统内存资源。例如：500个连接将会消耗 500*sort_buffer_size(8M)=4G内存
# Sort_Buffer_Size 超过2KB的时候，就会使用mmap() 而不是 malloc() 来进行内存分配，导致效率降低。
# 技术导读 http://blog.webshuo.com/2011/02/16/mysql-sort_buffer_size/
# dev-doc: http://dev.mysql.com/doc/refman/5.5/en/server-parameters.html
# explain select*from table where order limit；出现filesort
# 属重点优化参数

sort_buffer_size = 512K

# Mysql 读入缓冲区大小，对表进行顺序扫描的请求将分配一个读入缓冲区，，MySql会为它分配一段内存缓冲区。
# Mysql 会为其分配一段内存缓冲区（ 默认 128K ，此参数为每线程分配 ）
# read_buffer_size变量控制这一缓冲区的大小。如果对表的顺序扫描请求非常频繁，
# 并且你认为频繁扫描进行得太慢，可以通过增加该变量值以及内存缓冲区大小提高其性能。
# 和sort_buffer_size一样，该参数对应的分配内存也是每个连接独享。

read_buffer_size = 512K

# Mysql 随机 Query 缓冲区大小，当按任意顺序读取行时，将分配一个随机读取缓冲区。如进行排序查询时，Mysql 会首先扫描该缓冲，避免磁盘搜索，提高查询速度（ 默认 256K ，该缓冲也是为每线程分配 ）
read_rnd_buffer_size = 512k

# join 为 ALL、index、rang 或 index_merge 时使用的 buffer（ 默认 256K ，每 Thread 都会建立自己独立的 buffer ）
join_buffer_size = 512K

# 临时表大小，默认大小是 32M
# 如果一张临时表超出该大小，MySQL产生一个 The table tbl_name is full 形式的错误，
# 如果你做很多高级 GROUP BY 查询，增加 tmp_table_size 值。如果超过该值，则会将临时表写入磁盘。
tmp_table_size = 64M
max_heap_table_size = 64M

# 是否启用 query_cache ，0 为不使用（ 若要关闭 query_cache 时，需同时将 query_cache_size 、query_cache_limit 设为 0 ）
query_cache_type=0


# 工作原理：一个SELECT查询在DB中工作后，DB会把该语句缓存下来，当同样的一个SQL再次来到DB里调用时，
# DB在该表没发生变化的情况下把结果从缓存中返回给Client。
# 这里有一个关建点，就是DB在利用Query_cache工作时，要求该语句涉及的表在这段时间内没有发生变更。
# 如果该表在发生变更时，Query_cache里的数据又怎么处理呢？首先要把Query_cache和该表相关的语句全部置为失效，然后在写入更新。
# 如果Query_cache非常大，该表的查询结构又比较多，查询语句失效也慢，一个更新或是Insert就会很慢，这样看到的就是Update或是Insert怎么这么慢了。
# 所以在数据库写入量或是更新量也比较大的系统，该参数不适合分配过大。而且在高并发，写入量大的系统，建议把该功能禁掉。
# 重点优化参数（主库 增删改-MyISAM）
# 查询缓冲大小，当重复查询时会直接从该缓冲中获取，但是当所查询表有改变时，缓冲的查询将失效（ 频繁写入、更新、高并发的环境下建议关闭此缓冲 ）

query_cache_size = 0

# 单个查询所能够使用的缓冲区大小，缺省为1M
query_cache_limit = 1M

# 使用全文索引最小长度
ft_min_word_len = 1

# 默认是4KB，设置值大对大数据查询有好处，但如果你的查询都是小数据查询，就容易造成内存碎片和浪费
# 查询缓存碎片率 = Qcache_free_blocks / Qcache_total_blocks * 100%
# 如果查询缓存碎片率超过20%，可以用FLUSH QUERY CACHE整理缓存碎片，或者试试减小query_cache_min_res_unit，如果你的查询都是小数据量的话。
# 查询缓存利用率 = (query_cache_size – Qcache_free_memory) / query_cache_size * 100%
# 查询缓存利用率在25%以下的话说明query_cache_size设置的过大，可适当减小;
# 查询缓存利用率在80%以上而且Qcache_lowmem_prunes > 50的话说明query_cache_size可能有点小，要不就是碎片太多。
# 查询缓存命中率 = (Qcache_hits – Qcache_inserts) / Qcache_hits * 100%
query_cache_min_res_unit = 2k

# 批量插入数据缓存大小，可以有效提高插入效率，默认为8M
bulk_insert_buffer_size = 32M

## Thread Cache 池中存放的连接线程数（ 此池中的线程不是启动服务时就创建的，而是随着连接线程的创建和使用，逐渐将用完的线程存入其中，达到此值后将不再缓存连接线程 ）
## 缓存命中率计算公式：Thread_Cache_Hit = ( Connections - Thread_created ) / Connections * 100%
## 系统运行一段时间后，Thread Cache 命中率应该保持在 90% 以上
# 服务器线程缓存这个值表示可以重新利用保存在缓存中线程的数量,
# 当断开连接时如果缓存中还有空间,那么客户端的线程将被放到缓存中,如果线程重新被请求，那么请求将从缓存中读取,
# 如果缓存中是空的或者是新的请求，那么这个线程将被重新创建,如果有很多新的线程，增加这个值可以改善系统性能.
# 通过比较 Connections 和 Threads_created 状态的变量，可以看到这个变量的作用。
# 设置规则如下：1GB 内存配置为8，2GB配置为16，3GB配置为32，4GB或更高内存，可配置更大。
thread_cache_size = 64

# 设置thread_concurrency的值的正确与否, 对mysql的性能影响很大, 
# 在多个cpu(或多核)的情况下，错误设置了thread_concurrency的值, 会导致mysql不能充分利用多cpu(或多核), 出现同一时刻只能一个cpu(或核)在工作的情况。
# thread_concurrency应设为CPU核数的2倍. 比如有一个双核的CPU, 那么thread_concurrency的应该为4; 2个双核的cpu, thread_concurrency的值应为8
# 属重点优化参数
thread_concurrency = 32

# 每线程的堆栈大小，默认值足够大，可满足普通操作。可设置范围为128K至4GB，默认为192KB。
thread_stack = 256K
######### InnoDB #############
innodb_data_home_dir = /mysqlData/data
innodb_log_group_home_dir = /mysqlLog/logs

# 指定一个大小为 2G 的、可扩展的 ibdata1 数据文件
innodb_data_file_path = ibdata1:2G:autoextend

# Innodb 存储引擎核心参数，用于缓存 Innodb 表的索引、数据（ 默认 128M ，单独使用 Innodb 存储引擎且单一 Mysql 服务时建议设为物理内存的 70% - 80 % ）
# 可以通过 show status like 'innodb_buffer_pool_%'; 来获取 innodb buffer pool 的实时状态信息
## Innodb_buffer_pool_pages_total 总共的 pages（ Innodb 存储引擎中所有数据存放最小物理单位 page ，每个 page 默认为 16KB ）
## Innodb_buffer_pool_pages_free  空闲的 pages
## Innodb_buffer_pool_pages_data  有数据的 pages
## Innodb_buffer_pool_read_requests  总共的 read 请求次数
## Innodb_buffer_pool_reads  读取物理磁盘读取数据的次数，即：在 buffer pool 中没有找到
## Innodb_buffer_pool_wait_free  因 buffer 空间不足而产生的 wait_free
## Innodb_buffer_pool_read_ahead_rnd  记录进行随机读的时候产生的预读次数
## Innodb_buffer_pool_read_ahead_seq  记录连续读的时候产生的预读次数
## Innodb_buffer_pool_size  使用率 = innodb_buffer_pool_pages_data / innodb_buffer_pool_pages_total * 100%
## Innodb_buffer_pool_read  命中率 = （ innodb_buffer_pool_read_requests - innodb_buffer_pool_reads ）/ innodb_buffer_pool_read_requests * 100%

innodb_buffer_pool_size = 50G

innodb_buffer_pool_instances = 8

# 使用系统自带的内存分配器，替代 innodb_additional_mem_pool_size 参数
# innodb_use_sys_malloc = 1

# 这个参数用来设置 InnoDB 存储的数据目录信息和其它内部数据结构的内存池大小，类似于Oracle的library cache。这不是一个强制参数，可以被突破。
innodb_additional_mem_pool_size = 16M

# 此参数确定数据日志文件的大小，以M为单位，更大的设置可以提高性能，但也会增加恢复故障数据库所需的时间，Redo log文件
innodb_log_file_size = 1024M

# 事务日志所使用的缓冲区。Innodb 在写事务日志时，为了提高写 Log 的 IO 性能，先将信息写入 Innodb Log Buffer 中，当满足 Innodb_flush_log_trx_commit 参数或日志缓冲区写满时，再将日志同步到磁盘中。
# 默认 8M ，一般设为 16~64M 即可，可以通过 show status like 'innodb_log%'; 查看状态
innodb_log_buffer_size = 64M
# 为提高性能，MySQL可以以循环方式将日志文件写到多个文件。推荐设置为3M
innodb_log_files_in_group = 3

# Innodb 事务日志刷新方式，
# 0 为每隔一秒 log thread 会将 log buffer 中的数据写入到文件，并通知文件系统进行文件同步 flush 操作，极端情况下会丢失一秒的数据
# 1 为每次事务结束都会触发 log thread 将 log buffer 中的数据写入文件并通知文件系统同步文件，数据最安全、不会丢失任何已经提交的数据
# 2 为每次事务结束后 log thread 会将数据写入事务日志
# 但只是调用了文件系统的文件写入操作，并没有同步到物理磁盘，因为文件系统都是有缓存机制的，各文件系统的缓存刷新机制不同
# 当设为 1 时是最为安全的，但性能也是最差的。
# 0 为每秒同步一次，性能相对高一些。
# 设为 2 性能是最好的，但故障后丢失数据也最多（ OS 跟主机硬件、供电足够安全可以选择，或对数据少量丢失可以接受 ）。
innodb
innodb_flush_log_at_trx_commit = 2

#InnoDB 有其内置的死锁检测机制，LOCK TABLES语句锁定设置，能导致未完成的事务回滚。
#如果使用其他事务引擎处理锁，就会导致内置检测失效，需要通过配置对应参数解决
#InnoDB事务在被回滚之前可以设置一个等待锁的超时秒数，默认为50s，
#表示事务等待获取资源等待的最长时间，超过这个时间还未分配到资源则会返回应用失败，回滚数据
#通过以下方式可以动态设置
#set innodb_lock_wait_timeout=100;   ---------session
#set global innodb_lock_wait_timeout=100;   ---------global
#注意global的修改对当前线程是不生效的，只有建立新的连接才生效。
innodb_lock_wait_timeout = 10

innodb_sync_spin_loops = 40
innodb_max_dirty_pages_pct = 90
innodb_support_xa = 1

# Innodb 线程并发数，0 为不限制，默认 0
# 服务器有几个CPU就设置为几，建议用默认设置，一般为8.
innodb_thread_concurrency = 0

innodb_thread_sleep_delay = 500

# 文件IO的线程数，一般为 4，但是在 Windows 下，可以设置得较大。
innodb_file_io_threads    = 4

innodb_concurrency_tickets = 1000
log_bin_trust_function_creators = 1
innodb_flush_method = O_DIRECT

# 0:关闭独享表空间，使用共享表空间
innodb_file_per_table

# Innodb 使用后台线程处理数据页上的 IO 请求，根据 CPU 核数修改，默认 4
innodb_read_io_threads = 16
innodb_write_io_threads = 16

innodb_io_capacity = 2000
innodb_file_format = Barracuda
innodb_purge_threads=1
innodb_purge_batch_size = 32
innodb_old_blocks_pct=75
innodb_change_buffering=all

# 事务隔离级别，为了有效保证并发读取数据的正确性（ 默认 Repeatables Read 即：可重复读 ）
# Innodb 有四种隔离级别：Read Uncommitted（ 未提交读 ）、Read Committed（ 已提交读 ）、
# Repeatable Read（ 可重复读 ）、Serializable（ 可序列化 ）

transaction_isolation = READ-COMMITTED

####快速预热 Buffer_pool 缓冲池##########################
# 当机器正常重启后，热数据还保留在内存中，避免瞬间连接数爆满导致机器死机
#  关闭数据库时把热数据 dump 到本地磁盘。
innodb_buffer_pool_dump_at_shutdown = 1

# 采用手工方式把热数据 dump 到本地磁盘。
innodb_buffer_pool_dump_now = 1

# 启动时把热数据加载到内存。
innodb_buffer_pool_load_at_startup = 1

# 采用手工方式把热数据加载到内存。
innodb_buffer_pool_load_now = 1

[mysqldump]
quick
# 在使用mysqldump备份数据时，服务器发送和接受的最大包长度
#使用 mysqldump 工具备份数据库时，当某张表过大时备份会报错，需要增大该值（ 增大到大于表大小的值 ）
# 查看备份文件大小
max_allowed_packet = 128M

# MyISAM表发生变化时重新排序所需的缓冲
myisam_sort_buffer_size = 128M 

# MySQL重建索引时所允许的最大临时文件的大小 (当 REPAIR, ALTER TABLE 或者 LOAD DATA INFILE).
# 如果文件大小比此值更大,索引会通过键值缓冲创建(更慢)
myisam_max_sort_file_size = 10G

myisam_max_extra_sort_file_size = 10G

# 如果一个表拥有超过一个索引, MyISAM 可以通过并行排序使用超过一个线程去修复他们.
# 这对于拥有多个CPU以及大量内存情况的用户,是一个很好的选择.
myisam_repair_threads = 1

# 自动检查和修复没有适当关闭的 MyISAM 表
myisam_recover   



[mysql]
no-auto-rehash

[myisamchk]
key_buffer_size = 64M
sort_buffer_size = 256k
read_buffer = 2M
write_buffer = 2M

[mysqlhotcopy]
interactive-timeout

[mysqld_safe]
open-files-limit = 28192
```
