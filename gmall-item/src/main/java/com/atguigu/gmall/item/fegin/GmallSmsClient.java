package com.atguigu.gmall.item.fegin;

import com.atguigu.gmall.sms.Vo.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}
