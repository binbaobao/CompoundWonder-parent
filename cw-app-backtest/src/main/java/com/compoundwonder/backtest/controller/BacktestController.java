package com.compoundwonder.backtest.controller;


import com.compoundwonder.backtest.service.BacktestService;
import com.compoundwonder.backtest.service.Level2MinuteBarService;
import com.compoundwonder.backtest.service.StockSelectionBacktestService;
import com.compoundwonder.dto.*;
import com.compoundwonder.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("backtest")
public class BacktestController {


    private final BacktestService backtestService;

    private final Level2MinuteBarService level2MinuteBarService;

    private final StockSelectionBacktestService stockSelectionBacktestService;

    /**
     * 创建回测接口控制器。
     * 作用：注入回测查询服务和 Level2 分时查询服务。
     */
    public BacktestController(BacktestService backtestService, Level2MinuteBarService level2MinuteBarService, StockSelectionBacktestService stockSelectionBacktestService) {
        this.backtestService = backtestService;
        this.level2MinuteBarService = level2MinuteBarService;
        this.stockSelectionBacktestService = stockSelectionBacktestService;
    }


    /**
     * 查询可选择的情绪周期交易日。
     * 作用：给前端左侧日期下拉提供涨停、炸板和最高板摘要。
     */
    @GetMapping("trading-days")
    public Result<List<EmotionCycleCalendarDTO>> tradingDays() {
        return new Result<List<EmotionCycleCalendarDTO>>().ok(backtestService.findTradingDays());
    }

    /**
     * 查询股票池。
     * 作用：按 scope 返回涨停池、炸/断板池或上一交易日推荐盯盘池。
     */
    @GetMapping("stocks")
    public Result<List<Level2StockPoolDTO>> stocks(@RequestParam String date,
                                                   @RequestParam(defaultValue = "limit") String scope,
                                                   @RequestParam(defaultValue = "200") Integer limit) {
        LocalDate tradeDate = backtestService.findRecentTradingDay(date);
        if ("recommend".equals(scope)) {
            return new Result<List<Level2StockPoolDTO>>().ok(backtestService.findWatchingTaskPool(tradeDate, tradeDate, limit));
        }
        return new Result<List<Level2StockPoolDTO>>().ok(backtestService.findLevel2StockPool(tradeDate, scope, limit));
    }

    /**
     * 查询情绪周期摘要。
     * 作用：给前端左侧情绪面板提供涨停、连板、炸板、跌停、最高板和龙头信息。
     */
    @GetMapping("emotion-summary")
    public Result<EmotionCycleSummaryDTO> emotionSummary(@RequestParam String date) {
        LocalDate tradeDate = backtestService.findRecentTradingDay(date);
        return new Result<EmotionCycleSummaryDTO>().ok(backtestService.getEmotionSummary(tradeDate));
    }

    /**
     * 查询股票日 K。
     * 作用：给前端日 K 图表和详情日期选择器提供最近 N 条数据库日线。
     */
    @GetMapping("daily-bars")
    public Result<List<Level2ChartBarDTO>> dailyBars(@RequestParam String stockCode,
                                                     @RequestParam(defaultValue = "300") Integer limit) {
        return new Result<List<Level2ChartBarDTO>>().ok(backtestService.findDailyBars(stockCode, limit));
    }

    /**
     * 查询 Level2 分时。
     * 作用：按交易日读取本地 Level2 parquet 并返回前端分时图 tick 数据。
     */
    @GetMapping("minute-bars")
    public Result<List<Level2MinuteTickDTO>> minuteBars(@RequestParam String stockCode,
                                                        @RequestParam String date) {
        LocalDate tradeDate = backtestService.findRecentTradingDay(date);
        return new Result<List<Level2MinuteTickDTO>>().ok(level2MinuteBarService.findMinuteBars(stockCode, tradeDate));
    }

    /**
     * 查询历史回测记录。
     * 作用：按买入或卖出方向返回规则执行记录，供右侧回测列表和图表 B/S 标记使用。
     */
    @GetMapping("historical/backtest")
    public Result<List<RuleRecordDTO>> historicalBacktest(@RequestParam String stockCode,
                                                          @RequestParam String date,
                                                          @RequestParam Integer direction) {
        LocalDate tradeDate = backtestService.findRecentTradingDay(date);
        return new Result<List<RuleRecordDTO>>().ok(backtestService.findHistoricalBacktest(stockCode, tradeDate, direction));
    }


    /**
     * 选股回测
     *
     */
    @GetMapping("stock-selection-backtest")
    public Result<String> stockSelectionBacktest(@RequestParam String date) {
        stockSelectionBacktestService.stockSelectionBacktest(date);
        return new Result<String>().ok("ss");
    }



}
