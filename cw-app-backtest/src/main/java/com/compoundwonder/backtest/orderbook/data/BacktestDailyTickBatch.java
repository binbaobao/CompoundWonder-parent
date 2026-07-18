package com.compoundwonder.backtest.orderbook.data;

import com.compoundwonder.core.engine.TickData;

import java.time.LocalDate;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 单个交易日的可重复回放 Tick 批次。
 *
 * <p>完整历史回测会因隔夜撤单、盘中卖出后再买入而重复构建同一股票的订单簿。
 * 批次只负责复用当天已经读取的数据，每次回放仍由核心订单簿从空状态重新处理。</p>
 */
public interface BacktestDailyTickBatch {

    /** 批次对应的唯一交易日。 */
    LocalDate tradeDate();

    /** 批次中实际请求的股票代码。 */
    Set<String> stockCodes();

    /**
     * 按查询时已经确定的事件顺序，重新输出指定股票的全部 Tick。
     *
     * @return 实际输出的 Tick 数量；批次中没有该股票时返回 0
     */
    long replay(String stockCode, Consumer<TickData> tickConsumer);
}
