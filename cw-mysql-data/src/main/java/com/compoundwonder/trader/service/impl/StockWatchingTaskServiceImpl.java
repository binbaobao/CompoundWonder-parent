package com.compoundwonder.trader.service.impl;

import cn.hutool.core.util.ArrayUtil;
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
import com.compoundwonder.trader.dto.StockSelectionAssistDTO;
import com.compoundwonder.trader.entity.StockEmotionCycleDaily;
import com.compoundwonder.trader.entity.StockWatchingTask;
import com.compoundwonder.trader.mapper.StockWatchingTaskMapper;
import com.compoundwonder.trader.service.StockEmotionCycleDailyService;
import com.compoundwonder.trader.service.StockWatchingTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 股票盯盘任务服务实现。
 */
@Slf4j
@Service
@DS("trade")
public class StockWatchingTaskServiceImpl extends ServiceImpl<StockWatchingTaskMapper, StockWatchingTask> implements StockWatchingTaskService {

    /**
     * 交易模式：连板接力。
     */
    private static final int TRADE_MODE_RELAY_LIMIT_UP = 1;

    /**
     * 交易模式：优质首板。
     */
    private static final int TRADE_MODE_FIRST_LIMIT_UP = 2;

    private final StockDailyService stockDailyService;
    private final StockTradeCalendarService stockTradeCalendarService;
    private final StockCurrentStatusService stockCurrentStatusService;
    private final StockEmotionCycleDailyService stockEmotionCycleDailyService;
    private final StockConvertibleBondHistoryService stockConvertibleBondHistoryService;

    /**
     * 创建股票盯盘任务服务。
     * 作用：注入日 K 服务，用于根据日 K 结果生成选股盯盘任务。
     */
    public StockWatchingTaskServiceImpl(StockDailyService stockDailyService,
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
     * 创建收盘后选股盯盘任务。
     */
    @Override
    public List<StockWatchingTask> createPostCloseWatchingTasks(LocalDate tradeDate) {
        Set<String> convertibleBondStockCodes = listConvertibleBondStockCodes(tradeDate);
        List<StockWatchingTask> tasks = new ArrayList<>();
        tasks.addAll(createHighQualityFirstLimitUpTasks(tradeDate, convertibleBondStockCodes));
        tasks.addAll(createRelayLimitUpTasks(tradeDate, convertibleBondStockCodes));
        return tasks;
    }

    /**
     * 创建优质首板推荐任务。
     * 实现逻辑：查询当天非 ST、涨幅小于 11、K 线为涨停、连续涨停天数为 1 的股票，批量插入任务表。
     */
    private List<StockWatchingTask> createHighQualityFirstLimitUpTasks(LocalDate tradeDate,
                                                                       Set<String> convertibleBondStockCodes) {
        List<StockDailyEntity> stockDailyList = stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getTradeDate, tradeDate)
                .and(wrapper -> wrapper.isNull(StockDailyEntity::getIsSt).or().eq(StockDailyEntity::getIsSt, false))
                .lt(StockDailyEntity::getFloatMarketCap, 300_000)
                .lt(StockDailyEntity::getClosePrice, 40)
                .lt(StockDailyEntity::getChangeRate, 11)
                .between(StockDailyEntity::getKlineState, 1, 5)
                .eq(StockDailyEntity::getConsecutiveLimitUpDays, 1));
        // 过滤包含可转债的股票
        stockDailyList = stockDailyList.stream()
                .filter(stockDaily -> !convertibleBondStockCodes.contains(stockDaily.getStockCode()))
                .toList();

