package com.atguigu.gmall.cart.interceptor;

import com.alibaba.nacos.client.utils.StringUtils;
import com.atguigu.core.utils.CookieUtils;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.gmall.cart.properties.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

/**
 * 拦截器
 */

//目的获取userId以及userKey
@EnableConfigurationProperties(JwtProperties.class)
@Component
public class LoginInterceptor implements HandlerInterceptor {
	@Autowired
	private JwtProperties jwtProperties;
	private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		UserInfo userInfo = new UserInfo();

		String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
		//未登陆状态
		String userKey = CookieUtils.getCookieValue(request, jwtProperties.getUserKey());
		//判断userKey是否为空
		if(StringUtils.isEmpty(userKey)){
			//为空时，制作一个userKey放入cookie中
			userKey = UUID.randomUUID().toString();
			CookieUtils.setCookie(request,response,this.jwtProperties.getUserKey(),userKey,this.jwtProperties.getExpireTime());
		}
		userInfo.setUserKey(userKey);

		if(StringUtils.isEmpty(token)){
			//为空  未登陆  传递userKey到后台    把userInfo传递给后续业务
			THREAD_LOCAL.set(userInfo);
			return true;
		}
		//不为空  解析token
		try {
			Map<String, Object> infoToken = JwtUtils.getInfoFromToken(token, this.jwtProperties.getPublicKey());
			Long id = Long.valueOf(infoToken.get("id").toString());
			userInfo.setUserId(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//把userInfo传递给后续业务
/*		request.setAttribute("userKey",userKey);
		request.setAttribute("userId",userInfo.getUserId());*/

		THREAD_LOCAL.set(userInfo);
		return true;
	}

	public static UserInfo getUserInfo(){
		return THREAD_LOCAL.get();
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		//防止内存泄露   线程池： 请求介绍不表示线程结束
		THREAD_LOCAL.remove();
	}
}
