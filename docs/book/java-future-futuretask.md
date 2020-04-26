# 异步结果计算

## 任务提交与执行者的关系

Task Submitter把任务提交给Executor执行，他们之间需要一种 通讯手段，这种手段的具体实现，通常叫做Future。Future通常 包括get（阻塞至任务完成）， cancel，get(timeout)（等待一 段时间）等等。Future也用于异步变同步的场景。

<div align=center>

![1587887264164.png](..\images\1587887264164.png)

</div>
