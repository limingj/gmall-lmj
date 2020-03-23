package com.atguigu.gamll.pay.controller;

import com.atguigu.gamll.pay.vo.PayVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("pay")
public class PayController {


	//支付宝异步调用
	@PostMapping("success")
	public String successful(PayVo payVo){
		return null;
	}
}
