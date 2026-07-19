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

    List<StockDailyData> listDailyByTradeDate(LocalDate tradeDate);

    List<StockDailyData> listLatestDaily(String stockCode, LocalDate endDate, int limit);

    List<StockDailyData> listDailyBetween(String stockCode, LocalDate startDate,
                                         LocalDate endDate);

    List<StockDailyData> listEarliestDaily(String stockCode, LocalDate endDate, int limit);

    LocalDate findLatestStDate(String stockCode, LocalDate beforeDate);

    LocalDate findFirstTradeDate(String stockCode, LocalDate endDate);

    StockCurrentStatusData findCurrentStatus(String stockCode);

    List<MarketEmotionData> listLatestMarketEmotion(LocalDate endDate, int limit);

    Set<String> listConvertibleBondStockCodes(LocalDate tradeDate);

    LocalDate findNextTradeDate(LocalDate recommendDate);
}
