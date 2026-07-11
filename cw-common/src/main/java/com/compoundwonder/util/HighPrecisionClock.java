package com.compoundwonder.util;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
@Slf4j
public class HighPrecisionClock {
    // 加上 volatile，保证校准后所有线程立即看到最新的基准
    private static volatile long baseNanos;
    private static volatile long baseEpochNano;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSSSS");

    static {
        // 类加载时初始化一次
        sync();
    }

    /**
     * 手动校准/同步基准点
     * 建议每天开盘前（如 09:14:55）调用此方法
     */
    public static void sync() {
        // 使用 Instant 获取当前系统最精确的纳秒时间戳
        Instant now = Instant.now();
        baseEpochNano = now.getEpochSecond() * 1_000_000_000L + now.getNano();
        baseNanos = System.nanoTime();
        log.info("[Clock] 基准点已校准: {}",LocalTime.now());
    }

    /**
     * 将 nanoTime 转换为 LocalTime
     */
    public static LocalTime toLocalTime(long recordedNano) {
        long offset = recordedNano - baseNanos;
        long currentTotalNano = baseEpochNano + offset;

        long second = currentTotalNano / 1_000_000_000L;
        long nano = currentTotalNano % 1_000_000_000L;

        // 转换为本地时间（考虑时区）
        return Instant.ofEpochSecond(second, nano)
                .atZone(ZoneId.systemDefault())
                .toLocalTime();
    }

    public static String format(long recordedNano) {
        return toLocalTime(recordedNano).format(TIME_FORMATTER);
    }

    public static void main(String[] args) {
        // 模拟使用
        long t1 = System.nanoTime();
        System.out.println("当前时间: " + format(t1));

        // 模拟开盘前校准
        sync();

        long t2 = System.nanoTime();
        System.out.println("校准后时间: " + format(t2));
    }
}