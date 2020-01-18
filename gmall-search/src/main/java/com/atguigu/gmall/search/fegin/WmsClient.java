package com.atguigu.gmall.search.fegin;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.wms.api.GmallWmsApi;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("wms-service")
public interface WmsClient extends GmallWmsApi {
	@GetMapping("wms/waresku/{skuId}")
	public Resp<List<WareSkuEntity>> queryWareSkuBySkuId(@PathVariable("skuId")Long skuId);
}
