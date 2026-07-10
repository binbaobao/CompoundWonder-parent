package com.compoundwonder.service;


import com.compoundwonder.dto.TradingAccountDto;
import com.compoundwonder.dto.TstpOrderDto;
import com.compoundwonder.dto.TstpPositionDto;
import com.compoundwonder.dto.UserLoginInfoDto;
import com.tora.traderapi.CTORATstpOrderField;
import com.tora.traderapi.CTORATstpPositionField;
import com.tora.traderapi.CTORATstpRspUserLoginField;
import com.tora.traderapi.CTORATstpTradingAccountField;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 交易缓存服务
 */
@Slf4j
@Component
public class TradeCacheService {

    /**
     * 账户信息
     */
    @Getter
    private TradingAccountDto accountInfo = new TradingAccountDto();

    @Getter
    private UserLoginInfoDto userLoginInfoDto = new UserLoginInfoDto();

    /**
     * 持仓记录
     */
    private final ConcurrentHashMap<String, TstpPositionDto> positionRecords = new ConcurrentHashMap<>();
    /**
     * 委托记录
     */
    private final ConcurrentHashMap<Integer, TstpOrderDto> orderMap = new ConcurrentHashMap<>();

    /**
     * 添加账户信息
     */
    public void putStockAccountInfo(char marketID, String shareholderID) {
        accountInfo.getStringStringMap().put(marketID, shareholderID);
        log.info(" 更新股东账户 {} ", accountInfo);
    }

    /**
     * 更新账户信息
     *
     * @param pTradingAccount
     */
    public synchronized void updateAccountInfo(CTORATstpTradingAccountField pTradingAccount) {
        accountInfo.setDepartmentID(pTradingAccount.getDepartmentID());
        accountInfo.setAccountID(pTradingAccount.getAccountID());
        accountInfo.setCurrencyID(pTradingAccount.getCurrencyID());
        accountInfo.setPreDeposit(pTradingAccount.getPreDeposit());
        accountInfo.setUsefulMoney(pTradingAccount.getUsefulMoney());
        accountInfo.setFetchLimit(pTradingAccount.getFetchLimit());
        accountInfo.setPreUnDeliveredMoney(pTradingAccount.getPreUnDeliveredMoney());
        accountInfo.setUnDeliveredCommission(pTradingAccount.getUnDeliveredCommission());
        accountInfo.setDeposit(pTradingAccount.getDeposit());
        accountInfo.setWithdraw(pTradingAccount.getWithdraw());
        accountInfo.setFrozenCash(pTradingAccount.getFrozenCash());
        accountInfo.setUnDeliveredFrozenCash(pTradingAccount.getUnDeliveredFrozenCash());
        accountInfo.setFrozenCommission(pTradingAccount.getFrozenCommission());
        accountInfo.setUnDeliveredFrozenCommission(pTradingAccount.getUnDeliveredFrozenCommission());
        accountInfo.setCommission(pTradingAccount.getCommission());
        accountInfo.setAccountType(pTradingAccount.getAccountType());
        accountInfo.setInvestorID(pTradingAccount.getInvestorID());
        accountInfo.setBankID(pTradingAccount.getBankID());
        accountInfo.setBankAccountID(pTradingAccount.getBankAccountID());
        accountInfo.setRoyaltyIn(pTradingAccount.getRoyaltyIn());
        accountInfo.setRoyaltyOut(pTradingAccount.getRoyaltyOut());
        accountInfo.setCreditSellAmount(pTradingAccount.getCreditSellAmount());
        accountInfo.setCreditSellUseAmount(pTradingAccount.getCreditSellUseAmount());
        accountInfo.setVirtualAssets(pTradingAccount.getVirtualAssets());
        accountInfo.setCreditSellFrozenAmount(pTradingAccount.getCreditSellFrozenAmount());
        accountInfo.setOwnerUnit(pTradingAccount.getOwnerUnit());
        log.info(" 更新账户总信息 {} ", accountInfo);
    }

