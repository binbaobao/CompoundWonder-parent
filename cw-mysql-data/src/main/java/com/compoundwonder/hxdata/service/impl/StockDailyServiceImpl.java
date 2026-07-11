package com.compoundwonder.hxdata.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.compoundwonder.hxdata.dto.StockDayQuotationPoint;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.entity.StockFreeFloatShareHistory;
import com.compoundwonder.hxdata.entity.StockPreviousNameHistory;
import com.compoundwonder.hxdata.mapper.StockDailyMapper;
import com.compoundwonder.hxdata.service.StockDailyService;
import com.compoundwonder.hxdata.service.StockFreeFloatShareHistoryService;
import com.compoundwonder.hxdata.service.StockPreviousNameHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 股票日 K 服务实现。
 * 作用：把华鑫日 K 数据转换为 stock_daily，并补充名称、ST、流通股、换手率、市值和连板状态。
 */
@Service
@DS("market")
public class StockDailyServiceImpl extends ServiceImpl<StockDailyMapper, StockDailyEntity> implements StockDailyService {

    private static final DateTimeFormatter API_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final StockPreviousNameHistoryService stockPreviousNameHistoryService;
    private final StockFreeFloatShareHistoryService stockFreeFloatShareHistoryService;

    /**
     * 创建股票日 K 服务。
     * 作用：注入曾用名和自由流通股本服务，用于补充日 K 冗余字段。
     */
    public StockDailyServiceImpl(StockPreviousNameHistoryService stockPreviousNameHistoryService, StockFreeFloatShareHistoryService stockFreeFloatShareHistoryService) {
        this.stockPreviousNameHistoryService = stockPreviousNameHistoryService;
        this.stockFreeFloatShareHistoryService = stockFreeFloatShareHistoryService;
    }

    /**
     * 替换指定股票的日 K 历史数据。
     * 实现逻辑：按交易日升序重算连板和断板，确认有数据后再删除旧数据并批量保存。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int replaceStockDaily(String stockCode, List<StockDayQuotationPoint> quotations) {
        List<StockDayQuotationPoint> sortedQuotations = quotations.stream()
                .sorted(Comparator.comparing(StockDayQuotationPoint::getTradingDay))
                .toList();
        if (sortedQuotations.isEmpty()) {
            return 0;
        }

        LocalDate startDate = parseTradeDate(sortedQuotations.get(0));
        LocalDate endDate = parseTradeDate(sortedQuotations.get(sortedQuotations.size() - 1));
        List<StockPreviousNameHistory> nameHistories = stockPreviousNameHistoryService.findNamesByDateRange(stockCode, startDate, endDate);
        List<StockFreeFloatShareHistory> freeFloatHistories = stockFreeFloatShareHistoryService.findByDateRange(stockCode, startDate, endDate);

        int previousLimitUpDays = 0;
        List<StockDailyEntity> stockDailyList = new java.util.ArrayList<>();
        for (StockDayQuotationPoint quotation : sortedQuotations) {
            StockDailyEntity stockDaily = buildStockDaily(quotation, previousLimitUpDays, nameHistories, freeFloatHistories);
            stockDailyList.add(stockDaily);
            previousLimitUpDays = stockDaily.getConsecutiveLimitUpDays() != null && stockDaily.getConsecutiveLimitUpDays() > 0
                    ? stockDaily.getConsecutiveLimitUpDays()
                    : 0;
        }

        remove(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getStockCode, stockCode));
        saveBatch(stockDailyList, 1000);
        return stockDailyList.size();
    }

    /**
     * 保存指定交易日的全市场日 K。
     * 实现逻辑：接口按日期分页返回全市场数据，每页落库时按 stock_code + trade_date 覆盖当天记录。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveMarketDaily(LocalDate tradeDate, List<StockDayQuotationPoint> quotations) {
        List<StockDayQuotationPoint> filteredQuotations = quotations.stream()
                .filter(quotation -> quotation.getStockCode() != null && !quotation.getStockCode().isBlank())
                .filter(quotation -> tradeDate.equals(parseTradeDate(quotation)))
                .toList();
        if (filteredQuotations.isEmpty()) {
            return 0;
        }

        List<String> stockCodes = filteredQuotations.stream()
                .map(StockDayQuotationPoint::getStockCode)
                .distinct()
                .toList();
        Map<String, Integer> yesterdayLimitUpMap = findYesterdayLimitUpMap(tradeDate);
        List<StockDailyEntity> stockDailyList = filteredQuotations.stream()
                .map(quotation -> buildSingleDayStockDaily(quotation, tradeDate, yesterdayLimitUpMap))
                .filter(Objects::nonNull)
                .toList();
        if (stockDailyList.isEmpty()) {
            return 0;
        }

        remove(Wrappers.<StockDailyEntity>lambdaQuery()
                .eq(StockDailyEntity::getTradeDate, tradeDate)
                .in(StockDailyEntity::getStockCode, stockCodes));
        saveBatch(stockDailyList, 1000);
        return stockDailyList.size();
    }

    /**
     * 构建单日股票日 K。
     * 作用：每日更新只处理一个交易日，名称、流通股按单日查询补齐，连板状态使用昨日涨停池计算。
     */
    private StockDailyEntity buildSingleDayStockDaily(StockDayQuotationPoint quotation, LocalDate tradeDate, Map<String, Integer> yesterdayLimitUpMap) {
        List<StockPreviousNameHistory> nameHistories = stockPreviousNameHistoryService.findNamesByDateRange(quotation.getStockCode(), tradeDate, tradeDate);
        List<StockFreeFloatShareHistory> freeFloatHistories = stockFreeFloatShareHistoryService.findByDateRange(quotation.getStockCode(), tradeDate, tradeDate);
        return buildStockDaily(quotation, yesterdayLimitUpMap, nameHistories, freeFloatHistories);
    }

