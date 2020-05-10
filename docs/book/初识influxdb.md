1.1	初识InfluxDB
在上篇文章《时序数据库体系技术 – 时序数据存储模型设计》中笔者分别介绍了多种时序数据库在存储模型设计上的一些考虑，其中OpenTSDB基于HBase对维度值进行了全局字典编码优化，Druid采用列式存储并实现了Bitmap索引以及局部字典编码优化，InfluxDB和Beringei都将时间线挑了出来，大大降低了Tag的冗余。在这几种时序数据库中，InfluxDB无疑显的更加专业。接下来笔者将会针对InfluxDB的基本概念、内核实现等进行深入的分析。本篇文章先行介绍一些相关的基本概念。 
1.1.1	InfluxDB 数据模型
InfluxDB的数据模型和其他时序数据库有些许不同，下图是InfluxDB中的一张示意表： 
<div align=center>

![1589099669912.png](..\images\1589099669912.png)

</div>


1. Measurement：从原理上讲更像SQL中表的概念。这和其他很多时序数据库有些不同，其他时序数据库中Measurement可能与Metric等同，类似于下文讲到的Field，这点需要注意。 
2. Tags：维度列 
（1）上图中location和scientist分别是表中的两个Tag Key，其中location对应的维度值Tag Values为｛1, 2｝，scientist对应的维度值Tag Values为{langstroth, perpetual}，两者的组合TagSet有四种： 
location = 1 , scientist = langstroth
location = 1 , scientist = perpetual
location = 2 , scientist = langstroth
location = 2 , scientist = perpetual
（2）在InfluxDB中，表中Tags组合会被作为记录的主键，因此主键并不唯一，比如上表中第一行和第三行记录的主键都为’location=1,scientist=langstroth’。所有时序查询最终都会基于主键查询之后再经过时间戳过滤完成。 
3. Fields：数值列。数值列存放用户的时序数据。 
4. Point：类似SQL中一行记录，而并不是一个点。 
1.1.1	InfluxDB 核心概念 – Series
文章《时序数据库体系技术 – 时序数据存储模型设计》中提到时间线的概念，时序数据的时间线就是一个数据源采集的一个指标随着时间的流逝而源源不断地吐出数据，这样形成的一条数据线称之为时间线。如下图所示： 
<div align=center>

![1589099695175.png](..\images\1589099695175.png)

</div>





上图中有两个数据源，每个数据源会采集两种指标：butterflier和honeybees。InfluxDB中使用Series表示数据源，Series由Measurement和Tags组合而成，Tags组合用来唯一标识Measurement。Series是InfluxDB中最重要的概念，在接下来的内核分析中会经常用到。 
1.1.1	InfluxDB 系统架构
InfluxDB对数据的组织和其他数据库相比有很大的不同，为了更加清晰的说明，笔者按照自己的理解画了一张InfluxDB逻辑架构图： 

<div align=center>

![1589099719456.png](..\images\1589099719456.png)

</div>

1.1.1.1	DataBase
InfluxDB中有Database的概念，用户可以通过create database xxx来创建一个数据库。 
1.1.1.2	Retention Policy（RP）
数据保留策略。很长一段时间笔者对RP的理解都不足够充分，以为RP只规定了数据的过期时间。其实不然，RP在InfluxDB中是一个非常重要的概念，核心作用有3个：指定数据的过期时间，指定数据副本数量以及指定ShardGroup Duration。RP创建语句如下： 
CREATE RETENTION POLICY ON <retention_policy_name> ON <database_name> DURATION <duration> REPLICATION <n> [SHARD DURATION <duration> ] [DEFAULT]
其中retention_policy_name表示RP的名称，database_name表示数据库名称，duration表示TTL，n表示数据副本数。SHARD DURATION下文再讲。举个简单的栗子： 
CREATE RETENTION POLICY "one_day_only" ON "water_database" DURATION 1d REPLICATION 1 SHARD DURATION 1h DEFAULT 
InfluxDB中Retention Policy有这么几个性质和用法： 
1. RP是数据库级别而不是表级别的属性。这和很多数据库都不同。 
2. 每个数据库可以有多个数据保留策略，但只能有一个默认策略。 
3. 不同表可以根据保留策略规划在写入数据的时候指定RP进行写入，下面语句就指定six_mouth_rollup的rp进行写入： 
curl -X POST 'http://localhost:8086/write?db=mydb&rp=six_month_rollup' --data-binary 'disk_free,hostname=server01 value=442221834240i 1435362189575692182'
如果没有指定任何RP，则使用默认的RP。 
1.1.1.3	Shard Group
Shard Group是InfluxDB中一个重要的逻辑概念，从字面意思来看Shard Group会包含多个Shard，每个Shard Group只存储指定时间段的数据，不同Shard Group对应的时间段不会重合。比如2017年9月份的数据落在Shard Group0上，2017年10月份的数据落在Shard Group1上。 
每个Shard Group对应多长时间是通过Retention Policy中字段”SHARD DURATION”指定的，如果没有指定，也可以通过Retention Duration（数据过期时间）计算出来，两者的对应关系为： 
<div align=center>

