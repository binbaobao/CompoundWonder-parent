package com.compoundwonder.hxdata.api;



import cn.hutool.core.date.LocalDateTimeUtil;

import com.compoundwonder.hxdata.callback.ASharePreviousNameResponseHandler;
import com.compoundwonder.hxdata.callback.BondIssuanceResponseHandler;
import com.compoundwonder.hxdata.callback.CBondDescriptionResponseHandler;
import com.compoundwonder.hxdata.callback.FreeFloatSharesResponseHandler;
import com.compoundwonder.hxdata.callback.RegionInfoResponseHandler;
import com.compoundwonder.hxdata.callback.ShareCalendarResponseHandler;
import com.compoundwonder.hxdata.callback.ShareIssuanceResponseHandler;
import com.compoundwonder.hxdata.callback.StockDayQuotationResponseHandler;
import com.compoundwonder.hxdata.dto.ASharePreviousNamePoint;
import com.compoundwonder.hxdata.dto.ConvertibleBondDescriptionPoint;
import com.compoundwonder.hxdata.dto.ConvertibleBondIssuancePoint;
import com.compoundwonder.hxdata.dto.FreeFloatSharePoint;
import com.compoundwonder.hxdata.dto.ShareIssuancePoint;
import com.compoundwonder.hxdata.dto.StockDayQuotationPoint;
import com.compoundwonder.hxdata.entity.StockSyncTask;
import com.compoundwonder.hxdata.service.StockConvertibleBondHistoryService;
import com.compoundwonder.hxdata.service.StockCurrentStatusService;
import com.compoundwonder.hxdata.service.StockDailyUpdateTaskService;
import com.compoundwonder.hxdata.service.StockDailyService;
import com.compoundwonder.hxdata.service.StockFreeFloatShareHistoryService;
import com.compoundwonder.hxdata.service.StockPreviousNameHistoryService;
import com.compoundwonder.hxdata.service.StockSyncTaskService;
import com.compoundwonder.hxdata.service.StockTradeCalendarService;
import com.compoundwonder.hxdata.spi.SimpleQrySpi;
import com.qcvalueaddproapi.CQCVDASharePreviousNameField;
import com.qcvalueaddproapi.CQCVDBondIssuanceField;
import com.qcvalueaddproapi.CQCVDCBondDescriptionField;
import com.qcvalueaddproapi.CQCVDFreeFloatSharesDataField;
import com.qcvalueaddproapi.CQCVDRegionDataField;
import com.qcvalueaddproapi.CQCVDRspInfoField;
import com.qcvalueaddproapi.CQCVDQryFreeFloatSharesInfoField;
import com.qcvalueaddproapi.CQCVDQryRegionInfoField;
import com.qcvalueaddproapi.CQCVDReqQryASharePreviousNameField;
import com.qcvalueaddproapi.CQCVDReqQryBondIssuanceField;
import com.qcvalueaddproapi.CQCVDQryCBondDescriptionField;
import com.qcvalueaddproapi.CQCVDReqQryShareCalendarField;
import com.qcvalueaddproapi.CQCVDReqQryShareDescriptionField;
import com.qcvalueaddproapi.CQCVDReqQryShareIssuanceField;
import com.qcvalueaddproapi.CQCVDReqQryStockDayQuotationField;
import com.qcvalueaddproapi.CQCVDShareCalendarField;
import com.qcvalueaddproapi.CQCVDShareIssuanceField;
import com.qcvalueaddproapi.CQCVDStockDayQuotationField;
import com.qcvalueaddproapi.CQCValueAddProApi;
import com.qcvalueaddproapi.qcvalueaddproapiConstants;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基础数据API提供
 */
@Component
public class BasicDataApi implements ShareCalendarResponseHandler, FreeFloatSharesResponseHandler, StockDayQuotationResponseHandler, ShareIssuanceResponseHandler, ASharePreviousNameResponseHandler, BondIssuanceResponseHandler, CBondDescriptionResponseHandler, RegionInfoResponseHandler {

    private static final Logger log = LoggerFactory.getLogger(BasicDataApi.class);
    private static final int PAGE_COUNT = 1200;
    private static final int SHARE_CALENDAR_YEAR_PAGE_COUNT = 500;
    private static final String BEGIN_DATE = "20220101";
    private static final DateTimeFormatter API_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private int requestId = 1;
    private static String ADDRESS;
    private static int PORT;

    static {
        String property = System.getProperties().getProperty("os.name");
        if (property.contains("Linux")) {
            System.load("/home/usergcb/os/libqcvalueaddproapi_jini.so");
            ADDRESS = "192.168.84.61";
            PORT = 25557;
        }
    }

    private CQCValueAddProApi basicDataApi;

    private SimpleQrySpi simpleQrySpi = null;
    private final StockTradeCalendarService stockTradeCalendarService;
    private final StockCurrentStatusService stockCurrentStatusService;
    private final StockConvertibleBondHistoryService stockConvertibleBondHistoryService;
    private final StockDailyUpdateTaskService stockDailyUpdateTaskService;
    private final StockDailyService stockDailyService;
    private final StockFreeFloatShareHistoryService stockFreeFloatShareHistoryService;
    private final StockPreviousNameHistoryService stockPreviousNameHistoryService;
    private final StockSyncTaskService stockSyncTaskService;
    private final AtomicBoolean loginReady = new AtomicBoolean(false);
    private final AtomicBoolean freeFloatTaskRunning = new AtomicBoolean(false);
    private final AtomicBoolean stockDailyTaskRunning = new AtomicBoolean(false);
    private final AtomicBoolean convertibleBondDescriptionTaskRunning = new AtomicBoolean(false);
    private final AtomicBoolean regionInfoTaskRunning = new AtomicBoolean(false);
    private final AtomicBoolean preOpenTaskRunning = new AtomicBoolean(false);
    private final AtomicBoolean postCloseTaskRunning = new AtomicBoolean(false);
    private final Queue<String> regionInfoTaskQueue = new ConcurrentLinkedQueue<>();
    private volatile int regionInfoTaskTotal;
    private volatile int regionInfoTaskSuccess;
    private volatile LocalDate preOpenTaskDate;
    private volatile LocalDate postCloseTaskDate;
    private final CountDownLatch loginReadyLatch = new CountDownLatch(1);
    private final Map<Integer, ShareCalendarRequestContext> shareCalendarRequestContextMap = new ConcurrentHashMap<>();
    private final Map<Integer, FreeFloatSharesRequestContext> freeFloatSharesRequestContextMap = new ConcurrentHashMap<>();
    private final Map<Integer, StockDayQuotationRequestContext> stockDayQuotationRequestContextMap = new ConcurrentHashMap<>();
    private final Map<Integer, ShareIssuanceRequestContext> shareIssuanceRequestContextMap = new ConcurrentHashMap<>();
    private final Map<Integer, ASharePreviousNameRequestContext> aSharePreviousNameRequestContextMap = new ConcurrentHashMap<>();
    private final Map<Integer, BondIssuanceRequestContext> bondIssuanceRequestContextMap = new ConcurrentHashMap<>();
    private final Map<Integer, CBondDescriptionRequestContext> cBondDescriptionRequestContextMap = new ConcurrentHashMap<>();
    private final Map<Integer, RegionInfoRequestContext> regionInfoRequestContextMap = new ConcurrentHashMap<>();

    /**
     * 创建基础数据 API 组件。
     * 作用：注入交易日历服务，供异步回调收到数据后直接落库。
     */
    public BasicDataApi(StockTradeCalendarService stockTradeCalendarService, StockCurrentStatusService stockCurrentStatusService, StockConvertibleBondHistoryService stockConvertibleBondHistoryService, StockDailyUpdateTaskService stockDailyUpdateTaskService, StockDailyService stockDailyService, StockFreeFloatShareHistoryService stockFreeFloatShareHistoryService, StockPreviousNameHistoryService stockPreviousNameHistoryService, StockSyncTaskService stockSyncTaskService) {
        this.stockTradeCalendarService = stockTradeCalendarService;
        this.stockCurrentStatusService = stockCurrentStatusService;
        this.stockConvertibleBondHistoryService = stockConvertibleBondHistoryService;
        this.stockDailyUpdateTaskService = stockDailyUpdateTaskService;
        this.stockDailyService = stockDailyService;
        this.stockFreeFloatShareHistoryService = stockFreeFloatShareHistoryService;
        this.stockPreviousNameHistoryService = stockPreviousNameHistoryService;
        this.stockSyncTaskService = stockSyncTaskService;
    }

    /**
     * 初始化华鑫增值服务 API。
     * 注意：CQCValueAddProApi.Run() 会启动并等待 API 工作线程，不能阻塞 Spring Boot 主启动流程。
     * 这里通过独立线程启动，保证 Web 容器和 TestController 可以正常完成注册。
     */
    @PostConstruct
    public void basicDataApiInit() {
        Thread apiThread = new Thread(this::runBasicDataApi, "hx-basic-data-api");
        apiThread.setDaemon(false);
        apiThread.start();
    }

    /**
     * 在线程中启动华鑫基础数据 API。
     * 作用：完成 API 创建、前置地址注册、SPI 注册，并进入 API 工作循环。
     */
    private void runBasicDataApi() {
        log.info("basicDataApiInit");
        try {
            basicDataApi = CQCValueAddProApi.CreateInfoQryApi();
            basicDataApi.RegisterFront(ADDRESS, PORT);
            simpleQrySpi = new SimpleQrySpi(basicDataApi, this::markLoginReady, this, this, this, this, this, this, this, this);
            basicDataApi.RegisterSpi(simpleQrySpi);
            basicDataApi.Run();
        } catch (Throwable e) {
            log.error("增值服务 API 初始化失败", e);
        }
    }

    /**
     * 查询股票日 K 行情。
     * 请求接口：ReqReqQryStockDayQuotation。
     * 请求数据域：CQCVDReqQryStockDayQuotationField。
     * 回调接口：OnRspInquiryStockDayQuotation。
     * 应答数据域：CQCVDStockDayQuotationField。
     * 用途：观察 2022 年至今所有股票日 K 行情字段，后续用于设计日 K 落库表。
     */
    public void queryStockDayQuotation(int page) {
        if (!isLoginReady("股票日K行情")) {
            return;
        }

        CQCVDReqQryStockDayQuotationField request = new CQCVDReqQryStockDayQuotationField();

        request.setOrderType(qcvalueaddproapiConstants.QCVD_ORDST_ASC);
        request.setBegDate(BEGIN_DATE);
        request.setEndDate(today());
        request.setPageCount(PAGE_COUNT);
        request.setPageLocate(page);

        int ret = basicDataApi.ReqReqQryStockDayQuotation(request, nextRequestId());
        log.info("发起查询股票日K行情 page={} ret={}", page, ret);
    }

