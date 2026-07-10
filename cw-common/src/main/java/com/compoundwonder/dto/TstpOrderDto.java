package com.compoundwonder.dto;

import lombok.Data;

/**
 * 报单
 */
@Data
public class TstpOrderDto {

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
     * 股东账户代码
     */
    private String shareholderID;

    /**
     * 证券代码
     */
    private String securityID;

    /**
     * 买卖方向
     */
    private char direction;

    /**
     * 报单价格条件
     */
    private char orderPriceType;

    /**
     * 有效期类型
     */
    private char timeCondition;

    /**
     * 成交量类型
     */
    private char volumeCondition;

    /**
     * 价格
     */
    private double limitPrice;

    /**
     * 数量
     */
    private int volumeTotalOriginal;

    /**
     * 港股通订单数量类型
     */
    private char lotType;

    /**
     * 有效日期
     */
    private String gTDate;

    /**
     * 委托方式
     */
    private char operway;

    /**
     * 条件检查
     */
    private char condCheck;

    /**
     * 字符串附加信息
     */
    private String sInfo;

    /**
     * 整形附加信息
     */
    private int iInfo;

    /**
     * 请求编号
     */
    private int requestID;

    /**
     * 前置编号
     */
    private int frontID;

    /**
     * 会话编号
     */
    private int sessionID;

    /**
     * 报单引用
     */
    private int orderRef;

    /**
     * 本地报单编号
     */
    private String orderLocalID;

    /**
     * 系统报单编号
     */
    private String orderSysID;

    /**
     * 报单状态
     * 预埋 '0'
     * 未知 '1'
     * 交易所已接收 '2'
     * 部分成交 '3'
     * 全部成交 '4'
     * 部成部撤 '5'
     * 全部撤单 '6'
     * 交易所已拒绝 '7'
     */
    private char orderStatus;

    /**
     * 报单提交状态
     */
    private char orderSubmitStatus;

    /**
     * 状态信息
     */
    private String statusMsg;

    /**
     * 已成交数量
     */
    private int volumeTraded;

    /**
     * 已撤销数量
     */
    private int volumeCanceled;

    /**
     * 交易日
     */
    private String tradingDay;

    /**
     * 申报用户
     */
    private String insertUser;

    /**
     * 申报日期
     */
    private String insertDate;

    /**
     * 申报时间
     */
    private String insertTime;

    /**
     * 交易所接收时间
     */
    private String acceptTime;

    /**
     * 撤销用户
     */
    private String cancelUser;

    /**
     * 撤销时间
     */
    private String cancelTime;

    /**
     * 经纪公司部门代码
     */
    private String departmentID;

    /**
     * 资金账户代码
     */
    private String accountID;

    /**
     * 币种
     */
    private char currencyID;

    /**
     * 交易单元代码
     */
    private String pbuID;

    /**
     * 成交金额
     */
    private double turnover;

    /**
     * 报单类型
     */
    private char orderType;

    /**
     * 用户端产品信息
     */
    private String userProductInfo;

    /**
     * 强平原因(两融专用)
     */
    private char forceCloseReason;

    /**
     * 信用头寸编号(两融专用)
     */
    private String creditQuotaID;

    /**
     * 头寸类型(两融专用)
     */
    private char creditQuotaType;

    /**
     * 信用负债编号(两融专用)
     */
    private String creditDebtID;

    /**
     * IP地址
     */
    private String iPAddress;

    /**
     * Mac地址
     */
    private String macAddress;

    /**
     * 回报附加浮点型数据信息
     */
    private double rtnFloatInfo;

    /**
     * 回报附加整型数据
     */
    private int rtnIntInfo;

    /**
     * 回报附加浮点型数据1
     */
    private double rtnFloatInfo1;

    /**
     * 回报附加浮点型数据2
     */
    private double rtnFloatInfo2;

    /**
     * 回报附加浮点型数据3
     */
    private double rtnFloatInfo3;
    /**
     * 交易所接受时间
     */
    private long acceptTimeStamp;

}
