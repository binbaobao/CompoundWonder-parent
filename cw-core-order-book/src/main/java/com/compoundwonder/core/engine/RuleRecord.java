package com.compoundwonder.core.engine;


import com.compoundwonder.dto.RuleRecordDTO;
import com.compoundwonder.common.orderbook.TradeRuleRecord;

/**
 * Handler 私有缓冲区中复用的可写规则命中记录。
 *
 * <p>规则只有在返回 {@code true} 时才允许提交；每次重新评估前由
 * {@link RuleRecordBuffer#nextRecord()} 清空全部字段，避免未命中规则留下脏数据。</p>
 */
public final class RuleRecord implements TradeRuleRecord {

    /**
     * BUY / SELL / CANCEL / REBUY
     */
    public int actionType;

    /**
     * 规则编码。
     */
    public int ruleCode;

    /** 产生该记录的独立策略会话，用于同股多模式结果归属。 */
    public String strategySessionId;

    /** 稳定策略标识，例如 MODEL_1。 */
    public String strategyId;

    /** 回测交易模式编码。 */
    public int tradeMode;

    /**
     * 证券代码
     */
    public String symbol;

    /**
     * 交易日期
     */
    public String date;

    /**
     * 行情更新时间
     */
    public int time;

    /**
     * 最新价格
     */
    public int price;

    /**
     * 涨幅
     */
    public double increase;

    public String remark;

    public void reset() {
        this.actionType = 0;
        this.ruleCode = 0;
        strategySessionId = null;
        strategyId = null;
        tradeMode = 0;
        symbol = null;
        date = null;
        time = 0;
        price = 0;
        increase = 0;
        remark = null;
    }


    /** 规则命中后一次性填写交易动作、价格与诊断说明。 */
    @Override
    public void fill(int actionType, int ruleCode, String symbol, int time, int price, double increase, String remark) {

        this.actionType = actionType;
        this.ruleCode = ruleCode;
        this.symbol = symbol;
        this.time = time;
        this.increase = increase;
        this.price = price;
        this.remark = remark;

    }

    @Override
    public void bindExecutionContext(String strategySessionId, String strategyId, int tradeMode) {
        this.strategySessionId = strategySessionId;
        this.strategyId = strategyId;
        this.tradeMode = tradeMode;
    }

    /** 创建脱离复用缓冲区的传输对象，缓冲区清理不会影响返回结果。 */
    public RuleRecordDTO toDTO() {
        RuleRecordDTO dto = new RuleRecordDTO();

        dto.setActionType(this.actionType);
        dto.setRuleCode(this.ruleCode);
        dto.setStrategySessionId(this.strategySessionId);
        dto.setStrategyId(this.strategyId);
        dto.setTradeMode(this.tradeMode);
        dto.setSymbol(this.symbol);
        dto.setTime(this.time);
        dto.setIncrease(this.increase);
        dto.setPrice(this.price);
        dto.setRemark(this.remark);

        return dto;
    }

}
