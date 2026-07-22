package com.compoundwonder.common.mysqldata.selection;

import com.compoundwonder.common.mysqldata.selection.model.MarketEmotionData;
import com.compoundwonder.common.mysqldata.selection.model.StockCurrentStatusData;
import com.compoundwonder.common.mysqldata.selection.model.StockDailyData;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * MySQL 数据模块向选股策略提供的原始事实接口。
 *
 * <p>实现类只负责查询和对象转换，不能包含选股阈值、评分或模式分支。</p>
 */
public interface StockSelectionDataService {

    /**
     * 查询指定交易日的全部日 K 原始数据，由各交易模式在内存中建立自己的候选池。
     *
     * @param tradeDate 选股交易日
     * @return 当日全部日 K；无数据时返回空列表
     */
    List<StockDailyData> listDailyByTradeDate(LocalDate tradeDate);

    /**
     * 按交易日倒序查询某只股票截至指定日期的最近若干根日 K。
     *
     * @param stockCode 六位股票代码
     * @param endDate 包含在查询范围内的截止日期
     * @param limit 最大返回根数，必须大于 0
     * @return 按交易日倒序排列的日 K；参数无效或无数据时返回空列表
     */
    List<StockDailyData> listLatestDaily(String stockCode, LocalDate endDate, int limit);

    /**
     * 查询某只股票闭区间内的日 K，供自然日窗口指标计算使用。
     *
     * @param stockCode 六位股票代码
     * @param startDate 起始日期，包含当天
     * @param endDate 截止日期，包含当天
     * @return 按交易日倒序排列的日 K；参数无效或无数据时返回空列表
     */
    List<StockDailyData> listDailyBetween(String stockCode, LocalDate startDate,
                                         LocalDate endDate);

    /**
     * 按交易日正序查询某只股票最早的若干根日 K，用于排除上市初期数据。
     *
     * @param stockCode 六位股票代码
     * @param endDate 最早数据查询的截止日期，包含当天
     * @param limit 最大返回根数，必须大于 0
     * @return 按交易日正序排列的日 K；参数无效或无数据时返回空列表
     */
    List<StockDailyData> listEarliestDaily(String stockCode, LocalDate endDate, int limit);

    /**
     * 查询指定日期以前最近一次 ST 日 K 的交易日。
     *
     * @param stockCode 六位股票代码
     * @param beforeDate 查询上界，不包含当天
     * @return 最近一次 ST 日期；历史中没有 ST 记录时返回 {@code null}
     */
    LocalDate findLatestStDate(String stockCode, LocalDate beforeDate);

    /**
     * 查询某只股票截至指定日期最早存在的交易日。
     *
     * @param stockCode 六位股票代码
     * @param endDate 查询截止日期，包含当天
     * @return 首个交易日；没有日 K 时返回 {@code null}
     */
    LocalDate findFirstTradeDate(String stockCode, LocalDate endDate);

    /**
     * 查询股票当前基础属性；策略当前主要读取所属地区。
     *
     * @param stockCode 六位股票代码
     * @return 当前基础属性；没有记录时返回 {@code null}
     */
    StockCurrentStatusData findCurrentStatus(String stockCode);

    /**
     * 查询截至指定日期最近若干个交易日的市场情绪高度数据。
     *
     * @param endDate 查询截止日期，包含当天
     * @param limit 最大返回交易日数量
     * @return 按交易日倒序排列的市场情绪数据
     */
    List<MarketEmotionData> listLatestMarketEmotion(LocalDate endDate, int limit);

    /**
     * 查询指定交易日仍有效的可转债对应正股代码，用于从候选池排除。
     *
     * @param tradeDate 选股交易日
     * @return 有效可转债正股代码集合；无数据时返回空集合
     */
    Set<String> listConvertibleBondStockCodes(LocalDate tradeDate);

    /**
     * 查找推荐日之后的第一个交易日，作为盯盘任务执行日。
     *
     * @param recommendDate 收盘后执行选股的推荐日
     * @return 下一个交易日；交易日历缺失时由实现按约定回退
     */
    LocalDate findNextTradeDate(LocalDate recommendDate);

    /** 查询闭区间内去重后的交易日并按日期升序返回。 */
    default List<LocalDate> listTradeDates(LocalDate startDate, LocalDate endDate) {
        return List.of();
    }
}
