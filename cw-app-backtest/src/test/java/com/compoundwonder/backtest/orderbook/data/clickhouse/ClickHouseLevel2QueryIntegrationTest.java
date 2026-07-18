package com.compoundwonder.backtest.orderbook.data.clickhouse;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ClickHouse 真实连接性能探针。
 *
 * <p>默认测试不会执行；显式传入 {@code -Dclickhouse.probe=true} 才会全量读取三张表。</p>
 */
@SpringBootTest(
        classes = ClickHouseLevel2QueryIntegrationTest.ProbeConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIfSystemProperty(named = "clickhouse.probe", matches = "true")
class ClickHouseLevel2QueryIntegrationTest {

    private static final String SECURITY_ID = System.getProperty("clickhouse.security-id", "603567");
    private static final LocalDate TRADE_DATE = LocalDate.parse(
            System.getProperty("clickhouse.trade-date", "2026-07-16"));

    @Autowired
    private ClickHouseLevel2QueryService queryService;

    @Autowired
    private HikariDataSource clickHouseDataSource;

    @BeforeEach
    void initializeConnectionPoolBeforeTiming() throws SQLException {
        try (Connection ignored = clickHouseDataSource.getConnection()) {
            // 查询计时只统计 SQL、网络返回和对象映射，不把连接池首次启动算入 market。
        }
    }

    @Test
    void queriesAllThreeLevel2TablesWithoutLimit() {
        printAndVerify(queryService.queryMarket(SECURITY_ID, TRADE_DATE));
        ClickHouseQueryResult<ClickHouseOrderRow> orders = queryService.queryOrders(SECURITY_ID, TRADE_DATE);
        printAndVerify(orders);
        System.out.println("CLICKHOUSE_ORDER_TYPES " + orders.rows().stream()
                .collect(Collectors.groupingBy(
                        row -> row.tickType() + "/" + row.side(),
                        Collectors.counting())));
        ClickHouseQueryResult<ClickHouseTransRow> transactions = queryService.queryTransactions(SECURITY_ID, TRADE_DATE);
        printAndVerify(transactions);
        System.out.println("CLICKHOUSE_TRANS_TYPES " + transactions.rows().stream()
                .map(ClickHouseTransRow::tickType)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())));
    }

    @Test
    void replaysClickHouseLevel2AsOrderedTicks() {
        AtomicLong previousTime = new AtomicLong(-1L);
        AtomicInteger previousRank = new AtomicInteger(-1);
        long replayed = new ClickHouseBacktestTickDataSource(queryService)
                .replay(TRADE_DATE, SECURITY_ID, tick -> {
                    assertTrue(tick.time >= previousTime.get(), "Tick 时间发生倒序");
                    int rank = tick.dataType;
                    if (tick.time == previousTime.get()) {
                        assertTrue(rank >= previousRank.get(), "同毫秒事件类型发生倒序");
                    }
                    previousTime.set(tick.time);
                    previousRank.set(rank);
                });
        assertTrue(replayed > 0, "ClickHouse 回放没有输出 Tick");
    }

    private void printAndVerify(ClickHouseQueryResult<?> result) {
        System.out.printf(
                "CLICKHOUSE_PROBE table=%s rows=%d estimatedPayloadBytes=%d "
                        + "estimatedPayloadMiB=%.3f elapsedMs=%.3f%n",
                result.tableName(), result.rowCount(), result.estimatedPayloadBytes(),
                result.estimatedPayloadMiB(), result.elapsedMillis());
        assertTrue(result.rowCount() > 0, result.tableName() + " 没有返回数据");
    }

    /** 只装配 ClickHouse，避免性能探针启动订单簿、定时任务和两个 MySQL 数据源。 */
    @Configuration(proxyBeanMethods = false)
    static class ProbeConfiguration {

        @Bean(destroyMethod = "close")
        HikariDataSource clickHouseDataSource(
                @Value("${spring.datasource.dynamic.datasource.clickhouse.driver-class-name}") String driver,
                @Value("${spring.datasource.dynamic.datasource.clickhouse.url}") String url,
                @Value("${spring.datasource.dynamic.datasource.clickhouse.username}") String username,
                @Value("${spring.datasource.dynamic.datasource.clickhouse.password}") String password) {
            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setPoolName("clickhouse-probe");
            dataSource.setDriverClassName(driver);
            dataSource.setJdbcUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setMaximumPoolSize(1);
            return dataSource;
        }

        @Bean
        ClickHouseLevel2QueryService clickHouseLevel2QueryService(
                HikariDataSource clickHouseDataSource) {
            return new ClickHouseLevel2QueryService(new JdbcTemplate(clickHouseDataSource));
        }
    }
}
