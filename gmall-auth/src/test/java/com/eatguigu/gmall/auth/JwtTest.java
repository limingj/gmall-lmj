package com.eatguigu.gmall.auth;

import com.atguigu.core.utils.JwtUtils;
import com.atguigu.core.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {
	private static final String pubKeyPath = "E:\\jwt\\rsa.pub";

	private static final String priKeyPath = "E:\\jwt\\rsa.pri";

	private PublicKey publicKey;

	private PrivateKey privateKey;


	@Test
	public void testRsa() throws Exception {
		RsaUtils.generateKey(pubKeyPath, priKeyPath, "hfghfghsyh7@4");
	}

	@Before
	public void testGetRsa() throws Exception {
		this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
		this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
	}

	@Test
	public void testGenerateToken() throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("id", "11");
		map.put("username", "liuyan");
		// 生成token
		String token = JwtUtils.generateToken(map, privateKey, 5);
		System.out.println("token = " + token);
	}

	@Test
	public void testParseToken() throws Exception {
		String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE1NzkwOTQxODR9.bpDf_cv0LZdnIEITjIhtg1LYcQqL5AiDOVBpn-xuqSZNzFU-HtfgPWdueKuTzxDeL2QzRifPUQKdkLGkj_mlT_2Kp7XGIZzKldEeDs9ASpKoCWv3v40fwl-iBpvO3B2HHxQlZL1vTpHNFdKU6NKZmsoJtBcZK3ihFmP5ghRRvKcJFvJ5iMup7tsEPEWlMyThETEbwYnjdMhIBpqoirmrSTRvdOJjJrXO93EDeRyOSfXzOYILsZTvlYuwF3FHWFJqiEWc92jt3o5zvZiJAB-BctPK5ec0vXvVwNWrggq_N3YMpttfGfufZsScuTHbPPZvlJjZIC3QT5MD4lAIVqD18A";

		// 解析token
		Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
		System.out.println("id: " + map.get("id"));
		System.out.println("userName: " + map.get("username"));
	}
}
