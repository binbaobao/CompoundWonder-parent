package com.compoundwonder.backtest.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.compoundwonder.backtest.service.BacktestService;
import com.compoundwonder.dto.EmotionCycleCalendarDTO;
import com.compoundwonder.dto.EmotionCycleSummaryDTO;
import com.compoundwonder.dto.Level2ChartBarDTO;
import com.compoundwonder.dto.Level2StockPoolDTO;
import com.compoundwonder.dto.RuleRecordDTO;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.entity.StockTradeCalendar;
import com.compoundwonder.hxdata.service.StockDailyService;
import com.compoundwonder.hxdata.service.StockTradeCalendarService;
import com.compoundwonder.trader.entity.RuleExecuteRecord;
import com.compoundwonder.trader.entity.StockEmotionCycleDaily;
import com.compoundwonder.trader.entity.StockWatchingTask;
import com.compoundwonder.trader.mapper.RuleExecuteRecordMapper;
import com.compoundwonder.trader.mapper.StockWatchingTaskMapper;
import com.compoundwonder.trader.service.StockEmotionCycleDailyService;
import com.compoundwonder.util.SymbolUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 回测工作台查询服务实现。
 * 作用：把回测前端需要的股票池、情绪周期、日 K 和规则执行记录 DTO 从现有表中组装出来。
 */
@Service
public class BacktestServiceImpl implements BacktestService {

    private static final String SCOPE_RECOMMEND = "recommend";
    private static final String SCOPE_LIMIT = "limit";
    private static final String SCOPE_BREAK = "break";

    private final StockDailyService stockDailyService;
    private final StockTradeCalendarService stockTradeCalendarService;
    private final StockEmotionCycleDailyService stockEmotionCycleDailyService;
    private final StockWatchingTaskMapper stockWatchingTaskMapper;
    private final RuleExecuteRecordMapper ruleExecuteRecordMapper;

    /**
     * 创建回测查询服务。
     * 作用：注入市场日 K、交易日历、情绪周期和交易研究库 Mapper。
     */
    public BacktestServiceImpl(StockDailyService stockDailyService,
                               StockTradeCalendarService stockTradeCalendarService,
                               StockEmotionCycleDailyService stockEmotionCycleDailyService,
                               StockWatchingTaskMapper stockWatchingTaskMapper,
                               RuleExecuteRecordMapper ruleExecuteRecordMapper) {
        this.stockDailyService = stockDailyService;
        this.stockTradeCalendarService = stockTradeCalendarService;
        this.stockEmotionCycleDailyService = stockEmotionCycleDailyService;
        this.stockWatchingTaskMapper = stockWatchingTaskMapper;
        this.ruleExecuteRecordMapper = ruleExecuteRecordMapper;
    }

    /**
     * 查找不晚于指定日期的最近一个交易日。
     * 实现逻辑：优先按交易日历表向前找，日历缺失时回退为入参日期。
     */
    @Override
    @DS("market")
    public LocalDate findRecentTradingDay(String date) {
        LocalDate targetDate = LocalDate.parse(date);
        StockTradeCalendar calendar = stockTradeCalendarService.getOne(Wrappers.<StockTradeCalendar>lambdaQuery()
                .le(StockTradeCalendar::getTradeDate, targetDate)
                .orderByDesc(StockTradeCalendar::getTradeDate)
                .last("LIMIT 1"));
        return calendar == null ? targetDate : calendar.getTradeDate();
    }

    /**
     * 查找指定交易日前一个交易日。
     * 实现逻辑：按交易日历表倒序查询第一条小于当前交易日的记录。
     */
    @Override
    @DS("market")
    public LocalDate findPreviousTradingDay(LocalDate tradeDate) {
        StockTradeCalendar calendar = stockTradeCalendarService.getOne(Wrappers.<StockTradeCalendar>lambdaQuery()
                .lt(StockTradeCalendar::getTradeDate, tradeDate)
                .orderByDesc(StockTradeCalendar::getTradeDate)
                .last("LIMIT 1"));
        return calendar == null ? tradeDate.minusDays(1) : calendar.getTradeDate();
    }

    /**
     * 查询前端日期选择器需要的情绪周期交易日列表。
     * 实现逻辑：直接读取已聚合的情绪周期表，按日期升序返回涨停、炸板和最高板。
     */
    @Override
    @DS("trade")
    public List<EmotionCycleCalendarDTO> findTradingDays() {
        return stockEmotionCycleDailyService.list(Wrappers.<StockEmotionCycleDaily>lambdaQuery()
                        .orderByAsc(StockEmotionCycleDaily::getTradeDate))
                .stream()
                .map(this::toCalendarDTO)
                .toList();
    }

