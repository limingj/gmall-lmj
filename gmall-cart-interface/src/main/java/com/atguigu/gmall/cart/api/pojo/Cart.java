package com.atguigu.gmall.cart.api.pojo;

import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.Vo.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Cart {
	private Long skuId;
	private String skuTitle;
	private String image;
	private List<SkuSaleAttrValueEntity> saleAttrs;
	private BigDecimal price;
	private BigDecimal currentPrice;
	private Integer count;
	private Boolean store;
	private Boolean check;
	private List<ItemSaleVo> sales;//促销信息

}























