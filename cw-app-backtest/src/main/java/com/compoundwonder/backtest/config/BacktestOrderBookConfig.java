package com.compoundwonder.backtest.config;

import com.compoundwonder.core.engine.DisruptorOrderBookEngine;
import com.compoundwonder.core.service.OrderBookRepository;
import com.compoundwonder.common.orderbook.OrderExecutionGateway;
import com.compoundwonder.common.strategy.trade.TradeDecisionService;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 回测应用对核心订单簿的运行时装配。
 */
@Configuration
public class BacktestOrderBookConfig {

    @Bean(initMethod = "start", destroyMethod = "close")
    public DisruptorOrderBookEngine disruptorOrderBookEngine(
            OrderBookRepository orderBookRepository,
            OrderExecutionGateway orderExecutionGateway,
            TradeDecisionService tradeDecisionService,
            @Value("${backtest.order-book.ring-buffer-size:1048576}") int ringBufferSize) {
        return new DisruptorOrderBookEngine(
                orderBookRepository,
                orderExecutionGateway,
                tradeDecisionService,
                ringBufferSize,
                "backtest-order-book-",
                ProducerType.SINGLE,
                YieldingWaitStrategy::new);
    }
}
