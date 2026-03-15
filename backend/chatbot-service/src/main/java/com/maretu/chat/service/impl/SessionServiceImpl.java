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
    public Boolean isSessionOwner(Integer userId, String sessionId) {
        return lambdaQuery()
                .eq(Session::getSessionId, sessionId)
                .eq(Session::getUserId, userId)
                .count() > 0;
    }

    @Override
    public Session createSession(Integer userId) {
        Session session = new Session()
                .setSessionId(UUID.randomUUID().toString())
                .setUserId(Long.valueOf(userId));
        save(session);
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
        return removeById(session.getId());
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
}
