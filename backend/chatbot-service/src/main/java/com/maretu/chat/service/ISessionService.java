package com.maretu.chat.service;

import com.maretu.chat.pojo.Session;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 聊天会话表 服务类
 * </p>
 *
 * @author maretu
 * @since 2026-03-14
 */
public interface ISessionService extends IService<Session> {

    List<Session> getSessions(Integer userId);

    Session createSession(Integer userId, String title);

    void deleteSession(String sessionId);
}
