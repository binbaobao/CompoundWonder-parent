# cw-core-trading-strategy

该模块保存与数据库、ClickHouse、Disruptor 和券商接口无关的选股及交易规则。

## 模块边界

- `cw-common`：按来源模块划分接口与中立数据对象，不能依赖任何实现模块。
- `cw-mysql-data`：实现 common 中的原始选股数据接口；选股完成后只负责转换实体和落库。
- `cw-core-order-book`：维护 Handler 私有订单簿，通过 common 接口调用交易规则，不依赖策略实现。
- `cw-core-trading-strategy`：完整拥有三种模式的选股、买入、卖出和撤单规则；只依赖 common。
- `cw-app-live`、`cw-app-backtest`：组装根，负责把数据、策略、订单簿和交易出口实现组合起来。

除 app 组装根外，功能模块之间禁止直接依赖；跨模块调用必须经过 `cw-common` 接口。

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

规则记录通过 common 的 `TradeRuleRecord` 写入订单簿预分配缓冲区，策略只读取 common 的
`TradeMarketState`，因此不会破坏
Handler 私有订单簿的线程模型。
