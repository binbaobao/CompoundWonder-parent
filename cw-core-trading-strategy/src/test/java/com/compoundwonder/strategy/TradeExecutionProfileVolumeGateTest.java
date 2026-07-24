package com.compoundwonder.strategy;

import com.compoundwonder.common.orderbook.TradeStaticFacts;
import com.compoundwonder.common.strategy.trade.TradeExecutionProfile;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TradeExecutionProfileVolumeGateTest {

    @ParameterizedTest(name = "二板前一天量能状态 {0} 是否允许隔夜与竞价买入：{1}")
    @CsvSource({
            "-1, false",
            " 0, true",
            " 1, true"
    })
    void secondBoardRequiresPreviousVolumeStateAtLeastZero(int yesterdayVolumeState, boolean expectedAllowed) {
        // 调用 createProfile 构建目标二板的交易执行档案。
        TradeExecutionProfile profile = createProfile(2, 1, yesterdayVolumeState, 0);

        // 调用 assertEquals 验证目标二板的隔夜与开盘竞价资格。
        assertEquals(expectedAllowed, profile.openingAuctionBuyAllowed());
        if (!expectedAllowed) {
            // 调用 assertEquals 验证量能门槛不推迟连续竞价开始时间。
            assertEquals(0, profile.earliestContinuousBuyTime());
            // 调用 assertEquals 验证目标二板的量能阻断原因。
            assertEquals("首板量能状态小于0，禁止二板隔夜与开盘集合竞价买入", profile.openingAuctionBlockReason());
        }
    }

    @ParameterizedTest(name = "三板前两天量能状态 {0}+{1} 是否允许隔夜与竞价买入：{2}")
    @CsvSource({
            "-1, -1, false",
            "-1,  0, true",
            "-1,  1, true",
            " 0, -1, true",
            " 0,  0, true",
            " 0,  1, true",
            " 1, -1, true",
            " 1,  0, true",
            " 1,  1, true"
    })
    void thirdBoardRequiresPreviousTwoDayVolumeStateSumAtLeastNegativeOne(int yesterdayVolumeState, int twoDaysAgoVolumeState, boolean expectedAllowed) {
        // 调用 createProfile 构建目标三板的交易执行档案。
        TradeExecutionProfile profile = createProfile(1, 2, yesterdayVolumeState, twoDaysAgoVolumeState);

        // 调用 assertEquals 验证目标三板的隔夜与开盘竞价资格。
        assertEquals(expectedAllowed, profile.openingAuctionBuyAllowed());
        if (!expectedAllowed) {
            // 调用 assertEquals 验证量能门槛不推迟连续竞价开始时间。
            assertEquals(0, profile.earliestContinuousBuyTime());
            // 调用 assertEquals 验证目标三板的量能阻断原因。
            assertEquals("首板与二板量能状态和小于-1，禁止三板隔夜与开盘集合竞价买入", profile.openingAuctionBlockReason());
        }
    }

    @ParameterizedTest(name = "目标板位 {1} 缺失量能状态时禁止隔夜与竞价买入")
    @CsvSource({
            "-2, 1, 0",
            "-2, 2, 0",
            "-2, 2, 1",
            " 0, 2, -2",
            " 1, 2, -2"
    })
    void blocksPreOpenBuyWhenRequiredVolumeStateIsUnavailable(int yesterdayVolumeState, int previousBoardHeight, int twoDaysAgoVolumeState) {
        // 调用 createProfile 构建历史量能状态不足的交易执行档案。
        TradeExecutionProfile profile = createProfile(previousBoardHeight == 1 ? 2 : 1, previousBoardHeight, yesterdayVolumeState, twoDaysAgoVolumeState);

        // 调用 assertFalse 验证历史量能状态不足时禁止隔夜与开盘竞价买入。
        assertFalse(profile.openingAuctionBuyAllowed());
        // 调用 assertEquals 验证数据不足不会推迟连续竞价开始时间。
        assertEquals(0, profile.earliestContinuousBuyTime());
        // 调用 assertEquals 验证数据不足原因明确可统计。
        assertEquals("历史量能状态数据不足，禁止隔夜与开盘集合竞价买入", profile.openingAuctionBlockReason());
    }

    private TradeExecutionProfile createProfile(int tradeMode, int previousBoardHeight, int yesterdayVolumeState, int twoDaysAgoVolumeState) {
        // 调用 TradeStaticFacts 构造器生成量能状态明确的盘前静态事实。
        TradeStaticFacts facts = new TradeStaticFacts(tradeMode, previousBoardHeight, 1_000_000L, 35D, 90_000, 18D, 20D, 25D, 0, 6, 0, 1, 1, 8D, 20D, 8D,
                yesterdayVolumeState, twoDaysAgoVolumeState);
        // 调用 TradeExecutionProfile.from 编译统一的隔夜与开盘竞价资格。
        return TradeExecutionProfile.from(facts);
    }
}
