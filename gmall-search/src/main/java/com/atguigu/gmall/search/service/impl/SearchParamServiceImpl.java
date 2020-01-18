package com.atguigu.gmall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.response.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchParamService;
import com.atguigu.gmall.search.vo.SearchParamVo;
import com.atguigu.gmall.search.vo.SearchResponseAttrVO;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchParamServiceImpl implements SearchParamService {
	@Autowired
	private RestHighLevelClient highLevelClient;


	@Override
	public SearchResponseVo search(SearchParamVo params) throws IOException {

		SearchResponse searchResponse = this.highLevelClient.search(new SearchRequest(new String[]{"goods"}, buildDSL(params)), RequestOptions.DEFAULT);

		SearchResponseVo searchResponseVo = this.parseSearchResult(searchResponse);

		searchResponseVo.setPageNum(params.getPageNum());
		searchResponseVo.setPageSize(params.getPageSize());

		return searchResponseVo;

	}

	public SearchSourceBuilder buildDSL(SearchParamVo searchParam) {
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		//sourceBuilder.fetchSource(new String[]{"skuId", "skuTitle", "skuSubTitle", "price", "defaultImage"}, null);

		String key = searchParam.getKey();
		if (StringUtils.isEmpty(key)) {
			// 显示默认的商品列表
			return sourceBuilder;
		}

		// 1. 构建查询条件
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		// 1.1. 构建匹配查询
		boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", key).operator(Operator.AND));

		// 1.2. 构建过滤条件
		// 1.2.1. 品牌的过滤
		Long[] brandIds = searchParam.getBrand();
		if (brandIds != null && brandIds.length != 0) {
			boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandIds));
		}

		// 1.2.2. 分类的过滤
		Long[] catelog3 = searchParam.getCatelog3();
		if (catelog3 != null && catelog3.length != 0) {
			boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId", catelog3));
		}

		// 1.2.3. 价格区间的过滤
		RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price");
		Double priceFrom = searchParam.getPriceFrom();
		if (priceFrom != null) {
			rangeQueryBuilder.gte(priceFrom);
		}
		Double priceTo = searchParam.getPriceTo();
		if (priceTo != null) {
			rangeQueryBuilder.lte(priceTo);
		}
		boolQueryBuilder.filter(rangeQueryBuilder);

		// 1.2.4. 规格属性的过滤
		List<String> props = searchParam.getProps();
		if (!CollectionUtils.isEmpty(props)) {
			props.forEach(prop -> {
				String[] attr = StringUtils.split(prop, ":");
				if (attr != null && attr.length == 2) {
					String attrId = attr[0];
					String[] attrValues = StringUtils.split(attr[1], "-");
					BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
					boolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
					boolQuery.must(QueryBuilders.termsQuery("attrs.attrValues", attrValues));
					boolQueryBuilder.filter(QueryBuilders.nestedQuery("attrs", boolQuery, ScoreMode.None));
				}
			});
		}
		sourceBuilder.query(boolQueryBuilder);

		// 2. 构建排序
		String order = searchParam.getOrder();
		if (StringUtils.isNotBlank(order)) {
			String[] orders = StringUtils.split(":");
			if (orders != null && orders.length == 2) {
				String orderField = orders[0];
				String orderBy = orders[1];
				switch (orderField) {
					case "0":
						orderField = "_score";
						break;
					case "1":
						orderField = "sale";
						break;
					case "2":
						orderField = "price";
						break;
					default:
						orderField = "_score";
						break;
				}
				sourceBuilder.sort(orderField, StringUtils.equals(orderBy, "asc") ? SortOrder.ASC : SortOrder.DESC);
			}
		}

		// 3. 构建分页
		Integer pageNum = searchParam.getPageNum();
		Integer pageSize = searchParam.getPageSize();
		sourceBuilder.from((pageNum - 1) * pageSize);
		sourceBuilder.size(pageSize);

		// 4. 构建高亮
		sourceBuilder.highlighter(new HighlightBuilder().field("skuTitle").preTags("<span style='color:red;'>").postTags("</span>"));

		// 5. 构建聚合
		// 5.1. 品牌的聚合
		sourceBuilder.aggregation(
				AggregationBuilders.terms("brandIdAgg").field("brandId").subAggregation(
						AggregationBuilders.terms("brandNameAgg").field("brandName")
				)
		);

		// 5.2. 分类的聚合
		sourceBuilder.aggregation(
				AggregationBuilders.terms("categoryIdAgg").field("categoryId").subAggregation(
						AggregationBuilders.terms("categoryNameAgg").field("categoryName")
				)
		);

		// 5.3. 规格属性的聚合
		sourceBuilder.aggregation(
				AggregationBuilders.nested("attrsAgg", "attrs").subAggregation(
						AggregationBuilders.terms("attrIdAgg").field("attrs.attrId").subAggregation(
								AggregationBuilders.terms("attrNameAgg").field("attrs.attrNames")
						).subAggregation(
								AggregationBuilders.terms("attrValueAgg").field("attrs.attrValues")
						)
				)
		);
		return sourceBuilder;
	}


	private SearchResponseVo parseSearchResult(SearchResponse searchResponse) {
		SearchResponseVo searchResponseVo = new SearchResponseVo();
		SearchHits hits = searchResponse.getHits();
		//总记录数
		long totalHits = hits.getTotalHits();
		if (totalHits == 0) {
			System.out.println("没有数据");
			return null;
		}
		searchResponseVo.setTotal(totalHits);
		SearchHit[] hitsHits = hits.getHits();
		//查询结果集 query
		List<Goods> goodsList = new ArrayList<>();
		for (SearchHit hitsHit : hitsHits) {
			String sourceAsString = hitsHit.getSourceAsString();
			Goods goods = JSON.parseObject(sourceAsString, Goods.class);
			Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
			HighlightField skuTitle = highlightFields.get("skuTitle");
			//设置高亮字段
			goods.setSkuTitle(skuTitle.getFragments()[0].string());
			goodsList.add(goods);
		}
		searchResponseVo.setProducts(goodsList);


		Map<String, Aggregation> aggregationaMap = searchResponse.getAggregations().asMap();

		//品牌的聚和结果
		ParsedLongTerms brandIdAgg = (ParsedLongTerms) aggregationaMap.get("brandIdAgg");
		SearchResponseAttrVO brandAttrVO = new SearchResponseAttrVO();
		brandAttrVO.setProductAttributeId(null);
		brandAttrVO.setName("品牌");
		List<? extends Terms.Bucket> buckets = brandIdAgg.getBuckets();
		if (!CollectionUtils.isEmpty(buckets)) {
			List<String> brandValue = buckets.stream().map(bucket -> {
				//“品牌” value: [{id:100,name:华为}
				Map<String, Object> map = new HashMap<>();
				map.put("id", bucket.getKeyAsNumber());
				ParsedStringTerms brandNameAgg = (ParsedStringTerms) bucket.getAggregations().asMap().get("brandNameAgg");
				map.put("name", brandNameAgg.getBuckets().get(0).getKeyAsString());
				return JSON.toJSONString(map);
			}).collect(Collectors.toList());
			brandAttrVO.setValue(brandValue);
			searchResponseVo.setBrand(brandAttrVO);
		}

		//分类解析
		ParsedLongTerms catelogIdAgg = (ParsedLongTerms) aggregationaMap.get("categoryIdAgg");
		SearchResponseAttrVO cateAttrVO = new SearchResponseAttrVO();
		cateAttrVO.setName("分类");
		List<? extends Terms.Bucket> cateBuckets = catelogIdAgg.getBuckets();
		if (!CollectionUtils.isEmpty(cateBuckets)) {
			List<String> catelogValue = cateBuckets.stream().map(cateBucket -> {
				Map<String, Object> map = new HashMap<>();
				map.put("id", cateBucket.getKeyAsNumber());
				ParsedStringTerms categoryNameAgg = (ParsedStringTerms) cateBucket.getAggregations().asMap().get("categoryNameAgg");
				map.put("name", categoryNameAgg.getBuckets().get(0).getKeyAsString());
				return JSON.toJSONString(map);
			}).collect(Collectors.toList());
			cateAttrVO.setValue(catelogValue);
			searchResponseVo.setCatelog(cateAttrVO);
		}

		ParsedNested attrsAgg = (ParsedNested) aggregationaMap.get("attrsAgg");
		ParsedLongTerms attrIdAgg = (ParsedLongTerms) attrsAgg.getAggregations().get("attrIdAgg");
		List<? extends Terms.Bucket> idBuckets = attrIdAgg.getBuckets();
		if (!CollectionUtils.isEmpty(idBuckets)){
			List<SearchResponseAttrVO> attrVOS = idBuckets.stream().map(bucket -> {
				SearchResponseAttrVO attrVO = new SearchResponseAttrVO();
				attrVO.setProductAttributeId(bucket.getKeyAsNumber().longValue());
				// 获取规格参数名子聚合，解析出规格参数名
				ParsedStringTerms attrNameAgg = (ParsedStringTerms) bucket.getAggregations().get("attrNameAgg");
				attrVO.setName(attrNameAgg.getBuckets().get(0).getKeyAsString());
				// 获取规格参数值子聚合，解析出规格参数值集合
				ParsedStringTerms attrValueAgg = (ParsedStringTerms) bucket.getAggregations().get("attrValueAgg");
				List<? extends Terms.Bucket> valueBuckets = attrValueAgg.getBuckets();
				List<String> values = valueBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
				attrVO.setValue(values);
				return attrVO;
			}).collect(Collectors.toList());
			searchResponseVo.setAttrs(attrVOS);
		}
		return searchResponseVo;
	}
}







