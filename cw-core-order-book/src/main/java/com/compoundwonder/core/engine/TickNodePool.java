package com.compoundwonder.core.engine;

import java.util.Stack;

/**
 * 为了支撑全市场每天数亿笔的 Tick 吞吐，你不能依赖 JVM 的 new。你需要一个预分配的大数组作为“池子”。
 */
public class TickNodePool {
    private final Stack<TickNode> pool;
    private final int maxCapacity = 200000;

    public TickNodePool(int initialSize) {
        this.pool = new Stack<>();

        // 预热：提前创建对象，减少系统启动初期的延迟
        for (int i = 0; i < initialSize; i++) {
            pool.push(new TickNode());
        }
    }

    /**
     * 获取对象
     */
    public TickNode borrowNode() {
        if (!pool.isEmpty()) {
            return pool.pop();
        }
        // 如果池空了，创建一个新对象
        return new TickNode();
    }

    /**
     * 回收对象
     */
    public void release(TickNode node) {
        if (pool.size() < maxCapacity) {
            // 重置数据，防止脏数据影响下一次使用
            node.clear();
            pool.push(node);
        }
        // 超过最大容量的对象会被丢弃，等待 GC 回收
    }

    public int getFreeCount() {
        return pool.size();
    }

}
