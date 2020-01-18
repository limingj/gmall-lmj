package com.atguigu.gmall.pms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.CategoryDao;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;
import com.atguigu.gmall.pms.vo.CategoryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryDao categoryDao;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageVo(page);
    }

    //商品分类
    @Override
    public List<CategoryEntity> queryAllCategoryTree(Integer level, Long pid) {

        QueryWrapper<CategoryEntity> queryWrapper = new QueryWrapper();
        //分层查询
        if(level!=0){
            QueryWrapper<CategoryEntity> cat_level = queryWrapper.eq("cat_level", level);
        }

        if(pid!=null){
            QueryWrapper<CategoryEntity> parent_cid = queryWrapper.eq("parent_cid", pid);
        }
        return this.categoryDao.selectList(queryWrapper);
    }

    @Override
    public List<CategoryVo> querysubCates(Long pid) {
        List<CategoryVo> categoryVos = this.categoryDao.querysubCates(pid);
        return categoryVos;
    }

}