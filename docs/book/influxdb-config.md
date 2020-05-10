1.1	常用监控脚本
1.1.1	控制series数量
Series会被索引且存在内存中，如果量太大会对资源造成过多损耗，且查询效率也得不到保障。 
可以通过以下方式查询series的数量：
influx -database 'cloudportal' -execute 'show series' -format 'csv'|wc -l
通过以下方式查询tag values的数量：
influx -database 'cloudportal' -execute 'SHOW TAG VALUES FROM six_months.collapsar_flow WITH KEY = dip' -format 'csv'|wc -l
