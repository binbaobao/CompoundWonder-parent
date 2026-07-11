package com.compoundwonder.hxdata.callback;

import com.qcvalueaddproapi.CQCVDASharePreviousNameField;
import com.qcvalueaddproapi.CQCVDRspInfoField;

/**
 * A 股曾用名查询回调处理器。
 * 作用：隔离华鑫 SPI 回调和业务落库逻辑。
 */
public interface ASharePreviousNameResponseHandler {

    /**
     * 接收单条 A 股曾用名数据。
     */
    void onASharePreviousNameData(CQCVDASharePreviousNameField previousName, int requestId);

    /**
     * 接收 A 股曾用名分页结束事件。
     */
    void onASharePreviousNamePageEnd(CQCVDRspInfoField rspInfo, int requestId, boolean pageLast, boolean totalLast);
}
