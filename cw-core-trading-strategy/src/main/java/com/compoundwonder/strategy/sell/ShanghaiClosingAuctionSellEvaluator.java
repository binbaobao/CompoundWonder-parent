package com.compoundwonder.strategy.sell;

import cn.hutool.core.util.StrUtil;
import com.compoundwonder.common.orderbook.AuctionMarketEvent;
import com.compoundwonder.common.orderbook.TradeMarketState;
import com.compoundwonder.common.orderbook.TradeRuleRecord;
import com.compoundwonder.constant.RuleConstant;
import lombok.extern.slf4j.Slf4j;

/** 上海收盘集合竞价卖出场景。 */
@Slf4j
public final class ShanghaiClosingAuctionSellEvaluator {

    private ShanghaiClosingAuctionSellEvaluator() {
    }

    /**
     * 盯盘卖出状态下，尾盘竞价盯盘，最后几秒判断是否是涨停。
     * 上海后期可设置一个 14:59:58 的定时任务检查订单簿状态。
     */
    public static boolean evaluate(TradeMarketState market, AuctionMarketEvent event,
                                   int recordTime, TradeRuleRecord record) {
        // 如果竞价价格比涨停价格低，或者竞价买小于竞价卖。
        if (!ClosingAuctionSellEvaluator.evaluate(event.getPrice(), market.getLimitUpPrice(),
                event.getBuyerOrderId(), event.getSellerOrderId())) {
            return false;
        }

        String remark = StrUtil.format(
                "卖出 - 尾盘 {} 竞价 ： 如果竞价价格 {} 比涨停价格低 {} 或者竞价买 {} 小于竞价卖 {}, 股票代码 {} 以跌停价格 {} 卖出",
                event.getTime(), event.getPrice(), market.getLimitUpPrice(),
                event.getBuyerOrderId(), event.getSellerOrderId(), market.getSymbol(),
                market.getLimitDownPrice());
        log.info(remark);
        record.fill(RuleConstant.TRADING_MODE_SELL, 1, market.getSymbol(), recordTime,
                event.getPrice(), increase(event.getPrice(), market.getClosePrice()), remark);
        return true;
    }

    private static double increase(int price, int closePrice) {
        return (price - closePrice) * 100.0 / closePrice;
    }
}
