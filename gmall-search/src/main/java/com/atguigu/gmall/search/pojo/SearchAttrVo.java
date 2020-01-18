package com.atguigu.gmall.search.pojo;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;


@Data
public class SearchAttrVo {

	@Field(type = FieldType.Long)
	private Long attrId;
	@Field(type = FieldType.Keyword)
	private String attrNames;
	@Field(type = FieldType.Keyword)
	private String attrValues;

}