    /**
     * 启动盘前每日更新任务。
     * 处理逻辑：先判断是否交易日；交易日按曾用名、新股、自由流通股、可转债、地域、融资融券标识顺序串行执行。
     */
    public void startPreOpenUpdate(LocalDate taskDate) {
        if (!isLoginReady("盘前每日更新")) {
            return;
        }

        boolean tradeDay = stockTradeCalendarService.isTradeDay(taskDate);
        stockDailyUpdateTaskService.ensureTask(taskDate, tradeDay);
        if (!tradeDay) {
            stockDailyUpdateTaskService.markPreOpenFinished(taskDate);
            log.info("盘前每日更新跳过 taskDate={} reason=非交易日", taskDate);
            return;
        }

        if (!preOpenTaskRunning.compareAndSet(false, true)) {
            log.warn("盘前每日更新已在运行，本次启动请求忽略 taskDate={}", taskDate);
            return;
        }

        preOpenTaskDate = taskDate;
        log.info("盘前每日更新启动 taskDate={}", taskDate);
        syncDailyPreviousNameChanges(taskDate, 1, true);
    }

    /**
     * 启动盘后每日更新任务。
     * 处理逻辑：先判断是否交易日；交易日启动日 K 同步任务，完成后更新每日任务记录。
     */
    public void startPostCloseUpdate(LocalDate taskDate) {
        if (!isLoginReady("盘后每日更新")) {
            return;
        }

        boolean tradeDay = stockTradeCalendarService.isTradeDay(taskDate);
        stockDailyUpdateTaskService.ensureTask(taskDate, tradeDay);
        if (!tradeDay) {
            stockDailyUpdateTaskService.markPostCloseFinished(taskDate);
            log.info("盘后每日更新跳过 taskDate={} reason=非交易日", taskDate);
            return;
        }

        if (!postCloseTaskRunning.compareAndSet(false, true)) {
            log.warn("盘后每日更新已在运行，本次启动请求忽略 taskDate={}", taskDate);
            return;
        }

        postCloseTaskDate = taskDate;
        log.info("盘后每日更新启动 taskDate={}", taskDate);
        queryMarketStockDaily(taskDate, 1);
    }

    /**
     * 启动股票日 K 全量同步任务。
     * 处理逻辑：从任务表按股票代码升序取未完成任务，一只股票完成后自动继续下一只。
     */
    public void startStockDailySyncTasks() {
        if (!isLoginReady("股票日K全量同步")) {
            return;
        }

        if (!stockDailyTaskRunning.compareAndSet(false, true)) {
            log.warn("股票日K全量同步已在运行，本次启动请求忽略");
            return;
        }

        log.info("股票日K全量同步任务启动");
        queryNextStockDailyTask();
    }

    /**
     * 查询指定股票日 K 并落库。
     * 用途：单只股票测试入口，不修改任务表同步状态。
     */
    public void queryStockDailyAndSave(String stockCode, int page) {
        if (!isLoginReady("股票日K单股测试")) {
            return;
        }

        queryStockDaily(stockCode, page, false);
    }

    /**
     * 查询下一个未完成股票日 K 同步任务。
     * 处理逻辑：按股票代码升序取一条未完成任务，发起异步查询；没有任务时结束本轮同步。
     */
    private void queryNextStockDailyTask() {
        List<StockSyncTask> tasks = stockSyncTaskService.listPendingDailyKlineTasks(1);
        if (tasks.isEmpty()) {
            stockDailyTaskRunning.set(false);
            finishPostCloseDailyKlineTaskIfNeeded();
            log.info("股票日K全量同步任务完成，已没有待同步股票");
            return;
        }

        queryStockDailyTask(tasks.get(0).getStockCode(), 1);
    }

    /**
     * 查询任务表中的指定股票日 K。
     * 请求限制：PageCount 固定 1800，当前阶段按单只股票查询。
     */
    private void queryStockDailyTask(String stockCode, int page) {
        queryStockDaily(stockCode, page, true);
    }

    /**
     * 查询指定股票日 K。
     * 处理逻辑：按股票代码发起 2022 年至今的日 K 查询，回调结束后由上下文决定是否标记任务完成。
     */
    private void queryStockDaily(String stockCode, int page, boolean fromTask) {
        CQCVDReqQryStockDayQuotationField request = new CQCVDReqQryStockDayQuotationField();
        request.setSecurityID(stockCode);
        request.setOrderType(qcvalueaddproapiConstants.QCVD_ORDST_ASC);
        request.setBegDate(BEGIN_DATE);
        request.setEndDate(today());
        request.setPageCount(1800);
        request.setPageLocate(page);

        int currentRequestId = nextRequestId();
        stockDayQuotationRequestContextMap.put(currentRequestId, new StockDayQuotationRequestContext(stockCode, page, fromTask));
        int ret = basicDataApi.ReqReqQryStockDayQuotation(request, currentRequestId);
        log.info("发起股票日K任务查询 stockCode={} page={} requestId={} ret={}", stockCode, page, currentRequestId, ret);
        if (ret != 0) {
            stockDayQuotationRequestContextMap.remove(currentRequestId);
            if (fromTask) {
                stockDailyTaskRunning.set(false);
            }
            log.warn("股票日K任务查询发起失败 stockCode={} page={} ret={}，任务已暂停", stockCode, page, ret);
        }
    }

    /**
     * 查询 A 股交易日历。
     * 请求接口：ReqReqQryShareCalendar。
     * 请求数据域：CQCVDReqQryShareCalendarField。
     * 回调接口：OnRspInquiryShareCalendar。
     * 应答数据域：CQCVDShareCalendarField。
     * 用途：观察真实交易日数据，后续用于按交易日而不是自然日计算区间。
     */
    public void queryShareCalendar(int page) {
        if (!isLoginReady("A股交易日历")) {
            return;
        }

        CQCVDReqQryShareCalendarField request = new CQCVDReqQryShareCalendarField();
        request.setOrderType(qcvalueaddproapiConstants.QCVD_ORDST_ASC);
        request.setBegDate(BEGIN_DATE);
        request.setEndDate(today());
        request.setPageCount(PAGE_COUNT);
        request.setPageLocate(page);

        int ret = basicDataApi.ReqReqQryShareCalendar(request, nextRequestId());
        log.info("发起查询A股交易日历 page={} ret={}", page, ret);
    }

    /**
     * 同步指定年份范围的 A 股交易日历。
     * 处理逻辑：每一年发起一次查询，接口回调返回的数据会按页批量保存到 stock_trade_calendar。
     */
    public void syncShareCalendarYears(int beginYear, int endYear) {
        if (!isLoginReady("A股交易日历年度同步")) {
            return;
        }

        for (int year = beginYear; year <= endYear; year++) {
            queryShareCalendarByYear(year, 1);
        }
    }

    /**
     * 按年份查询 A 股交易日历。
     * 请求接口：ReqReqQryShareCalendar。
     * 请求范围：指定年份的 1 月 1 日至 12 月 31 日。
     */
    public void queryShareCalendarByYear(int year, int page) {
        if (!isLoginReady("A股交易日历年度同步")) {
            return;
        }

        String beginDate = year + "0101";
        String endDate = year + "1231";
        CQCVDReqQryShareCalendarField request = new CQCVDReqQryShareCalendarField();
        request.setOrderType(qcvalueaddproapiConstants.QCVD_ORDST_ASC);
        request.setBegDate(beginDate);
        request.setEndDate(endDate);
        request.setPageCount(SHARE_CALENDAR_YEAR_PAGE_COUNT);
        request.setPageLocate(page);

        int currentRequestId = nextRequestId();
        shareCalendarRequestContextMap.put(currentRequestId, new ShareCalendarRequestContext(year, page));
        int ret = basicDataApi.ReqReqQryShareCalendar(request, currentRequestId);
        log.info("发起年度同步A股交易日历 year={} page={} requestId={} ret={}", year, page, currentRequestId, ret);
        if (ret != 0) {
            shareCalendarRequestContextMap.remove(currentRequestId);
        }
    }

    /**
     * 查询 A 股基本资料。
     * 请求接口：ReqReqQryShareDescription。
     * 请求数据域：CQCVDReqQryShareDescriptionField。
     * 回调接口：OnRspInquiryShareDescription。
     * 应答数据域：CQCVDShareDescriptionField。
     * 用途：观察股票代码、名称、上市日期、退市日期、上市板块等主数据字段。
     */
    public void queryShareDescription(int page) {
        if (!isLoginReady("A股基本资料")) {
            return;
        }

        CQCVDReqQryShareDescriptionField request = new CQCVDReqQryShareDescriptionField();
        request.setOrderType(qcvalueaddproapiConstants.QCVD_ORDST_ASC);
        request.setPageCount(PAGE_COUNT);
        request.setPageLocate(page);

        int ret = basicDataApi.ReqReqQryShareDescription(request, nextRequestId());
        log.info("发起查询A股基本资料 page={} ret={}", page, ret);
    }

    /**
     * 查询中国 A 股发行信息。
     * 请求接口：ReqReqQryShareIssuance。
     * 请求数据域：CQCVDReqQryShareIssuanceField。
     * 回调接口：OnRspInquiryShareIssuance。
     * 应答数据域：CQCVDShareIssuanceField。
     * 用途：观察新股发行、申购、上市日期等字段，后续用于自动发现新上市股票。
     */
    public void queryShareIssuance(int page) {
        if (!isLoginReady("中国A股发行信息")) {
            return;
        }

        CQCVDReqQryShareIssuanceField request = new CQCVDReqQryShareIssuanceField();
        request.setBegListDate(BEGIN_DATE);
        request.setEndListDate(today());
        request.setIsFailure('0');
        request.setOrderType(qcvalueaddproapiConstants.QCVD_ORDST_ASC);
        request.setPageCount(PAGE_COUNT);
        request.setPageLocate(page);

        int ret = basicDataApi.ReqReqQryShareIssuance(request, nextRequestId());
        log.info("发起查询中国A股发行信息 page={} ret={}", page, ret);
    }

    /**
     * 发现指定上市日期的新股。
     * 处理逻辑：查询发行信息中的上市日期，发现任务表不存在的股票后新增同步任务；曾用名后续单独处理。
     */
    public void discoverNewListedStocks(LocalDate listDate, int page) {
        if (!isLoginReady("新上市股票发现")) {
            return;
        }

        discoverNewListedStocks(listDate, page, false);
    }

    /**
     * 发现指定上市日期的新股。
     * 处理逻辑：fromPreOpenTask 为 true 时，完成后标记每日任务并继续下一步。
     */
    private void discoverNewListedStocks(LocalDate listDate, int page, boolean fromPreOpenTask) {
        if (!isLoginReady("新上市股票发现")) {
            return;
        }

        String queryDate = formatApiDate(listDate);
        CQCVDReqQryShareIssuanceField request = new CQCVDReqQryShareIssuanceField();
        request.setBegListDate(queryDate);
        request.setEndListDate(queryDate);
        request.setIsFailure('0');
        request.setOrderType(qcvalueaddproapiConstants.QCVD_ORDST_ASC);
        request.setPageCount(PAGE_COUNT);
        request.setPageLocate(page);

        int currentRequestId = nextRequestId();
        shareIssuanceRequestContextMap.put(currentRequestId, new ShareIssuanceRequestContext(listDate, page, fromPreOpenTask));
        int ret = basicDataApi.ReqReqQryShareIssuance(request, currentRequestId);
        log.info("发起新上市股票发现 listDate={} page={} requestId={} ret={}", queryDate, page, currentRequestId, ret);
        if (ret != 0) {
            shareIssuanceRequestContextMap.remove(currentRequestId);
            log.warn("新上市股票发现发起失败 listDate={} page={} ret={}", queryDate, page, ret);
            if (fromPreOpenTask) {
                stopPreOpenTask("新上市股票发现发起失败");
            }
        }
    }

