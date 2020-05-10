
1.1	InfluxDB多维查询之倒排索引
时序数据库的基础技术栈主要包括高吞吐写入实现、数据分级存储｜TTL、数据高压缩率、多维度查询能力以及高效聚合能力等，基于InfluxDB存储引擎TSM介绍了时序数据库的高性能写入能力以及基于列式存储的数据高压缩率实现。接下来分别基于InfluxDB系统的倒排索引实现以及Druid系统的Bitmap索引实现介绍时序数据库的多维度查询实现原理。 
InfluxDB系统TSM存储引擎个人认为有两个最核心的工作模块，其一是TSM针对时序数据在内存以及文件格式上做了针对性的优化，优雅地实现了时序数据的高效率写入以及高压缩率存储，同时文件级别的B+树索引可以有效提高时序数据根据SeriesKey查询时间序列的性能；其二是InfluxDB系统还实现了内存以及文件级别的倒排索引，有效实现了根据给定维度fieldKey查询对应SeriesKey的功能，这样再根据SeriesKey、fieldKey和时间间隔就可以在文件中查找到对应的时序数据集合。 
SeriesKey等于measurement+tags(datasources)，其中measurement表示一张时序数据表，tags（多组维度值）唯一确定了数据源。用户的查询通常有以下两种查询场景，以广告时序数据平台来说： 
1. 查看最近一小时某一个广告（数据源）总的点击量，典型的根据SereisKey、fieldKey(点击量)和时间范围查找时序数据，再做聚合（sum）。 
2. 统计最近一天网易考拉（指定广告商）发布在网易云音乐（指定广告平台）的所有广告总的点击量。这种统计查询并没有给出具体的广告（SeriesKey），仅指定了两个广告维度（广告商和广告平台）以及查询指标 – 点击量。这种查询就首先需要使用倒排索引根据measurement以及部分维度组合（广告商＝网易考拉，广告平台＝网易云音乐）找到所有对应的广告源，假如网易考拉在网易云音乐上发布了100个广告，就需要查找到这100个广告点击量对应的SeriesKey，再分别针对所有SeriesKey在最近一天这个时间范围查找点击量数据，最后做sum聚合。 
如何根据measurement以及部分维度组合查找到所有满足条件的SeriesKey？InfluxDB给出了倒排索引的实现，称之为TimeSeries Index，意为TimeSeries的索引，简称TSI。InfluxDB TSI在1.3版本之前仅支持Memory-Based Index实现，1.3之后又实现了Disk-Based Index实现。 
1.1.1	Memory-Based Index
Memory-Based Index方案将所有TimeSeries索引加载到内存提供服务，核心数据结构主要有： 
<div align=center>

![1589100101720.png](..\images\1589100101720.png)

</div>

其中seriesByTagKeyValue是一个双重map，即
map<tagkey, map<tagvalue, List<SeriesID>>>。以上文中广告商＝网易考拉为例来解释： 
tagkey为广告商，广告商可以有网易考拉，还可能有网易严选，所以一个广告商这个tagkey对应一个map。map的key是tagvalue，value是SeriesID集合。示例中tagvalue为网易考拉，映射的值为SeriesID集合。 
因此上文中第二种查询场景就可以通过下述步骤完成： 
1. 通过seriesByTagKeyValue这个内存结构以及给定的维度值广告商＝网易考拉找到所有包含该维度值的SeriesID集合 
2. 同样的方法，通过seriesByTagKeyValue以及给定的维度值广告平台＝网易云音乐找到包含该维度值的SeriesID集合 
3. 两个SeriesID集合再做交集就是同时满足广告商＝网易考拉，广告平台＝网易云音乐的所有SeriesID 
4. 再在SeriesByID – map<SeriesID, SeriesKey>中根据SeriesID集合映射查找到SeriesKey集合 
5. 最后根据SeriesKey集合以及时间范围找到所有满足条件的时序数据集合 
这里为什么使用SeriesID作为跳板找到SeriesKey，而不是直接映射得到SeriesKey？因为seriesByTagKeyValue这个结构中索引到的SeriesKey会有大量冗余，一个SeriesKey包含多少Tag组合，就会有多少份冗余。举个简单的例子： 假如现在有3个Tag组合形成一个seriesKey：measurement=mm,tagk1=tagv1,tagk2=tagv2,tagk3=tagv3。那么构造形成的双重Map结构seriesByTagKeyValue就会为： 
<TAGK1, <TAGV1, SERIESKEY>>
<TAGK2, <TAGV2, SERIESKEY>>
<TAGK3, <TAGV3, SERIESKEY>>
此时，假如用户想找tagk1=tagv1这个维度条件下的seriesKey，那第一个map就满足条件。很显然，这种场景下3个Tag组成的seriesKey，最终形成的seriesByTagKeyValue就会有3重seriesKey冗余。 因此使用Int类型的SeriesID对SeriesKey进行编码，将长长的SeriesKey编码成短短的SeriesID，可以有效减少索引在内存中的存储量。另外，SeriesID集中存储在一起可以使用Int集合编码有效压缩。 
Memory-Based Index实现方案好处是可以根据tag查找SeriesKey会非常高效，但是缺点也非常明显： 
1. 受限于内存大小，无法支持大量的TimeSeries。尤其对于某些基数非常大的维度，会产生大量的SeriesKey，使用Memory-Based Index并不合适。 
2. 一旦InfluxDB进程宕掉，需要扫描解析所有TSM文件并在内存中全量构建TSI结构，恢复时间会很长。 
1.1.1	Disk-Based Index
正因为Memory-Based Index存在如此重大的缺陷，InfluxDB 1.3之后实现了Disk-Based Index。Disk-Based Index方案会将索引持久化到磁盘，在使用时再加载到内存。InfluxDB官网对Disk-Based Index实现方案做了如下说明： 
<div align=center>

