package com.compoundwonder.trader.selection.relay;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compoundwonder.hxdata.entity.StockCurrentStatus;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.service.StockCurrentStatusService;
import com.compoundwonder.hxdata.service.StockConvertibleBondHistoryService;
import com.compoundwonder.hxdata.service.StockDailyService;
import com.compoundwonder.hxdata.service.StockTradeCalendarService;
import com.compoundwonder.trader.selection.TradeMode;
import com.compoundwonder.trader.entity.StockEmotionCycleDaily;
import com.compoundwonder.trader.entity.StockWatchingTask;
import com.compoundwonder.trader.mapper.StockWatchingTaskMapper;
import com.compoundwonder.trader.service.StockEmotionCycleDailyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 连板接力模式独立选股服务。
 */
@Slf4j
@Service
@DS("trade")
public class RelaySelectionService extends ServiceImpl<StockWatchingTaskMapper, StockWatchingTask> {

    /**
     * 常规连板推荐最多保留 4 只，避免回测调参时再次把业务上限误改成 5 只。
     */
    static final int NORMAL_RELAY_TASK_LIMIT = 4;

    /**
     * 唯一弱 5 板属于主观卡位预判，只允许严格过滤后的 2 板候选保留前 3 只。
     */
    static final int WEAK_FIVE_BOARD_FALLBACK_TASK_LIMIT = 3;

    private final StockDailyService stockDailyService;
    private final StockTradeCalendarService stockTradeCalendarService;
    private final StockCurrentStatusService stockCurrentStatusService;
    private final StockEmotionCycleDailyService stockEmotionCycleDailyService;
    private final StockConvertibleBondHistoryService stockConvertibleBondHistoryService;

    /**
     * 创建股票盯盘任务服务。
     * 作用：注入日 K 服务，用于根据日 K 结果生成选股盯盘任务。
     */
    public RelaySelectionService(StockDailyService stockDailyService,
                                        StockTradeCalendarService stockTradeCalendarService,
                                        StockCurrentStatusService stockCurrentStatusService,
                                        StockEmotionCycleDailyService stockEmotionCycleDailyService,
                                        StockConvertibleBondHistoryService stockConvertibleBondHistoryService) {
        this.stockDailyService = stockDailyService;
        this.stockTradeCalendarService = stockTradeCalendarService;
        this.stockCurrentStatusService = stockCurrentStatusService;
        this.stockEmotionCycleDailyService = stockEmotionCycleDailyService;
        this.stockConvertibleBondHistoryService = stockConvertibleBondHistoryService;
    }

    /**
     * 执行连板接力模式选股并替换当天 mode=1 的任务。
     */
    public List<StockWatchingTask> select(LocalDate tradeDate) {
        // 调用可转债正股查询方法。
        Set<String> convertibleBondStockCodes = listConvertibleBondStockCodes(tradeDate);
        // 调用连板选股方法。
        return createRelayLimitUpTasks(tradeDate, convertibleBondStockCodes);
    }

