package com.compoundwonder.constant;

public class ToraConstants {

    // 交易方向 (Side)
    public static final byte SIDE_BUY = 1;
    public static final byte SIDE_SELL = 2;

    // 交易方向 (Side)
    public static final byte DATA_TYPE_ORDER = 1;
    public static final byte DATA_TYPE_TRANS = 2;


    // 执行类型 (ExecType / Transaction Type)
    public static final byte TYPE_TRADE = 0;   // 成交
    public static final byte TYPE_CANCEL = 1;  // 撤单

    // 市场标识
    public static final byte EXCHANGE_SSE = 1; // 上海
    public static final byte EXCHANGE_SZSE = 2; // 深圳

    /// 买
    public static final char TORA_TSTP_LSD_Buy = '1';
    /// 卖
    public static final char TORA_TSTP_LSD_Sell = '2';
    /// 借入
    public static final char TORA_TSTP_LSD_Borrow = '3';
    /// 借出
    public static final char TORA_TSTP_LSD_Lend = '4';

    /// 市价
    public static final char TORA_TSTP_LOT_Market = '1';
    /// 限价
    public static final char TORA_TSTP_LOT_Limit = '2';
    /// 本方最优
    public static final char TORA_TSTP_LOT_HomeBest = '3';

}