![1589100127242.png](..\images\1589100127242.png)

</div>


InfluxDB中倒排索引和时序数据使用了相同的存储机制 – LSM引擎。因此倒排索引也是先写入内存以及WAL，内存中达到一定阈值或者满足某些条件之后会执行持久化操作，将内存中的索引写入文件。当磁盘上文件数超过一定阈值会执行Compaction操作进行合并。实际实现中，时序数据点写入系统后会抽出Measurement、Tags并拼成SeriesKey，在系统中查看该SeriesKey是否已经存在，如果存在就忽略，否则写入内存中相应结构（参考log_file文件中变量InMemory Index）。接着内存中的数据会flush到文件（参考log_file文件中CompactTo方法），接下来笔者将会重点介绍TSI文件格式，如下图所示： 
<div align=center>

![1589100148961.png](..\images\1589100148961.png)

</div>


TSI文件主要由4个部分组成：Index File Trailer，Measurement Block，Tag Block以及Series Block。 
1. Index File Trailer主要记录Measurement Block、Tag Block以及Series Block在TSI文件中的偏移量以及数据大小。 
2. Measurement Block存储数据库中表的信息，通常来说Measurement不会太多，一个Block也就够了。 
3. Tag Block实际上是seriesByTagKeyValue这个双重map ：map<tagkey, map<tagvalue, List<SeriesID>>>在文件中的实际存储。 
4. Series Block存储了数据库中所有SeriesKey。 
1.1.1	Measurement Block
<div align=center>

![1589100174025.png](..\images\1589100174025.png)

</div>


Measurement Block存储数据库中所有时序数据表表名信息，Block主要由三部分组成：Block Trailer Section、Hash Index Section以及Measurement Entry Section。 
1. Block Trailer Section记录了Hash Index Section以及Measurement Data Section在文件中的偏移量以及数据大小，是Measurement Block读取解析的入口。 
2. Hash Index是一个Hash索引。实现机制很简单，就是一个Map结构 – map<measurement, offset>。使用Hash函数将给定measurement映射到数组的特定位置，将该特定数组位的值置为该measurement在文件中的实际偏移量。Hash Index主要有两个核心作用： 
（1）加快Measurement的查找效率。正常情况下在Block中查找某个Measurement Entry只能依次遍历查找，或者二分查找，而使用Hash索引可以直接在o(1)复杂度找到待查Measurement。 
（2）减小内存开销。如果没有Hash Index，在Measurement Block中查找一个Measurement Entry，需要将该Block全部加载到内存再查找。Measurement Block本身大小不特定，有可能很大，也可能很小，一旦Block很大的话内存开销会非常之大。而使用Hash Index的话，只需要将Hash Index加载到内存，根据Hash Index定位到Measurement Entry具体的offset，直接根据偏移量加载具体的待查找measurement。 
3. Measuremen是具体的时序数据表，比如广告信息表等。Measurement是一个复合结构，由一系列字段组成，其中name表示指标名，TagBlock offset以及TagBlock size表示该Measurement所对应的TagBlock在索引文件中的偏移量以及大小。因此可以使用Measurement过滤掉大量不属于该Measurement的Tags。 
1.1.1.1	Tag Block
<div align=center>

![1589100206802.png](..\images\1589100206802.png)

</div>


TagBlock中存储同一个Measurement下的Tags。Tag Block由三部分组成：Block Trailer、Tag Key Section以及Tag Value Section： 
1. Block Trailer：存储Tag Key Hash Index的offset以及size，TagKey Section的offset以及size，TagValue Section的offset以及size。通过解析Trailer，可以快速找到Block中各个部分的解析入口。 
2. Tag Key Section：存储指定Measurement下所有维度名信息，比如广告时序数据有publisher、advertiser、gender、country等维度。每个Tag Key由多个字段组成，是一个复合结构，如下图所示： 
<div align=center>

