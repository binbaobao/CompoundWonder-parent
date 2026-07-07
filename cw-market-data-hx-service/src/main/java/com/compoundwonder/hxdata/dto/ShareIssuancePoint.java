package com.compoundwonder.hxdata.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * A 股发行上市信息快照。
 * 作用：承接华鑫发行信息回调，用于新增曾用名和同步任务。
 */
@Data
public class ShareIssuancePoint {

    /**
     * 股票代码。
     */
    private String stockCode;

    /**
     * 股票名称。
     */
    private String stockName;

    /**
     * 上市日期。
     */
    private LocalDate listDate;

    /**
     * 上市板块名称。
     */
    private String listBoardName;
}
