package com.compoundwonder.service;

/** 实盘行情模块向 app 暴露的行情连接生命周期接口。 */
public interface MarketStreamService {

    /**
     * 初始化 Level2 逐笔行情连接并订阅当前订单簿关注的股票。
     *
     * @throws InterruptedException 初始化等待过程被线程中断时抛出
     */
    void level2ApiInit() throws InterruptedException;

    /**
     * 初始化三秒快照行情连接并订阅当前订单簿关注的股票。
     *
     * @throws InterruptedException 初始化等待过程被线程中断时抛出
     */
    void xmdApiInit() throws InterruptedException;
}
