package com.compoundwonder.backtest.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.compoundwonder.backtest.orderbook.BacktestOrderExecutionGateway;
import com.compoundwonder.backtest.orderbook.data.BacktestTickDataSource;
import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.core.engine.DisruptorOrderBookEngine;
import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.dto.RuleRecordDTO;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.service.StockDailyService;
import com.compoundwonder.util.SymbolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

/**
 * 单股票历史订单簿回测编排服务。
 *
 * <p>负责准备订单簿、驱动可替换的数据源、等待 Disruptor 消费完成并返回规则记录；
 * 不负责 Parquet 或 ClickHouse 的具体读取实现。</p>
 */
@Slf4j
@Service
public class BackTestTradeService {

    private final DisruptorOrderBookEngine orderBookEngine;
    private final BacktestTickDataSource tickDataSource;
    private final StockDailyService stockDailyService;
    private final BacktestOrderExecutionGateway executionGateway;
    private final Duration replayTimeout;

    public BackTestTradeService(DisruptorOrderBookEngine orderBookEngine,
                                BacktestTickDataSource tickDataSource,
                                StockDailyService stockDailyService,
                                BacktestOrderExecutionGateway executionGateway,
                                @Value("${backtest.replay-timeout-seconds:120}") long replayTimeoutSeconds) {
        this.orderBookEngine = orderBookEngine;
        this.tickDataSource = tickDataSource;
        this.stockDailyService = stockDailyService;
        this.executionGateway = executionGateway;
        this.replayTimeout = Duration.ofSeconds(replayTimeoutSeconds);
    }

    /**
     * 执行一次单股票、单交易日回测。
     *
     * <p>Disruptor 在应用启动时已经长期运行。该方法只清理并注册本轮订单簿，
     * 不启动或关闭 Disruptor。方法加锁是为了防止多个 HTTP 请求共享同一套 Handler
     * 私有数组时互相覆盖。</p>
     *
     * @param date 回测交易日，格式 yyyy-MM-dd
     * @param stockCode 60 或 00 开头的六位主板代码
     * @param direction 1 表示测试买入规则，2 表示测试卖出规则
     * @return 本轮对应方向触发的规则记录
     */
    public synchronized List<RuleRecordDTO> backTest(String date, String stockCode, int direction) {
        validateDirection(direction);
        validateStockCode(stockCode);
        LocalDate tradeDate = LocalDate.parse(date);
        int symbolId = SymbolUtil.fastSymbolToInt(stockCode);
        OrderBook orderBook = buildOrderBook(tradeDate, stockCode, direction);

        orderBookEngine.reset();
        executionGateway.clear();
        orderBookEngine.registerOrderBook(symbolId, orderBook);

        boolean replayStarted = false;
        boolean processed = false;
        try {
            replayStarted = true;
            long tickCount = tickDataSource.replay(tradeDate, stockCode, orderBookEngine::publish);
            if (tickCount == 0) {
                throw new IllegalStateException(tradeDate + " 没有找到股票 " + stockCode + " 的 Level2 Tick");
            }

            orderBookEngine.awaitProcessed(replayTimeout);
            processed = true;
            List<RuleRecordDTO> records = orderBookEngine.drainRuleRecords().stream()
                    .filter(record -> matchesDirection(record, direction))
                    .toList();
            log.info("回测完成 date={}, stockCode={}, direction={}, tickCount={}, ruleCount={}, orderBook={}",
                    tradeDate, stockCode, direction, tickCount, records.size(), orderBook);
            return records;
        } finally {
            if (replayStarted && !processed) {
                awaitPartiallyPublishedTicks();
            }
            orderBookEngine.reset();
            executionGateway.clear();
        }
    }

