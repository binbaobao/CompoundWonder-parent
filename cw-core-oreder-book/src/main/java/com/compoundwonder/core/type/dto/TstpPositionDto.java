package com.compoundwonder.core.type.dto;

import lombok.Data;

/**
 * 投资者持仓
 */
@Data
public class TstpPositionDto {

    /**
     * 交易所代码
     */
    private char exchangeID;

    /**
     * 投资者代码
     */
    private String investorID;

    /**
     * 投资单元代码
     */
    private String businessUnitID;

    /**
     * 市场代码
     */
    private char marketID;

    /**
     * 股东账户代码
     */
    private String shareholderID;

    /**
     * 交易日
     */
    private String tradingDay;

    /**
     * 证券代码
     */
    private String securityID;

    /**
     * 证券名称
     */
    private String securityName;

    /**
     * 昨仓
     */
    private int historyPos;

    /**
     * 昨仓冻结
     */
    private int historyPosFrozen;

    /**
     * 今买卖仓
     */
    private int todayBSPos;

    /**
     * 今买卖仓冻结
     */
    private int todayBSPosFrozen;

    /**
     * 今日申赎持仓
     */
    private int todayPRPos;

    /**
     * 今日申赎持仓冻结
     */
    private int todayPRPosFrozen;

    /**
     * 今拆分合并持仓
     */
    private int todaySMPos;

    /**
     * 今拆分合并持仓冻结
     */
    private int todaySMPosFrozen;

    /**
     * 昨仓成本价
     */
    private double historyPosPrice;

    /**
     * 持仓成本
     */
    private double totalPosCost;

    /**
     * 上次余额(盘中不变)
     */
    private int prePosition;

    /**
     * 股份可用
     */
    private int availablePosition;

    /**
     * 股份余额
     */
    private int currentPosition;

    /**
     * 开仓成本
     */
    private double openPosCost;

    /**
     * 融资仓位(两融专用)
     */
    private int creditBuyPos;

    /**
     * 融券仓位(两融专用)
     */
    private int creditSellPos;

    /**
     * 今日融券仓位(两融专用)
     */
    private int todayCreditSellPos;

    /**
     * 划出仓位(两融专用)
     */
    private int collateralOutPos;

    /**
     * 还券未成交数量(两融专用)
     */
    private int repayUntradeVolume;

    /**
     * 直接还券未成交数量(两融专用)
     */
    private int repayTransferUntradeVolume;

    /**
     * 担保品买入未成交金额(两融专用)
     */
    private double collateralBuyUntradeAmount;

    /**
     * 担保品买入未成交数量(两融专用)
     */
    private int collateralBuyUntradeVolume;

    /**
     * 融资买入金额(包含交易费用)(两融专用)
     */
    private double creditBuyAmount;

    /**
     * 融资买入未成交金额(包含交易费用)(两融专用)
     */
    private double creditBuyUntradeAmount;

    /**
     * 融资冻结保证金(两融专用)
     */
    private double creditBuyFrozenMargin;

    /**
     * 融资买入利息(两融专用)
     */
    private double creditBuyInterestFee;

    /**
     * 融资买入未成交数量(两融专用)
     */
    private int creditBuyUntradeVolume;

    /**
     * 融券卖出金额(以成交价计算)(两融专用)
     */
    private double creditSellAmount;

    /**
     * 融券卖出未成交金额(两融专用)
     */
    private double creditSellUntradeAmount;

    /**
     * 融券冻结保证金(两融专用)
     */
    private double creditSellFrozenMargin;

    /**
     * 融券卖出息费(两融专用)
     */
    private double creditSellInterestFee;

    /**
     * 融券卖出未成交数量(两融专用)
     */
    private int creditSellUntradeVolume;

    /**
     * 划入待收仓(两融专用)
     */
    private int collateralInPos;

    /**
     * 融资流动冻结保证金(两融专用)
     */
    private double creditBuyFrozenCirculateMargin;

    /**
     * 融券流动冻结保证金(两融专用)
     */
    private double creditSellFrozenCirculateMargin;

    /**
     * 累计平仓盈亏(两融专用)
     */
    private double closeProfit;

    /**
     * 当日累计开仓数量
     */
    private int todayTotalOpenVolume;

    /**
     * 今手续费
     */
    private double todayCommission;

    /**
     * 当日累计买入金额
     */
    private double todayTotalBuyAmount;

    /**
     * 当日累计卖出金额
     */
    private double todayTotalSellAmount;

    /**
     * 上日冻结(盘中不变)
     */
    private int preFrozen;

    /**
     * 当日累计平仓数量
     */
    private int todayTotalCloseVolume;

}
