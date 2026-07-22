package com.compoundwonder.backtest.orderbook;

import com.compoundwonder.common.orderbook.OrderExecutionGateway;
import com.compoundwonder.common.orderbook.TradeOrderIntent;
import com.compoundwonder.util.SymbolUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 回测交易动作记录器。第一阶段只记录策略信号，后续由模拟账户撮合实现消费这些动作。
 */
@Component
public class BacktestOrderExecutionGateway implements OrderExecutionGateway {

    private final List<Action> actions = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void execute(TradeOrderIntent intent) {
        actions.add(new Action(ActionType.valueOf(intent.action().name()), intent.date(),
                intent.symbol(), intent.price(), intent.limitDownPrice(), intent.time(),
                intent.strategySessionId(), intent.strategyId()));
    }

    @Override
    public void buy(String date, int symbol, int price, int time) {
        actions.add(new Action(ActionType.BUY, date, SymbolUtil.intToSymbol(symbol), price, 0, time));
    }

    @Override
    public void sell(String symbol, int price, int limitDownPrice) {
        actions.add(new Action(ActionType.SELL, null, symbol, price, limitDownPrice, 0));
    }

    @Override
    public void quickSell(String symbol, int price, int limitDownPrice) {
        actions.add(new Action(ActionType.QUICK_SELL, null, symbol, price, limitDownPrice, 0));
    }

    @Override
    public void cancel(String symbol) {
        actions.add(new Action(ActionType.CANCEL, null, symbol, 0, 0, 0));
    }

    @Override
    public void enableFirstLimitUpTradingMode(String symbol) {
        actions.add(new Action(ActionType.ENABLE_FIRST_LIMIT_UP_MODE, null, symbol, 0, 0, 0));
    }

    public List<Action> actions() {
        synchronized (actions) {
            return List.copyOf(actions);
        }
    }

    public void clear() {
        actions.clear();
    }

    public enum ActionType {
        BUY,
        SELL,
        QUICK_SELL,
        CANCEL,
        ENABLE_FIRST_LIMIT_UP_MODE
    }

    public record Action(ActionType type, String date, String symbol, int price,
                         int limitDownPrice, int time,
                         String strategySessionId, String strategyId) {
        public Action(ActionType type, String date, String symbol, int price,
                      int limitDownPrice, int time) {
            this(type, date, symbol, price, limitDownPrice, time, null, null);
        }
    }
}
