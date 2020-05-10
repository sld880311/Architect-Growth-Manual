1.1	数据库事务 
事务(TRANSACTION)是作为单个逻辑工作单元执行的一系列操作，这些操作作为一个整体一起向系统提交，要么都执行、要么都不执行 。事务是一个不可分割的工作逻辑单元  事务必须具备以下四个属性，简称 ACID 属性： 
原子性（Atomicity） 
1.	事务是一个完整的操作。事务的各步操作是不可分的（原子的）；要么都执行，要么都不执行。 
一致性（Consistency） 
2.	当事务完成时，数据必须处于一致状态。 
隔离性（Isolation） 
3.	对数据进行修改的所有并发事务是彼此隔离的，这表明事务必须是独立的，它不应以任何方式依赖于或影响其他事务。 
永久性（Durability） 
4.	事务完成后，它对数据库的修改被永久保持，事务日志能够保持事务的永久性。 
1.1.1	隔离级别
1.1.1.1	可重复读
1.1.1.1.1	幻读
行级锁的说明
幻读产生的条件
幻读： 新插入的行，隔离级别是可重复读隔离级别，幻读在当前读才可出现
在可重读隔离级别下，普通的查询是快照读，是不会看到别的事务插入的数据的。因为，幻读在“当前读”下才会出现。
幻读指的是新插入的行不是修改的行

	gap lock 和next-key lock 在可重复读隔离级别下生效。
	加锁规则
	加锁的基本单位是（next-key lock）,他是前开后闭原则
	插叙过程中访问的对象会增加锁
	锁是加载扫描过的索引上的
	索引上的等值查询--给唯一索引加锁的时候，next-key lock升级为行锁
	索引上的等值查询--向右遍历时最后一个值不满足查询需求时，next-key lock 退化为间隙锁
	唯一索引上的范围查询会访问到不满足条件的第一个值为止
	gap lock 是开区间 next-key lock是前开后闭区间
	有索引字段、普通索引字段、唯一索引字段加锁格则是什么，会产生什么后果？
	有索引字段，被索引覆盖的时候锁只在索引上，
	无索引字段，会锁住整张表
	唯一索引，会退化为行锁
	limit 语句加锁格则：limit 语句查找的时候不会有向后遍历的操作，数据 1,5,5,10加锁范围是（1,5]就结束了，不会产生（5,10）的gap lock
	怎么查看表的锁定情况：
select * from information_schema.innodb_locks;
<div align=center>

![1589106794166.png](..\images\1589106794166.png)

</div>

	范围锁不遵循锁优化规则
	select 是什么读，什么情况需要gap lock 和next-key lock？
	select 是一致性读，gap lock next-key lock是为了当前读的幻读
	记录不存在的时候，产生什么锁？加锁范围
	记录不存在的时候查询也会产生间隙锁，例如 1,4,7,9，对4 update 开始是（1,4]，（4,7），插入3 就变成了 （1,3]，（3,4]，（4,7）
	读已提交的锁的特点：读已提交的时候会锁住行，当语句提交的时候就不释放“不满足条件的行”，不是在事务提交的时候才释放锁的
	最后踢出问题，问什么要有锁来防止幻读呢：因为MVCC的原则是
	InnoDB**值查找创建版本早于当前事务id或者等于事务版本号的，**这样确保事务读取的行，要m在事务开始之前已经存在，要么是当前事务插入的
	删除版本号未指定或者大于当前事务版本号，即查询的事务开启之后确保读取的行未删除----不是在此事务版本号之前删除的
1.1.1.1.1.1	准备数据
CREATE TABLE `t` (
	`id` INT ( 11 ) NOT NULL,
	`c` INT ( 11 ) DEFAULT NULL,
	`d` INT ( 11 ) DEFAULT NULL,
	PRIMARY KEY ( `id` ),
KEY `c` ( `c` ) 
) ENGINE = INNODB;

