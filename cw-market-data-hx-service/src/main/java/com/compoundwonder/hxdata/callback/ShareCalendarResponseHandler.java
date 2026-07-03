package com.compoundwonder.hxdata.callback;

import com.qcvalueaddproapi.CQCVDRspInfoField;
import com.qcvalueaddproapi.CQCVDShareCalendarField;

/**
 * A 股交易日历回调处理器。
 * 作用：把 SPI 收到的交易日历异步回调转交给业务层处理。
 */
public interface ShareCalendarResponseHandler {

    /**
     * 处理单条交易日历数据。
     */
    void onShareCalendarData(CQCVDShareCalendarField shareCalendar, int requestId);

    /**
     * 处理交易日历分页结束事件。
     */
    void onShareCalendarPageEnd(CQCVDRspInfoField rspInfo, int requestId, boolean pageLast, boolean totalLast);
}
