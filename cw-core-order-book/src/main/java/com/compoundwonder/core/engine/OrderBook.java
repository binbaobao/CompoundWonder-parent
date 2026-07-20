package com.compoundwonder.core.engine;

import com.compoundwonder.constant.MarketEnum;
import com.compoundwonder.common.orderbook.TradeMarketState;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * 股票订单簿
 */
@Getter
@ToString
public class OrderBook implements TradeMarketState {

    private static final int DEFAULT_ACTIVE_ORDER_CAPACITY = 5_000;

    public enum AddOrderResult {
        ADDED,
        DUPLICATE,
        INVALID_PRICE,
        INVALID_DIRECTION
    }

    /**
     * 证券代码
     */
    private final String symbol;

    @Setter
    private String securityName;
    /**
     * 市场分类
     */
    private final MarketEnum market;
    /**
     * 交易日期
     */
    @Setter
    private String date;

    // 股票状态 1涨停 2.炸板 3回封涨停
    private int status = 0;

    //
    // 0 任务暂时不执行 或 任务已经执行(已经买入或者已经卖出)， 1 待买入，2 买入待撤单  -1待卖出 -2 卖出待撤单
    @Setter
    private int transactionStatus = 0;
    /**
     * 交易模式：1 连板接力，2 普通首板，3 小市值首板。
     *
     * <p>默认值 1 只用于兼容未显式设置模式的旧调用；回测和实盘初始化必须按任务覆盖。</p>
     */
    @Setter
    private int tradeMode = 1;
    /**
     * 连班次数
     */
    @Setter
    private int lbcs;

    /**
     * 行情更新时间
     */
    private int time;
    /**
     * 昨收价格
     */
    private int closePrice;
    /**
     * 最新价格
     */
    @Setter
    private int lastPrice;
    /**
     * 最低
     */
    private int lowPrice;

    /**
     * 最低价涨幅
     */
    private double lowPriceIncrease;

    /**
     * 最高
     */
    private int highestPrice;
    /**
     * 涨停价格
     */
    private int limitUpPrice;
    /**
     * 跌停价
     */
    private int limitDownPrice;
    /**
     * 开盘价
     */
    private int openPrice;
    /**
     * 开盘涨幅
     */
    private double openIncrease;

    /**
     * 涨幅
     */
    private double increase;

    /**
     * 振幅
     */
    private double amplitude;

    /**
     * 炸板深度，
     */
    private double limitUpBreakDepth;
    /**
     * 炸板最低价格
     */
    private int limitUpBreakLowestPrice;

    /**
     * 换手率
     */
    private double turnoverRate;
    /**
     * 成交额
     */
    @Setter
    private long turnover;
    /**
     * 成交量
     */
    @Setter
    private long volume;
    /**
     * 最大成交量
     */
    private final long maxVolume;
    /**
     * 最大换手
     */
    @Setter
    private double maxHs;
    /**
     * 流通股
     */
    private final long circulation;
    /**
     * 启动市值 万
     */
    @Setter
    private int initialMarketValue;
    /**
     * 前两日换手率
     * 判断是否连续缩量
     * 15.5% 为界限
     */
    @Setter
    private double threeDaysTurnover;
    /**
     * 前两日换手率
     * 判断是否连续缩量
     * 15.5% 为界限
     */
    @Setter
    private double twoDaysTurnover;
    /**
     * 昨天换手
     */
    @Setter
    private double yesterdayTurnover;
    /**
     * 一字板涨停
     */
    @Setter
    private int oneWordLimitUp;
    /**
     * 查询最近 15 天 平均高度
     */
    @Setter
    private int averageLimitUpHeight;
    /**
     * 距离下个交易日的天数
     */
    @Setter
    private int nextTradingDay;

    /**
     * 涨停买总金额，不涨停没有这个字段
     *
     */
    @Setter
    private long limitUpBuyAmount;
    /**
     * 最新涨停时间
     */
    private int lastLimitUptime;

    // 主索引：快速 orderId 删除
    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private Int2ObjectOpenHashMap<TickNode> idIndex;
    // 二级索引：连续价格档位 + 档位内买卖双向队列
    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private PriceLevel[] priceLevels;
    // 均价 ，每一分钟采集一次
    @ToString.Exclude
    public final int[] avgPrice = new int[60 * 4];
    @ToString.Exclude
    public final int[] price = new int[60 * 4];

    private long totalBuyVolume = 0;

