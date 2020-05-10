3	Spring
它是一个全面的、企业应用开发一站式的解决方案，贯穿表现层、业务层、持久层。但是 Spring 仍然可以和其他的框架无缝整合。Spring 特点 ：轻量级、控制反转、面向切面、容器、框架集合

<div align=center>

![1589097744385.png](..\images\1589097744385.png)

</div>

3.1	Spring 核心组件 

<div align=center>

![1589097771826.png](..\images\1589097771826.png)

</div>

3.2	Spring 常用模块 

<div align=center>

![1589097794875.png](..\images\1589097794875.png)

</div>

3.3	Spring 主要包 

<div align=center>

![1589097818671.png](..\images\1589097818671.png)

</div>

3.4	Spring 常用注解 

bean 注入与装配的的方式有很多种，可以通过 xml，get set 方式，构造函数或者注解等。简单易用的方式就是使用 Spring 的注解了，Spring 提供了大量的注解方式。 
<div align=center>

![1589097851270.png](..\images\1589097851270.png)

</div>

3.5	Spring 第三方结合 

<div align=center>

![1589097875312.png](..\images\1589097875312.png)

</div>

3.6	Spring IOC 原理 

1.1.1	概念 
Spring 通过一个配置文件描述 Bean 及 Bean 之间的依赖关系，利用 Java 语言的反射功能实例化 Bean 并建立 Bean 之间的依赖关系。 Spring 的 IoC 容器在完成这些底层工作的基础上，还提供了 Bean 实例缓存、生命周期管理、 Bean 实例代理、事件发布、资源装载等高级服务。 
1.1.2	Spring 容器高层视图 
Spring 启动时读取应用程序提供的 Bean 配置信息，并在 Spring 容器中生成一份相应的 Bean 配置注册表，然后根据这张注册表实例化 Bean，装配好 Bean 之间的依赖关系，为上层应用提供准备就绪的运行环境。其中 Bean 缓存池为 HashMap 实现 

<div align=center>

![1589097930337.png](..\images\1589097930337.png)

</div>

 
1.1.1	IOC 容器实现 
1.1.1.1	BeanFactory-框架基础设施 
BeanFactory 是 Spring 框架的基础设施，面向 Spring 本身；ApplicationContext 面向使用 Spring 框架的开发者，几乎所有的应用场合我们都直接使用 ApplicationContext 而非底层的 BeanFactory。 

<div align=center>

![1589097963803.png](..\images\1589097963803.png)

</div>

