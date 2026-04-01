package com.maretu.chat.service.impl;

import com.maretu.chat.pojo.Session;
import com.maretu.chat.mapper.SessionMapper;
import com.maretu.chat.service.ISessionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maretu.common.utils.RedisConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 聊天会话表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-03-14
 */
@Service
@RequiredArgsConstructor
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements ISessionService {

    private final StringRedisTemplate stringRedisTemplate;

    @Lazy
    @Autowired
    private SessionServiceImpl self;

    private String getOwnerCacheKey(Integer userId) {
        return RedisConstants.SESSION_OWNER_KEY + userId;
    }

    @Override
    public List<Session> getSessions(Integer userId) {
        return lambdaQuery()
                .eq(Session::getUserId, userId)
                .orderByDesc(Session::getUpdatedAt)
                .list();
    }

    @Override
    public Boolean isSessionOwner(Integer userId, String sessionId) {
        String key = getOwnerCacheKey(userId);

        // 1. 先从 Redis List 缓存中查找
        List<String> cachedSessionIds = stringRedisTemplate.opsForList().range(key, 0, -1);
        if (cachedSessionIds != null && !cachedSessionIds.isEmpty()) {
            // 缓存命中，异步续期
            self.refreshCacheExpire(key);
            return cachedSessionIds.contains(sessionId);
        }

        // 2. 缓存未命中，查数据库获取该用户所有 sessionId
        List<Session> sessions = lambdaQuery()
                .eq(Session::getUserId, userId)
                .select(Session::getSessionId)
                .list();

        if (sessions == null || sessions.isEmpty()) {
            return false;
        }

        // 3. 回填到 Redis List 中（同步回填，确保后续判断正确）
        List<String> sessionIds = sessions.stream()
                .map(Session::getSessionId)
                .toList();
        stringRedisTemplate.opsForList().rightPushAll(key, sessionIds);
        stringRedisTemplate.expire(key, RedisConstants.SESSION_OWNER_TTL, TimeUnit.MINUTES);

        return sessionIds.contains(sessionId);
    }

    @Override
    public Session createSession(Integer userId) {
        Session session = new Session()
                .setSessionId(UUID.randomUUID().toString())
                .setUserId(Long.valueOf(userId));
        save(session);

        // 异步更新 Redis 缓存：将新 sessionId 追加到 List 中
        self.addSessionToCache(userId, session.getSessionId());

        return session;
    }

    @Override
    public Boolean deleteSession(Integer userId, String sessionId) {
        Session session = lambdaQuery()
                .eq(Session::getSessionId, sessionId)
                .eq(Session::getUserId, userId)
                .one();
        if (session == null || !session.getUserId().equals(Long.valueOf(userId))) {
            throw new RuntimeException("会话不存在或无权限删除");
        }
        boolean removed = removeById(session.getId());

        // 异步更新 Redis 缓存：从 List 中移除该 sessionId
        if (removed) {
            self.removeSessionFromCache(userId, sessionId);
        }

        return removed;
    }

    @Override
    public Boolean renameSession(Integer userId, String sessionId, Session session) {
        if (!isSessionOwner(userId, sessionId)) {
            throw new RuntimeException("无权修改该会话");
        }
        return lambdaUpdate()
                .eq(Session::getSessionId, sessionId)
                .set(Session::getTitle, session.getTitle())
                .update();
    }

    // ===== 异步缓存操作方法 =====

    @Async("virtualThreadPoolExecutor")
    public void refreshCacheExpire(String key) {
        stringRedisTemplate.expire(key, RedisConstants.SESSION_OWNER_TTL, TimeUnit.MINUTES);
    }

    @Async("virtualThreadPoolExecutor")
    public void addSessionToCache(Integer userId, String sessionId) {
        String key = getOwnerCacheKey(userId);
        Boolean hasKey = stringRedisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(hasKey)) {
            stringRedisTemplate.opsForList().rightPush(key, sessionId);
            stringRedisTemplate.expire(key, RedisConstants.SESSION_OWNER_TTL, TimeUnit.MINUTES);
        }
    }

    @Async("virtualThreadPoolExecutor")
    public void removeSessionFromCache(Integer userId, String sessionId) {
        String key = getOwnerCacheKey(userId);
        stringRedisTemplate.opsForList().remove(key, 0, sessionId);
    }
}
