package com.compoundwonder.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 资金账户
 */
@Data
public class TradingAccountDto {

    /**
     * 经纪公司部门代码
     */
    private String departmentID;

    /**
     * 资金账户代码
     */
    private String accountID;

    /**
     * 币种代码
     */
    private char currencyID;

    /**
     * 上日结存
     */
    private double preDeposit;

    /**
     * 可用资金
     */
    private double usefulMoney;

    /**
     * 可取资金
     */
    private double fetchLimit;

    /**
     * 上日未交收金额(港股通专用字段)
     */
    private double preUnDeliveredMoney;

    /**
     * 可用未交收金额(港股通专用字段)
     */
    private double unDeliveredMoney;

    /**
     * 当日入金金额
     */
    private double deposit;

    /**
     * 当日出金金额
     */
    private double withdraw;

    /**
     * 冻结的资金(港股通该字段不包括未交收部分冻结资金)
     */
    private double frozenCash;

    /**
     * 冻结未交收金额(港股通专用)
     */
    private double unDeliveredFrozenCash;

    /**
     * 冻结的手续费(港股通该字段不包括未交收部分冻结手续费)
     */
    private double frozenCommission;

    /**
     * 冻结未交收手续费(港股通专用)
     */
    private double unDeliveredFrozenCommission;

    /**
     * 手续费(港股通该字段不包括未交收部分手续费)
     */
    private double commission;

    /**
     * 占用未交收手续费(港股通专用)
     */
    private double unDeliveredCommission;

    /**
     * 资金账户类型
     */
    private char accountType;

    /**
     * 资金账户所属投资者代码
     */
    private String investorID;

    /**
     * 银行代码
     */
    private char bankID;

    /**
     * 银行账户
     */
    private String bankAccountID;

    /**
     * 权利金收入(两融专用)
     */
    private double royaltyIn;

    /**
     * 权利金支出(两融专用)
     */
    private double royaltyOut;

    /**
     * 融券卖出金额(两融专用)
     */
    private double creditSellAmount;

    /**
     * 融券卖出使用金额(用于偿还融资负债或买特殊品种的金额)(两融专用)
     */
    private double creditSellUseAmount;

    /**
     * 虚拟资产(两融专用)
     */
    private double virtualAssets;

    /**
     * 融券卖出金额冻结(用于偿还融资负债或买特殊品种的未成交冻结金额)(两融专用)
     */
    private double creditSellFrozenAmount;

    /**
     * 属主单元
     */
    private String ownerUnit;
    //	const char TORA_TSTP_MKD_SHA = '1';
    //	///深圳A股
    //	const char TORA_TSTP_MKD_SZA = '2';
    private Map<Character, String> stringStringMap = new HashMap<>();
}
