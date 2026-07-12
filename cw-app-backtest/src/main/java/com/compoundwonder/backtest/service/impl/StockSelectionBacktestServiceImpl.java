package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.backtest.service.StockSelectionBacktestService;
import com.compoundwonder.trader.service.StockWatchingTaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class StockSelectionBacktestServiceImpl implements StockSelectionBacktestService {


    private final StockWatchingTaskService stockWatchingTaskService;
    public StockSelectionBacktestServiceImpl(StockWatchingTaskService stockWatchingTaskService) {
        this.stockWatchingTaskService = stockWatchingTaskService;
    }

    @Override
    public void stockSelectionBacktest(String date) {
        stockWatchingTaskService.createPostCloseWatchingTasks(LocalDate.parse(date));
    }
}