    /**
     * 同步指定日期生效的 A 股曾用名变化。
     * 请求接口：ReqQryASharePreviousName。
     * 请求数据域：CQCVDReqQryASharePreviousNameField。
     * 回调接口：OnRspQryASharePreviousName。
     * 应答数据域：CQCVDASharePreviousNameField。
     * 处理逻辑：只查 begin_date 等于指定日期的记录，收到后增量维护曾用名历史区间；新股首次出现时补充同步任务。
     */
    public void syncDailyPreviousNameChanges(LocalDate beginDate, int page) {
        if (!isLoginReady("每日曾用名同步")) {
            return;
        }

        syncDailyPreviousNameChanges(beginDate, page, false);
    }

    /**
     * 同步指定日期生效的 A 股曾用名变化。
     * 处理逻辑：fromPreOpenTask 为 true 时，完成后标记每日任务并继续下一步。
     */
    private void syncDailyPreviousNameChanges(LocalDate beginDate, int page, boolean fromPreOpenTask) {
        if (!isLoginReady("每日曾用名同步")) {
            return;
        }

        String queryDate = formatApiDate(beginDate);
        CQCVDReqQryASharePreviousNameField request = new CQCVDReqQryASharePreviousNameField();
        request.setBeginDateQryBeginDay(queryDate);
        request.setBeginDateQryEndDay(queryDate);
        request.setEndDateQryBeginDay("19900901");
        request.setEndDateQryEndDay(queryDate);
        request.setANNDateQryBeginDay("19900901");
        request.setANNDateQryEndDay(queryDate);
        request.setOrderType(qcvalueaddproapiConstants.QCVD_ORDST_ASC);
        request.setPageCount(PAGE_COUNT);
        request.setPageLocate(page);

        int currentRequestId = nextRequestId();
        aSharePreviousNameRequestContextMap.put(currentRequestId, new ASharePreviousNameRequestContext(beginDate, page, fromPreOpenTask));
        int ret = basicDataApi.ReqQryASharePreviousName(request, currentRequestId);
        log.info("发起每日曾用名同步 beginDate={} page={} requestId={} ret={}", queryDate, page, currentRequestId, ret);
        if (ret != 0) {
            aSharePreviousNameRequestContextMap.remove(currentRequestId);
            log.warn("每日曾用名同步发起失败 beginDate={} page={} ret={}", queryDate, page, ret);
            if (fromPreOpenTask) {
                stopPreOpenTask("每日曾用名同步发起失败");
            }
        }
    }

    /**
     * 调试 000301 可转债发行信息。
     * 请求接口：ReqReqQryBondIssuance。
     * 处理逻辑：按正股代码 000301 查询发行信息，分页结束后用返回的转债代码继续查询可转债基本资料。
     */
    public void debugBondIssuanceFor000301(int page) {
        if (!isLoginReady("调试000301可转债发行信息")) {
            return;
        }

        CQCVDReqQryBondIssuanceField request = new CQCVDReqQryBondIssuanceField();
        request.setSecurityID("127030");
        request.setBegListDate("19900101");
        request.setEndListDate(today());
        request.setPageCount(5);
        request.setPageLocate(page);

        int currentRequestId = nextRequestId();
        bondIssuanceRequestContextMap.put(currentRequestId, new BondIssuanceRequestContext(page, true));
        int ret = basicDataApi.ReqReqQryBondIssuance(request, currentRequestId);
        log.info("发起调试000301可转债发行信息 stockCode=000301 page={} requestId={} ret={} 说明=结束后会按转债代码继续查询基本资料", page, currentRequestId, ret);
        if (ret != 0) {
            bondIssuanceRequestContextMap.remove(currentRequestId);
        }
    }

    /**
     * 调试指定转债发行信息。
     * 请求接口：ReqReqQryBondIssuance、ReqQryCBondDescription。
     * 处理逻辑：先按转债代码查询发行信息，再查询同一转债代码的基本资料，用日志观察是否发行失败或缺少上市日期。
     */
    public void debugBondIssuanceByBondCode(String bondCode, int page) {
        if (!isLoginReady("调试指定转债发行信息")) {
            return;
        }

        CQCVDReqQryBondIssuanceField request = new CQCVDReqQryBondIssuanceField();
        request.setSecurityID(bondCode);
        request.setBegListDate("19900101");
        request.setEndListDate(today());
        request.setPageCount(100);
        request.setPageLocate(page);

        int currentRequestId = nextRequestId();
        int ret = basicDataApi.ReqReqQryBondIssuance(request, currentRequestId);
        log.info("发起调试指定转债发行信息 bondCode={} page={} requestId={} ret={} 说明=同时查询基本资料观察上市日期", bondCode, page, currentRequestId, ret);
        queryCBondDescription(bondCode);
    }

    /**
     * 全量同步当前可转债标识。
     * 请求接口：ReqReqQryBondIssuance。
     * 处理逻辑：查询历史可转债发行信息，只把已有正股且已有上市日期的发行关系落库；当前状态等基本资料补全后刷新。
     */
    public void syncConvertibleBondStatus(int page) {
        if (!isLoginReady("同步当前可转债标识")) {
            return;
        }

        syncConvertibleBondStatus(page, false);
    }

    /**
     * 全量同步当前可转债标识。
     * 处理逻辑：fromPreOpenTask 为 true 时，发行关系完成后继续补全基本资料，补全完成后再刷新当前状态。
     */
    private void syncConvertibleBondStatus(int page, boolean fromPreOpenTask) {
        if (!isLoginReady("同步当前可转债标识")) {
            return;
        }

        CQCVDReqQryBondIssuanceField request = new CQCVDReqQryBondIssuanceField();
        request.setBegListDate("19900101");
        request.setEndListDate(today());
        request.setPageCount(5000);
        request.setPageLocate(page);

        int currentRequestId = nextRequestId();
        bondIssuanceRequestContextMap.put(currentRequestId, new BondIssuanceRequestContext(page, false, fromPreOpenTask));
        int ret = basicDataApi.ReqReqQryBondIssuance(request, currentRequestId);
        log.info("发起同步当前可转债标识 page={} requestId={} ret={}", page, currentRequestId, ret);
        if (ret != 0) {
            bondIssuanceRequestContextMap.remove(currentRequestId);
            log.warn("同步当前可转债标识发起失败 page={} ret={}", page, ret);
            if (fromPreOpenTask) {
                stopPreOpenTask("同步当前可转债标识发起失败");
            }
        }
    }

    /**
     * 启动可转债基本资料历史补全任务。
     * 处理逻辑：从已落库的发行关系中查找生命周期字段缺失的转债代码，逐个查询基本资料并补全日期。
     */
    public void startConvertibleBondDescriptionFillTasks() {
        if (!isLoginReady("可转债基本资料历史补全")) {
            return;
        }

        startConvertibleBondDescriptionFillTasks(false);
    }

    /**
     * 启动可转债基本资料历史补全任务。
     * 处理逻辑：fromPreOpenTask 为 true 时，补全结束后标记每日任务并继续下一步。
     */
    private void startConvertibleBondDescriptionFillTasks(boolean fromPreOpenTask) {
        if (!isLoginReady("可转债基本资料历史补全")) {
            return;
        }

        if (!convertibleBondDescriptionTaskRunning.compareAndSet(false, true)) {
            log.warn("可转债基本资料历史补全已在运行，本次启动请求忽略");
            if (fromPreOpenTask) {
                stopPreOpenTask("可转债基本资料历史补全已在运行");
            }
            return;
        }

        log.info("可转债基本资料历史补全任务启动");
        queryNextConvertibleBondDescriptionTask(fromPreOpenTask);
    }

    /**
     * 查询下一个待补全基本资料的转债代码。
     * 处理逻辑：一次只发起一个查询，等待回调结束后再继续下一只。
     */
    private void queryNextConvertibleBondDescriptionTask(boolean fromPreOpenTask) {
        List<String> bondCodes = stockConvertibleBondHistoryService.listPendingDescriptionBondCodes(1);
        if (bondCodes.isEmpty()) {
            convertibleBondDescriptionTaskRunning.set(false);
            LocalDate refreshDate = fromPreOpenTask ? preOpenTaskDate : LocalDate.now();
            refreshConvertibleBondFlagsFromHistory("可转债基本资料历史补全完成", refreshDate);
            log.info("可转债基本资料历史补全任务完成，已没有待补全转债");
            if (fromPreOpenTask) {
                stockDailyUpdateTaskService.markConvertibleBondSynced(preOpenTaskDate);
                startPreOpenRegionStep();
            }
            return;
        }

        queryCBondDescription(bondCodes.get(0), true, fromPreOpenTask);
    }

    /**
     * 查询指定转债基本资料。
     * 请求接口：ReqQryCBondDescription。
     * 请求数据域：CQCVDQryCBondDescriptionField。
     * 回调接口：OnRspQryCBondDescription。
     * 应答数据域：CQCVDCBondDescriptionField。
     */
    public void queryCBondDescription(String bondCode) {
        queryCBondDescription(bondCode, false);
    }

    /**
     * 查询指定转债基本资料。
     * 处理逻辑：fromTask 为 true 时，当前转债查询完成后继续调度下一只。
     */
    private void queryCBondDescription(String bondCode, boolean fromTask) {
        queryCBondDescription(bondCode, fromTask, false);
    }

    /**
     * 查询指定转债基本资料。
     * 处理逻辑：fromPreOpenTask 为 true 时，补全队列完成后继续盘前任务。
     */
    private void queryCBondDescription(String bondCode, boolean fromTask, boolean fromPreOpenTask) {
        if (!isLoginReady("查询可转债基本资料")) {
            return;
        }

        CQCVDQryCBondDescriptionField request = new CQCVDQryCBondDescriptionField();
        request.setSecurityID(bondCode);
        request.setPageCount(100);
        request.setPageLocate(1);

        int currentRequestId = nextRequestId();
        cBondDescriptionRequestContextMap.put(currentRequestId, new CBondDescriptionRequestContext(bondCode, fromTask, fromPreOpenTask));
        int ret = basicDataApi.ReqQryCBondDescription(request, currentRequestId);
        log.info("发起查询可转债基本资料 bondCode={} requestId={} ret={}", bondCode, currentRequestId, ret);
        if (ret != 0) {
            cBondDescriptionRequestContextMap.remove(currentRequestId);
            log.warn("查询可转债基本资料发起失败 bondCode={} ret={}", bondCode, ret);
            if (fromTask) {
                convertibleBondDescriptionTaskRunning.set(false);
            }
            if (fromPreOpenTask) {
                stopPreOpenTask("查询可转债基本资料发起失败");
            }
        }
    }

