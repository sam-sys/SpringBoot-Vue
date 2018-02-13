package com.zoctan.fast.core.config;

import com.zoctan.fast.core.jwt.JwtAuthenticationEntryPoint;
import com.zoctan.fast.core.jwt.JwtAuthenticationFilter;
import com.zoctan.fast.core.jwt.JwtUtil;
import com.zoctan.fast.service.impl.UserDetailsServiceImpl;
import com.zoctan.fast.util.RSAUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Bean
    @Override
    public UserDetailsServiceImpl userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth)
            throws Exception {
        auth
                // 自定义获取用户信息
                .userDetailsService(this.userDetailsService())
                // 设置密码加密
                .passwordEncoder(this.passwordEncoder());
    }

    @Override
    protected void configure(final HttpSecurity http)
            throws Exception {
        http    // 关闭cors验证
                .cors().disable()
                // 关闭csrf验证
                .csrf().disable()
                // 无状态Session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                // 异常处理
                .exceptionHandling().authenticationEntryPoint(this.jwtAuthenticationEntryPoint()).and()
                // 对所有的请求都做权限校验
                .authorizeRequests()
                // 允许匿名请求
                .antMatchers(
                        "/user/login",
                        "/user/register",
                        "/swagger-ui.html**",
                        "/swagger-resources**",
                        "/webjars/**",
                        "/v2/**"
                ).permitAll()
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated().and();

        http    // 基于定制JWT安全过滤器
                .addFilterBefore(this.jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        // 禁用页面缓存
        http.headers().cacheControl();
    }

    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public RSAUtil rsaUtil() {
        return new RSAUtil();
    }

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}