package com.compoundwonder.trader.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 回测中一次买入至卖出的完整持仓生命周期。
 */
@Data
@TableName("backtest_position")
public class BacktestPosition {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long backtestRunId;
    private Long watchingTaskId;
    private String symbol;
    private String symbolName;
    private Integer tradeMode;
    private Integer limitUpScore;
    private LocalDate buyDate;
    private Integer buyTime;
    private Integer buyPrice;
    private Integer quantity;
    private BigDecimal buyAmount;
    private BigDecimal buyFee;
    private LocalDate sellDate;
    private Integer sellTime;
    private Integer sellPrice;
    private BigDecimal sellAmount;
    private BigDecimal sellFee;
    private Integer status;
    private Integer holdingTradeDays;
    private BigDecimal realizedProfit;
    private BigDecimal returnRate;
    private BigDecimal maxFloatingReturnRate;
    private BigDecimal maxDrawdownRate;
    private Integer limitUpBreakDays;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
