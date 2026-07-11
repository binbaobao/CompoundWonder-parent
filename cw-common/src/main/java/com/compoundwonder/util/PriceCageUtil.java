package com.compoundwonder.util;

/**
 * 价格笼子计算
 */
public class PriceCageUtil {

    private static final int THRESHOLD = 500; // 5元
    private static final int LOW_PRICE_RANGE = 10; // 0.1元 = 10

    /**
     * 获取价格笼子上限
     *
     * @param price 基准价（已放大100倍，例如 10.12 -> 1012）
     * @return 上限价格（同样放大100倍）
     */
    public static int getUpperLimit(int price) {
        if (price <= 0) {
            throw new IllegalArgumentException("price must be positive");
        }

        if (price >= THRESHOLD) {
            // 上浮 2%
            int delta = price * 2 / 100;
            return price + delta - 2;
        } else {
            // 上浮 0.1元
            return price + LOW_PRICE_RANGE;
        }
    }

    /**
     * 获取价格笼子下限
     *
     * @param price 基准价（已放大100倍）
     * @return 下限价格
     */
    public static int getLowerLimit(int price,int limitDownPrice) {
        if (price <= 0) {
            throw new IllegalArgumentException("price must be positive");
        }
        int retPrice ;
        if (price >= THRESHOLD) {
            // 下浮 2%
            int delta = price * 2 / 100;
            retPrice =  price - delta + 2;
        } else {
            // 下浮 0.1元
            retPrice = price - LOW_PRICE_RANGE;
        }
        return Math.max(retPrice, limitDownPrice);
    }
}
