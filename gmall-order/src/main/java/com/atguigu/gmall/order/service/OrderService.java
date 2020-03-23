package com.atguigu.gmall.order.service;

import com.atguigu.oms.entity.OrderEntity;
import com.atguigu.oms.vo.OrderSubmitVo;
import com.atguigu.order.vo.OrderConfirmVo;


public interface OrderService {
	OrderConfirmVo confirm();

	OrderEntity submit(OrderSubmitVo orderSubmitVo);
}