    /**
     * 调试 000301 地域属性。
     * 请求接口：ReqQryRegionInfo。
     * 处理逻辑：直接按证券代码 000301 查询地域属性，回调中打印收到的完整字段。
     */
    public void debugRegionInfoFor000301(int page) {
        if (!isLoginReady("调试000301地域属性")) {
            return;
        }

        CQCVDQryRegionInfoField request = new CQCVDQryRegionInfoField();
        request.setSecurityID("000301");
        request.setPageCount(100);
        request.setPageLocate(page);

        int currentRequestId = nextRequestId();
        regionInfoRequestContextMap.put(currentRequestId, new RegionInfoRequestContext("000301", page));
        int ret = basicDataApi.ReqQryRegionInfo(request, currentRequestId);
        log.info("发起调试000301地域属性 page={} requestId={} ret={}", page, currentRequestId, ret);
        if (ret != 0) {
            regionInfoRequestContextMap.remove(currentRequestId);
        }
    }

    /**
     * 同步指定股票地域属性。
     * 请求接口：ReqQryRegionInfo。
     * 处理逻辑：按股票代码查询地域属性，回调收到后更新当前状态表 region_name。
     */
    public void syncRegionInfo(String stockCode, int page) {
        if (!isLoginReady("同步地域属性")) {
            return;
        }

        queryRegionInfo(stockCode, page, false);
    }

    /**
     * 启动全部股票地域属性同步任务。
     * 处理逻辑：从当前状态表取全部股票代码，按代码升序逐只查询地域属性，避免一次性发起大量异步请求。
     */
    public void startAllRegionInfoSyncTasks() {
        if (!isLoginReady("同步全部地域属性")) {
            return;
        }

        startAllRegionInfoSyncTasks(false);
    }

    /**
     * 启动全部股票地域属性同步任务。
     * 处理逻辑：fromPreOpenTask 为 true 时，完成后标记每日任务并结束盘前任务。
     */
    private void startAllRegionInfoSyncTasks(boolean fromPreOpenTask) {
        if (!isLoginReady("同步全部地域属性")) {
            return;
        }

        if (!regionInfoTaskRunning.compareAndSet(false, true)) {
            log.warn("全部地域属性同步任务已在运行，本次启动请求忽略");
            if (fromPreOpenTask) {
                stopPreOpenTask("全部地域属性同步任务已在运行");
            }
            return;
        }

        List<String> stockCodes = stockCurrentStatusService.listAllStockCodes();
        regionInfoTaskQueue.clear();
        regionInfoTaskQueue.addAll(stockCodes);
        regionInfoTaskTotal = stockCodes.size();
        regionInfoTaskSuccess = 0;
        log.info("全部地域属性同步任务启动 total={}", regionInfoTaskTotal);
        queryNextRegionInfoTask(fromPreOpenTask);
    }

    /**
     * 查询下一个待同步地域属性的股票代码。
     * 处理逻辑：一个请求完成后再发起下一只，直到队列为空。
     */
    private void queryNextRegionInfoTask(boolean fromPreOpenTask) {
        String stockCode = regionInfoTaskQueue.poll();
        if (stockCode == null) {
            regionInfoTaskRunning.set(false);
            log.info("全部地域属性同步任务完成 total={} success={}", regionInfoTaskTotal, regionInfoTaskSuccess);
            if (fromPreOpenTask) {
                stockDailyUpdateTaskService.markRegionSynced(preOpenTaskDate);
                stockDailyUpdateTaskService.markMarginTradingSynced(preOpenTaskDate);
                stockDailyUpdateTaskService.markPreOpenFinished(preOpenTaskDate);
                preOpenTaskRunning.set(false);
                log.info("盘前每日更新完成 taskDate={}", preOpenTaskDate);
            }
            return;
        }

        queryRegionInfo(stockCode, 1, true, fromPreOpenTask);
    }

    /**
     * 查询指定股票地域属性。
     * 处理逻辑：fromTask 为 true 时，当前股票查询结束后继续调度下一只。
     */
    private void queryRegionInfo(String stockCode, int page, boolean fromTask) {
        queryRegionInfo(stockCode, page, fromTask, false);
    }

    /**
     * 查询指定股票地域属性。
     * 处理逻辑：fromPreOpenTask 为 true 时，全量队列结束后完成盘前任务。
     */
    private void queryRegionInfo(String stockCode, int page, boolean fromTask, boolean fromPreOpenTask) {
        if (!isLoginReady("同步地域属性")) {
            return;
        }

        CQCVDQryRegionInfoField request = new CQCVDQryRegionInfoField();
        request.setSecurityID(stockCode);
        request.setPageCount(100);
        request.setPageLocate(page);

        int currentRequestId = nextRequestId();
        regionInfoRequestContextMap.put(currentRequestId, new RegionInfoRequestContext(stockCode, page, fromTask, fromPreOpenTask));
        int ret = basicDataApi.ReqQryRegionInfo(request, currentRequestId);
        log.info("发起同步地域属性 stockCode={} page={} requestId={} ret={}", stockCode, page, currentRequestId, ret);
        if (ret != 0) {
            regionInfoRequestContextMap.remove(currentRequestId);
            log.warn("同步地域属性发起失败 stockCode={} page={} ret={}", stockCode, page, ret);
            if (fromTask) {
                queryNextRegionInfoTask(fromPreOpenTask);
            }
        }
    }

    /**
     * 查询自由流通股本信息。
     * 请求接口：ReqQryFreeFloatSharesInfo。
     * 请求数据域：CQCVDQryFreeFloatSharesInfoField。
     * 回调接口：OnRspInquiryFreeFloatShares。
     * 应答数据域：CQCVDFreeFloatSharesDataField。
     * 用途：观察自由流通股本变动日期和公告日期，后续用于生成真实流通股时间区间。
     */
    public void queryFreeFloatShares(int page) {
        if (!isLoginReady("自由流通股本信息")) {
            return;
        }

        CQCVDQryFreeFloatSharesInfoField request = new CQCVDQryFreeFloatSharesInfoField();
        request.setSecurityID("603991");
        request.setBegDate(BEGIN_DATE);
        request.setEndDate(today());
        request.setPageCount(1800);
        request.setPageLocate(page);

        int ret = basicDataApi.ReqQryFreeFloatSharesInfo(request, nextRequestId());
        log.info("发起查询自由流通股本信息 page={} ret={}", page, ret);
    }

    /**
     * 启动自由流通股本全量同步任务。
     * 处理逻辑：从任务表按股票代码升序取未完成任务，一只股票完成后自动继续下一只。
     */
    public void startFreeFloatShareSyncTasks() {
        if (!isLoginReady("自由流通股本全量同步")) {
            return;
        }

        if (!freeFloatTaskRunning.compareAndSet(false, true)) {
            log.warn("自由流通股本全量同步已在运行，本次启动请求忽略");
            return;
        }

        log.info("自由流通股本全量同步任务启动");
        queryNextFreeFloatShareTask();
    }

    /**
     * 查询指定股票自由流通股本并保存历史区间。
     * 请求接口：ReqQryFreeFloatSharesInfo。
     * 请求限制：PageCount 固定 1800，当前测试阶段不传交易所代码。
     */
    public void queryFreeFloatSharesAndSaveHistory(String stockCode, int page) {
        if (!isLoginReady("自由流通股本信息入库")) {
            return;
        }

        CQCVDQryFreeFloatSharesInfoField request = new CQCVDQryFreeFloatSharesInfoField();
        request.setSecurityID(stockCode);
        request.setBegDate(BEGIN_DATE);
        request.setEndDate(today());
        request.setPageCount(1800);
        request.setPageLocate(page);

        int currentRequestId = nextRequestId();
        freeFloatSharesRequestContextMap.put(currentRequestId, new FreeFloatSharesRequestContext(stockCode, page, false, false));
        int ret = basicDataApi.ReqQryFreeFloatSharesInfo(request, currentRequestId);
        log.info("发起查询自由流通股本入库 stockCode={} page={} requestId={} ret={}", stockCode, page, currentRequestId, ret);
        if (ret != 0) {
            freeFloatSharesRequestContextMap.remove(currentRequestId);
        }
    }

    /**
     * 同步指定日期自由流通股本变化。
     * 处理逻辑：不传股票代码，按日期查询全市场变化点，并增量合并到自由流通股本历史区间。
     */
    public void syncDailyFreeFloatShares(LocalDate date, int page) {
        if (!isLoginReady("每日自由流通股本同步")) {
            return;
        }

        syncDailyFreeFloatShares(date, page, false);
    }

    /**
     * 同步指定日期自由流通股本变化。
     * 处理逻辑：fromPreOpenTask 为 true 时，完成后标记每日任务并继续下一步。
     */
    private void syncDailyFreeFloatShares(LocalDate date, int page, boolean fromPreOpenTask) {
        if (!isLoginReady("每日自由流通股本同步")) {
            return;
        }

        String queryDate = formatApiDate(date);
        CQCVDQryFreeFloatSharesInfoField request = new CQCVDQryFreeFloatSharesInfoField();
        request.setBegDate(queryDate);
        request.setEndDate(queryDate);
        request.setPageCount(1000);
        request.setPageLocate(page);

        int currentRequestId = nextRequestId();
        freeFloatSharesRequestContextMap.put(currentRequestId, new FreeFloatSharesRequestContext(null, page, false, true, fromPreOpenTask, date));
        int ret = basicDataApi.ReqQryFreeFloatSharesInfo(request, currentRequestId);
        log.info("发起每日自由流通股本同步 date={} page={} requestId={} ret={} 说明=不设置SecurityID查询全市场", queryDate, page, currentRequestId, ret);
        if (ret != 0) {
            freeFloatSharesRequestContextMap.remove(currentRequestId);
            log.warn("每日自由流通股本同步发起失败 date={} page={} ret={}", queryDate, page, ret);
            if (fromPreOpenTask) {
                stopPreOpenTask("每日自由流通股本同步发起失败");
            }
        }
    }

    /**
     * 查询下一个未完成自由流通股本同步任务。
     * 处理逻辑：按股票代码升序取一条未完成任务，发起异步查询；没有任务时结束本轮同步。
     */
    private void queryNextFreeFloatShareTask() {
        List<StockSyncTask> tasks = stockSyncTaskService.listPendingFreeFloatTasks(1);
        if (tasks.isEmpty()) {
            freeFloatTaskRunning.set(false);
            log.info("自由流通股本全量同步任务完成，已没有待同步股票");
            return;
        }

        String stockCode = tasks.get(0).getStockCode();
        queryFreeFloatSharesTask(stockCode, 1);
    }

