package com.atguigu.oms.vo;

import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.Vo.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVo {

	private Long skuId;
	private String skuTitle;
	private String image;
	private List<SkuSaleAttrValueEntity> saleAttrs;
	private BigDecimal price;
	private Integer count;
	private Boolean store;
	private BigDecimal weight;
	private List<ItemSaleVo> sales;//促销信息
}