    /**
     * 添加持仓记录
     */
    public synchronized void addPositionRecord(CTORATstpPositionField pPosition) {
//TstpPositionDto(exchangeID=2, investorID=319000021663, businessUnitID=319000021663, marketID=2, shareholderID=0369429401, tradingDay=20251222, securityID=001331, securityName=胜通能源, historyPos=0, historyPosFrozen=0, todayBSPos=0, todayBSPosFrozen=0, todayPRPos=0, todayPRPosFrozen=0, todaySMPos=0, todaySMPosFrozen=0, historyPosPrice=0.0, totalPosCost=0.0, prePosition=0, availablePosition=0, currentPosition=0, openPosCost=0.0, creditBuyPos=0, creditSellPos=0, todayCreditSellPos=0, collateralOutPos=0, repayUntradeVolume=0, repayTransferUntradeVolume=0, collateralBuyUntradeAmount=0.0, collateralBuyUntradeVolume=0, creditBuyAmount=0.0, creditBuyUntradeAmount=0.0, creditBuyFrozenMargin=0.0, creditBuyInterestFee=0.0, creditBuyUntradeVolume=0, creditSellAmount=0.0, creditSellUntradeAmount=0.0, creditSellFrozenMargin=0.0, creditSellInterestFee=0.0, creditSellUntradeVolume=0, collateralInPos=0, creditBuyFrozenCirculateMargin=0.0, creditSellFrozenCirculateMargin=0.0, closeProfit=0.0, todayTotalOpenVolume=300, todayCommission=0.0, todayTotalBuyAmount=8625.0, todayTotalSellAmount=0.0, preFrozen=0, todayTotalCloseVolume=0)
        TstpPositionDto positionDto = new TstpPositionDto();
        positionDto.setExchangeID(pPosition.getExchangeID());
        positionDto.setInvestorID(pPosition.getInvestorID());
        positionDto.setBusinessUnitID(pPosition.getBusinessUnitID());
        positionDto.setMarketID(pPosition.getMarketID());
        positionDto.setShareholderID(pPosition.getShareholderID());
        positionDto.setTradingDay(pPosition.getTradingDay());
        positionDto.setSecurityID(pPosition.getSecurityID());
        positionDto.setSecurityName(pPosition.getSecurityName());
        positionDto.setHistoryPos(pPosition.getHistoryPos());
        positionDto.setHistoryPosFrozen(pPosition.getHistoryPosFrozen());
        positionDto.setTodayBSPos(pPosition.getTodayBSPos());
        positionDto.setTodayBSPosFrozen(pPosition.getTodayBSPosFrozen());
        positionDto.setTodayPRPos(pPosition.getTodayPRPos());
        positionDto.setTodayPRPosFrozen(pPosition.getTodayPRPosFrozen());
        positionDto.setTodaySMPos(pPosition.getTodaySMPos());
        positionDto.setTodaySMPosFrozen(pPosition.getTodaySMPosFrozen());
        positionDto.setHistoryPosPrice(pPosition.getHistoryPosPrice());
        positionDto.setTotalPosCost(pPosition.getTotalPosCost());
        positionDto.setPrePosition(pPosition.getPrePosition());
        positionDto.setAvailablePosition(positionDto.getAvailablePosition());
        positionDto.setCurrentPosition(positionDto.getCurrentPosition());
        positionDto.setOpenPosCost(pPosition.getOpenPosCost());
        positionDto.setCreditSellPos(pPosition.getCreditSellPos());
        positionDto.setCreditBuyPos(pPosition.getCreditBuyPos());
        positionDto.setTodayCreditSellPos(pPosition.getTodayCreditSellPos());
        positionDto.setCollateralOutPos(pPosition.getCollateralOutPos());
        positionDto.setRepayUntradeVolume(pPosition.getRepayUntradeVolume());
        positionDto.setRepayTransferUntradeVolume(pPosition.getRepayTransferUntradeVolume());
        positionDto.setCollateralBuyUntradeVolume(pPosition.getCollateralBuyUntradeVolume());
        positionDto.setCollateralBuyUntradeAmount(pPosition.getCollateralBuyUntradeAmount());
        positionDto.setCreditBuyAmount(pPosition.getCreditBuyAmount());
        positionDto.setCreditBuyUntradeAmount(pPosition.getCreditBuyUntradeAmount());
        positionDto.setCreditBuyFrozenMargin(pPosition.getCreditBuyFrozenMargin());
        positionDto.setCreditBuyInterestFee(pPosition.getCreditBuyInterestFee());
        positionDto.setCreditBuyUntradeVolume(pPosition.getCreditBuyUntradeVolume());
        positionDto.setCreditSellAmount(pPosition.getCreditSellAmount());
        positionDto.setCreditSellUntradeAmount(pPosition.getCreditSellUntradeAmount());
        positionDto.setCreditSellFrozenMargin(pPosition.getCreditSellFrozenMargin());
        positionDto.setCreditSellInterestFee(pPosition.getCreditSellInterestFee());
        positionDto.setCreditSellUntradeVolume(pPosition.getCreditSellUntradeVolume());
        positionDto.setCollateralInPos(pPosition.getCollateralInPos());
        positionDto.setCreditBuyFrozenCirculateMargin(pPosition.getCreditBuyFrozenCirculateMargin());
        positionDto.setCreditSellFrozenCirculateMargin(pPosition.getCreditSellFrozenCirculateMargin());
        positionDto.setCloseProfit(pPosition.getCloseProfit());
        positionDto.setTodayTotalOpenVolume(pPosition.getTodayTotalOpenVolume());
        positionDto.setTodayCommission(pPosition.getTodayCommission());
        positionDto.setTodayTotalBuyAmount(pPosition.getTodayTotalBuyAmount());
        positionDto.setTodayTotalSellAmount(pPosition.getTodayTotalSellAmount());
        positionDto.setPreFrozen(pPosition.getPreFrozen());
        positionDto.setTodayTotalCloseVolume(pPosition.getTodayTotalCloseVolume());
        positionRecords.put(pPosition.getSecurityID(), positionDto);
        log.info(" 更新持仓记录 {} ", positionDto);
    }


