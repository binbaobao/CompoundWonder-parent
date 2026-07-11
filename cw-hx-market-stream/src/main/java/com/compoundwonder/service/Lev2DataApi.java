package com.compoundwonder.service;

import com.compoundwonder.spi.Leve2DataSpi;
import com.tora.lev2mdapi.CTORATstpLev2MdApi;
import com.tora.lev2mdapi.lev2mdapi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Set;

@Slf4j
@Component
public class Lev2DataApi {

    static {
        String property = System.getProperties().getProperty("os.name");
        if (property.contains("Linux")) {
            System.load("/home/usergcb/os/libjavalev2mdapi.so");
        }
    }

    private final int targetHour = 15;
    private final int targetMinute = 2;

    @Autowired
    private DisruptorService disruptorManager;

    @Autowired
    private OrderBookService orderBookService;


    //组播收行情 非缓存模式
    private CTORATstpLev2MdApi lev2MdApi;

    private Leve2DataSpi leve2DataSpi = null;

    @Async("orderDataExecutor")
    public void levelApiInit() throws InterruptedException {

        lev2MdApi = CTORATstpLev2MdApi.CreateTstpLev2MdApi(lev2mdapi.getTORA_TSTP_MST_TCP(), false);
        leve2DataSpi = new Leve2DataSpi(lev2MdApi, disruptorManager,this);
        lev2MdApi.RegisterSpi(leve2DataSpi);
        lev2MdApi.RegisterFront("tcp://192.168.140.7:6900");

        // 初始化 盯盘数据
        lev2MdApi.Init();

        while (true) {
            LocalTime now = LocalTime.now();
            int currentHour = now.getHour();
            int currentMinute = now.getMinute();

            // 检查是否到达目标结束时间
            if (currentHour == targetHour && currentMinute >= targetMinute) {
                break;
            }
            // 每分钟检查一次
            Thread.sleep(1000);
        }
        // 销毁
        lev2MdApi.Release();
        leve2DataSpi = null;
        log.info("调度器已停止，level2服务接口已销毁，结束时间: {}", LocalTime.now());
    }

    /**
     * lv2 订阅盯盘股票
     */
    public void subscribeMarketData() {
        Set<String> list = orderBookService.getOrderBookCodes();
        log.info("level 2 行情数据订阅股票代码:{}", list);
        if (list == null || list.isEmpty()) {
            return;
        }
        String[] shang = list.stream().filter(code -> code.startsWith("60") || code.startsWith("68")).toArray(String[]::new);
        String[] shen = list.stream().filter(code -> code.startsWith("00") || code.startsWith("30")).toArray(String[]::new);

        // 上海逐笔数据
        if (shang.length > 0) {
            int i = lev2MdApi.SubscribeNGTSTick(shang, lev2mdapi.getTORA_TSTP_EXD_SSE());
            log.info("上交所 l2 订阅响应{}", i);
        }
        // 深交所 level2 数据
        if (shen.length > 0) {
            //逐笔委托
            int i = lev2MdApi.SubscribeOrderDetail(shen, lev2mdapi.getTORA_TSTP_EXD_SZSE());
            //逐笔成交
            int i1 = lev2MdApi.SubscribeTransaction(shen, lev2mdapi.getTORA_TSTP_EXD_SZSE());
            log.info("深交所 l2 逐笔委托响应:{},逐笔成交响应:{}", i, i1);
        }
    }
}
