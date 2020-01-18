package com.atguigu.gmall.search;

import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.config.GoodsRepository;
import com.atguigu.gmall.search.fegin.PmsClient;
import com.atguigu.gmall.search.fegin.WmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {

	@Autowired
	private ElasticsearchRestTemplate restTemplate;
	@Autowired
	private PmsClient pmsClient;

	@Autowired
	private GoodsRepository repository;
	@Autowired
	private WmsClient wmsClient;


	@Test
	void contextLoads() {
		restTemplate.createIndex(Goods.class);
		restTemplate.putMapping(Goods.class);
	}


	@Test
	void saveGoods(){
		Long pageNum =1l;
		Long pageSize =100l;
		do{
			QueryCondition queryCondition = new QueryCondition();
			queryCondition.setPage(pageNum);
			queryCondition.setLimit(pageSize);
			Resp<List<SpuInfoEntity>> listResp = this.pmsClient.searchPage(queryCondition);
			List<SpuInfoEntity> spuInfoEntities = listResp.getData();
			if (!CollectionUtils.isEmpty(spuInfoEntities)){
				for (SpuInfoEntity spuInfoEntity: spuInfoEntities) {
					Goods goods = new Goods();
					Long spuId = spuInfoEntity.getId();
					System.out.println(spuId);
					Resp<List<SkuInfoEntity>> skuResp = this.pmsClient.querySkuInfoBySkuId(spuId);
					List<SkuInfoEntity> skuInfoEntitys = skuResp.getData();
					if (!CollectionUtils.isEmpty(skuInfoEntitys)){
						List<Goods> goodsLists = skuInfoEntitys.stream().map(skuInfoEntity -> {

							goods.setSkuId(skuInfoEntity.getSkuId());
							goods.setSkuTitle(skuInfoEntity.getSkuTitle());
							goods.setSkuSubTitle(skuInfoEntity.getSkuSubtitle());
							goods.setPrice(skuInfoEntity.getPrice().doubleValue());
							goods.setDefaultImage(skuInfoEntity.getSkuDefaultImg());

							goods.setSale(10l);
							goods.setCreateTime(spuInfoEntity.getCreateTime());
							Resp<List<WareSkuEntity>> wmsResp = wmsClient.queryWareSkuBySkuId(skuInfoEntity.getSkuId());
							List<WareSkuEntity> wareSkuEntities = wmsResp.getData();
							if (!CollectionUtils.isEmpty(wareSkuEntities)){
								goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock()>0));

							}
							//stream流判断是否有货
							Resp<BrandEntity> brandEntityResp = this.pmsClient.queryBrandById(skuInfoEntity.getBrandId());
							BrandEntity brandEntity = brandEntityResp.getData();
							if (brandEntity != null) {
								goods.setBrandId(skuInfoEntity.getBrandId());
								goods.setBrandName(brandEntity.getName());
							}

							goods.setCategoryId(skuInfoEntity.getCatalogId());
							Resp<CategoryEntity> catResp = pmsClient.queryCategoryById(skuInfoEntity.getCatalogId());
							CategoryEntity categoryEntity = catResp.getData();
							if(categoryEntity!=null){
								goods.setCategoryName(categoryEntity.getName());
							}

							Resp<List<ProductAttrValueEntity>> attrValueResp = pmsClient.searchAttrValues(spuId);
							List<ProductAttrValueEntity>  attrValueEntitys= attrValueResp.getData();
							if (!CollectionUtils.isEmpty(attrValueEntitys)){
								List<SearchAttrVo> searchAttrVos = attrValueEntitys.stream().map(attrValueEntity -> {
									SearchAttrVo searchAttrVo = new SearchAttrVo();
									searchAttrVo.setAttrId(attrValueEntity.getAttrId());
									searchAttrVo.setAttrNames(attrValueEntity.getAttrName());
									searchAttrVo.setAttrValues(attrValueEntity.getAttrValue());
									return searchAttrVo;
								}).collect(Collectors.toList());
								goods.setAttrs(searchAttrVos);
							}
							return goods;
						}).collect(Collectors.toList());
					this.repository.saveAll(goodsLists);
					}
				}
			}
			pageSize = (long) spuInfoEntities.size();
			pageNum++;
		}while (pageSize==100);
	}
}
