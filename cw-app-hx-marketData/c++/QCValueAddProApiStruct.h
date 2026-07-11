#if !defined(_QCValueAddProApiStruct_H)
#define _QCValueAddProApiStruct_H

#if _MSC_VER > 1000
#pragma once
#endif /// _MSC_VER > 1000

#include "QCValueAddProApiDataType.h"

///1档盘口数量数组大小
#define CONST_FIRSTLEVELVOLUMEARRAYSIZE 50

namespace QCVALUEADDPROAPI
{
	/// fens用户信息
	struct CQCVDFensUserInfoField
	{
		///登录账户
		TQCVDLogInAccountType	LogInAccount;

		///登录账户类型
		TQCVDLogInAccountTypeType	LogInAccountType;
	};

	/// 登录请求
	struct CQCVDReqUserLoginField
	{
		///登录账户
		TQCVDLogInAccountType	LogInAccount;

		///登录账户类型
		TQCVDLogInAccountTypeType	LogInAccountType;

		///密码(密码认证时必填)
		TQCVDPasswordType	Password;

		///用户端产品信息
		TQCVDProductInfoType	UserProductInfo;

		///接口端产品信息
		TQCVDProductInfoType	InterfaceProductInfo;

		///协议信息
		TQCVDProtocolInfoType	ProtocolInfo;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///移动设备手机号
		TQCVDMobileType	Mobile;

		///内网IP地址
		TQCVDIPAddressType	InnerIPAddress;

		///接口语言
		TQCVDLangType	Lang;

		///终端信息
		TQCVDTerminalInfoType	TerminalInfo;

		///网关Mac地址
		TQCVDMacAddressType	GWMacAddress;

		///网关内网IP地址
		TQCVDIPAddressType	GWInnerIPAddress;

		///网关外网IP地址
		TQCVDIPAddressType	GWOuterIPAddress;

		///一级机构代码（以资金账号方式登录时必填）
		TQCVDDepartmentIDType	DepartmentID;

		///硬盘序列号
		TQCVDHDSerialType	HDSerial;

		///认证方式(指纹或钥匙串认证时必填)
		TQCVDAuthModeType	AuthMode;

		///设备标识(指纹认证时必填)
		TQCVDDeviceIDType	DeviceID;

		///认证序列号(指纹或钥匙串认证时必填)
		TQCVDCertSerialType	CertSerial;

		///外网IP地址
		TQCVDIPAddressType	OuterIPAddress;

		///动态密码
		TQCVDPasswordType	DynamicPassword;

		///外网端口号
		TQCVDPortType	OuterPort;

		///设备类型
		TQCVDDeviceClassType DeviceClass;
	};

	/// 登录应答
	struct CQCVDRspUserLoginField
	{
		///登录时间
		TQCVDTimeType	LoginTime;

		///登录账户
		TQCVDLogInAccountType	LogInAccount;

		///登录账户类型
		TQCVDLogInAccountTypeType	LogInAccountType;

		///交易系统名称
		TQCVDSystemNameType	SystemName;

		///前置编号
		TQCVDFrontIDType	FrontID;

		///会话编号
		TQCVDSessionIDType	SessionID;

		///最大报单引用
		TQCVDOrderRefType	MaxOrderRef;

		///私有流长度
		TQCVDVolumeType	PrivateFlowCount;

		///公有流长度
		TQCVDVolumeType	PublicFlowCount;

		///交易日
		TQCVDDateType	TradingDay;

		///用户代码
		TQCVDUserIDType	UserID;

		///用户名称
		TQCVDUserNameType	UserName;

		///用户类型
		TQCVDUserTypeType	UserType;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///终端IP地址
		TQCVDIPAddressType	InnerIPAddress;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///硬盘序列号
		TQCVDHDSerialType	HDSerial;

		///报单流控
		TQCVDCommFluxType	OrderInsertCommFlux;

		///密码修改周期(天),置为0表永久有效
		TQCVDCountType	PasswordUpdatePeriod;

		///密码有效剩余天数
		TQCVDCountType	PasswordRemainDays;

		///是否需要改密
		TQCVDBoolType	NeedUpdatePassword;

		///撤单流控
		TQCVDCommFluxType	OrderActionCommFlux;

		///移动设备手机号
		TQCVDMobileType	Mobile;

		///外网IP地址
		TQCVDIPAddressType	OuterIPAddress;

		///认证序列号
		TQCVDCertSerialType	CertSerial;

		///外网端口号
		TQCVDPortType	OuterPort;

		///设备类型
		TQCVDDeviceClassType DeviceClass;
	};

	/// 响应信息
	struct CQCVDRspInfoField
	{
		///错误代码
		TQCVDErrorIDType	ErrorID;

		///错误信息
		TQCVDErrorMsgType	ErrorMsg;
	};

	/// 用户登出
	struct CQCVDUserLogoutField
	{
		///用户代码
		TQCVDUserIDType	UserID;
	};

	/// 强制交易员退出
	struct CQCVDForceUserLogoutField
	{
		///用户代码
		TQCVDUserIDType	UserID;
	};

	/// 重置用户密码
	struct CQCVDUserPasswordUpdateField
	{
		///用户代码
		TQCVDUserIDType	UserID;

		///旧密码
		TQCVDPasswordType	OldPassword;

		///新密码
		TQCVDPasswordType	NewPassword;
	};

	/// 请求录入设备序列
	struct CQCVDReqInputDeviceSerialField
	{
		///用户代码
		TQCVDUserIDType	UserID;

		///设备标识
		TQCVDDeviceIDType	DeviceID;

		///设备序列号
		TQCVDCertSerialType	CertSerial;
	};

	/// 请求录入设备序列应答
	struct CQCVDRspInputDeviceSerialField
	{
		///用户代码
		TQCVDUserIDType	UserID;
	};

	/// 启用解锁用户
	struct CQCVDActivateUserField
	{
		///用户代码
		TQCVDUserIDType	UserID;
	};

	/// 用户会话标识
	struct CQCVDVerifyUserPasswordField
	{
		///用户代码
		TQCVDUserIDType	UserID;

		///密码
		TQCVDPasswordType	Password;
	};

	/// 输入报单
	struct CQCVDInputOrderField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///报单引用
		TQCVDOrderRefType	OrderRef;

		///用户代码
		TQCVDUserIDType	UserID;

		///报单价格条件
		TQCVDOrderPriceTypeType	OrderPriceType;

		///买卖方向
		TQCVDDirectionType	Direction;

		///组合开平标志
		TQCVDCombOffsetFlagType	CombOffsetFlag;

		///组合投机套保标志
		TQCVDCombHedgeFlagType	CombHedgeFlag;

		///价格
		TQCVDPriceType	LimitPrice;

		///数量
		TQCVDVolumeType	VolumeTotalOriginal;

		///有效期类型
		TQCVDTimeConditionType	TimeCondition;

		///成交量类型
		TQCVDVolumeConditionType	VolumeCondition;

		///最小成交量
		TQCVDVolumeType	MinVolume;

		///强平原因
		TQCVDForceCloseReasonType	ForceCloseReason;

		///请求编号
		TQCVDRequestIDType	RequestID;

		///用户强评标志
		TQCVDBoolType	UserForceClose;

		///互换单标志
		TQCVDBoolType	IsSwapOrder;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///IP地址
		TQCVDIPAddressType	IPAddress;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///港股通订单数量类型,默认整股买卖
		TQCVDLotTypeType	LotType;

		///系统报单编号
		TQCVDOrderSysIDType	OrderSysID;

		///终端信息
		TQCVDTerminalInfoType	TerminalInfo;

		///长字符串附加信息
		TQCVDBigsInfoType	BInfo;

		///短字符串附加信息
		TQCVDShortsInfoType	SInfo;

		///整形附加信息
		TQCVDIntInfoType	IInfo;

		///转入交易单元代码
		TQCVDPbuIDType	TransfereePbuID;

		///委托方式
		TQCVDOperwayType	Operway;

		///条件检查
		TQCVDCondCheckType	CondCheck;

		///硬盘序列号
		TQCVDHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDMobileType	Mobile;

		///有效日期
		TQCVDDateType	GTDate;
	};

	/// 报单
	struct CQCVDOrderField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///报单引用
		TQCVDIntOrderRefType	OrderRef;

		///用户代码
		TQCVDUserIDType	UserID;

		///报单价格条件
		TQCVDOrderPriceTypeType	OrderPriceType;

		///买卖方向
		TQCVDDirectionType	Direction;

		///价格
		TQCVDPriceType	LimitPrice;

		///数量
		TQCVDVolumeType	VolumeTotalOriginal;

		///有效期类型
		TQCVDTimeConditionType	TimeCondition;

		///成交量类型
		TQCVDVolumeConditionType	VolumeCondition;

		///请求编号
		TQCVDRequestIDType	RequestID;

		///本地报单编号
		TQCVDOrderLocalIDType	OrderLocalID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///证券在交易所代码
		TQCVDExchangeInstIDType	ExchangeInstID;

		///交易单元代码
		TQCVDTraderIDType	TraderID;

		///报单提交状态
		TQCVDOrderSubmitStatusType	OrderSubmitStatus;

		///交易日
		TQCVDDateType	TradingDay;

		///系统报单编号
		TQCVDOrderSysIDType	OrderSysID;

		///报单状态
		TQCVDOrderStatusType	OrderStatus;

		///报单类型
		TQCVDOrderTypeType	OrderType;

		///已成交数量
		TQCVDVolumeType	VolumeTraded;

		///剩余未完成数量
		TQCVDVolumeType	VolumeTotal;

		///报单日期
		TQCVDDateType	InsertDate;

		///报单时间
		TQCVDTimeType	InsertTime;

		///撤销时间
		TQCVDTimeType	CancelTime;

		///前置编号
		TQCVDFrontIDType	FrontID;

		///会话编号
		TQCVDSessionIDType	SessionID;

		///用户端产品信息
		TQCVDProductInfoType	UserProductInfo;

		///状态信息
		TQCVDErrorMsgType	StatusMsg;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///IP地址
		TQCVDIPAddressType	IPAddress;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///港股通订单数量类型,默认整股买卖
		TQCVDLotTypeType	LotType;

		///短字符串附加信息
		TQCVDShortsInfoType	SInfo;

		///整形附加信息
		TQCVDIntInfoType	IInfo;

		///委托方式
		TQCVDOperwayType	Operway;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///条件检查
		TQCVDCondCheckType	CondCheck;

		///成交金额
		TQCVDMoneyType	Turnover;

		///回报附加浮点型数据信息
		TQCVDFloatInfoType	RtnFloatInfo;

		///回报附加整型数据
		TQCVDIntInfoType	RtnIntInfo;

		///有效日期
		TQCVDDateType	GTDate;

		///核心编号
		TQCVDSequenceNoType  ServerID;

		///报单用户代码
		TQCVDUserIDType	InsertUser;

		///交易所接收时间
		TQCVDTimeType	AcceptTime;

		///撤单申报用户代码
		TQCVDUserIDType	CancelUser;

		///回报附加浮点型数据1
		TQCVDFloatInfoType	RtnFloatInfo1;

		///回报附加浮点型数据2
		TQCVDFloatInfoType	RtnFloatInfo2;

		///页定位符
		TQCVDPageLocateType	PageLocate;

		///字符串附加信息
		TQCVDStrInfoType	StrInfo;

	};

	/// 用户标识
	struct CQCVDUserRefField
	{
		///用户代码
		TQCVDUserIDType	UserID;
	};

	/// 输入撤单操作
	struct CQCVDInputOrderActionField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///报单操作引用
		TQCVDOrderRefType	OrderActionRef;

		///报单引用
		TQCVDOrderRefType	OrderRef;

		///请求编号
		TQCVDRequestIDType	RequestID;

		///前置编号
		TQCVDFrontIDType	FrontID;

		///会话编号
		TQCVDSessionIDType	SessionID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///系统报单编号
		TQCVDOrderSysIDType	OrderSysID;

		///操作标志
		TQCVDActionFlagType	ActionFlag;

		///报单价格
		TQCVDPriceType	Price;

		///报单数量
		TQCVDVolumeType	Volume;

		///用户代码
		TQCVDUserIDType	UserID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///IP地址
		TQCVDIPAddressType	IPAddress;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///本地撤单编号
		TQCVDOrderLocalIDType	CancelOrderLocalID;

		///终端信息
		TQCVDTerminalInfoType	TerminalInfo;

		///长字符串附加信息
		TQCVDBigsInfoType	BInfo;

		///短字符串附加信息
		TQCVDShortsInfoType	SInfo;

		///整形附加信息
		TQCVDIntInfoType	IInfo;

		///委托方式
		TQCVDOperwayType	Operway;

		///硬盘序列号
		TQCVDHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDMobileType	Mobile;
	};

	/// 报单操作
	struct CQCVDOrderActionField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///报单操作引用
		TQCVDIntOrderRefType	OrderActionRef;

		///报单引用
		TQCVDIntOrderRefType	OrderRef;

		///请求编号
		TQCVDRequestIDType	RequestID;

		///前置编号
		TQCVDFrontIDType	FrontID;

		///会话编号
		TQCVDSessionIDType	SessionID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///系统报单编号
		TQCVDOrderSysIDType	OrderSysID;

		///操作标志
		TQCVDActionFlagType	ActionFlag;

		///操作日期
		TQCVDDateType	ActionDate;

		///操作时间
		TQCVDTimeType	ActionTime;

		///交易单元代码
		TQCVDTraderIDType	TraderID;

		///本地报单编号
		TQCVDOrderLocalIDType	OrderLocalID;

		///操作本地编号
		TQCVDOrderLocalIDType	ActionLocalID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///报单操作状态
		TQCVDOrderActionStatusType	OrderActionStatus;

		///用户代码
		TQCVDUserIDType	UserID;

		///状态信息
		TQCVDErrorMsgType	StatusMsg;

		///IP地址
		TQCVDIPAddressType	IPAddress;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///短字符串附加信息
		TQCVDShortsInfoType	SInfo;

		///整形附加信息
		TQCVDIntInfoType	IInfo;

		///委托方式
		TQCVDOperwayType	Operway;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///核心编号
		TQCVDSequenceNoType  ServerID;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///本地撤单系统编号
		TQCVDOrderSysIDType	CancelOrderSysID;

		///页定位符
		TQCVDPageLocateType	PageLocate;

		///字符串附加信息
		TQCVDStrInfoType	StrInfo;

	};

	/// 成交
	struct CQCVDTradeField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///用户代码
		TQCVDUserIDType	UserID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///成交编号
		TQCVDTradeIDType	TradeID;

		///买卖方向
		TQCVDDirectionType	Direction;

		///系统报单编号
		TQCVDOrderSysIDType	OrderSysID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///证券在交易所代码
		TQCVDExchangeInstIDType	ExchangeInstID;

		///成交价格
		TQCVDPriceType	Price;

		///成交数量
		TQCVDVolumeType	Volume;

		///成交日期
		TQCVDDateType	TradeDate;

		///成交时间
		TQCVDTimeType	TradeTime;

		///交易单元代码
		TQCVDTraderIDType	TraderID;

		///本地报单编号
		TQCVDOrderLocalIDType	OrderLocalID;

		///交易日
		TQCVDDateType	TradingDay;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///报单引用
		TQCVDIntOrderRefType	OrderRef;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///核心编号
		TQCVDSequenceNoType  ServerID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 深度行情通知
	struct CQCVDMarketDataField
	{
		///交易日
		TQCVDDateType	TradingDay;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券名称
		TQCVDSecurityNameType	SecurityName;

		///昨收盘价
		TQCVDPriceType	PreClosePrice;

		///今开盘价
		TQCVDPriceType	OpenPrice;

		///成交量
		TQCVDLongVolumeType	Volume;

		///成交金额
		TQCVDMoneyType	Turnover;

		///成交笔数
		TQCVDLongVolumeType	TradingCount;

		///最新价
		TQCVDPriceType	LastPrice;

		///最高价
		TQCVDPriceType	HighestPrice;

		///最低价
		TQCVDPriceType	LowestPrice;

		///买1价
		TQCVDPriceType	BidPrice1;

		///卖1价
		TQCVDPriceType	AskPrice1;

		///涨停价
		TQCVDPriceType	UpperLimitPrice;

		///跌停价
		TQCVDPriceType	LowerLimitPrice;

		///市盈率1
		TQCVDRatioType	PERatio1;

		///市盈率2
		TQCVDRatioType	PERatio2;

		///价格升跌1
		TQCVDPriceType	PriceUpDown1;

		///价格升跌2
		TQCVDPriceType	PriceUpDown2;

		///持仓量
		TQCVDLargeVolumeType	OpenInterest;

		///买1量
		TQCVDLongVolumeType	BidVolume1;

		///卖1量
		TQCVDLongVolumeType	AskVolume1;

		///买2价
		TQCVDPriceType	BidPrice2;

		///买2量
		TQCVDLongVolumeType	BidVolume2;

		///卖2价
		TQCVDPriceType	AskPrice2;

		///卖2量
		TQCVDLongVolumeType	AskVolume2;

		///买3价
		TQCVDPriceType	BidPrice3;

		///买3量
		TQCVDLongVolumeType	BidVolume3;

		///卖3价
		TQCVDPriceType	AskPrice3;

		///卖3量
		TQCVDLongVolumeType	AskVolume3;

		///买4价
		TQCVDPriceType	BidPrice4;

		///买4量
		TQCVDLongVolumeType	BidVolume4;

		///卖4价
		TQCVDPriceType	AskPrice4;

		///卖4量
		TQCVDLongVolumeType	AskVolume4;

		///买5价
		TQCVDPriceType	BidPrice5;

		///买5量
		TQCVDLongVolumeType	BidVolume5;

		///卖5价
		TQCVDPriceType	AskPrice5;

		///卖5量
		TQCVDLongVolumeType	AskVolume5;

		///更新时间
		TQCVDTimeType	UpdateTime;

		///更新毫秒
		TQCVDMillisecType	UpdateMillisec;

		///今收盘价
		TQCVDPriceType	ClosePrice;

		///行情产品实时状态
		TQCVDMDSecurityStatType	MDSecurityStat;

		///是否警示板块
		TQCVDBoolType	HWFlag;
	};

	/// 盘后行情通知
	struct CQCVDPHMarketDataField
	{
		///交易日
		TQCVDDateType	TradingDay;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券名称
		TQCVDSecurityNameType	SecurityName;

		///成交量
		TQCVDLongVolumeType	Volume;

		///成交金额
		TQCVDMoneyType	Turnover;

		///今收盘价
		TQCVDPriceType	ClosePrice;

		///涨停价
		TQCVDPriceType	UpperLimitPrice;

		///跌停价
		TQCVDPriceType	LowerLimitPrice;

		///买入申报数量
		TQCVDLongVolumeType	BidVolume;

		///卖出申报数量
		TQCVDLongVolumeType	AskVolume;

		///更新时间
		TQCVDTimeType	UpdateTime;

		///更新毫秒
		TQCVDMillisecType	UpdateMillisec;

		///行情产品实时状态
		TQCVDMDSecurityStatType	MDSecurityStat;

		///是否警示板块
		TQCVDBoolType	HWFlag;
	};

	/// 市场状态
	struct CQCVDMarketStatusField
	{
		///市场代码
		TQCVDMarketIDType	MarketID;

		///市场状态
		TQCVDMarketStatusType	MarketStatus;
	};

	/// 条件单录入域
	struct CQCVDInputCondOrderField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///买卖方向
		TQCVDDirectionType	Direction;

		///价格类型
		TQCVDOrderPriceTypeType	OrderPriceType;

		///触发基准数量类型
		TQCVDTriggerOrderVolumeTypeType	TriggerOrderVolumeType;

		///有效期类型
		TQCVDTimeConditionType	TimeCondition;

		///成交量类型
		TQCVDVolumeConditionType	VolumeCondition;

		///报单价格
		TQCVDPriceType	LimitPrice;

		///报单数量
		TQCVDVolumeType	VolumeTotalOriginal;

		///组合开平标志
		TQCVDCombOffsetFlagType	CombOffsetFlag;

		///组合投机套保标志
		TQCVDCombHedgeFlagType	CombHedgeFlag;

		///条件报单引用
		TQCVDOrderRefType	CondOrderRef;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///用户代码
		TQCVDUserIDType	UserID;

		///请求编号
		TQCVDRequestIDType	RequestID;

		///IP地址
		TQCVDIPAddressType	IPAddress;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///报单编号
		TQCVDCondOrderIDType	CondOrderID;

		///终端信息
		TQCVDTerminalInfoType	TerminalInfo;

		///长字符串附加信息
		TQCVDBigsInfoType	BInfo;

		///短字符串附加信息
		TQCVDShortsInfoType	SInfo;

		///整形附加信息
		TQCVDIntInfoType	IInfo;

		///委托方式
		TQCVDOperwayType	Operway;

		///条件检查
		TQCVDCondCheckType	CondCheck;

		///条件单触发条件
		TQCVDContingentConditionType	ContingentCondition;

		///条件价
		TQCVDPriceType	ConditionPrice;

		///价格浮动tick数
		TQCVDVolumeType	PriceTicks;

		///数量浮动倍数
		TQCVDVolumeMultipleType	VolumeMultiple;

		///相关前置编号
		TQCVDFrontIDType	RelativeFrontID;

		///相关会话编号
		TQCVDSessionIDType	RelativeSessionID;

		///相关条件参数
		TQCVDRelativeCondParamType	RelativeParam;

		///附加条件单触发条件
		TQCVDContingentConditionType	AppendContingentCondition;

		///附加条件价
		TQCVDPriceType	AppendConditionPrice;

		///附加相关前置编号
		TQCVDFrontIDType	AppendRelativeFrontID;

		///附加相关会话编号
		TQCVDSessionIDType	AppendRelativeSessionID;

		///附加相关条件参数
		TQCVDRelativeCondParamType	AppendRelativeParam;

		///硬盘序列号
		TQCVDHDSerialType	HDSerial;

		///港股通订单数量类型,默认整股买卖
		TQCVDLotTypeType	LotType;

		///移动设备手机号
		TQCVDMobileType	Mobile;

		///触发基准价类型
		TQCVDTriggerOrderPriceTypeType	TriggerOrderPriceType;

		///有效日期
		TQCVDDateType	GTDate;
	};

	/// 条件单域
	struct CQCVDConditionOrderField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///买卖方向
		TQCVDDirectionType	Direction;

		///价格类型
		TQCVDOrderPriceTypeType	OrderPriceType;

		///触发基准数量类型
		TQCVDTriggerOrderVolumeTypeType	TriggerOrderVolumeType;

		///有效期类型
		TQCVDTimeConditionType	TimeCondition;

		///成交量类型
		TQCVDVolumeConditionType	VolumeCondition;

		///报单价格
		TQCVDPriceType	LimitPrice;

		///报单数量
		TQCVDVolumeType	VolumeTotalOriginal;

		///组合开平标志
		TQCVDCombOffsetFlagType	CombOffsetFlag;

		///组合投机套保标志
		TQCVDCombHedgeFlagType	CombHedgeFlag;

		///条件报单引用
		TQCVDOrderRefType	CondOrderRef;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///用户代码
		TQCVDUserIDType	UserID;

		///请求编号
		TQCVDRequestIDType	RequestID;

		///IP地址
		TQCVDIPAddressType	IPAddress;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///报单编号
		TQCVDCondOrderIDType	CondOrderID;

		///终端信息
		TQCVDTerminalInfoType	TerminalInfo;

		///长字符串附加信息
		TQCVDBigsInfoType	BInfo;

		///短字符串附加信息
		TQCVDShortsInfoType	SInfo;

		///整形附加信息
		TQCVDIntInfoType	IInfo;

		///委托方式
		TQCVDOperwayType	Operway;

		///条件检查
		TQCVDCondCheckType	CondCheck;

		///条件单触发条件
		TQCVDContingentConditionType	ContingentCondition;

		///条件价
		TQCVDPriceType	ConditionPrice;

		///价格浮动tick数
		TQCVDVolumeType	PriceTicks;

		///数量浮动倍数
		TQCVDVolumeMultipleType	VolumeMultiple;

		///相关前置编号
		TQCVDFrontIDType	RelativeFrontID;

		///相关会话编号
		TQCVDSessionIDType	RelativeSessionID;

		///相关条件参数
		TQCVDRelativeCondParamType	RelativeParam;

		///附加条件单触发条件
		TQCVDContingentConditionType	AppendContingentCondition;

		///附加条件价
		TQCVDPriceType	AppendConditionPrice;

		///附加相关前置编号
		TQCVDFrontIDType	AppendRelativeFrontID;

		///附加相关会话编号
		TQCVDSessionIDType	AppendRelativeSessionID;

		///附加相关条件参数
		TQCVDRelativeCondParamType	AppendRelativeParam;

		///交易日
		TQCVDDateType	TradingDay;

		///条件单状态
		TQCVDCondOrderStatusType	CondOrderStatus;

		///报单日期
		TQCVDDateType	InsertDate;

		///报单时间
		TQCVDTimeType	InsertTime;

		///撤销时间
		TQCVDTimeType	CancelTime;

		///撤销用户
		TQCVDUserIDType	CancelUser;

		///前置编号
		TQCVDFrontIDType	FrontID;

		///会话编号
		TQCVDSessionIDType	SessionID;

		///用户端产品信息
		TQCVDProductInfoType	UserProductInfo;

		///状态信息
		TQCVDErrorMsgType	StatusMsg;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///适当性控制业务类别
		TQCVDProperCtrlBusinessTypeType	ProperCtrlBusinessType;

		///适当性控制通过标识
		TQCVDProperCtrlPassFlagType	ProperCtrlPassFlag;

		///触发日期
		TQCVDDateType	ActiveDate;

		///触发时间
		TQCVDTimeType	ActiveTime;

		///硬盘序列号
		TQCVDHDSerialType	HDSerial;

		///港股通订单数量类型,默认整股买卖
		TQCVDLotTypeType	LotType;

		///移动设备手机号
		TQCVDMobileType	Mobile;

		///触发基准价类型
		TQCVDTriggerOrderPriceTypeType	TriggerOrderPriceType;

		///当前相关条件参数
		TQCVDRelativeCondParamType	TriggerRelativeParam;

		///附加条件信息
		TQCVDRelativeCondParamType	AppendCondParam;

		///有效日期
		TQCVDDateType	GTDate;
	};

	/// 条件单操作录入
	struct CQCVDInputCondOrderActionField
	{
		///请求编号
		TQCVDRequestIDType	RequestID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///条件单操作引用
		TQCVDOrderRefType	CondOrderActionRef;

		///条件单引用
		TQCVDOrderRefType	CondOrderRef;

		///前置编号
		TQCVDFrontIDType	FrontID;

		///会话编号
		TQCVDSessionIDType	SessionID;

		///条件单编号
		TQCVDCondOrderIDType	CondOrderID;

		///操作标志
		TQCVDActionFlagType	ActionFlag;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///用户代码
		TQCVDUserIDType	UserID;

		///IP地址
		TQCVDIPAddressType	IPAddress;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///条件单撤单编号
		TQCVDCondOrderIDType	CancelCondOrderID;

		///终端信息
		TQCVDTerminalInfoType	TerminalInfo;

		///长字符串附加信息
		TQCVDBigsInfoType	BInfo;

		///短字符串附加信息
		TQCVDShortsInfoType	SInfo;

		///整形附加信息
		TQCVDIntInfoType	IInfo;

		///委托方式
		TQCVDOperwayType	Operway;

		///硬盘序列号
		TQCVDHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDMobileType	Mobile;
	};

	/// 录入节点资金分配信息
	struct CQCVDInputNodeFundAssignmentField
	{
		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///资金账号
		TQCVDAccountIDType	AccountID;

		///币种
		TQCVDCurrencyIDType	CurrencyID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///节点编号1
		TQCVDNodeIDType	NodeID1;

		///资金比例1
		TQCVDRatioType	AmtRatio1;

		///节点编号2
		TQCVDNodeIDType	NodeID2;

		///资金比例2
		TQCVDRatioType	AmtRatio2;

		///节点编号3
		TQCVDNodeIDType	NodeID3;

		///资金比例3
		TQCVDRatioType	AmtRatio3;

		///节点编号4
		TQCVDNodeIDType	NodeID4;

		///资金比例4
		TQCVDRatioType	AmtRatio4;

		///节点编号5
		TQCVDNodeIDType	NodeID5;

		///资金比例5
		TQCVDRatioType	AmtRatio5;
	};

	/// 查询节点资金分配比例请求
	struct CQCVDReqInquiryNodeFundAssignmentField
	{
		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///资金账号
		TQCVDAccountIDType	AccountID;

		///币种
		TQCVDCurrencyIDType	CurrencyID;

		///节点编号
		TQCVDNodeIDType	NodeID;
	};

	/// 查询节点资金分配比例应答
	struct CQCVDRspInquiryNodeFundAssignmentField
	{
		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///资金账号
		TQCVDAccountIDType	AccountID;

		///币种
		TQCVDCurrencyIDType	CurrencyID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///节点编号1
		TQCVDNodeIDType	NodeID1;

		///资金比例1
		TQCVDRatioType	AmtRatio1;

		///节点编号2
		TQCVDNodeIDType	NodeID2;

		///资金比例2
		TQCVDRatioType	AmtRatio2;

		///节点编号3
		TQCVDNodeIDType	NodeID3;

		///资金比例3
		TQCVDRatioType	AmtRatio3;

		///节点编号4
		TQCVDNodeIDType	NodeID4;

		///资金比例4
		TQCVDRatioType	AmtRatio4;

		///节点编号5
		TQCVDNodeIDType	NodeID5;

		///资金比例5
		TQCVDRatioType	AmtRatio5;
	};

	/// 查询集中交易系统资金请求
	struct CQCVDReqInquiryJZFundField
	{
		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///币种
		TQCVDCurrencyIDCharType	CurrencyID;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;
	};

	/// 查询集中交易系统资金响应
	struct CQCVDRspInquiryJZFundField
	{
		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///币种
		TQCVDCurrencyIDCharType	CurrencyID;

		///可用金额
		TQCVDMoneyType	UsefulMoney;

		///可取额度
		TQCVDMoneyType	FetchLimit;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;
	};

	/// 资金转移
	struct CQCVDInputTransferFundField
	{
		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///币种
		TQCVDCurrencyIDType	CurrencyID;

		///申请流水号
		TQCVDExternalSerialType	ApplySerial;

		///转移方向
		TQCVDTransferDirectionType	TransferDirection;

		///转移金额
		TQCVDMoneyType	Amount;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///银行代码(银证转账时必填)
		TQCVDBankIDType	BankID;

		///资金密码(证券转银行时必填)
		TQCVDPasswordType	AccountPassword;

		///银行密码(银行转证券时必填)
		TQCVDPasswordType	BankPassword;

		///外部系统一级机构代码(外部系统转账时必填)
		TQCVDDepartmentIDType	ExternalDepartmentID;

		///外部系统资金账户(外部系统转账时必填)
		TQCVDAccountIDType	ExternalAccountID;

		///外部系统币种(外部系统转账时必填)
		TQCVDCurrencyIDType	ExternalCurrencyID;

		///外部系统银行代码(外部系统转账时必填)
		TQCVDBankIDType	ExternalBankID;

		///外部系统资金密码(外部系统转入时必填)
		TQCVDPasswordType	ExternalAccountPassword;

		///外部系统银行密码(外部系统转出时必填)
		TQCVDPasswordType	ExternalBankPassword;

		///外部系统交易密码(外部系统转账时必填)
		TQCVDPasswordType	ExternalTradePassword;

		///外部节点编号(外部系统转账时必填)
		TQCVDNodeIDType	ExternalNodeID;
	};

	/// 仓位转移
	struct CQCVDInputTransferPositionField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///申请流水号
		TQCVDExternalSerialType	ApplySerial;

		///转移方向
		TQCVDTransferDirectionType	TransferDirection;

		///数量
		TQCVDVolumeType	Volume;

		///转移持仓类型
		TQCVDTransferPositionTypeType	TransferPositionType;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///外部节点编号(外部系统转仓时必填)
		TQCVDNodeIDType	ExternalNodeID;
	};

	/// 资金转移回报
	struct CQCVDTransferFundField
	{
		///转移流水号
		TQCVDSerialType	FundSerial;

		///申请流水号
		TQCVDExternalSerialType	ApplySerial;

		///前置编号
		TQCVDFrontIDType	FrontID;

		///会话编号
		TQCVDSessionIDType	SessionID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///币种
		TQCVDCurrencyIDType	CurrencyID;

		///转移方向
		TQCVDTransferDirectionType	TransferDirection;

		///转移金额
		TQCVDMoneyType	Amount;

		///转移状态
		TQCVDTransferStatusType	TransferStatus;

		///操作人员
		TQCVDUserIDType	OperatorID;

		///操作日期
		TQCVDDateType	OperateDate;

		///操作时间
		TQCVDTimeType	OperateTime;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///签约银行账户
		TQCVDBankAccountIDType	BankAccountID;

		///银行代码
		TQCVDBankIDType	BankID;

		///IP地址
		TQCVDIPAddressType	IPAddress;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///硬盘序列号
		TQCVDHDSerialType	HDSerial;

		///移动端手机号
		TQCVDMobileType	Mobile;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///外部节点号
		TQCVDNodeIDType	ExternalNodeID;
	};

	/// 仓位转移回报
	struct CQCVDTransferPositionField
	{
		///仓位转移流水号
		TQCVDSerialType	PositionSerial;

		///申请流水号
		TQCVDExternalSerialType	ApplySerial;

		///前置编号
		TQCVDFrontIDType	FrontID;

		///会话编号
		TQCVDSessionIDType	SessionID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///交易日
		TQCVDDateType	TradingDay;

		///转移方向
		TQCVDTransferDirectionType	TransferDirection;

		///转移持仓类型
		TQCVDTransferPositionTypeType	TransferPositionType;

		///昨日持仓数量
		TQCVDVolumeType	HistoryVolume;

		///今日买卖持仓数量
		TQCVDVolumeType	TodayBSVolume;

		///今日申赎持仓数量
		TQCVDVolumeType	TodayPRVolume;

		///转移状态
		TQCVDTransferStatusType	TransferStatus;

		///操作人员
		TQCVDUserIDType	OperatorID;

		///操作日期
		TQCVDDateType	OperateDate;

		///操作时间
		TQCVDTimeType	OperateTime;

		///IP地址
		TQCVDIPAddressType	IPAddress;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///硬盘序列号
		TQCVDHDSerialType	HDSerial;

		///移动端手机号
		TQCVDMobileType	Mobile;

		///外部节点编号
		TQCVDNodeIDType	ExternalNodeID;

		///今日拆分合并持仓数量
		TQCVDVolumeType	TodaySMVolume;
	};

	/// 订阅行情
	struct CQCVDSpecificSecurityField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;
	};

	/// 担保品转移请求
	struct CQCVDInputTransferCollateralField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///申请流水号
		TQCVDExternalSerialType	ApplySerial;

		///担保品划转方向
		TQCVDCollateralDirectionType	CollateralDirection;

		///数量
		TQCVDVolumeType	Volume;

		///市场代码
		TQCVDMarketIDType	MarketID;
	};

	/// 查询银行账户余额请求
	struct CQCVDReqInquiryBankAccountFundField
	{
		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///币种
		TQCVDCurrencyIDCharType	CurrencyID;

		///银行代码
		TQCVDBankIDType	BankID;

		///银行密码
		TQCVDPasswordType	BankPassword;
	};

	/// 查询银行账户余额响应
	struct CQCVDRspInquiryBankAccountFundField
	{
		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///币种
		TQCVDCurrencyIDCharType	CurrencyID;

		///银行代码
		TQCVDBankIDType	BankID;

		///签约银行账户
		TQCVDBankAccountIDType	BankAccountID;

		///账户余额
		TQCVDMoneyType	Balance;
	};

	/// 查询行情快照
	struct CQCVDInquiryMarketDataField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;
	};

	/// 查询应答域
	struct CQCVDQryRspInfoField
	{
		///结束标识
		TQCVDEndFlagType	EndFlag;

		///错误代码
		TQCVDErrorIDType	ErrorID;

		///错误信息
		TQCVDErrorMsgType	ErrorMsg;
	};

	/// 请求修改开仓成本
	struct CQCVDReqModifyOpenPosCostField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///开仓成本
		TQCVDMoneyType	OpenPosCost;
	};

	/// 请求股东账户证券代码系统权限实时插入信息
	struct CQCVDReqInsSecurityPriorAuthField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///买卖方向
		TQCVDDirectionType	Direction;

		///是否禁止
		TQCVDBoolType	bForbidden;
	};

	/// 请求股东账户证券代码系统权限实时更新信息
	struct CQCVDReqUpdSecurityPriorAuthField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///买卖方向
		TQCVDDirectionType	Direction;

		///是否禁止
		TQCVDBoolType	bForbidden;
	};

	/// 请求股东账户证券代码系统权限实时删除信息
	struct CQCVDReqDelSecurityPriorAuthField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///买卖方向
		TQCVDDirectionType	Direction;
	};

	/// 请求实时插入柜员用户功能权限
	struct CQCVDReqInsBrokerUserFunctionField
	{
		///用户代码
		TQCVDUserIDType	UserID;

		///功能代码
		TQCVDFunctionIDType	FunctionID;

		///范围模式
		TQCVDRangeModeType	RangeMode;
	};

	/// 请求实时更新柜员用户功能权限
	struct CQCVDReqDelBrokerUserFunctionField
	{
		///用户代码
		TQCVDUserIDType	UserID;

		///功能代码
		TQCVDFunctionIDType	FunctionID;
	};

	/// 回传交易数据
	struct CQCVDUploadTradeDataField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///交易日
		TQCVDDateType	TradingDay;

		///是否强制回传
		TQCVDBoolType	bForce;
	};

	/// 修改股东订阅密码
	struct CQCVDGuardSubPasswordUpdateField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///旧密码
		TQCVDPasswordType	OldPassword;

		///新密码
		TQCVDPasswordType	NewPassword;
	};

	/// 订阅股东信息
	struct CQCVDGuardSubItemField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///订阅密码
		TQCVDPasswordType	SubPassword;
	};

	/// 退订股东信息
	struct CQCVDGuardUnSubItemField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;
	};

	/// 股东报单回报
	struct CQCVDGuardOrderField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///买卖方向
		TQCVDDirectionType	Direction;

		///价格
		TQCVDPriceType	LimitPrice;

		///数量
		TQCVDVolumeType	VolumeTotalOriginal;

		///报单类别
		TQCVDGOrderTypeType	OrderType;

		///交易单元代码
		TQCVDPbuIDType	PbuID;

		///报单编号
		TQCVDOrderLocalIDType	OrderLocalID;

		///报单日期
		TQCVDDateType	InsertDate;

		///报单时间
		TQCVDTimeType	InsertTime;

		///港股通订单数量类型,默认整股买卖
		TQCVDLotTypeType	LotType;
	};

	/// 股东成交回报
	struct CQCVDGuardTradeField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///成交编号
		TQCVDTradeIDType	TradeID;

		///交易单元代码
		TQCVDPbuIDType	PbuID;

		///报单编号
		TQCVDOrderLocalIDType	OrderLocalID;

		///成交价格
		TQCVDPriceType	Price;

		///成交数量
		TQCVDVolumeType	Volume;

		///成交日期
		TQCVDDateType	TradeDate;

		///成交时间
		TQCVDTimeType	TradeTime;

		///执行类别
		TQCVDExecTypeType	ExecType;

		///撤单编号(成交或交易所主动撤单时为空)
		TQCVDOrderLocalIDType	CancelOrderLocalID;
	};

	/// 输入指定交易登记撤销
	struct CQCVDInputDesignationRegistrationField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///用户代码
		TQCVDUserIDType	UserID;

		///指定交易操作类型
		TQCVDDesignationTypeType	DesignationType;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///系统报单编号
		TQCVDOrderSysIDType	OrderSysID;

		///IP地址
		TQCVDIPAddressType	IPAddress;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///终端信息
		TQCVDTerminalInfoType	TerminalInfo;

		///硬盘序列号
		TQCVDHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDMobileType	Mobile;
	};

	/// 输入深证转托管
	struct CQCVDInputCustodyTransferField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///用户代码
		TQCVDUserIDType	UserID;

		///转托管类型
		TQCVDCustodyTransferTypeType	CustodyTransferType;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///系统报单编号
		TQCVDOrderSysIDType	OrderSysID;

		///转入交易单元代码
		TQCVDPbuIDType	TransfereePbuID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///原始转托管本地报单编号
		TQCVDOrderLocalIDType	OrignalOrderLocalID;

		///本地报单编号
		TQCVDOrderLocalIDType	OrderLocalID;

		///数量
		TQCVDVolumeType	VolumeTotalOriginal;

		///IP地址
		TQCVDIPAddressType	IPAddress;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///终端信息
		TQCVDTerminalInfoType	TerminalInfo;

		///硬盘序列号
		TQCVDHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDMobileType	Mobile;
	};

	/// 交易成交集中度
	struct CQCVDInquiryTradeConcentrationField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///投资者成交量/成交总量
		TQCVDRatioType	ConcentrationRatio1;

		///投资者成交金额/成交总金额
		TQCVDRatioType	ConcentrationRatio2;
	};

	/// 特别行情通知
	struct CQCVDSpecialMarketDataField
	{
		///交易日
		TQCVDDateType	TradingDay;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券名称
		TQCVDSecurityNameType	SecurityName;

		///移动平均价
		TQCVDPriceType	MovingAvgPrice;

		///计算移动平均价的采样数量
		TQCVDVolumeType	MovingAvgPriceSamplingNum;

		///最后修改时间
		TQCVDTimeType	UpdateTime;

		///最后修改毫秒
		TQCVDMillisecType	UpdateMillisec;
	};

	/// 价格异常波动行情
	struct CQCVDEffectPriceMarketDataField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///最新价
		TQCVDPriceType	LastPrice;

		///当前成交数量
		TQCVDLongVolumeType	TradeVol;

		///当前成交金额
		TQCVDMoneyType	TradeTurnover;

		///最后修改时间
		TQCVDTimeType	UpdateTime;

		///最后修改毫秒
		TQCVDMillisecType	UpdateMillisec;
	};

	/// 数量异常波动行情
	struct CQCVDEffectVolumeMarketDataField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///最新价
		TQCVDPriceType	LastPrice;

		///当前成交数量
		TQCVDLongVolumeType	TradeVol;

		///当前成交金额
		TQCVDMoneyType	TradeTurnover;

		///最后修改时间
		TQCVDTimeType	UpdateTime;

		///最后修改毫秒
		TQCVDMillisecType	UpdateMillisec;
	};

	/// 资金流向数据行情
	struct CQCVDFundsFlowMarketDataField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///最后修改时间
		TQCVDTimeType	UpdateTime;

		///最后修改毫秒
		TQCVDMillisecType	UpdateMillisec;

		///散户买入金额
		TQCVDMoneyType	RetailBuyTurnover;

		///散户买入数量
		TQCVDLongVolumeType	RetailBuyVolume;

		///散户买入笔数
		TQCVDLongVolumeType	RetailBuyAmount;

		///散户卖出金额
		TQCVDMoneyType	RetailSellTurnover;

		///散户卖出数量
		TQCVDLongVolumeType	RetailSellVolume;

		///散户卖出笔数
		TQCVDLongVolumeType	RetailSellAmount;

		///中户买入金额
		TQCVDMoneyType	MiddleBuyTurnover;

		///中户买入数量
		TQCVDLongVolumeType	MiddleBuyVolume;

		///中户买入笔数
		TQCVDLongVolumeType	MiddleBuyAmount;

		///中户卖出金额
		TQCVDMoneyType	MiddleSellTurnover;

		///中户卖出数量
		TQCVDLongVolumeType	MiddleSellVolume;

		///中户卖出笔数
		TQCVDLongVolumeType	MiddleSellAmount;

		///大户买入金额
		TQCVDMoneyType	LargeBuyTurnover;

		///大户买入数量
		TQCVDLongVolumeType	LargeBuyVolume;

		///大户买入笔数
		TQCVDLongVolumeType	LargeBuyAmount;

		///大户卖出金额
		TQCVDMoneyType	LargeSellTurnover;

		///大户卖出数量
		TQCVDLongVolumeType	LargeSellVolume;

		///大户卖出笔数
		TQCVDLongVolumeType	LargeSellAmount;

		///机构买入金额
		TQCVDMoneyType	InstitutionBuyTurnover;

		///机构买入数量
		TQCVDLongVolumeType	InstitutionBuyVolume;

		///机构买入笔数
		TQCVDLongVolumeType	InstitutionBuyAmount;

		///机构卖出金额
		TQCVDMoneyType	InstitutionSellTurnover;

		///机构卖出数量
		TQCVDLongVolumeType	InstitutionSellVolume;

		///机构卖出笔数
		TQCVDLongVolumeType	InstitutionSellAmount;
	};

	/// 查询复权信息
	struct CQCVDQryRightsAdjustmentInfoField
	{
		///起始日期
		TQCVDDateType	BegDate;

		///结束日期
		TQCVDDateType	EndDate;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询历史响应信息
	struct CQCVDQryHistoryRspInfoField
	{
		///错误代码
		TQCVDErrorIDType	ErrorID;

		///错误信息
		TQCVDErrorMsgType	ErrorMsg;

		///分页是否结束
		TQCVDBoolType	bPageEnd;

		///总查询结果是否结束
		TQCVDBoolType	bResultEnd;
	};

	/// 复权数据
	struct CQCVDRightsAdjustmentDataField
	{
		///交易日期
		TQCVDDateType	TradingDay;

		///复权昨收盘价(元)
		TQCVDPriceType	ADJPreClose;

		///复权开盘价(元)
		TQCVDPriceType	ADJOpen;

		///复权最高价(元)
		TQCVDPriceType	ADJHigh;

		///复权最低价(元)
		TQCVDPriceType	ADJLow;

		///复权收盘价(元)
		TQCVDPriceType	ADJClose;

		///复权因子
		TQCVDPriceType	ADJFactor;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///页定位符
		TQCVDPageLocateType	PageLocate;

		///开盘价(元)
		TQCVDPriceType	Open;

		///最高价(元)
		TQCVDPriceType	High;

		///最低价(元)
		TQCVDPriceType	Low;

		///收盘价(元)
		TQCVDPriceType	Close;

		///货币代码
		TQCVDCodeType	CrncyCode;

		///涨跌(元)
		TQCVDPriceType	Change;

		///涨跌幅(%)
		TQCVDPriceType	PCTChange;

		///成交量(手)
		TQCVDPriceType	Volume;

		///成交金额(千元)
		TQCVDPriceType	Amount;

		///均价(VWAP)
		TQCVDPriceType	AVGPrice;

		///交易状态
		TQCVDTradeStatusType	TradeStatus;
	};

	/// 查询历史资金流向数据
	struct CQCVDQryHistoryFundsFlowInfoField
	{
		///起始日期
		TQCVDDateType	BegDate;

		///结束日期
		TQCVDDateType	EndDate;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 复权历史资金流向数据
	struct CQCVDHistoryFundsFlowDataField
	{
		///交易日期
		TQCVDDateType	TradingDay;

		///机构买入金额(万元)
		TQCVDMoneyType	BuyValueExlargeOrder;

		///机构卖出金额(万元)
		TQCVDMoneyType	SellValueExlargeOrder;

		///大户买入金额(万元)
		TQCVDMoneyType	BuyValueLargeOrder;

		///大户卖出金额(万元)
		TQCVDMoneyType	SellValueLargeOrder;

		///中户买入金额(万元)
		TQCVDMoneyType	BuyValueMedOrder;

		///中户卖出金额(万元)
		TQCVDMoneyType	SellValueMedOrder;

		///散户买入金额(万元)
		TQCVDMoneyType	BuyValueSmallOrder;

		///散户卖出金额(万元)
		TQCVDMoneyType	SellValueSmallOrder;

		///机构买入总量(手)
		TQCVDQuantityType	BuyVolumeExlargeOrder;

		///机构卖出总量(手)
		TQCVDQuantityType	SellVolumeExlargeOrder;

		///大户买入总量(手)
		TQCVDQuantityType	BuyVolumeLargeOrder;

		///大户卖出总量(手)
		TQCVDQuantityType	SellVolumeLargeOrder;

		///中户买入总量(手)
		TQCVDQuantityType	BuyVolumeMedOrder;

		///中户卖出总量(手)
		TQCVDQuantityType	SellVolumeMedOrder;

		///散户买入总量(手)
		TQCVDQuantityType	BuyVolumeSmallOrder;

		///散户卖出总量(手)
		TQCVDQuantityType	SellVolumeSmallOrder;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询财务指标信息
	struct CQCVDQryFinancialIndicatorInfoField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询分红信息
	struct CQCVDQryDividendInfoField
	{
		///起始日期
		TQCVDDateType	BegDate;

		///结束日期
		TQCVDDateType	EndDate;

		///方案进度,TQCVDProgressType是一个进度类型,由一个数字来表示。 1.董事会预案 2.股东大会通过 3.实施 4.未通过 12.停止实施 17.股东提议 19.董事会预案预披露分红实施进程：股东提议--董事会预案--股东大会--实施
		TQCVDProgressType	Progress;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 分红数据
	struct CQCVDDividendDataField
	{
		///最新公告日期
		TQCVDDateType	AnnouncementDate;

		///方案进度,TQCVDProgressType是一个进度类型,由一个数字来表示。 1.董事会预案 2.股东大会通过 3.实施 4.未通过 12.停止实施 17.股东提议 19.董事会预案预披露分红实施进程：股东提议--董事会预案--股东大会--实施
		TQCVDProgressType	Progress;

		///除权除息日
		TQCVDDateType	ExDate;

		///每股送转
		TQCVDShareType	STKDvdPerSh;

		///每股派息(税前)(元)
		TQCVDCashShareType	CashDvdPerShPreTax;

		///每股派息(税后)(元)
		TQCVDCashShareType	CashDvdPerShAfterTax;

		///股权登记日
		TQCVDDateType	EqyRecordDate;

		///派息日
		TQCVDDateType	DvdPayoutDate;

		///红股上市日
		TQCVDDateType	ListingDateOfDvdShr;

		///预案公告日
		TQCVDDateType	PrelanDate;

		///股东大会公告日
		TQCVDDateType	SMTGDate;

		///分红实施公告日
		TQCVDDateType	DvdAnnDate;

		///基准日期
		TQCVDDateType	BaseDate;

		///基准股本(万股)
		TQCVDQuantityType	BaseShare;

		///货币代码
		TQCVDCodeType	CrncyCode;

		///方案是否变更
		TQCVDIsChangedType	IsChanged;

		///分红年度
		TQCVDDateType	ReportPeriod;

		///方案变更说明
		TQCVDChangeContentType	Change;

		///每股送股比例
		TQCVDShareType	BonusRate;

		///每股转增比例
		TQCVDShareType	ConversedRate;

		///备注
		TQCVDMemoType	Memo;

		///预案预披露公告日
		TQCVDDateType	PreAnnDate;

		///分红对象
		TQCVDObjectType	DivObject;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询送股配股信息
	struct CQCVDQryRightIssueInfoField
	{
		///起始日期
		TQCVDDateType	BegDate;

		///结束日期
		TQCVDDateType	EndDate;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 送股配股数据
	struct CQCVDRightIssueDataField
	{
		///最新公告日期
		TQCVDDateType	AnnouncementDate;

		///方案进度
		TQCVDProgressType	Progress;

		///配股价格(元)
		TQCVDDvdPriceType	Price;

		///配股比例
		TQCVDRatioShareType	Ratio;

		///配股计划数量(万股)
		TQCVDQuantityType	Amount;

		///配股实际数量(万股)
		TQCVDQuantityType	AmountAct;

		///募集资金(元)
		TQCVDDvdPriceType	NetCollection;

		///股权登记日
		TQCVDDateType	RegDateShare;

		///除权日
		TQCVDDateType	ExDividendDate;

		///配股上市日
		TQCVDDateType	ListedDate;

		///缴款起始日
		TQCVDDateType	PayStartDate;

		///缴款终止日
		TQCVDDateType	PayEndDate;

		///预案公告日
		TQCVDDateType	PrePlanDate;

		///股东大会公告日
		TQCVDDateType	SMTGAnnceDate;

		///发审委通过公告日
		TQCVDDateType	PassDate;

		///证监会核准公告日
		TQCVDDateType	ApprovedDate;

		///配股实施公告日
		TQCVDDateType	AnnceDate;

		///配股结果公告日
		TQCVDDateType	ResultDate;

		///上市公告日
		TQCVDDateType	ListAnnDate;

		///基准年度
		TQCVDDateType	Guarantor;

		///基准股本(万股)
		TQCVDQuantityType	Guartype;

		///配售代码
		TQCVDCodeType	Code;

		///配股年度
		TQCVDDateType	Year;

		///配股说明
		TQCVDShareContentType	Content;

		///配股简称
		TQCVDDvdNameType	Name;

		///配股比例分母
		TQCVDRatioShareType	RatioDenominator;

		///配股比例分子
		TQCVDRatioShareType	RatioMolecular;

		///认购方式
		TQCVDSubscriptionMethodType	SubscriptionMethod;

		///预计募集资金(元)
		TQCVDDvdPriceType	ExpectedFundRaising;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询公司资料信息
	struct CQCVDQryCompanyDescriptionInfoField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询股本结构信息
	struct CQCVDQryEquityStructureInfoField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询主营业务信息
	struct CQCVDQrySalesSegmentInfoField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 主营业务数据
	struct CQCVDSalesSegmentDataField
	{
		///报告期
		TQCVDDateType	ReportPeriod;

		///货币代码
		TQCVDCodeType	CrncyCode;

		///项目类别
		TQCVDItemCodeType	ItemCode;

		///主营业务项目
		TQCVDItemType	Item;

		///主营业务收入(元)
		TQCVDPriceType	Sales;

		///主营业务利润(元)
		TQCVDPriceType	Profit;

		///主营业务成本(元)
		TQCVDPriceType	Cost;

		///是否公布值
		TQCVDIsChangedType	IsPublishedValue;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询十大股东信息
	struct CQCVDQryTopTenHoldersInfoField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 十大股东数据
	struct CQCVDTopTenHoldersDataField
	{
		///最新公告日期
		TQCVDDateType	AnnouncementDate;

		///股东名称
		TQCVDHolderNameType	HolderName;

		///持股数量
		TQCVDQuantityType	HolderQuantity;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询十大流通股东信息
	struct CQCVDQryTopTenFloatHoldersInfoField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 十大流通股东数据
	struct CQCVDTopTenFloatHoldersDataField
	{
		///最新公告日期
		TQCVDDateType	AnnouncementDate;

		///股东名称
		TQCVDFloatHolderNameType	HolderName;

		///持股数量
		TQCVDQuantityType	HolderQuantity;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询个股所属行业板块信息
	struct CQCVDQryIndustryInfoField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 个股所属行业板块数据
	struct CQCVDIndustryDataField
	{
		///纳入日期
		TQCVDDateType	EntryDate;

		///剔除日期
		TQCVDDateType	RemoveDate;

		///行业代码
		TQCVDIndustriesCodeType	IndustriesCode;

		///行业名称
		TQCVDIndustriesNameType	IndustriesName;

		///级数
		TQCVDLevelNumType	LevelNum;

		///是否有效
		TQCVDUsedType	Used;

		///板块别名
		TQCVDIndustriesAliasType	IndustriesAlias;

		///展示序号
		TQCVDSequenceType	Sequence;

		///备注
		TQCVDIndustriesMemoType	Memo;

		///板块中文定义
		TQCVDChineseDfinitionType	ChineseDfinition;

		///板块英文名称
		TQCVDIndustriesNameEngType	IndustriesNameEng;

		///行业对应指数代码
		TQCVDWindCodeType	IndexCode;

		///行业对应指数名称
		TQCVDIndustriesNameType	Name;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询个股所属概念板块信息
	struct CQCVDQryConceptionInfoField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 个股所属概念板块数据
	struct CQCVDConceptionDataField
	{
		///Wind概念板块代码
		TQCVDConceptionCodeType	ConceptionCode;

		///Wind概念板块名称
		TQCVDConceptionNameType	ConceptionName;

		///纳入日期
		TQCVDDateType	EntryDate;

		///剔除日期
		TQCVDDateType	RemoveDate;

		///最新标志
		TQCVDCurSignType	CurSign;

		///行业代码
		TQCVDIndustriesCodeType	IndustriesCode;

		///行业名称
		TQCVDIndustriesNameType	IndustriesName;

		///级数
		TQCVDLevelNumType	LevelNum;

		///是否有效
		TQCVDUsedType	Used;

		///板块别名
		TQCVDIndustriesAliasType	IndustriesAlias;

		///展示序号
		TQCVDSequenceType	Sequence;

		///备注
		TQCVDIndustriesMemoType	Memo;

		///板块中文定义
		TQCVDChineseDfinitionType	ChineseDfinition;

		///板块英文名称
		TQCVDIndustriesNameEngType	IndustriesNameEng;

		///行业对应指数代码
		TQCVDWindCodeType	IndexCode;

		///行业对应指数名称
		TQCVDIndustriesNameType	Name;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询个股所属地域板块信息
	struct CQCVDQryRegionInfoField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 个股所属地域板块数据
	struct CQCVDRegionDataField
	{
		///行业代码
		TQCVDIndustriesCodeType	IndustriesCode;

		///行业名称
		TQCVDIndustriesNameType	IndustriesName;

		///级数
		TQCVDLevelNumType	LevelNum;

		///是否有效
		TQCVDUsedType	Used;

		///板块别名
		TQCVDIndustriesAliasType	IndustriesAlias;

		///展示序号
		TQCVDSequenceType	Sequence;

		///备注
		TQCVDIndustriesMemoType	Memo;

		///板块中文定义
		TQCVDChineseDfinitionType	ChineseDfinition;

		///板块英文名称
		TQCVDIndustriesNameEngType	IndustriesNameEng;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询指数描述信息
	struct CQCVDQryIndexDescriptionInfoField
	{
		///指数代码
		TQCVDIndexIDType	IndexID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 指数描述信息数据
	struct CQCVDIndexDescriptionDataField
	{
		///指数简称
		TQCVDIndustriesNameType	Name;

		///板块代码
		TQCVDIndustryCodeType	IndustryCode;

		///板块名称
		TQCVDIndustriesNameType	IndustryName;

		///板块代码2
		TQCVDIndustryCodeType	IndustryCode2;

		///板块英文名称
		TQCVDIndustriesNameEngType	IndustryNameEng;

		///指数代码
		TQCVDIndexIDType	IndexID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询行业板块成分股信息
	struct CQCVDQryIndustryConstituentsInfoField
	{
		///指数代码
		TQCVDIndexIDType	IndexID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询概念板块成分股信息
	struct CQCVDQryConceptionConstituentsInfoField
	{
		///指数代码
		TQCVDIndexIDType	IndexID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询板块指数实时行情信息
	struct CQCVDQryIndustryCodeListField
	{
		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 行业板块列表数据
	struct CQCVDIndustryCodeListDataField
	{
		///板块中文定义
		TQCVDChineseDfinitionType	ChineseDfinition;

		///交易日期
		TQCVDDateType	TradingDay;

		///昨日收盘点位
		TQCVDPriceType	PreClosePoint;

		///指数代码
		TQCVDIndexIDType	IndexID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询概念指数实时行情信息
	struct CQCVDQryConceptionCodeListField
	{
		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 概念板块列表数据
	struct CQCVDConceptionCodeListDataField
	{
		///板块中文定义
		TQCVDChineseDfinitionType	ChineseDfinition;

		///交易日期
		TQCVDDateType	TradingDay;

		///昨日收盘点位
		TQCVDPriceType	PreClosePoint;

		///指数代码
		TQCVDIndexIDType	IndexID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询自由流通股本信息
	struct CQCVDQryFreeFloatSharesInfoField
	{
		///起始日期
		TQCVDDateType	BegDate;

		///结束日期
		TQCVDDateType	EndDate;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 自由流通股本数据
	struct CQCVDFreeFloatSharesDataField
	{
		///自由流通股本(万股)
		TQCVDShareType	FreeShares;

		///变动日期(除权日)
		TQCVDDateType	ChangeDateEX;

		///变动日期(上市日)
		TQCVDDateType	ChangeDateList;

		///最新公告日期
		TQCVDDateType	AnnouncementDate;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 订阅异常明细项
	struct CQCVDEffectDetailItemField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///波动阀值
		TQCVDRatioType	EffectRatio;
	};

	/// 价格异常波动委托明细
	struct CQCVDEffectOrderDetailField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///委托价格
		TQCVDPriceType	Price;

		///委托数量
		TQCVDLongVolumeType	Volume;

		///委托类别
		TQCVDLOrderTypeType	OrderType;

		///委托方向
		TQCVDLSideType	Side;

		///波动幅度
		TQCVDRatioType	EffectRatio;

		///委托主序号
		TQCVDSequenceNoType	OrderSeq1;

		///委托子序号
		TQCVDSequenceNoType	OrderSeq2;

		///更新时间(秒)
		TQCVDTimeType	UpdateTime;

		///更新时间(毫秒)
		TQCVDMillisecType	UpdateMillisec;

		///委托子序号
		TQCVDLongSequenceType	OrderSeq3;

		///订单状态
		TQCVDLOrderStatusType	OrderStatus;

		///更新时间戳
		TQCVDTimeStampType	OrderTime;
	};

	/// 价格异常波动成交明细
	struct CQCVDEffectTradeDetailField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///成交价格
		TQCVDPriceType	TradePrice;

		///成交数量
		TQCVDLongVolumeType	TradeVolume;

		///成交类别
		TQCVDExecTypeType	ExecType;

		///波动幅度
		TQCVDRatioType	EffectRatio;

		///成交主序号
		TQCVDSequenceNoType	TradeSeq1;

		///成交子序号
		TQCVDSequenceNoType	TradeSeq2;

		///买方委托序号
		TQCVDSequenceNoType	BuySideSeq;

		///卖方委托序号
		TQCVDSequenceNoType	SelSideSeq;

		///更新时间(秒)
		TQCVDTimeType	UpdateTime;

		///更新时间(毫秒)
		TQCVDMillisecType	UpdateMillisec;

		///更新时间戳
		TQCVDTimeStampType	TradeTime;
	};

	/// 查询盘口委托
	struct CQCVDInquiryQueueingOrdersField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///盘口价格
		TQCVDPriceType	QueuePrice;
	};

	/// 盘口委托
	struct CQCVDQueueingOrderField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///委托价格
		TQCVDPriceType	Price;

		///剩余未成交的挂单数量
		TQCVDLongVolumeType	Volume;

		///委托类别
		TQCVDLOrderTypeType	OrderType;

		///委托方向
		TQCVDLSideType	Side;

		///委托主序号
		TQCVDSequenceNoType	OrderSeq1;

		///委托子序号
		TQCVDSequenceNoType	OrderSeq2;

		///委托子序号(只有上海行情有效)
		TQCVDSequenceNoType	OrderSeq3;

		///时间戳
		TQCVDTimeStampType	OrderTime;
	};

	/// 查询1档盘口数量
	struct CQCVDInquiryFirstLevelVolumesField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
	};

	/// 1档盘口数量
	struct CQCVDFirstLevelVolumesField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///买1价
		TQCVDPriceType	BuyPrice;
		///买1量个数
		TQCVDVolumeArraySizeType	FirstLevelBuyNum;
		///买1量数组
		TQCVDVolumeType	FirstLevelBuyOrderVolumes[CONST_FIRSTLEVELVOLUMEARRAYSIZE];
		///卖1价
		TQCVDPriceType	SellPrice;
		///卖1量个数
		TQCVDVolumeArraySizeType	FirstLevelSellNum;
		///卖1量数组
		TQCVDVolumeType	FirstLevelSellOrderVolumes[CONST_FIRSTLEVELVOLUMEARRAYSIZE];
	};

	/// 查询持仓量价分布信息
	struct CQCVDQryPriceDistributionInfoField
	{
		///起始日期
		TQCVDDateType	BegDate;

		///结束日期
		TQCVDDateType	EndDate;

		///持仓分位点百分比
		TQCVDPercentValType	PercentNum;

		///价格数量分布柱状图的柱子数
		TQCVDVolumeType	DistributionType;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 持仓量价分布数据
	struct CQCVDPriceDistributionDataField
	{
		///数据日期
		TQCVDDateType	DataDate;

		///持仓分位点百分比
		TQCVDPercentValType	PercentNum;

		///在持仓分位点的价格
		TQCVDRatioType	PercentValue;

		///流通股中成本价小于现价的百分比
		TQCVDPercentValType	percentNow;

		///价格数量分布柱状图的柱子数
		TQCVDVolumeType	DistributionType;

		///价格数量分布的数据
		TQCVDDistriValueType	DistributionValue;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询历史极值价格信息
	struct CQCVDQryPriceExtremumInfoField
	{
		///交易日期
		TQCVDDateType	TradingDay;

		///起始时间
		TQCVDTimeType	BegTime;

		///结束时间
		TQCVDTimeType	EndTime;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 历史极值价格数据
	struct CQCVDPriceExtremumDataField
	{
		///交易日期
		TQCVDDateType	HighestTradingDay;

		///更新时间
		TQCVDTimeType	HighestUpdateTime;

		///更新毫秒
		TQCVDMillisecType	HighestUpdateMillisec;

		///最高价(元)
		TQCVDPriceType	HighestPrice;

		///交易日期
		TQCVDDateType	LowestTradingDay;

		///更新时间
		TQCVDTimeType	LowestUpdateTime;

		///更新毫秒
		TQCVDMillisecType	LowestUpdateMillisec;

		///最高价(元)
		TQCVDPriceType	LowestPrice;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询地域版块代码列表
	struct CQCVDQryRegionCodeListField
	{
		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 地域版块代码列表数据
	struct CQCVDRegionCodeListDataField
	{
		///板块中文定义
		TQCVDChineseDfinitionType	ChineseDfinition;

		///指数代码
		TQCVDIndexIDType	IndexID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 板块指数实时行情数据
	struct CQCVDIndustryIndexDataField
	{
		///行业板块指数今日实时点位
		TQCVDPriceType	IndexPoint;

		///行业板块指数昨日收盘点位
		TQCVDPriceType	PreClosePoint;

		///今开盘点位
		TQCVDPriceType	OpenPoint;

		///今收盘点位
		TQCVDPriceType	ClosePoint;

		///行业板块指数今日最高点位
		TQCVDPriceType	HighestPoint;

		///行业板块指数今日最低点位
		TQCVDPriceType	LowestPoint;

		///交易日期
		TQCVDDateType	TradingDay;

		///更新时间
		TQCVDTimeType	UpdateTime;

		///更新毫秒
		TQCVDMillisecType	UpdateMillisec;

		///指数代码
		TQCVDIndexIDType	IndexID;

		///页定位符
		TQCVDPageLocateType	PageLocate;

		///成交量(手)
		TQCVDMoneyType	Volume;

		///成交金额(千元)
		TQCVDMoneyType	Turnover;
	};

	/// 概念指数实时行情数据
	struct CQCVDConceptionIndexDataField
	{
		///概念板块指数今日实时点位
		TQCVDPriceType	IndexPoint;

		///概念板块指数昨日收盘点位
		TQCVDPriceType	PreClosePoint;

		///今开盘点位
		TQCVDPriceType	OpenPoint;

		///今收盘点位
		TQCVDPriceType	ClosePoint;

		///概念板块指数今日最高点位
		TQCVDPriceType	HighestPoint;

		///概念板块指数今日最低点位
		TQCVDPriceType	LowestPoint;

		///交易日期
		TQCVDDateType	TradingDay;

		///更新时间
		TQCVDTimeType	UpdateTime;

		///更新毫秒
		TQCVDMillisecType	UpdateMillisec;

		///指数代码
		TQCVDIndexIDType	IndexID;

		///页定位符
		TQCVDPageLocateType	PageLocate;

		///成交量(手)
		TQCVDMoneyType	Volume;

		///成交金额(千元)
		TQCVDMoneyType	Turnover;
	};

	/// 地域指数实时行情数据
	struct CQCVDRegionIndexDataField
	{
		///地域板块指数今日实时点位
		TQCVDPriceType	IndexPoint;

		///地域板块指数昨日收盘点位
		TQCVDPriceType	PreClosePoint;

		///今开盘点位
		TQCVDPriceType	OpenPoint;

		///今收盘点位
		TQCVDPriceType	ClosePoint;

		///地域板块指数今日最高点位
		TQCVDPriceType	HighestPoint;

		///地域板块指数今日最低点位
		TQCVDPriceType	LowestPoint;

		///交易日期
		TQCVDDateType	TradingDay;

		///更新时间
		TQCVDTimeType	UpdateTime;

		///更新毫秒
		TQCVDMillisecType	UpdateMillisec;

		///指数代码
		TQCVDIndexIDType	IndexID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询特定行情快照
	struct CQCVDInquirySpecialMarketDataField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;
	};

	/// 极速行情快照
	struct CQCVDRapidMarketDataField
	{
		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///行情交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///时间戳
		TQCVDTimeStampType	DataTimeStamp;

		///昨收盘
		TQCVDPriceType	PreClosePrice;

		///今开盘
		TQCVDPriceType	OpenPrice;

		///成交笔数
		TQCVDLongVolumeType	NumTrades;

		///成交总量
		TQCVDLongVolumeType	TotalVolumeTrade;

		///成交总金额
		TQCVDMoneyType	TotalValueTrade;

		///委托买入总量（只有上交所推送）
		TQCVDLongVolumeType	TotalBidVolume;

		///加权平均委托买价格（只有上交所推送）
		TQCVDPriceType	AvgBidPrice;

		///委托卖出总量（只有上交所推送）
		TQCVDLongVolumeType	TotalAskVolume;

		///加权平均委托卖价格（只有上交所推送）
		TQCVDPriceType	AvgAskPrice;

		///最高价
		TQCVDPriceType	HighestPrice;

		///最低价
		TQCVDPriceType	LowestPrice;

		///现价
		TQCVDPriceType	LastPrice;

		///申买价一
		TQCVDPriceType	BidPrice1;

		///申买量一
		TQCVDLongVolumeType	BidVolume1;

		///申卖价一
		TQCVDPriceType	AskPrice1;

		///申卖量一
		TQCVDLongVolumeType	AskVolume1;

		///申卖价二
		TQCVDPriceType	AskPrice2;

		///申卖量二
		TQCVDLongVolumeType	AskVolume2;

		///申卖价三
		TQCVDPriceType	AskPrice3;

		///申卖量三
		TQCVDLongVolumeType	AskVolume3;

		///申买价二
		TQCVDPriceType	BidPrice2;

		///申买量二
		TQCVDLongVolumeType	BidVolume2;

		///申买价三
		TQCVDPriceType	BidPrice3;

		///申买量三
		TQCVDLongVolumeType	BidVolume3;

		///申卖价四
		TQCVDPriceType	AskPrice4;

		///申卖量四
		TQCVDLongVolumeType	AskVolume4;

		///申卖价五
		TQCVDPriceType	AskPrice5;

		///申卖量五
		TQCVDLongVolumeType	AskVolume5;

		///申买价四
		TQCVDPriceType	BidPrice4;

		///申买量四
		TQCVDLongVolumeType	BidVolume4;

		///申买价五
		TQCVDPriceType	BidPrice5;

		///申买量五
		TQCVDLongVolumeType	BidVolume5;

		///申卖价六
		TQCVDPriceType	AskPrice6;

		///申卖量六
		TQCVDLongVolumeType	AskVolume6;

		///申卖价七
		TQCVDPriceType	AskPrice7;

		///申卖量七
		TQCVDLongVolumeType	AskVolume7;

		///申买价六
		TQCVDPriceType	BidPrice6;

		///申买量六
		TQCVDLongVolumeType	BidVolume6;

		///申买价七
		TQCVDPriceType	BidPrice7;

		///申买量七
		TQCVDLongVolumeType	BidVolume7;

		///申卖价八
		TQCVDPriceType	AskPrice8;

		///申卖量八
		TQCVDLongVolumeType	AskVolume8;

		///申卖价九
		TQCVDPriceType	AskPrice9;

		///申卖量九
		TQCVDLongVolumeType	AskVolume9;

		///申买价八
		TQCVDPriceType	BidPrice8;

		///申买量八
		TQCVDLongVolumeType	BidVolume8;

		///申买价九
		TQCVDPriceType	BidPrice9;

		///申买量九
		TQCVDLongVolumeType	BidVolume9;

		///申买价十
		TQCVDPriceType	BidPrice10;

		///申买量十
		TQCVDLongVolumeType	BidVolume10;

		///申卖价十
		TQCVDPriceType	AskPrice10;

		///申卖量十
		TQCVDLongVolumeType	AskVolume10;

		///附加信息1
		TQCVDIntInfoType	Info1;

		///附加信息2
		TQCVDIntInfoType	Info2;

		///附加信息3
		TQCVDIntInfoType	Info3;

		///涨停板价(只有深圳行情有效)
		TQCVDPriceType	UpperLimitPrice;

		///跌停板价(只有深圳行情有效)
		TQCVDPriceType	LowerLimitPrice;

		///今收盘价(只有上海行情有效)
		TQCVDPriceType	ClosePrice;

		///行情产品实时状态
		TQCVDMDSecurityStatType	MDSecurityStat;

		///买入总笔数(只有上海行情有效)
		TQCVDVolumeType	TotalBidNumber;

		///卖出总笔数(只有上海行情有效)
		TQCVDVolumeType	TotalOfferNumber;

		///买入委托成交量最大等待时间(只有上海行情有效)
		TQCVDVolumeType	BidTradeMaxDuration;

		///卖出委托成交量最大等待时间(只有上海行情有效)
		TQCVDVolumeType	OfferTradeMaxDuration;
	};

	/// 查询地域板块成分股信息
	struct CQCVDQryRegionConstituentsInfoField
	{
		///指数代码
		TQCVDIndexIDType	IndexID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 地域板块成分股数据
	struct CQCVDRegionConstituentsDataField
	{
		///地域指数代码
		TQCVDIndexIDType	IndexID;

		///地域指数名称
		TQCVDIndustriesNameType	RegionName;

		///纳入日期
		TQCVDDateType	EntryDate;

		///剔除日期
		TQCVDDateType	RemoveDate;

		///最新标志
		TQCVDCurSignType	CurSign;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询文件委托请求
	struct CQCVDInquiryFileOrderField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///委托起始流水号
		TQCVDSequenceNoType	OrderSerialBeg;

		///委托结束流水号
		TQCVDSequenceNoType	OrderSerialEnd;

		///委托提交状态
		TQCVDCommitStatusType	CommitStatus;
	};

	/// 文件委托
	struct CQCVDFileOrderField
	{
		///请求编号
		TQCVDRequestIDType	RequestID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///报单引用
		TQCVDOrderRefType	OrderRef;

		///文件报单类别
		TQCVDFileOrderTypeType	FileOrderType;

		///买卖方向
		TQCVDDirectionType	Direction;

		///限价单价格
		TQCVDPriceType	LimitPrice;

		///报单数量
		TQCVDVolumeType	VolumeTotalOriginal;

		///委托方式
		TQCVDOperwayType	Operway;

		///报单操作引用
		TQCVDOrderRefType	OrderActionRef;

		///报单编号
		TQCVDOrderSysIDType	OrderSysID;

		///委托检查
		TQCVDCondCheckType	CondCheck;

		///委托流水号
		TQCVDSequenceNoType	OrderSerial;

		///文件委托提交状态
		TQCVDCommitStatusType	CommitStatus;

		///文件委托状态信息
		TQCVDStatusMsgType	StatusMsg;

		///委托时间戳
		TQCVDBigTimeStampType	TimeStamp;
	};

	/// 文件委托复核请求
	struct CQCVDReviewFileOrderField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///委托起始流水号
		TQCVDSequenceNoType	OrderSerialBeg;

		///委托结束流水号
		TQCVDSequenceNoType	OrderSerialEnd;
	};

	/// 文件提交信息
	struct CQCVDCommitInfoField
	{
		///委托流水号
		TQCVDSequenceNoType	OrderSerial;

		///文件委托提交状态
		TQCVDCommitStatusType	CommitStatus;

		///文件委托状态信息
		TQCVDStatusMsgType	StatusMsg;
	};

	/// 请求插入交易通知
	struct CQCVDReqInsTradingNoticeField
	{
		///通知流水号
		TQCVDSerialType	NoticeSerial;

		///通知日期
		TQCVDDateType	InsertDate;

		///通知时间
		TQCVDTimeType	InsertTime;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///通知消息内容
		TQCVDContentType	Content;

		///操作员
		TQCVDUserIDType	OperatorID;
	};

	/// 交易通知
	struct CQCVDTradingNoticeField
	{
		///通知流水号
		TQCVDSerialType	NoticeSerial;

		///通知日期
		TQCVDDateType	InsertDate;

		///通知时间
		TQCVDTimeType	InsertTime;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///通知消息内容
		TQCVDContentType	Content;

		///操作员
		TQCVDUserIDType	OperatorID;
	};

	/// 装载文件委托
	struct CQCVDLoadFileOrderField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///是否需要复核
		TQCVDBoolType	bReview;
	};

	/// 文件委托信息
	struct CQCVDFileOrderInfoField
	{
		///请求编号
		TQCVDRequestIDType	RequestID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///报单引用
		TQCVDOrderRefType	OrderRef;

		///文件报单类别
		TQCVDFileOrderTypeType	FileOrderType;

		///买卖方向
		TQCVDDirectionType	Direction;

		///限价单价格
		TQCVDPriceType	LimitPrice;

		///报单数量
		TQCVDVolumeType	VolumeTotalOriginal;

		///委托方式
		TQCVDOperwayType	Operway;

		///报单操作引用
		TQCVDOrderRefType	OrderActionRef;

		///报单编号
		TQCVDOrderSysIDType	OrderSysID;

		///委托检查
		TQCVDCondCheckType	CondCheck;

		///委托流水号
		TQCVDSequenceNoType	OrderSerial;

		///文件委托提交状态
		TQCVDCommitStatusType	CommitStatus;

		///文件委托状态信息
		TQCVDStatusMsgType	StatusMsg;
	};

	/// 查询最大报单量请求
	struct CQCVDReqInquiryMaxOrderVolumeField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///买卖方向
		TQCVDDirectionType	Direction;

		///报单价格条件
		TQCVDOrderPriceTypeType	OrderPriceType;

		///组合开平标志
		TQCVDCombOffsetFlagType	CombOffsetFlag;

		///组合投机套保标志
		TQCVDCombHedgeFlagType	CombHedgeFlag;

		///有效期类型
		TQCVDTimeConditionType	TimeCondition;

		///成交量类型
		TQCVDVolumeConditionType	VolumeCondition;

		///价格
		TQCVDPriceType	LimitPrice;

		///转入交易单元代码(仅在转托管操作时有效)
		TQCVDPbuIDType	TransfereePbuID;

		///最大委托手数
		TQCVDVolumeType	MaxVolume;

		///港股通订单数量类型,默认整股买卖
		TQCVDLotTypeType	LotType;
	};

	/// 查询最大报单量应答
	struct CQCVDRspInquiryMaxOrderVolumeField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///买卖方向
		TQCVDDirectionType	Direction;

		///报单价格条件
		TQCVDOrderPriceTypeType	OrderPriceType;

		///组合开平标志
		TQCVDCombOffsetFlagType	CombOffsetFlag;

		///组合投机套保标志
		TQCVDCombHedgeFlagType	CombHedgeFlag;

		///有效期类型
		TQCVDTimeConditionType	TimeCondition;

		///成交量类型
		TQCVDVolumeConditionType	VolumeCondition;

		///价格
		TQCVDPriceType	LimitPrice;

		///转入交易单元代码(仅在转托管操作时有效)
		TQCVDPbuIDType	TransfereePbuID;

		///最大委托手数
		TQCVDVolumeType	MaxVolume;

		///港股通订单数量类型,默认整股买卖
		TQCVDLotTypeType	LotType;
	};

	/// 外围系统仓位转移回报
	struct CQCVDRtnPeripheryTransferPositionField
	{
		///仓位调拨流水号
		TQCVDIntSerialType	PositionSerial;

		///仓位调拨请求流水号
		TQCVDIntSerialType	ApplySerial;

		///前置编号
		TQCVDFrontIDType	FrontID;

		///会话编号
		TQCVDSessionIDType	SessionID;

		///仓位调拨方向
		TQCVDTransferDirectionType	TransferDirection;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///今日买卖仓位转入转出数量
		TQCVDVolumeType	TodayBSPos;

		///今日申赎仓位转入数量
		TQCVDVolumeType	TodayPRPos;

		///昨日仓位转入数量
		TQCVDVolumeType	HistoryPos;

		///交易日
		TQCVDDateType	TradingDay;

		///仓位调拨原因
		TQCVDTransferReasonType	TransferReason;

		///转移状态
		TQCVDTransferStatusType	TransferStatus;

		///操作日期
		TQCVDDateType	OperateDate;

		///操作时间
		TQCVDTimeType	OperateTime;

		///冲正日期
		TQCVDDateType	RepealDate;

		///冲正时间
		TQCVDTimeType	RepealTime;

		///冲正原因
		TQCVDTransferReasonType	RepealReason;

		///状态信息
		TQCVDErrorMsgType	StatusMsg;

		///今日拆分合并仓位转入数量
		TQCVDVolumeType	TodaySMPos;
	};

	/// 外围系统资金转移回报
	struct CQCVDRtnPeripheryTransferFundField
	{
		///资金调拨流水号
		TQCVDIntSerialType	FundSerial;

		///资金调拨请求流水号
		TQCVDIntSerialType	ApplySerial;

		///前置编号
		TQCVDFrontIDType	FrontID;

		///会话编号
		TQCVDSessionIDType	SessionID;

		///资金调拨方向
		TQCVDTransferDirectionType	TransferDirection;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///币种
		TQCVDCurrencyIDType	CurrencyID;

		///转移金额
		TQCVDMoneyType	Amount;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///资金调拨原因
		TQCVDTransferReasonType	TransferReason;

		///转移状态
		TQCVDTransferStatusType	TransferStatus;

		///操作日期
		TQCVDDateType	OperateDate;

		///操作时间
		TQCVDTimeType	OperateTime;

		///冲正日期
		TQCVDDateType	RepealDate;

		///冲正时间
		TQCVDTimeType	RepealTime;

		///冲正原因
		TQCVDTransferReasonType	RepealReason;

		///状态信息
		TQCVDErrorMsgType	StatusMsg;
	};

	/// 查询历史委托
	struct CQCVDQryHistoryOrderField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///起始日期
		TQCVDDateType	BegDate;

		///结束日期
		TQCVDDateType	EndDate;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 历史委托信息
	struct CQCVDHistoryOrderField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///报单引用
		TQCVDOrderRefType	OrderRef;

		///用户代码
		TQCVDUserIDType	UserID;

		///报单价格条件
		TQCVDOrderPriceTypeType	OrderPriceType;

		///买卖方向
		TQCVDDirectionType	Direction;

		///组合开平标志
		TQCVDCombOffsetFlagType	CombOffsetFlag;

		///组合投机套保标志
		TQCVDCombHedgeFlagType	CombHedgeFlag;

		///价格
		TQCVDPriceType	LimitPrice;

		///数量
		TQCVDVolumeType	VolumeTotalOriginal;

		///有效期类型
		TQCVDTimeConditionType	TimeCondition;

		///成交量类型
		TQCVDVolumeConditionType	VolumeCondition;

		///最小成交量
		TQCVDVolumeType	MinVolume;

		///强平原因
		TQCVDForceCloseReasonType	ForceCloseReason;

		///请求编号
		TQCVDRequestIDType	RequestID;

		///本地报单编号
		TQCVDOrderLocalIDType	OrderLocalID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///证券在交易所代码
		TQCVDExchangeInstIDType	ExchangeInstID;

		///交易单元代码
		TQCVDTraderIDType	TraderID;

		///报单提交状态
		TQCVDOrderSubmitStatusType	OrderSubmitStatus;

		///交易日
		TQCVDDateType	TradingDay;

		///系统报单编号
		TQCVDOrderSysIDType	OrderSysID;

		///报单状态
		TQCVDOrderStatusType	OrderStatus;

		///报单类型
		TQCVDOrderTypeType	OrderType;

		///已成交数量
		TQCVDVolumeType	VolumeTraded;

		///剩余未完成数量
		TQCVDVolumeType	VolumeTotal;

		///报单日期
		TQCVDDateType	InsertDate;

		///报单时间
		TQCVDTimeType	InsertTime;

		///撤销时间
		TQCVDTimeType	CancelTime;

		///最后修改交易单元代码
		TQCVDTraderIDType	ActiveTraderID;

		///前置编号
		TQCVDFrontIDType	FrontID;

		///会话编号
		TQCVDSessionIDType	SessionID;

		///用户端产品信息
		TQCVDProductInfoType	UserProductInfo;

		///状态信息
		TQCVDErrorMsgType	StatusMsg;

		///用户强评标志
		TQCVDBoolType	UserForceClose;

		///操作用户代码
		TQCVDUserIDType	ActiveUserID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///IP地址
		TQCVDIPAddressType	IPAddress;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///港股通订单数量类型,默认整股买卖
		TQCVDLotTypeType	LotType;

		///长字符串附加信息
		TQCVDBigsInfoType	BInfo;

		///短字符串附加信息
		TQCVDShortsInfoType	SInfo;

		///整形附加信息
		TQCVDIntInfoType	IInfo;

		///转入交易单元代码(仅在转托管操作时有效)
		TQCVDPbuIDType	TransfereePbuID;

		///委托方式
		TQCVDOperwayType	Operway;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///适当性控制业务类别
		TQCVDProperCtrlBusinessTypeType	ProperCtrlBusinessType;

		///适当性控制通过标示
		TQCVDProperCtrlPassFlagType	ProperCtrlPassFlag;

		///条件检查
		TQCVDCondCheckType	CondCheck;

		///是否预埋
		TQCVDBoolType	IsCacheOrder;

		///成交金额
		TQCVDMoneyType	Turnover;

		///回报附加浮点型数据信息
		TQCVDFloatInfoType	RtnFloatInfo;

		///回报附加整型数据
		TQCVDIntInfoType	RtnIntInfo;

		///硬盘序列号
		TQCVDHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDMobileType	Mobile;

		///有效日期
		TQCVDDateType	GTDate;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询历史成交
	struct CQCVDQryHistoryTradeField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///起始日期
		TQCVDDateType	BegDate;

		///结束日期
		TQCVDDateType	EndDate;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 历史成交信息
	struct CQCVDHistoryTradeField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///用户代码
		TQCVDUserIDType	UserID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///成交编号
		TQCVDTradeIDType	TradeID;

		///买卖方向
		TQCVDDirectionType	Direction;

		///系统报单编号
		TQCVDOrderSysIDType	OrderSysID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///证券在交易所代码
		TQCVDExchangeInstIDType	ExchangeInstID;

		///开平标志
		TQCVDOffsetFlagType	OffsetFlag;

		///投机套保标志
		TQCVDHedgeFlagType	HedgeFlag;

		///成交价格
		TQCVDPriceType	Price;

		///成交数量
		TQCVDVolumeType	Volume;

		///成交日期
		TQCVDDateType	TradeDate;

		///成交时间
		TQCVDTimeType	TradeTime;

		///交易单元代码
		TQCVDTraderIDType	TraderID;

		///本地报单编号
		TQCVDOrderLocalIDType	OrderLocalID;

		///交易日
		TQCVDDateType	TradingDay;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///报单引用
		TQCVDOrderRefType	OrderRef;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///实收佣金
		TQCVDPriceType	ActualBrokerage;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 录入标记事件
	struct CQCVDInputRemarkEventField
	{
		///录入标记事件序号
		TQCVDEventSequenceNoType	SequenceNo;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///标记内容
		TQCVDRemarkType	Remark;
	};

	/// 录入标记事件结果返回
	struct CQCVDRspInputRemarkEventField
	{
		///录入标记事件序号
		TQCVDEventSequenceNoType	SequenceNo;

		///发生日期
		TQCVDDateType	EventDate;

		///发生时间
		TQCVDTimeType	EventTime;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///标记内容
		TQCVDRemarkType	Remark;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;
	};

	/// 更新标记事件
	struct CQCVDUpdateRemarkEventField
	{
		///录入标记事件序号（查询条件，不可更新）
		TQCVDEventSequenceNoType	SequenceNo;

		///发生日期（仅供序号冲突时作为查询条件使用。不可更新）
		TQCVDDateType	EventDate;

		///发生时间（仅供序号冲突时作为查询条件使用。不可更新）
		TQCVDTimeType	EventTime;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///标记内容
		TQCVDRemarkType	Remark;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;
	};

	/// 更新标记事件结果返回
	struct CQCVDRspUpdateRemarkEventField
	{
		///录入标记事件序号（查询条件，不可更新）
		TQCVDEventSequenceNoType	SequenceNo;

		///发生日期（仅供序号冲突时作为查询条件使用。不可更新）
		TQCVDDateType	EventDate;

		///发生时间（仅供序号冲突时作为查询条件使用。不可更新）
		TQCVDTimeType	EventTime;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///标记内容
		TQCVDRemarkType	Remark;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;
	};

	/// 删除标记事件
	struct CQCVDDeleteRemarkEventField
	{
		///删除标记事件序号（查询条件，不可为空）
		TQCVDEventSequenceNoType	SequenceNo;

		///发生日期（仅供序号冲突时作为查询条件使用。可为空）
		TQCVDDateType	EventDate;

		///发生时间（仅供序号冲突时作为查询条件使用。可为空）
		TQCVDTimeType	EventTime;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;
	};

	/// 删除标记事件结果返回
	struct CQCVDRspDeleteRemarkEventField
	{
		///删除标记事件序号（查询条件，不可为空）
		TQCVDEventSequenceNoType	SequenceNo;
	};

	/// 查询历史标记事件
	struct CQCVDQryRemarkEventField
	{
		///录入标记事件序号
		TQCVDEventSequenceNoType	SequenceNo;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///发生起始日期
		TQCVDDateType	EventDateStart;

		///发生结束日期
		TQCVDDateType	EventDateEnd;

		///发生起始时间
		TQCVDTimeType	EventTimeStart;

		///发生结束时间
		TQCVDTimeType	EventTimeEnd;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 标记事件
	struct CQCVDRemarkEventField
	{
		///录入标记事件序号
		TQCVDEventSequenceNoType	SequenceNo;

		///发生日期
		TQCVDDateType	EventDate;

		///发生时间
		TQCVDTimeType	EventTime;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///标记内容
		TQCVDRemarkType	Remark;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询交易所
	struct CQCVDQryExchangeField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
	};

	/// 交易所
	struct CQCVDExchangeField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///交易所名称
		TQCVDNameType	ExchangeName;

		///交易日
		TQCVDDateType	TradingDay;
	};

	/// 查询PBU
	struct CQCVDQryPBUField
	{
		///交易单元代码
		TQCVDPbuIDType	PbuID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;
	};

	/// PBU
	struct CQCVDPBUField
	{
		///交易单元代码
		TQCVDPbuIDType	PbuID;

		///交易单元名称
		TQCVDNameType	PbuName;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;
	};

	/// 查询实时行情
	struct CQCVDQryMarketDataField
	{
		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
	};

	/// 查询证券信息
	struct CQCVDQrySecurityField
	{
		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券在交易所代码
		TQCVDExchangeInstIDType	ExchangeInstID;

		///产品代码
		TQCVDProductIDType	ProductID;
	};

	/// 证券信息
	struct CQCVDSecurityField
	{
		///合约代码
		TQCVDSecurityIDType	SecurityID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///合约名称
		TQCVDSecurityNameType	SecurityName;

		///合约在交易所的代码
		TQCVDExchangeInstIDType	ExchangeInstID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///产品代码
		TQCVDProductIDType	ProductID;

		///证券类别代码
		TQCVDSecurityTypeType	SecurityType;

		///申报单位
		TQCVDOrderUnitType	OrderUnit;

		///限价买入交易单位
		TQCVDTradingUnitType	LimitBuyTradingUnit;

		///限价卖出交易单位
		TQCVDTradingUnitType	LimitSellTradingUnit;

		///市价单买最大下单量
		TQCVDVolumeType	MaxMarketOrderBuyVolume;

		///市价单买最小下单量
		TQCVDVolumeType	MinMarketOrderBuyVolume;

		///限价单买最大下单量
		TQCVDVolumeType	MaxLimitOrderBuyVolume;

		///限价单买最小下单量
		TQCVDVolumeType	MinLimitOrderBuyVolume;

		///市价单卖最大下单量
		TQCVDVolumeType	MaxMarketOrderSellVolume;

		///市价单卖最小下单量
		TQCVDVolumeType	MinMarketOrderSellVolume;

		///限价单卖最大下单量
		TQCVDVolumeType	MaxLimitOrderSellVolume;

		///限价单卖最小下单量
		TQCVDVolumeType	MinLimitOrderSellVolume;

		///数量乘数
		TQCVDVolumeMultipleType	VolumeMultiple;

		///最小变动价位
		TQCVDPriceTickType	PriceTick;

		///上市日
		TQCVDDateType	OpenDate;

		///持仓类型
		TQCVDPositionTypeType	PositionType;

		///面值
		TQCVDParValueType	ParValue;

		///证券状态
		TQCVDSecurityStatusType	SecurityStatus;

		///债券应计利息
		TQCVDInterestType	BondInterest;

		///折算率
		TQCVDRatioType	ConversionRate;

		///是否担保品
		TQCVDBoolType	IsCollateral;

		///昨收盘价
		TQCVDPriceType	PreClosePrice;

		///涨停板价
		TQCVDPriceType	UpperLimitPrice;

		///跌停板价
		TQCVDPriceType	LowerLimitPrice;

		///交易日
		TQCVDDateType	TradingDay;

		///证券名称(短)
		TQCVDShortSecurityNameType	ShortSecurityName;

		///市价买入交易单位
		TQCVDTradingUnitType	MarketBuyTradingUnit;

		///市价卖出交易单位
		TQCVDTradingUnitType	MarketSellTradingUnit;

		///盘后定价买入交易单位
		TQCVDTradingUnitType	FixPriceBuyTradingUnit;

		///盘后定价卖出交易单位
		TQCVDTradingUnitType	FixPriceSellTradingUnit;

		///盘后定价买最大下单量
		TQCVDVolumeType	MaxFixPriceOrderBuyVolume;

		///盘后定价买最小下单量
		TQCVDVolumeType	MinFixPriceOrderBuyVolume;

		///盘后定价卖最大下单量
		TQCVDVolumeType	MaxFixPriceOrderSellVolume;

		///盘后定价卖最小下单量
		TQCVDVolumeType	MinFixPriceOrderSellVolume;
	};

	/// 查询ETF清单信息
	struct CQCVDQryETFFileField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///ETF二级市场交易代码
		TQCVDSecurityIDType	ETFSecurityID;

		///ETF一级市场申赎代码
		TQCVDSecurityIDType	ETFCreRedSecurityID;
	};

	/// ETF清单信息
	struct CQCVDETFFileField
	{
		///交易日
		TQCVDDateType	TradingDay;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///ETF交易代码
		TQCVDSecurityIDType	ETFSecurityID;

		///ETF申赎代码
		TQCVDSecurityIDType	ETFCreRedSecurityID;

		///最小申购赎回单位份数
		TQCVDVolumeType	CreationRedemptionUnit;

		///最大现金替代比例
		TQCVDRatioType	Maxcashratio;

		///是否可申购
		TQCVDBoolType	CreationStatus;

		///是否可赎回
		TQCVDBoolType	RedemptionStatus;

		///预估现金差额
		TQCVDMoneyType	EstimateCashComponent;

		///前一交易日现金差额
		TQCVDMoneyType	CashComponent;

		///前一交易日基金单位净值
		TQCVDMoneyType	NAV;

		///前一交易日申赎基准单位净值
		TQCVDMoneyType	NAVperCU;

		///当日申购赎回基准单位的红利金额
		TQCVDMoneyType	DividendPerCU;

		///ETF申赎类型
		TQCVDCreRedTypeType	ETFCreRedType;

		///ETF证券名称
		TQCVDSecurityNameType	ETFSecurityName;
	};

	/// 查询ETF成份证券信息
	struct CQCVDQryETFBasketField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///ETF二级市场交易代码
		TQCVDSecurityIDType	ETFSecurityID;

		///ETF成份证券代码
		TQCVDSecurityIDType	SecurityID;
	};

	/// ETF成份证券信息
	struct CQCVDETFBasketField
	{
		///交易日
		TQCVDDateType	TradingDay;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///ETF交易代码
		TQCVDSecurityIDType	ETFSecurityID;

		///ETF成份证券代码
		TQCVDSecurityIDType	SecurityID;

		///成分证券名称
		TQCVDSecurityNameType	SecurityName;

		///成分证券数量
		TQCVDVolumeType	Volume;

		///现金替代标志
		TQCVDETFCurrenceReplaceStatusType	ETFCurrenceReplaceStatus;

		///溢价比例
		TQCVDRatioType	Premium;

		///申购替代金额
		TQCVDMoneyType	CreationReplaceAmount;

		///赎回替代金额
		TQCVDMoneyType	RedemptionReplaceAmount;

		///挂牌市场
		TQCVDMarketIDType	MarketID;

		///ETF申赎类型
		TQCVDCreRedTypeType	ETFCreRedType;
	};

	/// 查询经纪公司部门信息
	struct CQCVDQryDepartmentInfoField
	{
		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;
	};

	/// 经纪公司部门信息
	struct CQCVDDepartmentInfoField
	{
		///经纪公司部门代码
		TQCVDDepartmentIDType	DepartmentID;

		///经纪公司部门名称
		TQCVDNameType	DepartmentName;
	};

	/// 查询新股信息
	struct CQCVDQryIPOInfoField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///申购代码
		TQCVDSecurityIDType	SecurityID;
	};

	/// 新股信息
	struct CQCVDIPOInfoField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///申购代码
		TQCVDSecurityIDType	SecurityID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///产品代码
		TQCVDProductIDType	ProductID;

		///证券类别代码
		TQCVDSecurityTypeType	SecurityType;

		///网上申购上限
		TQCVDVolumeType	MaxVolume;

		///最小申购价格
		TQCVDPriceType	MinPrice;

		///币种代码
		TQCVDCurrencyIDCharType	CurrencyID;

		///申购证券名称
		TQCVDSecurityNameType	SecurityName;

		///新股证券代码
		TQCVDSecurityIDType	UnderlyingSecurityID;

		///新股证券名称
		TQCVDSecurityNameType	UnderlyingSecurityName;

		///网上申购最小数量
		TQCVDVolumeType	MinVolume;

		///网上申购单位数量
		TQCVDVolumeType	VolumeUnit;

		///发行方式
		TQCVDIssueModeType	IssueMode;

		///交易日
		TQCVDDateType	TradingDay;

		///最大申购价格
		TQCVDPriceType	MaxPrice;
	};

	/// 查询BrokerUserFunction
	struct CQCVDQryBrokerUserFunctionField
	{
		///用户代码
		TQCVDUserIDType	UserID;
	};

	/// BrokerUserFunction
	struct CQCVDBrokerUserFunctionField
	{
		///用户代码
		TQCVDUserIDType	UserID;

		///功能代码
		TQCVDFunctionIDType	FunctionID;
	};

	/// 查询经纪公司用户与投资者关系
	struct CQCVDQryBUProxyField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///用户代码
		TQCVDUserIDType	UserID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;
	};

	/// 经纪公司用户与投资者关系
	struct CQCVDBUProxyField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///用户代码
		TQCVDUserIDType	UserID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///一级管理机构代码
		TQCVDDepartmentIDType	ManageDepartmentID;

		///二级机构编码
		TQCVDBranchIDType	InnerBranchID;
	};

	/// 查询User
	struct CQCVDQryUserField
	{
		///用户代码
		TQCVDUserIDType	UserID;

		///用户类型
		TQCVDUserTypeType	UserType;
	};

	/// User
	struct CQCVDUserField
	{
		///用户代码
		TQCVDUserIDType	UserID;

		///用户名称
		TQCVDUserNameType	UserName;

		///用户类型
		TQCVDUserTypeType	UserType;

		///是否活跃
		TQCVDBoolType	IsActive;

		///登录限制
		TQCVDLoginLimitType	LoginLimit;
	};

	/// 查询投资者
	struct CQCVDQryInvestorField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
	};

	/// 投资者
	struct CQCVDInvestorField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///投资者名称
		TQCVDInvestorNameType	InvestorName;

		///证件类型
		TQCVDIdCardTypeType	IdCardType;

		///证件号码
		TQCVDIdCardNoType	IdCardNo;

		///联系电话
		TQCVDTelephoneType	Telephone;

		///通讯地址
		TQCVDAddressType	Address;

		///开户日期
		TQCVDDateType	OpenDate;

		///手机
		TQCVDMobileType	Mobile;

		///委托方式
		TQCVDOperwaysType	Operways;

		///专业投资者类别
		TQCVDProfInvestorTypeType	ProfInvestorType;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///是否活跃
		TQCVDBoolType	IsActive;

		///登录限制
		TQCVDLoginLimitType	LoginLimit;

		///交易状态
		TQCVDTradingStatusType	TradingStatus;

		///核心编号
		TQCVDSequenceNoType ServerID;
	};

	/// 查询交易编码
	struct CQCVDQryShareholderAccountField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///股东账户账户代码
		TQCVDShareholderIDType	ShareholderID;

		///股东账户类型
		TQCVDShareholderIDTypeType	ShareholderIDType;
	};

	/// 交易编码
	struct CQCVDShareholderAccountField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///客户代码
		TQCVDShareholderIDType	ShareholderID;

		///股东账户类型
		TQCVDShareholderIDTypeType	ShareholderIDType;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///核心编号
		TQCVDSequenceNoType ServerID;
	};

	/// 查询投资单元
	struct CQCVDQryBusinessUnitField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
	};

	/// 投资单元
	struct CQCVDBusinessUnitField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///投资单元名称
		TQCVDBusinessUnitNameType	BusinessUnitName;
	};

	/// 查询投资单元与交易账户关系
	struct CQCVDQryBusinessUnitAndTradingAcctField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///产品代码
		TQCVDProductIDType	ProductID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///币种
		TQCVDCurrencyIDType	CurrencyID;
	};

	/// 投资单元与交易账户关系
	struct CQCVDBusinessUnitAndTradingAcctField
	{
		///经纪公司代码
		TQCVDInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///客户代码
		TQCVDShareholderIDType	ShareholderID;

		///产品代码
		TQCVDProductIDType	ProductID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///资金账户代码
		TQCVDCurrencyIDType	CurrencyID;

		///用户代码
		TQCVDUserIDType	UserID;
	};

	/// 查询报单
	struct CQCVDQryOrderField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///系统报单编号
		TQCVDOrderSysIDType	OrderSysID;

		///Insert Time
		TQCVDTimeType	InsertTimeStart;

		///Insert Time
		TQCVDTimeType	InsertTimeEnd;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///长字符串附加信息
		TQCVDBigsInfoType	BInfo;

		///短字符串附加信息(现已停用)
		TQCVDShortsInfoType	SInfo;

		///整形附加信息
		TQCVDIntInfoType	IInfo;

		///是否可撤
		TQCVDBoolType	IsCancel;

		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;

		///字符串附加信息
		TQCVDStrInfoType	StrInfo;

	};

	/// 查询撤单
	struct CQCVDQryOrderActionField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///长字符串附加信息
		TQCVDBigsInfoType	BInfo;

		///短字符串附加信息(现已停用)
		TQCVDShortsInfoType	SInfo;

		///整形附加信息
		TQCVDIntInfoType	IInfo;

		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;

		///字符串附加信息
		TQCVDStrInfoType	StrInfo;

	};

	/// 查询成交
	struct CQCVDQryTradeField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///成交编号
		TQCVDTradeIDType	TradeID;

		///Insert Time
		TQCVDTimeType	TradeTimeStart;

		///Insert Time
		TQCVDTimeType	TradeTimeEnd;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 查询资金账户
	struct CQCVDQryTradingAccountField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///币种
		TQCVDCurrencyIDCharType	CurrencyID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///资金账户类型
		TQCVDAccountTypeType	AccountType;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;
	};

	/// 资金账户
	struct CQCVDTradingAccountField
	{
		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///可用金额
		TQCVDMoneyType	UsefulMoney;
		///可取额度
		TQCVDMoneyType	FetchLimit;
		///币种
		TQCVDCurrencyIDCharType	CurrencyID;
		///入金金额
		TQCVDMoneyType	Deposit;

		///出金金额
		TQCVDMoneyType	Withdraw;

		///可用未交收金额(港股通专用字段)
		TQCVDMoneyType	UnDeliveredMoney;

		///冻结资金
		TQCVDMoneyType	FrozenCash;

		///冻结手续费
		TQCVDMoneyType	FrozenCommission;

		///上日未交收金额(港股通专用字段)
		TQCVDMoneyType	PreUnDeliveredMoney;

		///手续费
		TQCVDMoneyType	Commission;

		///资金账户类型
		TQCVDAccountTypeType	AccountType;
		///资金账户所属投资者代码
		TQCVDInvestorIDType	AccountOwner;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///银行代码
		TQCVDBankIDType	BankID;

		///签约银行账户
		TQCVDBankAccountIDType	BankAccountID;

		///冻结未交收金额(港股通专用)
		TQCVDMoneyType	UnDeliveredFrozenCash;

		///冻结未交收手续费(港股通专用)
		TQCVDMoneyType	UnDeliveredFrozenCommission;

		///占用未交收手续费(港股通专用)
		TQCVDMoneyType	UnDeliveredCommission;

		///核心编号
		TQCVDSequenceNoType	ServerID;

		///上日结存
		TQCVDMoneyType	PreDeposit;

		///交易日
		TQCVDDateType	TradingDay;
	};

	/// 查询投资者持仓
	struct CQCVDQryPositionField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;
	};

	/// 投资者持仓
	struct CQCVDPositionField
	{
		///合约代码
		TQCVDSecurityIDType	SecurityID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///客户代码
		TQCVDShareholderIDType	ShareholderID;

		///交易日
		TQCVDDateType	TradingDay;

		///昨仓
		TQCVDVolumeType	HistoryPos;

		///昨仓冻结
		TQCVDVolumeType	HistoryPosFrozen;

		///今买卖仓
		TQCVDVolumeType	TodayBSPos;

		///今买卖仓冻结
		TQCVDVolumeType	TodayBSFrozen;

		///今日申赎持仓
		TQCVDVolumeType	TodayPRPos;

		///今日申赎持仓冻结
		TQCVDVolumeType	TodayPRFrozen;

		///持仓成本
		TQCVDMoneyType	TotalPosCost;

		///今拆分合并持仓
		TQCVDVolumeType	TodaySMPos;

		///今拆分合并持仓冻结
		TQCVDVolumeType	TodaySMPosFrozen;

		///上次余额(盘中不变)
		TQCVDVolumeType	PrePosition;

		///股份可用
		TQCVDVolumeType	AvailablePosition;

		///股份余额
		TQCVDVolumeType	CurrentPosition;

		///开仓成本
		TQCVDMoneyType	OpenPosCost;

		///证券名称
		TQCVDSecurityNameType	SecurityName;

		///核心编号
		TQCVDSequenceNoType  ServerID;

		///是否上市
		TQCVDBoolType  IsListed;

		///昨仓成本价
		TQCVDPriceType	HistoryPosPrice;

		///今手续费
		TQCVDPriceType	TodayCommission;

		///最新价
		TQCVDPriceType	LastPrice;

		///上日冻结(盘中不变)
		TQCVDVolumeType	PreFrozen;

		///当日盈亏
		TQCVDMoneyType	CurrentProfit;

		///当日累计开仓数量
		TQCVDVolumeType	TodayTotalOpenVolume;

		///当日累计买入金额
		TQCVDMoneyType	TodayTotalBuyAmount;

		///当日累计卖出金额
		TQCVDMoneyType	TodayTotalSellAmount;

	};

	/// 查询基础交易费用
	struct CQCVDQryTradingFeeField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
	};

	/// 基础交易费用
	struct CQCVDTradingFeeField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///产品代码
		TQCVDProductIDType	ProductID;

		///证券类别代码
		TQCVDSecurityTypeType	SecurityType;

		///合约代码
		TQCVDSecurityIDType	SecurityID;

		///业务类别
		TQCVDBizClassType	BizClass;

		///印花税(港股印花税)按金额收取比例
		TQCVDRatioType	StampTaxRatioByAmt;

		///印花税(港股印花税)按面值收取比例
		TQCVDRatioType	StampTaxRatioByPar;

		///印花税(港股印花税,单位港币)按笔收取金额
		TQCVDMoneyType	StampTaxFeePerOrder;

		///印花税(港股印花税,单位港币)最低收取金额
		TQCVDMoneyType	StampTaxFeeMin;

		///印花税(港股印花税,单位港币)最高收取金额
		TQCVDMoneyType	StampTaxFeeMax;

		///过户费(港股证券组合费)按金额收取比例
		TQCVDRatioType	TransferRatioByAmt;

		///过户费(港股证券组合费)按面值收取比例
		TQCVDRatioType	TransferRatioByPar;

		///过户费(港股证券组合费,单位港币)按笔收取金额
		TQCVDMoneyType	TransferFeePerOrder;

		///过户费(港股证券组合费,单位港币)最低收取金额
		TQCVDMoneyType	TransferFeeMin;

		///过户费(港股证券组合费,单位港币)最高收取金额
		TQCVDMoneyType	TransferFeeMax;

		///经手费(港股交易费)按金额收取比例
		TQCVDRatioType	HandlingRatioByAmt;

		///经手费(港股交易费)按面值收取比例
		TQCVDRatioType	HandlingRatioByPar;

		///经手费(港股交易费,单位港币)按笔收取金额
		TQCVDMoneyType	HandlingFeePerOrder;

		///经手费(港股交易费,单位港币)最低收取金额
		TQCVDMoneyType	HandlingFeeMin;

		///经手费(港股交易费,单位港币)最高收取金额
		TQCVDMoneyType	HandlingFeeMax;

		///证管费(港股交易征费)按金额收取比例
		TQCVDRatioType	RegulateRatioByAmt;

		///证管费(港股交易征费)按面值收取比例
		TQCVDRatioType	RegulateRatioByPar;

		///证管费(港股交易征费,单位港币)按笔收取金额
		TQCVDMoneyType	RegulateFeePerOrder;

		///证管费(港股交易征费,单位港币)最低收取金额
		TQCVDMoneyType	RegulateFeeMin;

		///证管费(港股交易征费,单位港币)最高收取金额
		TQCVDMoneyType	RegulateFeeMax;

		///过户费(港股证券组合费,单位港币)按数量收取金额
		TQCVDMoneyType	TransferFeeByVolume;

		///经手费(港股交易费,单位港币)按数量收取金额
		TQCVDMoneyType	HandlingFeeByVolume;

		///结算费(港股股份交收费)按金额收取比例
		TQCVDRatioType	SettlementRatioByAmt;

		///结算费(港股股份交收费)按面值收取比例
		TQCVDRatioType	SettlementRatioByPar;

		///结算费(港股股份交收费,单位港币)按笔收取金额
		TQCVDMoneyType	SettlementFeePerOrder;

		///结算费(港股股份交收费,单位港币)按数量收取金额
		TQCVDMoneyType	SettlementFeeByVolume;

		///结算费(港股股份交收费,单位港币)最低收取金额
		TQCVDMoneyType	SettlementFeeMin;

		///结算费(港股股份交收费,单位港币)最高收取金额
		TQCVDMoneyType	SettlementFeeMax;

		///印花税(港股印花税,单位港币)按数量收取金额
		TQCVDMoneyType	StampTaxFeeByVolume;

		///证管费(港股交易征费,单位港币)按数量收取金额
		TQCVDMoneyType	RegulateFeeByVolume;
	};

	/// 查询佣金费率
	struct CQCVDQryInvestorTradingFeeField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;
	};

	/// 佣金费率
	struct CQCVDInvestorTradingFeeField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///产品代码
		TQCVDProductIDType	ProductID;

		///证券类别代码
		TQCVDSecurityTypeType	SecurityType;

		///合约代码
		TQCVDSecurityIDType	SecurityID;

		///业务类别
		TQCVDBizClassType	BizClass;

		///佣金类型
		TQCVDBrokerageTypeType	BrokerageType;

		///佣金按金额收取比例
		TQCVDRatioType	RatioByAmt;

		///佣金按面值收取比例
		TQCVDRatioType	RatioByPar;

		///佣金按笔收取金额
		TQCVDMoneyType	FeePerOrder;

		///佣金最低收取金额
		TQCVDMoneyType	FeeMin;

		///佣金最高收取金额
		TQCVDMoneyType	FeeMax;

		///佣金按数量收取金额
		TQCVDMoneyType	FeeByVolume;

		///经纪公司部门代码
		TQCVDDepartmentIDType	DepartmentID;

		///报单类型
		TQCVDOrderTypeType	OrderType;
	};

	/// 查询新股申购额度
	struct CQCVDQryIPOQuotaField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;
	};

	/// 新股申购额度
	struct CQCVDIPOQuotaField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///客户代码
		TQCVDShareholderIDType	ShareholderID;

		///可申购额度
		TQCVDLongVolumeType	MaxVolume;

		///科创板可申购额度
		TQCVDLongVolumeType	KCMaxVolume;

		///核心编号
		TQCVDSequenceNoType  ServerID;
	};

	/// 查询市场
	struct CQCVDQryMarketField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;
	};

	/// 市场
	struct CQCVDMarketField
	{
		///市场代码
		TQCVDMarketIDType	MarketID;

		///市场名称
		TQCVDNameType	MarketName;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场状态
		TQCVDMarketStatusType	MarketStatus;
	};

	/// 查询报单明细资金
	struct CQCVDQryOrderFundDetailField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///系统报单编号
		TQCVDOrderSysIDType	OrderSysID;

		///Insert Time
		TQCVDTimeType	InsertTimeStart;

		///Insert Time
		TQCVDTimeType	InsertTimeEnd;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;
	};

	/// 报单明细资金
	struct CQCVDOrderFundDetailField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///合约代码
		TQCVDSecurityIDType	SecurityID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///交易日
		TQCVDDateType	TradingDay;

		///系统报单编号
		TQCVDOrderSysIDType	OrderSysID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///初始冻结金额
		TQCVDMoneyType	TotalFrozen;

		///总费用
		TQCVDMoneyType	TotalFee;

		///印花税(港股为印花税)
		TQCVDMoneyType	StampTaxFee;

		///经手费(港股为交易费)
		TQCVDMoneyType	HandlingFee;

		///过户费(港股为证券组合费)
		TQCVDMoneyType	TransferFee;

		///证管费(港股为交易征费)
		TQCVDMoneyType	RegulateFee;

		///佣金
		TQCVDMoneyType	BrokerageFee;

		///结算费(港股为股份交收费)
		TQCVDMoneyType	SettlementFee;

		///初始冻结费用合计
		TQCVDMoneyType	TotalFeeFrozen;

		///报单初始冻结金额
		TQCVDMoneyType	OrderAmount;
	};

	/// 查询资金转移流水
	struct CQCVDQryFundTransferDetailField
	{
		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///币种
		TQCVDCurrencyIDCharType	CurrencyID;

		///转移方向
		TQCVDTransferDirectionType	TransferDirection;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;
	};

	/// 资金转移流水
	struct CQCVDFundTransferDetailField
	{
		///转账流水号
		TQCVDIntSerialType	FundSerial;

		///申请流水号
		TQCVDIntSerialType	ApplySerial;

		///前置编号
		TQCVDFrontIDType	FrontID;

		///会话编号
		TQCVDSessionIDType	SessionID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///币种
		TQCVDCurrencyIDCharType	CurrencyID;

		///转移方向
		TQCVDTransferDirectionType	TransferDirection;

		///出入金金额
		TQCVDMoneyType	Amount;

		///转移状态
		TQCVDTransferStatusType	TransferStatus;

		///操作来源
		TQCVDOperateSourceType	OperateSource;

		///操作人员
		TQCVDUserIDType	OperatorID;

		///操作日期
		TQCVDDateType	OperateDate;

		///操作时间
		TQCVDTimeType	OperateTime;

		///状态信息
		TQCVDErrorMsgType	StatusMsg;

		///经纪公司部门代码
		TQCVDDepartmentIDType	DepartmentID;

		///银行代码
		TQCVDBankIDType	BankID;

		///签约银行账户
		TQCVDBankAccountIDType	BankAccountID;

		///IP地址
		TQCVDIPAddressType	IPAddress;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///外部系统节点号
		TQCVDNodeIDType	ExternalNodeID;

		///核心编号
		TQCVDSequenceNoType  ServerID;
	};

	/// 查询持仓转移流水
	struct CQCVDQryPositionTransferDetailField
	{
		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///转移方向
		TQCVDTransferDirectionType	TransferDirection;
	};

	/// 持仓转移流水
	struct CQCVDPositionTransferDetailField
	{
		///流水号
		TQCVDIntSerialType	PositionSerial;

		///申请流水号
		TQCVDIntSerialType	ApplySerial;

		///前置编号
		TQCVDFrontIDType	FrontID;

		///会话编号
		TQCVDSessionIDType	SessionID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///交易账户代码
		TQCVDShareholderIDType	ShareholderID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///交易日期
		TQCVDDateType	TradingDay;

		///转移方向
		TQCVDTransferDirectionType	TransferDirection;

		///转移持仓类型
		TQCVDTransferPositionTypeType	TransferPositionType;

		///转移状态
		TQCVDTransferStatusType	TransferStatus;

		///昨日仓位数量
		TQCVDVolumeType	HistoryVolume;

		///今日买卖仓位数量
		TQCVDVolumeType	TodayBSVolume;

		///今日申赎仓位数量
		TQCVDVolumeType	TodayPRVolume;

		///操作人员
		TQCVDUserIDType	OperatorID;

		///操作日期
		TQCVDDateType	OperateDate;

		///操作时间
		TQCVDTimeType	OperateTime;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///状态信息
		TQCVDErrorMsgType	StatusMsg;

		///IP地址
		TQCVDIPAddressType	IPAddress;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///外部节点编号
		TQCVDNodeIDType	ExternalNodeID;

		///核心编号
		TQCVDSequenceNoType  ServerID;

		///操作来源
		TQCVDOperateSourceType	OperateSource;

		///转移持仓成本
		TQCVDMoneyType	TransTotalCost;

		///今日拆分合并数量
		TQCVDVolumeType	TodaySMVolume;

	};

	/// 查询投资者质押持仓
	struct CQCVDQryPledgePositionField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;
	};

	/// 投资者质押持仓
	struct CQCVDPledgePositionField
	{
		///合约代码
		TQCVDSecurityIDType	SecurityID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///客户代码
		TQCVDShareholderIDType	ShareholderID;

		///交易日
		TQCVDDateType	TradingDay;

		///昨日质押持仓
		TQCVDVolumeType	HisPledgePos;

		///昨日质押持仓冻结
		TQCVDVolumeType	HisPledgePosFrozen;

		///今日入库的质押持仓
		TQCVDVolumeType	TodayPledgePos;

		///今日入库的质押持仓冻结
		TQCVDVolumeType	TodayPledgePosFrozen;

		///昨日质押入库的现券总量
		TQCVDVolumeType	PreTotalPledgePos;

		///昨日质押入库的现券可用数量
		TQCVDVolumeType	preAvailablePledgePos;
	};

	/// 查询证券质押信息
	struct CQCVDQryPledgeInfoField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;
	};

	/// 证券质押信息
	struct CQCVDPledgeInfoField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///质押申报代码
		TQCVDSecurityIDType	PledgeOrderID;

		///标准券代码
		TQCVDSecurityIDType	StandardBondID;

		///是否可入库
		TQCVDBoolType	AllowPledgeIn;

		///是否可出库
		TQCVDBoolType	AllowPledgeOut;

		///标准券折算率/折算值
		TQCVDRatioType	ConversionRate;

		///每次可以入库的最小交易单位
		TQCVDTradingUnitType	PledgeInTradingUnit;

		///每次可以出库的最小交易单位
		TQCVDTradingUnitType	PledgeOutTradingUnit;

		///证券可以入库的最大数量
		TQCVDVolumeType	PledgeInVolMax;

		///证券可以入库的最小数量
		TQCVDVolumeType	PledgeInVolMin;

		///证券可以出库的最大数量
		TQCVDVolumeType	PledgeOutVolMax;

		///证券可以出库的最小数量
		TQCVDVolumeType	PledgeOutVolMin;

		///当日质押入库的质押券是否能出库
		TQCVDBoolType	IsTodayToPlegeOut;

		///是否可撤单
		TQCVDBoolType	IsCancelOrder;

		///质押名称
		TQCVDSecurityNameType	PledgeName;
	};

	/// 查询债券转股信息
	struct CQCVDQryConversionBondInfoField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;
	};

	/// 债券转股信息
	struct CQCVDConversionBondInfoField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///转股申报代码
		TQCVDSecurityIDType	ConvertOrderID;

		///转股价格
		TQCVDPriceType	ConvertPrice;

		///每次可以转股最小交易单位
		TQCVDTradingUnitType	ConvertVolUnit;

		///证券可以转股的最大数量
		TQCVDVolumeType	ConvertVolMax;

		///证券可以转股的最小数量
		TQCVDVolumeType	ConvertVolMin;

		///转股开始日期
		TQCVDDateType	BeginDate;

		///转股截至日期
		TQCVDDateType	EndDate;

		///是否可撤单
		TQCVDBoolType	IsSupportCancel;

		///转股名称
		TQCVDSecurityNameType	ConvertName;

		///是否处于转股期
		TQCVDBoolType	IsSupportConvert;
	};

	/// 查询债券回售信息
	struct CQCVDQryBondPutbackInfoField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;
	};

	/// 债券回售信息
	struct CQCVDBondPutbackInfoField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///回售申报代码
		TQCVDSecurityIDType	PutbackOrderID;

		///回售价格
		TQCVDPriceType	PutbackPrice;

		///每次可以回售最小交易单位
		TQCVDTradingUnitType	PutbackVolUnit;

		///债券可以回售的最大数量
		TQCVDVolumeType	PutbackVolMax;

		///债券可以回售的最小数量
		TQCVDVolumeType	PutbackVolMin;

		///回售开始日期
		TQCVDDateType	BeginDate;

		///回售截至日期
		TQCVDDateType	EndDate;

		///是否可撤单
		TQCVDBoolType	IsSupportCancel;

		///回售名称
		TQCVDSecurityNameType	PutbackName;

		///是否处于回售期
		TQCVDBoolType	IsSupportPutback;

		///是否处于撤销期
		TQCVDBoolType	IsSupportDelieve;
	};

	/// 查询投资者标准券额度
	struct CQCVDQryStandardBondPositionField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;
	};

	/// 投资者标准券额度
	struct CQCVDStandardBondPositionField
	{
		///合约代码
		TQCVDSecurityIDType	SecurityID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///客户代码
		TQCVDShareholderIDType	ShareholderID;

		///交易日
		TQCVDDateType	TradingDay;

		///标准券可用额度
		TQCVDPositionVolumeType	AvailablePosition;

		///标准券可用额度冻结
		TQCVDPositionVolumeType	AvailablePosFrozen;

		///标准券额度总量
		TQCVDPositionVolumeType	TotalPosition;
	};

	/// 查询指定交易登记&撤销报单
	struct CQCVDQryDesignationRegistrationField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///系统报单编号
		TQCVDOrderSysIDType	OrderSysID;

		///Insert Time
		TQCVDTimeType	InsertTimeStart;

		///Insert Time
		TQCVDTimeType	InsertTimeEnd;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;
	};

	/// 指定交易登记&撤销报单
	struct CQCVDDesignationRegistrationField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///用户代码
		TQCVDUserIDType	UserID;

		///买卖方向
		TQCVDDesignationTypeType	DesignationType;

		///本地报单编号
		TQCVDOrderLocalIDType	OrderLocalID;

		///客户代码
		TQCVDShareholderIDType	ShareholderID;

		///交易单元代码
		TQCVDPbuIDType	PbuID;

		///报单提交状态
		TQCVDOrderSubmitStatusType	OrderSubmitStatus;

		///交易日
		TQCVDDateType	TradingDay;

		///报单编号
		TQCVDOrderSysIDType	OrderSysID;

		///报单状态
		TQCVDOrderStatusType	OrderStatus;

		///报单日期
		TQCVDDateType	InsertDate;

		///委托时间
		TQCVDTimeType	InsertTime;

		///状态信息
		TQCVDErrorMsgType	StatusMsg;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///币种
		TQCVDCurrencyIDType	CurrencyID;

		///经纪公司部门代码
		TQCVDDepartmentIDType	DepartmentID;

		///IP地址
		TQCVDIPAddressType	IPAddress;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///硬盘序列号
		TQCVDHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDMobileType	Mobile;
	};

	/// 深交所转托管报单表
	struct CQCVDQryCustodyTransferField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///系统报单编号
		TQCVDOrderSysIDType	OrderSysID;

		///Insert Time
		TQCVDTimeType	InsertTimeStart;

		///Insert Time
		TQCVDTimeType	InsertTimeEnd;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;
	};

	/// 所转托管报单表
	struct CQCVDCustodyTransferField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///用户代码
		TQCVDUserIDType	UserID;

		///转托管类型
		TQCVDCustodyTransferTypeType	CustodyTransferType;

		///本地报单编号
		TQCVDOrderLocalIDType	OrderLocalID;

		///客户代码
		TQCVDShareholderIDType	ShareholderID;

		///交易单元代码
		TQCVDPbuIDType	PbuID;

		///报单提交状态
		TQCVDOrderSubmitStatusType	OrderSubmitStatus;

		///交易日
		TQCVDDateType	TradingDay;

		///报单编号
		TQCVDOrderSysIDType	OrderSysID;

		///报单状态
		TQCVDOrderStatusType	OrderStatus;

		///报单日期
		TQCVDDateType	InsertDate;

		///委托时间
		TQCVDTimeType	InsertTime;

		///状态信息
		TQCVDErrorMsgType	StatusMsg;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///币种
		TQCVDCurrencyIDType	CurrencyID;

		///经纪公司部门代码
		TQCVDDepartmentIDType	DepartmentID;

		///转入交易单元代码
		TQCVDPbuIDType	TransfereePbuID;

		///合约代码
		TQCVDSecurityIDType	SecurityID;

		///本地撤单编号,被主动撤单的转托管的原始编号
		TQCVDOrderLocalIDType	OrignalOrderLocalID;

		///报单数量
		TQCVDVolumeType	VolumeTotalOriginal;

		///撤销时间
		TQCVDTimeType	CancelTime;

		///撤销交易单元代码
		TQCVDPbuIDType	ActiveTraderID;

		///撤销操作员
		TQCVDUserIDType	ActiveUserID;

		///IP地址
		TQCVDIPAddressType	IPAddress;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///硬盘序列号
		TQCVDHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDMobileType	Mobile;
	};

	/// 查询未到期债券质押回购委托
	struct CQCVDQryPrematurityRepoOrderField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///本地报单编号
		TQCVDOrderLocalIDType	OrderLocalID;

		///证券品种代码
		TQCVDProductIDType	ProductID;

		///证券类别代码
		TQCVDSecurityTypeType	SecurityType;

		///买卖方向
		TQCVDDirectionType	Direction;

		///成交编号
		TQCVDTradeIDType	TradeID;
	};

	/// 未到期债券质押回购委托
	struct CQCVDPrematurityRepoOrderField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///交易账户代码
		TQCVDShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///成交日期
		TQCVDDateType	TradeDay;

		///到期日期
		TQCVDDateType	ExpireDay;

		///报单编号
		TQCVDOrderLocalIDType	OrderLocalID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///证券名称
		TQCVDSecurityNameType	SecurityName;

		///证券品种代码
		TQCVDProductIDType	ProductID;

		///证券类别代码
		TQCVDSecurityTypeType	SecurityType;

		///报单类别
		TQCVDDirectionType	Direction;

		///成交数量
		TQCVDVolumeType	VolumeTraded;

		///成交价格
		TQCVDPriceType	Price;

		///成交金额
		TQCVDMoneyType	Turnover;

		///成交编号
		TQCVDTradeIDType	TradeID;

		///购回应收金额
		TQCVDMoneyType	RepoTotalMoney;

		///利息金额
		TQCVDMoneyType	InterestAmount;

		///核心编号
		TQCVDSequenceNoType  ServerID;
	};

	/// 查询股东参数
	struct CQCVDQryShareholderParamField
	{
		///市场代码
		TQCVDMarketIDType	MarketID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///股东账户类型
		TQCVDClientIDTypeType	TradingCodeClass;

		///证券产品代码
		TQCVDProductIDType	ProductID;

		///证券类别代码
		TQCVDSecurityTypeType	SecurityType;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///参数类型
		TQCVDParamTypeType	ParamType;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
	};

	/// 股东参数
	struct CQCVDShareholderParamField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///交易账户代码
		TQCVDShareholderIDType	ShareholderID;

		///账户类型
		TQCVDClientIDTypeType	TradingCodeClass;

		///证券品种
		TQCVDProductIDType	ProductID;

		///证券类别
		TQCVDSecurityTypeType	SecurityType;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///股东参数类型
		TQCVDParamTypeType	ParamType;

		///股东参数值
		TQCVDParameterCharValType	ParamValue;
	};

	/// 查询外围系统仓位调拨流水
	struct CQCVDQryPeripheryPositionTransferDetailField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///仓位调拨方向
		TQCVDTransferDirectionType	TransferDirection;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;
	};

	/// 外围系统仓位调拨流水
	struct CQCVDPeripheryPositionTransferDetailField
	{
		///仓位调拨系统流水号
		TQCVDIntSerialType	PositionSerial;

		///仓位调拨请求流水号
		TQCVDIntSerialType	ApplySerial;

		///前置编号
		TQCVDFrontIDType	FrontID;

		///会话编号
		TQCVDSessionIDType	SessionID;

		///仓位调拨方向
		TQCVDTransferDirectionType	TransferDirection;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///现货系统投资者代码
		TQCVDInvestorIDType	InvestorID;

		///现货系统投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///现货系统交易账户代码
		TQCVDShareholderIDType	ShareholderID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///今日买卖仓位转入转出数量
		TQCVDVolumeType	TodayBSPos;

		///今日申赎仓位转入数量
		TQCVDVolumeType	TodayPRPos;

		///昨日仓位转入数量
		TQCVDVolumeType	HistoryPos;

		///交易日期
		TQCVDDateType	TradingDay;

		///仓位调拨原因
		TQCVDTransferReasonType	TransferReason;

		///转移状态
		TQCVDTransferStatusType	TransferStatus;

		///操作日期
		TQCVDDateType	OperateDate;

		///操作时间
		TQCVDTimeType	OperateTime;

		///冲正日期
		TQCVDDateType	RepealDate;

		///冲正时间
		TQCVDTimeType	RepealTime;

		///冲正原因
		TQCVDTransferReasonType	RepealReason;

		///状态信息
		TQCVDErrorMsgType	StatusMsg;

		///核心编号
		TQCVDSequenceNoType  ServerID;

		///今日拆分合并仓位转入数量
		TQCVDVolumeType	TodaySMPos;

		///转移持仓成本
		TQCVDMoneyType	TransTotalCost;

	};

	/// 查询投资者条件单限制参数
	struct CQCVDQryInvestorCondOrderLimitParamField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
	};

	/// 投资者条件单限制参数
	struct CQCVDInvestorCondOrderLimitParamField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///最大条件单数
		TQCVDVolumeType	MaxCondOrderLimitCnt;

		///当前条件单数
		TQCVDVolumeType	CurrCondOrderCnt;
	};

	/// 查询条件单
	struct CQCVDQryCondOrderField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///股东账户账户代码
		TQCVDShareholderIDType	ShareholderID;

		///报单编号
		TQCVDCondOrderIDType	CondOrderID;

		///Insert Time
		TQCVDTimeType	InsertTimeStart;

		///Insert Time
		TQCVDTimeType	InsertTimeEnd;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///长字符串附加信息
		TQCVDBigsInfoType	BInfo;

		///短字符串附加信息(现已停用)
		TQCVDShortsInfoType	SInfo;

		///整形附加信息
		TQCVDIntInfoType	IInfo;

		///字符串附加信息
		TQCVDStrInfoType	StrInfo;

	};

	/// 条件单
	struct CQCVDCondOrderField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///合约代码
		TQCVDSecurityIDType	SecurityID;

		///买卖方向
		TQCVDDirectionType	Direction;

		///价格类型
		TQCVDOrderPriceTypeType	OrderPriceType;

		///触发基准数量类型
		TQCVDTriggerOrderVolumeTypeType	TriggerOrderVolumeType;

		///有效期类型
		TQCVDTimeConditionType	TimeCondition;

		///成交量类型
		TQCVDVolumeConditionType	VolumeCondition;

		///报单价格
		TQCVDPriceType	LimitPrice;

		///报单数量
		TQCVDVolumeType	VolumeTotalOriginal;

		///条件报单引用
		TQCVDIntOrderRefType	CondOrderRef;

		///用户代码
		TQCVDUserIDType	UserID;

		///请求编号
		TQCVDRequestIDType	RequestID;

		///IP地址
		TQCVDIPAddressType	IPAddress;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///条件报单编号
		TQCVDCondOrderIDType	CondOrderID;

		///短字符串附加信息
		TQCVDShortsInfoType	SInfo;

		///整形附加信息
		TQCVDIntInfoType	IInfo;

		///委托方式
		TQCVDOperwayType	Operway;

		///条件检查
		TQCVDCondCheckType	CondCheck;

		///触发条件
		TQCVDContingentConditionType	ContingentCondition;

		///条件价
		TQCVDPriceType	ConditionPrice;

		///价格浮动tick数
		TQCVDVolumeType	PriceTicks;

		///数量浮动倍数
		TQCVDVolumeMultipleType	VolumeMultiple;

		///相关前置编号
		TQCVDFrontIDType	RelativeFrontID;

		///相关会话编号
		TQCVDSessionIDType	RelativeSessionID;

		///相关条件参数
		TQCVDRelativeCondParamType	RelativeParam;

		///附加触发条件
		TQCVDContingentConditionType	AppendContingentCondition;

		///附加条件价
		TQCVDPriceType	AppendConditionPrice;

		///附加相关前置编号
		TQCVDFrontIDType	AppendRelativeFrontID;

		///附加相关会话编号
		TQCVDSessionIDType	AppendRelativeSessionID;

		///附加相关条件参数
		TQCVDRelativeCondParamType	AppendRelativeParam;

		///交易日
		TQCVDDateType	TradingDay;

		///条件单状态
		TQCVDCondOrderStatusType	CondOrderStatus;

		///报单日期
		TQCVDDateType	InsertDate;

		///委托时间
		TQCVDTimeType	InsertTime;

		///撤销时间
		TQCVDTimeType	CancelTime;

		///撤销用户
		TQCVDUserIDType	CancelUser;

		///前置编号
		TQCVDFrontIDType	FrontID;

		///会话编号
		TQCVDSessionIDType	SessionID;

		///用户端产品信息
		TQCVDProductInfoType	UserProductInfo;

		///状态信息
		TQCVDErrorMsgType	StatusMsg;

		///经纪公司部门代码
		TQCVDDepartmentIDType	DepartmentID;

		///触发日期
		TQCVDDateType	ActiveDate;

		///触发时间
		TQCVDTimeType	ActiveTime;

		///港股通订单数量类型
		TQCVDLotTypeType	LotType;

		///触发基准价类型
		TQCVDTriggerOrderPriceTypeType	TriggerOrderPriceType;

		///当前相关条件参数
		TQCVDRelativeCondParamType	TriggerRelativeParam;

		///附加条件信息
		TQCVDRelativeCondParamType	AppendCondParam;

		///有效日期
		TQCVDDateType	GTDate;

		///核心编号
		TQCVDSequenceNoType  ServerID;

		///字符串附加信息
		TQCVDStrInfoType	StrInfo;

	};

	/// 查询条件单撤单
	struct CQCVDQryCondOrderActionField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///长字符串附加信息
		TQCVDBigsInfoType	BInfo;

		///短字符串附加信息(现已停用)
		TQCVDShortsInfoType	SInfo;

		///整形附加信息
		TQCVDIntInfoType	IInfo;

		///字符串附加信息
		TQCVDStrInfoType	StrInfo;

	};

	/// 条件单撤单
	struct CQCVDCondOrderActionField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///请求编号
		TQCVDRequestIDType	RequestID;

		///条件单操作引用
		TQCVDIntOrderRefType	CondOrderActionRef;

		///条件单引用
		TQCVDIntOrderRefType	CondOrderRef;

		///前置编号
		TQCVDFrontIDType	FrontID;

		///会话编号
		TQCVDSessionIDType	SessionID;

		///条件单编号
		TQCVDCondOrderIDType	CondOrderID;

		///操作标志
		TQCVDActionFlagType	ActionFlag;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///用户代码
		TQCVDUserIDType	UserID;

		///条件单撤单编号
		TQCVDCondOrderIDType	CancelCondOrderID;

		///IP地址
		TQCVDIPAddressType	IPAddress;

		///MAC地址
		TQCVDMacAddressType	MacAddress;

		///短字符串附加信息
		TQCVDShortsInfoType	SInfo;

		///整形附加信息
		TQCVDIntInfoType	IInfo;

		///委托方式
		TQCVDOperwayType	Operway;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///操作日期
		TQCVDDateType	ActionDate;

		///操作时间
		TQCVDTimeType	ActionTime;

		///核心编号
		TQCVDSequenceNoType  ServerID;

		///撤单前置编号
		TQCVDFrontIDType	ActionFrontID;

		///撤单会话编号
		TQCVDSessionIDType	ActionSessionID;

		///经纪公司部门代码
		TQCVDDepartmentIDType	DepartmentID;

		///字符串附加信息
		TQCVDStrInfoType	StrInfo;

	};

	/// 查询BrokerUserRole
	struct CQCVDQryBrokerUserRoleField
	{
		///角色编号
		TQCVDRoleIDType	RoleID;
	};

	/// BrokerUserRole
	struct CQCVDBrokerUserRoleField
	{
		///角色编号
		TQCVDRoleIDType	RoleID;

		///角色描述
		TQCVDRoleDescriptionType	RoleDescription;

		///功能权限集合
		TQCVDFunctionsType	Functions;
	};

	/// 查询BrokerUserRoleAssignment
	struct CQCVDQryBrokerUserRoleAssignmentField
	{
		///用户代码
		TQCVDUserIDType	UserID;
	};

	/// BrokerUserRoleAssignment
	struct CQCVDBrokerUserRoleAssignmentField
	{
		///用户代码
		TQCVDUserIDType	UserID;

		///角色编号
		TQCVDRoleIDType	RoleID;

		///角色描述
		TQCVDRoleDescriptionType	RoleDescription;
	};

	/// 查询交易通知
	struct CQCVDQryTradingNoticeField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///Insert Date
		TQCVDDateType	InsertDateStart;

		///Insert Date
		TQCVDDateType	InsertDateEnd;

		///Insert Time
		TQCVDTimeType	InsertTimeStart;

		///Insert Time
		TQCVDTimeType	InsertTimeEnd;
	};

	/// 查询新股申购配号结果
	struct CQCVDQryIPONumberResultField
	{
		///申购代码
		TQCVDSecurityIDType	SecurityID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///股东代码
		TQCVDShareholderIDType	ShareholderID;
	};

	/// 新股申购配号结果
	struct CQCVDIPONumberResultField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///日期
		TQCVDDateType	Day;

		///申购证券名称
		TQCVDSecurityNameType	SecurityName;

		///股东代码
		TQCVDShareholderIDType	ShareholderID;

		///证券类别代码
		TQCVDSecurityTypeType	SecurityType;

		///起始配号
		TQCVDIPONumberIDType	BeginNumberID;

		///配号数量
		TQCVDVolumeType	Volume;

		///核心编号
		TQCVDSequenceNoType  ServerID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;
	};

	/// 查询新股申购中签结果
	struct CQCVDQryIPOMatchNumberResultField
	{
		///申购代码
		TQCVDSecurityIDType	SecurityID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///股东代码
		TQCVDShareholderIDType	ShareholderID;

		///中签配号
		TQCVDIPONumberIDType	MatchNumberID;
	};

	/// 新股申购中签结果
	struct CQCVDIPOMatchNumberResultField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///日期
		TQCVDDateType	Day;

		///申购证券名称
		TQCVDSecurityNameType	SecurityName;

		///股东代码
		TQCVDShareholderIDType	ShareholderID;

		///证券类别代码
		TQCVDSecurityTypeType	SecurityType;

		///中签配号
		TQCVDIPONumberIDType	MatchNumberID;

		///此中签号拥有的证券数量
		TQCVDVolumeType	Volume;

		///申购价格
		TQCVDPriceType	Price;

		///申购金额
		TQCVDMoneyType	Amout;

		///核心编号
		TQCVDSequenceNoType  ServerID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;
	};

	/// 查询经纪公司部门信息
	struct CQCVDQryInnerBranchInfoField
	{
		///二级机构代码
		TQCVDBranchIDType	InnerBranchID;
	};

	/// 经纪公司部门信息
	struct CQCVDInnerBranchInfoField
	{
		///二级机构代码
		TQCVDBranchIDType	InnerBranchID;

		///二级机构名称
		TQCVDNameType	InnerBranchName;
	};

	/// 查询机构组织关系基础信息
	struct CQCVDQryDepartmentBranchInfoField
	{
		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///二级机构代码
		TQCVDBranchIDType	InnerBranchID;
	};

	/// 机构组织关系基础信息
	struct CQCVDDepartmentBranchInfoField
	{
		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///一级机构名称
		TQCVDNameType	DepartmentName;

		///二级机构名称
		TQCVDBranchIDType	InnerBranchID;

		///二级机构名称
		TQCVDNameType	InnerBranchName;
	};

	/// 查询机构组织关系基础信息
	struct CQCVDQryDepartmentManageInfoField
	{
		///一级管理机构代码
		TQCVDDepartmentIDType	ManageDepartmentID;

		///二级机构代码
		TQCVDBranchIDType	InnerBranchID;
	};

	/// 机构组织关系基础信息
	struct CQCVDDepartmentManageInfoField
	{
		///一级管理机构代码
		TQCVDDepartmentIDType	ManageDepartmentID;

		///一级管理机构名称
		TQCVDNameType	DepartmentName;

		///二级机构名称
		TQCVDBranchIDType	InnerBranchID;

		///二级机构名称
		TQCVDNameType	InnerBranchName;
	};

	/// 查询BrokerUser
	struct CQCVDQryBrokerUserField
	{
		///用户代码
		TQCVDUserIDType	UserID;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;
	};

	/// BrokerUser
	struct CQCVDBrokerUserField
	{
		///用户代码
		TQCVDUserIDType	UserID;

		///用户名称
		TQCVDUserNameType	UserName;

		///用户类型
		TQCVDUserTypeType	UserType;

		///状态
		TQCVDActiveStatusType	Status;

		///登录限制
		TQCVDLoginLimitType	LoginLimit;

		///密码连续输入错误限制
		TQCVDLoginLimitType	PasswordFailLimit;

		///一级机构编码
		TQCVDDepartmentIDType	DepartmentID;

		///二级机构编码
		TQCVDBranchIDType	InnerBranchID;

		///密码修改周期
		TQCVDCountType	PasswordUpdatePeriod;

		///密码有效剩余天数
		TQCVDCountType	PasswordRemainDays;

		///一级管理机构代码
		TQCVDDepartmentIDType	ManageDepartmentID;
	};

	/// 查询深港通国际市场互联状态信息
	struct CQCVDQrySZSEImcParamsField
	{
		///市场代码
		TQCVDMarketIDType	MarketID;
	};

	/// 深港通国际市场互联状态信息
	struct CQCVDSZSEImcParamsField
	{
		///市场代码
		TQCVDMarketIDType	MarketID;

		///是否开放
		TQCVDBoolType	OpenFlag;

		///初始额度
		TQCVDMoneyType	ThresholdAmount;

		///日中剩余额度
		TQCVDMoneyType	PosAmt;

		///额度是否可用
		TQCVDBoolType	AmountStatus;
	};

	/// 查询深港通国际市场互联汇率信息
	struct CQCVDQrySZSEImcExchangeRateField
	{
		///源货币币种
		TQCVDCurrencyIDType	FromCurrency;

		///目标货币币种
		TQCVDCurrencyIDType	ToCurrency;
	};

	/// 深港通国际市场互联汇率信息
	struct CQCVDSZSEImcExchangeRateField
	{
		///源货币币种
		TQCVDCurrencyIDType	FromCurrency;

		///目标货币币种
		TQCVDCurrencyIDType	ToCurrency;

		///参考汇率买入价
		TQCVDPriceType	BidRate;

		///参考汇率卖出价
		TQCVDPriceType	OfferRate;

		///参考汇率中间价
		TQCVDPriceType	MidPointRate;
	};

	/// 查询深港通最小价差信息
	struct CQCVDQrySZSEHKPriceTickInfoField
	{
		///价差品种
		TQCVDPriceTickIDType	PriceTickID;
	};

	/// 深港通最小价差信息
	struct CQCVDSZSEHKPriceTickInfoField
	{
		///价差品种
		TQCVDPriceTickIDType	PriceTickID;

		///价差组号
		TQCVDPriceTickGroupIDType	PriceTickGroupID;

		///价差类别
		TQCVDPriceTickTypeType	PriceTickType;

		///价差组起始价格
		TQCVDPriceType	BeginPrice;

		///价差组结束价格
		TQCVDPriceType	EndPrice;

		///价差值
		TQCVDPriceType	PriceTick;
	};

	/// 查询交易协议
	struct CQCVDQryShareholderSpecPrivilegeField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;
	};

	/// 交易协议
	struct CQCVDShareholderSpecPrivilegeField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///交易协议类别
		TQCVDSpecPrivilegeTypeType	SpecPrivilegeType;

		///买卖方向
		TQCVDDirectionType	Direction;

		///是否禁止
		TQCVDBoolType	bForbidden;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///核心编号
		TQCVDSequenceNoType  ServerID;
	};

	/// 查询投资者限仓信息
	struct CQCVDQryInvestorPositionLimitField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;
	};

	/// 投资者限仓信息
	struct CQCVDInvestorPositionLimitField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///当日买入额度
		TQCVDVolumeType	BuyLimit;

		///已使用当日买入额度
		TQCVDVolumeType	BuyFrozen;

		///当日卖出额度
		TQCVDVolumeType	SellLimit;

		///已使用当日卖出额度
		TQCVDVolumeType	SellFrozen;

		///当日申购额度
		TQCVDVolumeType	PurchaseLimit;

		///已使用当日申购额度
		TQCVDVolumeType	PurchaseFrozen;

		///当日赎回额度
		TQCVDVolumeType	RedeemLimit;

		///已使用当日赎回额度
		TQCVDVolumeType	RedeemFrozen;

		///当日质押入库额度
		TQCVDVolumeType	PledgeInLimit;

		///已使用当日质押入库额度
		TQCVDVolumeType	PledgeInFrozen;

		///当日质押出库额度
		TQCVDVolumeType	PledgeOutLimit;

		///已使用当日质押出库额度
		TQCVDVolumeType	PledgeOutFrozen;

		///当日债转股额度
		TQCVDVolumeType	ConvertLimit;

		///已使用当日债转股额度
		TQCVDVolumeType	ConvertFrozen;

		///当日债券回售额度
		TQCVDVolumeType	PutbackLimit;

		///已使用当日债券回售额度
		TQCVDVolumeType	PutbackFrozen;

		///当日配股配债额度
		TQCVDVolumeType	RationalLimit;

		///已使用当日配股配债额度
		TQCVDVolumeType	RationalFrozen;

		///总持仓额度
		TQCVDVolumeType	TotalPositionLimit;

		///已使用总持仓额度
		TQCVDVolumeType	TotalPositionFrozen;

		///当日拆分额度
		TQCVDVolumeType	SplitLimit;

		///已使用当日拆分额度
		TQCVDVolumeType	SplitFrozen;

		///当日合并额度
		TQCVDVolumeType	MergeLimit;

		///已使用当日合并额度
		TQCVDVolumeType	MergeFrozen;
	};

	/// 查询盘后行情
	struct CQCVDQryPHMarketDataField
	{
		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
	};

	/// 查询股东账户证券代码系统权限
	struct CQCVDQrySecurityPriorAuthField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;
	};

	/// 股东账户证券代码系统权限
	struct CQCVDSecurityPriorAuthField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///买卖方向
		TQCVDDirectionType	Direction;

		///是否禁止
		TQCVDBoolType	bForbidden;
	};

	/// 查询配股配债信息
	struct CQCVDQryRationalInfoField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///配股配债代码
		TQCVDSecurityIDType	SecurityID;
	};

	/// 配股配债信息
	struct CQCVDRationalInfoField
	{
		///交易日
		TQCVDDateType	TradingDay;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///申购代码
		TQCVDSecurityIDType	SecurityID;

		///配股配债价格
		TQCVDPriceType	Price;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///证券品种代码
		TQCVDProductIDType	ProductID;

		///证券类别代码
		TQCVDSecurityTypeType	SecurityType;

		///配股配债上限
		TQCVDVolumeType	MaxVolume;

		///配股名称
		TQCVDSecurityNameType	SecurityName;

		///基础证券代码
		TQCVDSecurityIDType	UnderlyingSecurityID;

		///基础证券名称
		TQCVDSecurityNameType	UnderlyingSecurityName;

		///配股配债最小数量
		TQCVDVolumeType	MinVolume;

		///配股配债单位数量
		TQCVDVolumeType	VolumeUnit;
	};

	/// 查询外围系统资金转移流水
	struct CQCVDQryPeripheryFundTransferDetailField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///币种
		TQCVDCurrencyIDCharType	CurrencyID;

		///资金调拨方向
		TQCVDTransferDirectionType	TransferDirection;
	};

	/// 外围系统资金转移流水
	struct CQCVDPeripheryFundTransferDetailField
	{
		///转账流水号
		TQCVDIntSerialType	FundSerial;

		///申请流水号
		TQCVDIntSerialType	ApplySerial;

		///前置编号
		TQCVDFrontIDType	FrontID;

		///会话编号
		TQCVDSessionIDType	SessionID;

		///经纪公司部门代码
		TQCVDDepartmentIDType	DepartmentID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///币种
		TQCVDCurrencyIDCharType	CurrencyID;

		///转移方向
		TQCVDTransferDirectionType	TransferDirection;

		///出入金金额
		TQCVDMoneyType	Amount;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///转移状态
		TQCVDTransferStatusType	TransferStatus;

		///资金调拨原因
		TQCVDTransferReasonType	TransferReason;

		///操作日期
		TQCVDDateType	OperateDate;

		///操作时间
		TQCVDTimeType	OperateTime;

		///冲正日期
		TQCVDDateType	RepealDate;

		///冲正时间
		TQCVDTimeType	RepealTime;

		///冲正原因
		TQCVDTransferReasonType	RepealReason;

		///状态信息
		TQCVDErrorMsgType	StatusMsg;

		///核心编号
		TQCVDSequenceNoType  ServerID;
	};

	/// 查询系统节点信息
	struct CQCVDQrySystemNodeInfoField
	{
		///节点编号
		TQCVDNodeIDType	NodeID;
	};

	/// 系统节点信息
	struct CQCVDSystemNodeInfoField
	{
		///节点编号
		TQCVDNodeIDType	NodeID;

		///节点信息
		TQCVDNodeInfoType	NodeInfo;

		///是否当前节点
		TQCVDBoolType	bCurrent;
	};

	/// 查询LOF基金信息
	struct CQCVDQryLofFundInfoField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///基金代码
		TQCVDSecurityIDType	FundID;

		///主基金代码
		TQCVDSecurityIDType	MainFundID;
	};

	/// LOF基金信息
	struct CQCVDLofFundInfoField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///基金代码
		TQCVDSecurityIDType	FundID;

		///主基金代码
		TQCVDSecurityIDType	MainFundID;

		///基金类型
		TQCVDFundTypeType	FundType;

		///拆分数量单位
		TQCVDVolumeType	SplitUnit;

		///最小拆分数量
		TQCVDVolumeType	SplitMinVol;

		///合并数量单位
		TQCVDVolumeType	MergeUnit;

		///最小合并数量
		TQCVDVolumeType	MergeMinVol;

		///基金转换系数
		TQCVDRatioType	FundRatio;
	};

	/// 查询投资者
	struct CQCVDCreditQryInvestorField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;
	};

	/// 查询User
	struct CQCVDCreditQryUserField
	{
		///用户代码
		TQCVDCreditUserIDType	UserID;

		///用户类型
		TQCVDCreditUserTypeType	UserType;
	};

	/// 查询股东账户
	struct CQCVDCreditQryShareholderAccountField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///存放上证所和深圳的股东代码。
		TQCVDCreditShareholderIDType	ShareholderID;

		///普通，信用，衍生品等
		TQCVDCreditShareholderIDTypeType	ShareholderIDType;
	};

	/// 查询资金账户
	struct CQCVDCreditQryTradingAccountField
	{
		///资金账户所属投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///币种
		TQCVDCreditCurrencyIDType	CurrencyID;

		///资金账户
		TQCVDCreditAccountIDType	AccountID;

		///普通、信用、衍生品等
		TQCVDCreditAccountTypeType	AccountType;

		///经纪公司部门代码
		TQCVDCreditDepartmentIDType	DepartmentID;
	};

	/// 查询报单
	struct CQCVDCreditQryOrderField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///合约在系统中的编号
		TQCVDCreditSecurityIDType	SecurityID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///客户在系统中的编号，编号唯一且遵循交易所制定的编码规则
		TQCVDCreditShareholderIDType	ShareholderID;

		///报单编号
		TQCVDCreditOrderSysIDType	OrderSysID;

		///Time
		TQCVDCreditTimeType	InsertTimeStart;

		///Time
		TQCVDCreditTimeType	InsertTimeEnd;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///符串附加信息
		TQCVDCreditStrInfoType	SInfo;

		///整形附加信息
		TQCVDCreditIntInfoType	IInfo;

		///排列方式 1-盈亏正序 2-盈亏倒序 默认倒序
		TQCVDCreditOrderSortType	OrderType;

		///每页记录数
		TQCVDCreditVolumeType	PageCount;

		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
	};

	/// 查询成交
	struct CQCVDCreditQryTradeField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///合约在系统中的编号
		TQCVDCreditSecurityIDType	SecurityID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///客户在系统中的编号，编号唯一且遵循交易所制定的编码规则
		TQCVDCreditShareholderIDType	ShareholderID;

		///成交编号
		TQCVDCreditTradeIDType	TradeID;

		///Time
		TQCVDCreditTimeType	TradeTimeStart;

		///Time
		TQCVDCreditTimeType	TradeTimeEnd;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///排列方式 1-盈亏正序 2-盈亏倒序 默认倒序
		TQCVDCreditOrderSortType	OrderType;

		///每页记录数
		TQCVDCreditVolumeType	PageCount;

		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
	};

	/// 查询投资者持仓
	struct CQCVDCreditQryPositionField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///交易账户代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;
	};

	/// 查询资金转移流水
	struct CQCVDCreditQryFundTransferDetailField
	{
		///资金账户代码
		TQCVDCreditAccountIDType	AccountID;

		///币种
		TQCVDCreditCurrencyIDType	CurrencyID;

		///转移方向
		TQCVDCreditTransferDirectionType	TransferDirection;

		///经纪公司部门代码
		TQCVDCreditDepartmentIDType	DepartmentID;
	};

	/// 查询持仓转移流水
	struct CQCVDCreditQryPositionTransferDetailField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///目前该字段存放的是上证所和深圳的股东代码。
		TQCVDCreditShareholderIDType	ShareholderID;

		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;

		///转移方向
		TQCVDCreditTransferDirectionType	TransferDirection;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;
	};

	/// 查询撤单
	struct CQCVDCreditQryOrderActionField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///存放上证所和深圳的股东代码。
		TQCVDCreditShareholderIDType	ShareholderID;

		///信用投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///交易单元代码
		TQCVDCreditPbuIDType	PbuID;

		///全系统的唯一报单编号
		TQCVDCreditOrderLocalIDType	CancelOrderLocalID;

		///本地报单编号
		TQCVDCreditOrderLocalIDType	OrderLocalID;

		///字符串附加信息
		TQCVDCreditStrInfoType	SInfo;

		///整形附加信息
		TQCVDCreditIntInfoType	IInfo;

		///排列方式 1-盈亏正序 2-盈亏倒序 默认倒序
		TQCVDCreditOrderSortType	OrderType;

		///每页记录数
		TQCVDCreditVolumeType	PageCount;

		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
	};

	/// 查询信用转移
	struct CQCVDCreditQryCreditTransferField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///目前该字段存放的是上证所和深圳的股东代码。
		TQCVDCreditShareholderIDType	ShareholderID;

		///全系统的唯一报单编号。
		TQCVDCreditOrderSysIDType	OrderSysID;

		///Time
		TQCVDCreditTimeType	InsertTimeStart;

		///Time
		TQCVDCreditTimeType	InsertTimeEnd;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///短字符串附加信息
		TQCVDCreditStrInfoType	SInfo;

		///整形附加信息
		TQCVDCreditIntInfoType	IInfo;
	};

	/// 查询撤销信用转移
	struct CQCVDCreditQryCancelCreditTransferField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///目前该字段存放的是上证所和深圳的股东代码。
		TQCVDCreditShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///交易单元代码
		TQCVDCreditPbuIDType	PbuID;

		///全系统的唯一报单编号。
		TQCVDCreditOrderLocalIDType	CancelOrderLocalID;

		///该撤单对应被撤报单的本地报单编号。
		TQCVDCreditOrderLocalIDType	OrderLocalID;

		///短字符串附加信息
		TQCVDCreditStrInfoType	SInfo;

		///整形附加信息
		TQCVDCreditIntInfoType	IInfo;
	};

	/// 查询投资者融资融券信息
	struct CQCVDCreditQryInvestorCreditInfoField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;
	};

	/// 查询投资者利率
	struct CQCVDCreditQryInvestorCreditInterestRateField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///经纪公司部门代码（00000000代表所有营业部）
		TQCVDCreditDepartmentIDType	DepartmentID;
	};

	/// 查询信用负债
	struct CQCVDCreditQryCreditDebtField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///信用负债编号
		TQCVDCreditCreditDebtIDType	CreditDebtID;

		///信用负债类型
		TQCVDCreditCreditDebtTypeType	CreditDebtType;

		///信用合约状态
		TQCVDCreditCreditDebtStatusType	CreditDebtStatus;

		///经纪公司部门代码
		TQCVDCreditDepartmentIDType	DepartmentID;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;
	};

	/// 查询条件单
	struct CQCVDCreditQryCondOrderField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///合约代码
		TQCVDCreditSecurityIDType	SecurityID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///存放上证所和深圳的股东代码。
		TQCVDCreditShareholderIDType	ShareholderID;

		///报单编号
		TQCVDCreditCondOrderIDType	CondOrderID;

		///Time
		TQCVDCreditTimeType	InsertTimeStart;

		///Time
		TQCVDCreditTimeType	InsertTimeEnd;

		///信用投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///短字符串附加信息
		TQCVDCreditStrInfoType	SInfo;

		///整形附加信息
		TQCVDCreditIntInfoType	IInfo;
	};

	/// 查询条件单撤单
	struct CQCVDCreditQryCondOrderActionField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///一个交易所的编号
		TQCVDCreditExchangeIDType	ExchangeID;

		///股东账户代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///短字符串附加信息
		TQCVDCreditStrInfoType	SInfo;

		///整形附加信息
		TQCVDCreditIntInfoType	IInfo;
	};

	/// 查询新股信息
	struct CQCVDCreditQryIPOInfoField
	{
		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///申购代码
		TQCVDCreditSecurityIDType	SecurityID;
	};

	/// 查询新股申购额度
	struct CQCVDCreditQryIPOQuotaField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///存放上证所和深圳的股东代码。
		TQCVDCreditShareholderIDType	ShareholderID;
	};

	/// 查询新股申购配号结果
	struct CQCVDCreditQryIPONumberResultField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///申购代码
		TQCVDCreditSecurityIDType	SecurityID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///股东代码
		TQCVDCreditShareholderIDType	ShareholderID;
	};

	/// 查询新股申购中签结果
	struct CQCVDCreditQryIPOMatchNumberResultField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///申购代码
		TQCVDCreditSecurityIDType	SecurityID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///股东代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///中签配号
		TQCVDCreditIPONumberIDType	MatchNumberID;
	};

	/// 查询投资者实时融资融券信息
	struct CQCVDCreditQryInvestorRealTimeCreditInfoField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;
	};

	/// 查询负债展期
	struct CQCVDCreditQryDebtExtendField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///目前该字段存放的是上证所和深圳的股东代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///展期流水号
		TQCVDCreditIntSerialType	ExtendSerial;

		///Time
		TQCVDCreditTimeType	InsertTimeStart;

		///Time
		TQCVDCreditTimeType	InsertTimeEnd;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///短字符串附加信息
		TQCVDCreditStrInfoType	SInfo;

		///整形附加信息
		TQCVDCreditIntInfoType	IInfo;
	};

	/// 查询撤销负债展期
	struct CQCVDCreditQryCancelDebtExtendField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///目前该字段存放的是上证所和深圳的股东代码。
		TQCVDCreditShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///信用负债编号
		TQCVDCreditCreditDebtIDType	CreditDebtID;

		///负债展期撤单编号
		TQCVDCreditIntSerialType	CancelExtendSerial;

		///被撤展期编号
		TQCVDCreditIntSerialType	ExtendSerial;

		///短字符串附加信息
		TQCVDCreditStrInfoType	SInfo;

		///整形附加信息
		TQCVDCreditIntInfoType	IInfo;
	};

	/// 查询外围系统资金转移流水
	struct CQCVDCreditQryPeripheryFundTransferDetailField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///一级机构代码
		TQCVDCreditDepartmentIDType	DepartmentID;

		///资金账户代码
		TQCVDCreditAccountIDType	AccountID;

		///币种
		TQCVDCreditCurrencyIDType	CurrencyID;

		///资金调拨方向
		TQCVDCreditTransferDirectionType	TransferDirection;
	};

	/// 查询交易协议
	struct CQCVDCreditQryShareholderSpecPrivilegeField
	{
		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///股东账户代码
		TQCVDCreditShareholderIDType	ShareholderID;
	};

	/// 查询配股配债信息
	struct CQCVDCreditQryRationalInfoField
	{
		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///配股配债代码
		TQCVDCreditSecurityIDType	SecurityID;
	};

	/// 查询外围系统仓位调拨流水
	struct CQCVDCreditQryPeripheryPositionTransferDetailField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///股东账户代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;

		///仓位调拨方向
		TQCVDCreditTransferDirectionType	TransferDirection;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;
	};

	/// 查询集中交易系统资金请求
	struct CQCVDCreditReqInquiryJZFundField
	{
		///资金账户代码
		TQCVDCreditAccountIDType	AccountID;

		///币种
		TQCVDCreditCurrencyIDType	CurrencyID;

		///一级机构代码
		TQCVDCreditDepartmentIDType	DepartmentID;
	};

	/// 查询集中交易系统资金响应
	struct CQCVDCreditRspInquiryJZFundField
	{
		///资金账户代码
		TQCVDCreditAccountIDType	AccountID;

		///币种
		TQCVDCreditCurrencyIDType	CurrencyID;

		///可用金额
		TQCVDCreditMoneyType	UsefulMoney;

		///可取额度
		TQCVDCreditMoneyType	FetchLimit;

		///一级机构代码
		TQCVDCreditDepartmentIDType	DepartmentID;
	};

	/// 报告投资者信息
	struct CQCVDCreditInvestorField
	{
		///交易日
		TQCVDCreditDateType	TradingDay;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///经纪公司部门代码
		TQCVDCreditDepartmentIDType	DepartmentID;

		///自然人、法人、特殊法人、资管、基金等
		TQCVDCreditInvestorTypeType	InvestorType;

		///投资者名称
		TQCVDCreditInvestorNameType	InvestorName;

		///证件类型
		TQCVDCreditIdCardTypeType	IdCardType;

		///证件号码
		TQCVDCreditIdCardNoType	IdCardNo;

		///开户日期
		TQCVDCreditDateType	OpenDate;

		///销户日期
		TQCVDCreditDateType	CloseDate;

		///交易状态
		TQCVDCreditTradingStatusType	TradingStatus;

		///委托方式
		TQCVDCreditOperwaysType	Operways;

		///手机
		TQCVDCreditMobileType	Mobile;

		///联系电话
		TQCVDCreditTelephoneType	Telephone;

		///电子邮件
		TQCVDCreditEmailType	Email;

		///传真
		TQCVDCreditFaxType	Fax;

		///通讯地址
		TQCVDCreditAddressType	Address;

		///邮政编码
		TQCVDCreditZipCodeType	ZipCode;

		///备注
		TQCVDCreditRemarkType	Remark;

		///费率模板
		TQCVDCreditTemplateIDType	FeeTemplateID;

		///保证金率模板
		TQCVDCreditTemplateIDType	MarginRateTemplateID;

		///担保品折算率模板
		TQCVDCreditTemplateIDType	ConversionRateTemplateID;

		///担保风险参数模板
		TQCVDCreditTemplateIDType	CollateralRiskParamTemplateID;

		///利率模板
		TQCVDCreditTemplateIDType	InterestRateTemplateID;

		///风险参数模板
		TQCVDCreditTemplateIDType	RiskParamTemplateID;

		///业务规模风险参数模板
		TQCVDCreditTemplateIDType	BusinessScaleConcentrationParamTemplateID;

		///反洗钱等级
		TQCVDCreditAMLLevelType	AMLLevel;

		///套餐类型
		TQCVDCreditPlanTypeType	PlanType;

		///优惠利率模板
		TQCVDCreditTemplateIDType	BenefitInterestRateTemplateID;

		///上日套餐类型
		TQCVDCreditPlanTypeType	PrePlanType;

		///第一次进入智能套餐日期
		TQCVDCreditDateType	SwitchSmartPlanDate;

		///是否允许投资者自切套餐
		TQCVDCreditBoolType	AllowSelfSwitchPlan;

		///当前交易日投资者自切套餐次数
		TQCVDCreditRecordCntType	SelfSwitchPlanCnt;

		///流动资产比例风险模板(预留)
		TQCVDCreditTemplateIDType	CirculateAssetRateTemplateID;

		///是否允许投资者做资金调拨业务
		TQCVDCreditBoolType	AllowMoveFund;

		///是否允许投资者做银证转账业务
		TQCVDCreditBoolType	AllowTransferFund;

		///是否通道用户
		TQCVDCreditBoolType	bChannel;

		///专业投资者类别
		TQCVDCreditProfInvestorTypeType	ProfInvestorType;

		///担保品买入是否检查集中度
		TQCVDCreditBoolType	CheckBuyIndicator;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	/// 报告股东账户信息
	struct CQCVDCreditShareholderAccountField
	{
		///交易日
		TQCVDCreditDateType	TradingDay;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///股东账户账户代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///股东账户类型
		TQCVDCreditShareholderIDTypeType	ShareholderIDType;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///交易单元代码
		TQCVDCreditPbuIDType	PbuID;

		///交易所营业部编码
		TQCVDCreditBranchIDType	BranchID;

		///交易权限模板
		TQCVDCreditTemplateIDType	TradingRightTemplateID;

		///债券适当性投资者类别
		TQCVDCreditQualificationTypeType	QualificationType;

		///展期权限模板
		TQCVDCreditTemplateIDType	DebtExtendRightTemplateID;

		///普通买卖白名单控制标志
		TQCVDCreditBoolType	BSWhiteListCtl;

		///是否允许投资者做仓位调拨业务
		TQCVDCreditBoolType	AllowMovePosition;

		///交易黑名单模板
		TQCVDCreditTemplateIDType	BlackListTemplateID;

		///主账户标识
		TQCVDCreditBoolType	MainFlag;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	/// 报告报单
	struct CQCVDCreditOrderField
	{
		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///股东账户代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;

		///买卖方向
		TQCVDCreditDirectionType	Direction;

		///报单价格条件
		TQCVDCreditOrderPriceTypeType	OrderPriceType;

		///有效期类型
		TQCVDCreditTimeConditionType	TimeCondition;

		///成交量类型
		TQCVDCreditVolumeConditionType	VolumeCondition;

		///价格
		TQCVDCreditPriceType	LimitPrice;

		///数量
		TQCVDCreditVolumeType	VolumeTotalOriginal;

		///港股通订单数量类型
		TQCVDCreditLotTypeType	LotType;

		///有效日期
		TQCVDCreditDateType	GTDate;

		///委托方式
		TQCVDCreditOperwayType	Operway;

		///条件检查
		TQCVDCreditCondCheckType	CondCheck;

		///字符串附加信息
		TQCVDCreditStrInfoType	SInfo;

		///整形附加信息
		TQCVDCreditIntInfoType	IInfo;

		///请求编号
		TQCVDCreditRequestIDType	RequestID;

		///前置编号
		TQCVDCreditFrontIDType	FrontID;

		///会话编号
		TQCVDCreditSessionIDType	SessionID;

		///报单引用
		TQCVDCreditOrderRefType	OrderRef;

		///本地报单编号
		TQCVDCreditOrderLocalIDType	OrderLocalID;

		///系统报单编号
		TQCVDCreditOrderSysIDType	OrderSysID;

		///报单状态
		TQCVDCreditOrderStatusType	OrderStatus;

		///报单提交状态
		TQCVDCreditOrderSubmitStatusType	OrderSubmitStatus;

		///状态信息
		TQCVDCreditErrorMsgType	StatusMsg;

		///已成交数量
		TQCVDCreditVolumeType	VolumeTraded;

		///已撤销数量
		TQCVDCreditVolumeType	VolumeCanceled;

		///交易日
		TQCVDCreditDateType	TradingDay;

		///申报用户
		TQCVDCreditUserIDType	InsertUser;

		///申报日期
		TQCVDCreditDateType	InsertDate;

		///申报时间
		TQCVDCreditTimeType	InsertTime;

		///申报时间(毫秒)
		TQCVDCreditMillisecType	InsertMillisec;

		///交易所接收时间
		TQCVDCreditTimeType	AcceptTime;

		///撤销用户
		TQCVDCreditUserIDType	CancelUser;

		///撤销时间
		TQCVDCreditTimeType	CancelTime;

		///经纪公司部门代码
		TQCVDCreditDepartmentIDType	DepartmentID;

		///资金账户代码
		TQCVDCreditAccountIDType	AccountID;

		///币种
		TQCVDCreditCurrencyIDType	CurrencyID;

		///成交金额
		TQCVDCreditMoneyType	Turnover;

		///报单类型
		TQCVDCreditOrderTypeType	OrderType;

		///用户端产品信息
		TQCVDCreditUserProductInfoType	UserProductInfo;

		///IP地址
		TQCVDCreditIPAddressType	IPAddress;

		///Mac地址
		TQCVDCreditMacAddressType	MacAddress;

		///回报附加浮点型数据信息
		TQCVDCreditFloatInfoType	RtnFloatInfo;

		///回报附加整型数据
		TQCVDCreditIntInfoType	RtnIntInfo;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///交易所营业部编码
		TQCVDCreditBranchIDType	BranchID;

		///交易单元代码
		TQCVDCreditPbuIDType	PbuID;

		///特定业务标识
		TQCVDCreditBoolType	SpecialBizFlag;

		///记录序号(仅上证报盘使用)
		TQCVDCreditSequenceNoType	RecordNumber;

		///交易所数量
		TQCVDCreditVolumeType	ExchVolume;

		///度量索引
		TQCVDCreditMeasureIndexType	MeasureIndex;

		///交易报盘编号
		TQCVDCreditTraderOfferIDType	TraderOfferID;

		///强平原因
		TQCVDCreditForceCloseReasonType	ForceCloseReason;

		///自然人、法人、特殊法人、资管、基金等
		TQCVDCreditInvestorTypeType	InvestorType;

		///信用头寸编号
		TQCVDCreditQuotaIDType	CreditQuotaID;

		///头寸类型
		TQCVDCreditCreditQuotaTypeType	CreditQuotaType;

		///信用负债编号
		TQCVDCreditCreditDebtIDType	CreditDebtID;

		///信用负债对应的交易所代码
		TQCVDCreditExchangeIDType	DebtExchangeID;

		///费息折扣券编号（0表示不使用折扣券）
		TQCVDCreditIntSerialType	DiscountCouponID;

		///节点号
		TQCVDCreditNodeIDType	ServerID;

		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
	};

	/// 报告撤单
	struct CQCVDCreditCancelOrderField
	{
		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///被撤报单前置编号
		TQCVDCreditFrontIDType	FrontID;

		///被撤报单会话编号
		TQCVDCreditSessionIDType	SessionID;

		///被撤报单引用
		TQCVDCreditOrderRefType	OrderRef;

		///被撤报单系统编号
		TQCVDCreditOrderSysIDType	OrderSysID;

		///操作标志
		TQCVDCreditActionFlagType	ActionFlag;

		///本地撤单编号
		TQCVDCreditOrderLocalIDType	CancelOrderLocalID;

		///委托方式
		TQCVDCreditOperwayType	Operway;

		///字符串附加信息
		TQCVDCreditStrInfoType	SInfo;

		///整形附加信息
		TQCVDCreditIntInfoType	IInfo;

		///经纪公司部门代码
		TQCVDCreditDepartmentIDType	DepartmentID;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///股东账户账户代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///本地报单编号
		TQCVDCreditOrderLocalIDType	OrderLocalID;

		///操作用户
		TQCVDCreditUserIDType	ActionUser;

		///交易日
		TQCVDCreditDateType	TradingDay;

		///操作日期
		TQCVDCreditDateType	ActionDate;

		///操作时间
		TQCVDCreditTimeType	ActionTime;

		///撤单状态
		TQCVDCreditCancelOrderStatusType	CancelOrderStatus;

		///状态信息
		TQCVDCreditErrorMsgType	StatusMsg;

		///请求编号
		TQCVDCreditRequestIDType	RequestID;

		///撤单前置编号
		TQCVDCreditFrontIDType	ActionFrontID;

		///撤单会话编号
		TQCVDCreditSessionIDType	ActionSessionID;

		///报单操作引用
		TQCVDCreditOrderRefType	OrderActionRef;

		///本地撤单系统编号
		TQCVDCreditOrderSysIDType	CancelOrderSysID;

		///撤单类型
		TQCVDCreditCancelOrderTypeType	CancelOrderType;

		///交易单元代码
		TQCVDCreditPbuIDType	PbuID;

		///交易所返回的撤单数量
		TQCVDCreditVolumeType	VolumeCanceled;

		///IP地址
		TQCVDCreditIPAddressType	IPAddress;

		///Mac地址
		TQCVDCreditMacAddressType	MacAddress;

		///记录序号(仅上证报盘使用)
		TQCVDCreditSequenceNoType	RecordNumber;

		///交易报盘编号
		TQCVDCreditTraderOfferIDType	TraderOfferID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///节点号
		TQCVDCreditNodeIDType	ServerID;

		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
	};

	/// 报告成交
	struct CQCVDCreditTradeField
	{
		///交易日
		TQCVDCreditDateType	TradingDay;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///成交编号
		TQCVDCreditTradeIDType	TradeID;

		///本地报单编号
		TQCVDCreditOrderLocalIDType	OrderLocalID;

		///全系统的唯一报单编号。
		TQCVDCreditOrderSysIDType	OrderSysID;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///目前该字段存放的是上证所和深圳的股东代码。
		TQCVDCreditShareholderIDType	ShareholderID;

		///资金账户
		TQCVDCreditAccountIDType	AccountID;

		///币种
		TQCVDCreditCurrencyIDType	CurrencyID;

		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;

		///报单类别
		TQCVDCreditDirectionType	Direction;

		///成交价格
		TQCVDCreditPriceType	Price;

		///成交数量
		TQCVDCreditVolumeType	Volume;

		///交易所为营业部分配的代码
		TQCVDCreditBranchIDType	BranchID;

		///交易单元代码
		TQCVDCreditPbuIDType	PbuID;

		///申报操作员
		TQCVDCreditUserIDType	InsertUser;

		///成交日期
		TQCVDCreditDateType	TradeDate;

		///成交时间
		TQCVDCreditTimeType	TradeTime;

		///报单引用
		TQCVDCreditOrderRefType	OrderRef;

		///经纪公司部门代码
		TQCVDCreditDepartmentIDType	DepartmentID;

		///节点号
		TQCVDCreditNodeIDType	ServerID;

		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
	};

	/// 报告资金
	struct CQCVDCreditTradingAccountField
	{
		///交易日
		TQCVDCreditDateType	TradingDay;

		///经纪公司部门代码
		TQCVDCreditDepartmentIDType	DepartmentID;

		///资金账户
		TQCVDCreditAccountIDType	AccountID;

		///币种
		TQCVDCreditCurrencyIDType	CurrencyID;

		///普通、信用、衍生品等
		TQCVDCreditAccountTypeType	AccountType;

		///上日结存
		TQCVDCreditMoneyType	PreDeposit;

		///可用金额
		TQCVDCreditMoneyType	UsefulMoney;

		///可取额度
		TQCVDCreditMoneyType	FetchLimit;

		///当日入金的总额
		TQCVDCreditMoneyType	Deposit;

		///当日出金的总额
		TQCVDCreditMoneyType	Withdraw;

		///因为下单而冻结的资金，包括买入现货的资金和买入期权的权利金
		TQCVDCreditMoneyType	FrozenCash;

		///因为下单而冻结的手续费
		TQCVDCreditMoneyType	FrozenCommission;

		///成交单产生的手续费
		TQCVDCreditMoneyType	Commission;

		///该资金账户当日已成交的总权利金收入
		TQCVDCreditMoneyType	RoyaltyIn;

		///该资金账户当日已成交的总权利金支出
		TQCVDCreditMoneyType	RoyaltyOut;

		///融券卖出剩余金额
		TQCVDCreditMoneyType	CreditSellAmount;

		///废弃字段
		TQCVDCreditMoneyType	CreditSellUseAmount;

		///资金账户所属投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///银行代码
		TQCVDCreditBankIDType	BankID;

		///签约银行账户
		TQCVDCreditBankAccountIDType	BankAccountID;

		///虚拟资产
		TQCVDCreditMoneyType	VirtualAssets;

		///融券卖出金额冻结(用于偿还融资负债或买特殊品种的未成交冻结金额)
		TQCVDCreditMoneyType	CreditSellFrozenAmount;

		///可用未交收金额(港股通专用字段)
		TQCVDCreditMoneyType	UnDeliveredMoney;

		///上日未交收金额(港股通专用字段)
		TQCVDCreditMoneyType	PreUnDeliveredMoney;

		///冻结未交收金额(港股通专用)
		TQCVDCreditMoneyType	UnDeliveredFrozenCash;

		///冻结的未交收金额中的手续费(港股通专用)
		TQCVDCreditMoneyType	UnDeliveredFrozenCommission;

		///占用未交收金额中的手续费(港股通专用)
		TQCVDCreditMoneyType	UnDeliveredCommission;

		///购买货币基金等品种占用融券卖出金额部分
		TQCVDCreditMoneyType	CreditSellOccupyAmount;

		///当日买入货币基金等品种占用融券卖出金额部分
		TQCVDCreditMoneyType	CreditSellTodayOccupyAmount;

		///当日卖出货币基金等品种释放融券卖出金额部分
		TQCVDCreditMoneyType	CreditSellTodayReleaseAmount;

		///可用资金调整
		TQCVDCreditMoneyType	UsefulMoneyAdjust;

		///融券资金调整
		TQCVDCreditMoneyType	CreditSellMoneyAdjust;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	/// 报告持仓
	struct CQCVDCreditPositionField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///股东账户代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///交易日期
		TQCVDCreditDateType	TradingDay;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;

		///昨日持仓（包含昨日持仓冻结数量）
		TQCVDCreditVolumeType	HistoryPos;

		///昨日持仓冻结
		TQCVDCreditVolumeType	HistoryPosFrozen;

		///今日买卖持仓（包含今日买卖持仓冻结数量）
		TQCVDCreditVolumeType	TodayBSPos;

		///今日买卖持仓冻结
		TQCVDCreditVolumeType	TodayBSPosFrozen;

		///今日申赎持仓（包含今日申赎持仓冻结数量）
		TQCVDCreditVolumeType	TodayPRPos;

		///今日申赎持仓冻结
		TQCVDCreditVolumeType	TodayPRPosFrozen;

		///今日拆分合并持仓（包含今日拆分合并持仓冻结数量）
		TQCVDCreditVolumeType	TodaySMPos;

		///今日拆分合并持仓冻结
		TQCVDCreditVolumeType	TodaySMPosFrozen;

		///昨日持仓成本价
		TQCVDCreditPriceType	HistoryPosPrice;

		///总持仓成本
		TQCVDCreditMoneyType	TotalPosCost;

		///开仓成本
		TQCVDCreditMoneyType	OpenPosCost;

		///上次余额(盘中不变)
		TQCVDCreditVolumeType	PrePosition;

		///股份可用
		TQCVDCreditVolumeType	AvailablePosition;

		///股份余额
		TQCVDCreditVolumeType	CurrentPosition;

		///当日盈亏
		TQCVDCreditMoneyType	CurrentProfit;

		///融资仓位
		TQCVDCreditVolumeType	CreditBuyPos;

		///融券仓位（包含今日融券仓位）
		TQCVDCreditVolumeType	CreditSellPos;

		///今日融券仓位
		TQCVDCreditVolumeType	TodayCreditSellPos;

		///划出仓位
		TQCVDCreditVolumeType	CollateralOutPos;

		///买券还券未成交数量(直接还券+买券还券)
		TQCVDCreditVolumeType	RepayUntradeVolume;

		///直接还券未成交数量
		TQCVDCreditVolumeType	RepayTransferUntradeVolume;

		///担保品买入未成交金额
		TQCVDCreditMoneyType	CollateralBuyUntradeAmount;

		///担保品买入未成交数量
		TQCVDCreditVolumeType	CollateralBuyUntradeVolume;

		///融资买入金额(包含交易费用)
		TQCVDCreditMoneyType	CreditBuyAmount;

		///融资买入未成交金额(包含交易费用)
		TQCVDCreditMoneyType	CreditBuyUntradeAmount;

		///融资冻结保证金
		TQCVDCreditMoneyType	CreditBuyFrozenMargin;

		///融资买入利息
		TQCVDCreditMoneyType	CreditBuyInterestFee;

		///融资买入未成交数量
		TQCVDCreditVolumeType	CreditBuyUntradeVolume;

		///融券卖出金额(以成交价计算)
		TQCVDCreditMoneyType	CreditSellAmount;

		///融券卖出未成交金额
		TQCVDCreditMoneyType	CreditSellUntradeAmount;

		///融券冻结保证金
		TQCVDCreditMoneyType	CreditSellFrozenMargin;

		///融券卖出息费
		TQCVDCreditMoneyType	CreditSellInterestFee;

		///融券卖出未成交数量
		TQCVDCreditVolumeType	CreditSellUntradeVolume;

		///划入待收仓位
		TQCVDCreditVolumeType	CollateralInPos;

		///融资流动冻结保证金
		TQCVDCreditMoneyType	CreditBuyFrozenCirculateMargin;

		///融券流动冻结保证金
		TQCVDCreditMoneyType	CreditSellFrozenCirculateMargin;

		///保留字段
		TQCVDCreditMoneyType	Reserve1;

		///保留字段
		TQCVDCreditMoneyType	Reserve2;

		///累计平仓盈亏
		TQCVDCreditMoneyType	CloseProfit;

		///当日累计开仓数量
		TQCVDCreditVolumeType	TodayTotalOpenVolume;

		///证券名称
		TQCVDCreditShortSecurityNameType	SecurityName;

		///最新价
		TQCVDCreditPriceType	LastPrice;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	/// 报告新股申购额度
	struct CQCVDCreditIPOQuotaField
	{
		///交易日
		TQCVDCreditDateType	TradingDay;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///股东账户代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///新股可申购额度
		TQCVDCreditLongVolumeType	MaxVolume;

		///科创板可申购额度
		TQCVDCreditLongVolumeType	KCMaxVolume;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	/// 报告资金转移流水
	struct CQCVDCreditFundTransferDetailField
	{
		///交易日
		TQCVDCreditDateType	TradingDay;

		///转移流水号
		TQCVDCreditIntSerialType	FundSerial;

		///申请流水号
		TQCVDCreditIntSerialType	ApplySerial;

		///前置编号
		TQCVDCreditFrontIDType	FrontID;

		///会话编号
		TQCVDCreditSessionIDType	SessionID;

		///资金账户代码
		TQCVDCreditAccountIDType	AccountID;

		///币种
		TQCVDCreditCurrencyIDType	CurrencyID;

		///转移方向
		TQCVDCreditTransferDirectionType	TransferDirection;

		///转移金额
		TQCVDCreditMoneyType	Amount;

		///转移状态
		TQCVDCreditTransferStatusType	TransferStatus;

		///操作来源
		TQCVDCreditOperateSourceType	OperateSource;

		///操作人员
		TQCVDCreditUserIDType	OperatorID;

		///操作日期
		TQCVDCreditDateType	OperateDate;

		///操作时间
		TQCVDCreditTimeType	OperateTime;

		///状态信息
		TQCVDCreditErrorMsgType	StatusMsg;

		///经纪公司部门代码
		TQCVDCreditDepartmentIDType	DepartmentID;

		///外部流水号
		TQCVDCreditExternalSerialType	ExternalSerial;

		///用户请求编号
		TQCVDCreditRequestIDType	RequestID;

		///签约银行账户
		TQCVDCreditBankAccountIDType	BankAccountID;

		///银行代码
		TQCVDCreditBankIDType	BankID;

		///IP地址
		TQCVDCreditIPAddressType	IPAddress;

		///Mac地址
		TQCVDCreditMacAddressType	MacAddress;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///外部系统节点号
		TQCVDCreditNodeIDType	ExternalNodeID;

		///(直接还款用)指定偿还的信用负债编号（该字段置空表示不指定偿还）
		TQCVDCreditCreditDebtIDType	CreditDebtID;

		///强平原因
		TQCVDCreditForceCloseReasonType	ForceCloseReason;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	/// 报告持仓转移流水
	struct CQCVDCreditPositionTransferDetailField
	{
		///转移流水号
		TQCVDCreditIntSerialType	PositionSerial;

		///申请流水号
		TQCVDCreditIntSerialType	ApplySerial;

		///前置编号
		TQCVDCreditFrontIDType	FrontID;

		///会话编号
		TQCVDCreditSessionIDType	SessionID;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///股东账户代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;

		///交易日期
		TQCVDCreditDateType	TradingDay;

		///转移方向
		TQCVDCreditTransferDirectionType	TransferDirection;

		///转移持仓类型
		TQCVDCreditTransferPositionTypeType	TransferPositionType;

		///昨日仓位转入转出数量
		TQCVDCreditVolumeType	HistoryVolume;

		///今日买卖仓位转入转出数量
		TQCVDCreditVolumeType	TodayBSVolume;

		///今日申赎仓位转入转出数量
		TQCVDCreditVolumeType	TodayPRVolume;

		///转移状态
		TQCVDCreditTransferStatusType	TransferStatus;

		///操作人员
		TQCVDCreditUserIDType	OperatorID;

		///操作日期
		TQCVDCreditDateType	OperateDate;

		///操作时间
		TQCVDCreditTimeType	OperateTime;

		///操作来源
		TQCVDCreditOperateSourceType	OperateSource;

		///状态信息
		TQCVDCreditErrorMsgType	StatusMsg;

		///用户请求编号
		TQCVDCreditRequestIDType	RequestID;

		///转移持仓成本
		TQCVDCreditMoneyType	TransTotalCost;

		///IP地址
		TQCVDCreditIPAddressType	IPAddress;

		///Mac地址
		TQCVDCreditMacAddressType	MacAddress;

		///外部节点编号
		TQCVDCreditNodeIDType	ExternalNodeID;

		///今日拆分合并数量
		TQCVDCreditVolumeType	TodaySMVolume;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	/// 报告条件单
	struct CQCVDCreditCondOrderField
	{
		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///股东账户代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;

		///买卖方向
		TQCVDCreditDirectionType	Direction;

		///价格类型
		TQCVDCreditOrderPriceTypeType	OrderPriceType;

		///有效期类型
		TQCVDCreditTimeConditionType	TimeCondition;

		///成交量类型
		TQCVDCreditVolumeConditionType	VolumeCondition;

		///报单价格
		TQCVDCreditPriceType	LimitPrice;

		///报单数量
		TQCVDCreditVolumeType	VolumeTotalOriginal;

		///委托方式
		TQCVDCreditOperwayType	Operway;

		///港股通订单数量类型
		TQCVDCreditLotTypeType	LotType;

		///条件检查
		TQCVDCreditCondCheckType	CondCheck;

		///有效日期
		TQCVDCreditDateType	GTDate;

		///条件报单引用
		TQCVDCreditOrderRefType	CondOrderRef;

		///报单编号
		TQCVDCreditCondOrderIDType	CondOrderID;

		///强平原因
		TQCVDCreditForceCloseReasonType	ForceCloseReason;

		///指定偿还的信用负债编号（该字段置空表示不指定偿还）
		TQCVDCreditCreditDebtIDType	CreditDebtID;

		///头寸类型
		TQCVDCreditCreditQuotaTypeType	CreditQuotaType;

		///费息折扣券编号（0表示不使用折扣券）
		TQCVDCreditIntSerialType	DiscountCouponID;

		///字符串附加信息
		TQCVDCreditStrInfoType	SInfo;

		///整形附加信息
		TQCVDCreditIntInfoType	IInfo;

		///触发基准数量类型
		TQCVDCreditTriggerOrderVolumeTypeType	TriggerOrderVolumeType;

		///触发基准价类型
		TQCVDCreditTriggerOrderPriceTypeType	TriggerOrderPriceType;

		///条件单触发条件
		TQCVDCreditContingentConditionType	ContingentCondition;

		///条件价
		TQCVDCreditPriceType	ConditionPrice;

		///价格浮动tick数
		TQCVDCreditVolumeType	PriceTicks;

		///数量浮动倍数
		TQCVDCreditVolumeMultipleType	VolumeMultiple;

		///相关前置编号
		TQCVDCreditFrontIDType	RelativeFrontID;

		///相关会话编号
		TQCVDCreditSessionIDType	RelativeSessionID;

		///相关条件参数
		TQCVDCreditRelativeCondParamType	RelativeParam;

		///附加条件单触发条件
		TQCVDCreditContingentConditionType	AppendContingentCondition;

		///附加条件价
		TQCVDCreditPriceType	AppendConditionPrice;

		///附加相关前置编号
		TQCVDCreditFrontIDType	AppendRelativeFrontID;

		///附加相关会话编号
		TQCVDCreditSessionIDType	AppendRelativeSessionID;

		///附加相关条件参数
		TQCVDCreditRelativeCondParamType	AppendRelativeParam;

		///请求编号
		TQCVDCreditRequestIDType	RequestID;

		///交易日
		TQCVDCreditDateType	TradingDay;

		///条件单状态
		TQCVDCreditCondOrderStatusType	CondOrderStatus;

		///状态信息
		TQCVDCreditErrorMsgType	StatusMsg;

		///申报用户
		TQCVDCreditUserIDType	InsertUser;

		///申报日期
		TQCVDCreditDateType	InsertDate;

		///申报时间
		TQCVDCreditTimeType	InsertTime;

		///申报时间(毫秒)
		TQCVDCreditMillisecType	InsertMillisec;

		///触发日期
		TQCVDCreditDateType	ActiveDate;

		///触发时间
		TQCVDCreditTimeType	ActiveTime;

		///撤销用户
		TQCVDCreditUserIDType	CancelUser;

		///撤销时间
		TQCVDCreditTimeType	CancelTime;

		///前置编号
		TQCVDCreditFrontIDType	FrontID;

		///会话编号
		TQCVDCreditSessionIDType	SessionID;

		///经纪公司部门代码
		TQCVDCreditDepartmentIDType	DepartmentID;

		///用户端产品信息
		TQCVDCreditUserProductInfoType	UserProductInfo;

		///IP地址
		TQCVDCreditIPAddressType	IPAddress;

		///Mac地址
		TQCVDCreditMacAddressType	MacAddress;

		///附加条件参数
		TQCVDCreditRelativeCondParamType	AppendCondParam;

		///当前触发条件
		TQCVDCreditContingentConditionType	TriggerContingentCondition;

		///当前触发条件价
		TQCVDCreditPriceType	TriggerConditionPrice;

		///当前触发相关参数
		TQCVDCreditRelativeCondParamType	TriggerRelativeParam;

		///条件单完成状态
		TQCVDCreditCondOrderFinishStatusType	CondOrderFinishStatus;

		///主触发条件信息,可能为OrderSysID,Time,SecurityID
		TQCVDCreditRelativeCondParamType	CondParam;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	/// 报告条件单撤单
	struct CQCVDCreditCancelCondOrderField
	{
		///交易日
		TQCVDCreditDateType	TradingDay;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///被撤条件单前置编号
		TQCVDCreditFrontIDType	FrontID;

		///被撤条件单会话编号
		TQCVDCreditSessionIDType	SessionID;

		///被撤条件单引用
		TQCVDCreditOrderRefType	CondOrderRef;

		///被撤条件单编号
		TQCVDCreditCondOrderIDType	CondOrderID;

		///操作标志
		TQCVDCreditActionFlagType	ActionFlag;

		///委托方式
		TQCVDCreditOperwayType	Operway;

		///条件单操作引用
		TQCVDCreditOrderRefType	CondOrderActionRef;

		///条件单撤单编号
		TQCVDCreditCondOrderIDType	CancelCondOrderID;

		///字符串附加信息
		TQCVDCreditStrInfoType	SInfo;

		///整形附加信息
		TQCVDCreditIntInfoType	IInfo;

		///请求编号
		TQCVDCreditRequestIDType	RequestID;

		///撤单前置编号
		TQCVDCreditFrontIDType	ActionFrontID;

		///撤单会话编号
		TQCVDCreditSessionIDType	ActionSessionID;

		///经纪公司部门代码
		TQCVDCreditDepartmentIDType	DepartmentID;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///股东账户代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///操作用户
		TQCVDCreditUserIDType	ActionUser;

		///操作日期
		TQCVDCreditDateType	ActionDate;

		///操作时间
		TQCVDCreditTimeType	ActionTime;

		///IP地址
		TQCVDCreditIPAddressType	IPAddress;

		///Mac地址
		TQCVDCreditMacAddressType	MacAddress;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	/// 报告新股配号信息
	struct CQCVDCreditIPONumberResultField
	{
		///交易日
		TQCVDCreditDateType	TradingDay;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///申购代码
		TQCVDCreditSecurityIDType	SecurityID;

		///股东代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///日期
		TQCVDCreditDateType	Day;

		///申购证券名称
		TQCVDCreditShortSecurityNameType	SecurityName;

		///证券类别代码
		TQCVDCreditSecurityTypeType	SecurityType;

		///起始配号
		TQCVDCreditIPONumberIDType	BeginNumberID;

		///配号数量
		TQCVDCreditVolumeType	Volume;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	/// 报告新股中签信息
	struct CQCVDCreditIPOMatchNumberResultField
	{
		///交易日
		TQCVDCreditDateType	TradingDay;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///申购代码
		TQCVDCreditSecurityIDType	SecurityID;

		///股东代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///中签配号
		TQCVDCreditIPONumberIDType	MatchNumberID;

		///日期
		TQCVDCreditDateType	Day;

		///申购证券名称
		TQCVDCreditShortSecurityNameType	SecurityName;

		///证券类别代码
		TQCVDCreditSecurityTypeType	SecurityType;

		///数量
		TQCVDCreditVolumeType	Volume;

		///申购价格
		TQCVDCreditPriceType	Price;

		///申购金额
		TQCVDCreditMoneyType	Amout;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	/// 报告投资者交易协议
	struct CQCVDCreditShareholderSpecPrivilegeField
	{
		///交易日
		TQCVDCreditDateType	TradingDay;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///股东账户代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///特殊权限类别
		TQCVDCreditSpecPrivilegeTypeType	SpecPrivilegeType;

		///买卖方向
		TQCVDCreditDirectionType	Direction;

		///是否禁止
		TQCVDCreditBoolType	bForbidden;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	/// 报告新股申购信息
	struct CQCVDCreditIPOInfoField
	{
		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///申购代码
		TQCVDCreditSecurityIDType	SecurityID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///证券品种代码
		TQCVDCreditProductIDType	ProductID;

		///证券类别代码
		TQCVDCreditSecurityTypeType	SecurityType;

		///发行价
		TQCVDCreditPriceType	Price;

		///币种
		TQCVDCreditCurrencyIDType	CurrencyID;

		///新股名称
		TQCVDCreditShortSecurityNameType	SecurityName;

		///基础证券代码
		TQCVDCreditSecurityIDType	UnderlyingSecurityID;

		///基础证券名称
		TQCVDCreditShortSecurityNameType	UnderlyingSecurityName;

		///网上申购最小数量
		TQCVDCreditVolumeType	MinVolume;

		///网上申购最大数量
		TQCVDCreditVolumeType	MaxVolume;

		///网上申购单位数量
		TQCVDCreditVolumeType	VolumeUnit;

		///发行方式
		TQCVDCreditIssueModeType	IssueMode;

		///交易日
		TQCVDCreditDateType	TradingDay;
	};

	/// 报告配股配债信息
	struct CQCVDCreditRationalInfoField
	{
		///交易日
		TQCVDCreditDateType	TradingDay;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///配股配债代码
		TQCVDCreditSecurityIDType	SecurityID;

		///配股配债价格
		TQCVDCreditPriceType	Price;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///证券品种代码
		TQCVDCreditProductIDType	ProductID;

		///证券类别代码
		TQCVDCreditSecurityTypeType	SecurityType;

		///配股名称
		TQCVDCreditShortSecurityNameType	SecurityName;

		///基础证券代码
		TQCVDCreditSecurityIDType	UnderlyingSecurityID;

		///基础证券名称
		TQCVDCreditShortSecurityNameType	UnderlyingSecurityName;

		///配股配债最小数量
		TQCVDCreditVolumeType	MinVolume;

		///配股配债最大数量
		TQCVDCreditVolumeType	MaxVolume;

		///配股配债单位数量
		TQCVDCreditVolumeType	VolumeUnit;
	};

	/// 报告外围资金转移流水
	struct CQCVDCreditPeripheryFundTransferDetailField
	{
		///交易日
		TQCVDCreditDateType	TradingDay;

		///资金调拨流水号
		TQCVDCreditIntSerialType	FundSerial;

		///资金调拨请求流水号
		TQCVDCreditIntSerialType	ApplySerial;

		///前置编号
		TQCVDCreditFrontIDType	FrontID;

		///会话编号
		TQCVDCreditSessionIDType	SessionID;

		///资金调拨方向
		TQCVDCreditTransferDirectionType	TransferDirection;

		///经纪公司部门代码
		TQCVDCreditDepartmentIDType	DepartmentID;

		///资金账户代码
		TQCVDCreditAccountIDType	AccountID;

		///币种
		TQCVDCreditCurrencyIDType	CurrencyID;

		///转移金额
		TQCVDCreditMoneyType	Amount;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///资金调拨原因
		TQCVDCreditTransferReasonType	TransferReason;

		///转移状态
		TQCVDCreditTransferStatusType	TransferStatus;

		///操作日期
		TQCVDCreditDateType	OperateDate;

		///操作时间
		TQCVDCreditTimeType	OperateTime;

		///冲正日期
		TQCVDCreditDateType	RepealDate;

		///冲正时间
		TQCVDCreditTimeType	RepealTime;

		///冲正原因
		TQCVDCreditTransferReasonType	RepealReason;

		///状态信息
		TQCVDCreditErrorMsgType	StatusMsg;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	/// 报告外围持仓转移流水
	struct CQCVDCreditPeripheryPositionTransferDetailField
	{
		///仓位调拨流水号
		TQCVDCreditIntSerialType	PositionSerial;

		///仓位调拨请求流水号
		TQCVDCreditIntSerialType	ApplySerial;

		///前置编号
		TQCVDCreditFrontIDType	FrontID;

		///会话编号
		TQCVDCreditSessionIDType	SessionID;

		///仓位调拨方向
		TQCVDCreditTransferDirectionType	TransferDirection;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///股东账户代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;

		///今日买卖仓位转入转出数量
		TQCVDCreditVolumeType	TodayBSPos;

		///今日申赎仓位转入数量
		TQCVDCreditVolumeType	TodayPRPos;

		///昨日仓位转入数量
		TQCVDCreditVolumeType	HistoryPos;

		///交易日
		TQCVDCreditDateType	TradingDay;

		///仓位调拨原因
		TQCVDCreditTransferReasonType	TransferReason;

		///转移状态
		TQCVDCreditTransferStatusType	TransferStatus;

		///操作日期
		TQCVDCreditDateType	OperateDate;

		///操作时间
		TQCVDCreditTimeType	OperateTime;

		///冲正日期
		TQCVDCreditDateType	RepealDate;

		///冲正时间
		TQCVDCreditTimeType	RepealTime;

		///冲正原因
		TQCVDCreditTransferReasonType	RepealReason;

		///状态信息
		TQCVDCreditErrorMsgType	StatusMsg;

		///今日拆分合并仓位转入数量
		TQCVDCreditVolumeType	TodaySMPos;

		///转移持仓成本
		TQCVDCreditMoneyType	TransTotalCost;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	/// 报告用户
	struct CQCVDCreditUserField
	{
		///交易日
		TQCVDCreditDateType	TradingDay;

		///用户代码
		TQCVDCreditUserIDType	UserID;

		///用户名称
		TQCVDCreditUserNameType	UserName;

		///用户类型
		TQCVDCreditUserTypeType	UserType;

		///经纪公司部门代码
		TQCVDCreditDepartmentIDType	DepartmentID;

		///密码
		TQCVDCreditUserPasswordType	UserPassword;

		///限制用户同时可在几个会话中登录，投资者用户默认为1
		TQCVDCreditLoginLimitType	LoginLimit;

		///限制用户最多能输错几次密码, -1表示不限制
		TQCVDCreditLoginLimitType	PasswordFailLimit;

		///登录状态
		TQCVDCreditLoginStatusType	LoginStatus;

		///开户日期
		TQCVDCreditDateType	OpenDate;

		///销户日期
		TQCVDCreditDateType	CloseDate;

		///撤单流控
		TQCVDCreditCommFluxType	OrderInsertCommFlux;

		///撤单流控
		TQCVDCreditCommFluxType	OrderActionCommFlux;

		///是否需要改密
		TQCVDCreditBoolType	NeedUpdatePassword;

		///密码到期日期
		TQCVDCreditDateType	PasswordExpiryDate;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	/// 报告信用负债
	struct CQCVDCreditCreditDebtField
	{
		///交易日
		TQCVDCreditDateType	TradingDay;

		///信用负债编号
		TQCVDCreditCreditDebtIDType	CreditDebtID;

		///信用负债类型
		TQCVDCreditCreditDebtTypeType	CreditDebtType;

		///信用合约状态
		TQCVDCreditCreditDebtStatusType	CreditDebtStatus;

		///头寸类型
		TQCVDCreditCreditQuotaTypeType	CreditQuotaType;

		///经纪公司部门代码
		TQCVDCreditDepartmentIDType	DepartmentID;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///股东代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;

		///开仓日期
		TQCVDCreditDateType	OpenDate;

		///开仓时间
		TQCVDCreditTimeType	OpenTime;

		///到期日
		TQCVDCreditDateType	ExpireDate;

		///数量
		TQCVDCreditVolumeType	Volume;

		///金额
		TQCVDCreditMoneyType	Amount;

		///未偿还数量
		TQCVDCreditVolumeType	UnpaidVolume;

		///未偿还金额
		TQCVDCreditMoneyType	UnpaidAmount;

		///未偿还交易费用（融资）
		TQCVDCreditMoneyType	UnpaidTradingFee;

		///未偿还息费（利息）
		TQCVDCreditMoneyType	UnpaidInterestFee;

		///还券未成交数量
		TQCVDCreditVolumeType	RepayUntradeVolume;

		///报单申报用户
		TQCVDCreditUserIDType	InsertUser;

		///资金账户
		TQCVDCreditAccountIDType	AccountID;

		///币种
		TQCVDCreditCurrencyIDType	CurrencyID;

		///已展期次数
		TQCVDCreditExtendNumType	ExtendNum;

		///日初利率
		TQCVDCreditRatioType	PreInterestRate;

		///利率
		TQCVDCreditRatioType	InterestRate;

		///费息折扣券编号（0表示不使用折扣券）
		TQCVDCreditIntSerialType	DiscountCouponID;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	/// 报告信用划转
	struct CQCVDCreditCreditTransferField
	{
		///交易日
		TQCVDCreditDateType	TradingDay;

		///交易单元代码
		TQCVDCreditPbuIDType	PbuID;

		///本地报单编号
		TQCVDCreditOrderLocalIDType	OrderLocalID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///全系统的唯一报单编号
		TQCVDCreditOrderSysIDType	OrderSysID;

		///前置编号
		TQCVDCreditFrontIDType	FrontID;

		///会话编号
		TQCVDCreditSessionIDType	SessionID;

		///报单引用
		TQCVDCreditOrderRefType	OrderRef;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///目前该字段存放的是上证所和深圳的股东代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///经纪公司部门代码
		TQCVDCreditDepartmentIDType	DepartmentID;

		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;

		///报单类别
		TQCVDCreditDirectionType	Direction;

		///报单申报数量
		TQCVDCreditVolumeType	VolumeTotalOriginal;

		///撤单数量
		TQCVDCreditVolumeType	VolumeCanceled;

		///信用转移状态
		TQCVDCreditCreditTransferStatusType	CreditTransferStatus;

		///报单操作状态
		TQCVDCreditOrderSubmitStatusType	OrderSubmitStatus;

		///状态信息
		TQCVDCreditStatusMsgType	StatusMsg;

		///系统错误代码
		TQCVDCreditErrorIDType	ErrorID;

		///交易所为营业部分配的代码
		TQCVDCreditBranchIDType	BranchID;

		///报单申报用户
		TQCVDCreditUserIDType	InsertUser;

		///申报日期
		TQCVDCreditDateType	InsertDate;

		///申报时间
		TQCVDCreditTimeType	InsertTime;

		///申报时间(毫秒)
		TQCVDCreditMillisecType	InsertMillisec;

		///交易所接收时间
		TQCVDCreditTimeType	AcceptTime;

		///撤销时间
		TQCVDCreditTimeType	CancelTime;

		///撤销申报用户
		TQCVDCreditUserIDType	CancelUser;

		///IP地址
		TQCVDCreditIPAddressType	IPAddress;

		///Mac地址
		TQCVDCreditMacAddressType	MacAddress;

		///请求编号
		TQCVDCreditRequestIDType	RequestID;

		///记录序号(仅上证报盘使用)
		TQCVDCreditSequenceNoType	RecordNumber;

		///持仓转移流水号
		TQCVDCreditIntSerialType	PositionSerial;

		///还券划转指定偿还信用负债编号
		TQCVDCreditCreditDebtIDType	CreditDebtID;

		///资金账户
		TQCVDCreditAccountIDType	AccountID;

		///币种
		TQCVDCreditCurrencyIDType	CurrencyID;

		///字符串附加信息
		TQCVDCreditStrInfoType	SInfo;

		///整形附加信息
		TQCVDCreditIntInfoType	IInfo;

		///委托方式
		TQCVDCreditOperwayType	Operway;

		///现货持仓转移流水号
		TQCVDCreditIntSerialType	StockPositionSerial;

		///投资者类型
		TQCVDCreditInvestorTypeType	InvestorType;

		///交易所流水号(仅深圳登记结算报盘使用)
		TQCVDCreditSerialNoType	SerialNumber;

		///强平原因
		TQCVDCreditForceCloseReasonType	ForceCloseReason;

		///交易报盘编号
		TQCVDCreditTraderOfferIDType	TraderOfferID;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	/// 报告撤销信用划转
	struct CQCVDCreditCancelCreditTransferField
	{
		///交易日
		TQCVDCreditDateType	TradingDay;

		///全系统的唯一报单编号
		TQCVDCreditOrderLocalIDType	CancelOrderLocalID;

		///撤单前置编号
		TQCVDCreditFrontIDType	ActionFrontID;

		///撤单会话编号
		TQCVDCreditSessionIDType	ActionSessionID;

		///撤单引用
		TQCVDCreditOrderRefType	OrderActionRef;

		///委托操作标志
		TQCVDCreditActionFlagType	ActionFlag;

		///该撤单对应被撤报单的本地报单编号
		TQCVDCreditOrderLocalIDType	OrderLocalID;

		///该撤单对应被撤报单的系统报单编号
		TQCVDCreditOrderSysIDType	OrderSysID;

		///被撤前置编号
		TQCVDCreditFrontIDType	FrontID;

		///被撤会话编号
		TQCVDCreditSessionIDType	SessionID;

		///报单引用
		TQCVDCreditOrderRefType	OrderRef;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///目前该字段存放的是上证所和深圳的股东代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;

		///撤单的状态
		TQCVDCreditCancelOrderStatusType	CancelOrderStatus;

		///交易所返回的撤单数量
		TQCVDCreditVolumeType	VolumeCanceled;

		///状态信息
		TQCVDCreditStatusMsgType	StatusMsg;

		///系统错误代码
		TQCVDCreditErrorIDType	ErrorID;

		///交易所为营业部分配的代码
		TQCVDCreditBranchIDType	BranchID;

		///交易单元代码
		TQCVDCreditPbuIDType	PbuID;

		///报单申报用户
		TQCVDCreditUserIDType	InsertUser;

		///申报日期
		TQCVDCreditDateType	InsertDate;

		///申报时间
		TQCVDCreditTimeType	InsertTime;

		///申报时间(毫秒)
		TQCVDCreditMillisecType	InsertMillisec;

		///IP地址
		TQCVDCreditIPAddressType	IPAddress;

		///Mac地址
		TQCVDCreditMacAddressType	MacAddress;

		///请求编号
		TQCVDCreditRequestIDType	RequestID;

		///委托方式
		TQCVDCreditOperwayType	Operway;

		///记录序号(仅上证报盘使用)
		TQCVDCreditSequenceNoType	RecordNumber;

		///短字符串附加信息
		TQCVDCreditStrInfoType	SInfo;

		///整形附加信息
		TQCVDCreditIntInfoType	IInfo;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	/// 报告投资者实时融资融券信息
	struct CQCVDCreditInvestorRealTimeCreditInfoField
	{
		///交易日
		TQCVDCreditDateType	TradingDay;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///经纪公司部门代码
		TQCVDCreditDepartmentIDType	DepartmentID;

		///维持担保比例
		TQCVDCreditRatioType	CollateralRatio;

		///可用保证金
		TQCVDCreditMoneyType	UsefulMargin;

		///上日结存
		TQCVDCreditMoneyType	PreDeposit;

		///可用金额
		TQCVDCreditMoneyType	UsefulMoney;

		///当日入金的总额
		TQCVDCreditMoneyType	Deposit;

		///当日出金的总额
		TQCVDCreditMoneyType	Withdraw;

		///证券市值（扣除担保品划出）
		TQCVDCreditMoneyType	StockMarketValue;

		///冲抵保证金证券市值(证券市值-融资买入市值)
		TQCVDCreditMoneyType	CollateralValue;

		///融资负债(包含交易费用)
		TQCVDCreditMoneyType	CreditBuyDebt;

		///融资未成交负债
		TQCVDCreditMoneyType	CreditBuyUntradeDebt;

		///融资未成交冻结保证金
		TQCVDCreditMoneyType	CreditBuyFrozenMargin;

		///融资盈亏（未折算）
		TQCVDCreditMoneyType	CreditBuyProfit;

		///融券卖出金额（按还券数量折算后）
		TQCVDCreditMoneyType	CreditSellMoney;

		///融券卖出负债（融券市值）
		TQCVDCreditMoneyType	CreditSellDebt;

		///融券卖出未成交负债
		TQCVDCreditMoneyType	CreditSellUntradeDebt;

		///融券冻结保证金
		TQCVDCreditMoneyType	CreditSellFrozenMargin;

		///融券盈亏（未折算）
		TQCVDCreditMoneyType	CreditSellProfit;

		///利息/费用
		TQCVDCreditMoneyType	InterestFee;

		///总资产
		TQCVDCreditMoneyType	TotalAsset;

		///总负债
		TQCVDCreditMoneyType	TotalDebt;

		///净资产
		TQCVDCreditMoneyType	NetAsset;

		///融资占用保证金
		TQCVDCreditMoneyType	CreditBuyMargin;

		///融券占用保证金
		TQCVDCreditMoneyType	CreditSellMargin;

		///折算后融资盈亏
		TQCVDCreditMoneyType	CreditConvertBuyProfit;

		///折算后融券盈亏
		TQCVDCreditMoneyType	CreditConvertSellProfit;

		///预计维持担保比例(包括担保划入待交收)
		TQCVDCreditRatioType	PredictCollateralRatio;

		///流动总资产
		TQCVDCreditMoneyType	TotalCirculateAsset;

		///剩余流动总资产
		TQCVDCreditMoneyType	RemainCirculateAsset;

		///融资流动占用保证金
		TQCVDCreditMoneyType	CreditBuyCirculateMargin;

		///融资流动冻结保证金
		TQCVDCreditMoneyType	CreditBuyFrozenCirculateMargin;

		///融券流动占用保证金
		TQCVDCreditMoneyType	CreditSellCirculateMargin;

		///融券流动冻结保证金
		TQCVDCreditMoneyType	CreditSellFrozenCirculateMargin;

		///更新时间
		TQCVDCreditTimeType	UpdateTime;

		///更新时间戳
		TQCVDCreditTimeStampType	TimeStamp;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	/// 报告负债展期
	struct CQCVDCreditDebtExtendField
	{
		///展期流水号
		TQCVDCreditIntSerialType	ExtendSerial;

		///申请流水号
		TQCVDCreditIntSerialType	ApplySerial;

		///前置编号
		TQCVDCreditFrontIDType	FrontID;

		///会话编号
		TQCVDCreditSessionIDType	SessionID;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///目前该字段存放的是上证所和深圳的股东代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;

		///交易日
		TQCVDCreditDateType	TradingDay;

		///信用负债编号
		TQCVDCreditCreditDebtIDType	CreditDebtID;

		///原负债到期日
		TQCVDCreditDateType	OldExpireDate;

		///新负债到期日
		TQCVDCreditDateType	NewExpireDate;

		///展期状态
		TQCVDCreditExtendStatusType	ExtendStatus;

		///状态信息
		TQCVDCreditErrorMsgType	StatusMsg;

		///手续费
		TQCVDCreditMoneyType	Commisition;

		///自动偿还利息
		TQCVDCreditMoneyType	AutoRepayInterest;

		///自动偿还资金流水号
		TQCVDCreditIntSerialType	FundSerial;

		///偿还流水号
		TQCVDCreditCreditRepayIDType	CreditRepayID;

		///操作人员
		TQCVDCreditUserIDType	OperatorID;

		///操作日期
		TQCVDCreditDateType	OperateDate;

		///操作时间
		TQCVDCreditTimeType	OperateTime;

		///最后修改时间
		TQCVDCreditTimeType	ActiveTime;

		///登录IP地址
		TQCVDCreditIPAddressType	IPAddress;

		///登录Mac地址
		TQCVDCreditMacAddressType	MacAddress;

		///用户请求编号
		TQCVDCreditRequestIDType	RequestID;

		///委托方式
		TQCVDCreditOperwayType	Operway;

		///字符串附加信息
		TQCVDCreditStrInfoType	SInfo;

		///整形附加信息
		TQCVDCreditIntInfoType	IInfo;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	/// 报告投资者融资融券信息
	struct CQCVDCreditInvestorCreditInfoField
	{
		///交易日
		TQCVDCreditDateType	TradingDay;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///经纪公司部门代码
		TQCVDCreditDepartmentIDType	DepartmentID;

		///投资者信用评级
		TQCVDCreditCreditRatingType	CreditRating;

		///普通资金头寸编号
		TQCVDCreditQuotaIDType	NormalFundQuotaID;

		///普通持仓头寸编号
		TQCVDCreditQuotaIDType	NormalPositionQuotaID;

		///专项资金头寸编号
		TQCVDCreditQuotaIDType	SpecialFundQuotaID;

		///专项持仓头寸编号
		TQCVDCreditQuotaIDType	SpecialPositionQuotaID;

		///融资额度上限
		TQCVDCreditMoneyType	CreditBuyLimitAmount;

		///融券额度上限
		TQCVDCreditMoneyType	CreditSellLimitAmount;

		///融资买入金额(包含交易费用)
		TQCVDCreditMoneyType	CreditBuyAmount;

		///融资买入未成交金额(包含交易费用)
		TQCVDCreditMoneyType	CreditBuyUntradeAmount;

		///融资冻结保证金
		TQCVDCreditMoneyType	CreditBuyFrozenMargin;

		///融资买入利息
		TQCVDCreditMoneyType	CreditBuyInterestFee;

		///融券卖出金额(以成交价计算)
		TQCVDCreditMoneyType	CreditSellAmount;

		///融券卖出未成交金额
		TQCVDCreditMoneyType	CreditSellUntradeAmount;

		///融券冻结保证金
		TQCVDCreditMoneyType	CreditSellFrozenMargin;

		///融券卖出息费
		TQCVDCreditMoneyType	CreditSellInterestFee;

		///融资流动冻结保证金
		TQCVDCreditMoneyType	CreditBuyFrozenCirculateMargin;

		///融券流动冻结保证金
		TQCVDCreditMoneyType	CreditSellFrozenCirculateMargin;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	/// 报告投资者利率
	struct CQCVDCreditInvestorCreditInterestRateField
	{
		///交易日
		TQCVDCreditDateType	TradingDay;

		///经纪公司部门代码（00000000代表所有营业部）
		TQCVDCreditDepartmentIDType	DepartmentID;

		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///融资利率
		TQCVDCreditRatioType	InterestRate;

		///融券费率
		TQCVDCreditRatioType	FeeRate;

		///专项融资利率
		TQCVDCreditRatioType	SpecialInterestRate;

		///专项融券利率
		TQCVDCreditRatioType	SpecialFeeRate;

		///交易权限模式
		TQCVDCreditRangeModeType	RangeMode;

		///节点号
		TQCVDCreditNodeIDType	ServerID;
	};

	///客户历史周期股票盈亏请求包
	struct CQCVDCreditQryHisShareProfitField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;
		///起始日期
		TQCVDCreditDateType	BegDate;
		///结束日期
		TQCVDCreditDateType	EndDate;
		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;
		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;
	};

	///客户历史周期股票盈亏应答包
	struct CQCVDCreditHisShareProfitField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;
		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;
		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;
		///期初市值
		TQCVDCreditPriceType	MarketValueBeg;
		///期末市值
		TQCVDCreditPriceType	MarketValueEnd;
		///开仓金额
		TQCVDCreditPriceType	OpenAmount;
		///平仓金额
		TQCVDCreditPriceType	CloseAmount;
		///盈亏
		TQCVDCreditPriceType	TotalProfit;
		///实际结束日期
		TQCVDCreditDateType	RealEndDate;
		///期初持仓
		TQCVDCreditVolumeType	BegPosition;
		///期末持仓
		TQCVDCreditVolumeType	EndPosition;
	};

	///历史资金请求包
	struct CQCVDCreditQryHistoryCapitalField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;
		///起始日期
		TQCVDCreditDateType	BegDate;
		///结束日期
		TQCVDCreditDateType	EndDate;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDCreditOrderSortType	OrderType;
		///每页记录数
		TQCVDCreditVolumeType	PageCount;
		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
	};

	///历史资金应答包
	struct CQCVDCreditHistoryCapitalField
	{
		///交易日
		TQCVDCreditDateType	TradingDay;
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;
		///总资产
		TQCVDCreditPriceType	TotalAsset;
		///客户资金
		TQCVDCreditPriceType	CustFund;
		///股票市值
		TQCVDCreditPriceType	StockValue;
		///入金
		TQCVDCreditPriceType	MoneyIn;
		///出金
		TQCVDCreditPriceType	MoneyOut;
		///当日盈亏
		TQCVDCreditPriceType	TodayProfit;
		///上日总资产
		TQCVDCreditPriceType	LastTotalAsset;
		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
		///总页数
		TQCVDCreditPageLocateType	PageTotal;
		///总负债
		TQCVDCreditPriceType	TotalDebt;
		///净资产
		TQCVDCreditPriceType	NetAsset;
		///总利息
		TQCVDCreditPriceType	TotalInterest;
	};

	///历史交割单请求包
	struct CQCVDCreditQryHistoryDeliveryField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;
		///起始日期
		TQCVDCreditDateType	BegDate;
		///结束日期
		TQCVDCreditDateType	EndDate;
		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;
		///委托类型 查询多个委托类型，需使用|分隔符进行拼接，1|2|3|
		TQCVDCreditTerminalInfoType	EntrustType;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDCreditOrderSortType	OrderType;
		///每页记录数
		TQCVDCreditVolumeType	PageCount;
		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
	};

	///历史交割应答包
	struct CQCVDCreditHistoryDeliveryField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;
		///成交日期
		TQCVDCreditDateType	TradeDate;
		///成交时间
		TQCVDCreditTimeType	TradeTime;
		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;
		///证券名称
		TQCVDCreditShortSecurityNameType SecurityName;
		///交易所代码
		TQCVDCreditLocalExchangeIDType ExchangeID;
		///交易所名称
		TQCVDCreditSecurityIDType ExchangeName;
		///币种类型
		TQCVDCreditLocalCurrencyIDType Currency;
		///委托类型
		TQCVDCreditLocalCurrencyIDType	EntrustType;
		///委托类型名称
		TQCVDCreditShortSecurityNameType EntrustTypeName;
		///实付金额
		TQCVDCreditPriceType ActuallyPayMoney;
		///成交金额
		TQCVDCreditPriceType Turnover;
		///成交数量
		TQCVDCreditVolumeType Volume;
		///成交价格
		TQCVDCreditPriceType	Price;
		///印花税
		TQCVDCreditPriceType	StampTax;
		///过户费
		TQCVDCreditPriceType	TransferFee;
		///实收佣金
		TQCVDCreditPriceType	ActualBrokerage;
		///交易规费
		TQCVDCreditPriceType	TransactionFee;
		///流水号
		TQCVDCreditOrderRefType	SerialNo;
		///股东账户代码
		TQCVDCreditShareholderIDType	ShareholderID;
		///本次股份余额
		TQCVDCreditVolumeType StockBalance;
		///本次资金余额
		TQCVDCreditPriceType	FundBalance;
		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
		///总页数
		TQCVDCreditPageLocateType	PageTotal;
		///开仓成本价
		TQCVDCreditPriceType	OpenAvg;
		///对应开仓成本价的平仓盈亏
		TQCVDCreditPriceType	OpenAvgProfit;
	};

	///历史资金流水请求包
	struct CQCVDCreditQryHistoryFundDetailField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;
		///起始日期
		TQCVDCreditDateType	BegDate;
		///结束日期
		TQCVDCreditDateType	EndDate;
		///业务科目 查询多个业务科目，需使用|分隔符进行拼接，10113|10213|
		TQCVDCreditTerminalInfoType	BusinessAccount;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDCreditOrderSortType	OrderType;
		///每页记录数
		TQCVDCreditVolumeType	PageCount;
		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
	};

	///历史资金流水应答包
	struct CQCVDCreditHistoryFundDetailField
	{
		///流水号
		TQCVDCreditSerialType	SerialNo;
		///交易日
		TQCVDCreditDateType	TradeDate;
		///入账时间
		TQCVDCreditTimeType	TradeTime;
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;
		///投资者名称
		TQCVDCreditSecurityIDType	InvestorName;
		///资金账户代码
		TQCVDCreditAccountIDType	AccountID;
		///业务科目
		TQCVDCreditDateType	BusinessAccount;
		///业务科目名称
		TQCVDCreditSecurityIDType	BusinessAccountName;
		///收入金额
		TQCVDCreditMoneyType MoneyIn;
		///付出金额
		TQCVDCreditMoneyType MoneyOut;
		///资金余额
		TQCVDCreditMoneyType FundBalance;
		///操作摘要
		TQCVDCreditUserNameType	OperSummary;
		///相关市场
		TQCVDCreditCombOffsetFlagType	RelevantMarket;
		///相关品种
		TQCVDCreditProgressType	RelevantVariety;
		///相关帐号
		TQCVDCreditBankAccountIDType	RelevantAccount;
		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
		///总页数
		TQCVDCreditPageLocateType	PageTotal;
	};
	///历史持仓请求包
	struct CQCVDCreditQryHistoryHoldField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;
		///起始日期
		TQCVDCreditDateType	BegDate;
		///结束日期
		TQCVDCreditDateType	EndDate;
		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDCreditOrderSortType	OrderType;
		///每页记录数
		TQCVDCreditVolumeType	PageCount;
		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
	};
	///历史持仓应答包
	struct CQCVDCreditHistoryHoldField
	{
		///交易日期
		TQCVDCreditDateType	TradingDay;
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;
		///投资者名称
		TQCVDCreditSecurityIDType	InvestorName;
		///股东账户代码
		TQCVDCreditShareholderIDType	ShareholderID;
		///交易所代码
		TQCVDCreditLocalExchangeIDType ExchangeID;
		///交易所名称
		TQCVDCreditSecurityIDType ExchangeName;
		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;
		///币种类型
		TQCVDCreditLocalCurrencyIDType Currency;
		///证券类型
		TQCVDCreditLocalCurrencyIDType	SecurityType;
		///证券类型名称
		TQCVDCreditShortSecurityNameType SecurityTypeName;
		///数量
		TQCVDCreditVolumeType Volume;
		///非流通数量
		TQCVDCreditVolumeType UncirculatedVolume;
		///持仓成本
		TQCVDCreditPriceType	HoldCost;
		///累计盈亏
		TQCVDCreditPriceType	TotalProfit;
		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
		///总页数
		TQCVDCreditPageLocateType	PageTotal;
		///开仓成本价
		TQCVDCreditPriceType	OpenAvg;
	};
	///历史委托数据请求包
	struct CQCVDCreditQryHistoryOrderEXField
	{
		///投资者代码(必填)
		TQCVDCreditShortInvestorIDType	InvestorID;
		///起始日期
		TQCVDCreditDateType	BegDate;
		///结束日期
		TQCVDCreditDateType	EndDate;
		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;
		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDCreditActionFlagType	OrderType;
		///每页记录数
		TQCVDCreditVolumeType	PageCount;
		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
	};
	///历史委托数据应答包
	struct CQCVDCreditHistoryOrderEXField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;

		///报单引用
		TQCVDCreditOrderRefType	OrderRef;

		///用户代码
		TQCVDCreditUserIDType	UserID;

		///报单价格条件
		TQCVDCreditOrderPriceTypeType	OrderPriceType;

		///买卖方向
		TQCVDCreditDirectionType	Direction;

		///组合开平标志
		TQCVDCreditCombOffsetFlagType	CombOffsetFlag;

		///组合投机套保标志
		TQCVDCreditCombHedgeFlagType	CombHedgeFlag;

		///价格
		TQCVDCreditPriceType	LimitPrice;

		///数量
		TQCVDCreditVolumeType	VolumeTotalOriginal;

		///有效期类型
		TQCVDCreditTimeConditionType	TimeCondition;

		///成交量类型
		TQCVDCreditVolumeConditionType	VolumeCondition;

		///最小成交量
		TQCVDCreditVolumeType	MinVolume;

		///强平原因
		TQCVDCreditForceCloseReasonType	ForceCloseReason;

		///请求编号
		TQCVDCreditRequestIDType	RequestID;

		///本地报单编号
		TQCVDCreditOrderLocalIDType	OrderLocalID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///股东账户代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///证券在交易所代码
		TQCVDCreditExchangeInstIDType	ExchangeInstID;

		///交易单元代码
		TQCVDCreditTraderIDType	TraderID;

		///报单提交状态
		TQCVDCreditOrderSubmitStatusType	OrderSubmitStatus;

		///交易日
		TQCVDCreditDateType	TradingDay;

		///系统报单编号
		TQCVDCreditOrderSysIDType	OrderSysID;

		///报单状态
		TQCVDCreditOrderStatusType	OrderStatus;

		///报单类型
		TQCVDCreditOrderTypeType	OrderType;

		///已成交数量
		TQCVDCreditVolumeType	VolumeTraded;

		///剩余未完成数量
		TQCVDCreditVolumeType	VolumeTotal;

		///报单日期
		TQCVDCreditDateType	InsertDate;

		///报单时间
		TQCVDCreditTimeType	InsertTime;

		///撤销时间
		TQCVDCreditTimeType	CancelTime;

		///最后修改交易单元代码
		TQCVDCreditTraderIDType	ActiveTraderID;

		///前置编号
		TQCVDCreditFrontIDType	FrontID;

		///会话编号
		TQCVDCreditSessionIDType	SessionID;

		///用户端产品信息
		TQCVDCreditProductInfoType	UserProductInfo;

		///状态信息
		TQCVDCreditErrorMsgType	StatusMsg;

		///用户强评标志
		TQCVDCreditBoolType	UserForceClose;

		///操作用户代码
		TQCVDCreditUserIDType	ActiveUserID;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///资金账户代码
		TQCVDCreditAccountIDType	AccountID;

		///IP地址
		TQCVDCreditIPAddressType	IPAddress;

		///Mac地址
		TQCVDCreditMacAddressType	MacAddress;

		///港股通订单数量类型,默认整股买卖
		TQCVDCreditLotTypeType	LotType;

		///长字符串附加信息
		TQCVDCreditBigsInfoType	BInfo;

		///短字符串附加信息
		TQCVDCreditShortsInfoType	SInfo;

		///整形附加信息
		TQCVDCreditIntInfoType	IInfo;

		///转入交易单元代码(仅在转托管操作时有效)
		TQCVDCreditPbuIDType	TransfereePbuID;

		///委托方式
		TQCVDCreditOperwayType	Operway;

		///一级机构代码
		TQCVDCreditDepartmentIDType	DepartmentID;

		///适当性控制业务类别
		TQCVDCreditProperCtrlBusinessTypeType	ProperCtrlBusinessType;

		///适当性控制通过标示
		TQCVDCreditProperCtrlPassFlagType	ProperCtrlPassFlag;

		///条件检查
		TQCVDCreditCondCheckType	CondCheck;

		///是否预埋
		TQCVDCreditBoolType	IsCacheOrder;

		///成交金额
		TQCVDCreditMoneyType	Turnover;

		///回报附加浮点型数据信息
		TQCVDCreditFloatInfoType	RtnFloatInfo;

		///回报附加整型数据
		TQCVDCreditIntInfoType	RtnIntInfo;

		///硬盘序列号
		TQCVDCreditHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDCreditMobileType	Mobile;

		///有效日期
		TQCVDCreditDateType	GTDate;

		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
		///总页数
		TQCVDCreditPageLocateType	PageTotal;
	};
	///历史成交请求包
	struct CQCVDCreditQryHistoryTradeEXField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;
		///起始日期
		TQCVDCreditDateType	BegDate;
		///结束日期
		TQCVDCreditDateType	EndDate;
		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;
		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDCreditActionFlagType	OrderType;
		///每页记录数
		TQCVDCreditVolumeType	PageCount;
		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
	};

	///历史成交应答包
	struct CQCVDCreditHistoryTradeEXField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;

		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;

		///用户代码
		TQCVDCreditUserIDType	UserID;

		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;

		///成交编号
		TQCVDCreditTradeIDType	TradeID;

		///买卖方向
		TQCVDCreditDirectionType	Direction;

		///系统报单编号
		TQCVDCreditOrderSysIDType	OrderSysID;

		///市场代码
		TQCVDCreditMarketIDType	MarketID;

		///股东账户代码
		TQCVDCreditShareholderIDType	ShareholderID;

		///证券在交易所代码
		TQCVDCreditExchangeInstIDType	ExchangeInstID;

		///开平标志
		TQCVDCreditOffsetFlagType	OffsetFlag;

		///投机套保标志
		TQCVDCreditHedgeFlagType	HedgeFlag;

		///成交价格
		TQCVDCreditPriceType	Price;

		///成交数量
		TQCVDCreditVolumeType	Volume;

		///成交日期
		TQCVDCreditDateType	TradeDate;

		///成交时间
		TQCVDCreditTimeType	TradeTime;

		///交易单元代码
		TQCVDCreditTraderIDType	TraderID;

		///本地报单编号
		TQCVDCreditOrderLocalIDType	OrderLocalID;

		///交易日
		TQCVDCreditDateType	TradingDay;

		///投资单元代码
		TQCVDCreditLongBusinessUnitIDType	BusinessUnitID;

		///资金账户代码
		TQCVDCreditAccountIDType	AccountID;

		///报单引用
		TQCVDCreditOrderRefType	OrderRef;

		///一级机构代码
		TQCVDCreditDepartmentIDType	DepartmentID;

		///实收佣金
		TQCVDCreditPriceType	ActualBrokerage;

		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
		///总页数
		TQCVDCreditPageLocateType	PageTotal;
	};

	///客户周期资金请求包
	struct CQCVDCreditQryCustPeriodCapiDataField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;
		///周期类型('1'-日期YYYYMMDD，'2'-月份YYYYMM)
		TQCVDCreditPeroidType	PeroidType;
		///起始日期
		TQCVDCreditDateType	BegDate;
		///结束日期
		TQCVDCreditDateType	EndDate;
	};

	///客户周期资金应答包
	struct CQCVDCreditCustPeriodCapiDataField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;
		///交易周期
		TQCVDCreditDateType	PeroidDate;
		///总资产
		TQCVDCreditPriceType	TotalAsset;
		///入金
		TQCVDCreditPriceType	MoneyIn;
		///出金
		TQCVDCreditPriceType	MoneyOut;
		///盈亏
		TQCVDCreditPriceType	PeroidProfit;
		///是否基准日
		TQCVDCreditStandardType	StandardType;
	};

	///客户周期盈亏请求包
	struct CQCVDCreditQryCustPeriodProfitDataField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;
		///起始日期
		TQCVDCreditDateType	BegDate;
		///结束日期
		TQCVDCreditDateType	EndDate;
		///排列方式 1-盈亏正序 2-盈亏倒序 默认倒序
		TQCVDCreditOrderSortType	OrderType;
		///每页记录数
		TQCVDCreditVolumeType	PageCount;
		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
	};

	///客户周期盈亏应答包
	struct CQCVDCreditCustPeriodProfitDataField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;
		///交易所代码（SH,SZ等）
		TQCVDCreditLocalExchangeIDType	ExchangeID;
		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;
		///盈亏
		TQCVDCreditPriceType	PeroidProfit;
		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
		///总页数
		TQCVDCreditPageLocateType	PageTotal;
	};

	///客户上日负债请求包
	struct CQCVDCreditQryCustLastDayDebtField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;
	};

	///客户上日负债请求应答包
	struct CQCVDCreditCustLastDayDebtField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;
		///节点号
		TQCVDCreditNodeIDType	ServerID;
		///上日融资负债金额
		TQCVDCreditMoneyType	CreditBuyDebt;
		///上日融券负债金额
		TQCVDCreditMoneyType	CreditSellDebt;
	};

	/// 两融历史负债信息请求包
	struct CQCVDCreditQryHistoryDebtInfoField
	{
		///投资者代码
		TQCVDCreditShortInvestorIDType	InvestorID;
		///起始交易日期
		TQCVDCreditDateType	BegTradingDay;
		///结束交易日期
		TQCVDCreditDateType	EndTradingDay;
		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;
		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDCreditOrderSortType	OrderType;
		///每页记录数
		TQCVDCreditVolumeType	PageCount;
		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
	};
	/// 两融历史负债信息应答包
	struct CQCVDCreditHistoryDebtInfoField
	{
		///交易日期
		TQCVDCreditDateType	TradingDay;
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;
		///投资者姓名
		TQCVDCreditInvestorNameType	InvestorName;
		///发生日期
		TQCVDCreditDateType	TradeDate;
		///委托号
		TQCVDCreditLocalEntrustNoType	EntrustNo;
		///委托类型
		TQCVDCreditLocalEntrustTypeType	TradeType;
		///交易所代码
		TQCVDCreditExchangeIDType	ExchangeID;
		///股东账户代码
		TQCVDCreditShareholderIDType	ShareholderID;
		///证券代码
		TQCVDCreditSecurityIDType	SecurityID;
		///证券简称
		TQCVDCreditSecurityNameType	SecurityName;
		///融资数量
		TQCVDCreditVolumeType	FinancingVolume;
		///融资金额
		TQCVDCreditMoneyType	FinancingAmount;
		///融券数量
		TQCVDCreditVolumeType	SecurityLoanVolume;
		///融券金额
		TQCVDCreditMoneyType	SecurityLoanAmount;
		///负债本金
		TQCVDCreditMoneyType	DebtAmount;
		///还款金额
		TQCVDCreditMoneyType	RepaymentAmount;
		///负债数量
		TQCVDCreditVolumeType	DebtVolume;
		///还券数量
		TQCVDCreditVolumeType	RepaySecurityVolume;
		///融资费用
		TQCVDCreditMoneyType	FinancingFee;
		///融券费用
		TQCVDCreditMoneyType	SecurityLoanFee;
		///变动日期
		TQCVDCreditDateType	ChangeDate;
		///利息基数
		TQCVDCreditMoneyType	InterestBase;
		///预计利息
		TQCVDCreditMoneyType	PredictInterest;
		///归还利息
		TQCVDCreditMoneyType	BackInterest;
		///币种类型
		TQCVDCreditLocalCurrencyIDType	Currency;
		///利率
		TQCVDCreditPriceType	InterestRate;
		///到期日期
		TQCVDCreditDateType	ExpireDate;
		///权益类别
		TQCVDCreditLocalRightTypeType	RightType;
		///最新市值
		TQCVDCreditMoneyType	NewMarketValue;
		///计息日期
		TQCVDCreditDateType	CalcInterestDate;
		///罚息利息基数
		TQCVDCreditMoneyType	PenaltyInterestBase;
		///罚息预计利息
		TQCVDCreditMoneyType	PenaltyPredictInterest;
		///罚息归还利息
		TQCVDCreditMoneyType	PenaltyBackInterest;
		///融券剩余资金
		TQCVDCreditMoneyType	SecurityLoanRemainAmount;
		///负债状态
		TQCVDCreditDebtStatusype	DebtStatus;
		///保证金比例
		TQCVDCreditMoneyType	MarginRate;
		///占用保证金
		TQCVDCreditMoneyType	MarginOccupied;
		///送股数量
		TQCVDCreditVolumeType	PresentVolume;
		///红利金额
		TQCVDCreditMoneyType	BonusAmount;
		///其他数量
		TQCVDCreditVolumeType	OtherVolume;
		///其他金额
		TQCVDCreditMoneyType	OtherAmount;
		///累计盈亏
		TQCVDCreditMoneyType	TotalProfit;
		///委托有效日期
		TQCVDCreditDateType	EntrustValidDate;
		///待偿还数量
		TQCVDCreditVolumeType	OutstandingVolume;
		///利息费用新增
		TQCVDCreditMoneyType	NewInterestFee;
		///罚息新增
		TQCVDCreditMoneyType	NewInterestPenalty;
		///还款利息
		TQCVDCreditMoneyType	RepaymentInterest;
		///计息截止日期
		TQCVDCreditDateType	CalcInterestEndDate;
		///折扣券编号
		TQCVDCreditDiscountCouponNoType	DiscountCouponNo;
		///折扣系数
		TQCVDCreditMoneyType	DiscountCouponfactor;
		///罚息利率
		TQCVDCreditMoneyType	PenaltyInterestRate;
		///罚息分段利息
		TQCVDCreditMoneyType	PenaltySegmentInterest;
		///期初合约利息
		TQCVDCreditMoneyType	OpeningContractInterest;
		///期初罚息金额
		TQCVDCreditMoneyType	OpeningPenaltyInterestAmount;
		///期初合约数量
		TQCVDCreditVolumeType	OpeningContractNum;
		///委托价格
		TQCVDCreditPriceType	TradePrice;
		///合约编号
		TQCVDCreditHYBHType	ContractNo;
		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
		///总页数
		TQCVDCreditPageLocateType	PageTotal;
	};

	/// 请求检查客户登录权限
	struct CQCVDReqCheckLoginRightField
	{
		///登录账户
		TQCVDLogInAccountType	LogInAccount;

		///密码(密码认证时必填)
		TQCVDPasswordType	Password;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///内网IP地址
		TQCVDIPAddressType	InnerIPAddress;

		///认证方式(指纹或钥匙串认证时必填)
		TQCVDAuthModeType	AuthMode;

		///设备标识(指纹认证时必填)
		TQCVDDeviceIDType	DeviceID;

		///认证序列号(指纹或钥匙串认证时必填)
		TQCVDCertSerialType	CertSerial;

		///外网IP地址
		TQCVDIPAddressType	OuterIPAddress;
	};

	/// 查询申万指数行情
	struct CQCVDReqQrySWSIndexDataField
	{
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///指数代码
		TQCVDIndexIDType	IndexID;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};
	/// 申万指数行情
	struct CQCVDSWSIndexDataField
	{
		///指数代码
		TQCVDIndexIDType	IndexID;
		///指数中文名称
		TQCVDConceptionCodeType	IndexName;
		///交易日期
		TQCVDDateType	TradingDay;
		///昨收盘价
		TQCVDPriceType	PreClosePrice;
		///开盘价
		TQCVDPriceType	OpenPrice;
		///最高价
		TQCVDPriceType	HighPrice;
		///最低价
		TQCVDPriceType	LowPrice;
		///收盘价
		TQCVDPriceType	ClosePrice;
		///成交量(百股)
		TQCVDLargeVolumeType	Volume;
		///成交金额(千元)
		TQCVDLargeVolumeType	Amount;
		///指数市盈率
		TQCVDRatioType	PERatio;
		///指数市净率
		TQCVDRatioType	PBRatio;
		///A股流通市值(万元)
		TQCVDMoneyType	FloatMV;
		///总市值(万元)
		TQCVDMoneyType	TotalMV;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;
	};

	///股票日K行情请求包
	struct CQCVDReqQryStockDayQuotationField
	{
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///交易所代码
		TQCVDExchangeIDType	 ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};
	///股票日K行情应答包
	struct CQCVDStockDayQuotationField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///交易日期
		TQCVDDateType	TradingDay;
		///币种类型
		TQCVDCurrencyIDType Currency;
		///昨收盘价(元)
		TQCVDPriceType PreClosePrice;
		///开盘价(元)
		TQCVDPriceType OpenPrice;
		///最高价(元)
		TQCVDPriceType HighPrice;
		///最低价(元)
		TQCVDPriceType LowPrice;
		///收盘价(元)
		TQCVDPriceType ClosePrice;
		///涨跌(元)
		TQCVDPriceType Change;
		///涨跌幅(%)
		TQCVDRatioType PercentChange;
		///成交量(手)
		TQCVDMoneyType	Volume;
		///成交金额(千元)
		TQCVDMoneyType	Turnover;
		///复权昨收盘价(元)
		TQCVDPriceType AdjustPreClosePrice;
		///复权开盘价(元)
		TQCVDPriceType AdjustOpenPrice;
		///复权最高价(元)
		TQCVDPriceType AdjustHighPrice;
		///复权最低价(元)
		TQCVDPriceType AdjustLowPrice;
		///复权收盘价(元)
		TQCVDPriceType AdjustClosePrice;
		///复权因子
		TQCVDRatioType AdjustFactor;
		///均价(VWAP)
		TQCVDPriceType AveragePrice;
		///交易状态
		TQCVDPbuIDType TradeStatus;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;
		///涨停价(元)
		TQCVDPriceType	LimitPrice;
		///跌停价(元)
		TQCVDPriceType	StoppingPrice;
		///前复权收盘价(元)
		TQCVDPriceType	AdjCloseBackwardPrice;
	};
	///A股交易日历请求包
	struct CQCVDReqQryShareCalendarField
	{
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};
	///A股交易日历应答包
	struct CQCVDShareCalendarField
	{
		///交易日期
		TQCVDDateType	TradingDay;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;
	};
	///申万行业分类请求包
	struct CQCVDReqQrySWIndustriesClassField
	{
		///生效日期(该日期大于纳入日期，小于剔除日期)
		TQCVDDateType	TradingDay;
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///申万行业代码
		TQCVDSecurityIDType	SWIndustryCode;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};
	///申万行业分类应答包
	struct CQCVDSWIndustriesClassField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///申万行业代码
		TQCVDSecurityIDType	SWIndustryCode;
		///申万行业名称
		TQCVDSecurityIDType	SWIndustryName;
		///纳入日期
		TQCVDDateType	EntryDate;
		///剔除日期
		TQCVDDateType	RemoveDate;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;
	};

	///A股日行情估值指标请求包
	struct CQCVDReqQryStockAssessIndicatorField
	{
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///交易所代码
		TQCVDExchangeIDType	 ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};
	///A股日行情估值指标应答包
	struct CQCVDStockAssessIndicatorField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///交易日期
		TQCVDDateType	TradingDay;
		///币种类型
		TQCVDCurrencyIDType Currency;
		///当日总市值
		TQCVDMoneyType	TotalMV;
		///当日流通市值
		TQCVDMoneyType	FloatMV;
		///52周最高价
		TQCVDPriceType HighPrice_52W;
		///52周最低价
		TQCVDPriceType LowPrice_52W;
		///市盈率(PE)
		TQCVDRatioType	PERatio;
		///市净率(PB)
		TQCVDRatioType	PBRatio;
		///市盈率(PE,TTM))
		TQCVDPriceType PE_TTMRatio;
		///市现率(PCF,经营现金流)
		TQCVDPriceType PCF_OCFRatio;
		///市现率(PCF,经营现金流TTM)
		TQCVDPriceType PCF_OCFTTMRatio;
		///市现率(PCF,现金净流量)
		TQCVDPriceType PCF_NCFRatio;
		///市现率(PCF,现金净流量TTM)
		TQCVDPriceType PCF_NCFTTMRatio;
		///市销率(PS)
		TQCVDRatioType PSRatio;
		///市销率(PS,TTM)
		TQCVDMoneyType	PS_TTMRatio;
		///换手率
		TQCVDMoneyType	TurnRatio;
		///换手率(基准.自由流通股本)
		TQCVDPriceType FreeTurnOverRatio;
		///当日总股本
		TQCVDShareType	TotalShareToday;
		///当日流通股本
		TQCVDShareType	FloatShareToday;
		///当日收盘价
		TQCVDPriceType ClosePrice;
		///股价/每股派息
		TQCVDCashShareType	PriceDivDps;
		///52周最高价(复权)
		TQCVDPriceType AdjustHighPrice_52W;
		///52周最低价(复权)
		TQCVDPriceType AdjustLowPrice_52W;
		///当日自由流通股本
		TQCVDPriceType FreeShareToday;
		///归属母公司净利润(TTM)
		TQCVDPriceType NetProfitParentCompTTM;
		///归属母公司净利润(LYR)
		TQCVDPriceType NetProfitParentCompLYR;
		///当日净资产
		TQCVDRatioType NstAssetsToday;
		///经营活动产生的现金流量净额(TTM)
		TQCVDPriceType NetCashFlowsOperActTTM;
		///经营活动产生的现金流量净额(LYR)
		TQCVDPriceType NetCashFlowsOperActLYR;
		///营业收入(TTM)
		TQCVDRatioType EvrTTM;
		///营业收入(LYR)
		TQCVDPriceType EvrLYR;
		///现金及现金等价物净增加额(TTM)
		TQCVDPriceType NetIncrCashEquTTM;
		///现金及现金等价物净增加额(LYR)
		TQCVDRatioType NetIncrCashEquLYR;
		///涨跌停状态
		TQCVDFunctionIDType UpDownLimitStatus;
		///最高最低价状态
		TQCVDFunctionIDType LowestHighestStatus;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;
	};
	///一致预测个股滚动指标请求包
	struct CQCVDReqQryConsensusRollingDataField
	{
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///交易所代码
		TQCVDExchangeIDType	 ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///滚动指标类型 (查询多个滚动指标类型，需使用|分隔符进行拼接，FY0|FY1|FY2|)
		TQCVDRollingType RollingType;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};
	///一致预测个股滚动指标应答包
	struct CQCVDConsensusRollingDataField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///交易日期
		TQCVDDateType	TradingDay;
		///滚动指标类型
		TQCVDRollingType RollingType;
		///净利润
		TQCVDMoneyType	NetProfit;
		///每股收益
		TQCVDMoneyType	EPS;
		///市盈率
		TQCVDRatioType	PERatio;
		///PEG
		TQCVDRatioType	PEGRatio;
		///市净率(PB)
		TQCVDRatioType	PBRatio;
		///净资产收益率
		TQCVDPriceType ROERatio;
		///营业收入
		TQCVDPriceType OperRevenue;
		///每股现金流
		TQCVDRatioType	CFPS;
		///每股股利
		TQCVDRatioType	DPS;
		///每股净资产
		TQCVDPriceType BPS;
		///息税前利润
		TQCVDPriceType EBIT;
		///	息税折旧摊销前利润
		TQCVDPriceType EBITDA;
		///利润总额
		TQCVDPriceType TotalProfit;
		///营业利润
		TQCVDPriceType OperProfit;
		///营业成本及附加
		TQCVDRatioType OperCost;
		///	基准年度
		TQCVDDateType BenchmarkYear;
		///预测基准股本综合值
		TQCVDMoneyType	BaseShare;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;
	};
	///指数日行情请求包
	struct CQCVDReqQryIndexDayQuotationField
	{
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///指数代码
		TQCVDSecurityIDType	IndexCode;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};
	///指数日行情应答包
	struct CQCVDIndexDayQuotationField
	{
		///指数代码
		TQCVDSecurityIDType	IndexCode;
		///交易日期
		TQCVDDateType	TradingDay;
		///币种类型
		TQCVDCurrencyIDType Currency;
		///昨收盘价(点)
		TQCVDPriceType PreClosePrice;
		///开盘价(点)
		TQCVDPriceType OpenPrice;
		///最高价(点)
		TQCVDPriceType HighPrice;
		///最低价(点)
		TQCVDPriceType LowPrice;
		///收盘价(点)
		TQCVDPriceType ClosePrice;
		///涨跌(点)
		TQCVDPriceType Change;
		///涨跌幅(%)
		TQCVDRatioType PercentChange;
		///成交量(手)
		TQCVDMoneyType	Volume;
		///成交金额(千元)
		TQCVDMoneyType	Turnover;
		///证券ID
		TQCVDSecurityIDType SecID;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;
	};
	///A股基本资料请求包
	struct CQCVDReqQryShareDescriptionField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};
	///A股基本资料应答包
	struct CQCVDShareDescriptionField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///证券简称
		TQCVDInvestorNameType	SecurityName;
		///公司中文名称
		TQCVDShareContentType	CompanyName;
		///公司英文名称
		TQCVDShareContentType	CompanyNameEnglish;
		///ISIN代码
		TQCVDNameType	ISINCode;
		///上市板类型
		TQCVDUserIDType	ListBoard;
		///上市日期
		TQCVDDateType	ListDate;
		///退市日期
		TQCVDDateType	DeListDate;
		///币种类型
		TQCVDCurrencyIDType Currency;
		///简称拼音
		TQCVDSecurityIDType	PinYin;
		///上市板
		TQCVDSecurityIDType	ListBoardName;
		///是否在沪股通或深港通范围内
		TQCVDShortsInfoType	SHSC;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;
	};

	/// 十大股东信息请求包
	struct CQCVDReqQryTopTenHoldersDetailField
	{
		///起始公告日期
		TQCVDDateType	BegAnnDate;
		///结束公告日期
		TQCVDDateType	EndAnnDate;
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///排列方式 1-正序 2-倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 十大股东数据应答包
	struct CQCVDTopTenHoldersDetailField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///公告日期
		TQCVDDateType	AnnDate;
		///截止日期
		TQCVDDateType	HolderEndDate;
		///股东类型
		TQCVDHolderCategoryType	HolderCategory;
		///股东名称
		TQCVDHolderNameType	HolderName;
		///持股数量
		TQCVDQuantityType	HolderQuantity;
		///持股比例
		TQCVDRatioType	HolderPercent;
		///持有限售股份（非流通股）数量
		TQCVDQuantityType	HolderRestrictedQuantity;
		/// 持股性质
		TQCVDHolderNameType HolderShareCategoryName;
		/// 股东说明
		TQCVDHolderNameType HolderMemo;
		///报告期
		TQCVDDateType	ReportPeriod;
		///股东性质
		TQCVDWindCodeType HolderNature;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;
	};

	///客户历史周期股票盈亏请求包
	struct CQCVDReqQryHisShareProfitField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
	};

	///客户历史周期股票盈亏应答包
	struct CQCVDHisShareProfitField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///期初市值
		TQCVDPriceType	MarketValueBeg;
		///期末市值
		TQCVDPriceType	MarketValueEnd;
		///开仓金额
		TQCVDPriceType	OpenAmount;
		///平仓金额
		TQCVDPriceType	CloseAmount;
		///盈亏
		TQCVDPriceType	TotalProfit;
		///实际结束日期
		TQCVDDateType	RealEndDate;
		///期初持仓
		TQCVDVolumeType	BegPosition;
		///期末持仓
		TQCVDVolumeType	EndPosition;
	};

	///根据客户号查询客户资金账户请求包
	struct CQCVDReqQryAccountIDByInvestorIDField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
	};

	///根据客户号查询客户资金账户应答包
	struct CQCVDAccountIDByInvestorIDField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///投资者资金帐号
		TQCVDAccountIDType	AccountID;
	};

	///客户周期资金请求包
	struct CQCVDReqQryCustPeriodCapiDataField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///周期类型('1'-日期YYYYMMDD，'2'-月份YYYYMM)
		TQCVDPeroidType	PeroidType;
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
	};

	///客户周期资金应答包
	struct CQCVDCustPeriodCapiDataField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///交易周期
		TQCVDDateType	PeroidDate;
		///总资产
		TQCVDPriceType	TotalAsset;
		///入金
		TQCVDPriceType	MoneyIn;
		///出金
		TQCVDPriceType	MoneyOut;
		///盈亏
		TQCVDPriceType	PeroidProfit;
		///是否基准日
		TQCVDStandardType	StandardType;
		///盈亏
		TQCVDPriceType	PeroidProfitEx;
	};

	///客户周期盈亏请求包
	struct CQCVDReqQryCustPeriodProfitDataField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///排列方式 1-盈亏正序 2-盈亏倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	///客户周期盈亏应答包
	struct CQCVDCustPeriodProfitDataField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///交易所代码（SH,SZ等）
		TQCVDCurrencyIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///盈亏
		TQCVDPriceType	PeroidProfit;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;
		///盈亏
		TQCVDPriceType	PeroidProfitEx;
	};

	///中国A股发行请求包
	struct CQCVDReqQryShareIssuanceField
	{
		///起始申购日期（网上发行Online）
		TQCVDDateType	BegApplyDate;
		///结束申购日期（网上发行Online）
		TQCVDDateType	EndApplyDate;
		///起始上市日期（上市ListDate）
		TQCVDDateType	BegListDate;
		///结束上市日期（上市ListDate）
		TQCVDDateType	EndListDate;
		///交易所代码
		TQCVDExchangeIDType	 ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///是否发行失败 0:发行正常1:发行失败2:发行暂缓
		TQCVDIssuanceIsFailureType	IsFailure;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};
	///中国A股发行应答包
	struct CQCVDShareIssuanceField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///证券名称
		TQCVDSecurityNameType	SecurityName;
		///	网上申购代码
		TQCVDSecurityIDType	OnlineCode;
		///网上发行申购名称
		TQCVDSecurityNameType	OnlineName;
		///网上发行日期
		TQCVDDateType	OnlineDate;
		///网上发行申购价格
		TQCVDMoneyType	OnlinePrice;
		///个人申购上限（股）
		TQCVDVolumeType	PurchaseUpLimit;
		///上市日期
		TQCVDDateType	ListDate;
		///上市板块
		TQCVDIPONumberIDType	ListBoardName;
		///公告日期
		TQCVDDateType	AnnDate;
		///是否发行失败 0:发行正常1:发行失败2:发行暂缓
		TQCVDIssuanceIsFailureType	IsFailure;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;
		///发行市盈率 空:无数据
		TQCVDPEValueStrType	IPODilutedPE;
		///行业市盈率 空:无数据
		TQCVDPEValueStrType	ValPE;
		///是否盈利 
		TQCVDIsProfitType	IsProfit;
		///是否协议控制
		TQCVDIsVIEType	IsVIE;
		///是否存在表决权差异
		TQCVDVoteRightDiffType	VoteRightDiff;
		///是否注册制
		TQCVDIsRegType	IsReg;
		///所属行业 空:无数据
		TQCVDIndustriesNameType	IndustriesName;
		///主营业务 空:无数据
		TQCVDMainBusinessType	InfoMainBusiness;
		///网上发行中签率(%) 空:无数据
		TQCVDPEValueStrType	IPOCashRatio;
		///网上中签结果公告日 空:无数据
		TQCVDDateType	FellowUnfrozeDate;
		///公开及老股发行数量合计(万股) 空:无数据
		TQCVDPEValueStrType	IPOAmount;
		///网上发行数量(万股) 空:无数据
		TQCVDPEValueStrType IPOAmtByPlacing;

	};
	///中国可转债发行请求包
	struct CQCVDReqQryBondIssuanceField
	{
		///起始申购日期（网上发行Online）
		TQCVDDateType	BegApplyDate;
		///结束申购日期（网上发行Online）
		TQCVDDateType	EndApplyDate;
		///起始上市日期（上市ListDate）
		TQCVDDateType	BegListDate;
		///结束上市日期（上市ListDate）
		TQCVDDateType	EndListDate;
		///交易所代码
		TQCVDExchangeIDType	 ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///是否发行失败 0:发行正常1:发行失败2:发行暂缓
		TQCVDIssuanceIsFailureType	IsFailure;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};
	///中国可转债发行应答包
	struct CQCVDBondIssuanceField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///证券名称
		TQCVDSecurityNameType	SecurityName;
		///网上发行日期
		TQCVDDateType	OnlineDate;
		///网上发行申购代码
		TQCVDSecurityIDType	OnlineCode;
		///网上发行申购名称
		TQCVDSecurityNameType	OnlineName;
		///网上发行申购价格
		TQCVDMoneyType	OnlinePrice;
		///个人申购上限（张）
		TQCVDVolumeType	PurchaseUpLimit;
		///老股东配售日期
		TQCVDDateType	RationDate;
		///老股东配售股权登记日
		TQCVDDateType	RationCheckInDate;
		///老股东配售缴款日
		TQCVDDateType	RationPayDate;
		///老股东配售代码
		TQCVDSecurityIDType	RationCode;
		///老股东配售简称
		TQCVDSecurityNameType	RationName;
		///老股东配售价格
		TQCVDMoneyType	RationPrice;
		///老股东配售比例分母
		TQCVDMoneyType	RationRatioDenominator;
		///老股东配售比例分子
		TQCVDMoneyType	RationRatioMolecule;
		///上市日期
		TQCVDDateType	ListDate;
		///公告日期
		TQCVDDateType	AnnDate;
		///是否发行失败 0:发行正常1:发行失败2:发行暂缓
		TQCVDIssuanceIsFailureType	IsFailure;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;
		///正股代码
		TQCVDSecurityIDType	StockID;
		///发行数量(万张)
		TQCVDMoneyType	IssueQuantity;

	};
	///中国A股股权质押信息请求包
	struct CQCVDReqQryShareEquityPledgeInfoField
	{
		///公告开始日期（AnnDate）
		TQCVDDateType	BegAnnDate;
		///公告开始日期（AnnDate）
		TQCVDDateType	EndAnnDate;
		///交易所代码
		TQCVDExchangeIDType	 ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};
	///中国A股股权质押信息应答包
	struct CQCVDShareEquityPledgeInfoField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///公告日期
		TQCVDDateType	AnnDate;
		///网上发行日期
		TQCVDDateType	PledgeBegDate;
		///质押结束时间
		TQCVDDateType	PledgeEndDate;
		///股东名称
		TQCVDHolderNameType	HolderName;
		///质押数量(万股)
		TQCVDMoneyType	PledgeShares;
		///质押方
		TQCVDHolderNameType	Pledgor;
		///解押日期
		TQCVDDateType	DischargeDate;
		///备注
		TQCVDRemarkType	Remark;
		///是否解押
		TQCVDDischargeType	Discharge;
		///股东类型代码
		TQCVDProductInfoType	HolderTypeCode;
		///股东ID
		TQCVDShareholderIDType	HolderID;
		///质押方类型代码
		TQCVDProductInfoType	PledgorTypeCode;
		///质押方ID
		TQCVDShareholderIDType PledgorID;
		///股份性质类别代码
		TQCVDProductInfoType	ShrCategoryCode;
		///持股总数(万股)
		TQCVDMoneyType	TotalHoldingShr;
		///累计质押股数(万股)
		TQCVDMoneyType	TotalPledgeShr;
		///本次质押股数占公司总股本比例
		TQCVDRatioType	PledgeShrRatio;
		///持股总数占公司总股本比例
		TQCVDRatioType	TotalHoldingShrRatio;
		///是否股权质押回购
		TQCVDBoolType	EquityPledgeRepo;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;
	};

	///历史资金请求包
	struct CQCVDReqQryHistoryCapitalField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	///历史资金应答包
	struct CQCVDHistoryCapitalField
	{
		///交易日
		TQCVDDateType	TradingDay;
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///总资产
		TQCVDPriceType	TotalAsset;
		///客户资金
		TQCVDPriceType	CustFund;
		///股票市值
		TQCVDPriceType	StockValue;
		///入金
		TQCVDPriceType	MoneyIn;
		///出金
		TQCVDPriceType	MoneyOut;
		///当日盈亏
		TQCVDPriceType	TodayProfit;
		///上日总资产
		TQCVDPriceType	LastTotalAsset;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;
		///未到期债券市值
		TQCVDPriceType	undueBondValue;
		///基金市值
		TQCVDPriceType	fundMarketValue;
		///OTC资产
		TQCVDPriceType	OTCAsset;
		///特殊基金资产（鑫金宝等在证券市场可用的基金资产，该部分包含在基金市值fundMarketValue里面）
		TQCVDPriceType	SpecFundAsset;
		///理财业务冷静期在途金额
		TQCVDMoneyType	CoolPeriodAmount;
	};

	///历史交割单请求包
	struct CQCVDReqQryHistoryDeliveryField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///委托类型 查询多个委托类型，需使用|分隔符进行拼接，1|2|3|
		TQCVDTerminalInfoType	EntrustType;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	///历史交割应答包
	struct CQCVDHistoryDeliveryField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///成交日期
		TQCVDDateType	TradeDate;
		///成交时间
		TQCVDTimeType	TradeTime;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///证券名称
		TQCVDSecurityNameType SecurityName;
		///交易所代码
		TQCVDCurrencyIDType ExchangeID;
		///交易所名称
		TQCVDSecurityIDType ExchangeName;
		///币种类型
		TQCVDCurrencyIDType Currency;
		///委托类型
		TQCVDCurrencyIDType	EntrustType;
		///委托类型名称
		TQCVDSecurityNameType EntrustTypeName;
		///实付金额
		TQCVDPriceType ActuallyPayMoney;
		///成交金额
		TQCVDPriceType Turnover;
		///成交数量
		TQCVDVolumeType Volume;
		///成交价格
		TQCVDPriceType	Price;
		///印花税
		TQCVDPriceType	StampTax;
		///过户费
		TQCVDPriceType	TransferFee;
		///实收佣金
		TQCVDPriceType	ActualBrokerage;
		///交易规费
		TQCVDPriceType	TransactionFee;
		///流水号
		TQCVDOrderRefType	SerialNo;
		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;
		///本次股份余额
		TQCVDVolumeType StockBalance;
		///本次资金余额
		TQCVDPriceType	FundBalance;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;
		///开仓成本价
		TQCVDPriceType	OpenAvg;
		///对应开仓成本价的平仓盈亏
		TQCVDPriceType	OpenAvgProfit;
		///合同编号
		TQCVDOrderRefType	EntrustNo;
		///委托本地报单编号：如果是买入或者卖出（EntrustType是1或者2）的情况，OrderLocalID是对应的委托报单编号；否则OrderLocalID设置为空字符串
		TQCVDOrderLocalIDType	OrderLocalID;
	};

	///历史资金流水请求包
	struct CQCVDReqQryHistoryFundDetailField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///业务科目 查询多个业务科目，需使用|分隔符进行拼接，10113|10213|
		TQCVDTerminalInfoType	BusinessAccount;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	///历史资金流水应答包
	struct CQCVDHistoryFundDetailField
	{
		///流水号
		TQCVDSerialType	SerialNo;
		///交易日
		TQCVDDateType	TradeDate;
		///入账时间
		TQCVDTimeType	TradeTime;
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///投资者名称
		TQCVDSecurityIDType	InvestorName;
		///资金账户代码
		TQCVDAccountIDType	AccountID;
		///业务科目
		TQCVDDateType	BusinessAccount;
		///业务科目名称
		TQCVDSecurityIDType	BusinessAccountName;
		///收入金额
		TQCVDMoneyType MoneyIn;
		///付出金额
		TQCVDMoneyType MoneyOut;
		///资金余额
		TQCVDMoneyType FundBalance;
		///操作摘要
		TQCVDUserNameType	OperSummary;
		///相关市场
		TQCVDCombOffsetFlagType	RelevantMarket;
		///相关品种
		TQCVDProgressType	RelevantVariety;
		///相关帐号
		TQCVDBankAccountIDType	RelevantAccount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;
	};

	///历史持仓请求包
	struct CQCVDReqQryHistoryHoldField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	///历史持仓应答包
	struct CQCVDHistoryHoldField
	{
		///交易日期
		TQCVDDateType	TradingDay;
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///投资者名称
		TQCVDSecurityIDType	InvestorName;
		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;
		///交易所代码
		TQCVDCurrencyIDType ExchangeID;
		///交易所名称
		TQCVDSecurityIDType ExchangeName;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///币种类型
		TQCVDCurrencyIDType Currency;
		///证券类型
		TQCVDCurrencyIDType	SecurityType;
		///证券类型名称
		TQCVDSecurityNameType SecurityTypeName;
		///数量
		TQCVDVolumeType Volume;
		///非流通数量
		TQCVDVolumeType UncirculatedVolume;
		///持仓成本
		TQCVDPriceType	HoldCost;
		///累计盈亏
		TQCVDPriceType	TotalProfit;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;
		///开仓成本价
		TQCVDPriceType	OpenAvg;
	};

	///历史委托数据请求包
	struct CQCVDReqQryHistoryOrderEXField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDActionFlagType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	///历史委托数据应答包
	struct CQCVDHistoryOrderEXField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///报单引用
		TQCVDOrderRefType	OrderRef;

		///用户代码
		TQCVDUserIDType	UserID;

		///报单价格条件
		TQCVDOrderPriceTypeType	OrderPriceType;

		///买卖方向
		TQCVDDirectionType	Direction;

		///组合开平标志
		TQCVDCombOffsetFlagType	CombOffsetFlag;

		///组合投机套保标志
		TQCVDCombHedgeFlagType	CombHedgeFlag;

		///价格
		TQCVDPriceType	LimitPrice;

		///数量
		TQCVDVolumeType	VolumeTotalOriginal;

		///有效期类型
		TQCVDTimeConditionType	TimeCondition;

		///成交量类型
		TQCVDVolumeConditionType	VolumeCondition;

		///最小成交量
		TQCVDVolumeType	MinVolume;

		///强平原因
		TQCVDForceCloseReasonType	ForceCloseReason;

		///请求编号
		TQCVDRequestIDType	RequestID;

		///本地报单编号
		TQCVDOrderLocalIDType	OrderLocalID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///证券在交易所代码
		TQCVDExchangeInstIDType	ExchangeInstID;

		///交易单元代码
		TQCVDTraderIDType	TraderID;

		///报单提交状态
		TQCVDOrderSubmitStatusType	OrderSubmitStatus;

		///交易日
		TQCVDDateType	TradingDay;

		///系统报单编号
		TQCVDOrderSysIDType	OrderSysID;

		///报单状态
		TQCVDOrderStatusType	OrderStatus;

		///报单类型
		TQCVDOrderTypeType	OrderType;

		///已成交数量
		TQCVDVolumeType	VolumeTraded;

		///剩余未完成数量
		TQCVDVolumeType	VolumeTotal;

		///报单日期
		TQCVDDateType	InsertDate;

		///报单时间
		TQCVDTimeType	InsertTime;

		///撤销时间
		TQCVDTimeType	CancelTime;

		///最后修改交易单元代码
		TQCVDTraderIDType	ActiveTraderID;

		///前置编号
		TQCVDFrontIDType	FrontID;

		///会话编号
		TQCVDSessionIDType	SessionID;

		///用户端产品信息
		TQCVDProductInfoType	UserProductInfo;

		///状态信息
		TQCVDErrorMsgType	StatusMsg;

		///用户强评标志
		TQCVDBoolType	UserForceClose;

		///操作用户代码
		TQCVDUserIDType	ActiveUserID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///IP地址
		TQCVDIPAddressType	IPAddress;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///港股通订单数量类型,默认整股买卖
		TQCVDLotTypeType	LotType;

		///长字符串附加信息
		TQCVDBigsInfoType	BInfo;

		///短字符串附加信息
		TQCVDShortsInfoType	SInfo;

		///整形附加信息
		TQCVDIntInfoType	IInfo;

		///转入交易单元代码(仅在转托管操作时有效)
		TQCVDPbuIDType	TransfereePbuID;

		///委托方式
		TQCVDOperwayType	Operway;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///适当性控制业务类别
		TQCVDProperCtrlBusinessTypeType	ProperCtrlBusinessType;

		///适当性控制通过标示
		TQCVDProperCtrlPassFlagType	ProperCtrlPassFlag;

		///条件检查
		TQCVDCondCheckType	CondCheck;

		///是否预埋
		TQCVDBoolType	IsCacheOrder;

		///成交金额
		TQCVDMoneyType	Turnover;

		///回报附加浮点型数据信息
		TQCVDFloatInfoType	RtnFloatInfo;

		///回报附加整型数据
		TQCVDIntInfoType	RtnIntInfo;

		///硬盘序列号
		TQCVDHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDMobileType	Mobile;

		///有效日期
		TQCVDDateType	GTDate;

		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;
	};

	///单日历史委托数据请求包
	struct CQCVDReqQryOneDayHistoryOrderField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///查询日期
		TQCVDDateType	TradingDay;
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///唯一序号（如果SeqNo设置为非0值，则查询历史委托的序号>=SeqNo;而如果SeqNo设置为0值，则SeqNo不作为查询条件）
		TQCVDLongVolumeType	SeqNo;
		///本次查询最大返回查询结果数量（如果MaxQryResultCount设置为非0值，则查询委托结果数量最多为MaxQryResultCount;而如果SeqNo设置为0值，则返回所有查询结果）
		TQCVDLongVolumeType	MaxQryResultCount;
		///用户自定义的附加查询条件字段
		TQCVDQryUserDataInfoType AdditionalUserData;
	};

	///单日历史委托数据应答包
	struct CQCVDOneDayHistoryOrderField
	{
		///唯一序号
		TQCVDLongVolumeType	SeqNo;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///报单引用
		TQCVDOrderRefType	OrderRef;

		///用户代码
		TQCVDUserIDType	UserID;

		///报单价格条件
		TQCVDOrderPriceTypeType	OrderPriceType;

		///买卖方向
		TQCVDDirectionType	Direction;

		///组合开平标志
		TQCVDCombOffsetFlagType	CombOffsetFlag;

		///组合投机套保标志
		TQCVDCombHedgeFlagType	CombHedgeFlag;

		///价格
		TQCVDPriceType	LimitPrice;

		///数量
		TQCVDVolumeType	VolumeTotalOriginal;

		///有效期类型
		TQCVDTimeConditionType	TimeCondition;

		///成交量类型
		TQCVDVolumeConditionType	VolumeCondition;

		///最小成交量
		TQCVDVolumeType	MinVolume;

		///强平原因
		TQCVDForceCloseReasonType	ForceCloseReason;

		///请求编号
		TQCVDRequestIDType	RequestID;

		///本地报单编号
		TQCVDOrderLocalIDType	OrderLocalID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///证券在交易所代码
		TQCVDExchangeInstIDType	ExchangeInstID;

		///交易单元代码
		TQCVDTraderIDType	TraderID;

		///报单提交状态
		TQCVDOrderSubmitStatusType	OrderSubmitStatus;

		///交易日
		TQCVDDateType	TradingDay;

		///系统报单编号
		TQCVDOrderSysIDType	OrderSysID;

		///报单状态
		TQCVDOrderStatusType	OrderStatus;

		///报单类型
		TQCVDOrderTypeType	OrderType;

		///已成交数量
		TQCVDVolumeType	VolumeTraded;

		///剩余未完成数量
		TQCVDVolumeType	VolumeTotal;

		///报单日期
		TQCVDDateType	InsertDate;

		///报单时间
		TQCVDTimeType	InsertTime;

		///撤销时间
		TQCVDTimeType	CancelTime;

		///最后修改交易单元代码
		TQCVDTraderIDType	ActiveTraderID;

		///前置编号
		TQCVDFrontIDType	FrontID;

		///会话编号
		TQCVDSessionIDType	SessionID;

		///用户端产品信息
		TQCVDProductInfoType	UserProductInfo;

		///状态信息
		TQCVDErrorMsgType	StatusMsg;

		///用户强评标志
		TQCVDBoolType	UserForceClose;

		///操作用户代码
		TQCVDUserIDType	ActiveUserID;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///IP地址
		TQCVDIPAddressType	IPAddress;

		///Mac地址
		TQCVDMacAddressType	MacAddress;

		///港股通订单数量类型,默认整股买卖
		TQCVDLotTypeType	LotType;

		///长字符串附加信息
		TQCVDBigsInfoType	BInfo;

		///短字符串附加信息
		TQCVDShortsInfoType	SInfo;

		///整形附加信息
		TQCVDIntInfoType	IInfo;

		///转入交易单元代码(仅在转托管操作时有效)
		TQCVDPbuIDType	TransfereePbuID;

		///委托方式
		TQCVDOperwayType	Operway;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///适当性控制业务类别
		TQCVDProperCtrlBusinessTypeType	ProperCtrlBusinessType;

		///适当性控制通过标示
		TQCVDProperCtrlPassFlagType	ProperCtrlPassFlag;

		///条件检查
		TQCVDCondCheckType	CondCheck;

		///是否预埋
		TQCVDBoolType	IsCacheOrder;

		///成交金额
		TQCVDMoneyType	Turnover;

		///回报附加浮点型数据信息
		TQCVDFloatInfoType	RtnFloatInfo;

		///回报附加整型数据
		TQCVDIntInfoType	RtnIntInfo;

		///硬盘序列号
		TQCVDHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDMobileType	Mobile;

		///有效日期
		TQCVDDateType	GTDate;
	};

	///历史成交请求包
	struct CQCVDReqQryHistoryTradeEXField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDActionFlagType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	///历史成交应答包
	struct CQCVDHistoryTradeEXField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///用户代码
		TQCVDUserIDType	UserID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///成交编号
		TQCVDTradeIDType	TradeID;

		///买卖方向
		TQCVDDirectionType	Direction;

		///系统报单编号
		TQCVDOrderSysIDType	OrderSysID;

		///市场代码
		TQCVDMarketIDType	MarketID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///证券在交易所代码
		TQCVDExchangeInstIDType	ExchangeInstID;

		///开平标志
		TQCVDOffsetFlagType	OffsetFlag;

		///投机套保标志
		TQCVDHedgeFlagType	HedgeFlag;

		///成交价格
		TQCVDPriceType	Price;

		///成交数量
		TQCVDVolumeType	Volume;

		///成交日期
		TQCVDDateType	TradeDate;

		///成交时间
		TQCVDTimeType	TradeTime;

		///交易单元代码
		TQCVDTraderIDType	TraderID;

		///本地报单编号
		TQCVDOrderLocalIDType	OrderLocalID;

		///交易日
		TQCVDDateType	TradingDay;

		///投资单元代码
		TQCVDBusinessUnitIDType	BusinessUnitID;

		///资金账户代码
		TQCVDAccountIDType	AccountID;

		///报单引用
		TQCVDOrderRefType	OrderRef;

		///一级机构代码
		TQCVDDepartmentIDType	DepartmentID;

		///实收佣金
		TQCVDPriceType	ActualBrokerage;

		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;
	};

	///计算期权希腊值请求包
	struct CQCVDReqOptionGreeceField
	{
		///交易所代码（必输）
		TQCVDExchangeIDType	ExchangeID;
		///证券代码（必输）
		TQCVDSecurityIDType	SecurityID;
		///期权权利金(不上传则使用期权权益金)
		TQCVDPriceType	Premium;
		///标的物价格(不上传则使用标的物最新价)
		TQCVDPriceType	UnderlyingSecurityPrice;
		///执行价(不上传则使用该期权行权价)
		TQCVDPriceType	StrikePrice;
		///无风险利率(不上传则使用shibor一年期利率)
		TQCVDRatioType	RiskFreeInterestRate;
		///隐含波动率(不上传则通过该期权权益金、标的物最新价、行权价、剩余时间等参数计算)
		TQCVDRatioType	ImpliedVolatility;
	};
	///计算期权希腊值应答包
	struct CQCVDRspOptionGreeceField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///期权类型
		TQCVDSPOptionsTypeType	OptionsType;
		///期权权利金
		TQCVDPriceType	Premium;
		///标的物的最新价
		TQCVDPriceType	UnderlyingSecurityPrice;
		///执行价
		TQCVDPriceType	StrikePrice;
		///无风险利率
		TQCVDPriceType	RiskFreeInterestRate;
		///隐含波动率
		TQCVDRatioType	ImpliedVolatility;

		///内在价值
		TQCVDPriceType	IntrinsicValue;
		///时间价值
		TQCVDPriceType	TimeValue;
		///杠杆率
		TQCVDRatioType	Leverage;
		///真实杠杆率
		TQCVDRatioType	RealLeverage;
		///Delta(衡量标的资产价格变动时，期权价格的变化幅度)
		TQCVDRatioType	Delta;
		///Gamma(衡量标的资产价格变动时，期权Delta值的变化幅度)
		TQCVDRatioType	Gamma;
		///Theta(衡量随着时间的消逝，期权价格的变化幅度)
		TQCVDRatioType	Theta;
		///Vega(衡量标的资产价格波动率变动时，期权价格的变化幅度)
		TQCVDRatioType	Vega;
		///Rho(衡量利率变动时，期权价格的变化幅度)
		TQCVDRatioType	Rho;
	};

	///查询债券日K行情请求
	struct CQCVDQryBondDayQuotationField
	{
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///交易所代码
		TQCVDExchangeIDType	 ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	///债券日K行情
	struct CQCVDBondDayQuotationField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///交易日
		TQCVDDateType	TradingDay;

		///货币代码
		TQCVDCodeType CurrencyID;

		///昨收盘价
		TQCVDPriceType	PreClosePrice;

		///开盘价
		TQCVDPriceType	OpenPrice;

		///最高价
		TQCVDPriceType	HighestPrice;

		///最低价
		TQCVDPriceType	LowestPrice;

		///收盘价
		TQCVDPriceType	ClosePrice;

		///涨跌(元)
		TQCVDPriceType	PriceChange;

		///涨跌幅(%)
		TQCVDRatioType	PriceChangePercentage;

		///成交量(手):上海、深圳交易所债券，1手=10张=1000元
		TQCVDLargeVolumeType	Volume;

		///成交金额
		TQCVDMoneyType	Amount;

		///均价(VWAP)
		TQCVDPriceType	AveragePrice;

		///交易状态
		TQCVDTradeStatusType	TradeStatus;

		///页定位符
		TQCVDPageLocateType	PageLocate;

		///总页数
		TQCVDPageLocateType	PageTotal;
	};

	///查询港股通资金流向信息请求
	struct CQCVDQryGGTEODPricesField
	{
		///港股通市场方向, 1:沪股通 2:深股通 3:港股通(沪) 4:港股通(深)
		TQCVDGGTMarketIDType	market;
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	///港股通资金流向信息
	struct CQCVDGGTEODPricesField
	{
		///序号
		TQCVDLongSequenceType	ID;

		///港股通市场方向, 1:沪股通 2:深股通 3:港股通(沪) 4:港股通(深)
		TQCVDGGTMarketIDType	market;

		///今已用,资金净流入
		TQCVDMoneyType	dayNetAmtIn;

		///今剩余,当日余额
		TQCVDMoneyType	dayAmtRemain;

		///今额度
		TQCVDMoneyType dayAmtThreshold;

		///成交额,净买额
		TQCVDPriceType	netBuyAmt;

		///成交额,净卖额
		TQCVDPriceType	netSellAmt;

		///涨
		TQCVDVolumeType	rise;

		///平
		TQCVDVolumeType	flat;

		///跌
		TQCVDVolumeType	fall;

		///领涨股名称
		TQCVDSecurityIDType	leadingStock;

		///股票价格
		TQCVDPriceType	leadingStockPrice;

		///涨跌幅(%)
		TQCVDRatioType	leadingStockPercent;

		///交易日
		TQCVDDateType	TradingDay;

		///交易时间
		TQCVDTimeType	UpdateTime;

		///领涨股代码
		TQCVDSecurityIDType	leadingStockCode;

		///页定位符
		TQCVDPageLocateType	PageLocate;

		///总页数
		TQCVDPageLocateType	PageTotal;
	};

	/// 财务指标数据
	struct CQCVDFinancialIndicatorDataField
	{
		///公告日期
		TQCVDDateType	AnnouncementDate;
		///	报告期
		TQCVDDateType	ReportPeriod;
		///基本每股收益
		TQCVDPriceType	EPSBasic;
		///每股净资产
		TQCVDPriceType	BPS;
		///每股资本公积
		TQCVDPriceType	SurplusCapitalPS;
		///每股未分配利润
		TQCVDPriceType	UndistributedPS;
		///每股经营活动产生的现金流量净额
		TQCVDPriceType	OCFPS;
		///净资产收益率
		TQCVDPriceType	ROE;
		///总资产净利润
		TQCVDPriceType	ROA;
		///每股营业收入
		TQCVDPriceType	ORPS;
		///营业总收入
		TQCVDPriceType	TotOperRev;
		///营业利润
		TQCVDPriceType	OperProfit;
		///利润总额
		TQCVDPriceType	TotProfit;
		///净利润(含少数股东损益)
		TQCVDPriceType	NetProfitInclMinIntInc;
		///净利润(不含少数股东损益)
		TQCVDPriceType	NetProfitExclMinIntInc;
		///资产总计
		TQCVDPriceType	Tot_Assets;
		///负债合计
		TQCVDPriceType	Tot_Liab;
		///少数股东权益
		TQCVDPriceType	MinorityInt;
		///股东权益合计(不含少数股东权益)
		TQCVDPriceType	TotShrhldrEqyExclMinInt;
		///股东权益合计(含少数股东权益)
		TQCVDPriceType	TotShrhldrEqyInclMinInt;
		///负债及股东权益总计
		TQCVDPriceType	TotLiabShrhldrEqy;
		///经营活动产生的现金流量净额
		TQCVDPriceType	NetCashFlowsOperAct;
		///投资活动产生的现金流量净额
		TQCVDPriceType	NetCashFlowsInvAct;
		///筹资活动产生的现金流量净额
		TQCVDPriceType	NetCashFlowsFncAct;
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 公司资料数据
	struct CQCVDCompanyDescriptionDataField
	{
		///公司中文名称
		TQCVDCompNameType	CompName;

		///上市日期
		TQCVDDateType	ListDate;

		///公司中文简介
		TQCVDChineseIntroductionType	ChineseIntroduction;

		///经营范围
		TQCVDBusinessScopeType	BusinessScope;

		///办公地址
		TQCVDOfficeType		Office;

		///发行价格(元)
		TQCVDIPOPriceType	IPOPrice;

		///公开及老股发行数量合计(万股)
		TQCVDQuantityType	IPOAmount;

		///网上发行数量(万股)
		TQCVDShareType	AMTByPlacing;

		///网下发行数量(万股)
		TQCVDShareType	AMTToJur;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 股本结构数据
	struct CQCVDEquityStructureDataField
	{
		///交易日期
		TQCVDDateType	TradingDay;

		///当日总股本
		TQCVDShareType	TotalShareToday;

		///当日流通股本
		TQCVDShareType	FloatShareToday;

		///股东总户数
		TQCVDHolderNumType	HolderTotalNum;

		///最新公告日期
		TQCVDDateType	AnnouncementDate;

		///截止日期
		TQCVDDateType	HolderEndDate;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 行业板块成分股数据
	struct CQCVDIndustryConstituentsDataField
	{
		///Wind行业代码
		TQCVDIndustriesNameType	WindIndCode;

		///行业指数代码
		TQCVDIndexIDType	IndexID;

		///纳入日期
		TQCVDDateType	EntryDate;

		///剔除日期
		TQCVDDateType	RemoveDate;

		///最新标志
		TQCVDCurSignType	CurSign;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 概念板块成分股数据
	struct CQCVDConceptionConstituentsDataField
	{
		///Wind概念代码
		TQCVDIndustriesNameType	WindSecCode;

		///概念指数代码
		TQCVDIndexIDType	IndexID;

		///纳入日期
		TQCVDDateType	EntryDate;

		///剔除日期
		TQCVDDateType	RemoveDate;

		///最新标志
		TQCVDCurSignType	CurSign;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 原始字节数据传输结构
	struct CQCVDRawContentDataField
	{
		///长度
		TQCVDRawContentDataLenType  DataLen;
		///原始字节数据
		TQCVDRawContentDataType	RawContentData;
	};

	/// 查询中国可转债转股价格请求
	struct CQCVDQryCBondConvPriceField
	{
		///公告起始日期
		TQCVDDateType	BegDate;

		///公告结束日期
		TQCVDDateType	EndDate;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///股票代码
		TQCVDSecurityIDType	SecurityID;

		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};
	/// 查询中国可转债转股价格请求
	struct CQCVDCBondConvPriceField
	{
		///公告日期
		TQCVDDateType announcementDate;
		///变动原因
		TQCVDChangeReasonType changeReason;
		///转股价格
		TQCVDPriceType convPrice;
		///截止日期
		TQCVDDateType endDate;
		///交易所编码
		TQCVDExchangeIDType exchangeID;
		///页定位符
		TQCVDPageLocateType pageLocate;
		///总页数
		TQCVDPageLocateType pageTotal;
		///股票代码
		TQCVDSecurityIDType securityID;
	};
	/// 查询上市基金日行情/中国封闭式基金日行情请求
	struct CQCVDQryChinaClosedFundField
	{
		///起始日期
		TQCVDDateType BegDate;
		///结束日期
		TQCVDDateType EndDate;
		///基金编号
		TQCVDSecurityIDType FundID;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType OrderType;
		///每页记录数
		TQCVDVolumeType PageCount;
		///页定位符
		TQCVDPageLocateType PageLocate;
	};
	/// 查询上市基金日行情/中国封闭式基金日行情响应
	struct CQCVDChinaClosedFundField
	{
		///复权收盘价（元）
		TQCVDPriceType adjClose;
		///复权因子
		TQCVDPriceType adjFactor;
		///复权最高价（元）
		TQCVDPriceType adjHigh;
		///复权最低价（元）
		TQCVDPriceType adjLow;
		///复权开盘价（元）
		TQCVDPriceType adjOpen;
		///复权昨日收盘价（元）
		TQCVDPriceType adjPreClose;
		///成交金额（千元）
		TQCVDMoneyType amount;
		///涨跌（元）
		TQCVDPriceType change;
		///收盘价（元）
		TQCVDPriceType close;
		///货币代码
		TQCVDCurrencyIDType crncyCode;
		///贴水率（%）
		TQCVDPriceType discountRate;
		///基金编号
		TQCVDSecurityIDType fundId;
		///最高价（元）
		TQCVDPriceType high;
		///最低价（元）
		TQCVDPriceType low;
		///开盘价（元）
		TQCVDPriceType open;
		///页定位符
		TQCVDPageLocateType pageLocate;
		///总页数
		TQCVDPageLocateType pageTotal;
		///昨日收盘价（元）
		TQCVDPriceType preClose;
		///涨跌幅度（%）
		TQCVDPriceType ptcChange;
		///交易日
		TQCVDDateType tardeDate;
		///成交笔数
		TQCVDVolumeType tradeCount;
		///成交量（手）
		TQCVDVolumeType volume;
	};
	/// 查询私募基金净值请求
	struct CQCVDQryChinaHedgeFundField
	{
		///起始日期
		TQCVDDateType BegDate;
		///结束日期
		TQCVDDateType EndDate;
		///基金编号
		TQCVDSecurityIDType FundID;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType OrderType;
		///每页记录数
		TQCVDVolumeType PageCount;
		///页定位符
		TQCVDPageLocateType PageLocate;
	};
	/// 查询私募基金净值响应
	struct CQCVDChinaHedgeFundField
	{
		///公告日期
		TQCVDDateType annDate;
		///货币代码
		TQCVDCurrencyIDType crncyCode;
		///基金编号
		TQCVDSecurityIDType fundId;
		///累计净值
		TQCVDMoneyType navAccumulated;
		///累计分红
		TQCVDMoneyType navDivAccumulated;
		///复权因子
		TQCVDPriceType navFactor;
		///单位净值
		TQCVDMoneyType navUnit;
		///页定位符
		TQCVDPageLocateType pageLocate;
		///总页数
		TQCVDPageLocateType pageTotal;
		///截止日期
		TQCVDDateType priceDate;
	};
	/// 查询中国共同基金日净值请求
	struct CQCVDQryChinaMutualFundField
	{
		///起始日期
		TQCVDDateType BegDate;
		///结束日期
		TQCVDDateType EndDate;
		///基金编号
		TQCVDSecurityIDType FundID;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType OrderType;
		///每页记录数
		TQCVDVolumeType PageCount;
		///页定位符
		TQCVDPageLocateType PageLocate;
	};
	/// 查询中国共同基金日净值响应
	struct CQCVDChinaMutualFundField
	{
		///公告日期
		TQCVDDateType annDate;
		///是否合计数据
		TQCVDBoolType assetMergedSharesOrNot;
		///货币代码
		TQCVDCurrencyIDType crncyCode;
		///是否净除权日
		TQCVDBoolType exdividendDate;
		///累计净值
		TQCVDMoneyType navAccumulated;
		///复权因子
		TQCVDPriceType navAdjFactor;
		///合计资产净值
		TQCVDMoneyType navAdjusted;
		///累计单位分配
		TQCVDMoneyType navDistribution;
		///累计分红
		TQCVDMoneyType navDivaccumulated;
		///单位净值
		TQCVDMoneyType navUnit;
		///合计资产净值
		TQCVDMoneyType netAssetTotal;
		///页定位符
		TQCVDPageLocateType pageLocate;
		///总页数
		TQCVDPageLocateType pageTotal;
		///截至日期
		TQCVDDateType priceDate;
		///资产净值
		TQCVDMoneyType prtNetasset;
		///交易代码
		TQCVDSecurityIDType tradeCode;
	};
	/// 查询中国期权日行情请求
	struct CQCVDQryChinaOptionEodPricesField
	{
		///起始日期
		TQCVDDateType BegDate;
		///结束日期
		TQCVDDateType EndDate;
		///交易所代码
		TQCVDExchangeIDType ExchangeID;
		///交易所代码
		TQCVDExchangeCodeType ExchangeCode;
		///期货代码
		TQCVDSecurityIDType TradeCode;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType OrderType;
		///每页记录数
		TQCVDVolumeType PageCount;
		///页定位符
		TQCVDPageLocateType PageLocate;
	};
	/// 查询中国期权日行情响应
	struct CQCVDChinaOptionEodPricesField
	{
		///收盘价(元)
		TQCVDPriceType closePrice;
		///结算价(元)
		TQCVDPriceType endPrice;
		///收盘价-前结算价
		TQCVDPriceType endPriorPrice;
		///交易所代码
		TQCVDExchangeCodeType exchangeCode;
		///最高价(元)
		TQCVDPriceType highPrice;
		///最低价(元)
		TQCVDPriceType lowPrice;
		///开盘价(元)
		TQCVDPriceType openPrice;
		///页定位符
		TQCVDPageLocateType pageLocate;
		///总页数
		TQCVDPageLocateType pageTotal;
		///前结算价(元)
		TQCVDPriceType priorPrice;
		///持仓量(手)
		TQCVDVolumeType saveVolume;
		///成交金额(万元)
		TQCVDMoneyType sucessAmout;
		///成交量(手)
		TQCVDVolumeType sucessVolume;
		///结算价-前结算价
		TQCVDPriceType totalPriorPrice;
		///期货代码
		TQCVDSecurityIDType tradeCode;
		///交易日期
		TQCVDDateType tradeDt;
	};
	/// 查询中国股指期货日行情请求
	struct CQCVDQryCindexfutureseodPricesField
	{
		///起始日期
		TQCVDDateType BegDate;
		///结束日期
		TQCVDDateType EndDate;
		///交易所代码
		TQCVDExchangeIDType ExchangeID;
		///交易所代码
		TQCVDExchangeCodeType ExchangeCode;
		///股指期货
		TQCVDSecurityIDType TradeCode;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType OrderType;
		///每页记录数
		TQCVDVolumeType PageCount;
		///页定位符
		TQCVDPageLocateType PageLocate;
	};
	/// 查询中国股指期货日行情响应
	struct CQCVDCindexfutureseodPricesField
	{
		///涨跌(元)
		TQCVDPriceType changeCount;
		///收盘价(元)
		TQCVDPriceType closePrice;
		///结算价(元)
		TQCVDPriceType endPrice;
		///交易所代码
		TQCVDExchangeCodeType exchangeCode;
		///最高价(元)
		TQCVDPriceType highPrice;
		///合约类型
		TQCVDContractTypeType infoType;
		///最低价(元)
		TQCVDPriceType lowPrice;
		///开盘价(元)
		TQCVDPriceType openPrice;
		///页定位符
		TQCVDPageLocateType pageLocate;
		///总页数
		TQCVDPageLocateType pageTotal;
		///前结算价(元)
		TQCVDPriceType priorPrice;
		///持仓量(手)
		TQCVDVolumeType saveVolume;
		///股指期货
		TQCVDSecurityIDType tradeCode;
		///成交金额(万元)
		TQCVDMoneyType sucessAmout;
		///成交量(手)
		TQCVDVolumeType sucessVolume;
		///交易日期
		TQCVDDateType tradeDt;
	};
	/// 查询货币基金每天净值收益请求
	struct CQCVDQryCMoneyMarketDailyFIncomeField
	{
		///起始日期
		TQCVDDateType BegDate;
		///结束日期
		TQCVDDateType EndDate;
		///交易代码
		TQCVDSecurityIDType TradeCode;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType OrderType;
		///每页记录数
		TQCVDVolumeType PageCount;
		///页定位符
		TQCVDPageLocateType PageLocate;
	};
	/// 查询货币基金每天净值收益响应
	struct CQCVDCMoneyMarketDailyFIncomeField
	{
		///累计单位净值
		TQCVDMoneyType accunitNav;
		///公告日期
		TQCVDDateType annDate;
		///起始日期
		TQCVDDateType beginDate;
		///结束日期
		TQCVDDateType endDate;
		///每万分收益
		TQCVDPriceType incomePerMillion;
		///页定位符
		TQCVDPageLocateType pageLocate;
		///总页数
		TQCVDPageLocateType pageTotal;
		///交易代码
		TQCVDSecurityIDType tradeCode;
		///单位净值
		TQCVDMoneyType unityield;
		///七日年化收益率
		TQCVDPriceType yearlyROE;
	};
	/// 查询商品期货日行情请求
	struct CQCVDQryCommodityFuturesPriceField
	{
		///起始日期
		TQCVDDateType BegDate;
		///结束日期
		TQCVDDateType EndDate;
		///交易所代码
		TQCVDExchangeIDType ExchangeID;
		///交易所代码
		TQCVDExchangeCodeType ExchangeCode;
		///股票代码
		TQCVDSecurityIDType TradeCode;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType OrderType;
		///每页记录数
		TQCVDVolumeType PageCount;
		///页定位符
		TQCVDPageLocateType PageLocate;
	};
	/// 查询商品期货日行情响应
	struct CQCVDCommodityFuturesPriceField
	{
		///成交金额(万元)
		TQCVDMoneyType amount;
		///涨跌(元)
		TQCVDPriceType change;
		///收盘价（元）
		TQCVDPriceType close;
		///交易所编码
		TQCVDExchangeCodeType exchangeCode;
		///合约类型
		TQCVDContractTypeType fsInfoType;
		///最高价（元）
		TQCVDPriceType high;
		///最低价（元）
		TQCVDPriceType low;
		///持仓量(手)
		TQCVDVolumeType oi;
		///持仓量变化
		TQCVDVolumeType oiChange;
		///开盘价（元）
		TQCVDPriceType open;
		///页定位符
		TQCVDPageLocateType pageLocate;
		///总页数
		TQCVDPageLocateType pageTotal;
		///前结算价(元)
		TQCVDPriceType presettle;
		///结算价(元)
		TQCVDPriceType settle;
		///股票代码
		TQCVDSecurityIDType tradeCode;
		///交易日
		TQCVDDateType tradeDate;
		///成交量(手)
		TQCVDVolumeType volume;
	};
	/// 查询黄金现货日行情请求
	struct CQCVDQryGoldSpotPricesField
	{
		///起始日期
		TQCVDDateType BegDate;
		///结束日期
		TQCVDDateType EndDate;
		///交易所代码
		TQCVDExchangeIDType ExchangeID;
		///交易所代码
		TQCVDExchangeCodeType ExchangeCode;
		///股票代码
		TQCVDSecurityIDType TradeCode;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType OrderType;
		///每页记录数
		TQCVDVolumeType PageCount;
		///页定位符
		TQCVDPageLocateType PageLocate;
	};
	/// 查询黄金现货日行情响应
	struct CQCVDGoldSpotPricesField
	{
		///成交金额(元)
		TQCVDMoneyType amount;
		///均价(元)
		TQCVDPriceType avgPrice;
		///收盘价（元）
		TQCVDPriceType close;
		///交收量(手)
		TQCVDVolumeType delAmt;
		///延期补偿费支付方式向类别代码
		TQCVDSecurityIDType delayPayTypeCode;
		///交易所编码
		TQCVDExchangeCodeType exchangeCode;
		///最高价（元）
		TQCVDPriceType high;
		///最低价（元）
		TQCVDPriceType low;
		///持仓量(手)
		TQCVDVolumeType oi;
		///开盘价（元）
		TQCVDPriceType open;
		///页定位符
		TQCVDPageLocateType pageLocate;
		///总页数
		TQCVDPageLocateType pageTotal;
		///涨跌幅(%)
		TQCVDPriceType pctChg;
		///结算价(元)
		TQCVDPriceType settle;
		///股票代码
		TQCVDSecurityIDType tradeCode;
		///交易日
		TQCVDDateType tradeDate;
		///成交量(千克)
		TQCVDVolumeType volume;
	};

	///查询中国共同基金分红请求
	struct CQCVDQryChinaMFDividendField
	{
		///起始日期（公告日期）
		TQCVDDateType   BegDate;
		///结束日期（公告日期）
		TQCVDDateType   EndDate;
		///基金代码
		TQCVDSecurityIDType FundID;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType  OrderType;
		///每页记录数
		TQCVDVolumeType PageCount;
		///页定位符
		TQCVDPageLocateType PageLocate;
	};
	///查询中国共同基金分红响应
	struct CQCVDChinaMFDividendField
	{
		///收益分配金额(元)
		TQCVDMoneyType  aprAmount;
		///可分配收益(元)
		TQCVDMoneyType  apr;
		///收益支付日
		TQCVDDateType   divIpayDate;
		///总页数
		TQCVDPageLocateType pageTotal;
		///份额基准年度
		TQCVDDateType   shBchYear;
		///分配对象
		TQCVDDivObjectType  divObject;
		///派息日(场外)
		TQCVDDateType   divPayDate;
		///净值除权日
		TQCVDDateType   exDivDate;
		///公告日期
		TQCVDDateType   annDate;
		///红利再投资到账日
		TQCVDDateType   reinvToacDate;
		///基金代码
		TQCVDSecurityIDType fundId;
		///除息日
		TQCVDDateType   exDate;
		///红利再投资可赎回起始日
		TQCVDDateType   reinvRedeem;
		///分红实施公告日
		TQCVDDateType   divImpDate;
		///可分配收益基准日
		TQCVDDateType   bchDate;
		///页定位符
		TQCVDPageLocateType pageLocate;
		///方案进度
		TQCVDDivProgressType    progress;
		///货币代码
		TQCVDCurrencyIDType currency;
		///每股派息(元)
		TQCVDMoneyType  perShTax;
		///红利再投资份额净值基准日
		TQCVDDateType   reinvBchDate;
		///权益登记日
		TQCVDDateType   recodeDate;
		///基准基金份额(万份)
		TQCVDMoneyType  bchUnit;
		///除息日(场外)
		TQCVDDateType   edExDate;
		///交易所代码
		TQCVDExchangeCodeType   exchangeCode;
		///派息日
		TQCVDDateType   payDate;
	};

	///查询中国A股停复牌信息
	struct CQCVDQryAShareTradingSuspensionField
	{
		///交易所
		TQCVDExchangeIDType ExchangeID;
		///证券代码
		TQCVDSecurityIDType SecurityID;
		///停牌开始日期
		TQCVDDateType SuspendBeginDate;
		///停牌结束日期
		TQCVDDateType SuspendEndDate;
		///复牌日期
		TQCVDDateType ResumeBeginDate;
		///复牌日期
		TQCVDDateType ResumeEndDate;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType  OrderType;
		///每页记录数
		TQCVDVolumeType PageCount;
		///页定位符
		TQCVDPageLocateType PageLocate;
	};
	///中国A股停复牌信息
	struct CQCVDAShareTradingSuspensionField
	{
		///交易所
		TQCVDExchangeIDType ExchangeID;
		///证券代码
		TQCVDSecurityIDType SecurityID;
		///停复牌时间
		TQCVDTimeRangeType SuspendTimeRange;
		///停牌日期
		TQCVDDateType SuspendDate;
		///复牌日期
		TQCVDDateType ResumeDate;
		///停牌类型代码
		TQCVDSuspendTypeCodeType SuspendTypeCode;
		///停牌类型
		TQCVDSuspendTypeType SuspendType;
		///停牌原因代码
		TQCVDSuspendReasonCodeType SuspendReasonCode;
		///停牌原因
		TQCVDSuspendReasonType SuspendReason;
		///页定位符
		TQCVDPageLocateType PageLocate;
		///总页数
		TQCVDPageLocateType PageTotal;
	};

	///查询领涨指数
	struct CQCVDQryAShareLeadingIndexField
	{
		///每页记录数
		TQCVDVolumeType PageCount;
		///页定位符
		TQCVDPageLocateType PageLocate;
		///指数类型
		TQCVDIndexTypeType IndexTypeType;
	};

	///领涨指数信息
	struct CQCVDAShareLeadingIndexField
	{
		///指数ID
		TQCVDSecurityIDType IndexID;
		///指数名称
		TQCVDSecurityNameType IndexName;
		///指数类型 行业、概念...
		TQCVDSecurityNameType IndexType;
		///交易日
		TQCVDDateType TradingDay;
		///更新时间
		TQCVDTimeType UpdateTime;
		///当前指数值
		TQCVDPriceType IndexPoint;
		///涨幅(%)
		TQCVDPriceType Increase;
		///总页数
		TQCVDPageLocateType PageTotal;
		///页定位符
		TQCVDPageLocateType PageLocate;
		///指数类型
		TQCVDIndexTypeType IndexTypeType;
	};

	///查询A股连续涨停股票
	struct CQCVDQryAShareConsecutiveUpField
	{
		///每页记录数
		TQCVDVolumeType PageCount;
		///页定位符
		TQCVDPageLocateType PageLocate;
	};

	///A股连续涨停股票信息
	struct CQCVDAShareConsecutiveUpField
	{
		///交易所
		TQCVDExchangeIDType ExchangeID;
		///证券代码
		TQCVDSecurityIDType SecurityID;
		///证券名称
		TQCVDSecurityNameType SecurityName;
		///涨停起始交易日
		TQCVDDateType BeginTradeDay;
		///涨停截至交易日
		TQCVDDateType EndTradeDay;
		///连续涨停天数
		TQCVDVolumeType UpDayNum;
		///起始交易日收盘价
		TQCVDPriceType BeginClosePrice;
		///截至交易日收盘价
		TQCVDPriceType EndClosePrice;
		///涨停幅度(%)序列，每个交易日涨停幅度之间用逗号','分隔，以最近交易日涨幅排在序列前面。
		TQCVDDistriValueType PriceUpPercentsSeries;
		///总页数
		TQCVDPageLocateType PageTotal;
		///页定位符
		TQCVDPageLocateType PageLocate;
	};

	/// 查询指数成分股信息
	struct CQCVDQryIndexConstituentsInfoField
	{
		///指数代码
		TQCVDIndexIDType	IndexID;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 指数成分股数据
	struct CQCVDIndexConstituentsDataField
	{
		///指数名称
		TQCVDIndustriesNameType	WindSecCode;

		///指数代码
		TQCVDIndexIDType	IndexID;

		///纳入日期
		TQCVDDateType	EntryDate;

		///剔除日期
		TQCVDDateType	RemoveDate;

		///最新标志
		TQCVDCurSignType	CurSign;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 申万指数实时行情信息
	struct CQCVDQrySWSCodeListField
	{
		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	/// 申万指数列表数据
	struct CQCVDSWSCodeListDataField
	{
		///板块中文定义
		TQCVDChineseDfinitionType	ChineseDfinition;

		///交易日期
		TQCVDDateType	TradingDay;

		///昨日收盘点位
		TQCVDPriceType	PreClosePoint;

		///指数代码
		TQCVDIndexIDType	IndexID;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	///查询奇点系统客户登录记录
	struct CQCVDReqQrySingularityLoginRecordField
	{
		///奇点系统客户号
		TQCVDInvestorIDType InvestorID;
		///交易市场: 现货市场 1 ; 两融市场 2; 期权市场 3; 期货市场 4 证券普通行情 5 证券逐笔行情 6
		TQCVDTradeMarketType TradeMarket;
		///认证方式(密码，指纹，登录串)
		TQCVDAuthModeType	AuthMode;
		///设备类型: PC 1; MOBILE 2
		TQCVDDeviceClassType DeviceClass;
		///开始日期
		TQCVDDateType	BeginDate;
		///开始时间
		TQCVDTimeType	BeginTime;
		///结束日期
		TQCVDDateType	EndDate;
		///结束时间
		TQCVDTimeType	EndTime;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///希望获取记录数
		TQCVDVolumeType	RequestCount;
		///登录记录范围
		TQCVDQryLoginScopeType	QryLoginScope;
	};

	///奇点系统客户登录记录
	struct CQCVDSingularityLoginRecordField
	{
		///奇点系统客户号InvestorID
		TQCVDInvestorIDType InvestorID;
		///交易市场: 现货市场 1 ; 两融市场 2; 期权市场 3; 期货市场 4 证券普通行情 5 证券逐笔行情 6
		TQCVDTradeMarketType TradeMarket;
		///认证方式(密码，指纹，登录串)
		TQCVDAuthModeType	AuthMode;
		///设备类型: PC 1; MOBILE 2
		TQCVDDeviceClassType DeviceClass;
		///设备标识
		TQCVDDeviceIDType	DeviceId;
		///IP地址
		TQCVDIPAddressType LoginIp;
		///MAC地址
		TQCVDMacAddressType MacAddr;
		///登录日期
		TQCVDDateType LoginDate;
		///登录时间
		TQCVDTimeType LoginTime;
		///是否成功登录
		TQCVDBoolType Status;
		///登录错误编号
		TQCVDErrorIDType ErrorID;
		///登录错误说明
		TQCVDErrorMsgType ErrorMsg;
	};

	///查询中国A股证券曾用名信息
	struct CQCVDReqQryASharePreviousNameField
	{
		///交易所
		TQCVDExchangeIDType ExchangeID;
		///证券代码
		TQCVDSecurityIDType SecurityID;
		///起始日期查询开始日期
		TQCVDDateType	BeginDateQryBeginDay;
		///起始日期查询结束日期
		TQCVDDateType	BeginDateQryEndDay;
		///截止日期查询开始日期
		TQCVDDateType	EndDateQryBeginDay;
		///截止日期查询结束日期
		TQCVDDateType	EndDateQryEndDay;
		///公告日期查询开始日期
		TQCVDDateType	ANNDateQryBeginDay;
		///公告日期查询结束日期
		TQCVDDateType	ANNDateQryEndDay;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	///中国A股证券曾用名信息
	struct CQCVDASharePreviousNameField
	{
		///交易所
		TQCVDExchangeIDType ExchangeID;
		///证券代码
		TQCVDSecurityIDType SecurityID;
		///证券名称
		TQCVDSecurityNameType SecurityName;
		///起始日期
		TQCVDDateType BeginDate;
		///截至日期
		TQCVDDateType EndDate;
		///公告日期
		TQCVDDateType ANNDate;
		///变动原因代码
		TQCVDASharePreviousNameChangeReasonType ChangeReason;
		///总页数
		TQCVDPageLocateType PageTotal;
		///页定位符
		TQCVDPageLocateType PageLocate;
	};

	///查询现货投资者大客户信息
	struct CQCVDQryBigInvestorInfoField
	{
		///投资者
		TQCVDInvestorIDType InvestorID;
		///投资者所在节点
		TQCVDNodeIDType NodeID;
	};

	///现货投资者大客户信息
	struct CQCVDBigInvestorInfoField
	{
		///投资者
		TQCVDInvestorIDType InvestorID;
		///投资者所在节点
		TQCVDNodeIDType NodeID;
		///附加信息
		TQCVDAddtionDataType AddtionData;
	};

	///查询交易信息统计
	struct CQCVDQryOrderStatisticsField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///节点编号
		TQCVDSequenceNoType	NodeID;

		///起始日期
		TQCVDDateType	BegDate;

		///结束日期
		TQCVDDateType	EndDate;
	};

	///交易信息统计
	struct CQCVDOrderStatisticsField
	{
		///交易日
		TQCVDDateType	TradingDay;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///节点编号
		TQCVDSequenceNoType  NodeID;

		///委托笔数
		TQCVDVolumeType	OrderNum;

		///成交率
		TQCVDRatioType TradeRate;

		///上海股票穿透平均延时（微秒）
		TQCVDDelayType SSEkernelStock;

		///上海债券穿透平均延时（微秒）
		TQCVDDelayType SSEkernelDebt;

		///上海股票链路平均延时（微秒）
		TQCVDDelayType SSErttStock;

		///上海债券链路平均延时（微秒）
		TQCVDDelayType SSErttDebt;

		///深圳股票穿透平均延时（微秒）
		TQCVDDelayType SZSEkernelStock;

		///深圳股票链路平均延时（微秒）
		TQCVDDelayType SZSErttStock;
	};

	///申请投资者节点变更
	struct CQCVDReqInvestorApplyNodeField
	{
		///投资者代码
		TQCVDApplyNodeInvestorIDType	InvestorID;
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;
		///源节点
		TQCVDApplyNodeIDType	FromNodeID;
		///源PBU
		TQCVDPbuIDType	FromPbuID;
		///新节点
		TQCVDApplyNodeIDType	ToNodeID;
		///新PBU
		TQCVDPbuIDType	ToPbuID;
		///外部申请ID
		TQCVDApplyIDType	ExApplyID;
		///终端类型
		TQCVDTerminalTypeType	TerminalType;
	};

	///投资者节点变更
	struct CQCVDInvestorApplyNodeField
	{
		///申请ID
		TQCVDApplyIDType	ApplyID;

		///交易日
		TQCVDDateType	TradingDay;

		///投资者代码
		TQCVDApplyNodeInvestorIDType	InvestorID;

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///股东账户代码
		TQCVDShareholderIDType	ShareholderID;

		///源节点
		TQCVDApplyNodeIDType	FromNodeID;

		///源PBU
		TQCVDPbuIDType	FromPbuID;

		///新节点
		TQCVDApplyNodeIDType	ToNodeID;

		///新PBU
		TQCVDPbuIDType	ToPbuID;

		///外部申请ID
		TQCVDApplyIDType	ExApplyID;

		///申请人
		TQCVDApplyNodeInvestorIDType	ApplyerID;

		///终端类型
		TQCVDTerminalTypeType	TerminalType;

		///申请时间
		TQCVDApplyNodeTimeType	ApplyTime;

		///申请状态
		TQCVDApplyStatusType	ApplyStatus;

		///备注
		TQCVDApplyNodeRemarkType	Remark;

		///是否强制执行
		TQCVDEnforceFlagType	EnforceFlag;

		///最后更新人
		TQCVDApplyNodeInvestorIDType UpdaterID;

		///最后更新时间
		TQCVDApplyNodeTimeType	UpdateTime;
	};

	///投资者节点变更查询请求
	struct CQCVDQryInvestorApplyNodeField
	{
		///投资者代码
		TQCVDApplyNodeInvestorIDType	InvestorID;

		///开始日期
		TQCVDDateType	BeginTradingDay;

		///结束日期
		TQCVDDateType	EndTradingDay;

		///外部申请ID
		TQCVDApplyIDType	ExApplyID;
	};

	///查询银证转账资金调拨处理信息
	struct CQCVDQryBankTransInfoField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///申请流水号
		TQCVDApplyNo	ApplyNo;

		///开始申请日期
		TQCVDDateType	BeginApplyDay;

		///结束申请日期
		TQCVDDateType	EndApplyDay;

		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;

		///每页记录数
		TQCVDVolumeType	PageCount;

		///页定位符
		TQCVDPageLocateType	PageLocate;
	};

	///查询银证转账资金调拨处理信息应答
	struct CQCVDBankTransInfoField
	{
		///申请流水号
		TQCVDApplyNo	ApplyNo;

		///申请日期
		TQCVDDateType	ApplyDate;

		///申请时间
		TQCVDTimeType	ApplyTime;

		///流水所属业务系统
		TQCVDSysType	SysType;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;

		///银行代码
		TQCVDBankCode	BankCode;

		///存管账号
		TQCVDDepositNo	DepositNo;

		///银行账号
		TQCVDBankNo	BankNo;

		///银证转帐金额
		TQCVDMoneyType	amount1;

		///实际入金金额
		TQCVDMoneyType	amount2;

		///入金节点号
		TQCVDNodeType	NodeType;

		///处理日期
		TQCVDDateType	HandleDate;

		///处理时间
		TQCVDTimeType	HandleTime;

		///页定位符
		TQCVDCreditPageLocateType	PageLocate;

		///总页数
		TQCVDCreditPageLocateType	PageTotal;
	};

	///订阅盯盘通知请求
	struct CQCVDSubRtnMarketWatchField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///股票代码
		TQCVDSecurityIDType	SecurityID;
		///盯盘状态
		TQCVDWatchStatusType	WatchStatus;
		///触发价格
		TQCVDPriceType	TriggerPrice;
		///排板封单金额（万元）
		TQCVDMoneyType	BuyAmount;
		///已封板不触发
		TQCVDBoolType	LimitNoTriger;
		///排撤封单金额（万元）
		TQCVDMoneyType	CancelAmount;
		///再买封单金额（万元）
		TQCVDMoneyType	BuyAgainAmount;
		///扫板卖1金额低于（万元）
		TQCVDMoneyType	BuyAsk1Amount;
		///序号
		TQCVDSequenceNoType	SeqNo;
		///扫版卖单金额减少比率
		TQCVDMoneyType	BuyAskDecreaseRatio;
		///触发成交量（手）
		TQCVDVolumeType	TriggerVolume;
	};

	///盯盘通知
	struct CQCVDRtnMarketWatchField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///股票代码
		TQCVDSecurityIDType	SecurityID;
		///盯盘状态
		TQCVDWatchStatusType	WatchStatus;
		///触发行情时间戳
		TQCVDTimeStampType	TrigerTime;
		///信号结果
		TQCVDSignalResultType	SignalResult;
		///触发价格
		TQCVDPriceType	TriggerPrice;
		///触发排板封单金额（万元）
		TQCVDMoneyType	TriggerBuyAmount;
		///触发排撤封单金额（万元）
		TQCVDMoneyType	TriggerCancelAmount;
		///触发再买封单金额（万元）
		TQCVDMoneyType	TriggerBugAgainAmount;
		///触发扫板卖1金额（万元）
		TQCVDMoneyType	TriggerBuyAsk1Amount;
		///序号
		TQCVDSequenceNoType	SeqNo;
		///触发扫版卖单金额减少比率
		TQCVDMoneyType	TriggerBuyAskDecreaseRatio;
		///触发成交量（手）
		TQCVDVolumeType	TriggerVolume;
	};

	struct CQCVDQryCCBondValuationField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///股票代码
		TQCVDSecurityIDType	SecurityID;
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///每页记录数
		TQCVDVolumeType PageCount;
		///页定位符
		TQCVDPageLocateType PageLocate;
	};

	struct CQCVDCCBondValuationField
	{
		///股票代码
		TQCVDSecurityIDType		SecurityID;
		///交易日期
		TQCVDDateType			TRADE_DT;
		///已计息天数
		TQCVDCountType			CB_ANAL_ACCRUEDDAYS;
		///应计利息
		TQCVDInterestType		CB_ANAL_ACCRUEDINTEREST;
		///剩余期限（年）
		TQCVDFloatInfoType		CB_ANAL_PTM;
		///当期收益率
		TQCVDRatioType			CB_ANAL_CURYIELD;
		///纯债到期收益率
		TQCVDRatioType			CB_ANAL_YTM;
		///纯债价值
		TQCVDPriceType			CB_ANAL_STRBVALUE;
		///纯债溢价
		TQCVDPriceType			CB_ANAL_STRBPREMIUM;
		///纯债溢价率
		TQCVDRatioType			CB_ANAL_STRBPREMIUMRATIO;
		///转股价
		TQCVDPriceType			CB_ANAL_CONVPRICE;
		///转股比例
		TQCVDRatioType			CB_ANAL_CONVRATIO;
		///转股价值
		TQCVDPriceType			CB_ANAL_CONVVALUE;
		///转股溢价
		TQCVDPriceType			CB_ANAL_CONVPREMIUM;
		///转股溢价率
		TQCVDRatioType			CB_ANAL_CONVPREMIUMRATIO;
		///平价/底价
		TQCVDPriceType			CB_ANAL_PARITYBASEPRICE;
	};

	///查询有序逐笔行情:包括逐笔成交(上海非债券类、深圳非债券类、深圳可转债)，逐笔委托(上海非债券类、深圳非债券类、深圳可转债)，盘后定价逐笔成交，上海XTS债券逐笔数据，深圳债券(不包括可转债)逐笔成交，深圳债券(不包括可转债)逐笔委托
	struct CQCVDReqQrySequencedTickMDField
	{
		///交易所，需填入上海交易所或者深圳交易所
		TQCVDExchangeIDType ExchangeID;
		///证券代码，可以不填写设置为空字符串。设置为非空字符串，则只返回该SecurityID的逐笔行情；否则，此字段不作为查询过滤条件。
		TQCVDSecurityIDType SecurityID;
		///查询主序号，必须填写
		TQCVDSequenceNoType QueryMainSeq;
		///查询起始子序号，必须填写。(如果查询的主序号属于上海逐笔成交或者上海逐笔委托，则请填写入BizIndex业务序号字段起始值；否则，请填写入SubSeq子序号字段起始值)。
		TQCVDLongSequenceType Query_Begin_SubSeq_OR_BizIndex;
		///最大返回查询结果数量，可不填写则返回默认设置数量
		TQCVDVolumeType MaxResultCount;
		///附加查询信息，可不填写
		TQCVDAdditionDataType AdditionData;
	};

	///查询有序逐笔行情(包括NGTSTick):包括上海NGTSTick逐笔数据(上海非债券类)，逐笔成交(深圳非债券类、深圳可转债)Transaction，逐笔委托(深圳非债券类、深圳可转债)OrderDetail，盘后定价逐笔成交PHTransaction，上海XTS债券逐笔数据XTSTick，深圳债券(不包括可转债)逐笔成交BondTransaction，深圳债券(不包括可转债)逐笔委托BondOrderDetail
	struct CQCVDReqQryNGTSTickSequencedTickMDField
	{
		///交易所，需填入上海交易所或者深圳交易所
		TQCVDExchangeIDType ExchangeID;
		///证券代码，可以不填写设置为空字符串。设置为非空字符串，则只返回该SecurityID的逐笔行情；否则，此字段不作为查询过滤条件。
		TQCVDSecurityIDType SecurityID;
		///查询主序号，必须填写
		TQCVDSequenceNoType QueryMainSeq;
		///查询起始子序号，必须填写。
		TQCVDLongSequenceType Query_Begin_SubSeq;
		///最大返回查询结果数量，可不填写则返回默认设置数量
		TQCVDVolumeType MaxResultCount;
		///附加查询信息，可不填写
		TQCVDAdditionDataType AdditionData;
	};

	/// 逐笔成交(上海非债券类、深圳非债券类、深圳可转债)
	struct CQCVDTransactionField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///时间戳
		TQCVDTimeStampType	TradeTime;

		///成交价格
		TQCVDPriceType	TradePrice;

		///成交数量
		TQCVDLongVolumeType	TradeVolume;

		///成交类别（只有深圳行情有效）
		TQCVDExecTypeType	ExecType;

		///主序号
		TQCVDSequenceNoType	MainSeq;

		///子序号
		TQCVDLongSequenceType	SubSeq;

		///买方委托序号
		TQCVDLongSequenceType	BuyNo;

		///卖方委托序号
		TQCVDLongSequenceType	SellNo;

		///附加信息1
		TQCVDIntInfoType	Info1;

		///附加信息2
		TQCVDIntInfoType	Info2;

		///附加信息3
		TQCVDIntInfoType	Info3;

		///内外盘标志（只有上海行情有效）
		TQCVDTradeBSFlagType	TradeBSFlag;

		///业务序号（只有上海行情有效）
		TQCVDLongSequenceType	BizIndex;
	};

	/// 逐笔委托(上海非债券类、深圳非债券类、深圳可转债)
	struct CQCVDOrderDetailField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///时间戳
		TQCVDTimeStampType	OrderTime;

		///委托价格
		TQCVDPriceType	Price;

		///委托数量
		TQCVDLongVolumeType	Volume;

		///委托方向
		TQCVDLSideType	Side;

		///订单类别（只有深圳行情有效）
		TQCVDLOrderTypeType	OrderType;

		///主序号
		TQCVDSequenceNoType	MainSeq;

		///子序号
		TQCVDSequenceNoType	SubSeq;

		///附加信息1
		TQCVDIntInfoType	Info1;

		///附加信息2
		TQCVDIntInfoType	Info2;

		///附加信息3
		TQCVDIntInfoType	Info3;

		///委托序号
		TQCVDLongSequenceType	OrderNO;

		///订单状态
		TQCVDLOrderStatusType	OrderStatus;

		///业务序号（只有上海行情有效）
		TQCVDLongSequenceType	BizIndex;
	};

	/// 盘后定价逐笔成交
	struct CQCVDPHTransactionField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///时间戳
		TQCVDTimeStampType	TradeTime;

		///成交价格
		TQCVDPriceType	TradePrice;

		///成交数量
		TQCVDLongVolumeType	TradeVolume;

		///成交金额(元)
		TQCVDMoneyType	TradeMoney;

		///成交类别
		TQCVDExecTypeType	ExecType;

		///主序号
		TQCVDSequenceNoType	MainSeq;

		///子序号
		TQCVDLongSequenceType	SubSeq;

		///买方委托序号
		TQCVDLongSequenceType	BuyNo;

		///卖方委托序号
		TQCVDLongSequenceType	SellNo;

		///附加信息1
		TQCVDIntInfoType	Info1;

		///附加信息2
		TQCVDIntInfoType	Info2;

		///附加信息3
		TQCVDIntInfoType	Info3;

		///内外盘标志
		TQCVDTradeBSFlagType	TradeBSFlag;
	};

	/// 上海债券逐笔行情(上海XTS债券逐笔数据XTSTick)
	struct CQCVDXTSTickField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///主序号
		TQCVDSequenceNoType	MainSeq;

		///子序号
		TQCVDLongSequenceType	SubSeq;

		///时间戳
		TQCVDTimeStampType	TickTime;

		///逐笔类型
		TQCVDLTickTypeType	TickType;

		///买方委托序号
		TQCVDLongSequenceType	BuyNo;

		///卖方委托序号
		TQCVDLongSequenceType	SellNo;

		///价格
		TQCVDPriceType	Price;

		///数量
		TQCVDLongVolumeType	Volume;

		///成交金额
		TQCVDMoneyType	TradeMoney;

		///委托方向
		TQCVDLSideType	Side;

		///内外盘标志
		TQCVDTradeBSFlagType	TradeBSFlag;

		///行情产品实时状态
		TQCVDMDSecurityStatType	MDSecurityStat;

		///附加信息1
		TQCVDIntInfoType	Info1;

		///附加信息2
		TQCVDIntInfoType	Info2;

		///附加信息3
		TQCVDIntInfoType	Info3;
	};

	/// 上海NGTSTick逐笔行情
	struct CQCVDNGTSTickField
	{

		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///主序号
		TQCVDSequenceNoType	MainSeq;

		///子序号
		TQCVDLongSequenceType	SubSeq;

		///时间戳
		TQCVDTimeStampType	TickTime;

		///逐笔类型
		TQCVDLTickTypeType	TickType;

		///买方委托序号
		TQCVDLongSequenceType	BuyNo;

		///卖方委托序号
		TQCVDLongSequenceType	SellNo;

		///价格
		TQCVDPriceType	Price;

		///数量
		TQCVDLongVolumeType	Volume;

		///成交金额
		TQCVDMoneyType	TradeMoney;

		///委托方向
		TQCVDLSideType	Side;

		///内外盘标志
		TQCVDTradeBSFlagType	TradeBSFlag;

		///行情产品实时状态
		TQCVDMDSecurityStatType	MDSecurityStat;

		///附加信息1
		TQCVDIntInfoType	Info1;

		///附加信息2
		TQCVDIntInfoType	Info2;

		///附加信息3
		TQCVDIntInfoType	Info3;

	};

	/// 深圳债券(不包括可转债)逐笔委托
	struct CQCVDBondOrderDetailField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///时间戳
		TQCVDTimeStampType	OrderTime;

		///委托价格
		TQCVDPriceType	Price;

		///委托数量
		TQCVDLongVolumeType	Volume;

		///委托方向
		TQCVDLSideType	Side;

		///订单类别
		TQCVDLOrderTypeType	OrderType;

		///主序号
		TQCVDSequenceNoType	MainSeq;

		///子序号
		TQCVDLongSequenceType	SubSeq;

		///附加信息1
		TQCVDIntInfoType	Info1;

		///附加信息2
		TQCVDIntInfoType	Info2;

		///附加信息3
		TQCVDIntInfoType	Info3;
	};

	/// 深圳债券(不包括可转债)逐笔成交
	struct CQCVDBondTransactionField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

		///证券代码
		TQCVDSecurityIDType	SecurityID;

		///时间戳
		TQCVDTimeStampType	TradeTime;

		///成交价格
		TQCVDPriceType	TradePrice;

		///成交数量
		TQCVDLongVolumeType	TradeVolume;

		///成交类别
		TQCVDExecTypeType	ExecType;

		///主序号
		TQCVDSequenceNoType	MainSeq;

		///子序号
		TQCVDLongSequenceType	SubSeq;

		///买方委托序号
		TQCVDLongSequenceType	BuyNo;

		///卖方委托序号
		TQCVDLongSequenceType	SellNo;

		///附加信息1
		TQCVDIntInfoType	Info1;

		///附加信息2
		TQCVDIntInfoType	Info2;

		///附加信息3
		TQCVDIntInfoType	Info3;
	};

	/// 查询中国债券基本资料请求包
	struct CQCVDQryCBondDescriptionField
	{
		///交易所代码
		TQCVDWndExchMarketIDType	WndExchMarketID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///每页记录数
		TQCVDCreditVolumeType	PageCount;
		///页定位符
		TQCVDCreditPageLocateType	PageLocate;
	};
	/// 查询中国债券基本资料应答包
	struct CQCVDCBondDescriptionField
	{
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///发行公告日
		TQCVDDateType	B_ISSUE_ANNOUNCEMENT;
		///发行起始日
		TQCVDDateType	B_ISSUE_FIRSTISSUE;
		///发行截止日
		TQCVDDateType	B_ISSUE_LASTISSUE;
		///实际发行总量(亿元)
		TQCVDMoneyType	B_ISSUE_AMOUNTACT;
		///发行价格
		TQCVDPriceType	B_INFO_ISSUEPRICE;
		///面值
		TQCVDPriceType	B_INFO_PAR;
		///发行票面利率(%)
		TQCVDRatioType	B_INFO_COUPONRATE;
		///利差(%)
		TQCVDRatioType	B_INFO_SPREAD;
		///计息起始日
		TQCVDDateType	B_INFO_CARRYDATE;
		///计息截止日
		TQCVDDateType	B_INFO_ENDDATE;
		///到期日
		TQCVDDateType	B_INFO_MATURITYDATE;
		///债券期限(天)
		TQCVDRatioType	B_INFO_TERM_DAY;
		///兑付日
		TQCVDDateType	B_INFO_PAYMENTDATE;
		///货币代码
		TQCVDCurrencyIDType	CRNCY_CODE;
		///债券简称
		TQCVDLocalBondNameType	S_INFO_NAME;
		///交易所代码
		TQCVDWndExchMarketIDType	WndExchMarketID;
		///担保人
		TQCVDLocalBondNameType	B_INFO_GUARANTOR;
		///上市日期
		TQCVDDateType	B_INFO_LISTDATE;
		///兑付登记起始日
		TQCVDDateType	S_DIV_RECORDDATE;
		///退市日期
		TQCVDDateType	B_INFO_DELISTDATE;
		///是否发行失败
		TQCVDBoolType	IS_FAILURE;
		///参考收益率
		TQCVDRatioType	B_TENDRST_REFERYIELD;
		///最新面值
		TQCVDPriceType	B_INFO_CURPAR;
		///是否公司债
		TQCVDBoolType	IS_CORPORATE_BOND;
		///是否可提前兑付
		TQCVDBoolType	IS_PAYADVANCED;
		///是否可赎回
		TQCVDBoolType	IS_CALLABLE;
		///是否有选择权
		TQCVDBoolType	IS_CHOOSERIGHT;
		///是否按实际天数计息
		TQCVDBoolType	IS_ACT_DAYS;
		///是否增发债
		TQCVDBoolType	IS_INCBONDS;
		///上市公告日
		TQCVDDateType	LIST_ANN_DATE;
		///偿还方式
		TQCVDLocalReimbursementType	REIMBURSEMENT;
		///发行时债券评级
		TQCVDLocalBondRatingType	BOND_RATING;
		///公告日期
		TQCVDDateType	ANN_DATE;
		///债券余额(亿元)
		TQCVDMoneyType	OutstandingBalance;

	};

	/// 查询中国A股指数估值数据请求包
	struct CQCVDQryAIndexValuationField
	{
		///交易所代码 
		TQCVDExchangeIDType	ExchangeID;
		///证券代码 
		TQCVDSecurityIDType	SecurityID;
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///每页记录数
		TQCVDCreditVolumeType	PageCount;
		///页定位符
		TQCVDCreditPageLocateType	PageLocate;

	};
	/// 查询中国A股指数估值数据应答包
	struct CQCVDAIndexValuationField
	{
		///交易日期
		TQCVDDateType	TRADE_DT;
		///证券代码 
		TQCVDSecurityIDType	SecurityID;
		///交易所代码 
		TQCVDExchangeIDType	ExchangeID;
		///证券简称 
		TQCVDLocalBondNameType	S_INFO_NAME;
		///成分股数量(个）
		TQCVDPriceType	CON_NUM;
		///市盈率(PE,LYR）(倍）
		TQCVDPriceType	PE_LYR;
		///市盈率(PE,TTM）(倍）
		TQCVDPriceType	PE_TTM;
		///市净率(PB,LF）(倍）
		TQCVDPriceType	PB_LF;
		///市现率(PCF,LYR）(倍）
		TQCVDPriceType	PCF_LYR;
		///市现率(PCF,TTM）(倍）
		TQCVDPriceType	PCF_TTM;
		///市销率(PS,LYR）(倍）
		TQCVDPriceType	PS_LYR;
		///市销率(PS,TTM）（倍）
		TQCVDPriceType	PS_TTM;
		///当日总市值合计（元）
		TQCVDMoneyType	MV_TOTAL;
		///当日流通市值合计（元）
		TQCVDMoneyType	MV_FLOAT;
		///股息率
		TQCVDPriceType	DIVIDEND_YIELD;
		///历史PEG(倍）
		TQCVDPriceType	PEG_HIS;
		///总股本合计（股）
		TQCVDPriceType	TOT_SHR;
		///流通股本合计（股）
		TQCVDPriceType	TOT_SHR_FLOAT;
		///自由流通股本合计（股）
		TQCVDPriceType	TOT_SHR_FREE;
		///换手率
		TQCVDPriceType	TURNOVER;
		///换手率(自由流通)
		TQCVDPriceType	TURNOVER_FREE;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};
	/// 查询中国A股资产负债表请求包
	struct CQCVDQryAShareBalanceSheetField
	{
		///交易所代码 
		TQCVDExchangeIDType	ExchangeID;
		///证券代码 
		TQCVDSecurityIDType	SecurityID;
		///起始报告期
		TQCVDDateType	BegDate;
		///结束报告期
		TQCVDDateType	EndDate;
		///每页记录数
		TQCVDCreditVolumeType	PageCount;
		///页定位符
		TQCVDCreditPageLocateType	PageLocate;

	};
	/// 查询中国A股资产负债表应答包
	struct CQCVDAShareBalanceSheetField
	{
		///证券代码 
		TQCVDSecurityIDType	SecurityID;
		///交易所代码 
		TQCVDExchangeIDType	ExchangeID;
		///报告期
		TQCVDDateType	REPORT_PERIOD;
		///公告日期
		TQCVDDateType	ANN_DT;
		///货币代码
		TQCVDCurrencyIDType	CRNCY_CODE;
		///货币资金
		TQCVDMoneyType	MONETARY_CAP;
		///存货
		TQCVDMoneyType	INVENTORIES;
		///应收账款
		TQCVDMoneyType	ACCT_RCV;
		///其他应收款
		TQCVDMoneyType	OTH_RCV;
		///流动资产合计
		TQCVDMoneyType	TOT_CUR_ASSETS;
		///非流动资产合计
		TQCVDMoneyType	TOT_NON_CUR_ASSETS;
		///其他流动负债
		TQCVDMoneyType	OTH_CUR_LIAB;
		///流动负债合计
		TQCVDMoneyType	TOT_CUR_LIAB;
		///现金及存放中央银行款项
		TQCVDMoneyType	CASH_DEPOSITS_CENTRAL_BANK;
		///存放同业和其它金融机构款项
		TQCVDMoneyType	ASSET_DEP_OTH_BANKS_FIN_INST;
		///贵金属
		TQCVDMoneyType	PRECIOUS_METALS;
		///拆出资金
		TQCVDMoneyType	LOANS_TO_OTH_BANKS;
		///衍生金融资产
		TQCVDMoneyType	DERIVATIVE_FIN_ASSETS;
		///买入返售金融资产
		TQCVDMoneyType	RED_MONETARY_CAP_FOR_SALE;
		///应收利息
		TQCVDMoneyType	INT_RCV;
		///应收票据
		TQCVDMoneyType	NOTES_RCV;
		///应收股利
		TQCVDMoneyType	DVD_RCV;
		///发放贷款及垫款
		TQCVDMoneyType	LOANS_AND_ADV_GRANTED;
		///交易性金融资产
		TQCVDMoneyType	TRADABLE_FIN_ASSETS;
		///债权投资(元)
		TQCVDMoneyType	DEBT_INVESTMENT;
		///其他债权投资(元)
		TQCVDMoneyType	OTHER_DEBT_INVESTMENT;
		///其他权益工具投资(元)
		TQCVDMoneyType	OTHER_EQUITY_INVESTMENT;
		///投资性房地产
		TQCVDMoneyType	INVEST_REAL_ESTATE;
		///固定资产
		TQCVDMoneyType	FIX_ASSETS;
		///使用权资产
		TQCVDMoneyType	RIGHT_USE_ASSETS;
		///无形资产
		TQCVDMoneyType	INTANG_ASSETS;
		///商誉
		TQCVDMoneyType	GOODWILL;
		///递延所得税资产
		TQCVDMoneyType	DEFERRED_TAX_ASSETS;
		///可供出售金融资产
		TQCVDMoneyType	FIN_ASSETS_AVAIL_FOR_SALE;
		///持有至到期投资
		TQCVDMoneyType	HELD_TO_MTY_INVEST;
		///应收款项类投资
		TQCVDMoneyType	RCV_INVEST;
		///以公允价值计量且其变动计入其他综合收益的金融资产
		TQCVDMoneyType	FIN_ASSETS_FAIR_VALUE;
		///以摊余成本计量的金融资产
		TQCVDMoneyType	FIN_ASSETS_COST_SHARING;
		///长期股权投资
		TQCVDMoneyType	LONG_TERM_EQY_INVEST;
		///其他资产
		TQCVDMoneyType	OTH_ASSETS;
		///资产总计
		TQCVDMoneyType	TOT_ASSETS;
		///向中央银行借款
		TQCVDMoneyType	BORROW_CENTRAL_BANK;
		///同业和其它金融机构存放款项
		TQCVDMoneyType	LIAB_DEP_OTH_BANKS_FIN_INST;
		///拆入资金
		TQCVDMoneyType	LOANS_OTH_BANKS;
		///交易性金融负债
		TQCVDMoneyType	TRADABLE_FIN_LIAB;
		///衍生金融负债
		TQCVDMoneyType	DERIVATIVE_FIN_LIAB;
		///卖出回购金融资产款
		TQCVDMoneyType	FUND_SALES_FIN_ASSETS_RP;
		///吸收存款及同业存放
		TQCVDMoneyType	DEPOSIT_RECEIVED_IB_DEPOSITS;
		///应付职工薪酬
		TQCVDMoneyType	EMPL_BEN_PAYABLE;
		///应交税费
		TQCVDMoneyType	TAXES_SURCHARGES_PAYABLE;
		///预计负债
		TQCVDMoneyType	PROVISIONS;
		///应付债券
		TQCVDMoneyType	BONDS_PAYABLE;
		///租赁负债
		TQCVDMoneyType	LEASE_LIAB;
		///应付利息
		TQCVDMoneyType	INT_PAYABLE;
		///递延所得税负债
		TQCVDMoneyType	DEFERRED_TAX_LIAB;
		///其他负债
		TQCVDMoneyType	OTH_LIAB;
		///负债合计
		TQCVDMoneyType	TOT_LIAB;
		///股本（元）
		TQCVDMoneyType	CAP_STK;
		///其他权益工具
		TQCVDMoneyType	OTHER_EQUITY_TOOLS;
		///其他权益工具:优先股
		TQCVDMoneyType	OTHER_EQUITY_TOOLS_P_SHR;
		///其他权益工具:永续债(元)
		TQCVDMoneyType	OTHER_SUSTAINABLE_BOND;
		///资本公积金
		TQCVDMoneyType	CAP_RSRV;
		///其他综合收益
		TQCVDMoneyType	OTHER_COMP_INCOME;
		///一般风险准备
		TQCVDMoneyType	PROV_NOM_RISKS;
		///未分配利润
		TQCVDMoneyType	UNDISTRIBUTED_PROFIT;
		///股东权益合计(不含少数股东权益)
		TQCVDMoneyType	TOT_SHRHLDR_EQY_EXCL_MIN_INT;
		///股东权益合计(含少数股东权益)
		TQCVDMoneyType	TOT_SHRHLDR_EQY_INCL_MIN_INT;
		///盈余公积金
		TQCVDMoneyType	SURPLUS_RSRV;
		///外币报表折算差额
		TQCVDMoneyType	CNVD_DIFF_FOREIGN_CURR_STAT;
		///少数股东权益
		TQCVDMoneyType	MINORITY_INT;
		///负债及股东权益总计
		TQCVDMoneyType	TOT_LIAB_SHRHLDR_EQY;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};
	/// 查询中国A股利润表请求包
	struct CQCVDQryAShareIncomeField
	{
		///交易所代码 
		TQCVDExchangeIDType	ExchangeID;
		///证券代码 
		TQCVDSecurityIDType	SecurityID;
		///起始报告期
		TQCVDDateType	BegDate;
		///结束报告期
		TQCVDDateType	EndDate;
		///每页记录数
		TQCVDCreditVolumeType	PageCount;
		///页定位符
		TQCVDCreditPageLocateType	PageLocate;

	};
	/// 查询中国A股利润表应答包
	struct CQCVDAShareIncomeField
	{
		///证券代码 
		TQCVDSecurityIDType	SecurityID;
		///交易所代码 
		TQCVDExchangeIDType	ExchangeID;
		///报告期
		TQCVDDateType	REPORT_PERIOD;
		///公告日期
		TQCVDDateType	ANN_DT;
		///货币代码
		TQCVDCurrencyIDType	CRNCY_CODE;
		///营业收入
		TQCVDMoneyType	OPER_REV;
		///利息净收入
		TQCVDMoneyType	NET_INT_INC;
		///利息收入
		TQCVDMoneyType	INT_INC;
		///减:利息支出
		TQCVDMoneyType	LESS_INT_EXP;
		///手续费及佣金净收入
		TQCVDMoneyType	NET_HANDLING_CHRG_COMM_INC;
		///手续费及佣金收入
		TQCVDMoneyType	HANDLING_CHRG_COMM_INC;
		///减:手续费及佣金支出
		TQCVDMoneyType	LESS_HANDLING_CHRG_COMM_EXP;
		///加:投资净收益
		TQCVDMoneyType	PLUS_NET_INVEST_INC;
		///加:公允价值变动净收益
		TQCVDMoneyType	PLUS_NET_GAIN_CHG_FV;
		///加:汇兑净收益
		TQCVDMoneyType	PLUS_NET_GAIN_FX_TRANS;
		///其他业务收入
		TQCVDMoneyType	OTHER_BUS_INC;
		///资产处置收益
		TQCVDMoneyType	ASSET_DISPOSAL_INCOME;
		///其他收益
		TQCVDMoneyType	OTHER_INCOME;
		///营业支出
		TQCVDMoneyType	OPER_EXP;
		///减:营业税金及附加
		TQCVDMoneyType	LESS_TAXES_SURCHARGES_OPS;
		///减:管理费用
		TQCVDMoneyType	LESS_GERL_ADMIN_EXP;
		///信用减值损失
		TQCVDMoneyType	CREDIT_IMPAIRMENT_LOSS;
		///减:资产减值损失
		TQCVDMoneyType	LESS_IMPAIR_LOSS_ASSETS;
		///其他资产减值损失
		TQCVDMoneyType	OTHER_IMPAIR_LOSS_ASSETS;
		///其他业务成本
		TQCVDMoneyType	OTHER_BUS_COST;
		///营业利润
		TQCVDMoneyType	OPER_PROFIT;
		///加:营业外收入
		TQCVDMoneyType	PLUS_NON_OPER_REV;
		///减:营业外支出
		TQCVDMoneyType	LESS_NON_OPER_EXP;
		///利润总额
		TQCVDMoneyType	TOT_PROFIT;
		///所得税
		TQCVDMoneyType	INC_TAX;
		///净利润(含少数股东损益)
		TQCVDMoneyType	NET_PROFIT_INCL_MIN_INT_INC;
		///净利润(不含少数股东损益)
		TQCVDMoneyType	NET_PROFIT_EXCL_MIN_INT_INC;
		///扣除非经常性损益后净利润（扣除少数股东损益）
		TQCVDMoneyType	NET_PROFIT_AFTER_DED_NR_LP;
		///持续经营净利润
		TQCVDMoneyType	CONTINUED_NET_PROFIT;
		///基本每股收益
		TQCVDMoneyType	S_FA_EPS_BASIC;
		///稀释每股收益
		TQCVDMoneyType	S_FA_EPS_DILUTED;
		///其他综合收益
		TQCVDMoneyType	OTHER_COMPREH_INC;
		///综合收益总额(母公司)
		TQCVDMoneyType	TOT_COMPREH_INC_PARENT_COMP;
		///综合收益总额(少数股东)
		TQCVDMoneyType	TOT_COMPREH_INC_MIN_SHRHLDR;
		///综合收益总额
		TQCVDMoneyType	TOT_COMPREH_INC;
		///营业总成本
		TQCVDMoneyType	TOT_OPER_COST;
		///减:销售费用
		TQCVDMoneyType	LESS_SELLING_DIST_EXP;
		///减:财务费用
		TQCVDMoneyType	LESS_FIN_EXP;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};
	/// 查询中国A股现金流量表请求包
	struct CQCVDQryAShareCashFlowField
	{
		///交易所代码 
		TQCVDExchangeIDType	ExchangeID;
		///证券代码 
		TQCVDSecurityIDType	SecurityID;
		///起始报告期
		TQCVDDateType	BegDate;
		///结束报告期
		TQCVDDateType	EndDate;
		///每页记录数
		TQCVDCreditVolumeType	PageCount;
		///页定位符
		TQCVDCreditPageLocateType	PageLocate;

	};
	/// 查询中国A股现金流量表应答包
	struct CQCVDAShareCashFlowField
	{
		///证券代码 
		TQCVDSecurityIDType	SecurityID;
		///交易所代码 
		TQCVDExchangeIDType	ExchangeID;
		///报告期
		TQCVDDateType	REPORT_PERIOD;
		///公告日期
		TQCVDDateType	ANN_DT;
		///货币代码
		TQCVDCurrencyIDType	CRNCY_CODE;
		///客户存款和同业存放款项净增加额
		TQCVDMoneyType	NET_INCR_DEP_COB;
		///向中央银行借款净增加额
		TQCVDMoneyType	NET_INCR_LOANS_CENTRAL_BANK;
		///拆入资金净增加额
		TQCVDMoneyType	NET_INCR_LOANS_OTHER_BANK;
		///收到其他与经营活动有关的现金
		TQCVDMoneyType	OTHER_CASH_RECP_RAL_OPER_ACT;
		///向其他金融机构拆入资金净增加额
		TQCVDMoneyType	NET_INCR_FUND_BORR_OFI;
		///收取利息和手续费净增加额
		TQCVDMoneyType	NET_INCR_INT_HANDLING_CHRG;
		///经营活动现金流入小计
		TQCVDMoneyType	STOT_CASH_INFLOWS_OPER_ACT;
		///客户贷款及垫款净增加额
		TQCVDMoneyType	NET_INCR_CLIENTS_LOAN_ADV;
		///存放央行和同业款项净增加额
		TQCVDMoneyType	NET_INCR_DEP_CBOB;
		///拆出资金净增加额
		TQCVDMoneyType	S_DISMANTLE_CAPITAL_ADD_NET;
		///支付手续费的现金
		TQCVDMoneyType	HANDLING_CHRG_PAID;
		///支付给职工以及为职工支付的现金
		TQCVDMoneyType	CASH_PAY_BEH_EMPL;
		///支付的各项税费
		TQCVDMoneyType	PAY_ALL_TYP_TAX;
		///支付其他与经营活动有关的现金
		TQCVDMoneyType	OTHER_CASH_PAY_RAL_OPER_ACT;
		///经营活动现金流出小计
		TQCVDMoneyType	STOT_CASH_OUTFLOWS_OPER_ACT;
		///经营活动产生的现金流量净额
		TQCVDMoneyType	NET_CASH_FLOWS_OPER_ACT;
		///吸收投资收到的现金
		TQCVDMoneyType	CASH_RECP_CAP_CONTRIB;
		///其中:子公司吸收少数股东投资收到的现金
		TQCVDMoneyType	INCL_CASH_REC_SAIMS;
		///收回投资收到的现金
		TQCVDMoneyType	CASH_RECP_DISP_WITHDRWL_INVEST;
		///取得投资收益收到的现金
		TQCVDMoneyType	CASH_RECP_RETURN_INVEST;
		///处置固定资产、无形资产和其他长期资产收回的现金净额
		TQCVDMoneyType	NET_CASH_RECP_DISP_FIOLTA;
		///投资活动现金流入小计
		TQCVDMoneyType	STOT_CASH_INFLOWS_INV_ACT;
		///投资支付的现金
		TQCVDMoneyType	CASH_PAID_INVEST;
		///购建固定资产、无形资产和其他长期资产支付的现金
		TQCVDMoneyType	CASH_PAY_ACQ_CONST_FIOLTA;
		///支付其他与筹资活动有关的现金
		TQCVDMoneyType	OTHER_CASH_PAY_RAL_FNC_ACT;
		///投资活动现金流出小计
		TQCVDMoneyType	STOT_CASH_OUTFLOWS_INV_ACT;
		///投资活动产生的现金流量净额
		TQCVDMoneyType	NET_CASH_FLOWS_INV_ACT;
		///发行债券收到的现金
		TQCVDMoneyType	PROC_ISSUE_BONDS;
		///筹资活动现金流入小计
		TQCVDMoneyType	STOT_CASH_INFLOWS_FNC_ACT;
		///偿还债务支付的现金
		TQCVDMoneyType	CASH_PREPAY_AMT_BORR;
		///分配股利、利润或偿付利息支付的现金
		TQCVDMoneyType	CASH_PAY_DIST_DPCP_INT_EXP;
		///其中:子公司支付给少数股东的股利、利润
		TQCVDMoneyType	INCL_DVD_PROFIT_PAID_SC_MS;
		///筹资活动现金流出小计
		TQCVDMoneyType	STOT_CASH_OUTFLOWS_FNC_ACT;
		///筹资活动产生的现金流量净额
		TQCVDMoneyType	NET_CASH_FLOWS_FNC_ACT;
		///汇率变动对现金的影响
		TQCVDMoneyType	EFF_FX_FLU_CASH;
		///现金及现金等价物净增加额
		TQCVDMoneyType	NET_INCR_CASH_CASH_EQU;
		///期初现金及现金等价物余额
		TQCVDMoneyType	CASH_CASH_EQU_BEG_PERIOD;
		///期末现金及现金等价物余额
		TQCVDMoneyType	CASH_CASH_EQU_END_PERIOD;
		///净利润
		TQCVDMoneyType	NET_PROFIT;
		///加:资产减值准备
		TQCVDMoneyType	PLUS_PROV_DEPR_ASSETS;
		///固定资产折旧、油气资产折耗、生产性生物资产折旧
		TQCVDMoneyType	DEPR_FA_COGA_DPBA;
		///无形资产摊销
		TQCVDMoneyType	AMORT_INTANG_ASSETS;
		///长期待摊费用摊销
		TQCVDMoneyType	AMORT_LT_DEFERRED_EXP;
		///处置固定、无形资产和其他长期资产的损失
		TQCVDMoneyType	LOSS_DISP_FIOLTA;
		///固定资产报废损失
		TQCVDMoneyType	LOSS_SCR_FA;
		///公允价值变动损失
		TQCVDMoneyType	LOSS_FV_CHG;
		///财务费用
		TQCVDMoneyType	FIN_EXP;
		///投资损失
		TQCVDMoneyType	INVEST_LOSS;
		///递延所得税资产减少
		TQCVDMoneyType	DECR_DEFERRED_INC_TAX_ASSETS;
		///递延所得税负债增加
		TQCVDMoneyType	INCR_DEFERRED_INC_TAX_LIAB;
		///存货的减少
		TQCVDMoneyType	DECR_INVENTORIES;
		///经营性应收项目的减少
		TQCVDMoneyType	DECR_OPER_PAYABLE;
		///经营性应付项目的增加
		TQCVDMoneyType	INCR_OPER_PAYABLE;
		///待摊费用减少
		TQCVDMoneyType	DECR_DEFERRED_EXP;
		///预提费用增加
		TQCVDMoneyType	INCR_ACC_EXP;
		///间接法-经营活动产生的现金流量净额
		TQCVDMoneyType	IM_NET_CASH_FLOWS_OPER_ACT;
		///现金的期末余额
		TQCVDMoneyType	END_BAL_CASH;
		///减:现金的期初余额
		TQCVDMoneyType	LESS_BEG_BAL_CASH;
		///加:现金等价物的期末余额
		TQCVDMoneyType	PLUS_END_BAL_CASH_EQU;
		///减:现金等价物的期初余额
		TQCVDMoneyType	LESS_BEG_BAL_CASH_EQU;
		///间接法-现金及现金等价物净增加额
		TQCVDMoneyType	IM_NET_INCR_CASH_CASH_EQU;
		///债务转为资本
		TQCVDMoneyType	CONV_DEBT_INTO_CAP;
		///一年内到期的可转换公司债券
		TQCVDMoneyType	CONV_CORP_BONDS_DUE_WITHIN_1Y;
		///融资租入固定资产
		TQCVDMoneyType	FA_FNC_LEASES;
		///其他会计科目
		TQCVDMoneyType	OTHER_ACCOUNTS;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};

	/// 查询行业板块分类信息请求包
	struct CQCVDQryIndustryClassInfoField
	{
		///行业类型
		TQCVDIndustryTypeType	Type;
		///级数:1~4级,0-全部级数
		TQCVDLevelNumType	LevelNum;
		///板块代码 
		TQCVDSectorCodeType	Code;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 查询行业板块分类信息应答包
	struct CQCVDIndustryClassInfoField
	{
		///板块代码 
		TQCVDSectorCodeType	Code;
		///板块名称
		TQCVDSectorNameType	Name;
		///级数:1~4级,0-全部级数
		TQCVDLevelNumType	LevelNum;
		///上级板块代码
		TQCVDSectorCodeType	ParentCode;
		///是否使用
		TQCVDSectorUsedType	Used;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};


	/// 期权查询历史委托数据请求包
	struct CQCVDOptionQryHistoryOrderField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;
		///起始日期
		TQCVDOptionDateType	BegDate;
		///结束日期
		TQCVDOptionTimeType	EndDate;
		///合约代码
		TQCVDOptionSecurityIDType	VarietyCode;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOptionActionFlagType	OrderType;
		///每页记录数
		TQCVDOptionVolumeType	PageCount;
		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
	};
	/// 期权查询历史委托数据应答包
	struct CQCVDOptionHistoryOrderField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;
		///证券代码
		TQCVDOptionSecurityIDType	SecurityID;
		///流水号
		TQCVDOptionLocalIDType	LocalID;
		///交易日期
		TQCVDOptionDateType	TradingDay;
		///内部委托号
		TQCVDOptionInteriorEntrustIDType	InteriorEntrustID;
		///本地报单编号（申报流水号）
		TQCVDOptionOrderLocalIDType	OrderLocalID;
		///营业部
		TQCVDOptionDepartmentIDType	DepartmentID;
		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;
		///股东账户代码
		TQCVDOptionShareholderIDType	ShareholderID;
		///合约代码
		TQCVDOptionSecurityIDType	VarietyCode;
		///合约名称
		TQCVDOptionHisSecurityNameType	VarietyName;
		///证券类别
		TQCVDOptionHisSecurityTypeType	SecurityType;
		///证券简称
		TQCVDOptionHisSecurityNameType	SecurityName;
		///期权类别
		TQCVDOptionOptionsTypeType	OptionType;
		///买卖方向
		TQCVDOptionDirectionType	Direction;
		///开平标志
		TQCVDOptionOpenCloseType	OpenCloseType;
		///备兑标签
		TQCVDOptionCoverFlagType	PreparationFlag;
		///撤单(修改)标志,O-委托,W-撤单,M-修改订单
		TQCVDOptionCancelFlagType	CancelFlag;
		///撤销(修改)对应的委托号
		TQCVDOptionCancelEntrustIDType	CancelEntrustID;
		///订单类别
		TQCVDOptionOrderTypeType	OrderType;
		///委托数量
		TQCVDOptionVolumeType	VolumeTotal;
		///委托价格
		TQCVDOptionPriceType	EntrustPrice;
		///发起方(1:本地,2:TA,-1:确认失败)
		TQCVDOptionSponsorType	Sponsor;
		///委托日期
		TQCVDOptionDateType	EntrustDate;
		///委托时间
		TQCVDOptionTimeType	EntrustTime;
		///申报日期
		TQCVDOptionDateType	OrderDate;
		///申报时间
		TQCVDOptionTimeType	OrderTime;
		///交易所订单编号
		TQCVDOptionExchangeTradeIDType	ExchangeTradeID;
		///申报结果
		TQCVDOptionOrderStatusType	OrderStatus;
		///结果说明
		TQCVDOptionResultCommentType	ResultComment;
		///撤单数量
		TQCVDOptionVolumeType	VolumeCancel;
		///已成交数量
		TQCVDOptionVolumeType	VolumeTraded;
		///成交金额
		TQCVDOptionMoneyType	Turnover;
		///本日成交数量
		TQCVDOptionVolumeType	TodayVolumeTraded;
		///成交均价
		TQCVDOptionPriceType	AveragePrice;
		///成交时间
		TQCVDOptionTimeType	TradeTime;
		///币种类型
		TQCVDOptionHisCurrencyIDType	Currency;
		///冻结资金
		TQCVDOptionMoneyType	FrozenFund;
		///结算资金
		TQCVDOptionMoneyType	SettleFund;
		///委托方式
		TQCVDOptionEntrustModeType	EntrustMode;
		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
		///总页数
		TQCVDOptionPageLocateType	PageTotal;
	};
	/// 期权查询历史交割单请求包
	struct CQCVDOptionQryHistoryDeliveryField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;
		///起始日期
		TQCVDOptionDateType	BegDate;
		///结束日期
		TQCVDOptionDateType	EndDate;
		///合约代码
		TQCVDOptionSecurityIDType	VarietyCode;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOptionActionFlagType	OrderType;
		///每页记录数
		TQCVDOptionVolumeType	PageCount;
		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
	};
	/// 期权查询历史交割应答包
	struct CQCVDOptionHistoryDeliveryField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;
		///股东账户代码
		TQCVDOptionShareholderIDType	ShareholderID;
		///证券代码
		TQCVDOptionSecurityIDType	SecurityID;
		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;
		///币种类型
		TQCVDOptionHisCurrencyIDType	Currency;
		///营业部
		TQCVDOptionDepartmentIDType	DepartmentID;
		///合约代码
		TQCVDOptionSecurityIDType	VarietyCode;
		///合约名称
		TQCVDOptionHisSecurityNameType	VarietyName;
		///证券类别
		TQCVDOptionHisSecurityTypeType	SecurityType;
		///期权类型
		TQCVDOptionOptionsTypeType	OptionType;
		///买卖方向
		TQCVDOptionDirectionType	Direction;
		///开平标志
		TQCVDOptionOpenCloseType	OpenCloseType;
		///备兑标签
		TQCVDOptionCoverFlagType	PreparationFlag;
		///成交日期
		TQCVDOptionDateType	TradeDate;
		///成交时间
		TQCVDOptionTimeType	TradeTime;
		///成交数量
		TQCVDOptionVolumeType	Volume;
		///成交价格
		TQCVDOptionPriceType	Price;
		///结算价
		TQCVDOptionPriceType	ClosePrice;
		///成交金额
		TQCVDOptionMoneyType	Turnover;
		///实收佣金
		TQCVDOptionMoneyType	ActualBrokerage;
		///印花税
		TQCVDOptionMoneyType	StampTax;
		///过户费
		TQCVDOptionMoneyType	TransferFee;
		///附加费
		TQCVDOptionMoneyType	AdditionalFee;
		///结算费
		TQCVDOptionMoneyType	settleFee;
		///交易规费
		TQCVDOptionMoneyType	TransactionFee;
		///实付金额
		TQCVDOptionMoneyType	ActuallyPayMoney;
		///本次资金余额
		TQCVDOptionMoneyType	FundBalance;
		///实际冻结(解冻)金额
		TQCVDOptionMoneyType	ActuallyFrozenMoney;
		///实收(付)股份
		TQCVDOptionVolumeType	ActuallyPayStock;
		///本次股份余额
		TQCVDOptionVolumeType	StockBalance;
		///实际冻结(解冻)股份
		TQCVDOptionVolumeType	ActuallyFrozenStock;
		///本次股份冻结余额
		TQCVDOptionVolumeType	StockFrozenBalance;
		///流水号
		TQCVDLongSequenceType	SerialNo;
		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
		///总页数
		TQCVDOptionPageLocateType	PageTotal;
	};
	/// 期权查询组合策略持仓请求包
	struct CQCVDOptionQryCombinationStrategyHoldField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;
		///起始日期
		TQCVDOptionDateType	BegDate;
		///结束日期
		TQCVDOptionDateType	EndDate;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOptionActionFlagType	OrderType;
		///每页记录数
		TQCVDOptionVolumeType	PageCount;
		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
	};
	/// 期权查询组合策略持仓应答包
	struct CQCVDOptionCombinationStrategyHoldField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;
		///营业部
		TQCVDOptionDepartmentIDType	DepartmentID;
		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;
		///股东账户代码
		TQCVDOptionShareholderIDType	ShareholderID;
		///买卖方向1
		TQCVDOptionDirectionType	Direction1;
		///备兑标签1
		TQCVDOptionCoverFlagType	PreparationFlag1;
		///合约代码1
		TQCVDOptionSecurityIDType	VarietyCode1;
		///买卖方向2
		TQCVDOptionDirectionType	Direction2;
		///备兑标签2
		TQCVDOptionCoverFlagType	PreparationFlag2;
		///合约代码2
		TQCVDOptionSecurityIDType	VarietyCode2;
		///买卖方向3
		TQCVDOptionDirectionType	Direction3;
		///备兑标签3
		TQCVDOptionCoverFlagType	PreparationFlag3;
		///合约代码3
		TQCVDOptionSecurityIDType	VarietyCode3;
		///买卖方向4
		TQCVDOptionDirectionType	Direction4;
		///备兑标签4
		TQCVDOptionCoverFlagType	PreparationFlag4;
		///合约代码4
		TQCVDOptionSecurityIDType	VarietyCode4;
		///COMPUTES估值
		TQCVDOptionMoneyType	Computes;
		///开仓日期
		TQCVDOptionDateType	OpenDate;
		///变动日期
		TQCVDOptionDateType	ChangeDate;
		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
		///总页数
		TQCVDOptionPageLocateType	PageTotal;
	};
	/// 期权查询存管交易申请历史请求包
	struct CQCVDOptionQryHistoryDepositApplyField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;
		///申请起始日期
		TQCVDOptionDateType	ApplyBegDate;
		///申请结束日期
		TQCVDOptionDateType	ApplyEndDate;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOptionActionFlagType	OrderType;
		///每页记录数
		TQCVDOptionVolumeType	PageCount;
		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
	};
	/// 期权查询存管交易申请历史应答包
	struct CQCVDOptionHistoryDepositApplyField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;
		///申请号
		TQCVDLongSequenceType	ApplyNo;
		///发起方
		TQCVDOptionOrganizerType	Organizer;
		///银行代码
		TQCVDOptionHisBankIDType	BankID;
		///存管账户
		TQCVDOptionDepositAcctType	DepositAcct;
		///银行账户
		TQCVDOptionBankAcctType	BankAcct;
		///币种类型
		TQCVDOptionHisCurrencyIDType	Currency;
		///转账金额
		TQCVDOptionMoneyType	TransferAmount;
		///发生营业部
		TQCVDOptionDepartmentIDType	TransferDepartment;
		///申请日期
		TQCVDOptionDateType	applyDate;
		///申请时间
		TQCVDOptionTimeType	applyTime;
		///处理日期
		TQCVDOptionDateType	OperateDate;
		///生成时间
		TQCVDOptionTimeType	CreateTime;
		///处理结果
		TQCVDOptionDealResultType	DealResult;
		///结果说明
		TQCVDOptionResultExplainType	ResultExplain;
		///外部处理结果
		TQCVDOptionExternalDealResultType	ExternalDealResult;
		///本次资金余额
		TQCVDOptionMoneyType	FundBalances;
		///营业部
		TQCVDOptionDepartmentIDType	DepartmentID;
		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
		///总页数
		TQCVDOptionPageLocateType	PageTotal;
	};
	/// 期权查询资金明细历史请求包
	struct CQCVDOptionQryHistoryMoneyDetailField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;
		///起始日期
		TQCVDOptionDateType	BegDate;
		///结束日期
		TQCVDOptionDateType	EndDate;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOptionActionFlagType	OrderType;
		///每页记录数
		TQCVDOptionVolumeType	PageCount;
		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
	};
	/// 期权查询资金明细历史应答包
	struct CQCVDOptionHistoryMoneyDetailField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;
		///资金账户
		TQCVDOptionFundsAcctType	FundsAcct;
		///科目
		TQCVDOptionSubjectType	Subject;
		///币种类型
		TQCVDOptionHisCurrencyIDType	Currency;
		///收入
		TQCVDOptionMoneyType	Income;
		///付出
		TQCVDOptionMoneyType	Give;
		///发生后资金余额
		TQCVDOptionMoneyType	HappenFundsAcct;
		///营业部
		TQCVDOptionDepartmentIDType	DepartmentID;
		///流水号
		TQCVDLongSequenceType	LocalID;
		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
		///总页数
		TQCVDOptionPageLocateType	PageTotal;
	};
	/// 期权查询资金信息历史请求包
	struct CQCVDOptionQryHistoryMoneyInfoField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;
		///起始日期
		TQCVDOptionDateType	BegDate;
		///结束日期
		TQCVDOptionDateType	EndDate;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOptionActionFlagType	OrderType;
		///每页记录数
		TQCVDOptionVolumeType	PageCount;
		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
	};
	/// 期权查询资金信息历史应答包
	struct CQCVDOptionHistoryMoneyInfoField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;
		///营业部
		TQCVDOptionDepartmentIDType	DepartmentID;
		///资金账户
		TQCVDOptionFundsAcctType	MoneyAcct;
		///币种类型
		TQCVDOptionHisCurrencyIDType	Currency;
		///当日开仓
		TQCVDOptionVolumeType	OpenPositionThatDay;
		///当日平仓
		TQCVDOptionVolumeType	ClosePositionThatDay;
		///保证金
		TQCVDOptionMoneyType	SecurityDeposit;
		///期权市值
		TQCVDOptionMoneyType	OptionMarketValue;
		///替代金额
		TQCVDOptionMoneyType	ReplaceAmount;
		///资金余额
		TQCVDOptionMoneyType	AmountBalance;
		///资金保证金
		TQCVDOptionMoneyType	AmontSecurityDeposit;
		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
		///总页数
		TQCVDOptionPageLocateType	PageTotal;
	};
	/// 期权查询行情历史请求包
	struct CQCVDOptionQryHistoryQuotationsField
	{
		///起始日期
		TQCVDOptionDateType	BegDate;
		///结束日期
		TQCVDOptionDateType	EndDate;
		///合约代码
		TQCVDOptionSecurityIDType	VarietyCode;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOptionActionFlagType	OrderType;
		///每页记录数
		TQCVDOptionVolumeType	PageCount;
		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
	};
	/// 期权查询行情历史应答包
	struct CQCVDOptionHistoryQuotationsField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;
		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;
		///合约代码
		TQCVDOptionSecurityIDType	VarietyCode;
		///最新价
		TQCVDOptionPriceType	NewPrice;
		///结算价
		TQCVDOptionPriceType	SettlePrice;
		///合约前收盘价
		TQCVDOptionPriceType	PreContClosingPrice;
		///合约前结算价
		TQCVDOptionPriceType	PreContrSettlePrice;
		///今开盘
		TQCVDOptionPriceType	PresentOpening;
		///最高价
		TQCVDOptionPriceType	HigestPrice;
		///最低价
		TQCVDOptionPriceType	LowestPrice;
		///成交数量
		TQCVDOptionVolumeType	DealVolume;
		///成交金额
		TQCVDOptionMoneyType	DealAmount;
		///合约名称
		TQCVDOptionHisSecurityNameType	VarietyName;
		///证券代码
		TQCVDOptionSecurityIDType	SecurityID;
		///标的证券最新价
		TQCVDOptionPriceType	SecurityNewPrice;
		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
		///总页数
		TQCVDOptionPageLocateType	PageTotal;
	};
	/// 期权查询合约持仓请求包
	struct CQCVDOptionQryHistoryVarietyHoldField
	{
		///起始日期
		TQCVDOptionDateType	BegDate;
		///结束日期
		TQCVDOptionDateType	EndDate;
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;
		///合约代码
		TQCVDOptionSecurityIDType	VarietyCode;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOptionActionFlagType	OrderType;
		///每页记录数
		TQCVDOptionVolumeType	PageCount;
		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
	};
	/// 期权查询合约持仓应答包
	struct CQCVDOptionHistoryVarietyHoldField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;
		///营业部
		TQCVDOptionDepartmentIDType	DepartmentID;
		///股东账户代码
		TQCVDOptionShareholderIDType	ShareholderID;
		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;
		///币种类型
		TQCVDOptionHisCurrencyIDType	Currency;
		///合约代码
		TQCVDOptionSecurityIDType	VarietyCode;
		///合约名称
		TQCVDOptionHisSecurityNameType	VarietyName;
		///期权类别
		TQCVDOptionOptionsTypeType	OptionType;
		///证券类别
		TQCVDOptionHisSecurityTypeType	SecurityType;
		///证券代码
		TQCVDOptionSecurityIDType	SecurityID;
		///证券简称
		TQCVDOptionHisSecurityNameType	SecurityName;
		///买卖方向
		TQCVDOptionDirectionType	Direction;
		///备兑标签
		TQCVDOptionCoverFlagType	PreparationFlag;
		///数量
		TQCVDOptionVolumeType	Volume;
		///非流通数量
		TQCVDOptionVolumeType	UncirculatedVolume;
		///开仓委托数量
		TQCVDOptionVolumeType	OpenEntrustVolume;
		///平仓委托数量
		TQCVDOptionVolumeType	CloseEntrustVolume;
		///开仓成交数量
		TQCVDOptionVolumeType	OpenDoneVolume;
		///平仓成交数量
		TQCVDOptionVolumeType	CloseDoneVolume;
		///开仓成交金额
		TQCVDOptionMoneyType	OpenDoneAmount;
		///平仓成交金额
		TQCVDOptionMoneyType	CloseDoneAmount;
		///开仓日期
		TQCVDOptionDateType	OpenDate;
		///变动日期
		TQCVDOptionDateType	ChangeDate;
		///开仓金额
		TQCVDOptionMoneyType	OpenAmount;
		///开仓数量
		TQCVDOptionVolumeType	OpenVolume;
		///平仓金额
		TQCVDOptionMoneyType	CloseAmount;
		///平仓数量
		TQCVDOptionVolumeType	CloseVolume;
		///持仓成本
		TQCVDOptionMoneyType	HoldCost;
		///摊薄成本
		TQCVDOptionMoneyType	DilutedCost;
		///累计盈亏
		TQCVDOptionMoneyType	TotalProfit;
		///行权盈亏
		TQCVDOptionMoneyType	StrikeProfit;
		///保证金
		TQCVDOptionMoneyType	Margin;
		///买入成本
		TQCVDOptionMoneyType	BuyCost;
		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
		///总页数
		TQCVDOptionPageLocateType	PageTotal;
	};

	/// 查询集中交易系统资金请求
	struct CQCVDOptionReqInquiryJZFundField
	{
		///资金账户代码
		TQCVDOptionAccountIDType	AccountID;

		///币种
		TQCVDOptionCurrencyIDType	CurrencyID;

		///一级机构代码
		TQCVDOptionDepartmentIDType	DepartmentID;

		///投资者代码
		TQCVDInvestorIDType	InvestorID;
	};

	/// 查询集中交易系统资金响应
	struct CQCVDOptionRspInquiryJZFundField
	{
		///资金账户代码
		TQCVDOptionAccountIDType	AccountID;

		///币种
		TQCVDOptionCurrencyIDType	CurrencyID;

		///可用金额
		TQCVDOptionMoneyType	UsefulMoney;

		///可取额度
		TQCVDOptionMoneyType	FetchLimit;

		///一级机构代码
		TQCVDOptionDepartmentIDType	DepartmentID;
	};

	/// 查询投资者
	struct CQCVDOptionQryInvestorField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;
	};

	/// 投资者
	struct CQCVDOptionInvestorField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///经纪公司部门代码
		TQCVDOptionDepartmentIDType	DepartmentID;

		///投资者类型
		TQCVDOptionInvestorTypeType	InvestorType;

		///投资者名称
		TQCVDOptionInvestorNameType	InvestorName;

		///证件类型
		TQCVDOptionIdCardTypeType	IdCardType;

		///证件号码
		TQCVDOptionIdCardNoType	IdCardNo;

		///合同编号
		TQCVDOptionContractNoType	ContractNo;

		///预留字段1
		TQCVDOptionBranchIDType	Reserve1;

		///投资者分级类别
		TQCVDOptionInvestorLevelType	InvestorLevel;

		///预留字段2
		TQCVDOptionRemarkType	Reserve2;

		///委托方式
		TQCVDOptionOperwaysType	Operways;

		///投资者保证金提取比例
		TQCVDOptionRatioType	MarginFetchRatio;

		///佣金模板代码
		TQCVDOptionTradingFeeTemplateIDType	TradingFeeTemplateID;

		///保证金模板代码
		TQCVDOptionMarginFeeTemplateIDType	MarginFeeTemplateID;

		///风控参数模板代码
		TQCVDOptionRiskParamTemplateIDType	RiskParamTemplateID;

		///专业投资者类别
		TQCVDOptionProfInvestorTypeType	ProfInvestorType;

		///初始、启用、禁用、激活等
		TQCVDOptionActiveStatusType	Status;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询User
	struct CQCVDOptionQryUserField
	{
		///用户代码
		TQCVDOptionUserIDType	UserID;

		///用户类型
		TQCVDOptionUserTypeType	UserType;
	};

	/// User
	struct CQCVDOptionUserField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///用户代码
		TQCVDOptionUserIDType	UserID;

		///用户名称
		TQCVDOptionUserNameType	UserName;

		///用户类型
		TQCVDOptionUserTypeType	UserType;

		///经纪公司部门代码
		TQCVDOptionDepartmentIDType	DepartmentID;

		///登录限制
		TQCVDOptionLoginLimitType	LoginLimit;

		///密码连续输入错误限制
		TQCVDOptionLoginLimitType	PasswordFailLimit;

		///状态
		TQCVDOptionActiveStatusType	Status;

		///联系人
		TQCVDOptionContacterType	Contacter;

		///传真
		TQCVDOptionFaxType	Fax;

		///联系电话
		TQCVDOptionTelephoneType	Telephone;

		///电子邮件
		TQCVDOptionEmailType	Email;

		///通讯地址
		TQCVDOptionAddressType	Address;

		///邮政编码
		TQCVDOptionZipCodeType	ZipCode;

		///开户日期
		TQCVDOptionDateType	OpenDate;

		///销户日期
		TQCVDOptionDateType	CloseDate;

		///通讯流量
		TQCVDOptionCommFluxType	OrderInsertCommFlux;

		///撤单流控
		TQCVDOptionCommFluxType	OrderActionCommFlux;

		///上证通讯流量
		TQCVDOptionCommFluxType	SSEOrderInsertCommFlux;

		///上证撤单流控
		TQCVDOptionCommFluxType	SSEOrderActionCommFlux;

		///深证通讯流量
		TQCVDOptionCommFluxType	SZSEOrderInsertCommFlux;

		///深证撤单流控
		TQCVDOptionCommFluxType	SZSEOrderActionCommFlux;

		///手机
		TQCVDOptionMobileType	Mobile;

		///加密方式
		TQCVDOptionEncodeModeType	PasswordEncodeMode;

		///密码修改周期(天)
		TQCVDOptionCountType	PasswordUpdatePeriod;

		///密码有效剩余天数
		TQCVDOptionCountType	PasswordRemainDays;

		///是否需要改密
		TQCVDOptionBoolType	NeedUpdatePassword;

		///密码
		TQCVDOptionUserPasswordType	UserPassword;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询股东账户
	struct CQCVDOptionQryShareholderAccountField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///交易所下的交易市场，如沪A、沪B市场
		TQCVDOptionMarketIDType	MarketID;

		///存放上证所和深圳的股东代码。
		TQCVDOptionShareholderIDType	ShareholderID;

		///普通，信用，衍生品等
		TQCVDOptionShareholderIDTypeType	ShareholderIDType;
	};

	/// 股东账户
	struct CQCVDOptionShareholderAccountField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///客户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///交易编码类型
		TQCVDOptionShareholderIDTypeType	ShareholderIDType;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///交易所营业部编码
		TQCVDOptionBranchIDType	BranchID;

		///交易权限模板
		TQCVDOptionTradingRightTemplateIDType	TradingRightTemplateID;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询资金账户
	struct CQCVDOptionQryTradingAccountField
	{
		///资金账户所属投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///币种
		TQCVDOptionCurrencyIDType	CurrencyID;

		///资金账户
		TQCVDOptionAccountIDType	AccountID;

		///普通、信用、衍生品等
		TQCVDOptionAccountTypeType	AccountType;

		///经纪公司部门代码
		TQCVDOptionDepartmentIDType	DepartmentID;
	};

	/// 资金账户
	struct CQCVDOptionTradingAccountField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///部门代码
		TQCVDOptionDepartmentIDType	DepartmentID;

		///资金账户代码
		TQCVDOptionAccountIDType	AccountID;

		///币种代码
		TQCVDOptionCurrencyIDType	CurrencyID;

		///资金账户类型
		TQCVDOptionAccountTypeType	AccountType;

		///上日结存
		TQCVDOptionMoneyType	PreDeposit;

		///昨行权待交收冻结资金
		TQCVDOptionMoneyType	PreFrozenCash;

		///可用资金
		TQCVDOptionMoneyType	UsefulMoney;

		///可取资金
		TQCVDOptionMoneyType	FetchLimit;

		///入金金额
		TQCVDOptionMoneyType	Deposit;

		///出金金额
		TQCVDOptionMoneyType	Withdraw;

		///冻结的保证金
		TQCVDOptionMoneyType	FrozenMargin;

		///冻结的资金
		TQCVDOptionMoneyType	FrozenCash;

		///冻结的手续费
		TQCVDOptionMoneyType	FrozenCommission;

		///当前保证金总额
		TQCVDOptionMoneyType	CurrMargin;

		///手续费
		TQCVDOptionMoneyType	Commission;

		///权利金收入
		TQCVDOptionMoneyType	RoyaltyIn;

		///权利金支出
		TQCVDOptionMoneyType	RoyaltyOut;

		///资金账户所属投资者代码
		TQCVDOptionInvestorIDType	AccountOwner;

		///签约银行账户
		TQCVDOptionBankAccountIDType	BankAccountID;

		///银行代码
		TQCVDOptionBankIDType	BankID;

		///当日行权待交收冻结资金
		TQCVDOptionMoneyType	ExcerciseFrozen;

		///公司实时风险度
		TQCVDOptionRatioType	RiskLivePercent;

		///实时风险级别
		TQCVDOptionRiskLevelType	RiskLiveLevel;

		///实时可用资金
		TQCVDOptionMoneyType	LiveUsefulMoney;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询报单
	struct CQCVDOptionQryOrderField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///合约在系统中的编号
		TQCVDOptionSecurityIDType	SecurityID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///交易所下的交易市场，如沪A、沪B市场
		TQCVDOptionMarketIDType	MarketID;

		///客户在系统中的编号，编号唯一且遵循交易所制定的编码规则
		TQCVDOptionShareholderIDType	ShareholderID;

		///报单编号
		TQCVDOptionOrderSysIDType	OrderSysID;

		///Time
		TQCVDOptionTimeType	InsertTimeStart;

		///Time
		TQCVDOptionTimeType	InsertTimeEnd;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;

		///交易所组合编码(单边平仓必填)
		TQCVDOptionExchangeCombIDType ExchangeCombID;

		///排列方式 1正序 2倒序 默认倒序
		TQCVDOptionOrderSortType	OrderType;

		///每页记录数
		TQCVDOptionVolumeType	PageCount;

		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
	};

	/// 报单
	struct CQCVDOptionOrderField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///合约在系统中的编号
		TQCVDOptionSecurityIDType	SecurityID;

		///前置编号
		TQCVDOptionFrontIDType	FrontID;

		///每一位交易员或其它登录者登录系统获得的会话编号，当前时刻唯一
		TQCVDOptionSessionIDType	SessionID;

		///投资者说明的对报单的唯一引用
		TQCVDOptionOrderRefType	OrderRef;

		///报单编号
		TQCVDOptionOrderSysIDType	OrderSysID;

		///交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///本地报单顺序号
		TQCVDOptionOrderLocalIDType	OrderLocalID;

		///限价单或市价单
		TQCVDOptionOrderPriceTypeType	OrderPriceType;

		///买、卖
		TQCVDOptionDirectionType	Direction;

		///按字节表示各单个合约的开平方向
		TQCVDOptionCombOffsetFlagType	CombOffsetFlag;

		///按字节表示各单个合约的组合套保标志
		TQCVDOptionCombHedgeFlagType	CombHedgeFlag;

		///限价单价格
		TQCVDOptionPriceType	Price;

		///报单数量
		TQCVDOptionVolumeType	VolumeTotalOriginal;

		///IOC、GFS、GFD、GTD、GTC、GFA
		TQCVDOptionTimeConditionType	TimeCondition;

		///AV、MV、CV
		TQCVDOptionVolumeConditionType	VolumeCondition;

		///当成交量类型为MV时有效
		TQCVDOptionVolumeType	MinVolume;

		///请求编号
		TQCVDOptionRequestIDType	RequestID;

		///交易所下的交易市场，如沪A、沪B市场
		TQCVDOptionMarketIDType	MarketID;

		///客户在系统中的编号，编号唯一且遵循交易所制定的编码规则
		TQCVDOptionShareholderIDType	ShareholderID;

		///报单操作状态
		TQCVDOptionOrderOperateStatusType	OrderOperateStatus;

		///核心已处理、交易所已接收、部分成交、全部成交、部撤、全撤、废单等
		TQCVDOptionOrderStatusType	OrderStatus;

		///报单完成数量
		TQCVDOptionVolumeType	VolumeTraded;

		///报单日期
		TQCVDOptionDateType	InsertDate;

		///委托时间
		TQCVDOptionTimeType	InsertTime;

		///交易所接收时间
		TQCVDOptionTimeType	AcceptTime;

		///撤销时间
		TQCVDOptionTimeType	CancelTime;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///状态信息
		TQCVDOptionStatusMsgType	StatusMsg;

		///用户强评标志
		TQCVDOptionBoolType	UserForceClose;

		///申报操作员
		TQCVDOptionUserIDType	InsertUserID;

		///申报撤销操作员
		TQCVDOptionUserIDType	CancelUserID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///经纪公司部门代码
		TQCVDOptionDepartmentIDType	DepartmentID;

		///资金账户
		TQCVDOptionAccountIDType	AccountID;

		///币种
		TQCVDOptionCurrencyIDType	CurrencyID;

		///内网IP地址
		TQCVDOptionIPAddressType	InnerIPAddress;

		///Mac地址
		TQCVDOptionMacAddressType	MacAddress;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;

		///强平原因
		TQCVDOptionForceCloseReasonType	ForceCloseReason;

		///终端信息
		TQCVDOptionTerminalInfoType	TerminalInfo;

		///委托方式
		TQCVDOptionOperwayType	Operway;

		///硬盘序列号
		TQCVDOptionHDSerialType	HDSerial;

		///交易所返回的撤单数量
		TQCVDOptionVolumeType	VolumeCanceled;

		///交易所组合编码(单边平仓必填)
		TQCVDOptionExchangeCombIDType	ExchangeCombID;

		///外网IP地址
		TQCVDOptionIPAddressType	OuterIPAddress;

		///外网端口号
		TQCVDOptionPortType	OuterPort;

		///成交金额
		TQCVDOptionMoneyType	TradeAmount;

		///移动设备手机号
		TQCVDOptionMobileType	Mobile;

		///交易所为营业部分配的代码
		TQCVDOptionBranchIDType	BranchID;

		///普通、信用、衍生品
		TQCVDOptionShareholderIDTypeType	ShareholderIDType;

		///度量索引
		TQCVDOptionMeasureIndexType	MeasureIndex;

		///系统错误代码
		TQCVDOptionErrorIDType	ErrorID;

		///投资者类型
		TQCVDOptionInvestorTypeType	InvestorType;

		///记录序号(仅上证报盘使用)
		TQCVDOptionSequenceNoType	RecordNumber;

		///申报时间(毫秒)
		TQCVDOptionMillisecType	InsertMillisec;

		///节点号
		TQCVDOptionNodeIDType	ServerID;

		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
	};

	/// 查询成交
	struct CQCVDOptionQryTradeField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///合约在系统中的编号
		TQCVDOptionSecurityIDType	SecurityID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///交易所下的交易市场，如沪A、沪B市场
		TQCVDOptionMarketIDType	MarketID;

		///客户在系统中的编号，编号唯一且遵循交易所制定的编码规则
		TQCVDOptionShareholderIDType	ShareholderID;

		///成交编号
		TQCVDOptionTradeIDType	TradeID;

		///Time
		TQCVDOptionTimeType	TradeTimeStart;

		///Time
		TQCVDOptionTimeType	TradeTimeEnd;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///排列方式 1正序 2倒序 默认倒序
		TQCVDOptionOrderSortType	OrderType;

		///每页记录数
		TQCVDOptionVolumeType	PageCount;

		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
	};

	/// 成交
	struct CQCVDOptionTradeField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///合约在系统中的编号
		TQCVDOptionSecurityIDType	SecurityID;

		///用户代码
		TQCVDOptionUserIDType	InsertUserID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///成交编号
		TQCVDOptionTradeIDType	TradeID;

		///买、卖
		TQCVDOptionDirectionType	Direction;

		///报单编号
		TQCVDOptionOrderSysIDType	OrderSysID;

		///交易所下的交易市场，如沪A、沪B市场
		TQCVDOptionMarketIDType	MarketID;

		///客户在系统中的编号，编号唯一且遵循交易所制定的编码规则
		TQCVDOptionShareholderIDType	ShareholderID;

		///开仓、平仓等
		TQCVDOptionOffsetFlagType	OffsetFlag;

		///投机、套利等
		TQCVDOptionHedgeFlagType	HedgeFlag;

		///成交价格
		TQCVDOptionPriceType	Price;

		///成交数量
		TQCVDOptionVolumeType	Volume;

		///成交日期
		TQCVDOptionDateType	TradeDate;

		///成交时间
		TQCVDOptionTimeType	TradeTime;

		///交易所交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///本地报单顺序号
		TQCVDOptionOrderLocalIDType	OrderLocalID;

		///交易发生的日期
		TQCVDOptionDateType	TradingDay;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///资金账户
		TQCVDOptionAccountIDType	AccountID;

		///币种
		TQCVDOptionCurrencyIDType	CurrencyID;

		///报单引用
		TQCVDOptionOrderRefType	OrderRef;

		///经纪公司部门代码
		TQCVDOptionDepartmentIDType	DepartmentID;

		///交易所为营业部分配的代码
		TQCVDOptionBranchIDType	BranchID;

		///普通、信用、衍生品
		TQCVDOptionShareholderIDTypeType	ShareholderIDType;

		///节点号
		TQCVDOptionNodeIDType	ServerID;

		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
	};

	/// 查询投资者持仓
	struct CQCVDOptionQryPositionField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///证券代码
		TQCVDOptionSecurityIDType	SecurityID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///交易所下的交易市场，如沪A、沪B市场
		TQCVDOptionMarketIDType	MarketID;

		///交易账户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///多、空
		TQCVDOptionPosiDirectionType	PosiDirection;

		///投机套保标志
		TQCVDOptionHedgeFlagType	HedgeFlag;
	};

	/// 投资者持仓
	struct CQCVDOptionPositionField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///客户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///交易日
		TQCVDOptionDateType	TradingDay;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///合约代码
		TQCVDOptionSecurityIDType	SecurityID;

		///持仓方向
		TQCVDOptionPosiDirectionType	PosiDirection;

		///持仓方向
		TQCVDOptionHedgeFlagType	HedgeFlag;

		///昨日持仓
		TQCVDOptionVolumeType	HistoryPos;

		///昨日持仓冻结
		TQCVDOptionVolumeType	HistoryPosFrozen;

		///今日买卖持仓
		TQCVDOptionVolumeType	TodayPos;

		///今日买卖持仓冻结
		TQCVDOptionVolumeType	TodayPosFrozen;

		///持仓成本
		TQCVDOptionMoneyType	TotalPosCost;

		///多头冻结（买入开仓+买入平仓）的持仓数量
		TQCVDOptionVolumeType	LongFrozen;

		///空头冻结（卖出开仓+卖出平仓）的持仓数量
		TQCVDOptionVolumeType	ShortFrozen;

		///多头报单冻结的金额（不包含手续费、保证金）
		TQCVDOptionMoneyType	LongFrozenAmount;

		///空头报单冻结的金额（不包含手续费、保证金
		TQCVDOptionMoneyType	ShortFrozenAmount;

		///开仓量
		TQCVDOptionVolumeType	OpenVolume;

		///平仓量
		TQCVDOptionVolumeType	CloseVolume;

		///开仓金额
		TQCVDOptionMoneyType	OpenAmount;

		///平仓金额
		TQCVDOptionMoneyType	CloseAmount;

		///占用的保证金
		TQCVDOptionMoneyType	Margin;

		///冻结的保证金
		TQCVDOptionMoneyType	FrozenMargin;

		///冻结的资金
		TQCVDOptionMoneyType	FrozenCash;

		///冻结的手续费
		TQCVDOptionMoneyType	FrozenCommission;

		///资金差额
		TQCVDOptionMoneyType	CashIn;

		///手续费
		TQCVDOptionMoneyType	Commission;

		///执行冻结
		TQCVDOptionVolumeType	StrikeFrozen;

		///执行冻结金额
		TQCVDOptionMoneyType	StrikeFrozenAmount;

		///上次余额(盘中不变)
		TQCVDOptionVolumeType	PrePosition;

		///最新价
		TQCVDOptionPriceType	LastPrice;

		///昨日持仓组合仓位
		TQCVDOptionVolumeType	HistoryCombPos;

		///今日持仓组合仓位
		TQCVDOptionVolumeType	TodayCombPos;

		///昨日组合持仓拆分冻结
		TQCVDOptionVolumeType	HistoryCombPosSplitFrozen;

		///今日组合持仓拆分冻结
		TQCVDOptionVolumeType	TodayCombPosSplitFrozen;

		///昨日持仓组合冻结
		TQCVDOptionVolumeType	HistoryPosCombFrozen;

		///今日持仓组合冻结
		TQCVDOptionVolumeType	TodayPosCombFrozen;

		///买入成本
		TQCVDOptionMoneyType	OpenPosCost;

		///昨日平仓盈亏
		TQCVDOptionMoneyType	PreCloseProfit;

		///当日平仓盈亏
		TQCVDOptionMoneyType	TodayCloseProfit;

		///占用买入额度
		TQCVDOptionMoneyType	BuyQuotaUsed;

		///保留字段
		TQCVDOptionMoneyType	TodayProfit;

		///上次组合持仓余额(盘中不变)
		TQCVDOptionVolumeType	PreCombPosition;

		///昨日持仓成本价
		TQCVDOptionMoneyType	HistoryPosPrice;

		///证券名称
		TQCVDOptionSecurityNameType	SecurityName;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询资金转移流水
	struct CQCVDOptionQryFundTransferDetailField
	{
		///资金账户代码
		TQCVDOptionAccountIDType	AccountID;

		///币种
		TQCVDOptionCurrencyIDType	CurrencyID;

		///转移方向
		TQCVDOptionTransferDirectionType	TransferDirection;

		///经纪公司部门代码
		TQCVDOptionDepartmentIDType	DepartmentID;
	};

	/// 资金转移流水
	struct CQCVDOptionFundTransferDetailField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///转账流水号
		TQCVDOptionIntSerialType	FundSerial;

		///申请流水号
		TQCVDOptionIntSerialType	ApplySerial;

		///前置编号
		TQCVDOptionFrontIDType	FrontID;

		///会话编号
		TQCVDOptionSessionIDType	SessionID;

		///经纪公司部门代码
		TQCVDOptionDepartmentIDType	DepartmentID;

		///资金账户代码
		TQCVDOptionAccountIDType	AccountID;

		///币种
		TQCVDOptionCurrencyIDType	CurrencyID;

		///转移方向
		TQCVDOptionTransferDirectionType	TransferDirection;

		///出入金金额
		TQCVDOptionMoneyType	Amount;

		///转移状态
		TQCVDOptionTransferStatusType	TransferStatus;

		///操作来源
		TQCVDOptionOperateSourceType	OperateSource;

		///操作人员
		TQCVDOptionUserIDType	OperatorID;

		///操作日期
		TQCVDOptionDateType	OperateDate;

		///操作时间
		TQCVDOptionTimeType	OperateTime;

		///状态信息
		TQCVDOptionErrorMsgType	StatusMsg;

		///外部流水号(银证转账使用)
		TQCVDOptionExternalSerialType	ExternalSerial;

		///用户请求编号
		TQCVDOptionRequestIDType	RequestID;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询持仓转移流水
	struct CQCVDOptionQryPositionTransferDetailField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///目前该字段存放的是上证所和深圳的股东代码。
		TQCVDOptionShareholderIDType	ShareholderID;

		///证券代码
		TQCVDOptionSecurityIDType	SecurityID;

		///转移方向
		TQCVDOptionTransferDirectionType	TransferDirection;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///多、空
		TQCVDOptionPosiDirectionType	PosiDirection;

		///投机套保标志
		TQCVDOptionHedgeFlagType	HedgeFlag;
	};

	/// 持仓转移流水
	struct CQCVDOptionPositionTransferDetailField
	{
		///流水号
		TQCVDOptionIntSerialType	PositionSerial;

		///申请流水号
		TQCVDOptionIntSerialType	ApplySerial;

		///前置编号
		TQCVDOptionFrontIDType	FrontID;

		///会话编号
		TQCVDOptionSessionIDType	SessionID;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///交易账户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///持仓方向
		TQCVDOptionPosiDirectionType	PosiDirection;

		///投机套保标志
		TQCVDOptionHedgeFlagType	HedgeFlag;

		///证券代码
		TQCVDOptionSecurityIDType	SecurityID;

		///交易日期
		TQCVDOptionDateType	TradingDay;

		///转移方向
		TQCVDOptionTransferDirectionType	TransferDirection;

		///转移持仓类型
		TQCVDOptionTransferPositionTypeType	TransferPositionType;

		///昨日仓位数量
		TQCVDOptionVolumeType	HistoryVolume;

		///今日买卖仓位数量
		TQCVDOptionVolumeType	TodayVolume;

		///转移状态
		TQCVDOptionTransferStatusType	TransferStatus;

		///操作人员
		TQCVDOptionUserIDType	OperatorID;

		///操作日期
		TQCVDOptionDateType	OperateDate;

		///操作时间
		TQCVDOptionTimeType	OperateTime;

		///状态信息
		TQCVDOptionErrorMsgType	StatusMsg;

		///用户请求编号
		TQCVDOptionRequestIDType	RequestID;

		///操作来源：0：DBMT；1：API指令
		TQCVDOptionOperateSourceType	OperateSource;

		///持仓成本变化
		TQCVDOptionMoneyType	TotalCostChange;

		///开仓成本变化
		TQCVDOptionMoneyType	OpenCostChange;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询撤单
	struct CQCVDOptionQryCancelOrderField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///交易所下的交易市场，如沪A、沪B市场
		TQCVDOptionMarketIDType	MarketID;

		///存放上证所和深圳的股东代码。
		TQCVDOptionShareholderIDType	ShareholderID;

		///信用投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///全系统的唯一报单编号
		TQCVDOptionOrderLocalIDType	CancelOrderLocalID;

		///本地报单编号
		TQCVDOptionOrderLocalIDType	OrderLocalID;

		///字符串长附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///字符串短附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;

		///排列方式 1正序 2倒序 默认倒序
		TQCVDOptionOrderSortType	OrderType;

		///每页记录数
		TQCVDOptionVolumeType	PageCount;

		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
	};

	/// 撤单
	struct CQCVDOptionCancelOrderField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///操作本地编号
		TQCVDOptionOrderLocalIDType	CancelOrderLocalID;

		///撤单前置编号
		TQCVDOptionFrontIDType	ActionFrontID;

		///撤单会话编号
		TQCVDOptionSessionIDType	ActionSessionID;

		///撤单引用
		TQCVDOptionOrderRefType	OrderActionRef;

		///本地被撤报单编号
		TQCVDOptionOrderLocalIDType	OrderLocalID;

		///报单编号
		TQCVDOptionOrderSysIDType	OrderSysID;

		///前置编号
		TQCVDOptionFrontIDType	FrontID;

		///会话编号
		TQCVDOptionSessionIDType	SessionID;

		///报单引用
		TQCVDOptionOrderRefType	OrderRef;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///股东账户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///合约代码
		TQCVDOptionSecurityIDType	SecurityID;

		///操作标志
		TQCVDOptionOrderActionFlagType	OrderActionFlag;

		///撤单状态
		TQCVDOptionCancelOrderStatusType	CancelOrderStatus;

		///撤单数量
		TQCVDOptionVolumeType	VolumeCanceled;

		///状态信息
		TQCVDOptionStatusMsgType	StatusMsg;

		///系统错误代码
		TQCVDOptionErrorIDType	ErrorID;

		///交易所营业部编码
		TQCVDOptionBranchIDType	BranchID;

		///交易所交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///报单申报用户
		TQCVDOptionUserIDType	InsertUserID;

		///操作日期
		TQCVDOptionDateType	InsertDate;

		///操作时间
		TQCVDOptionTimeType	InsertTime;

		///申报时间(毫秒)
		TQCVDOptionMillisecType	InsertMillisec;

		///内网IP地址
		TQCVDOptionIPAddressType	InnerIPAddress;

		///Mac地址
		TQCVDOptionMacAddressType	MacAddress;

		///请求编号
		TQCVDOptionRequestIDType	RequestID;

		///终端信息
		TQCVDOptionTerminalInfoType	TerminalInfo;

		///记录序号(仅上证报盘使用)
		TQCVDOptionSequenceNoType	RecordNumber;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;

		///委托方式
		TQCVDOptionOperwayType	Operway;

		///硬盘序列号
		TQCVDOptionHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDOptionMobileType	Mobile;

		///外网IP地址
		TQCVDOptionIPAddressType	OuterIPAddress;

		///外网端口号
		TQCVDOptionPortType	OuterPort;

		///节点号
		TQCVDOptionNodeIDType	ServerID;

		///页定位符
		TQCVDOptionPageLocateType	PageLocate;
	};

	/// 查询条件单
	struct CQCVDOptionQryCondOrderField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///合约代码
		TQCVDOptionSecurityIDType	SecurityID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///存放上证所和深圳的股东代码。
		TQCVDOptionShareholderIDType	ShareholderID;

		///报单编号
		TQCVDOptionCondOrderIDType	CondOrderID;

		///Time
		TQCVDOptionTimeType	InsertTimeStart;

		///Time
		TQCVDOptionTimeType	InsertTimeEnd;

		///信用投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///字符串长附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///字符串短附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;
	};

	/// 条件单
	struct CQCVDOptionCondOrderField
	{
		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///股东账户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///合约代码
		TQCVDOptionSecurityIDType	SecurityID;

		///买卖方向
		TQCVDOptionDirectionType	Direction;

		///条件单价格类型
		TQCVDOptionOrderPriceTypeType	OrderPriceType;

		///条件单数量类型
		TQCVDOptionOrderVolumeTypeType	OrderVolumeType;

		///有效期类型
		TQCVDOptionTimeConditionType	TimeCondition;

		///成交量类型
		TQCVDOptionVolumeConditionType	VolumeCondition;

		///最小成交量
		TQCVDOptionVolumeType	MinVolume;

		///强平原因
		TQCVDOptionForceCloseReasonType	ForceCloseReason;

		///报单价格
		TQCVDOptionPriceType	LimitPrice;

		///报单数量
		TQCVDOptionVolumeType	VolumeTotalOriginal;

		///组合开平标志
		TQCVDOptionCombOffsetFlagType	CombOffsetFlag;

		///组合投机套保标志
		TQCVDOptionCombHedgeFlagType	CombHedgeFlag;

		///条件报单引用
		TQCVDOptionOrderRefType	CondOrderRef;

		///资金账户代码
		TQCVDOptionAccountIDType	AccountID;

		///请求编号
		TQCVDOptionRequestIDType	RequestID;

		///内网IP地址
		TQCVDOptionIPAddressType	InnerIPAddress;

		///Mac地址
		TQCVDOptionMacAddressType	MacAddress;

		///条件报单编号
		TQCVDOptionCondOrderIDType	CondOrderID;

		///终端信息
		TQCVDOptionTerminalInfoType	TerminalInfo;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;

		///触发条件
		TQCVDOptionContingentConditionType	ContingentCondition;

		///条件价
		TQCVDOptionPriceType	ConditionPrice;

		///价格浮动tick数
		TQCVDOptionVolumeType	PriceTicks;

		///数量浮动倍数
		TQCVDOptionVolumeMultipleType	VolumeMultiple;

		///相关前置编号
		TQCVDOptionFrontIDType	RelativeFrontID;

		///相关会话编号
		TQCVDOptionSessionIDType	RelativeSessionID;

		///相关条件参数
		TQCVDOptionRelativeCondParamType	RelativeParam;

		///附加触发条件
		TQCVDOptionContingentConditionType	AppendContingentCondition;

		///附加条件价
		TQCVDOptionPriceType	AppendConditionPrice;

		///附加相关前置编号
		TQCVDOptionFrontIDType	AppendRelativeFrontID;

		///附加相关会话编号
		TQCVDOptionSessionIDType	AppendRelativeSessionID;

		///附加相关条件参数
		TQCVDOptionRelativeCondParamType	AppendRelativeParam;

		///交易日
		TQCVDOptionDateType	TradingDay;

		///前置编号
		TQCVDOptionFrontIDType	FrontID;

		///会话编号
		TQCVDOptionSessionIDType	SessionID;

		///经纪公司部门代码
		TQCVDOptionDepartmentIDType	DepartmentID;

		///条件单状态
		TQCVDOptionCondOrderStatusType	CondOrderStatus;

		///状态信息
		TQCVDOptionStatusMsgType	StatusMsg;

		///报单申报用户
		TQCVDOptionUserIDType	InsertUserID;

		///报单日期
		TQCVDOptionDateType	InsertDate;

		///委托时间
		TQCVDOptionTimeType	InsertTime;

		///委托毫秒
		TQCVDOptionMillisecType	InsertMillisec;

		///撤销时间
		TQCVDOptionTimeType	CancelTime;

		///撤销用户
		TQCVDOptionUserIDType	CancelUserID;

		///触发时间
		TQCVDOptionTimeType	ActiveTime;

		///委托方式
		TQCVDOptionOperwayType	Operway;

		///硬盘序列号
		TQCVDOptionHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDOptionMobileType	Mobile;

		///外网IP地址
		TQCVDOptionIPAddressType	OuterIPAddress;

		///外网端口号
		TQCVDOptionPortType	OuterPort;

		///用户强评标志
		TQCVDOptionBoolType	UserForceClose;

		///当前触发条件
		TQCVDOptionContingentConditionType	TriggerContingentCondition;

		///当前触发条件价
		TQCVDOptionPriceType	TriggerConditionPrice;

		///当前相关条件参数,可能为OrderSysID,Time,SecurityID
		TQCVDOptionRelativeCondParamType	TriggerRelativeParam;

		///主触发条件信息,可能为OrderSysID,Time,SecurityID
		TQCVDOptionRelativeCondParamType	CondParam;

		///附加条件信息,可能为OrderSysID,Time,SecurityID
		TQCVDOptionRelativeCondParamType	AppendCondParam;

		///条件单完成状态
		TQCVDOptionCondOrderFinishStatusType	CondOrderFinishStatus;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询条件单撤单
	struct CQCVDOptionQryCancelCondOrderField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///一个交易所的编号
		TQCVDOptionExchangeIDType	ExchangeID;

		///股东账户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///字符串长附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///字符串短附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;
	};

	/// 条件单撤单
	struct CQCVDOptionCancelCondOrderField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///请求编号
		TQCVDOptionRequestIDType	RequestID;

		///条件单操作引用
		TQCVDOptionOrderRefType	CondOrderActionRef;

		///条件单引用
		TQCVDOptionOrderRefType	CondOrderRef;

		///前置编号
		TQCVDOptionFrontIDType	FrontID;

		///会话编号
		TQCVDOptionSessionIDType	SessionID;

		///条件单编号
		TQCVDOptionCondOrderIDType	CondOrderID;

		///操作标志
		TQCVDOptionOrderActionFlagType	OrderActionFlag;

		///合约代码
		TQCVDOptionSecurityIDType	SecurityID;

		///条件单撤单编号
		TQCVDOptionCondOrderIDType	CancelCondOrderID;

		///IP地址
		TQCVDOptionIPAddressType	InnerIPAddress;

		///MAC地址
		TQCVDOptionMacAddressType	MacAddress;

		///终端信息
		TQCVDOptionTerminalInfoType	TerminalInfo;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///股东账户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///申报用户
		TQCVDOptionUserIDType	InsertUserID;

		///申报日期
		TQCVDOptionDateType	InsertDate;

		///申报时间
		TQCVDOptionTimeType	InsertTime;

		///申报毫秒
		TQCVDOptionMillisecType	InsertMillisec;

		///委托方式
		TQCVDOptionOperwayType	Operway;

		///硬盘序列号
		TQCVDOptionHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDOptionMobileType	Mobile;

		///外网IP地址
		TQCVDOptionIPAddressType	OuterIPAddress;

		///外网端口号
		TQCVDOptionPortType	OuterPort;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询投资者限仓信息
	struct CQCVDOptionQryInvestorLimitPositionField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///证券代码
		TQCVDOptionSecurityIDType	SecurityID;
	};

	/// 投资者限仓信息
	struct CQCVDOptionInvestorLimitPositionField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///标的证券代码
		TQCVDOptionSecurityIDType	SecurityID;

		///总持仓限额
		TQCVDOptionVolumeType	TotalPositionLimit;

		///多头持仓限额
		TQCVDOptionVolumeType	LongPositionLimit;

		///单日买入开仓限额
		TQCVDOptionVolumeType	TodayBuyOpenLimit;

		///单日卖出开仓限额
		TQCVDOptionVolumeType	TodaySellOpenLimit;

		///单日备兑开仓限额
		TQCVDOptionVolumeType	TodayCoveredOpenLimit;

		///单日开仓限额
		TQCVDOptionVolumeType	TodayOpenLimit;

		///认购多头持仓限额
		TQCVDOptionVolumeType	LongCallPositionLimit;

		///认沽多头持仓限额
		TQCVDOptionVolumeType	LongPutPositionLimit;

		///标的多头持仓限额
		TQCVDOptionVolumeType	LongUnderlyingPositionLimit;

		///标的空头持仓限额
		TQCVDOptionVolumeType	ShortUnderlyingPositionLimit;

		///总持仓冻结额度
		TQCVDOptionVolumeType	TotalPositionFrozen;

		///多头持仓冻结额度
		TQCVDOptionVolumeType	LongPositionFrozen;

		///单日买入开仓冻结额度
		TQCVDOptionVolumeType	TodayBuyOpenFrozen;

		///单日卖出开仓冻结额度
		TQCVDOptionVolumeType	TodaySellOpenFrozen;

		///单日备兑开仓冻结额度
		TQCVDOptionVolumeType	TodayCoveredOpenFrozen;

		///单日开仓冻结额度
		TQCVDOptionVolumeType	TodayOpenFrozen;

		///认购多头持仓冻结额度
		TQCVDOptionVolumeType	LongCallPositionFrozen;

		///认沽多头持仓冻结额度
		TQCVDOptionVolumeType	LongPutPositionFrozen;

		///标的多头持仓冻结额度
		TQCVDOptionVolumeType	LongUnderlyingPositionFrozen;

		///标的空头持仓冻结额度
		TQCVDOptionVolumeType	ShortUnderlyingPositionFrozen;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询报单明细资金
	struct CQCVDOptionQryOrderFundDetailField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///证券代码
		TQCVDOptionSecurityIDType	SecurityID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///系统报单编号
		TQCVDOptionOrderSysIDType	OrderSysID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;
	};

	/// 报单明细资金
	struct CQCVDOptionOrderFundDetailField
	{
		///交易日期
		TQCVDOptionDateType	TradingDay;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///系统报单编号
		TQCVDOptionOrderSysIDType	OrderSysID;

		///交易所交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///交易所交易单元代码
		TQCVDOptionOrderLocalIDType	OrderLocalID;

		///合约代码
		TQCVDOptionSecurityIDType	SecurityID;

		///资金账户代码
		TQCVDOptionAccountIDType	AccountID;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///成交金额
		TQCVDOptionMoneyType	TradeAmount;

		///印花税
		TQCVDOptionMoneyType	StampTaxFee;

		///过户费
		TQCVDOptionMoneyType	TransferFee;

		///经手费
		TQCVDOptionMoneyType	HandlingFee;

		///证管费
		TQCVDOptionMoneyType	RegulateFee;

		///佣金
		TQCVDOptionMoneyType	BrokerageFee;

		///结算费
		TQCVDOptionMoneyType	SettlementFee;

		///保证金
		TQCVDOptionMoneyType	Margin;

		///报单初始冻结金额
		TQCVDOptionMoneyType	OrderCashFrozen;

		///初始冻结费用合计
		TQCVDOptionMoneyType	TotalFeeFrozen;

		///申报金额
		TQCVDOptionMoneyType	OrderAmount;

		///买卖方向
		TQCVDOptionDirectionType	Direction;

		///本笔报单初始需要冻结的证管费
		TQCVDOptionMoneyType	RegulateFrozen;

		///本笔报单初始需要冻结的佣金
		TQCVDOptionMoneyType	BrokerageFrozen;

		///本笔报单初始需要冻结的结算费
		TQCVDOptionMoneyType	SettlementFrozen;

		///本笔报单初始需要冻结的印花税
		TQCVDOptionMoneyType	StampTaxFrozen;

		///本笔报单初始需要冻结的保证金
		TQCVDOptionMoneyType	MarginFrozen;

		///本笔报单初始需要冻结的经手费
		TQCVDOptionMoneyType	HandlingFrozen;

		///本笔报单初始需要冻结的过户费
		TQCVDOptionMoneyType	TransferFrozen;

		///平仓后释放的保证金
		TQCVDOptionMoneyType	ReleaseMargin;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询交易通知
	struct CQCVDOptionQryTradingNoticeField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///Insert Date
		TQCVDOptionDateType	InsertDateStart;

		///Insert Date
		TQCVDOptionDateType	InsertDateEnd;

		///Insert Time
		TQCVDOptionTimeType	InsertTimeStart;

		///Insert Time
		TQCVDOptionTimeType	InsertTimeEnd;
	};

	/// 交易通知
	struct CQCVDOptionTradingNoticeField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///通知流水号
		TQCVDOptionSerialType	NoticeSerial;

		///通知日期
		TQCVDOptionDateType	InsertDate;

		///通知时间
		TQCVDOptionTimeType	InsertTime;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///通知消息内容
		TQCVDOptionMessageType	Content;

		///操作员
		TQCVDOptionUserIDType	OperatorID;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询行权
	struct CQCVDOptionQryExerciseField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///合约在系统中的编号
		TQCVDOptionSecurityIDType	SecurityID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///目前该字段存放的是上证所和深圳的股东代码。
		TQCVDOptionShareholderIDType	ShareholderID;

		///全系统的唯一报单编号
		TQCVDOptionOrderSysIDType	ExerciseSysID;

		///Time
		TQCVDOptionTimeType	InsertTimeStart;

		///Time
		TQCVDOptionTimeType	InsertTimeEnd;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;
	};

	/// 行权
	struct CQCVDOptionExerciseField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///合约在系统中的编号
		TQCVDOptionSecurityIDType	SecurityID;

		///前置编号
		TQCVDOptionFrontIDType	FrontID;

		///会话编号
		TQCVDOptionSessionIDType	SessionID;

		///投资者说明的对报单的唯一引用
		TQCVDOptionOrderRefType	ExerciseRef;

		///全系统的唯一报单编号
		TQCVDOptionOrderSysIDType	ExerciseSysID;

		///交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///本地报单编号
		TQCVDOptionOrderLocalIDType	ExerciseLocalID;

		///执行类型
		TQCVDOptionExerciseTypeType	ExerciseType;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///行权数量
		TQCVDOptionVolumeType	Volume;

		///交易所返回的撤单数量
		TQCVDOptionVolumeType	VolumeCanceled;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///目前该字段存放的是上证所和深圳的股东代码。
		TQCVDOptionShareholderIDType	ShareholderID;

		///a：普通，b：信用，c：衍生品
		TQCVDOptionShareholderIDTypeType	ShareholderIDType;

		///经纪公司部门代码
		TQCVDOptionDepartmentIDType	DepartmentID;

		///资金账户
		TQCVDOptionAccountIDType	AccountID;

		///币种
		TQCVDOptionCurrencyIDType	CurrencyID;

		///行权状态
		TQCVDOptionExerciseStatusType	ExerciseStatus;

		///行权操作状态
		TQCVDOptionOrderOperateStatusType	ExerciseOperateStatus;

		///状态信息
		TQCVDOptionStatusMsgType	StatusMsg;

		///系统错误代码
		TQCVDOptionErrorIDType	ErrorID;

		///交易所为营业部分配的代码
		TQCVDOptionBranchIDType	BranchID;

		///报单申报用户
		TQCVDOptionUserIDType	InsertUserID;

		///申报日期
		TQCVDOptionDateType	InsertDate;

		///申报时间
		TQCVDOptionTimeType	InsertTime;

		///申报时间(毫秒)
		TQCVDOptionMillisecType	InsertMillisec;

		///交易所接收时间
		TQCVDOptionTimeType	AcceptTime;

		///撤销时间
		TQCVDOptionTimeType	CancelTime;

		///撤销申报用户
		TQCVDOptionUserIDType	CancelUserID;

		///现货仓位行权冻结流水号
		TQCVDOptionIntSerialType	StockPositionExerciseSerial;

		///现货系统仓位调拨流水号
		TQCVDOptionIntSerialType	StockPositionSerial;

		///内网IP地址
		TQCVDOptionIPAddressType	InnerIPAddress;

		///Mac地址
		TQCVDOptionMacAddressType	MacAddress;

		///请求编号
		TQCVDOptionRequestIDType	RequestID;

		///终端信息
		TQCVDOptionTerminalInfoType	TerminalInfo;

		///记录序号(仅上证报盘使用)
		TQCVDOptionSequenceNoType	RecordNumber;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;

		///委托方式
		TQCVDOptionOperwayType	Operway;

		///硬盘序列号
		TQCVDOptionHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDOptionMobileType	Mobile;

		///外网IP地址
		TQCVDOptionIPAddressType	OuterIPAddress;

		///外网端口号
		TQCVDOptionPortType	OuterPort;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询锁定
	struct CQCVDOptionQryLockField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///合约在系统中的编号
		TQCVDOptionSecurityIDType	SecurityID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///交易所下的交易市场，如沪A、沪B市场
		TQCVDOptionMarketIDType	MarketID;

		///客户在系统中的编号，编号唯一且遵循交易所制定的编码规则
		TQCVDOptionShareholderIDType	ShareholderID;

		///报单编号
		TQCVDOptionOrderSysIDType	LockSysID;

		///Time
		TQCVDOptionTimeType	InsertTimeStart;

		///Time
		TQCVDOptionTimeType	InsertTimeEnd;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///锁定类别
		TQCVDOptionLockTypeType	LockType;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;
	};

	/// 锁定委托
	struct CQCVDOptionLockField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///合约在系统中的编号
		TQCVDOptionSecurityIDType	SecurityID;

		///前置编号
		TQCVDOptionFrontIDType	FrontID;

		///每一位交易员或其它登录者登录系统获得的会话编号，当前时刻唯一
		TQCVDOptionSessionIDType	SessionID;

		///投资者说明的对锁定的唯一引用
		TQCVDOptionOrderRefType	LockRef;

		///报单编号
		TQCVDOptionOrderSysIDType	LockSysID;

		///交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///本地报单顺序号
		TQCVDOptionOrderLocalIDType	LockLocalID;

		///锁定类别
		TQCVDOptionLockTypeType	LockType;

		///报单数量
		TQCVDOptionVolumeType	Volume;

		///交易所返回的撤单数量
		TQCVDOptionVolumeType	VolumeCanceled;

		///请求编号
		TQCVDOptionRequestIDType	RequestID;

		///交易所下的交易市场，如沪A、沪B市场
		TQCVDOptionMarketIDType	MarketID;

		///客户在系统中的编号，编号唯一且遵循交易所制定的编码规则
		TQCVDOptionShareholderIDType	ShareholderID;

		///普通、信用、衍生品
		TQCVDOptionShareholderIDTypeType	ShareholderIDType;

		///锁定状态
		TQCVDOptionLockStatusType	LockStatus;

		///报单日期
		TQCVDOptionDateType	InsertDate;

		///申报时间
		TQCVDOptionTimeType	InsertTime;

		///交易所接收时间
		TQCVDOptionTimeType	AcceptTime;

		///撤单时间
		TQCVDOptionTimeType	CancelTime;

		///交易所营业部编码
		TQCVDOptionBranchIDType	BranchID;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///报盘错误代码
		TQCVDOptionErrorIDType	ErrorID;

		///状态信息
		TQCVDOptionStatusMsgType	StatusMsg;

		///申报操作员
		TQCVDOptionUserIDType	InsertUserID;

		///撤销申报用户
		TQCVDOptionUserIDType	CancelUserID;

		///现货仓位锁定流水号
		TQCVDOptionIntSerialType	StockPositionLockSerial;

		///现货系统仓位调拨流水号
		TQCVDOptionIntSerialType	StockPositionSerial;

		///内网IP地址
		TQCVDOptionIPAddressType	InnerIPAddress;

		///Mac地址
		TQCVDOptionMacAddressType	MacAddress;

		///终端信息
		TQCVDOptionTerminalInfoType	TerminalInfo;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;

		///记录序号(仅上证报盘使用)
		TQCVDOptionSequenceNoType	RecordNumber;

		///委托方式
		TQCVDOptionOperwayType	Operway;

		///硬盘序列号
		TQCVDOptionHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDOptionMobileType	Mobile;

		///外网IP地址
		TQCVDOptionIPAddressType	OuterIPAddress;

		///外网端口号
		TQCVDOptionPortType	OuterPort;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询锁定持仓
	struct CQCVDOptionQryLockPositionField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///证券代码
		TQCVDOptionSecurityIDType	SecurityID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///交易所下的交易市场，如沪A、沪B市场
		TQCVDOptionMarketIDType	MarketID;

		///交易账户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;
	};

	/// 投资者锁定持仓
	struct CQCVDOptionLockPositionField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///客户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///交易日
		TQCVDOptionDateType	TradingDay;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///合约代码
		TQCVDOptionSecurityIDType	SecurityID;

		///锁定持仓总数量
		TQCVDOptionVolumeType	Volume;

		///锁定持仓冻结数量
		TQCVDOptionVolumeType	FrozenVolume;

		///昨日持仓
		TQCVDOptionVolumeType	HistoryPos;

		///今日买卖持仓
		TQCVDOptionVolumeType	TodayBSPos;

		///今日申赎持仓
		TQCVDOptionVolumeType	TodayPRPos;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询保证金费率
	struct CQCVDOptionQryInvestorMarginFeeField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///经纪公司部门代码
		TQCVDOptionDepartmentIDType	DepartmentID;

		///产品代码
		TQCVDOptionProductIDType	ProductID;

		///证券类别代码
		TQCVDOptionSecurityTypeType	SecurityType;

		///业务类别
		TQCVDOptionBusinessClassType	BusinessClass;

		///合约代码
		TQCVDOptionSecurityIDType	SecurityID;
	};

	/// 保证金费率
	struct CQCVDOptionInvestorMarginFeeField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///经纪公司部门代码
		TQCVDOptionDepartmentIDType	DepartmentID;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///产品代码
		TQCVDOptionProductIDType	ProductID;

		///证券类别代码
		TQCVDOptionSecurityTypeType	SecurityType;

		///合约代码
		TQCVDOptionSecurityIDType	SecurityID;

		///业务类别
		TQCVDOptionBusinessClassType	BusinessClass;

		///佣金按数量收取金额
		TQCVDOptionMoneyType	FeeByVolume;

		///合约标的价格调整系数
		TQCVDOptionRatioType	PriceAdjustRatio;

		///虚值期权优惠比率
		TQCVDOptionRatioType	OTMPreferRatio;

		///合约标的价格调整保障系数
		TQCVDOptionRatioType	PriceAdjustGuardRatio;

		///上浮比率
		TQCVDOptionRatioType	UpperRatio;

		///保证金模式
		TQCVDOptionRangeModeType	RangeMode;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询标的持仓转移明细
	struct CQCVDOptionQryStockPositionTransferDetailField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///目前该字段存放的是上证所和深圳的股东代码。
		TQCVDOptionShareholderIDType	ShareholderID;

		///证券代码
		TQCVDOptionSecurityIDType	SecurityID;

		///个股期权标的现货转移原因
		TQCVDOptionSPStockTransferReasonType	SPStockTransferReason;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///转移持仓类型
		TQCVDOptionTransferPositionTypeType	TransferPositionType;
	};

	/// 现货持仓转移流水
	struct CQCVDOptionStockPositionTransferDetailField
	{
		///流水号
		TQCVDOptionIntSerialType	PositionSerial;

		///申请流水号
		TQCVDOptionIntSerialType	ApplySerial;

		///前置编号
		TQCVDOptionFrontIDType	FrontID;

		///会话编号
		TQCVDOptionSessionIDType	SessionID;

		///用户请求编号
		TQCVDOptionRequestIDType	RequestID;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///交易账户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///现货系统投资者代码
		TQCVDOptionInvestorIDType	OuterInvestorID;

		///现货系统投资单元代码
		TQCVDOptionBusinessUnitIDType	OuterBusinessUnitID;

		///现货系统交易账户代码
		TQCVDOptionShareholderIDType	OuterShareholderID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///证券代码
		TQCVDOptionSecurityIDType	SecurityID;

		///交易日期
		TQCVDOptionDateType	TradingDay;

		///个股期权标的现货转移原因
		TQCVDOptionSPStockTransferReasonType	SPStockTransferReason;

		///转移持仓类型
		TQCVDOptionTransferPositionTypeType	TransferPositionType;

		///昨日仓位数量
		TQCVDOptionVolumeType	HistoryVolume;

		///今日买卖仓位数量
		TQCVDOptionVolumeType	TodayBSVolume;

		///今日申赎持仓数量
		TQCVDOptionVolumeType	TodayPRVolume;

		///转移状态
		TQCVDOptionTransferStatusType	TransferStatus;

		///操作人员
		TQCVDOptionUserIDType	OperatorID;

		///操作日期
		TQCVDOptionDateType	OperateDate;

		///操作时间
		TQCVDOptionTimeType	OperateTime;

		///状态信息
		TQCVDOptionErrorMsgType	StatusMsg;

		///仓位转移现货系统流水号
		TQCVDOptionIntSerialType	StockPositionSerial;

		///是否冲正
		TQCVDOptionBoolType	bRepeal;

		///原始仓位转移流水号
		TQCVDOptionIntSerialType	OriginPositionSerial;

		///操作来源
		TQCVDOptionOperateSourceType	OperateSource;

		///业务编号
		TQCVDOptionBizRefType	BizRef;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询现货持仓
	struct CQCVDOptionQryStockPositionField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///证券代码
		TQCVDOptionSecurityIDType	SecurityID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///交易所下的交易市场，如沪A、沪B市场
		TQCVDOptionMarketIDType	MarketID;

		///交易账户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;
	};

	/// 投资者现货持仓
	struct CQCVDOptionStockPositionField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///客户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///交易日
		TQCVDOptionDateType	TradingDay;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///合约代码
		TQCVDOptionSecurityIDType	SecurityID;

		///昨日持仓
		TQCVDOptionVolumeType	HistoryPos;

		///昨日持仓冻结
		TQCVDOptionVolumeType	HistoryPosFrozen;

		///今日买卖持仓
		TQCVDOptionVolumeType	TodayBSPos;

		///今日买卖持仓冻结
		TQCVDOptionVolumeType	TodayBSPosFrozen;

		///今日申赎持仓
		TQCVDOptionVolumeType	TodayPRPos;

		///今日申赎持仓冻结
		TQCVDOptionVolumeType	TodayPRPosFrozen;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询组合持仓
	struct CQCVDOptionQryCombPositionField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///组合证券代码(A&B)
		TQCVDOptionSecurityIDType	SecurityID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///交易所下的交易市场，如沪A、沪B市场
		TQCVDOptionMarketIDType	MarketID;

		///交易账户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///组合策略(CNSJC/CXSJC/PXSJC/PNSJC/KS/KKS/ZBD)
		TQCVDOptionCombinationStrategyType	CombinationStrategy;
	};

	/// 投资者组合持仓
	struct CQCVDOptionCombPositionField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///客户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///交易日
		TQCVDOptionDateType	TradingDay;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///组合证券代码
		TQCVDOptionSecurityIDType	SecurityID;

		///组合策略
		TQCVDOptionCombinationStrategyType	CombinationStrategy;

		///昨日持仓
		TQCVDOptionVolumeType	HistoryPos;

		///昨日持仓冻结
		TQCVDOptionVolumeType	HistoryPosFrozen;

		///今日买卖持仓
		TQCVDOptionVolumeType	TodayPos;

		///今日买卖持仓冻结
		TQCVDOptionVolumeType	TodayPosFrozen;

		///占用的保证金
		TQCVDOptionMoneyType	Margin;

		///冻结的保证金
		TQCVDOptionMoneyType	FrozenMargin;

		///冻结的手续费
		TQCVDOptionMoneyType	FrozenCommission;

		///手续费
		TQCVDOptionMoneyType	Commission;

		///上次余额(盘中不变)
		TQCVDOptionVolumeType	PrePosition;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询组合持仓明细
	struct CQCVDOptionQryCombPosDetailField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///组合合约代码
		TQCVDOptionSecurityIDType	CombSecurityID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///交易账户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///组合策略
		TQCVDOptionCombinationStrategyType	CombinationStrategy;

		///交易所组合编码
		TQCVDOptionExchangeCombIDType	ExchangeCombID;

		///组合状态
		TQCVDOptionCombinationStatusType	CombinationStatus;
	};

	/// 组合持仓明细
	struct CQCVDOptionCombPosDetailField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///交易所组合编码
		TQCVDOptionExchangeCombIDType	ExchangeCombID;

		///组合策略
		TQCVDOptionCombinationStrategyType	CombinationStrategy;

		///组合证券代码
		TQCVDOptionSecurityIDType	CombSecurityID;

		///经纪公司部门代码
		TQCVDOptionDepartmentIDType	DepartmentID;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///客户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///昨日持仓
		TQCVDOptionVolumeType	HistoryPos;

		///今日买卖持仓
		TQCVDOptionVolumeType	TodayPos;

		///组合时期
		TQCVDOptionDateType	TradeDate;

		///组合时间
		TQCVDOptionTimeType	TradeTime;

		///组合状态
		TQCVDOptionCombinationStatusType	CombinationStatus;

		///成份合约个数，最多四条腿
		TQCVDOptionRecordCntType	NoLegs;

		///第一腿合约代码
		TQCVDOptionSecurityIDType	Leg1SecurityID;

		///第一腿昨日组合数量
		TQCVDOptionVolumeType	Leg1HistoryPos;

		///第一腿今日组合数量
		TQCVDOptionVolumeType	Leg1TodayPos;

		///第一腿持仓方向
		TQCVDOptionPosiDirectionType	Leg1PosiDirection;

		///第一腿期权类型
		TQCVDOptionOptionsTypeType	Leg1OptionsType;

		///第二腿合约代码
		TQCVDOptionSecurityIDType	Leg2SecurityID;

		///第二腿昨日组合数量
		TQCVDOptionVolumeType	Leg2HistoryPos;

		///第二腿今日组合数量
		TQCVDOptionVolumeType	Leg2TodayPos;

		///第二腿持仓方向
		TQCVDOptionPosiDirectionType	Leg2PosiDirection;

		///第二腿期权类型
		TQCVDOptionOptionsTypeType	Leg2OptionsType;

		///第三腿合约代码
		TQCVDOptionSecurityIDType	Leg3SecurityID;

		///第三腿昨日组合数量
		TQCVDOptionVolumeType	Leg3HistoryPos;

		///第三腿今日组合数量
		TQCVDOptionVolumeType	Leg3TodayPos;

		///第三腿持仓方向
		TQCVDOptionPosiDirectionType	Leg3PosiDirection;

		///第三腿期权类型
		TQCVDOptionOptionsTypeType	Leg3OptionsType;

		///第四腿合约代码
		TQCVDOptionSecurityIDType	Leg4SecurityID;

		///第四腿昨日组合数量
		TQCVDOptionVolumeType	Leg4HistoryPos;

		///第四腿今日组合数量
		TQCVDOptionVolumeType	Leg4TodayPos;

		///第四腿持仓方向
		TQCVDOptionPosiDirectionType	Leg4PosiDirection;

		///第四腿期权类型
		TQCVDOptionOptionsTypeType	Leg4OptionsType;

		///拆分冻结的昨日组合数量
		TQCVDOptionVolumeType	HistoryPosSplitFrozen;

		///拆分冻结的今日组合数量
		TQCVDOptionVolumeType	TodayPosSplitFrozen;

		///拆分冻结的第一腿昨日组合数量
		TQCVDOptionVolumeType	Leg1HistoryPosSplitFrozen;

		///拆分冻结的第一腿今日组合数量
		TQCVDOptionVolumeType	Leg1TodayPosSplitFrozen;

		///拆分冻结的第二腿昨日组合数量
		TQCVDOptionVolumeType	Leg2HistoryPosSplitFrozen;

		///拆分冻结的第二腿今日组合数量
		TQCVDOptionVolumeType	Leg2TodayPosSplitFrozen;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询投资者限额
	struct CQCVDOptionQryInvestorLimitAmountField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;
	};

	/// 投资者限额
	struct CQCVDOptionInvestorLimitAmountField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///多头金额限额
		TQCVDOptionMoneyType	LongAmountLimit;

		///多头金额冻结
		TQCVDOptionMoneyType	LongAmountFrozen;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询组合撤单
	struct CQCVDOptionQryCombOrderActionField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///交易所下的交易市场，如沪A、沪B市场
		TQCVDOptionMarketIDType	MarketID;

		///目前该字段存放的是上证所和深圳的股东代码。
		TQCVDOptionShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///全系统的唯一的组合报单撤单编号
		TQCVDOptionOrderLocalIDType	CancelCombOrderLocalID;

		///本地组合报单编号
		TQCVDOptionOrderLocalIDType	CombOrderLocalID;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;
	};

	/// 组合撤单
	struct CQCVDOptionCombOrderActionField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///交易所营业部编码
		TQCVDOptionBranchIDType	BranchID;

		///交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///本地组合委托撤单编号
		TQCVDOptionOrderLocalIDType	CancelCombOrderLocalID;

		///组合撤单前置编号
		TQCVDOptionFrontIDType	ActionFrontID;

		///组合撤单会话编号
		TQCVDOptionSessionIDType	ActionSessionID;

		///组合撤单引用
		TQCVDOptionOrderRefType	CombOrderActionRef;

		///被撤本地组合委托报单编号
		TQCVDOptionOrderLocalIDType	CombOrderLocalID;

		///被撤系统组合报单编号
		TQCVDOptionOrderSysIDType	CombOrderSysID;

		///前置编号
		TQCVDOptionFrontIDType	FrontID;

		///会话编号
		TQCVDOptionSessionIDType	SessionID;

		///组合报单引用
		TQCVDOptionOrderRefType	CombOrderRef;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///股东账户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///组合代码
		TQCVDOptionSecurityIDType	SecurityID;

		///组合委托操作标志
		TQCVDOptionOrderActionFlagType	CombOrderActionFlag;

		///撤单状态
		TQCVDOptionCancelOrderStatusType	CancelOrderStatus;

		///撤单数量
		TQCVDOptionVolumeType	VolumeCanceled;

		///状态信息
		TQCVDOptionStatusMsgType	StatusMsg;

		///系统错误代码
		TQCVDOptionErrorIDType	ErrorID;

		///报单申报用户
		TQCVDOptionUserIDType	InsertUserID;

		///操作日期
		TQCVDOptionDateType	InsertDate;

		///操作时间
		TQCVDOptionTimeType	InsertTime;

		///申报时间(毫秒)
		TQCVDOptionMillisecType	InsertMillisec;

		///内网IP地址
		TQCVDOptionIPAddressType	InnerIPAddress;

		///Mac地址
		TQCVDOptionMacAddressType	MacAddress;

		///请求编号
		TQCVDOptionRequestIDType	RequestID;

		///终端信息
		TQCVDOptionTerminalInfoType	TerminalInfo;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;

		///委托方式
		TQCVDOptionOperwayType	Operway;

		///硬盘序列号
		TQCVDOptionHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDOptionMobileType	Mobile;

		///外网IP地址
		TQCVDOptionIPAddressType	OuterIPAddress;

		///外网端口号
		TQCVDOptionPortType	OuterPort;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询组合委托
	struct CQCVDOptionQryCombOrderField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///组合合约代码
		TQCVDOptionSecurityIDType	SecurityID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///交易所下的交易市场，如沪A、沪B市场
		TQCVDOptionMarketIDType	MarketID;

		///客户在系统中的编号，编号唯一且遵循交易所制定的编码规则
		TQCVDOptionShareholderIDType	ShareholderID;

		///组合系统编号
		TQCVDOptionOrderSysIDType	CombOrderSysID;

		///Time
		TQCVDOptionTimeType	InsertTimeStart;

		///Time
		TQCVDOptionTimeType	InsertTimeEnd;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///组合策略
		TQCVDOptionCombinationStrategyType	CombinationStrategy;

		///组合报单类别
		TQCVDOptionCombDirectionType	CombDirection;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;
	};

	/// 组合委托
	struct CQCVDOptionCombOrderField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///组合合约代码
		TQCVDOptionSecurityIDType	SecurityID;

		///组合策略
		TQCVDOptionCombinationStrategyType	CombinationStrategy;

		///组合报单类别
		TQCVDOptionCombDirectionType	CombDirection;

		///前置编号
		TQCVDOptionFrontIDType	FrontID;

		///每一位交易员或其它登录者登录系统获得的会话编号，当前时刻唯一
		TQCVDOptionSessionIDType	SessionID;

		///投资者说明的对组合的唯一引用
		TQCVDOptionOrderRefType	CombOrderRef;

		///组合系统编号
		TQCVDOptionOrderSysIDType	CombOrderSysID;

		///交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///本地组合报单编号
		TQCVDOptionOrderLocalIDType	CombOrderLocalID;

		///报单数量
		TQCVDOptionVolumeType	Volume;

		///交易所返回的撤单数量
		TQCVDOptionVolumeType	VolumeCanceled;

		///请求编号
		TQCVDOptionRequestIDType	RequestID;

		///交易所下的交易市场，如沪A、沪B市场
		TQCVDOptionMarketIDType	MarketID;

		///客户在系统中的编号，编号唯一且遵循交易所制定的编码规则
		TQCVDOptionShareholderIDType	ShareholderID;

		///普通、信用、衍生品
		TQCVDOptionShareholderIDTypeType	ShareholderIDType;

		///组合委托状态
		TQCVDOptionOrderStatusType	CombOrderStatus;

		///组合委托操作状态
		TQCVDOptionOrderOperateStatusType	CombOrderOperateStatus;

		///经纪公司部门代码
		TQCVDOptionDepartmentIDType	DepartmentID;

		///资金账户
		TQCVDOptionAccountIDType	AccountID;

		///币种
		TQCVDOptionCurrencyIDType	CurrencyID;

		///交易所组合编码
		TQCVDOptionExchangeCombIDType	ExchangeCombID;

		///报单日期
		TQCVDOptionDateType	InsertDate;

		///申报时间
		TQCVDOptionTimeType	InsertTime;

		///申报时间(毫秒)
		TQCVDOptionMillisecType	InsertMillisec;

		///交易所接收时间
		TQCVDOptionTimeType	AcceptTime;

		///撤单时间
		TQCVDOptionTimeType	CancelTime;

		///交易所营业部编码
		TQCVDOptionBranchIDType	BranchID;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///报盘错误代码
		TQCVDOptionErrorIDType	ErrorID;

		///状态信息
		TQCVDOptionStatusMsgType	StatusMsg;

		///申报操作员
		TQCVDOptionUserIDType	InsertUserID;

		///撤销申报用户
		TQCVDOptionUserIDType	CancelUserID;

		///内网IP地址
		TQCVDOptionIPAddressType	InnerIPAddress;

		///Mac地址
		TQCVDOptionMacAddressType	MacAddress;

		///终端信息
		TQCVDOptionTerminalInfoType	TerminalInfo;

		///记录序号(仅上证报盘使用)
		TQCVDOptionSequenceNoType	RecordNumber;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;

		///委托方式
		TQCVDOptionOperwayType	Operway;

		///硬盘序列号
		TQCVDOptionHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDOptionMobileType	Mobile;

		///外网IP地址
		TQCVDOptionIPAddressType	OuterIPAddress;

		///外网端口号
		TQCVDOptionPortType	OuterPort;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询组合行权委托
	struct CQCVDOptionQryCombExerciseField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///认购合约代码
		TQCVDOptionSecurityIDType	CallSecurityID;

		///认沽合约代码
		TQCVDOptionSecurityIDType	PutSecurityID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///目前该字段存放的是上证所和深圳的股东代码。
		TQCVDOptionShareholderIDType	ShareholderID;

		///系统合并行权编号
		TQCVDOptionOrderSysIDType	CombExerciseSysID;

		///Time
		TQCVDOptionTimeType	InsertTimeStart;

		///Time
		TQCVDOptionTimeType	InsertTimeEnd;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;
	};

	/// 组合行权委托
	struct CQCVDOptionCombExerciseField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///认购合约代码
		TQCVDOptionSecurityIDType	CallSecurityID;

		///认沽合约代码
		TQCVDOptionSecurityIDType	PutSecurityID;

		///前置编号
		TQCVDOptionFrontIDType	FrontID;

		///会话编号
		TQCVDOptionSessionIDType	SessionID;

		///合并行权引用
		TQCVDOptionOrderRefType	CombExerciseRef;

		///系统合并行权编号
		TQCVDOptionOrderSysIDType	CombExerciseSysID;

		///交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///本地合并行权编号
		TQCVDOptionOrderLocalIDType	CombExerciseLocalID;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///行权数量
		TQCVDOptionVolumeType	Volume;

		///交易所返回的撤单数量
		TQCVDOptionVolumeType	VolumeCanceled;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///目前该字段存放的是上证所和深圳的股东代码。
		TQCVDOptionShareholderIDType	ShareholderID;

		///a：普通，b：信用，c：衍生品
		TQCVDOptionShareholderIDTypeType	ShareholderIDType;

		///经纪公司部门代码
		TQCVDOptionDepartmentIDType	DepartmentID;

		///资金账户
		TQCVDOptionAccountIDType	AccountID;

		///币种
		TQCVDOptionCurrencyIDType	CurrencyID;

		///行权状态
		TQCVDOptionExerciseStatusType	ExerciseStatus;

		///行权操作状态
		TQCVDOptionOrderOperateStatusType	ExerciseOperateStatus;

		///状态信息
		TQCVDOptionStatusMsgType	StatusMsg;

		///系统错误代码
		TQCVDOptionErrorIDType	ErrorID;

		///交易所为营业部分配的代码
		TQCVDOptionBranchIDType	BranchID;

		///报单申报用户
		TQCVDOptionUserIDType	InsertUserID;

		///申报日期
		TQCVDOptionDateType	InsertDate;

		///申报时间
		TQCVDOptionTimeType	InsertTime;

		///申报时间(毫秒)
		TQCVDOptionMillisecType	InsertMillisec;

		///交易所接收时间
		TQCVDOptionTimeType	AcceptTime;

		///撤销时间
		TQCVDOptionTimeType	CancelTime;

		///撤销申报用户
		TQCVDOptionUserIDType	CancelUserID;

		///内网IP地址
		TQCVDOptionIPAddressType	InnerIPAddress;

		///Mac地址
		TQCVDOptionMacAddressType	MacAddress;

		///请求编号
		TQCVDOptionRequestIDType	RequestID;

		///终端信息
		TQCVDOptionTerminalInfoType	TerminalInfo;

		///记录序号(仅上证报盘使用)
		TQCVDOptionSequenceNoType	RecordNumber;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;

		///委托方式
		TQCVDOptionOperwayType	Operway;

		///硬盘序列号
		TQCVDOptionHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDOptionMobileType	Mobile;

		///外网IP地址
		TQCVDOptionIPAddressType	OuterIPAddress;

		///外网端口号
		TQCVDOptionPortType	OuterPort;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询合并行权撤单
	struct CQCVDOptionQryCombExerciseActionField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///交易所下的交易市场，如沪A、沪B市场
		TQCVDOptionMarketIDType	MarketID;

		///目前该字段存放的是上证所和深圳的股东代码。
		TQCVDOptionShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///本地组合行权撤单编号
		TQCVDOptionOrderLocalIDType	CancelCombExerciseLocalID;

		///本地合并行权编号
		TQCVDOptionOrderLocalIDType	CombExerciseLocalID;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;
	};

	/// 合并行权撤单
	struct CQCVDOptionCombExerciseActionField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///交易所营业部编码
		TQCVDOptionBranchIDType	BranchID;

		///交易所交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///本地合并行权撤单编号
		TQCVDOptionOrderLocalIDType	CancelCombExerciseLocalID;

		///撤单前置编号
		TQCVDOptionFrontIDType	ActionFrontID;

		///撤单会话编号
		TQCVDOptionSessionIDType	ActionSessionID;

		///合并行权撤单引用
		TQCVDOptionOrderRefType	CombExerciseActionRef;

		///被撤本地合并行权报单编号
		TQCVDOptionOrderLocalIDType	CombExerciseLocalID;

		///被撤系统合并行权报单编号
		TQCVDOptionOrderSysIDType	CombExerciseSysID;

		///前置编号
		TQCVDOptionFrontIDType	FrontID;

		///会话编号
		TQCVDOptionSessionIDType	SessionID;

		///合并行权引用
		TQCVDOptionOrderRefType	CombExerciseRef;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///股东账户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///认购合约代码
		TQCVDOptionSecurityIDType	CallSecurityID;

		///认沽合约代码
		TQCVDOptionSecurityIDType	PutSecurityID;

		///操作标志
		TQCVDOptionOrderActionFlagType	ExerciseActionFlag;

		///撤单状态
		TQCVDOptionCancelOrderStatusType	CancelOrderStatus;

		///撤单数量
		TQCVDOptionVolumeType	VolumeCanceled;

		///状态信息
		TQCVDOptionStatusMsgType	StatusMsg;

		///系统错误代码
		TQCVDOptionErrorIDType	ErrorID;

		///报单申报用户
		TQCVDOptionUserIDType	InsertUserID;

		///操作日期
		TQCVDOptionDateType	InsertDate;

		///操作时间
		TQCVDOptionTimeType	InsertTime;

		///申报时间(毫秒)
		TQCVDOptionMillisecType	InsertMillisec;

		///内网IP地址
		TQCVDOptionIPAddressType	InnerIPAddress;

		///Mac地址
		TQCVDOptionMacAddressType	MacAddress;

		///请求编号
		TQCVDOptionRequestIDType	RequestID;

		///终端信息
		TQCVDOptionTerminalInfoType	TerminalInfo;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;

		///记录序号(仅上证报盘使用)
		TQCVDOptionSequenceNoType	RecordNumber;

		///委托方式
		TQCVDOptionOperwayType	Operway;

		///硬盘序列号
		TQCVDOptionHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDOptionMobileType	Mobile;

		///外网IP地址
		TQCVDOptionIPAddressType	OuterIPAddress;

		///外网端口号
		TQCVDOptionPortType	OuterPort;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询行权指派明细
	struct CQCVDOptionQryExerciseAppointmentField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///行权日期
		TQCVDOptionDateType	ExerciseDay;

		///交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///客户在系统中的编号，编号唯一且遵循交易所制定的编码规则
		TQCVDOptionShareholderIDType	ShareholderID;

		///资金账户
		TQCVDOptionAccountIDType	AccountID;

		///合约在系统中的编号
		TQCVDOptionSecurityIDType	SecurityID;
	};

	/// 行权指派明细
	struct CQCVDOptionExerciseAppointmentField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///投资者行权指派明细
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///行权日期
		TQCVDOptionDateType	ExerciseDay;

		///交易所交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///股东账户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///资金账户代码
		TQCVDOptionAccountIDType	AccountID;

		///币种
		TQCVDOptionCurrencyIDType	CurrencyID;

		///合约代码
		TQCVDOptionSecurityIDType	SecurityID;

		///期权类型
		TQCVDOptionOptionsTypeType	OptionsType;

		///合约单位
		TQCVDOptionVolumeType	OptionUnit;

		///标的证券代码
		TQCVDOptionSecurityIDType	UnderlyingSecurityID;

		///行权数量
		TQCVDOptionVolumeType	ExerciseVolume;

		///行权价
		TQCVDOptionMoneyType	ExercisePrice;

		///行权方向
		TQCVDOptionExerciseDirectionType	ExerciseDirection;

		///备兑标志
		TQCVDOptionCoverFlagType	CoverFlag;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询证券处置
	struct CQCVDOptionQryStockDisposalField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///个股期权标的合约代码
		TQCVDOptionSecurityIDType	SecurityID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///目前该字段存放的是上证所和深圳的股东代码。
		TQCVDOptionShareholderIDType	ShareholderID;

		///系统证券处置编号
		TQCVDOptionOrderSysIDType	StockDisposalSysID;

		///Time
		TQCVDOptionTimeType	InsertTimeStart;

		///Time
		TQCVDOptionTimeType	InsertTimeEnd;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;
	};

	/// 证券处置
	struct CQCVDOptionStockDisposalField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///个股期权标的合约代码
		TQCVDOptionSecurityIDType	SecurityID;

		///前置编号
		TQCVDOptionFrontIDType	FrontID;

		///会话编号
		TQCVDOptionSessionIDType	SessionID;

		///投资者说明的对证券处置的唯一引用
		TQCVDOptionOrderRefType	StockDisposalRef;

		///系统证券处置编号
		TQCVDOptionOrderSysIDType	StockDisposalSysID;

		///交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///本地证券处置编号
		TQCVDOptionOrderLocalIDType	StockDisposalLocalID;

		///证券处置类别
		TQCVDOptionStockDisposalTypeType	StockDisposalType;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///证券处置数量
		TQCVDOptionVolumeType	Volume;

		///证券处置撤销数量
		TQCVDOptionVolumeType	VolumeCanceled;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///目前该字段存放的是上证所和深圳的股东代码。
		TQCVDOptionShareholderIDType	ShareholderID;

		///普通、信用、衍生品
		TQCVDOptionShareholderIDTypeType	ShareholderIDType;

		///经纪公司部门代码
		TQCVDOptionDepartmentIDType	DepartmentID;

		///证券处置状态
		TQCVDOptionStockDisposalStatusType	StockDisposalStatus;

		///证券处置操作状态
		TQCVDOptionOrderOperateStatusType	StockDisposalOperateStatus;

		///状态信息
		TQCVDOptionStatusMsgType	StatusMsg;

		///系统错误代码
		TQCVDOptionErrorIDType	ErrorID;

		///交易所为营业部分配的代码
		TQCVDOptionBranchIDType	BranchID;

		///报单申报用户
		TQCVDOptionUserIDType	InsertUserID;

		///申报日期
		TQCVDOptionDateType	InsertDate;

		///申报时间
		TQCVDOptionTimeType	InsertTime;

		///申报时间(毫秒)
		TQCVDOptionMillisecType	InsertMillisec;

		///交易所接收时间
		TQCVDOptionTimeType	AcceptTime;

		///撤销时间
		TQCVDOptionTimeType	CancelTime;

		///撤销申报用户
		TQCVDOptionUserIDType	CancelUserID;

		///内网IP地址
		TQCVDOptionIPAddressType	InnerIPAddress;

		///Mac地址
		TQCVDOptionMacAddressType	MacAddress;

		///请求编号
		TQCVDOptionRequestIDType	RequestID;

		///终端信息
		TQCVDOptionTerminalInfoType	TerminalInfo;

		///记录序号(仅上证报盘使用)
		TQCVDOptionSequenceNoType	RecordNumber;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;

		///委托方式
		TQCVDOptionOperwayType	Operway;

		///硬盘序列号
		TQCVDOptionHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDOptionMobileType	Mobile;

		///外网IP地址
		TQCVDOptionIPAddressType	OuterIPAddress;

		///外网端口号
		TQCVDOptionPortType	OuterPort;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询证券处置撤单
	struct CQCVDOptionQryStockDisposalActionField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///交易所下的交易市场，如沪A、沪B市场
		TQCVDOptionMarketIDType	MarketID;

		///目前该字段存放的是上证所和深圳的股东代码。
		TQCVDOptionShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///全系统的唯一报单编号。
		TQCVDOptionOrderLocalIDType	CancelStockDisposalLocalID;

		///本地证券处置编号
		TQCVDOptionOrderLocalIDType	StockDisposalLocalID;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;
	};

	/// 证券处置撤单
	struct CQCVDOptionStockDisposalActionField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///交易所营业部编码
		TQCVDOptionBranchIDType	BranchID;

		///交易所交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///本地证券处置撤单编号
		TQCVDOptionOrderLocalIDType	CancelStockDisposalLocalID;

		///撤单前置编号
		TQCVDOptionFrontIDType	ActionFrontID;

		///撤单会话编号
		TQCVDOptionSessionIDType	ActionSessionID;

		///证券处置撤单引用
		TQCVDOptionOrderRefType	StockDisposalActionRef;

		///被撤本地证券处置报单编号
		TQCVDOptionOrderLocalIDType	StockDisposalLocalID;

		///被撤证券处置系统报单编号
		TQCVDOptionOrderSysIDType	StockDisposalSysID;

		///前置编号
		TQCVDOptionFrontIDType	FrontID;

		///会话编号
		TQCVDOptionSessionIDType	SessionID;

		///被撤证券处置引用
		TQCVDOptionOrderRefType	StockDisposalRef;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///股东账户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///合约代码
		TQCVDOptionSecurityIDType	SecurityID;

		///证券处置操作标志
		TQCVDOptionOrderActionFlagType	StockDisposalActionFlag;

		///撤单状态
		TQCVDOptionCancelOrderStatusType	CancelStockDisposalStatus;

		///撤单数量
		TQCVDOptionVolumeType	VolumeCanceled;

		///状态信息
		TQCVDOptionStatusMsgType	StatusMsg;

		///系统错误代码
		TQCVDOptionErrorIDType	ErrorID;

		///报单申报用户
		TQCVDOptionUserIDType	InsertUserID;

		///操作日期
		TQCVDOptionDateType	InsertDate;

		///操作时间
		TQCVDOptionTimeType	InsertTime;

		///申报时间(毫秒)
		TQCVDOptionMillisecType	InsertMillisec;

		///内网IP地址
		TQCVDOptionIPAddressType	InnerIPAddress;

		///Mac地址
		TQCVDOptionMacAddressType	MacAddress;

		///请求编号
		TQCVDOptionRequestIDType	RequestID;

		///终端信息
		TQCVDOptionTerminalInfoType	TerminalInfo;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;

		///记录序号(仅上证报盘使用)
		TQCVDOptionSequenceNoType	RecordNumber;

		///委托方式
		TQCVDOptionOperwayType	Operway;

		///硬盘序列号
		TQCVDOptionHDSerialType	HDSerial;

		///电话
		TQCVDOptionMobileType	Mobile;

		///外网IP地址
		TQCVDOptionIPAddressType	OuterIPAddress;

		///外网端口号
		TQCVDOptionPortType	OuterPort;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询锁定撤单
	struct CQCVDOptionQryLockActionField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///交易所下的交易市场，如沪A、沪B市场
		TQCVDOptionMarketIDType	MarketID;

		///目前该字段存放的是上证所和深圳的股东代码。
		TQCVDOptionShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///全系统的唯一报单编号。
		TQCVDOptionOrderLocalIDType	CancelLockLocalID;

		///本地报单顺序号
		TQCVDOptionOrderLocalIDType	LockLocalID;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;
	};

	/// 锁定撤单
	struct CQCVDOptionLockActionField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///交易所营业部编码
		TQCVDOptionBranchIDType	BranchID;

		///交易所交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///本地锁定撤单编号
		TQCVDOptionOrderLocalIDType	CancelLockLocalID;

		///撤单前置编号
		TQCVDOptionFrontIDType	ActionFrontID;

		///撤单会话编号
		TQCVDOptionSessionIDType	ActionSessionID;

		///锁定撤单引用
		TQCVDOptionOrderRefType	LockActionRef;

		///被撤本地锁定报单编号
		TQCVDOptionOrderLocalIDType	LockLocalID;

		///被撤系统锁定报单编号
		TQCVDOptionOrderSysIDType	LockSysID;

		///前置编号
		TQCVDOptionFrontIDType	FrontID;

		///会话编号
		TQCVDOptionSessionIDType	SessionID;

		///锁定引用
		TQCVDOptionOrderRefType	LockRef;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///股东账户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///合约代码
		TQCVDOptionSecurityIDType	SecurityID;

		///锁定操作标志
		TQCVDOptionOrderActionFlagType	LockActionFlag;

		///撤单状态
		TQCVDOptionCancelOrderStatusType	CancelOrderStatus;

		///撤单数量
		TQCVDOptionVolumeType	VolumeCanceled;

		///状态信息
		TQCVDOptionStatusMsgType	StatusMsg;

		///系统错误代码
		TQCVDOptionErrorIDType	ErrorID;

		///报单申报用户
		TQCVDOptionUserIDType	InsertUserID;

		///操作日期
		TQCVDOptionDateType	InsertDate;

		///操作时间
		TQCVDOptionTimeType	InsertTime;

		///申报时间(毫秒)
		TQCVDOptionMillisecType	InsertMillisec;

		///内网IP地址
		TQCVDOptionIPAddressType	InnerIPAddress;

		///Mac地址
		TQCVDOptionMacAddressType	MacAddress;

		///请求编号
		TQCVDOptionRequestIDType	RequestID;

		///终端信息
		TQCVDOptionTerminalInfoType	TerminalInfo;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;

		///记录序号(仅上证报盘使用)
		TQCVDOptionSequenceNoType	RecordNumber;

		///委托方式
		TQCVDOptionOperwayType	Operway;

		///硬盘序列号
		TQCVDOptionHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDOptionMobileType	Mobile;

		///外网IP地址
		TQCVDOptionIPAddressType	OuterIPAddress;

		///外网端口号
		TQCVDOptionPortType	OuterPort;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询行权撤单
	struct CQCVDOptionQryExerciseActionField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///交易所下的交易市场，如沪A、沪B市场
		TQCVDOptionMarketIDType	MarketID;

		///目前该字段存放的是上证所和深圳的股东代码。
		TQCVDOptionShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///全系统的唯一报单编号。
		TQCVDOptionOrderLocalIDType	CancelExerciseLocalID;

		///本地报单编号
		TQCVDOptionOrderLocalIDType	ExerciseLocalID;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;
	};

	/// 行权撤单
	struct CQCVDOptionExerciseActionField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///交易所营业部编码
		TQCVDOptionBranchIDType	BranchID;

		///交易所交易单元代码
		TQCVDOptionPbuIDType	PbuID;

		///本地行权撤单编号
		TQCVDOptionOrderLocalIDType	CancelExerciseLocalID;

		///撤单前置编号
		TQCVDOptionFrontIDType	ActionFrontID;

		///撤单会话编号
		TQCVDOptionSessionIDType	ActionSessionID;

		///行权撤单引用
		TQCVDOptionOrderRefType	ExerciseActionRef;

		///被撤本地行权报单编号
		TQCVDOptionOrderLocalIDType	ExerciseLocalID;

		///被撤系统行权报单编号
		TQCVDOptionOrderSysIDType	ExerciseSysID;

		///前置编号
		TQCVDOptionFrontIDType	FrontID;

		///会话编号
		TQCVDOptionSessionIDType	SessionID;

		///行权引用
		TQCVDOptionOrderRefType	ExerciseRef;

		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///股东账户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///合约代码
		TQCVDOptionSecurityIDType	SecurityID;

		///委托操作标志
		TQCVDOptionOrderActionFlagType	ExerciseActionFlag;

		///撤单状态
		TQCVDOptionCancelOrderStatusType	CancelOrderStatus;

		///撤单数量
		TQCVDOptionVolumeType	VolumeCanceled;

		///状态信息
		TQCVDOptionStatusMsgType	StatusMsg;

		///系统错误代码
		TQCVDOptionErrorIDType	ErrorID;

		///报单申报用户
		TQCVDOptionUserIDType	InsertUserID;

		///操作日期
		TQCVDOptionDateType	InsertDate;

		///操作时间
		TQCVDOptionTimeType	InsertTime;

		///申报时间(毫秒)
		TQCVDOptionMillisecType	InsertMillisec;

		///内网IP地址
		TQCVDOptionIPAddressType	InnerIPAddress;

		///Mac地址
		TQCVDOptionMacAddressType	MacAddress;

		///请求编号
		TQCVDOptionRequestIDType	RequestID;

		///长字符串附加信息
		TQCVDOptionBigsStrInfoType	BInfo;

		///短字符串附加信息
		TQCVDOptionShortsStrInfoType	SInfo;

		///整形附加信息
		TQCVDOptionIntInfoType	IInfo;

		///委托方式
		TQCVDOptionOperwayType	Operway;

		///硬盘序列号
		TQCVDOptionHDSerialType	HDSerial;

		///移动设备手机号
		TQCVDOptionMobileType	Mobile;

		///外网IP地址
		TQCVDOptionIPAddressType	OuterIPAddress;

		///外网端口号
		TQCVDOptionPortType	OuterPort;

		///终端信息
		TQCVDOptionTerminalInfoType	TerminalInfo;

		///记录序号(仅上证报盘使用)
		TQCVDOptionSequenceNoType	RecordNumber;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询组合合约信息
	struct CQCVDOptionQryCombSecurityField
	{
		///组合合约代码
		TQCVDOptionSecurityIDType	CombSecurityID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///组合策略
		TQCVDOptionCombinationStrategyType	CombinationStrategy;
	};

	/// 组合合约信息
	struct CQCVDOptionCombSecurityField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///组合合约代码
		TQCVDOptionSecurityIDType	CombSecurityID;

		///组合策略
		TQCVDOptionCombinationStrategyType	CombinationStrategy;

		///成分一合约代码
		TQCVDOptionSecurityIDType	Leg1SecurityID;

		///成分一合约名称
		TQCVDOptionSecurityNameType	Leg1SecurityName;

		///策略要求的成分一合约多空方向
		TQCVDOptionPosiDirectionType	RequiredLeg1PosiDirection;

		///成分二合约代码
		TQCVDOptionSecurityIDType	Leg2SecurityID;

		///成分二合约名称
		TQCVDOptionSecurityNameType	Leg2SecurityName;

		///策略要求的成分二合约多空方向
		TQCVDOptionPosiDirectionType	RequiredLeg2PosiDirection;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询合约信息
	struct CQCVDOptionQrySecurityField
	{
		///合约代码
		TQCVDOptionSecurityIDType	SecurityID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;
	};

	/// 合约信息
	struct CQCVDOptionSecurityField
	{
		///交易日
		TQCVDOptionDateType	TradingDay;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///合约代码
		TQCVDOptionSecurityIDType	SecurityID;

		///交易所合约代码
		TQCVDOptionExchSecurityIDType	ExchSecurityID;

		///合约名称
		TQCVDOptionSecurityNameType	SecurityName;

		///基础证券代码
		TQCVDOptionSecurityIDType	UnderlyingSecurityID;

		///基础证券名称
		TQCVDOptionSecurityNameType	UnderlyingSecurityName;

		///合约基础商品乘数
		TQCVDOptionVolumeMultipleType	UnderlyingMultiple;

		///执行方式
		TQCVDOptionStrikeModeType	StrikeMode;

		///期权类型
		TQCVDOptionOptionsTypeType	OptionsType;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///产品代码
		TQCVDOptionProductIDType	ProductID;

		///证券类别代码
		TQCVDOptionSecurityTypeType	SecurityType;

		///币种
		TQCVDOptionCurrencyIDType	CurrencyID;

		///申报单位
		TQCVDOptionOrderUnitType	OrderUnit;

		///买入交易单位
		TQCVDOptionTradingUnitType	BuyTradingUnit;

		///卖出交易单位
		TQCVDOptionTradingUnitType	SellTradingUnit;

		///市价单买最大下单量
		TQCVDOptionVolumeType	MaxMarketOrderBuyVolume;

		///市价单买最小下单量
		TQCVDOptionVolumeType	MinMarketOrderBuyVolume;

		///限价单买最大下单量
		TQCVDOptionVolumeType	MaxLimitOrderBuyVolume;

		///限价单买最小下单量
		TQCVDOptionVolumeType	MinLimitOrderBuyVolume;

		///市价单卖最大下单量
		TQCVDOptionVolumeType	MaxMarketOrderSellVolume;

		///市价单卖最小下单量
		TQCVDOptionVolumeType	MinMarketOrderSellVolume;

		///限价单卖最大下单量
		TQCVDOptionVolumeType	MaxLimitOrderSellVolume;

		///限价单卖最小下单量
		TQCVDOptionVolumeType	MinLimitOrderSellVolume;

		///数量乘数
		TQCVDOptionVolumeMultipleType	VolumeMultiple;

		///最小变动价位
		TQCVDOptionPriceTickType	PriceTick;

		///持仓类型
		TQCVDOptionPositionTypeType	PositionType;

		///证券状态
		TQCVDOptionSecurityStatusType	SecurityStatus;

		///执行价
		TQCVDOptionPriceType	StrikePrice;

		///首交易日
		TQCVDOptionDateType	FirstDate;

		///最后交易日
		TQCVDOptionDateType	LastDate;

		///行权日
		TQCVDOptionDateType	StrikeDate;

		///到期日
		TQCVDOptionDateType	ExpireDate;

		///交割日
		TQCVDOptionDateType	DelivDate;

		///是否有涨跌幅限制
		TQCVDOptionBoolType	IsUpDownLimit;

		///期权单位保证金
		TQCVDOptionPriceType	MarginUnit;

		///合约前结算价
		TQCVDOptionPriceType	PreSettlementPrice;

		///合约前收盘价
		TQCVDOptionPriceType	PreClosePrice;

		///标的合约前收盘价
		TQCVDOptionPriceType	UnderlyingPreClosePrice;

		///合约前持仓量
		TQCVDOptionVolumeType	PreOpenInterest;

		///报价买最大下单量
		TQCVDOptionVolumeType	MaxQuoteOrderBuyVolume;

		///报价买最小下单量
		TQCVDOptionVolumeType	MinQuoteOrderBuyVolume;

		///报价卖最大下单量
		TQCVDOptionVolumeType	MaxQuoteOrderSellVolume;

		///报价卖最小下单量
		TQCVDOptionVolumeType	MinQuoteOrderSellVolume;

		///涨停价格
		TQCVDOptionPriceType	UpperLimitPrice;

		///跌停价格
		TQCVDOptionPriceType	LowerLimitPrice;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	/// 查询备兑股份不足仓位
	struct CQCVDOptionQryInsufficientCoveredStockPositionField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///标的证券代码
		TQCVDOptionSecurityIDType	SecurityID;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///目前该字段存放的是上证所和深圳的股东代码。
		TQCVDOptionShareholderIDType	ShareholderID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;
	};

	/// 备兑股份不足仓位
	struct CQCVDOptionInsufficientCoveredStockPositionField
	{
		///投资者代码
		TQCVDOptionInvestorIDType	InvestorID;

		///投资单元代码
		TQCVDOptionBusinessUnitIDType	BusinessUnitID;

		///市场代码
		TQCVDOptionMarketIDType	MarketID;

		///客户代码
		TQCVDOptionShareholderIDType	ShareholderID;

		///交易日
		TQCVDOptionDateType	TradingDay;

		///交易所代码
		TQCVDOptionExchangeIDType	ExchangeID;

		///证券代码
		TQCVDOptionSecurityIDType	SecurityID;

		///初始备兑股份不足数量
		TQCVDOptionVolumeType	TotalInsufficientVolume;

		///初始备兑股份预冻结数量
		TQCVDOptionVolumeType	PreFrozenVolume;

		///备兑平仓已偿还的备兑股份数量
		TQCVDOptionVolumeType	RepaidVolume;

		///节点号
		TQCVDOptionNodeIDType	ServerID;
	};

	///查询投资者指纹注册记录请求
	struct CQCVDReqQryFingerPrintRegisterRecordField
	{
		///投资者代码
		TQCVDInvestorIDType InvestorID;
		///交易市场: 现货市场 1 ; 两融市场 2; 期权市场 3; 期货市场 4 证券Lev1行情 5 证券Lev2行情 6
		TQCVDTradeMarketType TradeMarket;
		///设备类型: 通用 0; PC 1; MOBILE 2; HTML5 3
		TQCVDDeviceClassType DeviceClass;
		///唯一设备号
		TQCVDDeviceIDType DeviceId;
		///开始注册日期
		TQCVDDateType	BeginDate;
		///开始注册时间
		TQCVDTimeType	BeginTime;
		///结束注册日期
		TQCVDDateType	EndDate;
		///结束注册时间
		TQCVDTimeType	EndTime;
		///排序方式，默认是按照时间倒序
		TQCVDOrderSortType OrderType;

	};

	///投资者指纹注册记录
	struct CQCVDFingerPrintRegisterRecordField
	{
		///注册记录编号
		TQCVDSequenceNoType	RecordID;
		///认证中心登录用户名称
		TQCVDLogInAccountType LogInAccount;
		///认证中心编号
		TQCVDFrontIDType	ServerID;
		///会话编号
		TQCVDLongSequenceType SessionID;
		///投资者代码
		TQCVDInvestorIDType InvestorID;
		///交易市场: 现货市场 1 ; 两融市场 2; 期权市场 3; 期货市场 4 证券Lev1行情 5 证券Lev2行情 6
		TQCVDTradeMarketType TradeMarket;
		///设备类型: 通用 0; PC 1; MOBILE 2; HTML5 3
		TQCVDDeviceClassType DeviceClass;
		///唯一设备号
		TQCVDDeviceIDType DeviceId;
		///IP地址
		TQCVDIPAddressType LoginIp;
		///MAC地址
		TQCVDMacAddressType MacAddr;
		///注册日期
		TQCVDDateType	InsertDate;
		///注册时间
		TQCVDTimeType	InsertTime;
		///附加信息
		TQCVDAdditionalInfoType AdditionalInfo;
	};

	/// 查询ST股票信息请求包
	struct CQCVDQrySTAShareDescriptionField
	{
		///交易所代码 
		TQCVDExchangeIDType	ExchangeID;
		///证券代码 
		TQCVDSecurityIDType	SecurityID;
		///查询ST类别
		TQCVDSTTypeType	STType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 查询ST股票信息应答包
	struct CQCVDSTAShareDescriptionField
	{
		///交易所代码 
		TQCVDExchangeIDType	ExchangeID;
		///证券代码 
		TQCVDSecurityIDType	SecurityID;
		///证券简称
		TQCVDSecurityNameType	SecurityName;
		///上市板类型
		TQCVDUserIDType	ListBoard;
		///上市日期
		TQCVDDateType	ListDate;
		///退市日期
		TQCVDDateType	DeListDate;
		///上市板
		TQCVDSecurityIDType	ListBoardName;
		///是否在沪股通或深港通范围内
		TQCVDShortsInfoType	SHSC;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};

	/// 查询华证债券ESG评级数据请求包
	struct CQCVDQryEsgBondIndexValueField
	{
		///开始日期
		TQCVDDateType	StartDate;
		///结束日期 
		TQCVDDateType	EndDate;
		///ESG评级类型
		TQCVDEsgTypeType	EsgType;
		///债券编码列表(|分隔)
		TQCVDSecurityCodeListType	SecurityCodeList;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 查询华证债券ESG评级数据应答包
	struct CQCVDEsgBondIndexValueField
	{
		///评级日期
		TQCVDDateType	EsgRatingDate;
		///发行人
		TQCVDLocalBondNameType	Issuer;
		///债券代码
		TQCVDSecurityIDType	BondCode;
		///债券简称
		TQCVDLocalBondNameType	BondName;
		///ESG总分
		TQCVDEsgScoreType	EsgScore;
		///ESG评级
		TQCVDEsgRateType	EsgRate;
		///E得分
		TQCVDEsgScoreType	EScore;
		///E评级
		TQCVDEsgRateType	ERate;
		///S得分
		TQCVDEsgScoreType	SScore;
		///S评级
		TQCVDEsgRateType	SRate;
		///G得分
		TQCVDEsgScoreType	GScore;
		///G评级
		TQCVDEsgRateType	GRate;
		///E_气候变化
		TQCVDEsgScoreType	BondClimateChange;
		///E_资源利用
		TQCVDEsgScoreType	BondResourceUtilization;
		///E_环境污染
		TQCVDEsgScoreType	BondEnvironmentalPollution;
		///E_环境友好
		TQCVDEsgScoreType	BondEnvironmentalFriendly;
		///E_环境管理
		TQCVDEsgScoreType	BondEnvironmentalManagement;
		///S_人力资本
		TQCVDEsgScoreType	BondHumanCapital;
		///S_产品责任
		TQCVDEsgScoreType	BondProductResponsibility;
		///S_供应链
		TQCVDEsgScoreType	BondSupplyChain;
		///S_社会贡献
		TQCVDEsgScoreType	BondSocialContribution;
		///S_投资者保护
		TQCVDEsgScoreType	BondInvestorProtectionBranch;
		///G_股东权益
		TQCVDEsgScoreType	BondShareHolderInterest;
		///G_治理结构
		TQCVDEsgScoreType	BondGovernanceStructure;
		///G_信披质量
		TQCVDEsgScoreType	BondInfoDisclosureQuality;
		///G_治理风险
		TQCVDEsgScoreType	BondGovernanceRisk;
		///G_外部处分
		TQCVDEsgScoreType	BondAdministrativePenaltyBranch;
		///G_商业道德
		TQCVDEsgScoreType	BondBusinessEthicsBranch;
		///温室气体排放管理与认证
		TQCVDEsgScoreType	BondCarbonEmission;
		///绿色金融
		TQCVDEsgScoreType	BondGreenFinance;
		///能源管理
		TQCVDEsgScoreType	BondEnergyManagement;
		///水资源利用
		TQCVDEsgScoreType	BondWaterUtilization;
		///土地利用和生物多样性
		TQCVDEsgScoreType	BondLandUseBiodiversity;
		///废弃物排放
		TQCVDEsgScoreType	BondWasteEmission;
		///环境信用
		TQCVDEsgScoreType	BondEnvironmentalCredit;
		///污染控制
		TQCVDEsgScoreType	BondPollutionControl;
		///绿色建筑
		TQCVDEsgScoreType	BondGreenBuilding;
		///绿色工厂
		TQCVDEsgScoreType	BondGreenFactory;
		///可持续认证
		TQCVDEsgScoreType	BondSustainableCertification;
		///绿色供应链
		TQCVDEsgScoreType	BondGreenSupplyChain;
		///环保处罚
		TQCVDEsgScoreType	BondEnvironmentalPenalty;
		///员工激励
		TQCVDEsgScoreType	BondsPerCapitaSalary;
		///劳工关系
		TQCVDEsgScoreType	BondLaborRelation;
		///员工权益保障
		TQCVDEsgScoreType	BondRightsProtection;
		///员工健康与安全
		TQCVDEsgScoreType	BondHealthSafety;
		///产品质量
		TQCVDEsgScoreType	BondProductQuality;
		///产品安全
		TQCVDEsgScoreType	BondProductSafety;
		///售后服务
		TQCVDEsgScoreType	BondAfterSaleService;
		///供应商管理
		TQCVDEsgScoreType	BondSupplierManagement;
		///经销商管理
		TQCVDEsgScoreType	BondDistributorsManagement;
		///纳税贡献
		TQCVDEsgScoreType	BondTaxContribution;
		///就业贡献
		TQCVDEsgScoreType	BondEmploymentContribution;
		///科技贡献
		TQCVDEsgScoreType	BondTechnologyContribution;
		///债券违约及展期
		TQCVDEsgScoreType	BondDefault;
		///投资者保护
		TQCVDEsgScoreType	BondInvestorProtection;
		///实控人持股
		TQCVDEsgScoreType	BondActualControllerShareHolder;
		///董事会独立性
		TQCVDEsgScoreType	BondIndependentDirector;
		///管理层稳定性
		TQCVDEsgScoreType	BondManagementStable;
		///管理层相对规模
		TQCVDEsgScoreType	BondManagementRelativeSize;
		///审计意见
		TQCVDEsgScoreType	BondAuditOpinion;
		///信息披露处罚
		TQCVDEsgScoreType	BondInfoDisclosurePenalty;
		///大股东行为
		TQCVDEsgScoreType	BondMajorShareHolderBehavior;
		///偿债能力
		TQCVDEsgScoreType	BondDebtServicingAbility;
		///司法风险
		TQCVDEsgScoreType	BondJudicialRisk;
		///行政处罚
		TQCVDEsgScoreType	BondAdministrativePenalty;
		///商业道德
		TQCVDEsgScoreType	BondBusinessEthics;
		///债券偿债能力风险
		TQCVDEsgRiskNameType	BondSolvencyRisk;
		///债券负面经营事件
		TQCVDEsgRiskNameType	BondNegativeBusiness;
		///债券股东行为
		TQCVDEsgRiskNameType	BondHolderBehavior;
		///债券司法风险
		TQCVDEsgRiskNameType	BondJudicialRiskEsg;
		///财务可信度风险
		TQCVDEsgRiskNameType	BondFinancialForgery;
		///华证债券ESG尾部风险类型
		TQCVDEsgRiskNameType	BondEsgTailRisk;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};
	/// 查询华证A股ESG评级数据请求包
	struct CQCVDQryEsgStockIndexValueField
	{
		///开始日期
		TQCVDDateType	StartDate;
		///结束日期 
		TQCVDDateType	EndDate;
		///ESG评级类型
		TQCVDEsgTypeType	EsgType;
		///A股编码列表(|分隔)
		TQCVDSecurityCodeListType	SecurityCodeList;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 查询华证A股ESG评级数据应答包
	struct CQCVDEsgStockIndexValueField
	{
		///评级日期
		TQCVDDateType	EsgRatingDate;
		///证券代码
		TQCVDSecurityIDType	StockCode;
		///证券简称
		TQCVDLocalBondNameType	StockName;
		///ESG总分
		TQCVDEsgScoreType	EsgScore;
		///ESG评级
		TQCVDEsgRateType	EsgRate;
		///E得分
		TQCVDEsgScoreType	EScore;
		///E评级
		TQCVDEsgRateType	ERate;
		///S得分
		TQCVDEsgScoreType	SScore;
		///S评级
		TQCVDEsgRateType	SRate;
		///G得分
		TQCVDEsgScoreType	GScore;
		///G评级
		TQCVDEsgRateType	GRate;
		///E_气候变化
		TQCVDEsgScoreType	StockClimateChange;
		///E_资源利用
		TQCVDEsgScoreType	StockResourceUtilization;
		///E_环境污染
		TQCVDEsgScoreType	StockEnvironmentalPollution;
		///E_环境友好
		TQCVDEsgScoreType	StockEnvironmentalFriendly;
		///E_环境管理
		TQCVDEsgScoreType	StockEnvironmentalManagement;
		///S_人力资本
		TQCVDEsgScoreType	StockHumanCapital;
		///S_产品责任
		TQCVDEsgScoreType	StockProductResponsibility;
		///S_供应商
		TQCVDEsgScoreType	StockSupplier;
		///S_社会贡献
		TQCVDEsgScoreType	StockSocialContribution;
		///S_数据安全与隐私
		TQCVDEsgScoreType	StockDataSecurityPrivacyBranch;
		///G_股东权益
		TQCVDEsgScoreType	StockShareHolderInterestBranch;
		///G_治理结构
		TQCVDEsgScoreType	StockGovernanceStructure;
		///G_信披质量
		TQCVDEsgScoreType	StockInfoDisclosureQuality;
		///G_治理风险
		TQCVDEsgScoreType	StockGovernanceRisk;
		///G_外部处分
		TQCVDEsgScoreType	StockExternalSanctionBranch;
		///G_商业道德
		TQCVDEsgScoreType	StockBusinessEthicsBranch;
		///温室气体排放管理与认证
		TQCVDEsgScoreType	StockCarbonEmission;
		///碳减排路线
		TQCVDEsgScoreType	StockRoadmapToNetZero;
		///应对气候变化
		TQCVDEsgScoreType	StockResponseToClimateChange;
		///海绵城市
		TQCVDEsgScoreType	StockSpongeCity;
		///绿色金融
		TQCVDEsgScoreType	StockGreenFinance;
		///土地利用及生物多样性
		TQCVDEsgScoreType	StockLandUseBiodiversity;
		///水资源消耗
		TQCVDEsgScoreType	StockWaterConsumption;
		///材料消耗
		TQCVDEsgScoreType	StockMaterialConsumption;
		///工业排放
		TQCVDEsgScoreType	StockIndustryEmission;
		///有害垃圾
		TQCVDEsgScoreType	StockHazardousWaste;
		///电子垃圾, 
		TQCVDEsgScoreType	StockElectronicWaste;
		///可再生能源
		TQCVDEsgScoreType	StockRenewableEnergy;
		///绿色建筑
		TQCVDEsgScoreType	StockGreenBuilding;
		///绿色工厂
		TQCVDEsgScoreType	StockGreenFactory;
		///可持续认证
		TQCVDEsgScoreType	StockSustainableCertification;
		///供应链管理-E
		TQCVDEsgScoreType	StockGreenSupplyChain;
		///环保处罚
		TQCVDEsgScoreType	StockEnvironmentalPenalties;
		///员工健康与安全, 
		TQCVDEsgScoreType	StockHealthSafety;
		///员工激励和发展
		TQCVDEsgScoreType	StockIncentiveDevelopment;
		///员工关系
		TQCVDEsgScoreType	StockEmployeeRelation;
		///质量认证
		TQCVDEsgScoreType	StockQualityCertification;
		///召回
		TQCVDEsgScoreType	StockRecall;
		///投诉
		TQCVDEsgScoreType	StockComplaint;
		///供应商风险和管理
		TQCVDEsgScoreType	StockSupplierRiskManagement;
		///供应链关系
		TQCVDEsgScoreType	StockSupplierRelation;
		///普惠
		TQCVDEsgScoreType	StockInclusive;
		///社区投资
		TQCVDEsgScoreType	StockCommunityInvestment;
		///就业
		TQCVDEsgScoreType	StockEmployment;
		///科技创新
		TQCVDEsgScoreType	StockTechnologyInnovation;
		///数据安全与隐私
		TQCVDEsgScoreType	StockDataSecurityPrivacy;
		///股东权益保护
		TQCVDEsgScoreType	StockShareholderInterest;
		///ESG治理
		TQCVDEsgScoreType	StockGovernance;
		///风险控制
		TQCVDEsgScoreType	StockRiskManagement;
		///董事会结构
		TQCVDEsgScoreType	StockBoardStructure;
		///管理层稳定性
		TQCVDEsgScoreType	StockManagementStable;
		///ESG外部鉴证
		TQCVDEsgScoreType	StockExternalAssurance;
		///信息披露可信度
		TQCVDEsgScoreType	StockInfoDisclosureCredibility;
		///大股东行为
		TQCVDEsgScoreType	StockMajorShareHolderBehavior;
		///偿债能力
		TQCVDEsgScoreType	StockDebtServicingAbility;
		///法律诉讼
		TQCVDEsgScoreType	StockLawsuit;
		///税收透明度
		TQCVDEsgScoreType	StockTaxTransparency;
		///外部处分
		TQCVDEsgScoreType	StockExternalSanction;
		///商业道德
		TQCVDEsgScoreType	StockBusinessEthics;
		///反贪污和贿赂
		TQCVDEsgScoreType	StockAntiCorruption;
		///股东行为
		TQCVDEsgRiskNameType	StockHolderBehavior;
		///负面经营事件
		TQCVDEsgRiskNameType	StockNegativeBusiness;
		///违法违规
		TQCVDEsgRiskNameType	StockIllegalEvent;
		///过度扩张
		TQCVDEsgScoreType	StockOverExpansion;
		///财务可信度
		TQCVDEsgScoreType	StockFinancialForgery;
		///华证A股ESG尾部风险类型
		TQCVDEsgScoreType	StockEsgTailRisk;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};
	/// 查询华证港股ESG评级数据请求包
	struct CQCVDQryEsgHKStockIndexValueField
	{
		///开始日期
		TQCVDDateType	StartDate;
		///结束日期 
		TQCVDDateType	EndDate;
		///ESG评级类型
		TQCVDEsgTypeType	EsgType;
		///港股编码列表(|分隔)
		TQCVDSecurityCodeListType	SecurityCodeList;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 查询华证港股ESG评级数据应答包
	struct CQCVDEsgHKStockIndexValueField
	{
		///评级日期
		TQCVDDateType	EsgRatingDate;
		///证券代码
		TQCVDSecurityIDType	StockCode;
		///证券简称
		TQCVDLocalBondNameType	StockName;
		///ESG总分
		TQCVDEsgScoreType	EsgScore;
		///ESG评级
		TQCVDEsgRateType	EsgRate;
		///E得分
		TQCVDEsgScoreType	EScore;
		///E评级
		TQCVDEsgRateType	ERate;
		///S得分
		TQCVDEsgScoreType	SScore;
		///S评级
		TQCVDEsgRateType	SRate;
		///G得分
		TQCVDEsgScoreType	GScore;
		///G评级
		TQCVDEsgRateType	GRate;
		///E_气候变化
		TQCVDEsgScoreType	StockClimateChange;
		///E_资源利用
		TQCVDEsgScoreType	StockResourceUtilization;
		///E_环境污染
		TQCVDEsgScoreType	StockEnvironmentalPollution;
		///E_环境友好
		TQCVDEsgScoreType	StockEnvironmentalFriendly;
		///E_环境管理
		TQCVDEsgScoreType	StockEnvironmentalManagement;
		///S_人力资本
		TQCVDEsgScoreType	StockHumanCapital;
		///S_产品责任
		TQCVDEsgScoreType	StockProductResponsibility;
		///S_供应商
		TQCVDEsgScoreType	StockSupplier;
		///S_社会贡献
		TQCVDEsgScoreType	StockSocialContribution;
		///S_数据安全与隐私
		TQCVDEsgScoreType	StockDataSecurityPrivacyBranch;
		///G_股东权益
		TQCVDEsgScoreType	StockShareHolderInterestBranch;
		///G_治理结构
		TQCVDEsgScoreType	StockGovernanceStructure;
		///G_信披质量
		TQCVDEsgScoreType	StockInfoDisclosureQuality;
		///G_治理风险
		TQCVDEsgScoreType	StockGovernanceRisk;
		///G_外部处分
		TQCVDEsgScoreType	StockExternalSanctionBranch;
		///G_商业道德
		TQCVDEsgScoreType	StockBusinessEthicsBranch;
		///温室气体排放管理与认证
		TQCVDEsgScoreType	StockCarbonEmission;
		///碳减排路线
		TQCVDEsgScoreType	StockRoadmapToNetZero;
		///应对气候变化
		TQCVDEsgScoreType	StockResponseToClimateChange;
		///海绵城市
		TQCVDEsgScoreType	StockSpongeCity;
		///绿色金融
		TQCVDEsgScoreType	StockGreenFinance;
		///土地利用及生物多样性
		TQCVDEsgScoreType	StockLandUseBiodiversity;
		///水资源消耗
		TQCVDEsgScoreType	StockWaterConsumption;
		///材料消耗
		TQCVDEsgScoreType	StockMaterialConsumption;
		///工业排放
		TQCVDEsgScoreType	StockIndustryEmission;
		///有害垃圾
		TQCVDEsgScoreType	StockHazardousWaste;
		///电子垃圾, 
		TQCVDEsgScoreType	StockElectronicWaste;
		///可再生能源
		TQCVDEsgScoreType	StockRenewableEnergy;
		///绿色建筑
		TQCVDEsgScoreType	StockGreenBuilding;
		///绿色工厂
		TQCVDEsgScoreType	StockGreenFactory;
		///可持续认证
		TQCVDEsgScoreType	StockSustainableCertification;
		///供应链管理-E
		TQCVDEsgScoreType	StockGreenSupplyChain;
		///环保处罚
		TQCVDEsgScoreType	StockEnvironmentalPenalties;
		///员工健康与安全, 
		TQCVDEsgScoreType	StockHealthSafety;
		///员工激励和发展
		TQCVDEsgScoreType	StockIncentiveDevelopment;
		///员工关系
		TQCVDEsgScoreType	StockEmployeeRelation;
		///质量认证
		TQCVDEsgScoreType	StockQualityCertification;
		///召回
		TQCVDEsgScoreType	StockRecall;
		///投诉
		TQCVDEsgScoreType	StockComplaint;
		///供应商风险和管理
		TQCVDEsgScoreType	StockSupplierRiskManagement;
		///供应链关系
		TQCVDEsgScoreType	StockSupplierRelation;
		///普惠
		TQCVDEsgScoreType	StockInclusive;
		///社区投资
		TQCVDEsgScoreType	StockCommunityInvestment;
		///就业
		TQCVDEsgScoreType	StockEmployment;
		///科技创新
		TQCVDEsgScoreType	StockTechnologyInnovation;
		///数据安全与隐私
		TQCVDEsgScoreType	StockDataSecurityPrivacy;
		///股东权益保护
		TQCVDEsgScoreType	StockShareholderInterest;
		///ESG治理
		TQCVDEsgScoreType	StockGovernance;
		///风险控制
		TQCVDEsgScoreType	StockRiskManagement;
		///董事会结构
		TQCVDEsgScoreType	StockBoardStructure;
		///管理层稳定性
		TQCVDEsgScoreType	StockManagementStable;
		///ESG外部鉴证
		TQCVDEsgScoreType	StockExternalAssurance;
		///信息披露可信度
		TQCVDEsgScoreType	StockInfoDisclosureCredibility;
		///大股东行为
		TQCVDEsgScoreType	StockMajorShareHolderBehavior;
		///偿债能力
		TQCVDEsgScoreType	StockDebtServicingAbility;
		///法律诉讼
		TQCVDEsgScoreType	StockLawsuit;
		///税收透明度
		TQCVDEsgScoreType	StockTaxTransparency;
		///外部处分
		TQCVDEsgScoreType	StockExternalSanction;
		///商业道德
		TQCVDEsgScoreType	StockBusinessEthics;
		///反贪污和贿赂
		TQCVDEsgScoreType	StockAntiCorruption;
		///股东行为
		TQCVDEsgRiskNameType	StockHolderBehavior;
		///负面经营事件
		TQCVDEsgRiskNameType	StockNegativeBusiness;
		///违法违规
		TQCVDEsgRiskNameType	StockIllegalEvent;
		///过度扩张
		TQCVDEsgRiskNameType	StockOverExpansion;
		///财务可信度
		TQCVDEsgRiskNameType	StockFinancialForgery;
		///华证港股ESG尾部风险类型
		TQCVDEsgRiskNameType	StockEsgTailRisk;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};

	/// 快速股票指数行情
	struct CQCVDRapidSecurityIndexDataField
	{
		///交易日
		TQCVDDateType	TradingDay;
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///证券名称
		TQCVDSecurityNameType	SecurityName;
		///昨收盘价
		TQCVDPriceType	PreClosePrice;
		///今开盘价
		TQCVDPriceType	OpenPrice;
		///成交量
		TQCVDLongVolumeType	Volume;
		///成交额
		TQCVDMoneyType	Turnover;
		///最新价
		TQCVDPriceType	LastPrice;
		///最高价
		TQCVDPriceType	HighestPrice;
		///最低价
		TQCVDPriceType	LowestPrice;
		///今收盘价
		TQCVDPriceType	ClosePrice;
		///更新时间
		TQCVDTimeType	UpdateTime;
		///更新毫秒
		TQCVDMillisecType	UpdateMillisec;

	};

	/// 查询行业板块指数分钟K线数据请求
	struct CQCVDQryIndustryIndexMinKDataField
	{
		///指数代码
		TQCVDIndexIDType	IndexID;
		///开始日期
		TQCVDDateType	BeginDate;
		///结束日期
		TQCVDDateType	EndDate;
		///开始时间(分钟)
		TQCVDTimeType	BeginTime;
		///结束时间(分钟)
		TQCVDTimeType	EndTime;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 行业板块指数分钟K线数据
	struct CQCVDIndustryIndexMinKDataField
	{
		///交易日期
		TQCVDDateType	TradingDay;
		///行情时间(分钟)
		TQCVDTimeType	IndexTime;
		///指数代码
		TQCVDIndexIDType	IndexID;
		///最新点位
		TQCVDPriceType	IndexPoint;
		///开盘点位
		TQCVDPriceType	OpenPoint;
		///收盘点位
		TQCVDPriceType	ClosePoint;
		///最高点位
		TQCVDPriceType	HighestPoint;
		///最低点位
		TQCVDPriceType	LowestPoint;
		///成交量(手)
		TQCVDPriceType	Volume;
		///成交金额(千元)
		TQCVDPriceType	Amount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;
		///总成交量(手)
		TQCVDPriceType	TotalVolume;
		///总成交额(千元)
		TQCVDPriceType	TotalAmount;

	};
	/// 查询概念板块指数分钟K线数据请求
	struct CQCVDQryConceptionIndexMinKDataField
	{
		///指数代码
		TQCVDIndexIDType	IndexID;
		///开始日期
		TQCVDDateType	BeginDate;
		///结束日期
		TQCVDDateType	EndDate;
		///开始时间(分钟)
		TQCVDTimeType	BeginTime;
		///结束时间(分钟)
		TQCVDTimeType	EndTime;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 概念板块指数分钟K线数据
	struct CQCVDConceptionIndexMinKDataField
	{
		///交易日期
		TQCVDDateType	TradingDay;
		///行情时间(分钟)
		TQCVDTimeType	IndexTime;
		///指数代码
		TQCVDIndexIDType	IndexID;
		///最新点位
		TQCVDPriceType	IndexPoint;
		///开盘点位
		TQCVDPriceType	OpenPoint;
		///收盘点位
		TQCVDPriceType	ClosePoint;
		///最高点位
		TQCVDPriceType	HighestPoint;
		///最低点位
		TQCVDPriceType	LowestPoint;
		///成交量(手)
		TQCVDPriceType	Volume;
		///成交金额(千元)
		TQCVDPriceType	Amount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;
		///总成交量(手)
		TQCVDPriceType	TotalVolume;
		///总成交额(千元)
		TQCVDPriceType	TotalAmount;

	};

	/// 板块排名实时数据
	struct CQCVDIndexRankNDataField
	{
		///指数代码
		TQCVDIndexIDType	IndexID;
		///排名
		TQCVDVolumeType	Rank;
		///成分股数
		TQCVDVolumeType	ConstituentNum;
		///上涨家数
		TQCVDVolumeType	UpNum;
		///下跌家数
		TQCVDVolumeType	DownNum;
		///涨停家数
		TQCVDVolumeType	UpperLimitNum;
		///跌停家数
		TQCVDVolumeType	LowerLimitNum;
		///90日新高数
		TQCVDVolumeType	New90HighNum;
		///资金净流入
		TQCVDMoneyType	MoneyIn;
		///交易日期
		TQCVDDateType	TradingDay;
		///更新时间
		TQCVDTimeType	UpdateTime;
		///涨幅1-交易所代码
		TQCVDExchangeIDType	ExchangeID1;
		///涨幅1-证券代码
		TQCVDSecurityIDType	SecurityID1;
		///涨幅1-证券名称
		TQCVDSecurityNameType	SecurityName1;
		///涨幅2-交易所代码
		TQCVDExchangeIDType	ExchangeID2;
		///涨幅2-证券代码
		TQCVDSecurityIDType	SecurityID2;
		///涨幅2-证券名称
		TQCVDSecurityNameType	SecurityName2;
		///涨幅3-交易所代码
		TQCVDExchangeIDType	ExchangeID3;
		///涨幅3-证券代码
		TQCVDSecurityIDType	SecurityID3;
		///涨幅3-证券名称
		TQCVDSecurityNameType	SecurityName3;
		///涨幅4-交易所代码
		TQCVDExchangeIDType	ExchangeID4;
		///涨幅4-证券代码
		TQCVDSecurityIDType	SecurityID4;
		///涨幅4-证券名称
		TQCVDSecurityNameType	SecurityName4;
		///指数涨跌幅
		TQCVDPriceType	UpDownRate;
		///涨幅1-证券涨跌幅
		TQCVDPriceType	SecurityUpDownRate1;
		///涨幅2-证券涨跌幅
		TQCVDPriceType	SecurityUpDownRate2;
		///涨幅3-证券涨跌幅
		TQCVDPriceType	SecurityUpDownRate3;
		///涨幅4-证券涨跌幅
		TQCVDPriceType	SecurityUpDownRate4;
		///批次号
		TQCVDVolumeType	BatchNum;

	};
	/// 大盘龙头实时数据
	struct CQCVDStockHeaderDataField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///排名
		TQCVDVolumeType	Rank;
		///涨跌幅
		TQCVDMoneyType	UpDownRate;
		///连板数
		TQCVDVolumeType	ConUpperLimitNum;
		///板块代码1
		TQCVDIndexIDType	IndexID1;
		///板块代码2
		TQCVDIndexIDType	IndexID2;
		///更新时间
		TQCVDTimeType	UpdateTime;
		///交易日
		TQCVDDateType	TradingDay;
		///批次号
		TQCVDVolumeType	BatchNum;

	};

	/// 查询板块指数涨跌幅排名数据
	struct CQCVDQryIndexUpDownTopNDataField
	{
		///开始日期
		TQCVDDateType	BeginDate;
		///结束日期
		TQCVDDateType	EndDate;

	};
	/// 板块指数涨跌幅排名数据
	struct CQCVDIndexUpDownTopNDataField
	{
		///交易日期
		TQCVDDateType	TradingDay;
		///排名
		TQCVDVolumeType	Rank;
		///指数代码
		TQCVDIndexIDType	IndexID;
		///板块名称
		TQCVDSecurityNameType	IndexName;
		///涨跌幅
		TQCVDMoneyType	UpDownRate;

	};
	/// 板块指数热门排名数据
	struct CQCVDIndexHottestTopNDataField
	{
		///指数代码
		TQCVDIndexIDType	IndexID;
		///板块名称
		TQCVDSecurityNameType	IndexName;
		///评分
		TQCVDMoneyType	Score;

	};
	/// 股票指数行情数据
	struct CQCVDStockIndexDataField
	{
		///交易日
		TQCVDDateType	TradingDay;
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///昨收盘价
		TQCVDPriceType	PreClosePrice;
		///今开盘价
		TQCVDPriceType	OpenPrice;
		///成交量
		TQCVDLongVolumeType	Volume;
		///成交额
		TQCVDMoneyType	Turnover;
		///最新价
		TQCVDPriceType	LastPrice;
		///最高价
		TQCVDPriceType	HighestPrice;
		///最低价
		TQCVDPriceType	LowestPrice;
		///更新时间
		TQCVDTimeType	UpdateTime;
		///今收盘价
		TQCVDPriceType	ClosePrice;
		///领先今开盘价
		TQCVDPriceType	LXOpenPrice;
		///领先最新价
		TQCVDPriceType	LXLastPrice;
		///领先最高价
		TQCVDPriceType	LXHighestPrice;
		///领先最低价
		TQCVDPriceType	LXLowestPrice;
		///领先收盘价
		TQCVDPriceType	LXClosePrice;

	};
	/// 查询股票指数分钟K线数据请求
	struct CQCVDQryStockIndexMinKDataField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///开始日期
		TQCVDDateType	BeginDate;
		///结束日期
		TQCVDDateType	EndDate;
		///开始时间(分钟)
		TQCVDTimeType	BeginTime;
		///结束时间(分钟)
		TQCVDTimeType	EndTime;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 股票指数分钟K线数据
	struct CQCVDStockIndexMinKDataField
	{
		///交易日期
		TQCVDDateType	TradingDay;
		///行情时间(分钟)
		TQCVDTimeType	UpdateTime;
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码
		TQCVDSecurityIDType	SecurityID;
		///开盘价
		TQCVDPriceType	OpenPrice;
		///收盘价
		TQCVDPriceType	ClosePrice;
		///最高价
		TQCVDPriceType	HighestPrice;
		///最低价
		TQCVDPriceType	LowestPrice;
		///成交量
		TQCVDPriceType	Volume;
		///成交额
		TQCVDPriceType	Amount;
		///领先开盘价
		TQCVDPriceType	LXOpenPrice;
		///领先收盘价
		TQCVDPriceType	LXClosePrice;
		///领先最高价
		TQCVDPriceType	LXHighestPrice;
		///领先最低价
		TQCVDPriceType	LXLowestPrice;
		///总成交量
		TQCVDPriceType	TotalVolume;
		///总成交额
		TQCVDPriceType	TotalAmount;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};

	/// AH股关联证券查询请求包
	struct CQCVDQryAHRelatedSecuritiesField
	{
		///A股交易所代码
		TQCVDExchangeIDType	ExchangeID_A;
		///A股证券代码 
		TQCVDSecurityIDType	SecurityID_A;
		///H股证券代码 
		TQCVDSecurityIDType	SecurityID_H;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// AH股关联证券应答包
	struct CQCVDAHRelatedSecuritiesField
	{
		///A股交易所代码
		TQCVDExchangeIDType	ExchangeIDA;
		///A股证券代码 
		TQCVDSecurityIDType	SecurityIDA;
		///A股交易所名称
		TQCVDNameType	ExchangeNameA;
		///H股证券代码 
		TQCVDSecurityIDType	SecurityIDH;
		///H股交易所名称
		TQCVDNameType	ExchangeNameH;
		///证券简称
		TQCVDSecurityNameType	SecurityName;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};
	/// 查询共同基金被动型基金跟踪指数请求包
	struct CQCVDQryChinaMutualFundTrackingIndexField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///基金代码 
		TQCVDSecurityIDType	SecurityID;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 查询共同基金被动型基金跟踪指数应答包
	struct CQCVDChinaMutualFundTrackingIndexField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///基金代码 
		TQCVDSecurityIDType	SecurityID;
		///指数代码
		TQCVDSecurityIDType	IndexCode;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};
	/// 查询中国债券份额变动请求包
	struct CQCVDQryCBondAmountField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///合约代码 
		TQCVDSecurityIDType	SecurityID;
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 查询中国债券份额变动应答包
	struct CQCVDCBondAmountField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///合约代码 
		TQCVDSecurityIDType	SecurityID;
		///变动日期
		TQCVDDateType	ChangeDate;
		///变动原因
		TQCVDVolumeType	ChangeReason;
		///债券份额(亿元)
		TQCVDMoneyType	OutstandingBalance;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};

	/// 查询中国共同基金场内流通份额请求包
	struct CQCVDQryChinaMutualFundFloatShareField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///合约代码
		TQCVDSecurityIDType	SecurityID;
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 查询中国共同基金场内流通份额应答包
	struct CQCVDChinaMutualFundFloatShareField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///合约代码 
		TQCVDSecurityIDType	SecurityID;
		///交易日期
		TQCVDDateType	TradeDate;
		///场内份额(份)
		TQCVDMoneyType	FloatShare;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};
	/// 查询A股股本请求包
	struct CQCVDQryAShareCapitalizationField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///合约代码
		TQCVDSecurityIDType	SecurityID;
		///起始变动日期
		TQCVDDateType	BegChangeDate;
		///结束变动日期
		TQCVDDateType	EndChangeDate;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 查询A股股本应答包
	struct CQCVDAShareCapitalizationField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///合约代码 
		TQCVDSecurityIDType	SecurityID;
		///变动日期
		TQCVDDateType	ChangeDate;
		///总股本(万股)
		TQCVDMoneyType	Tot;
		///流通股(万股)
		TQCVDMoneyType	Float;
		///流通A股(万股)
		TQCVDMoneyType	FloatA;
		///流通B股(万股)
		TQCVDMoneyType	FloatB;
		///香港流通股(万股)
		TQCVDMoneyType	FloatH;
		///海外流通股(万股)
		TQCVDMoneyType	FloatOverseas;
		///限售A股(万股)
		TQCVDMoneyType	RestrictedA;
		///限售A股(国家持股)
		TQCVDMoneyType	RtdState;
		///限售A股(国有法人持股)
		TQCVDMoneyType	RtdStateJur;
		///流限售A股(其他内资持股)
		TQCVDMoneyType	RtdSubOtherDomes;
		///限售A股(其他内资持股:境内法人持股)
		TQCVDMoneyType	RtdDomesJur;
		///限售A股(其他内资持股:机构配售股)
		TQCVDMoneyType	RtdInst;
		///限售A股(其他内资持股:境内自然人持股)
		TQCVDMoneyType	RtdDomesNp;
		///限售股份(高管持股)(万股)
		TQCVDMoneyType	RtdSenManager;
		///限售A股(外资持股)
		TQCVDMoneyType	RtdSubFrgn;
		///限售A股(境外法人持股)
		TQCVDMoneyType	RtdFrgnJur;
		///限售A股(境外自然人持股)
		TQCVDMoneyType	RtdFrgnNp;
		///限售B股(万股)
		TQCVDMoneyType	RestrictedBShr;
		///其他限售股
		TQCVDMoneyType	OtherRestricted;
		///非流通股
		TQCVDMoneyType	NonTradable;
		///非流通股(国有股)
		TQCVDMoneyType	NtrdStatePct;
		///非流通股(国家股)
		TQCVDMoneyType	NtrdState;
		///非流通股(国有法人股)
		TQCVDMoneyType	NtrdStatJur;
		///非流通股(境内法人股)
		TQCVDMoneyType	NtrdSubDomesJur;
		///非流通股(境内法人股:境内发起人股)
		TQCVDMoneyType	NtrdDomesInitor;
		///非流通股(境内法人股:募集法人股)
		TQCVDMoneyType	NtrdIpoJurIS;
		///非流通股(境内法人股:一般法人股)
		TQCVDMoneyType	NtrdGenJurIS;
		///非流通股(境内法人股:战略投资者持股)
		TQCVDMoneyType	NtrdStrtInvestor;
		///非流通股(境内法人股:基金持股)
		TQCVDMoneyType	NtrdFundBal;
		///非流通股(自然人股)
		TQCVDMoneyType	NtrdIpoINIP;
		///转配股(万股)
		TQCVDMoneyType	NtrdTrfn;
		///流通股(高管持股)
		TQCVDMoneyType	NtrdSnorMnger;
		///内部职工股(万股)
		TQCVDMoneyType	NtrdInsderemp;
		///优先股(万股)
		TQCVDMoneyType	NtrdPrf;
		///非流通股(非上市外资股)
		TQCVDMoneyType	NtrdNonlstfrgn;
		///STAQ股(万股)
		TQCVDMoneyType	NtrdStaq;
		///NET股(万股)
		TQCVDMoneyType	NtrdNet;
		///股本变动原因
		TQCVDSerialType	ChangeReason;
		///A股合计
		TQCVDMoneyType	TotalA;
		///B股合计
		TQCVDMoneyType	TotalB;
		///三板A股
		TQCVDMoneyType	OTCA;
		///三板B股
		TQCVDMoneyType	OTCB;
		///三板合计
		TQCVDMoneyType	TotalOTC;
		///香港上市股
		TQCVDMoneyType	ShareH;
		///流通股合计
		TQCVDMoneyType	TotalTradable;
		///限售股合计
		TQCVDMoneyType	TotalRestricted;
		///公告日期
		TQCVDDateType	AnnDate;
		///登记日期
		TQCVDDateType	ChangeDate1;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};
	/// 查询A股投资评级汇总请求包
	struct CQCVDQryAShareStockRatingConsusField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///合约代码
		TQCVDSecurityIDType	SecurityID;
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 查询A股投资评级汇总应答包
	struct CQCVDAShareStockRatingConsusField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///合约代码 
		TQCVDSecurityIDType	SecurityID;
		///日期
		TQCVDDateType	RatingDate;
		///综合评级
		TQCVDMoneyType	Avg;
		///评级机构数量
		TQCVDMoneyType	InstNum;
		///调高家数（相比一月前）
		TQCVDMoneyType	Upgrade;
		///调低家数（相比一月前）
		TQCVDMoneyType	Downgrade;
		///维持家数（相比一月前）
		TQCVDMoneyType	Maintain;
		///买入家数
		TQCVDMoneyType	BuyNum;
		///增持家数
		TQCVDMoneyType	OutperformNum;
		///中性家数
		TQCVDMoneyType	HoldNum;
		///减持家数
		TQCVDMoneyType	UnderperformNum;
		///卖出家数
		TQCVDMoneyType	SellNum;
		///周期
		TQCVDCodeType	Cycle;
		///一致预测目标价
		TQCVDPriceType	Price;
		///目标价预测机构数
		TQCVDMoneyType	PriceInstNum;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};
	/// 查询A股日收益率请求包
	struct CQCVDQryAShareYieldField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///合约代码
		TQCVDSecurityIDType	SecurityID;
		///起始日期
		TQCVDDateType	BegDate;
		///结束日期
		TQCVDDateType	EndDate;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 查询A股日收益率应答包
	struct CQCVDAShareYieldField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///合约代码 
		TQCVDSecurityIDType	SecurityID;
		///交易日期
		TQCVDDateType	TradeDate;
		///日涨跌幅(%)
		TQCVDMoneyType	PctChangeD;
		///周涨跌幅(%)
		TQCVDMoneyType	PctChangeW;
		///月涨跌幅(%)
		TQCVDMoneyType	PctChangeM;
		///周成交量
		TQCVDMoneyType	VolumeW;
		///月成交量
		TQCVDMoneyType	VolumeM;
		///周成交额
		TQCVDMoneyType	AmountW;
		///月成交额
		TQCVDMoneyType	AmountM;
		///日换手率
		TQCVDMoneyType	TurnoverD;
		///日换手率(自由流通股本)
		TQCVDMoneyType	TurnoverDFloat;
		///周换手率
		TQCVDMoneyType	TurnoverW;
		///周换手率(自由流通股本)
		TQCVDMoneyType	TurnoverWFloat;
		///周平均换手率
		TQCVDMoneyType	TurnoverWAve;
		///周平均换手率(自由流通股本)
		TQCVDMoneyType	TurnoverWAveFloat;
		///月换手率
		TQCVDMoneyType	TurnoverM;
		///月换手率(自由流通股本)
		TQCVDMoneyType	TurnoverMFloat;
		///月平均换手率
		TQCVDMoneyType	TurnoverMAve;
		///月平均换手率(自由流通股本)
		TQCVDMoneyType	TurnoverMAveFloat;
		///周平均涨跌幅(100周)
		TQCVDMoneyType	PctChangeAve100W;
		///周标准差(100周)
		TQCVDMoneyType	StdDeviation100W;
		///周方差(100周)
		TQCVDMoneyType	Variance100W;
		///月平均涨跌幅(24月)
		TQCVDMoneyType	PctChangeAve24M;
		///月标准差(24月)
		TQCVDMoneyType	StdDeviation24M;
		///月方差(24月)
		TQCVDMoneyType	Variance24M;
		///月平均涨跌幅(60月)
		TQCVDMoneyType	PctChangeAve60M;
		///月标准差(60月)
		TQCVDMoneyType	StdDeviation60M;
		///月方差(60月)
		TQCVDMoneyType	Variance60M;
		///日BETA(1年)
		TQCVDMoneyType	BetaDay1Y;
		///日BETA(2年)
		TQCVDMoneyType	BetaDay2Y;
		///日ALPHA(1年)
		TQCVDMoneyType	AlphaDay1Y;
		///日ALPHA(2年)
		TQCVDMoneyType	AlphaDay2Y;
		///周BETA(100周)
		TQCVDMoneyType	Beta100W;
		///周ALPHA(100周)
		TQCVDMoneyType	Alpha100W;
		///月BETA(24月)
		TQCVDMoneyType	Beta24M;
		///月BETA(60月)
		TQCVDMoneyType	Beta60M;
		///月ALPHA(24月)
		TQCVDMoneyType	Alpha24M;
		///月ALPHA(60月)
		TQCVDMoneyType	Alpha60M;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};
	/// 查询A股盈利预测汇总请求包
	struct CQCVDQryAShareConsensusDataField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///合约代码
		TQCVDSecurityIDType	SecurityID;
		///起始预测日期
		TQCVDDateType	BegEstDate;
		///结束预测日期
		TQCVDDateType	EndEstDate;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 查询A股盈利预测汇总应答包
	struct CQCVDAShareConsensusDataField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///合约代码 
		TQCVDSecurityIDType	SecurityID;
		///预测日期
		TQCVDDateType	EstDate;
		///预测报告期
		TQCVDDateType	EstReportDate;
		///预测机构家数
		TQCVDVolumeType	EstInstNum;
		///每股收益平均值(元)
		TQCVDMoneyType	EpsAvg;
		///主营业务收入平均值(万元)
		TQCVDMoneyType	MainBusIncAvg;
		///净利润平均值(万元)
		TQCVDMoneyType	NetProfitAvg;
		///息税前利润平均值(万元)
		TQCVDMoneyType	EbitAvg;
		///息税折旧摊销前利润平均值(万元)
		TQCVDMoneyType	EbitdaAvg;
		///每股收益中值(元)
		TQCVDMoneyType	EpsMedian;
		///主营业务收入中值(万元)
		TQCVDMoneyType	MainBusIncMedian;
		///净利润中值(万元)
		TQCVDMoneyType	NetProfitMedian;
		///息税前利润中值(万元)
		TQCVDMoneyType	EbitMedian;
		///息税折旧摊销前利润中值(万元)
		TQCVDMoneyType	EbitdaMedian;
		///综合值周期类型
		TQCVDCodeType	ConsenDataCycleTyp;
		///每股收益标准差
		TQCVDMoneyType	EpsDev;
		///主营业务收入标准差(万元)
		TQCVDMoneyType	MainBusIncDev;
		///净利润标准差(万元)
		TQCVDMoneyType	NetProfitDev;
		///息税前利润标准差(万元)
		TQCVDMoneyType	EbitDev;
		///息税折旧摊销前利润标准差(万元)
		TQCVDMoneyType	EbitdaDev;
		///每股收益最大值
		TQCVDMoneyType	EpsMax;
		///每股收益最小值
		TQCVDMoneyType	EpsMin;
		///主营业务收入最大值(万元)
		TQCVDMoneyType	MainBusIncMax;
		///主营业务收入最小值(万元)
		TQCVDMoneyType	MainBusIncMin;
		///主营业务收入调高家数（与一个月前相比）
		TQCVDMoneyType	MainBusIncUpgrade;
		///主营业务收入调低家数（与一个月前相比）
		TQCVDMoneyType	MainBusIncDowngrade;
		///主营业务收入维持家数（与一个月前相比）
		TQCVDMoneyType	MainBusIncMaintain;
		///净利润最大值（万元)
		TQCVDMoneyType	NetProfitMax;
		///净利润最小值（万元)
		TQCVDMoneyType	NetProfitMin;
		///净利润调高家数（与一个月前相比）
		TQCVDMoneyType	NetProfitUpgrade;
		///净利润调低家数（与一个月前相比）
		TQCVDMoneyType	NetProfitDowngrade;
		///净利润维持家数（与一个月前相比）
		TQCVDMoneyType	NetProfitMaintain;
		///每股现金流平均值
		TQCVDMoneyType	AvgCps;
		///每股现金流中值
		TQCVDMoneyType	MedianCps;
		///每股现金流标准差
		TQCVDMoneyType	StdCps;
		///每股现金流最大值
		TQCVDMoneyType	MaxCps;
		///每股现金流最小值
		TQCVDMoneyType	MinCps;
		///每股股利平均值
		TQCVDMoneyType	AvgDps;
		///每股股利中值
		TQCVDMoneyType	MedianDps;
		///每股股利标准差
		TQCVDMoneyType	StdDps;
		///每股股利最大值
		TQCVDMoneyType	MaxDps;
		///每股股利最小值
		TQCVDMoneyType	MinDps;
		///息税前利润最大值(万元)
		TQCVDMoneyType	EbitMax;
		///息税前利润最小值(万元)
		TQCVDMoneyType	EbitMin;
		///息税折旧摊销前利润最大值(万元)
		TQCVDMoneyType	EbitdaMax;
		///息税折旧摊销前利润最小值(万元)
		TQCVDMoneyType	EbitdaMin;
		///每股净资产平均值
		TQCVDMoneyType	AvgBps;
		///每股净资产中值
		TQCVDMoneyType	MedianBps;
		///每股净资产标准差
		TQCVDMoneyType	StdBps;
		///每股净资产最大值
		TQCVDMoneyType	MaxBps;
		///每股净资产最小值
		TQCVDMoneyType	MinBps;
		///利润总额平均值(万元)
		TQCVDMoneyType	AvgEbt;
		///利润总额中值(万元)
		TQCVDMoneyType	MedianEbt;
		///利润总额标准差(万元)
		TQCVDMoneyType	StdEbt;
		///利润总额最大值(万元)
		TQCVDMoneyType	MaxEbt;
		///利润总额最小值(万元)
		TQCVDMoneyType	MinEbt;
		///总资产收益率平均值（%）
		TQCVDMoneyType	AvgRoa;
		///总资产收益率中值（%）
		TQCVDMoneyType	MedianRoa;
		///总资产收益率标准差（%）
		TQCVDMoneyType	StdRoa;
		///总资产收益率最大值（%）
		TQCVDMoneyType	MaxRoa;
		///总资产收益率最小值（%）
		TQCVDMoneyType	MinRoa;
		///净资产收益率平均值（%）
		TQCVDMoneyType	AvgRoe;
		///净资产收益率中值（%）
		TQCVDMoneyType	MedianRoe;
		///净资产收益率标准差（%）
		TQCVDMoneyType	StdRoe;
		///净资产收益率最大值（%）
		TQCVDMoneyType	MaxRoe;
		///净资产收益率最小值（%）
		TQCVDMoneyType	MinRoe;
		///营业利润平均值(万元)
		TQCVDMoneyType	AvgOperatingProfit;
		///营业利润中值(万元)
		TQCVDMoneyType	MedianOperatingProfit;
		///营业利润标准差(万元)
		TQCVDMoneyType	StdOperatingProfit;
		///营业利润最大值(万元)
		TQCVDMoneyType	MaxOperatingProfit;
		///营业利润最小值(万元)
		TQCVDMoneyType	MinOperatingProfit;
		///每股收益预测家数
		TQCVDMoneyType	EpsInstNum;
		///主营业务收入预测家数
		TQCVDMoneyType	MainBusincInstNum;
		///净利润预测家数
		TQCVDMoneyType	NetProfitInstNum;
		///每股现金流预测家数
		TQCVDMoneyType	CpsInstNum;
		///每股股利预测家数
		TQCVDMoneyType	DpsInstNum;
		///息税前利润预测家数
		TQCVDMoneyType	EbitInstNum;
		///息税折旧摊销前利润预测家数
		TQCVDMoneyType	EbitDaInstNum;
		///每股净资产预测家数
		TQCVDMoneyType	BpsInstNum;
		///利润总额预测家数
		TQCVDMoneyType	EbtInstNum;
		///总资产收益率预测家数
		TQCVDMoneyType	RoaInstNum;
		///净资产资产收益率预测家数
		TQCVDMoneyType	RoeInstNum;
		///营业利润预测家数
		TQCVDMoneyType	OprofitInstNum;
		///营业成本及附加平均值(万元)
		TQCVDMoneyType	AvgOc;
		///营业成本及附加中值(万元)
		TQCVDMoneyType	MediaOc;
		///营业成本及附加标准差(万元)
		TQCVDMoneyType	StOc;
		///营业成本及附加最大值(万元)
		TQCVDMoneyType	MaxOc;
		///营业成本及附加最小值(万元)
		TQCVDMoneyType	MinOc;
		///营业成本及附加预测家数
		TQCVDMoneyType	OcInstNum;
		///预测基准股本综合值
		TQCVDMoneyType	BaseShare;
		///预测报告期
		TQCVDNodeType	EstYearType;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};
	/// 查询A股中信行业分类请求包
	struct CQCVDQryAShareIndustriesClassCITICSField
	{
		///生效日期(该日期大于纳入日期，小于剔除日期)
		TQCVDDateType	TradingDay;
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///合约代码
		TQCVDSecurityIDType	SecurityID;
		///中信行业代码
		TQCVDSecurityIDType	CITICSIndustryCode;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 查询A股中信行业分类应答包
	struct CQCVDAShareIndustriesClassCITICSField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///合约代码 
		TQCVDSecurityIDType	SecurityID;
		///中信行业代码
		TQCVDSecurityIDType	CITICSIndustryCode;
		///中信行业名称
		TQCVDSecurityNameType	CITICSIndustryName;
		///纳入日期
		TQCVDDateType	EntryDate;
		///剔除日期
		TQCVDDateType	RemoveDate;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};
	/// 查询A股指数月权重请求包
	struct CQCVDQryAIndexMonthWeightField
	{
		///指数代码
		TQCVDSecurityIDType	IndexID;
		///起始公布日期
		TQCVDDateType	BegDate;
		///结束公布日期  
		TQCVDDateType	EndDate;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 查询A股指数月权重应答包
	struct CQCVDAIndexMonthWeightField
	{
		///指数代码 
		TQCVDSecurityIDType	IndexID;
		///成分股交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///成分股合约代码 
		TQCVDSecurityIDType	SecurityID;
		///公布日期
		TQCVDDateType	TradeDate;
		///权重
		TQCVDRatioType	Weight;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};
	/// 查询A股盈利预测明细请求包
	struct CQCVDQryAShareEarningEstField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///合约代码
		TQCVDSecurityIDType	SecurityID;
		///起始预测日期
		TQCVDDateType	BegEstDate;
		///结束预测日期
		TQCVDDateType	EndEstDate;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 查询A股盈利预测明细应答包
	struct CQCVDAShareEarningEstField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///合约代码 
		TQCVDSecurityIDType	SecurityID;
		///研究机构名称
		TQCVDNameType	ResearchInstName;
		///分析师名称
		TQCVDNameType	AnalystName;
		///预测日期
		TQCVDDateType	EstDate;
		///预测报告期
		TQCVDDateType	ReportingPeriod;
		///预测主营业务收入(万元)
		TQCVDMoneyType	EstEpsDiluted;
		///预测主营业务收入(万元)
		TQCVDMoneyType	EstNetProfit;
		///预测主营业务收入(万元)
		TQCVDMoneyType	EstMainBusInc;
		///预测息税前利润(万元)
		TQCVDMoneyType	EstEbit;
		///预测息税折旧摊销前利润(万元)
		TQCVDMoneyType	EstEbitda;
		///预测基准股本(万股)
		TQCVDMoneyType	EstBaseCap;
		///公告日期(内部)
		TQCVDDateType	AnnDate;
		///预测每股现金流
		TQCVDMoneyType	Cps;
		///预测每股股利
		TQCVDMoneyType	Dps;
		///预测每股净资产
		TQCVDMoneyType	Bps;
		///预测利润总额（万元）
		TQCVDMoneyType	Ebt;
		///预测总资产收益率
		TQCVDMoneyType	Roa;
		///预测净资产收益率
		TQCVDMoneyType	Roe;
		///预测营业利润(万元）
		TQCVDMoneyType	Oprofit;
		///预测每股收益(稀释)(元)
		TQCVDMoneyType	Epsdiluted;
		///预测每股收益(基本)(元)
		TQCVDMoneyType	Epsbasic;
		///预测营业成本及附加（万元）
		TQCVDMoneyType	Oc;
		///预测净利润（换算）（万元）
		TQCVDMoneyType	Npcal;
		///预测每股收益(换算)
		TQCVDMoneyType	Epscal;
		///预测净利润调整比率
		TQCVDMoneyType	Nprate;
		///预测EPS调整比率
		TQCVDMoneyType	Epsrate;
		///预测市盈率
		TQCVDMoneyType	Pe;
		///预测市净率
		TQCVDMoneyType	Pb;
		///预测EV/EBITDA
		TQCVDMoneyType	Evebitda;
		///预测股息率
		TQCVDMoneyType	DividendYield;
		///预测有效截止
		TQCVDDateType	EndDate;
		///预测主营业务利润率
		TQCVDMoneyType	Ope;
		///收录日期
		TQCVDDateType	CollectDate;
		///收录时间
		TQCVDTimeType	CollectTime;
		///报告类型
		TQCVDVolumeType	ReportTypeCode;
		///报告标题
		TQCVDChineseIntroductionType	ReportName;
		///综合值计算标记
		TQCVDVolumeType	ValueCalculation;
		///市销率
		TQCVDMoneyType	Ps;
		///预测毛利率
		TQCVDMoneyType	GrossProfitMargin;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};
	/// 查询A股投资评级明细请求包
	struct CQCVDQryAShareStockRatingField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///合约代码
		TQCVDSecurityIDType	SecurityID;
		///起始评级日期
		TQCVDDateType	BegDate;
		///结束评级日期
		TQCVDDateType	EndDate;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 查询A股投资评级明细应答包
	struct CQCVDAShareStockRatingField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///合约代码 
		TQCVDSecurityIDType	SecurityID;
		///研究机构名称
		TQCVDCompNameType	InstituteName;
		///分析师名称
		TQCVDNameType	RatingAnalyst;
		///评级日期
		TQCVDDateType	EstNewTimeInst;
		///本次标准评级
		TQCVDMoneyType	ScoreRating;
		///前次标准评级
		TQCVDMoneyType	PreScoreRating;
		///本次最低目标价
		TQCVDMoneyType	LowPrice;
		///本次最高目标价
		TQCVDMoneyType	HighPrice;
		///前次最低目标价
		TQCVDMoneyType	PreLowPrice;
		///前次最高目标价
		TQCVDMoneyType	PreHighPrice;
		///公告日期(内部)
		TQCVDDateType	AnnDate;
		///本次评级
		TQCVDOperwaysType	Rating;
		///前次评级
		TQCVDOperwaysType	PreRating;
		///报告标题
		TQCVDDistriValueType	ReportTitle;
		///报告类别
		TQCVDVolumeType	ReportType;
		///评级变动方向
		TQCVDVolumeType	RatingChange;
		///评级有效截止日
		TQCVDDateType	RatingValidEndDate;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};
	/// 查询A股员工构成请求包
	struct CQCVDQryAShareStaffStructureField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///合约代码
		TQCVDSecurityIDType	SecurityID;
		///起始统计日期
		TQCVDDateType	BegDate;
		///结束统计日期
		TQCVDDateType	EndDate;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 查询A股员工构成应答包
	struct CQCVDAShareStaffStructureField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///合约代码 
		TQCVDSecurityIDType	SecurityID;
		///人数类别代码
		TQCVDVolumeType	StaffTypeCode;
		///截止日期
		TQCVDDateType	EndDate;
		///报告类型代码
		TQCVDVolumeType	ReportTypeCode;
		///项目分类代码
		TQCVDVolumeType	ItemTypeCode;
		///项目
		TQCVDMemoType	ItemName;
		///项目代码
		TQCVDVolumeType	ItemCode;
		///人数
		TQCVDVolumeType	StaffNumber;
		///所占比例
		TQCVDMoneyType	Proportion;
		///公告日期
		TQCVDDateType	AnnDate;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};

	/// 查询A股特别处理请求包
	struct CQCVDQryAShareSTField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码 
		TQCVDSecurityIDType	SecurityID;
		///起始实施日期
		TQCVDDateType	BegEntryDate;
		///结束实施日期
		TQCVDDateType	EndEntryDate;
		///特别处理类型
		TQCVDTradeStatusType	STType;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 查询A股特别处理应答包
	struct CQCVDAShareSTField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///证券代码 
		TQCVDSecurityIDType	SecurityID;
		///特别处理类型
		TQCVDTradeStatusType	STType;
		///实施日期
		TQCVDDateType	EntryDate;
		///撤销日期
		TQCVDDateType	RemoveDate;
		///公告日期
		TQCVDDateType	AnnDate;
		///实施原因
		TQCVDRemarkType	Reason;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};

	/// 查询现货投资者多节点信息请求包
	struct CQCVDQryInvestorNodePosStockField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

	};
	/// 查询现货投资者多节点信息应答包
	struct CQCVDInvestorNodePosStockField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///节点代码
		TQCVDNodeInfoType	NodeID;

	};
	/// 查询两融投资者多节点信息请求包
	struct CQCVDQryInvestorNodePosCreditField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;

	};
	/// 查询两融投资者多节点信息应答包
	struct CQCVDInvestorNodePosCreditField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///节点代码
		TQCVDNodeInfoType	NodeID;

	};

	/// 查询港股权益事件请求包
	struct CQCVDQryHKshareEventField
	{
		///证券代码 
		TQCVDSecurityIDType	SecurityID;
		///事件类型代码
		TQCVDVolumeType	EventTypeCode;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;

	};
	/// 查询港股权益事件应答包
	struct CQCVDHKshareEventField
	{
		///证券代码 
		TQCVDSecurityIDType	SecurityID;
		///事件类型
		TQCVDMemoType	EventType;
		///除权日
		TQCVDDateType	ExDate;
		///截止过户起始日
		TQCVDDateType	StartDate;
		///截止过户截止日
		TQCVDDateType	EndDate;
		///股东大会会议日期
		TQCVDDateType	GMDate;
		///现金发放日
		TQCVDDateType	PaymentDate;
		///红股发送日
		TQCVDDateType	BonusShareDDate;
		///交易币种派息比例
		TQCVDMoneyType	CashDivRatio;
		///红股送转比例
		TQCVDMoneyType	BonusShareRatio;
		///实物派送比例
		TQCVDMoneyType	InSpecieRatio;
		///红利认股权证派送比例
		TQCVDMoneyType	BonusWarrantRatio;
		///供股\公开发售比例
		TQCVDMoneyType	RightIssueRatio;
		///每股供股股份\发售股份价格
		TQCVDPriceType	RightIssuePrice;
		///合并比例
		TQCVDMoneyType	ConsolidationRatio;
		///拆分比例
		TQCVDMoneyType	SplitRatio;
		///换股比例
		TQCVDMoneyType	ExchangeRatio;
		///削减比例
		TQCVDMoneyType	CancellationRatio;
		///货币代码
		TQCVDCurrencyIDType	CrncyCode;
		///首次公告日期
		TQCVDDateType	FAnnDate;
		///最新公告日期
		TQCVDDateType	NAnnDate;
		///报告起始日
		TQCVDDateType	RStartDate;
		///报告截止日
		TQCVDDateType	REndDate;
		///报告类型代码
		TQCVDVolumeType	ReType;
		///认股权证发送日
		TQCVDDateType	BonusWarrantDate;
		///实物股票发送日
		TQCVDDateType	InSpecieDate;
		///供股\公开发售股份发送日
		TQCVDDateType	RightIssueDDate;
		///红股上市日
		TQCVDDateType	BonusShareLDate;
		///供股股份\发售股份上市日
		TQCVDDateType	RightIssueLDate;
		///转增比例
		TQCVDMoneyType	TransferRatio;
		///送股比例
		TQCVDMoneyType	PresentRatio;
		///派息原始币种
		TQCVDCurrencyIDType	DividendCurrency;
		///原始币种派息比例
		TQCVDMoneyType	CashDivRatio1;
		///股权登记日
		TQCVDDateType	ShareRecordDate;
		///事件类型代码
		TQCVDVolumeType	EventTypeCode;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};

	/// 查询奇点客户实时业务标识
	struct CQCVDQrySingularityRealTimeBuFlagField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;

	};
	/// 奇点客户实时业务标识
	struct CQCVDSingularityRealTimeBuFlagField
	{
		///投资者代码
		TQCVDInvestorIDType	InvestorID;
		///是否为奇点客户
		TQCVDBoolType	IsSingularity;
		///是否有深A持仓
		TQCVDBoolType	IsExistSZPosition;
		///是否有深A委托
		TQCVDBoolType	IsExistSZOrder;
		///是否有深A新股配号
		TQCVDBoolType	IsExistSZIPONumberResult;
		///是否有深A新股中签
		TQCVDBoolType	IsExistSZIPOMatchNumberResult;

	};

	/// 查询A股指数成份股请求包
	struct CQCVDQryAIndexMembersField
	{
		///交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///指数代码 
		TQCVDSecurityIDType	IndexID;
		///排列方式 1正序 2倒序 默认倒序
		TQCVDOrderSortType	OrderType;
		///每页记录数
		TQCVDVolumeType	PageCount;
		///页定位符
		TQCVDPageLocateType	PageLocate;


	};
	/// 查询A股指数成份股应答包
	struct CQCVDAIndexMembersField
	{
		///指数交易所代码
		TQCVDExchangeIDType	ExchangeID;
		///指数代码
		TQCVDSecurityIDType	IndexID;
		///成分股交易所代码
		TQCVDExchangeIDType	ConExchangeID;
		///成分股代码
		TQCVDSecurityIDType	ConSecurityID;
		///纳入日期
		TQCVDDateType	ConInDate;
		///页定位符
		TQCVDPageLocateType	PageLocate;
		///总页数
		TQCVDPageLocateType	PageTotal;

	};


}

#endif