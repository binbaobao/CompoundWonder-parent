package com.compoundwonder.spi;

import com.compoundwonder.constant.ToraConstants;
import com.compoundwonder.service.DisruptorService;
import com.compoundwonder.service.TradeCacheService;
import com.compoundwonder.service.impl.RealTradeServiceImpl;
import com.compoundwonder.util.SymbolUtil;
import com.compoundwonder.util.ThreadSafeIdGenerator;
import com.tora.traderapi.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TraderSpi extends CTORATstpTraderSpi {

    private CTORATstpTraderApi api;

    private DisruptorService disruptorManager;

    private TradeCacheService tradeCacheService;

    private RealTradeServiceImpl realTradeService;

    /**
     * 请求 Req*** ReqUserLogin
     * 响应 OnRsp*** OnRspUserLogin
     * 查询请求 ReqQry*** ReqQrySecurity
     * 查询响应 OnRspQry*** OnRspQrySecurity
     * 回报 OnRtn*** OnRtnOrder
     * 错误回报 OnErrRtn*** OnErrRtnOrderInsert
     */
    public TraderSpi(CTORATstpTraderApi api, TradeCacheService tradeCacheService, DisruptorService disruptorManager, RealTradeServiceImpl realTradeService) {
        this.api = api;
        this.realTradeService = realTradeService;
        this.disruptorManager = disruptorManager;
        this.tradeCacheService = tradeCacheService;
    }

    /**
     * 连接成功，尝试登陆
     */
    public void OnFrontConnected() {
        log.info("连接成功，尝试登陆");
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
            log.info("登录失败：{}", ret);
        }
    }

    public void OnFrontDisconnected(int nReason) {
        log.info("连接断开回调:{}", nReason);
    }

    /**
     * 错误应答
     *
     * @param pRspInfoField
     * @param nRequestID
     * @param bIsLast
     */
    public void OnRspError(CTORATstpRspInfoField pRspInfoField, int nRequestID, boolean bIsLast) {
        log.info("错误应答:{},错误信息:{}", nRequestID, pRspInfoField.getErrorMsg());
    }

    /**
     * 获取连接信息应答
     *
     * @param pConnectionInfoField
     * @param pRspInfoField
     * @param nRequestID
     */
    public void OnRspGetConnectionInfo(CTORATstpConnectionInfoField pConnectionInfoField, CTORATstpRspInfoField pRspInfoField, int nRequestID) {
        log.info("获取连接信息应答:{} 应答：{} nRequestID:{}nRequestID", pConnectionInfoField.getUserRequestID(), pRspInfoField.getErrorMsg(), nRequestID);
    }


    public void OnRspUserLogin(CTORATstpRspUserLoginField pRspUserLoginField, CTORATstpRspInfoField pRspInfo, int nRequestID) {
        if (pRspInfo.getErrorID() == 0) {
            log.info("交易服务登录成功，开始初始化调用查询服务");
            // 记录登录信息，缓存登录信息
            tradeCacheService.setUserLoginInfoDto(pRspUserLoginField);
            // 查询股东账户
            api.ReqQryShareholderAccount(new CTORATstpQryShareholderAccountField(), ThreadSafeIdGenerator.generateId());
            // 查询资金账户，获取账户资金
            api.ReqQryTradingAccount(new CTORATstpQryTradingAccountField(), ThreadSafeIdGenerator.generateId());
            // 查询报单记录，隔夜单需要撤单
            api.ReqQryOrder(new CTORATstpQryOrderField(), ThreadSafeIdGenerator.generateId());
            // 查询持仓记录，后续要进行买卖
            api.ReqQryPosition(new CTORATstpQryPositionField(), ThreadSafeIdGenerator.generateId());
        } else {
            log.info("登录失败, error_id {}, error_msg {}", pRspInfo.getErrorID(), pRspInfo.getErrorMsg());
        }
    }

    /**
     * 查询股东账号回调
     *
     * @param pShareholderAccount
     * @param pRspInfo
     * @param nRequestID
     * @param bIsLast
     */
    public void OnRspQryShareholderAccount(CTORATstpShareholderAccountField pShareholderAccount, CTORATstpRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        if (pShareholderAccount != null) {
            log.info("查询股东账号回调 nRequestID : {}", nRequestID);
            tradeCacheService.putStockAccountInfo(pShareholderAccount.getMarketID(), pShareholderAccount.getShareholderID());
        }
    }


    /**
     * 账户余额
     *
     * @param pTradingAccount
     * @param pRspInfo
     * @param nRequestID
     * @param bIsLast
     */
    public void OnRspQryTradingAccount(CTORATstpTradingAccountField pTradingAccount, CTORATstpRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        if (pTradingAccount != null) {
            tradeCacheService.updateAccountInfo(pTradingAccount);
        }
    }

    /**
     * 返回持仓记录
     *
     * @param pPosition
     * @param pRspInfo
     * @param nRequestID
     * @param bIsLast
     */
    public void OnRspQryPosition(CTORATstpPositionField pPosition, CTORATstpRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        if (pPosition != null) {
            tradeCacheService.addPositionRecord(pPosition);
        }
    }

    /**
     * 报单后主动回报
     * <p>
     * 报单状态
     * 预埋 '0'
     * 未知 '1'
     * 交易所已接收 '2'
     * 部分成交 '3'
     * 全部成交 '4'
     * 部成部撤 '5'
     * 全部撤单 '6'
     * 交易所已拒绝 '7'
     *
     * @param pOrder
     */
    public void OnRtnOrder(CTORATstpOrderField pOrder) {

        if (pOrder != null) {
            tradeCacheService.updateOrAddOrder(pOrder);
            //买入 '0' 卖出 '1'
            //全部成交 '4' 发送修改股票状态消息
            if (pOrder.getOrderStatus() == '4' && pOrder.getDirection() == '0') {
                disruptorManager.publishTransInfoData(pOrder.getSecurityID(), 2);
            }
            // 交易所接到的时候time 和 level2 的时间是一致的
            if (pOrder.getOrderStatus() == '2' && pOrder.getDirection() == '0') {
                int symbolInt = SymbolUtil.fastSymbolToInt(pOrder.getSecurityID());
                int priceInt = (int) (pOrder.getLimitPrice() * 100 + 0.5);
                int volInt = pOrder.getVolumeTotalOriginal();
                int time = 0;
                if (SymbolUtil.getHandlerIndex(symbolInt) == 0) {
                    time = (int) (pOrder.getAcceptTimeStamp() / 100000 * 10);
                } else {
                    time = (int) (pOrder.getAcceptTimeStamp() - Integer.parseInt(pOrder.getTradingDay()) * 1_000_000_000L);
                }
                byte side = (pOrder.getDirection() == '1') ? ToraConstants.SIDE_BUY : ToraConstants.SIDE_SELL;
                byte type = (byte) (pOrder.getOrderType() - '0');
                disruptorManager.pushOrderInfo(symbolInt, time, priceInt, volInt, side, type);
            }
            if (pOrder.getOrderStatus() == '4' && pOrder.getDirection() == '1') {
                disruptorManager.publishTransInfoData(pOrder.getSecurityID(), 3);
            }
            //卖出 '1' 部成部撤 '5' 发送连续卖逻辑
            if (pOrder.getOrderStatus() == '5' && pOrder.getDirection() == '1') {
                tradeCacheService.updateHistoryVolume(pOrder.getSecurityID(), pOrder.getRtnIntInfo());
                disruptorManager.publishTransInfoData(pOrder.getSecurityID(), 6);
            }
            //全部撤单 '6' 发送撤单消息
            if (pOrder.getOrderStatus() == '6' && pOrder.getDirection() == '0') {
                disruptorManager.publishTransInfoData(pOrder.getSecurityID(), 4);
            }
        }
    }


    /**
     * 登录初始化查询的回报
     *
     * @param pOrder
     * @param pRspInfo
     * @param nRequestID
     * @param bIsLast
     */
    public void OnRspQryOrder(CTORATstpOrderField pOrder, CTORATstpRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        if (pOrder != null) {
            tradeCacheService.updateOrAddOrder(pOrder);
        }
        if (bIsLast) {
            log.info("查询报单结束!");
        }
    }

    /**
     * 查询证券信息响应
     *
     * @param pSecurity
     * @param pRspInfo
     * @param nRequestID
     * @param bIsLast
     */
    public void OnRspQrySecurity(CTORATstpSecurityField pSecurity, CTORATstpRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        if (pSecurity != null) {
            realTradeService.updateOrderBookInfo(pSecurity);
        }
    }
}
