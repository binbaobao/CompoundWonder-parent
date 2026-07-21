package com.compoundwonder.trader.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 单模式回测中一只候选股票的独立买卖样本。 */
@Data
@TableName("single_mode_backtest_sample")
public class SingleModeBacktestSample {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long runId;
    private String symbol;
    private String symbolName;
    private Integer tradeMode;
    private Integer limitUpScore;
    private LocalDate recommendDate;
    private LocalDate tradeDate;
    private Integer selectionBoard;
    /** 1 已选出，2 未买入，3 持仓至数据末尾，4 已卖出，5 数据异常。 */
    private Integer status;
    private String noBuyReason;
    private LocalDate buyDate;
    private Integer buyTime;
    private Integer buyPrice;
    private Integer buyRuleCode;
    private String buyRemark;
    private Integer buyDayKlineState;
    private LocalDate sellDate;
    private Integer sellTime;
    private Integer sellPrice;
    private Integer sellRuleCode;
    private String sellRemark;
    private Integer sellBoard;
    private Integer holdingTradeDays;
    private BigDecimal returnRate;
    private BigDecimal maxFloatingReturnRate;
    private BigDecimal maxDrawdownRate;
    private Integer maxSealedBoards;
    private Integer maxTouchedBoards;
    private BigDecimal potentialMaxReturnRate;
    private BigDecimal postSellMaxReturnRate;
    private LocalDate sampleEndDate;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
