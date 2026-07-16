/**
 * 订单簿交易条件评估器。
 *
 * <p>本包组合字段统一使用以下口径：</p>
 * <ul>
 *     <li>订单簿价格使用 {@code int} 分，例如 {@code 1000} 表示 10 元；</li>
 *     <li>行情时间使用紧凑格式 {@code HHmmssSSS}，例如 09:31:00.000 为 {@code 93100000}；</li>
 *     <li>启动市值、涨停封单金额使用万元，累计成交额使用元，成交量和委托量使用股；</li>
 *     <li>涨跌幅、振幅、回撤、换手率和 EMA 变化率均使用百分数，数值 {@code 3.5} 表示 3.5%；</li>
 *     <li>{@code lbcs} 表示昨日已经完成的连续涨停天数，判断“2进3”时值为 {@code 2}；</li>
 *     <li>订单簿 {@code status} 为奇数时表示当前封板，偶数时表示未封板或已经炸板。</li>
 * </ul>
 */
package com.compoundwonder.core.processor.evaluator;