1.1.1.1.1	BeanDefinitionRegistry注册表 
1.  Spring 配置文件中每一个节点元素在 Spring 容器里都通过一个 BeanDefinition 对象表示，它描述了 Bean 的配置信息。而 BeanDefinitionRegistry 接口提供了向容器手工注册 BeanDefinition 对象的方法。 
1.1.1.1.2	BeanFactory 顶层接口 
1.	位于类结构树的顶端 ，它最主要的方法就是 getBean(String beanName)，该方法从容器中返回特定名称的 Bean，BeanFactory 的功能通过其他的接口得到不断扩展： 
1.1.1.1.3	ListableBeanFactory 
3. 该接口定义了访问容器中 Bean 基本信息的若干方法，如查看 Bean 的个数、获取某一类型 Bean 的配置名、查看容器中是否包括某一 Bean 等方法； 
1.1.1.1.4	HierarchicalBeanFactory父子级联 
4. 	父子级联 IoC 容器的接口，子容器可以通过接口方法访问父容器； 通过 
HierarchicalBeanFactory 接口， Spring 的 IoC 容器可以建立父子层级关联的容器体系，子容器可以访问父容器中的 Bean，但父容器不能访问子容器的 Bean。Spring 使用父子容器实现了很多功能，比如在 Spring MVC 中，展现层 Bean 位于一个子容器中，而业务层和持久层的 Bean 位于父容器中。这样，展现层 Bean 就可以引用业务层和持久层的 Bean，而业务层和持久层的 Bean 则看不到展现层的 Bean。 
1.1.1.1.5	ConfigurableBeanFactory 
5. 	是一个重要的接口，增强了 IoC 容器的可定制性，它定义了设置类装载器、属性编辑器、容器初始化后置处理器等方法； 
1.1.1.1.6	AutowireCapableBeanFactory 自动装配 
6. 	定义了将容器中的 Bean 按某种规则（如按名字匹配、按类型匹配等）进行自动装配的方法； 
1.1.1.1.7	SingletonBeanRegistry运行期间注册单例Bean 
7. 定义了允许在运行期间向容器注册单实例 Bean 的方法；对于单实例（ singleton）的 Bean 来说，BeanFactory 会缓存 Bean 实例，所以第二次使用 getBean() 获取 Bean 时将直接从 IoC 容器的缓存中获取 Bean 实例。Spring 在 DefaultSingletonBeanRegistry 类中提供了一个用于缓存单实例 Bean 的缓存器，它是一个用 HashMap 实现的缓存器，单实例的 Bean 以 beanName 为键保存在这个 HashMap 中。 
1.1.1.1.8	依赖日志框框 
8. 在初始化 BeanFactory 时，必须为其提供一种日志框架，比如使用 Log4J， 即在类路径下提供 Log4J 配置文件，这样启动 Spring 容器才不会报错。 
1.1.1.2	ApplicationContext 面向开发应用 
ApplicationContext 由 BeanFactory 派 生 而 来 ， 提 供 了 更 多 面 向 实 际 应 用 的 功 能 。 
ApplicationContext 继承了 HierarchicalBeanFactory 和 ListableBeanFactory 接口，在此基础
上，还通过多个其他的接口扩展了 BeanFactory 的功能： 

<div align=center>

![1589098002144.png](..\images\1589098002144.png)

</div>

1.	ClassPathXmlApplicationContext：默认从类路径加载配置文件 
2.	FileSystemXmlApplicationContext：默认从文件系统中装载配置文件 
3.	ApplicationEventPublisher：让容器拥有发布应用上下文事件的功能，包括容器启动事件、关闭事件等。 
4.	MessageSource：为应用提供 i18n 国际化消息访问的功能； 
5.	ResourcePatternResolver ： 所 有 ApplicationContext 实现类都实现了类似于 PathMatchingResourcePatternResolver 的功能，可以通过带前缀的 Ant 风格的资源文件路径装载 Spring 的配置文件。 
6.	LifeCycle：该接口是 Spring 2.0 加入的，该接口提供了 start()和 stop()两个方法，主要用于控制异步处理过程。在具体使用时，该接口同时被 ApplicationContext 实现及具体 Bean 实现， ApplicationContext 会将 start/stop 的信息传递给容器中所有实现了该接口的 Bean，以达到管理和控制 JMX、任务调度等目的。 
7.	ConfigurableApplicationContext 扩展于 ApplicationContext，它新增加了两个主要的方法： refresh()和 close()，让 ApplicationContext 具有启动、刷新和关闭应用上下文的能力。在应用上下文关闭的情况下调用 refresh()即可启动应用上下文，在已经启动的状态下，调用 refresh()则清除缓存并重新装载配置信息，而调用 close()则可关闭应用上下文。 
1.1.1.1	WebApplication 体系架构 
WebApplicationContext 是专门为 Web 应用准备的，它允许从相对于 Web 根目录的路径中装载配置文件完成初始化工作。从 WebApplicationContext 中可以获得 ServletContext 的引用，整个 Web 应用上下文对象将作为属性放置到 ServletContext 中，以便 Web 应用环境可以访问 Spring 应用上下文。 

<div align=center>

![1589098030114.png](..\images\1589098030114.png)

</div>

