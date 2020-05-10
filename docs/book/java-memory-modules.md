Java 里面进行多线程通信的主要方式就是共享内存的方式，共享内存主要的关注点有两个：可见性和有序性原子性。Java 内存模型（JMM）解决了可见性和有序性的问题，而锁解决了原子性的问题，理想情况下我们希望做到“同步”和“互斥”。有以下常规实现方法： 
将数据抽象成一个类，并将数据的操作作为这个类的方法 
将数据抽象成一个类，并将对这个数据的操作作为这个类的方法，这么设计可以和容易做到同步，只要在方法上加”synchronized“ 
public class MyData { 
     private int j=0; public  synchronized void add(){ 
         j++; 
System.out.println("线程"+Thread.currentThread().getName()+"j 为："+j); 
} 
public  synchronized void dec(){ 
         j--; 
      System.out.println("线程"+Thread.currentThread().getName()+"j 为："+j); 
     } 
     public int getData(){          return j; 
     } 
} 
public class AddRunnable implements Runnable{ 
    MyData data;     public AddRunnable(MyData data){         this.data= data; 
    }  
    public void run() {             data.add(); 
     } 
 } 
public class DecRunnable implements Runnable { 
    MyData data;     public DecRunnable(MyData data){         this.data = data; 
    } 
    public void run() {             data.dec(); 
    } 
} 
 public static void main(String[] args) { 
        MyData data = new MyData(); 
        Runnable add = new AddRunnable(data);         Runnable dec = new DecRunnable(data);         for(int i=0;i<2;i++){             new Thread(add).start();             new Thread(dec).start(); 
        } 
Runnable 对象作为一个类的内部类 
1.	将 Runnable 对象作为一个类的内部类，共享数据作为这个类的成员变量，每个线程对共享数据的操作方法也封装在外部类，以便实现对数据的各个操作的同步和互斥，作为内部类的各个 Runnable 对象调用外部类的这些方法。 
public class MyData { 
     private int j=0;      public  synchronized void add(){          j++; 
     System.out.println("线程"+Thread.currentThread().getName()+"j 为："+j); 
     } 
     public  synchronized void dec(){          j--; 
      System.out.println("线程"+Thread.currentThread().getName()+"j 为："+j); 
     } 
     public int getData(){ 
         return j; 
     } 
} 
public class TestThread { 
    public static void main(String[] args) {         final MyData data = new MyData();         for(int i=0;i<2;i++){             new Thread(new Runnable(){                public void run() {                     data.add(); 
                 } 
             }).start(); 
            new Thread(new Runnable(){                  public void run() {                     data.dec();  
                 } 
             }).start(); 
        } 
    } 
} 
 



1.1.1	volatile 关键字的作用（变量可见性、禁止重排序） 
Java 语言提供了一种稍弱的同步机制，即 volatile 变量，用来确保将变量的更新操作通知到其他线程。volatile 变量具备两种特性，volatile 变量不会被缓存在寄存器或者对其他处理器不可见的地方，因此在读取 volatile 类型的变量时总会返回最新写入的值。 
变量可见性其一是保证该变量对所有线程可见，这里的可见性指的是当一个线程修改了变量的值，那么新的值对于其他线程是可以立即获取的。 
禁止重排序 
 volatile 禁止了指令重排。 
比sychronized 更轻量级的同步锁 
在访问 volatile 变量时不会执行加锁操作，因此也就不会使执行线程阻塞，因此 volatile 变量是一
种比 sychronized 关键字更轻量级的同步机制。volatile 适合这种场景：一个变量被多个线程共享，线程直接给这个变量赋值。 
<div align=center>

![1589108822519.png](..\images\1589108822519.png)

</div>

当对非 volatile 变量进行读写的时候，每个线程先从内存拷贝变量到 CPU 缓存中。如果计算机有
多个 CPU，每个线程可能在不同的 CPU 上被处理，这意味着每个线程可以拷贝到不同的 CPU 
cache 中。而声明变量是 volatile 的，JVM 保证了每次读变量都从内存中读，跳过 CPU cache 这一步。 
适用场景值得说明的是对 volatile 变量的单次读/写操作可以保证原子性的，如 long 和 double 类型变量，
但是并不能保证 i++这种操作的原子性，因为本质上 i++是读、写两次操作。在某些场景下可以
代替 Synchronized。但是,volatile 的不能完全取代 Synchronized 的位置，只有在一些特殊的场景下，才能适用 volatile。总的来说，必须同时满足下面两个条件才能保证在并发环境的线程安全： 
（1）	对变量的写操作不依赖于当前值（比如 i++），或者说是单纯的变量赋值（boolean flag = true）。 
（2）	该变量没有包含在具有其他变量的不变式中，也就是说，不同的 volatile 变量之间，不能互相依赖。只有在状态真正独立于程序内其他内容时才能使用 volatile。 

