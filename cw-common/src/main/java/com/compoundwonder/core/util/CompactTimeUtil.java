package com.compoundwonder.core.util;

import java.time.LocalTime;

/**
 * 高频行情时间戳转换工具
 * <p>
 * 用 int 表示时间，格式为 HHMMSSmmm，例如：
 * 91501710  -> 09:15:01.710
 * 150000000 -> 15:00:00.000
 * <p>
 * 适用于逐笔委托 / 成交 / 撮合时间压缩存储。
 */
public final class CompactTimeUtil {

    private CompactTimeUtil() {
    }

    /**
     * 将 int 格式的时间（如 91501710）转换为当天的毫秒数。
     */
    public static int compactToMillis(int compact) {
        int hour = compact / 10_000_000;
        int minute = (compact / 100_000) % 100;
        int second = (compact / 1000) % 100;
        int millis = compact % 1000;

        return ((hour * 60 + minute) * 60 + second) * 1000 + millis;
    }

    /**
     * 将毫秒数（当天起）转换回 int 格式时间（如 91501710）。
     */
    public static int millisToCompact(int millisOfDay) {
        int hour = (millisOfDay / 3600000);
        int minute = ((millisOfDay / 60000) % 60);
        int second = ((millisOfDay / 1000) % 60);
        int milli = (millisOfDay % 1000);

        return hour * 10000000 + minute * 100000 + second * 1000 + milli;
    }

    public static int calculateMillis(int time,int compact){
        int compactToMillis = compactToMillis(time);
        return millisToCompact(compactToMillis + compact);
    }

    /**
     * 将 int 格式时间转换为 LocalTime。
     */
    public static LocalTime compactToLocalTime(int compact) {
        return LocalTime.ofNanoOfDay((long) compactToMillis(compact) * 1_000_000);
    }

    /**
     * 将 LocalTime 转为 int 格式时间。
     */
    public static int localTimeToCompact(LocalTime time) {
        int millis = time.toSecondOfDay() * 1000 + time.getNano() / 1_000_000;
        return millisToCompact(millis);
    }

    /**
     * 格式化输出（09:15:01.710）
     */
    public static String formatCompact(int compact) {
        int hour = compact / 1_000_000;
        int minute = (compact / 10_000) % 100;
        int second = (compact / 1000) % 100;
        int millis = compact % 1000;
        return String.format("%02d:%02d:%02d.%03d", hour, minute, second, millis);
    }

    /**
     * 根据时间戳获取数组下标
     *
     * @param timeInt
     * @return
     */
    public static int calculateIndex(int timeInt) {
        int hhmm = timeInt / 100000;
        int hh = hhmm / 100;
        int mm = hhmm % 100;

        if (hhmm < 1131) {
            int idx = (hh - 9) * 60 + mm - 30;
            return Math.max(0, idx);
        } else if (hhmm >= 1300) {
            int idx = (hh - 13) * 60 + mm + 120;
            return Math.min(idx, 239);
        }
        return 0;
    }
}
