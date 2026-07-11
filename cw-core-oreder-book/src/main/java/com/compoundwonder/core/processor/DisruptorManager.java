package com.compoundwonder.core.processor;

import com.compoundwonder.core.service.CacheService;
import com.compoundwonder.core.engine.TickData;
import com.compoundwonder.service.DisruptorService;
import com.compoundwonder.util.SymbolUtil;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DisruptorManager implements DisruptorService {


    private final Disruptor<TickData>[] disruptorArray = new Disruptor[2];
    @Autowired
    private CacheService cacheService;

    public DisruptorManager(Map<Integer, Disruptor<TickData>> disruptorMap) {
        disruptorMap.forEach((k, v) -> disruptorArray[k] = v);
    }


    /**
     * 推送逐笔二合一数据
     */
    public void publishNGTSTickData(int symbolInt, int time, byte dataType, int orderId, int amount, byte direction, int price, int volume, byte bizType, int buyNo, int sellNo) {
        int shard = SymbolUtil.getHandlerIndex(symbolInt);
        Disruptor<TickData> tickDataDisruptor = disruptorArray[shard];
        RingBuffer<TickData> ringBuffer = tickDataDisruptor.getRingBuffer();
        long sequence = ringBuffer.next();
        try {
            TickData event = ringBuffer.get(sequence);
            event.symbolId = symbolInt;
            event.time = time;
            event.dataType = dataType;
            event.orderId = orderId;
            event.amount = amount;
            event.direction = direction;
            event.price = price;
            event.quantity = volume;
            event.type = bizType;
            event.buyerOrderId = buyNo;
            event.sellerOrderId = sellNo;
            event.time1 = System.nanoTime();
        } finally {
            ringBuffer.publish(sequence);
        }
    }


    /**
     * 推送 逐笔委托订单数据
     */
    public void publishTickOrderData(int symbolInt, int time, int orderId, int price, int quantity, byte direction, byte type) {
        int shard = SymbolUtil.getHandlerIndex(symbolInt);
        Disruptor<TickData> tickDataDisruptor = disruptorArray[shard];
        RingBuffer<TickData> ringBuffer = tickDataDisruptor.getRingBuffer();
        long sequence = ringBuffer.next();
        try {
            TickData event = ringBuffer.get(sequence);
            event.symbolId = symbolInt;
            event.time = time;
            event.dataType = 1;
            event.orderId = orderId;
            event.amount = 0;
            event.direction = direction;
            event.price = price;
            event.quantity = quantity;
            event.type = type;
            event.buyerOrderId = 0;
            event.sellerOrderId = 0;
            event.time1 = System.nanoTime();
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    /**
     * 推送委托信息
     * @param symbolInt
     * @param time
     * @param price
     * @param quantity
     * @param direction
     * @param type
     */
    public void pushOrderInfo(int symbolInt, int time, int price, int quantity, byte direction, byte type){
        int shard = SymbolUtil.getHandlerIndex(symbolInt);
        Disruptor<TickData> tickDataDisruptor = disruptorArray[shard];
        RingBuffer<TickData> ringBuffer = tickDataDisruptor.getRingBuffer();
        long sequence = ringBuffer.next();
        try {
            TickData event = ringBuffer.get(sequence);
            event.symbolId = symbolInt;
            event.time = time;
            event.dataType = 5;
            event.orderId = 0;
            event.amount = 0;
            event.direction = direction;
            event.price = price;
            event.quantity = quantity;
            event.type = type;
            event.buyerOrderId = 0;
            event.sellerOrderId = 0;
            event.time1 = System.nanoTime();
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    /**
     * 推送逐笔成交订单数据
     */
    public void publishTickTradeData(int symbolInt, int time, int orderId, int price, int quantity, int amount, byte direction, byte type, int buyerOrderId, int sellerOrderId) {
        int shard = SymbolUtil.getHandlerIndex(symbolInt);
        Disruptor<TickData> tickDataDisruptor = disruptorArray[shard];
        RingBuffer<TickData> ringBuffer = tickDataDisruptor.getRingBuffer();
        long sequence = ringBuffer.next();
        try {
            TickData event = ringBuffer.get(sequence);
            event.symbolId = symbolInt;
            event.time = time;
            event.dataType = 2;
            event.orderId = orderId;
            event.amount = amount;
            event.direction = direction;
            event.price = price;
            event.quantity = quantity;
            event.type = type;
            event.buyerOrderId = buyerOrderId;
            event.sellerOrderId = sellerOrderId;
            event.time1 = System.nanoTime();
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    public void publishLogData(int[] tick) {
        int symbolInt = tick[0];
        int shard = SymbolUtil.getHandlerIndex(symbolInt);
        Disruptor<TickData> tickDataDisruptor = disruptorArray[shard];
        RingBuffer<TickData> ringBuffer = tickDataDisruptor.getRingBuffer();
        long sequence = ringBuffer.next();
        try {
            TickData event = ringBuffer.get(sequence);
            event.symbolId = symbolInt;
            event.dataType = (byte) tick[1];
            event.time = tick[2];
            event.orderId = tick[3];
            event.price = tick[4];
            event.quantity = tick[5];
            event.amount = tick[6];
            event.direction = (byte) tick[7];
            event.type = (byte) tick[8];
            event.buyerOrderId = tick[9];
            event.sellerOrderId = tick[10];
            event.time1 = System.nanoTime();
        } finally {
            ringBuffer.publish(sequence);
        }
    }

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
     *               <br>下单，卖出，把自己的先关了，其他都是关闭状态，其他的暂时不动，也不用发
     */
    public void publishTransInfoData(String symbol, int type) {
        // 全部的盯盘任务，有买有卖，有1进2，有擒龙捉妖
        Set<String> orderBookCodes = cacheService.getOrderBookCodes();
        // 全部擒龙捉妖模式的买入任务
        List<String> qinLongCodes = cacheService.getQinLongCodes();
        byte close = 0, open = 1, sell = -2;
        if (type == 1) {
            // 买入下单，买入，自己这个已经关了，修改成盯撤单状态，然后关闭其他的下单
            for (String bookCode : orderBookCodes) {
                if (!bookCode.equals(symbol)) {
                    this.publishTransInfoDataOne(bookCode, close);
                }
            }
        }
        if (type == 2) {
            //买入成交，买入，自己关了，其他的也关了,在下单的时候其他的已经关了，所以只关自己的就行
            this.publishTransInfoDataOne(symbol, close);
        }
        if (type == 3) {
            //卖出成交，卖出，自己的关了，其他的打开（1,2板；如果持有的是龙头股，3板第二天就是中位）
            if (orderBookCodes.contains(symbol)) {
                this.publishTransInfoDataOne(symbol, close);
            }
            for (String bookCode : orderBookCodes) {
                if (!bookCode.equals(symbol)) {
                    this.publishTransInfoDataOne(bookCode, open);
                }
            }
        }
        if (type == 4) {
            // 如果 是 9 点半之后 撤单就把所有打开
            if (qinLongCodes.isEmpty() || LocalTime.now().isAfter(LocalTime.of(9, 30, 30))) {
                // 如果不是擒龙捉妖 就全部打开，（全部打开要区分是否是已经卖出的，现在只有竞价买或者隔夜单，所以现在不用在意）
                for (String bookCode : orderBookCodes) {
                    this.publishTransInfoDataOne(bookCode, open);
                }
            } else {
                // 买入撤单，如果有擒龙捉妖就只打开擒龙捉妖
                for (String qinLongCode : qinLongCodes) {
                    this.publishTransInfoDataOne(qinLongCode, open);
                }
            }
        }
        if (type == 5) {
            // 如果擒龙捉妖到指定时间还没有买入，就把所有的盯盘都打开
            for (String bookCode : orderBookCodes) {
                this.publishTransInfoDataOne(bookCode, open);
            }
        }
        if (type == 6) {
            // 撤单后继续卖出
            this.publishTransInfoDataOne(symbol, open);
        }
        if (type == 7) {
            // 卖出下单修改自己的状态，基本只有在手动卖出时有用
            this.publishTransInfoDataOne(symbol, sell);
        }
        if (type == 8) {
            if (qinLongCodes.size() == 1) {
                for (String orderBookCode : orderBookCodes) {
                    if (!orderBookCode.equals(symbol)) {
                        this.publishTransInfoDataOne(orderBookCode, open);
                    }
                }
            }
        }
    }

    /**
     * 推送单个消息到盯盘
     *
     * @param symbol
     * @param type
     */
    public void publishTransInfoDataOne(String symbol, byte type) {
        int symbolToInt = SymbolUtil.fastSymbolToInt(symbol);
        int shard = SymbolUtil.getHandlerIndex(symbolToInt);
        Disruptor<TickData> tickDataDisruptor = disruptorArray[shard];
        RingBuffer<TickData> ringBuffer = tickDataDisruptor.getRingBuffer();
        long sequence = ringBuffer.next();
        try {
            TickData event = ringBuffer.get(sequence);
            event.symbolId = symbolToInt;
            event.time = 0;
            event.dataType = 3;
            event.orderId = 0;
            event.amount = 0;
            event.direction = 0;
            event.price = 0;
            event.quantity = 0;
            event.type = type;
            event.buyerOrderId = 0;
            event.sellerOrderId = 0;
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    /**
     * 推送 3 秒行情快照信息
     * 集合竞价 买入（只上海） 卖出
     *
     * @param symbol     代码
     * @param time       快照时间
     * @param sellVolume 竞价涨停总卖
     * @param buyVolume  竞价涨停总买
     */
    public void publishSnapshotData(int symbol, int time, int price, long amount, long sellVolume, long buyVolume) {

        if (sellVolume > Integer.MAX_VALUE) {
            sellVolume = Integer.MAX_VALUE;
        }
        if (buyVolume > Integer.MAX_VALUE) {
            buyVolume = Integer.MAX_VALUE;
        }

        int shard = SymbolUtil.getHandlerIndex(symbol);
        Disruptor<TickData> tickDataDisruptor = disruptorArray[shard];
        RingBuffer<TickData> ringBuffer = tickDataDisruptor.getRingBuffer();
        long sequence = ringBuffer.next();
        try {
            TickData event = ringBuffer.get(sequence);
            event.symbolId = symbol;
            event.time = time;
            event.dataType = 4;
            if (amount > Integer.MAX_VALUE) {
                event.type = 1;
                event.orderId = (int) (amount / 100);
            } else {
                event.type = 0;
                event.orderId = (int) amount;
            }
            event.direction = 0;
            event.price = price;
            event.quantity = (int) buyVolume;//集合竞价期间 买数量 手 、、 非集合竞价期间 买一量
            event.buyerOrderId = (int) buyVolume;//集合竞价期间 买数量 手 、、 非集合竞价期间 买一量
            event.sellerOrderId = (int) sellVolume;//集合竞价期间 卖数量 手、、 非集合竞价期间 总成交量
            event.time1 = System.nanoTime();
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    /**
     * 推送 实时快照信息
     * 集合竞价 买入（只上海） 卖出
     *
     * @param symbol     代码
     * @param time       快照时间
     * @param sellVolume 竞价涨停总卖
     * @param buyVolume  竞价涨停总买
     */
    public void pushRealTimeSnapshot(int symbol, int time, int price, long amount, long sellVolume, long buyVolume) {

        if (sellVolume > Integer.MAX_VALUE) {
            sellVolume = Integer.MAX_VALUE;
        }
        if (buyVolume > Integer.MAX_VALUE) {
            buyVolume = Integer.MAX_VALUE;
        }

        int shard = SymbolUtil.getHandlerIndex(symbol);
        Disruptor<TickData> tickDataDisruptor = disruptorArray[shard];
        RingBuffer<TickData> ringBuffer = tickDataDisruptor.getRingBuffer();
        long sequence = ringBuffer.next();
        try {
            TickData event = ringBuffer.get(sequence);
            event.symbolId = symbol;
            event.time = time;
            event.dataType = 5;
            if (amount > Integer.MAX_VALUE) {
                event.type = 1;
                event.orderId = (int) (amount / 100);
            } else {
                event.type = 0;
                event.orderId = (int) amount;
            }
            event.direction = 0;
            event.price = price;
            event.quantity = (int) buyVolume;//集合竞价期间 买数量 手 、、 非集合竞价期间 买一量
            event.buyerOrderId = (int) buyVolume;//集合竞价期间 买数量 手 、、 非集合竞价期间 买一量
            event.sellerOrderId = (int) sellVolume;//集合竞价期间 卖数量 手、、 非集合竞价期间 总成交量
            event.time1 = System.nanoTime();
        } finally {
            ringBuffer.publish(sequence);
        }
    }

}
