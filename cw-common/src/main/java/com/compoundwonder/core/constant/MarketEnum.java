package com.compoundwonder.core.constant;

public enum MarketEnum {

    SH, SZ, CY, KC, BJ;

    public static MarketEnum getMarketEnum(String symbol) {

        if (symbol.startsWith("60")) {
            return SH;
        } else if (symbol.startsWith("00")) {
            return SZ;
        } else if (symbol.startsWith("30")) {
            return CY;
        } else if (symbol.startsWith("68")) {
            return KC;
        }
        return BJ;
    }
}
