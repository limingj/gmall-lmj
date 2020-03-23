package com.atguigu.gmall.order.controller;

import com.alipay.api.AlipayApiException;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.template.AlipayTemplate;
import com.atguigu.gmall.order.vo.PayAsyncVo;
import com.atguigu.gmall.order.vo.PayVo;
import com.atguigu.oms.entity.OrderEntity;
import com.atguigu.oms.vo.OrderSubmitVo;
import com.atguigu.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("order")
public class OrderController {
	@Autowired
	private OrderService orderService;
	@Autowired
	private AlipayTemplate alipayTemplate;

	@GetMapping("confirm")
	public Resp<OrderConfirmVo> confirm(){
		OrderConfirmVo orderConfirmVo = this.orderService.confirm();
		return Resp.ok(orderConfirmVo);
	}

	@PostMapping("submit")
	public Resp<Object> submit(@RequestBody OrderSubmitVo orderSubmitVo){
		Resp<OrderEntity> orderEntityResp = Resp.ok(this.orderService.submit(orderSubmitVo));
		OrderEntity orderEntity = orderEntityResp.getData();
		if(orderEntity != null){
			//远程调用支付接口
			PayVo payVo = new PayVo();
			payVo.setTotal_amount(orderEntity.getTotalAmount().toString());
			payVo.setOut_trade_no(orderEntity.getOrderSn());
			payVo.setSubject("谷粒商场");
			payVo.setBody("谷粒商场支付平台");
			String form = null;
			try {
				form = alipayTemplate.pay(payVo);
			} catch (AlipayApiException e) {
				e.printStackTrace();
			}
		}
		return Resp.ok(null);
	}

	@PostMapping("pay/success")
	public Resp<Object> success(PayAsyncVo payAsyncVo){

		return Resp.ok(true);
	}
}
