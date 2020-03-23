package com.atguigu.gmall.cart.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.api.pojo.Cart;
import com.atguigu.gmall.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("cart")
public class CartController {
	@Autowired
	private CartService cartService;

	@PostMapping
	public Resp<Object> addCart(@RequestBody Cart cart){
		cartService.addCart(cart);
		return Resp.ok(null);
	}

	@GetMapping
	public Resp<List<Cart>> queryCarts(){
		List<Cart> carts = this.cartService.queryCarts();
		return Resp.ok(carts);
	}

	@GetMapping("update")
	public Resp<Object> updateNum(@RequestBody Cart cart){
		this.cartService.updateNum(cart);
		return Resp.ok(null);
	}

	/**
	 * 选中状态：传递参数：skuId  check的状态
	 * @param cart
	 * @return
	 */
	@PostMapping("check")
	public Resp<Object> check(@RequestBody Cart cart){
		this.cartService.check(cart);
		return Resp.ok(null);
	}

	@PostMapping("delete")
	public Resp<Object> delete(Long skuId){
		this.cartService.delete(skuId);
		return Resp.ok(null);
	}

	//order调用接口通过地址栏是传递userId
	@GetMapping("{userId}")
	public List<Cart> queryCheckedCarts(@PathVariable("userId")Long userId){
		return this.cartService.queryCheckedCarts(userId);
	}

}
