package com.atguigu.gmall.cart.config;

import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GmallWebMvcConfig implements WebMvcConfigurer {
	@Autowired
	private LoginInterceptor loginInterceptor;
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		//在注册器中添加拦截器
		registry.addInterceptor(loginInterceptor)
		//设置拦截路径
		.addPathPatterns("/**");
	}
}
