<!-- TOC -->

- [Mysql高可用](#mysql%e9%ab%98%e5%8f%af%e7%94%a8)
  - [互为主从复制](#%e4%ba%92%e4%b8%ba%e4%b8%bb%e4%bb%8e%e5%a4%8d%e5%88%b6)
    - [背景](#%e8%83%8c%e6%99%af)
    - [环境](#%e7%8e%af%e5%a2%83)
    - [关键步骤](#%e5%85%b3%e9%94%ae%e6%ad%a5%e9%aa%a4)
    - [修改配置文件my.cnf](#%e4%bf%ae%e6%94%b9%e9%85%8d%e7%bd%ae%e6%96%87%e4%bb%b6mycnf)
      - [H1](#h1)
      - [H2](#h2)
    - [设置主从bin-log和读写位置](#%e8%ae%be%e7%bd%ae%e4%b8%bb%e4%bb%8ebin-log%e5%92%8c%e8%af%bb%e5%86%99%e4%bd%8d%e7%bd%ae)
      - [查看master信息](#%e6%9f%a5%e7%9c%8bmaster%e4%bf%a1%e6%81%af)
        - [H1](#h1-1)
        - [H2](#h2-1)
      - [设置读写位置](#%e8%ae%be%e7%bd%ae%e8%af%bb%e5%86%99%e4%bd%8d%e7%bd%ae)
        - [H1：设置H2的信息](#h1%e8%ae%be%e7%bd%aeh2%e7%9a%84%e4%bf%a1%e6%81%af)
        - [置H1的信息](#%e7%bd%aeh1%e7%9a%84%e4%bf%a1%e6%81%af)
      - [查看状态](#%e6%9f%a5%e7%9c%8b%e7%8a%b6%e6%80%81)
    - [测试](#%e6%b5%8b%e8%af%95)
      - [初始状态](#%e5%88%9d%e5%a7%8b%e7%8a%b6%e6%80%81)
        - [H1数据库信息](#h1%e6%95%b0%e6%8d%ae%e5%ba%93%e4%bf%a1%e6%81%af)
        - [H2数据库信息](#h2%e6%95%b0%e6%8d%ae%e5%ba%93%e4%bf%a1%e6%81%af)
      - [H1创建数据库，在H2中是否显示](#h1%e5%88%9b%e5%bb%ba%e6%95%b0%e6%8d%ae%e5%ba%93%e5%9c%a8h2%e4%b8%ad%e6%98%af%e5%90%a6%e6%98%be%e7%a4%ba)
      - [H2创建数据库，在H1中是否显示](#h2%e5%88%9b%e5%bb%ba%e6%95%b0%e6%8d%ae%e5%ba%93%e5%9c%a8h1%e4%b8%ad%e6%98%af%e5%90%a6%e6%98%be%e7%a4%ba)
  - [Keepalived+Mysql主主](#keepalivedmysql%e4%b8%bb%e4%b8%bb)
    - [安装keepalived注意：(关闭selinux策略 setenforce 0)](#%e5%ae%89%e8%a3%85keepalived%e6%b3%a8%e6%84%8f%e5%85%b3%e9%97%adselinux%e7%ad%96%e7%95%a5-setenforce-0)
      - [离线或在线现在安装包](#%e7%a6%bb%e7%ba%bf%e6%88%96%e5%9c%a8%e7%ba%bf%e7%8e%b0%e5%9c%a8%e5%ae%89%e8%a3%85%e5%8c%85)
      - [安装](#%e5%ae%89%e8%a3%85)
      - [将keepalived配置成系统服务](#%e5%b0%86keepalived%e9%85%8d%e7%bd%ae%e6%88%90%e7%b3%bb%e7%bb%9f%e6%9c%8d%e5%8a%a1)
    - [修改配置文件](#%e4%bf%ae%e6%94%b9%e9%85%8d%e7%bd%ae%e6%96%87%e4%bb%b6)
      - [keepalived.conf](#keepalivedconf)
      - [mysql_check.sh](#mysqlchecksh)
      - [mysql_down.sh](#mysqldownsh)
      - [启动keepalived](#%e5%90%af%e5%8a%a8keepalived)
    - [测试](#%e6%b5%8b%e8%af%95-1)
      - [在H1和H2中配置远程连接](#%e5%9c%a8h1%e5%92%8ch2%e4%b8%ad%e9%85%8d%e7%bd%ae%e8%bf%9c%e7%a8%8b%e8%bf%9e%e6%8e%a5)
      - [通过vip链接(可以通过客户端连接)](#%e9%80%9a%e8%bf%87vip%e9%93%be%e6%8e%a5%e5%8f%af%e4%bb%a5%e9%80%9a%e8%bf%87%e5%ae%a2%e6%88%b7%e7%ab%af%e8%bf%9e%e6%8e%a5)
      - [测试切换](#%e6%b5%8b%e8%af%95%e5%88%87%e6%8d%a2)
        - [H1132、H2137正常启动](#h1132h2137%e6%ad%a3%e5%b8%b8%e5%90%af%e5%8a%a8)
          - [H1 IP](#h1-ip)
          - [H2 IP](#h2-ip)
        - [关闭H1：service mysqld stop，ip自动切换](#%e5%85%b3%e9%97%adh1service-mysqld-stopip%e8%87%aa%e5%8a%a8%e5%88%87%e6%8d%a2)
          - [H1 IP](#h1-ip-1)
          - [H2 IP](#h2-ip-1)
        - [重启H1：service mysqld start，IP不切换（减少资源浪费）](#%e9%87%8d%e5%90%afh1service-mysqld-startip%e4%b8%8d%e5%88%87%e6%8d%a2%e5%87%8f%e5%b0%91%e8%b5%84%e6%ba%90%e6%b5%aa%e8%b4%b9)
          - [H1 IP](#h1-ip-2)
          - [H2 IP](#h2-ip-2)
        - [关闭H2：service mysqld stop，IP切换](#%e5%85%b3%e9%97%adh2service-mysqld-stopip%e5%88%87%e6%8d%a2)
          - [H1 IP](#h1-ip-3)
          - [H2 IP](#h2-ip-3)
  - [其他](#%e5%85%b6%e4%bb%96)
    - [log-slave-updates说明](#log-slave-updates%e8%af%b4%e6%98%8e)
    - [场景](#%e5%9c%ba%e6%99%af)
      - [官方说明](#%e5%ae%98%e6%96%b9%e8%af%b4%e6%98%8e)
  - [参考](#%e5%8f%82%e8%80%83)

<!-- /TOC -->
# Mysql高可用

## 互为主从复制

### 背景

在一些高可用的环境中，mysql的主从不能满足现实中的一些实际需求。比如，一些流量大的网站数据库访问有了瓶颈，需要负载均衡的时候就用两个或者多个的mysql服务器，而这些mysql服务器的数据库数据必须要保持一致，那么就会用到主主复制。  

mysql主从架构中其实就一个主在工作，而从就相当于一个备份机器，从通过日志监测的方式来备份主库上的数据而保证主库的数据安全。在这种架构中如果从上的数据做了改变，主数据是不会用任何变化的。因为mysql主从架构主要是mysql从监控mysql主的日志变化来实现同步，相反的在这个架构中主并没有监控从的日志变化。所以，mysql从数据反生变化，主也就没有什么变化了。  

通过上述描述，可以看到如果想实现主主复制，无非就是在mysql主从架构上让mysql主实现监测从的日志变化，从而实现两台机器相互同步。  

### 环境

1. H1：192.168.209.132 root/123456a?
2. H2：192.168.209.137 root/123456a?

### 关键步骤

1. 第一、server-id，主server-id小于从server-id（必须不一样）
2. 第二、主数据库，建立一个能复制的帐号并授权。
3. 第三、从服务器开启复制功能就OK了。

### 修改配置文件my.cnf

```conf
log-bin=mysql-bin：这个选项基本默认都是开着的，如果没有打开，可以手动打开。
log-slave-updates=1：这个选项特别的重要它是为了让slave也能充当master，同时也为了更好的服务于 m-m + s 的环境，保证slave挂在任何一台master上都会接收到另一个master的写入信息。当然不局限于这个架构，级联复制的架构同样也需要log-slave-updates的支持。
server-id = 1：这个ID为服务器ID如果配置一样会出现冲突，而不能复制
binlog-ignore-db = mysql,information_schema       #忽略写入binlog日志的库
auto-increment-increment = 2             #字段变化增量值
auto-increment-offset = 1              #初始字段ID为1
slave-skip-errors = all                       #忽略所有复制产生的错误
```

配置完成之后，使用命令`service mysqld restart`，完成重启，重启效果如下：  

#### H1

<div align=center>

![1590058835864.png](..\images\1590058835864.png)

![1590058843893.png](..\images\1590058843893.png)

</div>

#### H2

<div align=center>

![1590058864954.png](..\images\1590058864954.png)

![1590058870961.png](..\images\1590058870961.png)

</div>

### 设置主从bin-log和读写位置

#### 查看master信息

##### H1

```sql
mysql> show master status;
+------------------+----------+--------------+--------------------------+-------------------+
| File             | Position | Binlog_Do_DB | Binlog_Ignore_DB         | Executed_Gtid_Set |
+------------------+----------+--------------+--------------------------+-------------------+
| mysql-bin.000001 |      154 |              | mysql,information_schema |                   |
+------------------+----------+--------------+--------------------------+-------------------+
1 row in set (0.01 sec)
```

##### H2

```sql
mysql> show master status;
+------------------+----------+--------------+--------------------------+-------------------+
| File             | Position | Binlog_Do_DB | Binlog_Ignore_DB         | Executed_Gtid_Set |
+------------------+----------+--------------+--------------------------+-------------------+
| mysql-bin.000001 |      154 |              | mysql,information_schema |                   |
+------------------+----------+--------------+--------------------------+-------------------+
1 row in set (0.00 sec)
```

#### 设置读写位置

##### H1：设置H2的信息

```sql
mysql> GRANT REPLICATION SLAVE ON *.* TO 'sunld_backup'@'%' IDENTIFIED  BY '123456a?';
mysql> flush  privileges;
mysql> change  master to
    ->  master_host='192.168.209.137',
    ->  master_user='sunld_backup',
    ->  master_password='123456a?',
    ->  master_log_file='mysql-bin.000001',
    ->  master_log_pos=154;  #对端状态显示的值
mysql> start  slave;         #启动同步
```

##### 置H1的信息

```sql
mysql> GRANT  REPLICATION SLAVE ON *.* TO 'sunld_backup'@'%' IDENTIFIED  BY '123456a?';
mysql> flush  privileges;
mysql> change  master to
    ->  master_host='192.168.209.132',
    ->  master_user='sunld_backup',
    ->  master_password='123456a?',
    ->  master_log_file='mysql-bin.000001',
    ->  master_log_pos=154;  #对端状态显示的值
mysql> start  slave;         #启动同步
```

#### 查看状态

可以看到

1. Slave_IO_Running: Yes
2. Slave_SQL_Running: Yes

### 测试

#### 初始状态

##### H1数据库信息

```bash
[root@localhost ~]# mysql -uroot -p123456a? -h192.168.209.132 -e 'show databases;'
mysql: [Warning] Using a password on the command line interface can be insecure.
+--------------------+
| Database           |
+--------------------+
| information_schema |
| mysql              |
| performance_schema |
| sys                |
+--------------------+
```

##### H2数据库信息

```bash
[root@localhost ~]# mysql -uroot -p123456a? -h192.168.209.137 -e 'show databases;'
mysql: [Warning] Using a password on the command line interface can be insecure.
+--------------------+
| Database           |
+--------------------+
| information_schema |
| mysql              |
| performance_schema |
| sys                |
+--------------------+
```

#### H1创建数据库，在H2中是否显示

```bash
[root@localhost ~]# mysql -uroot -p123456a? -h192.168.209.132 -e 'create database h1;'
mysql: [Warning] Using a password on the command line interface can be insecure.
[root@localhost ~]# mysql -uroot -p123456a? -h192.168.209.137 -e 'show databases;'
mysql: [Warning] Using a password on the command line interface can be insecure.
+--------------------+
| Database           |
+--------------------+
| information_schema |
| h1                 |
| mysql              |
| performance_schema |
| sys                |
+--------------------+
```

#### H2创建数据库，在H1中是否显示

```bash
[root@localhost ~]# mysql -uroot -p123456a? -h192.168.209.137 -e 'create database h2;'
mysql: [Warning] Using a password on the command line interface can be insecure.
[root@localhost ~]# mysql -uroot -p123456a? -h192.168.209.132 -e 'show databases;'
mysql: [Warning] Using a password on the command line interface can be insecure.
+--------------------+
| Database           |
+--------------------+
| information_schema |
| h1                 |
| h2                 |
| mysql              |
| performance_schema |
| sys                |
+--------------------+
```

## Keepalived+Mysql主主

**mysql主主参考上一章节。**  

使用keepalived可以实现热备，mysql之间的自动切换。  

### 安装keepalived注意：(关闭selinux策略 setenforce 0)

#### 离线或在线现在安装包

1. 下载最新版本：1.4.0
2. 离线下载（使用该方式完成）：官方现在地址：http://www.keepalived.org/download.html
3. 在线下载安装包：wget http://www.keepalived.org/software/keepalived-1.4.0.tar.gz
4. 在线安装：yum install keepalived -y

#### 安装

```bash
#解压
tar -zxvf keepalived-1.4.0.tar.gz
#删除安装包
rm -rf keepalived-1.4.0.tar.gz
#编译
cd /app/keepalived-1.4.0/
./configure --prefix=/usr/local/keepalived
#出现如下错误：
checking for a BSD-compatible install... /usr/bin/install -c
checking whether build environment is sane... yes
checking for a thread-safe mkdir -p... /usr/bin/mkdir -p
checking for gawk... gawk
checking whether make sets $(MAKE)... yes
checking whether make supports nested variables... yes
checking whether make supports nested variables... (cached) yes
checking for pkg-config... /usr/bin/pkg-config
checking pkg-config is at least version 0.9.0... yes
checking for gcc... no
checking for cc... no
checking for cl.exe... no
configure: error: in `/app/keepalived-1.4.0':
configure: error: no acceptable C compiler found in $PATH
#安装编译依赖包
yum -y install gcc
#Openssl错误
configure: error: 
  !!! OpenSSL is not properly installed on your system. !!!
  !!! Can not include OpenSSL headers files.            !!!
#安装openssl
yum -y install openssl-devel 
#最终编译
make && make install
```

#### 将keepalived配置成系统服务

```bash
cp -r /usr/local/keepalived/etc/keepalived /etc/init.d/
cp -r  /usr/local/keepalived/etc/sysconfig/keepalived /etc/sysconfig/
mkdir /etc/keepalived/
cp /usr/local/keepalived/etc/keepalived/keepalived.conf /etc/keepalived/
cp /usr/local/keepalived/sbin/keepalived /usr/sbin/
```

### 修改配置文件

#### keepalived.conf

vi /etc/keepalived/keepalived.conf  

```conf
! Configuration File for keepalived

global_defs {
   #设置报警通知邮件地址，可以设置多个
   notification_email {
     acassen@firewall.loc
     failover@firewall.loc
     sysadmin@firewall.loc
   }
   #设置邮件的发送地址
   notification_email_from Alexandre.Cassen@firewall.loc
   #设置smtp server的地址,该地址必须是存在的
   smtp_server 127.0.0.1
   #设置连接smtp server的超时时间
   smtp_connect_timeout 30
   #运行Keepalived服务器的标识，发邮件时显示在邮件标题中的信息，可以设置为主机名 
   router_id MYSQL_HA      #标识，双主相同
}
#检测脚本
vrrp_script check_run {
	script "/etc/keepalived/bin/mysql_check.sh"
	interval 10
}
#定义VRRP实例,实例名自定义
vrrp_instance VI_1 {
    #指定Keepalived的角色，MASTER主机 BACKUP备份
    state BACKUP           #两台都设置BACKUP
    #指定HA监测的接口
    interface ens33       #实际网卡
        #虚拟路由标识，这个标识是一个数字(1-255)，在一个VRRP实例中主备服务器ID必须一样
    virtual_router_id 51   #主备相同
        #优先级，数字越大优先级越高，在一个实例中主服务器优先级要高于备服务器
    priority 100           #优先级，backup设置90
    #设置主备之间同步检查的时间间隔单位秒
    advert_int 1
    #设置不抢占模式
    nopreempt              #不主动抢占资源，只在master这台优先级高的设置，backup不设置（防止频繁切换）
        #设置验证类型和密码
    authentication {
        #验证类型有两种{PASS|HA}
        auth_type PASS
        #设置验证密码，在一个实例中主备密码保持一样
        auth_pass 1111
    }
    track_script {
	    check_run          #执行上述检查mysql状态脚本
    }
    #定义虚拟IP地址,可以有多个，每行一个
    virtual_ipaddress {
        192.168.209.138
    }
}

virtual_server 192.168.209.138 3306 {
    delay_loop 2
    #lb_algo rr            #LVS算法，用不到，我们就关闭了
    #lb_kind NAT           #LVS模式，如果不关闭，备用服务器不能通过VIP连接主MySQL
    #nat_mask 255.255.255.0
    persistence_timeout 50 #同一IP的连接50秒内被分配到同一台真实服务器
    protocol TCP

    real_server 192.168.209.132 3306 { #检测本地mysql，backup也要写检测本地mysql
        weight 3
	    notify_down /etc/keepalived/bin/mysql_down.sh #当mysql服down时，执行此脚本，杀死keepalived，启动其他服务
	    TCP_CHECK {
            connect_timeout 3      #连接超时
            nb_get_retry 3         #重试次数
            delay_before_retry 3   #重试间隔时间
            connect_port 3306
        }
    }
}
```

#### mysql_check.sh

```bash
mkdir /etc/keepalived/bin
vim /etc/keepalived/bin/mysql_check.sh
chmod a+x /etc/keepalived/bin/mysql_check.sh

#!/bin/bash
#This scripts is check for Mysql Slave status
Mysqlbin=/usr/bin/mysql
user=root
pw='123456a?'
port=3306
host=127.0.0.1
#最大延时
sbm=120

#Check for $Mysqlbin
if [ ! -f $Mysqlbin ];then
        echo 'Mysqlbin not found,check the variable Mysqlbin'
        pkill keepalived
fi

#Get Mysql Slave Status
IOThread=`$Mysqlbin -h $host -P $port -u$user -p$pw -e 'show slave status\G'  2>/dev/null|grep 'Slave_IO_Running:'|awk '{print $NF}'`
SQLThread=`$Mysqlbin -h $host -P $port -u$user -p$pw -e 'show slave status\G' 2>/dev/null|grep 'Slave_SQL_Running:'|awk '{print $NF}'`
SBM=`$Mysqlbin -h $host -P $port -u$user -p$pw -e 'show slave status\G' 2>/dev/null|grep 'Seconds_Behind_Master:'|awk '{print $NF}'`

#Check if the mysql run
if [[ -z "$IOThread" ]];then
        pkill keepalived
fi

#Check if the thread run 
if [[ "$IOThread" == "No" || "$SQLThread" == "No" ]];then
        pkill keepalived
        elif [[ $SBM -ge $sbm ]];then
                pkill keepalived
        else
                exit 0
fi
```

#### mysql_down.sh

```bash
vim /etc/keepalived/bin/mysql_down.sh
chmod a+x /etc/keepalived/bin/mysql_down.sh

#!/bin/bash
pkill keepalived
/sbin/ifdown ens33 && /sbin/ifup ens33 #ens33按照实际网卡名填写
```

#### 启动keepalived

```bash
service keepalived start
```

### 测试

#### 在H1和H2中配置远程连接

```sql
mysql> grant all on *.* to'root'@'%' identified by '123456a?';
mysql> flush privileges;
```

#### 通过vip链接(可以通过客户端连接)

```bash
[root@hadoop keepalived]# mysql -uroot -p123456a? -h 192.168.209.138 
mysql: [Warning] Using a password on the command line interface can be insecure.
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 55
Server version: 5.7.20-log MySQL Community Server (GPL)

Copyright (c) 2000, 2017, Oracle and/or its affiliates. All rights reserved.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql>
```

#### 测试切换

1. 通过Mysql客户端通过VIP连接，看是否连接成功。
2. 停止master这台mysql服务，是否能正常切换过去，可通过ip addr命令来查看VIP在哪台服务器上。
3. 可通过查看/var/log/messges日志，看出主备切换过程
4. master服务器故障恢复后，是否主动抢占资源，成为活动服务器。

##### H1132、H2137正常启动

###### H1 IP

```bash
[root@hadoop etc]# ip a
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN qlen 1
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host 
       valid_lft forever preferred_lft forever
2: ens33: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP qlen 1000
    link/ether 00:0c:29:c2:a2:df brd ff:ff:ff:ff:ff:ff
    inet 192.168.209.132/24 brd 192.168.209.255 scope global ens33
       valid_lft forever preferred_lft forever
    inet 192.168.209.138/32 scope global ens33
       valid_lft forever preferred_lft forever
    inet6 fe80::ffc6:3a33:ea90:cc5f/64 scope link 
       valid_lft forever preferred_lft forever
```

###### H2 IP

```bash
[root@localhost etc]# ip a
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN qlen 1
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host 
       valid_lft forever preferred_lft forever
2: ens33: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP qlen 1000
    link/ether 00:0c:29:4c:51:01 brd ff:ff:ff:ff:ff:ff
    inet 192.168.209.137/24 brd 192.168.209.255 scope global ens33
       valid_lft forever preferred_lft forever
    inet6 fe80::cfc:5056:d04a:340d/64 scope link 
       valid_lft forever preferred_lft forever
```

##### 关闭H1：service mysqld stop，ip自动切换

###### H1 IP

```bash
[root@hadoop etc]# ip a
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN qlen 1
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host 
       valid_lft forever preferred_lft forever
2: ens33: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP qlen 1000
    link/ether 00:0c:29:c2:a2:df brd ff:ff:ff:ff:ff:ff
    inet 192.168.209.132/24 brd 192.168.209.255 scope global ens33
       valid_lft forever preferred_lft forever
    inet6 fe80::ffc6:3a33:ea90:cc5f/64 scope link 
       valid_lft forever preferred_lft forever
```

###### H2 IP

```bash
[root@localhost etc]# ip a
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN qlen 1
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host 
       valid_lft forever preferred_lft forever
2: ens33: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP qlen 1000
    link/ether 00:0c:29:4c:51:01 brd ff:ff:ff:ff:ff:ff
    inet 192.168.209.137/24 brd 192.168.209.255 scope global ens33
       valid_lft forever preferred_lft forever
    inet 192.168.209.138/32 scope global ens33
       valid_lft forever preferred_lft forever
    inet6 fe80::cfc:5056:d04a:340d/64 scope link 
       valid_lft forever preferred_lft forever
```

##### 重启H1：service mysqld start，IP不切换（减少资源浪费）

###### H1 IP

```bash
[root@hadoop etc]# ip a
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN qlen 1
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host 
       valid_lft forever preferred_lft forever
2: ens33: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP qlen 1000
    link/ether 00:0c:29:c2:a2:df brd ff:ff:ff:ff:ff:ff
    inet 192.168.209.132/24 brd 192.168.209.255 scope global ens33
       valid_lft forever preferred_lft forever
    inet6 fe80::ffc6:3a33:ea90:cc5f/64 scope link 
       valid_lft forever preferred_lft forever
```

###### H2 IP

```bash
[root@localhost etc]# ip a
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN qlen 1
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host 
       valid_lft forever preferred_lft forever
2: ens33: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP qlen 1000
    link/ether 00:0c:29:4c:51:01 brd ff:ff:ff:ff:ff:ff
    inet 192.168.209.137/24 brd 192.168.209.255 scope global ens33
       valid_lft forever preferred_lft forever
    inet 192.168.209.138/32 scope global ens33
       valid_lft forever preferred_lft forever
    inet6 fe80::cfc:5056:d04a:340d/64 scope link 
       valid_lft forever preferred_lft forever
```

##### 关闭H2：service mysqld stop，IP切换

###### H1 IP

```bash
[root@hadoop keepalived]# ip a
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN qlen 1
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host 
       valid_lft forever preferred_lft forever
2: ens33: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP qlen 1000
    link/ether 00:0c:29:c2:a2:df brd ff:ff:ff:ff:ff:ff
    inet 192.168.209.132/24 brd 192.168.209.255 scope global ens33
       valid_lft forever preferred_lft forever
    inet 192.168.209.138/32 scope global ens33
       valid_lft forever preferred_lft forever
    inet6 fe80::ffc6:3a33:ea90:cc5f/64 scope link 
       valid_lft forever preferred_lft forever
```

###### H2 IP

```bash
[root@localhost etc]# ip a
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN qlen 1
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host 
       valid_lft forever preferred_lft forever
2: ens33: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP qlen 1000
    link/ether 00:0c:29:4c:51:01 brd ff:ff:ff:ff:ff:ff
    inet 192.168.209.137/24 brd 192.168.209.255 scope global ens33
       valid_lft forever preferred_lft forever
    inet6 fe80::cfc:5056:d04a:340d/64 scope link 
       valid_lft forever preferred_lft forever
```

## 其他

### log-slave-updates说明

当从库log_slave_updates参数没有开启时，从库的binlog不会记录来源于主库的操作记录。只有开启log_slave_updates，从库binlog才会记录主库同步的操作日志。

```sql
mysql> show variables like '%log_slave%';
+-------------------+-------+
| Variable_name     | Value |
+-------------------+-------+
| log_slave_updates | ON    |
+-------------------+-------+
1 row in set (0.00 sec)
```

### 场景

M01和M02为主主复制，M01和R01为主从复制；在测试的过程中发现了以下问题：

1. M01和M02的主主复制是没有问题的（从M01写入数据能同步到M02，从M02写入数据能够同步到M01);
2. 主从同步的时候，当从M01写入的时候，数据可以写入到R01；
3. 当从M02写入的时候，数据就不能写入到R01；

问题的原因：log_slave_updates参数的状态为NO  

#### 官方说明

Normally, a slave does not log to its own binary log any updates that are received from a master server. This option tells the slave to log the updates performed by its SQL thread to its own binary log. For this option to have any effect, the slave must also be started with the --log-bin option to enable binary logging. Prior to MySQL 5.5, the server would not start when using the --log-slave-updates option without also starting the server with the --log-bin option, and would fail with an error; in MySQL 5.5, only a warning is generated. (Bug #44663) --log-slave-updates is used when you want to chain replication servers. For example, you might want to set up replication servers using this arrangement:  
A -> B -> C  
Here, A serves as the master for the slave B, and B serves as the master for the slave C. For this to work, B must be both a master and a slave. You must start both A and B with --log-bin to enable binary logging, and B with the --log-slave-updates option so that updates received from A are logged by B to its binary log.  

a) M01同步从M02同步数据过来的时候，log_slave_updates参数用来控制M01是否把所有的操作写入到binary log，默认的情况下mysql是关闭的;  
b) R01数据的更新需要通过读取到M01的binary log才能进行更新，这个时候M01是没有写binary log的，所以当数据从M02写入的时候，R01也就没有更新了。。  

## 参考

1. [Linux下的MYSQL主主复制](https://blog.51cto.com/duyunlong/1306841)
2. [MySQL高可用性之Keepalived+Mysql（双主热备）](https://blog.51cto.com/lizhenliang/1362313)
3. [Mysql+Keepalived双主互备高可用详细配置](https://blog.csdn.net/HzSunshine/article/details/67059532)
4. [Linux下keepalived+mysql实现高可用](https://blog.51cto.com/duyunlong/1310405)
5. [mysql高可用架构](https://www.cnblogs.com/Aiapple/p/5794901.html)
