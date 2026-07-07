CREATE TABLE IF NOT EXISTS stock_trade_calendar (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    trade_date DATE NOT NULL COMMENT '交易日期',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_trade_date (trade_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票交易日历表';

CREATE TABLE IF NOT EXISTS stock_sync_task (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    stock_code VARCHAR(6) NOT NULL COMMENT '股票代码',
    free_float_synced TINYINT NOT NULL DEFAULT 0 COMMENT '是否已完成自由流通股同步：0=未完成，1=已完成',
    daily_kline_synced TINYINT NOT NULL DEFAULT 0 COMMENT '是否已完成日K同步：0=未完成，1=已完成',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_stock_code (stock_code),
    KEY idx_free_float_synced (free_float_synced),
    KEY idx_daily_kline_synced (daily_kline_synced)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票历史数据同步任务进度表';

CREATE TABLE IF NOT EXISTS stock_free_float_share_history (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    stock_code VARCHAR(6) NOT NULL COMMENT '股票代码',
    free_shares BIGINT NOT NULL COMMENT '自由流通股本，单位：股；接口返回万股后放大10000倍',
    start_date DATE NOT NULL COMMENT '区间开始日期，对应接口ChangeDateEX',
    end_date DATE DEFAULT NULL COMMENT '区间结束日期；NULL表示当前仍然有效',
    announcement_date DATE DEFAULT NULL COMMENT '公告日期，对应接口AnnouncementDate',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_stock_start_date (stock_code, start_date),
    KEY idx_stock_code (stock_code),
    KEY idx_stock_date_range (stock_code, start_date, end_date),
    KEY idx_current (stock_code, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票自由流通股本历史区间表';

CREATE TABLE IF NOT EXISTS stock_previous_name_history (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    stock_code VARCHAR(6) DEFAULT NULL COMMENT '股票代码',
    stock_name VARCHAR(20) NOT NULL COMMENT '股票名称',
    start_date DATE NOT NULL COMMENT '名称开始使用日期',
    end_date DATE DEFAULT NULL COMMENT '名称结束使用日期；NULL表示当前仍然有效',
    PRIMARY KEY (id),
    UNIQUE KEY uk_stock_start_date (stock_code, start_date),
    KEY idx_stock_date_range (stock_code, start_date, end_date),
    KEY idx_current (stock_code, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票曾用名历史表';

CREATE TABLE IF NOT EXISTS stock_current_status (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    stock_code VARCHAR(6) NOT NULL COMMENT '股票代码',
    margin_trading TINYINT NOT NULL DEFAULT 1 COMMENT '是否融资融券：0=否，1=是',
    convertible_bond TINYINT NOT NULL DEFAULT 0 COMMENT '是否有可转债：0=否，1=是',
    region_name VARCHAR(50) DEFAULT NULL COMMENT '地域名称',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_stock_code (stock_code),
    KEY idx_margin_trading (margin_trading),
    KEY idx_convertible_bond (convertible_bond),
    KEY idx_region_name (region_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票当前状态表';

CREATE TABLE IF NOT EXISTS stock_convertible_bond_history (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    stock_code VARCHAR(6) NOT NULL COMMENT '正股代码',
    bond_code VARCHAR(6) NOT NULL COMMENT '转债代码',
    market VARCHAR(10) NOT NULL COMMENT '转债市场',
    bond_name VARCHAR(50) DEFAULT NULL COMMENT '转债名称',
    start_date DATE DEFAULT NULL COMMENT '上市日期',
    end_date DATE DEFAULT NULL COMMENT '退市或到期日期',
    maturity_date DATE DEFAULT NULL COMMENT '到期日期',
    failure INT DEFAULT NULL COMMENT '是否失效',
    outstanding_balance DOUBLE DEFAULT NULL COMMENT '存续余额',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_bond_market (bond_code, market),
    KEY idx_stock_code (stock_code),
    KEY idx_stock_date_range (stock_code, start_date, end_date),
    KEY idx_bond_code (bond_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票可转债历史表';

CREATE TABLE IF NOT EXISTS stock_daily (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    stock_code VARCHAR(6) NOT NULL COMMENT '股票代码',
    stock_name VARCHAR(20) DEFAULT NULL COMMENT '当日股票名称',
    is_st TINYINT NOT NULL DEFAULT 0 COMMENT '当日是否ST：0=否，1=是',
    trade_date DATE NOT NULL COMMENT '交易日期',
    open_price DOUBLE DEFAULT NULL COMMENT '开盘价',
    high_price DOUBLE DEFAULT NULL COMMENT '最高价',
    low_price DOUBLE DEFAULT NULL COMMENT '最低价',
    close_price DOUBLE DEFAULT NULL COMMENT '收盘价',
    prev_close DOUBLE DEFAULT NULL COMMENT '前收盘价',
    adjust_pre_close_price DOUBLE DEFAULT NULL COMMENT '复权昨收盘价',
    adjust_open_price DOUBLE DEFAULT NULL COMMENT '复权开盘价',
    adjust_high_price DOUBLE DEFAULT NULL COMMENT '复权最高价',
    adjust_low_price DOUBLE DEFAULT NULL COMMENT '复权最低价',
    adjust_close_price DOUBLE DEFAULT NULL COMMENT '复权收盘价',
    adjust_factor DOUBLE DEFAULT NULL COMMENT '复权因子',
    volume BIGINT DEFAULT NULL COMMENT '成交量，单位：股',
    turnover DOUBLE DEFAULT NULL COMMENT '成交额，单位：万元',
    turnover_rate DOUBLE DEFAULT NULL COMMENT '真实换手率，单位：%',
    change_rate DOUBLE DEFAULT NULL COMMENT '涨跌幅，单位：%',
    amplitude DOUBLE DEFAULT NULL COMMENT '振幅，单位：%',
    float_market_cap DOUBLE DEFAULT NULL COMMENT '流通市值，单位：万元',
    float_shares BIGINT DEFAULT NULL COMMENT '流通股本，单位：股',
    kline_state INT DEFAULT NULL COMMENT 'K线状态',
    consecutive_limit_up_days INT DEFAULT NULL COMMENT '连续涨停天数或断板高度',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_stock_trade_date (stock_code, trade_date),
    KEY idx_trade_date (trade_date),
    KEY idx_stock_code (stock_code),
    KEY idx_kline_state (kline_state),
    KEY idx_consecutive_limit_up_days (consecutive_limit_up_days)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票日K线数据表';
