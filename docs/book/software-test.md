
# 系统工程之测试

## 测试分类

### 单元测试

1. 软件测试的最基本单元，针对软件中的基本组成部分进行测试，比如一个模块、一个方法；
2. 目的是验证最小单位的正确性，其正确性依赖详细设计
3. 主要方法有控制流测试、数据流测试、排错测试、分域测试等等。
4. 过程：研发整个过程中需要实施，尤其是针对关键核心代码的准确性测试
5. 优点： 
   - 一个好的单元测试将会在产品开发的阶段发现大部分的缺陷，并且修改他们的成本也很低；
   - 在软件开发的后期阶段，缺陷的修改将会变得更加困难，要消耗大量的时间和费用。
   - 经过单元测试的系统，系统集成过程将会达达的简化。

### 集成测试

1. 集成测试发生在模块交互和UI集成阶段
2. 目的是检查软件单位之间的接口是否正确。
3. 策略主要有自顶向下和自底向上两种。
4. 集成测试也叫做组装测试，通常在单元测试的基础上，将所有的程序模块进行有序的、递增的测试。
5. 集成测试是检验程序单元或部件的接口关系，逐步集成为符合设计要求的程序部件或整个系统。
6. 软件集成的过程是一个持续的过程，会形成很多个临时版本，在每个版本提交时，都需要进行冒烟测试，即对程序主要功能进行验证。

### 系统测试

系统测试是对已经集成好的软件系统进行彻底的测试，以验证软件系统的正确性和性能等满足其规约所指定的要求，检查软件的行为和输出是否正确并非一项简单的任务，它被称为测试的“先知者问题”。因此，系统测试应该按照测试计划进行，其输入、输出和其他动态运行行为应该与软件规约进行对比。软件系统测试方法很多，主要有功能测试、性能测试、随机测试等等。


### 验收测试

验收测试旨在向软件的购买者展示该软件系统满足其用户的需求。它的测试数据通常是系统测试的测试数据的子集。所不同的是，验收测试常常有软件系统的购买者代表在现场，甚至是在软件安装使用的现场。这是软件在投入使用之前的最后测试。

验收测试（用户接受度测试、用户体验测试，UAT：user acceptance test）

(1).alpha测试：由最终的用户在开发的环境中，对软件进行测试（在实际中已经常由开发方自主完成）；

(2).beta测试：由最终的用户在实际的环境中进行测试使用。对于一些没有固定用户群体的公共类软件（办公软件，游戏，输入法），一般会发行公测版（beta版），让用用户免费使用，发现bug后进行信息反馈。

## 测试的价值

1. 测试越多，额外测试的价值越少。第一个测试最有可能是针对代码**最重要的区域**，因此带来高价值与高风险。当我们为几乎所有事情编写测试后，那些仍然没有测试覆盖的地方很有可能是最不重要和最不可能破坏的。
<div align=center>

![1597710996282.png](../images/1597710996282.png)

</div>

2. 第一个稳态表明编写更多更好的测试不再带来额外的价值。第二个稳态进一步爬升，从我们想法的改变中发现更多的回报（将测试认为是丰富的资源，而不仅仅是验证工具，可以出尽高质量代码的编写和个人思考问题的提升）--**影响生产力**
<div align=center>

![1597711435963.png](../images/1597711435963.png)

![1597717234915.png](../images/1597717234915.png)
单元测试对生产力的影响

</div>

## 单元测试

### 规范

#### 测试准则

1. 满足AIR原则
   - A：Automatic（自动化）
   - I：Independent（独立性）
   - R：Repeatable（可重复）