1.1.1	Spring Bean 作用域 
Spring 3 中为 Bean 定义了 5 中作用域，分别为 singleton（单例）、prototype（原型）、 request、session 和 global session，5 种作用域说明如下： singleton：单例模式（多线程下不安全） 
1.	singleton：单例模式，Spring IoC 容器中只会存在一个共享的 Bean 实例，无论有多少个 Bean 引用它，始终指向同一对象。该模式在多线程下是不安全的。Singleton 作用域是 Spring 中的缺省作用域，也可以显示的将 Bean 定义为 singleton 模式，配置为： 
<bean id="userDao" class="com.ioc.UserDaoImpl" scope="singleton"/> 
prototype:原型模式每次使用时创建 
2.	prototype:原型模式，每次通过 Spring 容器获取 prototype 定义的 bean 时，容器都将创建一个新的 Bean 实例，每个 Bean 实例都有自己的属性和状态，而 singleton 全局只有一个对象。根据经验，对有状态的bean使用prototype作用域，而对无状态的bean使用singleton 作用域。 
Request：一次request 一个实例 
3.	request：在一次 Http 请求中，容器会返回该 Bean 的同一实例。而对不同的 Http 请求则会产生新的 Bean，而且该 bean 仅在当前 Http Request 内有效,当前 Http 请求结束，该 bean 实例也将会被销毁。 
<bean id="loginAction" class="com.cnblogs.Login" scope="request"/> session 
4.	session：在一次 Http Session 中，容器会返回该 Bean 的同一实例。而对不同的 Session 请求则会创建新的实例，该 bean 实例仅在当前 Session 内有效。同 Http 请求相同，每一次 session 请求创建新的实例，而不同的实例之间不共享属性，且实例仅在自己的 session 请求内有效，请求结束，则实例将被销毁。 
<bean id="userPreference" class="com.ioc.UserPreference" scope="session"/> global Session 
5.	global Session：在一个全局的 Http Session 中，容器会返回该 Bean 的同一个实例，仅在使用 portlet context 时有效。 
1.1.2	Spring Bean 生命周期 
实例化 
1.	实例化一个 Bean，也就是我们常说的 new。 
IOC 依赖注入 
2.	按照 Spring 上下文对实例化的 Bean 进行配置，也就是 IOC 注入。 
setBeanName 实现 
3.	如果这个 Bean 已经实现了 BeanNameAware 接口，会调用它实现的 setBeanName(String) 方法，此处传递的就是 Spring 配置文件中 Bean 的 id 值 
BeanFactoryAware 实现 
4.	如果这个 Bean 已经实现了 BeanFactoryAware 接口，会调用它实现的 setBeanFactory， setBeanFactory(BeanFactory)传递的是 Spring 工厂自身（可以用这个方式来获取其它 Bean，只需在 Spring 配置文件中配置一个普通的 Bean 就可以）。 
ApplicationContextAware 实现 
5.	如果这个 Bean 已经实现了 ApplicationContextAware 接口，会调用 setApplicationContext(ApplicationContext)方法，传入 Spring 上下文（同样这个方式也可以实现步骤 4 的内容，但比 4 更好，因为 ApplicationContext 是 BeanFactory 的子接口，有更多的实现方法） 
postProcessBeforeInitialization接口实现-初始化预处理 
6.	如果这个 Bean 关联了 BeanPostProcessor 接口，将会调用
postProcessBeforeInitialization(Object obj, String s)方法，BeanPostProcessor 经常被用
作是 Bean 内容的更改，并且由于这个是在 Bean 初始化结束时调用那个的方法，也可以被应用于内存或缓存技术。 
init-method 
7.	如果 Bean 在 Spring 配置文件中配置了 init-method 属性会自动调用其配置的初始化方法。 
postProcessAfterInitialization 
8.	如果这个 Bean 关联了 BeanPostProcessor 接口，将会调用 postProcessAfterInitialization(Object obj, String s)方法。 
注：以上工作完成以后就可以应用这个 Bean 了，那这个 Bean 是一个 Singleton 的，所以一般情况下我们调用同一个 id 的 Bean 会是在内容地址相同的实例，当然在 Spring 配置文件中也可以配置非 Singleton。 
Destroy 过期自动清理阶段 
9.	当 Bean 不再需要时，会经过清理阶段，如果 Bean 实现了 DisposableBean 这个接口，会调用那个其实现的 destroy()方法； destroy-method 自配置清理 
10.	最后，如果这个 Bean 的 Spring 配置中配置了 destroy-method 属性，会自动调用其配置的销毁方法。 

