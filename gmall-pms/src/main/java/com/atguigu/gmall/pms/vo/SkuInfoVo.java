package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuImagesEntity;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author shkstart
 * @create 2020-01-05 9:05

**/
@Data
public class SkuInfoVo extends SkuInfoEntity {

    //商品sku积分设置
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
    private List<String> work;
    //打折
    private Integer fullCount;
    private BigDecimal discount;
    //是否叠加
    private Integer ladderAddOther;
    //满减
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;

    private List<SkuSaleAttrValueEntity> saleAttrs;

    private List<String> images;
}
