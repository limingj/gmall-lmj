package com.atguigu.gmall.search.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParamVo {

	private String key;

	private Long[] catelog3;

	private Long[] brand;

	private Double priceFrom;
	private Double priceTo;

	//属性嵌套  attrId  attrName
	private List<String> props;

	//排序方式 （0，1，2）：des/aes
	private String order;

	private Integer pageNum=1;
	private Integer pageSize=64;

}