<div align=center>

![1589098060187.png](..\images\1589098060187.png)

</div>

1.	bean 标签有两个重要的属性（init-method 和 destroy-method）。用它们你可以自己定制初始化和注销方法。它们也有相应的注解（@PostConstruct 和@PreDestroy）。 
<bean id="" class="" init-method="初始化方法"  destroy-method="销毁方法"> 
1.1.1	Spring 依赖注入四种方式 
构造器注入 
    
 /*带参数，方便利用构造器进行注入*/   
    public CatDaoImpl(String message){           this. message = message;   
    }  
<bean id="CatDaoImpl" class="com.CatDaoImpl">   
<constructor-arg value=" message "></constructor-arg>   
</bean> 
setter方法注入 
  public class Id {       private int id;       public int getId() {    return id;    }       public void setId(int id) {     this.id = id;   }   
}  
 <bean id="id" class="com.id ">  <property name="id" value="123"></property>  </bean>   
静态工厂注入 
静态工厂顾名思义，就是通过调用静态工厂的方法来获取自己需要的对象，为了让 spring 管理所有对象，我们不能直接通过"工程类.静态方法()"来获取对象，而是依然通过 spring 注入的形式获取： 
public class DaoFactory {   //静态工厂       public static final FactoryDao getStaticFactoryDaoImpl(){           return new StaticFacotryDaoImpl();   
    }   
}   
 public class SpringAction {             private FactoryDao staticFactoryDao;  //注入对象   
    //注入对象的 set 方法   
    public void setStaticFactoryDao(FactoryDao staticFactoryDao) {           this.staticFactoryDao = staticFactoryDao;   
    }   
}   
//factory-method="getStaticFactoryDaoImpl"指定调用哪个工厂方法 
    <bean name="springAction" class=" SpringAction" >   
        <!--使用静态工厂的方法注入对象,对应下面的配置文件-->   
        <property name="staticFactoryDao" ref="staticFactoryDao"></property>              </bean>   
    <!--此处获取对象的方式是从工厂类中获取静态方法-->   
<bean name="staticFactoryDao" class="DaoFactory"  factory-method="getStaticFactoryDaoImpl"></bean> 
 
实例工厂 
实例工厂的意思是获取对象实例的方法不是静态的，所以你需要首先 new 工厂类，再调用普通的实例方法：  
   public class DaoFactory {   //实例工厂        public FactoryDao getFactoryDaoImpl(){           return new FactoryDaoImpl();   
    }   
}  
public class SpringAction {       private FactoryDao factoryDao;      //注入对象         public void setFactoryDao(FactoryDao factoryDao) {           this.factoryDao = factoryDao;   
    }   
}   
    <bean name="springAction" class="SpringAction">   
        <!--使用实例工厂的方法注入对象,对应下面的配置文件-->   
        <property name="factoryDao" ref="factoryDao"></property>       </bean>   
     <!--此处获取对象的方式是从工厂类中获取实例方法-->   
<bean name="daoFactory" class="com.DaoFactory"></bean>   
<bean name="factoryDao" factory-bean="daoFactory"  factory-method="getFactoryDaoImpl"></bean>   
 	 
