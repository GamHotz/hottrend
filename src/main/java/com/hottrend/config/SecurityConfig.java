package com.hottrend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置
 * 配置哪些路径需要认证，哪些路径公开访问
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF（前后端分离项目通常需要禁用）
                .csrf(csrf -> csrf.disable())
                // 配置授权规则
                .authorizeHttpRequests(auth -> auth
                        // 静态资源放行
                        .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**").permitAll()
                        // Knife4j/Swagger 相关路径放行
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
                                "/swagger-resources/**", "/webjars/**", "/doc.html", "/knife4j/**").permitAll()
                        // Spring Boot Actuator 端点放行
                        .requestMatchers("/actuator/**").permitAll()
                        // Spring Boot Admin 端点放行
                        .requestMatchers("/instances/**", "/api/applications/**").permitAll()
                        // 所有 API 接口需要认证
                        .requestMatchers("/api/**").permitAll()
                        // 首页放行
                        .requestMatchers("/").permitAll()
                        // 其他请求都需要认证
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}