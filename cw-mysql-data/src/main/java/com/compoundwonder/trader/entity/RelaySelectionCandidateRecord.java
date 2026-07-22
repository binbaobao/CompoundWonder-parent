package com.compoundwonder.trader.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 触发板数池中一只股票的选股快照、过滤轨迹和后续日 K 理论结果。 */
@Data
@TableName("relay_selection_candidate_record")
public class RelaySelectionCandidateRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long runId;
    private Long triggerRecordId;
    private LocalDate recommendDate;
    private LocalDate tradeDate;
    private String stockCode;
    private String stockName;
    private Integer candidateBoard;
    private String selectionStrength;
    private Boolean isSt;
    private Boolean hasConvertibleBond;
    private String province;
    private BigDecimal changeRate;
    private BigDecimal floatMarketCap;
    private BigDecimal currentPrice;
    private BigDecimal startMarketCap;
    private BigDecimal startPrice;
    private BigDecimal currentTurnoverRate;
    private BigDecimal currentTurnover;
    private BigDecimal currentAmplitude;
    private Integer nonStMonthCount;
    private Integer listingMonthCount;
    private BigDecimal maxTurnoverRate;
    private Integer historicalHighestBoard;
    private Integer priorNinetyDayHighestBoard;
    private BigDecimal priorNinetyDayMaxTurnoverRate;
    private Long historicalMaxVolume;
    private BigDecimal maxVolumeDayTurnoverRate;
    private BigDecimal maxVolumeDayTurnover;
    private Boolean twoAcceleratedShrinkVolumeLimitUps;
    private Integer abnormalKlineStateCount;
    private Integer priorTwentyDayAbnormalKlineCount;
    private BigDecimal fiveDayAmplitude;
    private BigDecimal tenDayChangeRate;
    /** 0 未完成指标，1 过滤拒绝，2 合格未入选，3 Top3入选。 */
    private Integer decisionStatus;
    private Boolean filterPassed;
    private String firstRejectStage;
    private String firstRejectReason;
    private String decisionTrace;
    private String scoreDetail;
    private Integer selectionScore;
    private Integer boardRank;
    private Integer finalRank;
    private Boolean isSelected;
    /** 0 待计算，1 D+1未触板，2 持续连板待断，3 已断板完成，4 数据缺失。 */
    private Integer outcomeStatus;
    private BigDecimal buyLimitPrice;
    private BigDecimal buyDayHighPrice;
    private BigDecimal buyDayClosePrice;
    private Integer buyDayKlineState;
    private Boolean isTouchedLimitUp;
    private Boolean isSealedLimitUp;
    private Integer postSelectionSealedDays;
    private LocalDate breakDate;
    private BigDecimal breakDayOpenPrice;
    private BigDecimal breakDayHighPrice;
    private BigDecimal breakDayLowPrice;
    private BigDecimal breakDayClosePrice;
    private BigDecimal breakDayLimitPrice;
    private Integer breakDayKlineState;
    private Boolean isBreakDayTouchedLimitUp;
    private BigDecimal theoreticalMaxSellPrice;
    private BigDecimal theoreticalMaxReturnRate;
    private Boolean isTheoreticalWin;
    private Boolean isDailyTheoreticalBest;
    private LocalDateTime outcomeUpdatedTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
