package com.compoundwonder.spi;

import com.compoundwonder.constant.ToraConstants;
import com.compoundwonder.service.DisruptorService;
import com.compoundwonder.api.Level2DataApi;
import com.compoundwonder.util.SymbolUtil;
import com.compoundwonder.util.ThreadSafeIdGenerator;
import com.tora.lev2mdapi.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Leve2DataSpi extends CTORATstpLev2MdSpi {

    private CTORATstpLev2MdApi api;

    private Level2DataApi lev2DataApi;

    private DisruptorService disruptorManager;

    public Leve2DataSpi(CTORATstpLev2MdApi api, DisruptorService disruptorManager, Level2DataApi lev2DataApi) {
        this.api = api;
        this.lev2DataApi = lev2DataApi;
        this.disruptorManager = disruptorManager;
    }

    public void OnFrontConnected() {
        CTORATstpReqUserLoginField ctoraTstpReqUserLoginField = new CTORATstpReqUserLoginField();

        ctoraTstpReqUserLoginField.setLogInAccount("15810892100");
        ctoraTstpReqUserLoginField.setLogInAccountType(lev2mdapi.getTORA_TSTP_LACT_UserID());
        ctoraTstpReqUserLoginField.setPassword("guo0606BIN");
        ctoraTstpReqUserLoginField.setUserProductInfo("HX5VNYKBY4");
        ctoraTstpReqUserLoginField.setDynamicPassword("eIODpsBc");
        ctoraTstpReqUserLoginField.setTerminalInfo("PC;IIP=192.168.141.68;IPORT=NA;LIP=NA;MAC=5C6F697464F1;HD=0003e9930c792a872e0043639df098cd;@HX5VNYKBY4");

        int ret = api.ReqUserLogin(ctoraTstpReqUserLoginField, ThreadSafeIdGenerator.generateId());
        if (ret == 0) {
            log.info("level2 行情登录调用成功");
        } else {
            log.info("level2 行情服务登录调用失败 ，失败代码{}", ret);
        }
    }

    /**
     * 当客户端与交易后台通信连接断开时，该方法被调用。当发生这个情况后，API会自动重新连接，客户端可不做处理
     * 	-3 连接已断开
     * 	-4 网络读失败
     * 	-5 网络写失败
     * 	-6 订阅流错误
     * 	-7 流序号错误
     * 	-8 错误的心跳报文
     * 	-9 错误的报文
     * 	-15 网络读失败
     * 	-16 网络写失败
     * @param nReason
     */
    public void OnFrontDisconnected(int nReason) {
        log.info("tcp level2 通信连接断:{}", nReason);
    }

    public void OnRspUserLogin(CTORATstpRspUserLoginField pRspUserLogin, CTORATstpRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {

        if (pRspInfo.getErrorID() == 0) {
            log.info("level2 行情登录成功");
            lev2DataApi.subscribeMarketData();
        } else {
            log.info("level2 行情服务登录失败 ，错误代码 {},错误信息 {}", pRspInfo.getErrorID(), pRspInfo.getErrorMsg());
        }
    }

    /**
     * 深圳逐笔成交
     *
     * @param pTransaction
     */
    public void OnRtnTransaction(CTORATstpLev2TransactionField pTransaction) {
        if (pTransaction != null) {
            int symbolInt = SymbolUtil.fastSymbolToInt(pTransaction.getSecurityID());
            long buyNo = pTransaction.getBuyNo();
            long sellNo = pTransaction.getSellNo();
            double tradePrice = pTransaction.getTradePrice();
            long tradeVolume = pTransaction.getTradeVolume();
            char execType = pTransaction.getExecType();

            // 2. 预计算业务逻辑，减轻后续 Handler 负担
            // 注意：尽量避免在 Spi 线程做复杂逻辑，但简单的赋值转换是可以的
            int priceInt = (int) (tradePrice * 100 + 0.5);
            int volInt = (int) tradeVolume;
            int amount = priceInt * volInt / 100;

            // 确定主委托单号和买卖方向
            long mainSeq;
            byte side;

            if (buyNo == 0) {
                mainSeq = sellNo;
                side = ToraConstants.SIDE_SELL; // 卖方主动
            } else {
                mainSeq = buyNo;
                side = ToraConstants.SIDE_BUY;  // 买方主动
            }
            byte type = (execType == '1') ? ToraConstants.TYPE_TRADE : ToraConstants.TYPE_CANCEL;

            //证券代码, 成交时间, 成交编号, 成交单价, 成交数量, 成交金额, 交易方向, 交易类型, 买方委托编号, 卖方委托编号
            disruptorManager.publishTickTradeData(symbolInt, pTransaction.getTradeTime(), (int) mainSeq, priceInt, volInt, amount, side, type, (int) buyNo, (int) sellNo);
        }
    }

    /**
     * 深圳逐笔委托
     *
     * @param orderDetail
     */
    public void OnRtnOrderDetail(CTORATstpLev2OrderDetailField orderDetail) {

        if (orderDetail != null) {
            int symbolInt = SymbolUtil.fastSymbolToInt(orderDetail.getSecurityID());
            double price = orderDetail.getPrice();
            long volume = orderDetail.getVolume();
            // 2. 预计算业务逻辑，减轻后续 Handler 负担
            // 注意：尽量避免在 Spi 线程做复杂逻辑，但简单的赋值转换是可以的
            int priceInt = (int) (price * 100 + 0.5);
            int volInt = (int) volume;
            char rawSide = orderDetail.getSide();
            byte side = (rawSide == '1') ? ToraConstants.SIDE_BUY : ToraConstants.SIDE_SELL;
            byte type = (byte) (orderDetail.getOrderType() - '0');
            //证券代码, 委托时间, 委托编号, 委托单价, 委托数量, 交易方向, 交易类型
            disruptorManager.publishTickOrderData(symbolInt, orderDetail.getOrderTime(), orderDetail.getSubSeq(), priceInt, volInt, side, type);
        }
    }

    /**
     * 上海逐笔数据 二合一
     *
     * @param pTick
     */
    public void OnRtnNGTSTick(CTORATstpLev2NGTSTickField pTick) {

        if (pTick == null) return;

        char tickType = pTick.getTickType();
        if (tickType == 'S') return; // 跳过状态包

        // 1. 快速转换 ID，彻底抛弃 String
        int symbolInt = SymbolUtil.fastSymbolToInt(pTick.getSecurityID());

        // 2. 缓存必要原始字段，减少 JNI 调用
        long buyNo = pTick.getBuyNo();
        long sellNo = pTick.getSellNo();
        char sideChar = pTick.getSide();

        // 3. 预计算核心数值
        int time = pTick.getTickTime() * 10;
        int price = (int) (pTick.getPrice() * 100 + 0.5);
        int volume = (int) pTick.getVolume();

        // 4. 利用局部变量暂存状态，最后一次性推送到 Disruptor
        byte dataType;
        int orderId;
        int amount = 0;
        byte direction;
        byte bizType; // 对应你的 setType

        // 优化分支逻辑：上海逐笔 A-新增, D-撤单, T-成交
        if (tickType == 'T') {
            dataType = 2; // 成交
            orderId = (int) pTick.getSubSeq();
            amount = (int) pTick.getTradeMoney();
            direction = 0;
            bizType = 2;
        } else {
            // A 或 D 的逻辑合并处理
            dataType = 1; // 委托/撤单
            direction = (byte) (sideChar - '0'); // '1'->1, '2'->2
            orderId = (int) (sideChar == '1' ? buyNo : sellNo);
            bizType = (tickType == 'D') ? (byte) 10 : (byte) 2;
        }

        // 5. 关键：直接发布到 Disruptor，不创建任何新对象
        // 内部应使用 ringBuffer.next() -> get(seq) -> 填充字段 -> publish(seq)
        disruptorManager.publishNGTSTickData(symbolInt, time, dataType, orderId, amount, direction, price, volume, bizType, (int) buyNo, (int) sellNo);
    }
}
