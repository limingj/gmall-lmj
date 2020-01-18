package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.sms.Vo.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ItemVo {

	private Long skuId;
	private String skuTitle;
	private String skuSubTitle;
	private BigDecimal weight;
	private BigDecimal price;


	private Long spuId;
	private String spuName;
	private Long categoryId;
	private String categoryName;
	private Long brandId;
	private String brandName;

	//sku图片
	private List<SkuImagesEntity> images;
	//库存信息
	private Boolean store;
	//所有促销属性
	private List<ItemSaleVo> Sales;  //积分/打折/满减

	//spu下的所有sku所有销售属性
	private List<SkuSaleAttrValueEntity> saleAttrValue;

	private List<String> desc;

	//规格参数
	private List<ItemGroupVo> groupVos;
}
