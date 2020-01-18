package com.atguigu.gmall.item.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {
	@Bean
	public ThreadPoolExecutor threadPoolExecutor() {
		return new ThreadPoolExecutor(500, 800, 30, TimeUnit.SECONDS
				, new ArrayBlockingQueue<>(1000000));
	}
}


/*@Component
@ConfigurationProperties(prefix = "thread")
public class ThreadPoolConfig {
	@Value("{thread.corePoolSize}")
	private int corePoolSize;
	@Value("thread.maximumPoolSize")
	private int maximumPoolSize;
	@Value("thread.keepAliveTime")
	private long keepAliveTime;
	@Value("thread.unit")
	private TimeUnit unit;
	@Value("thread.workQueue")
	private BlockingQueue<Runnable> workQueue;

	public ThreadPoolExecutor threadPoolExecutor(){
		return new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,unit,workQueue);
	}*/

