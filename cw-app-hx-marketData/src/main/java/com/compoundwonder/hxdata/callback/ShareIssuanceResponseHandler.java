package com.compoundwonder.hxdata.callback;

import com.qcvalueaddproapi.CQCVDRspInfoField;
import com.qcvalueaddproapi.CQCVDShareIssuanceField;

/**
 * A 股发行信息响应处理器。
 * 作用：接收华鑫发行信息异步回调，用于发现新上市股票。
 */
public interface ShareIssuanceResponseHandler {

    /**
     * 接收单条 A 股发行信息。
     */
    void onShareIssuanceData(CQCVDShareIssuanceField shareIssuance, int requestId);

    /**
     * 接收 A 股发行信息分页结束事件。
     */
    void onShareIssuancePageEnd(CQCVDRspInfoField rspInfo, int requestId, boolean pageLast, boolean totalLast);
}
