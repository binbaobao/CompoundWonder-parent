package com.compoundwonder.core.engine;

import com.compoundwonder.constant.MarketEnum;

/** 单只股票、单个交易日不随逐笔行情变化的市场机械参数。 */
public final class MarketSessionSpec {
    private final String symbol;
    private final MarketEnum market;
    private final String date;
    private final long circulation;
    private String securityName;
    private int closePrice;
    private int limitUpPrice;
    private int limitDownPrice;

    public MarketSessionSpec(String symbol, String securityName, MarketEnum market,
                             String date, long circulation, int closePrice,
                             int limitUpPrice, int limitDownPrice) {
        if (symbol == null || symbol.isBlank() || market == null || circulation <= 0) {
            throw new IllegalArgumentException("市场会话基础参数无效");
        }
        validatePriceLimits(closePrice, limitUpPrice, limitDownPrice);
        this.symbol = symbol;
        this.securityName = securityName;
        this.market = market;
        this.date = date;
        this.circulation = circulation;
        this.closePrice = closePrice;
        this.limitUpPrice = limitUpPrice;
        this.limitDownPrice = limitDownPrice;
    }

    public static MarketSessionSpec fromPreviousClose(String symbol, String securityName,
                                                       String date, long circulation,
                                                       double previousClose) {
        MarketEnum market = MarketEnum.getMarketEnum(symbol);
        boolean mainBoard = market == MarketEnum.SH || market == MarketEnum.SZ;
        int closePrice = (int) Math.round(previousClose * 100);
        int limitUpPrice = ((closePrice * (mainBoard ? 110 : 120)) + 50) / 100;
        int limitDownPrice = ((closePrice * (mainBoard ? 90 : 80)) + 50) / 100;
        return new MarketSessionSpec(symbol, securityName, market, date, circulation,
                closePrice, limitUpPrice, limitDownPrice);
    }

    public void updatePreOpenPriceLimits(double closePrice, double limitUpPrice,
                                         double limitDownPrice, String securityName) {
        int close = (int) Math.round(closePrice * 100);
        int up = (int) Math.round(limitUpPrice * 100);
        int down = (int) Math.round(limitDownPrice * 100);
        validatePriceLimits(close, up, down);
        this.securityName = securityName;
        this.closePrice = close;
        this.limitUpPrice = up;
        this.limitDownPrice = down;
    }

    private static void validatePriceLimits(int close, int up, int down) {
        if (close <= 0 || down <= 0 || up < close || down > close) {
            throw new IllegalArgumentException("价格边界无效: close=" + close
                    + ", limitUp=" + up + ", limitDown=" + down);
        }
    }

    public String symbol() { return symbol; }
    public String securityName() { return securityName; }
    public MarketEnum market() { return market; }
    public String date() { return date; }
    public long circulation() { return circulation; }
    public int closePrice() { return closePrice; }
    public int limitUpPrice() { return limitUpPrice; }
    public int limitDownPrice() { return limitDownPrice; }
}