![1589100229618.png](..\images\1589100229618.png)

</div>


其中key字段表示维度名，TagValue相关字段（TagValue.offset、TagValue.size，…）表示该维度下所有维度值在文件中的存储区域。 
3. Tag Value Section：存储某个维度下的所有维度值。比如广告时序数据中advertiser这个维度可能有多个值，比如google.com、baidu.com、163music.com等等一系列值，所有这些值会集中存储在一起，这个区域就是advertiser维度对应的Tag Value Section。同理，其他维度诸如publisher、gender、country等都会有对应的Tag Value Section。Tag Value Section中每个Tag Value也是一个复合结构，如下图所示： 

<div align=center>

![1589100253999.png](..\images\1589100253999.png)

</div>


其中value字段和series.data两个字段是需要重点关注的两个字段。前者表示具体的维度值，后者表示这个维度值对应的一系列SeriesKey。存储的时候并没有直接存储SeriesKey，而是存储SeriesID。
关于Tag Block，笔者在思考的时候一直在思考两个问题： 
1. Tag Block中每个数据Section都有对应的Hash Index，用来加速查找。但是有没有注意到Hash Index只能实现等值查找加速，但是不能实现范围查找，比如大于、小于条件查找。假如现在用户想要根据维度advertiser=163music.com查找对应的所有seriesKey，可以很容易： 
（1）在Tag Key Section的Hash Index一下子就找到对应Tag Key（advertiser）在文件中的offset 
（2）再从文件中加载出Tag Key，解析出advertiser对应的Tag Value Section在文件中的offset 
（3）根据Tag Vlaue Section在文件中的offset加载出Tag Value Section对应的Hash Index，使用163music.com在Hash Index中就可以一下子找到对应的Tag Value的offset 
（4）根据offset加载出Tag Value对应的series.data，即对应的一系列SeriesID，即一系列SeriesKey 
但是，如果用户想查询advertiser>163music.com对应的所有seriesKey，怎么玩？很显然，只根据Hash Index是玩不转的（有一种结构可以玩的转 – B+树，上篇文章有提到过），这里教大家一招，如果能够保证数据（Tag Value Section中Tag Value有序存储）的有序，就可以玩的转了。也就是说，Hash Index + 有序就可以实现B+树可以实现的快速范围查找。这一招很有用！ 
2. 根据SeriesID如何找到对应的SeriesKey？首先SeriesKey是如何映射为SeriesID的（即字典编码的实现），其次SeriesID与SeriesKey的对应关系是否需要存储下来？读完下文才会明白。 
1.1.1.1	Series Block
<div align=center>

![1589100278301.png](..\images\1589100278301.png)

</div>

Series Block（只有一个block）用来存储整个数据库中所有SeriesKey，Series Block主要由四部分构成：Block Trailer、Bloom Filter Section、Series Index Chunk以及一系列SeriesKeyChunk。 
1. Block Trailer：和其他Block Trailer一样，主要存储该Block中其他Section在文件中的偏移量以及大小，是读取解析Block的入口。 
2. Bloom Filter Section：和Hash Index基本一样的原理，不过Bloom Filter只用来表征给定seriesKey是否已经在文件中存在。 
3. Series Index Chunk：B+树索引，由多个Index Entry组成，每个Index Entry又由三个部分构成，分别是Capacity、MinSeriesKey、HashIndex。如下图所示： 
<div align=center>

![1589100299828.png](..\images\1589100299828.png)

</div>

其中MinSeriesKey作为B+树的节点值，用来与给定检索值进行对比，比之大则继续查找右子树，比之小则查找左子树。HashIndex又是一个Hash索引，如果确定待检索seriesKey的叶子索引节点就是该Index Entry，就使用该Hash Index直接进行定位。 
4. Series Key Chunk：存储SeriesKey集合，如下图所示，SeriesKey字段是一个复合结构，字段中记录所有包含的Tag信息以及seriesKey的命名。 
<div align=center>

![1589100320632.png](..\images\1589100320632.png)

</div>

