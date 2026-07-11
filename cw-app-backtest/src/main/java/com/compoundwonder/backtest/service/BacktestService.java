package com.compoundwonder.backtest.service;

import com.compoundwonder.dto.EmotionCycleCalendarDTO;
import com.compoundwonder.dto.EmotionCycleSummaryDTO;
import com.compoundwonder.dto.Level2ChartBarDTO;
import com.compoundwonder.dto.Level2StockPoolDTO;
import com.compoundwonder.dto.RuleRecordDTO;

import java.time.LocalDate;
import java.util.List;

public interface BacktestService {

    /**
     * 查找不晚于指定日期的最近一个交易日。
     */
    LocalDate findRecentTradingDay(String date);

    /**
     * 查找指定交易日之前的最近一个交易日。
     */
    LocalDate findPreviousTradingDay(LocalDate tradeDate);

    /**
     * 查询前端日期选择器需要的情绪周期交易日列表。
     */
    List<EmotionCycleCalendarDTO> findTradingDays();

    /**
     * 查询指定交易日的涨停池或炸/断板池。
     */
    List<Level2StockPoolDTO> findLevel2StockPool(LocalDate tradeDate, String scope, int limit);

    /**
     * 查询推荐盯盘池，并用实际交易日的日 K 数据补齐行情字段。
     */
    List<Level2StockPoolDTO> findWatchingTaskPool(LocalDate tradeDate, LocalDate taskDate, int limit);

    /**
     * 查询单只股票的日 K 图表数据。
     */
    List<Level2ChartBarDTO> findDailyBars(String stockCode, int limit);

    /**
     * 查询指定交易日的情绪周期摘要。
     */
    EmotionCycleSummaryDTO getEmotionSummary(LocalDate tradeDate);

    /**
     * 查询历史规则回测记录。
     */
    List<RuleRecordDTO> findHistoricalBacktest(String stockCode, LocalDate tradeDate, int direction);

}