    /**
     * 查询上一交易日涨停股票映射。
     * 作用：每日全市场更新前一次性查出昨日涨停池，用于计算今日连板和断板高度。
     */
    private Map<String, Integer> findYesterdayLimitUpMap(LocalDate tradeDate) {
        LocalDate previousTradeDate = findPreviousTradeDate(tradeDate);
        if (previousTradeDate == null) {
            return Map.of();
        }

        return list(Wrappers.<StockDailyEntity>lambdaQuery()
                .select(StockDailyEntity::getStockCode, StockDailyEntity::getConsecutiveLimitUpDays)
                .eq(StockDailyEntity::getTradeDate, previousTradeDate)
                .gt(StockDailyEntity::getChangeRate, 8)
                .lt(StockDailyEntity::getChangeRate, 11)
                .gt(StockDailyEntity::getKlineState, 0)
                .lt(StockDailyEntity::getKlineState, 6))
                .stream()
                .filter(stockDaily -> stockDaily.getStockCode() != null)
                .filter(stockDaily -> stockDaily.getConsecutiveLimitUpDays() != null && stockDaily.getConsecutiveLimitUpDays() > 0)
                .collect(Collectors.toMap(StockDailyEntity::getStockCode, StockDailyEntity::getConsecutiveLimitUpDays, (left, right) -> left));
    }

    /**
     * 查询当前交易日前最近一个已入库交易日。
     */
    private LocalDate findPreviousTradeDate(LocalDate tradeDate) {
        StockDailyEntity previous = getOne(Wrappers.<StockDailyEntity>lambdaQuery()
                .select(StockDailyEntity::getTradeDate)
                .lt(StockDailyEntity::getTradeDate, tradeDate)
                .orderByDesc(StockDailyEntity::getTradeDate)
                .last("LIMIT 1"));
        return previous == null ? null : previous.getTradeDate();
    }

