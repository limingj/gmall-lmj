package com.atguigu.gamll.pay.template;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gamll.pay.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2016101600698775";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAQC1NKSakbTF6tXSWSl4rLYoZfH4PTDHeOA4ULXrMDapLrM9QzRPgk6DMXnAbCEiKFtAcHLavf71qgSaeIBMrJRgLHPewwiDTA9HxnQ9/M8SR6DiLb/vsvPfs6HuYp1ps78rVWT9mO764l7MQkAsKq53abcKA1y/fYYpDcz+244bZvbzg5aHSjUlBOlqvTFqDpHDKOTLFd0mu7gjwVehJcvob5XRg68e+7BiLGAoCQWY8V4SkjXBWgn72l7psS6L9lAgWG4C4esNI0S7H0ZCTrYjrl5O7H2XgpAnZxF8TEq2FsQGTVIoZ7dRhSdFI4HqPzaP2pp1cbYWlPLeDs8HrJexAgMBAAECggEBAI/sWRTfx9ovFFe3M8ZCP4hEuHR3fYfhjQ2NpYBA0hl8Gyb8es7v65YuNMrN118++INtDPyKTbgnqvJZ5WWA6XSdugZjmB5YygYzLN6TLpERTUp4Feu3khXKokIqF2b+Okeb0tPNpXqlU7AeqsOpvtrfn18XnpIFjAqNiJWgIvXFciZ7PC52a5donVEMTOs8CDk5kxcgzw4RqM0WESihSYmdV5a4INIokkx/GuH1L0GgPPuQrvKURkkso6IHl1O8TBu7v1ZUBLW/ejg7S86Ky6RjXPTbtvWi6kHMbwz49Yv6n3ujFhe1FU7XZEziazYR0Y3UYnUPeo11lqxILlyKDzECgYEA6A9RVWpYORv2FvUQ1Pzxty6ShcRVJU0TnfFQhNDsxpam+gpP5+qJXwckcDW13O1yOPUZSwsDka0eMkjuxP++SMT8jeNhD40GfHYhqjDovRhoQbHulqfIWHFuMjR4/g+kE2iqWj9OP8mEh12zo2FoYytQxIumZgRxWAgTK1aP4LUCgYEAx+ZFE9L4PXJMbViOKMx1n2Up2kE+bEjmVg5ZAk8A2cpIHj/I5WfBOdPFvQAC8HPTn974ZT1Xq63wdtGPbg0S8UABXPK1NwbaChbhmyJjJN93+W2m8lq9fRimVAEAE2n/zntdXolsLwtV/d9E+7iIzrPfPIMAhfGSKsxPUnEfBI0CgYEApZ5RCOn1/wRCtQ3ALpI8HOWDMy71KcMBriaMtc26Pt1EXI3Bi54q3oRlcCQVIJDihV3/6Dv7Fxv7wh2lMznm316fdNCD9CpNJ8TTr/hqEpL5zXEk+6bRaLXD1Nb4RzJ4glWfxvsJaKL1D/tC+ubJLoW6hhu47XRcRFy2+aY010UCgYEAjfFsy47axocKzfo7t6y3ON/UwC3j7XA2XpUBjyoaKIwCebhBJuRtyGof1vzTGXqlorfpGbpdLoLsZPKZLkQKV1Rjo2SUWOZ8nP1yZX4deGlV/79k9GKKevGt6ahsLVzvT+c5fY+HSkB6ZzLLARMf0ebXdVhwXqwSZmF9UcURiWECgYEA3WJUKz0BrS4WSbc3iMkCblI00LXg66nbk6mUt2FL/4hNPaaRGCc2mkkbb6XGjubfqJSifnENbnllzF59Jr4M9FjptXBtGLqffQSqsBZzm+lwUIJIJH6YdNbnpH4xT69+l2uVwvTbZTBtrEynTRMcHQk2FfrqiHFiruKyYxSF59Y=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoN3R25iz6lKCgtoEjn6qosgHmriZJns7DCXoMg/tQPDvAFA3Eicv6OWaTTO6BsQ8Rm8MH0IoMDqLpOy15DGR2y3spdBCyPDLGXp/pRhceRtRasvZceOVDViCYalQmZEozErtsm4ZrjKSNWW310+Jpsj+9xlcp5EAf5B5bYB/8bXrQ1BiBl7jLPs8/5ycTCBOe7paYYWqzSDkIBoQvdZ2lgQc4VT7it+FztCfQ1cAnoBsSrgNv+WO+0hFcZoJAbXGxRL118A+oAtDHUvytTDuMlUpK19eSPZ4MnAGKaGE12juWmtgvKt+L9ofs/8V80mh0mMbrHdy2B488exAQujK+QIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url;

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url;

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
