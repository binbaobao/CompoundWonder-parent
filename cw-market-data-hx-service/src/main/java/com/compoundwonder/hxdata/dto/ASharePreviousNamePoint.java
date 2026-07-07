package com.compoundwonder.hxdata.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * A 股曾用名变更点。
 * 作用：把华鑫曾用名回调字段复制成普通 Java 对象，避免后续业务依赖 SDK 对象生命周期。
 */
@Data
public class ASharePreviousNamePoint {

    /**
     * 股票代码。
     */
    private String stockCode;

    /**
     * 股票名称。
     */
    private String stockName;

    /**
     * 名称开始使用日期。
     */
    private LocalDate startDate;

    /**
     * 华鑫返回的结束日期。
     * 说明：每日更新时当前区间结束日由本系统维护，这个字段仅用于观察和排查。
     */
    private LocalDate sourceEndDate;

    /**
     * 公告日期。
     */
    private LocalDate announcementDate;

    /**
     * 变动原因编码。
     */
    private Integer changeReason;
}
