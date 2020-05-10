1.1	InfluxDB TSM存储引擎之TSMFile
为了保证时序数据写入的高效，InfluxDB采用LSM结构，数据先写入内存以及WAL，当内存容量达到一定阈值之后flush成文件，文件数超过一定阈值执行合并。这个套路与其他LSM系统（比如HBase）大同小异。不过，InfluxDB在LSM体系架构的基础上针对时序数据做了针对性的存储改进，官方称改进后的存储引擎为TSM（Time-Structured Merge Tree）结构引擎。本篇文章主要集中介绍TSM引擎中文件格式针对时序数据做了哪些针对性的改进，才使得InfluxDB在处理时序数据存储、读写方面表现的如此优秀。 
1.1.1	TSM引擎核心基石－时间线
InfluxDB在时序数据模型设计方面提出了一个非常重要的概念：SeriesKey。SeriesKey实际上就是measurement+datasource(tags)，在1.7之后包含了fieldKey。时序数据写入内存之后按照SeriesKey进行组织： 
<div align=center>

![1589099894766.png](..\images\1589099894766.png)

</div>


我们可以认为SeriesKey就是一个数据源，源源不断的产生时序数据，只要数据源还在，时序数据就会一直产生。举个简单的例子，SeriesKey可以认为是智能手环，智能手环可以有多个采集组件，比如说心跳采集器、脉搏采集器等，每个采集器好比上图中field1、field2和field3。心跳采集器可以不间断的采集用户的心跳信息，心跳信息就是紫色方框表示的时序数据序列。当然，智能手环非常之多，使用SeriesKey要唯一表示某个智能手环，就必须使用标签唯一刻画出该智能手环，所以SeriesKey设计为measurement+tags，其中measurement表示智能手环（类似于通常意义上的表），tags用多组维度来唯一表示该智能手环，比如用智能手环用户+智能手环型号两个标签来表示。 
1.1.1	TSM引擎工作原理－时序数据写入
InfluxDB在内存中使用一个Map来存储时间线数据，这个Map可以表示为<Key,List<Timestamp|Value>>。其中Key表示为seriesKey+fieldKey，Map中一个Key对应一个List，List中存储时间线数据。基于Map这样的数据结构，时序数据写入内存流程可以表示为如下三步： 
1. 时间序列数据进入系统之后首先根据measurement + datasource(tags)拼成seriesKey 
2. 根据这个seriesKey以及待查fieldKey拼成Key，再在Map中根据Key找到对应的时间序列集合，如果没有的话就新建一个新的List 
3. 找到之后将Timestamp|Value组合值追加写入时间线数据链表中 
1.1.2	TSM文件结构
每隔一段时间，内存中的时序数据就会执行flush操作将数据写入到文件（称为TSM文件），整个文件的组织和HBase中HFile基本相同。相同点主要在于两个方面： 
1. 数据都是以Block为最小读取单元存储在文件中 
2. 文件数据块都有相应的类B+树索引，而且数据块和索引结构存储在同一个文件中 
笔者参考InfluxDB最新代码按照自己的理解将文件结构表示为： 
<div align=center>

![1589099918997.png](..\images\1589099918997.png)

</div>
TSM文件最核心的由Series Data Section以及Series Index Section两个部分组成，其中前者表示存储时序数据的Block，而后者存储文件级别B+树索引Block，用于在文件中快速查询时间序列数据块。 
1.1.1.1	Series Data Block
上文说到时序数据在内存中表示为一个Map：<Key, List<Timestamp|Value>>， 其中Key = seriesKey + fieldKey。这个Map执行flush操作形成TSM文件。 
Map中一个Key对应一系列时序数据，因此能想到的最简单的flush策略是将这一系列时序数据在内存中构建成一个Block并持久化到文件。然而，有可能一个Key对应的时序数据非常之多，导致一个Block非常之大，超过Block大小阈值，因此在实际实现中有可能会将同一个Key对应的时序数据构建成多个连续的Block。但是，在任何时候，同一个Block中只会存储同一种Key的数据。 
另一个需要关注的点在于，Map会按照Key顺序排列并执行flush，这是构建索引的需求。Series Data Block文件结构如下图所示： 
<div align=center>

![1589099943171.png](..\images\1589099943171.png)

</div>


