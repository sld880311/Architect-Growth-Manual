1.1	具体场景分析
1.1.1	Select执行流程


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

