1.1	概念 
base 是分布式、面向列的开源数据库（其实准确的说是面向列族）。HDFS 为 Hbase 提供可靠的底层数据存储服务，MapReduce 为 Hbase 提供高性能的计算能力，Zookeeper 为 Hbase 提供稳定服务和 Failover 机制，因此我们说 Hbase 是一个通过大量廉价的机器解决海量数据的高速存储和读取的分布式数据库解决方案。 
1.2	列式存储 
列方式所带来的重要好处之一就是，由于查询中的选择规则是通过列来定义的，因此整个数据库是自动索引化的。 
<div align=center>

![1589095542727.png](..\images\1589095542727.png)

</div>
这里的列式存储其实说的是列族存储，Hbase 是根据列族来存储数据的。列族下面可以有非常多的列，列族在创建表的时候就必须指定。为了加深对 Hbase 列族的理解，下面是一个简单的关系型数据库的表和 Hbase 数据库的表： 
<div align=center>

![1589095566206.png](..\images\1589095566206.png)

</div>

1.1	Hbase 核心概念 
 
1.1.1	Column Family 列族 
Column Family 又叫列族，Hbase 通过列族划分数据的存储，列族下面可以包含任意多的列，实现灵活的数据存取。Hbase 表的创建的时候就必须指定列族。就像关系型数据库创建的时候必须指定具体的列是一样的。Hbase的列族不是越多越好，官方推荐的是列族最好小于或者等于3。我们使用的场景一般是 1 个列族。 
1.1.2	Rowkey（Rowkey 查询，Rowkey 范围扫描，全表扫描） 
Rowkey 的概念和 mysql 中的主键是完全一样的，Hbase 使用 Rowkey 来唯一的区分某一行的数据。Hbase 只支持 3 中查询方式：基于 Rowkey 的单行查询，基于 Rowkey 的范围扫描，全表扫描。 
1.1.3	Region 分区 
	Region：Region 的概念和关系型数据库的分区或者分片差不多。Hbase 会将一个大表的数据基于 Rowkey 的不同范围分配到不通的 Region 中，每个 Region 负责一定范围的数据访问和存储。这样即使是一张巨大的表，由于被切割到不通的 region，访问起来的时延也很低。 
1.1.4	TimeStamp 多版本 
	TimeStamp 是实现 Hbase 多版本的关键。在 Hbase 中使用不同的 timestame 来标识相同 rowkey 行对应的不通版本的数据。在写入数据的时候，如果用户没有指定对应的
timestamp，Hbase 会自动添加一个 timestamp，timestamp 和服务器时间保持一致。在
Hbase 中，相同 rowkey 的数据按照 timestamp 倒序排列。默认查询的是最新的版本，用户可同指定 timestamp 的值来读取旧版本的数据。 
1.2	Hbase 核心架构 
Hbase 是由 Client、Zookeeper、Master、HRegionServer、HDFS 等几个组建组成。 
<div align=center>

![1589095598015.png](..\images\1589095598015.png)

</div>
1.1.1	Client： 
	Client 包含了访问 Hbase 的接口，另外 Client 还维护了对应的 cache 来加速 Hbase 的访问，比如 cache 的.META.元数据的信息。 
1.1.2	Zookeeper： 
	Hbase 通过 Zookeeper 来做 master 的高可用、RegionServer 的监控、元数据的入口以及集群配置的维护等工作。具体工作如下： 
1.	通过 Zoopkeeper 来保证集群中只有 1 个 master 在运行，如果 master 异常，会通过竞争机制产生新的 master 提供服务 
2.	通过 Zoopkeeper 来监控 RegionServer 的状态，当 RegionSevrer 有异常的时候，通过回调的形式通知 Master RegionServer 上下限的信息 
3.	通过 Zoopkeeper 存储元数据的统一入口地址。 
1.1.3	Hmaster 
	master 节点的主要职责如下： 
1.	为 RegionServer 分配 Region 
2.	维护整个集群的负载均衡 
3.	维护集群的元数据信息发现失效的 Region，并将失效的 Region 分配到正常 RegionServer 上当 RegionSever 失效的时候，协调对应 Hlog 的拆分 
1.1.4	HregionServer 
	HregionServer 直接对接用户的读写请求，是真正的“干活”的节点。它的功能概括如下： 
1.	管理 master 为其分配的 Region 
2.	处理来自客户端的读写请求 
3.	负责和底层 HDFS 的交互，存储数据到 HDFS 
4.	负责 Region 变大以后的拆分 
5.	负责 Storefile 的合并工作 
1.1.5	Region 寻址方式（通过 zookeeper .META） 
第 1 步：Client 请求 ZK 获取.META.所在的 RegionServer 的地址。 
第 2 步：Client 请求.META.所在的 RegionServer 获取访问数据所在的 RegionServer 地址，client 会将.META.的相关信息 cache 下来，以便下一次快速访问。 
第 3 步：Client 请求数据所在的 RegionServer，获取所需要的数据。 
<div align=center>

![1589095628789.png](..\images\1589095628789.png)

</div>
 
  
1.1.1	HDFS 
	HDFS 为 Hbase 提供最终的底层数据存储服务，同时为 Hbase 提供高可用（Hlog 存储在 HDFS）的支持。  
