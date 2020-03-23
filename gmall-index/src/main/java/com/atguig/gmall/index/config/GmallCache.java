package com.atguig.gmall.index.config;

import java.lang.annotation.*;

/**
 * 自定义注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface GmallCache {
	//自定义缓存key值
	String value() default "";
	//自定义缓存有期时间
	int timeout() default 30;
	//防止雪崩  设置随机值范围   保证不存在大量缓存同时过期
	int bound() default 5;
	//自定义锁名
	String lockName() default "lock";
}
