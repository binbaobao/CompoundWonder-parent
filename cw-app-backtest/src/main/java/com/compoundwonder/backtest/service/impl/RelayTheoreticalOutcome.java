package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.common.mysqldata.selection.model.StockDailyData;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 从 D+1 到首次收盘断板的日 K 理论结果。 */
public record RelayTheoreticalOutcome(int status,
                                      BigDecimal buyLimitPrice,
                                      StockDailyData buyDaily,
                                      boolean touchedLimitUp,
                                      boolean sealedLimitUp,
                                      int postSelectionSealedDays,
                                      LocalDate breakDate,
                                      StockDailyData breakDaily,
                                      BigDecimal breakDayLimitPrice,
                                      boolean breakDayTouchedLimitUp,
                                      BigDecimal theoreticalMaxSellPrice,
                                      BigDecimal theoreticalMaxReturnRate,
                                      Boolean theoreticalWin) {
}
