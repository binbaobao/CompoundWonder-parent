package com.compoundwonder.backtest.service.model;

import java.math.BigDecimal;

/** 从某一板晋级下一板的触板、封板和炸板统计。 */
public record SingleModeBoardStat(int fromBoard, int eligibleCount, int touchCount,
                                  int sealedCount, int breakCount,
                                  BigDecimal touchRate, BigDecimal sealRate,
                                  BigDecimal breakRate) {
}