    /**
     * 构建单条股票日 K 记录。
     * 处理逻辑：映射接口字段，补充名称、ST、流通股本，并计算衍生指标。
     */
    private StockDailyEntity buildStockDaily(StockDayQuotationPoint stockDay, int previousLimitUpDays, List<StockPreviousNameHistory> nameHistories, List<StockFreeFloatShareHistory> freeFloatHistories) {
        String stockCode = stockDay.getStockCode();
        LocalDate tradeDate = parseTradeDate(stockDay);
        String stockName = findStockName(nameHistories, tradeDate);
        Long floatShares = findFreeShares(freeFloatHistories, tradeDate);

        int klineState = getKLineState(stockDay.getLimitPrice(), stockDay.getStoppingPrice(), stockDay.getOpenPrice(), stockDay.getLowPrice(), stockDay.getHighPrice(), stockDay.getClosePrice());
        Integer consecutiveLimitUpDays = calculateConsecutiveLimitUpDays(klineState, previousLimitUpDays);

        StockDailyEntity stockDaily = new StockDailyEntity();
        stockDaily.setStockCode(stockCode);
        stockDaily.setStockName(stockName);
        stockDaily.setIsSt(isStName(stockName));
        stockDaily.setTradeDate(tradeDate);
        stockDaily.setOpenPrice(stockDay.getOpenPrice());
        stockDaily.setHighPrice(stockDay.getHighPrice());
        stockDaily.setLowPrice(stockDay.getLowPrice());
        stockDaily.setClosePrice(stockDay.getClosePrice());
        stockDaily.setPrevClose(stockDay.getPreClosePrice());
        stockDaily.setAdjustPreClosePrice(stockDay.getAdjustPreClosePrice());
        stockDaily.setAdjustOpenPrice(stockDay.getAdjustOpenPrice());
        stockDaily.setAdjustHighPrice(stockDay.getAdjustHighPrice());
        stockDaily.setAdjustLowPrice(stockDay.getAdjustLowPrice());
        stockDaily.setAdjustClosePrice(stockDay.getAdjustClosePrice());
        stockDaily.setAdjustFactor(stockDay.getAdjustFactor());
        stockDaily.setVolume(toLong(stockDay.getVolume() * 100));
        stockDaily.setTurnover(stockDay.getTurnover() / 10);
        stockDaily.setTurnoverRate(calculateTurnoverRate(stockDaily.getVolume(), floatShares));
        stockDaily.setChangeRate(stockDay.getPercentChange());
        stockDaily.setAmplitude(calculateAmplitude(stockDay.getHighPrice(), stockDay.getLowPrice(), stockDay.getPreClosePrice()));
        stockDaily.setFloatMarketCap(calculateMarketCap(stockDay.getClosePrice(), floatShares));
        stockDaily.setFloatShares(floatShares);
        stockDaily.setKlineState(klineState);
        stockDaily.setConsecutiveLimitUpDays(consecutiveLimitUpDays);
        return stockDaily;
    }

    /**
     * 构建单条每日股票日 K 记录。
     * 处理逻辑：涨停时用昨日涨停池递增；昨日涨停但今日未涨停时记录负数断板高度。
     */
    private StockDailyEntity buildStockDaily(StockDayQuotationPoint stockDay, Map<String, Integer> yesterdayLimitUpMap, List<StockPreviousNameHistory> nameHistories, List<StockFreeFloatShareHistory> freeFloatHistories) {
        StockDailyEntity stockDaily = buildStockDaily(stockDay, 0, nameHistories, freeFloatHistories);
        String stockCode = stockDay.getStockCode();
        Integer yesterdayLimitUpDays = yesterdayLimitUpMap.get(stockCode);
        if (stockDaily.getKlineState() != null && stockDaily.getKlineState() >= 1 && stockDaily.getKlineState() <= 5) {
            stockDaily.setConsecutiveLimitUpDays(yesterdayLimitUpDays == null ? 1 : yesterdayLimitUpDays + 1);
        } else if (yesterdayLimitUpDays != null) {
            stockDaily.setConsecutiveLimitUpDays(-yesterdayLimitUpDays);
        } else {
            stockDaily.setConsecutiveLimitUpDays(0);
        }
        return stockDaily;
    }

    /**
     * 解析交易日期。
     * 作用：把华鑫 yyyyMMdd 日期转换成 LocalDate。
     */
    private LocalDate parseTradeDate(StockDayQuotationPoint stockDay) {
        return LocalDate.parse(stockDay.getTradingDay(), API_DATE_FORMATTER);
    }

    /**
     * 从内存曾用名区间中查找指定交易日名称。
     */
    private String findStockName(List<StockPreviousNameHistory> nameHistories, LocalDate tradeDate) {
        return nameHistories.stream()
                .filter(history -> containsDate(history.getStartDate(), history.getEndDate(), tradeDate))
                .map(StockPreviousNameHistory::getStockName)
                .findFirst()
                .orElse(null);
    }

    /**
     * 从内存自由流通股区间中查找指定交易日流通股本。
     */
    private Long findFreeShares(List<StockFreeFloatShareHistory> freeFloatHistories, LocalDate tradeDate) {
        return freeFloatHistories.stream()
                .filter(history -> containsDate(history.getStartDate(), history.getEndDate(), tradeDate))
                .map(StockFreeFloatShareHistory::getFreeShares)
                .findFirst()
                .orElse(null);
    }

    /**
     * 判断日期是否落在开始结束区间内。
     * 结束日期为空表示当前仍有效。
     */
    private boolean containsDate(LocalDate startDate, LocalDate endDate, LocalDate tradeDate) {
        return !startDate.isAfter(tradeDate) && (endDate == null || !endDate.isBefore(tradeDate));
    }