insert into t values(0,0,0),(5,5,5),
(10,10,10),(15,15,15),(20,20,20),(25,25,25);

1.1.1.1.1.2	等值查询间隙锁（唯一类索引）
	加锁单位是next-key lock
	等值查询，向右遍历到最后一个不满足的索引的时候会退化为间隙锁
表中数据
<div align=center>

![1589106831889.png](..\images\1589106831889.png)

</div>

执行步骤

<div align=center>

![1589106853976.png](..\images\1589106853976.png)

</div>

由于表中没有id=7的记录：
	原则1，加锁单位是next-key lock，sessionA加锁范围就是（5,10]；
	同时根据优化2，这是一个等值查询（id=7），而id=10不满足查询条件，next-key lock退化成间隙锁，因此加锁范围是（5,10）
<div align=center>

![1589106890949.png](..\images\1589106890949.png)

</div>

1.1.1.1.1.1	非唯一索引等值查询（普通索引）
表内数据
<div align=center>

![1589106924372.png](..\images\1589106924372.png)

</div>

执行流程
<div align=center>

![1589106945501.png](..\images\1589106945501.png)

</div>

<div align=center>

![1589106962255.png](..\images\1589106962255.png)

</div>



	原则1，加锁单位是next-key lock，因此会给（0,5]加上next-lock lock。
	要注意c是普通索引，因此仅访问c=5这一条记录是不能马上停下来的，需要向右遍历，查到c=10才放弃，根据原则2，访问到的都要加锁，因此要给（5,10]加next-key lock
	同时这个符合优化2：等值查询，向右遍历，最后一个不满足c=5这个等值条件，因此退化成间隙锁（5,10）
	根据原则2，访问到的对象才会加锁，这个查询使用索引覆盖，并不需要访问主键，所以主键上没有加任何锁，这就是为什么sessionB的update语句可以执行成功
	但是session C要插入一个（7,7,7）的记录就会被sessionA的间隙锁（5,10）锁住

需要注意
在这个例子中，lock in share mode 只锁覆盖索引，但是如果是 for update 就不一样了。 执行 for update 时，系统会认为你接下来要更新数据，因此会顺便给主键索引上满足条件的行加上行锁
这个例子说明，锁是加在索引上的；同时，它给我们的指导是，如果你要用 lock in share mode 来给行加读锁避免数据被更新的话，就必须得绕过覆盖索引的优化，在查询字段中加入索引中不存在的字段。比如，将 session A 的查询语句改成 select d from t where c=5 lock in share mode。（sessionB会阻塞）
1.1.1.1.1.1	主键索引范围查询
mysql> select * from t where id=10 for update;
mysql> select * from t where id>=10 and id<11 for update;
表内数据
<div align=center>

![1589106991007.png](..\images\1589106991007.png)

</div>

加锁过程
<div align=center>

![1589107012020.png](..\images\1589107012020.png)

</div>

	开始执行的时候，要找到第一个id=10的这行，因此next-key lock 是（5,10]。根据优化1，主键id上的等值条件，退化成行锁，只加了id=10这一行的行锁
	范围查找就往后继续找，找到id=15的这行停下来，因此需要加next-key lock（10,15]（等值查询才会退化成间隙锁）
	所以sessionA这时候锁的范围就是行锁id=10,和next-key lock（10,15] ,这样再看sessionB和session C
	这里注意一点，sessionA定位查找id=10的行进行的时候，是当做等值查询来判断的，向后扫描代15的时候，用的是范围判断。
1.1.1.1.1.1	非唯一索引范围锁
表内数据
<div align=center>

![1589107034266.png](..\images\1589107034266.png)

</div>

执行过程
<div align=center>

![1589107055616.png](..\images\1589107055616.png)

</div>

<div align=center>

![1589107070919.png](..\images\1589107070919.png)

