package com.compoundwonder.api;

import com.compoundwonder.service.DisruptorService;
import com.compoundwonder.service.OrderBookService;
import com.compoundwonder.spi.XmdTcpDataSpi;
import com.tora.xmdapi.CTORATstpXMdApi;
import com.tora.xmdapi.xmdapi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Set;

@Slf4j
@Component
public class XmdTcpDataApi {

    static {
        String property = System.getProperties().getProperty("os.name");
        if (property.contains("Linux")) {
            System.load("/home/usergcb/os/libjavaxmdapi.so");
        }
    }

    private final int targetHour = 15;
    private final int targetMinute = 2;

    private volatile boolean status = false;

    @Autowired
    private DisruptorService disruptorManager;

    @Autowired
    private OrderBookService orderBookService;

    private CTORATstpXMdApi xMdApi;

    private XmdTcpDataSpi xmdDataSpi;


    @Async("orderDataExecutor")
    public void xmdApiInit() throws InterruptedException {

        xMdApi = CTORATstpXMdApi.CreateTstpXMdApi(xmdapi.getTORA_TSTP_MST_TCP());
        xmdDataSpi = new XmdTcpDataSpi(xMdApi, disruptorManager, this);

        xMdApi.RegisterSpi(xmdDataSpi);
        xMdApi.RegisterFront("tcp://172.16.36.5:7780");// 生产
        // 初始化 盯盘数据
        xMdApi.Init();

        while (true) {
            LocalTime now = LocalTime.now();
            int currentHour = now.getHour();
            int currentMinute = now.getMinute();

            // 检查是否到达目标结束时间
            if (currentHour == targetHour && currentMinute >= targetMinute || status) {
                break;
            }
            // 每分钟检查一次
            Thread.sleep(1000);
        }
        // 销毁
        xMdApi.Release();
        xmdDataSpi = null;
        log.info("调度器已停止，tcp level1 服务接口已销毁，结束时间: {}", LocalTime.now());
    }


    /**
     * 登录成功后订阅行情信息
     *
     * @throws InterruptedException
     */
    public void subscribeMarketData() {
        Set<String> list = orderBookService.getOrderBookCodes();
        log.info("tcp level 1普通行情数据订阅股票代码:{}", list);
        if (list == null || list.isEmpty()) {
            return;
        }
        String[] shang = list.stream().filter(code -> code.startsWith("60") || code.startsWith("68")).toArray(String[]::new);
        String[] shen = list.stream().filter(code -> code.startsWith("00") || code.startsWith("30")).toArray(String[]::new);

        // 上交所 快照
        if (shang.length > 0) {
            xMdApi.SubscribeMarketData(shang, xmdapi.getTORA_TSTP_EXD_SSE());
        }
        // 深交所 快照
        if (shen.length > 0) {
            xMdApi.SubscribeMarketData(shen, xmdapi.getTORA_TSTP_EXD_SZSE());
        }
    }
}
