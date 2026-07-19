package com.compoundwonder.core.engine;


import lombok.extern.slf4j.Slf4j;

/**
 * 规则记录缓冲区。
 *
 * 设计目标：
 * 1. 实盘 / 回测高频路径不 new 对象。
 * 2. 启动时预分配 RuleRecord 数组。
 * 3. 命中买 / 卖 / 撤单规则时，直接填充数组中的对象。
 * 4. 收盘后遍历 records[0 ~ size) 批量落库。
 * 5. 正常情况下不扩容，扩容只是最后保底。
 *
 * 使用方式：
 *
 * RuleRecord record = buffer.nextRecord();
 * tradeStrategyDispatcher.evaluateBuy(orderBook, record);
 *
 * if (matched) {
 *     buffer.commit();
 *     buy(...);
 * } else {
 *     record.reset();
 * }
 *
 * 注意：
 * 一个 RuleRecordBuffer 只给一个 EventHandler 使用。
 * 不要多个线程同时写同一个 buffer。
 */
@Slf4j
public final class RuleRecordBuffer {

    /**
     * 已预分配的规则记录对象。
     *
     * records[0 ~ cursor - 1]：
     *      已提交，收盘需要落库。
     *
     * records[cursor]：
     *      当前可写对象。
     *
     * records[cursor + 1 ~ length - 1]：
     *      未使用对象。
     */
    private RuleRecord[] records;

    /**
     * 当前写入位置，同时也是已提交数量。
     */
    private int cursor;

    /**
     * 扩容次数。
     *
     * 正常实盘中应该一直是 0。
     * 如果大于 0，说明初始容量设置偏小，或者规则异常频繁触发。
     */
    private int growCount;

    public RuleRecordBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must > 0");
        }

        this.records = new RuleRecord[capacity];

        for (int i = 0; i < capacity; i++) {
            this.records[i] = new RuleRecord();
        }
    }

    /**
     * 获取当前可写对象。
     *
     * 注意：
     * 这个方法不会移动 cursor。
     *
     * 只有规则真正命中后，才调用 commit()。
     * 如果规则没有命中，只需要 reset()，该对象不会被收盘落库。
     */
    public RuleRecord nextRecord() {
        if (cursor >= records.length) {
            grow();
        }

        RuleRecord record = records[cursor];
        record.reset();
        return record;
    }

    /**
     * 提交当前对象。
     *
     * 调用 commit() 表示：
     * records[cursor] 已经是一条有效规则记录，
     * 收盘时需要落库。
     */
    public void commit() {
        cursor++;
    }

    /**
     * 扩容。
     *
     * 这是最后保底逻辑。
     * 正常盘中不应该频繁触发。
     */
    private void grow() {
        int oldCapacity = records.length;
        int newCapacity = oldCapacity << 1;

        RuleRecord[] newRecords = new RuleRecord[newCapacity];

        System.arraycopy(records, 0, newRecords, 0, oldCapacity);

        for (int i = oldCapacity; i < newCapacity; i++) {
            newRecords[i] = new RuleRecord();
        }

        records = newRecords;
        growCount++;

        log.warn("RuleRecordBuffer grow: " + oldCapacity + " -> " + newCapacity);
    }

    /**
     * 已提交记录数量。
     *
     * 收盘落库遍历范围：
     * records[0 ~ size() - 1]
     */
    public int size() {
        return cursor;
    }

    /**
     * 返回底层数组。
     *
     * 收盘落库时：
     *
     * RuleRecord[] records = buffer.records();
     * for (int i = 0; i < buffer.size(); i++) {
     *     RuleRecord r = records[i];
     * }
     */
    public RuleRecord[] records() {
        return records;
    }

    /**
     * 当前容量。
     */
    public int capacity() {
        return records.length;
    }

    /**
     * 扩容次数。
     */
    public int growCount() {
        return growCount;
    }

    /**
     * 收盘落库完成后清空。
     *
     * 不销毁对象，不重新分配数组。
     * 只是把已经使用过的对象字段清空，
     * 下一交易日继续复用。
     */
    public void clear() {
        for (int i = 0; i < cursor; i++) {
            records[i].reset();
        }

        cursor = 0;
        growCount = 0;
    }
}
