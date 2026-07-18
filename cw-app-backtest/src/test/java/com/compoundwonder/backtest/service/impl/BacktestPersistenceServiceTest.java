package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.constant.RuleConstant;
import com.compoundwonder.dto.RuleRecordDTO;
import com.compoundwonder.trader.entity.BacktestDailyRecord;
import com.compoundwonder.trader.entity.BacktestPosition;
import com.compoundwonder.trader.entity.RuleExecuteRecord;
import com.compoundwonder.trader.entity.StockWatchingTask;
import com.compoundwonder.trader.mapper.BacktestDailyRecordMapper;
import com.compoundwonder.trader.mapper.BacktestPositionMapper;
import com.compoundwonder.trader.mapper.BacktestRunMapper;
import com.compoundwonder.trader.mapper.RuleExecuteRecordMapper;
import com.compoundwonder.trader.mapper.StockWatchingTaskMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BacktestPersistenceServiceTest {

    @Test
    void savesEveryTriggeredRuleAndKeepsExecutedBuyAsTheRichRecord() {
        List<RuleExecuteRecord> savedRules = new ArrayList<>();
        BacktestRunMapper runMapper = noOpMapper(BacktestRunMapper.class);
        BacktestPositionMapper positionMapper = noOpMapper(BacktestPositionMapper.class);
        BacktestDailyRecordMapper dailyRecordMapper = noOpMapper(BacktestDailyRecordMapper.class);
        RuleExecuteRecordMapper ruleMapper = ruleMapper(savedRules);
        StockWatchingTaskMapper watchingTaskMapper = noOpMapper(StockWatchingTaskMapper.class);
        BacktestPersistenceService service = new BacktestPersistenceService(
                runMapper, positionMapper, dailyRecordMapper, ruleMapper, watchingTaskMapper);

        LocalDate tradeDate = LocalDate.of(2026, 7, 14);
        StockWatchingTask task = watchingTask(9L, "600001");
        RuleRecordDTO executedBuy = buyRule("600001", 100_000_000, 100_000_600);
        RuleRecordDTO submittedTooLate = buyRule("600001", 101_000_000, 101_000_400);
        BacktestPosition position = position(12L, task, tradeDate);
        BacktestDailyRecord dailyRecord = new BacktestDailyRecord();

        service.saveDay(new BacktestDayWrite(
                7L, tradeDate, null, position,
                null, executedBuy, task, List.of(),
                List.of(
                        new BacktestRuleAction(task, executedBuy),
                        new BacktestRuleAction(task, submittedTooLate)),
                dailyRecord));

        assertEquals(2, savedRules.size());
        RuleExecuteRecord savedExecutedBuy = savedRules.get(0);
        assertEquals(12L, savedExecutedBuy.getPositionId());
        assertEquals(9_900, savedExecutedBuy.getQuantity());
        assertEquals(new BigDecimal("99000.00"), savedExecutedBuy.getTradeAmount());

        RuleExecuteRecord savedLateRule = savedRules.get(1);
        assertEquals(101_000_000, savedLateRule.getTime());
        assertEquals(101_000_400, savedLateRule.getLastOrderTime());
        assertNull(savedLateRule.getQuantity());
        assertNull(savedLateRule.getTradeAmount());
    }

    @SuppressWarnings("unchecked")
    private <T> T noOpMapper(Class<T> mapperType) {
        return (T) Proxy.newProxyInstance(
                mapperType.getClassLoader(), new Class<?>[]{mapperType},
                (proxy, method, args) -> defaultValue(method.getReturnType()));
    }

    private RuleExecuteRecordMapper ruleMapper(List<RuleExecuteRecord> savedRules) {
        return (RuleExecuteRecordMapper) Proxy.newProxyInstance(
                RuleExecuteRecordMapper.class.getClassLoader(),
                new Class<?>[]{RuleExecuteRecordMapper.class},
                (proxy, method, args) -> {
                    if ("insert".equals(method.getName())) {
                        savedRules.add((RuleExecuteRecord) args[0]);
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private Object defaultValue(Class<?> returnType) {
        if (returnType == int.class) {
            return 1;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == boolean.class) {
            return false;
        }
        return null;
    }

    private StockWatchingTask watchingTask(long id, String symbol) {
        StockWatchingTask task = new StockWatchingTask();
        task.setId(id);
        task.setStockCode(symbol);
        task.setStockName("测试股票");
        task.setTradeMode(1);
        task.setLimitUpScore(90);
        return task;
    }

    private BacktestPosition position(long id, StockWatchingTask task, LocalDate tradeDate) {
        BacktestPosition position = new BacktestPosition();
        position.setId(id);
        position.setWatchingTaskId(task.getId());
        position.setSymbol(task.getStockCode());
        position.setSymbolName(task.getStockName());
        position.setBuyDate(tradeDate);
        position.setQuantity(9_900);
        position.setBuyAmount(new BigDecimal("99000.00"));
        position.setBuyFee(new BigDecimal("13.86"));
        position.setTradeMode(task.getTradeMode());
        position.setLimitUpScore(task.getLimitUpScore());
        return position;
    }

    private RuleRecordDTO buyRule(String symbol, int time, int lastOrderTime) {
        RuleRecordDTO rule = new RuleRecordDTO();
        rule.setActionType(RuleConstant.TRADING_MODE_BUY);
        rule.setRuleCode(14);
        rule.setSymbol(symbol);
        rule.setTime(time);
        rule.setLastOrderTime(lastOrderTime);
        rule.setPrice(1_000);
        return rule;
    }
}