    /**
     * 创建连板接力推荐任务。
     * 实现逻辑：先按照正常情绪周期处理当天非 ST 的 2/3 连板候选；
     * 正常内存候选为空时，再判断是否需要启动唯一弱 5 板的严格 2 板兜底。
     */
    private List<StockWatchingTask> createRelayLimitUpTasks(LocalDate tradeDate,
                                                            Set<String> convertibleBondStockCodes) {
        List<StockDailyEntity> stockDailyEntities = stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getTradeDate, tradeDate)
                .and(wrapper -> wrapper.isNull(StockDailyEntity::getIsSt).or().eq(StockDailyEntity::getIsSt, false))
                .lt(StockDailyEntity::getChangeRate, 11)
                .lt(StockDailyEntity::getFloatMarketCap, 500_000)
                .lt(StockDailyEntity::getClosePrice, 45)
                .between(StockDailyEntity::getConsecutiveLimitUpDays, 2, 3));
        // 调用连板可转债过滤方法。
        stockDailyEntities = filterConvertibleBondStocks("连板", stockDailyEntities, convertibleBondStockCodes);

        // 先查出 今天 昨天 前天 的最高板
        List<StockEmotionCycleDaily> entityList = stockEmotionCycleDailyService.list(Wrappers.<StockEmotionCycleDaily>lambdaQuery()
                .le(StockEmotionCycleDaily::getTradeDate, tradeDate)
                .orderByDesc(StockEmotionCycleDaily::getTradeDate)
                .last("LIMIT 3"));
        if (entityList.size() < 3) {
            return List.of();
        }

        // 今天 昨天 前天 的最高板
        int todayMaxLbc = Objects.requireNonNullElse(entityList.get(0).getHighestConsecutiveLimitUpDays(), 0);
        StockEmotionCycleDaily yesterdayMaxLbc = entityList.get(1);
        StockEmotionCycleDaily yesterdayMaxLbc2 = entityList.get(2);
        int yesterdayHighestLimitUp = Objects.requireNonNullElse(yesterdayMaxLbc.getHighestConsecutiveLimitUpDays(), 0);
        int dayBeforeYesterdayHighestLimitUp = Objects.requireNonNullElse(yesterdayMaxLbc2.getHighestConsecutiveLimitUpDays(), 0);

        Integer minConsecutiveLimitUpDays = null;
        Integer maxConsecutiveLimitUpDays = null;
        // 高度压制到 3 板以下就推荐
        //2.高度压制 2板，推荐2板股票
        //3.高度压制 3板，推荐2/3板股票
        if (todayMaxLbc <= 4) {
            minConsecutiveLimitUpDays = 2;
            maxConsecutiveLimitUpDays = 3;
        } else if (yesterdayHighestLimitUp <= dayBeforeYesterdayHighestLimitUp) {
            //5.连板高度 >=5 板，判断是否是龙头断板第二天，推荐昨天的2板票
            String yesterdayMaxLbc2Code = findHighestLimitUp(yesterdayMaxLbc2.getTradeDate(), dayBeforeYesterdayHighestLimitUp);
            // 如果前天发生大退潮而且高度大于5板，推荐 3 板
            // 判断前天大退潮的时候高度有没有超过7板
            if (dayBeforeYesterdayHighestLimitUp < 7) {
                // 没有超过7板直接推荐三板
                minConsecutiveLimitUpDays = 3;
                maxConsecutiveLimitUpDays = 3;
            }
            if (StrUtil.isNotEmpty(yesterdayMaxLbc2.getDominantCycleStockCode()) && Objects.equals(yesterdayMaxLbc2Code, yesterdayMaxLbc2.getDominantCycleStockCode())) {
                // 超过7板，判断断板的股票是否是占领情绪周期的股票，如果是推荐三板
                minConsecutiveLimitUpDays = 3;
                maxConsecutiveLimitUpDays = 3;
            }
        } else if (todayMaxLbc <= yesterdayHighestLimitUp) {
            String yesterdayMaxLbcCode = findHighestLimitUp(yesterdayMaxLbc.getTradeDate(), yesterdayHighestLimitUp);
            // 6.连板高度 >=5 板，判断是否是龙头断板，推荐今天的3,2板票
            // 如果今天发生退潮而且高度大于5板，推荐 2,3 班
            // 今日高度降低，判断昨日高度是否超过 7 板
            if (yesterdayHighestLimitUp < 7) {
                // 如果昨日高度没有超过7板直接推荐 2,3 板
                minConsecutiveLimitUpDays = 2;
                maxConsecutiveLimitUpDays = 3;
            } else if (StrUtil.isNotEmpty(yesterdayMaxLbc.getDominantCycleStockCode()) && Objects.equals(yesterdayMaxLbcCode, yesterdayMaxLbc.getDominantCycleStockCode())) {
                // 如果昨日高度超过7板，判断是否占领情绪周期，如果占领情绪周期才推荐，为了避免一些高度较高的中位票
                minConsecutiveLimitUpDays = 2;
                maxConsecutiveLimitUpDays = 3;
            }
        }

        int minLimitUpDays = Objects.requireNonNullElse(minConsecutiveLimitUpDays, 0);
        int maxLimitUpDays = Objects.requireNonNullElse(maxConsecutiveLimitUpDays, 0);
        List<StockDailyEntity> selectedStockDailyList = new ArrayList<>();
        for (StockDailyEntity stockDaily : stockDailyEntities) {
            int consecutiveLimitUpDays = Objects.requireNonNullElse(stockDaily.getConsecutiveLimitUpDays(), 0);
            if (minConsecutiveLimitUpDays == null
                    || consecutiveLimitUpDays < minLimitUpDays
                    || consecutiveLimitUpDays > maxLimitUpDays) {
                log.info("连板选股过滤 tradeDate={} stockCode={} stockName={} step=情绪周期板数范围 detail=actual={}, required=[{},{}]",
                        tradeDate, stockDaily.getStockCode(), stockDaily.getStockName(), consecutiveLimitUpDays,
                        minConsecutiveLimitUpDays, maxConsecutiveLimitUpDays);
                continue;
            }
            if (!isIcePointThreeFourBoardCandidate(todayMaxLbc, consecutiveLimitUpDays)
                    && Objects.requireNonNullElse(stockDaily.getClosePrice(), 0D) >= 40D) {
                log.info("连板选股过滤 tradeDate={} stockCode={} stockName={} step=当日价格 detail=actual={}, required<40",
                        tradeDate, stockDaily.getStockCode(), stockDaily.getStockName(), stockDaily.getClosePrice());
                continue;
            }
            selectedStockDailyList.add(stockDaily);
        }
        // 调用连板辅助对象构建方法。
        List<RelaySelectionAssist> assistList = buildSelectionAssistList(selectedStockDailyList);

        // 调用连板共同过滤与评分方法。
        List<StockWatchingTask> eligibleTasks = selectEligibleRelayTasks(
                assistList, todayMaxLbc, true, "连板");
        int taskLimit = NORMAL_RELAY_TASK_LIMIT;
        String selectionMode = "连板";
        List<RelaySelectionAssist> rankingAssistList = assistList;

        /*
         * 唯一弱 5 板是常规选股完全没有内存候选后的兜底，不能在常规流程之前抢跑。
         * 老项目先把任务插库再查询数量；现在任务统一在方法末尾 replaceTasks，
         * 因此直接使用 eligibleTasks.isEmpty() 判断，避免历史旧记录影响当天重跑结果。
        */
        if (eligibleTasks.isEmpty() && todayMaxLbc == 5) {
            // 调用当日非 ST 五板查询方法。
            List<StockDailyEntity> fiveBoardDailyList = listNonStFiveBoardDaily(tradeDate);
            // 调用五板质量快照构建方法。
            List<WeakFiveBoardFallbackPolicy.FiveBoardQuality> fiveBoardQualities =
                    buildFiveBoardQualities(fiveBoardDailyList);
            // 调用弱五板兜底判断方法。
            WeakFiveBoardFallbackPolicy.Decision fallbackDecision =
                    WeakFiveBoardFallbackPolicy.evaluate(todayMaxLbc, false, fiveBoardQualities);

            log.info("弱5板严格2板兜底判断 tradeDate={} triggered={} layer={} detail={}",
                    tradeDate, fallbackDecision.triggered(), fallbackDecision.layer(), fallbackDecision.detail());

            if (fallbackDecision.triggered()) {
                /*
                 * 只选择 2 板做低位卡位预判，不包含 3 板。
                 * 这里必须保留真实市场高度 5，并显式关闭冰点通道：弱 5 板次日仍可能继续涨停，
                 * 如果把它当作 4 板并放宽候选质地，低位票很容易在高位压制下炸板。
                 */
                // 调用弱五板严格二板候选筛选方法。
                List<StockDailyEntity> fallbackDailyList =
                        selectWeakFiveBoardFallbackDailyCandidates(stockDailyEntities);
                // 调用弱五板辅助对象复用构建方法。
                List<RelaySelectionAssist> fallbackAssistList =
                        buildSelectionAssistListReusing(fallbackDailyList, assistList);
                // 调用弱五板严格通道过滤与评分方法。
                eligibleTasks = selectEligibleRelayTasks(
                        fallbackAssistList, todayMaxLbc, false, "弱5板严格2板");
                taskLimit = WEAK_FIVE_BOARD_FALLBACK_TASK_LIMIT;
                selectionMode = "弱5板严格2板";
                rankingAssistList = fallbackAssistList;
            }
        }

        // 调用连板价格索引构建方法。
        Map<String, Double> currentPriceByStockCode = indexCurrentPrices(rankingAssistList);
        // 调用连板候选排序方法。
        sortSelectionTasks(eligibleTasks, currentPriceByStockCode);
        // 调用连板 TopN 截取方法。
        List<StockWatchingTask> tasks = takeTopTasks(selectionMode, eligibleTasks, taskLimit);
        // 调用连板任务替换方法。
        replaceTasks(tradeDate, tasks);
        return tasks;
    }

    /**
     * 执行连板候选共同过滤，并按开关决定是否允许冰点 3/4 板宽松通道。
     *
     * <p>弱 5 板兜底传入 {@code allowIcePoint=false}，其 2 板候选会完整经过
     * 前 20 日异常、一字板、历史换手、近期形态、启动价格、最低评分和筹码过滤，
     * 与正常严格通道保持一致。</p>
     */
    private List<StockWatchingTask> selectEligibleRelayTasks(List<RelaySelectionAssist> assistList,
                                                              int todayMaxLbc,
                                                              boolean allowIcePoint,
                                                              String selectionMode) {
        List<StockWatchingTask> eligibleTasks = new ArrayList<>();
        for (RelaySelectionAssist dto : assistList) {
            log.info("{}------:{}:{}", selectionMode, calculateSelectionScore(dto), dto);

            int priorTwentyDayAbnormalCount = Objects.requireNonNullElse(
                    dto.getPriorTwentyDayAbnormalKlineStateCount(), 0);
            if (!isRecentAbnormalKlineCountAllowed(priorTwentyDayAbnormalCount)) {
                logSelectionFiltered(selectionMode, dto, "前20日非正常K线次数",
                        "actual=" + priorTwentyDayAbnormalCount + ", required<4");
                continue;
            }

            if (dto.isTwoAcceleratedShrinkVolumeLimitUps()) {
                logSelectionFiltered(selectionMode, dto, "加速缩量板",
                        "本轮至少2根加速缩量板：首板判断一字板或振幅<3%，后续板增加换手率<15%");
                continue;
            }

            // 调用连板九十日历史换手过滤方法。
            RelayChipFilter.Decision ninetyDayTurnoverDecision =
                    RelayChipFilter.evaluateRelayNinetyDayTurnoverLimit(dto);
            if (!ninetyDayTurnoverDecision.passed()) {
                logSelectionFiltered(selectionMode, dto,
                        "连板筹码过滤-" + ninetyDayTurnoverDecision.layer(),
                        ninetyDayTurnoverDecision.detail());
                continue;
            }

            double maxTurnoverRate = Objects.requireNonNullElse(dto.getMaxTurnoverRate(), 0D);
            int nonStMonthCount = Objects.requireNonNullElse(dto.getNonStMonthCount(), 0);
            int listingMonthCount = Objects.requireNonNullElse(dto.getListingMonthCount(), 0);
            if (maxTurnoverRate <= 25 && nonStMonthCount < 18 && nonStMonthCount < listingMonthCount) {
                logSelectionFiltered(selectionMode, dto, "历史换手与非ST月份", "maxTurnoverRate=" + maxTurnoverRate
                        + ", nonStMonthCount=" + nonStMonthCount + ", listingMonthCount=" + listingMonthCount);
                continue;
            }
            int consecutiveLimitUpDays = Objects.requireNonNullElse(dto.getConsecutiveLimitUpDays(), 2);

            // 调用连板近期形态过滤方法。
            RelayRecentPatternFilter.Decision recentPatternDecision = RelayRecentPatternFilter.evaluate(dto);
            if (!recentPatternDecision.passed()) {
                logSelectionFiltered(selectionMode, dto,
                        consecutiveLimitUpDays + "连板近期形态-" + recentPatternDecision.layer(),
                        recentPatternDecision.detail());
                continue;
            }

            if (allowIcePoint && isIcePointThreeFourBoardCandidate(todayMaxLbc, consecutiveLimitUpDays)) {
                // 调用冰点三四板宽松通道过滤方法。
                IcePointThreeFourBoardFilter.Decision icePointDecision = IcePointThreeFourBoardFilter.evaluate(dto);
                if (!icePointDecision.passed()) {
                    logSelectionFiltered("冰点3/4板", dto,
                            "冰点3/4板-" + icePointDecision.layer(), icePointDecision.detail());
                    continue;
                }

                // 调用连板评分方法。
                int icePointScore = calculateSelectionScore(dto);
                // 调用连板任务构建方法。
                eligibleTasks.add(buildWatchingTask(dto, icePointScore));
                log.info("冰点3/4板入选 tradeDate={} stockCode={} stockName={} score={} layer={} detail={}",
                        dto.getTradeDate(), dto.getStockCode(), dto.getStockName(), icePointScore,
                        icePointDecision.layer(), icePointDecision.detail());
                continue;
            }

            double startPrice = Objects.requireNonNullElse(dto.getStartPrice(), 0D);
            Double startMarketCap = dto.getStartMarketCap();
            if (startPrice <= 3 && startMarketCap < 250_000) {
                logSelectionFiltered(selectionMode, dto, "启动价格", "actual=" + startPrice + ", required>3");
                continue;
            }

            // 调用连板评分方法。
            int score = calculateSelectionScore(dto);
            if (score < 15) {
                logSelectionFiltered(selectionMode, dto, "选股评分", "actual=" + score + ", required>=15");
                continue;
            }
            // 调用连板筹码过滤方法。
            RelayChipFilter.Decision chipDecision = RelayChipFilter.evaluate(dto);
            if (!chipDecision.passed()) {
                logSelectionFiltered(selectionMode, dto,
                        "筹码过滤-" + chipDecision.layer(), chipDecision.detail());
                continue;
            }
            // 调用连板任务构建方法。
            eligibleTasks.add(buildWatchingTask(dto, score));
        }
        return eligibleTasks;
    }

    /**
     * 查询当天过滤 ST 后的全部 5 板股票，不添加涨幅范围条件。
     * 这里统计的是市场 5 板数量，不受可转债候选过滤影响；is_st 为空仍按非 ST 处理。
     */
    private List<StockDailyEntity> listNonStFiveBoardDaily(LocalDate tradeDate) {
        return stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getTradeDate, tradeDate)
                .and(wrapper -> wrapper.isNull(StockDailyEntity::getIsSt)
                        .or().eq(StockDailyEntity::getIsSt, false))
                .eq(StockDailyEntity::getConsecutiveLimitUpDays, 5));
    }

    /**
     * 构建 5 板质量快照。只有恰好一只 5 板时才计算完整辅助对象，
     * 避免在数量条件已经失败时执行多余的历史日 K 查询。
     */
    private List<WeakFiveBoardFallbackPolicy.FiveBoardQuality> buildFiveBoardQualities(
            List<StockDailyEntity> fiveBoardDailyList) {
        if (fiveBoardDailyList == null || fiveBoardDailyList.isEmpty()) {
            return List.of();
        }
        if (fiveBoardDailyList.size() != 1) {
            return fiveBoardDailyList.stream()
                    .map(daily -> toFiveBoardQuality(daily, null))
                    .toList();
        }

        StockDailyEntity fiveBoardDaily = fiveBoardDailyList.get(0);
        RelaySelectionAssist fiveBoardAssist = buildSelectionAssist(fiveBoardDaily);
        return List.of(toFiveBoardQuality(fiveBoardDaily, fiveBoardAssist));
    }

    /**
     * 组合 5 板质量数据：当日市值、换手和振幅取当天日 K；
     * 启动价格必须取选股辅助对象中的本轮首板前一交易日收盘价，不能再用收盘价除以 1.6 估算。
     */
    static WeakFiveBoardFallbackPolicy.FiveBoardQuality toFiveBoardQuality(
            StockDailyEntity fiveBoardDaily,
            RelaySelectionAssist fiveBoardAssist) {
        return new WeakFiveBoardFallbackPolicy.FiveBoardQuality(
                fiveBoardDaily == null ? null : fiveBoardDaily.getStockCode(),
                fiveBoardDaily == null ? null : fiveBoardDaily.getFloatMarketCap(),
                fiveBoardDaily == null ? null : fiveBoardDaily.getTurnoverRate(),
                fiveBoardDaily == null ? null : fiveBoardDaily.getAmplitude(),
                fiveBoardAssist == null ? null : fiveBoardAssist.getStartPrice());
    }

    /**
     * 弱 5 板兜底只允许 2 板股票，并继续使用正常严格通道的收盘价上限 40 元。
     * 传入列表已经完成非 ST、可转债、基础市值和基础价格查询过滤。
     */
    static List<StockDailyEntity> selectWeakFiveBoardFallbackDailyCandidates(
            List<StockDailyEntity> stockDailyEntities) {
        if (stockDailyEntities == null || stockDailyEntities.isEmpty()) {
            return List.of();
        }
        return stockDailyEntities.stream()
                .filter(daily -> Integer.valueOf(2).equals(daily.getConsecutiveLimitUpDays()))
                .filter(daily -> Objects.requireNonNullElse(daily.getClosePrice(), 0D) < 40D)
                .toList();
    }

    /**
     * 弱 5 板兜底是少数场景：优先复用常规流程已经构建的辅助对象，
     * 只为尚未参与常规板数范围的 2 板补充查询，避免多年回测中重复计算历史指标。
     */
    private List<RelaySelectionAssist> buildSelectionAssistListReusing(
            List<StockDailyEntity> stockDailyList,
            List<RelaySelectionAssist> reusableAssistList) {
        Map<String, RelaySelectionAssist> reusableAssistByCode = new HashMap<>();
        for (RelaySelectionAssist assist : reusableAssistList) {
            reusableAssistByCode.put(assist.getStockCode(), assist);
        }

        List<RelaySelectionAssist> result = new ArrayList<>();
        for (StockDailyEntity stockDaily : stockDailyList) {
            RelaySelectionAssist assist = reusableAssistByCode.get(stockDaily.getStockCode());
            result.add(assist == null ? buildSelectionAssist(stockDaily) : assist);
        }
        return result;
    }

    /**
     * 市场当日最高板为 3 板或 4 板时，所有 2、3 连板候选启用冰点宽松通道。
     */
    static boolean isIcePointThreeFourBoardCandidate(int todayHighestLimitUp,
                                                      Integer consecutiveLimitUpDays) {
        boolean icePointMarket = todayHighestLimitUp == 3 || todayHighestLimitUp == 4;
        boolean relayCandidate = Integer.valueOf(2).equals(consecutiveLimitUpDays)
                || Integer.valueOf(3).equals(consecutiveLimitUpDays);
        return icePointMarket && relayCandidate;
    }

    /**
     * 候选任务排序：优先按分数降序；分数相同时按选股当日收盘价升序；
     * 分数和价格都相同时按股票代码升序，保证截断结果稳定。
     */
    static void sortSelectionTasks(List<StockWatchingTask> tasks,
                                   Map<String, Double> currentPriceByStockCode) {
        tasks.sort(Comparator
                .comparing(StockWatchingTask::getLimitUpScore,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(task -> currentPriceByStockCode.get(task.getStockCode()),
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(StockWatchingTask::getStockCode,
                        Comparator.nullsLast(Comparator.naturalOrder())));
    }

    /**
     * 建立股票代码到选股当日收盘价的索引，供候选任务同分排序使用。
     */
    private Map<String, Double> indexCurrentPrices(List<RelaySelectionAssist> assistList) {
        Map<String, Double> currentPriceByStockCode = new HashMap<>();
        for (RelaySelectionAssist assist : assistList) {
            currentPriceByStockCode.put(assist.getStockCode(), assist.getCurrentPrice());
        }
        return currentPriceByStockCode;
    }

    /**
     * 按分数顺序保留指定数量任务，并记录因数量上限被过滤的股票。
     */
    private List<StockWatchingTask> takeTopTasks(String selectionMode,
                                                 List<StockWatchingTask> sortedTasks,
                                                 int limit) {
        List<StockWatchingTask> selectedTasks = new ArrayList<>();
        for (int i = 0; i < sortedTasks.size(); i++) {
            StockWatchingTask task = sortedTasks.get(i);
            if (i >= limit) {
                log.info("{}选股过滤 tradeDate={} stockCode={} stockName={} step=数量上限 detail=rank={}, limit={}, score={}",
                        selectionMode, task.getRecommendDate(), task.getStockCode(), task.getStockName(),
                        i + 1, limit, task.getLimitUpScore());
                continue;
            }
            selectedTasks.add(task);
        }
        return selectedTasks;
    }

    /**
     * 使用循环排除有可转债的股票，并记录过滤原因。
     */
    private List<StockDailyEntity> filterConvertibleBondStocks(String selectionMode,
                                                               List<StockDailyEntity> stockDailyList,
                                                               Set<String> convertibleBondStockCodes) {
        List<StockDailyEntity> selectedStockDailyList = new ArrayList<>();
        for (StockDailyEntity stockDaily : stockDailyList) {
            if (convertibleBondStockCodes.contains(stockDaily.getStockCode())) {
                log.info("{}选股过滤 tradeDate={} stockCode={} stockName={} step=可转债 detail=当天存在有效可转债",
                        selectionMode, stockDaily.getTradeDate(), stockDaily.getStockCode(), stockDaily.getStockName());
                continue;
            }
            selectedStockDailyList.add(stockDaily);
        }
        return selectedStockDailyList;
    }

    /**
     * 记录辅助对象在选股流程中被过滤的具体步骤和指标值。
     */
    private void logSelectionFiltered(String selectionMode,
                                      RelaySelectionAssist dto,
                                      String step,
                                      String detail) {
        log.info("{}选股过滤 tradeDate={} stockCode={} stockName={} step={} detail={}",
                selectionMode, dto.getTradeDate(), dto.getStockCode(), dto.getStockName(), step, detail);
    }

    /**
     * 查询指定日期指定最高板的非 ST 股票代码，用于判断情绪周期龙头是否断板。
     * is_st 为空表示历史 ST 状态未记录，按非 ST 候选处理，与选股主查询保持一致。
     */
    private String findHighestLimitUp(LocalDate tradeDate, Integer highestLimitUp) {
        StockDailyEntity stockDaily = stockDailyService.getOne(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getTradeDate, tradeDate)
                .and(wrapper -> wrapper.isNull(StockDailyEntity::getIsSt).or().eq(StockDailyEntity::getIsSt, false))
                .eq(StockDailyEntity::getConsecutiveLimitUpDays, highestLimitUp)
                .orderByDesc(StockDailyEntity::getChangeRate)
                .last("LIMIT 1"));
        return stockDaily == null ? null : stockDaily.getStockCode();
    }

    /**
     * 查询指定日期当天有有效可转债的正股代码，供所有选股候选池排除。
     */
    private Set<String> listConvertibleBondStockCodes(LocalDate tradeDate) {
        return stockConvertibleBondHistoryService.listTradableStockCodes(tradeDate);
    }

    /**
     * 根据选股辅助对象构建盯盘任务。
     */
    private StockWatchingTask buildWatchingTask(RelaySelectionAssist assist, Integer limitUpScore) {
        StockWatchingTask task = new StockWatchingTask();
        task.setStockCode(assist.getStockCode());
        task.setStockName(assist.getStockName());
        task.setLimitUpScore(limitUpScore);
        task.setConsecutiveLimitUpDays(assist.getConsecutiveLimitUpDays());
        task.setRecommendDate(assist.getTradeDate());
        task.setTradeDate(findNextTradeDate(assist.getTradeDate()));
        task.setTradeMode(TradeMode.RELAY_LIMIT_UP.code());
        task.setCreatedTime(LocalDateTime.now());
        return task;
    }

    /**
     * 补跑时先删除同一天同模式旧任务，再写入新任务，避免重复推荐。
     */
    private void replaceTasks(LocalDate recommendDate, List<StockWatchingTask> tasks) {
        remove(Wrappers.<StockWatchingTask>lambdaQuery()
                .eq(StockWatchingTask::getRecommendDate, recommendDate)
                .eq(StockWatchingTask::getTradeMode, TradeMode.RELAY_LIMIT_UP.code()));
        if (!tasks.isEmpty()) {
            saveBatch(tasks);
        }
    }

    /**
     * 按启动市值、历史最大换手、启动价格、地域板块、选股当日换手率计算基础分，
     * 以 27 亿启动流通市值为基准动态计算非正常状态免扣次数，最终分数不低于 0 分。
     */
    private Integer calculateSelectionScore(RelaySelectionAssist assist) {
        int score = scoreStartMarketCap(assist.getStartMarketCap())
                + scoreMaxTurnover(assist.getMaxTurnoverRate())
                + scoreStartPrice(assist.getStartPrice())
                + scoreProvince(assist.getProvince())
                + scoreCurrentTurnover(assist.getCurrentTurnoverRate())
                + scoreConsecutiveLimitUpDays(assist.getConsecutiveLimitUpDays());
        int abnormalCount = Objects.requireNonNullElse(assist.getAbnormalKlineStateCount(), 0);
        int noDeductionCount = calculateAbnormalNoDeductionCount(assist.getStartMarketCap());
        return Math.max(0, score - Math.max(0, abnormalCount - noDeductionCount));
    }

    /**
     * 根据启动流通市值计算非正常状态免扣次数：27 亿减去启动市值四舍五入后的亿数。
     * 启动市值达到或超过 27 亿时免扣次数为 0，异常次数全部参与扣分。
     */
    private int calculateAbnormalNoDeductionCount(Double startMarketCap) {
        if (startMarketCap == null) {
            return 0;
        }
        int startMarketCapInYi = (int) Math.round(startMarketCap / 10000D);
        return Math.max(0, 27 - startMarketCapInYi);
    }

    /**
     * 启动市值评分：单位为万元，8.1 亿以内 30 分，超过 20 亿 0 分，中间区间线性扣分。
     */
    private int scoreStartMarketCap(Double marketCap) {
        if (marketCap == null || marketCap > 200000) return 0;
        if (marketCap <= 81000) return 30;
        if (marketCap <= 95000) return interpolate(marketCap, 81000, 95000, 30, 20);
        if (marketCap <= 150000) return interpolate(marketCap, 95000, 150000, 20, 10);
        return interpolate(marketCap, 150000, 200000, 10, 0);
    }

    /**
     * 历史最大换手评分：15% 以内 25 分，超过 55% 0 分，中间区间按区间端点线性扣分。
     */
    private int scoreMaxTurnover(Double turnoverRate) {
        if (turnoverRate == null || turnoverRate > 55) return 0;
        if (turnoverRate <= 15) return 25;
        if (turnoverRate <= 25) return interpolate(turnoverRate, 15, 25, 25, 20);
        if (turnoverRate <= 37.5) return interpolate(turnoverRate, 25, 37.5, 20, 15);
        return interpolate(turnoverRate, 37.5, 55, 15, 0);
    }

    /**
     * 启动价格评分：低于 3.3 元或超过 19.5 元得 0 分；3.3 至 4 元得 10 分；
     * 4 至 10 元得 15 分；10 至 19.5 元按现有分段线性扣分。
     */
    private int scoreStartPrice(Double price) {
        if (price == null || price < 3.3 || price > 19.5) return 0;
        if (price <= 4) return 10;
        if (price <= 10) return 15;
        if (price <= 12.5) return interpolate(price, 10, 12.5, 15, 10);
        if (price <= 15.5) return interpolate(price, 12.5, 15.5, 10, 7);
        return interpolate(price, 15.5, 19.5, 7, 0);
    }

    /**
     * 地域板块评分：江浙粤沪深 10 分，中部活跃省份 7 分，指定弱偏好省份 3 分，其余 0 分。
     */
    private int scoreProvince(String province) {
        if (province == null || province.length() < 2) return 0;
        String provincePrefix = province.substring(0, 2);
        if (List.of("江苏", "浙江", "广东", "上海", "深圳").contains(provincePrefix)) return 10;
        if (List.of("山东", "湖南", "湖北", "安徽").contains(provincePrefix)) return 7;
        if (List.of("吉林", "辽宁", "黑龙江", "四川").contains(provincePrefix)) return 3;
        return 0;
    }

    /**
     * 选股当日换手率评分：17% 以内 10 分；较高换手按 7 至 2 分递减，超过 55% 视为爆量 0 分。
     */
    private int scoreCurrentTurnover(Double turnoverRate) {
        if (turnoverRate == null || turnoverRate > 55) return 0;
        if (turnoverRate <= 17) return 10;
        if (turnoverRate <= 30) return 7;
        return interpolate(turnoverRate, 30, 55, 7, 2);
    }

    /**
     * 连板评分：2 板加 5 分，3 板加 15 分，其他板数不加分。
     */
    private int scoreConsecutiveLimitUpDays(Integer consecutiveLimitUpDays) {
        if (consecutiveLimitUpDays == 2) return 5;
        if (consecutiveLimitUpDays == 3) return 15;
        return 0;
    }

    /**
     * 对一个连续指标按给定边界做线性插值，并将结果四舍五入为整数分。
     */
    private int interpolate(double value, double min, double max, int minScore, int maxScore) {
        return (int) Math.round(minScore + (value - min) * (maxScore - minScore) / (max - min));
    }

    /**
     * 推荐日收盘后生成的任务，在下一交易日盯盘。
     */
    private LocalDate findNextTradeDate(LocalDate recommendDate) {
        LocalDate tradeDate = recommendDate.plusDays(1);
        for (int i = 0; i < 15; i++) {
            if (stockTradeCalendarService.isTradeDay(tradeDate)) {
                return tradeDate;
            }
            tradeDate = tradeDate.plusDays(1);
        }
        return recommendDate.plusDays(1);
    }

    /**
     * 把涨停日 K 候选股填充为选股辅助对象，后续过滤和打分都基于该对象扩展。
     */
    private List<RelaySelectionAssist> buildSelectionAssistList(List<StockDailyEntity> stockDailyList) {
        return stockDailyList.stream()
                .map(this::buildSelectionAssist)
                .toList();
    }

    /**
     * 填充单只股票的选股辅助字段。
     */
    private RelaySelectionAssist buildSelectionAssist(StockDailyEntity stockDaily) {
        List<StockDailyEntity> recentDailyList = listRecentDaily(stockDaily);
        List<StockDailyEntity> selectionWindowDailyList = listSelectionWindowDaily(stockDaily);
        List<StockDailyEntity> ascRecentDailyList = recentDailyList.stream()
                .sorted(Comparator.comparing(StockDailyEntity::getTradeDate))
                .toList();
        StockDailyEntity startDaily = findStartDaily(
                recentDailyList, stockDaily.getConsecutiveLimitUpDays());
        LocalDate chipHistoryEndDate = startDaily == null ? null : startDaily.getTradeDate();
        // 调用连板筹码历史查询方法。
        List<StockDailyEntity> chipHistoryDailyList =
                listChipHistoryDaily(stockDaily.getStockCode(), chipHistoryEndDate);
        // 调用连板最早日 K 查询方法。
        List<StockDailyEntity> earliestStoredDailyList =
                listEarliestStoredDaily(stockDaily.getStockCode(), chipHistoryEndDate);
        // 调用连板历史筹码指标计算方法。
        RelayChipFilter.HistoricalMetrics chipMetrics = RelayChipFilter.calculateHistoricalMetrics(
                chipHistoryDailyList, earliestStoredDailyList, chipHistoryEndDate);

        RelaySelectionAssist assist = new RelaySelectionAssist();
        assist.setStockCode(stockDaily.getStockCode());
        assist.setStockName(stockDaily.getStockName());
        assist.setTradeDate(stockDaily.getTradeDate());
        assist.setConsecutiveLimitUpDays(stockDaily.getConsecutiveLimitUpDays());
        assist.setTwoAcceleratedShrinkVolumeLimitUps(hasAtLeastTwoAcceleratedShrinkVolumeLimitUps(
                recentDailyList, stockDaily.getConsecutiveLimitUpDays()));
        assist.setProvince(findProvince(stockDaily.getStockCode()));
        assist.setCurrentPrice(stockDaily.getClosePrice());
        assist.setStartMarketCap(startDaily == null ? null : startDaily.getFloatMarketCap());
        assist.setStartPrice(startDaily == null ? null : startDaily.getClosePrice());
        assist.setCurrentTurnoverRate(stockDaily.getTurnoverRate());
        assist.setCurrentTurnover(stockDaily.getTurnover());
        assist.setCurrentAmplitude(stockDaily.getAmplitude());
        assist.setNonStMonthCount(calculateNonStMonthCount(stockDaily));
        assist.setListingMonthCount(calculateListingMonthCount(stockDaily));
        assist.setMaxTurnoverRate(chipMetrics.maxTurnoverRate());
        assist.setHistoricalMaxVolume(chipMetrics.maxVolume());
        assist.setMaxVolumeDayTurnoverRate(chipMetrics.maxVolumeDayTurnoverRate());
        assist.setMaxVolumeDayTurnover(chipMetrics.maxVolumeDayTurnover());
        assist.setHighestConsecutiveLimitUpDays(chipMetrics.twoHundredKlineHighestBoard());
        assist.setPriorNinetyDayHighestConsecutiveLimitUpDays(chipMetrics.ninetyDayHighestBoard());
        assist.setPriorNinetyDayMaxTurnoverRate(chipMetrics.ninetyDayMaxTurnoverRate());
        assist.setAbnormalKlineStateCount(countAbnormalKlineState(selectionWindowDailyList, stockDaily.getConsecutiveLimitUpDays()));
        assist.setPriorTwentyDayAbnormalKlineStateCount(
                countPriorTwentyDayAbnormalKlineState(
                        recentDailyList, stockDaily.getConsecutiveLimitUpDays()));
        assist.setFiveDayAmplitude(calculateSelectionAdjustedAmplitude(
                ascRecentDailyList, stockDaily.getConsecutiveLimitUpDays()));
        assist.setTenDayChangeRate(calculateAdjustedCloseChangeRate(ascRecentDailyList, 10));
        return assist;
    }

    /**
     * 查询当前交易日及之前最多 22 个交易日：最多跳过本轮 3 个连板日后，再统计前 20 个交易日。
     */
    private List<StockDailyEntity> listRecentDaily(StockDailyEntity stockDaily) {
        return stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getStockCode, stockDaily.getStockCode())
                .le(StockDailyEntity::getTradeDate, stockDaily.getTradeDate())
                .orderByDesc(StockDailyEntity::getTradeDate)
                .last("LIMIT 23"));
    }

    /**
     * 查询选股日期往前 18 个自然月内的日 K，用于计算过滤和打分辅助指标。
     */
    private List<StockDailyEntity> listSelectionWindowDaily(StockDailyEntity stockDaily) {
        return stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getStockCode, stockDaily.getStockCode())
                .ge(StockDailyEntity::getTradeDate, stockDaily.getTradeDate().minusMonths(18))
                .le(StockDailyEntity::getTradeDate, stockDaily.getTradeDate())
                .orderByDesc(StockDailyEntity::getTradeDate));
    }

    /**
     * 查询本轮首板前最近 200 根日 K，供独立筹码过滤器计算历史指标。
     * 其他异常状态、评分辅助指标仍使用原来的 18 个自然月窗口。
     */
    private List<StockDailyEntity> listChipHistoryDaily(String stockCode, LocalDate historyEndDate) {
        if (stockCode == null || historyEndDate == null) {
            return List.of();
        }
        return stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getStockCode, stockCode)
                .le(StockDailyEntity::getTradeDate, historyEndDate)
                .orderByDesc(StockDailyEntity::getTradeDate)
                .last("LIMIT 200"));
    }

    /**
     * 查询数据库中该股票最早的 11 根日 K，用第 11 根确定历史筹码统计的首个有效交易日。
     * 这样只排除新股上市最早 10 根日 K，不会误删老股票最近 200 根筹码窗口的数据。
     */
    private List<StockDailyEntity> listEarliestStoredDaily(String stockCode, LocalDate historyEndDate) {
        if (stockCode == null || historyEndDate == null) {
            return List.of();
        }
        return stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .select(StockDailyEntity::getTradeDate)
                .eq(StockDailyEntity::getStockCode, stockCode)
                .le(StockDailyEntity::getTradeDate, historyEndDate)
                .orderByAsc(StockDailyEntity::getTradeDate)
                .last("LIMIT 11"));
    }

    /**
     * 查询省份属性。
     */
    private String findProvince(String stockCode) {
        StockCurrentStatus status = stockCurrentStatusService.getOne(Wrappers.<StockCurrentStatus>lambdaQuery()
                .eq(StockCurrentStatus::getStockCode, stockCode)
                .last("LIMIT 1"));
        return status == null ? null : status.getRegionName();
    }

    /**
     * 定位本轮首板前一交易日，用于读取启动市值和启动价格。
     * 日 K 按交易日倒序排列，下标等于当前连板数：1 板取下标 1，2 板取下标 2，以此类推。
     */
    private StockDailyEntity findStartDaily(List<StockDailyEntity> recentDailyList, Integer consecutiveLimitUpDays) {
        int startIndex = Math.max(1, Objects.requireNonNullElse(consecutiveLimitUpDays, 1));
        return recentDailyList.size() <= startIndex ? null : recentDailyList.get(startIndex);
    }

    /**
     * 按当前连板数回看本轮涨停日，判断是否至少有两根加速缩量板。
     *
     * <p>本轮首板只判断 {@code klineState == 3} 的一字板或振幅严格小于 3%，
     * 不使用换手率条件；第 2/3 板满足一字板、振幅严格小于 3%、换手率严格
     * 小于 15% 中的任意一项即命中。因此 2 板要求两根都命中，3 板要求三根中
     * 至少两根命中。</p>
     */
    static boolean hasAtLeastTwoAcceleratedShrinkVolumeLimitUps(
            List<StockDailyEntity> recentDailyList,
            Integer consecutiveLimitUpDays) {
        if (recentDailyList == null) {
            return false;
        }
        int consecutiveDays = Math.max(0, Objects.requireNonNullElse(consecutiveLimitUpDays, 0));
        int count = 0;
        for (int i = 0; i < consecutiveDays && i < recentDailyList.size(); i++) {
            StockDailyEntity stockDailyEntity = recentDailyList.get(i);
            Double amplitude = stockDailyEntity.getAmplitude();
            Double turnoverRate = stockDailyEntity.getTurnoverRate();
            boolean firstBoardOfCurrentRun = i == consecutiveDays - 1;
            if (Objects.equals(stockDailyEntity.getKlineState(), 3)
                    || (amplitude != null && amplitude < 3D)
                    || (!firstBoardOfCurrentRun && turnoverRate != null && turnoverRate < 15D)) {
                if (++count >= 2) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 计算上次 ST 后或新上市以来的自然月数。
     */
    private Integer calculateNonStMonthCount(StockDailyEntity stockDaily) {
        StockDailyEntity lastStDaily = stockDailyService.getOne(Wrappers.<StockDailyEntity>lambdaQuery()
                .select(StockDailyEntity::getTradeDate)
                .eq(StockDailyEntity::getStockCode, stockDaily.getStockCode())
                .lt(StockDailyEntity::getTradeDate, stockDaily.getTradeDate())
                .eq(StockDailyEntity::getIsSt, true)
                .orderByDesc(StockDailyEntity::getTradeDate)
                .last("LIMIT 1"));
        StockDailyEntity firstDaily = stockDailyService.getOne(Wrappers.<StockDailyEntity>lambdaQuery()
                .select(StockDailyEntity::getTradeDate)
                .eq(StockDailyEntity::getStockCode, stockDaily.getStockCode())
                .le(StockDailyEntity::getTradeDate, stockDaily.getTradeDate())
                .orderByAsc(StockDailyEntity::getTradeDate)
                .last("LIMIT 1"));
        LocalDate startDate = lastStDaily == null ? firstDaily == null ? null : firstDaily.getTradeDate() : lastStDaily.getTradeDate().plusDays(1);
        if (startDate == null) {
            return null;
        }
        return Math.toIntExact(ChronoUnit.MONTHS.between(startDate.withDayOfMonth(1), stockDaily.getTradeDate().withDayOfMonth(1)));
    }

    /**
     * 计算股票从最早日 K 交易日到选股日期的自然月数。
     */
    private Integer calculateListingMonthCount(StockDailyEntity stockDaily) {
        StockDailyEntity firstDaily = stockDailyService.getOne(Wrappers.<StockDailyEntity>lambdaQuery()
                .select(StockDailyEntity::getTradeDate)
                .eq(StockDailyEntity::getStockCode, stockDaily.getStockCode())
                .le(StockDailyEntity::getTradeDate, stockDaily.getTradeDate())
                .orderByAsc(StockDailyEntity::getTradeDate)
                .last("LIMIT 1"));
        if (firstDaily == null || firstDaily.getTradeDate() == null) {
            return null;
        }
        return Math.toIntExact(ChronoUnit.MONTHS.between(
                firstDaily.getTradeDate().withDayOfMonth(1),
                stockDaily.getTradeDate().withDayOfMonth(1)));
    }

    /**
     * 统计选股窗口内非正常 K 线状态次数。
     */
    /**
     * 统计 18 个月窗口内的非正常状态次数，并减去本次连板数。
     */
    private Integer countAbnormalKlineState(List<StockDailyEntity> selectionWindowDailyList, Integer consecutiveLimitUpDays) {
        int currentConsecutiveDays = Math.max(0, Objects.requireNonNullElse(consecutiveLimitUpDays, 0));
        long abnormalCount = selectionWindowDailyList.stream()
                .map(StockDailyEntity::getKlineState)
                .filter(Objects::nonNull)
                .filter(klineState -> klineState != 0)
                .count();
        return Math.max(0, Math.toIntExact(abnormalCount) - currentConsecutiveDays);
    }

    /**
     * 统计本轮连续涨停开始前 20 个交易日中 klineState != 0 的日 K 数量。
     * recentDailyList 按交易日倒序排列，先按当前连板数跳过本轮涨停日，再读取之前 20 根日 K。
     */
    static int countPriorTwentyDayAbnormalKlineState(
            List<StockDailyEntity> recentDailyList, Integer consecutiveLimitUpDays) {
        int currentConsecutiveDays = Math.max(0, Objects.requireNonNullElse(consecutiveLimitUpDays, 0));
        return Math.toIntExact(recentDailyList.stream()
                .skip(currentConsecutiveDays)
                .limit(20)
                .map(StockDailyEntity::getKlineState)
                .filter(Objects::nonNull)
                .filter(klineState -> klineState != 0)
                .count());
    }

    /**
     * 前 20 个交易日非正常 K 线少于 4 次才允许进入后续筛选。
     */
    static boolean isRecentAbnormalKlineCountAllowed(Integer abnormalCount) {
        return Objects.requireNonNullElse(abnormalCount, 0) < 4;
    }

    /**
     * 使用复权收盘价计算 N 日涨跌幅。
     */
    private Double calculateAdjustedCloseChangeRate(List<StockDailyEntity> ascRecentDailyList, int days) {
        if (ascRecentDailyList.size() <= days) {
            return 0.0;
        }
        Double basePrice = ascRecentDailyList.get(ascRecentDailyList.size() - 1 - days).getAdjustClosePrice();
        Double currentPrice = ascRecentDailyList.get(ascRecentDailyList.size() - 1).getAdjustClosePrice();
        if (basePrice == null || currentPrice == null || basePrice == 0) {
            return 0.0;
        }
        return (currentPrice - basePrice) * 100 / basePrice;
    }

    /**
     * 计算包含当日在内的选股振幅：首板使用 3 个交易日，连板使用 5 个交易日。
     * 公式为（当日复权收盘价 - 窗口最低复权价）/ 窗口最低复权价。
     */
    static Double calculateSelectionAdjustedAmplitude(
            List<StockDailyEntity> ascRecentDailyList, Integer consecutiveLimitUpDays) {
        int windowDays = Objects.equals(consecutiveLimitUpDays, 1) ? 3 : 5;
        if (ascRecentDailyList.size() < windowDays) {
            return 0.0;
        }
        int currentIndex = ascRecentDailyList.size() - 1;
        Double currentClosePrice = ascRecentDailyList.get(currentIndex).getAdjustClosePrice();
        Double lowestPrice = ascRecentDailyList.subList(
                        ascRecentDailyList.size() - windowDays, ascRecentDailyList.size())
                .stream()
                .map(StockDailyEntity::getAdjustLowPrice)
                .filter(Objects::nonNull)
                .min(Double::compareTo)
                .orElse(null);
        if (currentClosePrice == null || lowestPrice == null || lowestPrice <= 0) {
            return 0.0;
        }
        return (currentClosePrice - lowestPrice) * 100 / lowestPrice;
    }
}
