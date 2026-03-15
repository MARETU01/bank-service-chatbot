package com.maretu.chat.service.impl;

import com.maretu.chat.pojo.Session;
import com.maretu.chat.mapper.SessionMapper;
import com.maretu.chat.service.ISessionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements ISessionService {

    @Override
    public List<Session> getSessions(Integer userId) {
        return lambdaQuery()
                .eq(Session::getUserId, userId)
                .orderByDesc(Session::getUpdatedAt)
                .list();
    }

    @Override
    public Session createSession(Integer userId, String title) {
        Session session = new Session()
                .setSessionId(UUID.randomUUID().toString())
                .setUserId(Long.valueOf(userId))
                .setTitle(title != null ? title : "新会话");
        save(session);
        return session;
    }

    @Override
    public void deleteSession(String sessionId) {
        lambdaUpdate()
                .eq(Session::getSessionId, sessionId)
                .set(Session::getDeleted, 1)
                .set(Session::getUpdatedAt, LocalDateTime.now())
                .update();
    }
}
