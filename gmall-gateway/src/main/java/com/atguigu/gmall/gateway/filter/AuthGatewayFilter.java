package com.atguigu.gmall.gateway.filter;

import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.gateway.config.JwtConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@EnableConfigurationProperties(JwtConfig.class)
public class AuthGatewayFilter implements GatewayFilter {
	@Autowired
	private JwtConfig jwtConfig;
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		
		//网关自定义过滤
		ServerHttpRequest request = exchange.getRequest();
		ServerHttpResponse response = exchange.getResponse();
		//1.获取jwt类型的token的信息
		MultiValueMap<String, HttpCookie> cookies = request.getCookies();
		//如果cookie为空  或  cookie中不包含token信息
		if(CollectionUtils.isEmpty(cookies) || !cookies.containsKey(jwtConfig.getCookieName())){
			//身份认证状态码，身份未认证  拦截 ==》设置响应状态码401
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			//设置响应结束
			return response.setComplete();
		}
		//获取cookie
		HttpCookie cookie = cookies.getFirst(this.jwtConfig.getCookieName());
		if(cookie==null){
			//身份认证状态码，身份未认证  拦截 ==》设置响应状态码401
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			//设置响应结束
			return response.setComplete();
		}
		String token = cookie.getValue();
		//2.判断是否为空
		if(token==null){
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return response.setComplete();
		}
		//3.解析token
		try {
			JwtUtils.getInfoFromToken(token,this.jwtConfig.getPublicKey());
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return response.setComplete();
		}
		//放行
		return chain.filter(exchange);
	}
}

























