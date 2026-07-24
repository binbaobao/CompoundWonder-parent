package com.compoundwonder.backtest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.compoundwonder.backtest.orderbook.data.clickhouse.ClickHouseBacktestTickDataSource;
import com.compoundwonder.backtest.orderbook.data.clickhouse.ClickHouseLevel2QueryService;
import com.compoundwonder.backtest.orderbook.data.clickhouse.ClickHouseMarketRow;
import com.compoundwonder.backtest.service.Level2MinuteBarService;
import com.compoundwonder.core.engine.TickData;
import com.compoundwonder.dto.Level2MinuteTickDTO;
import com.compoundwonder.hxdata.entity.StockDailyEntity;
import com.compoundwonder.hxdata.service.StockDailyService;
import com.compoundwonder.util.SymbolUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 使用 ClickHouse stock.market 十档快照构建前端 Level2 分时数据。 */
@Service
public class Level2MinuteBarServiceImpl implements Level2MinuteBarService {

    private static final ZoneId MARKET_ZONE = ZoneId.of("Asia/Shanghai");

    private final ClickHouseLevel2QueryService clickHouseQueryService;
    private final StockDailyService stockDailyService;

    public Level2MinuteBarServiceImpl(ClickHouseLevel2QueryService clickHouseQueryService,
                                      StockDailyService stockDailyService) {
        this.clickHouseQueryService = clickHouseQueryService;
        this.stockDailyService = stockDailyService;
    }

    /**
     * 查询指定股票在指定交易日的 Level2 分时快照。
     *
     * <p>集合竞价保留每个三秒快照；连续竞价每分钟只保留最后一条，字段编码与订单簿
     * 回放使用的 ClickHouse 快照转换完全一致。</p>
     */
    @Override
    public List<Level2MinuteTickDTO> findMinuteBars(String stockCode, LocalDate tradeDate) {
        int symbolId = SymbolUtil.fastSymbolToInt(stockCode);
        int handlerIndex = SymbolUtil.getHandlerIndex(symbolId);
        if (symbolId < 0 || handlerIndex < 0) {
            return List.of();
        }

        double limitUpPrice = findLimitUpPrice(stockCode, tradeDate, symbolId);
        List<Level2MinuteTickDTO> ticks = new ArrayList<>();
        Level2MinuteTickDTO pendingMinuteTick = null;
        String pendingMinute = null;

        for (ClickHouseMarketRow row : clickHouseQueryService
                .queryMarket(stockCode, tradeDate).rows()) {
            TickData snapshot = ClickHouseBacktestTickDataSource.toMarketTick(row, symbolId);
            if (snapshot.price == 0) {
                continue;
            }
            String tickTime = formatTradeTime(row.tradeTime());
            Level2MinuteTickDTO dto = toDto(row, snapshot, symbolId, handlerIndex,
                    tickTime, limitUpPrice);
            if (isAuctionTradeTime(tickTime)) {
                flushPendingMinuteTick(ticks, pendingMinuteTick);
                pendingMinuteTick = null;
                pendingMinute = null;
                ticks.add(dto);
            } else if (isContinuousTradeTime(tickTime)) {
                String minute = tickTime.substring(0, 5);
                if (pendingMinute != null && !pendingMinute.equals(minute)) {
                    ticks.add(pendingMinuteTick);
                }
                pendingMinute = minute;
                pendingMinuteTick = dto;
            }
        }
        flushPendingMinuteTick(ticks, pendingMinuteTick);
        return ticks;
    }

    private Level2MinuteTickDTO toDto(ClickHouseMarketRow row, TickData snapshot,
                                      int symbolId, int handlerIndex, String tickTime,
                                      double limitUpPrice) {
        double rawPrice = snapshot.price / 100D;
        // 分时展示沿用已确认口径：卖一/买一低于涨停价时补一分钱作为打板挂单价。
        double price = rawPrice < limitUpPrice ? roundPrice(rawPrice + 0.01D) : rawPrice;
        Level2MinuteTickDTO dto = new Level2MinuteTickDTO();
        dto.setTimestamp(displayTimestamp(row.tradeDate(), row.tradeTime()));
        dto.setTickTime(tickTime);
        dto.setDataType(400 + snapshot.type);
        dto.setRawTime(row.tradeTime());
        dto.setRawPrice(rawPrice);
        dto.setSellPrice(rawPrice);
        dto.setPrice(price);
        // 快照累计成交额大于 int 时按核心 TickData 口径除以 100，并以 dataType=401 标记。
        dto.setAmount((long) snapshot.orderId);
        dto.setQuantity((long) snapshot.quantity);
        dto.setBuyerOrderId((long) snapshot.buyerOrderId);
        dto.setSellerOrderId((long) snapshot.sellerOrderId);
        dto.setSymbolId(symbolId);
        dto.setHandlerIndex(handlerIndex);
        dto.setRawFields(rawFields(row));
        return dto;
    }

