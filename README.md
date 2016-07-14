# Msobicents事件路由机制优化方案

------

**Mobicents**采用事件驱动的系统结构，所构建的事件路由，将所有事件转发至对应的业务模块。因此，事件路由的调度算法、事件路由结构的优化，原理上通过多线程甚至多机的并行方法，提高事件路由的能力，使得融合网络业务平台具备高速转发和处理的能力。
本方案主要优化了Mobicents服务器的事件路由机制，通过整理服务器源代码，对其中**事件转发策略**以及**队列饱和策略**改进源代码，提升服务器在应对高并发请求场景下的事件路由转发效率。

下面将按如下方式介绍：
> * 简单介绍mobicents事件路由机制
> * 事件转发策略与队列饱和策略方案修改（Executor默认饱和策略的修改+多线程代码编写）
> * 测试用例设计（使用countDownLatch+ExecutorSerivce构建高并发请求）
> * 实验结果展示


### [（一）mobicents事件路由机制介绍](http://www.ibm.com/developerworks/cn/opensource/os-jain-slee/)


## 什么是 Mobicents

Mobicents 是一个专业开源的 VoIP 中间件平台。Mobicents 是第一个而且是目前唯一开源并被 JAIN SLEE 1.0 认定的产品，它从交换协议构造上基于 JAIN-SIP。

