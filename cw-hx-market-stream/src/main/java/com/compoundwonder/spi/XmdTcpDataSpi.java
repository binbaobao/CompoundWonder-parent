package com.compoundwonder.spi;

import com.cbacb.compoundWonder.trader.constant.ConstantUtil;
import com.cbacb.compoundWonder.trader.hxctp.api.XmdTcpDataApi;
import com.cbacb.compoundWonder.trader.processor.DisruptorManager;
import com.cbacb.compoundWonder.trader.util.SymbolUtil;
import com.cbacb.compoundWonder.trader.util.ThreadSafeIdGenerator;
import com.tora.traderapi.traderapi;
import com.tora.xmdapi.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XmdTcpDataSpi extends CTORATstpXMdSpi {

    private CTORATstpXMdApi api;
    private XmdTcpDataApi  xmdDataApi;
    private DisruptorManager disruptorManager;

    public XmdTcpDataSpi(CTORATstpXMdApi api, DisruptorManager disruptorManager, XmdTcpDataApi xmdDataApi) {
        this.api = api;
        this.xmdDataApi = xmdDataApi;
        this.disruptorManager = disruptorManager;
    }

    public void OnFrontConnected() {
        log.info("tcp level1 链接成功");
        // 连接成功，尝试登陆
        CTORATstpReqUserLoginField ctoraTstpReqUserLoginField = new CTORATstpReqUserLoginField();
        ctoraTstpReqUserLoginField.setLogInAccount("319000021663");
        ctoraTstpReqUserLoginField.setLogInAccountType(traderapi.getTORA_TSTP_LACT_UserID());
        ctoraTstpReqUserLoginField.setPassword("920606");
        ctoraTstpReqUserLoginField.setUserProductInfo("HX5VNYKBY4");
        ctoraTstpReqUserLoginField.setDynamicPassword("eIODpsBc");
        ctoraTstpReqUserLoginField.setTerminalInfo("PC;IIP=10.225.29.68;IPORT=NA;LIP=NA;MAC=5C6F697464F1;HD=0003e9930c792a872e0043639df098cd;@HX5VNYKBY4");

        int ret = api.ReqUserLogin(ctoraTstpReqUserLoginField, ThreadSafeIdGenerator.generateId());
        if (ret != 0) {
            log.info("登录失败 ret {}", ret);
        }
    }

    public void OnFrontDisconnected(int nReason) {
        log.info("tcp level1 通信连接断开{}", nReason);
    }

    public void OnRspUserLogin(CTORATstpRspUserLoginField pRspUserLoginField, CTORATstpRspInfoField pRspInfo, int nRequestID) {
        if (pRspInfo.getErrorID() == 0) {
            log.info("tcp level1 登录成功");
            xmdDataApi.subscribeMarketData();
        } else {
            log.info("tcp level1 登录失败, error_id {}, error_msg {}", pRspInfo.getErrorID(), pRspInfo.getErrorMsg());
        }
    }

    public void OnRtnMarketData(CTORATstpMarketDataField pMarketDataField) {
        if (pMarketDataField != null) {
            int compact = parseTimeToMillis(pMarketDataField.getUpdateTime()) + pMarketDataField.getUpdateMillisec();
            int symbolToInt = SymbolUtil.fastSymbolToInt(pMarketDataField.getSecurityID());
            // 判断是否是集合竞价期间 || ConstantUtil.TIME_1457 <= compact
            if (ConstantUtil.TIME_930 >= compact || (ConstantUtil.TIME_1457 <= compact && ConstantUtil.TIME_1500 > compact)) {
                disruptorManager.publishSnapshotData(symbolToInt,
                        compact,
                        (int) (pMarketDataField.getAskPrice1() * 100 + 0.5),
                        0L,
                        pMarketDataField.getAskVolume1() + pMarketDataField.getAskVolume2(),
                        pMarketDataField.getBidVolume1() + pMarketDataField.getBidVolume2());
            } else {
                disruptorManager.publishSnapshotData(symbolToInt,
                        compact,
                        (int) (pMarketDataField.getBidPrice1() * 100 + 0.5),
                        (long) pMarketDataField.getTurnover(),
                        pMarketDataField.getVolume(),
                        pMarketDataField.getBidVolume1());
            }
        }
    }

    /**
     * 09:15:58
     *
     * @param timeStr
     * @return
     */
    public int parseTimeToMillis(String timeStr) {
        if (timeStr == null || timeStr.length() < 8) {
            return 0;
        }

        // 提取每一位数字
        int h1 = timeStr.charAt(0) - '0';
        int h2 = timeStr.charAt(1) - '0';
        int m1 = timeStr.charAt(3) - '0';
        int m2 = timeStr.charAt(4) - '0';
        int s1 = timeStr.charAt(6) - '0';
        int s2 = timeStr.charAt(7) - '0';

        // 直接拼接数字逻辑：
        // (h1*10 + h2) 是小时部分，占万位和十万位
        // (m1*10 + m2) 是分钟部分，占百位和千位
        // (s1*10 + s2) 是秒部分，个位和十位
        int numericTime = (h1 * 100000) +
                (h2 * 10000) +
                (m1 * 1000) +
                (m2 * 100) +
                (s1 * 10) +
                (s2);
        // 最后统一乘以 1000 得到你想要的后缀
        return numericTime * 1000;
    }
}
