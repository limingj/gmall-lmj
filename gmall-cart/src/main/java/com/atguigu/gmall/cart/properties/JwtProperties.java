package com.atguigu.gmall.cart.properties;

import com.atguigu.core.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

@ConfigurationProperties(prefix = "jwt.token")
@Data
public class JwtProperties {

    private String pubKeyPath;
    private String cookieName;
    private PublicKey publicKey;

    private String userKey;
    private Integer expireTime;
    //Bean初始化
    @PostConstruct
    public void init(){
        try {
            publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
