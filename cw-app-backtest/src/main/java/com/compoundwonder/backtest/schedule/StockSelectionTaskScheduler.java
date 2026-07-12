package com.compoundwonder.backtest.schedule;

import com.compoundwonder.hxdata.service.StockTradeCalendarService;
import com.compoundwonder.trader.entity.StockEmotionCycleDaily;
import com.compoundwonder.trader.service.StockEmotionCycleDailyService;
import com.compoundwonder.trader.service.StockWatchingTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class StockSelectionTaskScheduler {

    private final StockTradeCalendarService stockTradeCalendarService;
    private final StockEmotionCycleDailyService stockEmotionCycleDailyService;
    private final StockWatchingTaskService stockWatchingTaskService;

    public StockSelectionTaskScheduler(StockTradeCalendarService stockTradeCalendarService,
                                       StockEmotionCycleDailyService stockEmotionCycleDailyService,
                                       StockWatchingTaskService stockWatchingTaskService) {
        this.stockTradeCalendarService = stockTradeCalendarService;
        this.stockEmotionCycleDailyService = stockEmotionCycleDailyService;
        this.stockWatchingTaskService = stockWatchingTaskService;
    }

    @Scheduled(cron = "00 10 19 * * ?", zone = "Asia/Shanghai")
    public void schedule() {
        LocalDate now = LocalDate.now();
        LocalDate parse = LocalDate.parse("2023-08-01");

        while (parse.isBefore(now)) {
            boolean tradeDay = stockTradeCalendarService.isTradeDay(parse);
            System.out.println(parse + " ----------------- " + tradeDay);
            if (tradeDay) {
                int taskCount = stockWatchingTaskService.createRelayLimitUpTasks(parse).size();
                log.info("收盘后选股任务生成完成 tradeDate={} taskCount={}", parse, taskCount);
            }
            parse = parse.plusDays(1);
        }
    }

}