    public void testAdd(TstpPositionDto positionDto) {
        positionRecords.put(positionDto.getSecurityID(), positionDto);
    }


    /**
     * 根据 股票代码查询 持仓数量
     *
     * @param securityID
     * @return
     */
    public TstpPositionDto getPositionRecords(String securityID) {
        TstpPositionDto tstpPositionDto = positionRecords.get(securityID);
        if (tstpPositionDto == null) {
            return null;
        }else if (tstpPositionDto.getTodayBSPos() == 0 && tstpPositionDto.getHistoryPos() == 0){
            return  null ;
        }else {
            return tstpPositionDto;
        }
    }

    /**
     * 获取持仓的股票
     *
     * @return
     */
    public List<String> getPositionCodes() {
        return positionRecords.values()
                .stream().filter(p -> p.getHistoryPos() != 0)
                .map(TstpPositionDto::getSecurityID).toList();
    }

    /**
     * 更新或者添加报单记录
     */
    public synchronized void updateOrAddOrder(CTORATstpOrderField pOrder) {

        int orderRef = pOrder.getOrderRef();

        TstpOrderDto orderDto = new TstpOrderDto();
        orderDto.setExchangeID(pOrder.getExchangeID());
        orderDto.setInvestorID(pOrder.getInvestorID());
        orderDto.setBusinessUnitID(pOrder.getBusinessUnitID());
        orderDto.setShareholderID(pOrder.getShareholderID());
        orderDto.setSecurityID(pOrder.getSecurityID());
        orderDto.setDirection(pOrder.getDirection());
        orderDto.setOrderPriceType(pOrder.getOrderPriceType());
        orderDto.setTimeCondition(pOrder.getTimeCondition());
        orderDto.setVolumeCanceled(pOrder.getVolumeCanceled());
        orderDto.setLimitPrice(pOrder.getLimitPrice());
        orderDto.setVolumeTotalOriginal(pOrder.getVolumeTotalOriginal());
        orderDto.setLotType(pOrder.getLotType());
        orderDto.setGTDate(pOrder.getGTDate());
        orderDto.setOperway(pOrder.getOperway());
        orderDto.setCondCheck(pOrder.getCondCheck());
        orderDto.setSInfo(pOrder.getSInfo());
        orderDto.setIInfo(pOrder.getIInfo());
        orderDto.setRequestID(pOrder.getRequestID());
        orderDto.setFrontID(pOrder.getFrontID());
        orderDto.setSessionID(pOrder.getSessionID());
        orderDto.setOrderRef(pOrder.getOrderRef());
        orderDto.setOrderLocalID(pOrder.getOrderLocalID());
        orderDto.setOrderSysID(pOrder.getOrderSysID());
        orderDto.setOrderStatus(pOrder.getOrderStatus());
        orderDto.setOrderSubmitStatus(pOrder.getOrderSubmitStatus());
        orderDto.setStatusMsg(pOrder.getStatusMsg());
        orderDto.setVolumeTraded(pOrder.getVolumeTraded());
        orderDto.setVolumeCanceled(pOrder.getVolumeCanceled());
        orderDto.setTradingDay(pOrder.getTradingDay());
        orderDto.setInsertUser(pOrder.getInsertUser());
        orderDto.setInsertDate(pOrder.getInsertDate());
        orderDto.setInsertTime(pOrder.getInsertTime());
        orderDto.setAcceptTime(pOrder.getAcceptTime());
        orderDto.setCancelUser(pOrder.getCancelUser());
        orderDto.setCancelTime(pOrder.getCancelTime());
        orderDto.setDepartmentID(pOrder.getDepartmentID());
        orderDto.setAccountID(pOrder.getAccountID());
        orderDto.setCurrencyID(pOrder.getCurrencyID());
        orderDto.setPbuID(pOrder.getPbuID());
        orderDto.setTurnover(pOrder.getTurnover());
        orderDto.setOrderType(pOrder.getOrderType());
        orderDto.setUserProductInfo(pOrder.getUserProductInfo());
        orderDto.setForceCloseReason(pOrder.getForceCloseReason());
        orderDto.setCreditQuotaID(pOrder.getCreditQuotaID());
        orderDto.setCreditQuotaType(pOrder.getCreditQuotaType());
        orderDto.setCreditDebtID(pOrder.getCreditDebtID());
        orderDto.setIPAddress(pOrder.getIPAddress());
        orderDto.setMacAddress(pOrder.getMacAddress());
        orderDto.setRtnFloatInfo(pOrder.getRtnFloatInfo());
        orderDto.setRtnIntInfo(pOrder.getRtnIntInfo());
        orderDto.setRtnFloatInfo1(pOrder.getRtnFloatInfo1());
        orderDto.setRtnFloatInfo2(pOrder.getRtnFloatInfo2());
        orderDto.setRtnFloatInfo3(pOrder.getRtnFloatInfo3());
        orderDto.setAcceptTimeStamp(pOrder.getAcceptTimeStamp());
        orderMap.put(orderRef, orderDto);
        log.info(" 更新报单记录 orderRef : {},订单详情 {} ,", orderDto.getOrderRef(), orderDto);

        if (pOrder.getOrderStatus() >='3'){
            // 更新账户余额
            accountInfo.setUsefulMoney(pOrder.getRtnFloatInfo());
            // 更新持仓
            positionRecords.compute(pOrder.getSecurityID(), (key, value) -> {
                if (value != null) {
                    value.setHistoryPos(pOrder.getRtnIntInfo());
                }
                return value;
            });
        }

    }


