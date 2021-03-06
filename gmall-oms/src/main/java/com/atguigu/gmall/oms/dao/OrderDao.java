package com.atguigu.gmall.oms.dao;

import com.atguigu.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2020-01-01 18:39:44
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

	public int closeOrder(String orderToken);
}