        List<StockSelectionAssistDTO> assistList = buildSelectionAssistList(stockDailyList);
        for (StockSelectionAssistDTO stockSelectionAssistDTO : assistList) {
            log.info("首板------:{}:{}", calculateSelectionScore(stockSelectionAssistDTO), stockSelectionAssistDTO);
        }
        List<StockWatchingTask> tasks = assistList.stream()
                .filter(dto -> dto.getAbnormalKlineStateCount() < 20) // 首板选择质地更好的股票，太多非正常 k 线的股性不好
                .filter(dto -> dto.getMaxTurnoverRate() > 25 || dto.getNonStMonthCount() >= 18 || dto.getNonStMonthCount() >= dto.getListingMonthCount())// 最大换手大于 25的 或者 摘帽大于 18个月 或者新上市公司
                .filter(dto -> dto.getTenDayChangeRate() < 25 && dto.getStartPrice() > 3)
                .map(assist -> buildWatchingTask(assist, TRADE_MODE_FIRST_LIMIT_UP, calculateSelectionScore(assist)))
                .sorted(Comparator.comparing(StockWatchingTask::getLimitUpScore).reversed())
                .filter(task -> task.getLimitUpScore() > 30)
                .limit(3)//只取前三个
                .toList();
        replaceTasks(tradeDate, TRADE_MODE_FIRST_LIMIT_UP, tasks);
        return tasks;
    }

    /**
     * 创建连板接力推荐任务。
     * 实现逻辑：查询当天非 ST、涨幅小于 11、K 线为涨停、连续涨停天数为 2/3/4 的股票，批量插入任务表。
     */
    private List<StockWatchingTask> createRelayLimitUpTasks(LocalDate tradeDate,
                                                            Set<String> convertibleBondStockCodes) {
        List<StockDailyEntity> stockDailyEntities = stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getTradeDate, tradeDate)
                .and(wrapper -> wrapper.isNull(StockDailyEntity::getIsSt).or().eq(StockDailyEntity::getIsSt, false))
                .lt(StockDailyEntity::getChangeRate, 11)
                .lt(StockDailyEntity::getFloatMarketCap, 600_000)
                .lt(StockDailyEntity::getClosePrice, 40)
                .between(StockDailyEntity::getKlineState, 1, 5)
                .between(StockDailyEntity::getConsecutiveLimitUpDays, 2, 3));
        // 过滤包含可转债的股票
        stockDailyEntities = stockDailyEntities.stream()
                .filter(stockDaily -> !convertibleBondStockCodes.contains(stockDaily.getStockCode()))
                .toList();

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
        // 高度压制到 4 板以下就推荐
        //2.高度压制 2板，推荐2板股票
        //3.高度压制 3板，推荐2/3板股票
        //4.高度压制 4板，推荐4/3/2板
        if (todayMaxLbc <= 4) {
            minConsecutiveLimitUpDays = 2;
            maxConsecutiveLimitUpDays = 3;
        } else if (yesterdayHighestLimitUp <= dayBeforeYesterdayHighestLimitUp) {
            //5.连板高度 >=5 板，判断是否是龙头断板第二天，推荐昨天的2板票
            String yesterdayMaxLbc2Code = findHighestLimitUp(yesterdayMaxLbc2.getTradeDate(), dayBeforeYesterdayHighestLimitUp);
            // 如果前天发生大退潮而且高度大于5板，推荐 3,4 班
            // 判断前天大退潮的时候高度有没有超过7板
            if (dayBeforeYesterdayHighestLimitUp < 7) {
                // 没有超过7板直接推荐三四班
                minConsecutiveLimitUpDays = 3;
                maxConsecutiveLimitUpDays = 3;
            }
            if (StrUtil.isNotEmpty(yesterdayMaxLbc2.getDominantCycleStockCode()) && Objects.equals(yesterdayMaxLbc2Code, yesterdayMaxLbc2.getDominantCycleStockCode())) {
                // 超过7板，判断断板的股票是否是占领情绪周期的股票，如果是推荐三四班
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
        List<StockDailyEntity> selectedStockDailyList = minConsecutiveLimitUpDays == null
                ? List.of()
                : stockDailyEntities.stream()
                .filter(stockDaily -> stockDaily.getConsecutiveLimitUpDays() >= minLimitUpDays
                        && stockDaily.getConsecutiveLimitUpDays() <= maxLimitUpDays)
                .toList();
        List<StockSelectionAssistDTO> assistList = buildSelectionAssistList(selectedStockDailyList);


        List<StockWatchingTask> tasks = new ArrayList<>();
        if (!assistList.isEmpty()) {

            for (StockSelectionAssistDTO stockSelectionAssistDTO : assistList) {
                log.info("连板------:{}:{}", calculateSelectionScore(stockSelectionAssistDTO), stockSelectionAssistDTO);
            }

            tasks = assistList.stream()
                    .filter(dto -> dto.getConsecutiveOneWordLimitUpDays() < 2 && dto.getRecentThreeMonthHighestConsecutiveLimitUpDays() < 3)// 一字板次数一定要小于 2 ，现在只推荐 2，3板的，就是说只能有一个一字板,近三个月不能有超过三板的高度
                    .filter(dto -> dto.getMaxTurnoverRate() > 25 || dto.getNonStMonthCount() >= 18 || dto.getNonStMonthCount() >= dto.getListingMonthCount())// 最大换手大于 25的 或者 摘帽大于 18个月 或者新上市公司
                    .filter(dto -> dto.getTenDayChangeRate() < 60 && dto.getStartPrice() > 3 )// 连板要对 5、10日涨幅做判断。不能是大深坑往外爬
                    .map(assist -> buildWatchingTask(assist, TRADE_MODE_RELAY_LIMIT_UP, calculateSelectionScore(assist)))
                    .filter(stockWatchingTask -> stockWatchingTask.getLimitUpScore() > 15)
                    .sorted(Comparator.comparing(StockWatchingTask::getLimitUpScore).reversed())
                    .limit(3)//只取前三个
                    .toList();
            replaceTasks(tradeDate, TRADE_MODE_RELAY_LIMIT_UP, tasks);
        }
        return tasks;

    }

    private String findHighestLimitUp(LocalDate tradeDate, Integer highestLimitUp) {
        StockDailyEntity stockDaily = stockDailyService.getOne(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getTradeDate, tradeDate)
                .eq(StockDailyEntity::getIsSt, false)
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
    private StockWatchingTask buildWatchingTask(StockSelectionAssistDTO assist, int tradeMode, Integer limitUpScore) {
        StockWatchingTask task = new StockWatchingTask();
        task.setStockCode(assist.getStockCode());
        task.setStockName(assist.getStockName());
        task.setLimitUpScore(limitUpScore);
        task.setConsecutiveLimitUpDays(assist.getConsecutiveLimitUpDays());
        task.setRecommendDate(assist.getTradeDate());
        task.setTradeDate(findNextTradeDate(assist.getTradeDate()));
        task.setTradeMode(tradeMode);
        task.setCreatedTime(LocalDateTime.now());
        return task;
    }

    /**
     * 补跑时先删除同一天同模式旧任务，再写入新任务，避免重复推荐。
     */
    private void replaceTasks(LocalDate recommendDate, int tradeMode, List<StockWatchingTask> tasks) {
        remove(Wrappers.<StockWatchingTask>lambdaQuery()
                .eq(StockWatchingTask::getRecommendDate, recommendDate)
                .eq(StockWatchingTask::getTradeMode, tradeMode));
        if (!tasks.isEmpty()) {
            saveBatch(tasks);
        }
    }

    /**
     * 接力任务分数优先反映连板高度，空间龙额外加分，便于盯盘列表排序。
     */
    private Integer calculateRelayLimitUpScore(StockSelectionAssistDTO assist, StockEmotionCycleDaily emotionCycleDaily) {
        int score = calculateSelectionScore(assist);
        if (emotionCycleDaily != null && Objects.equals(assist.getStockCode(), emotionCycleDaily.getDominantCycleStockCode())) {
            score += 10;
        }
        return score;
    }

    /**
     * 按启动市值、历史最大换手、启动价格、地域板块、选股当日换手率计算基础分，
     * 以 27 亿启动流通市值为基准动态计算非正常状态免扣次数，最终分数不低于 0 分。
     */
    private Integer calculateSelectionScore(StockSelectionAssistDTO assist) {
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
     * 启动价格评分：低于 3 元 50 分，3 至 10 元 15 分，超过 19.5 元 0 分，中间区间线性扣分。
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
     * 连板评分：首板 0 分，每增加 1 板增加 10 分。
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
    private List<StockSelectionAssistDTO> buildSelectionAssistList(List<StockDailyEntity> stockDailyList) {
        return stockDailyList.stream()
                .map(this::buildSelectionAssist)
                .toList();
    }

    /**
     * 填充单只股票的选股辅助字段。
     */
    private StockSelectionAssistDTO buildSelectionAssist(StockDailyEntity stockDaily) {
        List<StockDailyEntity> recentDailyList = listRecentDaily(stockDaily);
        List<StockDailyEntity> selectionWindowDailyList = listSelectionWindowDaily(stockDaily);
        List<StockDailyEntity> ascRecentDailyList = recentDailyList.stream()
                .sorted(Comparator.comparing(StockDailyEntity::getTradeDate))
                .toList();

        StockSelectionAssistDTO assist = new StockSelectionAssistDTO();
        assist.setStockCode(stockDaily.getStockCode());
        assist.setStockName(stockDaily.getStockName());
        assist.setTradeDate(stockDaily.getTradeDate());
        assist.setConsecutiveLimitUpDays(stockDaily.getConsecutiveLimitUpDays());
        assist.setConsecutiveOneWordLimitUpDays(countConsecutiveOneWordLimitUpDays(
                recentDailyList, stockDaily.getConsecutiveLimitUpDays()));
        assist.setProvince(findProvince(stockDaily.getStockCode()));
        assist.setCurrentPrice(stockDaily.getClosePrice());
        assist.setStartMarketCap(findStartMarketCap(recentDailyList, stockDaily.getConsecutiveLimitUpDays()));
        StockDailyEntity startDaily = findStartDaily(recentDailyList, stockDaily.getConsecutiveLimitUpDays());
        assist.setStartPrice(startDaily == null ? null : startDaily.getClosePrice());
        assist.setCurrentTurnoverRate(stockDaily.getTurnoverRate());
        assist.setNonStMonthCount(calculateNonStMonthCount(stockDaily));
        assist.setListingMonthCount(calculateListingMonthCount(stockDaily));
        assist.setMaxTurnoverRate(findMaxTurnoverRate(selectionWindowDailyList));
        assist.setHighestConsecutiveLimitUpDays(findHighestConsecutiveLimitUpDays(selectionWindowDailyList));
        assist.setRecentThreeMonthHighestConsecutiveLimitUpDays(
                findRecentThreeMonthHighestConsecutiveLimitUpDays(
                        selectionWindowDailyList, recentDailyList, stockDaily.getConsecutiveLimitUpDays()));
        assist.setAbnormalKlineStateCount(countAbnormalKlineState(selectionWindowDailyList, stockDaily.getConsecutiveLimitUpDays()));
        assist.setFiveDayChangeRate(calculateAdjustedCloseChangeRate(ascRecentDailyList, 5));
        assist.setTenDayChangeRate(calculateAdjustedCloseChangeRate(ascRecentDailyList, 10));
        return assist;
    }

    /**
     * 查询当前交易日前 10 个交易日和当天，用于计算 5/10 日涨跌幅等辅助指标。
     */
    private List<StockDailyEntity> listRecentDaily(StockDailyEntity stockDaily) {
        return stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getStockCode, stockDaily.getStockCode())
                .le(StockDailyEntity::getTradeDate, stockDaily.getTradeDate())
                .orderByDesc(StockDailyEntity::getTradeDate)
                .last("LIMIT 11"));
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
     * 查询省份属性。
     */
    private String findProvince(String stockCode) {
        StockCurrentStatus status = stockCurrentStatusService.getOne(Wrappers.<StockCurrentStatus>lambdaQuery()
                .eq(StockCurrentStatus::getStockCode, stockCode)
                .last("LIMIT 1"));
        return status == null ? null : status.getRegionName();
    }

    /**
     * 本轮首板前一交易日的流通市值作为启动市值。
     */
    private Double findStartMarketCap(List<StockDailyEntity> recentDailyList, Integer consecutiveLimitUpDays) {
        StockDailyEntity startDaily = findStartDaily(recentDailyList, consecutiveLimitUpDays);
        return startDaily == null ? null : startDaily.getFloatMarketCap();
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
     * 按当前连板数回看本轮涨停日，统计其中 klineState 为 3 的一字板数量。
     */
    private int countConsecutiveOneWordLimitUpDays(List<StockDailyEntity> recentDailyList,
                                                   Integer consecutiveLimitUpDays) {
        int consecutiveDays = Math.max(0, Objects.requireNonNullElse(consecutiveLimitUpDays, 0));
        int count = 0;
        for (int i = 0; i < consecutiveDays && i < recentDailyList.size(); i++) {
            if (Objects.equals(recentDailyList.get(i).getKlineState(), 3)) {
                count++;
            }
        }
        return count;
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
     * 统计选股窗口内最大换手率，排除股票上市后最早的 10 根日 K，
     * 避免新上市初期异常高换手干扰历史量能判断。
     */
    private Double findMaxTurnoverRate(List<StockDailyEntity> recentDailyList) {
        return recentDailyList.stream()
                .sorted(Comparator.comparing(StockDailyEntity::getTradeDate))
                .skip(10)
                .map(StockDailyEntity::getTurnoverRate)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(null);
    }

    /**
     * 统计选股窗口内最高板。
     */
    private Integer findHighestConsecutiveLimitUpDays(List<StockDailyEntity> recentDailyList) {
        return recentDailyList.stream()
                .map(StockDailyEntity::getConsecutiveLimitUpDays)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
    }

    /**
     * 排除本轮连板后，统计本轮首板前一个交易日往前 3 个自然月内的最高板。
     */
    private Integer findRecentThreeMonthHighestConsecutiveLimitUpDays(
            List<StockDailyEntity> selectionWindowDailyList,
            List<StockDailyEntity> recentDailyList,
            Integer consecutiveLimitUpDays) {
        int currentConsecutiveDays = Math.max(0, Objects.requireNonNullElse(consecutiveLimitUpDays, 0));
        if (recentDailyList.size() <= currentConsecutiveDays) {
            return null;
        }
        LocalDate endDate = recentDailyList.get(currentConsecutiveDays).getTradeDate();
        LocalDate startDate = endDate.minusMonths(3);
        return selectionWindowDailyList.stream()
                .filter(daily -> !daily.getTradeDate().isBefore(startDate))
                .filter(daily -> !daily.getTradeDate().isAfter(endDate))
                .map(StockDailyEntity::getConsecutiveLimitUpDays)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
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
}