    private long totalSellVolume = 0;
    // 上海重新组合计算接到或者成交的委托，深圳直接就是本次接到的委托买。后续买入判断是否是大单
    @ToString.Exclude
    public final TickNode buyMaxOrder;
//    public TickNode sellMaxOrder;
    public OrderBook(String symbol, long circulation, double closePrice, long maxVolume) {
        this.symbol = symbol;
        this.market = MarketEnum.getMarketEnum(symbol);
        boolean market = this.market.equals(MarketEnum.SH) || this.market.equals(MarketEnum.SZ);
        this.circulation = circulation;
        this.closePrice = (int) Math.round(closePrice * 100);
        this.limitUpPrice = ((this.closePrice * (market ? 110 : 120)) + 50) / 100;
        this.lowPrice = this.limitUpPrice;
        this.limitDownPrice = ((this.closePrice * (market ? 90 : 80)) + 50) / 100;
        this.maxVolume = maxVolume;
        this.idIndex = new Int2ObjectOpenHashMap<>(DEFAULT_ACTIVE_ORDER_CAPACITY);
        this.limitUpBreakLowestPrice = this.limitUpPrice;
        // 价格档位对象在首笔委托到达时按需创建
        int range = limitUpPrice - limitDownPrice + 1;
        this.priceLevels = new PriceLevel[range];
        this.buyMaxOrder = new TickNode();
    }

    /**
     * 开盘前修正昨收价和当日涨跌停价格，并按照修正后的价格区间重建订单簿索引。
     *
     * <p>该方法只能在第一条行情进入订单簿前调用，禁止在盘中行情处理期间调用。</p>
     *
     * @param closePrice 昨收价
     * @param limitUpPrice 当日涨停价
     * @param limitDownPrice 当日跌停价
     * @param securityName 证券名称
     */
    public void updatePreOpenPriceLimits(double closePrice, double limitUpPrice,
                                         double limitDownPrice, String securityName) {
        this.securityName = securityName;
        this.closePrice = (int) Math.round(closePrice * 100);
        this.limitUpPrice = (int) Math.round(limitUpPrice * 100);
        this.limitDownPrice = (int) Math.round(limitDownPrice * 100);
        this.lowPrice = this.limitUpPrice;
        this.idIndex = new Int2ObjectOpenHashMap<>(DEFAULT_ACTIVE_ORDER_CAPACITY);
        // 价格区间变化后重新建立连续价位索引
        int range = this.limitUpPrice - this.limitDownPrice + 1;
        this.priceLevels = new PriceLevel[range];
        this.totalBuyVolume = 0;
        this.totalSellVolume = 0;
        this.limitUpBreakLowestPrice = this.limitUpPrice;
    }

    /**
     * 新增委托，并同步维护订单编号索引、价位队列和全盘口数量。
     */
    public AddOrderResult addOrder(TickNode node) {
        if (node.getDirection() != 1 && node.getDirection() != 2) {
            return AddOrderResult.INVALID_DIRECTION;
        }
        int priceIndex = priceToIndexIfValid(node.getPrice());
        if (priceIndex < 0) {
            return AddOrderResult.INVALID_PRICE;
        }
        TickNode existing = idIndex.putIfAbsent(node.getOrderId(), node);
        if (existing != null) {
            return AddOrderResult.DUPLICATE;
        }
        PriceLevel level = priceLevels[priceIndex];
        if (level == null) {
            level = new PriceLevel();
            priceLevels[priceIndex] = level;
        }
        level.add(node);
        increaseTotalVolume(node.getDirection(), node.getQuantity());
        return AddOrderResult.ADDED;
    }

    /**
     * 扣减指定委托的成交数量。
     *
     * @return 委托全部成交时返回已摘除的节点；部分成交或委托不存在时返回 {@code null}
     */
    public TickNode applyTrade(int orderId, int quantity) {
        TickNode node = idIndex.get(orderId);
        if (node == null) {
            return null;
        }
        int priceIndex = priceToIndex(node.getPrice());
        PriceLevel level = priceLevels[priceIndex];
        int deducted = level.applyTrade(node, quantity);
        decreaseTotalVolume(node.getDirection(), deducted);
        if (node.getQuantity() > 0) {
            return null;
        }
        idIndex.remove(orderId);
        return node;
    }

    /**
     * 撤销指定委托，并同步清理所有订单簿索引。
     *
     * @return 被撤销的节点；委托不存在时返回 {@code null}
     */
    public TickNode cancelOrder(int orderId) {
        TickNode node = idIndex.remove(orderId);
        if (node == null) {
            return null;
        }
        int priceIndex = priceToIndex(node.getPrice());
        PriceLevel level = priceLevels[priceIndex];
        int cancelled = level.remove(node);
        decreaseTotalVolume(node.getDirection(), cancelled);
        return node;
    }

    /**
     * 清空当日委托状态，并将仍在订单簿中的节点交给调用方回收。
     *
     * <p>只能在 Handler 停止消费当前批次后调用。</p>
     */
    public void clearOrders(Consumer<TickNode> recycler) {
        idIndex.values().forEach(recycler);
        idIndex.clear();
        Arrays.fill(priceLevels, null);
        totalBuyVolume = 0;
        totalSellVolume = 0;
    }

