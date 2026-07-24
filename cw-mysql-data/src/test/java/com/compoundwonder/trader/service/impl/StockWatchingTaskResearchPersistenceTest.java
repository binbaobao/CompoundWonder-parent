package com.compoundwonder.trader.service.impl;

import com.compoundwonder.common.strategy.selection.StockSelectionService;
import com.compoundwonder.common.strategy.selection.model.SelectionTaskData;
import com.compoundwonder.common.strategy.trade.TradeMode;
import com.compoundwonder.trader.entity.StockWatchingTask;
import com.compoundwonder.trader.mapper.StockWatchingTaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockWatchingTaskResearchPersistenceTest {

    private RecordingMapper recordingMapper;
    private StockWatchingTaskServiceImpl service;

    @BeforeEach
    void setUp() {
        recordingMapper = new RecordingMapper();
        StockWatchingTaskMapper mapper = (StockWatchingTaskMapper) Proxy.newProxyInstance(
                StockWatchingTaskMapper.class.getClassLoader(),
                new Class<?>[]{StockWatchingTaskMapper.class}, recordingMapper::invoke);
        service = new StockWatchingTaskServiceImpl((StockSelectionService) null);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
    }

    @Test
    void replacesRelayTasksAndKeepsResearchAuditReferences() {
        LocalDate recommendDate = LocalDate.of(2026, 7, 21);
        SelectionTaskData selected = new SelectionTaskData();
        selected.setStockCode("600001");
        selected.setStockName("测试股份");
        selected.setLimitUpScore(88);
        selected.setConsecutiveLimitUpDays(2);
        selected.setRecommendDate(recommendDate);
        selected.setTradeDate(LocalDate.of(2026, 7, 22));
        selected.setTradeMode(TradeMode.RELAY_LIMIT_UP.code());
        selected.setSelectionTrigger("HEIGHT_SUPPRESSION");
        selected.setSelectionStrength("NORMAL");
        selected.setStrategyVersion("relay-v1");
        selected.setSelectionRunId(11L);
        selected.setRelayCandidateRecordId(501L);
        selected.setCreatedTime(LocalDateTime.of(2026, 7, 21, 17, 30));

        List<StockWatchingTask> saved = service.replaceRelaySelectionTasks(
                recommendDate, List.of(selected));

        assertEquals(1, saved.size());
        assertEquals(1, recordingMapper.deleteCount);
        assertEquals(1, recordingMapper.inserted.size());
        StockWatchingTask task = recordingMapper.inserted.get(0);
        assertEquals("600001", task.getStockCode());
        assertEquals(TradeMode.RELAY_LIMIT_UP.code(), task.getTradeMode());
        assertEquals("HEIGHT_SUPPRESSION", task.getSelectionTrigger());
        assertEquals("NORMAL", task.getSelectionStrength());
        assertEquals("relay-v1", task.getStrategyVersion());
        assertEquals(11L, task.getSelectionRunId());
        assertEquals(501L, task.getRelayCandidateRecordId());
    }

    @Test
    void emptyRelayResultStillRemovesExistingTasksForThatDate() {
        List<StockWatchingTask> saved = service.replaceRelaySelectionTasks(
                LocalDate.of(2026, 7, 21), List.<SelectionTaskData>of());

        assertTrue(saved.isEmpty());
        assertEquals(1, recordingMapper.deleteCount);
        assertTrue(recordingMapper.inserted.isEmpty());
    }

    private static final class RecordingMapper {
        private int deleteCount;
        private final List<StockWatchingTask> inserted = new ArrayList<>();

        private Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) {
            if ("delete".equals(method.getName())) {
                deleteCount++;
                return 1;
            }
            if ("insert".equals(method.getName())
                    && args != null && args.length == 1
                    && args[0] instanceof StockWatchingTask task) {
                inserted.add(task);
                return 1;
            }
            if ("toString".equals(method.getName())) return "RecordingStockWatchingTaskMapper";
            Class<?> returnType = method.getReturnType();
            if (returnType == boolean.class) return false;
            if (returnType == int.class) return 0;
            if (returnType == long.class) return 0L;
            return null;
        }
    }
}
