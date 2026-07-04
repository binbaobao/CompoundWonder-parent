package com.compoundwonder.hxdata.controller;



import com.compoundwonder.hxdata.api.BasicDataApi;
import com.compoundwonder.hxdata.service.StockSyncTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @author chaobin
 * @since 1.0.0 2024-08-18
 */
@RestController
@RequestMapping("test")
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private BasicDataApi basicDataApi;

    @Autowired
    private StockSyncTaskService stockSyncTaskService;


    /**
     * 测试打开基础数据查询。
     * 作用：保留临时调试入口，用于手动触发当前关注的华鑫接口。
     */
    @GetMapping("open")
    public String openMarketTask()  {
        log.info("test openMarketTask");

//        basicDataApi.queryStockDayQuotation(1);
//        basicDataApi.queryShareCalendar(1);
//        basicDataApi.queryShareCalendar(2);

//        basicDataApi.queryShareDescription(1);
//        basicDataApi.queryShareIssuance(1);
        basicDataApi.queryFreeFloatShares(1);
        return "open ok";
    }

    /**
     * 同步指定年份 A 股交易日历。
     * 作用：手动传入年份，每次最多取 500 条交易日，并通过异步回调落库。
     */
    @GetMapping("sync-calendar")
    public String syncCalendar(@RequestParam Integer year) {
        if (!basicDataApi.awaitLoginReady(10000)) {
            return "增值服务尚未登录成功，请稍后重试";
        }

        basicDataApi.syncShareCalendarYears(year, year);
        return "sync calendar started year=" + year;
    }

    /**
     * 初始化股票历史数据同步任务。
     * 作用：从曾用名历史表去重股票代码，为每只股票创建一条同步进度任务。
     */
    @GetMapping("init-sync-task")
    public String initSyncTask() {
        int count = stockSyncTaskService.initTasksFromPreviousNameHistory();
        return "init sync task count=" + count;
    }

    /**
     * 启动自由流通股本全量同步任务。
     * 作用：从任务表按股票代码升序逐只同步自由流通股本历史区间。
     */
    @GetMapping("free-float-start")
    public String freeFloatStart() {
        if (!basicDataApi.awaitLoginReady(10000)) {
            return "增值服务尚未登录成功，请稍后重试";
        }

        basicDataApi.startFreeFloatShareSyncTasks();
        return "free float sync started";
    }

    /**
     * 启动股票日 K 全量同步任务。
     * 作用：从任务表按股票代码升序逐只同步股票日 K 数据。
     */
    @GetMapping("daily-kline-start")
    public String dailyKlineStart() {
        if (!basicDataApi.awaitLoginReady(10000)) {
            return "增值服务尚未登录成功，请稍后重试";
        }

        basicDataApi.startStockDailySyncTasks();
        return "daily kline sync started";
    }

    /**
     * 测试单只股票日 K 入库。
     * 作用：默认查询 603928，从 2022-01-01 至今落库，用于观察辅助字段计算是否正确。
     */
    @GetMapping("daily-kline-test")
    public String dailyKlineTest(@RequestParam(defaultValue = "603928") String stockCode) {
        if (!basicDataApi.awaitLoginReady(10000)) {
            return "增值服务尚未登录成功，请稍后重试";
        }

        basicDataApi.queryStockDailyAndSave(stockCode, 1);
        return "daily kline test started stockCode=" + stockCode;
    }
}
