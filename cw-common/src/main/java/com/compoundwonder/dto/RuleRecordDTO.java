package com.compoundwonder.dto;

import lombok.Data;

import java.io.Serializable;

/** 规则命中记录的跨模块传输对象。价格单位为分，时间格式为 {@code HHmmssSSS}。 */
@Data
public class RuleRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** BUY / SELL / CANCEL / REBUY 对应的动作编码。 */
    private Integer actionType;

    /** 当前交易模式内稳定的规则编码。 */
    private Integer ruleCode;

    /** 单只股票、单个交易日、单个策略的执行会话标识。 */
    private String strategySessionId;

    /** 稳定策略标识，例如 MODEL_1。 */
    private String strategyId;

    /** 交易模式编码：1 连板、2 普通首板、3 小市值首板。 */
    private Integer tradeMode;

    /** 六位证券代码。 */
    private String symbol;

    /** 规则触发时的行情时间。 */
    private Integer time;

    /** 回放结束时同价位队首委托时间，用于判断隔夜排队是否可成交。 */
    private Integer lastOrderTime;

    /** 下单或触发价格，单位为分。 */
    private Integer price;

    /** 触发时相对昨收的涨跌幅，单位为百分比。 */
    private Double increase;

    /** 规则命中条件及股票、日期等诊断说明。 */
    private String remark;
}
