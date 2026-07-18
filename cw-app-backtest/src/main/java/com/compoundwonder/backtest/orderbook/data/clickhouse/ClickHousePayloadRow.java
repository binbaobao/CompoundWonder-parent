package com.compoundwonder.backtest.orderbook.data.clickhouse;

import java.nio.charset.StandardCharsets;

/**
 * ClickHouse 查询行的原始字段载荷估算接口。
 *
 * <p>估算值按 ClickHouse 字段的未压缩基础宽度累计，不包含网络协议块、压缩、
 * LowCardinality 字典、JVM 对象头、数组对象和 List 容器开销。</p>
 */
public interface ClickHousePayloadRow {

    /** 返回当前行的未压缩字段载荷估算字节数。 */
    long estimatedPayloadBytes();

    /** 按 UTF-8 计算 String 字段的实际内容字节数。 */
    static int utf8Bytes(String value) {
        return value == null ? 0 : value.getBytes(StandardCharsets.UTF_8).length;
    }
}
