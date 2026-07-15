package com.compoundwonder.backtest.orderbook.data;

import com.compoundwonder.core.engine.TickData;

import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * 回测 Tick 数据源。
 * DuckDB/Parquet、ClickHouse 等实现只负责按确定顺序输出统一 TickData。
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
}
