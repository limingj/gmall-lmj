package com.atguigu.gmall.search.listence;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.config.GoodsRepository;
import com.atguigu.gmall.search.fegin.PmsClient;
import com.atguigu.gmall.search.fegin.WmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "更新检索库")
@RestController

@RequestMapping("/send")
public class PmsListence {
	@Autowired
	private PmsClient pmsClient;
	@Autowired
	private GoodsRepository repository;
	@Autowired
	private WmsClient wmsClient;

	//消费方绑定队列信息

	@GetMapping("/insert")
	@ApiOperation("insert")
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = "GMALL-SEARCH-QUEUE",durable = "true"),  //持久化
			exchange = @Exchange(value = "GMALL-PMS-EXCHANGE"       //交换机名字
					             ,ignoreDeclarationExceptions = "true"   //忽略声明异常
								 ,type = ExchangeTypes.TOPIC),           //交换机类型
			key = ("item.insert")
	))

	public void listence(Long spuId){
		Resp<List<SkuInfoEntity>> skuResp = this.pmsClient.querySkuInfoBySkuId(spuId);
		List<SkuInfoEntity> skuInfoEntitys = skuResp.getData();
		if (!CollectionUtils.isEmpty(skuInfoEntitys)){
			List<Goods> goodsLists = skuInfoEntitys.stream().map(skuInfoEntity -> {
				Goods goods = new Goods();
				goods.setSkuId(skuInfoEntity.getSkuId());
				goods.setSkuTitle(skuInfoEntity.getSkuTitle());
				goods.setSkuSubTitle(skuInfoEntity.getSkuSubtitle());
				goods.setPrice(skuInfoEntity.getPrice().doubleValue());
				goods.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
				goods.setSale(10l);

				Resp<SpuInfoEntity> spuInfoEntityResp = pmsClient.querySpuinfoById(spuId);
				SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();
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
