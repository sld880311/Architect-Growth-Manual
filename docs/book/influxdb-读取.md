1.1	InfluxDB TSM存储引擎之数据读取
任何一个数据库系统内核关注的重点无非：数据在内存中如何存储、在文件中如何存储、索引结构如何存储、数据写入流程以及数据读取流程。关于InfluxDB存储内核，笔者在之前的文章中已经比较全面的介绍了数据的文件存储格式、倒排索引存储实现以及数据写入流程，本篇文章重点介绍InfluxDB中时序数据的读取流程。 
InfluxDB支持类SQL查询，称为InfluxQL。InfluxQL支持基本的DDL操作和DML操作语句，详见InfluxQL_Spec，比如Select语句： 
SELECT_STMT = "SELECT" FIELDS FROM_CLAUSE [ INTO_CLAUSE ] [ WHERE_CLAUSE ]
[ GROUP_BY_CLAUSE ] [ ORDER_BY_CLAUSE ] [ LIMIT_CLAUSE ]              
[ OFFSET_CLAUSE ] [ SLIMIT_CLAUSE ] [ SOFFSET_CLAUSE ] .
使用InfluxQL可以非常方便、人性化地对InfluxDB中的时序数据进行多维聚合分析。那InfluxDB内部是如何处理Query请求的呢？接下来笔者结合源码对InfluxDB的查询流程做一个剖析。另外，如果看官对源码这部分感兴趣，推荐先阅读官方文档对应部分：https://docs.influxdata.com/influxdb/v1.0/query_language/spec/#query-engine-internals 
本文篇幅相对较长。为了方便阅读，本文分为上下两部分，上半部分会从原理层面介绍InfluxDB的数据读取流程，下半部分会举一个例子模拟整个数据读取的过程。 
1.1.1	上半部分：InfluxDB数据读取流程原理
LSM(TSM)引擎对于读流程的处理通常来说都比较复杂，建议保持足够的耐心和专注力。理论部分会分两个小模块进行介绍，第一个模块会从宏观框架层面简单梳理整个读取流程，第二个模块会从微观细节层面分析TSM存储引擎（TSDB）内部详细的执行逻辑。 
1.1.1.1	InfluxDB读取流程框架
<div align=center>

![1589100713593.png](..\images\1589100713593.png)

</div>

