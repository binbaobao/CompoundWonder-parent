package com.compoundwonder.backtest.config;

import com.compoundwonder.common.mysqldata.selection.StockSelectionDataService;
import com.compoundwonder.common.strategy.selection.StockSelectionService;
import com.compoundwonder.common.strategy.trade.TradeDecisionService;
import com.compoundwonder.strategy.TradeStrategyDispatcher;
import com.compoundwonder.strategy.selection.DefaultStockSelectionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 回测应用对选股与高频交易策略实现的统一装配。 */
@Configuration
public class BacktestStrategyConfig {

    /** 组装快照与 Level2 事件使用的三模式交易判断实现。 */
    @Bean
    public TradeDecisionService tradeDecisionService() {
        return new TradeStrategyDispatcher();
    }

    /** 组装收盘后三种模式选股实现。 */
    @Bean
    public StockSelectionService stockSelectionService(
            StockSelectionDataService stockSelectionDataService) {
        return new DefaultStockSelectionService(stockSelectionDataService);
    }
}
