package com.compoundwonder.service.impl;

import com.compoundwonder.api.Level2DataApi;
import com.compoundwonder.api.XmdTcpDataApi;
import com.compoundwonder.service.MarketStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MarketStreamServiceImpl implements MarketStreamService {

    @Autowired
    private Level2DataApi level2DataApi;

    @Autowired
    private XmdTcpDataApi xmdTcpDataApi;

    @Override
    public void level2ApiInit() throws InterruptedException {
        level2DataApi.levelApiInit();
    }

    @Override
    public void xmdApiInit() throws InterruptedException {
        xmdTcpDataApi.xmdApiInit();
    }
}
