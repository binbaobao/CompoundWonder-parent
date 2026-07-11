package com.compoundwonder.hxdata.callback;

import com.qcvalueaddproapi.CQCVDFreeFloatSharesDataField;
import com.qcvalueaddproapi.CQCVDRspInfoField;

/**
 * 自由流通股本回调处理器。
 * 作用：把 SPI 收到的自由流通股本异步回调转交给业务层处理。
 */
public interface FreeFloatSharesResponseHandler {

    /**
     * 处理单条自由流通股本数据。
     */
    void onFreeFloatSharesData(CQCVDFreeFloatSharesDataField freeFloatSharesData, int requestId);

    /**
     * 处理自由流通股本分页结束事件。
     */
    void onFreeFloatSharesPageEnd(CQCVDRspInfoField rspInfo, int requestId, boolean pageLast, boolean totalLast);
}