整个读取流程从宏观上分为四个部分： 
1. Query：InfluxQL允许用户使用类SQL语句执行查询分析聚合，InfluxQL语法详见：https://docs.influxdata.com/influxdb/v1.0/query_language/spec/ 
2. QueryParser：InfluxQL进入系统之后，系统首先会对InfluxQL执行切词并解析为抽象语法树（AST），抽象树中标示出了数据源、查询条件、查询列以及聚合函数等等，分别对应上图中Source、Condition以及Aggration。InfluxQL没有使用通用的第三方AST解析库，自己实现了一套解析库，对细节感兴趣的可以参考：https://github.com/influxdata/influxql。接着InfluxDB会将抽象树转化为一个Query实体对象，供后续查询中使用。 
3. BuildIterators：InfluxQL语句转换为Query实体对象之后，就进入读取流程中最重要最核心的一个环节 – 构建Iterator体系。构建Iterator体系是一个非常复杂的逻辑过程，其中细节非常繁复，笔者尽可能化繁为简，将其中的主线抽出来。为了方便理解，笔者将Iterator体系分为三个子体系：顶层Iterator子体系、中间层Iterator子体系以及底层Iterator子体系。 
（1）顶层Iterator子体系 
InfluxDB会为InfluxQL中所有查询field构造一个FieldIterator，FieldIterator表示每个查询列都会创建一个Iterator（称为ExprIterator），这是因为InfluxDB是列式存储系统，所有的列都是独立存储的，因此基于列分别构建Iterator方便执行查询聚合操作。比如sum(click)，sum(impressions)和sum(revenue)三个查询列就分别对应一个ExprIterator。 
ExprIterator根据查询列值是否需要聚合可以分为VarRefIterator和CallIterator，前者表示列值可以直接查询返回，不需要聚合；后者表示查询列需要执行某些聚合操作。示例中查询sum(click)就是典型的CallIterator，CallIterator实际实现分为两步，首先通过VarRefIterator把对应的列值查询到，再通过对应的Reduce函数执行相应聚合。比如sum(click)这个CallIterator就需要雇佣一个VarRefIterator把满足条件的click列值拿上来，再执行Reduce函数sum执行聚合操作。 
（2）中间层Iterator子体系 
InfluxDB中一个查询列的值可能分布在不同的Shard上，需要根据TimeRange决定给定时间段在哪些shard上，并为每个Shard构建一个Iterator，雇佣这个逻辑Iterator负责查询这个shard上对应列的列值。目前单机版所有shard都在同一个InfluxDB实例上，如果实现分布式管理，需要在这一层做处理。 
（3）底层Iterator子体系 
底层Iterator子体系负责单个shard(engine)上满足条件的某一列值的查找或者单机聚合，是Iterator体系中实际干活的Iterator。比如满足where advertiser = “baidu.com” 这个条件就需要先在倒排索引中根据advertiser = “baidu.com”查到包含该tag的所有series，再为每个series构建一个TagsetIterator去查找对应的列值，TagsetIterator会将查找指针置于最小的列值处。 
纵观整个Iterator体系的构建，整体逻辑还是很清晰的。总结起来就是，查询按照查询列构建最顶层FieldIterator，每个FieldIterator会根据TimeRange雇佣多个ShardIterator去处理单个Shard上面对应列值的查找，对查找到的值要么直接返回要么执行Reduce函数进行聚合操作。每个Shard内部首先会根据查询条件利用倒排索引定位到所有满足条件的series，再为每个series构建一个TagsetIterator用来查找具体的列值数据。因此，TagsetIterator是整个体系中唯一干活的Iterator，所有其他上层Iterator都是逻辑Iterator。 
另一个非常重要的点是，同一个Shard内的所有TagsetIterator在构建完成会合并成一个ShardIterator，这个合并过程是对这些TagsetIterator进行排序的过程，排序规则是按照series由小到大排序或者由大到小排序（由用户SQL对查询结果是由小到大排序还是由大到小排序决定）。同理，一个列值对应的多个ShardIterator构建完成之后会合并成一个FieldIterator，合并过程亦是一个排序过程，不过排序是针对所有Shard中的TagsetIterator进行的，排序规则是先比较series，再比较时间。可见，一个FieldIterator最终是由一系列排序过的TagsetIterator构成的。 
4. Emitter.Emit：Iterator体系构建完成之后就完成了查询聚合前的准备工作，接下来就开始干活了。干活逻辑简单来讲是遍历所有FieldIterator，对每个FieldIterator执行一次Next函数，就会返回每个查询列的结果值，组装到一起就是一行数据。FieldIterator执行Next()函数会传递到最底层的TagsetIterator，TagsetIterator执行Next函数实际返回真实的时序数据。 
1.1.1.1	TSDB存储引擎执行逻辑
TSDB存储引擎（实际上就是一个Shard）根据用户的查询请求执行原始数据的查询就是上文中提到的底层Iterator子体系的构建。查询过程分为两个部分：倒排索引查询过滤以及TSM数据层查询，前者通过Query中的where条件结合倒排索引过滤掉不满足条件的SeriesKey；后者根据留下的SeriesKey以及where条件中时间段信息（TimeRange）在TSMFile中以及内存中查出最终满足条件的数值列。TSDB存储引擎会将查询到的所有满足条件的原始数值列返回给上层，上层根据聚合函数对原始数据进行聚合并将聚合结果返回给用户。整个过程如下图所示： 
<div align=center>

![1589100741706.png](..\images\1589100741706.png)