1.1.2	5 种不同方式的自动装配  
Spring 装配包括手动装配和自动装配，手动装配是有基于 xml 装配、构造方法、setter 方法等自动装配有五种自动装配的方式，可以用来指导 Spring 容器用自动装配方式来进行依赖注入。 
1.	no：默认的方式是不进行自动装配，通过显式设置 ref 属性来进行装配。 
2.	byName：通过参数名 自动装配，Spring容器在配置文件中发现bean的autowire 属性被设置成 byname，之后容器试图匹配、装配和该 bean 的属性具有相同名字的 bean。 
3.	byType：通过参数类型自动装配，Spring 容器在配置文件中发现 bean 的 autowire 属性被设置成byType，之后容器试图匹配、装配和该bean的属性具有相同类型的bean。如果有多个 bean 符合条件，则抛出错误。 
4.	constructor：这个方式类似于 byType， 但是要提供给构造器参数，如果没有确定的带参数的构造器参数类型，将会抛出异常。 
5.	autodetect：首先尝试使用constructor 来自动装配，如果无法工作，则使用byType方式。 

3.7	Spring AOP 原理 

1.1.1	概念 
"横切"的技术，剖解开封装的对象内部，并将那些影响了多个类的公共行为封装到一个可重用模块，
并将其命名为"Aspect"，即切面。所谓"切面"，简单说就是那些与业务无关，却为业务模块所共同调用的逻辑或责任封装起来，便于减少系统的重复代码，降低模块之间的耦合度，并有利于未来的可操作性和可维护性。 
使用"横切"技术，AOP 把软件系统分为两个部分：核心关注点和横切关注点。业务处理的主要流程是核心关注点，与之关系不大的部分是横切关注点。横切关注点的一个特点是，他们经常发生在核心关注点的多处，而各处基本相似，比如权限认证、日志、事物。AOP 的作用在于分离系统中的各种关注点，将核心关注点和横切关注点分离开来。 
AOP 主要应用场景有： 
1.	Authentication 权限 
2.	Caching 缓存 
3.	Context passing 内容传递 
4.	Error handling 错误处理 
5.	Lazy loading 懒加载 
6.	Debugging  调试 
7.	logging, tracing, profiling and monitoring 记录跟踪 优化 校准 
8.	Performance optimization 性能优化 
9.	Persistence  持久化 
10.	Resource pooling 资源池 
11.	Synchronization 同步 
12.	Transactions 事务 
1.1.2	AOP 核心概念 
1、	切面（aspect）：类是对物体特征的抽象，切面就是对横切关注点的抽象 
2、	横切关注点：对哪些方法进行拦截，拦截后怎么处理，这些关注点称之为横切关注点。 
3、	连接点（joinpoint）：被拦截到的点，因为 Spring 只支持方法类型的连接点，所以在 Spring 中连接点指的就是被拦截到的方法，实际上连接点还可以是字段或者构造器。 
4、	切入点（pointcut）：对连接点进行拦截的定义 
5、	通知（advice）：所谓通知指的就是指拦截到连接点之后要执行的代码，通知分为前置、后置、异常、最终、环绕通知五类。 
6、	目标对象：代理的目标对象 
7、	织入（weave）：将切面应用到目标对象并导致代理对象创建的过程 8、引入（introduction）：在不修改代码的前提下，引入可以在运行期为类动态地添加一些方法或字段。 
参考：https://segmentfault.com/a/1190000007469968 
<div align=center>

![1589098123498.png](..\images\1589098123498.png)

