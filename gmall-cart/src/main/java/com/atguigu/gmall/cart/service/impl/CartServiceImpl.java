package com.atguigu.gmall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.api.pojo.Cart;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.Vo.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private StringRedisTemplate redisTemplate;
	private static final String KEY_PREFIX="cart:item:";
	private static final String PRICE_PREFIX="price:";
	@Autowired
	private GmallPmsClient pmsClient;
	
	@Autowired
	private GmallWmsClient wmsClient;

	@Autowired
	private GmallSmsClient smsClient;
	@Override
	public void addCart(Cart cart) {
		String key = KEY_PREFIX;
		//获取用户登陆信息
		UserInfo userInfo = LoginInterceptor.getUserInfo();
		//判断是否登陆过
		if(userInfo.getUserId() != null){
			//已登录userId
			key+=userInfo.getUserId();
		}else {
			//未登录  userKey
			key+=userInfo.getUserKey();
		}
		//1.获取购物车信息
		BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
		String skuId = cart.getSkuId().toString();
		Integer count = cart.getCount();
		//判断购物车中是否有该商品
		if(hashOps.hasKey(skuId)){
			//已经加入过  数量累加
			String cartJson = hashOps.get(skuId).toString();
			cart= JSON.parseObject(cartJson, Cart.class);
			cart.setCount(cart.getCount()+count);
		}else {
			//未加入
			Resp<SkuInfoEntity> skuInfoEntityResp = pmsClient.quserySkuInfoByShuId(cart.getSkuId());
			SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
			if(skuInfoEntity == null){
				return;
			}
			cart.setPrice(skuInfoEntity.getPrice());
			cart.setSkuTitle(skuInfoEntity.getSkuTitle());
			cart.setImage(skuInfoEntity.getSkuDefaultImg());
			cart.setCheck(true); //默认选中状态

			Resp<List<WareSkuEntity>> listResp = wmsClient.queryWareSkuBySkuId(cart.getSkuId());
			List<WareSkuEntity> wareSkuEntityList = listResp.getData();
			if(!CollectionUtils.isEmpty(wareSkuEntityList)){
				cart.setStore(wareSkuEntityList.stream().anyMatch(
						wareSkuEntity -> wareSkuEntity.getStock()>0));
			}

			Resp<List<SkuSaleAttrValueEntity>> saleAttrsBySkuIdResp = this.pmsClient.querySaleAttrsBySkuId(cart.getSkuId());
			List<SkuSaleAttrValueEntity> saleAttrValueEntities = saleAttrsBySkuIdResp.getData();
			cart.setSaleAttrs(saleAttrValueEntities);

			Resp<List<ItemSaleVo>> itemSaleBySkuIdResp = this.smsClient.queryItemSaleBySkuId(cart.getSkuId());
			List<ItemSaleVo> itemSaleVos = itemSaleBySkuIdResp.getData();
			cart.setSales(itemSaleVos);

			//保存当前价格
			this.redisTemplate.opsForValue().set(PRICE_PREFIX+skuId,skuInfoEntity.getPrice().toString());
		}
		hashOps.put(skuId,JSON.toJSONString(cart));
	}

	@Override
	public List<Cart> queryCarts() {
		//获取用户登陆信息
		UserInfo userInfo = LoginInterceptor.getUserInfo();
		Long userId = userInfo.getUserId();
		String userKey= KEY_PREFIX + userInfo.getUserKey();
		//1.先查询未登陆的购物车
		BoundHashOperations<String, Object, Object> userKeyHashOps = this.redisTemplate.boundHashOps(userKey);
		List<Object> values = userKeyHashOps.values();
		List<Cart> carts = null;
		if(!CollectionUtils.isEmpty(values)){
			carts=values.stream().map(cartJson->{
				Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);

				//查询当前价格
				String price = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
				cart.setCurrentPrice(new BigDecimal(price));
				return cart;
			}).collect(Collectors.toList());
		}
		//2.判断是否登陆，未登陆直接返回
		if(userId==null){
			return carts;
		}
		//3.已登陆  合并购物车
		String userIdKey = KEY_PREFIX + userId;
		BoundHashOperations<String, Object, Object> userIdHashOps = this.redisTemplate.boundHashOps(userIdKey);
		if(!CollectionUtils.isEmpty(carts)){
			carts.forEach(cart -> {
				Integer count = cart.getCount();
				Long skuId = cart.getSkuId();
				if(userIdHashOps.hasKey(cart.getSkuId().toString())){ // 如果登录状态下有该记录，更新数量
					String cartJson = userIdHashOps.get(skuId.toString()).toString();
					cart = JSON.parseObject(cartJson, Cart.class);
					cart.setCount(cart.getCount() + count);

					//查询当前价格
					String price = this.redisTemplate.opsForValue().get(PRICE_PREFIX + skuId.toString());
					cart.setCurrentPrice(new BigDecimal(price));
				}
				//如果没有该记录，直接添加
				userIdHashOps.put(cart.getSkuId().toString(),JSON.toJSONString(cart));
			});
		}

		//4.删除未登录时的购物车
		redisTemplate.delete(userKey);
		//5.查询返回
		List<Object> userIdValues = userIdHashOps.values();
		List<Object> userIdCartJsons = userIdHashOps.values();
		if(!CollectionUtils.isEmpty(userIdCartJsons)){
			return userIdCartJsons.stream().map(cartJson->
				JSON.parseObject(cartJson.toString(),Cart.class)).collect(Collectors.toList());
		}
		return null;
	}

	@Override
	public void updateNum(Cart cart) {

		//获取用户登陆信息
		UserInfo userInfo = LoginInterceptor.getUserInfo();
		String key = KEY_PREFIX;  //外层key
		//判断是否登陆过
		if(userInfo.getUserId() != null){
			//已登录userId
			key+=userInfo.getUserId();
		}else {
			//未登录  userKey
			key+=userInfo.getUserKey();
		}
		//获取购物车 内层的map
		BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
		//判断购物车中是否存在该商品
		if(hashOps.hasKey(cart.getSkuId().toString())){
			String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
			Integer count = cart.getCount();
			cart = JSON.parseObject(cartJson, Cart.class);
			cart.setCount(count);
			hashOps.put(cart.getSkuId().toString(),JSON.toJSONString(cart));
		}
	}

	/**
	 * 购物车选中状态
	 * @param cart
	 */
	@Override
	public void check(Cart cart) {
		//获取外层key
		UserInfo userInfo = LoginInterceptor.getUserInfo();
		String key = KEY_PREFIX;
		if(userInfo.getUserId()!=null){
			key+=userInfo.getUserId();
		}else{
			key+=userInfo.getUserKey();
		}
		//获取内层map
		BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

		//判断购物车中是否有该商品
		if(hashOps.hasKey(cart.getSkuId().toString())){
			String cartJson = hashOps.get(cart.getSkuId()).toString();
			Boolean check = cart.getCheck();
			cart = JSON.parseObject(cartJson,Cart.class);
			cart.setCheck(check);
			hashOps.put(cart.getSkuId().toString(),JSON.toJSONString(cart));
		}
	}

	@Override
	public List<Cart> queryCheckedCarts(Long userId) {
		BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
		//获取所以购物车记录
		List<Object> values = hashOps.values();
		if(!CollectionUtils.isEmpty(values)){  //获取选中的购物车记录
			return values.stream()
					.map(cartJson->JSON.parseObject(cartJson.toString(),Cart.class))
					.filter(cart -> cart.getCheck()).collect(Collectors.toList());
		}
		return null;
	}

	@Override
	public void delete(Long skuId) {
		String key = KEY_PREFIX;
		//获取用户登陆信息
		UserInfo userInfo = LoginInterceptor.getUserInfo();
		//判断是否登陆过
		if(userInfo.getUserId() != null){
			//已登录userId
			key+=userInfo.getUserId();
		}else {
			//未登录  userKey
			key+=userInfo.getUserKey();
		}
		//获取购物车
		BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
		//判断购物车中是否存在该商品
		if(hashOps.hasKey(skuId.toString())){
			hashOps.delete(skuId.toString());  //以内层map删除
		}
	}


}
















