package com.compoundwonder.core.config;

import com.compoundwonder.core.processor.TickEventFactory;
import com.compoundwonder.core.type.TickData;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

@Slf4j
@Configuration
public class DisruptorConfig {

    // 环大小，2^N 16384
    private static final int RING_BUFFER_SIZE = 1 << 20;

    private static final int SHARD_COUNT = 2; // 可根据CPU核数调整

    @Bean
    public Map<Integer, Disruptor<TickData>> disruptor() {
        Map<Integer, Disruptor<TickData>> disruptorMap = new ConcurrentHashMap<>();
        for (int i = 0; i < SHARD_COUNT; i++) {
            final int shardId = i;
            // 优化 1: 自定义线程工厂，提供清晰的监控标识
            ThreadFactory factory = r -> {
                Thread t = new Thread(r);
                if (shardId % 2== 0){
                    t.setName("Disruptor-SH");
                }else {
                    t.setName("Disruptor-SE");
                }
                // 即使不绑核，也可以通过设置高优先级尝试让调度器优先处理
                t.setPriority(Thread.MAX_PRIORITY);
                t.setDaemon(false);
                return t;
            };

            // 优化 2: 显式指定 WaitStrategy
            // 权限不足时，使用 YieldingWaitStrategy 代替 BusySpin
            // 它在没数据时会尝试 Thread.yield()，比 BusySpin 更省电且更兼容
            WaitStrategy strategy = new YieldingWaitStrategy();
            TickEventFactory tickEventFactory = new TickEventFactory();
            Disruptor<TickData> disruptor = new Disruptor<>(
                    tickEventFactory, // 使用方法引用，确保 RingBuffer 预分配对象
                    RING_BUFFER_SIZE,
                    factory,
                    ProducerType.MULTI, // 生产端：如果是多线程解析行情并写入，用 MULTI
                    strategy
            );
            disruptorMap.put(i, disruptor);
        }
        return disruptorMap;
    }
}