package com.compoundwonder.backtest.controller;


import com.compoundwonder.backtest.service.BacktestService;
import com.compoundwonder.backtest.service.HistoricalBacktestTradeService;
import com.compoundwonder.backtest.service.Level2MinuteBarService;
import com.compoundwonder.backtest.service.SingleModeBacktestService;
import com.compoundwonder.backtest.service.model.SingleModeBacktestSummary;
import com.compoundwonder.backtest.service.model.SingleModeBoardStat;
import com.compoundwonder.backtest.service.model.SingleModeSamplePage;
import com.compoundwonder.backtest.service.impl.BackTestTradeService;
import com.compoundwonder.common.strategy.trade.TradeMode;
import com.compoundwonder.dto.*;
import com.compoundwonder.trader.entity.BacktestDailyRecord;
import com.compoundwonder.trader.entity.BacktestPosition;
import com.compoundwonder.trader.entity.BacktestRun;
import com.compoundwonder.trader.entity.RuleExecuteRecord;
import com.compoundwonder.trader.entity.SingleModeBacktestRun;
import com.compoundwonder.trader.service.StockWatchingTaskService;
import com.compoundwonder.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("backtest")
public class BacktestController {


    private final BacktestService backtestService;

    private final Level2MinuteBarService level2MinuteBarService;

    private final StockWatchingTaskService stockWatchingTaskService;

    private final BackTestTradeService backTestTradeService;

    private final HistoricalBacktestTradeService historicalBacktestTradeService;
    private final SingleModeBacktestService singleModeBacktestService;

    /**
     * 创建回测接口控制器。
     * 作用：注入回测查询服务和 Level2 分时查询服务。
     */
    public BacktestController(BacktestService backtestService,
                              Level2MinuteBarService level2MinuteBarService,
                              StockWatchingTaskService stockWatchingTaskService,
                              BackTestTradeService backTestTradeService,
                              HistoricalBacktestTradeService historicalBacktestTradeService,
                              SingleModeBacktestService singleModeBacktestService) {
        this.backtestService = backtestService;
        this.level2MinuteBarService = level2MinuteBarService;
        this.stockWatchingTaskService = stockWatchingTaskService;
        this.backTestTradeService = backTestTradeService;
        this.historicalBacktestTradeService = historicalBacktestTradeService;
        this.singleModeBacktestService = singleModeBacktestService;
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
     * 作用：给前端日 K 图表和详情日期选择器提供指定日期前后窗口内的数据库日线。
     */
    @GetMapping("daily-bars")
    public Result<List<Level2ChartBarDTO>> dailyBars(@RequestParam String stockCode,
                                                     @RequestParam String date,
                                                     @RequestParam(defaultValue = "300") Integer beforeLimit,
                                                     @RequestParam(defaultValue = "100") Integer afterLimit) {
        LocalDate tradeDate = backtestService.findRecentTradingDay(date);
        return new Result<List<Level2ChartBarDTO>>().ok(backtestService.findDailyBars(stockCode, tradeDate, beforeLimit, afterLimit));
    }

    /**
     * 查询 Level2 分时。
     * 作用：按交易日读取 ClickHouse Level2 快照并返回前端分时图 tick 数据。
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
        stockWatchingTaskService.createPostCloseWatchingTasks(LocalDate.parse(date));
        return new Result<String>().ok("ss");
    }

    /**
     * 使用指定交易日的 Level2 逐笔数据回放单只股票的订单簿。
     *
     * @param stockCode 股票代码
     * @param date 回放日期
     * @param direction 交易方向：1 买入（包含集合竞价撤单），2 卖出
     * @return 回放过程中触发的规则记录
     */
    @GetMapping("order-book/replay")
    public Result<List<RuleRecordDTO>> replayOrderBook(@RequestParam String stockCode,
                                                        @RequestParam String date,
                                                        @RequestParam(defaultValue = "1") Integer direction) {
        return new Result<List<RuleRecordDTO>>().ok(backTestTradeService.backTest(date, stockCode, direction));
    }

    /**
     * 按交易日串行执行完整的全仓单票历史回测。
     */
    @PostMapping("trade-runs")
    public Result<BacktestRun> runHistoricalTradingBacktest(@RequestParam LocalDate startDate,
                                                            @RequestParam LocalDate endDate) {
        return new Result<BacktestRun>().ok(
                historicalBacktestTradeService.startRange(startDate, endDate));
    }

    /** 查询最近创建的完整历史回测任务，供前端选择查看。 */
    @GetMapping("trade-runs")
    public Result<List<BacktestRun>> historicalTradingBacktestRuns(@RequestParam(defaultValue = "20") Integer limit) {
        return new Result<List<BacktestRun>>().ok(historicalBacktestTradeService.findRecentRuns(limit));
    }

    /**
     * 查询回测任务进度和最终结果。
     */
    @GetMapping("trade-runs/{runId}")
    public Result<BacktestRun> historicalTradingBacktestRun(@PathVariable Long runId) {
        return new Result<BacktestRun>().ok(historicalBacktestTradeService.findRun(runId));
    }

    /** 查询完整历史回测的每日权益记录。 */
    @GetMapping("trade-runs/{runId}/daily-records")
    public Result<List<BacktestDailyRecord>> historicalTradingBacktestDailyRecords(@PathVariable Long runId) {
        return new Result<List<BacktestDailyRecord>>().ok(historicalBacktestTradeService.findDailyRecords(runId));
    }

    /** 查询完整历史回测的持仓生命周期。 */
    @GetMapping("trade-runs/{runId}/positions")
    public Result<List<BacktestPosition>> historicalTradingBacktestPositions(@PathVariable Long runId) {
        return new Result<List<BacktestPosition>>().ok(historicalBacktestTradeService.findPositions(runId));
    }

    /**
     * 查询完整历史回测产生的全部规则。
     *
     * <p>包含最终成交、撤单以及因下单过晚未成交的规则；买入是否成交可根据
     * {@code time}、{@code lastOrderTime} 和回测任务保存的沪深市场延迟判断。</p>
     */
    @GetMapping("trade-runs/{runId}/rules")
    public Result<List<RuleExecuteRecord>> historicalTradingBacktestRules(@PathVariable Long runId) {
        return new Result<List<RuleExecuteRecord>>().ok(historicalBacktestTradeService.findRules(runId));
    }

    /** 启动指定交易模式、不受仓位约束的全样本回测。 */
    @PostMapping("single-mode-runs")
    public Result<SingleModeBacktestRun> runSingleModeBacktest(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "3") Integer tradeMode) {
        return new Result<SingleModeBacktestRun>().ok(
                singleModeBacktestService.startRange(
                        startDate, endDate, requireSingleModeTradeMode(tradeMode)));
    }