</div>



	索引c>=10 所以加上了next-key lock（5,10]，向后扫描加上了next-key lock（10,15]
	此时session B 和session C被blocked了
1.1.1.1.1.1	唯一索引范围bug
表内数据
<div align=center>

![1589107092507.png](..\images\1589107092507.png)

</div>

执行步骤
<div align=center>

![1589107111743.png](..\images\1589107111743.png)

</div>

<div align=center>

![1589107127357.png](..\images\1589107127357.png)

</div>


	session A 是一个范围查询，按照规则1，应该索引id上只加（10,15]这个next-key lock，并且因为id是唯一键，所以循环到15这一行就应该停止。
	但实际上InnoDB 会往前扫描到第一个不满足条件的行为止，也就是 id=20。而且由于这是个范围扫描，因此索引 id 上的 (15,20] 这个 next-key lock 也会被锁上。
	所以你看到了，session B 要更新 id=20 这一行，是会被锁住的。同样地，session C 要插入 id=16 的一行，也会被锁住。
	照理说，这里锁住 id=20 这一行的行为，其实是没有必要的。因为扫描到 id=15，就可以确定不用往后再找了。但实现上还是这么做了。

1.1.1.1.1.1	非唯一索引上存在“等值"的例子
插入一条数据
mysql> insert into t values(30,10,30);
表内数据
<div align=center>

![1589107152152.png](..\images\1589107152152.png)

</div>

新插入的这一行c=10，也就说表里有两个c=10的行
<div align=center>

![1589107174764.png](..\images\1589107174764.png)

</div>


可以看到，虽然有两个 c=10，但是它们的主键值 id 是不同的（分别是 10 和 30），因此这两个 c=10 的记录之间，也是有间隙的。
执行过程
delete 语句加锁的逻辑，其实跟 select … for update 是类似的，也就是两个“原则”、两个“优化”和一个“bug”。
<div align=center>

![1589107195328.png](..\images\1589107195328.png)

</div>

<div align=center>

![1589107210910.png](..\images\1589107210910.png)

</div>

	删除和for update是一样的，产生了（5,10]的next-key lock和（10,15]的next转为（10,15）的间隙锁
主键被锁情况
<div align=center>

![1589107232806.png](..\images\1589107232806.png)

</div>



	可以看到 delete 和for update是一样的，c中有（5,10] （10,15）主键中锁的对应的是10 和30 的行锁
1.1.1.1.1.1	limit 语句加锁
表内数据
<div align=center>

![1589107257099.png](..\images\1589107257099.png)

</div>

执行流程
<div align=center>

![1589107277534.png](..\images\1589107277534.png)

</div>

<div align=center>

![1589107294170.png](..\images\1589107294170.png)

</div>


这个例子里，session A 的 delete 语句加了 limit 2。你知道表 t 里 c=10 的记录其实只有两条，因此加不加 limit 2，删除的效果都是一样的，但是加锁的效果却不同。
可以看到，session B 的 insert 语句执行通过了，跟案例六的结果不同。这是因为，案例七里的 delete 语句明确加了 limit 2 的限制，因此在遍历到 (c=10, id=30) 这一行之后，满足条件的语句已经有两条，循环就结束了。
因此，索引 c 上的加锁范围就变成了从（c=5,id=5) 到（c=10,id=30) 这个前开后闭区间，如下图所示：
<div align=center>

![1589107316877.png](..\images\1589107316877.png)

</div>


