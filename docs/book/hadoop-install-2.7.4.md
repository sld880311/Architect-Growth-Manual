<!-- TOC -->

- [开发环境安装（Hadoop2.7.4）](#%e5%bc%80%e5%8f%91%e7%8e%af%e5%a2%83%e5%ae%89%e8%a3%85hadoop274)
  - [虚拟机基本配置](#%e8%99%9a%e6%8b%9f%e6%9c%ba%e5%9f%ba%e6%9c%ac%e9%85%8d%e7%bd%ae)
    - [设置机器名（重启后生效）--使用ubuntu](#%e8%ae%be%e7%bd%ae%e6%9c%ba%e5%99%a8%e5%90%8d%e9%87%8d%e5%90%af%e5%90%8e%e7%94%9f%e6%95%88--%e4%bd%bf%e7%94%a8ubuntu)
    - [设置host映射文件](#%e8%ae%be%e7%bd%aehost%e6%98%a0%e5%b0%84%e6%96%87%e4%bb%b6)
    - [关闭防火墙](#%e5%85%b3%e9%97%ad%e9%98%b2%e7%81%ab%e5%a2%99)
      - [Centos7](#centos7)
    - [关闭Selinux（ubuntu不需要）](#%e5%85%b3%e9%97%adselinuxubuntu%e4%b8%8d%e9%9c%80%e8%a6%81)
  - [SSH](#ssh)
    - [无密钥SHH](#%e6%97%a0%e5%af%86%e9%92%a5shh)
  - [JDK](#jdk)
    - [配置环境变量](#%e9%85%8d%e7%bd%ae%e7%8e%af%e5%a2%83%e5%8f%98%e9%87%8f)
    - [验证](#%e9%aa%8c%e8%af%81)
    - [【建议也设置默认启动】](#%e5%bb%ba%e8%ae%ae%e4%b9%9f%e8%ae%be%e7%bd%ae%e9%bb%98%e8%ae%a4%e5%90%af%e5%8a%a8)
    - [设置全局环境变量](#%e8%ae%be%e7%bd%ae%e5%85%a8%e5%b1%80%e7%8e%af%e5%a2%83%e5%8f%98%e9%87%8f)
  - [Mysql](#mysql)
  - [Tomcat](#tomcat)
    - [防火墙](#%e9%98%b2%e7%81%ab%e5%a2%99)
    - [vsftpd](#vsftpd)
  - [Mongodb](#mongodb)
  - [Hadoop](#hadoop)
    - [下载安装安装包](#%e4%b8%8b%e8%bd%bd%e5%ae%89%e8%a3%85%e5%ae%89%e8%a3%85%e5%8c%85)
    - [配置环境变量](#%e9%85%8d%e7%bd%ae%e7%8e%af%e5%a2%83%e5%8f%98%e9%87%8f-1)
    - [创建文件目录](#%e5%88%9b%e5%bb%ba%e6%96%87%e4%bb%b6%e7%9b%ae%e5%bd%95)
    - [编辑配置文件](#%e7%bc%96%e8%be%91%e9%85%8d%e7%bd%ae%e6%96%87%e4%bb%b6)
      - [hadoop-env.sh（可以不修改）](#hadoop-envsh%e5%8f%af%e4%bb%a5%e4%b8%8d%e4%bf%ae%e6%94%b9)
      - [core-site.xml](#core-sitexml)
      - [hdfs-site.xml](#hdfs-sitexml)
      - [mapred-site.xml](#mapred-sitexml)
      - [yarn-site.xml](#yarn-sitexml)
    - [启动](#%e5%90%af%e5%8a%a8)
      - [格式化](#%e6%a0%bc%e5%bc%8f%e5%8c%96)
      - [启动](#%e5%90%af%e5%8a%a8-1)
      - [验证](#%e9%aa%8c%e8%af%81-1)
        - [输入命令：jps](#%e8%be%93%e5%85%a5%e5%91%bd%e4%bb%a4jps)
        - [192.168.209.135:8088/cluster](#1921682091358088cluster)
        - [192.168.209.135:50070](#19216820913550070)
        - [ps -ef|grep hadoop](#ps--efgrep-hadoop)
        - [使用测试用例测试](#%e4%bd%bf%e7%94%a8%e6%b5%8b%e8%af%95%e7%94%a8%e4%be%8b%e6%b5%8b%e8%af%95)
          - [创建测试目录](#%e5%88%9b%e5%bb%ba%e6%b5%8b%e8%af%95%e7%9b%ae%e5%bd%95)
          - [准备数据](#%e5%87%86%e5%a4%87%e6%95%b0%e6%8d%ae)
          - [运行wordcount例子](#%e8%bf%90%e8%a1%8cwordcount%e4%be%8b%e5%ad%90)
          - [查看结果](#%e6%9f%a5%e7%9c%8b%e7%bb%93%e6%9e%9c)

<!-- /TOC -->
# 开发环境安装（Hadoop2.7.4）

## 虚拟机基本配置

### 设置机器名（重启后生效）--使用ubuntu

<div align=center>

![1589969553959.png](..\images\1589969553959.png)

![1589969589813.png](..\images\1589969589813.png)

</div>
对于ubuntu修改文件是/etc/hostname

### 设置host映射文件

```bash
sudo vi /etc/hosts
```

<div align=center>

![1589969667963.png](..\images\1589969667963.png)

</div>
使用ping命令验证配置是否正确

### 关闭防火墙

```bash
sudo service iptables status（sudo ufw status）
sudo chkconfig iptables off（sudo ufw enable|disable ）
```

#### Centos7

```bash
systemctl stop firewalld.service #停止firewall
systemctl disable firewalld.service #禁止firewall开机启动
firewall-cmd --state #查看默认防火墙状态（关闭后显示notrunning，开启后显示running）
```

### 关闭Selinux（ubuntu不需要）

1. 使用getenforce命令查看是否关闭
2. 修改/etc/selinux/config 文件

将SELINUX=enforcing改为SELINUX=disabled，执行该命令后重启机器生效

## SSH

```bash
ps -e | grep ssh
sudo yum install openssh-server
/etc/init.d/ssh start
```

<div align=center>

![1589969827184.png](..\images\1589969827184.png)

</div>

### 无密钥SHH

在ROOT用户下执行：`ssh-keygen -t rsa`

<div align=center>

![1589969893571.png](..\images\1589969893571.png)

![1589969908640.png](..\images\1589969908640.png)

</div>

`cat id_rsa.pub > authorized_keys`

<div align=center>

![1589969941859.png](..\images\1589969941859.png)

</div>

## JDK

下载安装包，并且完成安装：

```bash
tar -zxvf jdk-8u111-linux-x64.tar.gz
```

### 配置环境变量

```bash
# vi /etc/profile
#JAVA INFO START
export JAVA_HOME=/app/jdk1.8.0_111
export JRE_HOME=/app/jdk1.8.0_111/jre
export PATH=$JAVA_HOME/bin:$JRE_HOME/bin:$PATH
export CLASSPATH=.:$JAVA_HOME/jre/lib/rt.jar:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
#JAVA INFO END
```

<div align=center>

![1589970098784.png](..\images\1589970098784.png)

</div>

保存，退出！

```bash
shell> source /etc/profile    #使之立即生效
```

### 验证

<div align=center>

![1589970142470.png](..\images\1589970142470.png)

</div>

### 【建议也设置默认启动】

```bash
# vi /etc/rc.local

#JAVA INFO START
export JAVA_HOME=/app/jdk1.8.0_111
export JRE_HOME=/app/jdk1.8.0_111/jre
export PATH=$JAVA_HOME/bin:$JRE_HOME/bin:$PATH
export CLASSPATH=.:$JAVA_HOME/jre/lib/rt.jar:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
#JAVA INFO END
```

<div align=center>

![1589970305076.png](..\images\1589970305076.png)

</div>

### 设置全局环境变量

## Mysql

## Tomcat

```bash
tar -zxvf apache-tomcat-8.0.23.tar.gz
gedit startup.sh
JAVA_HOME=/app/jdk1.8.0_111
JRE_HOME=$JAVA_HOME/jre
PATH=$PATH:$JAVA_HOME/bin:$JRE_HOME
CLASSPATH=.:$JRE_HOME/lib/rt.jar:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
TOMCAT_HOME=/app/apache-tomcat-8.5.23
```

<div align=center>

![1589970472693.png](..\images\1589970472693.png)

![1589970480429.png](..\images\1589970480429.png)

</div>

### 防火墙

```bash
gedit /etc/sysconfig/iptables
```

<div align=center>

![1589970544921.png](..\images\1589970544921.png)

</div>

```bash
/etc/init.d/iptables restart
```

### vsftpd

1. 查看是否安装：rpm –qa|grep vsftpd
2. 安装：yum -y install vsftpd
3. 启动：service vsftpd start
4. 重启：service vsftpd restart
5. 设置相关参数：setsebool -P ftp_home_dir 1

<div align=center>

![1589970692117.png](..\images\1589970692117.png)

</div>

## Mongodb

```bash
tar -zxvf mongodb-linux-x86_64-3.0.3.gz
mkdir data
mkdir data/mongodb
touch logs
```

```conf
#【代表端口号，如果不指定则默认为   27017   】
port=27017
#数据库路径】
dbpath= /usr/mongodb/mongodb-linux-x86_64-3.0.3/data/mongodb
#【日志路径】
logpath= /usr/mongodb/mongodb-linux-x86_64-3.0.3/logs
#【日志文件自动累加，而不是覆盖】
logappend=true
```

<div align=center>

![1589970793295.png](..\images\1589970793295.png)

</div>

```bash
/usr/mongodb/mongodb-linux-x86_64-3.0.3/bin/mongod -f /usr/mongodb/mongodb-linux-x86_64-3.0.3/mongodb.conf
pkill mongod
进入mongo shell ：运行 db.shutdownServer()
/usr/mongodb/mongodb-linux-x86_64-3.0.3/bin/mongo
```

## Hadoop

### 下载安装安装包

```bash
tar -zxvf hadoop-2.7.4.tar.gz
```

### 配置环境变量

```bash
vim /etc/profile

#hadoop info start
export HADOOP_HOME=/app/hadoop-2.7.4
export PATH=$HADOOP_HOME/bin:$PATH
#hadoop info end
```

<div align=center>

![1589970955405.png](..\images\1589970955405.png)

</div>

`source /etc/profile`  

加入rc.local  

<div align=center>

![1589970994958.png](..\images\1589970994958.png)

</div>

### 创建文件目录

```bash
root@ubuntu:/app/hadoop-2.7.4# mkdir hdfs
root@ubuntu:/app/hadoop-2.7.4# mkdir hdfs/name
root@ubuntu:/app/hadoop-2.7.4# mkdir hdfs/data
root@ubuntu:/app/hadoop-2.7.4# mkdir tmp
root@ubuntu:/app/hadoop-2.7.4# mkdir mapred
root@ubuntu:/app/hadoop-2.7.4# mkdir mapred/local
root@ubuntu:/app/hadoop-2.7.4# mkdir mapred/system
```

<div align=center>

![1589971047049.png](..\images\1589971047049.png)

</div>

### 编辑配置文件

配置文件目录位置：/app/hadoop-2.7.4/etc/hadoop，注意如果在配置文件中使用ip需要使用实际的ip比如192.168.209.132，不要使用127.0.0.1或者localhost

#### hadoop-env.sh（可以不修改）

```bash
vim hadoop-env.sh

# The java implementation to use.
export JAVA_HOME=/app/jdk1.8.0_111
```

<div align=center>

![1589971142145.png](..\images\1589971142145.png)

</div>

#### core-site.xml

vim core-site.xml

```conf
<configuration>
  <property>
    <name>fs.defaultFS</name>
    <value>hdfs://192.168.209.132:9000</value>
  </property>
  <property>
    <name>hadoop.tmp.dir</name>
    <value>file:/app/hadoop-2.7.4/tmp</value>
  </property>
  <property>
    <name>dfs.namenode.name.dir</name>
    <value>file:/app/hadoop-2.7.4/hdfs/name</value>
  </property>

  <property>
    <name>dfs.datanode.data.dir</name>
    <value>file:/app/hadoop-2.7.4/hdfs/data</value>
  </property>
</configuration>
```

<div align=center>

![1589971248474.png](..\images\1589971248474.png)

</div>

#### hdfs-site.xml

vim hdfs-site.xml

```conf
<configuration>
  <property>
    <name>dfs.replication</name>
    <value>1</value>
  </property>

  <property>
    <name>dfs.permissions</name>
    <value>false</value>
  </property>

  <property>
    <name>dfs.namenode.name.dir</name>
    <value>file:/app/hadoop-2.7.4/hdfs/name</value>
  </property>

  <property>
    <name>dfs.datanode.data.dir</name>
    <value>file:/app/hadoop-2.7.4/hdfs/data</value>
  </property>
</configuration>
```

<div align=center>

![1589971325940.png](..\images\1589971325940.png)

</div>

#### mapred-site.xml

修改mapred-site.xml(默认没有这个配置文件，可以拷贝改目录下的mapred-site.xml.template    :  cp mapred-site.xml.template mapred-site.xml)内容如下  

cp mapred-site.xml.template mapred-site.xml  

vim mapred-site.xml  

```conf
<configuration>
  <property>
    <name>mapreduce.jobtracker.address</name>
    <value>192.168.209.132:9001</value>
    <final>true</final>
  </property>
  <property>
    <name>mapred.system.dir</name>
    <value>file:/app/hadoop-2.7.4/mapred/system</value>
    <final>true</final>
  </property>

  <property>
    <name>mapred.local.dir</name>
    <value>file:/app/hadoop-2.7.4/mapred/local</value>
    <final>true</final>
  </property>
</configuration>
```

<div align=center>

![1589971406545.png](..\images\1589971406545.png)

</div>

#### yarn-site.xml

```conf
<configuration>
<!-- Site specific YARN configuration properties -->
  <!--<property>
    <name>mapreduce.framework.name</name>
    <value>yarn</value>
  </property>  -->

  <property>
    <name>yarn.nodemanager.aux-services</name>
    <value>mapreduce_shuffle</value>
  </property>
</configuration>
```

<div align=center>

![1589971460120.png](..\images\1589971460120.png)

</div>

### 启动

#### 格式化

首次运行需要进行hdfs格式化：hdfs namenode -format  

/app/hadoop-2.7.4/bin/hdfs namenode -format  

<div align=center>

![1589971563872.png](..\images\1589971563872.png)

![1589971601483.png](..\images\1589971601483.png)

![1589971615684.png](..\images\1589971615684.png)

</div>

#### 启动

进入sbin文件夹，执行：./start-all.sh  
/app/hadoop-2.7.4/sbin/start-all.sh  

<div align=center>

![1589971690763.png](..\images\1589971690763.png)

</div>

#### 验证

运行启动后，使用jps命令查看是否将服务启动成功：  

##### 输入命令：jps

包括NameNode,SecondaryNameNode, ResourceManager, DataNode, NodeManager和jps；

<div align=center>

![1589971742904.png](..\images\1589971742904.png)

</div>

##### 192.168.209.135:8088/cluster

<div align=center>

![1589971784740.png](..\images\1589971784740.png)

</div>

##### 192.168.209.135:50070

<div align=center>

![1589971819683.png](..\images\1589971819683.png)

</div>

##### ps -ef|grep hadoop

<div align=center>

![1589971869797.png](..\images\1589971869797.png)

</div>

##### 使用测试用例测试

###### 创建测试目录

```bash
/app/hadoop-2.7.4/bin/hadoop fs -mkdir -p /class3/input
/app/hadoop-2.7.4/bin/hadoop fs -ls /class3/
```

<div align=center>

![1589971947610.png](..\images\1589971947610.png)

</div>

###### 准备数据

```bash
/app/hadoop-2.7.4/bin/hadoop fs -copyFromLocal /app/hadoop-2.7.4/etc/hadoop/* /class3/input
```

<div align=center>

![1589971996895.png](..\images\1589971996895.png)

</div>

该异常处理：（hadoop的本身bug,不用处理，使用centos7安装没问题）  
重新复制文件(可以不使用)  

```bash
/app/hadoop-2.7.4/bin/hadoop fs -rm -r -f /class3/
/app/hadoop-2.7.4/bin/hadoop fs -mkdir -p /class3/input
/app/hadoop-2.7.4/bin/hadoop fs -ls /class3/
/app/hadoop-2.7.4/bin/hadoop fs -copyFromLocal /app/hadoop-2.7.4/etc/hadoop/* /class3/input
```

/app/hadoop-2.7.4/bin/hadoop fs -ls /class3/input  

<div align=center>

![1589972057584.png](..\images\1589972057584.png)

</div>

###### 运行wordcount例子

```bash
cd /app/hadoop-2.7.4/
bin/hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-2.7.4.jar wordcount /class3/input /class3/output
```

任务卡住，解决方案：
<div align=center>

![1589972113482.png](..\images\1589972113482.png)

</div>

修改yarn-site.xml配置文件，增加如下信息  

```conf
 <property>  
     <name>yarn.nodemanager.resource.memory-mb</name>  
     <value>2048</value>  
 </property>
 <property>  
     <name>yarn.scheduler.minimum-allocation-mb</name>
     <value>2048</value>
 </property>
 <property>
     <name>yarn.nodemanager.vmem-pmem-ratio</name>
     <value>2.1</value>
 </property>  
 <property>
     <name>yarn.log-aggregation-enable</name>
     <value>true</value>
 </property>
 <property>
     <name>yarn.log-aggregation.retain-seconds</name>
     <value>604800</value>
 </property>
 <!--<property>
     <name>yarn.resourcemanager.hostname</name>
     <value>ubuntu</value>
 </property>-->
```

###### 查看结果

```bash
bin/hadoop fs -ls /class3/output/  
bin/hadoop fs -cat /class3/output/part-r-00000 | less
```
