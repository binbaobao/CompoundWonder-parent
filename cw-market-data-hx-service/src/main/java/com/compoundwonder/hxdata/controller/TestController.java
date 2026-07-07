package com.compoundwonder.hxdata.controller;



import com.compoundwonder.hxdata.api.BasicDataApi;
import com.compoundwonder.hxdata.service.StockCurrentStatusService;
import com.compoundwonder.hxdata.service.StockSyncTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


/**
 * @author chaobin
 * @since 1.0.0 2024-08-18
 */
@RestController
@RequestMapping("test")
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);
    private static final DateTimeFormatter API_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private BasicDataApi basicDataApi;

    @Autowired
    private StockSyncTaskService stockSyncTaskService;

    @Autowired
    private StockCurrentStatusService stockCurrentStatusService;


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
     * 初始化股票当前状态。
     * 作用：从曾用名历史表去重股票代码，为每只股票创建一条当前状态记录。
     */
    @GetMapping("init-current-status")
    public String initCurrentStatus() {
        int count = stockCurrentStatusService.initFromPreviousNameHistory();
        return "init current status count=" + count;
    }

    /**
     * 调试 000301 当前状态来源接口。
     * 作用：只打印 000301 收到的转债发行和地域属性字段，先观察接口数据口径。
     */
    @GetMapping("debug-current-status-000301")
    public String debugCurrentStatus000301() {
        if (!basicDataApi.awaitLoginReady(10000)) {
            return "增值服务尚未登录成功，请稍后重试";
        }

        basicDataApi.debugBondIssuanceFor000301(1);
//        basicDataApi.debugRegionInfoFor000301(1);
        return "debug current status 000301 started";
    }

    /**
     * 调试 111025 转债发行信息。
     * 作用：按转债代码查询发行信息和基本资料，观察发行失败时是否缺少上市日期。
     */
    @GetMapping("debug-bond-issuance-111025")
    public String debugBondIssuance111025() {
        if (!basicDataApi.awaitLoginReady(10000)) {
            return "增值服务尚未登录成功，请稍后重试";
        }

        basicDataApi.debugBondIssuanceByBondCode("111025", 1);
        return "debug bond issuance 111025 started";
    }

    /**
     * 同步当前可转债标识。
     * 作用：全量查询可转债发行信息，过滤有效转债后刷新当前状态表。
     */
    @GetMapping("sync-convertible-bond")
    public String syncConvertibleBond() {
        if (!basicDataApi.awaitLoginReady(10000)) {
            return "增值服务尚未登录成功，请稍后重试";
        }

        basicDataApi.syncConvertibleBondStatus(1);
        return "sync convertible bond started";
    }

    /**
     * 同步可转债历史发行关系。
     * 作用：全量查询可转债发行信息，先把正股、转债、市场对应关系落库。
     */
    @GetMapping("sync-convertible-bond-history")
    public String syncConvertibleBondHistory() {
        if (!basicDataApi.awaitLoginReady(10000)) {
            return "增值服务尚未登录成功，请稍后重试";
        }

        basicDataApi.syncConvertibleBondStatus(1);
        return "sync convertible bond history started";
    }

    /**
     * 补全可转债基本资料历史。
     * 作用：从已落库的转债发行关系中逐个查询基本资料，补充上市日期、退市日期、到期日期和余额。
     */
    @GetMapping("fill-convertible-bond-description")
    public String fillConvertibleBondDescription() {
        if (!basicDataApi.awaitLoginReady(10000)) {
            return "增值服务尚未登录成功，请稍后重试";
        }

        basicDataApi.startConvertibleBondDescriptionFillTasks();
        return "fill convertible bond description started";
    }

    /**
     * 同步指定股票地域属性。
     * 作用：按股票代码查询地域名称并更新当前状态表。
     */
    @GetMapping("sync-region")
    public String syncRegion(@RequestParam(defaultValue = "000301") String stockCode) {
        if (!basicDataApi.awaitLoginReady(10000)) {
            return "增值服务尚未登录成功，请稍后重试";
        }

        basicDataApi.syncRegionInfo(stockCode, 1);
        return "sync region started stockCode=" + stockCode;
    }

    /**
     * 同步全部股票地域属性。
     * 作用：从当前状态表读取全部股票代码，逐只查询地域名称并更新 region_name。
     */
    @GetMapping("sync-all-region")
    public String syncAllRegion() {
        if (!basicDataApi.awaitLoginReady(10000)) {
            return "增值服务尚未登录成功，请稍后重试";
        }

        basicDataApi.startAllRegionInfoSyncTasks();
        return "sync all region started";
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

    /**
     * 盘前发现新上市股票。
     * 作用：按上市日期查询发行信息，只新增同步任务，不维护曾用名历史。
     */
    @GetMapping("pre-open-new-listing")
    public String preOpenNewListing(@RequestParam(required = false) String date) {
        if (!basicDataApi.awaitLoginReady(10000)) {
            return "增值服务尚未登录成功，请稍后重试";
        }

        LocalDate listDate = parseDateOrToday(date);
        basicDataApi.discoverNewListedStocks(listDate, 1);
        return "pre open new listing started date=" + listDate.format(API_DATE_FORMATTER);
    }

    /**
     * 盘前同步自由流通股本变化。
     * 作用：按日期查询全市场自由流通股本变化点，并增量维护区间表。
     */
    @GetMapping("pre-open-free-float")
    public String preOpenFreeFloat(@RequestParam(required = false) String date) {
        if (!basicDataApi.awaitLoginReady(10000)) {
            return "增值服务尚未登录成功，请稍后重试";
        }

        LocalDate queryDate = parseDateOrToday(date);
        basicDataApi.syncDailyFreeFloatShares(queryDate, 1);
        return "pre open free float started date=" + queryDate.format(API_DATE_FORMATTER);
    }

    /**
     * 盘前同步曾用名变化。
     * 作用：按名称开始日期查询当天生效的曾用名记录，并增量维护曾用名历史区间。
     */
    @GetMapping("pre-open-previous-name")
    public String preOpenPreviousName(@RequestParam(required = false) String date) {
        if (!basicDataApi.awaitLoginReady(10000)) {
            return "增值服务尚未登录成功，请稍后重试";
        }

        LocalDate queryDate = parseDateOrToday(date);
        basicDataApi.syncDailyPreviousNameChanges(queryDate, 1);
        return "pre open previous name started date=" + queryDate.format(API_DATE_FORMATTER);
    }

    /**
     * 盘前基础数据更新。
     * 作用：按交易日判断后，依次执行曾用名、新股、自由流通股、可转债、地域和融资融券标识维护。
     */
    @GetMapping("pre-open-update")
    public String preOpenUpdate(@RequestParam(required = false) String date) {
        if (!basicDataApi.awaitLoginReady(10000)) {
            return "增值服务尚未登录成功，请稍后重试";
        }

        LocalDate queryDate = parseDateOrToday(date);
        basicDataApi.startPreOpenUpdate(queryDate);
        return "pre open update started date=" + queryDate;
    }

    /**
     * 盘后行情数据更新。
     * 作用：按交易日判断后，启动日 K 同步任务并记录每日任务状态。
     */
    @GetMapping("post-close-update")
    public String postCloseUpdate(@RequestParam(required = false) String date) {
        if (!basicDataApi.awaitLoginReady(10000)) {
            return "增值服务尚未登录成功，请稍后重试";
        }

        LocalDate queryDate = parseDateOrToday(date);
        basicDataApi.startPostCloseUpdate(queryDate);
        return "post close update started date=" + queryDate;
    }

    /**
     * 解析接口日期。
     * 作用：未传日期时默认使用当天，传入时支持 yyyy-MM-dd 或 yyyyMMdd。
     */
    private LocalDate parseDateOrToday(String date) {
        if (date == null || date.isBlank()) {
            return LocalDate.now();
        }
        if (date.contains("-")) {
            return LocalDate.parse(date);
        }
        return LocalDate.parse(date, API_DATE_FORMATTER);
    }
}
