package com.atguigu.oms.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.oms.entity.OrderEntity;
import com.atguigu.oms.vo.OrderSubmitVo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface GmallOmdApi {
	@PostMapping("oms/order/{userId}")
	public Resp<OrderEntity> saveOrder(@RequestBody OrderSubmitVo orderSubmitVo,
									   @PathVariable("userId")Long userId);
}
