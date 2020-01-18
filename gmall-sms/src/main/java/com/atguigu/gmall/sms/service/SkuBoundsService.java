package com.atguigu.gmall.sms.service;

import com.atguigu.gmall.sms.Vo.vo.ItemSaleVo;
import com.atguigu.gmall.sms.Vo.vo.SaleVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 商品sku积分设置
 *
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2020-01-02 08:07:26
 */
public interface SkuBoundsService extends IService<SkuBoundsEntity> {

    PageVo queryPage(QueryCondition params);

    void saveSalles(SaleVo saleVo);

	List<ItemSaleVo> queryItemSaleBySkuId(Long skuId);
}