2. [符合27条准则](https://blog.csdn.net/neo_ustc/article/details/22612759)

#### 结构规范

1. 目录结构：不要与源码混合在一起，与源码的路径保持一致
2. 命名规范：与源码具有一致性要求，并且体现其测试功能，比如源码UserService，测试代码应该是UserServiceTest

#### 步骤

1. Arrange：准备当前测试场景的前置条件
2. Action：执行被测试方法
3. Assert：对被测试方法的执行结果进行断言判断（几乎每种测试框架都提供了Assert库，当然，也可以根据情况丰富我们自己的Assert库）

### 编码原则

#### 必须符合 BCDE 原则

1. B：Border，边界值测试，包括循环边界、特殊取值、特殊时间点、数据顺序等。
2. C：Correct，正确的输入，并得到预期的结果。
3. D：Design，与设计文档相结合，来编写单元测试。
4. E：Error，强制错误信息输入（如：非法数据、异常流程、非业务允许输入等），并得到预期的结果。

#### 避免以下情况

1. 构造方法中做的事情过多。
2. 存在过多的全局变量和静态方法。
3. 存在过多的外部依赖。
4. 存在过多的条件语句。

#### 建议

1. 涉及到的某些扩展模块可以使用mock模拟
2. 测试用例不要使用@ignored或者被注释掉，切记切记。

### 测试用例的设计

1. 使用一种或多种白盒测试方法分析模块的逻辑关系
2. 使用黑盒测试方法依照需求说明补充测试用例

#### 接口功能性测试

接口功能的正确性,即保证接口能够被正常调用，并输出有效数据!
1. 是否被顺利调用
2. 参数是否符合预期

#### 局部数据结构测试
保证数据结构的正确性
1. 变量是否有初始值或在某场景下是否有默认值
2. 变量是否溢出

#### 边界条件测试
1. 变量无赋值(null)
2. 变量是数值或字符
3. 主要边界：最大值，最小值，无穷大
4. 溢出边界：在边界外面取值+/-1
5. 临近边界：在边界值之内取值+/-1
6. 字符串的边界，引用 "变量字符"的边界
7. 字符串的设置，空字符串
8. 字符串的应用长度测试
9. 空白集合
10. 目标集合的类型和应用边界
11. 集合的次序
12. 变量是规律的，测试无穷大的极限，无穷小的极限

##### 逻辑性测试

1. 精度问题
2. 死代码
3. 表达式判断
4. 具体的业务逻辑（需要参与详细设计进行评估测试，并且加强代码走查），**比如数据的使用范围，数据的最终一致性，数据的依赖性**

#### 所有独立代码测试
保证每一句代码，所有分支都测试完成，主要包括代码覆盖率，异常处理通路测试
1. 语句覆盖率：每个语句都执行到了
2. 判定覆盖率：每个分支都执行到了
3. 条件覆盖率：每个条件都返回布尔
4. 路径覆盖率：每个路径都覆盖到了

#### 异常模块测试

后续处理模块测试:是否包闭当前异常或者对异常形成消化,是否影响结果!

### 测试目标

**语句覆盖率：>=70% 分支覆盖率：100% 函数覆盖率：100% 行覆盖率： >=80%**

最主要的目的是发现bug，解决bug，提高研发生产力。

### 编写单元测试的技巧

1. **不应该编写成功通过的单元测试**：以发现问题的角度去编写测试用例，让测试用例逐步变成通过
2. **测试类应该只测试一个功能**
3. **测试类具备可读性**，规范的注释，以及相关的测试用例
4. **良好的命名规范**
5. **把断言从行为中分离出来**：你的断言应该用来检验结果，而不是执行逻辑操作的。
6. **使用具体的输入**-不要使用任何的自动化测试数据来输入，像date()这些产生的数据会引入差异。
7. **把测试类分类**，放在不同的地方-从逻辑的角度看，当没有错误指向特定的问题时这更容易去查找。
8. **好的测试都是**：一些独立的测试类-你应该让测试类与其他的测试、环境设置等没有任何依赖。这利于创建多个测试点。
9. **不要包含私有的方法**：他们都是一些具体的实现，不应该包含在单元测试里。
10. **不要连接数据库或者数据源**-这是不靠谱的。因为你不能确保数据服务总是一样的并且能够创建测试点，创建用于测试的数据库，并且快速完成数据的回收
11. **一个测试不要超过一个模拟(mock对象)**-我们努力去消除错误和不一致性。
12. **单元测试不是集成测试**-如果你想测试结果，不要使用单元测试。
13. **测试必须具有确定性**-你需要一个确定的预测结果，所以，如果有时候测试通过了，但是不意味着完成测试了。
14. **保持你的测试是幂等的**-你应该能够运行你的测试多次而不改变它的输出结果，并且测试也不应该改变任何的数据或者添加任何东西。无论是运行一次还是一百万次，它的效果都应该是一样的。
15. **测试类一次仅测试一个类，测试方法一次仅测试一个方法**-组织方法能够在问题出现时检测出来，并帮你确定测试依赖。
16. **在你的测试里使用异常**-你在测试里会遇到异常，所以，请不要忽略它，要使用它。
17. **不要使用你自己的测试类去测试第三方库的功能**-大多数好的库都应该有它们自己的测试，如果没考虑用mocks去产生一致性的结果的话。
18. **限制规则**-当在一些规则下写测试时，记住你的限制和它们（最小和最大）设置成最大的一致性。
19. **测试类不应该需要配置或者自定义安装**-你的测试类应该能够给任何人使用并且使它运行。“在我的机器上运行”不应该出现在这。

## 基于Python unittest的示例

### 准备测试对象

```python
# UnitTest/unit_test/course.py
class CourseManage(object):

    def __init__(self, course):
        self.course = course
        self.students = []

    def show_course(self):
        print("课程:", self.course)

    def add_student(self, name):
        self.students.append(name)

    def show_students(self):
        print("所有学员:")
        for student in self.students:
            print('-', student)
```

### 正常使用该对象

```python
from unit_test.course import CourseManage

course = CourseManage("Python")
course.show_course()
print("准备录入学员...")
print("Enter 'q' at any time to quit.\n")
while True:
    resp = input("Student's Name: ")
    if resp == 'q':
        break
    if resp:
        course.add_student(resp.title())
print("\n录入完毕...")
course.show_students()
```

数据正常输出：

```python
课程: Python
准备录入学员...
Enter 'q' at any time to quit.

Student's Name: oliver queen
Student's Name: barry allen
Student's Name: kara
Student's Name: sara lance
Student's Name: q

录入完毕...
所有学员:
- Oliver Queen
- Barry Allen
- Kara
- Sara Lance

Process finished with exit code 0
```

### 编写测试用例

```python
# UnitTest/unit_test/test/test_course.py
# 添加用户，使用断言assertIn验证
import unittest
from unit_test.course import CourseManage

class TestCourseManage(unittest.TestCase):

    def test_add_student(self):
        course = CourseManage("Python")
        name = 'snart'
        course.add_student(name.title())
        self.assertIn('Snart', course.students)

if __name__ == '__main__':
    unittest.main()
```

### 常用断言方法

模块在unittest.TestCase类中提供了很多断言方法，之前已经用一个了。下面是6个常用的断言方法：
1. assertEqual(a, b) ： 核实a == b
2. assertNotEqual(a, b) ： 核实a != b
3. assertTrue(x) ： 核实x为True
4. assertFalse(x) ： 核实x为False
5. assertIn(item, list) ： 核实item在list中
6. assertNotIn(item, list) ： 核实item不在list中

### 特殊方法说明

1. setUp()&tearDown()：在每个测试方法（用例）运行时被调用一次
2. setUp()主要实现测试前的初始化工作，而tearDown()则主要实现测试完成后的垃圾回收等工作 
3. setUpClass()&tearDownClass(): 全程只调用一次，必须使用@classmethod 装饰器

## 其他

### 如何快速推荐单元测试

需要有研发流程和研发文化的支持。

<div align=center>

![1597716819955.png](../images/1597716819955.png)

</div>

## 参考

1. [Unit Testing Guidelines](https://petroware.no/html/unittesting.html)
2. [单元测试的用例清单](https://cloud.tencent.com/developer/article/1570016)
3. [实践单元测试的姿势](https://cloud.tencent.com/developer/article/1005432)
4. [Golang UnitTest单元测试](https://cloud.tencent.com/developer/article/1399249)
5. [Python的单元测试](https://cloud.tencent.com/developer/article/1382018)
6. [Python 单元测试（unittest)](https://cloud.tencent.com/developer/article/1564228)
7. [Java单元测试之JUnit 5快速上手](https://cloud.tencent.com/developer/article/1509641)