    /**
     * 查询任务表中的指定股票自由流通股本。
     * 请求限制：PageCount 固定 1800，当前阶段不传交易所代码。
     */
    private void queryFreeFloatSharesTask(String stockCode, int page) {
        CQCVDQryFreeFloatSharesInfoField request = new CQCVDQryFreeFloatSharesInfoField();
        request.setSecurityID(stockCode);
        request.setBegDate(BEGIN_DATE);
        request.setEndDate(today());
        request.setPageCount(1800);
        request.setPageLocate(page);

        int currentRequestId = nextRequestId();
        freeFloatSharesRequestContextMap.put(currentRequestId, new FreeFloatSharesRequestContext(stockCode, page, true, false));
        int ret = basicDataApi.ReqQryFreeFloatSharesInfo(request, currentRequestId);
        log.info("发起自由流通股本任务查询 stockCode={} page={} requestId={} ret={}", stockCode, page, currentRequestId, ret);
        if (ret != 0) {
            freeFloatSharesRequestContextMap.remove(currentRequestId);
            freeFloatTaskRunning.set(false);
            log.warn("自由流通股本任务查询发起失败 stockCode={} page={} ret={}，任务已暂停", stockCode, page, ret);
        }
    }

    /**
     * 释放华鑫基础数据 API。
     * 作用：服务关闭或手动销毁时释放底层 API 资源。
     */
    public void release() {
        basicDataApi.Release();
        simpleQrySpi = null;
        log.info("增值服务 API 接口销毁");
    }

    /**
     * 等待华鑫增值服务登录完成。
     * 作用：给控制器或任务调度入口提供登录就绪判断，避免未登录时直接发起查询。
     */
    public boolean awaitLoginReady(long timeoutMillis) {
        if (loginReady.get()) {
            return true;
        }

        try {
            return loginReadyLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("等待增值服务登录完成时被中断", e);
            return false;
        }
    }

    /**
     * 接收单条 A 股交易日历数据。
     * 处理逻辑：按请求 ID 找到年度同步上下文，把接口日期转换成 LocalDate 后暂存到当前页缓存。
     */
    @Override
    public void onShareCalendarData(CQCVDShareCalendarField shareCalendar, int requestId) {
        ShareCalendarRequestContext context = shareCalendarRequestContextMap.get(requestId);
        if (context == null) {
            return;
        }

        LocalDate tradeDate = parseApiDate(shareCalendar.getTradingDay());
        if (tradeDate == null) {
            return;
        }
        context.tradeDates.add(tradeDate);
    }

    /**
     * 接收 A 股交易日历分页结束事件。
     * 处理逻辑：保存当前页交易日期；年度交易日历每次查询 500 条，正常不需要翻页。
     */
    @Override
    public void onShareCalendarPageEnd(CQCVDRspInfoField rspInfo, int requestId, boolean pageLast, boolean totalLast) {
        ShareCalendarRequestContext context = shareCalendarRequestContextMap.remove(requestId);
        if (context == null) {
            return;
        }

        if (rspInfo != null && rspInfo.getErrorID() != 0) {
            log.warn("年度同步A股交易日历失败 year={} page={} requestId={} ErrorID={} ErrorMsg={}",
                    context.year, context.page, requestId, rspInfo.getErrorID(), rspInfo.getErrorMsg());
            return;
        }

        int saveCount = stockTradeCalendarService.saveTradeDates(context.tradeDates);
        log.info("年度同步A股交易日历完成一页 year={} page={} requestId={} rows={} totalLast={}",
                context.year, context.page, requestId, saveCount, totalLast);

        if (!totalLast) {
            log.warn("年度同步A股交易日历返回未结束 year={} page={} requestId={}，请检查 PageCount={} 是否不足",
                    context.year, context.page, requestId, SHARE_CALENDAR_YEAR_PAGE_COUNT);
        }
    }

    /**
     * 接收单条自由流通股本数据。
     * 处理逻辑：按请求 ID 找到查询上下文，把接口返回值转换成原始时间点暂存。
     */
    @Override
    public void onFreeFloatSharesData(CQCVDFreeFloatSharesDataField freeFloatSharesData, int requestId) {
        FreeFloatSharesRequestContext context = freeFloatSharesRequestContextMap.get(requestId);
        if (context == null) {
            return;
        }

        LocalDate changeDate = parseApiDate(freeFloatSharesData.getChangeDateEX());
        if (changeDate == null) {
            return;
        }

        FreeFloatSharePoint point = new FreeFloatSharePoint();
        point.setStockCode(freeFloatSharesData.getSecurityID());
        point.setFreeSharesTenThousand(BigDecimal.valueOf(freeFloatSharesData.getFreeShares()));
        point.setChangeDate(changeDate);
        point.setAnnouncementDate(parseNullableApiDate(freeFloatSharesData.getAnnouncementDate()));
        context.points.add(point);
    }

    /**
     * 接收自由流通股本分页结束事件。
     * 处理逻辑：本页结束后把缓存数据压缩成区间并落库；当前测试阶段不自动翻页。
     */
    @Override
    public void onFreeFloatSharesPageEnd(CQCVDRspInfoField rspInfo, int requestId, boolean pageLast, boolean totalLast) {
        FreeFloatSharesRequestContext context = freeFloatSharesRequestContextMap.remove(requestId);
        if (context == null) {
            return;
        }

        if (rspInfo != null && rspInfo.getErrorID() != 0) {
            log.warn("自由流通股本入库失败 stockCode={} page={} requestId={} ErrorID={} ErrorMsg={}",
                    context.stockCode, context.page, requestId, rspInfo.getErrorID(), rspInfo.getErrorMsg());
            if (context.fromTask) {
                freeFloatTaskRunning.set(false);
            }
            if (context.fromPreOpenTask) {
                stopPreOpenTask("自由流通股本同步失败");
            } else if (context.dailyUpdate) {
                log.warn("每日自由流通股本同步失败 page={}", context.page);
            }
            return;
        }

        if (!totalLast) {
            if (context.dailyUpdate) {
                int changeCount = stockFreeFloatShareHistoryService.mergeChangePoints(context.points);
                log.info("每日自由流通股本同步完成一页 page={} requestId={} rawRows={} changeRows={} totalLast={}",
                        context.page, requestId, context.points.size(), changeCount, totalLast);
                syncDailyFreeFloatShares(context.queryDate, context.page + 1, context.fromPreOpenTask);
            } else {
                log.warn("自由流通股本入库返回未结束 stockCode={} page={} requestId={} rawRows={}，未落库也未标记完成，请调大 PageCount 或补充翻页逻辑",
                        context.stockCode, context.page, requestId, context.points.size());
                if (context.fromTask) {
                    freeFloatTaskRunning.set(false);
                }
            }
            return;
        }

        if (context.dailyUpdate) {
            try {
                int changeCount = stockFreeFloatShareHistoryService.mergeChangePoints(context.points);
                log.info("每日自由流通股本同步完成 page={} requestId={} rawRows={} changeRows={} totalLast={}",
                        context.page, requestId, context.points.size(), changeCount, totalLast);
            } catch (RuntimeException e) {
                log.error("每日自由流通股本同步异常 page={} requestId={} rawRows={}",
                        context.page, requestId, context.points.size(), e);
                if (context.fromPreOpenTask) {
                    stopPreOpenTask("每日自由流通股本同步异常");
                }
                return;
            }
            if (context.fromPreOpenTask) {
                stockDailyUpdateTaskService.markFreeFloatSynced(preOpenTaskDate);
                syncConvertibleBondStatus(1, true);
            }
            return;
        }

        try {
            int saveCount = stockFreeFloatShareHistoryService.replaceStockHistory(context.stockCode, context.points);
            if (context.fromTask) {
                stockSyncTaskService.markFreeFloatSynced(context.stockCode);
            }
            log.info("自由流通股本入库完成 stockCode={} page={} requestId={} rawRows={} historyRows={} totalLast={}",
                    context.stockCode, context.page, requestId, context.points.size(), saveCount, totalLast);
        } catch (RuntimeException e) {
            log.error("自由流通股本入库异常 stockCode={} page={} requestId={} rawRows={}，任务已暂停",
                    context.stockCode, context.page, requestId, context.points.size(), e);
            if (context.fromTask) {
                freeFloatTaskRunning.set(false);
            }
            return;
        }

        if (context.fromTask) {
            queryNextFreeFloatShareTask();
        }
    }

    /**
     * 查询指定交易日全市场日 K。
     * 处理逻辑：不设置 SecurityID，只按日期和页码查询全市场日 K，PageCount 固定 1000。
     */
    private void queryMarketStockDaily(LocalDate tradeDate, int page) {
        CQCVDReqQryStockDayQuotationField request = new CQCVDReqQryStockDayQuotationField();
        String queryDate = formatApiDate(tradeDate);
        request.setOrderType(qcvalueaddproapiConstants.QCVD_ORDST_ASC);
        request.setBegDate(queryDate);
        request.setEndDate(queryDate);
        request.setPageCount(1000);
        request.setPageLocate(page);

        int currentRequestId = nextRequestId();
        stockDayQuotationRequestContextMap.put(currentRequestId, StockDayQuotationRequestContext.marketDaily(tradeDate, page));
        int ret = basicDataApi.ReqReqQryStockDayQuotation(request, currentRequestId);
        log.info("发起盘后全市场日K查询 tradeDate={} page={} requestId={} ret={} 说明=不设置SecurityID查询全市场", queryDate, page, currentRequestId, ret);
        if (ret != 0) {
            stockDayQuotationRequestContextMap.remove(currentRequestId);
            postCloseTaskRunning.set(false);
            log.warn("盘后全市场日K查询发起失败 tradeDate={} page={} ret={}", queryDate, page, ret);
        }
    }

    /**
     * 接收单条股票日 K 数据。
     * 处理逻辑：按请求 ID 找到查询上下文，暂存原始日 K 数据。
     */
    @Override
    public void onStockDayQuotationData(CQCVDStockDayQuotationField stockDayQuotation, int requestId) {
        StockDayQuotationRequestContext context = stockDayQuotationRequestContextMap.get(requestId);
        if (context == null) {
            return;
        }
        context.quotations.add(copyStockDayQuotation(stockDayQuotation));
    }

