package com.compoundwonder.core.engine;

import com.compoundwonder.constant.MarketEnum;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * 股票订单簿
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OrderBook {

    /**
     * 证券代码
     */
    private String symbol;

    private String securityName;
    /**
     * 市场分类
     */
    private MarketEnum market;
    /**
     * 交易日期
     */
    private String date;

    // 股票状态 1涨停 2.炸板 3回封涨停
    private int status = 0;

    //
    // 0 任务暂时不执行 或 任务已经执行(已经买入或者已经卖出)， 1 待买入，2 买入待撤单  -1待卖出 -2 卖出待撤单
    private int transactionStatus = 0;
    /**
     * 连班次数
     */
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
    private int lastPrice;
    /**
     * 最低
     */
    private int lowPrice;
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
    private long turnover;
    /**
     * 成交量
     */
    private long volume;
    /**
     * 最大成交量
     */
    private long maxVolume;
    /**
     * 最大换手
     */
    private double maxHs;
    /**
     * 流通股
     */
    private long circulation;
    /**
     * 启动市值 万
     */
    private int initialMarketValue;
    /**
     * 前两日换手率
     * 判断是否连续缩量
     * 15.5% 为界限
     */
    private double threeDaysTurnover;
    /**
     * 前两日换手率
     * 判断是否连续缩量
     * 15.5% 为界限
     */
    private double twoDaysTurnover;
    /**
     * 昨天换手
     */
    private double yesterdayTurnover;
    /**
     * 一字板涨停
     */
    private int oneWordLimitUp;
    /**
     * 查询最近 15 天 平均高度
     */
    private int averageLimitUpHeight;
    /**
     * 距离下个交易日的天数
     */
    private int nextTradingDay;

    /**
     * 涨停买总金额，不涨停没有这个字段
     *
     */
    private long limitUpBuyAmount;
    /**
     * 最新涨停时间
     */
    private int lastLimitUptime;

    // 主索引：快速 orderId 删除
    @ToString.Exclude
    private Int2ObjectOpenHashMap<TickNode> idIndex;
    // 二级索引：连续价格档位 + 档位内买卖双向队列
    @ToString.Exclude
    private PriceLevel[] priceLevels;
    // 均价 ，每一分钟采集一次
    @ToString.Exclude
    public final int[] avgPrice = new int[60 * 4];
    @ToString.Exclude
    public final int[] price = new int[60 * 4];

    public int totalBuyVolume = 0;

    public int totalSellVolume = 0;
    @ToString.Exclude
    public TickNode buyMaxOrder;
//    public TickNode sellMaxOrder;
    /**
     * 上一次的快照
     */
    @ToString.Exclude
    private TickData snapshot;


    public OrderBook(String symbol, long circulation, double closePrice, long maxVolume) {
        this.symbol = symbol;
        this.market = MarketEnum.getMarketEnum(symbol);
        boolean market = this.market.equals(MarketEnum.SH) || this.market.equals(MarketEnum.SZ);
        this.circulation = circulation;
        this.closePrice = (int) Math.round(closePrice * 100);
        this.limitUpPrice = ((this.closePrice * (market ? 110 : 120)) + 50) / 100;
        this.limitDownPrice = ((this.closePrice * (market ? 90 : 80)) + 50) / 100;
        this.maxVolume = maxVolume;
        this.idIndex = new Int2ObjectOpenHashMap<>();
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
        this.idIndex = new Int2ObjectOpenHashMap<>(5000);
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
    public boolean addOrder(TickNode node) {
        int priceIndex = priceToIndex(node.getPrice());
        if (node.getDirection() != 1 && node.getDirection() != 2) {
            throw new IllegalArgumentException("不支持的委托方向: " + node.getDirection());
        }
        TickNode existing = idIndex.putIfAbsent(node.getOrderId(), node);
        if (existing != null) {
            return false;
        }
        PriceLevel level = priceLevels[priceIndex];
        if (level == null) {
            level = new PriceLevel();
            priceLevels[priceIndex] = level;
        }
        level.add(node);
        increaseTotalVolume(node.getDirection(), node.getQuantity());
        return true;
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
        removeEmptyPriceLevel(priceIndex, level);
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
        removeEmptyPriceLevel(priceIndex, level);
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

    private int priceToIndex(int price) {
        int index = price - limitDownPrice;
        if (index < 0 || index >= priceLevels.length) {
            throw new IllegalArgumentException("委托价格超出订单簿范围: " + price);
        }
        return index;
    }

    private void removeEmptyPriceLevel(int priceIndex, PriceLevel level) {
        if (level.isEmpty()) {
            priceLevels[priceIndex] = null;
        }
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
        this.lowPrice = Math.min(this.lowPrice == 0 ? tradePrice : this.lowPrice, tradePrice);
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
     * EMA平滑系数
     */
    @ToString.Exclude
    private static final double ALPHA = 0.25;

    /**
     * 每50笔逐笔数据更新一次EMA
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
    private int lastEmaVolume = 0;

    /**
     * EMA变化率
     */
    @ToString.Exclude
    private double changePercent = 0.0;

    /**
     * 封单变化100万立即更新一次EMA
     */
    private static final int EMA_AMOUNT_CHANGE = 100;

    /**
     * 上次计算EMA时的封单金额
     */
    private int lastSealAmount = 0;


    public void updateLimitUpStatus() {
        if (this.time == 0) {
            return;
        }
        // 一笔砸穿的情况，涨停买还有，但是价格可能已经不是涨停价了
        if (this.lastLimitUptime != 0 && lastPrice != limitUpPrice) {
            this.lastLimitUptime = 0;
        }
        // 在没涨停的时候，一个大的委托买单会吃掉笼子内所有卖单
        if (this.status % 2 == 0 && lastPrice == limitUpPrice && getBuyQuantity(limitUpPrice) > 0) {
            // 封单
            // 涨停买一总金额，不涨停这个字段是 0
            this.limitUpBuyAmount = getBuyQuantity(limitUpPrice) / 100L * limitUpPrice / 10000L;
            // 封单大于 100万 视为一次涨停封单
            if (this.limitUpBuyAmount > 100) {
                this.status++;
                this.lastLimitUptime = time;
                this.lastEmaVolume = 0;
            }
        } else if (this.status % 2 == 1) {
            // 如果是涨停的状态，但是成交价已经不是涨停价，视为破板
            // 如果涨停封单金额小于 100万
            if (lastPrice != limitUpPrice || this.limitUpBuyAmount < 100) {
                this.status++;
                this.limitUpBuyAmount = 0;
                this.lastEmaVolume = 0;
                this.changePercent = 0;
                this.lastSealAmount = 0;
            } else {
                long limitUpOrderVolume = getBuyQuantity(limitUpPrice);
                // 涨停买一总金额，不涨停这个字段是 0
                this.limitUpBuyAmount = limitUpOrderVolume / 100L * limitUpPrice / 10000L;

                int currentSealAmount = (int) this.limitUpBuyAmount;

                // 触发条件：
                // 1. 达到50笔
                // 2. 封单变化超过100万
                boolean needUpdate =
                        ++emaCounter >= EMA_UPDATE_INTERVAL
                                || Math.abs(currentSealAmount - lastSealAmount) >= EMA_AMOUNT_CHANGE;
                this.lastSealAmount = (int) this.limitUpBuyAmount;


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

                    lastEmaVolume = (int) currentEma;

                    // 记录本次计算使用的封单金额
                    lastSealAmount = currentSealAmount;
                }
            }
        }
    }
}
