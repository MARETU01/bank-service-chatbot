package com.maretu.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maretu.user.pojo.Roles;

import java.util.List;

/**
 * 角色服务类
 *
 * @author maretu
 * @since 2025-04-23
 */
public interface IRolesService extends IService<Roles> {

    /**
     * 根据角色代码获取角色
     */
    Roles getByCode(String roleCode);

    /**
     * 获取用户的所有角色代码
     */
    List<String> getRoleCodesByUserId(Long userId);
}
