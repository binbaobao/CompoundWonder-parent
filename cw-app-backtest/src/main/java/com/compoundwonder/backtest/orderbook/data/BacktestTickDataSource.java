package com.compoundwonder.backtest.orderbook.data;

import com.compoundwonder.core.engine.TickData;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 回测 Tick 数据源。
 * ClickHouse 实现只负责按确定顺序输出统一 TickData。
 */
public interface BacktestTickDataSource {

    /**
     * 流式回放指定股票、交易日的 Tick。
     *
     * @param tradeDate 回测交易日
     * @param stockCode 六位主板证券代码
     * @param tickConsumer 每读取一条 Tick 立即调用的消费者
     * @return 实际输出的 Tick 数量
     */
    long replay(LocalDate tradeDate, String stockCode, Consumer<TickData> tickConsumer);

    /**
     * 准备一个交易日的多股票 Tick 批次。
     *
     * <p>默认实现保持数据源接口兼容，回放时仍逐只读取。ClickHouse 实现会覆盖此方法，
     * 使用一条 UNION ALL 查询一次取回当天全部候选股票并支持重复回放。</p>
     */
    default BacktestDailyTickBatch loadDay(LocalDate tradeDate, Collection<String> stockCodes) {
        Set<String> requestedCodes = Set.copyOf(new LinkedHashSet<>(stockCodes));
        return new BacktestDailyTickBatch() {
            @Override
            public LocalDate tradeDate() {
                return tradeDate;
            }

            @Override
            public Set<String> stockCodes() {
                return requestedCodes;
            }

            @Override
            public long replay(String stockCode, Consumer<TickData> tickConsumer) {
                if (!requestedCodes.contains(stockCode)) {
                    return 0;
                }
                return BacktestTickDataSource.this.replay(tradeDate, stockCode, tickConsumer);
            }
        };
    }
}
