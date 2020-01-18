package com.eatguigu.gmall.auth.config;

import com.atguigu.core.utils.RsaUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@ConfigurationProperties(prefix = "jwt.token")
public class JwtConfig {
	private String pubKeyPath;
	private String priKeyPath;
	private String secret;
	private String cookieName;
	private Integer expireTime;

	//封装成对象
	private PublicKey publicKey;
	private PrivateKey privateKey;

	//生命周期
	@PostConstruct
	public void init(){

		try {
			File pubFile = new File(pubKeyPath);
			File priFile = new File(priKeyPath);
			if(!pubFile.exists() || !priFile.exists()) {
				RsaUtils.generateKey(pubKeyPath, priKeyPath, secret);
			}
			 publicKey = RsaUtils.getPublicKey(pubKeyPath);
			 privateKey = RsaUtils.getPrivateKey(priKeyPath);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