    /**
     * 使用回测当天日 K 的参考昨收价初始化订单簿价格区间。
     *
     * <p>当天日 K 提供 prevClose 和流通股本；名称和所有策略统计仍使用回测日前的数据。</p>
     */
    OrderBook buildOrderBook(LocalDate tradeDate, String stockCode, int direction) {
        List<StockDailyEntity> dailyRows = stockDailyService.list(
                Wrappers.<StockDailyEntity>lambdaQuery()
                        .eq(StockDailyEntity::getStockCode, stockCode)
                        .le(StockDailyEntity::getTradeDate, tradeDate)
                        .orderByDesc(StockDailyEntity::getTradeDate)
                        .last("LIMIT 201"));
        if (dailyRows.isEmpty() || !tradeDate.equals(dailyRows.get(0).getTradeDate())) {
            throw new IllegalArgumentException(tradeDate + " 没有股票 " + stockCode + " 的当日日 K 数据");
        }

        StockDailyEntity currentDaily = dailyRows.get(0);
        List<StockDailyEntity> history = dailyRows.stream()
                .filter(daily -> daily.getTradeDate() != null && daily.getTradeDate().isBefore(tradeDate))
                .limit(200)
                .toList();
        if (history.isEmpty()) {
            throw new IllegalArgumentException(tradeDate + " 之前没有股票 " + stockCode + " 的日 K 数据");
        }
        StockDailyEntity previousDaily = history.get(0);
        if (currentDaily.getPrevClose() == null || currentDaily.getPrevClose() <= 0) {
            throw new IllegalStateException("回测当日日 K 的 prevClose 缺失: " + stockCode + " " + tradeDate);
        }
        if (currentDaily.getFloatShares() == null || currentDaily.getFloatShares() <= 0) {
            throw new IllegalStateException("回测当日日 K 的流通股本缺失: " + stockCode + " " + tradeDate);
        }
        long circulation = currentDaily.getFloatShares();
        long maxVolume = history.stream()
                .map(StockDailyEntity::getVolume)
                .filter(java.util.Objects::nonNull)
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        OrderBook orderBook = new OrderBook(
                stockCode, circulation, currentDaily.getPrevClose(), maxVolume);
        orderBook.setSecurityName(previousDaily.getStockName());
        orderBook.setDate(tradeDate.toString());
        orderBook.setLbcs(Math.max(0, valueOrZero(previousDaily.getConsecutiveLimitUpDays())));
        orderBook.setYesterdayTurnover(valueOrZero(previousDaily.getTurnoverRate()));
        if (history.size() > 1) {
            orderBook.setTwoDaysTurnover(valueOrZero(history.get(1).getTurnoverRate()));
        }
        if (history.size() > 2) {
            orderBook.setThreeDaysTurnover(valueOrZero(history.get(2).getTurnoverRate()));
        }
        orderBook.setInitialMarketValue(calculateInitialMarketValue(previousDaily, orderBook.getLbcs()));
        orderBook.setMaxHs(maxVolume * 100.0 / circulation);
        orderBook.setTransactionStatus(direction == 1 ? 1 : -1);
        return orderBook;
    }

    private int calculateInitialMarketValue(StockDailyEntity previousDaily, int consecutiveLimitUpDays) {
        if (previousDaily.getFloatMarketCap() == null) {
            return 0;
        }
        return (int) Math.round(previousDaily.getFloatMarketCap()
                / Math.pow(1.1, consecutiveLimitUpDays));
    }

    private boolean matchesDirection(RuleRecordDTO record, int direction) {
        return direction == 1
                ? Integer.valueOf(RuleConstant.TRADING_MODE_BUY).equals(record.getActionType())
                : Integer.valueOf(RuleConstant.TRADING_MODE_SELL).equals(record.getActionType());
    }

    private void validateDirection(int direction) {
        if (direction != 1 && direction != 2) {
            throw new IllegalArgumentException("direction 只能是 1（买入）或 2（卖出）");
        }
    }

    private void validateStockCode(String stockCode) {
        if (stockCode == null || !stockCode.matches("(?:60|00)\\d{4}")) {
            throw new IllegalArgumentException("当前回测只支持 60/00 沪深主板股票: " + stockCode);
        }
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private double valueOrZero(Double value) {
        return value == null ? 0D : value;
    }

    /**
     * 数据源中途失败时，先等待已经发布的 Tick 消费完成，再清理 Handler 私有数组。
     */
    private void awaitPartiallyPublishedTicks() {
        try {
            orderBookEngine.awaitProcessed(replayTimeout);
        } catch (RuntimeException exception) {
            log.warn("等待部分回测 Tick 消费完成失败，准备清理本轮状态", exception);
        }
    }
}
