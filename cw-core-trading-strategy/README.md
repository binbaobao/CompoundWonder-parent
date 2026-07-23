# cw-core-trading-strategy

`cw-core-trading-strategy` 只负责选股规则与交易信号判断。它不查询数据库、不修改订单簿，也不发送券商委托。实盘和回测应用都通过 `cw-common` 的接口调用本模块，因此两种运行环境使用同一套规则代码。

## 模块边界

- `cw-common`：按来源模块划分接口与中立数据对象，不能依赖任何实现模块。
- `cw-mysql-data`：实现 common 中的原始选股数据接口；选股完成后只负责实体转换和落库。
- `cw-core-order-book`：维护 Handler 私有的 `OrderBookSession`，通过会话内已编译模板调用规则，不依赖策略实现。
- `cw-core-trading-strategy`：完整拥有三种模式的选股、买入、撤单，以及统一的连续竞价卖出规则；只依赖 common。
- `cw-app-live`、`cw-app-backtest`：组装根，负责组合数据、策略、订单簿和交易出口实现。

除两个 app 组装根外，功能模块之间禁止直接依赖；跨模块调用必须经过 `cw-common` 接口。

三个买入模式各自拥有独立的 `selection` 和 `trade` 包，禁止模式之间调用实现。持仓卖出不属于任何买入模式，统一放在 `strategy/sell` 下。

## 买入场景

买入模式在创建 `TradeExecutionTemplate` 时按静态事实固定解析：

| tradeMode | 模式 | 上海集合竞价 | 深圳集合竞价 | 09:31 后连续竞价 |
|---|---|---|---|---|
| 1 | 连板 | `relay/trade/ShanghaiAuctionBuyEvaluator` | `relay/trade/ShenzhenAuctionBuyEvaluator` | `relay/trade/ContinuousLimitUpBuyEvaluator` |
| 2 | 普通首板 | `firstboard/trade/ShanghaiAuctionBuyEvaluator` | `firstboard/trade/ShenzhenAuctionBuyEvaluator` | `firstboard/trade/ContinuousLimitUpBuyEvaluator` |
| 3 | 小市值首板 | `smallcapfirstboard/trade/ShanghaiAuctionBuyEvaluator` | `smallcapfirstboard/trade/ShenzhenAuctionBuyEvaluator` | `smallcapfirstboard/trade/ContinuousLimitUpBuyEvaluator` |

集合竞价撤单与对应市场的集合竞价买入放在同一文件，因为两者共享撮合价、买卖总量和挂单状态。场景类直接读取 Disruptor 已预分配的 `AuctionMarketEvent`，负责派生买量、规则判断、原有备注和规则记录；不会复制 Tick 或创建临时 DTO。EventHandler 只保留市场阶段识别、订单簿更新、交易状态、执行网关调用和记录提交。盘中撤单仍由各模式的 `ConditionEvaluatorCancel` 管理。

## 卖出场景

卖出不读取历史买入 `tradeMode`，也不按 `lbcs × initialMarketValue` 选择不同执行器。
连续竞价盘口卖出和分钟均价卖出统一进入
`strategy/sell/ContinuousSellStrategy`，按固定优先级逐条判断。板高、市值、换手等
事实只允许出现在具体规则条件中，避免预分派导致本应适用的规则没有执行。

上海、深圳收盘集合竞价分别由 `ShanghaiClosingAuctionSellEvaluator`、`ShenzhenClosingAuctionSellEvaluator` 填充原有卖出记录，公共判断由 `ClosingAuctionSellEvaluator` 复用。买入日炸板后次日按开盘价卖出的回测特例由 `BreakBoardNextOpenSellPolicy` 判断，执行时间仍为集合竞价结束的 09:25，不进入 09:31 盘中卖出规则。

上海开盘集合竞价和深圳开盘集合竞价仍是两个独立执行器，分别使用各交易所的买入与
撤单规则；统一连续竞价卖出不介入这两个阶段。

## 订单簿会话与高频路径约束

回测组装根在第一条行情到达前构建五个对象：

- `MarketSessionSpec`：代码、名称、日期、昨收、涨跌停价和流通股本；
- `TradeStaticFacts`：模式、板位、启动市值、历史最大量/换手、最近换手、K 线形态、市场平均高度和交易日间隔；
- `OrderBook`：逐笔委托、价位队列、成交、分钟价、涨停状态和 EMA 等盘中热数据；
- `TradeExecutionTemplate`：根据买入模式和静态事实预编译的六阶段规则；
- `TradeExecutionState`：买卖监控状态、上海竞价前一快照、首次触板时间和当日否决状态。

六个稳定阶段为：上海开盘集合竞价买入、深圳开盘集合竞价买入、连续竞价买入、连续竞价卖出、分钟均线卖出、收盘集合竞价卖出。买入后的竞价撤单跟随对应市场的开盘集合竞价执行器。

Handler 保存并读取同一个 `OrderBookSession`。热路径不使用反射、Map、Spring Bean 查找，不查询数据库，不创建策略上下文，也不再按 `tradeMode`、`lbcs` 或市值执行重复分发。
