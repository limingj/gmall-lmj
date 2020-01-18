package com.atguigu.gmall.pms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.dao.AttrGroupDao;
import com.atguigu.gmall.pms.dao.ProductAttrValueDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.pms.vo.AttrGroupVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    private AttrDao attrDao;
    @Autowired
    private ProductAttrValueDao valueDao;

    @Autowired
    private AttrGroupDao attrGroupDao;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageVo(page);
    }

/*    public IPage<T> page(IPage<T> page, Wrapper<T> queryWrapper) {
        return baseMapper.selectPage(page, queryWrapper);
    }*/
    @Override
    public PageVo queryArrtGroupByCid(Long catid, QueryCondition queryCondition) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(queryCondition),
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catid));
        return new PageVo(page);
    }

    @Override
    public AttrGroupVo queryAttrGroupByGid(Long gid) {
        AttrGroupVo attrGroupVo = new AttrGroupVo();
        //根据查询组
        AttrGroupEntity attrGroup = this.getById(gid);
        BeanUtils.copyProperties(attrGroup,attrGroupVo);

        //查询中间表relation
        List<AttrAttrgroupRelationEntity> relations = this.attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>()
                .eq("attr_group_id", gid));
        attrGroupVo.setRelations(relations);

        //中间表为空
        if(CollectionUtils.isEmpty(relations)){
            return attrGroupVo;
        }

        //拿到规格参数Id   stream表达式   把一个集合对象转为另一个集合对象
        List<Long> attrIds = relations.stream().map(relation -> relation.getAttrId()).collect(Collectors.toList());

        //根据规格参数Id，查询规格参数（属性）
        List<AttrEntity> attrEntities = this.attrDao.selectBatchIds(attrIds);
        attrGroupVo.setAttrEntities(attrEntities);
        return attrGroupVo;
    }

    @Override
    public List<AttrGroupVo> queryWithAttrsByCid(Long catId) {

        //根据分类Id 查询规格参数组  1：n
/*        List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>()
                .eq("catelog_id", catId));*/

        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>()
                .eq("catelog_id", catId));

        //根据各组Id  ==》查询中间表
        return attrGroupEntities.stream().map(attrGroupEntity ->
                this.queryAttrGroupByGid(attrGroupEntity.getAttrGroupId()))
                .collect(Collectors.toList());
        }

    @Override
    public List<ItemGroupVo> queryItemGroupVosByCIdAndSpuId(Long cid, Long spuId) {
        //查询规格参数  spu ==>对应的categoryId
        //根据categoryId ==> 查询商品分类所在分组
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", cid));
        if (CollectionUtils.isEmpty(groupEntities)){
            return null;
        }
        List<ItemGroupVo> itemGroupVos = groupEntities.stream().map(group -> {
            ItemGroupVo itemGroupVo = new ItemGroupVo();
            itemGroupVo.setId(group.getAttrGroupId());
            itemGroupVo.setName(group.getAttrGroupName());
            //根据分组Id ==>查询中间表 ==》获得属性id
            List<AttrAttrgroupRelationEntity> relationEntitys = this.attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", group.getAttrGroupId()));
            List<Long> attrIds = relationEntitys.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(attrIds)) {
                //根据属性id  ==》 查询属性值
                List<ProductAttrValueEntity> productAttrValueEntities = valueDao.selectList(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId).in("attr_id", attrIds));
                itemGroupVo.setBaseAttrValues(productAttrValueEntities);
            }
            return itemGroupVo;
        }).collect(Collectors.toList());
        return itemGroupVos;
    }
}