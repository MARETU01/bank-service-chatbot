package com.maretu.user.service.impl;

import com.maretu.user.pojo.UserSecurity;
import com.maretu.user.mapper.UserSecurityMapper;
import com.maretu.user.service.IUserSecurityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户安全信息表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Service
public class UserSecurityServiceImpl extends ServiceImpl<UserSecurityMapper, UserSecurity> implements IUserSecurityService {

}
