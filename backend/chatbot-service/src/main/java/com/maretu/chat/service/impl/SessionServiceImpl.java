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
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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

    /**
     * Lua脚本：原子性地读取List并自动续期
     * 如果key存在且List非空，则刷新TTL并返回数据；否则返回空列表
     */
    @SuppressWarnings("unchecked")
    private static final DefaultRedisScript<List<String>> GET_AND_RENEW_SCRIPT;
    static {
        GET_AND_RENEW_SCRIPT = new DefaultRedisScript<>();
        GET_AND_RENEW_SCRIPT.setScriptText(
                "local data = redis.call('LRANGE', KEYS[1], 0, -1) " +
                "if #data > 0 then " +
                "  redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
                "end " +
                "return data"
        );
        GET_AND_RENEW_SCRIPT.setResultType((Class) List.class);
    }

    /**
     * Lua脚本：原子性地写入List并设置TTL（先清除旧数据再写入）
     * 保证RPUSH和EXPIRE在同一原子操作中完成
     */
    private static final DefaultRedisScript<Long> PUSH_LIST_WITH_TTL_SCRIPT;
    static {
        PUSH_LIST_WITH_TTL_SCRIPT = new DefaultRedisScript<>();
        PUSH_LIST_WITH_TTL_SCRIPT.setScriptText(
                "redis.call('DEL', KEYS[1]) " +
                "for i = 2, #ARGV do " +
                "  redis.call('RPUSH', KEYS[1], ARGV[i]) " +
                "end " +
                "redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
                "return 1"
        );
        PUSH_LIST_WITH_TTL_SCRIPT.setResultType(Long.class);
    }

    /**
     * Lua脚本：原子性地追加元素到List并续期（仅当key存在时）
     * 消除hasKey + rightPush + expire之间的TOCTOU竞态
     */
    private static final DefaultRedisScript<Long> ADD_TO_LIST_IF_EXISTS_SCRIPT;
    static {
        ADD_TO_LIST_IF_EXISTS_SCRIPT = new DefaultRedisScript<>();
        ADD_TO_LIST_IF_EXISTS_SCRIPT.setScriptText(
                "if redis.call('EXISTS', KEYS[1]) == 1 then " +
                "  redis.call('RPUSH', KEYS[1], ARGV[1]) " +
                "  redis.call('EXPIRE', KEYS[1], ARGV[2]) " +
                "  return 1 " +
                "end " +
                "return 0"
        );
        ADD_TO_LIST_IF_EXISTS_SCRIPT.setResultType(Long.class);
    }

    /**
     * Lua脚本：原子性地从List中移除元素，如果List变空则自动删除key
     * 避免留下空List占用内存
     */
    private static final DefaultRedisScript<Long> REMOVE_AND_CLEANUP_SCRIPT;
    static {
        REMOVE_AND_CLEANUP_SCRIPT = new DefaultRedisScript<>();
        REMOVE_AND_CLEANUP_SCRIPT.setScriptText(
                "redis.call('LREM', KEYS[1], 0, ARGV[1]) " +
                "if redis.call('LLEN', KEYS[1]) == 0 then " +
                "  redis.call('DEL', KEYS[1]) " +
                "end " +
                "return 1"
        );
        REMOVE_AND_CLEANUP_SCRIPT.setResultType(Long.class);
    }

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
        String ttlSeconds = String.valueOf(RedisConstants.SESSION_OWNER_TTL * 60);

        // 1. 使用Lua脚本原子性地读取缓存并自动续期
        List<String> cachedSessionIds = stringRedisTemplate.execute(
                GET_AND_RENEW_SCRIPT,
                List.of(key),
                ttlSeconds
        );
        if (cachedSessionIds != null && !cachedSessionIds.isEmpty()) {
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

        // 3. 使用Lua脚本原子性地回填缓存并设置TTL
        List<String> sessionIds = sessions.stream()
                .map(Session::getSessionId)
                .toList();
        String[] args = new String[sessionIds.size() + 1];
        args[0] = ttlSeconds;
        for (int i = 0; i < sessionIds.size(); i++) {
            args[i + 1] = sessionIds.get(i);
        }
        stringRedisTemplate.execute(PUSH_LIST_WITH_TTL_SCRIPT, List.of(key), (Object[]) args);

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
    public void addSessionToCache(Integer userId, String sessionId) {
        String key = getOwnerCacheKey(userId);
        String ttlSeconds = String.valueOf(RedisConstants.SESSION_OWNER_TTL * 60);
        // 使用Lua脚本原子性地追加元素，仅当key存在时才追加并续期，消除TOCTOU竞态
        stringRedisTemplate.execute(ADD_TO_LIST_IF_EXISTS_SCRIPT, List.of(key), sessionId, ttlSeconds);
    }

    @Async("virtualThreadPoolExecutor")
    public void removeSessionFromCache(Integer userId, String sessionId) {
        String key = getOwnerCacheKey(userId);
        // 使用Lua脚本原子性地移除元素，如果List变空则自动删除key，避免内存泄漏
        stringRedisTemplate.execute(REMOVE_AND_CLEANUP_SCRIPT, List.of(key), sessionId);
    }
}