1.2	Hbase 的写逻辑 
1.2.1	Hbase 的写入流程 
<div align=center>

![1589095655541.png](..\images\1589095655541.png)

</div>

从上图可以看出氛围 3 步骤： 
获取RegionServer 第 1 步：Client 获取数据写入的 Region 所在的 RegionServer 请求写Hlog 第 2 步：请求写 Hlog, Hlog 存储在 HDFS，当 RegionServer 出现异常，需要使用 Hlog 来恢复数据。 
请求写MemStore 第 3 步：请求写 MemStore,只有当写 Hlog 和写 MemStore 都成功了才算请求写入完成。
MemStore 后续会逐渐刷到 HDFS 中。 
1.1.1	MemStore 刷盘 
为了提高 Hbase 的写入性能，当写请求写入 MemStore 后，不会立即刷盘。而是会等到一定的时候进行刷盘的操作。具体是哪些场景会触发刷盘的操作呢？总结成如下的几个场景： 
全局内存控制 
1.	这个全局的参数是控制内存整体的使用情况，当所有 memstore 占整个 heap 的最大比例的时候，会触发刷盘的操作。这个参数是 hbase.regionserver.global.memstore.upperLimit，默认为整个 heap 内存的 40%。
但这并不意味着全局内存触发的刷盘操作会将所有的 MemStore 都进行输盘，而是通过
另外一个参数 hbase.regionserver.global.memstore.lowerLimit 来控制，默认是整个 heap 内存的 35%。当 flush 到所有 memstore 占整个 heap 内存的比率为 35%的时候，就停止刷盘。这么做主要是为了减少刷盘对业务带来的影响，实现平滑系统负载的目的。 
MemStore达到上限 
2.	当 MemStore 的大小达到 hbase.hregion.memstore.flush.size 大小的时候会触发刷盘，默认 128M 大小 
RegionServer 的Hlog 数量达到上限 
3.	前面说到 Hlog 为了保证 Hbase 数据的一致性，那么如果 Hlog 太多的话，会导致故障恢复的时间太长，因此 Hbase 会对 Hlog 的最大个数做限制。当达到 Hlog 的最大个数的时候，会强制刷盘。这个参数是 hase.regionserver.max.logs，默认是 32 个。 
手工触发 
4.	可以通过 hbase shell 或者 java api 手工触发 flush 的操作。 
关闭RegionServer触发 
5.	在正常关闭 RegionServer 会触发刷盘的操作，全部数据刷盘后就不需要再使用 Hlog 恢复数据。 
Region 使用HLOG 恢复完数据后触发 
6.	：当 RegionServer 出现故障的时候，其上面的 Region 会迁移到其他正常的 RegionServer 上，在恢复完 Region 的数据后，会触发刷盘，当刷盘完成后才会提供给业务访问。 
1.2	HBase vs Cassandra 

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
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;HBase&nbsp;&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;Cassandra&nbsp;&nbsp;&nbsp;&nbsp;</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;语言&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;Java&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;Java&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;出发点&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;BigTable&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;BigTable and&nbsp;&nbsp;&nbsp;Dynamo&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;License&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;Apache&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;Apache&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;Protocol&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;HTTP/REST (also&nbsp;&nbsp;&nbsp;Thrift)&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;Custom, binary&nbsp;&nbsp;&nbsp;(Thrift)&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;数据分布&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;表划分为多个 region 存在不同 region&nbsp;&nbsp;&nbsp;&nbsp;server 上&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;改进的一致性哈希（虚拟节点）&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;存储目标&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;大文件&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;小文件&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;一致性&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;强一致性&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;最终一致性，Quorum NRW 策略&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;架构&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;master/slave&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;p2p&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;高可用性&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;NameNode 是 HDFS 的单点故障点&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;P2P 和去中心化设计，不会出现单点故障&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;伸缩性&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;Region Server 扩容，通过将自身发布到&nbsp;&nbsp;&nbsp;Master，Master 均匀分布 Region&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;扩容需在 Hash Ring 上多个节点间调整数据分布&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;读写性能&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;数据读写定位可能要通过最多 6 次的网络 RPC，性能较低。&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;数据读写定位非常快&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;数据冲突处理&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;乐观并发控制（optimistic concurrency control）&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;向量时钟&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;临时故障处理&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;Region Server 宕机，重做 HLog&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;数据回传机制：某节点宕机，hash 到该节点的新数据自动路由到下一节点做 hinted handoff，源节点恢复后，推送回源节点。&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;永久故障恢复&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;Region Server 恢复，master 重新给其分配 region&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;Merkle 哈希树，通过 Gossip 协议同步 Merkle Tree，维&nbsp;&nbsp;&nbsp;护集群节点间的数据一致性&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;成员通信及错误检测&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;Zookeeper&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-sjuo">&nbsp;&nbsp;&nbsp;基于 Gossip&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;CAP&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;1，强一致性，0 数据丢失。2，可用性低。3，扩容方便。&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;1，弱一致性，数据可能丢失。2，可用性高。3，扩容方便。&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
  </tr>
</tbody>
</table>