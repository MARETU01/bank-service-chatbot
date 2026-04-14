package com.maretu.bank.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.maretu.bank.dto.DashboardStats;
import com.maretu.bank.pojo.Accounts;
import com.maretu.bank.pojo.Transactions;
import com.maretu.bank.service.ITransactionsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AccountsServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountsServiceImpl 账户服务测试")
class AccountsServiceImplTest {

    @Mock
    private ITransactionsService transactionsService;

    @InjectMocks
    @Spy
    private AccountsServiceImpl accountsService;

    // ==================== 辅助方法 ====================

    private Accounts buildAccount(Long id, Long userId, BigDecimal balance, int status) {
        return new Accounts()
                .setId(id)
                .setUserId(userId)
                .setAccountNumber("620000000000000" + id)
                .setAccountName("测试账户" + id)
                .setBalance(balance)
                .setStatus(status)
                .setDailyLimit(new BigDecimal("50000.00"))
                .setCurrency("CNY");
    }

    // ==================== 更新账户状态 ====================

    @Nested
    @DisplayName("更新账户状态")
    class UpdateStatusTest {

        @Test
        @DisplayName("账户不存在 - 应抛出异常")
        void accountNotExist_shouldThrowException() {
            doReturn(null).when(accountsService).getAccountByIdAndUserId(1L, 1L);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> accountsService.updateStatus(1L, 1L, 1));
            assertEquals("Account does not exist or no permission to access", ex.getMessage());
        }

