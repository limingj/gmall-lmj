package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.gmall.order.execption.OrderExecption;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.Vo.vo.ItemSaleVo;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.atguigu.oms.entity.OrderEntity;
import com.atguigu.oms.vo.OrderItemVo;
import com.atguigu.oms.vo.OrderSubmitVo;
import com.atguigu.order.vo.OrderConfirmVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
	@Autowired
	private GmallUmsClient umsClient;
	@Autowired
	private GmallCartClient cartClient;
	@Autowired
	private GmallPmsClient pmsClient;
	@Autowired
	private GmallWmsClient wmsClient;
	@Autowired
	private GmallSmsClient smsClient;
	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private GmallOmsClient omsClient;

	@Autowired
	private AmqpTemplate amqpTemplate;

	@Autowired
	private ThreadPoolExecutor threadPoolExecutor;

	private static final String TOKEN_PREFIX="order:token:";
	@Override
	public OrderConfirmVo confirm() {
		OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
		//获取UserId  拦截器
		UserInfo userInfo = LoginInterceptor.getUserInfo();
		Long userId = userInfo.getUserId();
		//获取用户地址信息  （远程调用ums）
		CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
			Resp<List<MemberReceiveAddressEntity>> addressResp = this.umsClient.queryAddressByUserId(userId);
			List<MemberReceiveAddressEntity> addressEntities = addressResp.getData();
			orderConfirmVo.setAddresses(addressEntities);
		}, threadPoolExecutor);
		HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
		objectObjectHashMap.put("","");

		//获取购物车详情列表
		//获取购物车被选中的购物车记录
		CompletableFuture<Void> cartItemFuture = CompletableFuture.supplyAsync(() -> {  //开启一个有返回值的异步线程
			return this.cartClient.queryCheckedCarts(userId);
		}).thenAcceptAsync(carts -> {    //拿到上一个任务的结果  ==》开启一个新线程
			List<OrderItemVo> orderItemVos = carts.stream().map(cart -> {
				//skuId  count 从购物车中取
				Long skuId = cart.getSkuId();
				Integer count = cart.getCount();
				OrderItemVo orderItemVo = new OrderItemVo();
				orderItemVo.setCount(count);
				orderItemVo.setSkuId(skuId);
				//其他属性直接去获取，不从cart获取  目的：不依赖cart微服务
				CompletableFuture<Void> skuFuture = CompletableFuture.runAsync(() -> {
					Resp<SkuInfoEntity> skuInfoEntityResp = this.pmsClient.quserySkuInfoByShuId(skuId);
					SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
					if (skuInfoEntity != null) {
						orderItemVo.setImage(skuInfoEntity.getSkuDefaultImg());
						orderItemVo.setPrice(skuInfoEntity.getPrice());
						orderItemVo.setSkuTitle(skuInfoEntity.getSkuTitle());
						orderItemVo.setWeight(skuInfoEntity.getWeight());
					}
				}, threadPoolExecutor);

				CompletableFuture<Void> saleFuture = CompletableFuture.runAsync(() -> {
					Resp<List<SkuSaleAttrValueEntity>> attrsResp = this.pmsClient.querySaleAttrsBySkuId(skuId);
					List<SkuSaleAttrValueEntity> attrValueEntities = attrsResp.getData();
					if (!CollectionUtils.isEmpty(attrValueEntities)) {
						orderItemVo.setSaleAttrs(attrValueEntities);
					}
				}, threadPoolExecutor);

				CompletableFuture<Void> wareFuture = CompletableFuture.runAsync(() -> {
					Resp<List<WareSkuEntity>> wareResp = this.wmsClient.queryWareSkuBySkuId(skuId);
					List<WareSkuEntity> wareSkuEntities = wareResp.getData();
					if (!CollectionUtils.isEmpty(wareSkuEntities)) {
						orderItemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
					}
				}, threadPoolExecutor);

				CompletableFuture<Void> orderFuture = CompletableFuture.runAsync(() -> {
					Resp<List<ItemSaleVo>> saleResp = this.smsClient.queryItemSaleBySkuId(skuId);
					List<ItemSaleVo> itemSaleVos = saleResp.getData();
					orderItemVo.setSales(itemSaleVos);
				}, threadPoolExecutor);

				CompletableFuture.allOf(skuFuture, saleFuture, wareFuture, orderFuture).join();
				return orderItemVo;
			}).collect(Collectors.toList());
			orderConfirmVo.setOrderItemVos(orderItemVos);
		}, threadPoolExecutor);


		//获取积分信息  ums
		CompletableFuture<Void> boundFuture = CompletableFuture.runAsync(() -> {
			Resp<MemberEntity> memberEntityResp = this.umsClient.queryMemberById(userId);
			MemberEntity memberEntity = memberEntityResp.getData();
			if (memberEntity != null) {
				orderConfirmVo.setBounds(memberEntity.getIntegration());
			}
		}, threadPoolExecutor);

		//获取订单token防止重复提交  分布式id生成器
		CompletableFuture<Void> tokenFuture = CompletableFuture.runAsync(() -> {
			String orderToken = IdWorker.getTimeId();
			orderConfirmVo.setOrderToken(orderToken);
			redisTemplate.opsForValue().set(TOKEN_PREFIX + orderToken, orderToken, 3, TimeUnit.HOURS);
		}, threadPoolExecutor);

		CompletableFuture.allOf(addressFuture,cartItemFuture,boundFuture,tokenFuture).join();
		return orderConfirmVo;
	}

	@Override
	public OrderEntity submit(OrderSubmitVo orderSubmitVo) {
		//1.判断orderToken
		String orderToken = orderSubmitVo.getOrderToken();
		//保证验证与删除的原子性
		//orderToken存在，则提交并删除orderToken
		//String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
		//指定返回值类型：Long.class
		//Long flag= redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(TOKEN_PREFIX + orderToken));
		//String orderToken = orderSubmitVO.getOrderToken();

		/**
		 *   一、防止订单重复提交  orderToken  分布式Id雪花算法   MyredisPlus提供：IdWork
		 */
		String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
		Long flag = (Long)this.redisTemplate.execute(
				new DefaultRedisScript<>(script, Long.class),
				Arrays.asList(TOKEN_PREFIX + orderSubmitVo.getOrderToken()),
				orderToken);
		if(flag == 0){
			throw new OrderExecption("订单已提交！");
		}

		//2.验价
		BigDecimal totalPrice = orderSubmitVo.getTotalPrice();
		//获取实时价格
		List<OrderItemVo> items = orderSubmitVo.getItems();
		if(CollectionUtils.isEmpty(items)){
			throw new OrderExecption("请勾选需要购买的商品！");
		}

		items.stream().map(orderItemVo -> {
			Long skuId = orderItemVo.getSkuId();
			Resp<SkuInfoEntity> skuInfoEntityResp = this.pmsClient.quserySkuInfoByShuId(skuId);
			SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
			if(skuInfoEntity != null){
				return skuInfoEntity.getPrice().multiply(new BigDecimal(orderItemVo.getCount()));
			}
			return new BigDecimal(0);
		}).reduce((a,b)->a.add(b)).get();//迭代计算

		// 比较价格是否一致
		if(totalPrice.compareTo(totalPrice) != 0) {
			throw new OrderExecption("页面已过期，请刷新后再试！");
		}


	//3.验证库存，锁库存(具备原子性，支付完成后才真正减库存)
		List<SkuLockVo> skuLockVos = items.stream().map(orderItemVo -> {
			SkuLockVo skuLockVo = new SkuLockVo();
			skuLockVo.setSkuId(orderItemVo.getSkuId());
			skuLockVo.setCount(orderItemVo.getCount());
			skuLockVo.setOrderToken(orderSubmitVo.getOrderToken());
			return skuLockVo;
		}).collect(Collectors.toList());

		Resp<List<SkuLockVo>> lockResp = this.wmsClient.checkAndLockStock(skuLockVos);
		// 1 释放库存，可能会没有机会执行（原因：锁库存虽然成功，但是响应时出现了网络传输异常）  消息队列

		List<SkuLockVo> lockVos = lockResp.getData();
		//为空，锁定成功  不空，有锁定失败的
		if(!CollectionUtils.isEmpty(lockVos)){
			throw new OrderExecption(JSON.toJSONString(lockVos));
		}

		OrderEntity orderEntity = null;
		// 4. （保存订单及订单项）
		UserInfo userInfo = LoginInterceptor.getUserInfo();
		try {
			Resp<OrderEntity> orderEntityResp = this.omsClient.saveOrder(orderSubmitVo, userInfo.getUserId());
			orderEntity = orderEntityResp.getData();
			// 2 如果在这里定时关单，可能订单创建成功，而没有正常响应  所以在oms中关单
		} catch (Exception e) {
			e.printStackTrace();
			// 订单创建异常应该立马释放库存： feign（导致业务阻塞）  消息队列（异步）
			this.amqpTemplate.convertAndSend("ORDER-CART-EXCHANGE", "stock.unlock", orderSubmitVo.getOrderToken());
			throw new OrderExecption("服务器错误，订单保存失败!");
		}
		// 5. 删除购物车，发送消息
		try {
			//发送的内容
			Map<String, Object> map = new HashMap<>();
			map.put("userId",userInfo.getUserId());
			List<Long> skuIds = items.stream().map(orderItemVo -> orderItemVo.getSkuId()).collect(Collectors.toList());
			map.put("skuIds",JSON.toJSONString(skuIds));
			this.amqpTemplate.convertAndSend("ORDER-CART-EXCHANGE","cart.delete",map);
		} catch (AmqpException e) {
			e.printStackTrace();
		}
		return orderEntity;
	}
}
