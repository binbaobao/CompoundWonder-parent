package com.compoundwonder.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 规则记录 DTO
 */
@Data

public class RuleRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * BUY / SELL / CANCEL / REBUY
     */
    // "操作类型")
    private Integer actionType;

    // "规则编码")
    private Integer ruleCode;

    private String strategySessionId;

    private String strategyId;

    private Integer tradeMode;

    // "证券代码")
    private String symbol;

    // "行情更新时间")
    private Integer time;

    // "最后委托时间")
    private Integer lastOrderTime;

    // "下单价格")
    private Integer price;

    // "涨幅")
    private Double increase;

    // "交易说明")
    private String remark;

}
