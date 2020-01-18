package com.atguig.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import com.atguig.gmall.index.config.GmallCache;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class GmallCacheAspect {
	//返回值必须Object
	//形参
	//方法必须抛出
	//调用原始方法

	//execution(* *.*(..)) 拦截所有
	//@Around("execution(* *.*(..))")
	//拦截注解所在方法
	@Autowired
	private StringRedisTemplate redisTemplate;
	@Autowired
	private RedissonClient redissonClient;
	@Around("@annotation(com.atguig.gmall.index.config.GmallCache)")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
		//获取方法对象
		MethodSignature signature = (MethodSignature)joinPoint.getSignature();
		Method method = signature.getMethod();
		//返回值对象
		Class returnType = signature.getReturnType();
		//获取注解对象
		GmallCache gmallCache = method.getAnnotation(GmallCache.class);
		List<Object> args = Arrays.asList(joinPoint.getArgs());
		String prefix = gmallCache.value();
		//获取缓存数据
		String key = prefix + args;
		String cateJson = redisTemplate.opsForValue().get("");
		//判断数据
		if(StringUtils.isNotBlank(cateJson)){
			return JSON.parseObject(cateJson,returnType);
		}
		//为空  加锁
		String lockName = gmallCache.lockName();
		RLock fairLock = this.redissonClient.getFairLock(lockName + args);
		fairLock.lock();
		//再判断缓存
		String cateJson2 = redisTemplate.opsForValue().get("");
		//判断数据
		if(StringUtils.isNotBlank(cateJson2)){
			fairLock.unlock();
			return JSON.parseObject(cateJson2,returnType);
		}
		//执行目标方法
		Object result = joinPoint.proceed(joinPoint.getArgs());
		//把数据放入缓存
		this.redisTemplate.opsForValue().set(key,JSON.toJSONString(result)
				                           ,gmallCache.timeout()+new Random().nextInt(gmallCache.bound()), TimeUnit.MINUTES);
		fairLock.unlock();
		return result;
	}
}




