    /**
     * 接收股票日 K 分页结束事件。
     * 处理逻辑：本页结束后批量构建并保存 stock_daily；来自任务表时标记完成并继续下一只。
     */
    @Override
    public void onStockDayQuotationPageEnd(CQCVDRspInfoField rspInfo, int requestId, boolean pageLast, boolean totalLast) {
        StockDayQuotationRequestContext context = stockDayQuotationRequestContextMap.remove(requestId);
        if (context == null) {
            return;
        }

        if (rspInfo != null && rspInfo.getErrorID() != 0) {
            log.warn("股票日K入库失败 stockCode={} page={} requestId={} ErrorID={} ErrorMsg={}",
                    context.stockCode, context.page, requestId, rspInfo.getErrorID(), rspInfo.getErrorMsg());
            if (context.fromTask) {
                stockDailyTaskRunning.set(false);
            }
            if (context.marketDaily) {
                postCloseTaskRunning.set(false);
            }
            return;
        }

        if (context.marketDaily) {
            try {
                int saveCount = stockDailyService.saveMarketDaily(context.tradeDate, context.quotations);
                log.info("盘后全市场日K入库完成 tradeDate={} page={} requestId={} rawRows={} saveRows={} totalLast={}",
                        context.tradeDate, context.page, requestId, context.quotations.size(), saveCount, totalLast);
            } catch (RuntimeException e) {
                postCloseTaskRunning.set(false);
                log.error("盘后全市场日K入库异常 tradeDate={} page={} requestId={} rows={}",
                        context.tradeDate, context.page, requestId, context.quotations.size(), e);
                return;
            }

            if (totalLast) {
                finishPostCloseDailyKlineTaskIfNeeded();
            } else {
                queryMarketStockDaily(context.tradeDate, context.page + 1);
            }
            return;
        }

        if (!totalLast) {
            log.warn("股票日K入库返回未结束 stockCode={} page={} requestId={} rows={}，未落库也未标记完成，请调大 PageCount 或补充翻页逻辑",
                    context.stockCode, context.page, requestId, context.quotations.size());
            if (context.fromTask) {
                stockDailyTaskRunning.set(false);
            }
            return;
        }

        try {
            int saveCount = stockDailyService.replaceStockDaily(context.stockCode, context.quotations);
            if (context.fromTask) {
                stockSyncTaskService.markDailyKlineSynced(context.stockCode);
            }
            log.info("股票日K入库完成 stockCode={} page={} requestId={} rows={} totalLast={}",
                    context.stockCode, context.page, requestId, saveCount, totalLast);
        } catch (RuntimeException e) {
            log.error("股票日K入库异常 stockCode={} page={} requestId={} rows={}，任务已暂停",
                    context.stockCode, context.page, requestId, context.quotations.size(), e);
            if (context.fromTask) {
                stockDailyTaskRunning.set(false);
            }
            return;
        }

        if (context.fromTask) {
            queryNextStockDailyTask();
        }
    }

    /**
     * 接收单条 A 股发行信息。
     * 处理逻辑：按请求 ID 找到查询上下文，暂存上市股票信息。
     */
    @Override
    public void onShareIssuanceData(CQCVDShareIssuanceField shareIssuance, int requestId) {
        ShareIssuanceRequestContext context = shareIssuanceRequestContextMap.get(requestId);
        if (context == null) {
            return;
        }

        LocalDate listDate = parseApiDate(shareIssuance.getListDate());
        if (listDate == null || !listDate.equals(context.listDate)) {
            return;
        }

        ShareIssuancePoint point = new ShareIssuancePoint();
        point.setStockCode(shareIssuance.getSecurityID());
        point.setStockName(shareIssuance.getSecurityName());
        point.setListDate(listDate);
        point.setListBoardName(shareIssuance.getListBoardName());
        context.points.add(point);
    }

    /**
     * 接收 A 股发行信息分页结束事件。
     * 处理逻辑：把新上市股票补充到同步任务表；曾用名历史不在这里维护。
     */
    @Override
    public void onShareIssuancePageEnd(CQCVDRspInfoField rspInfo, int requestId, boolean pageLast, boolean totalLast) {
        ShareIssuanceRequestContext context = shareIssuanceRequestContextMap.remove(requestId);
        if (context == null) {
            return;
        }

        if (rspInfo != null && rspInfo.getErrorID() != 0) {
            log.warn("新上市股票发现失败 listDate={} page={} requestId={} ErrorID={} ErrorMsg={}",
                    context.listDate, context.page, requestId, rspInfo.getErrorID(), rspInfo.getErrorMsg());
            if (context.fromPreOpenTask) {
                stopPreOpenTask("新上市股票发现失败");
            }
            return;
        }

        int newTaskCount = 0;
        ArrayList<ASharePreviousNamePoint> newNamePoints = new ArrayList<>();
        for (ShareIssuancePoint point : context.points) {
            if (point.getStockCode() == null || point.getStockCode().isBlank()) {
                continue;
            }
            boolean marginTrading = !isStName(point.getStockName());
            stockCurrentStatusService.ensureStatus(point.getStockCode(), marginTrading);
            ASharePreviousNamePoint namePoint = new ASharePreviousNamePoint();
            namePoint.setStockCode(point.getStockCode());
            namePoint.setStockName(point.getStockName());
            namePoint.setStartDate(point.getListDate());
            newNamePoints.add(namePoint);
            boolean created = stockSyncTaskService.ensureTask(point.getStockCode(), false, false);
            if (created) {
                newTaskCount++;
                log.info("发现新上市股票 stockCode={} stockName={} listDate={} board={}",
                        point.getStockCode(), point.getStockName(), point.getListDate(), point.getListBoardName());
            }
        }
        int newNameCount = stockPreviousNameHistoryService.mergeDailyNameChanges(newNamePoints);

        log.info("新上市股票发现完成 listDate={} page={} requestId={} rows={} newTasks={} newNames={} totalLast={}",
                context.listDate, context.page, requestId, context.points.size(), newTaskCount, newNameCount, totalLast);
        if (!totalLast) {
            log.warn("新上市股票发现返回未结束 listDate={} page={} requestId={}，请检查 PageCount={} 是否不足",
                    context.listDate, context.page, requestId, PAGE_COUNT);
        }
        if (context.fromPreOpenTask) {
            stockDailyUpdateTaskService.markNewListingSynced(context.listDate);
            syncDailyFreeFloatShares(context.listDate, 1, true);
        }
    }

    /**
     * 接收单条 A 股曾用名数据。
     * 处理逻辑：按请求 ID 找到查询上下文，只缓存开始日期等于本次查询日期的记录。
     */
    @Override
    public void onASharePreviousNameData(CQCVDASharePreviousNameField previousName, int requestId) {
        ASharePreviousNameRequestContext context = aSharePreviousNameRequestContextMap.get(requestId);
        if (context == null) {
            return;
        }

        LocalDate startDate = parseApiDate(previousName.getBeginDate());
        if (startDate == null || !startDate.equals(context.beginDate)) {
            return;
        }

        ASharePreviousNamePoint point = new ASharePreviousNamePoint();
        point.setStockCode(previousName.getSecurityID());
        point.setStockName(previousName.getSecurityName());
        point.setStartDate(startDate);
        point.setSourceEndDate(parseNullableApiDate(previousName.getEndDate()));
        point.setAnnouncementDate(parseNullableApiDate(previousName.getANNDate()));
        point.setChangeReason(previousName.getChangeReason());
        context.points.add(point);
    }

    /**
     * 接收 A 股曾用名分页结束事件。
     * 处理逻辑：把当日生效的新名称合并到曾用名历史表，并为首次出现的新股补充同步任务。
     */
    @Override
    public void onASharePreviousNamePageEnd(CQCVDRspInfoField rspInfo, int requestId, boolean pageLast, boolean totalLast) {
        ASharePreviousNameRequestContext context = aSharePreviousNameRequestContextMap.remove(requestId);
        if (context == null) {
            return;
        }

        if (rspInfo != null && rspInfo.getErrorID() != 0) {
            log.warn("每日曾用名同步失败 beginDate={} page={} requestId={} ErrorID={} ErrorMsg={}",
                    context.beginDate, context.page, requestId, rspInfo.getErrorID(), rspInfo.getErrorMsg());
            if (context.fromPreOpenTask) {
                stopPreOpenTask("每日曾用名同步失败");
            }
            return;
        }

        int insertCount = stockPreviousNameHistoryService.mergeDailyNameChanges(context.points);
        int disableMarginCount = 0;
        for (ASharePreviousNamePoint point : context.points) {
            if (isStName(point.getStockName())) {
                if (stockCurrentStatusService.disableMarginTrading(point.getStockCode())) {
                    disableMarginCount++;
                }
            }
        }
        log.info("每日曾用名同步完成 beginDate={} page={} requestId={} rows={} inserts={} totalLast={}",
                context.beginDate, context.page, requestId, context.points.size(), insertCount, totalLast);
        log.info("每日曾用名融资融券标识维护完成 beginDate={} disableMarginCount={} 说明=戴帽关闭，摘帽不恢复",
                context.beginDate, disableMarginCount);
        if (!totalLast) {
            log.warn("每日曾用名同步返回未结束 beginDate={} page={} requestId={}，请检查 PageCount={} 是否不足",
                    context.beginDate, context.page, requestId, PAGE_COUNT);
        }
        if (context.fromPreOpenTask) {
            stockDailyUpdateTaskService.markPreviousNameSynced(context.beginDate);
            discoverNewListedStocks(context.beginDate, 1, true);
        }
    }

    /**
     * 接收单条可转债发行信息。
     * 处理逻辑：发行接口只保存正股和转债关系；当前是否有可交易转债，需要等基本资料补全上市日期后再按历史表刷新。
     */
    @Override
    public void onBondIssuanceData(CQCVDBondIssuanceField bondIssuance, int requestId) {
        BondIssuanceRequestContext context = bondIssuanceRequestContextMap.get(requestId);
        if (context == null) {
            return;
        }

        if (bondIssuance.getStockID() == null || bondIssuance.getStockID().isBlank()) {
            return;
        }

        ConvertibleBondIssuancePoint point = copyBondIssuance(bondIssuance);
        if (stockConvertibleBondHistoryService.saveOrUpdateIssuance(point)) {
            context.savedRelations++;
        }

        if (context.queryDescriptionAfterEnd && "000301".equals(bondIssuance.getStockID())) {
            context.bondCodes.add(bondIssuance.getSecurityID());
        }
    }

    /**
     * 接收可转债发行信息分页结束事件。
     * 处理逻辑：发行关系落库后，从历史表按已上市且未退市口径刷新当前状态表可转债标识。
     */
    @Override
    public void onBondIssuancePageEnd(CQCVDRspInfoField rspInfo, int requestId, boolean pageLast, boolean totalLast) {
        BondIssuanceRequestContext context = bondIssuanceRequestContextMap.remove(requestId);
        if (context == null) {
            return;
        }

        if (rspInfo != null && rspInfo.getErrorID() != 0) {
            log.warn("同步当前可转债标识失败 page={} requestId={} ErrorID={} ErrorMsg={}",
                    context.page, requestId, rspInfo.getErrorID(), rspInfo.getErrorMsg());
            if (context.fromPreOpenTask) {
                stopPreOpenTask("同步当前可转债标识失败");
            }
            return;
        }

        if (context.queryDescriptionAfterEnd) {
            log.info("调试000301转债发行信息完成 requestId={} bondCodes={}", requestId, context.bondCodes);
            for (String bondCode : context.bondCodes) {
                queryCBondDescription(bondCode);
            }
        } else {
            log.info("同步可转债发行关系完成 page={} requestId={} savedRelations={} totalLast={} 说明=当前状态将在基本资料补全完成后刷新",
                    context.page, requestId, context.savedRelations, totalLast);
            if (context.fromPreOpenTask) {
                startConvertibleBondDescriptionFillTasks(true);
            }
        }
        if (!totalLast) {
            log.warn("同步当前可转债标识返回未结束 page={} requestId={}，请检查 PageCount=5000 是否不足", context.page, requestId);
        }
    }

    /**
     * 接收单条可转债基本资料。
     * 处理逻辑：本阶段只观察日志，实际字段已经由 SPI 完整打印。
     */
    @Override
    public void onCBondDescriptionData(CQCVDCBondDescriptionField description, int requestId) {
        CBondDescriptionRequestContext context = cBondDescriptionRequestContextMap.get(requestId);
        if (context == null) {
            return;
        }
        context.rows++;
        stockConvertibleBondHistoryService.fillDescription(copyCBondDescription(description));
    }

