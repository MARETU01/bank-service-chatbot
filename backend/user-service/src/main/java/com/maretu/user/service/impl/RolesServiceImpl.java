package com.maretu.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maretu.common.utils.RedisConstants;
import com.maretu.user.pojo.Roles;
import com.maretu.user.mapper.RolesMapper;
import com.maretu.user.service.IRolesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RolesServiceImpl extends ServiceImpl<RolesMapper, Roles> implements IRolesService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 启动时预热角色缓存
     */
    @PostConstruct
    public void initRolesCache() {
        try {
            List<Roles> roles = list();
            if (roles == null || roles.isEmpty()) {
                return;
            }
            Map<String, String> roleMap = roles.stream()
                    .collect(Collectors.toMap(
                            Roles::getRoleCode,
                            this::toJson
                    ));
            stringRedisTemplate.opsForHash().putAll(RedisConstants.ROLES_KEY, roleMap);
            log.info("角色缓存预热完成，共加载 {} 个角色", roles.size());
        } catch (Exception e) {
            log.warn("角色缓存预热失败，将在首次查询时加载", e);
        }
    }

    @Override
    public List<Roles> getAllRoles() {
        // 1. 从缓存读取
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(RedisConstants.ROLES_KEY);
        if (!entries.isEmpty()) {
            return entries.values().stream()
                    .map(v -> fromJson((String) v))
                    .collect(Collectors.toList());
        }

        // 2. 缓存未命中，查询数据库
        List<Roles> roles = list();
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 写入缓存（不设置TTL）
        Map<String, String> roleMap = roles.stream()
                .collect(Collectors.toMap(
                        Roles::getRoleCode,
                        this::toJson
                ));
        stringRedisTemplate.opsForHash().putAll(RedisConstants.ROLES_KEY, roleMap);

        return roles;
    }

    private String toJson(Roles role) {
        try {
            return objectMapper.writeValueAsString(role);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize role", e);
        }
    }

    private Roles fromJson(String json) {
        try {
            return objectMapper.readValue(json, Roles.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize role", e);
        }
    }
}