    /**
     * 获取指定价格档位；该价格尚无委托时返回 {@code null}。
     */
    public PriceLevel getPriceLevel(int price) {
        return priceLevels[priceToIndex(price)];
    }

    /**
     * 获取最新成交价价位中，最早仍在买方队列里的委托时间。
     *
     * <p>用于回测结束后填充规则记录的最后委托时间，等价于旧价格 List 在最新价位
     * 取买方队首，但不会向外暴露可修改的订单节点。深圳同一价位可能同时存在
     * 买卖队列，买入成交延迟只与买方队列比较。</p>
     *
     * @return 委托时间；最新价无有效委托时返回 0
     */
    public int getLastPriceOrderTime() {
        int priceIndex = priceToIndexIfValid(lastPrice);
        if (priceIndex < 0) {
            return 0;
        }
        PriceLevel level = priceLevels[priceIndex];
        return level == null ? 0 : level.getFirstBuyOrderTime();
    }

    /**
     * 获取指定价格的买方剩余总量。
     */
    public long getBuyQuantity(int price) {
        PriceLevel level = getPriceLevel(price);
        return level == null ? 0 : level.getBuyQuantity();
    }

    /**
     * 获取指定价格的卖方剩余总量。
     */
    public long getSellQuantity(int price) {
        PriceLevel level = getPriceLevel(price);
        return level == null ? 0 : level.getSellQuantity();
    }

    /**
     * 当前仍在订单簿中的有效委托数量。
     */
    public int getActiveOrderCount() {
        return idIndex.size();
    }

    /**
     * 判断订单号是否仍在订单簿中，不暴露可修改的主索引。
     */
    public boolean containsOrder(int orderId) {
        return idIndex.containsKey(orderId);
    }

    /** 交易策略读取指定分钟的最新价，不对外暴露可替换的数组引用。 */
    @Override
    public int getMinutePriceAt(int index) {
        return price[index];
    }

    /** 交易策略读取指定分钟的均价，不对外暴露可替换的数组引用。 */
    @Override
    public int getAveragePriceAt(int index) {
        return avgPrice[index];
    }

    /** 当前仍在订单簿中的最大买委托价格，单位：分。codex 注释错误 TODO */
    // 上海重新组合计算接到或者成交的委托，深圳直接就是本次接到的委托买。后续买入判断是否是大单
    @Override
    public int getLargestBuyOrderPrice() {
        return buyMaxOrder.getPrice();
    }

    /** 当前仍在订单簿中的最大买委托剩余数量，单位：股。codex 注释错误 TODO */
    // 上海重新组合计算接到或者成交的委托，深圳直接就是本次接到的委托买。后续买入判断是否是大单
    @Override
    public int getLargestBuyOrderQuantity() {
        return buyMaxOrder.getQuantity();
    }

    private int priceToIndex(int price) {
        int index = priceToIndexIfValid(price);
        if (index < 0) {
            throw new IllegalArgumentException("委托价格超出订单簿范围: " + price);
        }
        return index;
    }

    private int priceToIndexIfValid(int price) {
        int index = price - limitDownPrice;
        return index < 0 || index >= priceLevels.length ? -1 : index;
    }

    private void increaseTotalVolume(byte direction, int quantity) {
        if (direction == 1) {
            totalBuyVolume += quantity;
        } else {
            totalSellVolume += quantity;
        }
    }

    private void decreaseTotalVolume(byte direction, int quantity) {
        if (direction == 1) {
            totalBuyVolume -= quantity;
        } else {
            totalSellVolume -= quantity;
        }
    }

    /**
     * 逐笔成交更新价格
     * 成交额，成交量，最高价格，最低价格,最新价格
     */
    public void updatePrice(long turnover, long volume, int tradePrice, int time) {

        //// 成交额，成交量，最高价格，最低价格,最新价格,更新时间
        this.turnover += turnover;
        this.volume += volume;
        this.highestPrice = Math.max(this.highestPrice, tradePrice);
        this.lowPrice = Math.min(this.lowPrice, tradePrice);
        if (this.lowPrice == tradePrice) {
            this.lowPriceIncrease = Math.round((lowPrice - this.closePrice) * 100.0 / this.closePrice * 100.0) / 100.0;
        }
        this.lastPrice = tradePrice;
        if (this.openPrice == 0) {
            this.openPrice = tradePrice;
            this.openIncrease = Math.round((tradePrice - this.closePrice) * 100.0 / this.closePrice * 100.0) / 100.0;
        }
        this.time = time;
        //(bb - aa)*100.0 / aa  计算涨幅
        this.increase = (this.lastPrice - this.closePrice) * 100.0 / this.closePrice;
        // 计算振幅
        this.amplitude = (this.highestPrice - this.lowPrice) * 100.0 / this.closePrice;
        // 换手率 Math.round(bb*100.0/aa*100.0)/100.00 ;
        this.turnoverRate = this.volume * 100.0 / this.circulation;
        // 如果是炸板状态
        if (this.status > 1 && this.status % 2 == 0 && tradePrice < this.limitUpBreakLowestPrice) {
            this.limitUpBreakLowestPrice = tradePrice;
            this.limitUpBreakDepth = (this.limitUpPrice - tradePrice) * 100.0 / this.closePrice;
        }
    }

