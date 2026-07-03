#if !defined(QCValueAddProApi_H)
#define QCValueAddProApi_H

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "QCValueAddProApiStruct.h"

#ifdef QY_API_EXPORT
#ifdef WINDOWS
#define QY_API_DLL_EXPORT __declspec(dllexport)
#else
#define QY_API_DLL_EXPORT __attribute__ ((visibility("default")))
#endif
#else
#define QY_API_DLL_EXPORT 
#endif

namespace QCVALUEADDPROAPI
{

	class CQCValueAddProSpi
	{
	public:
		///当客户端与交易后台建立起通信连接时（还未登录前），该方法被调用。
		virtual void OnFrontConnected(){};

		///当客户端与交易后台通信连接断开时，该方法被调用。当发生这个情况后，API会自动重新连接，客户端可不做处理。
		///@param nReason 错误原因
		///        -3 连接已断开
		///        -4 网络读失败
		///        -5 网络写失败
		///        -6 订阅流错误
		///        -7 流序号错误
		///        -8 错误的心跳报文
		///        -9 错误的报文
		virtual void OnFrontDisconnected(int nReason){};

		//登录应答	
		virtual void OnRspUserLogin(CQCVDRspUserLoginField *pRspUserLoginField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//登出应答	
		virtual void OnRspUserLogout(CQCVDUserLogoutField *pUserLogoutField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//查询奇点系统客户登录记录响应	
		virtual void OnRspQrySingularityLoginRecord(CQCVDSingularityLoginRecordField *pSingularityLoginRecordField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		///投资者持仓变动通知
		virtual void OnRtnPosition(CQCVDPositionField *pPosition) {};

		///投资者现货报单变动通知
		virtual void OnRtnOrder(CQCVDOrderField *pOrder) {};

		///投资者现货成交通知
		virtual void OnRtnTrade(CQCVDTradeField *pTrade) {};

		///投资者现货资金变动通知
		virtual void OnRtnTradingAccount(CQCVDTradingAccountField *pTradingAccount) {};

		///订阅投资者现货持仓变动通知的应答
		virtual void OnRspSubRtnPosition(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///取消订阅投资者现货持仓变动通知的应答
		virtual void OnRspUnSubRtnPosition(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅投资者现货报单变动通知的应答
		virtual void OnRspSubRtnOrder(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///取消订阅投资者现货报单变动通知的应答
		virtual void OnRspUnSubRtnOrder(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅投资者现货成交通知的应答
		virtual void OnRspSubRtnTrade(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///取消订阅投资者现货成交通知的应答
		virtual void OnRspUnSubRtnTrade(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅投资者现货资金变动通知的应答
		virtual void OnRspSubRtnTradingAccount(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///取消订阅投资者现货资金变动通知的应答
		virtual void OnRspUnSubRtnTradingAccount(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货查询集中交易系统资金应答	
		virtual void OnRspInquiryJZFund(CQCVDRspInquiryJZFundField *pRspInquiryJZFundField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货查询银行账户余额应答	
		virtual void OnRspInquiryBankAccountFund(CQCVDRspInquiryBankAccountFundField *pRspInquiryBankAccountFundField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货查询当日新股信息应答
		virtual void OnRspQryIPOInfo(CQCVDIPOInfoField *pIPOInfo, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货查询投资者应答
		virtual void OnRspQryInvestor(CQCVDInvestorField *pInvestor, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货查询股东账户应答
		virtual void OnRspQryShareholderAccount(CQCVDShareholderAccountField *pShareholderAccount, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货查询当日报单应答
		virtual void OnRspQryOrder(CQCVDOrderField *pOrder, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货查询当日撤单应答
		virtual void OnRspQryOrderAction(CQCVDOrderActionField *pOrderAction, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货查询当日成交应答
		virtual void OnRspQryTrade(CQCVDTradeField *pTrade, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货分页式查询当日报单应答
		virtual void OnRspQryOrderByPage(CQCVDOrderField *pOrder, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};

		//现货分页式查询当日撤单应答
		virtual void OnRspQryOrderActionByPage(CQCVDOrderActionField *pOrderAction, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};

		//现货分页式查询当日成交应答
		virtual void OnRspQryTradeByPage(CQCVDTradeField *pTrade, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};

		//现货查询当日资金账户应答
		virtual void OnRspQryTradingAccount(CQCVDTradingAccountField *pTradingAccount, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货查询当日投资者持仓应答
		virtual void OnRspQryPosition(CQCVDPositionField *pPosition, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货查询当日新股申购额度应答
		virtual void OnRspQryIPOQuota(CQCVDIPOQuotaField *pIPOQuota, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货查询当日资金转移流水应答
		virtual void OnRspQryFundTransferDetail(CQCVDFundTransferDetailField *pFundTransferDetail, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货查询当日持仓转移流水应答
		virtual void OnRspQryPositionTransferDetail(CQCVDPositionTransferDetailField *pPositionTransferDetail, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货查询当日未到期债券质押回购委托应答
		virtual void OnRspQryPrematurityRepoOrder(CQCVDPrematurityRepoOrderField *pPrematurityRepoOrder, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货查询当日外围系统仓位调拨流水应答
		virtual void OnRspQryPeripheryPositionTransferDetail(CQCVDPeripheryPositionTransferDetailField *pPeripheryPositionTransferDetail, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货查询当日条件单应答
		virtual void OnRspQryCondOrder(CQCVDCondOrderField *pCondOrder, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货查询当日条件单撤单应答
		virtual void OnRspQryCondOrderAction(CQCVDCondOrderActionField *pCondOrderAction, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货查询当日新股申购配号结果应答
		virtual void OnRspQryIPONumberResult(CQCVDIPONumberResultField *pIPONumberResult, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货查询当日新股申购中签结果应答
		virtual void OnRspQryIPOMatchNumberResult(CQCVDIPOMatchNumberResultField *pIPOMatchNumberResult, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货查询当日交易协议应答
		virtual void OnRspQryShareholderSpecPrivilege(CQCVDShareholderSpecPrivilegeField *pShareholderSpecPrivilege, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货查询当日配股配债信息应答
		virtual void OnRspQryRationalInfo(CQCVDRationalInfoField *pRationalInfo, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//现货查询当日外围系统资金调拨流水应答
		virtual void OnRspQryPeripheryFundTransferDetail(CQCVDPeripheryFundTransferDetailField *pPeripheryFundTransferDetail, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
	
		//查询奇点客户实时业务标识应答
		virtual void OnRspQrySingularityRealTimeBuFlag(CQCVDSingularityRealTimeBuFlagField* pSingularityRealTimeBuFlag, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};


		///订阅特定行情应答
		virtual void OnRspSubSpecialMarketData(CQCVDSpecificSecurityField *pSpecificSecurity, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///取消订阅特定行情应答
		virtual void OnRspUnSubSpecialMarketData(CQCVDSpecificSecurityField *pSpecificSecurity, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅资金流向行情应答
		virtual void OnRspSubFundsFlowMarketData(CQCVDSpecificSecurityField *pSpecificSecurity, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///取消订阅资金流向行情应答
		virtual void OnRspUnSubFundsFlowMarketData(CQCVDSpecificSecurityField *pSpecificSecurity, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅行业指数行情应答
		virtual void OnRspSubIndustryIndexData(CQCVDSpecificSecurityField *pSpecificSecurity, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///取消订阅行业指数行情应答
		virtual void OnRspUnSubIndustryIndexData(CQCVDSpecificSecurityField *pSpecificSecurity, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅概念指数行情应答
		virtual void OnRspSubConceptionIndexData(CQCVDSpecificSecurityField *pSpecificSecurity, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///取消订阅概念指数行情应答
		virtual void OnRspUnSubConceptionIndexData(CQCVDSpecificSecurityField *pSpecificSecurity, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅地域指数行情应答
		virtual void OnRspSubRegionIndexData(CQCVDSpecificSecurityField *pSpecificSecurity, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///取消订阅地域指数行情应答
		virtual void OnRspUnSubRegionIndexData(CQCVDSpecificSecurityField *pSpecificSecurity, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅价格异常波动委托明细应答
		virtual void OnRspSubEffectOrderDetail(CQCVDEffectDetailItemField *pEffectDetailItem, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///取消订阅价格异常波动委托明细应答
		virtual void OnRspUnSubEffectOrderDetail(CQCVDSpecificSecurityField *pSpecificSecurity, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅价格异常波动成交明细应答
		virtual void OnRspSubEffectTradeDetail(CQCVDEffectDetailItemField *pEffectDetailItem, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///取消订阅价格异常波动成交明细应答
		virtual void OnRspUnSubEffectTradeDetail(CQCVDSpecificSecurityField *pSpecificSecurity, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		///订阅行业板块涨幅排名
		virtual void OnRspSubIndustryIndexUpRankNData(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///退订行业板块涨幅排名
		virtual void OnRspUnSubIndustryIndexUpRankNData(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅概念板块涨幅排名
		virtual void OnRspSubConceptionIndexUpRankNData(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///退订概念板块涨幅排名
		virtual void OnRspUnSubConceptionIndexUpRankNData(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅行业板块跌幅排名
		virtual void OnRspSubIndustryIndexDownRankNData(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///退订行业板块跌幅排名
		virtual void OnRspUnSubIndustryIndexDownRankNData(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅概念板块跌幅排名
		virtual void OnRspSubConceptionIndexDownRankNData(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///退订概念板块跌幅排名
		virtual void OnRspUnSubConceptionIndexDownRankNData(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅行业板块涨停家数排名
		virtual void OnRspSubIndustryIndexUpperLimitRankNData(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///退订行业板块涨停家数排名
		virtual void OnRspUnSubIndustryIndexUpperLimitRankNData(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅概念板块涨停家数排名
		virtual void OnRspSubConceptionIndexUpperLimitRankNData(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///退订概念板块涨停家数排名
		virtual void OnRspUnSubConceptionIndexUpperLimitRankNData(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅大盘龙头实时数据
		virtual void OnRspSubStockHeaderData(CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		///退订大盘龙头实时数据
		virtual void OnRspUnSubStockHeaderData(CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅股票指数行情
		virtual void OnRspSubStockIndexData(CQCVDSpecificSecurityField* pSpecificSecurity, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		///退订股票指数行情
		virtual void OnRspUnSubStockIndexData(CQCVDSpecificSecurityField* pSpecificSecurity, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};


		///查询自由流通股本数据应答
		virtual void OnRspInquiryFreeFloatShares(CQCVDFreeFloatSharesDataField *pFreeFloatSharesData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		///查询复权信息应答
		virtual void OnRspInquiryRightsAdjustment(CQCVDRightsAdjustmentDataField *pRightsAdjustmentData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询历史资金流向数据应答
		virtual void OnRspInquiryHistoryFundsFlowData(CQCVDHistoryFundsFlowDataField *pHistoryFundsFlowData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询财务指标信息应答
		virtual void OnRspInquiryFinancialIndicatorData(CQCVDFinancialIndicatorDataField *pFinancialIndicatorData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询分红信息应答
		virtual void OnRspInquiryDividendData(CQCVDDividendDataField *pDividendData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询送股配股信息应答
		virtual void OnRspInquiryRightIssueData(CQCVDRightIssueDataField *pRightIssueData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询公司资料信息应答
		virtual void OnRspInquiryCompanyDescriptionData(CQCVDCompanyDescriptionDataField *pCompanyDescriptionData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询股本结构信息应答
		virtual void OnRspInquiryEquityStructureData(CQCVDEquityStructureDataField *pEquityStructureData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询主营业务信息应答
		virtual void OnRspInquirySalesSegmentData(CQCVDSalesSegmentDataField *pSalesSegmentData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询十大股东信息应答
		virtual void OnRspInquiryTopTenHoldersData(CQCVDTopTenHoldersDataField *pTopTenHoldersData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询十大流通股东信息应答
		virtual void OnRspInquiryTopTenFloatHoldersData(CQCVDTopTenFloatHoldersDataField *pTopTenFloatHoldersData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询个股所属行业板块信息应答
		virtual void OnRspInquiryIndustryData(CQCVDIndustryDataField *pIndustryData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询个股所属概念板块信息应答
		virtual void OnRspInquiryConceptionData(CQCVDConceptionDataField *pConceptionData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询个股所属地域板块信息应答
		virtual void OnRspInquiryRegionData(CQCVDRegionDataField *pRegionData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询指数描述信息应答
		virtual void OnRspInquiryIndexDescriptionData(CQCVDIndexDescriptionDataField *pIndexDescriptionData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询行业板块成分股信息应答
		virtual void OnRspInquiryIndustryConstituentsData(CQCVDIndustryConstituentsDataField *pIndustryConstituentsData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询概念板块成分股信息应答
		virtual void OnRspInquiryConceptionConstituentsData(CQCVDConceptionConstituentsDataField *pConceptionConstituentsData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询行业板块代码列表应答
		virtual void OnRspInquiryIndustryCodeListData(CQCVDIndustryCodeListDataField *pIndustryCodeListData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询概念板块代码列表应答
		virtual void OnRspInquiryConceptionCodeListData(CQCVDConceptionCodeListDataField *pConceptionCodeListData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询地域版块代码列表应答
		virtual void OnRspInquiryRegionCodeListData(CQCVDRegionCodeListDataField *pRegionCodeListData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询申万指数代码列表应答
		virtual void OnRspQrySWSCodeListData(CQCVDSWSCodeListDataField *pRegionCodeListData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询WND指数日行情应答
		virtual void OnRspQryWINDIndexDayQuotation(CQCVDIndexDayQuotationField *pIndexDayQuotationField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast){};
		//查询申万指数成分股信息应答
		virtual void OnRspQrySWSIndexConstituentsInfo(CQCVDIndexConstituentsDataField *pIndexConstituentsInfoField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast){};
		//查询申万指数行情应答
		virtual void OnRspInquirySWSIndexData(CQCVDSWSIndexDataField *pSWSIndexData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//股票日K行情请求包应答
		virtual void OnRspInquiryStockDayQuotation(CQCVDStockDayQuotationField *pStockDayQuotation, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//A股交易日历请求包应答
		virtual void OnRspInquiryShareCalendar(CQCVDShareCalendarField *pShareCalendar, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//申万行业分类请求包应答
		virtual void OnRspInquirySWIndustriesClass(CQCVDSWIndustriesClassField *pSWIndustriesClass, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//A股日行情估值指标请求包应答
		virtual void OnRspInquiryStockAssessIndicator(CQCVDStockAssessIndicatorField *pStockAssessIndicator, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//一致预测个股滚动指标请求包应答
		virtual void OnRspInquiryConsensusRollingData(CQCVDConsensusRollingDataField *pConsensusRollingData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//指数日行情请求包应答
		virtual void OnRspInquiryIndexDayQuotation(CQCVDIndexDayQuotationField *pIndexDayQuotation, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//A股基本资料请求包应答
		virtual void OnRspInquiryShareDescription(CQCVDShareDescriptionField *pShareDescription, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//十大股东信息请求包应答
		virtual void OnRspInquiryTopTenHoldersDetail(CQCVDTopTenHoldersDetailField *pTopTenHoldersDetail, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//客户历史周期股票盈亏请求包应答
		virtual void OnRspInquiryHisShareProfit(CQCVDHisShareProfitField *pHisShareProfit, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//根据客户号查询客户资金账户请求包应答
		virtual void OnRspInquiryAccountIDByInvestorID(CQCVDAccountIDByInvestorIDField *pAccountIDByInvestorID, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//客户周期资金请求包应答
		virtual void OnRspInquiryCustPeriodCapiData(CQCVDCustPeriodCapiDataField *pCustPeriodCapiData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//客户周期盈亏请求包应答
		virtual void OnRspInquiryCustPeriodProfitData(CQCVDCustPeriodProfitDataField *pCustPeriodProfitData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//中国A股发行请求包应答
		virtual void OnRspInquiryShareIssuance(CQCVDShareIssuanceField *pShareIssuance, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//中国可转债发行请求包应答
		virtual void OnRspInquiryBondIssuance(CQCVDBondIssuanceField *pBondIssuance, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//中国A股股权质押信息请求包应答
		virtual void OnRspInquiryShareEquityPledgeInfo(CQCVDShareEquityPledgeInfoField *pShareEquityPledgeInfo, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//现货查询历史资金应答
		virtual void OnRspInquiryHistoryCapital(CQCVDHistoryCapitalField *pHistoryCapital, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//现货查询历史交割单应答
		virtual void OnRspInquiryHistoryDelivery(CQCVDHistoryDeliveryField *pHistoryDelivery, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//现货查询历史资金流水应答
		virtual void OnRspInquiryHistoryFundDetail(CQCVDHistoryFundDetailField *pHistoryFundDetail, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//现货查询历史持仓应答
		virtual void OnRspInquiryHistoryHold(CQCVDHistoryHoldField *pHistoryHold, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//现货查询历史委托数据应答
		virtual void OnRspInquiryHistoryOrderEX(CQCVDHistoryOrderEXField *pHistoryOrderEX, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//现货查询当日历史委托数据应答
		virtual void OnRspQryOneDayHistoryOrder(CQCVDOneDayHistoryOrderField* pHistoryOrder, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//现货查询历史成交应答
		virtual void OnRspInquiryHistoryTradeEX(CQCVDHistoryTradeEXField *pHistoryTradeEX, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//录入标记事件应答
		virtual void OnRspInputRemarkEvent(CQCVDRspInputRemarkEventField *pRspInputRemarkEvent, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//更新标记事件应答
		virtual void OnRspUpdateRemarkEvent(CQCVDRspUpdateRemarkEventField *pRspUpdateRemarkEvent, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//删除标记事件应答
		virtual void OnRspDeleteRemarkEvent(CQCVDRspDeleteRemarkEventField *pRspDeleteRemarkEvent, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//查询历史标记事件应答
		virtual void OnRspQryRemarkEvent(CQCVDRemarkEventField *pRemarkEvent, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//查询持仓量价分布信息应答
		virtual void OnRspQryPriceDistributionData(CQCVDPriceDistributionDataField *pPriceDistributionData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//查询历史极值价格信息应答
		virtual void OnRspQryPriceExtremumData(CQCVDPriceExtremumDataField *pPriceExtremumData, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询盘口委托应答
		virtual void OnRspInquiryQueueingOrder(CQCVDQueueingOrderField *pQueueingOrder, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//查询1档盘口数量应答
		virtual void OnRspInquiryFirstLevelVolumes(CQCVDFirstLevelVolumesField *pFirstLevelVolumes, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		//查询有序逐笔行情应答
		virtual void OnRspQrySequencedTickMD(TQCVDSequencedTickMDTypeType cSequencedTickMDType,
			CQCVDTransactionField* pTransaction, CQCVDOrderDetailField* pOrderDetail, CQCVDPHTransactionField* pPHTransaction,
			CQCVDXTSTickField* pXTSTick, CQCVDBondOrderDetailField* pBondOrderDetail, CQCVDBondTransactionField* pBondTransaction,
			CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};

		//查询有序逐笔行情应答
		virtual void OnRspQryNGTSTickSequencedTickMD(TQCVDSequencedTickMDTypeType cLev2SequencedTickMDType,
			CQCVDTransactionField* pTransaction, CQCVDOrderDetailField* pOrderDetail, CQCVDPHTransactionField* pPHTransaction,
			CQCVDXTSTickField* pXTSTick, CQCVDBondOrderDetailField* pBondOrderDetail, CQCVDBondTransactionField* pBondTransaction, CQCVDNGTSTickField* pNGTSTick,
			CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};


		//计算期权希腊值请求包应答
		virtual void OnRspQryOptionGreece(CQCVDRspOptionGreeceField *pRspOptionGreece, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//查询债券日K行情请求应答
		virtual void OnRspInquiryBondDayQuotation(CQCVDBondDayQuotationField *pBondDayQuotation, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询港股通资金流向信息请求应答
		virtual void OnRspInquiryGGTEODPrices(CQCVDGGTEODPricesField *pGGTEODPrices, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		///特定行情通知
		virtual void OnRtnSpecialMarketData(CQCVDSpecialMarketDataField *pSpecialMarketData) {};
		///资金流向行情通知
		virtual void OnRtnFundsFlowMarketData(CQCVDFundsFlowMarketDataField *pFundsFlowMarketData) {};
		///价格波动异常行情通知
		virtual void OnRtnEffectPriceMarketData(CQCVDEffectPriceMarketDataField *pEffectPriceMarketData) {};
		///数量波动异常行情通知
		virtual void OnRtnEffectVolumeMarketData(CQCVDEffectVolumeMarketDataField *pEffectVolumeMarketData) {};
		///价格异常波动委托明细通知
		virtual void OnRtnEffectOrderDetail(CQCVDEffectOrderDetailField *pEffectOrderDetail) {};
		///价格异常波动成交明细通知
		virtual void OnRtnEffectTradeDetail(CQCVDEffectTradeDetailField *pEffectTradeDetail) {};
		///行业指数行情通知
		virtual void OnRtnIndustryIndexData(CQCVDIndustryIndexDataField *pIndustryIndexData) {};
		///概念指数行情通知
		virtual void OnRtnConceptionIndexData(CQCVDConceptionIndexDataField *pConceptionIndexData) {};
		///地域指数行情通知
		virtual void OnRtnRegionIndexData(CQCVDRegionIndexDataField *pRegionIndexData) {};
		///行业板块涨幅排名通知
		virtual void OnRtnIndustryIndexUpRankNData(CQCVDIndexRankNDataField* pIndexRankNData) {};
		///概念板块涨幅排名通知
		virtual void OnRtnConceptionIndexUpRankNData(CQCVDIndexRankNDataField* pIndexRankNData) {};
		///行业板块跌幅排名通知
		virtual void OnRtnIndustryIndexDownRankNData(CQCVDIndexRankNDataField* pIndexRankNData) {};
		///概念板块跌幅排名通知
		virtual void OnRtnConceptionIndexDownRankNData(CQCVDIndexRankNDataField* pIndexRankNData) {};
		///行业板块涨停家数排名通知
		virtual void OnRtnIndustryIndexUpperLimitRankNData(CQCVDIndexRankNDataField* pIndexRankNData) {};
		///概念板块涨停家数排名通知
		virtual void OnRtnConceptionIndexUpperLimitRankNData(CQCVDIndexRankNDataField* pIndexRankNData) {};
		///大盘龙头实时数据通知
		virtual void OnRtnStockHeaderData(CQCVDStockHeaderDataField* pStockHeaderData) {};
		///股票指数行情数据通知
		virtual void OnRtnStockIndexData(CQCVDStockIndexDataField* pStockIndexData) {};

		/// 查询中国可转债转股价格响应
		virtual void OnRspQryCBondConvPrice(CQCVDCBondConvPriceField *pCBondConvPrice, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		/// 查询上市基金日行情/中国封闭式基金日行情响应
		virtual void OnRspQryChinaClosedFund(CQCVDChinaClosedFundField *pChinaClosedFund, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		/// 查询私募基金净值响应
		virtual void OnRspQryChinaHedgeFund(CQCVDChinaHedgeFundField *pChinaHedgeFund, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		/// 查询中国共同基金日净值响应
		virtual void OnRspQryChinaMutualFund(CQCVDChinaMutualFundField *pChinaMutualFund, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		/// 查询中国期权日行情响应
		virtual void OnRspQryChinaOptionEodPrices(CQCVDChinaOptionEodPricesField *pChinaOptionEodPrices, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		/// 查询中国股指期货日行情响应
		virtual void OnRspQryCindexfutureseodPrices(CQCVDCindexfutureseodPricesField *pCindexfutureseodPrices, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		/// 查询货币基金每天净值收益响应
		virtual void OnRspQryCMoneyMarketDailyFIncome(CQCVDCMoneyMarketDailyFIncomeField *pCMoneyMarketDailyFIncome, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		/// 查询商品期货日行情响应
		virtual void OnRspQryCommodityFuturesPrice(CQCVDCommodityFuturesPriceField *pCommodityFuturesPrice, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		/// 查询黄金现货日行情响应
		virtual void OnRspQryGoldSpotPrices(CQCVDGoldSpotPricesField *pGoldSpotPrices, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		/// 查询地域板块成分股响应
		virtual void OnRspInquiryRegionConstituentsData(CQCVDRegionConstituentsDataField *pRegionConstituentsInfo, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		/// 查询中国共同基金分红响应
		virtual void OnRspQryChinaMFDividend(CQCVDChinaMFDividendField *pChinaMFDividend, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		/// 查询中国A股停复牌信息响应
		virtual void OnRspQryAShareTradingSuspension(CQCVDAShareTradingSuspensionField *pAShareTradingSuspension, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		/// 查询中国A股领涨指数响应
		virtual void OnRspQryAShareLeadingIndex(CQCVDAShareLeadingIndexField *pAShareLeadingIndexField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		/// 查询精选中国A股领涨指数响应
		virtual void OnRspQrySelectedAShareLeadingIndex(CQCVDAShareLeadingIndexField *pAShareLeadingIndexField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		/// 查询中国A股连续涨停股票响应
		virtual void OnRspQryAShareConsecutiveUp(CQCVDAShareConsecutiveUpField *pAShareConsecutiveUpField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		/// 查询中国A股证券曾用名信息响应
		virtual void OnRspQryASharePreviousName(CQCVDASharePreviousNameField *pASharePreviousNameField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		/// 查询现货投资者大客户信息响应
		virtual void OnRspQryBigInvestorInfoField(CQCVDBigInvestorInfoField *pBigInvestorInfoField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		/// 查询交易信息统计响应
		virtual void OnRspQryOrderStatistics(CQCVDOrderStatisticsField* pOrderStatisticsField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		/// 申请投资者节点变更响应
		virtual void OnRspInvestorApplyNode(CQCVDReqInvestorApplyNodeField* pReqInvestorApplyNodeField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		/// 查询投资者节点变更响应
		virtual void OnRspQryInvestorApplyNode(CQCVDInvestorApplyNodeField* pInvestorApplyNodeField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		/// 查询银证转账资金调拨处理信息应答
		virtual void OnRspQryBankTransInfo(CQCVDBankTransInfoField* pBankTransInfoField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		///订阅快速股票指数行情应答
		virtual void OnRspSubRapidSecurityIndexData(CQCVDSpecificSecurityField* pSpecificSecurity, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		///取消订阅快速股票指数行情应答
		virtual void OnRspUnSubRapidSecurityIndexData(CQCVDSpecificSecurityField* pSpecificSecurity, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		///快速股票指数行情通知
		virtual void OnRtnRapidSecurityIndexData(CQCVDRapidSecurityIndexDataField* pRapidSecurityIndexData) {};

		//两融查询投资者应答	
		virtual void OnRspQryCreditInvestor(CQCVDCreditInvestorField *pInvestorField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询股东账户应答	
		virtual void OnRspQryCreditShareholderAccount(CQCVDCreditShareholderAccountField *pShareholderAccountField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日资金账户应答	
		virtual void OnRspQryCreditTradingAccount(CQCVDCreditTradingAccountField *pTradingAccountField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日报单应答	
		virtual void OnRspQryCreditOrder(CQCVDCreditOrderField *pOrderField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日撤单应答	
		virtual void OnRspQryCreditCancelOrder(CQCVDCreditCancelOrderField *pCancelOrderField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日成交应答	
		virtual void OnRspQryCreditTrade(CQCVDCreditTradeField *pTradeField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融分页式查询当日报单应答	
		virtual void OnRspQryCreditOrderByPage(CQCVDCreditOrderField *pOrderField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//两融分页式查询当日撤单应答	
		virtual void OnRspQryCreditCancelOrderByPage(CQCVDCreditCancelOrderField *pCancelOrderField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//两融分页式查询当日成交应答	
		virtual void OnRspQryCreditTradeByPage(CQCVDCreditTradeField *pTradeField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//两融查询投资者当日持仓应答	
		virtual void OnRspQryCreditPosition(CQCVDCreditPositionField *pPositionField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//查询当日资金转移流水应答	
		virtual void OnRspQryCreditFundTransferDetail(CQCVDCreditFundTransferDetailField *pFundTransferDetailField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日持仓转移流水应答	
		virtual void OnRspQryCreditPositionTransferDetail(CQCVDCreditPositionTransferDetailField *pPositionTransferDetailField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日信用转移应答
		virtual void OnRspQryCreditCreditTransfer(CQCVDCreditCreditTransferField *pCreditTransferField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日撤销信用转移应答	
		virtual void OnRspQryCreditCancelCreditTransfer(CQCVDCreditCancelCreditTransferField *pCancelCreditTransferField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日投资者融资融券信息应答	
		virtual void OnRspQryCreditInvestorCreditInfo(CQCVDCreditInvestorCreditInfoField *pInvestorCreditInfoField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日投资者利率应答	
		virtual void OnRspQryCreditInvestorCreditInterestRate(CQCVDCreditInvestorCreditInterestRateField *pInvestorCreditInterestRateField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日信用负债应答
		virtual void OnRspQryCreditCreditDebt(CQCVDCreditCreditDebtField *pCreditDebtField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日条件单应答	
		virtual void OnRspQryCreditCondOrder(CQCVDCreditCondOrderField *pCondOrderField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日条件单撤单应答	
		virtual void OnRspQryCreditCancelCondOrder(CQCVDCreditCancelCondOrderField *pCancelCondOrderField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日新股信息应答	
		virtual void OnRspQryCreditIPOInfo(CQCVDCreditIPOInfoField *pIPOInfoField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日新股申购额度应答	
		virtual void OnRspQryCreditIPOQuota(CQCVDCreditIPOQuotaField *pIPOQuotaField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日新股申购配号结果应答	
		virtual void OnRspQryCreditIPONumberResult(CQCVDCreditIPONumberResultField *pIPONumberResultField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日新股申购中签结果应答	
		virtual void OnRspQryCreditIPOMatchNumberResult(CQCVDCreditIPOMatchNumberResultField *pIPOMatchNumberResultField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日投资者实时融资融券信息应答	
		virtual void OnRspQryCreditInvestorRealTimeCreditInfo(CQCVDCreditInvestorRealTimeCreditInfoField *pInvestorRealTimeCreditInfoField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日负债展期应答	
		virtual void OnRspQryCreditDebtExtend(CQCVDCreditDebtExtendField *pDebtExtendField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日外围系统资金转移流水应答	
		virtual void OnRspQryCreditPeripheryFundTransferDetail(CQCVDCreditPeripheryFundTransferDetailField *pPeripheryFundTransferDetailField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日交易协议应答	
		virtual void OnRspQryCreditShareholderSpecPrivilege(CQCVDCreditShareholderSpecPrivilegeField *pShareholderSpecPrivilegeField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日配股配债信息应答	
		virtual void OnRspQryCreditRationalInfo(CQCVDCreditRationalInfoField *pRationalInfoField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询当日外围系统仓位调拨流水应答	
		virtual void OnRspQryCreditPeripheryPositionTransferDetail(CQCVDCreditPeripheryPositionTransferDetailField *pPeripheryPositionTransferDetailField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询集中交易系统资金应答	
		virtual void OnRspInquiryCreditJZFundField(CQCVDCreditRspInquiryJZFundField *pJZFundField, CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询客户历史周期股票盈亏应答
		virtual void OnRspQryCreditHisShareProfit(CQCVDCreditHisShareProfitField* pHisShareProfitField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询历史资金应答
		virtual void OnRspInquiryCreditHistoryCapital(CQCVDCreditHistoryCapitalField* pHistoryCapitalField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//两融查询历史交割单应答
		virtual void OnRspInquiryCreditHistoryDelivery(CQCVDCreditHistoryDeliveryField* pHistoryDeliveryField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//两融查询历史资金流水应答
		virtual void OnRspInquiryCreditHistoryFundDetail(CQCVDCreditHistoryFundDetailField* pHistoryFundDetailField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//两融查询历史持仓应答
		virtual void OnRspInquiryCreditHistoryHold(CQCVDCreditHistoryHoldField* pHistoryHoldField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//两融查询历史委托数据应答
		virtual void OnRspInquiryCreditHistoryOrderEX(CQCVDCreditHistoryOrderEXField* pHistoryOrderEXField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//两融查询历史成交应答
		virtual void OnRspInquiryCreditHistoryTradeEX(CQCVDCreditHistoryTradeEXField* pHistoryTradeEXField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//两融查询投资者周期资金应答
		virtual void OnRspInquiryCreditCustPeriodCapiData(CQCVDCreditCustPeriodCapiDataField* pCustPeriodCapiData, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//两融查询投资者周期盈亏应答
		virtual void OnRspInquiryCreditCustPeriodProfitData(CQCVDCreditCustPeriodProfitDataField* pCustPeriodProfitData, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//两融查询投资者上日负债应答
		virtual void OnRspQryCreditCustLastDayDebt(CQCVDCreditCustLastDayDebtField* pCustLastDayDebtField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//两融查询历史负债信息应答
		virtual void OnRspQryCreditHistoryDebtInfo(CQCVDCreditHistoryDebtInfoField* pHistoryDebtInfoField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};

		///投资者两融持仓变动通知
		virtual void OnRtnCreditPosition(CQCVDCreditPositionField *pPosition) {};

		///投资者两融报单变动通知
		virtual void OnRtnCreditOrder(CQCVDCreditOrderField *pOrder) {};

		///投资者两融成交通知
		virtual void OnRtnCreditTrade(CQCVDCreditTradeField *pTrade) {};

		///投资者两融资金变动通知
		virtual void OnRtnCreditTradingAccount(CQCVDCreditTradingAccountField *pTradingAccount) {};

		///投资者两融实时融资融券信息通知
		virtual void OnRtnCreditInvestorRealTimeCreditInfo(CQCVDCreditInvestorRealTimeCreditInfoField *pInvestorRealTimeCreditInfo) {};

		///订阅投资者两融持仓变动通知的应答
		virtual void OnRspSubRtnCreditPosition(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///取消订阅投资者两融持仓变动通知的应答
		virtual void OnRspUnSubRtnCreditPosition(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅投资者两融报单变动通知的应答
		virtual void OnRspSubRtnCreditOrder(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///取消订阅投资者两融报单变动通知的应答
		virtual void OnRspUnSubRtnCreditOrder(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅投资者两融成交通知的应答
		virtual void OnRspSubRtnCreditTrade(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///取消订阅投资者两融成交通知的应答
		virtual void OnRspUnSubRtnCreditTrade(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅投资者两融资金变动通知的应答
		virtual void OnRspSubRtnCreditTradingAccount(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///取消订阅投资者两融资金变动通知的应答
		virtual void OnRspUnSubRtnCreditTradingAccount(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅投资者两融实时融资融券信息通知的应答
		virtual void OnRspSubRtnCreditInvestorRealTimeCreditInfo(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};
		///取消订阅投资者两融实时融资融券信息通知的应答
		virtual void OnRspUnSubRtnCreditInvestorRealTimeCreditInfo(CQCVDRspInfoField *pRspInfo, int nRequestID, bool bIsLast) {};

		///订阅盯盘通知的应答
		virtual void OnRspSubRtnMarketWatch(CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		///盯盘通知
		virtual void OnRtnMarketWatch(CQCVDRtnMarketWatchField* pMarketWatch) {};

		//查询投资者指纹注册记录应答
		virtual void OnRspQryFingerPrintRegisterRecord(CQCVDFingerPrintRegisterRecordField* pFingerPrintRegisterRecordField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};

		//查询中国可转债衍生指标应答	
		virtual void OnRspQryCCBondValuation(CQCVDCCBondValuationField* pCCBondValuationField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询中国债券基本资料应答包	
		virtual void OnRspQryCBondDescription(CQCVDCBondDescriptionField* pCBondDescriptionField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询中国A股指数估值数据应答包
		virtual void OnRspQryAIndexValuation(CQCVDAIndexValuationField* pAIndexValuationField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询中国A股资产负债表应答包
		virtual void OnRspQryAShareBalanceSheet(CQCVDAShareBalanceSheetField* pAShareBalanceSheetField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询中国A股利润表应答包
		virtual void OnRspQryAShareIncome(CQCVDAShareIncomeField* pAShareIncomeField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询中国A股现金流量表应答包
		virtual void OnRspQryAShareCashFlow(CQCVDAShareCashFlowField* pAShareCashFlowField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询行业板块分类信息应答包
		virtual void OnRspQryIndustryClassInfo(CQCVDIndustryClassInfoField* pIndustryClassInfoField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询ST股票信息应答包
		virtual void OnRspQrySTAShareDescription(CQCVDSTAShareDescriptionField* pSTAShareDescriptionField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询华证债券ESG评级数据应答包
		virtual void OnRspQryEsgBondIndexValue(CQCVDEsgBondIndexValueField* pEsgBondIndexValueField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询华证A股ESG评级数据应答包
		virtual void OnRspQryEsgStockIndexValue(CQCVDEsgStockIndexValueField* pEsgStockIndexValueField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询华证港股ESG评级数据应答包
		virtual void OnRspQryEsgHKStockIndexValue(CQCVDEsgHKStockIndexValueField* pEsgHKStockIndexValueField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询行业板块指数分钟K线数据应答包
		virtual void OnRspQryIndustryIndexMinKData(CQCVDIndustryIndexMinKDataField* pIndustryIndexMinKDataField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询概念板块指数分钟K线数据应答包
		virtual void OnRspQryConceptionIndexMinKData(CQCVDConceptionIndexMinKDataField* pConceptionIndexMinKDataField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询行业板块涨幅排名数据应答包
		virtual void OnRspQryIndustryIndexUpTopNData(CQCVDIndexUpDownTopNDataField* pIndexUpDownTopNDataField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询行业板块跌幅排名数据应答包
		virtual void OnRspQryIndustryIndexDownTopNData(CQCVDIndexUpDownTopNDataField* pIndexUpDownTopNDataField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询概念板块涨幅排名数据应答包
		virtual void OnRspQryConceptionIndexUpTopNData(CQCVDIndexUpDownTopNDataField* pIndexUpDownTopNDataField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询概念板块跌幅排名数据应答包
		virtual void OnRspQryConceptionIndexDownTopNData(CQCVDIndexUpDownTopNDataField* pIndexUpDownTopNDataField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询行业板块热门排名数据应答包
		virtual void OnRspQryIndustryIndexHottestTopNData(CQCVDIndexHottestTopNDataField* pIndexHottestTopNDataField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询概念板块热门排名数据应答包
		virtual void OnRspQryConceptionIndexHottestTopNData(CQCVDIndexHottestTopNDataField* pIndexHottestTopNDataField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询股票指数分钟K线数据应答包
		virtual void OnRspQryStockIndexMinKData(CQCVDStockIndexMinKDataField* pStockIndexMinKDataField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//AH股关联证券查询应答包
		virtual void OnRspQryAHRelatedSecurities(CQCVDAHRelatedSecuritiesField* pAHRelatedSecuritiesField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询共同基金被动型基金跟踪指数应答包
		virtual void OnRspQryChinaMutualFundTrackingIndex(CQCVDChinaMutualFundTrackingIndexField* pChinaMutualFundTrackingIndexField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询中国债券份额变动应答包
		virtual void OnRspQryCBondAmount(CQCVDCBondAmountField* pCBondAmountField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询中国共同基金场内流通份额应答包
		virtual void OnRspQryChinaMutualFundFloatShare(CQCVDChinaMutualFundFloatShareField* pChinaMutualFundFloatShareField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询A股股本应答包
		virtual void OnRspQryAShareCapitalization(CQCVDAShareCapitalizationField* pAShareCapitalizationField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询A股投资评级汇总应答包
		virtual void OnRspQryAShareStockRatingConsus(CQCVDAShareStockRatingConsusField* pAShareStockRatingConsusField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询A股日收益率应答包
		virtual void OnRspQryAShareYield(CQCVDAShareYieldField* pAShareYieldField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询A股盈利预测汇总应答包
		virtual void OnRspQryAShareConsensusData(CQCVDAShareConsensusDataField* pAShareConsensusDataField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询A股中信行业分类应答包
		virtual void OnRspQryAShareIndustriesClassCITICS(CQCVDAShareIndustriesClassCITICSField* pAShareIndustriesClassCITICSField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询A股指数月权重应答包
		virtual void OnRspQryAIndexMonthWeight(CQCVDAIndexMonthWeightField* pAIndexMonthWeightField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询A股盈利预测明细应答包
		virtual void OnRspQryAShareEarningEst(CQCVDAShareEarningEstField* pAShareEarningEstField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询A股投资评级明细应答包
		virtual void OnRspQryAShareStockRating(CQCVDAShareStockRatingField* pAShareStockRatingField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询A股员工构成应答包
		virtual void OnRspQryAShareStaffStructure(CQCVDAShareStaffStructureField* pAShareStaffStructureField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询A股特别处理应答包
		virtual void OnRspQryAShareST(CQCVDAShareSTField* pAShareSTField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询现货投资者多节点信息应答包
		virtual void OnRspQryInvestorNodePosStock(CQCVDInvestorNodePosStockField* pInvestorNodePosStockField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询两融投资者多节点信息应答包
		virtual void OnRspQryInvestorNodePosCredit(CQCVDInvestorNodePosCreditField* pInvestorNodePosCreditField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询港股权益事件应答包
		virtual void OnRspQryHKshareEvent(CQCVDHKshareEventField* pHKshareEventField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//查询A股指数成份股应答包
		virtual void OnRspQryAIndexMembers(CQCVDAIndexMembersField* pAIndexMembersField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};

		//期权查询历史委托数据应答包
		virtual void OnRspQryOptionHistoryOrder(CQCVDOptionHistoryOrderField* pHistoryOrderField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//期权查询历史交割应答包
		virtual void OnRspQryOptionHistoryDelivery(CQCVDOptionHistoryDeliveryField* pHistoryDeliveryField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//期权查询组合策略持仓应答包
		virtual void OnRspQryOptionCombinationStrategyHold(CQCVDOptionCombinationStrategyHoldField* pCombinationStrategyHoldField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//期权查询存管交易申请历史应答包
		virtual void OnRspQryOptionHistoryDepositApply(CQCVDOptionHistoryDepositApplyField* pHistoryDepositApplyField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//期权查询资金明细历史应答包
		virtual void OnRspQryOptionHistoryMoneyDetail(CQCVDOptionHistoryMoneyDetailField* pHistoryMoneyDetailField	, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//期权查询资金信息历史应答包
		virtual void OnRspQryOptionHistoryMoneyInfo(CQCVDOptionHistoryMoneyInfoField* pHistoryMoneyInfoField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//期权查询行情历史应答包
		virtual void OnRspQryOptionHistoryQuotations(CQCVDOptionHistoryQuotationsField* pHistoryQuotationsField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//期权查询合约持仓应答包
		virtual void OnRspQryOptionHistoryVarietyHold(CQCVDOptionHistoryVarietyHoldField* pHistoryVarietyHoldField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};


		//查询投资者应答
		virtual void OnRspQryOptionInvestor(CQCVDOptionInvestorField* pInvestorField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询股东账户应答
		virtual void OnRspQryOptionShareholderAccount(CQCVDOptionShareholderAccountField* pShareholderAccountField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询资金账户应答
		virtual void OnRspQryOptionTradingAccount(CQCVDOptionTradingAccountField* pTradingAccountField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询报单应答
		virtual void OnRspQryOptionOrder(CQCVDOptionOrderField* pOrderField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询成交应答
		virtual void OnRspQryOptionTrade(CQCVDOptionTradeField* pTradeField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询投资者持仓应答
		virtual void OnRspQryOptionPosition(CQCVDOptionPositionField* pPositionField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询资金转移流水应答
		virtual void OnRspQryOptionFundTransferDetail(CQCVDOptionFundTransferDetailField* pFundTransferDetailField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询持仓转移流水应答
		virtual void OnRspQryOptionPositionTransferDetail(CQCVDOptionPositionTransferDetailField* pPositionTransferDetailField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询撤单应答
		virtual void OnRspQryOptionCancelOrder(CQCVDOptionCancelOrderField* pCancelOrderField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询条件单应答
		virtual void OnRspQryOptionCondOrder(CQCVDOptionCondOrderField* pCondOrderField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询条件单撤单应答
		virtual void OnRspQryOptionCancelCondOrder(CQCVDOptionCancelCondOrderField* pCancelCondOrderField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询投资者限仓信息应答
		virtual void OnRspQryOptionInvestorLimitPosition(CQCVDOptionInvestorLimitPositionField* pInvestorLimitPositionField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询报单明细资金应答
		virtual void OnRspQryOptionOrderFundDetail(CQCVDOptionOrderFundDetailField* pOrderFundDetailField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询交易通知应答
		virtual void OnRspQryOptionTradingNotice(CQCVDOptionTradingNoticeField* pTradingNoticeField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询行权应答
		virtual void OnRspQryOptionExercise(CQCVDOptionExerciseField* pExerciseField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询锁定委托应答
		virtual void OnRspQryOptionLock(CQCVDOptionLockField* pLockField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询投资者锁定持仓应答
		virtual void OnRspQryOptionLockPosition(CQCVDOptionLockPositionField* pLockPositionField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询保证金费率应答
		virtual void OnRspQryOptionInvestorMarginFee(CQCVDOptionInvestorMarginFeeField* pInvestorMarginFeeField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询现货持仓转移流水应答
		virtual void OnRspQryOptionStockPositionTransferDetail(CQCVDOptionStockPositionTransferDetailField* pStockPositionTransferDetailField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询投资者现货持仓应答
		virtual void OnRspQryOptionStockPosition(CQCVDOptionStockPositionField* pStockPositionField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询投资者组合持仓应答
		virtual void OnRspQryOptionCombPosition(CQCVDOptionCombPositionField* pCombPositionField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询组合持仓明细应答
		virtual void OnRspQryOptionCombPosDetail(CQCVDOptionCombPosDetailField* pCombPosDetailField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询投资者限额应答
		virtual void OnRspQryOptionInvestorLimitAmount(CQCVDOptionInvestorLimitAmountField* pInvestorLimitAmountField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询组合撤单应答
		virtual void OnRspQryOptionCombOrderAction(CQCVDOptionCombOrderActionField* pCombOrderActionField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询组合委托应答
		virtual void OnRspQryOptionCombOrder(CQCVDOptionCombOrderField* pCombOrderField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询组合行权委托应答
		virtual void OnRspQryOptionCombExercise(CQCVDOptionCombExerciseField* pCombExerciseField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询合并行权撤单应答
		virtual void OnRspQryOptionCombExerciseAction(CQCVDOptionCombExerciseActionField* pCombExerciseActionField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询行权指派明细应答
		virtual void OnRspQryOptionExerciseAppointment(CQCVDOptionExerciseAppointmentField* pExerciseAppointmentField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询证券处置应答
		virtual void OnRspQryOptionStockDisposal(CQCVDOptionStockDisposalField* pStockDisposalField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询证券处置撤单应答
		virtual void OnRspQryOptionStockDisposalAction(CQCVDOptionStockDisposalActionField* pStockDisposalActionField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询锁定撤单应答
		virtual void OnRspQryOptionLockAction(CQCVDOptionLockActionField* pLockActionField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询行权撤单应答
		virtual void OnRspQryOptionExerciseAction(CQCVDOptionExerciseActionField* pExerciseActionField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询组合合约信息应答
		virtual void OnRspQryOptionCombSecurity(CQCVDOptionCombSecurityField* pCombSecurityField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询合约信息应答
		virtual void OnRspQryOptionSecurity(CQCVDOptionSecurityField* pSecurityField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询备兑股份不足仓位应答
		virtual void OnRspQryOptionInsufficientCoveredStockPosition(CQCVDOptionInsufficientCoveredStockPositionField* pInsufficientCoveredStockPositionField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//查询集中交易系统资金应答
		virtual void OnRspInquiryOptionJZFundField(CQCVDOptionRspInquiryJZFundField* pJZFundField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//分页查询报单应答
		virtual void OnRspQryOptionOrderByPage(CQCVDOptionOrderField* pOrderField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//分页查询撤单应答
		virtual void OnRspQryOptionCancelOrderByPage(CQCVDOptionCancelOrderField* pCancelOrderField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};
		//分页查询成交应答
		virtual void OnRspQryOptionTradeByPage(CQCVDOptionTradeField* pTradeField, CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsPageLast, bool bIsTotalLast) {};

		///投资者期权持仓变动通知
		virtual void OnRtnOptionPosition(CQCVDOptionPositionField* pPosition) {};
		///投资者期权报单变动通知
		virtual void OnRtnOptionOrder(CQCVDOptionOrderField* pOrder) {};
		///投资者期权成交通知
		virtual void OnRtnOptionTrade(CQCVDOptionTradeField* pTrade) {};
		///投资者期权资金变动通知
		virtual void OnRtnOptionTradingAccount(CQCVDOptionTradingAccountField* pTradingAccount) {};
		///投资者合并行权推送通知
		virtual void OnRtnOptionCombExercise(CQCVDOptionCombExerciseField* pCombExercise) {};
		///投资者组合委托推送通知
		virtual void OnRtnOptionCombOrder(CQCVDOptionCombOrderField* pCombOrder) {};
		///投资者证券处置推送通知
		virtual void OnRtnOptionStockDisposal(CQCVDOptionStockDisposalField* pStockDisposal) {};
		///投资者行权推送通知
		virtual void OnRtnOptionExercise(CQCVDOptionExerciseField* pExercise) {};
		///投资者锁定推送通知
		virtual void OnRtnOptionLock(CQCVDOptionLockField* pLock) {};

		///订阅投资者期权持仓变动通知的应答
		virtual void OnRspSubRtnOptionPosition(CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		///取消订阅投资者期权持仓变动通知的应答
		virtual void OnRspUnSubRtnOptionPosition(CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅投资者期权报单变动通知的应答
		virtual void OnRspSubRtnOptionOrder(CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		///取消订阅投资者期权报单变动通知的应答
		virtual void OnRspUnSubRtnOptionOrder(CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅投资者期权成交通知的应答
		virtual void OnRspSubRtnOptionTrade(CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		///取消订阅投资者期权成交通知的应答
		virtual void OnRspUnSubRtnOptionTrade(CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		///订阅投资者期权资金变动通知的应答
		virtual void OnRspSubRtnOptionTradingAccount(CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		///取消订阅投资者期权资金变动通知的应答
		virtual void OnRspUnSubRtnOptionTradingAccount(CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//订阅投资者合并行权推送通知的应答
		virtual void OnRspSubRtnOptionCombExercise(CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//取消订阅投资者合并行权推送通知的应答
		virtual void OnRspUnSubRtnOptionCombExercise(CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//订阅投资者组合委托推送通知的应答
		virtual void OnRspSubRtnOptionCombOrder(CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//取消订阅投资者组合委托推送通知的应答
		virtual void OnRspUnSubRtnOptionCombOrder(CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//订阅投资者证券处置推送通知的应答
		virtual void OnRspSubRtnOptionStockDisposal(CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//取消订阅投资者证券处置推送通知的应答
		virtual void OnRspUnSubRtnOptionStockDisposal(CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//订阅投资者行权推送通知的应答
		virtual void OnRspSubRtnOptionExercise(CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//取消订阅投资者行权推送通知的应答
		virtual void OnRspUnSubRtnOptionExercise(CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//订阅投资者锁定推送通知的应答
		virtual void OnRspSubRtnOptionLock(CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
		//取消订阅投资者锁定推送通知的应答
		virtual void OnRspUnSubRtnOptionLock(CQCVDRspInfoField* pRspInfo, int nRequestID, bool bIsLast) {};
	};

	class QY_API_DLL_EXPORT CQCValueAddProApi
	{
	public:
		static CQCValueAddProApi *CreateInfoQryApi();

		///获取API版本号
		///@return 版本号
		static const char* GetApiVersion();

		///删除接口对象本身
		///@remark 不再使用本接口对象时,调用该函数删除接口对象
		virtual void Release() = 0;

		///启动API线程开始工作
		///@param 是否是启动后一直等待模式：如果是，那除非API线程退出，否则此函数一直等待不会退出；否则，启动API线程后，此函数立即退出。
		///@return 线程退出代码
		virtual int Run(bool isto_infinite_wait = true) = 0;

		///注册远端服务器的地址和端口
		///@param address：服务器地址。
		///@remark 服务器地址的格式，例如：“127.0.0.1”。 
		///@param port：服务器端口号。
		///@remark 服务器监听端口号，例如：31000。 
		virtual void RegisterFront(char *address, int port) = 0;

		///注册回调接口
		///@param pSpi 派生自回调接口类的实例
		virtual void RegisterSpi(CQCValueAddProSpi *pSpi) = 0;

		//登录请求	
		virtual int ReqUserLogin(CQCVDReqUserLoginField *pReqUserLoginField, int nRequestID) = 0;

		//登出请求	
		virtual int ReqUserLogout(CQCVDUserLogoutField *pUserLogoutField, int nRequestID) = 0;

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////查询证券现货投资者交易信息 Begin
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		//查询奇点系统客户登录记录	
		virtual int ReqQrySingularityLoginRecord(CQCVDReqQrySingularityLoginRecordField *pReqQrySingularityLoginRecordField, int nRequestID) = 0;

		//现货查询集中交易系统资金请求	
		virtual int ReqInquiryJZFund(CQCVDReqInquiryJZFundField *pReqInquiryJZFundField, int nRequestID) = 0;

		//现货查询银行账户余额请求	
		virtual int ReqInquiryBankAccountFund(CQCVDReqInquiryBankAccountFundField *pReqInquiryBankAccountFundField, int nRequestID) = 0;

		//现货查询当日新股信息	
		virtual int ReqQryIPOInfo(CQCVDQryIPOInfoField *pQryIPOInfoField, int nRequestID) = 0;

		//现货查询投资者	
		virtual int ReqQryInvestor(CQCVDQryInvestorField *pQryInvestorField, int nRequestID) = 0;

		//现货查询股东账户	
		virtual int ReqQryShareholderAccount(CQCVDQryShareholderAccountField *pQryShareholderAccountField, int nRequestID) = 0;

		//现货查询当日报单	
		virtual int ReqQryOrder(CQCVDQryOrderField *pQryOrderField, int nRequestID) = 0;

		//现货查询当日撤单	
		virtual int ReqQryOrderAction(CQCVDQryOrderActionField *pQryOrderActionField, int nRequestID) = 0;

		//现货查询当日成交	
		virtual int ReqQryTrade(CQCVDQryTradeField *pQryTradeField, int nRequestID) = 0;

		//现货分页式查询当日报单	
		virtual int ReqQryOrderByPage(CQCVDQryOrderField *pQryOrderField, int nRequestID) = 0;

		//现货分页式查询当日撤单	
		virtual int ReqQryOrderActionByPage(CQCVDQryOrderActionField *pQryOrderActionField, int nRequestID) = 0;

		//分页式查询当日成交	
		virtual int ReqQryTradeByPage(CQCVDQryTradeField *pQryTradeField, int nRequestID) = 0;

		//现货查询当日资金账户	
		virtual int ReqQryTradingAccount(CQCVDQryTradingAccountField *pQryTradingAccountField, int nRequestID) = 0;

		//现货查询当日投资者持仓	
		virtual int ReqQryPosition(CQCVDQryPositionField *pQryPositionField, int nRequestID) = 0;

		//现货查询当日新股申购额度	
		virtual int ReqQryIPOQuota(CQCVDQryIPOQuotaField *pQryIPOQuotaField, int nRequestID) = 0;

		//现货查询当日资金转移流水	
		virtual int ReqQryFundTransferDetail(CQCVDQryFundTransferDetailField *pQryFundTransferDetailField, int nRequestID) = 0;

		//现货查询当日持仓转移流水	
		virtual int ReqQryPositionTransferDetail(CQCVDQryPositionTransferDetailField *pQryPositionTransferDetailField, int nRequestID) = 0;

		//现货查询当日未到期债券质押回购委托	
		virtual int ReqQryPrematurityRepoOrder(CQCVDQryPrematurityRepoOrderField *pQryPrematurityRepoOrderField, int nRequestID) = 0;

		//现货查询当日外围系统仓位调拨流水	
		virtual int ReqQryPeripheryPositionTransferDetail(CQCVDQryPeripheryPositionTransferDetailField *pQryPeripheryPositionTransferDetailField, int nRequestID) = 0;

		//现货查询当日条件单
		virtual int ReqQryCondOrder(CQCVDQryCondOrderField *pQryCondOrderField, int nRequestID) = 0;

		//现货查询当日条件单撤单	
		virtual int ReqQryCondOrderAction(CQCVDQryCondOrderActionField *pQryCondOrderActionField, int nRequestID) = 0;

		//现货查询当日新股申购配号结果	
		virtual int ReqQryIPONumberResult(CQCVDQryIPONumberResultField *pQryIPONumberResultField, int nRequestID) = 0;

		//现货查询当日新股申购中签结果	
		virtual int ReqQryIPOMatchNumberResult(CQCVDQryIPOMatchNumberResultField *pQryIPOMatchNumberResultField, int nRequestID) = 0;

		//现货查询当日交易协议
		virtual int ReqQryShareholderSpecPrivilege(CQCVDQryShareholderSpecPrivilegeField *pQryShareholderSpecPrivilegeField, int nRequestID) = 0;

		//现货查询当日配股配债信息	
		virtual int ReqQryRationalInfo(CQCVDQryRationalInfoField *pQryRationalInfoField, int nRequestID) = 0;

		//现货查询当日外围系统资金调拨流水	
		virtual int ReqQryPeripheryFundTransferDetail(CQCVDQryPeripheryFundTransferDetailField *pQryPeripheryFundTransferDetailField, int nRequestID) = 0;
	
		//查询奇点客户实时业务标识	
		virtual int ReqQrySingularityRealTimeBuFlag(CQCVDQrySingularityRealTimeBuFlagField* pQrySingularityRealTimeBuFlagField, int nRequestID) = 0;

		//现货查询投资者历史周期股票盈亏
		virtual int ReqReqQryHisShareProfit(CQCVDReqQryHisShareProfitField *pReqQryHisShareProfitField, int nRequestID) = 0;
		//根据客户号查询客户资金账户请求包
		virtual int ReqReqQryAccountIDByInvestorID(CQCVDReqQryAccountIDByInvestorIDField *pReqQryAccountIDByInvestorIDField, int nRequestID) = 0;
		//现货查询投资者周期资金
		virtual int ReqReqQryCustPeriodCapiData(CQCVDReqQryCustPeriodCapiDataField *pReqQryCustPeriodCapiDataField, int nRequestID) = 0;
		//现货查询投资者周期盈亏
		virtual int ReqReqQryCustPeriodProfitData(CQCVDReqQryCustPeriodProfitDataField *pReqQryCustPeriodProfitDataField, int nRequestID) = 0;
		//现货查询历史资金
		virtual int ReqReqQryHistoryCapital(CQCVDReqQryHistoryCapitalField *pReqQryHistoryCapitalField, int nRequestID) = 0;
		//现货查询历史交割单
		virtual int ReqReqQryHistoryDelivery(CQCVDReqQryHistoryDeliveryField *pReqQryHistoryDeliveryField, int nRequestID) = 0;
		//现货查询历史资金流水
		virtual int ReqReqQryHistoryFundDetail(CQCVDReqQryHistoryFundDetailField *pReqQryHistoryFundDetailField, int nRequestID) = 0;
		//现货查询历史持仓
		virtual int ReqReqQryHistoryHold(CQCVDReqQryHistoryHoldField *pReqQryHistoryHoldField, int nRequestID) = 0;
		//现货查询历史委托数据
		virtual int ReqReqQryHistoryOrderEX(CQCVDReqQryHistoryOrderEXField *pReqQryHistoryOrderEXField, int nRequestID) = 0;
		//现货查询当日历史委托数据
		virtual int ReqQryOneDayHistoryOrder(CQCVDReqQryOneDayHistoryOrderField* pReqQryOneDayHistoryOrderField, int nRequestID) = 0;
		//现货查询历史成交
		virtual int ReqReqQryHistoryTradeEX(CQCVDReqQryHistoryTradeEXField *pReqQryHistoryTradeEXField, int nRequestID) = 0;
		//录入标记事件
		virtual int ReqInputRemarkEvent(CQCVDInputRemarkEventField *pInputRemarkEventField, int nRequestID) = 0;
		//更新标记事件
		virtual int ReqUpdateRemarkEvent(CQCVDUpdateRemarkEventField *pUpdateRemarkEventField, int nRequestID) = 0;
		//删除标记事件
		virtual int ReqDeleteRemarkEvent(CQCVDDeleteRemarkEventField *pDeleteRemarkEventField, int nRequestID) = 0;
		//查询历史标记事件
		virtual int ReqQryRemarkEvent(CQCVDQryRemarkEventField *pQryRemarkEventField, int nRequestID) = 0;
		//查询交易信息统计
		virtual int ReqQryOrderStatistics(CQCVDQryOrderStatisticsField* pQryOrderStatisticsField, int nRequestID) = 0;

		//订阅现货投资者持仓变动推送通知
		virtual int SubscribeRtnPosition(int nRequestID) = 0;
		//取消订阅现货投资者持仓变动推送通知
		virtual int UnSubscribeRtnPosition(int nRequestID) = 0;
		//订阅现货投资者报单变动推送通知
		virtual int SubscribeRtnOrder(int nRequestID) = 0;
		//取消订阅现货投资者报单变动推送通知
		virtual int UnSubscribeRtnOrder(int nRequestID) = 0;
		//订阅现货投资者成交推送通知
		virtual int SubscribeRtnTrade(int nRequestID) = 0;
		//取消订阅现货投资者成交推送通知
		virtual int UnSubscribeRtnTrade(int nRequestID) = 0;
		//订阅现货投资者资金变动推送通知
		virtual int SubscribeRtnTradingAccount(int nRequestID) = 0;
		//取消订阅现货投资者资金变动推送通知
		virtual int UnSubscribeRtnTradingAccount(int nRequestID) = 0;

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////查询证券现货投资者交易信息 End
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////查询两融投资者交易信息 Begin
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//两融查询投资者	
		virtual int ReqQryCreditInvestor(CQCVDCreditQryInvestorField *pQryInvestorField, int nRequestID) = 0;
		//两融查询股东账户	
		virtual int ReqQryCreditShareholderAccount(CQCVDCreditQryShareholderAccountField *pQryShareholderAccountField, int nRequestID) = 0;
		//两融查询当日资金账户	
		virtual int ReqQryCreditTradingAccount(CQCVDCreditQryTradingAccountField *pQryTradingAccountField, int nRequestID) = 0;
		//两融查询当日报单	
		virtual int ReqQryCreditOrder(CQCVDCreditQryOrderField *pQryOrderField, int nRequestID) = 0;
		//两融查询当日撤单	
		virtual int ReqQryCreditCancelOrder(CQCVDCreditQryOrderActionField *pQryCancelOrderField, int nRequestID) = 0;
		//两融查询当日成交	
		virtual int ReqQryCreditTrade(CQCVDCreditQryTradeField *pQryTradeField, int nRequestID) = 0;
		//两融分页式查询当日报单	
		virtual int ReqQryCreditOrderByPage(CQCVDCreditQryOrderField *pQryOrderField, int nRequestID) = 0;
		//两融分页式查询当日撤单	
		virtual int ReqQryCreditCancelOrderByPage(CQCVDCreditQryOrderActionField *pQryCancelOrderField, int nRequestID) = 0;
		//两融分页式查询当日成交	
		virtual int ReqQryCreditTradeByPage(CQCVDCreditQryTradeField *pQryTradeField, int nRequestID) = 0;
		//两融查询投资者当日持仓	
		virtual int ReqQryCreditPosition(CQCVDCreditQryPositionField *pQryPositionField, int nRequestID) = 0;
		//两融查询当日资金转移流水	
		virtual int ReqQryCreditFundTransferDetail(CQCVDCreditQryFundTransferDetailField *pQryFundTransferDetailField, int nRequestID) = 0;
		//两融查询当日持仓转移流水	
		virtual int ReqQryCreditPositionTransferDetail(CQCVDCreditQryPositionTransferDetailField *pQryPositionTransferDetailField, int nRequestID) = 0;
		//两融查询当日信用转移	
		virtual int ReqQryCreditCreditTransfer(CQCVDCreditQryCreditTransferField *pQryCreditTransferField, int nRequestID) = 0;
		//两融查询撤销当日信用转移	
		virtual int ReqQryCreditCancelCreditTransfer(CQCVDCreditQryCancelCreditTransferField *pQryCancelCreditTransferField, int nRequestID) = 0;
		//两融查询当日投资者融资融券信息	
		virtual int ReqQryCreditInvestorCreditInfo(CQCVDCreditQryInvestorCreditInfoField *pQryInvestorCreditInfoField, int nRequestID) = 0;
		//两融查询当日投资者利率	
		virtual int ReqQryCreditInvestorCreditInterestRate(CQCVDCreditQryInvestorCreditInterestRateField *pQryInvestorCreditInterestRateField, int nRequestID) = 0;
		//两融查询当日信用负债	
		virtual int ReqQryCreditCreditDebt(CQCVDCreditQryCreditDebtField *pQryCreditDebtField, int nRequestID) = 0;
		//两融查询当日条件单	
		virtual int ReqQryCreditCondOrder(CQCVDCreditQryCondOrderField *pQryCondOrderField, int nRequestID) = 0;
		//两融查询当日条件单撤单	
		virtual int ReqQryCreditCancelCondOrder(CQCVDCreditQryCondOrderActionField *pQrytCancelCondOrderField, int nRequestID) = 0;
		//两融查询当日新股信息	
		virtual int ReqQryCreditIPOInfo(CQCVDCreditQryIPOInfoField *pQryIPOInfoField, int nRequestID) = 0;
		//两融查询当日新股申购额度	
		virtual int ReqQryCreditIPOQuota(CQCVDCreditQryIPOQuotaField *pQryIPOQuotaField, int nRequestID) = 0;
		//两融查询当日新股申购配号结果	
		virtual int ReqQryCreditIPONumberResult(CQCVDCreditQryIPONumberResultField *pQryIPONumberResultField, int nRequestID) = 0;
		//两融查询当日新股申购中签结果	
		virtual int ReqQryCreditIPOMatchNumberResult(CQCVDCreditQryIPOMatchNumberResultField *pQryIPOMatchNumberResultField, int nRequestID) = 0;
		//两融查询当日投资者实时融资融券信息	
		virtual int ReqQryCreditInvestorRealTimeCreditInfo(CQCVDCreditQryInvestorRealTimeCreditInfoField *pQryInvestorRealTimeCreditInfoField, int nRequestID) = 0;
		//两融查询当日负债展期	
		virtual int ReqQryCreditDebtExtend(CQCVDCreditQryDebtExtendField *pQryDebtExtendField, int nRequestID) = 0;
		//两融查询当日外围系统资金转移流水	
		virtual int ReqQryCreditPeripheryFundTransferDetail(CQCVDCreditQryPeripheryFundTransferDetailField *pQryPeripheryFundTransferDetailField, int nRequestID) = 0;
		//两融查询当日交易协议	
		virtual int ReqQryCreditShareholderSpecPrivilege(CQCVDCreditQryShareholderSpecPrivilegeField *pQryShareholderSpecPrivilegeField, int nRequestID) = 0;
		//两融查询当日配股配债信息	
		virtual int ReqQryCreditRationalInfo(CQCVDCreditQryRationalInfoField *pQryRationInfoField, int nRequestID) = 0;
		//两融查询当日外围系统仓位调拨流水	
		virtual int ReqQryCreditPeripheryPositionTransferDetail(CQCVDCreditQryPeripheryPositionTransferDetailField *pQryPeripheryPositionTransferDetailField, int nRequestID) = 0;
		//两融查询集中交易系统资金请求	
		virtual int ReqInquiryCreditJZFundField(CQCVDCreditReqInquiryJZFundField *pQryJZFundField, int nRequestID) = 0;
		//两融查询投资者历史周期股票盈亏
		virtual int ReqQryCreditHisShareProfit(CQCVDCreditQryHisShareProfitField* pQryHisShareProfitField, int nRequestID) = 0;
		//两融查询历史资金
		virtual int ReqQryCreditHistoryCapital(CQCVDCreditQryHistoryCapitalField* pQryHistoryCapitalField, int nRequestID) = 0;
		//两融查询历史交割单
		virtual int ReqQryCreditHistoryDelivery(CQCVDCreditQryHistoryDeliveryField* pQryHistoryDeliveryField, int nRequestID) = 0;
		//两融查询历史资金流水
		virtual int ReqQryCreditHistoryFundDetail(CQCVDCreditQryHistoryFundDetailField* pQryHistoryFundDetailField, int nRequestID) = 0;
		//两融查询历史持仓
		virtual int ReqQryCreditHistoryHold(CQCVDCreditQryHistoryHoldField* pQryHistoryHoldField, int nRequestID) = 0;
		//两融查询历史委托数据
		virtual int ReqQryCreditHistoryOrderEX(CQCVDCreditQryHistoryOrderEXField* pQryHistoryOrderEXField, int nRequestID) = 0;
		//两融查询历史成交
		virtual int ReqQryCreditHistoryTradeEX(CQCVDCreditQryHistoryTradeEXField* pQryHistoryTradeEXField, int nRequestID) = 0;
		//两融查询投资者周期资金
		virtual int ReqQryCreditCustPeriodCapiData(CQCVDCreditQryCustPeriodCapiDataField* pQryCustPeriodCapiDataField, int nRequestID) = 0;
		//两融查询投资者周期盈亏
		virtual int ReqQryCreditCustPeriodProfitData(CQCVDCreditQryCustPeriodProfitDataField* pQryCustPeriodProfitDataField, int nRequestID) = 0;
		//两融查询投资者上日负债
		virtual int ReqQryCreditCustLastDayDebt(CQCVDCreditQryCustLastDayDebtField* pQryCustLastDayDebtField, int nRequestID) = 0;
		//两融查询历史负债信息
		virtual int ReqQryCreditHistoryDebtInfo(CQCVDCreditQryHistoryDebtInfoField* pQryHistoryDebtInfoField, int nRequestID) = 0;


		//订阅两融投资者持仓变动推送通知
		virtual int SubscribeRtnCreditPosition(int nRequestID) = 0;
		//取消订阅两融投资者持仓变动推送通知
		virtual int UnSubscribeRtnCreditPosition(int nRequestID) = 0;
		//订阅两融投资者报单变动推送通知
		virtual int SubscribeRtnCreditOrder(int nRequestID) = 0;
		//取消订阅两融投资者报单变动推送通知
		virtual int UnSubscribeRtnCreditOrder(int nRequestID) = 0;
		//订阅两融投资者成交推送通知
		virtual int SubscribeRtnCreditTrade(int nRequestID) = 0;
		//取消订阅两融投资者成交推送通知
		virtual int UnSubscribeRtnCreditTrade(int nRequestID) = 0;
		//订阅两融投资者资金变动推送通知
		virtual int SubscribeRtnCreditTradingAccount(int nRequestID) = 0;
		//取消订阅两融投资者资金变动推送通知
		virtual int UnSubscribeRtnCreditTradingAccount(int nRequestID) = 0;
		//订阅两融投资者实时融资融券信息推送通知
		virtual int SubscribeRtnCreditInvestorRealTimeCreditInfo(int nRequestID) = 0;
		//取消订阅两融投资者实时融资融券信息推送通知
		virtual int UnSubscribeRtnCreditInvestorRealTimeCreditInfo(int nRequestID) = 0;

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////查询两融投资者交易信息 End
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


		//期权查询历史委托数据请求包
		virtual int ReqQryOptionHistoryOrder(CQCVDOptionQryHistoryOrderField* pQryHistoryOrderField, int nRequestID) = 0;
		//期权查询历史交割单请求包
		virtual int ReqQryOptionHistoryDelivery(CQCVDOptionQryHistoryDeliveryField* pQryHistoryDeliveryField, int nRequestID) = 0;
		//期权查询组合策略持仓请求包
		virtual int ReqQryOptionCombinationStrategyHold(CQCVDOptionQryCombinationStrategyHoldField* pQryCombinationStrategyHoldField, int nRequestID) = 0;
		//期权查询存管交易申请历史请求包
		virtual int ReqQryOptionHistoryDepositApply(CQCVDOptionQryHistoryDepositApplyField* pQryHistoryDepositApplyField, int nRequestID) = 0;
		//期权查询资金明细历史请求包
		virtual int ReqQryOptionHistoryMoneyDetail(CQCVDOptionQryHistoryMoneyDetailField* pQryHistoryMoneyDetailField, int nRequestID) = 0;
		//期权查询资金信息历史请求包
		virtual int ReqQryOptionHistoryMoneyInfo(CQCVDOptionQryHistoryMoneyInfoField* pQryHistoryMoneyInfoField, int nRequestID) = 0;
		//期权查询行情历史请求包
		virtual int ReqQryOptionHistoryQuotations(CQCVDOptionQryHistoryQuotationsField* pQryHistoryQuotationsField, int nRequestID) = 0;
		//期权查询合约持仓请求包
		virtual int ReqQryOptionHistoryVarietyHold(CQCVDOptionQryHistoryVarietyHoldField* pQryHistoryVarietyHoldField, int nRequestID) = 0;


		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////查询公开信息，如行情，证券基本信息等等 Begin
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///订阅特定行情。
		///@param ppSecurityID 合约ID  
		///@param nCount 要订阅/退订行情的合约个数
		///@remark 
		virtual int SubscribeSpecialMarketData(char **ppSecurityID, int nCount, TQCVDExchangeIDType ExchageID) = 0;
		///退订特定行情。
		///@param ppSecurityID 合约ID  
		///@param nCount 要订阅/退订行情的合约个数
		///@remark 
		virtual int UnSubscribeSpecialMarketData(char **ppSecurityID, int nCount, TQCVDExchangeIDType ExchageID) = 0;
		///订阅资金流向行情。
		///@param ppSecurityID 合约ID
		///@param nCount 要订阅/退订行情的合约个数
		///@remark
		virtual int SubscribeFundsFlowMarketData(char **ppSecurityID, int nCount, TQCVDExchangeIDType ExchageID) = 0;
		///退订资金流向行情。
		///@param ppSecurityID 合约ID
		///@param nCount 要订阅/退订行情的合约个数
		///@remark
		virtual int UnSubscribeFundsFlowMarketData(char **ppSecurityID, int nCount, TQCVDExchangeIDType ExchageID) = 0;
		///订阅行业指数行情。
		///@param ppSecurityID 合约ID
		///@param nCount 要订阅/退订行情的合约个数
		///@remark
		virtual int SubscribeIndustryIndexData(char **ppSecurityID, int nCount) = 0;
		///退订行业指数行情。
		///@param ppSecurityID 合约ID
		///@param nCount 要订阅/退订行情的合约个数
		///@remark
		virtual int UnSubscribeIndustryIndexData(char **ppSecurityID, int nCount) = 0;
		///订阅概念指数行情。
		///@param ppSecurityID 合约ID
		///@param nCount 要订阅/退订行情的合约个数
		///@remark
		virtual int SubscribeConceptionIndexData(char **ppSecurityID, int nCount) = 0;
		///退订概念指数行情。
		///@param ppSecurityID 合约ID
		///@param nCount 要订阅/退订行情的合约个数
		///@remark
		virtual int UnSubscribeConceptionIndexData(char **ppSecurityID, int nCount) = 0;
		///订阅地域指数行情。
		///@param ppSecurityID 合约ID
		///@param nCount 要订阅/退订行情的合约个数
		///@remark
		virtual int SubscribeRegionIndexData(char **ppSecurityID, int nCount) = 0;
		///退订地域指数行情。
		///@param ppSecurityID 合约ID
		///@param nCount 要订阅/退订行情的合约个数
		///@remark
		virtual int UnSubscribeRegionIndexData(char **ppSecurityID, int nCount) = 0;
		///订阅价格波动异常委托明细
		///@param ExchangeID 交易所代码
		///@param SecurityID 合约代码
		///@param Ratio 波动幅度
		///@remark
		virtual int SubscribeEffectOrderDetail(TQCVDExchangeIDType ExchangeID, TQCVDSecurityIDType SecurityID, TQCVDRatioType Ratio) = 0;
		///退订价格波动异常委托明细
		///@param ExchangeID 交易所代码
		///@param SecurityID 合约代码
		///@remark
		virtual int UnSubscribeEffectOrderDetail(TQCVDExchangeIDType ExchangeID, TQCVDSecurityIDType SecurityID) = 0;
		///订阅价格波动异常成交明细
		///@param ExchangeID 交易所代码
		///@param SecurityID 合约代码
		///@param Ratio 波动幅度
		///@remark
		virtual int SubscribeEffectTradeDetail(TQCVDExchangeIDType ExchangeID, TQCVDSecurityIDType SecurityID, TQCVDRatioType Ratio) = 0;
		///退订价格波动异常成交明细
		///@param ExchangeID 交易所代码
		///@param SecurityID 合约代码
		///@remark
		virtual int UnSubscribeEffectTradeDetail(TQCVDExchangeIDType ExchangeID, TQCVDSecurityIDType SecurityID) = 0;
		
		///订阅行业板块涨幅排名
		virtual int SubscribeIndustryIndexUpRankNData(int nRequestID) = 0;
		///退订行业板块涨幅排名
		virtual int UnSubscribeIndustryIndexUpRankNData(int nRequestID) = 0;
		///订阅概念板块涨幅排名
		virtual int SubscribeConceptionIndexUpRankNData(int nRequestID) = 0;
		///退订概念板块涨幅排名
		virtual int UnSubscribeConceptionIndexUpRankNData(int nRequestID) = 0;
		///订阅行业板块跌幅排名
		virtual int SubscribeIndustryIndexDownRankNData(int nRequestID) = 0;
		///退订行业板块跌幅排名
		virtual int UnSubscribeIndustryIndexDownRankNData(int nRequestID) = 0;
		///订阅概念板块跌幅排名
		virtual int SubscribeConceptionIndexDownRankNData(int nRequestID) = 0;
		///退订概念板块跌幅排名
		virtual int UnSubscribeConceptionIndexDownRankNData(int nRequestID) = 0;
		///订阅行业板块涨停家数排名
		virtual int SubscribeIndustryIndexUpperLimitRankNData(int nRequestID) = 0;
		///退订行业板块涨停家数排名
		virtual int UnSubscribeIndustryIndexUpperLimitRankNData(int nRequestID) = 0;
		///订阅概念板块涨停家数排名
		virtual int SubscribeConceptionIndexUpperLimitRankNData(int nRequestID) = 0;
		///退订概念板块涨停家数排名
		virtual int UnSubscribeConceptionIndexUpperLimitRankNData(int nRequestID) = 0;
		///订阅大盘龙头实时数据
		virtual int SubscribeStockHeaderData(int nRequestID) = 0;
		///退订大盘龙头实时数据
		virtual int UnSubscribeStockHeaderData(int nRequestID) = 0;
		///订阅股票指数行情
		virtual int SubscribeStockIndexData(TQCVDExchangeIDType ExchangeID, TQCVDSecurityIDType SecurityID) = 0;
		///退订股票指数行情
		virtual int UnSubscribeStockIndexData(TQCVDExchangeIDType ExchangeID, TQCVDSecurityIDType SecurityID) = 0;

		//查询自由流通股本信息
		virtual int ReqQryFreeFloatSharesInfo(CQCVDQryFreeFloatSharesInfoField *pReqQryFreeFloatSharesInfoField, int nRequestID) = 0;
		//查询复权信息
		virtual int ReqQryRightsAdjustmentInfo(CQCVDQryRightsAdjustmentInfoField *pQryRightsAdjustmentInfoField, int nRequestID) = 0;
		//查询历史资金流向数据
		virtual int ReqQryHistoryFundsFlowInfo(CQCVDQryHistoryFundsFlowInfoField *pQryHistoryFundsFlowInfoField, int nRequestID) = 0;
		//查询财务指标信息
		virtual int ReqQryFinancialIndicatorInfo(CQCVDQryFinancialIndicatorInfoField *pQryFinancialIndicatorInfoField, int nRequestID) = 0;
		//查询分红信息
		virtual int ReqQryDividendInfo(CQCVDQryDividendInfoField *pQryDividendInfoField, int nRequestID) = 0;
		//查询送股配股信息
		virtual int ReqQryRightIssueInfo(CQCVDQryRightIssueInfoField *pQryRightIssueInfoField, int nRequestID) = 0;
		//查询公司资料信息
		virtual int ReqQryCompanyDescriptionInfo(CQCVDQryCompanyDescriptionInfoField *pQryCompanyDescriptionInfoField, int nRequestID) = 0;
		//查询股本结构信息
		virtual int ReqQryEquityStructureInfo(CQCVDQryEquityStructureInfoField *pQryEquityStructureInfoField, int nRequestID) = 0;
		//查询主营业务信息
		virtual int ReqQrySalesSegmentInfo(CQCVDQrySalesSegmentInfoField *pQrySalesSegmentInfoField, int nRequestID) = 0;
		//查询十大股东信息
		virtual int ReqQryTopTenHoldersInfo(CQCVDQryTopTenHoldersInfoField *pQryTopTenHoldersInfoField, int nRequestID) = 0;
		//查询十大流通股东信息
		virtual int ReqQryTopTenFloatHoldersInfo(CQCVDQryTopTenFloatHoldersInfoField *pQryTopTenFloatHoldersInfoField, int nRequestID) = 0;
		//查询个股所属行业板块信息
		virtual int ReqQryIndustryInfo(CQCVDQryIndustryInfoField *pQryIndustryInfoField, int nRequestID) = 0;
		//查询个股所属概念板块信息
		virtual int ReqQryConceptionInfo(CQCVDQryConceptionInfoField *pQryConceptionInfoField, int nRequestID) = 0;
		//查询个股所属地域板块信息
		virtual int ReqQryRegionInfo(CQCVDQryRegionInfoField *pQryRegionInfoField, int nRequestID) = 0;
		//查询指数描述信息
		virtual int ReqQryIndexDescriptionInfo(CQCVDQryIndexDescriptionInfoField *pQryIndexDescriptionInfoField, int nRequestID) = 0;
		//查询行业板块成分股信息
		virtual int ReqQryIndustryConstituentsInfo(CQCVDQryIndustryConstituentsInfoField *pQryIndustryConstituentsInfoField, int nRequestID) = 0;
		//查询概念板块成分股信息
		virtual int ReqQryConceptionConstituentsInfo(CQCVDQryConceptionConstituentsInfoField *pQryConceptionConstituentsInfoField, int nRequestID) = 0;
		//查询行业板块代码列表
		virtual int ReqQryIndustryCodeList(CQCVDQryIndustryCodeListField *pQryIndustryCodeListField, int nRequestID) = 0;
		//查询概念板块代码列表
		virtual int ReqQryConceptionCodeList(CQCVDQryConceptionCodeListField *pQryConceptionCodeListField, int nRequestID) = 0;
		//查询地域板块代码列表
		virtual int ReqQryRegionCodeList(CQCVDQryRegionCodeListField *pQryRegionCodeListField, int nRequestID) = 0;
		//查询申万指数代码列表
		virtual int ReqQrySWSCodeList(CQCVDQrySWSCodeListField *pQrySWSCodeListField, int nRequestID) = 0;
		//查询WND指数日行情
		virtual int ReqQryWINDIndexDayQuotation(CQCVDReqQryIndexDayQuotationField *pReqQryIndexDayQuotationField, int nRequestID) = 0;
		//查询申万指数成分股信息
		virtual int ReqQrySWSIndexConstituentsInfo(CQCVDQryIndexConstituentsInfoField *pQryIndexConstituentsInfoField, int nRequestID) = 0;
		//查询申万指数行情
		virtual int ReqReqQrySWSIndexData(CQCVDReqQrySWSIndexDataField *pReqQrySWSIndexDataField, int nRequestID) = 0;
		//股票日K行情请求包
		virtual int ReqReqQryStockDayQuotation(CQCVDReqQryStockDayQuotationField *pReqQryStockDayQuotationField, int nRequestID) = 0;
		//A股交易日历请求包
		virtual int ReqReqQryShareCalendar(CQCVDReqQryShareCalendarField *pReqQryShareCalendarField, int nRequestID) = 0;
		//申万行业分类请求包
		virtual int ReqReqQrySWIndustriesClass(CQCVDReqQrySWIndustriesClassField *pReqQrySWIndustriesClassField, int nRequestID) = 0;
		//A股日行情估值指标请求包
		virtual int ReqReqQryStockAssessIndicator(CQCVDReqQryStockAssessIndicatorField *pReqQryStockAssessIndicatorField, int nRequestID) = 0;
		//一致预测个股滚动指标请求包
		virtual int ReqReqQryConsensusRollingData(CQCVDReqQryConsensusRollingDataField *pReqQryConsensusRollingDataField, int nRequestID) = 0;
		//指数日行情请求包
		virtual int ReqReqQryIndexDayQuotation(CQCVDReqQryIndexDayQuotationField *pReqQryIndexDayQuotationField, int nRequestID) = 0;
		//A股基本资料请求包
		virtual int ReqReqQryShareDescription(CQCVDReqQryShareDescriptionField *pReqQryShareDescriptionField, int nRequestID) = 0;
		//十大股东信息请求包
		virtual int ReqReqQryTopTenHoldersDetail(CQCVDReqQryTopTenHoldersDetailField *pReqQryTopTenHoldersDetailField, int nRequestID) = 0;
		//中国A股发行请求包
		virtual int ReqReqQryShareIssuance(CQCVDReqQryShareIssuanceField *pReqQryShareIssuanceField, int nRequestID) = 0;
		//中国可转债发行请求包
		virtual int ReqReqQryBondIssuance(CQCVDReqQryBondIssuanceField *pReqQryBondIssuanceField, int nRequestID) = 0;
		//中国A股股权质押信息请求包
		virtual int ReqReqQryShareEquityPledgeInfo(CQCVDReqQryShareEquityPledgeInfoField *pReqQryShareEquityPledgeInfoField, int nRequestID) = 0;
		//查询持仓量价分布信息
		virtual int ReqQryPriceDistributionInfo(CQCVDQryPriceDistributionInfoField *pQryPriceDistributionInfoField, int nRequestID) = 0;
		//查询盘口委托
		virtual int ReqInquiryQueueingOrders(CQCVDInquiryQueueingOrdersField *pInquiryQueueingOrdersField, int nRequestID) = 0;
		//查询1档盘口委托数量
		virtual int ReqInquiryFirstLevelVolumes(CQCVDInquiryFirstLevelVolumesField *pInquiryFirstLevelVolumesField, int nRequestID) = 0;
		//查询有序逐笔行情
		virtual int ReqQrySequencedTickMD(CQCVDReqQrySequencedTickMDField* pReqQrySequencedTickMDField, int nRequestID) = 0;

		//查询有序逐笔行情
		virtual int ReqQryNGTStickSequencedTickMD(CQCVDReqQryNGTSTickSequencedTickMDField* pReqQrySequencedTickMDField, int nRequestID) = 0;

		//计算期权希腊值请求包
		virtual int ReqQryOptionGreece(CQCVDReqOptionGreeceField *pReqOptionGreeceField, int nRequestID) = 0;
		//查询债券日K行情请求
		virtual int ReqQryBondDayQuotation(CQCVDQryBondDayQuotationField *pQryBondDayQuotationField, int nRequestID) = 0;
		//查询港股通资金流向信息请求
		virtual int ReqQryGGTEODPrices(CQCVDQryGGTEODPricesField *pQryGGTEODPricesField, int nRequestID) = 0;
		/// 查询中国可转债转股价格请求
		virtual int ReqQryCBondConvPrice(CQCVDQryCBondConvPriceField *pQryGGTEODPricesField, int nRequestID) = 0;
		/// 查询上市基金日行情/中国封闭式基金日行情请求
		virtual int ReqQryChinaClosedFund(CQCVDQryChinaClosedFundField *pQryChinaClosedFundField, int nRequestID) = 0;
		/// 查询私募基金净值请求
		virtual int ReqQryChinaHedgeFund(CQCVDQryChinaHedgeFundField *pQryChinaHedgeFundField, int nRequestID) = 0;
		/// 查询中国共同基金日净值请求
		virtual int ReqQryChinaMutualFund(CQCVDQryChinaMutualFundField *pQryChinaMutualFundField, int nRequestID) = 0;
		/// 查询中国期权日行情请求
		virtual int ReqQryChinaOptionEodPrices(CQCVDQryChinaOptionEodPricesField *pQryChinaOptionEodPricesField, int nRequestID) = 0;
		/// 查询中国股指期货日行情请求
		virtual int ReqQryCindexfutureseodPrices(CQCVDQryCindexfutureseodPricesField *pQryCindexfutureseodPricesField, int nRequestID) = 0;
		/// 查询货币基金每天净值收益请求
		virtual int ReqQryCMoneyMarketDailyFIncome(CQCVDQryCMoneyMarketDailyFIncomeField *pQryCMoneyMarketDailyFIncomeField, int nRequestID) = 0;
		/// 查询商品期货日行情请求
		virtual int ReqQryCommodityFuturesPrice(CQCVDQryCommodityFuturesPriceField *pQryCommodityFuturesPriceField, int nRequestID) = 0;
		/// 查询黄金现货日行情请求
		virtual int ReqQryGoldSpotPrices(CQCVDQryGoldSpotPricesField *pQryGoldSpotPricesField, int nRequestID) = 0;
		/// 查询地域板块成分股请求
		virtual int ReqQryRegionConstituentsInfo(CQCVDQryRegionConstituentsInfoField *pQryRegionConstituentsInfoField, int nRequestID) = 0;
		/// 查询中国共同基金分红请求
		virtual int ReqQryChinaMFDividend(CQCVDQryChinaMFDividendField *pQryChinaMFDividendField, int nRequestID) = 0;
		/// 查询中国A股停复牌信息
		virtual int ReqQryAShareTradingSuspension(CQCVDQryAShareTradingSuspensionField *pQryAShareTradingSuspensionField, int nRequestID) = 0;
		/// 查询中国A股领涨指数
		virtual int ReqQryAShareLeadingIndex(CQCVDQryAShareLeadingIndexField *pQryAShareLeadingIndexField, int nRequestID) = 0;
		/// 查询精选中国A股领涨指数
		virtual int ReqQrySelectedAShareLeadingIndex(CQCVDQryAShareLeadingIndexField *pQryAShareLeadingIndexField, int nRequestID) = 0;
		/// 查询中国A股连续涨停股票
		virtual int ReqQryAShareConsecutiveUp(CQCVDQryAShareConsecutiveUpField *pQryAShareConsecutiveUpField, int nRequestID) = 0;
		/// 查询中国A股证券曾用名信息
		virtual int ReqQryASharePreviousName(CQCVDReqQryASharePreviousNameField *pReqQryASharePreviousNameField, int nRequestID) = 0;
		/// 查询现货投资者大客户信息
		virtual int ReqQryBigInvestorInfo(CQCVDQryBigInvestorInfoField *pReqQryBigInvestorInfoField, int nRequestID) = 0;
		/// 申请投资者节点变更
		virtual int ReqInvestorApplyNode(CQCVDReqInvestorApplyNodeField* pReqInvestorApplyNodeField, int nRequestID) = 0;
		/// 查询投资者节点变更
		virtual int ReqQryInvestorApplyNode(CQCVDQryInvestorApplyNodeField* pQryInvestorApplyNodeField, int nRequestID) = 0;
		/// 查询银证转账资金调拨处理信息
		virtual int ReqQryBankTransInfo(CQCVDQryBankTransInfoField* pQryBankTransInfoField, int nRequestID) = 0;
		/// 查询中国可转债衍生指标信息
		virtual int ReqQryCCBondValuation(CQCVDQryCCBondValuationField* pQryCCBondValuationField, int nRequestID) = 0;
		/// 查询中国债券基本资料请求包
		virtual int ReqQryCBondDescription(CQCVDQryCBondDescriptionField* pQryCBondDescriptionField, int nRequestID) = 0;
		/// 查询中国A股指数估值数据请求包
		virtual int ReqQryAIndexValuation(CQCVDQryAIndexValuationField* pQryAIndexValuationField, int nRequestID) = 0;
		/// 查询中国A股资产负债表请求包
		virtual int ReqQryAShareBalanceSheet(CQCVDQryAShareBalanceSheetField* pQryAShareBalanceSheetField, int nRequestID) = 0;
		/// 查询中国A股利润表请求包
		virtual int ReqQryAShareIncome(CQCVDQryAShareIncomeField* pQryAShareIncomeField, int nRequestID) = 0;
		/// 查询中国A股现金流量表请求包
		virtual int ReqQryAShareCashFlow(CQCVDQryAShareCashFlowField* pQryAShareCashFlowField, int nRequestID) = 0;
		/// 查询行业板块分类信息请求包
		virtual int ReqQryIndustryClassInfo(CQCVDQryIndustryClassInfoField* pQryIndustryClassInfoField, int nRequestID) = 0;
		//查询ST股票信息请求包
		virtual int ReqQrySTAShareDescription(CQCVDQrySTAShareDescriptionField* pQrySTAShareDescriptionField, int nRequestID) = 0;

		///订阅盯盘通知请求
		virtual int SubRtnMarketWatch(CQCVDSubRtnMarketWatchField* pSubRtnMarketWatchField, int nRequestID) = 0;

		//查询投资者指纹注册记录
		virtual int ReqQryFingerPrintRegisterRecord(CQCVDReqQryFingerPrintRegisterRecordField* pReqQryFingerPrintRegisterRecordField, int nRequestID) = 0;
		//查询华证债券ESG评级数据请求包
		virtual int ReqQryEsgBondIndexValue(CQCVDQryEsgBondIndexValueField* pQryEsgBondIndexValueField, int nRequestID) = 0;
		//查询华证A股ESG评级数据请求包
		virtual int ReqQryEsgStockIndexValue(CQCVDQryEsgStockIndexValueField* pQryEsgStockIndexValueField, int nRequestID) = 0;
		//查询华证港股ESG评级数据请求包
		virtual int ReqQryEsgHKStockIndexValue(CQCVDQryEsgHKStockIndexValueField* pQryEsgHKStockIndexValueField, int nRequestID) = 0;
		///订阅快速股票指数行情
		virtual int SubscribeRapidSecurityIndexData(TQCVDExchangeIDType ExchangeID, TQCVDSecurityIDType SecurityID) = 0;
		///退订快速股票指数行情
		virtual int UnSubscribeRapidSecurityIndexData(TQCVDExchangeIDType ExchangeID, TQCVDSecurityIDType SecurityID) = 0;
		//查询行业板块指数分钟K线数据请求包
		virtual int ReqQryIndustryIndexMinKData(CQCVDQryIndustryIndexMinKDataField* pQryIndustryIndexMinKDataField, int nRequestID) = 0;
		//查询概念板块指数分钟K线数据请求包
		virtual int ReqQryConceptionIndexMinKData(CQCVDQryConceptionIndexMinKDataField* pQryConceptionIndexMinKDataField, int nRequestID) = 0;
		//查询行业板块涨幅排名数据请求包
		virtual int ReqQryIndustryIndexUpTopNData(CQCVDQryIndexUpDownTopNDataField* pQryIndexUpDownTopNDataField, int nRequestID) = 0;
		//查询行业板块跌幅排名数据请求包
		virtual int ReqQryIndustryIndexDownTopNData(CQCVDQryIndexUpDownTopNDataField* pQryIndexUpDownTopNDataField, int nRequestID) = 0;
		//查询概念板块涨幅排名数据请求包
		virtual int ReqQryConceptionIndexUpTopNData(CQCVDQryIndexUpDownTopNDataField* pQryIndexUpDownTopNDataField, int nRequestID) = 0;
		//查询概念板块跌幅排名数据请求包
		virtual int ReqQryConceptionIndexDownTopNData(CQCVDQryIndexUpDownTopNDataField* pQryIndexUpDownTopNDataField, int nRequestID) = 0;
		//查询行业板块热门排名数据请求包
		virtual int ReqQryIndustryIndexHottestTopNData(int nRequestID) = 0;
		//查询概念板块热门排名数据请求包
		virtual int ReqQryConceptionIndexHottestTopNData(int nRequestID) = 0;
		//查询股票指数分钟K线数据请求包
		virtual int ReqQryStockIndexMinKData(CQCVDQryStockIndexMinKDataField* pQryStockIndexMinKDataField, int nRequestID) = 0;
		//AH股关联证券查询请求包
		virtual int ReqQryAHRelatedSecurities(CQCVDQryAHRelatedSecuritiesField* pQryAHRelatedSecuritiesField, int nRequestID) = 0;
		//查询共同基金被动型基金跟踪指数请求包
		virtual int ReqQryChinaMutualFundTrackingIndex(CQCVDQryChinaMutualFundTrackingIndexField* pQryChinaMutualFundTrackingIndexField, int nRequestID) = 0;
		//查询中国债券份额变动请求包
		virtual int ReqQryCBondAmount(CQCVDQryCBondAmountField* pQryCBondAmountField, int nRequestID) = 0;
		//查询中国共同基金场内流通份额请求包
		virtual int ReqQryChinaMutualFundFloatShare(CQCVDQryChinaMutualFundFloatShareField* pQryChinaMutualFundFloatShareField, int nRequestID) = 0;
		//查询A股股本请求包
		virtual int ReqQryAShareCapitalization(CQCVDQryAShareCapitalizationField* pQryAShareCapitalizationField, int nRequestID) = 0;
		//查询A股投资评级汇总请求包
		virtual int ReqQryAShareStockRatingConsus(CQCVDQryAShareStockRatingConsusField* pQryAShareStockRatingConsusField, int nRequestID) = 0;
		//查询A股日收益率请求包
		virtual int ReqQryAShareYield(CQCVDQryAShareYieldField* pQryAShareYieldField, int nRequestID) = 0;
		//查询A股盈利预测汇总请求包
		virtual int ReqQryAShareConsensusData(CQCVDQryAShareConsensusDataField* pQryAShareConsensusDataField, int nRequestID) = 0;
		//查询A股中信行业分类请求包
		virtual int ReqQryAShareIndustriesClassCITICS(CQCVDQryAShareIndustriesClassCITICSField* pQryAShareIndustriesClassCITICSField, int nRequestID) = 0;
		//查询A股指数月权重请求包
		virtual int ReqQryAIndexMonthWeight(CQCVDQryAIndexMonthWeightField* pQryAIndexMonthWeightField, int nRequestID) = 0;
		//查询A股盈利预测明细请求包
		virtual int ReqQryAShareEarningEst(CQCVDQryAShareEarningEstField* pQryAShareEarningEstField, int nRequestID) = 0;
		//查询A股投资评级明细请求包
		virtual int ReqQryAShareStockRating(CQCVDQryAShareStockRatingField* pQryAShareStockRatingField, int nRequestID) = 0;
		//查询A股员工构成请求包
		virtual int ReqQryAShareStaffStructure(CQCVDQryAShareStaffStructureField* pQryAShareStaffStructureField, int nRequestID) = 0;
		//查询A股特别处理请求包
		virtual int ReqQryAShareST(CQCVDQryAShareSTField* pQryAShareSTField, int nRequestID) = 0;
		//查询现货投资者多节点信息请求包
		virtual int ReqQryInvestorNodePosStock(CQCVDQryInvestorNodePosStockField* pQryInvestorNodePosStockField, int nRequestID) = 0;
		//查询两融投资者多节点信息请求包
		virtual int ReqQryInvestorNodePosCredit(CQCVDQryInvestorNodePosCreditField* pQryInvestorNodePosCreditField, int nRequestID) = 0;
		//查询港股权益事件请求包
		virtual int ReqQryHKshareEvent(CQCVDQryHKshareEventField* pQryHKshareEventField, int nRequestID) = 0;
		//查询A股指数成份股请求包
		virtual int ReqQryAIndexMembers(CQCVDQryAIndexMembersField* pQryAIndexMembersField, int nRequestID) = 0;

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////查询公开信息，如行情，证券基本信息等等 End
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////查询期权投资者交易信息 Begin
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		//查询投资者
		virtual int ReqQryOptionInvestor(CQCVDOptionQryInvestorField* pQryInvestorField, int nRequestID) = 0;
		//查询股东账户
		virtual int ReqQryOptionShareholderAccount(CQCVDOptionQryShareholderAccountField* pQryShareholderAccountField, int nRequestID) = 0;
		//查询资金账户
		virtual int ReqQryOptionTradingAccount(CQCVDOptionQryTradingAccountField* pQryTradingAccountField, int nRequestID) = 0;
		//查询报单
		virtual int ReqQryOptionOrder(CQCVDOptionQryOrderField* pQryOrderField, int nRequestID) = 0;
		//查询成交
		virtual int ReqQryOptionTrade(CQCVDOptionQryTradeField* pQryTradeField, int nRequestID) = 0;
		//查询投资者持仓
		virtual int ReqQryOptionPosition(CQCVDOptionQryPositionField* pQryPositionField, int nRequestID) = 0;
		//查询资金转移流水
		virtual int ReqQryOptionFundTransferDetail(CQCVDOptionQryFundTransferDetailField* pQryFundTransferDetailField, int nRequestID) = 0;
		//查询持仓转移流水
		virtual int ReqQryOptionPositionTransferDetail(CQCVDOptionQryPositionTransferDetailField* pQryPositionTransferDetailField, int nRequestID) = 0;
		//查询撤单
		virtual int ReqQryOptionCancelOrder(CQCVDOptionQryCancelOrderField* pQryCancelOrderField, int nRequestID) = 0;
		//查询分页报单
		virtual int ReqQryOptionOrderByPage(CQCVDOptionQryOrderField* pQryOrderField, int nRequestID) = 0;
		//分页查询撤单
		virtual int ReqQryOptionCancelOrderByPage(CQCVDOptionQryCancelOrderField* pQryCancelOrderField, int nRequestID) = 0;
		//分页查询成交
		virtual int ReqQryOptionTradeByPage(CQCVDOptionQryTradeField* pQryTradeField, int nRequestID) = 0;
		//查询条件单
		virtual int ReqQryOptionCondOrder(CQCVDOptionQryCondOrderField* pQryCondOrderField, int nRequestID) = 0;
		//查询条件单撤单
		virtual int ReqQryOptionCancelCondOrder(CQCVDOptionQryCancelCondOrderField* pQryCancelCondOrderField, int nRequestID) = 0;
		//查询投资者限仓信息
		virtual int ReqQryOptionInvestorLimitPosition(CQCVDOptionQryInvestorLimitPositionField* pQryInvestorLimitPositionField, int nRequestID) = 0;
		//查询报单明细资金
		virtual int ReqQryOptionOrderFundDetail(CQCVDOptionQryOrderFundDetailField* pQryOrderFundDetailField, int nRequestID) = 0;
		//查询交易通知
		virtual int ReqQryOptionTradingNotice(CQCVDOptionQryTradingNoticeField* pQryTradingNoticeField, int nRequestID) = 0;
		//查询行权
		virtual int ReqQryOptionExercise(CQCVDOptionQryExerciseField* pQryExerciseField, int nRequestID) = 0;
		//查询锁定委托
		virtual int ReqQryOptionLock(CQCVDOptionQryLockField* pQryLockField, int nRequestID) = 0;
		//查询投资者锁定持仓
		virtual int ReqQryOptionLockPosition(CQCVDOptionQryLockPositionField* pQryLockPositionField, int nRequestID) = 0;
		//查询保证金费率
		virtual int ReqQryOptionInvestorMarginFee(CQCVDOptionQryInvestorMarginFeeField* pQryInvestorMarginFeeField, int nRequestID) = 0;
		//查询现货持仓转移流水
		virtual int ReqQryOptionStockPositionTransferDetail(CQCVDOptionQryStockPositionTransferDetailField* pQryStockPositionTransferDetailField, int nRequestID) = 0;
		//查询投资者现货持仓
		virtual int ReqQryOptionStockPosition(CQCVDOptionQryStockPositionField* pQryStockPositionField, int nRequestID) = 0;
		//查询投资者组合持仓
		virtual int ReqQryOptionCombPosition(CQCVDOptionQryCombPositionField* pQryCombPositionField, int nRequestID) = 0;
		//查询组合持仓明细
		virtual int ReqQryOptionCombPosDetail(CQCVDOptionQryCombPosDetailField* pQryCombPosDetailField, int nRequestID) = 0;
		//查询投资者限额
		virtual int ReqQryOptionInvestorLimitAmount(CQCVDOptionQryInvestorLimitAmountField* pQryInvestorLimitAmountField, int nRequestID) = 0;
		//查询组合撤单
		virtual int ReqQryOptionCombOrderAction(CQCVDOptionQryCombOrderActionField* pQryCombOrderActionField, int nRequestID) = 0;
		//查询组合委托
		virtual int ReqQryOptionCombOrder(CQCVDOptionQryCombOrderField* pQryCombOrderField, int nRequestID) = 0;
		//查询组合行权委托
		virtual int ReqQryOptionCombExercise(CQCVDOptionQryCombExerciseField* pQryCombExerciseField, int nRequestID) = 0;
		//查询合并行权撤单
		virtual int ReqQryOptionCombExerciseAction(CQCVDOptionQryCombExerciseActionField* pQryCombExerciseActionField, int nRequestID) = 0;
		//查询行权指派明细
		virtual int ReqQryOptionExerciseAppointment(CQCVDOptionQryExerciseAppointmentField* pQryExerciseAppointmentField, int nRequestID) = 0;
		//查询证券处置
		virtual int ReqQryOptionStockDisposal(CQCVDOptionQryStockDisposalField* pQryStockDisposalField, int nRequestID) = 0;
		//查询证券处置撤单
		virtual int ReqQryOptionStockDisposalAction(CQCVDOptionQryStockDisposalActionField* pQryStockDisposalActionField, int nRequestID) = 0;
		//查询锁定撤单
		virtual int ReqQryOptionLockAction(CQCVDOptionQryLockActionField* pQryLockActionField, int nRequestID) = 0;
		//查询行权撤单
		virtual int ReqQryOptionExerciseAction(CQCVDOptionQryExerciseActionField* pQryExerciseActionField, int nRequestID) = 0;
		//查询组合合约信息
		virtual int ReqQryOptionCombSecurity(CQCVDOptionQryCombSecurityField* pQryCombSecurityField, int nRequestID) = 0;
		//查询合约信息
		virtual int ReqQryOptionSecurity(CQCVDOptionQrySecurityField* pQrySecurityField, int nRequestID) = 0;
		//查询备兑股份不足仓位
		virtual int ReqQryOptionInsufficientCoveredStockPosition(CQCVDOptionQryInsufficientCoveredStockPositionField* pQryInsufficientCoveredStockPositionField, int nRequestID) = 0;
		//查询集中交易系统资金请求
		virtual int ReqInquiryOptionJZFundField(CQCVDOptionReqInquiryJZFundField* pQryJZFundField, int nRequestID) = 0;

		//订阅期权投资者持仓变动推送通知
		virtual int SubscribeRtnOptionPosition(int nRequestID) = 0;
		//取消订阅期权投资者持仓变动推送通知
		virtual int UnSubscribeRtnOptionPosition(int nRequestID) = 0;
		//订阅期权投资者报单变动推送通知
		virtual int SubscribeRtnOptionOrder(int nRequestID) = 0;
		//取消订阅期权投资者报单变动推送通知
		virtual int UnSubscribeRtnOptionOrder(int nRequestID) = 0;
		//订阅期权投资者成交推送通知
		virtual int SubscribeRtnOptionTrade(int nRequestID) = 0;
		//取消订阅期权投资者成交推送通知
		virtual int UnSubscribeRtnOptionTrade(int nRequestID) = 0;
		//订阅期权投资者资金变动推送通知
		virtual int SubscribeRtnOptionTradingAccount(int nRequestID) = 0;
		//取消订阅期权投资者资金变动推送通知
		virtual int UnSubscribeRtnOptionTradingAccount(int nRequestID) = 0;
		//订阅期权投资者合并行权推送通知
		virtual int SubscribeRtnOptionCombExercise(int nRequestID) = 0;
		//取消订阅期权投资者合并行权推送通知
		virtual int UnSubscribeRtnOptionCombExercise(int nRequestID) = 0;
		//订阅期权投资者组合委托推送通知
		virtual int SubscribeRtnOptionCombOrder(int nRequestID) = 0;
		//取消订阅期权投资者组合委托推送通知
		virtual int UnSubscribeRtnOptionCombOrder(int nRequestID) = 0;
		//订阅期权投资者证券处置推送通知
		virtual int SubscribeRtnOptionStockDisposal(int nRequestID) = 0;
		//取消订阅期权投资者证券处置推送通知
		virtual int UnSubscribeRtnOptionStockDisposal(int nRequestID) = 0;
		//订阅期权投资者行权推送通知
		virtual int SubscribeRtnOptionExercise(int nRequestID) = 0;
		//取消订阅期权投资者行权推送通知
		virtual int UnSubscribeRtnOptionExercise(int nRequestID) = 0;
		//订阅期权投资者锁定推送通知
		virtual int SubscribeRtnOptionLock(int nRequestID) = 0;
		//取消订阅期权投资者锁定推送通知
		virtual int UnSubscribeRtnOptionLock(int nRequestID) = 0;
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////查询期权投资者交易信息 End
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	protected:
		~CQCValueAddProApi(){};
	};

}

#endif
