<!-- TOC -->

- [优化服务器设置](#优化服务器设置)
  - [优化技巧](#优化技巧)
  - [Buffer Pool设置思路](#buffer-pool设置思路)
  - [InnoDB缓冲池（InnoDB Buffer Pool）](#innodb缓冲池innodb-buffer-pool)
    - [缓存内容](#缓存内容)
  - [线程缓存](#线程缓存)
  - [Table Cache](#table-cache)
  - [IO配置](#io配置)
  - [并发配置](#并发配置)

<!-- /TOC -->
# 优化服务器设置

## 优化技巧

1. 配置文件的快速查找
   ```bash
    /usr/libexec/mysqld --verbose --help|grep -A 1 'Default options'
    200815 16:01:04 [Note] Plugin 'FEEDBACK' is disabled.
    Default options are read from the following files in the given order:
    /etc/mysql/my.cnf /etc/my.cnf ~/.my.cnf
   ```
2. 配置项使用小写，单词之间使用横线或下划线分隔，并且使用统一的规范
3. 作用域：全局、会话、对象

## Buffer Pool设置思路

1. 从服务器内存开始，减去系统本身使用内存，减去其他服务使用内存（如果存在）
2. 减去MySQL本身使用到的内存（比如为每个查询操作分配的一些缓冲）
3. 减去足够让操作系统缓存InnoDB日志文件的内存，至少是足够缓存最近经常访问的部分；预留至少可以缓存二进制日志最后一部分以便用于主从复制
4. 减去其他配置的MySQL缓冲和缓存需要的内存（比如MyISAM的Key Cache或Query Cache）
5. 除以105%，向下取整

## InnoDB缓冲池（InnoDB Buffer Pool）

### 缓存内容

1. 索引
2. 行数据
3. 自适应哈希索引
4. Insert Buffer
5. 锁
6. 其他内部数据结构

## 线程缓存

1. 保存当前没有与连接关联但是准备为后面新的连接服务的线程。
2. 使用**thread_cache_size**进行设置。
3. 线程池中的每个线程或者休眠状态的线程占用内存大小为256KB。
4. 常用观察参数
   - Threads_cached：线程缓存内的线程的数量。
   - Threads_connected：当前打开的连接的数量。
   - Threads_created：创建用来处理连接的线程数。如果Threads_created较大，你可能要增加thread_cache_size值。缓存访问率的计算方法Threads_created/Connections。
   - Threads_running：激活的（非睡眠状态）线程数


## Table Cache

1. 表.frm文件的解析结果
2. 打开表的缓存：table_open_cache，每个线程独有（表.frm文件的解析结果）
3. 表定义缓存：table_definition_cache，全局
4. 参考opened_tables参数进行缓存配置的调整

## IO配置

## 并发配置

1. innodb_thread_concurrency：CPU数量 * 磁盘数量 * 2