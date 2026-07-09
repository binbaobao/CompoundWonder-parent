package com.compoundwonder.trader.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 规则执行记录。
 * 作用：记录交易规则触发后的买入、卖出、撤单、重买等执行信息。
 */
@Data
@TableName("rule_execute_record")
public class RuleExecuteRecord {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * BUY / SELL / CANCEL / REBUY
     */
    private Integer actionType;

    /**
     * 规则编码。
     */
    private Integer ruleCode;

    /**
     * 证券代码。
     */
    private String symbol;

    /**
     * 证券名称。
     */
    private String symbolName;

    /**
     * 交易日期。
     */
    private LocalDate tradeDate;

    /**
     * 下单时间。
     */
    private Integer time;

    /**
     * 最后委托时间。
     */
    private Integer lastOrderTime;

    /**
     * 下单价格。
     */
    private Integer price;

    /**
     * 涨幅。
     */
    private Double increase;

    /**
     * 交易说明。
     */
    private String remark;

    /**
     * 创建时间。
     */
    private LocalDateTime createdTime;
}
