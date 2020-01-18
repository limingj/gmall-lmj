package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.AttrSaveVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 商品属性
 *
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2020-01-01 18:16:29
 */
public interface AttrService extends IService<AttrEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo queryAttrByCidAndAttrType(Long type, Long cid, QueryCondition queryCondition);

    void saveVo(AttrSaveVo attrSaveVo);
}

