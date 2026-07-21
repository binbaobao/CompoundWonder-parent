package com.compoundwonder.backtest.service.model;

import com.compoundwonder.trader.entity.SingleModeBacktestSample;

import java.util.List;

/** 单模式样本分页结果。 */
public record SingleModeSamplePage(long total, int page, int pageSize,
                                   List<SingleModeBacktestSample> records) {
    public SingleModeSamplePage {
        records = List.copyOf(records);
    }
}
