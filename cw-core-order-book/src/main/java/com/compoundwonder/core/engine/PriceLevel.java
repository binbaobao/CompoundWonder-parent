package com.compoundwonder.core.engine;

import lombok.Getter;

/**
 * 单个价格档位，分别维护买卖委托的时间优先队列和剩余总量。
 *
 * <p>队列节点直接使用 {@link TickNode} 的前后指针，不创建额外的链表包装对象。</p>
 */
@Getter
public final class PriceLevel {

    private TickNode buyHead;
    private TickNode buyTail;
    private TickNode sellHead;
    private TickNode sellTail;
    private long buyQuantity;
    private long sellQuantity;
    private int buyOrderCount;
    private int sellOrderCount;

    /**
     * 将委托追加到对应买卖队列的尾部。
     */
    void add(TickNode node) {
        node.setPrevious(null);
        node.setNext(null);
        if (node.getDirection() == 1) {
            if (buyTail == null) {
                buyHead = node;
            } else {
                buyTail.setNext(node);
                node.setPrevious(buyTail);
            }
            buyTail = node;
            buyQuantity += node.getQuantity();
            buyOrderCount++;
            return;
        }
        if (node.getDirection() == 2) {
            if (sellTail == null) {
                sellHead = node;
            } else {
                sellTail.setNext(node);
                node.setPrevious(sellTail);
            }
            sellTail = node;
            sellQuantity += node.getQuantity();
            sellOrderCount++;
            return;
        }
        throw new IllegalArgumentException("不支持的委托方向: " + node.getDirection());
    }

    /**
     * 扣减委托剩余量；全部成交时同时从价位队列摘除。
     *
     * @return 实际扣减数量
     */
    int applyTrade(TickNode node, int quantity) {
        if (quantity <= 0 || node.getQuantity() <= 0) {
            return 0;
        }
        int deducted = Math.min(quantity, node.getQuantity());
        node.setQuantity(node.getQuantity() - deducted);
        decreaseQuantity(node.getDirection(), deducted);
        if (node.getQuantity() == 0) {
            unlink(node);
        }
        return deducted;
    }

    /**
     * 撤销委托并从价位队列摘除。
     *
     * @return 被撤销的剩余数量
     */
    int remove(TickNode node) {
        int remaining = node.getQuantity();
        decreaseQuantity(node.getDirection(), remaining);
        unlink(node);
        return remaining;
    }

    private void decreaseQuantity(byte direction, int quantity) {
        if (direction == 1) {
            buyQuantity -= quantity;
        } else if (direction == 2) {
            sellQuantity -= quantity;
        } else {
            throw new IllegalArgumentException("不支持的委托方向: " + direction);
        }
    }

    private void unlink(TickNode node) {
        TickNode previous = node.getPrevious();
        TickNode next = node.getNext();
        if (previous == null) {
            setHead(node.getDirection(), next);
        } else {
            previous.setNext(next);
        }
        if (next == null) {
            setTail(node.getDirection(), previous);
        } else {
            next.setPrevious(previous);
        }
        node.setPrevious(null);
        node.setNext(null);
        if (node.getDirection() == 1) {
            buyOrderCount--;
        } else {
            sellOrderCount--;
        }
    }

    private void setHead(byte direction, TickNode node) {
        if (direction == 1) {
            buyHead = node;
        } else {
            sellHead = node;
        }
    }

    private void setTail(byte direction, TickNode node) {
        if (direction == 1) {
            buyTail = node;
        } else {
            sellTail = node;
        }
    }
}
