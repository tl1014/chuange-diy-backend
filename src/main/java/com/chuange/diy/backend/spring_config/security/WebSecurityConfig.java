package com.chuange.diy.backend.spring_config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public WebSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String[] adminRoles = {"ADMIN"};
        String[] userOrAdmin = {"USER", "ADMIN"};

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 登录、注册、公开商品/分类查询、上传静态资源放行
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/public/**", "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories").permitAll()
                        // 后台管理接口只允许 ADMIN
                        .requestMatchers("/api/admin/**", "/api/categories/admin").hasAnyAuthority(adminRoles)
                        .requestMatchers(HttpMethod.POST, "/api/products/**", "/api/categories/**", "/api/media/**").hasAnyAuthority(adminRoles)
                        .requestMatchers(HttpMethod.PUT, "/api/products/**", "/api/categories/**").hasAnyAuthority(adminRoles)
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**", "/api/categories/**").hasAnyAuthority(adminRoles)
                        // 设计稿、订单、当前用户等接口需登录（USER 或 ADMIN）
                        .requestMatchers("/api/auth/me", "/api/designs/**", "/api/orders/**").hasAnyAuthority(userOrAdmin)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}