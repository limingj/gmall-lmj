package com.atguigu.gmall.search.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

@Data
@Document(indexName = "goods",type = "info",shards = 3,replicas = 2)
public class Goods {
	@Id
	private Long skuId;
	@Field(type = FieldType.Text,analyzer = "ik_max_word")
	private String skuTitle;
	@Field(type = FieldType.Keyword,index = false)
	private String skuSubTitle;
	@Field(type = FieldType.Double)
	private Double price;
	@Field(type = FieldType.Text,index = false)
	private String defaultImage;


	@Field(type = FieldType.Long)
	private Long sale;
	@Field(type = FieldType.Date)
	private Date createTime;
	@Field(type = FieldType.Boolean)
	private boolean store;

	@Field(type = FieldType.Long)
	private Long brandId;
	@Field(type = FieldType.Keyword)
	private String brandName;
	@Field(type = FieldType.Long)
	private Long categoryId;   //分类Id
	@Field(type = FieldType.Keyword)
	private String categoryName;



	//销售属性值  客户输入
	@Field(type = FieldType.Nested)   //Nested  嵌套子查询
	private List<SearchAttrVo> attrs;
}