    /**
     * 接收可转债基本资料查询结束事件。
     * 处理逻辑：打印本次查询收到的记录数。
     */
    @Override
    public void onCBondDescriptionEnd(CQCVDRspInfoField rspInfo, int requestId, boolean last) {
        CBondDescriptionRequestContext context = cBondDescriptionRequestContextMap.remove(requestId);
        if (context == null) {
            return;
        }

        if (rspInfo != null && rspInfo.getErrorID() != 0) {
            log.warn("查询可转债基本资料失败 bondCode={} requestId={} ErrorID={} ErrorMsg={}",
                    context.bondCode, requestId, rspInfo.getErrorID(), rspInfo.getErrorMsg());
            if (context.fromPreOpenTask) {
                stopPreOpenTask("查询可转债基本资料失败");
            }
            return;
        }

        log.info("查询可转债基本资料完成 bondCode={} requestId={} rows={} last={}",
                context.bondCode, requestId, context.rows, last);
        if (context.fromTask) {
            queryNextConvertibleBondDescriptionTask(context.fromPreOpenTask);
        }
    }

    /**
     * 接收单条地域属性。
     * 处理逻辑：保存地域名称到当前状态表，地域代码不入库。
     */
    @Override
    public void onRegionInfoData(CQCVDRegionDataField regionData, int requestId) {
        RegionInfoRequestContext context = regionInfoRequestContextMap.get(requestId);
        if (context == null) {
            return;
        }

        if (regionData.getSecurityID() == null || regionData.getIndustriesName() == null || regionData.getIndustriesName().isBlank()) {
            return;
        }

        context.stockCode = regionData.getSecurityID();
        context.regionName = regionData.getIndustriesName();
    }

    /**
     * 接收地域属性分页结束事件。
     * 处理逻辑：当前页拿到地域名称时更新当前状态表。
     */
    @Override
    public void onRegionInfoPageEnd(CQCVDRspInfoField rspInfo, int requestId, boolean pageLast, boolean totalLast) {
        RegionInfoRequestContext context = regionInfoRequestContextMap.remove(requestId);
        if (context == null) {
            return;
        }

        if (rspInfo != null && rspInfo.getErrorID() != 0) {
            log.warn("同步地域属性失败 stockCode={} page={} requestId={} ErrorID={} ErrorMsg={}",
                    context.stockCode, context.page, requestId, rspInfo.getErrorID(), rspInfo.getErrorMsg());
            if (context.fromTask) {
                queryNextRegionInfoTask(context.fromPreOpenTask);
            }
            return;
        }

        if (context.regionName == null || context.regionName.isBlank()) {
            log.warn("同步地域属性完成但未收到地域名称 stockCode={} page={} requestId={} totalLast={}",
                    context.stockCode, context.page, requestId, totalLast);
            if (context.fromTask) {
                queryNextRegionInfoTask(context.fromPreOpenTask);
            }
            return;
        }

        boolean updated = stockCurrentStatusService.updateRegionName(context.stockCode, context.regionName);
        if (updated && context.fromTask) {
            regionInfoTaskSuccess++;
        }
        log.info("同步地域属性完成 stockCode={} regionName={} page={} requestId={} updated={} totalLast={}",
                context.stockCode, context.regionName, context.page, requestId, updated, totalLast);
        if (context.fromTask) {
            queryNextRegionInfoTask(context.fromPreOpenTask);
        }
    }

    /**
     * 复制股票日 K 回调数据。
     * 作用：把 SDK 原生对象转换成普通 Java 对象，后续排序和落库不再依赖回调对象生命周期。
     */
    private StockDayQuotationPoint copyStockDayQuotation(CQCVDStockDayQuotationField stockDayQuotation) {
        StockDayQuotationPoint point = new StockDayQuotationPoint();
        point.setStockCode(stockDayQuotation.getSecurityID());
        point.setTradingDay(stockDayQuotation.getTradingDay());
        point.setLimitPrice(stockDayQuotation.getLimitPrice());
        point.setStoppingPrice(stockDayQuotation.getStoppingPrice());
        point.setOpenPrice(stockDayQuotation.getOpenPrice());
        point.setHighPrice(stockDayQuotation.getHighPrice());
        point.setLowPrice(stockDayQuotation.getLowPrice());
        point.setClosePrice(stockDayQuotation.getClosePrice());
        point.setPreClosePrice(stockDayQuotation.getPreClosePrice());
        point.setAdjustPreClosePrice(stockDayQuotation.getAdjustPreClosePrice());
        point.setAdjustOpenPrice(stockDayQuotation.getAdjustOpenPrice());
        point.setAdjustHighPrice(stockDayQuotation.getAdjustHighPrice());
        point.setAdjustLowPrice(stockDayQuotation.getAdjustLowPrice());
        point.setAdjustClosePrice(stockDayQuotation.getAdjustClosePrice());
        point.setAdjustFactor(stockDayQuotation.getAdjustFactor());
        point.setVolume(stockDayQuotation.getVolume());
        point.setTurnover(stockDayQuotation.getTurnover());
        point.setPercentChange(stockDayQuotation.getPercentChange());
        return point;
    }

    /**
     * 复制可转债发行关系数据。
     * 作用：把发行接口中的正股、转债和市场关系转换为落库 DTO。
     */
    private ConvertibleBondIssuancePoint copyBondIssuance(CQCVDBondIssuanceField bondIssuance) {
        ConvertibleBondIssuancePoint point = new ConvertibleBondIssuancePoint();
        point.setStockCode(bondIssuance.getStockID());
        point.setBondCode(bondIssuance.getSecurityID());
        point.setMarket(convertExchangeIdToMarket(bondIssuance.getExchangeID()));
        point.setBondName(bondIssuance.getSecurityName());
        point.setListDate(parseNullableApiDate(bondIssuance.getListDate()));
        return point;
    }

    /**
     * 复制可转债基本资料数据。
     * 作用：把基本资料接口中的上市、退市、到期和余额字段转换为落库 DTO。
     */
    private ConvertibleBondDescriptionPoint copyCBondDescription(CQCVDCBondDescriptionField description) {
        ConvertibleBondDescriptionPoint point = new ConvertibleBondDescriptionPoint();
        point.setBondCode(description.getSecurityID());
        point.setMarket(description.getWndExchMarketID());
        point.setBondName(description.getS_INFO_NAME());
        LocalDate maturityDate = parseNullableApiDate(description.getB_INFO_MATURITYDATE());
        LocalDate delistDate = parseNullableApiDate(description.getB_INFO_DELISTDATE());
        point.setStartDate(parseNullableApiDate(description.getB_INFO_LISTDATE()));
        point.setEndDate(delistDate != null ? delistDate : maturityDate);
        point.setMaturityDate(maturityDate);
        point.setFailure(description.getIS_FAILURE());
        point.setOutstandingBalance(description.getOutstandingBalance());
        return point;
    }

    /**
     * 从可转债历史表刷新当前可交易转债标识。
     * 作用：只把已经上市且尚未退市的转债正股设置为有转债，未上市转债不计入当前状态。
     */
    private int refreshConvertibleBondFlagsFromHistory(String scene) {
        return refreshConvertibleBondFlagsFromHistory(scene, LocalDate.now());
    }

    /**
     * 从可转债历史表刷新当前可交易转债标识。
     * 作用：按指定日期判断转债是否已经上市且尚未退市。
     */
    private int refreshConvertibleBondFlagsFromHistory(String scene, LocalDate tradeDate) {
        Set<String> tradableStockCodes = stockConvertibleBondHistoryService.listTradableStockCodes(tradeDate);
        int updateCount = stockCurrentStatusService.refreshConvertibleBondFlags(tradableStockCodes);
        log.info("{} 刷新可交易转债标识 tradeDate={} tradableStockCount={} updateCount={}",
                scene, tradeDate, tradableStockCodes.size(), updateCount);
        return updateCount;
    }

    /**
     * 启动盘前地域信息同步步骤。
     * 作用：可转债状态刷新完成后，继续同步地域信息。
     */
    private void startPreOpenRegionStep() {
        startAllRegionInfoSyncTasks(true);
    }

    /**
     * 停止盘前每日任务。
     * 作用：某个关键步骤失败时释放运行标识，后续可手动补跑。
     */
    private void stopPreOpenTask(String reason) {
        preOpenTaskRunning.set(false);
        log.warn("盘前每日更新停止 taskDate={} reason={}", preOpenTaskDate, reason);
    }

    /**
     * 完成盘后日 K 任务记录。
     * 作用：日 K 队列全部完成后，更新每日任务表标识。
     */
    private void finishPostCloseDailyKlineTaskIfNeeded() {
        if (!postCloseTaskRunning.get()) {
            return;
        }
        stockDailyUpdateTaskService.markDailyKlineSynced(postCloseTaskDate);
        stockDailyUpdateTaskService.markPostCloseFinished(postCloseTaskDate);
        postCloseTaskRunning.set(false);
        log.info("盘后每日更新完成 taskDate={}", postCloseTaskDate);
    }

    /**
     * 转换华鑫交易所编号为基本资料接口市场代码。
     * 作用：用发行接口 ExchangeID 和基本资料接口 WndExchMarketID 建立同一市场口径。
     */
    private String convertExchangeIdToMarket(char exchangeId) {
        if (exchangeId == '1') {
            return "SSE";
        }
        if (exchangeId == '2') {
            return "SZSE";
        }
        return String.valueOf(exchangeId);
    }

    /**
     * 标记华鑫增值服务已登录。
     * 作用：登录回调成功后释放等待中的查询入口。
     */
    private void markLoginReady() {
        if (loginReady.compareAndSet(false, true)) {
            loginReadyLatch.countDown();
            log.info("增值服务登录状态已就绪，可以开始查询基础数据");
        }
    }

    /**
     * 判断华鑫增值服务是否已登录。
     * 作用：所有查询接口发起前统一检查登录状态。
     */
    private boolean isLoginReady(String queryName) {
        if (loginReady.get()) {
            return true;
        }

        log.warn("{} 未发起：增值服务尚未登录成功，请等待 OnRspUserLogin 返回 ErrorID=0", queryName);
        return false;
    }

    /**
     * 生成华鑫查询请求 ID。
     * 作用：给异步请求和回调建立唯一关联。
     */
    private synchronized int nextRequestId() {
        return requestId++;
    }

    /**
     * 获取今天日期。
     * 返回格式：yyyyMMdd，适配华鑫查询接口的日期字段。
     */
    private String today() {
        return LocalDateTimeUtil.format(LocalDate.now(), "yyyyMMdd");
    }

    /**
     * 格式化华鑫接口日期。
     */
    private String formatApiDate(LocalDate date) {
        return date.format(API_DATE_FORMATTER);
    }

