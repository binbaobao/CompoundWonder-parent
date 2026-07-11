package com.compoundwonder.backtest.schedule;

import com.compoundwonder.hxdata.service.StockTradeCalendarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class StockSelectionTaskScheduler {

    private StockTradeCalendarService stockTradeCalendarService;

    public StockSelectionTaskScheduler(StockTradeCalendarService stockTradeCalendarService) {
        this.stockTradeCalendarService = stockTradeCalendarService;
    }

    @Scheduled(cron = "00 48 17 * * ?", zone = "Asia/Shanghai")
    public void schedule() {
        LocalDate now = LocalDate.now();
        LocalDate parse = LocalDate.parse("2023-01-01");

        while (parse.isBefore(now)) {
            boolean tradeDay = stockTradeCalendarService.isTradeDay(parse);
            System.out.println(parse + " ----------------- " + tradeDay);
            if (tradeDay) {

            }

            parse = parse.plusDays(1);
        }
    }

}
