# cw-core-trading-strategy

`cw-core-trading-strategy` 只负责选股规则与交易信号判断。它不查询数据库、不修改订单簿，也不发送券商委托。实盘和回测应用都通过 `cw-common` 的接口调用本模块，因此两种运行环境使用同一套规则代码。

## 模块边界

- `cw-common`：按来源模块划分接口与中立数据对象，不能依赖任何实现模块。
- `cw-mysql-data`：实现 common 中的原始选股数据接口；选股完成后只负责实体转换和落库。
- `cw-core-order-book`：维护 Handler 私有订单簿，通过 common 接口调用规则，不依赖策略实现。
- `cw-core-trading-strategy`：完整拥有三种模式的选股、买入、撤单，以及按板高和市值划分的卖出规则；只依赖 common。
- `cw-app-live`、`cw-app-backtest`：组装根，负责组合数据、策略、订单簿和交易出口实现。

除两个 app 组装根外，功能模块之间禁止直接依赖；跨模块调用必须经过 `cw-common` 接口。

三个买入模式各自拥有独立的 `selection` 和 `trade` 包，禁止模式之间调用实现。持仓卖出不属于任何买入模式，统一放在 `strategy/sell` 下。

## 买入场景

买入由订单簿的 `tradeMode` 固定分发：

| tradeMode | 模式 | 上海集合竞价 | 深圳集合竞价 | 09:31 后连续竞价 |
|---|---|---|---|---|
| 1 | 连板 | `relay/trade/ShanghaiAuctionBuyEvaluator` | `relay/trade/ShenzhenAuctionBuyEvaluator` | `relay/trade/ContinuousLimitUpBuyEvaluator` |
| 2 | 普通首板 | `firstboard/trade/ShanghaiAuctionBuyEvaluator` | `firstboard/trade/ShenzhenAuctionBuyEvaluator` | `firstboard/trade/ContinuousLimitUpBuyEvaluator` |
| 3 | 小市值首板 | `smallcapfirstboard/trade/ShanghaiAuctionBuyEvaluator` | `smallcapfirstboard/trade/ShenzhenAuctionBuyEvaluator` | `smallcapfirstboard/trade/ContinuousLimitUpBuyEvaluator` |

集合竞价撤单与对应市场的集合竞价买入放在同一文件，因为两者共享撮合价、买卖总量和挂单状态。场景类直接读取 Disruptor 已预分配的 `AuctionMarketEvent`，负责派生买量、规则判断、原有备注和规则记录；不会复制 Tick 或创建临时 DTO。EventHandler 只保留市场阶段识别、订单簿更新、交易状态、执行网关调用和记录提交。盘中撤单仍由各模式的 `ConditionEvaluatorCancel` 管理。

## 卖出场景

卖出不读取历史买入 `tradeMode`。`SellStrategyDispatcher` 先按订单簿 `lbcs` 判断“2进3”至“8进9”或更高板，再由每个板高类按启动流通市值分档：

- 小市值：`initialMarketValue < 119999` 万元。
- 普通市值：`initialMarketValue >= 119999` 万元。
- 每个板高类都显式保留四个修改入口：小市值盘口、普通市值盘口、小市值分钟均价、普通市值分钟均价。
- 当前四个入口先调用 `LegacySellRules`，保持拆分前的阈值、规则优先级、规则编号和日志内容不变。后续按回测结果逐个替换，不修改总分发器。

上海、深圳收盘集合竞价分别由 `ShanghaiClosingAuctionSellEvaluator`、`ShenzhenClosingAuctionSellEvaluator` 填充原有卖出记录，公共判断由 `ClosingAuctionSellEvaluator` 复用。买入日炸板后次日按开盘价卖出的回测特例由 `BreakBoardNextOpenSellPolicy` 判断，执行时间仍为集合竞价结束的 09:25，不进入 09:31 盘中卖出规则。

不建立“常规早盘低开立即卖出”场景。现有分钟走势规则仍按原优先级保留，本次结构拆分不主动修改任何已有卖出条件。

## 高频路径约束

所有分发器都持有常驻策略实例并使用直接 `switch`。热路径不使用反射、Map、Spring Bean 查找，也不为每条 Tick 创建策略上下文对象。订单簿只保存 `tradeMode`、`lbcs`、启动市值等状态，不保存策略实现引用。
