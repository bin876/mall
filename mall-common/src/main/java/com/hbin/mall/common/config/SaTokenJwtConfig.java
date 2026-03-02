package com.hbin.mall.common.config;


import cn.dev33.satoken.jwt.SaJwtTemplate;
import cn.dev33.satoken.jwt.SaJwtUtil;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import cn.hutool.jwt.JWT;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class SaTokenJwtConfig {

    @PostConstruct
    public void setSaJwtTemplate() {
        SaJwtUtil.setSaJwtTemplate(new SaJwtTemplate() {
            @Override
            public String generateToken(JWT jwt, String keyt) {
                return super.generateToken(jwt, keyt);
            }
        });
    }

    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

    @Bean("/requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return (RequestTemplate requestTemplate) -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String token = request.getHeader("token");
                String cookie = request.getHeader("Cookie");
                String authorization = request.getHeader("Authorization");
                requestTemplate.header("token", token);
                requestTemplate.header("Cookie", cookie);
                requestTemplate.header("Authorization", authorization);
            }
        };
    }
}
