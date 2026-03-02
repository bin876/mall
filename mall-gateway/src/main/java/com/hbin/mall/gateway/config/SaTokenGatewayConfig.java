package com.hbin.mall.gateway.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import com.hbin.mall.common.satoken.StpAdminUtil;
import com.hbin.mall.common.satoken.StpMemberUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.AntPathMatcher;

import java.util.Map;

@Configuration
public class SaTokenGatewayConfig {

    private static final String RESOURCE_RULES_KEY = "resource:rules";
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Bean
    public SaReactorFilter getSaReactorFilter(IgnoreUrlsConfig ignoreUrlsConfig) {
        return new SaReactorFilter()
                .addInclude("/**")
                .setExcludeList(ignoreUrlsConfig.getUrls())
                .setAuth(auth -> {
                    SaRouter.match("/api/**", () -> {
                        StpLogic stp = StpMemberUtil.getStpLogic();
                        stp.checkLogin();
                    }).stop();

                    SaRouter.match("/admin/**", () -> {
                        StpLogic stp = StpAdminUtil.getStpLogic();
                        stp.checkLogin();

                        String path = SaHolder.getRequest().getRequestPath();
                        String method = SaHolder.getRequest().getMethod();
                        String needPermission = resolvePermission(path, method);
                        if (needPermission != null) {
                            StpAdminUtil.checkPermissionOr(needPermission);
                        }
                    }).stop();
                });
    }

    private String resolvePermission(String path, String method) {
        Map<Object, Object> ruleMap = stringRedisTemplate.opsForHash().entries(RESOURCE_RULES_KEY);
        if (ruleMap == null || ruleMap.isEmpty()) {
            return null;
        }

        for (Map.Entry<Object, Object> entry : ruleMap.entrySet()) {
            String ruleKey = (String) entry.getKey();
            String permission = (String) entry.getValue();

            String[] parts = ruleKey.split("#", 2);
            if (parts.length != 2) continue;

            String pattern = parts[0];
            String ruleMethod = parts[1];

            if (method.equalsIgnoreCase(ruleMethod) && pathMatcher.match(pattern, path)) {
                return permission;
            }
        }
        return null;
    }
}