</div>

 
1.1.1	AOP 两种代理方式 
Spring 提供了两种方式来生成代理对象: JDKProxy 和 Cglib，具体使用哪种方式生成由 AopProxyFactory 根据 AdvisedSupport 对象的配置来决定。默认的策略是如果目标类是接口，则使用 JDK 动态代理技术，否则使用 Cglib 来生成代理。 
JDK动态接口代理 
1.	JDK 动态代理主要涉及到 java.lang.reflect 包中的两个类：Proxy 和 InvocationHandler。
InvocationHandler是一个接口，通过实现该接口定义横切逻辑，并通过反射机制调用目标类的代码，动态将横切逻辑和业务逻辑编制在一起。Proxy 利用 InvocationHandler 动态创建一个符合某一接口的实例，生成目标类的代理对象。 
CGLib 动态代理 
2.	：CGLib 全称为 Code Generation Library，是一个强大的高性能，高质量的代码生成类库，可以在运行期扩展 Java 类与实现 Java 接口，CGLib 封装了 asm，可以再运行期动态生成新的 class。和 JDK 动态代理相比较：JDK 创建代理有一个限制，就是只能为接口创建代理实例，而对于没有通过接口定义业务方法的类，则可以通过 CGLib 创建动态代理。 
6.1.8.2. 实现原理 
 @Aspect public class TransactionDemo { 
       @Pointcut(value="execution(* com.yangxin.core.service.*.*.*(..))")     public void point(){ 
      } 
     @Before(value="point()")     public void before(){ 
        System.out.println("transaction begin"); 
    } 
       @AfterReturning(value = "point()")     public void after(){ 
        System.out.println("transaction commit"); 
    } 
        @Around("point()") 
    public void around(ProceedingJoinPoint joinPoint) throws Throwable{         System.out.println("transaction begin");         joinPoint.proceed(); 
        System.out.println("transaction commit"); 
    } 
} 
<div align=center>

![1589098154095.png](..\images\1589098154095.png)

</div>

3.8	Spring MVC 原理 

Spring 的模型-视图-控制器（MVC）框架是围绕一个 DispatcherServlet 来设计的，这个 Servlet 会把请求分发给各个处理器，并支持可配置的处理器映射、视图渲染、本地化、时区与主题渲染等，甚至还能支持文件上传。 
1.1.1	MVC 流程 
<div align=center>

![1589098189457.png](..\images\1589098189457.png)

</div>

Http 请求到DispatcherServlet 
(1)	客户端请求提交到 DispatcherServlet。 
HandlerMapping 寻找处理器 
(2)	由 DispatcherServlet 控制器查询一个或多个 HandlerMapping，找到处理请求的
Controller。 
调用处理器Controller 
(3)	DispatcherServlet 将请求提交到 Controller。 
Controller 调用业务逻辑处理后，返回ModelAndView 
(4)(5)调用业务处理和返回结果：Controller 调用业务逻辑处理后，返回 ModelAndView。 
DispatcherServlet 查询ModelAndView 
(6)(7)处理视图映射并返回模型： DispatcherServlet 查询一个或多个 ViewResoler 视图解析器，找到 ModelAndView 指定的视图。 
ModelAndView 反馈浏览器HTTP 
(8) Http 响应：视图负责将结果显示到客户端。 
1.1.1	MVC 常用注解 

<div align=center>

![1589098215837.png](..\images\1589098215837.png)

</div>

1.1	Spring Boot 原理 
Spring Boot 是由 Pivotal 团队提供的全新框架，其设计目的是用来简化新 Spring 应用的初始搭建以及开发过程。该框架使用了特定的方式来进行配置，从而使开发人员不再需要定义样板化的配置。通过这种方式，Spring Boot 致力于在蓬勃发展的快速应用开发领域(rapid application development)成为领导者。其特点如下： 
1.	创建独立的 Spring 应用程序 
2.	嵌入的 Tomcat，无需部署 WAR 文件 
3.	简化 Maven 配置 
4.	自动配置 Spring 
5.	提供生产就绪型功能，如指标，健康检查和外部配置 
6.	绝对没有代码生成和对 XML 没有要求配置 [1] 
1.2	JPA 原理 
1.2.1	事务 
事务是计算机应用中不可或缺的组件模型，它保证了用户操作的原子性 ( Atomicity )、一致性 
( Consistency )、隔离性 ( Isolation ) 和持久性 ( Durabilily )。 
1.2.2	本地事务 
紧密依赖于底层资源管理器（例如数据库连接 )，事务处理局限在当前事务资源内。此种事务处理方式不存在对应用服务器的依赖，因而部署灵活却无法支持多数据源的分布式事务。在数据库连接中使用本地事务示例如下： 
  public void transferAccount() {  
 	 	 Connection conn = null;   	 	 Statement stmt = null;  
 	 	 try{  
 	 	 	 conn = getDataSource().getConnection();  
 // 将自动提交设置为 false，若设置为 true 则数据库将会把每一次数据更新认定为一个事务并自动提交  	 	 	 conn.setAutoCommit(false);  	 	 	 stmt = conn.createStatement();  
 	 	 	 // 将 A 账户中的金额减少 500  
 	 	 stmt.execute("update t_account set amount = amount - 500 where account_id = 'A'"); 
 	 	 	 // 将 B 账户中的金额增加 500  
 	 	 stmt.execute("update t_account set amount = amount + 500 where account_id = 'B'"); 
 	 	 	 // 提交事务 
 	 	     conn.commit(); 
 	            // 事务提交：转账的两步操作同时成功 
 	 	 } catch(SQLException sqle){  	 	 	 
 	 	 	 	 // 发生异常，回滚在本事务中的操做                                                        conn.rollback(); 
 	 	 	 	 // 事务回滚：转账的两步操作完全撤销                                                       stmt.close();                                                          conn.close();  
 	 	 }  
 	 } 
