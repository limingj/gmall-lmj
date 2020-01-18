package com.atguigu.gmall.pms.fegin;

import com.atguigu.gmall.sms.Vo.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author shkstart
 * @create 2020-01-05 14:35
 */
@FeignClient("sms-service")
public interface SmsFegin extends GmallSmsApi {
}
