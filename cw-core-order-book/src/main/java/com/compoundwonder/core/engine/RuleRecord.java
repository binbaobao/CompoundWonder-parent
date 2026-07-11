package com.compoundwonder.core.engine;


import com.compoundwonder.dto.RuleRecordDTO;

public final class RuleRecord {

    /**
     * BUY / SELL / CANCEL / REBUY
     */
    public int actionType;

    /**
     * 规则编码。
     */
    public int ruleCode;

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
        symbol = null;
        date = null;
        time = 0;
        increase = 0;
    }


    /**
     * 填充记录规则的
     *
     */
    public void fill(int actionType, int ruleCode, String symbol, int time, int price, double increase, String remark) {

        this.actionType = actionType;
        this.ruleCode = ruleCode;
        this.symbol = symbol;
        this.time = time;
        this.increase = increase;
        this.price = price;
        this.remark = remark;

    }

    public RuleRecordDTO toDTO() {
        RuleRecordDTO dto = new RuleRecordDTO();

        dto.setActionType(this.actionType);
        dto.setRuleCode(this.ruleCode);
        dto.setSymbol(this.symbol);
        dto.setTime(this.time);
        dto.setIncrease(this.increase);
        dto.setPrice(this.price);
        dto.setRemark(this.remark);

        return dto;
    }

}
