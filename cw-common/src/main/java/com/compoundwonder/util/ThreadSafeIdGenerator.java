package com.compoundwonder.util;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于系统内存的id 自增
 */
public class ThreadSafeIdGenerator {

    private static final AtomicInteger idCounter = new AtomicInteger(LocalDate.now().getDayOfYear() * 10000);

    // 生成下一个 ID
    public static int generateId() {
        return idCounter.incrementAndGet();
    }

}
