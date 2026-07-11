package com.compoundwonder.backtest.schedule;

import com.compoundwonder.backtest.service.EmotionCycleDailyAggregateService;
import com.compoundwonder.hxdata.service.StockTradeCalendarService;
import com.compoundwonder.trader.entity.StockEmotionCycleDaily;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class StockSelectionTaskScheduler {

    private final StockTradeCalendarService stockTradeCalendarService;
    private final EmotionCycleDailyAggregateService emotionCycleDailyAggregateService;

    public StockSelectionTaskScheduler(StockTradeCalendarService stockTradeCalendarService, EmotionCycleDailyAggregateService emotionCycleDailyAggregateService) {
        this.stockTradeCalendarService = stockTradeCalendarService;
        this.emotionCycleDailyAggregateService = emotionCycleDailyAggregateService;
    }

    @Scheduled(cron = "00 57 18 * * ?", zone = "Asia/Shanghai")
    public void schedule() {
        LocalDate now = LocalDate.now();
        LocalDate parse = LocalDate.parse("2023-01-01");

        while (parse.isBefore(now)) {
            boolean tradeDay = stockTradeCalendarService.isTradeDay(parse);
            System.out.println(parse + " ----------------- " + tradeDay);
            if (tradeDay) {
                StockEmotionCycleDaily stockEmotionCycleDaily = emotionCycleDailyAggregateService.aggregateAndSave(parse);
                log.info("");
            }

            parse = parse.plusDays(1);
        }
    }

}