    /** 固定源任务的真实/虚拟买入事实，不重新选股，只重放卖出。 */
    @PostMapping("single-mode-runs/{sourceRunId}/replays")
    public Result<SingleModeBacktestRun> replaySingleModeBacktest(
            @PathVariable Long sourceRunId) {
        return new Result<SingleModeBacktestRun>().ok(
                singleModeBacktestService.startReplay(sourceRunId));
    }

    /** 固定复用已完成任务的全部候选，不重新选股，按当前规则重新执行买入和卖出。 */
    @PostMapping("single-mode-runs/{sourceRunId}/candidate-replays")
    public Result<SingleModeBacktestRun> replaySingleModeCandidates(
            @PathVariable Long sourceRunId) {
        return new Result<SingleModeBacktestRun>().ok(
                singleModeBacktestService.startCandidateReplay(sourceRunId));
    }

    /** 按交易模式查询最近的单模式任务。 */
    @GetMapping("single-mode-runs")
    public Result<List<SingleModeBacktestRun>> singleModeRuns(
            @RequestParam(defaultValue = "3") Integer tradeMode,
            @RequestParam(defaultValue = "20") Integer limit) {
        return new Result<List<SingleModeBacktestRun>>().ok(
                singleModeBacktestService.findRecentRuns(
                        requireSingleModeTradeMode(tradeMode), limit == null ? 20 : limit));
    }

    /** 查询单模式任务进度。 */
    @GetMapping("single-mode-runs/{runId}")
    public Result<SingleModeBacktestRun> singleModeRun(@PathVariable Long runId) {
        return new Result<SingleModeBacktestRun>().ok(singleModeBacktestService.findRun(runId));
    }

    /** 查询样本胜率、收益和首板晋级摘要。 */
    @GetMapping("single-mode-runs/{runId}/summary")
    public Result<SingleModeBacktestSummary> singleModeSummary(@PathVariable Long runId) {
        return new Result<SingleModeBacktestSummary>().ok(singleModeBacktestService.summarize(runId));
    }

    /** 查询每个板位的触板、封板和炸板率。 */
    @GetMapping("single-mode-runs/{runId}/board-stats")
    public Result<List<SingleModeBoardStat>> singleModeBoardStats(@PathVariable Long runId) {
        return new Result<List<SingleModeBoardStat>>().ok(singleModeBacktestService.boardStats(runId));
    }

    /** 分页查询独立买卖样本；持仓类型为空时查询全部，1 为真实成交，2 为虚拟卖出。 */
    @GetMapping("single-mode-runs/{runId}/samples")
    public Result<SingleModeSamplePage> singleModeSamples(
            @PathVariable Long runId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer pageSize,
            @RequestParam(required = false) Integer positionType) {
        return new Result<SingleModeSamplePage>().ok(
                singleModeBacktestService.findSamples(
                        runId, page == null ? 1 : page, pageSize == null ? 50 : pageSize,
                        requireSamplePositionType(positionType)));
    }

    private Integer requireSamplePositionType(Integer positionType) {
        if (positionType == null || positionType == 1 || positionType == 2) {
            return positionType;
        }
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "样本持仓类型仅支持真实成交或虚拟卖出");
    }

    private int requireSingleModeTradeMode(Integer tradeMode) {
        if (tradeMode == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "交易模式不能为空");
        }
        try {
            return TradeMode.fromCode(tradeMode).code();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "单模式全量回测仅支持 Model 1、2、3", exception);
        }
    }



}