JAIN SLEE 是一个以事件为驱动的中间件，采用了各个服务单元(Sbb)消息机制，减少了在事物处理上的等待延迟，其工作方式是从外部协议资源扫描事件状态，然后将这 些事件递交到各个处理单元去，可以以它为核心设计成网关和网守，软交换上层的应用服务器，媒体服务器等多种设备，同时适配多种交换协议。
![](https://github.com/sainty7/Mobicents/blob/master/photos/image002.jpg)
1> SBB 服务管理单元（Sbb Service Management）：这个部分面向上层的应用，也就是 Service Block Building 的构造和部署的主要部分，其中包含了对象持久性（Persistence），类似 EJB 的CMP 一样，对数据对象的持久性（包括生存期和数据库连接等等）由 SLEE 容器自动完成的，SBB 分成 Sbb 实体和 Sbb 对象两个组成。对整个 Sbb 的服务管理单元来说，包含有 Sbb 工厂，持久性管理，Sbb 对象池管理，服务部署者，这些对于实际的使用者而言是不可见的。用户的应用部署文件是 sbb.jar，而用户服务描述是 service.xml。

2> Sbb 运行环境（Sbb Runtime）：Sbb 运行环境就是Sbb的执行体，核心是事件导向单元--Event Router，（获得事件并且分配导入到指定的 Sbb 中去），SLEE 端点管理（连接资源适配器产生事件送达端点），通过上下文（Context）方式来实现各个实体之间的联系（参看图7），和 Sbb服务管理单元之间的接口是 ActivityContext（以下为活跃实体或者行为实体上下文），用于表示独立的事件接口；和资源适配器之间的接口是 Activity，也就是行为实体，具体事件的封装，例如 SIP 的注册事件（SIP Register），这个事件会引发 Sbb 的相关注册服务（例如 RegisteraSbb）；另一个接口是 SLEE Activity，这个是 SLEE 内部的行为实体，例如一些内部的工具产生的行为实体，例如定时器事件（Timer Event）和用于调试的 Trace 事件。

3> SLEE 的一些工具：和 J2EE 中的工具一样，提供了一些工具来使用，工具在 SLEE 中的定义是一些标准的功能组件，他们的提供了一些预定义的接口为应用提供服务，其中包括了Adress,Profile,Alarm,Naming。 

4> 资源适配器（RA），用于具体的协议在SLEE上的封装，例如(SCAP,SS7,SIP,MGCP,H.323)当然也可以是自己的私有协议，封装的方式可以参看Mobicents官方网站的一些介绍，协议和SLEE Run-time的接口就是具体的行为实体，也就是事件的封装, 用户定义的资源适配器部署文件是RA.jar在本文中SIP资源适配器的部署文件是sip-local-ra.jar和sip-type-ra.jar。

## Mobicents的事件路由机制是怎样的
![](https://github.com/sainty7/Mobicents/blob/master/photos/image024.gif)
下面详细介绍一下事件路由的流程：
> * (1)event由ResourceAdaptor触发,router至SleeEndPoint,SleeEndPoint会调用fireEvent()方法
> * (2) fireEvent调用ActivityContextImpl的fireEvent()方法
> * (3)调用ActivityEventQueueManagerImpl的commit()方法
> * (4)调用EventRouterExecutorImpl的routeEvent方法，在其中使用ExecutorService新起一个线程
> * (5)调用EventRouterExecutorImpl的run()方法

### [（二）事件转发策略与队列饱和策略方案修改](http://##)


每当事件要选择event router来完成事件路由工作时，会调用getExecutors（）方法，默认的获取方式使用是计算ActivityContextHandler参数的hash值进行随机获取，也可以采用RR轮询的方式。因此原本router的选择是一种无状态的随机的方式。

当获取了EventRouterExecutorImpl对象之后，该对象会使用ExecutorService生成一个newSingleThread,这个新的线程会真正处理事件的转发过程。此时新的线程是生产者消费者模式，事件进入该线程后会先进入LinkedBlockingQueue中排队，等待消费者执行事件转发过程。

修改一：每次执行setExecutor()方法时，会使用对象锁（因为EventRouterImpl对象的实现是单例模式）依次获取每个EventRouterExecutorImpl对象中ExecutorService的LinkedBlockingQueue中的队列个数。只要有任意一个线程中的队列个数超过该线程队列总数的2/5，就会执行resize()方法，即增加新的线程（即增加新的EventRouterExecutorImpl对象，也就是增加ExecutorService的个数）来处理事件路由。
```java
	 *(1)先对类Eventrouterimpl生成的对象加锁，这里之所已不使用类锁是因为这里的对象是由单例模式生成的
	 *(2)获取executor的策略是：检查每个Executor的LinkedBlockingQueue数量,如果有一个Executor的Queue达到40%,那么就执行resize()方法
	 *(3)执行resize()方法，增加可用线程数量
	 * 
	*/
	public EventRouterExecutor getExecutor(
			ActivityContextHandle activityContextHandle) {
		synchronized (eventrouterimpl) {
			System.out.println("++"+eventrouterimpl.hashCode());
			int len = executors.length;
			System.out.println("num_of_executors :"+len);
			for(int i=0;i<len;i++){
				if((Integer.parseInt(executors[i].toString())) >= 4){
					eventrouterimpl.resize();
					break;
				}
			}
			System.out.println("finish");
		}
		return executors[(activityContextHandle.hashCode() & Integer.MAX_VALUE)
				% executors.length];
	}

```
修改二：当队列LinkedBlockingQueue达到最大值时，默认的队列饱和机制是AbortPolicy，executor抛出未检查RejectedExecutionException，调用者捕获这个异常，然后自己编写能满足自己需求的处理代码。现在使用CallerRunsPolicy，此时既不会丢弃任务，也不会抛出任何异常，而是把一些任务推到调用者中处理，因为调用者要处理任务，不会产生新的事件以此减缓新任务流。
```java
	public EventRouterExecutorImpl(boolean collectStats, SleeContainer sleeContainer) {		
		//this.executor = Executors.newSingleThreadExecutor();
		/*
			@sainty 2016-07-04
			替换原有的单线程的饱和策略由默认的Abortion改为Call-Runners
			并配置新的排队机制，设置LinkedBlockingQueue队列长度为10个。
		*/
		ThreadPoolExecutor executor_new = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>(10));
		executor_new.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		this.executor = executor_new;
		
		stats = collectStats ? new EventRouterExecutorStatisticsImpl() : null;
		this.sleeContainer = sleeContainer;
	}

```

### [（三）测试用例设计](http://##)
测试用例设计：
（一）EventRouter个数50，每个队列长度10。
（二）EventRouter个数50，每个队列长度10。（修改后方案）
使用闭锁，并发400个请求。
```java
	public static void timeTask(int nThreads) throws InterruptedException {
        ExecutorService service = Executors.newCachedThreadPool(); 
		final CountDownLatch startGate = new CountDownLatch(1);
		final CountDownLatch endGate = new CountDownLatch(nThreads);
		for(int i=0;i<nThreads;i++){
		    Runnable runnable = new Runnable(){
	            public void run(){
	                try {   
	                    startGate.await();
	                    try{
					    	task();
						}finally{
						    endGate.countDown();
					    }	             
	                }catch (Exception e) {
	                    e.printStackTrace();
	                }                
	            }
	        };
			service.execute(runnable);
		}
		try {
			Thread.sleep(2000);
			long start = System.nanoTime();
			System.out.println("开始执行所有任务");
			startGate.countDown();
			endGate.await();
			long end = System.nanoTime();
			System.out.println("共执行了"+(end-start)+"纳秒");
		} finally{
			service.shutdown();
		}
	}
public static void task(){
		try {
			URL url = new URL("http://127.0.0.1:9090/registermanager/register");
			HttpURLConnection http = (HttpURLConnection)url.openConnection();
			http.setRequestMethod("POST");
			http.setDoOutput(true);
			http.setDoInput(true);
			http.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; rv:11.0) like Gecko");
			http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			http.setRequestProperty("Content-Language", "zh-CN");
			http.setRequestProperty("Connection", "Keep-Alive");
			http.setRequestProperty("Cache-Control", "no-cache");
			http.setRequestProperty("Host", "127.0.0.1:9090");
			//http.setRequestProperty("Referer", "http://192.168.168.168/0.htm");
			http.setRequestProperty("Accept-Encoding", "gzip, deflate");
			http.setRequestProperty("DNT", "1");
			//String info = "";
			String info = "{\"Container\":{\"Name\":\"container\",\"PrimerBlockIndex\":0,\"Blocks\":[{\"Name\":\"device 0\",\"ContactURL\":\"tel:15851811232\",\"ip\":\"10.10.123.111\",\"Type\":\"PHONE\",\"CP\":\"CIRCULT\",\"AVD\":\"AUDIO\",\"Active\":\"OFFLINE\",\"BandWidthGrade\":\"AUDIO\",\"WIFI\":false,\"DSL\":true,\"BlueTooth\":false,\"thirdG\":false,\"expires\":10000}]}}";
			PrintWriter out = new PrintWriter(http.getOutputStream());
			out.print(info);
			out.close();
			http.connect();
			System.out.println(http.getResponseCode());

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
```
### [（四）实验结果展示](http://##)

作者 sainty    
2015 年 07月 14日   