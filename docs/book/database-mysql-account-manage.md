<!-- TOC -->

- [Mysql账户管理](#mysql账户管理)
    - [Mysql账户体系](#mysql账户体系)
        - [服务实例级账号](#服务实例级账号)
        - [数据库级别账号-db](#数据库级别账号-db)
        - [数据表级别账号-tables_priv](#数据表级别账号-tables_priv)
        - [字段级别的权限-columns_priv](#字段级别的权限-columns_priv)
        - [存储程序级别的账号](#存储程序级别的账号)
        - [user表](#user表)
    - [配置权限](#配置权限)
        - [grant 命令语法格式](#grant-命令语法格式)
        - [创建服务实例级账号](#创建服务实例级账号)
        - [创建数据库实例账号](#创建数据库实例账号)
        - [创建数据表级别的账号](#创建数据表级别的账号)
        - [作用在表中的列上](#作用在表中的列上)
        - [作用在存储过程、函数上](#作用在存储过程函数上)
        - [权限的增删改查](#权限的增删改查)
    - [用户管理](#用户管理)
        - [查询用户](#查询用户)
        - [增加用户](#增加用户)
        - [设置用户密码](#设置用户密码)
            - [在安装validate_password插件之后会出现如下信息](#在安装validate_password插件之后会出现如下信息)
        - [删除用户](#删除用户)
    - [其他](#其他)
        - [注意事项](#注意事项)
            - [MySQL grant、revoke 用户权限注意事项](#mysql-grantrevoke-用户权限注意事项)
            - [@符号](#符号)
    - [参考](#参考)

<!-- /TOC -->
# Mysql账户管理

## Mysql账户体系

```sql
mysql> use mysql;
mysql> show tables;
+---------------------------+
| Tables_in_mysql           |
+---------------------------+
| columns_priv              |
| db                        |
| procs_priv                |
| proxies_priv              |
| tables_priv               |
| user                      |
+---------------------------+
31 rows in set (0.00 sec)
```

根据账户所具有的权限的不同，MySQL的账户可以分为以下几种：

### 服务实例级账号

如果某用户如root,拥有服务实例级分配的权限，那么该账号就可以删除所有的数据库、连同这些库中的表。

### 数据库级别账号-db

1. 该账号可以在具有权限的数据库中执行增删改查的所有操作（如果分配了这些权限）。
2. db表列出数据库，而用户有权限访问它们。在这里指定的权限适用于一个数据库中的**所有表**。

```sql
mysql> desc db;
+-----------------------+---------------+------+-----+---------+-------+
| Field                 | Type          | Null | Key | Default | Extra |
+-----------------------+---------------+------+-----+---------+-------+
| Host                  | char(60)      | NO   | PRI |         |       |
| Db                    | char(64)      | NO   | PRI |         |       |
| User                  | char(32)      | NO   | PRI |         |       |
| Select_priv           | enum('N','Y') | NO   |     | N       |       |
| Insert_priv           | enum('N','Y') | NO   |     | N       |       |
| Update_priv           | enum('N','Y') | NO   |     | N       |       |
| Delete_priv           | enum('N','Y') | NO   |     | N       |       |
| Create_priv           | enum('N','Y') | NO   |     | N       |       |
| Drop_priv             | enum('N','Y') | NO   |     | N       |       |
| Grant_priv            | enum('N','Y') | NO   |     | N       |       |
| References_priv       | enum('N','Y') | NO   |     | N       |       |
| Index_priv            | enum('N','Y') | NO   |     | N       |       |
| Alter_priv            | enum('N','Y') | NO   |     | N       |       |
| Create_tmp_table_priv | enum('N','Y') | NO   |     | N       |       |
| Lock_tables_priv      | enum('N','Y') | NO   |     | N       |       |
| Create_view_priv      | enum('N','Y') | NO   |     | N       |       |
| Show_view_priv        | enum('N','Y') | NO   |     | N       |       |
| Create_routine_priv   | enum('N','Y') | NO   |     | N       |       |
| Alter_routine_priv    | enum('N','Y') | NO   |     | N       |       |
| Execute_priv          | enum('N','Y') | NO   |     | N       |       |
| Event_priv            | enum('N','Y') | NO   |     | N       |       |
| Trigger_priv          | enum('N','Y') | NO   |     | N       |       |
+-----------------------+---------------+------+-----+---------+-------+
22 rows in set (0.00 sec)
*************************** 7. row ***************************
                 Host: %
                   Db: sunld_db
                 User: account_sunld_db
          Select_priv: Y
          Insert_priv: Y
          Update_priv: Y
          Delete_priv: Y
          Create_priv: Y
            Drop_priv: Y
           Grant_priv: Y
      References_priv: Y
           Index_priv: Y
           Alter_priv: Y
Create_tmp_table_priv: Y
     Lock_tables_priv: Y
     Create_view_priv: Y
       Show_view_priv: Y
  Create_routine_priv: Y
   Alter_routine_priv: Y
         Execute_priv: Y
           Event_priv: Y
         Trigger_priv: Y
7 rows in set (0.00 sec)
```

### 数据表级别账号-tables_priv

1. 该账号可以在具有权限的表上执行增删改查等所有操作（如果分配了这些权限）。
2. tables_priv表指定表级权限，在这里指定的一个权限适用于一个表的**所有列**。

```sql
mysql> desc tables_priv;
+-------------+-----------------------------------------------------------------------------------------------------------------------------------+------+-----+-------------------+-----------------------------+
| Field       | Type                                                                                                                              | Null | Key | Default           | Extra                       |
+-------------+-----------------------------------------------------------------------------------------------------------------------------------+------+-----+-------------------+-----------------------------+
| Host        | char(60)                                                                                                                          | NO   | PRI |                   |                             |
| Db          | char(64)                                                                                                                          | NO   | PRI |                   |                             |
| User        | char(32)                                                                                                                          | NO   | PRI |                   |                             |
| Table_name  | char(64)                                                                                                                          | NO   | PRI |                   |                             |
| Grantor     | char(93)                                                                                                                          | NO   | MUL |                   |                             |
| Timestamp   | timestamp                                                                                                                         | NO   |     | CURRENT_TIMESTAMP | on update CURRENT_TIMESTAMP |
| Table_priv  | set('Select','Insert','Update','Delete','Create','Drop','Grant','References','Index','Alter','Create View','Show view','Trigger') | NO   |     |                   |                             |
| Column_priv | set('Select','Insert','Update','References')                                                                                      | NO   |     |                   |                             |
+-------------+-----------------------------------------------------------------------------------------------------------------------------------+------+-----+-------------------+-----------------------------+
8 rows in set (0.00 sec)
*************************** 4. row ***************************
       Host: %
         Db: sunld_db
       User: account_sunld_table
 Table_name: tb_a
    Grantor: root@localhost
  Timestamp: 0000-00-00 00:00:00
 Table_priv: Select,Insert,Update,Delete,Create,Drop,References,Index,Alter,Create View,Show view,Trigger
Column_priv:
```

### 字段级别的权限-columns_priv

1. 该账号可以对某些表中具有权限的字段进行操作（取决于所分配的权限）。
2. columns_priv表指定列级权限。这里指定的权限适用于一个表的特定列。

```sql
mysql> desc columns_priv;
+-------------+----------------------------------------------+------+-----+-------------------+-----------------------------+
| Field       | Type                                         | Null | Key | Default           | Extra                       |
+-------------+----------------------------------------------+------+-----+-------------------+-----------------------------+
| Host        | char(60)                                     | NO   | PRI |                   |                             |
| Db          | char(64)                                     | NO   | PRI |                   |                             |
| User        | char(32)                                     | NO   | PRI |                   |                             |
| Table_name  | char(64)                                     | NO   | PRI |                   |                             |
| Column_name | char(64)                                     | NO   | PRI |                   |                             |
| Timestamp   | timestamp                                    | NO   |     | CURRENT_TIMESTAMP | on update CURRENT_TIMESTAMP |
| Column_priv | set('Select','Insert','Update','References') | NO   |     |                   |                             |
+-------------+----------------------------------------------+------+-----+-------------------+-----------------------------+
7 rows in set (0.00 sec)
```

### 存储程序级别的账号

该账号可以对存储程序进行增删改查的操作（取决于所分配的权限）

### user表

user表列出可以连接服务器的用户及其口令，并且它指定他们有哪种全局（超级用户）权限。在user表启用的任何权限均是全局权限，并适用于所有数据库。例如，如果你启用了DELETE权限，在这里列出的用户可以从任何表中删除记录，所以在你这样做之前要认真考虑。 

```sql
mysql> desc user;
+------------------------+-----------------------------------+------+-----+-----------------------+-------+
| Field                  | Type                              | Null | Key | Default               | Extra |
+------------------------+-----------------------------------+------+-----+-----------------------+-------+
| Host                   | char(60)                          | NO   | PRI |                       |       |
| User                   | char(32)                          | NO   | PRI |                       |       |
| Select_priv            | enum('N','Y')                     | NO   |     | N                     |       |
| Insert_priv            | enum('N','Y')                     | NO   |     | N                     |       |
| Update_priv            | enum('N','Y')                     | NO   |     | N                     |       |
| Delete_priv            | enum('N','Y')                     | NO   |     | N                     |       |
| Create_priv            | enum('N','Y')                     | NO   |     | N                     |       |
| Drop_priv              | enum('N','Y')                     | NO   |     | N                     |       |
| Reload_priv            | enum('N','Y')                     | NO   |     | N                     |       |
| Shutdown_priv          | enum('N','Y')                     | NO   |     | N                     |       |
| Process_priv           | enum('N','Y')                     | NO   |     | N                     |       |
| File_priv              | enum('N','Y')                     | NO   |     | N                     |       |
| Grant_priv             | enum('N','Y')                     | NO   |     | N                     |       |
| References_priv        | enum('N','Y')                     | NO   |     | N                     |       |
| Index_priv             | enum('N','Y')                     | NO   |     | N                     |       |
| Alter_priv             | enum('N','Y')                     | NO   |     | N                     |       |
| Show_db_priv           | enum('N','Y')                     | NO   |     | N                     |       |
| Super_priv             | enum('N','Y')                     | NO   |     | N                     |       |
| Create_tmp_table_priv  | enum('N','Y')                     | NO   |     | N                     |       |
| Lock_tables_priv       | enum('N','Y')                     | NO   |     | N                     |       |
| Execute_priv           | enum('N','Y')                     | NO   |     | N                     |       |
| Repl_slave_priv        | enum('N','Y')                     | NO   |     | N                     |       |
| Repl_client_priv       | enum('N','Y')                     | NO   |     | N                     |       |
| Create_view_priv       | enum('N','Y')                     | NO   |     | N                     |       |
| Show_view_priv         | enum('N','Y')                     | NO   |     | N                     |       |
| Create_routine_priv    | enum('N','Y')                     | NO   |     | N                     |       |
| Alter_routine_priv     | enum('N','Y')                     | NO   |     | N                     |       |
| Create_user_priv       | enum('N','Y')                     | NO   |     | N                     |       |
| Event_priv             | enum('N','Y')                     | NO   |     | N                     |       |
| Trigger_priv           | enum('N','Y')                     | NO   |     | N                     |       |
| Create_tablespace_priv | enum('N','Y')                     | NO   |     | N                     |       |
| ssl_type               | enum('','ANY','X509','SPECIFIED') | NO   |     |                       |       |
| ssl_cipher             | blob                              | NO   |     | NULL                  |       |
| x509_issuer            | blob                              | NO   |     | NULL                  |       |
| x509_subject           | blob                              | NO   |     | NULL                  |       |
| max_questions          | int(11) unsigned                  | NO   |     | 0                     |       |
| max_updates            | int(11) unsigned                  | NO   |     | 0                     |       |
| max_connections        | int(11) unsigned                  | NO   |     | 0                     |       |
| max_user_connections   | int(11) unsigned                  | NO   |     | 0                     |       |
| plugin                 | char(64)                          | NO   |     | mysql_native_password |       |
| authentication_string  | text                              | YES  |     | NULL                  |       |
| password_expired       | enum('N','Y')                     | NO   |     | N                     |       |
| password_last_changed  | timestamp                         | YES  |     | NULL                  |       |
| password_lifetime      | smallint(5) unsigned              | YES  |     | NULL                  |       |
| account_locked         | enum('N','Y')                     | NO   |     | N                     |       |
+------------------------+-----------------------------------+------+-----+-----------------------+-------+
45 rows in set (0.00 sec)
*************************** 12. row ***************************
                  Host: %
                  User: account_sunld_db_2
           Select_priv: N
           Insert_priv: N
           Update_priv: N
           Delete_priv: N
           Create_priv: N
             Drop_priv: N
           Reload_priv: N
         Shutdown_priv: N
          Process_priv: N
             File_priv: N
            Grant_priv: N
       References_priv: N
            Index_priv: N
            Alter_priv: N
          Show_db_priv: N
            Super_priv: N
 Create_tmp_table_priv: N
      Lock_tables_priv: N
          Execute_priv: N
       Repl_slave_priv: N
      Repl_client_priv: N
      Create_view_priv: N
        Show_view_priv: N
   Create_routine_priv: N
    Alter_routine_priv: N
      Create_user_priv: N
            Event_priv: N
          Trigger_priv: N
Create_tablespace_priv: N
              ssl_type: 
            ssl_cipher: 
           x509_issuer: 
          x509_subject: 
         max_questions: 0
           max_updates: 0
       max_connections: 0
  max_user_connections: 0
                plugin: mysql_native_password
 authentication_string: *6BB4837EB74329105EE4568DDA7DC67ED2CA2AD9
      password_expired: N
 password_last_changed: 2017-12-30 12:33:32
     password_lifetime: NULL
        account_locked: N
```

## 配置权限

### grant 命令语法格式

```sql
grant 权限名称[字段列表] on [数据库资源类型]数据库资源 to MySQL账户1,[MySQL账户2] [with grant option]

#增删改数据库数据权限
grant select, insert, update, delete on ****
#创建、修改、删除 MySQL 数据表结构权限
grant create ,alter  ,drop   on ****
#操作 MySQL 外键权限。
grant references on ****
#操作 MySQL 临时表权限
grant create temporary tables on ****
#操作 MySQL 索引权限
grant index on ****
#操作 MySQL 视图、查看视图源代码 权限
grant create view on ****
grant show   view on ****
#操作 MySQL 存储过程、函数 权限
grant create routine on ****; -- now, can show procedure status
grant alter  routine on ****; -- now, you can drop a procedure
grant execute        on ****;
#普通 DBA 管理某个 MySQL 数据库的权限
grant all privileges on db*** to
其中，关键字 “privileges” 可以省略。
#高级 DBA 管理 MySQL 中所有数据库的权限
grant all on *.* to
```

### 创建服务实例级账号

```sql
# 创建账号sunld_all，拥有所有数据库权限，并且具有grant权限，
# 可以创建其他拥有服务实例权限的其他用户。
# 查询
mysql> show grants for account_sunld_all;
ERROR 1141 (42000): There is no such grant defined for user 'account_sunld_all' on host '%'
# 创建
grant all privileges on *.* to 'account_sunld_all'@'%' identified by '123456' with grant option;

flush privileges;
# 查询
mysql> show grants for account_sunld_all;
+--------------------------------------------------------------------------+
| Grants for account_sunld_all@%                                           |
+--------------------------------------------------------------------------+
| GRANT ALL PRIVILEGES ON *.* TO 'account_sunld_all'@'%' WITH GRANT OPTION |
+--------------------------------------------------------------------------+
1 row in set (0.00 sec)
```

### 创建数据库实例账号

```sql
#创建账号sunld_db,拥有sunlddb数据库的所有权限，可以对该库中的表进行所有操作。
#查询
mysql> show grants for account_sunld_db;
ERROR 1141 (42000): There is no such grant defined for user 'account_sunld_db' on host '%'
#创建
grant all privileges on sunld_db.* to 'account_sunld_db'@'%' identified by '123456' with grant option;
#查询
mysql> show grants for account_sunld_db;
+----------------------------------------------------------------------------------+
| Grants for account_sunld_db@%                                                    |
+----------------------------------------------------------------------------------+
| GRANT USAGE ON *.* TO 'account_sunld_db'@'%'                                     |
| GRANT ALL PRIVILEGES ON `sunld_db`.* TO 'account_sunld_db'@'%' WITH GRANT OPTION |
+----------------------------------------------------------------------------------+
2 rows in set (0.00 sec)
```

### 创建数据表级别的账号

```sql
#创建账号sunld_table，对sunlddb数据中的tb_a表拥有所有权限
#查询
mysql> show grants for account_sunld_table;
ERROR 1141 (42000): There is no such grant defined for user 'account_sunld_table' on host '%'
#创建
grant all privileges on table sunld_db.tb_a to 'account_sunld_table'@'%' identified by '123456';
#查询
mysql> show grants for account_sunld_table;
+------------------------------------------------------------------------+
| Grants for account_sunld_table@%                                       |
+------------------------------------------------------------------------+
| GRANT USAGE ON *.* TO 'account_sunld_table'@'%'                        |
| GRANT ALL PRIVILEGES ON `sunld_db`.`tb_a` TO 'account_sunld_table'@'%' |
+------------------------------------------------------------------------+
2 rows in set (0.00 sec)

#这里在给一个用户授权多张表时，可以多次执行以上语句。例如：
grant select(user_id,username) on smp.users to mo_user@'%' identified by '123345';
grant select on smp.mo_sms to mo_user@'%' identified by '123345';
```

### 作用在表中的列上

```sql
grant select(id, se, rank) on testdb.apache_log to dba@localhost;
```

### 作用在存储过程、函数上

```sql
grant execute on procedure testdb.pr_add to 'dba'@'localhost'
grant execute on function testdb.fn_add to 'dba'@'localhost'
```

字段设置冗余复杂，则每次MySQL在进行SQL执行的时候回进行同样复杂的权限判断，造成效率降低性能下降，同时也会存在安全隐患。因此简单、易管理的、高可用的权限才是可取的。

### 权限的增删改查

```sql
#查询
mysql> show grants for account_sunld_db_1;
ERROR 1141 (42000): There is no such grant defined for user 'account_sunld_db_1' on host '%'
#比如原本的权限为：
grant select,insert on sunld_db.* to 'account_sunld_db_1'@'%' identified by '123456' with grant option;
#查询
mysql> show grants for account_sunld_db_1;
+------------------------------------------------------------------------------------+
| Grants for account_sunld_db_1@%                                                    |
+------------------------------------------------------------------------------------+
| GRANT USAGE ON *.* TO 'account_sunld_db_1'@'%'                                     |
| GRANT SELECT, INSERT ON `sunld_db`.* TO 'account_sunld_db_1'@'%' WITH GRANT OPTION |
+------------------------------------------------------------------------------------+
2 rows in set (0.00 sec)
#想要增加update, delete,alter 权限可以如下操作：
grant update,delete,alter on sunld_db.* to 'account_sunld_db_1'@'%' identified by '123456' with grant option;
#然后使用
show grants for account_sunld_db_1;
mysql> show grants for account_sunld_db_1;
+-----------------------------------------------------------------------------------------------------------+
| Grants for account_sunld_db_1@%                                                                           |
+-----------------------------------------------------------------------------------------------------------+
| GRANT USAGE ON *.* TO 'account_sunld_db_1'@'%'                                                            |
| GRANT SELECT, INSERT, UPDATE, DELETE, ALTER ON `sunld_db`.* TO 'account_sunld_db_1'@'%' WITH GRANT OPTION |
+-----------------------------------------------------------------------------------------------------------+
2 rows in set (0.00 sec)
#删除权限insert
revoke insert on sunld_db.* from  'account_sunld_db_1'@'%';
查看
mysql> show grants for account_sunld_db_1;
+---------------------------------------------------------------------------------------------------+
| Grants for account_sunld_db_1@%                                                                   |
+---------------------------------------------------------------------------------------------------+
| GRANT USAGE ON *.* TO 'account_sunld_db_1'@'%'                                                    |
| GRANT SELECT, UPDATE, DELETE, ALTER ON `sunld_db`.* TO 'account_sunld_db_1'@'%' WITH GRANT OPTION |
+---------------------------------------------------------------------------------------------------+
2 rows in set (0.00 sec)
```

## 用户管理

用户管理也无非是增加用户、删除用户、密码修改和授权之类的。

### 查询用户

查询数据库mysql下的表user;

```sql
mysql> select user,host from user;
+----------------------------+-----------+
| user                       | host      |
+----------------------------+-----------+
| account_sunld_all          | %         |
| account_sunld_db           | %         |
| account_sunld_db_1         | %         |
| account_sunld_table        | %         |
| inspurCloudDB              | %         |
| inspurCloudCheckDBDiskSize | 127.0.0.1 |
| inspurCloudDB              | 127.0.0.1 |
| root                       | 127.0.0.1 |
| inspurCloudDB              | localhost |
| mysql.sys                  | localhost |
| root                       | localhost |
+----------------------------+-----------+
11 rows in set (0.00 sec)
```

### 增加用户

```sql
create user 'USER_NAME'@'HOST' identified by 'PASSOWRD';
#例子
create user 'account_sunld_db_2'@'%' identified by '123456';
```

### 设置用户密码

```sql
set password=password('123456');
```

其实这个命令不仅能够设置当前用户的密码，也可以设置其他用户的密码，这个命令的完整格式是：

```sql
set password [for USER] = passowrd('新密码');
#例子
set password for account_sunld_db_2=password('123456');
```

#### 在安装validate_password插件之后会出现如下信息

ERROR 1819 (HY000): Your password does not satisfy the current policy requirements  

```sql
mysql> SHOW VARIABLES LIKE 'validate_password%';
+--------------------------------------+--------+
| Variable_name                        | Value  |
+--------------------------------------+--------+
| validate_password_check_user_name    | OFF    |
| validate_password_dictionary_file    |        |
| validate_password_length             | 8      |
| validate_password_mixed_case_count   | 1      |
| validate_password_number_count       | 1      |
| validate_password_policy             | MEDIUM |
| validate_password_special_char_count | 1      |
+--------------------------------------+--------+
7 rows in set (0.00 sec)
```

validate_password_policy有以下取值：  

<style type="text/css">
.tg  {border-collapse:collapse;border-color:#bbb;border-spacing:0;}
.tg td{background-color:#E0FFEB;border-color:#bbb;border-style:solid;border-width:1px;color:#594F4F;
  font-family:Arial, sans-serif;font-size:14px;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg th{background-color:#9DE0AD;border-color:#bbb;border-style:solid;border-width:1px;color:#493F3F;
  font-family:Arial, sans-serif;font-size:14px;font-weight:normal;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg .tg-0lax{text-align:left;vertical-align:top}
</style>
<table class="tg">
<thead>
  <tr>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;Policy&nbsp;&nbsp;&nbsp;</th>
    <th class="tg-0lax">&nbsp;&nbsp;&nbsp;Tests Performed&nbsp;&nbsp;&nbsp;</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;0 or LOW&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;Length&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;1 or MEDIUM&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;Length;&nbsp;&nbsp;&nbsp;numeric, lowercase/uppercase, and special characters&nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;2 or STRONG&nbsp;&nbsp;&nbsp;</td>
    <td class="tg-0lax">&nbsp;&nbsp;&nbsp;Length;&nbsp;&nbsp;&nbsp;numeric, lowercase/uppercase, and special characters; dictionary file&nbsp;&nbsp;&nbsp;</td>
  </tr>
</tbody>
</table>

```sql
mysql> set global validate_password_policy=0;
mysql> set global validate_password_length=1;
mysql> set password=password('123456a?');
```

### 删除用户

```sql
drop user USER_NAME；
#例子
drop user account_sunld_db_2;
```

## 其他

### 注意事项

#### MySQL grant、revoke 用户权限注意事项

1. grant, revoke 用户权限后，该用户只有重新连接 MySQL 数据库，权限才能生效。
2. 如果想让授权的用户，也可以将这些权限 grant 给其他用户，需要选项 “grant option“,grant select on testdb.* to 'dba'@'localhost' with grant option;这个特性一般用不到。实际中，数据库权限最好由 DBA 来统一管理。

#### @符号

当不加@选项时，效果与加@'%'是一样的，'%'从名义上包括任何主机，（%必须加上引号，不然与@放在一起可能不会被辨认出。）不过有些时候（有些版本）'%'不包括localhost，要单独对@'localhost'进行赋值。

## 参考

1. [mysql 用户管理和权限设置](https://www.cnblogs.com/fslnet/p/3143344.html)
2. [MySQL- MySQL的Grant命令](https://www.cnblogs.com/hcbin/archive/2010/04/23/1718379.html)
3. [mysql 数据库授权(给某个用户授权某个数据库)](https://www.cnblogs.com/jifeng/archive/2011/03/06/1972183.html)
4. [ERROR 1819 (HY000): Your password does not satisfy the current policy requirements](https://www.cnblogs.com/ivictor/p/5142809.html)
5. [MySQL账户管理](https://www.cnblogs.com/roverliang/p/6444512.html)
