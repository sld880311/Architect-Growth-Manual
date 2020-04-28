<!-- TOC -->

- [JDK研发环境设置](#jdk%e7%a0%94%e5%8f%91%e7%8e%af%e5%a2%83%e8%ae%be%e7%bd%ae)
  - [Windows版本](#windows%e7%89%88%e6%9c%ac)
    - [版本说明](#%e7%89%88%e6%9c%ac%e8%af%b4%e6%98%8e)
    - [配置步骤](#%e9%85%8d%e7%bd%ae%e6%ad%a5%e9%aa%a4)
    - [测试](#%e6%b5%8b%e8%af%95)
  - [Linux版本](#linux%e7%89%88%e6%9c%ac)

<!-- /TOC -->
# JDK研发环境设置

## Windows版本

### 版本说明

1. 操作系统版本：Window10 X64，其他操作系统类似
2. JDK版本：jdk1.8.0_192，安装后目录：`C:\Program Files\Java\jdk1.8.0_192`

### 配置步骤

1. 打开环境变量配置页面
   
<div align=center>

![1588070689018.png](..\images\1588070689018.png)

</div>
2. 在环境变量中新建`JAVA_HOME`,输入实际地址`C:\Program Files\Java\jdk1.8.0_192`
<div align=center>

![1588070882938.png](..\images\1588070882938.png)

</div>
3. 修改Path：找到Path点击编辑打开编辑页面，在编辑页面中新增JDK的相关配置`%JAVA_HOME%\bin`
<div align=center>

![1588071036233.png](..\images\1588071036233.png)

</div>
4. 增加classpath，输入为`.;%JAVA_HOME%\lib;%JAVA_HOME%\lib\tools.jar`
<div align=center>

![1588071135219.png](..\images\1588071135219.png)

</div>
5. 配置完成之后保存，并且关闭环境变量配置的相关窗口

### 测试

重新打开cmd窗口输入`java`和`java`显示出对应的信息，则表示配置成功。

## Linux版本
