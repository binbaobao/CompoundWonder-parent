package com.compoundwonder.hxdata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 股票当前状态实体。
 * 作用：维护实盘盘前过滤会用、但暂时不做历史区间维护的当前属性。
 */
@Data
@TableName("stock_current_status")
public class StockCurrentStatus {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 股票代码。
     */
    private String stockCode;

    /**
     * 是否融资融券。
     */
    private Boolean marginTrading;

    /**
     * 是否有可转债。
     */
    private Boolean convertibleBond;

    /**
     * 地域名称。
     */
    private String regionName;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedTime;
}
