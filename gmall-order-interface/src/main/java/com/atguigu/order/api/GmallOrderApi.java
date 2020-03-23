package com.atguigu.order.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.oms.entity.OrderEntity;
import com.atguigu.oms.vo.OrderSubmitVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface GmallOrderApi {
	@PostMapping("order/submit")
	public Resp<OrderEntity> submit(@RequestBody OrderSubmitVo orderSubmitVo);
}
