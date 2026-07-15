package com.compoundwonder.core.engine;

/**
 * 为了支撑全市场每天数亿笔的 Tick 吞吐，你不能依赖 JVM 的 new。你需要一个预分配的大数组作为“池子”。
 */
public final class TickNodePool {

    private static final int MAX_CAPACITY = 200_000;

    /** Handler 单线程独占的对象栈，不需要 Stack 的同步开销。 */
    private final TickNode[] pool = new TickNode[MAX_CAPACITY];

    /** 当前可以借出的节点数量，同时也是下一个归还位置。 */
    private int size;

    public TickNodePool(int initialSize) {
        if (initialSize < 0 || initialSize > MAX_CAPACITY) {
            throw new IllegalArgumentException("initialSize must be between 0 and " + MAX_CAPACITY);
        }

        // 预热：提前创建对象，减少系统启动初期的延迟
        for (int i = 0; i < initialSize; i++) {
            pool[size++] = new TickNode();
        }
    }

    /**
     * 获取对象
     */
    public TickNode borrowNode() {
        if (size > 0) {
            int index = --size;
            TickNode node = pool[index];
            pool[index] = null;
            return node;
        }
        // 如果池空了，创建一个新对象
        return new TickNode();
    }

    /**
     * 回收对象
     */
    public void release(TickNode node) {
        // 即使池已满也先清理，避免被误持有时保留订单簿链表引用。
        node.clear();
        if (size < pool.length) {
            pool[size++] = node;
        }
        // 超过最大容量的对象会被丢弃，等待 GC 回收
    }

    public int getFreeCount() {
        return size;
    }

}
