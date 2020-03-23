package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.cart.api.GmallCartApi;
import com.atguigu.gmall.cart.api.pojo.Cart;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("cart-service")
public interface GmallCartClient extends GmallCartApi {

	@GetMapping("cart/{userId}")
	public List<Cart> queryCheckedCarts(@PathVariable("userId")Long userId);
}
