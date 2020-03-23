package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.api.pojo.Cart;

import java.util.List;

public interface CartService {
	void addCart(Cart cart);

	List<Cart> queryCarts();

	void updateNum(Cart cart);

	void delete(Long skuId);

	void check(Cart cart);

	List<Cart> queryCheckedCarts(Long userId);
}
