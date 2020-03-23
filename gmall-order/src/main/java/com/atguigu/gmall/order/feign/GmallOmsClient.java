package com.atguigu.gmall.order.feign;

import com.atguigu.oms.api.GmallOmdApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("oms-service")
public interface GmallOmsClient extends GmallOmdApi {
}
