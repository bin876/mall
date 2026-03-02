package com.hbin.mall.gateway.component;

import cn.dev33.satoken.stp.StpInterface;
import com.hbin.mall.common.satoken.StpAdminUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String ADMIN_PERMISSIONS_PREFIX = "admin:permissions:";

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        if (!StpAdminUtil.getLoginType().equals(loginType)) {
            return Collections.emptyList();
        }
        
        String key = ADMIN_PERMISSIONS_PREFIX + loginId;
        Set<String> permissions = stringRedisTemplate.opsForSet().members(key);
        return permissions == null ? Collections.emptyList() : new ArrayList<>(permissions);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return Collections.emptyList();
    }
}