![1589099752681.png](..\images\1589099752681.png)

</div>

问题来了，为什么需要将数据按照时间分成一个一个Shard Group？个人认为有两个原因： 
1. 将数据按照时间分割成小的粒度会使得数据过期实现非常简单，InfluxDB中数据过期删除的执行粒度就是Shard Group，系统会对每一个Shard Group判断是否过期，而不是一条一条记录判断。 
2. 实现了将数据按照时间分区的特性。将时序数据按照时间分区是时序数据库一个非常重要的特性，基本上所有时序数据查询操作都会带有时间的过滤条件，比如查询最近一小时或最近一天，数据分区可以有效根据时间维度选择部分目标分区，淘汰部分分区。 
1.1.1.1	Shard
Shard Group实现了数据分区，但是Shard Group只是一个逻辑概念，在它里面包含了大量Shard，Shard才是InfluxDB中真正存储数据以及提供读写服务的概念，类似于HBase中Region，Kudu中Tablet的概念。关于Shard，需要弄清楚两个方面： 
1. Shard是InfluxDB的存储引擎实现，具体称之为TSM(Time Sort Merge Tree) Engine，负责数据的编码存储、读写服务等。TSM类似于LSM，因此Shard和HBase Region一样包含Cache、WAL以及Data File等各个组件，也会有flush、compaction等这类数据操作。 
<div align=center>

![1589099781029.png](..\images\1589099781029.png)

</div>

2. Shard Group对数据按时间进行了分区，那落在一个Shard Group中的数据又是如何映射到哪个Shard上呢？ 
InfluxDB采用了Hash分区的方法将落到同一个Shard Group中的数据再次进行了一次分区。这里特别需要注意的是，InfluxDB是根据hash(Series)将时序数据映射到不同的Shard，而不是根据Measurement进行hash映射，这样会使得相同Series的数据肯定会存在同一个Shard中，但这样的映射策略会使得一个Shard中包含多个Measurement的数据，不像HBase中一个Region的数据肯定都属于同一张表。 
1.1.1	InfluxDB Sharding策略
上文已经对InfluxDB的Sharding策略进行了介绍，这里简单地做下总结。我们知道通常分布式数据库一般有两种Sharding策略：Range Sharding和Hash Sharding，前者对于基于主键的范围扫描比较高效，HBase以及TiDB都采用的这种Sharding策略；后者对于离散大规模写入以及随即读取相对比较友好，通常最简单的Hash策略是采用取模法，但取模法有个很大的弊病就是取模基础需要固定，一旦变化就需要数据重分布，当然可以采用更加复杂的一致性Hash策略来缓解数据重分布影响。 
InfluxDB的Sharding策略是典型的两层Sharding，上层使用Range Sharding，下层使用Hash Sharding。对于时序数据库来说，基于时间的Range Sharding是最合理的考虑，但如果仅仅使用Time Range Sharding，会存在一个很严重的问题，即写入会存在热点，基于Time Range Sharding的时序数据库写入必然会落到最新的Shard上，其他老Shard不会接收写入请求。对写入性能要求很高的时序数据库来说，热点写入肯定不是最优的方案。解决这个问题最自然的思路就是再使用Hash进行一次分区，我们知道基于Key的Hash分区方案可以通过散列很好地解决热点写入的问题，但同时会引入两个新问题： 
1. 导致Key Range Scan性能比较差。InfluxDB很优雅的解决了这个问题，上文笔者提到时序数据库基本上所有查询都是基于Series（数据源）来完成的，因此只要Hash分区是按照Series进行Hash就可以将相同Series的时序数据放在一起，这样Range Scan性能就可以得到保证。事实上InfluxDB正是这样实现的。 
2. Hash分区的个数必须固定，如果要改变Hash分区数会导致大量数据重分布。除非使用一致性Hash算法。笔者看到InfluxDB源码中Hash分区的个数固定是1，对此还不是很理解，如果哪位看官对此比较熟悉可以指导一二。 
1.1.2	总结
本篇文章重点介绍InfluxDB中一些基本概念，为后面分析InfluxDB内核实现奠定一个基础。文章主要介绍了三个重要模块： 
1. 首先介绍了InfluxDB中一些基本概念，包括Measurement、Tags、Fields以及Point。 
2. 接着介绍了Series这个非常非常重要的概念。 
3. 最后重点介绍了InfluxDB中数据的组织形式，总结起来就是：先按照RP划分，不同过期时间的数据划分到不同的RP，同一个RP下的数据再按照时间Range分区形成ShardGroup，同一个ShardGroup中的数据再按照Series进行Hash分区，将数据划分成更小粒度的管理单元。Shard是InfluxDB中实际工作者，是InfluxDB的存储引擎。下文会重点介绍Shard的工作原理。
