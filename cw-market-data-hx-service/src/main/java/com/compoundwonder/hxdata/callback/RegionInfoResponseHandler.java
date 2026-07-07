package com.compoundwonder.hxdata.callback;

import com.qcvalueaddproapi.CQCVDRegionDataField;
import com.qcvalueaddproapi.CQCVDRspInfoField;

/**
 * 地域属性回调处理器。
 * 作用：隔离华鑫 SPI 回调和当前状态表更新逻辑。
 */
public interface RegionInfoResponseHandler {

    /**
     * 接收单条地域属性。
     */
    void onRegionInfoData(CQCVDRegionDataField regionData, int requestId);

    /**
     * 接收地域属性分页结束事件。
     */
    void onRegionInfoPageEnd(CQCVDRspInfoField rspInfo, int requestId, boolean pageLast, boolean totalLast);
}
