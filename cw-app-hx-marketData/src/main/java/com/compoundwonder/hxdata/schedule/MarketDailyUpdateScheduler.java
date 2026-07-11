package com.compoundwonder.hxdata.schedule;

import com.compoundwonder.hxdata.api.BasicDataApi;
import com.compoundwonder.hxdata.service.StockDailyUpdateTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 股票每日更新定时任务。
 * 作用：按固定时间触发盘前基础数据更新和盘后日 K 更新，实际是否执行由任务内部交易日判断决定。
 */
@Component
public class MarketDailyUpdateScheduler {

    private static final Logger log = LoggerFactory.getLogger(MarketDailyUpdateScheduler.class);

    private final BasicDataApi basicDataApi;

    /**
     * 创建股票每日更新定时任务。
     * 作用：注入华鑫基础数据 API 编排组件。
     */
    public MarketDailyUpdateScheduler(BasicDataApi basicDataApi) {
        this.basicDataApi = basicDataApi;
    }

    /**
     * 盘前每日更新任务。
     * 执行时间：每天早上 7 点。
     */
    @Scheduled(cron = "0 0 7 * * ?", zone = "Asia/Shanghai")
    public void runPreOpenUpdate() {
        LocalDate taskDate = LocalDate.now();
        log.info("定时触发盘前每日更新 taskDate={}", taskDate);
        basicDataApi.startPreOpenUpdate(taskDate);
    }

    /**
     * 盘后每日更新任务。
     * 执行时间：每天下午 17 点。
     */
    @Scheduled(cron = "0 0 17 * * ?", zone = "Asia/Shanghai")
    public void runPostCloseUpdate() {
        LocalDate taskDate = LocalDate.now();
        log.info("定时触发盘后每日更新 taskDate={}", taskDate);
        basicDataApi.startPostCloseUpdate(taskDate);
    }
}
