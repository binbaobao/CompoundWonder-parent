package com.compoundwonder.hxdata.api;



import cn.hutool.core.date.LocalDateTimeUtil;

import com.compoundwonder.hxdata.callback.FreeFloatSharesResponseHandler;
import com.compoundwonder.hxdata.callback.ShareCalendarResponseHandler;
import com.compoundwonder.hxdata.dto.FreeFloatSharePoint;
import com.compoundwonder.hxdata.entity.StockSyncTask;
import com.compoundwonder.hxdata.service.StockFreeFloatShareHistoryService;
import com.compoundwonder.hxdata.service.StockSyncTaskService;
import com.compoundwonder.hxdata.service.StockTradeCalendarService;
import com.compoundwonder.hxdata.spi.SimpleQrySpi;
import com.qcvalueaddproapi.CQCVDFreeFloatSharesDataField;
import com.qcvalueaddproapi.CQCVDRspInfoField;
import com.qcvalueaddproapi.CQCVDQryFreeFloatSharesInfoField;
import com.qcvalueaddproapi.CQCVDReqQryShareCalendarField;
import com.qcvalueaddproapi.CQCVDReqQryShareDescriptionField;
import com.qcvalueaddproapi.CQCVDReqQryShareIssuanceField;
import com.qcvalueaddproapi.CQCVDReqQryStockDayQuotationField;
import com.qcvalueaddproapi.CQCVDShareCalendarField;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基础数据API提供
 */
@Component
public class BasicDataApi implements ShareCalendarResponseHandler, FreeFloatSharesResponseHandler {

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
    private final StockFreeFloatShareHistoryService stockFreeFloatShareHistoryService;
    private final StockSyncTaskService stockSyncTaskService;
    private final AtomicBoolean loginReady = new AtomicBoolean(false);
    private final AtomicBoolean freeFloatTaskRunning = new AtomicBoolean(false);
    private final CountDownLatch loginReadyLatch = new CountDownLatch(1);
    private final Map<Integer, ShareCalendarRequestContext> shareCalendarRequestContextMap = new ConcurrentHashMap<>();
    private final Map<Integer, FreeFloatSharesRequestContext> freeFloatSharesRequestContextMap = new ConcurrentHashMap<>();

    /**
     * 创建基础数据 API 组件。
     * 作用：注入交易日历服务，供异步回调收到数据后直接落库。
     */
    public BasicDataApi(StockTradeCalendarService stockTradeCalendarService, StockFreeFloatShareHistoryService stockFreeFloatShareHistoryService, StockSyncTaskService stockSyncTaskService) {
        this.stockTradeCalendarService = stockTradeCalendarService;
        this.stockFreeFloatShareHistoryService = stockFreeFloatShareHistoryService;
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
            simpleQrySpi = new SimpleQrySpi(basicDataApi, this::markLoginReady, this, this);
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
        freeFloatSharesRequestContextMap.put(currentRequestId, new FreeFloatSharesRequestContext(stockCode, page, false));
        int ret = basicDataApi.ReqQryFreeFloatSharesInfo(request, currentRequestId);
        log.info("发起查询自由流通股本入库 stockCode={} page={} requestId={} ret={}", stockCode, page, currentRequestId, ret);
        if (ret != 0) {
            freeFloatSharesRequestContextMap.remove(currentRequestId);
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
        freeFloatSharesRequestContextMap.put(currentRequestId, new FreeFloatSharesRequestContext(stockCode, page, true));
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
            return;
        }

        int saveCount = stockFreeFloatShareHistoryService.replaceStockHistory(context.stockCode, context.points);
        if (context.fromTask) {
            stockSyncTaskService.markFreeFloatSynced(context.stockCode);
        }
        log.info("自由流通股本入库完成 stockCode={} page={} requestId={} rawRows={} historyRows={} totalLast={}",
                context.stockCode, context.page, requestId, context.points.size(), saveCount, totalLast);
        if (!totalLast) {
            log.warn("自由流通股本入库返回未结束 stockCode={} page={} requestId={}，当前测试阶段未自动翻页",
                    context.stockCode, context.page, requestId);
        }
        if (context.fromTask) {
            queryNextFreeFloatShareTask();
        }
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
     * 解析华鑫接口日期。
     * 入参格式：yyyyMMdd；解析失败时返回 null 并记录警告日志。
     */
    private LocalDate parseApiDate(String apiDate) {
        try {
            return LocalDate.parse(apiDate, API_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("解析交易日历日期失败 apiDate={}", apiDate, e);
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
         * 当前页自由流通股本原始点缓存。
         */
        private final ArrayList<FreeFloatSharePoint> points = new ArrayList<>();

        /**
         * 创建自由流通股本查询上下文。
         */
        private FreeFloatSharesRequestContext(String stockCode, int page, boolean fromTask) {
            this.stockCode = stockCode;
            this.page = page;
            this.fromTask = fromTask;
        }
    }

}
