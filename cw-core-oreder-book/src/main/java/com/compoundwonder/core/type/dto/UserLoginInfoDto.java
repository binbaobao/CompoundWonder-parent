package com.compoundwonder.core.type.dto;

import lombok.Data;

/**
 * 用户登录信息
 */
@Data
public class UserLoginInfoDto {

    /**
     * 用户请求编号
     */
    private int userRequestID;

    /**
     * 经纪公司部门代码
     */
    private String departmentID;

    /**
     * 登录账户
     */
    private String logInAccount;

    /**
     * 登录账户类型
     */
    private char logInAccountType;

    /**
     * 前置编号
     */
    private int frontID;

    /**
     * 会话编号
     */
    private int sessionID;

    /**
     * 最大报单引用
     */
    private int maxOrderRef;

    /**
     * 私有流长度
     */
    private int privateFlowCount;

    /**
     * 公有流长度
     */
    private int publicFlowCount;

    /**
     * 登录时间
     */
    private String loginTime;

    /**
     * 交易系统名称
     */
    private String systemName;

    /**
     * 交易日
     */
    private String tradingDay;

    /**
     * 用户代码
     */
    private String userID;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户类型
     */
    private char userType;

    /**
     * 报单流控
     */
    private int orderInsertCommFlux;

    /**
     * 撤单流控
     */
    private int orderActionCommFlux;

    /**
     * 密码到期日期
     */
    private String passwordExpiryDate;

    /**
     * 是否需要改密
     */
    private int needUpdatePassword;

    /**
     * 认证序列号
     */
    private String certSerial;

    /**
     * 内网IP地址
     */
    private String innerIPAddress;

    /**
     * 外网IP地址
     */
    private String outerIPAddress;

    /**
     * Mac地址
     */
    private String macAddress;

    /**
     * 关联节点编号(内部使用)
     */
    private int nodeRef;

    /**
     * 交易流控
     */
    private int tradeCommFlux;

    /**
     * 查询流控
     */
    private int queryCommFlux;
}
