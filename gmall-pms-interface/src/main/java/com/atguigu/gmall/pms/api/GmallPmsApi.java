package com.atguigu.gmall.pms.api;


import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.CategoryVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {
	@PostMapping("pms/spuinfo/page")
	public Resp<List<SpuInfoEntity>> searchPage(@RequestBody QueryCondition queryCondition);

	@GetMapping("pms/spuinfo/info/{id}")
	public Resp<SpuInfoEntity> querySpuinfoById(@PathVariable("id") Long id);

	@GetMapping("pms/skuinfo/{spuId}")
	public Resp<List<SkuInfoEntity>> querySkuInfoBySkuId(@PathVariable("spuId")Long spuId);
	@GetMapping("pms/brand/info/{brandId}")
	public Resp<BrandEntity> queryBrandById(@PathVariable("brandId") Long brandId);
	@GetMapping("pms/category/info/{catId}")
	public Resp<CategoryEntity> queryCategoryById(@PathVariable("catId") Long catId);

	@GetMapping("pms/category/{pid}")
	public Resp<List<CategoryVo>> querysubCates(@PathVariable("pid") Long pid);

	@GetMapping("pms/category")
	public Resp<List<CategoryEntity>> queryAllCategoryTree(
			@RequestParam(value = "level",defaultValue = "0") Integer level,
			@RequestParam(value = "parentCid",required = false) Long pid);
	@GetMapping("pms/productattrvalue/{spuId}")
	public Resp<List<ProductAttrValueEntity>> searchAttrValues(@PathVariable ("spuId") Long spuId);

	@GetMapping("pms/skuinfo/info/{skuId}")
	public Resp<SkuInfoEntity> quserySkuInfoByShuId(@PathVariable("skuId") Long skuId);

	@GetMapping("pms/spuinfo/info/{id}")
	public Resp<SpuInfoEntity> spuInfoBySpuId(@PathVariable("id") Long id);

	@GetMapping("pms/spuinfodesc/info/{spuId}")
	public Resp<SpuInfoDescEntity> spuInfoDescBySpuId(@PathVariable("spuId") Long spuId);

	@GetMapping("pms/skuimages/{skuId}")
	public Resp<List<SkuImagesEntity>> queryImagesBySkuId(@PathVariable("skuId")Long skuId);

	@GetMapping("pms/attrgroup/withattrvalues")
	public Resp<List<ItemGroupVo>> queryItemGroupVosByCIdAndSpuId(
			@RequestParam("cid") Long cid,
			@RequestParam("spuId") Long spuId
	);
	@GetMapping("pms/skusaleattrvalue/{spuId}")
	public Resp<List<SkuSaleAttrValueEntity>> querySaleValueBySpuId(@PathVariable("spuId")Long spuId);
}
