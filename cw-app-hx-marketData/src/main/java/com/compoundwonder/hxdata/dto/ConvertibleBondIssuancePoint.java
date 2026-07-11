package com.compoundwonder.hxdata.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 可转债发行关系点。
 * 作用：承接发行接口返回的正股和转债对应关系。
 */
@Data
public class ConvertibleBondIssuancePoint {

    /**
     * 正股代码。
     */
    private String stockCode;

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
     * 转债上市日期。
     */
    private LocalDate listDate;
}
