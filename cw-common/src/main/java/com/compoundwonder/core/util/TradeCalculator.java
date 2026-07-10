package com.compoundwonder.core.util;

public class TradeCalculator {

    // --- 业务常量 ---
    private static final int MIN_BUY_QTY = 100;
    private static final double FEE_RATE = 0.00014;
    private static final double MIN_FEE = 5.0;

    // --- 板块单笔上限 ---
    private static final int MAX_QTY_MAIN = 1_000_000;
    private static final int MAX_QTY_GEM = 300_000;
    private static final int MAX_QTY_STAR = 100_000;

    // --- 极致优化：单例缓冲区 (Zero-GC) ---
    private static final int BUFFER_SIZE = 100;
    private static final OrderRequest[] BUFFER = new OrderRequest[BUFFER_SIZE];

    // 永远返回这个对象，通过内部的 size 区分是否有单
    private static final OrderResultContainer SINGLETON_RESULT = new OrderResultContainer(BUFFER);

    static {
        for (int i = 0; i < BUFFER_SIZE; i++) {
            BUFFER[i] = new OrderRequest();
        }
    }

    /**
     * 计算买入下单数量
     * 保证永远不返回 null，方便外部直接判断 size
     */
    public static OrderResultContainer calculateBuyOrders(int internalId, double totalMoney, int priceInt) {
        // 首先重置计数器，这是单例模式下最重要的一步
        SINGLETON_RESULT.clear();

        // 1. 还原真实价格
        double realPrice = priceInt / 100.0;

        // 2. 资金检查与手续费预留
        double effectiveMoney = totalMoney - MIN_FEE;
        if (effectiveMoney <= 0) {
            return SINGLETON_RESULT; // size 为 0
        }

        if (effectiveMoney >= 30_020_000) {
            effectiveMoney = 30_020_000; // 单笔下单最大 三千万一笔
        }
        // 3. 计算理论最大股数（扣除手续费影响）
        int totalCanBuy = (int) (effectiveMoney / (realPrice * (1 + FEE_RATE)));

        // 4. 对齐到 100 股一手
        totalCanBuy = (totalCanBuy / 100) * 100;

        // 5. 门槛检查：低于 2 手不买
        if (totalCanBuy < MIN_BUY_QTY) {
            return SINGLETON_RESULT; // size 为 0
        }

        // 6. 获取板块上限
        int maxPerOrder = getMaxQtyByInternalId(internalId);

        // 7. 填充缓冲区
        int remainingQty = totalCanBuy;
        int count = 0;
        String symbolStr = SymbolUtil.intToSymbol(internalId);

        while (remainingQty > 0 && count < BUFFER_SIZE) {
            int currentOrderQty = Math.min(remainingQty, maxPerOrder);

            // 直接操作预分配的对象属性
            OrderRequest req = BUFFER[count];
            req.symbol = symbolStr;
            req.price = realPrice;
            req.quantity = currentOrderQty;

            remainingQty -= currentOrderQty;
            count++;
        }

        SINGLETON_RESULT.setSize(count);
        return SINGLETON_RESULT;
    }

    /**
     * 计算卖出上限
     *
     * @param internalId
     * @param totalHoldQty
     * @param priceInt
     * @return
     */
    public static OrderResultContainer calculateSellOrders(int internalId, int totalHoldQty, int priceInt,int limitDownPrice) {
        // 1. 重置单例缓冲区
        SINGLETON_RESULT.clear();

        if (totalHoldQty <= 1) {
            return SINGLETON_RESULT;
        }

        // 2. 还原价格
        double realPrice = PriceCageUtil.getLowerLimit(priceInt,limitDownPrice)  / 100.0;

        // 3. 获取板块上限
        int maxPerOrder = getMaxQtyByInternalId(internalId);

        // 4. 拆单填充缓冲区
        int remainingQty = totalHoldQty;
        int count = 0;
        String symbolStr = SymbolUtil.intToSymbol(internalId);

        // 特殊处理：如果是科创板(688)，卖出可以有碎股，但拆单逻辑一致
        // 只要剩余数量大于 0 且缓冲区没满
        while (remainingQty > 0 && count < BUFFER_SIZE) {
            int currentOrderQty = Math.min(remainingQty, maxPerOrder);

            // 复用对象，Zero-GC
            OrderRequest req = BUFFER[count];
            req.symbol = symbolStr;
            req.price = realPrice;
            req.quantity = currentOrderQty;

            remainingQty -= currentOrderQty;
            count++;
        }

        SINGLETON_RESULT.setSize(count);
        return SINGLETON_RESULT;
    }


    private static int getMaxQtyByInternalId(int internalId) {
        int code = internalId - 1_000_000;
        int prefix3 = code / 1000;
        int prefix2 = code / 10000;

        if (prefix3 == 688) return MAX_QTY_STAR;
        if (prefix2 == 30) return MAX_QTY_GEM;
        return MAX_QTY_MAIN;
    }

    // --- 内部静态包装类 ---

    public static class OrderRequest {
        public String symbol;
        public double price;
        public int quantity;
    }

    public static class OrderResultContainer {
        private final OrderRequest[] allOrders;
        private int size = 0;

        public OrderResultContainer(OrderRequest[] buffer) {
            this.allOrders = buffer;
        }

        public void clear() {
            this.size = 0;
        }

        public void setSize(int s) {
            this.size = s;
        }

        public int getSize() {
            return size;
        }

        public OrderRequest getOrder(int index) {
            return allOrders[index];
        }
    }

    public static void main(String[] args) {


//        OrderResultContainer orderRequests = TradeCalculator.calculateBuyOrders(1000001, 50000, 1890);
//        for (OrderRequest orderRequest : orderRequests.allOrders) {
//            System.out.println(orderRequest.toString());
//        }

        OrderResultContainer orderRequests = TradeCalculator.calculateBuyOrders(1000001, 3164.15, 1269);
        // 注意：一定要根据 getSize() 遍历，不要直接遍历 allOrders
        for (int i = 0; i < orderRequests.getSize(); i++) {
            OrderRequest req = orderRequests.getOrder(i);
            System.out.println("Symbol: " + req.symbol + ", Qty: " + req.quantity + ", Price: " + req.price);
        }

        // 测试：卖出 250 万股主板股票 (每笔上限 100 万)
        OrderResultContainer results = TradeCalculator.calculateSellOrders(1000001, 1000080, 1890,1890);

        // 注意：一定要根据 getSize() 遍历，不要直接遍历 allOrders
        for (int i = 0; i < results.getSize(); i++) {
            OrderRequest req = results.getOrder(i);
            System.out.println("Symbol: " + req.symbol + ", Qty: " + req.quantity + ", Price: " + req.price);
        }
    }

}