    /**
     * 卖出后成交更新持仓数量
     *
     * @param securityID
     * @param rtnIntInfo
     */
    public void updateHistoryVolume(String securityID, int rtnIntInfo) {
        positionRecords.compute(securityID, (key, value) -> {
            if (value != null) {
                value.setHistoryPos(rtnIntInfo);
            }
            return value;
        });
    }


    /**
     * 新下单订单
     *
     * @param orderRef
     * @param securityID
     */
    public void ddOrder(int orderRef, String securityID) {
        TstpOrderDto orderDto = new TstpOrderDto();
        orderDto.setOrderRef(orderRef);
        orderDto.setSecurityID(securityID);
        orderDto.setOrderStatus('0');
        orderMap.put(orderRef, orderDto);
        log.info(" 更新报单记录 {} ", orderDto);
    }

    /**
     * 获取未成交单数据,设置撤单警戒线
     */
    public long getUnsoldOrderCount() {
        return orderMap.size();
    }

    /**
     * 根据股票代码，获取可撤单的订单
     */
    public List<TstpOrderDto> findEntrustmentRecords(String securityID) {
        return orderMap.values().stream().filter(order -> order.getOrderStatus() <= '3' && order.getSecurityID().equals(securityID)).toList();
    }

    public TstpOrderDto getOrder(Integer orderRef) {
        return orderMap.get(orderRef);
    }

