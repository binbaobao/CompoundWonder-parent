# cw-core-trading-strategy

该模块保存与数据库、ClickHouse、Disruptor 和券商接口无关的选股及交易规则。

## 模块边界

- `cw-mysql-data`：查询日 K、交易日历、ST、可转债等数据，构建候选指标；调用本模块的选股策略后落库。
- `cw-core-order-book`：维护订单簿和分钟行情；把自身作为只读 `TradeMarketState` 交给本模块判断规则。
- `cw-core-trading-strategy`：只做规则判断，不查询数据库，不修改订单簿，不直接调用券商。
- 回测与实盘：提供不同的交易动作出口，但共用同一订单簿与同一套模式规则。

## 三种模式

每种模式都拥有独立的 `selection` 和 `trade` 包，禁止模式之间调用选股或交易实现：

```text
strategy
├── relay
│   ├── selection   # mode=1 连板接力
│   └── trade       # 独立买入、卖出、撤单规则
├── firstboard
│   ├── selection   # mode=2 普通首板
│   └── trade
└── smallcapfirstboard
    ├── selection   # mode=3 小市值首板
    └── trade
```

当前三套交易代码由原统一 evaluator 和两个市场 Handler 中的交易判断完整复制，行为保持一致，包含
连续竞价买入、盘口卖出、分钟均价卖出、盘中撤单，以及上海/深圳集合竞价买入、撤单和尾盘卖出。
后续优化某种模式时，只修改该模式包。

## 高频调用

`OrderBook.tradeMode` 保存任务或持仓的稳定模式编号。Handler 调用 `TradeStrategyDispatcher`，分发器只用
`switch` 选择预创建的策略实例；热路径不使用 Map、反射、Spring Bean 查询或临时快照对象。

规则记录通过 `TradeRuleRecord` 写入订单簿预分配缓冲区，策略只读取 `TradeMarketState`，因此不会破坏
Handler 私有订单簿的线程模型。
