package com.maretu.bank.service.impl;

import com.maretu.bank.pojo.Bills;
import com.maretu.bank.mapper.BillsMapper;
import com.maretu.bank.service.IBillsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 账单表 服务实现类
 * </p>
 *
 * @author maretu
 * @since 2026-02-16
 */
@Service
public class BillsServiceImpl extends ServiceImpl<BillsMapper, Bills> implements IBillsService {

}
