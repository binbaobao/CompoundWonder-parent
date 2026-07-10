package com.compoundwonder.dto;

import lombok.Data;

import java.util.Map;

/**
 * Level2 3 秒分时 tick 数据。
 */
@Data
public class Level2MinuteTickDTO {
    private Long timestamp;
    private String tickTime;
    private Integer dataType;
    private Object rawTime;
    private Double rawPrice;
    private Double sellPrice;
    private Double price;
    private Long amount;
    private Long quantity;
    private Long buyerOrderId;
    private Long sellerOrderId;
    private Object symbolId;
    private Object handlerIndex;
    private Map<String, Object> rawFields;
}
