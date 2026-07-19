package com.compoundwonder.strategy.sell;

/**
 * 买入日炸板后下一交易日开盘卖出的回测专用策略。
 *
 * <p>该场景不依赖 Level2，也不进入正常的 09:31 盘中卖出判断。回测应用在
 * 下一交易日 09:25 集合竞价结束时，按日 K 开盘价生成合成卖出记录。策略模块
 * 只判断是否属于该场景，数据库查询和规则落库仍由回测应用负责。</p>
 */
public final class BreakBoardNextOpenSellPolicy {

    private BreakBoardNextOpenSellPolicy() {
    }

    /**
     * @param buyDayKlineState 买入日 K 线状态
     * @return 状态 11、12、13 表示买入日发生炸板，需要次日开盘卖出
     */
    public static boolean shouldSellAtNextOpen(Integer buyDayKlineState) {
        return buyDayKlineState != null
                && (buyDayKlineState == 11 || buyDayKlineState == 12 || buyDayKlineState == 13);
    }
}
