package com.compoundwonder.hxdata.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 可转债基本资料点。
 * 作用：承接基本资料接口返回的上市、退市、到期和余额等生命周期字段。
 */
@Data
public class ConvertibleBondDescriptionPoint {

    /**
     * 转债代码。
     */
    private String bondCode;

    /**
     * 转债市场。
     */
    private String market;

    /**
     * 转债名称。
     */
    private String bondName;

    /**
     * 上市日期。
     */
    private LocalDate startDate;

    /**
     * 退市或到期日期。
     */
    private LocalDate endDate;

    /**
     * 到期日期。
     */
    private LocalDate maturityDate;

    /**
     * 是否失效。
     */
    private Integer failure;

    /**
     * 存续余额。
     */
    private Double outstandingBalance;
}
