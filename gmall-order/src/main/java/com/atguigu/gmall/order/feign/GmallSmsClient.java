package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.sms.Vo.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}
