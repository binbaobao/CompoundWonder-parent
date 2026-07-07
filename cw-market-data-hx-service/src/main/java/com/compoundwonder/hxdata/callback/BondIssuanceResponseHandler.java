package com.compoundwonder.hxdata.callback;

import com.qcvalueaddproapi.CQCVDBondIssuanceField;
import com.qcvalueaddproapi.CQCVDRspInfoField;

/**
 * 可转债发行信息回调处理器。
 * 作用：隔离华鑫 SPI 回调和当前状态表更新逻辑。
 */
public interface BondIssuanceResponseHandler {

    /**
     * 接收单条可转债发行信息。
     */
    void onBondIssuanceData(CQCVDBondIssuanceField bondIssuance, int requestId);

    /**
     * 接收可转债发行信息分页结束事件。
     */
    void onBondIssuancePageEnd(CQCVDRspInfoField rspInfo, int requestId, boolean pageLast, boolean totalLast);
}
