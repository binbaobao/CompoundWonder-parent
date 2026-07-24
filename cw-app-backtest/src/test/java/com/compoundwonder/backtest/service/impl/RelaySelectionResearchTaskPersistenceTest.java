package com.compoundwonder.backtest.service.impl;

import com.compoundwonder.common.mysqldata.selection.StockSelectionDataService;
import com.compoundwonder.common.mysqldata.selection.model.MarketEmotionData;
import com.compoundwonder.common.mysqldata.selection.model.StockCurrentStatusData;
import com.compoundwonder.common.mysqldata.selection.model.StockDailyData;
import com.compoundwonder.common.strategy.selection.model.SelectionTaskData;
import com.compoundwonder.common.strategy.trade.TradeMode;
import com.compoundwonder.strategy.relay.selection.RelayBoardPlan;
import com.compoundwonder.strategy.relay.selection.RelayCandidateEvaluation;
import com.compoundwonder.strategy.relay.selection.RelaySelectionPlan;
import com.compoundwonder.strategy.relay.selection.RelaySelectionResult;
import com.compoundwonder.strategy.relay.selection.RelaySelectionService;
import com.compoundwonder.strategy.relay.selection.RelaySelectionStrength;
import com.compoundwonder.strategy.relay.selection.RelaySelectionTrigger;
import com.compoundwonder.trader.entity.RelaySelectionCandidateRecord;
import com.compoundwonder.trader.entity.RelaySelectionRun;
import com.compoundwonder.trader.entity.RelaySelectionTriggerRecord;
import com.compoundwonder.trader.entity.StockWatchingTask;
import com.compoundwonder.trader.service.StockWatchingTaskService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelaySelectionResearchTaskPersistenceTest {

    @Test
    void selectedResearchCandidatesAreCopiedToWatchingTasksWithAuditIds() {
        LocalDate recommendDate = LocalDate.of(2026, 7, 21);
        LocalDate tradeDate = LocalDate.of(2026, 7, 22);
        RelaySelectionRun run = run(11L, recommendDate);
        FakeSelectionDataService dataService = new FakeSelectionDataService(
                recommendDate, tradeDate);
        FakePersistence persistenceService = new FakePersistence(run);
        RecordingWatchingTaskService recordingTasks = new RecordingWatchingTaskService();

        RelaySelectionResearchServiceImpl service = new RelaySelectionResearchServiceImpl(
                new StubRelaySelectionService(selectedResult(recommendDate, tradeDate)),
                dataService, persistenceService, new ObjectMapper(), Runnable::run,
                recordingTasks.proxy());

        service.runRange(recommendDate, recommendDate);

        assertEquals(1, recordingTasks.calls.size());
        RecordedTaskCall call = recordingTasks.calls.get(0);
        assertEquals(recommendDate, call.recommendDate());
        assertEquals(1, call.tasks().size());
        SelectionTaskData task = call.tasks().get(0);
        assertEquals("600001", task.getStockCode());
        assertEquals(11L, task.getSelectionRunId());
        assertEquals(501L, task.getRelayCandidateRecordId());
    }

    @Test
    void noTriggerDateClearsOldRelayWatchingTasks() {
        LocalDate recommendDate = LocalDate.of(2026, 7, 21);
        RelaySelectionRun run = run(12L, recommendDate);
        RelaySelectionPlan none = RelaySelectionPlan.none("没有触发");
        RelaySelectionResult noneResult = new RelaySelectionResult(
                recommendDate, none, none, List.of(), List.of(), null);
        FakeSelectionDataService dataService = new FakeSelectionDataService(
                recommendDate, recommendDate.plusDays(1));
        FakePersistence persistenceService = new FakePersistence(run);
        RecordingWatchingTaskService recordingTasks = new RecordingWatchingTaskService();

        RelaySelectionResearchServiceImpl service = new RelaySelectionResearchServiceImpl(
                new StubRelaySelectionService(noneResult), dataService, persistenceService,
                new ObjectMapper(), Runnable::run, recordingTasks.proxy());

        service.runRange(recommendDate, recommendDate);

        assertEquals(1, recordingTasks.calls.size());
        assertEquals(recommendDate, recordingTasks.calls.get(0).recommendDate());
        assertTrue(recordingTasks.calls.get(0).tasks().isEmpty());
    }

    private RelaySelectionRun run(long id, LocalDate date) {
        RelaySelectionRun run = new RelaySelectionRun();
        run.setId(id);
        run.setStartDate(date);
        run.setEndDate(date);
        return run;
    }

    private RelaySelectionResult selectedResult(LocalDate recommendDate,
                                                  LocalDate tradeDate) {
        StockDailyData daily = new StockDailyData();
        daily.setStockCode("600001");
        daily.setStockName("测试股份");
        daily.setTradeDate(recommendDate);
        daily.setConsecutiveLimitUpDays(2);
        daily.setClosePrice(10D);

        RelayCandidateEvaluation evaluation = new RelayCandidateEvaluation(
                RelaySelectionTrigger.HEIGHT_SUPPRESSION,
                RelaySelectionStrength.NORMAL,
                daily, null, false, true, 88, true, 1,
                "通过", "进入Top3");

        SelectionTaskData task = new SelectionTaskData();
        task.setStockCode("600001");
        task.setStockName("测试股份");
        task.setLimitUpScore(88);
        task.setConsecutiveLimitUpDays(2);
        task.setRecommendDate(recommendDate);
        task.setTradeDate(tradeDate);
        task.setTradeMode(TradeMode.RELAY_LIMIT_UP.code());
        task.setSelectionTrigger(RelaySelectionTrigger.HEIGHT_SUPPRESSION.name());
        task.setSelectionStrength(RelaySelectionStrength.NORMAL.name());
        task.setStrategyVersion(RelaySelectionService.STRATEGY_VERSION);

        RelaySelectionPlan plan = new RelaySelectionPlan(
                RelaySelectionTrigger.HEIGHT_SUPPRESSION,
                List.of(new RelayBoardPlan(2, RelaySelectionStrength.NORMAL)),
                3, "高度压制");
        return new RelaySelectionResult(recommendDate, plan, plan,
                List.of(evaluation), List.of(task), null);
    }

    private static final class StubRelaySelectionService extends RelaySelectionService {
        private final RelaySelectionResult result;

        private StubRelaySelectionService(RelaySelectionResult result) {
            super(null);
            this.result = result;
        }

        @Override
        public RelaySelectionResult selectDetailed(LocalDate tradeDate) {
            return result;
        }
    }

    private static final class FakeSelectionDataService implements StockSelectionDataService {
        private final LocalDate recommendDate;
        private final LocalDate tradeDate;

        private FakeSelectionDataService(LocalDate recommendDate, LocalDate tradeDate) {
            this.recommendDate = recommendDate;
            this.tradeDate = tradeDate;
        }

        @Override
        public List<LocalDate> listTradeDates(LocalDate startDate, LocalDate endDate) {
            return List.of(recommendDate);
        }

        @Override
        public LocalDate findNextTradeDate(LocalDate date) {
            return tradeDate;
        }

        @Override
        public List<MarketEmotionData> listLatestMarketEmotion(LocalDate date, int limit) {
            return List.of(new MarketEmotionData(recommendDate, 2, null));
        }

        @Override public List<StockDailyData> listDailyByTradeDate(LocalDate date) { return List.of(); }
        @Override public List<StockDailyData> listLatestDaily(String code, LocalDate date, int limit) { return List.of(); }
        @Override public List<StockDailyData> listDailyBetween(String code, LocalDate start, LocalDate end) { return List.of(); }
        @Override public List<StockDailyData> listEarliestDaily(String code, LocalDate date, int limit) { return List.of(); }
        @Override public LocalDate findLatestStDate(String code, LocalDate date) { return null; }
        @Override public LocalDate findFirstTradeDate(String code, LocalDate date) { return null; }
        @Override public StockCurrentStatusData findCurrentStatus(String code) { return null; }
        @Override public Set<String> listConvertibleBondStockCodes(LocalDate date) { return Set.of(); }
    }

    private static final class FakePersistence
            extends RelaySelectionResearchPersistenceService {
        private final RelaySelectionRun run;

        private FakePersistence(RelaySelectionRun run) {
            super(null, null, null);
            this.run = run;
        }

        @Override
        public RelaySelectionRun createRun(LocalDate startDate, LocalDate endDate,
                                           String version, String parameters) {
            return run;
        }

        @Override public RelaySelectionRun findRun(long runId) { return run; }
        @Override public void insertTrigger(RelaySelectionTriggerRecord trigger) { trigger.setId(101L); }
        @Override public void insertCandidate(RelaySelectionCandidateRecord candidate) { candidate.setId(501L); }
        @Override public void updateCandidate(RelaySelectionCandidateRecord candidate) { }
        @Override public void updateTrigger(RelaySelectionTriggerRecord trigger) { }
        @Override public void updateProgress(RelaySelectionRun updated) { }
        @Override public void complete(RelaySelectionRun completed) { completed.setStatus(COMPLETED); }
        @Override public void fail(long runId, RuntimeException exception) { throw exception; }
    }

    private static final class RecordingWatchingTaskService {
        private final List<RecordedTaskCall> calls = new ArrayList<>();

        private StockWatchingTaskService proxy() {
            return (StockWatchingTaskService) Proxy.newProxyInstance(
                    StockWatchingTaskService.class.getClassLoader(),
                    new Class<?>[]{StockWatchingTaskService.class},
                    (proxy, method, args) -> {
                        if ("replaceRelaySelectionTasks".equals(method.getName())) {
                            @SuppressWarnings("unchecked")
                            List<SelectionTaskData> tasks =
                                    List.copyOf((List<SelectionTaskData>) args[1]);
                            calls.add(new RecordedTaskCall((LocalDate) args[0], tasks));
                            return List.<StockWatchingTask>of();
                        }
                        if ("toString".equals(method.getName())) {
                            return "RecordingStockWatchingTaskService";
                        }
                        Class<?> returnType = method.getReturnType();
                        if (returnType == boolean.class) return false;
                        if (returnType == int.class) return 0;
                        if (returnType == long.class) return 0L;
                        return null;
                    });
        }
    }

    private record RecordedTaskCall(LocalDate recommendDate,
                                    List<SelectionTaskData> tasks) {
    }
}
