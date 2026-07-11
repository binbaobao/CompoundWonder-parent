package com.compoundwonder.backtest.service;



import com.compoundwonder.dto.Level2MinuteTickDTO;

import java.time.LocalDate;
import java.util.List;

public interface Level2MinuteBarService {

    /**
     * 查询指定股票在指定交易日的 Level2 分时 tick 数据。
     */
    List<Level2MinuteTickDTO> findMinuteBars(String stockCode, LocalDate tradeDate);
}