    /**
     * 判断股票名称是否为 ST。
     * 规则：当日名称包含 ST 即视为 ST。
     */
    private Boolean isStName(String stockName) {
        return stockName != null && stockName.toUpperCase().contains("ST") || stockName.contains("退");
    }

    /**
     * 计算连板天数或断板高度。
     * 规则：涨停状态 1 到 5 递增；昨天涨停今天未涨停则记录负数断板高度。
     */
    private Integer calculateConsecutiveLimitUpDays(int klineState, int previousLimitUpDays) {
        if (klineState >= 1 && klineState <= 5) {
            return previousLimitUpDays > 0 ? previousLimitUpDays + 1 : 1;
        }
        if (previousLimitUpDays > 0) {
            return -previousLimitUpDays;
        }
        return 0;
    }

    /**
     * 计算日 K 线状态。
     * 状态覆盖涨停、跌停、涨停炸板、跌停炸板等形态。
     */
    private int getKLineState(double limitPrice, double stoppingPrice, double openPrice, double lowPrice, double highPrice, double closePrice) {
        boolean closeLimitUp = equalsPrice(closePrice, limitPrice);
        boolean highLimitUp = equalsPrice(highPrice, limitPrice);
        boolean openLimitUp = equalsPrice(openPrice, limitPrice);
        boolean lowLimitUp = equalsPrice(lowPrice, limitPrice);
        boolean lowLimitDown = equalsPrice(lowPrice, stoppingPrice);
        boolean closeLimitDown = equalsPrice(closePrice, stoppingPrice);
        boolean openLimitDown = equalsPrice(openPrice, stoppingPrice);
        boolean highLimitDown = equalsPrice(highPrice, stoppingPrice);

        if (closeLimitUp && highLimitUp) {
            if (openLimitUp && lowLimitUp) {
                return 3;
            }
            if (openLimitUp && lowLimitDown) {
                return 5;
            }
            if (openLimitUp) {
                return 2;
            }
            if (lowLimitDown) {
                return 4;
            }
            return 1;
        }

        if (closeLimitDown && lowLimitDown) {
            if (openLimitDown && highLimitDown) {
                return -3;
            }
            if (openLimitDown && highLimitUp) {
                return -5;
            }
            if (openLimitDown) {
                return -2;
            }
            if (highLimitUp) {
                return -4;
            }
            return -1;
        }

        if (highLimitUp && !closeLimitUp) {
            if (lowLimitDown) {
                return 13;
            }
            if (openLimitUp) {
                return 12;
            }
            return 11;
        }

        if (lowLimitDown && !closeLimitDown) {
            if (openLimitDown) {
                return -12;
            }
            return -11;
        }

        return 0;
    }

    /**
     * 判断两个价格是否相等。
     * 处理逻辑：保留两位小数后比较，避免浮点误差。
     */
    private boolean equalsPrice(double left, double right) {
        return BigDecimal.valueOf(left).setScale(2, RoundingMode.HALF_UP)
                .compareTo(BigDecimal.valueOf(right).setScale(2, RoundingMode.HALF_UP)) == 0;
    }

    /**
     * 计算振幅。
     * 公式：(最高价 - 最低价) / 昨收价 * 100，单位为百分比。
     */
    private Double calculateAmplitude(double highPrice, double lowPrice, double preClosePrice) {
        if (preClosePrice == 0) {
            return null;
        }
        return BigDecimal.valueOf(highPrice)
                .subtract(BigDecimal.valueOf(lowPrice))
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(preClosePrice), 4, RoundingMode.HALF_UP)
                .setScale(4, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * 计算市值。
     * 公式：收盘价乘以股本再除以 10000，单位为万元。
     */
    private Double calculateMarketCap(double closePrice, Long shares) {
        if (shares == null) {
            return null;
        }
        return BigDecimal.valueOf(closePrice)
                .multiply(BigDecimal.valueOf(shares))
                .divide(BigDecimal.valueOf(10000), 4, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * 计算真实换手率。
     * 公式：成交股数除以流通股本再乘以 100。
     */
    private Double calculateTurnoverRate(Long volume, Long floatShares) {
        if (volume == null || floatShares == null || floatShares == 0) {
            return null;
        }
        return BigDecimal.valueOf(volume)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(floatShares), 4, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * 把 double 数值转换成 Long。
     */
    private Long toLong(double value) {
        return BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP).longValue();
    }
}
