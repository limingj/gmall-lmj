package com.atguigu.gmall.sms.Vo.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.sms.Vo.vo.ItemSaleVo;
import com.atguigu.gmall.sms.Vo.vo.SaleVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author shkstart
 * @create 2020-01-05 14:33
 */
public interface GmallSmsApi {
    @PostMapping("sms/skubounds/sales")
    public Resp<Object> saveSalles(@RequestBody SaleVo saleVo);

    @GetMapping("sms/skubounds/{skuId}")
    public Resp<List<ItemSaleVo>> queryItemSaleBySkuId(@PathVariable("skuId")Long skuId);
}
