package com.compoundwonder.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 股票信息
 *
 * @author chaobin
 * @since 1.0.0 2024-08-20
 */
@Data
public class ShareInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // "")
    private Long id;

    // "代码")
    private String dm;

    // "转债 0无，1有")
    private Integer zz;

    // "融资融券 0不能，1能")
    private Integer rz;

    // "市场类型（1.主板 2创业 3科创 4北郊 5st 6退市）")
    private Integer marketType;

    // "省份")
    private String province;

    // "收盘价格")
    private BigDecimal price;

    // "成交额（元）")
    private BigDecimal maxCje;

    // "股票名称")
    private String name;
    // "年最大真实")
    private double maxHs;

    // "最大成量")
    private Long maxVolume;

    // "总股本")
    private Long totalV;

    // "流通比例")
    private BigDecimal ltbl;

    // "涨停次数")
    private Integer limitUpTimes;

    // "黑名单（1，加入，0没有加入）")
    private Integer blacklist;

    // "")
    private Date updateDate;

    private List<LimitUpStockDTO> limitUpStockDTOList;

}
