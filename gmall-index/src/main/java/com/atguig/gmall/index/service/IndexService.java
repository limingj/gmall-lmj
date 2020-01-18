package com.atguig.gmall.index.service;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVo;

import java.util.List;

public interface IndexService {
	List<CategoryEntity> queryCates();

	List<CategoryVo> querysubCates(Long pid);

}
