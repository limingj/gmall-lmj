package com.atguigu.gmall.wms.service;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


/**
 * 商品库存
 *
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2020-01-02 08:00:37
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageVo queryPage(QueryCondition params);

    List<SkuLockVo> checkAndLockStock(List<SkuLockVo> skuLockVos);

}