    /**
     * 解析华鑫接口日期。
     * 入参格式：yyyyMMdd；解析失败时返回 null 并记录警告日志。
     */
    private LocalDate parseApiDate(String apiDate) {
        if (apiDate == null || apiDate.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(apiDate, API_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("解析华鑫接口日期失败 apiDate={}", apiDate);
            return null;
        }
    }

    /**
     * 解析可为空的华鑫接口日期。
     * 作用：公告日期等非必填字段为空时直接返回 null。
     */
    private LocalDate parseNullableApiDate(String apiDate) {
        if (apiDate == null || apiDate.isBlank()) {
            return null;
        }
        return parseApiDate(apiDate);
    }

    /**
     * 判断股票名称是否为 ST 名称。
     * 作用：盘前维护融资融券标识时，戴帽关闭融资融券，摘帽不自动恢复。
     */
    private boolean isStName(String stockName) {
        return stockName != null && stockName.toUpperCase().contains("ST");
    }

    /**
     * 判断可转债发行记录当前是否有效。
     * 规则：发行状态正常、正股代码存在、转债名称不包含退市。
     */
    private boolean isValidConvertibleBond(CQCVDBondIssuanceField bondIssuance) {
        if (bondIssuance == null) {
            return false;
        }
        if (bondIssuance.getIsFailure() != '0') {
            return false;
        }
        if (bondIssuance.getStockID() == null || bondIssuance.getStockID().isBlank()) {
            return false;
        }
        return bondIssuance.getSecurityName() == null || !bondIssuance.getSecurityName().contains("退市");
    }

    /**
     * A 股交易日历年度同步请求上下文。
     * 作用：绑定请求 ID、年份、页码和当前页收到的交易日期。
     */
    private static class ShareCalendarRequestContext {

        /**
         * 查询年份。
         */
        private final int year;

        /**
         * 查询页码。
         */
        private final int page;

        /**
         * 当前页交易日期缓存。
         */
        private final ArrayList<LocalDate> tradeDates = new ArrayList<>();

        /**
         * 创建交易日历请求上下文。
         */
        private ShareCalendarRequestContext(int year, int page) {
            this.year = year;
            this.page = page;
        }
    }

    /**
     * 自由流通股本查询请求上下文。
     * 作用：绑定请求 ID、股票代码、页码和当前页收到的自由流通股本原始点。
     */
    private static class FreeFloatSharesRequestContext {

        /**
         * 股票代码。
         */
        private final String stockCode;

        /**
         * 查询页码。
         */
        private final int page;

        /**
         * 是否来自任务表全量同步。
         */
        private final boolean fromTask;

        /**
         * 是否为每日增量同步。
         */
        private final boolean dailyUpdate;

        /**
         * 是否来自盘前每日任务。
         */
        private final boolean fromPreOpenTask;

        /**
         * 每日同步查询日期。
         */
        private final LocalDate queryDate;

        /**
         * 当前页自由流通股本原始点缓存。
         */
        private final ArrayList<FreeFloatSharePoint> points = new ArrayList<>();

        /**
         * 创建自由流通股本查询上下文。
         */
        private FreeFloatSharesRequestContext(String stockCode, int page, boolean fromTask, boolean dailyUpdate) {
            this(stockCode, page, fromTask, dailyUpdate, false, null);
        }

        /**
         * 创建自由流通股本查询上下文。
         */
        private FreeFloatSharesRequestContext(String stockCode, int page, boolean fromTask, boolean dailyUpdate, boolean fromPreOpenTask) {
            this(stockCode, page, fromTask, dailyUpdate, fromPreOpenTask, null);
        }

        /**
         * 创建自由流通股本查询上下文。
         */
        private FreeFloatSharesRequestContext(String stockCode, int page, boolean fromTask, boolean dailyUpdate, boolean fromPreOpenTask, LocalDate queryDate) {
            this.stockCode = stockCode;
            this.page = page;
            this.fromTask = fromTask;
            this.dailyUpdate = dailyUpdate;
            this.fromPreOpenTask = fromPreOpenTask;
            this.queryDate = queryDate;
        }
    }

    /**
     * 新上市股票发现请求上下文。
     * 作用：绑定请求 ID、上市日期、页码和当前页发行上市信息。
     */
    private static class ShareIssuanceRequestContext {

        /**
         * 查询上市日期。
         */
        private final LocalDate listDate;

        /**
         * 查询页码。
         */
        private final int page;

        /**
         * 当前页发行上市信息缓存。
         */
        private final ArrayList<ShareIssuancePoint> points = new ArrayList<>();

        /**
         * 是否来自盘前每日任务。
         */
        private final boolean fromPreOpenTask;

        /**
         * 创建新上市股票发现上下文。
         */
        private ShareIssuanceRequestContext(LocalDate listDate, int page) {
            this(listDate, page, false);
        }

        /**
         * 创建新上市股票发现上下文。
         */
        private ShareIssuanceRequestContext(LocalDate listDate, int page, boolean fromPreOpenTask) {
            this.listDate = listDate;
            this.page = page;
            this.fromPreOpenTask = fromPreOpenTask;
        }
    }

    /**
     * A 股曾用名每日同步请求上下文。
     * 作用：绑定请求 ID、查询生效日期、页码和当前页曾用名变更点。
     */
    private static class ASharePreviousNameRequestContext {

        /**
         * 查询生效日期。
         */
        private final LocalDate beginDate;

        /**
         * 查询页码。
         */
        private final int page;

        /**
         * 当前页曾用名变更点缓存。
         */
        private final ArrayList<ASharePreviousNamePoint> points = new ArrayList<>();

        /**
         * 是否来自盘前每日任务。
         */
        private final boolean fromPreOpenTask;

        /**
         * 创建 A 股曾用名每日同步上下文。
         */
        private ASharePreviousNameRequestContext(LocalDate beginDate, int page) {
            this(beginDate, page, false);
        }

        /**
         * 创建 A 股曾用名每日同步上下文。
         */
        private ASharePreviousNameRequestContext(LocalDate beginDate, int page, boolean fromPreOpenTask) {
            this.beginDate = beginDate;
            this.page = page;
            this.fromPreOpenTask = fromPreOpenTask;
        }
    }

    /**
     * 可转债发行信息同步请求上下文。
     * 作用：绑定请求 ID、页码和本页保存的可转债发行关系数量。
     */
    private static class BondIssuanceRequestContext {

        /**
         * 查询页码。
         */
        private final int page;

        /**
         * 是否在发行信息结束后继续查询可转债基本资料。
         */
        private final boolean queryDescriptionAfterEnd;

        /**
         * 是否来自盘前每日任务。
         */
        private final boolean fromPreOpenTask;

        /**
         * 已保存的转债和正股关系数量。
         */
        private int savedRelations;

        /**
         * 本次调试收集到的转债代码集合。
         */
        private final Set<String> bondCodes = new HashSet<>();

        /**
         * 创建可转债发行信息同步上下文。
         */
        private BondIssuanceRequestContext(int page, boolean queryDescriptionAfterEnd) {
            this(page, queryDescriptionAfterEnd, false);
        }

        /**
         * 创建可转债发行信息同步上下文。
         */
        private BondIssuanceRequestContext(int page, boolean queryDescriptionAfterEnd, boolean fromPreOpenTask) {
            this.page = page;
            this.queryDescriptionAfterEnd = queryDescriptionAfterEnd;
            this.fromPreOpenTask = fromPreOpenTask;
        }
    }

    /**
     * 可转债基本资料查询上下文。
     * 作用：绑定请求 ID、转债代码和收到的记录数。
     */
    private static class CBondDescriptionRequestContext {

        /**
         * 转债代码。
         */
        private final String bondCode;

        /**
         * 是否来自历史补全任务。
         */
        private final boolean fromTask;

        /**
         * 是否来自盘前每日任务。
         */
        private final boolean fromPreOpenTask;

        /**
         * 收到的记录数。
         */
        private int rows;

        /**
         * 创建可转债基本资料查询上下文。
         */
        private CBondDescriptionRequestContext(String bondCode, boolean fromTask) {
            this(bondCode, fromTask, false);
        }

        /**
         * 创建可转债基本资料查询上下文。
         */
        private CBondDescriptionRequestContext(String bondCode, boolean fromTask, boolean fromPreOpenTask) {
            this.bondCode = bondCode;
            this.fromTask = fromTask;
            this.fromPreOpenTask = fromPreOpenTask;
        }
    }

    /**
     * 地域属性同步请求上下文。
     * 作用：绑定请求 ID、股票代码、页码和回调收到的地域名称。
     */
    private static class RegionInfoRequestContext {

        /**
         * 股票代码。
         */
        private String stockCode;

        /**
         * 查询页码。
         */
        private final int page;

        /**
         * 地域名称。
         */
        private String regionName;

        /**
         * 是否来自全量地域同步任务。
         */
        private final boolean fromTask;

        /**
         * 是否来自盘前每日任务。
         */
        private final boolean fromPreOpenTask;

        /**
         * 创建地域属性同步上下文。
         */
        private RegionInfoRequestContext(String stockCode, int page) {
            this(stockCode, page, false, false);
        }

        /**
         * 创建地域属性同步上下文。
         */
        private RegionInfoRequestContext(String stockCode, int page, boolean fromTask) {
            this(stockCode, page, fromTask, false);
        }

        /**
         * 创建地域属性同步上下文。
         */
        private RegionInfoRequestContext(String stockCode, int page, boolean fromTask, boolean fromPreOpenTask) {
            this.stockCode = stockCode;
            this.page = page;
            this.fromTask = fromTask;
            this.fromPreOpenTask = fromPreOpenTask;
        }
    }

    /**
     * 股票日 K 查询请求上下文。
     * 作用：绑定请求 ID、股票代码、页码和当前页收到的股票日 K 原始数据。
     */
    private static class StockDayQuotationRequestContext {

        /**
         * 股票代码。
         */
        private final String stockCode;

        /**
         * 查询页码。
         */
        private final int page;

        /**
         * 是否来自任务表全量同步。
         */
        private final boolean fromTask;

        /**
         * 是否为盘后全市场每日更新。
         */
        private final boolean marketDaily;

        /**
         * 每日更新交易日期。
         */
        private final LocalDate tradeDate;

        /**
         * 当前页股票日 K 原始数据缓存。
         */
        private final ArrayList<StockDayQuotationPoint> quotations = new ArrayList<>();

        /**
         * 创建股票日 K 查询上下文。
         */
        private StockDayQuotationRequestContext(String stockCode, int page, boolean fromTask) {
            this.stockCode = stockCode;
            this.page = page;
            this.fromTask = fromTask;
            this.marketDaily = false;
            this.tradeDate = null;
        }

        /**
         * 创建盘后全市场每日更新上下文。
         */
        private static StockDayQuotationRequestContext marketDaily(LocalDate tradeDate, int page) {
            return new StockDayQuotationRequestContext(tradeDate, page);
        }

        /**
         * 创建盘后全市场每日更新上下文。
         */
        private StockDayQuotationRequestContext(LocalDate tradeDate, int page) {
            this.stockCode = null;
            this.page = page;
            this.fromTask = false;
            this.marketDaily = true;
            this.tradeDate = tradeDate;
        }
    }

}
