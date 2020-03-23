package com.atguigu.gmall.order.execption;

public class OrderExecption extends RuntimeException{
	public OrderExecption() {
		super();
	}

	public OrderExecption(String message) {
		super(message);
	}
}
