package com.atguigu.gmall.order.feign;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.wms.api.GmallWmsApi;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {

}
