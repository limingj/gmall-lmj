package com.atguigu.gmall.oms.dao;

import com.atguigu.oms.entity.PaymentInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付信息表
 * 
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2020-01-01 18:39:44
 */
@Mapper
public interface PaymentInfoDao extends BaseMapper<PaymentInfoEntity> {
	
}
