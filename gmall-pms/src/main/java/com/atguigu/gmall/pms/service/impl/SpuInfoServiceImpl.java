package com.atguigu.gmall.pms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.SkuInfoDao;
import com.atguigu.gmall.pms.dao.SpuInfoDao;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.fegin.SmsFegin;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.BaseAttrVo;
import com.atguigu.gmall.pms.vo.SkuInfoVo;
import com.atguigu.gmall.pms.vo.SpuInfoVo;
import com.atguigu.gmall.sms.Vo.vo.SaleVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoService spuInfoService;
    @Autowired
    private SpuInfoDescService descService;
    @Autowired
    private SkuImagesService imagesService;
    @Autowired
    private SkuInfoDao skuInfoDao;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private SkuSaleAttrValueService saleAttrValueService;
    @Autowired
    private SmsFegin smsFegin;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private SpuInfoDao spuInfoDao;


    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );
        return new PageVo(page);
    }
    @Override
    public PageVo querySpuInfoByCid(QueryCondition queryCondition, Long cid) {
        //select * from pms_spu_info where catelog_id=225 and(spu_id="" or spu_nam4 like "%xxx%")
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();
        //判断是本类查询还是全站查询
        if(cid!=0){
            queryWrapper.eq("catalog_id", cid);
        }
        //判断搜索关键字是否存在
        String key = queryCondition.getKey();
        if(!StringUtils.isEmpty(key)){
            queryWrapper.and(t->t.eq("id",key).or().like("spu_name",key));
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(queryCondition),
                queryWrapper);

        return new PageVo(page);
    }

    @GlobalTransactional
    @Override
    public void bigSave(SpuInfoVo spuInfoVo) {
        //保存spu_Info
        Long spuId = this.saveSpuInfoVo(spuInfoVo);
        //保存spu信息介绍spu_info_desc
        saveSpuInfoDesc(spuInfoVo,spuId);
        //保存ProductAAttrValue基础属性相关信息
        saveBaseAttrs(spuInfoVo,spuId);
        //保存sku_Info
        saveSkuInfoWithSaleInfo(spuInfoVo,spuId);
        //int i =1/0;
        //发送方  ==》声明交换机
        sendMsg(spuId,"insert");
    }

    private void sendMsg(Long spuId,String type) {
        amqpTemplate.convertAndSend("GMALL-PMS-EXCHANGE","item."+type,spuId);
    }


    public void saveSkuInfoWithSaleInfo(SpuInfoVo spuInfoVo,Long spuId) {
        List<SkuInfoVo> skus = spuInfoVo.getSkus();
        if(CollectionUtils.isEmpty(skus)) {
            return;
        }

        skus.forEach(sku->{
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(sku, skuInfoEntity);
            skuInfoEntity.setSpuId(spuId);
            List<String> images = sku.getImages();
            if(!CollectionUtils.isEmpty(images)){
                skuInfoEntity.setSkuDefaultImg(skuInfoEntity.getSkuDefaultImg()==null?images.get(0):skuInfoEntity.getSkuDefaultImg());
            }
            skuInfoEntity.setSkuCode(UUID.randomUUID().toString());
            skuInfoEntity.setCatalogId(spuInfoVo.getCatalogId());
            skuInfoEntity.setBrandId(spuInfoVo.getBrandId());
            this.skuInfoDao.insert(skuInfoEntity);
            Long skuId = skuInfoEntity.getSkuId();

            //保存图片
            if(!CollectionUtils.isEmpty(images)){
                List<SkuImagesEntity> skuImagesEntitys = images.stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img);
                    skuImagesEntity.setImgSort(0);
                    //是否是默认图片
                    skuImagesEntity.setDefaultImg(StringUtils.equals(img, skuInfoEntity.getSkuDefaultImg()) ? 1 : 0);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                imagesService.saveBatch(skuImagesEntitys);
            }

            List<SkuSaleAttrValueEntity> saleAttrs = sku.getSaleAttrs();
            if(!CollectionUtils.isEmpty(saleAttrs)){
                saleAttrs.forEach(skuSaleAttrValueEntity -> {
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    skuSaleAttrValueEntity.setAttrSort(0);
                });
                saleAttrValueService.saveBatch(saleAttrs);
            }
            //  远程调用  设置商品sku积分  打折  满减  叠加
            SaleVo saleVo = new SaleVo();
            BeanUtils.copyProperties(sku,saleVo);
            saleVo.setSkuId(skuId);
            smsFegin.saveSalles(saleVo);
        });
    }

    public void saveBaseAttrs(SpuInfoVo spuInfoVo,Long spuId) {

        List<BaseAttrVo> baseAttrs = spuInfoVo.getBaseAttrs();
        if(!CollectionUtils.isEmpty(baseAttrs)){
            List<ProductAttrValueEntity> attrValueEntities = baseAttrs.stream().map(baseAttrVo -> {
                ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                BeanUtils.copyProperties(baseAttrVo,productAttrValueEntity);
                productAttrValueEntity.setSpuId(spuId);
                productAttrValueEntity.setAttrSort(0);
                productAttrValueEntity.setQuickShow(0);
                return productAttrValueEntity;
            }).collect(Collectors.toList());
            productAttrValueService.saveBatch(attrValueEntities);
        }
    }

    public void saveSpuInfoDesc(SpuInfoVo spuInfoVo,Long spuId) {
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuId);
        descEntity.setDecript(spuInfoVo.getSpuDescription());
        descService.save(descEntity);
    }

    public Long saveSpuInfoVo(SpuInfoVo spuInfoVo) {
        spuInfoVo.setCreateTime(new Date());
        spuInfoVo.setUodateTime(spuInfoVo.getCreateTime());
        this.save(spuInfoVo);
        return spuInfoVo.getId();
    }
}