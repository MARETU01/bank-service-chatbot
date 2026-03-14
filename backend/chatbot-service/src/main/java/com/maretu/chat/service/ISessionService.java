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

    /**
     * 获取用户的所有会话
     * @param userId 用户 ID
     * @return 会话列表
     */
    List<Session> getSessions(Integer userId);

    /**
     * 创建新会话
     * @param userId 用户 ID
     * @param title 会话标题
     * @return 创建后的会话
     */
    Session createSession(Integer userId, String title);

    /**
     * 删除会话（软删除）
     * @param sessionId 会话 ID
     */
    void deleteSession(Long sessionId);

    /**
     * 根据 sessionId 获取会话
     * @param sessionId 会话 ID
     * @return 会话
     */
    Session getBySessionId(String sessionId);
}
