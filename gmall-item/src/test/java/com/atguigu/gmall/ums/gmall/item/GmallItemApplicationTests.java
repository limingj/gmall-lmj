package com.atguigu.gmall.ums.gmall.item;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;

@SpringBootTest
class GmallItemApplicationTests {

	@Test
	void contextLoads() {
		//开启另一个子任务，不需要子任务的返回值
		CompletableFuture.runAsync(() -> {
			System.out.println("开启不带返回值的子任务");
		}).whenCompleteAsync((t,u)->{
			System.out.println("t"+t);
			System.out.println("u"+u);
		});
		CompletableFuture.supplyAsync(()->{
			System.out.println("开启带返回值的子任务");
			int i = 1/0;
			return "hellosupplyAsync";
		}).exceptionally((t)->{
			System.out.println("exceptionally  t "+t);
			return "exceptionally";
		});
	}

}