        @Test
        @DisplayName("无效状态值(负数) - 应抛出异常")
        void invalidStatusNegative_shouldThrowException() {
            Accounts account = buildAccount(1L, 1L, BigDecimal.ZERO, 1);
            doReturn(account).when(accountsService).getAccountByIdAndUserId(1L, 1L);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> accountsService.updateStatus(1L, 1L, -1));
            assertEquals("Invalid status value", ex.getMessage());
        }

        @Test
        @DisplayName("无效状态值(>2) - 应抛出异常")
        void invalidStatusTooLarge_shouldThrowException() {
            Accounts account = buildAccount(1L, 1L, BigDecimal.ZERO, 1);
            doReturn(account).when(accountsService).getAccountByIdAndUserId(1L, 1L);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> accountsService.updateStatus(1L, 1L, 3));
            assertEquals("Invalid status value", ex.getMessage());
        }

        @Test
        @DisplayName("直接关闭账户(status=2) - 应抛出异常")
        void closeAccountDirectly_shouldThrowException() {
            Accounts account = buildAccount(1L, 1L, BigDecimal.ZERO, 1);
            doReturn(account).when(accountsService).getAccountByIdAndUserId(1L, 1L);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> accountsService.updateStatus(1L, 1L, 2));
            assertTrue(ex.getMessage().contains("not supported"));
        }

        @Test
        @DisplayName("正常冻结账户(1→0) - 应成功")
        void freezeAccount_shouldSucceed() {
            Accounts account = buildAccount(1L, 1L, new BigDecimal("10000"), 1);
            doReturn(account).when(accountsService).getAccountByIdAndUserId(1L, 1L);
            doReturn(true).when(accountsService).updateById(any(Accounts.class));

            Accounts result = accountsService.updateStatus(1L, 1L, 0);
            assertEquals(0, result.getStatus());
        }
    }

    // ==================== 更新账户信息 ====================

    @Nested
    @DisplayName("更新账户信息")
    class UpdateAccountTest {

        @Test
        @DisplayName("账户不存在 - 应抛出异常")
        void accountNotExist_shouldThrowException() {
            doReturn(null).when(accountsService).getAccountByIdAndUserId(1L, 1L);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> accountsService.updateAccount(1L, 1L, new Accounts()));
            assertEquals("Account does not exist or no permission to access", ex.getMessage());
        }
    }

    // ==================== Dashboard统计 ====================

    @Nested
    @DisplayName("Dashboard统计")
    class DashboardStatsTest {

        @Test
        @DisplayName("无账户 - 应返回全零统计")
        void noAccounts_shouldReturnZeroStats() {
            doReturn(List.of()).when(accountsService).getAccountsByUserId(1L);

            DashboardStats stats = accountsService.getDashboardStats(1L);

            assertEquals(BigDecimal.ZERO, stats.getTotalBalance());
            assertEquals(BigDecimal.ZERO, stats.getIncome());
            assertEquals(BigDecimal.ZERO, stats.getExpense());
            assertEquals(0, stats.getAccountCount());
            assertEquals(0, stats.getTransactionCount());
        }

        @Test
        @DisplayName("有账户有交易 - 应正确计算统计数据")
        @SuppressWarnings("unchecked")
        void withAccountsAndTransactions_shouldCalculateCorrectly() {
            // 准备两个账户
            Accounts account1 = buildAccount(1L, 1L, new BigDecimal("10000.00"), 1);
            Accounts account2 = buildAccount(2L, 1L, new BigDecimal("5000.00"), 1);
            doReturn(List.of(account1, account2)).when(accountsService).getAccountsByUserId(1L);

            // 准备交易记录
            Transactions deposit = new Transactions().setAccountId(1L)
                    .setTransactionType("DEPOSIT").setAmount(new BigDecimal("3000.00")).setStatus(1);
            Transactions transferIn = new Transactions().setAccountId(1L)
                    .setTransactionType("TRANSFER_IN").setAmount(new BigDecimal("2000.00")).setStatus(1);
            Transactions transferOut = new Transactions().setAccountId(1L)
                    .setTransactionType("TRANSFER_OUT").setAmount(new BigDecimal("1000.00")).setStatus(1);
            Transactions payment = new Transactions().setAccountId(2L)
                    .setTransactionType("PAYMENT").setAmount(new BigDecimal("500.00")).setStatus(1);

            // mock transactionsService.lambdaQuery() 链式调用
            LambdaQueryChainWrapper<Transactions> txWrapper = mock(LambdaQueryChainWrapper.class);
            when(transactionsService.lambdaQuery()).thenReturn(txWrapper);
            when(txWrapper.in(any(), (List<?>) any())).thenReturn(txWrapper);
            when(txWrapper.eq(any(), any())).thenReturn(txWrapper);
            when(txWrapper.list()).thenReturn(List.of(deposit, transferIn, transferOut, payment));

            DashboardStats stats = accountsService.getDashboardStats(1L);

            // 总资产 = 10000 + 5000 = 15000
            assertEquals(0, new BigDecimal("15000.00").compareTo(stats.getTotalBalance()));
            // 收入 = 3000(DEPOSIT) + 2000(TRANSFER_IN) = 5000
            assertEquals(0, new BigDecimal("5000.00").compareTo(stats.getIncome()));
            // 支出 = 1000(TRANSFER_OUT) + 500(PAYMENT) = 1500
            assertEquals(0, new BigDecimal("1500.00").compareTo(stats.getExpense()));
            // 账户数 = 2
            assertEquals(2, stats.getAccountCount());
            // 交易笔数 = 4
            assertEquals(4, stats.getTransactionCount());
        }

        @Test
        @DisplayName("冻结账户不计入总资产")
        void frozenAccount_shouldNotCountInTotalBalance() {
            Accounts activeAccount = buildAccount(1L, 1L, new BigDecimal("10000.00"), 1);
            Accounts frozenAccount = buildAccount(2L, 1L, new BigDecimal("5000.00"), 0); // 冻结
            doReturn(List.of(activeAccount, frozenAccount)).when(accountsService).getAccountsByUserId(1L);

            // mock 空交易记录
            @SuppressWarnings("unchecked")
            LambdaQueryChainWrapper<Transactions> txWrapper = mock(LambdaQueryChainWrapper.class);
            when(transactionsService.lambdaQuery()).thenReturn(txWrapper);
            when(txWrapper.in(any(), (List<?>) any())).thenReturn(txWrapper);
            when(txWrapper.eq(any(), any())).thenReturn(txWrapper);
            when(txWrapper.list()).thenReturn(List.of());

            DashboardStats stats = accountsService.getDashboardStats(1L);

            // 只计算活跃账户的余额
            assertEquals(0, new BigDecimal("10000.00").compareTo(stats.getTotalBalance()));
            // 但账户数包含所有账户
            assertEquals(2, stats.getAccountCount());
        }
    }
}
