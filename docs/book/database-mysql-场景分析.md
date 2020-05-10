1.1	具体场景分析
1.1.1	Select执行流程
1.1.2	Update执行流程
mysql> update T set c=c+1 where ID=2;
1.	执行器先找引擎取 ID=2 这一行。ID 是主键，引擎直接用树搜索找到这一行。如果 ID=2 这一行所在的数据页本来就在内存中，就直接返回给执行器；否则，需要先从磁盘读入内存，然后再返回。
2.	执行器拿到引擎给的行数据，把这个值加上 1，比如原来是 N，现在就是 N+1，得到新的一行数据，再调用引擎接口写入这行新数据。
3.	引擎将这行新数据更新到内存中，同时将这个更新操作记录到 redo log 里面，此时 redo log 处于 prepare 状态。然后告知执行器执行完成了，随时可以提交事务。
4.	执行器生成这个操作的 binlog，并把 binlog 写入磁盘。
5.	执行器调用引擎的提交事务接口，引擎把刚刚写入的 redo log 改成提交（commit）状态，更新完成。
<div align=center>

![1589106235941.png](..\images\1589106235941.png)

</div>

1.1.1	Order by执行流程
Explain中的Extra 这个字段中的“Using filesort”表示的就是需要排序，MySQL 会给每个线程分配一块内存用于排序，称为 sort_buffer。
1.1.1.1	参考语句
CREATE TABLE `t` (
  `id` int(11) NOT NULL,
  `city` varchar(16) NOT NULL,
  `name` varchar(16) NOT NULL,
  `age` int(11) NOT NULL,
  `addr` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `city` (`city`)
) ENGINE=InnoDB;
select city,name,age from t where city='杭州' order by name limit 1000  ;
1.1.1.2	全字段排序
<div align=center>

![1589106261487.png](..\images\1589106261487.png)

</div>

1.	初始化 sort_buffer，确定放入 name、city、age 这三个字段；
2.	从索引 city 找到第一个满足 city='杭州’条件的主键 id，也就是图中的 ID_X；
3.	到主键 id 索引取出整行，取 name、city、age 三个字段的值，存入 sort_buffer 中；
4.	从索引 city 取下一个记录的主键 id；
5.	重复步骤 3、4 直到 city 的值不满足查询条件为止，对应的主键 id 也就是图中的 ID_Y；
6.	对 sort_buffer 中的数据按照字段 name 做快速排序；
7.	按照排序结果取前 1000 行返回给客户端
1.1.1.1	Rowid排序
max_length_for_sort_data，是 MySQL 中专门控制用于排序的行数据的长度的一个参数。它的意思是，如果单行的长度超过这个值，MySQL 就认为单行太大，要换一个算法。

1.	初始化 sort_buffer，确定放入两个字段，即 name 和 id；
2.	从索引 city 找到第一个满足 city='杭州’条件的主键 id，也就是图中的 ID_X；
3.	到主键 id 索引取出整行，取 name、id 这两个字段，存入 sort_buffer 中；
4.	从索引 city 取下一个记录的主键 id；
5.	重复步骤 3、4 直到不满足 city='杭州’条件为止，也就是图中的 ID_Y；
6.	对 sort_buffer 中的数据按照字段 name 进行排序；
7.	遍历排序结果，取前 1000 行，并按照 id 的值回到原表中取出 city、name 和 age 三个字段返回给客户端。
<div align=center>

![1589106288813.png](..\images\1589106288813.png)

</div>

1.1.1.1	sort_buffer_size
sort_buffer_size参数决定了排序是在内存中完成还是通过磁盘临时文件完成。
1.1.1.2	优化方式
<div align=center>

![1589106320716.png](..\images\1589106320716.png)

</div>
<div align=center>

![1589106340644.png](..\images\1589106340644.png)

</div>


1.1.1	Count使用
1.1.1.1	语法
count() 是一个聚合函数，对于返回的结果集，一行行地判断，如果 count 函数的参数不是 NULL，累计值就加 1，否则不加。最后返回累计值。所以，count(*)、count(主键 id) 和 count(1) 都表示返回满足条件的结果集的总行数；而 count(字段），则表示返回满足条件的数据行里面，参数“字段”不为 NULL 的总个数。
1.1.1.2	count(主键 id) 
InnoDB 引擎会遍历整张表，把每一行的 id 值都取出来，返回给 server 层。server 层拿到 id 后，判断是不可能为空的，就按行累加。
1.1.1.3	count(1) 
InnoDB 引擎遍历整张表，但不取值。server 层对于返回的每一行，放一个数字“1”进去，判断是不可能为空的，按行累加。单看这两个用法的差别的话，count(1) 执行得要比 count(主键 id) 快。因为从引擎返回 id 会涉及到解析数据行，以及拷贝字段值的操作。
1.1.1.4	count(字段) 
如果这个“字段”是定义为 not null 的话，一行行地从记录里面读出这个字段，判断不能为 null，按行累加；
如果这个“字段”定义允许为 null，那么执行的时候，判断到有可能是 null，还要把值取出来再判断一下，不是 null 才累加。
1.1.1.5	count(*)
并不会把全部字段取出来，而是专门做了	优化，不取值。count(*) 肯定不是 null，按行累加。
<div align=center>

![1589106378967.png](..\images\1589106378967.png)

</div>