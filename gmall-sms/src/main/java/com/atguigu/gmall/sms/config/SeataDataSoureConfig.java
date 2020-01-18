package com.atguigu.gmall.sms.config;

import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * seata管理数据源
 */
@Configuration
public class SeataDataSoureConfig {
	/**
	 * 需要将 DataSourceProxy 设置为主数据源，否则事务无法回滚
	 *
	 * @param
	 * @return The default datasource
	 */
	@Primary
	@Bean("dataSource")
	public DataSourceProxy dataSource(@Value("${spring.datasource.url}")String url,
									   @Value("${spring.datasource.username}")String username,
									   @Value("${spring.datasource.password}")String password,
									   @Value("${spring.datasource.driver-class-name}")String classname) {
		HikariDataSource hikariDataSource = new HikariDataSource();
		hikariDataSource.setJdbcUrl(url);
		hikariDataSource.setUsername(username);
		hikariDataSource.setDriverClassName(classname);
		hikariDataSource.setPassword(password);
		return new DataSourceProxy(hikariDataSource);
	}

}
