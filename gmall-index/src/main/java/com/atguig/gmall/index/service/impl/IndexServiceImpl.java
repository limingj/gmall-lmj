package com.atguig.gmall.index.service.impl;

import com.atguig.gmall.index.config.GmallCache;
import com.atguig.gmall.index.fegin.PmsClient;
import com.atguig.gmall.index.service.IndexService;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVo;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndexServiceImpl implements IndexService {

	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	private static final String KEY_PRECATES = "index:cates:";
	@Autowired
	private PmsClient pmsClient;
	@Autowired
	private RedissonClient redissonClient;

	@Override
	public List<CategoryEntity> queryCates() {
		Resp<List<CategoryEntity>> listResp = pmsClient.queryAllCategoryTree(1, null);
		List<CategoryEntity> categoryEntities = listResp.getData();
		return categoryEntities;
	}



	@Override
	@GmallCache(value = "index:cates:",timeout = 7200,bound = 100,lockName = "lock")
	public List<CategoryVo> querysubCates(Long pid) {
		Resp<List<CategoryVo>> listResp = this.pmsClient.querysubCates(pid);
		List<CategoryVo> categoryVos = listResp.getData();
		return categoryVos;
	}

/*	@Override
	public List<CategoryVo> querysubCates(Long pid) {
		String cacheCategoryVos = stringRedisTemplate.opsForValue().get(KEY_PRECATES + pid);
		if (StringUtils.isNotBlank(cacheCategoryVos)) {
			//缓存中存在  直接返回
			List<CategoryVo> categoryVos = JSON.parseArray(cacheCategoryVos, CategoryVo.class);
			return categoryVos;
		}

		//加分布式锁
		RLock lock = this.redissonClient.getLock("lock"+pid);
		lock.lock();
		String cacheCateJson = stringRedisTemplate.opsForValue().get(KEY_PRECATES + pid);
		//
		if (StringUtils.isNotBlank(cacheCateJson)) {
			lock.unlock();
			List<CategoryVo> categoryVos = JSON.parseArray(cacheCategoryVos, CategoryVo.class);
			return categoryVos;
		}

		//不存在  （1）查数据库  （2）放到redis
		Resp<List<CategoryVo>> listResp = this.pmsClient.querysubCates(pid);
		List<CategoryVo> categoryVos = listResp.getData();
		//解决雪崩  设置 随机的 过期时间  ===》保证不存在大量缓存同时过期
		stringRedisTemplate.opsForValue().set(KEY_PRECATES + pid, JSON.toJSONString(categoryVos)
			                            	, 5 + new Random().nextInt(5), TimeUnit.DAYS);
		lock.unlock();
		return categoryVos;
	}*/

}