    private Map<String, Object> rawFields(ClickHouseMarketRow row) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("SecurityID", row.securityId());
        fields.put("ExchangeID", row.exchangeId());
        fields.put("TradeDate", row.tradeDate().toString());
        fields.put("TradeTime", row.tradeTime());
        fields.put("TradeTimeStamp", row.tradeTimeStamp().toString());
        fields.put("LastPrice", row.lastPrice());
        fields.put("PreClosePrice", row.preClosePrice());
        fields.put("OpenPrice", row.openPrice());
        fields.put("HighestPrice", row.highestPrice());
        fields.put("LowestPrice", row.lowestPrice());
        fields.put("IOPV", row.iopv());
        fields.put("AvgBidPrice", row.avgBidPrice());
        fields.put("AvgAskPrice", row.avgAskPrice());
        fields.put("TotalVolumeTrade", row.totalVolumeTrade());
        fields.put("TotalValueTrade", row.totalValueTrade());
        fields.put("TotalBidVolume", row.totalBidVolume());
        fields.put("TotalAskVolume", row.totalAskVolume());
        fields.put("NumTrades", row.numTrades());
        fields.put("AskPrices", row.askPrices());
        fields.put("BidPrices", row.bidPrices());
        fields.put("AskVolumes", row.askVolumes());
        fields.put("BidVolumes", row.bidVolumes());
        return fields;
    }

    private void flushPendingMinuteTick(List<Level2MinuteTickDTO> ticks,
                                        Level2MinuteTickDTO pendingMinuteTick) {
        if (pendingMinuteTick != null) {
            ticks.add(pendingMinuteTick);
        }
    }

    private boolean isAuctionTradeTime(String tickTime) {
        int second = tradeSecond(tickTime);
        return (second >= tradeSecond(9, 15, 0) && second <= tradeSecond(9, 25, 0))
                || (second >= tradeSecond(14, 57, 0) && second <= tradeSecond(15, 0, 0));
    }

    private boolean isContinuousTradeTime(String tickTime) {
        int second = tradeSecond(tickTime);
        return (second >= tradeSecond(9, 30, 0) && second <= tradeSecond(11, 30, 0))
                || (second >= tradeSecond(13, 0, 0) && second < tradeSecond(14, 57, 0));
    }

    private int tradeSecond(String tickTime) {
        if (tickTime == null || tickTime.length() < 8) {
            return -1;
        }
        return tradeSecond(Integer.parseInt(tickTime.substring(0, 2)),
                Integer.parseInt(tickTime.substring(3, 5)),
                Integer.parseInt(tickTime.substring(6, 8)));
    }

    private int tradeSecond(int hour, int minute, int second) {
        return (hour * 60 + minute) * 60 + second;
    }

    private String formatTradeTime(long raw) {
        String normalized = normalizedTradeTime(raw);
        return normalized.substring(0, 2) + ":" + normalized.substring(2, 4)
                + ":" + normalized.substring(4, 6);
    }

    /**
     * 兼容 ClickHouse 历史数据中的 HHmmss 与 HHmmssSSS 两种 TradeTime 编码。
     */
    private String normalizedTradeTime(long raw) {
        return raw >= 0 && raw <= 235_959L
                ? String.format("%06d", raw)
                : String.format("%09d", raw);
    }

    /**
     * 使用交易日与 TradeTime 生成前端时间轴，避免历史数据中 TradeTimeStamp 的时分秒失真。
     */
    private long displayTimestamp(LocalDate tradeDate, long rawTradeTime) {
        String normalized = normalizedTradeTime(rawTradeTime);
        int hour = Integer.parseInt(normalized.substring(0, 2));
        int minute = Integer.parseInt(normalized.substring(2, 4));
        int second = Integer.parseInt(normalized.substring(4, 6));
        int millis = normalized.length() == 9 ? Integer.parseInt(normalized.substring(6, 9)) : 0;
        return LocalDateTime.of(tradeDate.getYear(), tradeDate.getMonthValue(), tradeDate.getDayOfMonth(),
                        hour, minute, second, millis * 1_000_000)
                .atZone(MARKET_ZONE)
                .toInstant()
                .toEpochMilli();
    }

    private double findLimitUpPrice(String stockCode, LocalDate tradeDate, int symbolId) {
        StockDailyEntity daily = stockDailyService.getOne(new QueryWrapper<StockDailyEntity>()
                .eq("stock_code", stockCode)
                .eq("trade_date", tradeDate)
                .last("limit 1"));
        double prevClose = daily == null || daily.getPrevClose() == null ? 0D : daily.getPrevClose();
        if (prevClose <= 0) {
            return Double.MAX_VALUE;
        }
        int prefix = (symbolId - 1_000_000) / 10_000;
        double multiplier = prefix == 30 || prefix == 68 ? 1.2D : 1.1D;
        return BigDecimal.valueOf(prevClose)
                .multiply(BigDecimal.valueOf(multiplier))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private double roundPrice(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