1.2.3	分布式事务 
 
Java 事务编程接口（JTA：Java Transaction API）和 Java 事务服务 (JTS；Java Transaction Service) 为 J2EE 平台提供了分布式事务服务。分布式事务（Distributed Transaction）包括事务管理器（Transaction Manager）和一个或多个支持 XA 协议的资源管理器 ( Resource Manager )。我们可以将资源管理器看做任意类型的持久化数据存储；事务管理器承担着所有事务参与单元的协调与控制。 
public void transferAccount() {  
 	 	 UserTransaction userTx = null;  
 	 	 Connection connA = null; Statement stmtA = null;  	  	 	 Connection connB = null; Statement stmtB = null;  
 try{  
// 获得 Transaction 管理对象  	userTx = (UserTransaction)getContext().lookup("java:comp/UserTransaction");   	connA = getDataSourceA().getConnection();// 从数据库 A 中取得数据库连接  	 connB = getDataSourceB().getConnection();// 从数据库 B 中取得数据库连接 
userTx.begin();   // 启动事务  stmtA = connA.createStatement();// 将 A 账户中的金额减少 500  
stmtA.execute("update t_account set amount = amount - 500 where account_id = 'A'"); 
 	 	 	 // 将 B 账户中的金额增加 500  
 	 	 	 stmtB = connB.createStatement();  
stmtB.execute("update t_account set amount = amount + 500 where account_id = 'B'"); 
 	 	 	 userTx.commit();// 提交事务 
 	 // 事务提交：转账的两步操作同时成功（数据库 A 和数据库 B 中的数据被同时更新） 
 } catch(SQLException sqle){  
 	 // 发生异常，回滚在本事务中的操纵 
                 userTx.rollback();// 事务回滚：数据库 A 和数据库 B 中的数据更新被同时撤销  	} catch(Exception ne){ }  
} 
1.2.4	两阶段提交 
两阶段提交主要保证了分布式事务的原子性：即所有结点要么全做要么全不做，所谓的两个阶段是指：第一阶段：准备阶段；第二阶段：提交阶段。 
<div align=center>

![1589098269601.png](..\images\1589098269601.png)

</div>

1准备阶段 
事务协调者(事务管理器)给每个参与者(资源管理器)发送 Prepare 消息，每个参与者要么直接返回失败(如权限验证失败)，要么在本地执行事务，写本地的 redo 和 undo 日志，但不提交，到达一种“万事俱备，只欠东风”的状态。 
2提交阶段： 
如果协调者收到了参与者的失败消息或者超时，直接给每个参与者发送回滚(Rollback)消息；否则，发送提交(Commit)消息；参与者根据协调者的指令执行提交或者回滚操作，释放所有事务处理过程中使用的锁资源。(注意:必须在最后阶段释放锁资源) 将提交分成两阶段进行的目的很明确，就是尽可能晚地提交事务，让事务在提交前尽可能地完成所有能完成的工作。 
