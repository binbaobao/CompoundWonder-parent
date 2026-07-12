package com.compoundwonder.backtest.schedule;

import com.compoundwonder.hxdata.service.StockTradeCalendarService;
import com.compoundwonder.trader.entity.StockEmotionCycleDaily;
import com.compoundwonder.trader.service.StockEmotionCycleDailyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class StockSelectionTaskScheduler {

    private final StockTradeCalendarService stockTradeCalendarService;
    private final StockEmotionCycleDailyService stockEmotionCycleDailyService;

    public StockSelectionTaskScheduler(StockTradeCalendarService stockTradeCalendarService, StockEmotionCycleDailyService stockEmotionCycleDailyService) {
        this.stockTradeCalendarService = stockTradeCalendarService;
        this.stockEmotionCycleDailyService = stockEmotionCycleDailyService;
    }

    @Scheduled(cron = "20 06 12 * * ?", zone = "Asia/Shanghai")
    public void schedule() {
        LocalDate now = LocalDate.now();
        LocalDate parse = LocalDate.parse("2023-01-01");

        while (parse.isBefore(now)) {
            boolean tradeDay = stockTradeCalendarService.isTradeDay(parse);
            System.out.println(parse + " ----------------- " + tradeDay);
            if (tradeDay) {
                StockEmotionCycleDaily stockEmotionCycleDaily = stockEmotionCycleDailyService.aggregateAndSave(parse);
                log.info("");
            }
            parse = parse.plusDays(1);
        }
    }

}
