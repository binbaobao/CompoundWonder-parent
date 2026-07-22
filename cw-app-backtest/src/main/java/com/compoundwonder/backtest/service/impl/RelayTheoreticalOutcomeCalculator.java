package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.common.mysqldata.selection.model.StockDailyData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** 只使用日 K 计算打板成本与首次收盘断板日理论最高收益。 */
public final class RelayTheoreticalOutcomeCalculator {
    static final int NOT_TOUCHED = 1;
    static final int OPEN = 2;
    static final int CLOSED = 3;
    static final int DATA_MISSING = 4;

    private RelayTheoreticalOutcomeCalculator() {
    }

    public static RelayTheoreticalOutcome evaluate(String stockCode,
                                                   boolean isSt,
                                                   LocalDate expectedBuyDate,
                                                   List<StockDailyData> dailyAfterSelection) {
        if (expectedBuyDate == null || dailyAfterSelection == null
                || dailyAfterSelection.isEmpty()) {
            return missing();
        }
        List<StockDailyData> ascending = dailyAfterSelection.stream()
                .filter(daily -> daily != null && daily.getTradeDate() != null)
                .sorted(Comparator.comparing(StockDailyData::getTradeDate))
                .toList();
        if (ascending.isEmpty()) {
            return missing();
        }

        StockDailyData buyDaily = ascending.get(0);
        if (!Objects.equals(expectedBuyDate, buyDaily.getTradeDate())) {
            return missing();
        }
        boolean buyDaySt = buyDaily.getIsSt() == null
                ? isSt : Boolean.TRUE.equals(buyDaily.getIsSt());
        boolean touched = isTouched(buyDaily.getKlineState());
        BigDecimal buyLimitPrice = touched
                ? decimal(buyDaily.getHighPrice())
                : calculateLimitPrice(stockCode, buyDaySt, buyDaily.getPrevClose());
        if (buyLimitPrice == null) {
            return missing();
        }
        boolean sealed = isSealed(buyDaily.getKlineState());
        if (!touched) {
            return new RelayTheoreticalOutcome(NOT_TOUCHED, buyLimitPrice, buyDaily,
                    false, false, 0, null, null, null, false,
                    null, null, null);
        }

        int sealedDays = 0;
        StockDailyData breakDaily = null;
        for (StockDailyData daily : ascending) {
            if (isSealed(daily.getKlineState())) {
                sealedDays++;
                continue;
            }
            breakDaily = daily;
            break;
        }
        if (breakDaily == null) {
            return new RelayTheoreticalOutcome(OPEN, buyLimitPrice, buyDaily,
                    true, sealed, sealedDays, null, null, null, false,
                    null, null, null);
        }

        BigDecimal sellPrice = decimal(breakDaily.getHighPrice());
        BigDecimal maxReturn = sellPrice == null ? null
                : sellPrice.subtract(buyLimitPrice)
                .divide(buyLimitPrice, 8, RoundingMode.HALF_UP);
        BigDecimal breakLimitPrice = calculateLimitPrice(stockCode,
                Boolean.TRUE.equals(breakDaily.getIsSt()), breakDaily.getPrevClose());
        return new RelayTheoreticalOutcome(CLOSED, buyLimitPrice, buyDaily,
                true, sealed, sealedDays, breakDaily.getTradeDate(), breakDaily,
                breakLimitPrice, isTouched(breakDaily.getKlineState()), sellPrice,
                maxReturn, maxReturn == null ? null : maxReturn.signum() > 0);
    }

    static BigDecimal calculateLimitPrice(String stockCode, boolean isSt, Double previousClose) {
        if (previousClose == null || previousClose <= 0D) {
            return null;
        }
        BigDecimal ratio;
        if (isSt) {
            ratio = new BigDecimal("1.05");
        } else if (stockCode != null && (stockCode.startsWith("300")
                || stockCode.startsWith("301") || stockCode.startsWith("688")
                || stockCode.startsWith("689"))) {
            ratio = new BigDecimal("1.20");
        } else if (stockCode != null && (stockCode.startsWith("4")
                || stockCode.startsWith("8"))) {
            ratio = new BigDecimal("1.30");
        } else {
            ratio = new BigDecimal("1.10");
        }
        return BigDecimal.valueOf(previousClose).multiply(ratio)
                .setScale(2, RoundingMode.HALF_UP);
    }

    static boolean isTouched(Integer klineState) {
        return klineState != null && klineState > 0;
    }

    static boolean isSealed(Integer klineState) {
        return klineState != null && klineState >= 1 && klineState <= 5;
    }

    private static BigDecimal decimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private static RelayTheoreticalOutcome missing() {
        return new RelayTheoreticalOutcome(DATA_MISSING, null, null,
                false, false, 0, null, null, null, false,
                null, null, null);
    }
}
