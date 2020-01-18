package com.atguigu.gmall.item.service.impl;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.item.fegin.GmallPmsClient;
import com.atguigu.gmall.item.fegin.GmallSmsClient;
import com.atguigu.gmall.item.fegin.GmallWmsClient;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.sms.Vo.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemServiceImpl implements ItemService {
	@Autowired
	private GmallPmsClient gmallPmsClient;
	@Autowired
	private GmallWmsClient gmallWmsClient;
	@Autowired
	private GmallSmsClient gmallSmsClient;
	@Autowired
	private ThreadPoolExecutor threadPoolExecutor;

	@Override
	public ItemVo queryItemVoBySkuId(Long skuId) {
		ItemVo itemVo = new ItemVo();
		itemVo.setSkuId(skuId);

		CompletableFuture<SkuInfoEntity> skuInfoFuture = CompletableFuture.supplyAsync(() -> {
			//根据skuId ==》查询sku
			Resp<SkuInfoEntity> skuyResp = gmallPmsClient.quserySkuInfoByShuId(skuId);
			SkuInfoEntity skuInfoEntity = skuyResp.getData();
			if (skuInfoEntity == null) {
				return null;
			}
			itemVo.setSkuTitle(skuInfoEntity.getSkuTitle());
			itemVo.setSkuSubTitle(skuInfoEntity.getSkuSubtitle());
			itemVo.setWeight(skuInfoEntity.getWeight());
			itemVo.setPrice(skuInfoEntity.getPrice());
			return skuInfoEntity;
		},threadPoolExecutor);

		CompletableFuture<Void> brandFuture = skuInfoFuture.thenAcceptAsync(skuInfoEntity -> {
			//根据skuId中的brandId  =>查询品牌信息
			Resp<BrandEntity> brandResp = gmallPmsClient.queryBrandById(skuInfoEntity.getBrandId());
			BrandEntity brandEntity = brandResp.getData();
			if (brandEntity != null) {
				itemVo.setBrandId(brandEntity.getBrandId());
				itemVo.setBrandName(brandEntity.getName());
			}
		},threadPoolExecutor);

		CompletableFuture<Void> cateFuture = skuInfoFuture.thenAcceptAsync(skuInfoEntity -> {
			//根据skuId中的categoryId  =>查询 分类信息
			Resp<CategoryEntity> cateResp = gmallPmsClient.queryCategoryById(skuInfoEntity.getCatalogId());
			CategoryEntity categoryEntity = cateResp.getData();
			if (categoryEntity != null) {
				itemVo.setCategoryId(categoryEntity.getCatId());
				itemVo.setCategoryName(categoryEntity.getName());
			}
		},threadPoolExecutor);


		CompletableFuture<Void> spuFuture = skuInfoFuture.thenAcceptAsync(skuInfoEntity -> {
			//根据skuId中的spuId  =>查询 spu信息
			Resp<SpuInfoEntity> spuResp = gmallPmsClient.spuInfoBySpuId(skuInfoEntity.getSpuId());
			SpuInfoEntity spuInfoEntity = spuResp.getData();
			if (spuInfoEntity != null) {
				itemVo.setSpuId(spuInfoEntity.getId());
				itemVo.setSpuName(spuInfoEntity.getSpuName());
			}
		},threadPoolExecutor);

		CompletableFuture<Void> spuDescFuture = skuInfoFuture.thenAcceptAsync(skuInfoEntity -> {
			//根据skuId中的spuId  ==>查询商品描述
			Resp<SpuInfoDescEntity> spuInfoDescResp = gmallPmsClient.spuInfoDescBySpuId(skuInfoEntity.getSpuId());
			SpuInfoDescEntity spuInfoDescEntity = spuInfoDescResp.getData();
			if (spuInfoDescEntity != null && spuInfoDescEntity.getDecript() != null) {
				String[] desc = StringUtils.split(spuInfoDescEntity.getDecript(), ",");
				itemVo.setDesc(Arrays.asList(desc));
			}
		},threadPoolExecutor);

		CompletableFuture<Void> groupVoFuture = skuInfoFuture.thenAcceptAsync(skuInfoEntity -> {
			//查询规格参数
			//根据categoryId ==> 查询商品分类所在分组
			//根据分组Id ==>查询中间表 ==》获得属性id
			//根据属性id  ==》 查询属性值
			Resp<List<ItemGroupVo>> groupVoResp = gmallPmsClient.queryItemGroupVosByCIdAndSpuId(skuInfoEntity.getCatalogId(), skuInfoEntity.getSpuId());
			List<ItemGroupVo> itemGroupVos = groupVoResp.getData();
			itemVo.setGroupVos(itemGroupVos);
		},threadPoolExecutor);


		CompletableFuture<Void> saleAttrFuture = skuInfoFuture.thenAcceptAsync(skuInfoEntity -> {
			//查询销售属性值
			//根据sku中的spuId  ==>查询skus
			//根据skus         ==》获得skuIds
			//根据skuIds      ==》查询attr_values
			Resp<List<SkuSaleAttrValueEntity>> saleValueResp = gmallPmsClient.querySaleValueBySpuId(skuInfoEntity.getSpuId());
			List<SkuSaleAttrValueEntity> saleAttrValueEntities = saleValueResp.getData();
			itemVo.setSaleAttrValue(saleAttrValueEntities);
		},threadPoolExecutor);


		CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
			//根据skuId ==>  查询sku图片
			Resp<List<SkuImagesEntity>> imageResp = gmallPmsClient.queryImagesBySkuId(skuId);
			List<SkuImagesEntity> imagesEntities = imageResp.getData();
			itemVo.setImages(imagesEntities);
		},threadPoolExecutor);


		CompletableFuture<Void> storeFuture = CompletableFuture.runAsync(() -> {
			//根据skuId ==>查询库存
			Resp<List<WareSkuEntity>> wareResp = gmallWmsClient.queryWareSkuBySkuId(skuId);
			List<WareSkuEntity> wareSkuEntities = wareResp.getData();
			if (!CollectionUtils.isEmpty(wareSkuEntities)) {
				itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> (wareSkuEntity.getStock() > 0)));
			}
		},threadPoolExecutor);

		CompletableFuture<Void> itemFuture = CompletableFuture.runAsync(() -> {
			//根据skuId查询营销信息  积分  满减  打折
			Resp<List<ItemSaleVo>> itemSaleVoResp = gmallSmsClient.queryItemSaleBySkuId(skuId);
			List<ItemSaleVo> itemSaleVos = itemSaleVoResp.getData();
			itemVo.setSales(itemSaleVos);
		},threadPoolExecutor);

		CompletableFuture.allOf(cateFuture,brandFuture,imageFuture
								,spuDescFuture,spuFuture,saleAttrFuture
				                ,groupVoFuture,storeFuture,itemFuture).join();
		return itemVo;
	}
}
