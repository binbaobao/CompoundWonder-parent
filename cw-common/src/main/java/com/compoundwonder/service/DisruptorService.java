package com.compoundwonder.service;


/**
 * 行情流、交易回报与订单簿 Disruptor 之间的事件发布接口。
 *
 * <p>所有方法直接写入可复用 RingBuffer 事件，调用方不得在发布后依赖传入数组被保留。</p>
 */
public interface DisruptorService {

    /**
     * 发布上海 NGTS 委托、撤单和成交二合一逐笔事件。
     *
     * @param symbolInt 内部整数证券代码
     * @param time 行情时间，格式为 {@code HHmmssSSS}
     * @param dataType 订单簿事件类型：1 委托或撤单，2 成交
     * @param orderId 事件对应的主委托号或成交序号
     * @param amount 成交金额；非成交事件传 0
     * @param direction 委托方向：1 买、2 卖，成交事件可传 0
     * @param price 价格，整数价格口径为元乘以 100
     * @param volume 委托或成交数量，单位为股
     * @param bizType 上海业务类型：2 新增或成交，10 撤单
     * @param buyNo 买方委托号
     * @param sellNo 卖方委托号
     */
    void publishNGTSTickData(int symbolInt, int time, byte dataType, int orderId, int amount, byte direction, int price, int volume, byte bizType, int buyNo, int sellNo);


    /**
     * 发布深圳逐笔委托事件。
     *
     * @param symbolInt 内部整数证券代码
     * @param time 委托时间，格式为 {@code HHmmssSSS}
     * @param orderId 交易所委托号
     * @param price 委托价格，整数价格口径为元乘以 100；0 表示市价单
     * @param quantity 委托数量，单位为股
     * @param direction 委托方向：1 买、2 卖
     * @param type 深圳委托类型：0 撤单、1 限价、2 市价、3 本方最优
     */
    void publishTickOrderData(int symbolInt, int time, int orderId, int price, int quantity, byte direction, byte type);

    /**
     * 发布当前账户的委托回报，供订单簿同步个人订单状态。
     *
     * @param symbolInt 内部整数证券代码
     * @param time 委托回报时间，格式为 {@code HHmmssSSS}
     * @param price 委托价格，整数价格口径为元乘以 100
     * @param quantity 委托数量，单位为股
     * @param direction 委托方向：1 买、2 卖
     * @param type 柜台委托状态或业务类型
     */
    void pushOrderInfo(int symbolInt, int time, int price, int quantity, byte direction, byte type);

    /**
     * 发布深圳逐笔成交或撤单事件。
     *
     * @param symbolInt 内部整数证券代码
     * @param time 成交时间，格式为 {@code HHmmssSSS}
     * @param orderId 当前事件主委托号
     * @param price 成交价格，整数价格口径为元乘以 100
     * @param quantity 成交数量，单位为股
     * @param amount 成交金额，使用订单簿内部整数金额口径
     * @param direction 主动方向：1 买、2 卖
     * @param type 深圳逐笔类型：0 成交、1 撤单
     * @param buyerOrderId 买方委托号
     * @param sellerOrderId 卖方委托号
     */
    void publishTickTradeData(int symbolInt, int time, int orderId, int price, int quantity, int amount, byte direction, byte type, int buyerOrderId, int sellerOrderId);

    /**
     * 发布已经按 TickData 字段顺序编码的调试事件。
     *
     * <p>数组下标依次为：证券代码、数据类型、时间、委托号、价格、数量、金额、
     * 方向、业务类型、买方委托号、卖方委托号。</p>
     *
     * @param tick 长度至少为 11 的整数事件数组；方法只在调用期间读取数组
     */
    void publishLogData(int[] tick);

    /**
     * 整理 个人交易信息
     *
     * @param symbol 代码
     * @param type   交易信息：1、买入下单 2、买入成交 3、卖出成交，4，买入撤单(现在只有集合竞价有撤单)
     *               <br>1、买入下单，买入，自己这个已经关了，修改成盯撤单状态，然后关闭其他的下单
     *               <br>2、买入成交，买入，自己关了，其他的也关了
     *               <br>3、卖出成交，卖出，自己的关了，其他的打开（1,2板；如果持有的是龙头股，3板第二天就是中位）
     *               <br>4、买入撤单，买入，自己的打开，别的也打开,如果是擒龙捉妖就只打开擒龙捉妖
     *               <br>5、如果擒龙捉妖到指定时间还没有买入，就把所有的盯盘都打开
     *               <br>6、撤单后继续卖出
     *               <br>7、卖出下单修改自己的状态，基本只有在手动卖出时有用
     *               <br>8、唯一优先候选释放后，重新打开其他盯盘股票
     *               <br>下单，卖出，把自己的先关了，其他都是关闭状态，其他的暂时不动，也不用发
     */
    void publishTransInfoData(String symbol, int type);

    /**
     * 向单只股票订单簿发布交易状态变更事件。
     *
     * @param symbol 六位股票代码
     * @param type 目标交易状态，沿用订单簿 transactionStatus 编码
     */
    void publishTransInfoDataOne(String symbol, byte type);

    /**
     * 推送 3 秒行情快照信息
     * 集合竞价 买入（只上海） 卖出
     *
     * @param symbol 内部整数证券代码
     * @param time 快照时间，格式为 {@code HHmmssSSS}
     * @param price 集合竞价阶段为卖一价，连续竞价阶段为买一价；整数价格口径为元乘以 100
     * @param amount 连续竞价累计成交额；集合竞价阶段传 0
     * @param sellVolume 集合竞价阶段为卖一加卖二量，连续竞价阶段为累计成交量
     * @param buyVolume 集合竞价阶段为买一加买二量，连续竞价阶段为买一量
     */
    void publishSnapshotData(int symbol, int time, int price, long amount, long sellVolume, long buyVolume);

}