    /**
     * 查询指定交易日的涨停池或炸/断板池。
     * 实现逻辑：只关注连板，涨停池保留 2 板及以上，炸/断板池保留 2 板断板及以上，并过滤 ST。
     */
    @Override
    @DS("market")
    public List<Level2StockPoolDTO> findLevel2StockPool(LocalDate tradeDate, String scope, int limit) {
        String normalizedScope = normalizeScope(scope);
        List<StockDailyEntity> dailyList = stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getTradeDate, tradeDate)
                .and(wrapper -> wrapper.isNull(StockDailyEntity::getIsSt).or().eq(StockDailyEntity::getIsSt, false))
                .ge(SCOPE_LIMIT.equals(normalizedScope), StockDailyEntity::getConsecutiveLimitUpDays, 2)
                .le(SCOPE_BREAK.equals(normalizedScope), StockDailyEntity::getConsecutiveLimitUpDays, -2)
                .orderByDesc(StockDailyEntity::getConsecutiveLimitUpDays)
                .orderByDesc(StockDailyEntity::getTurnover)
                .last("LIMIT " + safeLimit(limit)));
        return dailyList.stream()
                .map(daily -> toStockPoolDTO(daily, normalizedScope))
                .toList();
    }

    /**
     * 查询推荐盯盘池。
     * 实现逻辑：按推荐日期读取 stock_watching_task，再用实际交易日 stock_daily 补齐行情字段。
     */
    @Override
    @DS("trade")
    public List<Level2StockPoolDTO> findWatchingTaskPool(LocalDate tradeDate, LocalDate taskDate, int limit) {
        List<StockWatchingTask> tasks = stockWatchingTaskMapper.selectList(Wrappers.<StockWatchingTask>lambdaQuery()
                .eq(StockWatchingTask::getTradeDate, taskDate)
                .orderByDesc(StockWatchingTask::getConsecutiveLimitUpDays)
                .orderByDesc(StockWatchingTask::getLimitUpScore)
                .last("LIMIT " + safeLimit(limit)));
        Map<String, StockDailyEntity> dailyMap = findDailyMap(tradeDate, tasks.stream()
                .map(StockWatchingTask::getStockCode)
                .filter(Objects::nonNull)
                .toList());
        return tasks.stream()
                .map(task -> toWatchingTaskDTO(task, dailyMap.get(task.getStockCode())))
                .toList();
    }

    /**
     * 查询单只股票的日 K 图表数据。
     * 实现逻辑：按交易日倒序取最近 N 条，再恢复为升序方便前端时间轴渲染。
     */
    @Override
    @DS("market")
    public List<Level2ChartBarDTO> findDailyBars(String stockCode, int limit) {
        return stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                        .eq(StockDailyEntity::getStockCode, stockCode)
                        .orderByDesc(StockDailyEntity::getTradeDate)
                        .last("LIMIT " + safeLimit(limit)))
                .stream()
                .sorted(Comparator.comparing(StockDailyEntity::getTradeDate))
                .map(this::toChartBarDTO)
                .toList();
    }

    /**
     * 查询指定交易日的情绪周期摘要。
     * 实现逻辑：读取当日和前一交易日情绪数据，计算连板率和炸板率后返回前端摘要 DTO。
     */
    @Override
    @DS("trade")
    public EmotionCycleSummaryDTO getEmotionSummary(LocalDate tradeDate) {
        StockEmotionCycleDaily current = findEmotionCycleDaily(tradeDate);
        StockEmotionCycleDaily previous = findEmotionCycleDaily(findPreviousTradingDay(tradeDate));
        return toEmotionSummaryDTO(tradeDate, current, previous);
    }

    /**
     * 查询历史规则回测记录。
     * 实现逻辑：买入方向只返回买入记录，卖出方向返回卖出和撤单记录，供前端右侧列表和图表标记使用。
     */
    @Override
    @DS("trade")
    public List<RuleRecordDTO> findHistoricalBacktest(String stockCode, LocalDate tradeDate, int direction) {
        return ruleExecuteRecordMapper.selectList(Wrappers.<RuleExecuteRecord>lambdaQuery()
                        .eq(RuleExecuteRecord::getSymbol, stockCode)
                        .eq(RuleExecuteRecord::getTradeDate, tradeDate)
                        .eq(direction == 1, RuleExecuteRecord::getActionType, 1)
                        .in(direction == 2, RuleExecuteRecord::getActionType, List.of(2, 3))
                        .orderByAsc(RuleExecuteRecord::getTime))
                .stream()
                .map(this::toRuleRecordDTO)
                .toList();
    }

    /**
     * 批量查询指定交易日的一组日 K 记录。
     */
    @DS("market")
    protected Map<String, StockDailyEntity> findDailyMap(LocalDate tradeDate, List<String> stockCodes) {
        if (stockCodes.isEmpty()) {
            return Map.of();
        }
        return stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                        .eq(StockDailyEntity::getTradeDate, tradeDate)
                        .in(StockDailyEntity::getStockCode, stockCodes))
                .stream()
                .collect(Collectors.toMap(StockDailyEntity::getStockCode, Function.identity(), (left, right) -> left));
    }

    /**
     * 查询单日情绪周期记录。
     */
    @DS("trade")
    protected StockEmotionCycleDaily findEmotionCycleDaily(LocalDate tradeDate) {
        return stockEmotionCycleDailyService.getOne(Wrappers.<StockEmotionCycleDaily>lambdaQuery()
                .eq(StockEmotionCycleDaily::getTradeDate, tradeDate)
                .last("LIMIT 1"));
    }

    /**
     * 把情绪周期实体转换为前端日期项 DTO。
     */
    private EmotionCycleCalendarDTO toCalendarDTO(StockEmotionCycleDaily entity) {
        EmotionCycleCalendarDTO dto = new EmotionCycleCalendarDTO();
        dto.setDate(entity.getTradeDate());
        dto.setLimitUpCount(defaultInt(entity.getLimitUpCount()));
        dto.setExplodeCount(defaultInt(entity.getLimitUpBrokenCount()));
        dto.setHighestLimitUp(defaultInt(entity.getHighestConsecutiveLimitUpDays()));
        return dto;
    }

    /**
     * 把日 K 实体转换为股票池行 DTO。
     */
    private Level2StockPoolDTO toStockPoolDTO(StockDailyEntity daily, String scope) {
        Level2StockPoolDTO dto = new Level2StockPoolDTO();
        dto.setCode(daily.getStockCode());
        dto.setSymbolId(SymbolUtil.fastSymbolToInt(daily.getStockCode()));
        dto.setName(daily.getStockName());
        dto.setBoardLabel(boardLabel(daily.getConsecutiveLimitUpDays()));
        dto.setTheme("");
        dto.setStrength(Math.abs(defaultInt(daily.getConsecutiveLimitUpDays())));
        dto.setAmount(formatTurnover(daily.getTurnover()));
        dto.setPrice(daily.getClosePrice());
        dto.setResultRate(daily.getChangeRate());
        dto.setScope(scope);
        fillDailyFields(dto, daily);
        return dto;
    }

    /**
     * 把盯盘任务和当日行情转换为推荐股票池行 DTO。
     */
    private Level2StockPoolDTO toWatchingTaskDTO(StockWatchingTask task, StockDailyEntity daily) {
        Level2StockPoolDTO dto = daily == null ? new Level2StockPoolDTO() : toStockPoolDTO(daily, SCOPE_RECOMMEND);
        dto.setCode(task.getStockCode());
        dto.setSymbolId(SymbolUtil.fastSymbolToInt(task.getStockCode()));
        if (daily == null) {
            dto.setName(task.getStockName());
        }
        dto.setBoardLabel(boardLabel(task.getConsecutiveLimitUpDays()));
        dto.setStrength(defaultInt(task.getLimitUpScore()));
        dto.setScope(SCOPE_RECOMMEND);
        if (daily == null) {
            dto.setTheme("");
            dto.setAmount("0");
            dto.setLbc(task.getConsecutiveLimitUpDays());
        }
        return dto;
    }

    /**
     * 把日 K 实体转换为图表 K 线 DTO。
     */
    private Level2ChartBarDTO toChartBarDTO(StockDailyEntity daily) {
        Level2ChartBarDTO dto = new Level2ChartBarDTO();
        dto.setDate(daily.getTradeDate().toString());
        dto.setTimestamp(daily.getTradeDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        dto.setOpen(daily.getOpenPrice());
        dto.setHigh(daily.getHighPrice());
        dto.setLow(daily.getLowPrice());
        dto.setClose(daily.getClosePrice());
        dto.setPrevClose(daily.getPrevClose());
        dto.setVolume(daily.getVolume());
        dto.setTurnover(daily.getTurnover());
        dto.setTurnoverRate(daily.getTurnoverRate());
        dto.setAmplitude(daily.getAmplitude());
        dto.setChangeRate(daily.getChangeRate());
        dto.setFloatMarketCap(daily.getFloatMarketCap());
        dto.setFloatShares(daily.getFloatShares());
        dto.setKlineState(daily.getKlineState());
        dto.setConsecutiveLimitUpDays(daily.getConsecutiveLimitUpDays());
        return dto;
    }

    /**
     * 把情绪周期实体转换为前端摘要 DTO。
     */
    private EmotionCycleSummaryDTO toEmotionSummaryDTO(LocalDate tradeDate, StockEmotionCycleDaily current, StockEmotionCycleDaily previous) {
        EmotionCycleSummaryDTO dto = new EmotionCycleSummaryDTO();
        dto.setDate(tradeDate.toString());
        dto.setLimitUpCount(current == null ? 0 : defaultInt(current.getLimitUpCount()));
        dto.setYesterdayLimitUpCount(previous == null ? 0 : defaultInt(previous.getLimitUpCount()));
        dto.setConsecutiveLimitUpCount(current == null ? 0 : defaultInt(current.getConsecutiveLimitUpCount()));
        dto.setExplodeCount(current == null ? 0 : defaultInt(current.getLimitUpBrokenCount()));
        dto.setLimitDownCount(current == null ? 0 : defaultInt(current.getLimitDownCount()));
        dto.setHighestLimitUp(current == null ? 0 : defaultInt(current.getHighestConsecutiveLimitUpDays()));
        dto.setConsecutiveRate(rate(dto.getConsecutiveLimitUpCount(), dto.getLimitUpCount()));
        dto.setExplodeRate(rate(dto.getExplodeCount(), dto.getLimitUpCount() + dto.getExplodeCount()));
        dto.setLeaderCode(current == null ? null : current.getDominantCycleStockCode());
        dto.setLeaderName(current == null ? null : current.getDominantCycleStockName());
        return dto;
    }

    /**
     * 把规则执行实体转换为前端回测记录 DTO。
     */
    private RuleRecordDTO toRuleRecordDTO(RuleExecuteRecord entity) {
        RuleRecordDTO dto = new RuleRecordDTO();
        dto.setActionType(entity.getActionType());
        dto.setRuleCode(entity.getRuleCode());
        dto.setSymbol(entity.getSymbol());
        dto.setTime(entity.getTime());
        dto.setLastOrderTime(entity.getLastOrderTime());
        dto.setPrice(entity.getPrice());
        dto.setIncrease(entity.getIncrease());
        dto.setRemark(entity.getRemark());
        return dto;
    }

    /**
     * 把日 K 行情字段填入股票池 DTO。
     */
    private void fillDailyFields(Level2StockPoolDTO dto, StockDailyEntity daily) {
        dto.setTurnover(daily.getTurnover());
        dto.setTurnoverRate(daily.getTurnoverRate());
        dto.setChangeRate(daily.getChangeRate());
        dto.setAmplitude(daily.getAmplitude());
        dto.setVolume(daily.getVolume());
        dto.setKlineState(daily.getKlineState());
        dto.setConsecutiveLimitUpDays(daily.getConsecutiveLimitUpDays());
        dto.setLbc(daily.getConsecutiveLimitUpDays());
        dto.setSt(Boolean.TRUE.equals(daily.getIsSt()) ? 1 : 0);
        dto.setHistorySt(Boolean.TRUE.equals(daily.getIsSt()) ? 1 : 0);
    }

    /**
     * 统一前端股票池范围入参。
     */
    private String normalizeScope(String scope) {
        if (SCOPE_RECOMMEND.equals(scope) || SCOPE_BREAK.equals(scope)) {
            return scope;
        }
        return SCOPE_LIMIT;
    }

    /**
     * 根据连板天数生成前端标签。
     */
    private String boardLabel(Integer consecutiveLimitUpDays) {
        int days = defaultInt(consecutiveLimitUpDays);
        return days == 0 ? "-" : Math.abs(days) + "板";
    }

    /**
     * 把成交额数值转换为前端排序可解析的字符串。
     */
    private String formatTurnover(Double turnover) {
        return turnover == null ? "0" : BigDecimal.valueOf(turnover).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * 计算百分比，分母为 0 时返回 0。
     */
    private double rate(Integer numerator, Integer denominator) {
        if (denominator == null || denominator == 0) {
            return 0D;
        }
        return BigDecimal.valueOf(defaultInt(numerator))
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * 限制列表查询条数，避免前端异常入参造成无界查询。
     */
    private int safeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 200;
        }
        return Math.min(limit, 1000);
    }

    /**
     * 把空整数转换为 0。
     */
    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }
}
