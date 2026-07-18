package com.compoundwonder.backtest.orderbook.data.clickhouse;

/**
 * ClickHouse 单日批量查询返回的最小 Level2 字段集合。
 *
 * <p>委托、成交和三秒快照共用一个 UNION ALL 结果。无关事件的字段使用 0 或空串，
 * 避免传输时间戳、完整十档数组以及订单簿回放不使用的快照字段。</p>
 */
record ClickHouseLevel2BatchRow(
        String securityId,
        int tradeTime,
        byte eventSource,
        String tickType,
        String side,
        float price,
        long volume,
        long orderNo,
        long buyNo,
        long sellNo,
        float askPrice1,
        float bidPrice1,
        long askVolume1,
        long askVolume2,
        long bidVolume1,
        long bidVolume2,
        long totalVolumeTrade,
        long totalValueTrade) {

    static final byte EVENT_ORDER = 0;
    static final byte EVENT_TRANSACTION = 1;
    static final byte EVENT_MARKET = 2;
}
