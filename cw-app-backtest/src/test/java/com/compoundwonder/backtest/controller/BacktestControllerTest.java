package com.compoundwonder.backtest.controller;

import com.compoundwonder.backtest.orderbook.BacktestOrderExecutionGateway;
import com.compoundwonder.backtest.service.impl.BackTestTradeService;
import com.compoundwonder.dto.RuleRecordDTO;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BacktestControllerTest {

    @Test
    void replaysSingleStockOrderBook() throws Exception {
        RuleRecordDTO rule = new RuleRecordDTO();
        rule.setActionType(1);
        rule.setSymbol("600000");
        BackTestTradeService tradeService = new BackTestTradeService(
                null, null, null, new BacktestOrderExecutionGateway(), 1) {
            @Override
            public synchronized List<RuleRecordDTO> backTest(String date, String stockCode, int direction) {
                return List.of(rule);
            }
        };
        BacktestController controller = new BacktestController(null, null, null, tradeService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/backtest/order-book/replay")
                        .param("stockCode", "600000")
                        .param("date", "2026-07-15")
                        .param("direction", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].actionType").value(1))
                .andExpect(jsonPath("$.data[0].symbol").value("600000"));
    }
}
