package com.atguigu.gamll.pay.fegin;

import com.atguigu.order.api.GmallOrderApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("order.service")
public interface GmallOrderClient extends GmallOrderApi {
}