    /**
     * 集合竞价就开始，更新最低价格
     * @param lowPrice
     */
    public void updateLowestPrice(int lowPrice){
        if (lowPrice < limitDownPrice)return;
        this.lowPrice = Math.min(this.lowPrice, lowPrice);
        if (this.lowPrice == lowPrice) {
            this.lowPriceIncrease = Math.round((lowPrice - this.closePrice) * 100.0 / this.closePrice * 100.0) / 100.0;
        }
    }

    /**
     * EMA平滑系数
     */
    @ToString.Exclude
    private static final double ALPHA = 0.25;

    /**
     * 每20笔逐笔数据更新一次EMA
     */
    @ToString.Exclude
    private static final int EMA_UPDATE_INTERVAL = 20;

    /**
     * EMA更新计数器
     */
    @ToString.Exclude
    private int emaCounter = 0;

    /**
     * EMA封单量
     */
    @ToString.Exclude
    private double lastEmaVolume = 0;

    /**
     * EMA变化率
     */
    @ToString.Exclude
    private double changePercent = 0.0;

    /**
     * 封单变化100万立即更新一次EMA
     */
    private static final long EMA_AMOUNT_CHANGE = 100L;

    /**
     * 上次计算EMA时的封单金额
     */
    private long lastSealAmount = 0;


    public void updateLimitUpStatus() {
        if (this.time == 0) {
            return;
        }
        long limitUpOrderVolume = getBuyQuantity(limitUpPrice);
        long currentLimitUpBuyAmount = limitUpOrderVolume / 100L * limitUpPrice / 10000L;
        // 一笔砸穿的情况，涨停买还有，但是价格可能已经不是涨停价了
        if (this.lastLimitUptime != 0 && lastPrice != limitUpPrice) {
            this.lastLimitUptime = 0;
        }
        // 在没涨停的时候，一个大的委托买单会吃掉笼子内所有卖单
        if (this.status % 2 == 0 && lastPrice == limitUpPrice && limitUpOrderVolume > 0) {
            // 封单
            // 涨停买一总金额，不涨停这个字段是 0
            this.limitUpBuyAmount = currentLimitUpBuyAmount;
            // 封单大于 100万 视为一次涨停封单
            if (this.limitUpBuyAmount > 100) {
                this.status++;
                this.lastLimitUptime = time;
                this.lastEmaVolume = 0;
                this.emaCounter = 0;
            }
        } else if (this.status % 2 == 1) {
            this.limitUpBuyAmount = currentLimitUpBuyAmount;
            // 如果是涨停的状态，但是成交价已经不是涨停价，视为破板
            // 如果涨停封单金额小于 100万
            if (lastPrice != limitUpPrice || this.limitUpBuyAmount < 100) {
                this.status++;
                this.limitUpBuyAmount = 0;
                this.lastEmaVolume = 0;
                this.changePercent = 0;
                this.lastSealAmount = 0;
                this.emaCounter = 0;
            } else {
                long currentSealAmount = this.limitUpBuyAmount;

                // 触发条件：
                // 1. 达到20笔
                // 2. 封单变化超过100万
                boolean needUpdate =
                        ++emaCounter >= EMA_UPDATE_INTERVAL
                                || Math.abs(currentSealAmount - lastSealAmount) >= EMA_AMOUNT_CHANGE;
                this.lastSealAmount = this.limitUpBuyAmount;


                if (needUpdate && limitUpBuyAmount > 100) {
                    emaCounter = 0;

                    double currentEma;

                    if (lastEmaVolume == 0) {
                        currentEma = limitUpOrderVolume;
                    } else {
                        currentEma = (1 - ALPHA) * lastEmaVolume + ALPHA * limitUpOrderVolume;
                    }

                    if (lastEmaVolume > 0) {
                        changePercent = Math.round((currentEma - lastEmaVolume) / lastEmaVolume * 10000.0) / 100.0;
                    }

                    lastEmaVolume = currentEma;

                    // 记录本次计算使用的封单金额
                    lastSealAmount = currentSealAmount;
                }
            }
        }
    }
}
