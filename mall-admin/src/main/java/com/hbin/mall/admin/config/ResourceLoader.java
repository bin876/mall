package com.hbin.mall.admin.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hbin.mall.admin.domain.SysResource;
import com.hbin.mall.admin.mapper.SysResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ResourceLoader implements CommandLineRunner {

    @Autowired
    private SysResourceMapper resourceMapper;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String RESOURCE_RULES_KEY = "resource:rules";

    @Override
    public void run(String... args) {
        loadResourcesToRedis();
    }

    public void loadResourcesToRedis() {
        stringRedisTemplate.delete(RESOURCE_RULES_KEY);

        List<SysResource> resources = resourceMapper.selectList(
            new LambdaQueryWrapper<SysResource>().eq(SysResource::getStatus, 1)
        );
        
        Map<String, String> ruleMap = new HashMap<>();
        for (SysResource resource : resources) {
            String ruleKey = resource.getPath() + "#" + resource.getMethod().toUpperCase();
            ruleMap.put(ruleKey, resource.getPermissionCode());
        }
        
        if (!ruleMap.isEmpty()) {
            stringRedisTemplate.opsForHash().putAll(RESOURCE_RULES_KEY, ruleMap);
        }
    }
}