<!-- TOC -->

- [eclipse使用技巧](#eclipse%e4%bd%bf%e7%94%a8%e6%8a%80%e5%b7%a7)
  - [集成javap命令](#%e9%9b%86%e6%88%90javap%e5%91%bd%e4%bb%a4)
    - [配置](#%e9%85%8d%e7%bd%ae)
    - [使用](#%e4%bd%bf%e7%94%a8)
    - [javap的命令说明](#javap%e7%9a%84%e5%91%bd%e4%bb%a4%e8%af%b4%e6%98%8e)

<!-- /TOC -->
# eclipse使用技巧

## 集成javap命令

### 配置

1. eclipse中点击工具栏: Run > External Tools > External Tools Configuration
2. 双击”程序”
3. 修改配置

<div align=center>

![1588039059895.png](..\images\1588039059895.png)

</div>

```java
Name: javap(随意)
location: jdk实际路径
Working Directory:${workspace_loc}\${project_name} （不要改）
Arguments:-c -verbose  -classpath  ${workspace_loc}/ ${project_name}/bin/${java_type_name}
```

### 使用

<div align=center>

![1588039225454.png](..\images\1588039225454.png)

</div>

### javap的命令说明

参考《[javap详解](book/javap.md)》
