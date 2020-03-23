package com.atguigu.order.vo;

import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.atguigu.oms.vo.OrderItemVo;
import lombok.Data;

import java.util.List;

@Data
public class OrderConfirmVo {

	private List<MemberReceiveAddressEntity> addresses;
	private List<OrderItemVo> orderItemVos;

	private Integer bounds;//积分信息

	private String orderToken;  //防止订单重复提交
}
