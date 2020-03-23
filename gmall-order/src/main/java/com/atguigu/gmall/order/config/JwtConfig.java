package com.atguigu.gmall.order.config;

import com.atguigu.core.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

@ConfigurationProperties(prefix = "jwt.token")
@Data
public class JwtConfig {
	private String pubKeyPath;
	private String cookieName;
	//封装成对象
	private PublicKey publicKey;
	private String userKey;
	private Integer expireTime;

	//生命周期
	@PostConstruct
	public void init(){

		try {
			publicKey = RsaUtils.getPublicKey(pubKeyPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
