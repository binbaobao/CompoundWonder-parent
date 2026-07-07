package com.compoundwonder.hxdata.callback;

import com.qcvalueaddproapi.CQCVDCBondDescriptionField;
import com.qcvalueaddproapi.CQCVDRspInfoField;

/**
 * 可转债基本资料回调处理器。
 * 作用：隔离华鑫 SPI 回调和可转债基本资料观察逻辑。
 */
public interface CBondDescriptionResponseHandler {

    /**
     * 接收单条可转债基本资料。
     */
    void onCBondDescriptionData(CQCVDCBondDescriptionField description, int requestId);

    /**
     * 接收可转债基本资料查询结束事件。
     */
    void onCBondDescriptionEnd(CQCVDRspInfoField rspInfo, int requestId, boolean last);
}
