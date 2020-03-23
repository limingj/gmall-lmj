package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.cart.api.pojo.Cart;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
	@GetMapping("{userId}")
	public List<Cart> queryCheckedCarts(@PathVariable("userId")Long userId);
}
