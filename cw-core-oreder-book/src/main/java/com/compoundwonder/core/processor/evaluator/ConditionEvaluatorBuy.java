package com.compoundwonder.core.processor.evaluator;


import cn.hutool.core.util.StrUtil;
import com.compoundwonder.core.constant.ConstantUtil;
import com.compoundwonder.core.constant.RuleConstant;
import com.compoundwonder.core.engine.RuleRecord;
import com.compoundwonder.core.engine.OrderBook;
import com.compoundwonder.core.util.CompactTimeUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 买入的条件评估器
 */
@Slf4j
public class ConditionEvaluatorBuy {
    /**
     * 主评估方法
     * TODO 快速拉升，卖单较少，大委托单
     *
     * @param orderBook 当前股票盘口快照
     * @return bitmask 标志位
     */
    public static boolean evaluate(OrderBook orderBook, RuleRecord ruleRecord) {

        // 提前取出字段，避免重复访问 getter
        long mv = orderBook.getInitialMarketValue();      // 市值 (万元)
        double turnover = orderBook.getTurnoverRate();    // 换手率 (%)
//        long volume = orderBook.getVolume();              // 当前成交量
        long maxVolume = orderBook.getMaxVolume();        // 截止当日最大成交量
        int lastPrice = orderBook.getLastPrice();         // 最新价
        int limitUpPrice = orderBook.getLimitUpPrice();   // 涨停价
        long limitUpBuyAmount = orderBook.getLimitUpBuyAmount(); // 涨停封单量
        int status = orderBook.getStatus();               // 状态标志（奇数代表涨停中）
        long tradingAmount = orderBook.getTurnover();     // 成交额
        double amplitude = orderBook.getAmplitude();
        double limitUpBreakDepth = orderBook.getLimitUpBreakDepth();// 炸板深度
        long circulation = orderBook.getCirculation();
        double maxHs = maxVolume * 100.0 / circulation;
        double changePercent = orderBook.getChangePercent();
        int lbc = orderBook.getLbcs();
        //最新涨停时间
        int lastLimitUpMillis = CompactTimeUtil.compactToMillis(orderBook.getLastLimitUptime());
        int time = orderBook.getTime();
        int updateMillis = CompactTimeUtil.compactToMillis(time);
        double increase = orderBook.getIncrease();
        // --- 区间参数定义 ---
        boolean limitUpOk;
        // --- 市值区间判断 ---

        boolean hsFlg = Boolean.FALSE;
        if ((lbc == 1 || amplitude > 7 || limitUpBreakDepth > 3 || tradingAmount > 250_000_000L)
                && maxHs / 3.3 <= turnover && turnover <= 35) {
            hsFlg = Boolean.TRUE;
        } else if (12.5 <= turnover && turnover <= 35) {
            hsFlg = Boolean.TRUE;
        }
        if (mv < 90_000) {
            limitUpOk = lastPrice == limitUpPrice;
        } else if (mv < 140_000) {
            limitUpOk = limitUpBuyAmount > 500;
        } else if (mv < 155_000) {
            limitUpOk = limitUpBuyAmount > 1000;
            if (time >= ConstantUtil.TIME_1030) {
                limitUpOk = limitUpBuyAmount > 500;
            }
        } else if (mv < 170_000) {
            limitUpOk = limitUpBuyAmount > 2000;
            if (time >= ConstantUtil.TIME_1030) {
                limitUpOk = limitUpBuyAmount > 800;
            }
        } else if (mv < 185_000) {
            limitUpOk = limitUpBuyAmount > 3000;
            if (time >= ConstantUtil.TIME_1030) {
                limitUpOk = limitUpBuyAmount > 1000;
            }
        } else {
            limitUpOk = limitUpBuyAmount > 4500;
            if (time >= ConstantUtil.TIME_1030) {
                limitUpOk = limitUpBuyAmount > 2000;
            }
        }
        // 当换手大于最小限制的时候，发现买入方向申报价格是涨停价的大委托单(大于9000手，或者单笔超过700w)， 立马跟随 limitUpPrice > 1000
        if (orderBook.buyMaxOrder.getPrice() != 0 && limitUpBuyAmount <= 8000 && hsFlg && orderBook.buyMaxOrder.getPrice() == limitUpPrice && changePercent >= 0 && (lastLimitUpMillis == 0 || (updateMillis - lastLimitUpMillis) < 30000)) {
            if (limitUpPrice > 1000) {
                if (mv < 198_000 && (orderBook.buyMaxOrder.getQuantity() >= 990000 || (orderBook.buyMaxOrder.getQuantity() / 100 * orderBook.buyMaxOrder.getPrice()) > 9000_000)) {
                    String remark = StrUtil.format("大单扫板：市值中大 - 金额与数量都要大 ，大单扫板跟随；条件：启动市值 {} 万，涨停封单金额 {} 万，当前换手 {} %，封单变化EMA {} %，大单委托数量 {}",
                            mv, limitUpBuyAmount,  turnover, changePercent, orderBook.buyMaxOrder.getQuantity());
                    log.info(remark);
                    ruleRecord.fill(RuleConstant.TRADING_MODE_BUY, 11, orderBook.getSymbol(), time, lastPrice, increase, remark);
                    return true;
                }
                if (mv < 150_000 && (orderBook.buyMaxOrder.getQuantity() >= 900000 || (orderBook.buyMaxOrder.getQuantity() / 100 * orderBook.buyMaxOrder.getPrice()) > 7000_000)) {
                    String remark = StrUtil.format("大单扫板：市值中小 - 金额与数量也中等 价格中大单扫板跟随；条件：启动市值 {} 万，涨停封单金额 {} 万，当前换手 {} %，封单变化EMA {} %，大单委托数量 {}",
                            mv, limitUpBuyAmount,  turnover, changePercent, orderBook.buyMaxOrder.getQuantity());
                    log.info(remark);
                    ruleRecord.fill(RuleConstant.TRADING_MODE_BUY, 12, orderBook.getSymbol(), time, lastPrice, increase, remark);
                    return true;
                }
            } else {
                if (orderBook.buyMaxOrder.getQuantity() >= 888800) {
                    String remark = StrUtil.format("大单扫板：低价股 - 要求必须是 8888 以上，大单扫板跟随；条件：启动市值 {} 万，涨停封单金额 {} 万，当前换手 {} %，封单变化EMA {} %，大单委托数量 {}",
                            mv, limitUpBuyAmount,  turnover, changePercent, orderBook.buyMaxOrder.getQuantity());
                    log.info(remark);
                    ruleRecord.fill(RuleConstant.TRADING_MODE_BUY, 13, orderBook.getSymbol(), time, lastPrice, increase, remark);
                    return true;
                }
            }
        }

        // 现在只能扫板了，因为lv2行情不准确
        if (limitUpBuyAmount <= 8000 && hsFlg && limitUpOk && (lastLimitUpMillis == 0 || (updateMillis - lastLimitUpMillis) < 30000)) {
            String remark = StrUtil.format("正常排板：启动市值 {} 万，涨停封单金额 {} 万，当前换手 {} %，封单变化EMA {} %，涨停下单时间差:{} ms", mv, limitUpBuyAmount,  turnover,changePercent, updateMillis - lastLimitUpMillis);
            log.info(remark);
            ruleRecord.fill(RuleConstant.TRADING_MODE_BUY, 14, orderBook.getSymbol(), time, lastPrice, increase, remark);
            // 封单比较大就不用买了
            return true;
        } else {
            return false;
        }
    }
}
