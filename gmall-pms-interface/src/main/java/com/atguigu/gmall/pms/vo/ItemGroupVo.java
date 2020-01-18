package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import lombok.Data;

import java.util.List;

@Data
public class ItemGroupVo {
	private Long id;     //组id
	private String name;  //组名称
	private List<ProductAttrValueEntity> baseAttrValues;  //值
}
