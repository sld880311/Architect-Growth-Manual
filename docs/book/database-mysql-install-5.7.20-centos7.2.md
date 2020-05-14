<!-- TOC -->

- [Centos7.2离线安装mysql5.7.20](#centos72%e7%a6%bb%e7%ba%bf%e5%ae%89%e8%a3%85mysql5720)
  - [安装包下载](#%e5%ae%89%e8%a3%85%e5%8c%85%e4%b8%8b%e8%bd%bd)
  - [安装新版mysql前，需将系统自带的mariadb-lib卸载](#%e5%ae%89%e8%a3%85%e6%96%b0%e7%89%88mysql%e5%89%8d%e9%9c%80%e5%b0%86%e7%b3%bb%e7%bb%9f%e8%87%aa%e5%b8%a6%e7%9a%84mariadb-lib%e5%8d%b8%e8%bd%bd)
  - [解压安装包](#%e8%a7%a3%e5%8e%8b%e5%ae%89%e8%a3%85%e5%8c%85)
  - [安装](#%e5%ae%89%e8%a3%85)
    - [mysql-community-common-5.7.20-1.el7.x86_64.rpm](#mysql-community-common-5720-1el7x8664rpm)
    - [mysql-community-libs-5.7.20-1.el7.x86_64.rpm](#mysql-community-libs-5720-1el7x8664rpm)
    - [mysql-community-client-5.7.20-1.el7.x86_64.rpm](#mysql-community-client-5720-1el7x8664rpm)
    - [mysql-community-server-5.7.20-1.el7.x86_64.rpm](#mysql-community-server-5720-1el7x8664rpm)
      - [安装libaio库](#%e5%ae%89%e8%a3%85libaio%e5%ba%93)
      - [安装server](#%e5%ae%89%e8%a3%85server)
      - [安装依赖包](#%e5%ae%89%e8%a3%85%e4%be%9d%e8%b5%96%e5%8c%85)
  - [初始化数据库](#%e5%88%9d%e5%a7%8b%e5%8c%96%e6%95%b0%e6%8d%ae%e5%ba%93)
  - [更改mysql数据库目录的所属用户及其所属组（没用创建mysql用户）](#%e6%9b%b4%e6%94%b9mysql%e6%95%b0%e6%8d%ae%e5%ba%93%e7%9b%ae%e5%bd%95%e7%9a%84%e6%89%80%e5%b1%9e%e7%94%a8%e6%88%b7%e5%8f%8a%e5%85%b6%e6%89%80%e5%b1%9e%e7%bb%84%e6%b2%a1%e7%94%a8%e5%88%9b%e5%bb%bamysql%e7%94%a8%e6%88%b7)
  - [启动mysql](#%e5%90%af%e5%8a%a8mysql)
    - [问题](#%e9%97%ae%e9%a2%98)
    - [跟踪日志/var/log/mysqld.log](#%e8%b7%9f%e8%b8%aa%e6%97%a5%e5%bf%97varlogmysqldlog)
    - [授权](#%e6%8e%88%e6%9d%83)
  - [登录到mysql，更改root用户的密码](#%e7%99%bb%e5%bd%95%e5%88%b0mysql%e6%9b%b4%e6%94%b9root%e7%94%a8%e6%88%b7%e7%9a%84%e5%af%86%e7%a0%81)
  - [创建用户，及作权限分配](#%e5%88%9b%e5%bb%ba%e7%94%a8%e6%88%b7%e5%8f%8a%e4%bd%9c%e6%9d%83%e9%99%90%e5%88%86%e9%85%8d)
  - [远程登陆授权](#%e8%bf%9c%e7%a8%8b%e7%99%bb%e9%99%86%e6%8e%88%e6%9d%83)
  - [设置mysql开机启动](#%e8%ae%be%e7%bd%aemysql%e5%bc%80%e6%9c%ba%e5%90%af%e5%8a%a8)
  - [默认配置文件路径](#%e9%bb%98%e8%ae%a4%e9%85%8d%e7%bd%ae%e6%96%87%e4%bb%b6%e8%b7%af%e5%be%84)
  - [配置默认编码为utf8](#%e9%85%8d%e7%bd%ae%e9%bb%98%e8%ae%a4%e7%bc%96%e7%a0%81%e4%b8%bautf8)
  - [参考](#%e5%8f%82%e8%80%83)

<!-- /TOC -->
# Centos7.2离线安装mysql5.7.20

## 安装包下载

https://dev.mysql.com/downloads/mysql/  
根据系统版本下载

<div align=center>

![1589454272587.png](..\images\1589454272587.png)

</div>

## 安装新版mysql前，需将系统自带的mariadb-lib卸载

```bash
[root@hadoop ~]# rpm -qa|grep mariadb
mariadb-libs-5.5.52-1.el7.x86_64
[root@hadoop ~]# rpm -e --nodeps mariadb-libs-5.5.52-1.el7.x86_64
[root@hadoop ~]# rpm -qa|grep mariadb
```

<div align=center>

![1589454331277.png](..\images\1589454331277.png)

</div>

## 解压安装包

```bash
tar -xvf mysql-5.7.20-1.el7.x86_64.rpm-bundle.tar
```

<div align=center>

![1589454370778.png](..\images\1589454370778.png)

</div>

## 安装

### mysql-community-common-5.7.20-1.el7.x86_64.rpm

```bash
rpm -ivh mysql-community-common-5.7.20-1.el7.x86_64.rpm
```

<div align=center>

![1589454434717.png](..\images\1589454434717.png)

</div>

### mysql-community-libs-5.7.20-1.el7.x86_64.rpm

```bash
rpm -ivh mysql-community-libs-5.7.20-1.el7.x86_64.rpm
```

<div align=center>

![1589454721424.png](..\images\1589454721424.png)

</div>

### mysql-community-client-5.7.20-1.el7.x86_64.rpm

```bash
rpm -ivh mysql-community-client-5.7.20-1.el7.x86_64.rpm 

```

<div align=center>

![1589454775475.png](..\images\1589454775475.png)

</div>

### mysql-community-server-5.7.20-1.el7.x86_64.rpm

在安装之前需要安装libaio  

```bash
[root@hadoop app]# rpm -qa|grep libaio
libaio-0.3.109-13.el7.x86_64
```

如果不存需要下载离线包：  
http://mirror.centos.org/centos/6/os/x86_64/Packages/  

<div align=center>

![1589454830994.png](..\images\1589454830994.png)

</div>

#### 安装libaio库

```bash
rpm -ivh libaio-0.3.107-10.el6.x86_64.rpm（若在有网情况下可执行yum install libaio）
```

#### 安装server

```bash
rpm -ivh mysql-community-server-5.7.20-1.el7.x86_64.rpm
```

出现如下错误：（最好安装服务版的centos，最小化安装会出现如下错误，处理比较麻烦，需要安装各种依赖包）

```bash
error: Failed dependencies:
	/usr/bin/perl is needed by mysql-community-server-5.7.20-1.el7.x86_64
	perl(Getopt::Long) is needed by mysql-community-server-5.7.20-1.el7.x86_64
	perl(strict) is needed by mysql-community-server-5.7.20-1.el7.x86_64
```

#### 安装依赖包

```bash
yum -y install perl*
```

<div align=center>

![1589454969007.png](..\images\1589454969007.png)

</div>

## 初始化数据库

```bash
// 指定datadir, 执行后会生成~/.mysql_secret密码文件（5.7以后不在使用）
[root@slave mytmp]# mysql_install_db --datadir=/var/lib/mysql

// 初始化，执行生会在/var/log/mysqld.log生成随机密码
[root@slave mytmp]# mysqld --initialize
```

## 更改mysql数据库目录的所属用户及其所属组（没用创建mysql用户）

```bash
chown mysql:mysql /var/lib/mysql -R
```

## 启动mysql

`systemctl start mysqld.service`

<div align=center>

![1589455054435.png](..\images\1589455054435.png)

</div>

### 问题

Process: 5924 ExecStart=/usr/sbin/mysqld --daemonize --pid-file=/var/run/mysqld/mysqld.pid $MYSQLD_OPTS (code=exited, status=1/FAILURE)

### 跟踪日志/var/log/mysqld.log

<div align=center>

![1589455100018.png](..\images\1589455100018.png)

</div>

### 授权

`chmod -R 777 mysql`  

在启动查看：  
<div align=center>

![1589455136671.png](..\images\1589455136671.png)

</div>

## 登录到mysql，更改root用户的密码

命令可以查看初始密码  

`grep 'temporary password' /var/log/mysqld.log`  

<div align=center>

![1589455206763.png](..\images\1589455206763.png)

![1589455212725.png](..\images\1589455212725.png)

</div>

`set password=password('123456a?');`  

## 创建用户，及作权限分配

```sql
CREATE USER 'sunld'@'%' IDENTIFIED BY '123456a?';

GRANT ALL PRIVILEGES ON *.* TO 'sunld'@'%';

flush privileges;
```

<div align=center>

![1589455267259.png](..\images\1589455267259.png)

</div>

## 远程登陆授权

```sql
grant all privileges on *.* to 'root'@'%' identified by '123456a?' with grant option;

flush privileges;
```

<div align=center>

![1589455303080.png](..\images\1589455303080.png)

![1589455308288.png](..\images\1589455308288.png)

</div>

## 设置mysql开机启动

```bash
// 检查是否已经是开机启动
systemctl list-unit-files | grep mysqld
// 开机启动
systemctl enable mysqld.service
```

<div align=center>

![1589455358323.png](..\images\1589455358323.png)

</div>

## 默认配置文件路径

1. 配置文件：/etc/my.cnf
2. 日志文件：/var/log/mysqld.log
3. 服务启动脚本：/usr/lib/systemd/system/mysqld.service
4. socket文件：/var/run/mysqld/mysqld.pid

## 配置默认编码为utf8

修改/etc/my.cnf配置文件，在[mysqld]下添加编码配置，如下所示：

```sql
[mysqld]
character_set_server=utf8
init_connect='SET NAMES utf8'
```

## 参考
