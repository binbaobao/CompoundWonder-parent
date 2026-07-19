package com.compoundwonder.trader.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 多数据源选股编排的路由约束。
 */
class StockWatchingTaskDataSourceRoutingTest {

    @Test
    void postCloseOrchestrationMustNotOpenSingleDataSourceTransaction() throws Exception {
        Transactional transactional = StockWatchingTaskServiceImpl.class
                .getMethod("createPostCloseWatchingTasks", LocalDate.class)
                .getAnnotation(Transactional.class);

        assertNull(transactional,
                "顶层事务会把 market 查询和 trade 写入固定到同一数据库连接");
    }
}
