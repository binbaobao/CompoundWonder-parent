package com.compoundwonder.hxdata.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 自由流通股本原始时间点。
 * 作用：承接华鑫接口返回的某一天自由流通股本数据，再由服务层压缩成历史区间。
 */
@Data
public class FreeFloatSharePoint {

    /**
     * 股票代码。
     */
    private String stockCode;

    /**
     * 自由流通股本，接口原始单位为万股。
     */
    private BigDecimal freeSharesTenThousand;

    /**
     * 生效日期，对应接口 ChangeDateEX。
     */
    private LocalDate changeDate;

    /**
     * 公告日期，对应接口 AnnouncementDate。
     */
    private LocalDate announcementDate;
}