    /**
     * 根据股票代码，获取可撤单的订单
     */
    public List<TstpOrderDto> getAllOrderList() {
        return orderMap.values().stream().toList();
    }


    /**
     * 根据股票代码，获取是否
     * 报单状态
     * 预埋 '0'
     * 未知 '1'
     * 交易所已接收 '2'
     * 部分成交 '3'
     * 全部成交 '4'
     * 部成部撤 '5'
     * 全部撤单 '6'
     * 交易所已拒绝 '7'
     */
    public List<TstpOrderDto> findOrderBySecurityID(String securityID) {
        return orderMap.values().stream().filter(order -> order.getOrderStatus() <= '4' && order.getSecurityID().equals(securityID)).toList();
    }



    /**
     * 查询已挂单，一般是隔夜单记录
     *
     * @return
     */
    public TstpOrderDto findNocturnalRecords() {
        Optional<TstpOrderDto> first = orderMap.values().stream().filter(order -> order.getOrderStatus() <= '3').findFirst();
        return first.orElse(null);
    }

    /**
     * 记录登录信息，缓存登录信息
     *
     * @param pRspUserLoginField
     */
    public void setUserLoginInfoDto(CTORATstpRspUserLoginField pRspUserLoginField) {
        userLoginInfoDto.setUserRequestID(pRspUserLoginField.getUserRequestID());
        userLoginInfoDto.setDepartmentID(pRspUserLoginField.getDepartmentID());
        userLoginInfoDto.setLogInAccount(pRspUserLoginField.getLogInAccount());
        userLoginInfoDto.setLogInAccountType(pRspUserLoginField.getLogInAccountType());
        userLoginInfoDto.setFrontID(pRspUserLoginField.getFrontID());
        userLoginInfoDto.setSessionID(pRspUserLoginField.getSessionID());
        userLoginInfoDto.setMaxOrderRef(pRspUserLoginField.getMaxOrderRef());
        userLoginInfoDto.setPrivateFlowCount(pRspUserLoginField.getPrivateFlowCount());
        userLoginInfoDto.setPublicFlowCount(pRspUserLoginField.getPublicFlowCount());
        userLoginInfoDto.setLoginTime(pRspUserLoginField.getLoginTime());
        userLoginInfoDto.setSystemName(pRspUserLoginField.getSystemName());
        userLoginInfoDto.setTradingDay(pRspUserLoginField.getTradingDay());
        userLoginInfoDto.setUserName(pRspUserLoginField.getUserName());
        userLoginInfoDto.setUserID(pRspUserLoginField.getUserID());
        userLoginInfoDto.setUserType(pRspUserLoginField.getUserType());
        userLoginInfoDto.setOrderInsertCommFlux(pRspUserLoginField.getOrderInsertCommFlux());
        userLoginInfoDto.setOrderActionCommFlux(pRspUserLoginField.getOrderActionCommFlux());
        userLoginInfoDto.setPasswordExpiryDate(pRspUserLoginField.getPasswordExpiryDate());
        userLoginInfoDto.setNeedUpdatePassword(pRspUserLoginField.getNeedUpdatePassword());
        userLoginInfoDto.setCertSerial(pRspUserLoginField.getCertSerial());
        userLoginInfoDto.setInnerIPAddress(pRspUserLoginField.getInnerIPAddress());
        userLoginInfoDto.setOuterIPAddress(pRspUserLoginField.getOuterIPAddress());
        userLoginInfoDto.setMacAddress(pRspUserLoginField.getMacAddress());
        userLoginInfoDto.setNodeRef(pRspUserLoginField.getNodeRef());
        userLoginInfoDto.setTradeCommFlux(pRspUserLoginField.getTradeCommFlux());
        userLoginInfoDto.setQueryCommFlux(pRspUserLoginField.getQueryCommFlux());
        log.info("登录成功,登录信息： {} ", userLoginInfoDto);
    }

    public void clearCache() {
        accountInfo = new TradingAccountDto();
        userLoginInfoDto = new UserLoginInfoDto();
        positionRecords.clear();
        orderMap.clear();
    }
}
