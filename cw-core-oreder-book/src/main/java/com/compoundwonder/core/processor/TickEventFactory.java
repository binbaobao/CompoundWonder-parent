package com.compoundwonder.core.processor;

import com.compoundwonder.core.type.TickData;
import com.lmax.disruptor.EventFactory;

// 事件工厂（预分配对象）
public class TickEventFactory implements EventFactory<TickData> {
    public TickData newInstance() {
        return new TickData();
    }
}
