package com.atguigu.gmall.ums.fegin;

import com.atguigu.gmall.ums.api.UmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ums-service")
public interface UmsClient extends UmsApi {
}