Series Data Block由四部分构成：Type、Length、Timestamps以及Values，分别表示意义如下： 
1. Type：表示该seriesKey对应的时间序列的数据类型，数值数据类型通常为int、long、float以及double等。不同的数据类型对应不同的编码方式。 
2. Length：len(Timestamps)，用于读取Timestamps区域数据，解析Block。 
时序数据的时间值以及指标值在一个Block内部是按照列式存储的：所有的时间值存储在一起，所有的指标值存储在一起。使用列式存储可以极大提高系统的压缩效率。 
3. Timestamps：时间值存储在一起形成的数据集，通常来说，时间序列中时间值的间隔都是比较固定的，比如每隔一秒钟采集一次的时间值间隔都是1s，这种具有固定间隔值的时间序列压缩非常高效，TSM采用了Facebook开源的Geringei系统中对时序时间的压缩算法：delta-delta编码。 
4. Values：指标值存储在一起形成的数据集，同一种Key对应的指标值数据类型都是相同的，由Type字段表征，相同类型的数据值可以很好的压缩，而且时序数据的特点决定了这些相邻时间序列的数据值基本都相差不大，因此也可以非常高效的压缩。需要注意的是，不同数据类型对应不同的编码算法。 
1.1.1.1	Series Index Block
很多时候用户需要根据Key查询某段时间（比如最近一小时）的时序数据，如果没有索引，就会需要将整个TSM文件加载到内存中才能一个Data Block一个Data Block查找，这样一方面非常占用内存，另一方面查询效率非常之低。为了在不占用太多内存的前提下提高查询效率，TSM文件引入了索引，其实TSM文件索引和HFile文件索引基本相同。TSM文件索引数据由一系列索引Block组成，每个索引Block的结构如下图所示： 
<div align=center>

![1589099966789.png](..\images\1589099966789.png)

</div>


Series Index Block由Index Block Meta以及一系列Index Entry构成： 
1. Index Block Meta最核心的字段是Key，表示这个索引Block内所有IndexEntry所索引的时序数据块都是该Key对应的时序数据。 
2. Index Entry表示一个索引字段，指向对应的Series Data Block。指向的Data Block由Offset唯一确定，Offset表示该Data Block在文件中的偏移量，Size表示指向的Data Block大小。Min Time和Max Time表示指向的Data Block中时序数据集合的最小时间以及最大时间，用户在根据时间范围查找时可以根据这两个字段进行过滤。 
1.1.1	TSM文件总体结构
<div align=center>

![1589099988201.png](..\images\1589099988201.png)

</div>



1.1.1	TSM引擎工作原理－时序数据读取
基于对TSM文件的了解，在一个文件内部根据Key查找一个某个时间范围的时序数据就会变得很简单，整个过程如下图所示： 
<div align=center>

![1589100010876.png](..\images\1589100010876.png)

</div>

上图中中间部分为索引层，TSM在启动之后就会将TSM文件的索引部分加载到内存，数据部分因为太大并不会直接加载到内存。用户查询可以分为三步： 
1. 首先根据Key找到对应的SeriesIndex Block，因为Key是有序的，所以可以使用二分查找来具体实现 
2. 找到SeriesIndex Block之后再根据查找的时间范围，使用[MinTime, MaxTime]索引定位到可能的Series Data Block列表 
3. 将满足条件的Series Data Block加载到内存中解压进一步使用二分查找算法查找即可找到 
1.1.1	文章总结
本文对InfluxDB的存储引擎TSM进行分析，主要介绍了TSM针对时序数据如何在内存中存储、在文件中存储，又如何根据文件索引实现在文件中查找时序数据。TSM存储引擎基于LSM存储引擎针对时序数据做了相应的优化，确实是一款相当专业的时序数据库！ 
1.1.2	参考文献
https://github.com/influxdata/influxdb/blob/master/tsdb/engine/tsm1/DESIGN.md 
https://docs.influxdata.com/influxdb/v1.3/concepts/key_concepts/ 
https://docs.influxdata.com/influxdb/v1.3/concepts/storage_engine/ 
http://blog.fatedier.com/2016/08/05/detailed-in-influxdb-tsm-storage-engine-one/ 
http://blog.fatedier.com/2016/08/15/detailed-in-influxdb-tsm-storage-engine-two/
