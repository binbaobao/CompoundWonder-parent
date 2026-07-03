package com.compoundwonder.hxdata.controller;



import com.compoundwonder.hxdata.api.BasicDataApi;
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


    /**
     * 测试打开基础数据查询。
     * 作用：保留临时调试入口，用于手动触发当前关注的华鑫接口。
     */
    @GetMapping("open")
    public String openMarketTask()  {
        log.info("test openMarketTask");

//        basicDataApi.queryStockDayQuotation(1);
        basicDataApi.queryShareCalendar(1);
        basicDataApi.queryShareCalendar(2);

//        basicDataApi.queryShareDescription(1);
//        basicDataApi.queryShareIssuance(1);
//        basicDataApi.queryFreeFloatShares(1);
        return "open ok";
    }

    /**
     * 同步 2022 年至 2026 年 A 股交易日历。
     * 作用：每年发起一次查询，每次最多取 500 条交易日，并通过异步回调落库。
     */
    @GetMapping("sync-calendar")
    public String syncCalendar() {
        if (!basicDataApi.awaitLoginReady(10000)) {
            return "增值服务尚未登录成功，请稍后重试";
        }

        basicDataApi.syncShareCalendarYears(2022, 2026);
        return "sync calendar started";
    }
}
