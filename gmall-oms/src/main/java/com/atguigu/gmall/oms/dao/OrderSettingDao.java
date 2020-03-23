package com.atguigu.gmall.oms.dao;

import com.atguigu.oms.entity.OrderSettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单配置信息
 * 
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2020-01-01 18:39:44
 */
@Mapper
public interface OrderSettingDao extends BaseMapper<OrderSettingEntity> {
	
}
