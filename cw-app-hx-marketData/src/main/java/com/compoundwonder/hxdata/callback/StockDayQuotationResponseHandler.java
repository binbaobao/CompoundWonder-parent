package com.compoundwonder.hxdata.callback;

import com.qcvalueaddproapi.CQCVDRspInfoField;
import com.qcvalueaddproapi.CQCVDStockDayQuotationField;

/**
 * 股票日 K 行情回调处理器。
 * 作用：把 SPI 收到的股票日 K 异步回调转交给业务层处理。
 */
public interface StockDayQuotationResponseHandler {

    /**
     * 处理单条股票日 K 数据。
     */
    void onStockDayQuotationData(CQCVDStockDayQuotationField stockDayQuotation, int requestId);

    /**
     * 处理股票日 K 分页结束事件。
     */
    void onStockDayQuotationPageEnd(CQCVDRspInfoField rspInfo, int requestId, boolean pageLast, boolean totalLast);
}
