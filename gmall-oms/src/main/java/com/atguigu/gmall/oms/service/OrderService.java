package com.atguigu.gmall.oms.service;

import com.atguigu.oms.vo.OrderSubmitVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.oms.entity.OrderEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 订单
 *
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2020-01-01 18:39:44
 */
public interface OrderService extends IService<OrderEntity> {

    PageVo queryPage(QueryCondition params);

	OrderEntity saveOrder(OrderSubmitVo orderSubmitVo,Long userId);
}

