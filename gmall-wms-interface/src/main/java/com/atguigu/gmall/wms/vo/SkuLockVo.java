package com.atguigu.gmall.wms.vo;

import lombok.Data;

@Data

/**
 * 验库存  锁库存
 */
public class SkuLockVo {
	private Long skuId;

	private Integer count;

	private Boolean lock = false;//锁定状态  true=验证成功并锁定  false =库存不足

	private String orderToken;

	private Long wareSkuId; //锁定成功后，记录锁定的仓库
static int i = 0;
	public static void main(String[] args) {
		System.out.println(2 + 2 + "5" + 2 + 2);
		Thread t =new Thread();
	}
}










