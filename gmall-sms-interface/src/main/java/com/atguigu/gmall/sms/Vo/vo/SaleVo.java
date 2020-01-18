package com.atguigu.gmall.sms.Vo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author shkstart
 * @create 2020-01-05 13:32
 */
@Data
public class SaleVo {
    private Long skuId;
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
}
