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
