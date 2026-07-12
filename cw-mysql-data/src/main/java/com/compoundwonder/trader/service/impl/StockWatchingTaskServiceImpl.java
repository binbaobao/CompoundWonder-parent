package com.compoundwonder.trader.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compoundwonder.hxdata.entity.StockCurrentStatus;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.service.StockCurrentStatusService;
import com.compoundwonder.hxdata.service.StockDailyService;
import com.compoundwonder.hxdata.service.StockTradeCalendarService;
import com.compoundwonder.trader.dto.StockSelectionAssistDTO;
import com.compoundwonder.trader.entity.StockEmotionCycleDaily;
import com.compoundwonder.trader.entity.StockWatchingTask;
import com.compoundwonder.trader.mapper.StockWatchingTaskMapper;
import com.compoundwonder.trader.service.StockEmotionCycleDailyService;
import com.compoundwonder.trader.service.StockWatchingTaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 股票盯盘任务服务实现。
 */
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

    /**
     * 创建股票盯盘任务服务。
     * 作用：注入日 K 服务，用于根据日 K 结果生成选股盯盘任务。
     */
    public StockWatchingTaskServiceImpl(StockDailyService stockDailyService,
                                        StockTradeCalendarService stockTradeCalendarService,
                                        StockCurrentStatusService stockCurrentStatusService,
                                        StockEmotionCycleDailyService stockEmotionCycleDailyService) {
        this.stockDailyService = stockDailyService;
        this.stockTradeCalendarService = stockTradeCalendarService;
        this.stockCurrentStatusService = stockCurrentStatusService;
        this.stockEmotionCycleDailyService = stockEmotionCycleDailyService;
    }

    /**
     * 创建收盘后选股盯盘任务。
     */
    @Override
    public List<StockWatchingTask> createPostCloseWatchingTasks(LocalDate tradeDate) {
        List<StockWatchingTask> tasks = new ArrayList<>();
        tasks.addAll(createHighQualityFirstLimitUpTasks(tradeDate));
        tasks.addAll(createRelayLimitUpTasks(tradeDate));
        return tasks;
    }

    /**
     * 创建优质首板推荐任务。
     * 实现逻辑：查询当天非 ST、涨幅小于 11、K 线为涨停、连续涨停天数为 1 的股票，批量插入任务表。
     */
    @Override
    public List<StockWatchingTask> createHighQualityFirstLimitUpTasks(LocalDate tradeDate) {
        List<StockDailyEntity> stockDailyList = stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getTradeDate, tradeDate)
                .and(wrapper -> wrapper.isNull(StockDailyEntity::getIsSt).or().eq(StockDailyEntity::getIsSt, false))
                .lt(StockDailyEntity::getChangeRate, 11)
                .between(StockDailyEntity::getKlineState, 1, 5)
                .eq(StockDailyEntity::getConsecutiveLimitUpDays, 1));

        List<StockSelectionAssistDTO> assistList = buildSelectionAssistList(stockDailyList, null);
        List<StockWatchingTask> tasks = assistList.stream()
                .map(assist -> buildWatchingTask(assist, TRADE_MODE_FIRST_LIMIT_UP, assist.getConsecutiveLimitUpDays()))
                .toList();
        replaceTasks(tradeDate, TRADE_MODE_FIRST_LIMIT_UP, tasks);
        return tasks;
    }

    /**
     * 创建连板接力推荐任务。
     * 实现逻辑：查询当天非 ST、涨幅小于 11、K 线为涨停、连续涨停天数为 2/3/4 的股票，批量插入任务表。
     */
    @Override
    public List<StockWatchingTask> createRelayLimitUpTasks(LocalDate tradeDate) {

        // 先查出 今天 昨天 前天 的最高板
        List<StockEmotionCycleDaily> entityList = stockEmotionCycleDailyService.list(Wrappers.<StockEmotionCycleDaily>lambdaQuery()
                .le(StockEmotionCycleDaily::getTradeDate, tradeDate)
                .orderByDesc(StockEmotionCycleDaily::getTradeDate)
                .last("LIMIT 3"));
        StockEmotionCycleDaily todayEmotionCycleDaily = entityList.get(0);
        StockEmotionCycleDaily yesterdayEmotionCycleDaily = entityList.get(0);
        StockEmotionCycleDaily dayBeforeYesterdayEmotionCycleDaily = entityList.get(0);
        int todayMaxLbc = todayEmotionCycleDaily.getHighestConsecutiveLimitUpDays();
        // 高度压制到 4 板以下就推荐
        //2.高度压制 2板，推荐2板股票
        //3.高度压制 3板，推荐2/3板股票
        //4.高度压制 4板，推荐4/3/2板
        if (todayEmotionCycleDaily.getHighestConsecutiveLimitUpDays() <=4){

        }


//        // 先查出 今天 昨天 前天 的最高板
//        QueryWrapper<EmotionCycleInfoEntity> wrapper1 = new QueryWrapper<>();
//        wrapper1.le("date", date);
//        wrapper1.orderByDesc("date");
//        wrapper1.last("limit 3");
//        List<EmotionCycleInfoEntity> entityList = emotionCycleInfoDao.selectList(wrapper1);
//
//        // 今天 昨天 前天 的最高板
//        int todayMaxLbc = entityList.get(0).getHighestLimitUp();
//        EmotionCycleInfoEntity yesterdayMaxLbc = entityList.get(1);
//        EmotionCycleInfoEntity yesterdayMaxLbc2 = entityList.get(2);
//
//        // 高度压制到 4 板以下就推荐
//        //2.高度压制 2板，推荐2板股票
//        //3.高度压制 3板，推荐2/3板股票
//        //4.高度压制 4板，推荐4/3/2板
//        if (todayMaxLbc <= 4) {
//            List<StockDailyEntity> list = stockDailyEntities.stream().filter(stockDaily -> stockDaily.getConsecutiveLimitUpDays() >= 2 && stockDaily.getConsecutiveLimitUpDays() <= 4).toList();
//            this.filterStocks(date, list, todayMaxLbc, shareInfoMap);
//        } else if (yesterdayMaxLbc.getHighestLimitUp() <= yesterdayMaxLbc2.getHighestLimitUp()) {
//            //5.连板高度 >=5 板，判断是否是龙头断板第二天，推荐昨天的2板票
//            String yesterdayMaxLbc2Code = findHighestLimitUp(yesterdayMaxLbc2.getDate().toString(), yesterdayMaxLbc2.getHighestLimitUp());
//            // 如果前天发生大退潮而且高度大于5板，推荐 3,4 班
//            // 判断前天大退潮的时候高度有没有超过7板
//            if (yesterdayMaxLbc2.getHighestLimitUp() < 7) {
//                // 没有超过7板直接推荐三四班
//                List<StockDailyEntity> list = stockDailyEntities.stream().filter(stockDaily -> stockDaily.getConsecutiveLimitUpDays() >= 3 && stockDaily.getClosePrice() > 3 && stockDaily.getConsecutiveLimitUpDays() <= 4).toList();
//                this.filterStocks(date, list, todayMaxLbc, shareInfoMap);
//            }
//            if (StrUtil.isNotEmpty(yesterdayMaxLbc2.getCode()) && yesterdayMaxLbc2Code.equals(yesterdayMaxLbc2.getCode())) {
//                // 超过7板，判断断板的股票是否是占领情绪周期的股票，如果是推荐三四班
//                List<StockDailyEntity> list = stockDailyEntities.stream().filter(stockDaily -> stockDaily.getConsecutiveLimitUpDays() >= 3 && stockDaily.getClosePrice() > 3 && stockDaily.getConsecutiveLimitUpDays() <= 4).toList();
//                this.filterStocks(date, list, todayMaxLbc, shareInfoMap);
//            }
//        } else if (todayMaxLbc <= yesterdayMaxLbc.getHighestLimitUp()) {
//            String yesterdayMaxLbcCode = findHighestLimitUp(yesterdayMaxLbc.getDate().toString(), yesterdayMaxLbc.getHighestLimitUp());
//            // 6.连板高度 >=5 板，判断是否是龙头断板，推荐今天的3,2板票
//            // 如果今天发生退潮而且高度大于5板，推荐 2,3 班
//            // 今日高度降低，判断昨日高度是否超过 7 板
//            if (yesterdayMaxLbc.getHighestLimitUp() < 7) {
//                // 如果昨日高度没有超过7板直接推荐 2,3 板
//                List<StockDailyEntity> list = stockDailyEntities.stream().filter(stockDaily -> stockDaily.getConsecutiveLimitUpDays() >= 2 && stockDaily.getClosePrice() > 3 && stockDaily.getConsecutiveLimitUpDays() <= 3).toList();
//                this.filterStocks(date, list, todayMaxLbc, shareInfoMap);
//            } else if (StrUtil.isNotEmpty(yesterdayMaxLbc.getCode()) && yesterdayMaxLbcCode.equals(yesterdayMaxLbc.getCode())) {
//                // 如果昨日高度超过7板，判断是否占领情绪周期，如果占领情绪周期才推荐，为了避免一些高度较高的中位票
//                List<StockDailyEntity> list = stockDailyEntities.stream().filter(stockDaily -> stockDaily.getConsecutiveLimitUpDays() >= 2 && stockDaily.getClosePrice() > 3 && stockDaily.getConsecutiveLimitUpDays() <= 4).toList();
//                this.filterStocks(date, list, todayMaxLbc, shareInfoMap);
//            }
//        }
//        // 如果最高是 5 板执行特殊逻辑
//        if (todayMaxLbc == 5) {
//            List<StockDailyEntity> dailyEntities = stockDailyDao.selectList(new QueryWrapper<StockDailyEntity>()
//                    .eq("trade_date", date)
//                    .gt("change_rate", 9)
//                    .lt("change_rate", 11)
//                    .eq("consecutive_limit_up_days", 5));
//            QueryWrapper<StockWatchingTaskEntity> wrapper = new QueryWrapper<>();
//            wrapper.eq("task_date", date);
//            wrapper.eq("transaction_mode", 1);
//            Long aLong = stockWatchingTaskDao.selectCount(wrapper);
//            // 如果只有一个 5 板，而且前面没有推荐执行特殊逻辑
//            if (dailyEntities.size() == 1 && aLong == 0) {
//                StockDailyEntity stockDailyEntity = dailyEntities.get(0);
//                double closePrice = stockDailyEntity.getClosePrice() / 1.6;
//                // 如果唯一 5 板市值过大，换手高，振幅太小，振幅太大，其中一个情况就推荐下面 2 板股票 股票
//                if (stockDailyEntity.getFloatMarketCap() > 450000 || stockDailyEntity.getTurnoverRate() > 45 || stockDailyEntity.getAmplitude() > 13 || closePrice < 2.5 || closePrice > 30) {
//                    List<StockDailyEntity> list = stockDailyEntities.stream().filter(stockDaily -> stockDaily.getConsecutiveLimitUpDays() == 2 && stockDaily.getConsecutiveLimitUpDays() <= 3).toList();
//                    this.filterStocks(date, list, 4, shareInfoMap);// 如果 5 板很拉按照最高四板逻辑执行逻辑，其实就是一个卡位预判逻辑
//                }
//            }
//        }



        List<StockDailyEntity> stockDailyList = stockDailyService.list(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getTradeDate, tradeDate)
                .and(wrapper -> wrapper.isNull(StockDailyEntity::getIsSt).or().eq(StockDailyEntity::getIsSt, false))
                .lt(StockDailyEntity::getChangeRate, 11)
                .between(StockDailyEntity::getKlineState, 1, 5)
                .in(StockDailyEntity::getConsecutiveLimitUpDays, List.of(2, 3, 4)));



        List<StockSelectionAssistDTO> assistList = buildSelectionAssistList(stockDailyList, emotionCycleDaily);
        List<StockWatchingTask> tasks = assistList.stream()
                .map(assist -> buildWatchingTask(assist, TRADE_MODE_RELAY_LIMIT_UP, calculateRelayLimitUpScore(assist, emotionCycleDaily)))
                .toList();
        replaceTasks(tradeDate, TRADE_MODE_RELAY_LIMIT_UP, tasks);
        return tasks;
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
        int score = Objects.requireNonNullElse(assist.getConsecutiveLimitUpDays(), 0);
        if (emotionCycleDaily != null && Objects.equals(assist.getStockCode(), emotionCycleDaily.getDominantCycleStockCode())) {
            score += 10;
        }
        return score;
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
    private List<StockSelectionAssistDTO> buildSelectionAssistList(List<StockDailyEntity> stockDailyList, StockEmotionCycleDaily emotionCycleDaily) {
        return stockDailyList.stream()
                .map(stockDaily -> buildSelectionAssist(stockDaily, emotionCycleDaily))
                .toList();
    }

    /**
     * 填充单只股票的选股辅助字段。
     */
    private StockSelectionAssistDTO buildSelectionAssist(StockDailyEntity stockDaily, StockEmotionCycleDaily emotionCycleDaily) {
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
        assist.setProvince(findProvince(stockDaily.getStockCode()));
        assist.setCurrentPrice(stockDaily.getClosePrice());
        assist.setStartMarketCap(findStartMarketCap(recentDailyList, stockDaily.getConsecutiveLimitUpDays()));
        assist.setNonStMonthCount(calculateNonStMonthCount(stockDaily));
        assist.setMaxTurnoverRate(findMaxTurnoverRate(selectionWindowDailyList));
        assist.setHighestConsecutiveLimitUpDays(findHighestConsecutiveLimitUpDays(selectionWindowDailyList, emotionCycleDaily));
        assist.setAbnormalKlineStateCount(countAbnormalKlineState(selectionWindowDailyList));
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
        int offset = Objects.requireNonNullElse(consecutiveLimitUpDays, 1);
        if (recentDailyList.size() <= offset) {
            return null;
        }
        return recentDailyList.get(offset).getFloatMarketCap();
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
     * 统计选股窗口内最大换手率。
     */
    private Double findMaxTurnoverRate(List<StockDailyEntity> recentDailyList) {
        return recentDailyList.stream()
                .map(StockDailyEntity::getTurnoverRate)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(null);
    }

    /**
     * 统计选股窗口内最高板。
     */
    private Integer findHighestConsecutiveLimitUpDays(List<StockDailyEntity> recentDailyList, StockEmotionCycleDaily emotionCycleDaily) {
        Integer stockHighest = recentDailyList.stream()
                .map(StockDailyEntity::getConsecutiveLimitUpDays)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
        if (emotionCycleDaily == null || emotionCycleDaily.getHighestConsecutiveLimitUpDays() == null) {
            return stockHighest;
        }
        if (stockHighest == null) {
            return emotionCycleDaily.getHighestConsecutiveLimitUpDays();
        }
        return Math.max(stockHighest, emotionCycleDaily.getHighestConsecutiveLimitUpDays());
    }

    /**
     * 统计选股窗口内非正常 K 线状态次数。
     */
    private Integer countAbnormalKlineState(List<StockDailyEntity> recentDailyList) {
        return Math.toIntExact(recentDailyList.stream()
                .map(StockDailyEntity::getKlineState)
                .filter(Objects::nonNull)
                .filter(klineState -> klineState != 0)
                .count());
    }

    /**
     * 使用复权收盘价计算 N 日涨跌幅。
     */
    private Double calculateAdjustedCloseChangeRate(List<StockDailyEntity> ascRecentDailyList, int days) {
        if (ascRecentDailyList.size() <= days) {
            return null;
        }
        Double basePrice = ascRecentDailyList.get(ascRecentDailyList.size() - 1 - days).getAdjustClosePrice();
        Double currentPrice = ascRecentDailyList.get(ascRecentDailyList.size() - 1).getAdjustClosePrice();
        if (basePrice == null || currentPrice == null || basePrice == 0) {
            return null;
        }
        return (currentPrice - basePrice) * 100 / basePrice;
    }
}
