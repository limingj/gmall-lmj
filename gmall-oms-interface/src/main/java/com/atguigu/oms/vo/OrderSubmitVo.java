package com.atguigu.oms.vo;

import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.atguigu.oms.vo.OrderItemVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSubmitVo {

	private String orderToken;//防重

	private MemberReceiveAddressEntity address;

	private Integer payType;//支付方式

	private String deliveryCompany;//物流公司

	private List<OrderItemVo> items;//送货清单

	private BigDecimal totalPrice;

	private Integer bounds;
}
