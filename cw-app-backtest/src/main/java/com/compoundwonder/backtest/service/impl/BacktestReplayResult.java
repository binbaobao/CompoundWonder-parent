package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.dto.RuleRecordDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 单股票单交易日的完整回放结果。
 *
 * <p>除规则与最终盘口外，同时带回模板预编译的竞价许可和最早盘中买入时间，完整历史
 * 回测据此决定是评估隔夜排队成交，还是从允许时点重新寻找盘中买点。</p>
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
                                   long tickCount,
                                   boolean openingAuctionBuyAllowed,
                                   int earliestContinuousBuyTime,
                                   String openingAuctionBlockReason) {

    /** 旧测试和旧调用方兼容构造器。 */
    public BacktestReplayResult(LocalDate tradeDate, String symbol, String symbolName,
                                BacktestReplayMode mode, List<RuleRecordDTO> records,
                                int finalTransactionStatus, int lastOrderTime,
                                int limitUpPrice, int lastPrice, long tickCount) {
        this(tradeDate, symbol, symbolName, mode, records, finalTransactionStatus,
                lastOrderTime, limitUpPrice, lastPrice, tickCount, true, 0, null);
    }

    public BacktestReplayResult {
        // 与 Handler 的复用 RuleRecordBuffer 脱钩，并禁止调用方改写本轮结果集合。
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
