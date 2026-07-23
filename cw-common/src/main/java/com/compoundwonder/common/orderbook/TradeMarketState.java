package com.compoundwonder.common.orderbook;

/**
 * 交易策略读取的订单簿只读视图。
 *
 * <p>订单簿直接实现该接口，热路径不复制快照，也不创建临时 DTO。</p>
 */
public interface TradeMarketState {

    /** @return 稳定交易模式编号：1 连板、2 普通首板、3 小市值首板 */
    int getTradeMode();

    /** @return 六位股票代码 */
    String getSymbol();

    /** @return 涨停状态变更序号；奇数表示封板中，偶数表示未封板或已炸板 */
    int getStatus();

    /**
     * @return 初始化订单簿时传入的昨日连续涨停高度；卖出分发中 2 表示今日处于 2 进 3
     */
    int getLbcs();

    /** @return 当前已处理行情时间，格式为 {@code HHmmssSSS} */
    int getTime();

    /** @return 昨收价，整数价格口径为元乘以 100 */
    int getClosePrice();

    /** @return 最新成交价，整数价格口径为元乘以 100 */
    int getLastPrice();

    /** @return 当日最低成交价，整数价格口径为元乘以 100 */
    int getLowPrice();

    /** @return 当日最低价相对昨收的涨跌幅，单位为百分比 */
    double getLowPriceIncrease();

    /** @return 当日最高成交价，整数价格口径为元乘以 100 */
    int getHighestPrice();

    /** @return 当日涨停价，整数价格口径为元乘以 100 */
    int getLimitUpPrice();

    /** @return 当日跌停价，整数价格口径为元乘以 100 */
    int getLimitDownPrice();

    /** @return 集合竞价确定的当日开盘价，整数价格口径为元乘以 100 */
    int getOpenPrice();

    /** @return 开盘价相对昨收的涨跌幅，单位为百分比 */
    double getOpenIncrease();

    /** @return 最新价相对昨收的涨跌幅，单位为百分比 */
    double getIncrease();

    /** @return 当日最高价与最低价形成的振幅，单位为百分比 */
    double getAmplitude();

    /** @return 从涨停价回落到炸板最低价的深度，单位为百分比 */
    double getLimitUpBreakDepth();

    /** @return 当日实时换手率，单位为百分比 */
    double getTurnoverRate();

    /** @return 当日累计成交额，使用订单簿内部整数金额口径 */
    long getTurnover();

    /** @return 当日累计成交股数 */
    long getVolume();

    /** @return 初始化时传入的历史最大成交股数 */
    long getMaxVolume();

    /** @return 历史最大成交量对应的换手率，单位为百分比 */
    double getMaxHs();

    /** @return 当日可交易流通股本，单位为股 */
    long getCirculation();

    /** @return 本轮行情启动前的流通市值，单位为万元 */
    int getInitialMarketValue();

    /** @return 交易日前三个交易日换手率的平均值，单位为百分比 */
    double getThreeDaysTurnover();

    /** @return 交易日前两个交易日换手率的平均值，单位为百分比 */
    double getTwoDaysTurnover();

    /** @return 上一交易日换手率，单位为百分比 */
    double getYesterdayTurnover();

    /** @return 交易日前最近三根日 K 中从昨日开始连续一字涨停的数量 */
    int getOneWordLimitUp();

    /** @return 交易日前最近 15 个自然日市场最高连板高度的平均值 */
    int getAverageLimitUpHeight();

    /** @return 当前交易日与下一交易日之间间隔的非交易自然日数量 */
    int getNextTradingDay();

    /** @return 当前涨停价买单总金额，单位为万元；未封板时为 0 */
    long getLimitUpBuyAmount();

    /** @return 最近一次有效封板时间，格式为 {@code HHmmssSSS} */
    int getLastLimitUptime();

    /** @return 最近一次炸板时间，格式为 {@code HHmmssSSS}；当日尚未炸板时为 0 */
    int getLastLimitUpBreakTime();

    /** @return 最近一次封板状态下记录的振幅，炸板后保留用于确认炸板前状态 */
    double getLastSealedAmplitude();

    /** @return 最近一次封板状态下记录的 EMA 变化率，炸板后保留用于确认炸板前状态 */
    double getLastSealedChangePercent();

    /** @return 最近一次封板状态下记录的封单金额，单位为万元 */
    long getLastSealedAmount();

    /** @return 最近一次计算得到的涨停封单量 EMA，单位为股 */
    double getLastEmaVolume();

    /** @return 本次封单量 EMA 相对上次的变化率，单位为百分比 */
    double getChangePercent();

    /**
     * @return 最近 5 次 EMA 变化形成的封单趋势：-1 持续减弱、0 方向未确认、1 持续增强
     */
    int getEmaSealTrend();

    /** @return 最近一次涨停状态更新时记录的封单金额，单位为万元 */
    long getLastSealAmount();

    /** @return 已经定格的分钟累计均价中的最低值，整数价格口径为元乘以 100 */
    int getMinAveragePrice();

    /** @return 最低分钟累计均价相对昨收的涨跌幅，单位为百分比 */
    double getMinAveragePriceIncrease();

    /**
     * 读取指定分钟槽位记录的最新成交价。
     *
     * @param index 从开盘开始计算的分钟槽位下标
     * @return 分钟价格，整数价格口径为元乘以 100
     */
    int getMinutePriceAt(int index);

    /**
     * 读取指定分钟槽位记录的当日成交均价。
     *
     * @param index 从开盘开始计算的分钟槽位下标
     * @return 分钟均价，整数价格口径为元乘以 100
     */
    int getAveragePriceAt(int index);

    /**
     * @return 当前行情事件重建出的买方向单笔委托价格，整数价格口径为元乘以 100；
     *         该值服务大单买入规则，不代表全订单簿数量最大的挂单
     */
    int getLargestBuyOrderPrice();

    /** @return 当前行情事件重建出的买方向单笔委托数量，单位为股 */
    int getLargestBuyOrderQuantity();
}
