package com.atguig.gmall.index.fegin;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("pms-service")
public interface PmsClient extends GmallPmsApi {
}
