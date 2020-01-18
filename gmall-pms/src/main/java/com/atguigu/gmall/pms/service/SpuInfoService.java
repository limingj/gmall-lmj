package com.atguigu.gmall.pms.service;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.gmall.pms.vo.SpuInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;


/**
 * spu信息
 *
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2020-01-01 18:16:28
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo querySpuInfoByCid(QueryCondition queryCondition, Long cid);

    void bigSave(SpuInfoVo spuInfoVo);
    Long saveSpuInfoVo(SpuInfoVo spuInfoVo);

    void saveBaseAttrs(SpuInfoVo spuInfoVo,Long spuId);
    void saveSkuInfoWithSaleInfo(SpuInfoVo spuInfoVo,Long spuId);

}

