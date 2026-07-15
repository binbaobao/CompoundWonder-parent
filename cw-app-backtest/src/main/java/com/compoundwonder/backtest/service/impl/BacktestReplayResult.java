package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.dto.RuleRecordDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 单股票单交易日的完整回放结果。
 */
public record BacktestReplayResult(LocalDate tradeDate,
                                   String symbol,
                                   String symbolName,
                                   BacktestReplayMode mode,
                                   List<RuleRecordDTO> records,
                                   int finalTransactionStatus,
                                   int lastOrderTime,
                                   int limitUpPrice,
                                   int lastPrice,
                                   long tickCount) {

    public BacktestReplayResult {
        records = List.copyOf(records);
    }

    public Optional<RuleRecordDTO> firstCancelRecord() {
        return firstRecord(RuleConstant.TRADING_MODE_CANCEL);
    }

    public Optional<RuleRecordDTO> firstSellRecord() {
        return firstRecord(RuleConstant.TRADING_MODE_SELL);
    }

    private Optional<RuleRecordDTO> firstRecord(int actionType) {
        return records.stream()
                .filter(record -> Integer.valueOf(actionType).equals(record.getActionType()))
                .min(java.util.Comparator.comparingInt(RuleRecordDTO::getTime));
    }
}
