package com.ctrip.xpipe.redis.console.spring;

import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;

import com.ctrip.xpipe.api.sso.LogoutHandler;
import com.ctrip.xpipe.api.sso.UserInfoHolder;
import com.ctrip.xpipe.redis.console.config.ConsoleConfig;
import com.ctrip.xpipe.redis.console.config.DefaultConsoleConfig;
import com.ctrip.xpipe.redis.console.sso.UserAccessFilter;
import com.ctrip.xpipe.redis.console.util.DefaultMetaServerConsoleServiceManagerWrapper;
import com.ctrip.xpipe.redis.console.util.MetaServerConsoleServiceManagerWrapper;
import com.ctrip.xpipe.redis.core.metaserver.MetaServerConsoleServiceManager;
import com.ctrip.xpipe.redis.core.metaserver.impl.DefaultMetaServerConsoleServiceManager;
import com.ctrip.xpipe.redis.core.spring.AbstractRedisConfigContext;
import com.ctrip.xpipe.spring.AbstractProfile;

/**
 * @author shyin
 *
 *         Jul 28, 2016
 */
@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {"com.ctrip.xpipe.service.sso"})
public class ConsoleContextConfig extends AbstractRedisConfigContext {

	@Bean
	public MetaServerConsoleServiceManager getMetaServerConsoleServiceManager() {
		return new DefaultMetaServerConsoleServiceManager();
	}

	@Bean
	public MetaServerConsoleServiceManagerWrapper getMetaServerConsoleServiceManagerWraper() {
		return new DefaultMetaServerConsoleServiceManagerWrapper();
	}

	@Bean
	public ConsoleConfig consoleConfig(){
		return new DefaultConsoleConfig();
	}
	@Bean
	public UserInfoHolder userInfoHolder(){
		return UserInfoHolder.DEFAULT;
	}

	@Bean
	public LogoutHandler logoutHandler(){
		return LogoutHandler.DEFAULT;
	}

	@Bean
	@Profile(AbstractProfile.PROFILE_NAME_PRODUCTION)
	public FilterRegistrationBean userAccessFilter(ConsoleConfig consoleConfig) {

		FilterRegistrationBean userAccessFilter = new FilterRegistrationBean();

		userAccessFilter.setFilter(new UserAccessFilter(UserInfoHolder.DEFAULT, consoleConfig));
		userAccessFilter.addUrlPatterns("/*");

		return userAccessFilter;
	}
}
