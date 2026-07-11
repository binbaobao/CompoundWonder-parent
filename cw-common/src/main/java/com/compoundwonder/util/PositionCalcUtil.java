package com.compoundwonder.util;

/**
 * 仓位计算工具类（极简版：基于满仓组合判断）
 *
 * 逻辑：
 * 1. 不计算已用资金
 * 2. 只判断该模式还能不能买
 * 3. 每次买入金额固定
 * 4. 可用资金不够 → 返回全部可用资金
 */
public final class PositionCalcUtil {

    private PositionCalcUtil() {}

    // 每个模式最大持仓数（满仓组合）
    private static final int[] MAX_COUNTS = {1, 2, 3};

    // 每个模式单只仓位比例
    private static final double[] PER_STOCK_RATIO = {
            0.4,  // 模式1：1只 → 40%
            0.3,  // 模式2：每只30%
            0.2   // 模式3：每只20%
    };

    /**
     * 计算当前模式可买金额
     *
     * @param totalAmount   总资产（包含持仓）
     * @param availableCash 可用资金
     * @param mode          模式（1/2/3）
     * @param counts        当前持仓数量 [mode1, mode2, mode3]
     * @return 可买金额
     */
    public static double calcBuyAmount(double totalAmount,
                                       double availableCash,
                                       int mode,
                                       int[] counts) {

        if (totalAmount <= 0 || availableCash <= 1000) {
            return 0.0;
        }

        int idx = mode - 1;

        // 当前模式持仓数
        int current = counts[idx];

        // 最大可持仓数
        int max = MAX_COUNTS[idx];

        // 已满，不可再买
        if (current >= max) {
            return 0.0;
        }

        // 本次买入固定金额
        double buyAmount = totalAmount * PER_STOCK_RATIO[idx];

        // 可用资金不足 → 全部用掉
        return Math.min(buyAmount, availableCash);
    }
}