package com.atguigu.gmall.wms.listener;


import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WmsListener {
	@Autowired
	private StringRedisTemplate redisTemplate;
	//锁库存
	private static final String KEY_PREFIX = "wms:stock:";
	@Autowired
	private WareSkuDao wareSkuDao;
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = "STOCK-UNLOCK-QUEUE",durable = "true"),
			exchange = @Exchange(value = "ORDER-CART-EXCHANGE",ignoreDeclarationExceptions = "true",durable = "true"),
			//key = {"stock.unlock","wms.dead"}
			key = {"stock.unlock","wms.dead"}
	))
	public void unLock(String orderToken){
		String json = this.redisTemplate.opsForValue().get(KEY_PREFIX + orderToken);
		if(StringUtils.isEmpty(json)){
			return;
		}
		//反序列化，解锁库存
		List<SkuLockVo> skuLockVos = JSON.parseArray(json, SkuLockVo.class);
		skuLockVos.forEach(skuLockVo -> {
			wareSkuDao.unLock(skuLockVo.getWareSkuId(),skuLockVo.getCount());
			//删除redis中库存
			this.redisTemplate.delete(KEY_PREFIX+orderToken);
		});
	}
}