Series Block内部竟然有B+树索引，这个配置可是有点高级的。而且索引节点中竟然有Hash Index。可见这个Block的配置绝对是文件级别的配置，这个Block的结构和HFile的结构其实很像。 
1.1.1	内存中倒排索引构建
1. 时序数据写入到系统之后先将measurement和所有的维度值拼成一个seriesKey 
2. 在文件中确认该seriesKey是否已经存在，如果已经存在就忽略，不需要再将其加入到内存倒排索引。那问题转化为如何在文件中查找某个seriesKey是否已经存在？这就是Series Block中Bloom Filter的核心作用。 
（1）首先使用Bloom Filter进行判断，如果不存在，肯定不存在。如果存在，不一定存在，需要进一步判断。 
（2）使用B+树以及HashIndex进一步判断。 
3. 如果seriesKey在文件中不存在，需要将其写入内存。这里可以将内存中的结构理解为两个核心数据结构： 
（1）<measurement, List<tagKey>>，表示时序表与对应维度集合的映射 
（2）seriesByTagKeyValue那样一个双重Map结构：<tagKey, <tagValue, List<SeriesKey>>> 
1.1.2	倒排索引flush成文件
1. <measurement,List<tagKey>>以及<tagKey,<tagValue,List<SeriesKey>>都需要经过排序处理，排序的意义在于有序数据可以结合Hash Index实现范围查询，另外Series Block中B+树的构建也需要SeriesKey排序。 
2. 在排序的基础上首先持久化<tagKey,<tagValue,List<SeriesKey>>>结构中所有的SeriesKey，也就是先构建Series Block。依次持久化SeriesKey到SeriesKeyChunk，当Chunk满了之后，根据Chunk中最小的SeriesKey构建B+树中的Index Entry节点。当然，Hash Index以及Bloom Filter是需要实时构建的。这个过程类似于HFile的构建过程以及上篇文章TSM文件的构建过程。但与TSM文件构建过程不一样的是，Series Block在构建的同时需要记录下SeriesKey与该Key在文件中偏移量的对应关系，即<SeriesKey, SeriesKeyOffset>，这一点至关重要。 
3.将<tagKey,<tagValue,List<SeriesKey>>>结构中所有的SeriesKey由第二步<SeriesKey,SeriesKeyOffset>中的SeriesKeyOffset代替。形成新的结构：<tagKey,<tagValue, List<SeriesKeyOffset>>,新结构其实就是<tagKey, <tagValue, List<SeriesKeyID>>>。 
4. 在新结构<tagKey, <tagValue, List<SeriesKeyId>>>的基础上首先持久化tagValue，将同一个tagKey下的所有tagValue持久化在一起并生成对应Hash Index写入文件，接着持久话写下一个tagKey的的所有tagValue。 
5. 所有tagValue都持久话完成之后再以此持久化所有的tagKey，形成Tag Block。最后持久化measurement形成Measurement Block。 
1.1.3	使用倒排索引加速维度条件过滤查询
上文提到TSI体系也是LSM结构，所以倒排索引文件会不止一个，这些文件会根据一定规则触发compaction形成一些大文件。如果用户想根据某个表的部分维度查询某个时间段的所有时序数据的话（where tagk1=tagv1 from measurement1），是首先需要到所有TSI文件中查找的，为了方便起见，这里假设只有一个TSI文件： 
1. 根据measurement1在Measument Block进行过滤，可以直接定位到该measurement1对应的所有维度值所在的文件区域。 
2. 加载出该measurement1对应tag key区域的Hash Index，使用tagk1进行hash可以直接定位到该tagk1对应的tag value的存储区域。 
3. 加载出tagk1对应tag value区域的Hash Index，使用tagv1进行hash可以直接定位到该tagv1对应的所有SeriesID。 
4. SeriesID就是对应SeriesKey在索引文件中的offset，直接根据SeriesID可以加载出对应的SeriesKey。 
5. 根据SeriesKey、fieldKey以及时间范围在TSM文件中查找对应的满足查询条件的时间序列
1.1.4	文章总结
InfluxDB的倒排索引是一个很有代表性的实现方案，方案中文件格式定义、Hash Index以及B+树索引的使用、全局编码的实现都很有借鉴意义。但是，Disk-Based Index倒排索引相比其他系统来说还是有很多不同的： 
1. Disk-Based Index是一个完整的LSM结构，LSM系统需要做的事情它都需要实现，比如flush、compaction等。因此可以把它看作一个独立的系统，与原数据没有任何耦合。 
2. Disk-Based Index仅仅实现了Tag到SeriesKey的映射，而没有实现Tag到SeriesKey+FieldKey+Timestamp映射。这能保证InfluxDB的倒排文件比较小，可以有效利用缓存，否则倒排索引文件将会变的非常之大。而且会引入索引数据失效过期的问题，比如某些很久以前的时序过期了，索引对应的数据集就需要相应的调整。 
1.1.5	参考文献
https://github.com/influxdata/influxdb/blob/master/tsdb/index/tsi1/doc.go?spm=5176.100239.blogcont158312.24.NUvEu3&file=doc.go
https://yq.aliyun.com/articles/158312?spm=5176.100239.blogrightarea106382.21.PmSguT
http://blog.fatedier.com/2016/08/15/detailed-in-influxdb-tsm-storage-engine-two/