</div>
上图需要从底部向上浏览，整个流程可以整理为如下： 
1. 根据where condition以及所有倒排索引文件查处所有满足条件的SeriesKey 
2. 将满足条件的SeriesKey根据GroupBy维度列进行分组，不同分组后续的所有操作都可以独立并发执行，因此可以多线程处理 
3. 针对某个分组的SeriesKey集合以及待查询列，根据指定查询时间段（TimeRange）在所有TSMFile中根据B+树索引构建查询iterator 
4. 将满足条件的原始数据返回给上层进行聚合运算，并将聚合运算的结果返回给用户 
实际执行的过程可能比较抽象，为了更好的理解，笔者在下半部分举了一个示例。没有理解上面的逻辑没关系，可以先看下面的示例，看完之后再看上面的理论逻辑相信会更加容易理解。 
1.1.1	下半部分：InfluxDB查询流程示例
文章上半部分从理论层面对InfluxDB查询流程进行了介绍。为了方便理解TSDB存储引擎处理查询流程的逻辑，笔者通过如下一个真实示例将其中的核心步骤进行说明。下表为原始时序数据表，表中有3个维度列：publisher、advertiser以及gender，3个数值列：impression、click以及revenue： 
<style type="text/css">
.tg  {border-collapse:collapse;border-color:#bbb;border-spacing:0;}
.tg td{background-color:#E0FFEB;border-color:#bbb;border-style:solid;border-width:1px;color:#594F4F;
  font-family:Arial, sans-serif;font-size:14px;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg th{background-color:#9DE0AD;border-color:#bbb;border-style:solid;border-width:1px;color:#493F3F;
  font-family:Arial, sans-serif;font-size:14px;font-weight:normal;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg .tg-0lax{text-align:left;vertical-align:top}
.tg .tg-sjuo{background-color:#C2FFD6;text-align:left;vertical-align:top}
</style>
<table class="tg">
<thead>
  <tr>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;timestamp&nbsp;&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;publisher&nbsp;&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;advertiser&nbsp;&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;gender&nbsp;&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;impression&nbsp;&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;click&nbsp;&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;revenue&nbsp;&nbsp;&nbsp;&nbsp;</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;2017-11-01T00:00:00&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;ultrarimfast.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;baidu.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;male&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;1800&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;23&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;11.24&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;2017-12-01T00:00:00&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;bieberfever.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;google.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;male&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;2074&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;72&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;31.22&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;2018-01-04T00:00:00&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;ultrarimfast.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;baidu.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;false&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;1079&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;54&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;9.72&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;2018-01-08T00:00:01&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;ultrarimfast.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;google.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;male&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;1912&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;11&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;3.74&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;2018-01-21T00:00:01&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;bieberfever.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;baidu.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;male&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;897&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;17&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;5.48&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;2018-01-26T00:00:01&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;ultrarimfast.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;baidu.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;male&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;1120&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;73&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;6.48&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
</tbody>
</table>

现在用户想查询2018年1月份发布在baidu.com平台上的不同广告商的曝光量、点击量以及总收入，SQL如下所示： 
SELECT SUM(CLICK),SUM(IMPRESSION),SUM(REVENUE) FROM TABLE GROUP BY PUBLISHER WHERE ADVERTISER = "BAIDU.COM" AND TIMESTAMP > "2018-01-01" AND TIMESTAMP < "2018-02-01"
1.1.1.1	步骤一：倒排索引过滤+groupby分组
原始查询语句：select ….  from ad_datasource where advertiser = “baidu.com” …… 。倒排索引即根据条件advertiser=”baidu.com”在所有Index File中遍历查询包含该tag的所有SeriesKey，具体原理（详见《时序数据库技术体系 – InfluxDB 多维查询之倒排索引》）如下： 
1. 根据Index File中Measurement Block根据”ad_datasource”进行过滤，可以直接定位到给定source对应的所有TagKey所在的文件offset|size。 
2. 加载出对应TagKey区域的Hash Index，使用给定TagKey（”advertiser”）进行hash可以直接定位到该TagKey对应的TagValue的文件offset|size。 
3. 加载出TagKey对应TagValue区域的Hash Index，使用过滤条件TagValue（”baidu.com”）进行hash可以直接定位到该TagValue对应的所有SeriesID。 
4. SeriesID就是对应SeriesKey在索引文件中的offset，直接根据SeriesID可以加载出对应的SeriesKey。 
满足条件的所有SeriesKey如下表所示，共有3个： 

<style type="text/css">
.tg  {border-collapse:collapse;border-color:#bbb;border-spacing:0;}
.tg td{background-color:#E0FFEB;border-color:#bbb;border-style:solid;border-width:1px;color:#594F4F;
  font-family:Arial, sans-serif;font-size:14px;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg th{background-color:#9DE0AD;border-color:#bbb;border-style:solid;border-width:1px;color:#493F3F;
  font-family:Arial, sans-serif;font-size:14px;font-weight:normal;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg .tg-0lax{text-align:left;vertical-align:top}
.tg .tg-sjuo{background-color:#C2FFD6;text-align:left;vertical-align:top}
</style>
<table class="tg">
<thead>
  <tr>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;publisher&nbsp;&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;advertiser&nbsp;&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;gender&nbsp;&nbsp;&nbsp;&nbsp;</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;ultrarimfast.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;baidu.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;male&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;ultrarimfast.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;baidu.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;false&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;bieberfever.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;baidu.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;male&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
</tbody>
</table>
根据倒排索引查询得到所有的SeriesKey之后，这里有一个非常重要的步骤：根据groupby条件对SeriesKey进行分组，分组算法为hash。示例查询中聚合条件为group by publisher，因此需要将上面得到的3个SeriesKey按照publisher的不同分成如下两组： 
<style type="text/css">
.tg  {border-collapse:collapse;border-color:#bbb;border-spacing:0;}
.tg td{background-color:#E0FFEB;border-color:#bbb;border-style:solid;border-width:1px;color:#594F4F;
  font-family:Arial, sans-serif;font-size:14px;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg th{background-color:#9DE0AD;border-color:#bbb;border-style:solid;border-width:1px;color:#493F3F;
  font-family:Arial, sans-serif;font-size:14px;font-weight:normal;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg .tg-0lax{text-align:left;vertical-align:top}
.tg .tg-sjuo{background-color:#C2FFD6;text-align:left;vertical-align:top}
</style>
<table class="tg">
<thead>
  <tr>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;publisher&nbsp;&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;advertiser&nbsp;&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;gender&nbsp;&nbsp;&nbsp;&nbsp;</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;bieberfever.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;baidu.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;male&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;publisher&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;advertiser&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;gender&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;ultrarimfast.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;baidu.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;male&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;ultrarimfast.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;baidu.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;female&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
</tbody>
</table>

在倒排索引之后执行分组意义非常重大，分组后不同group的SeriesKey是可以并行独立执行查询并最终执行聚合的，因此后续的所有操作都可以使用多个线程并发执行，极大提升整个查询性能。 
1.1.1.1	步骤二：TSM文件数据检索
到这一步，我们已经按照groupby得到分组后的SeriesKey集合。接下来需要根据SeriesKey以及TimeRange在TSM数据文件中查找满足条件的待查询列。在TSM数据文件中根据SeriesKey以及TimeRange查询field的具体过程（详见：《时序数据库技术体系 – InfluxDB TSM存储引擎之TSMFile》）如下： 
<div align=center>

![1589100899055.png](..\images\1589100899055.png)

</div>

上图中中间部分为索引层，TSM在启动之后就会将TSM文件的索引部分加载到内存，数据部分因为太大并不会直接加载到内存。用户查询可以分为三步： 
1. 首先根据Key（SeriesKey+fieldKey）找到对应的SeriesIndex Block，因为Key是有序的，所以可以使用二分查找来具体实现 
2. 找到SeriesIndex Block之后再根据查找的时间范围，使用[MinTime, MaxTime]索引定位到可能的Series Data Block列表 
3. 将满足条件的Series Data Block加载到内存中解压进一步使用二分查找算法查找即可找到 
在TSM中查询满足TimeRange条件的SeriesKey对应的待查询列值，因为InfluxDB会根据不同的查询列设置独立的FieldIterator，因此查询列有多少就有多少个FieldIterator，如下所示： 
<div align=center>

![1589100922976.png](..\images\1589100922976.png)

</div>


1.1.1.1	步骤三：原始数据聚合
查询到满足条件的所有原始数据之后，InfluxDB会根据查询聚合函数对原始数据进行聚合，如下图所示： 
<style type="text/css">
.tg  {border-collapse:collapse;border-color:#bbb;border-spacing:0;}
.tg td{background-color:#E0FFEB;border-color:#bbb;border-style:solid;border-width:1px;color:#594F4F;
  font-family:Arial, sans-serif;font-size:14px;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg th{background-color:#9DE0AD;border-color:#bbb;border-style:solid;border-width:1px;color:#493F3F;
  font-family:Arial, sans-serif;font-size:14px;font-weight:normal;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg .tg-0lax{text-align:left;vertical-align:top}
.tg .tg-sjuo{background-color:#C2FFD6;text-align:left;vertical-align:top}
</style>
<table class="tg">
<thead>
  <tr>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;publisher&nbsp;&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;sum(impression)&nbsp;&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;sum(click)&nbsp;&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;sum(revenue)&nbsp;&nbsp;&nbsp;&nbsp;</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;bieberfever.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;897&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;17&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;5.48&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;ultrarimfast.com&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;1079 + 1120&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;54 + 73&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;9.72 + 6.48&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
</tbody>
</table>
 
1.1.1	文章总结
本文主要结合InfluxDB源码对查询聚合请求在服务器端的处理框架进行了系统理论介绍，同时深入介绍了InfluxDB Shard Engine是如何利用倒排索引、时序数据存储文件（TSMFile）处理用户的查询请求。最后，举了一个示例对Shard Engine的执行流程进行了形象化说明。整个读取的示意图附件： 
InfluxDB最新版(1.6)查询聚合框架
