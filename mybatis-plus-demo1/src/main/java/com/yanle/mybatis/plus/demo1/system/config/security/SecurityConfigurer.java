package com.yanle.mybatis.plus.demo1.system.config.security;

import com.yanle.mybatis.plus.demo1.common.base.Constants;
import com.yanle.mybatis.plus.demo1.system.config.filter.ValidateCodeFilter;
import com.yanle.mybatis.plus.demo1.system.config.security.handler.AuthenticationFailureHandler;
import com.yanle.mybatis.plus.demo1.system.config.security.handler.AuthenticationSuccessHandler;
import com.yanle.mybatis.plus.demo1.system.config.security.handler.CustomLogoutSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SecurityConfigurer extends WebSecurityConfigurerAdapter {

    /**
     * 最大登录数
     */
    @Value("${security.max-session}")
    private Integer maxSession;

    /**
     * 超出最大登录数，是否阻止登录
     */
    @Value("${security.prevents-login}")
    private Boolean preventsLogin;

    private final UserDetailServiceImpl userDetailService;

    private final CustomAuthenticationProvider customAuthenticationProvider;

    // todo
    private final ValidateCodeFilter validateCodeFilter;

    private final CustomInvalidSessionStrategy customInvalidSessionStrategy;

    private final CustomExpiredSessionStrategy customExpiredSessionStrategy;

    private final AuthenticationSuccessHandler authenticationSuccessHandler;

    private final AuthenticationFailureHandler authenticationFailureHandler;

    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    private CustomLogoutSuccessHandler customLogoutSuccessHandler;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers()
                .frameOptions()
                .disable();

        http.addFilterBefore(validateCodeFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/static/**", "/favicon.ico", "/actuator/**", "/code", "/invalid_session", "/expired", "/logout", "/403").permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                .accessDeniedPage("/403")
                .and()
                // form 登录
                .formLogin()
                .loginProcessingUrl(Constants.LOGIN_URL)
                .loginPage(Constants.LOGIN_URL)
                .permitAll()
                .and()
                .csrf().disable()
                .cors()
                .and()
                // 登出
                .logout()
                .logoutUrl(Constants.LOGOUT_URL)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler(customLogoutSuccessHandler)
                .and()
                // session
                .sessionManagement()
                // 失效处理
                //.invalidSessionUrl("/invalid_session")
                //失效处理
                .invalidSessionStrategy(customInvalidSessionStrategy)
                //同一账号同时允许多个设备在线
                .maximumSessions(maxSession)
                //新用户挤走前用户
                .maxSessionsPreventsLogin(preventsLogin)
                //.expiredUrl("/expired")
                //超时处理
                .expiredSessionStrategy(customExpiredSessionStrategy)
                .sessionRegistry(sessionRegistry);
    }

    /**
     * 验证用户信息
     * @param auth auth
     * @throws Exception exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(customAuthenticationProvider);
    }

    @Bean
    public SessionRegistry getSessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");
        config.setAllowCredentials(true);
        config.addAllowedMethod("*");
        config.setAllowedMethods(Arrays.asList("GET","POST"));
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean bean = new FilterRegistrationBean(new org.springframework.web.filter.CorsFilter(source));
        bean.setOrder(0);
        return new CorsFilter(source);
    }
}
