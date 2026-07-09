package com.compoundwonder.hxdata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 股票可转债历史实体。
 * 作用：维护正股和可转债的历史对应关系及转债生命周期区间。
 */
@Data
@TableName("stock_convertible_bond_history")
public class StockConvertibleBondHistory {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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

    /**
     * 创建时间。
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedTime;
}
