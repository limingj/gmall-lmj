package com.atguigu.gmall.sms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.sms.Vo.vo.ItemSaleVo;
import com.atguigu.gmall.sms.Vo.vo.SaleVo;
import com.atguigu.gmall.sms.dao.SkuBoundsDao;
import com.atguigu.gmall.sms.dao.SkuFullReductionDao;
import com.atguigu.gmall.sms.dao.SkuLadderDao;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsDao, SkuBoundsEntity> implements SkuBoundsService {

    @Autowired
    private SkuLadderDao skuLadderDao;
    @Autowired
    private SkuFullReductionDao skuFullReductionDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuBoundsEntity> page = this.page(
                new Query<SkuBoundsEntity>().getPage(params),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageVo(page);
    }

    @Transactional
    @Override
    public void saveSalles(SaleVo saleVo) {
        //保存积分
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(saleVo,skuBoundsEntity);
        // `work` tinyint(1) DEFAULT NULL COMMENT '
        // 优惠生效情况[1111（四个状态位，从右到左）;
        // 0 - 无优惠，成长积分是否赠送;1 - 无优惠，购物积分是否赠送;
        // 2 - 有优惠，成长积分是否赠送;
        // 3 -有优惠，购物积分是否赠送【状态位0：不赠送，1：赠送】]',
        List<String> work = saleVo.getWork();
        skuBoundsEntity.setWork(
                 new Integer(work.get(0))+new Integer(work.get(1))*2+
                 new Integer(work.get(2))*4+new Integer(work.get(3))*8);
        this.save(skuBoundsEntity);

        //保存打折
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(saleVo,skuLadderEntity);
        skuLadderEntity.setAddOther(saleVo.getLadderAddOther());
        this.skuLadderDao.insert(skuLadderEntity);

        //满减
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(saleVo,skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(saleVo.getFullAddOther());
        skuFullReductionDao.insert(skuFullReductionEntity);
    }

    @Override
    public List<ItemSaleVo> queryItemSaleBySkuId(Long skuId) {

        List<ItemSaleVo> itemSaleVos = new ArrayList<>();
        //积分
        SkuBoundsEntity boundsEntity = this.getOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        if(boundsEntity != null){
            ItemSaleVo itemSaleVo = new ItemSaleVo();
            itemSaleVo.setType("积分");
            itemSaleVo.setDesc("赠送"+boundsEntity.getGrowBounds()+"成长积分"+boundsEntity.getBuyBounds()+"购物积分");
            itemSaleVos.add(itemSaleVo);
        }
        //打折信息
        SkuLadderEntity ladderEntity = skuLadderDao.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        if(ladderEntity != null){
            ItemSaleVo itemSaleVo = new ItemSaleVo();
            itemSaleVo.setType("打折");
            itemSaleVo.setDesc("满"+ladderEntity.getFullCount()+"件，打"+ladderEntity.getDiscount().divide(new BigDecimal(10))+"折");
            itemSaleVos.add(itemSaleVo);
        }

        SkuFullReductionEntity fullReductionEntity = skuFullReductionDao.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
        if(fullReductionEntity != null){
            ItemSaleVo itemSaleVo = new ItemSaleVo();
            itemSaleVo.setType("满减");
            itemSaleVo.setDesc("满"+fullReductionEntity.getFullPrice()+"减"+fullReductionEntity.getReducePrice());
            itemSaleVos.add(itemSaleVo);
        }
        return itemSaleVos;
    }
}