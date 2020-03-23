package com.atguigu.gmall.wms.api;


import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


public interface GmallWmsApi {
	@GetMapping("wms/waresku/{skuId}")
	public Resp<List<WareSkuEntity>> queryWareSkuBySkuId(@PathVariable("skuId")Long skuId);
	@PostMapping("wms/waresku")   //传Json数据，用post请求传递数据
	public Resp<List<SkuLockVo>> checkAndLockStock(@RequestBody List<SkuLockVo> skuLockVos);
}
