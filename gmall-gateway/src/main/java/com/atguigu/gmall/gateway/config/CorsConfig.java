package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @author shkstart
 * @create 2020-01-03 10:12
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsFilter(){

        //初始化cors配置对象
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //设置允许头信息
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedOrigin("Http://localhost:1000");
        corsConfiguration.addAllowedOrigin("Http://127.0.0.1:1000");
        corsConfiguration.setAllowCredentials(true);



//        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
//        configSource.registerCorsConfiguration("/**",corsConfiguration);

        // 添加映射路径，我们拦截一切请求
        UrlBasedCorsConfigurationSource configSource =new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration("/**",corsConfiguration);
        return new CorsWebFilter(configSource);
    }
}
