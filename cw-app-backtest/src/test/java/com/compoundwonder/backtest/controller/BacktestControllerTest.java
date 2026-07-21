package com.compoundwonder.backtest.controller;

import com.compoundwonder.backtest.orderbook.BacktestOrderExecutionGateway;
import com.compoundwonder.backtest.service.HistoricalBacktestTradeService;
import com.compoundwonder.backtest.service.impl.BackTestTradeService;
import com.compoundwonder.dto.RuleRecordDTO;
import com.compoundwonder.trader.entity.BacktestRun;
import com.compoundwonder.trader.entity.BacktestDailyRecord;
import com.compoundwonder.trader.entity.BacktestPosition;
import com.compoundwonder.trader.entity.RuleExecuteRecord;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BacktestControllerTest {

    @Test
    void replaysSingleStockOrderBook() throws Exception {
        RuleRecordDTO rule = new RuleRecordDTO();
        rule.setActionType(1);
        rule.setSymbol("600000");
        BackTestTradeService tradeService = new BackTestTradeService(
                null, null, null, null, null, new BacktestOrderExecutionGateway(), 1) {
            @Override
            public synchronized List<RuleRecordDTO> backTest(String date, String stockCode, int direction) {
                return List.of(rule);
            }
        };
        BacktestController controller = new BacktestController(
                null, null, null, tradeService, null, null);
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

    @Test
    void runsHistoricalTradingDateRange() throws Exception {
        HistoricalBacktestTradeService historicalService = new HistoricalBacktestTradeService() {
            @Override
            public BacktestRun startRange(LocalDate startDate, LocalDate endDate) {
                return runRange(startDate, endDate);
            }

            @Override
            public BacktestRun runRange(LocalDate startDate, LocalDate endDate) {
                BacktestRun run = new BacktestRun();
                run.setId(12L);
                run.setStartDate(startDate);
                run.setEndDate(endDate);
                run.setStatus(2);
                return run;
            }

            @Override
            public BacktestRun findRun(long runId) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<BacktestRun> findRecentRuns(int limit) {
                return List.of();
            }

            @Override
            public List<BacktestDailyRecord> findDailyRecords(long runId) {
                return List.of();
            }

            @Override
            public List<BacktestPosition> findPositions(long runId) {
                return List.of();
            }

            @Override
            public List<RuleExecuteRecord> findRules(long runId) {
                return List.of();
            }
        };
        BacktestController controller = new BacktestController(
                null, null, null, null, historicalService, null);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/backtest/trade-runs")
                        .param("startDate", "2026-03-01")
                        .param("endDate", "2026-07-14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(12))
                .andExpect(jsonPath("$.data.startDate").value("2026-03-01"))
                .andExpect(jsonPath("$.data.endDate").value("2026-07-14"));
    }

    @Test
    void rejectsModesOneAndTwoFromSingleModeBacktestEndpoint() throws Exception {
        BacktestController controller = new BacktestController(
                null, null, null, null, null, null);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/backtest/single-mode-runs")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-10")
                        .param("tradeMode", "1"))
                .andExpect(status().isBadRequest());
    }
}
