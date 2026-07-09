package com.compoundwonder.core.type.dto;

import lombok.Data;

/**
 * 成交回报
 */
@Data
public class TstpTradeDto {

    /**
     * 交易所代码
     */
    private char exchangeID;

    /**
     * 经纪公司部门代码
     */
    private String departmentID;

    /**
     * 投资者代码
     */
    private String investorID;

    /**
     * 投资单元代码
     */
    private String businessUnitID;

    /**
     * 股东账户代码
     */
    private String shareholderID;

    /**
     * 证券代码
     */
    private String securityID;

    /**
     * 成交编号
     */
    private String tradeID;

    /**
     * 买卖方向
     */
    private char direction;

    /**
     * 系统报单编号
     */
    private String orderSysID;

    /**
     * 本地报单编号
     */
    private String orderLocalID;

    /**
     * 成交价格
     */
    private double price;

    /**
     * 成交数量
     */
    private int volume;

    /**
     * 成交日期
     */
    private String tradeDate;

    /**
     * 成交时间
     */
    private String tradeTime;

    /**
     * 交易日
     */
    private String tradingDay;

    /**
     * 交易单元代码
     */
    private String pbuID;

    /**
     * 报单引用
     */
    private int orderRef;

    /**
     * 资金账户代码
     */
    private String accountID;

    /**
     * 币种
     */
    private char currencyID;

}
