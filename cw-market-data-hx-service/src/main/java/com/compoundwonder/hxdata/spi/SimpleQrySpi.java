package com.compoundwonder.hxdata.spi;

import com.compoundwonder.hxdata.callback.FreeFloatSharesResponseHandler;
import com.compoundwonder.hxdata.callback.ShareCalendarResponseHandler;
import com.compoundwonder.hxdata.callback.StockDayQuotationResponseHandler;
import com.qcvalueaddproapi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleQrySpi extends CQCValueAddProSpi {

    private static final Logger log = LoggerFactory.getLogger(SimpleQrySpi.class);

    private int requestId = 1;

    private CQCValueAddProApi api;
    private Runnable loginSuccessCallback;
    private ShareCalendarResponseHandler shareCalendarResponseHandler;
    private FreeFloatSharesResponseHandler freeFloatSharesResponseHandler;
    private StockDayQuotationResponseHandler stockDayQuotationResponseHandler;

    public static final String TRADING_HALT = "停牌";
    /**
     * 创建华鑫查询 SPI。
     * 作用：保存 API 对象、登录成功回调和各业务回调处理器，供异步回调时使用。
     */
    public SimpleQrySpi(CQCValueAddProApi api, Runnable loginSuccessCallback, ShareCalendarResponseHandler shareCalendarResponseHandler, FreeFloatSharesResponseHandler freeFloatSharesResponseHandler, StockDayQuotationResponseHandler stockDayQuotationResponseHandler) {
        this.api = api;
        this.loginSuccessCallback = loginSuccessCallback;
        this.shareCalendarResponseHandler = shareCalendarResponseHandler;
        this.freeFloatSharesResponseHandler = freeFloatSharesResponseHandler;
        this.stockDayQuotationResponseHandler = stockDayQuotationResponseHandler;
    }

    /**
     * 华鑫前置连接成功回调。
     * 回调接口：OnFrontConnected。
     * 处理逻辑：连接建立后立即发起登录请求 ReqUserLogin，登录成功后才能继续查询基础数据。
     */
    @Override
    public void OnFrontConnected() {
        log.info("增值服务链接成功，开始尝试登录");

        CQCVDReqUserLoginField req = new CQCVDReqUserLoginField();
        req.setLogInAccount("319000021663");
        req.setPassword("920606");
        req.setAuthMode(qcvalueaddproapiConstants.QCVD_AM_Password);
        int ret = api.ReqUserLogin(req, nextRequestId());
        if (ret == 0) {
            log.info("增值服务登录成功");
        } else {
            log.info("增值服务登录失败 ，失败代码{}", ret);
        }
    }


    /**
     * 股票日 K 行情查询应答。
     * 请求接口：ReqReqQryStockDayQuotation。
     * 回调接口：OnRspInquiryStockDayQuotation。
     * 应答数据域：CQCVDStockDayQuotationField。
     * 打印字段：交易所、证券代码、交易日、开高低收、成交量、成交额、涨跌幅、交易状态、涨跌停价、复权因子。
     */
    @Override
    public void OnRspInquiryStockDayQuotation(CQCVDStockDayQuotationField pStockDayQuotation, CQCVDRspInfoField pRspInfo, int nRequestID, boolean bIsPageLast, boolean bIsTotalLast) {
        if (pStockDayQuotation == null && !TRADING_HALT.equals(pStockDayQuotation.getTradeStatus())) {
            logPageEnd("股票日K行情", pRspInfo, nRequestID, bIsPageLast, bIsTotalLast);
            stockDayQuotationResponseHandler.onStockDayQuotationPageEnd(pRspInfo, nRequestID, bIsPageLast, bIsTotalLast);
            return;
        }
        stockDayQuotationResponseHandler.onStockDayQuotationData(pStockDayQuotation, nRequestID);
    }

    /**
     * 登录请求应答。
     * 请求接口：ReqUserLogin。
     * 回调接口：OnRspUserLogin。
     * 应答数据域：CQCVDRspUserLoginField。
     * 打印字段：错误码、错误信息、登录账号、账号类型、登录时间、交易日。
     */
    @Override
    public void OnRspUserLogin(CQCVDRspUserLoginField pRspUserLoginField, CQCVDRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        if (null != pRspUserLoginField) {
            log.info("OnRspUserLogin ErrorID= {} ErrorMsg= {} LogInAccount= {} LogInAccountType= {} LoginTime= {} TradingDay= {}",
                    pRspInfo.getErrorID(), pRspInfo.getErrorMsg(), pRspUserLoginField.getLogInAccount(), pRspUserLoginField.getLogInAccountType(),
                    pRspUserLoginField.getLoginTime(), pRspUserLoginField.getTradingDay());
            if (pRspInfo != null && pRspInfo.getErrorID() == 0) {
                loginSuccessCallback.run();
            }
        } else if (pRspInfo != null) {
            log.warn("OnRspUserLogin 返回为空 ErrorID={} ErrorMsg={} RequestID={} IsLast={}",
                    pRspInfo.getErrorID(), pRspInfo.getErrorMsg(), nRequestID, bIsLast);
        } else {
            log.warn("OnRspUserLogin 返回为空且无错误信息 RequestID={} IsLast={}", nRequestID, bIsLast);
        }
    }

    /**
     * A 股交易日历查询应答。
     * 请求接口：ReqReqQryShareCalendar。
     * 回调接口：OnRspInquiryShareCalendar。
     * 应答数据域：CQCVDShareCalendarField。
     * 打印字段：交易日期、页定位符、总页数。
     */
    @Override
    public void OnRspInquiryShareCalendar(CQCVDShareCalendarField pShareCalendar, CQCVDRspInfoField pRspInfo, int nRequestID, boolean bIsPageLast, boolean bIsTotalLast) {
        if (pShareCalendar == null) {
            logPageEnd("A股交易日历", pRspInfo, nRequestID, bIsPageLast, bIsTotalLast);
            shareCalendarResponseHandler.onShareCalendarPageEnd(pRspInfo, nRequestID, bIsPageLast, bIsTotalLast);
            return;
        }

        log.info("A股交易日历 RequestID={} IsPageLast={} IsTotalLast={} TradingDay={} PageLocate={} PageTotal={}",
                nRequestID, bIsPageLast, bIsTotalLast, pShareCalendar.getTradingDay(), pShareCalendar.getPageLocate(),
                pShareCalendar.getPageTotal());
        shareCalendarResponseHandler.onShareCalendarData(pShareCalendar, nRequestID);
    }

    /**
     * A 股基本资料查询应答。
     * 请求接口：ReqReqQryShareDescription。
     * 回调接口：OnRspInquiryShareDescription。
     * 应答数据域：CQCVDShareDescriptionField。
     * 打印字段：交易所、证券代码、证券简称、公司名称、上市板类型、上市板名称、上市日期、退市日期、币种、拼音、沪深港通标记。
     */
    @Override
    public void OnRspInquiryShareDescription(CQCVDShareDescriptionField pShareDescription, CQCVDRspInfoField pRspInfo, int nRequestID, boolean bIsPageLast, boolean bIsTotalLast) {
        if (pShareDescription == null) {
            logPageEnd("A股基本资料", pRspInfo, nRequestID, bIsPageLast, bIsTotalLast);
            return;
        }

        log.info("A股基本资料 RequestID={} IsPageLast={} IsTotalLast={} ExchangeID={} SecurityID={} SecurityName={} CompanyName={} ListBoard={} ListBoardName={} ListDate={} DeListDate={} Currency={} PinYin={} SHSC={} PageLocate={} PageTotal={}",
                nRequestID, bIsPageLast, bIsTotalLast, pShareDescription.getExchangeID(), pShareDescription.getSecurityID(),
                pShareDescription.getSecurityName(), pShareDescription.getCompanyName(), pShareDescription.getListBoard(),
                pShareDescription.getListBoardName(), pShareDescription.getListDate(), pShareDescription.getDeListDate(),
                pShareDescription.getCurrency(), pShareDescription.getPinYin(), pShareDescription.getSHSC(),
                pShareDescription.getPageLocate(), pShareDescription.getPageTotal());
    }

    /**
     * 中国 A 股发行信息查询应答。
     * 请求接口：ReqReqQryShareIssuance。
     * 回调接口：OnRspInquiryShareIssuance。
     * 应答数据域：CQCVDShareIssuanceField。
     * 打印字段：交易所、证券代码、证券名称、网上申购代码、申购日期、发行价格、申购上限、上市日期、上市板块、公告日期、发行状态、行业和主营业务。
     */
    @Override
    public void OnRspInquiryShareIssuance(CQCVDShareIssuanceField pShareIssuance, CQCVDRspInfoField pRspInfo, int nRequestID, boolean bIsPageLast, boolean bIsTotalLast) {
        if (pShareIssuance == null) {
            logPageEnd("中国A股发行信息", pRspInfo, nRequestID, bIsPageLast, bIsTotalLast);
            return;
        }

        log.info("中国A股发行信息 RequestID={} IsPageLast={} IsTotalLast={} ExchangeID={} SecurityID={} SecurityName={} OnlineCode={} OnlineName={} OnlineDate={} OnlinePrice={} PurchaseUpLimit={} ListDate={} ListBoardName={} AnnDate={} IsFailure={} IndustriesName={} InfoMainBusiness={} IPOAmount={} IPOAmtByPlacing={} PageLocate={} PageTotal={}",
                nRequestID, bIsPageLast, bIsTotalLast, pShareIssuance.getExchangeID(), pShareIssuance.getSecurityID(),
                pShareIssuance.getSecurityName(), pShareIssuance.getOnlineCode(), pShareIssuance.getOnlineName(),
                pShareIssuance.getOnlineDate(), pShareIssuance.getOnlinePrice(), pShareIssuance.getPurchaseUpLimit(),
                pShareIssuance.getListDate(), pShareIssuance.getListBoardName(), pShareIssuance.getAnnDate(),
                pShareIssuance.getIsFailure(), pShareIssuance.getIndustriesName(), pShareIssuance.getInfoMainBusiness(),
                pShareIssuance.getIPOAmount(), pShareIssuance.getIPOAmtByPlacing(), pShareIssuance.getPageLocate(),
                pShareIssuance.getPageTotal());
    }

    /**
     * 自由流通股本信息查询应答。
     * 请求接口：ReqQryFreeFloatSharesInfo。
     * 回调接口：OnRspInquiryFreeFloatShares。
     * 应答数据域：CQCVDFreeFloatSharesDataField。
     * 打印字段：交易所、证券代码、自由流通股本、除权变动日期、上市变动日期、公告日期、页定位符。
     */
    @Override
    public void OnRspInquiryFreeFloatShares(CQCVDFreeFloatSharesDataField pFreeFloatSharesData, CQCVDRspInfoField pRspInfo, int nRequestID, boolean bIsPageLast, boolean bIsTotalLast) {
        if (pFreeFloatSharesData == null) {
            logPageEnd("自由流通股本信息", pRspInfo, nRequestID, bIsPageLast, bIsTotalLast);
            freeFloatSharesResponseHandler.onFreeFloatSharesPageEnd(pRspInfo, nRequestID, bIsPageLast, bIsTotalLast);
            return;
        }
        freeFloatSharesResponseHandler.onFreeFloatSharesData(pFreeFloatSharesData, nRequestID);
    }

    /**
     * 生成登录请求 ID。
     * 作用：给 SPI 内部主动发起的登录请求分配递增编号。
     */
    private int nextRequestId() {
        return requestId++;
    }

    /**
     * 打印分页结束日志。
     * 作用：统一输出接口分页完成、错误码和错误信息，便于观察异步接口状态。
     */
    private void logPageEnd(String name, CQCVDRspInfoField pRspInfo, int requestId, boolean isPageLast, boolean isTotalLast) {
        if (pRspInfo == null) {
            log.info("{} 本页结束 RequestID={} IsPageLast={} IsTotalLast={} RspInfo=null", name, requestId, isPageLast, isTotalLast);
            return;
        }

        log.info("{} 本页结束 RequestID={} IsPageLast={} IsTotalLast={} ErrorID={} ErrorMsg={}",
                name, requestId, isPageLast, isTotalLast, pRspInfo.getErrorID(), pRspInfo.getErrorMsg());
    }

}