可以看到，(c=10,id=30）之后的这个间隙并没有在加锁范围里，因此 insert 语句插入 c=12 是可以执行成功的。
这个例子对我们实践的指导意义就是，在删除数据的时候尽量加 limit。这样不仅可以控制删除数据的条数，让操作更安全，还可以减小加锁的范围。
1.1.1.1.1.1	一个死锁的案例
前面的例子中，我们在分析的时候，是按照 next-key lock 的逻辑来分析的，因为这样分析比较方便。最后我们再看一个案例，目的是说明：next-key lock 实际上是间隙锁和行锁加起来的结果。
执行流程
<div align=center>

![1589107336315.png](..\images\1589107336315.png)

</div>


	sessionA 启动事务后执行查询语句加lock in share mode,在索引c上加了next-key lock（5,10]和间隙锁（10,15）
	sessionB的update语句也要在索引c上加next-key lock(5,10],进入锁等待
	然后sessionA要插入（8,8,8）这一行，被sessionB的间隙锁锁住。由于出现了死锁，InnoDB让sessionB回滚。
sessionB的next-key lock 不是还没申请成功吗？
其实是这样的，session B 的“加 next-key lock(5,10] ”操作，实际上分成了两步，先是加 (5,10) 的间隙锁，加锁成功；然后加 c=10 的行锁，这时候才被锁住的。
也就是说，我们在分析加锁规则的时候可以用 next-key lock 来分析。但是要知道，具体执行的时候，是要分成间隙锁和行锁两段来执行的。

1.1.1.1.1.1	不存在记录的加锁
建表&数据

CREATE TABLE `test` (
 `id` int(1) NOT NULL AUTO_INCREMENT,
 `name` varchar(8) DEFAULT NULL,
 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `test` VALUES ('1', '小罗');
INSERT INTO `test` VALUES ('5', '小黄');
INSERT INTO `test` VALUES ('7', '小明');
INSERT INTO `test` VALUES ('11', '小红');
执行流程
<div align=center>

![1589107363279.png](..\images\1589107363279.png)

</div>
<div align=center>

![1589107377317.png](..\images\1589107377317.png)

</div>


	对于不存在的记录查询的时候，会加锁，会产生记录锁和间隙锁，上图中的行锁是id=3 和gap （1，3),(3,3),(3,5],所以1,5不锁，2 ，4 锁定
	说明锁是动态的，当查询记录不存在的时候，会行锁和gap lock
1.1.1.1.1.1	读提交锁的特点
读提交隔离级别下还有一个优化，即：**语句执行过程中加上的行锁，在语句执行完成后，就要把“不满足条件的行”上的行锁直接释放了，不需要等到事务提交。也就是说，读提交隔离级别下，锁的范围更小，锁的时间更短，**这也是不少业务都默认使用读提交隔离级别的原因。

1.1.1.1.1.2	思考题
建表语句和数据

CREATE TABLE `t` (
  `id` int(11) NOT NULL,
  `c` int(11) DEFAULT NULL,
  `d` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `c` (`c`)
) ENGINE=InnoDB;

insert into t values(0,0,0),(5,5,5),
(10,10,10),(15,15,15),(20,20,20),(25,25,25);
<div align=center>

![1589107413235.png](..\images\1589107413235.png)

</div>




	由于是order by desc，第一个索引的是索引c上“最右边的”c=20的行，所以会加上间隙锁（20,25）和next-key lock（15,20]
	在索引c向左遍历，要扫描到c=10才停止下来，所以next-key lock会追加到[5,10]，这正是阻塞sessionB 的 insert 的语句的原因。
	在扫描过程中，c=20 、c=15、c=10 这三行都存在值（不存在的时候加next-key lock），由于是select * ，所以会在主键加上这三个行锁
因此：
session A 的select语句锁的范围就是：
索引c上的（5,25）
主键索引上id=10 id=15 id=20 三个行锁
1.1.1.1.1.1	有趣的例子
*表内数据
<div align=center>

![1589107435699.png](..\images\1589107435699.png)

</div>

执行流程
<div align=center>

![1589107455736.png](..\images\1589107455736.png)

</div>

分析过程
<div align=center>

![1589107485576.png](..\images\1589107485576.png)

</div>

<div align=center>

![1589107517990.png](..\images\1589107517990.png)

</div>

<div align=center>

![1589107524388.png](..\images\1589107524388.png)

</div>