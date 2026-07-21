create table stock_convertible_bond_history
(
    id                  bigint auto_increment comment '主键ID'
        primary key,
    stock_code          varchar(6)                         null comment '正股代码',
    bond_code           varchar(6)                         not null comment '转债代码',
    market              varchar(10)                        not null comment '转债市场',
    bond_name           varchar(50)                        null comment '转债名称',
    start_date          date                               null comment '上市日期',
    end_date            date                               null comment '退市或到期日期',
    maturity_date       date                               null comment '到期日期',
    failure             int                                null comment '是否失效',
    outstanding_balance double                             null comment '存续余额',
    created_time        datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_time        datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_bond_market
        unique (bond_code, market)
)
    comment '股票可转债历史表';

create index idx_bond_code
    on stock_convertible_bond_history (bond_code);

create index idx_stock_code
    on stock_convertible_bond_history (stock_code);

create index idx_stock_date_range
    on stock_convertible_bond_history (stock_code, start_date, end_date);

create table stock_current_status
(
    id               bigint auto_increment comment '主键ID'
        primary key,
    stock_code       varchar(6)                         not null comment '股票代码',
    margin_trading   tinyint  default 1                 not null comment '是否融资融券：0=否，1=是',
    convertible_bond tinyint  default 0                 not null comment '是否有可转债：0=否，1=是',
    region_name      varchar(50)                        null comment '地域名称',
    updated_time     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_stock_code
        unique (stock_code)
)
    comment '股票当前状态表';

create index idx_convertible_bond
    on stock_current_status (convertible_bond);

create index idx_margin_trading
    on stock_current_status (margin_trading);

create index idx_region_name
    on stock_current_status (region_name);

create table stock_daily
(
    id                        bigint auto_increment comment '主键ID'
        primary key,
    stock_code                varchar(6)                         not null comment '股票代码',
    stock_name                varchar(20)                        null comment '当日股票名称',
    is_st                     tinyint  default 0                 not null comment '当日是否ST：0=否，1=是',
    trade_date                date                               not null comment '交易日期',
    open_price                double                             null comment '开盘价',
    high_price                double                             null comment '最高价',
    low_price                 double                             null comment '最低价',
    close_price               double                             null comment '收盘价',
    prev_close                double                             null comment '前收盘价',
    adjust_pre_close_price    double                             null comment '复权昨收盘价',
    adjust_open_price         double                             null comment '复权开盘价',
    adjust_high_price         double                             null comment '复权最高价',
    adjust_low_price          double                             null comment '复权最低价',
    adjust_close_price        double                             null comment '复权收盘价',
    adjust_factor             double                             null comment '复权因子',
    volume                    bigint                             null comment '成交量，单位：股',
    turnover                  double                             null comment '成交额，单位：万元',
    turnover_rate             double                             null comment '真实换手率，单位：%',
    change_rate               double                             null comment '涨跌幅，单位：%',
    amplitude                 double                             null comment '振幅，单位：%',
    float_market_cap          double                             null comment '流通市值，单位：万元',
    float_shares              bigint                             null comment '流通股本，单位：股',
    kline_state               int                                null comment 'K线状态',
    consecutive_limit_up_days int                                null comment '连续涨停天数或断板高度',
    created_at                datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint uk_stock_trade_date
        unique (stock_code, trade_date)
)
    comment '股票日K线数据表';

create index idx_consecutive_limit_up_days
    on stock_daily (consecutive_limit_up_days);

create index idx_kline_state
    on stock_daily (kline_state);

create index idx_stock_code
    on stock_daily (stock_code);

create index idx_trade_date
    on stock_daily (trade_date);

create table stock_daily_update_task
(
    id                      bigint auto_increment comment '主键ID'
        primary key,
    task_date               date                                 not null comment '任务日期',
    trade_day               tinyint(1) default 0                 not null comment '是否交易日',
    previous_name_synced    tinyint(1) default 0                 not null comment '曾用名同步是否完成',
    new_listing_synced      tinyint(1) default 0                 not null comment '新上市股票发现是否完成',
    free_float_synced       tinyint(1) default 0                 not null comment '自由流通股本同步是否完成',
    convertible_bond_synced tinyint(1) default 0                 not null comment '可转债当前状态同步是否完成',
    region_synced           tinyint(1) default 0                 not null comment '地域信息同步是否完成',
    margin_trading_synced   tinyint(1) default 0                 not null comment '融资融券标识维护是否完成',
    daily_kline_synced      tinyint(1) default 0                 not null comment '日K同步是否完成',
    pre_open_finished       tinyint(1) default 0                 not null comment '盘前任务是否完成',
    post_close_finished     tinyint(1) default 0                 not null comment '盘后任务是否完成',
    created_time            datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_time            datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_task_date
        unique (task_date)
)
    comment '股票每日更新任务记录表';

create table stock_free_float_share_history
(
    id                bigint auto_increment comment '主键ID'
        primary key,
    stock_code        varchar(6)                         not null comment '股票代码',
    free_shares       bigint                             not null comment '自由流通股本，单位：股；接口返回万股后放大10000倍',
    start_date        date                               not null comment '区间开始日期，对应接口ChangeDateEX',
    end_date          date                               null comment '区间结束日期；NULL表示当前仍然有效',
    announcement_date date                               null comment '公告日期，对应接口AnnouncementDate',
    created_time      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_time      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_stock_start_date
        unique (stock_code, start_date)
)
    comment '股票自由流通股本历史区间表';

create index idx_current
    on stock_free_float_share_history (stock_code, end_date);

create index idx_stock_code
    on stock_free_float_share_history (stock_code);

create index idx_stock_date_range
    on stock_free_float_share_history (stock_code, start_date, end_date);

create table stock_previous_name_history
(
    id         bigint auto_increment comment '主键ID'
        primary key,
    stock_code varchar(6)  null,
    stock_name varchar(20) not null,
    start_date date        not null,
    end_date   date        null,
    constraint uk_stock_start_date
        unique (stock_code, start_date)
)
    comment '股票曾用名历史表' charset = utf8mb3;

create table stock_sync_task
(
    id                 bigint auto_increment comment '主键ID'
        primary key,
    stock_code         varchar(6)                         not null comment '股票代码',
    free_float_synced  tinyint  default 0                 not null comment '是否已完成自由流通股同步：0=未完成，1=已完成',
    daily_kline_synced tinyint  default 0                 not null comment '是否已完成日K同步：0=未完成，1=已完成',
    created_time       datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_time       datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_stock_code
        unique (stock_code)
)
    comment '股票历史数据同步任务进度表';

create index idx_daily_kline_synced
    on stock_sync_task (daily_kline_synced);

create index idx_free_float_synced
    on stock_sync_task (free_float_synced);

create table stock_trade_calendar
(
    id           bigint auto_increment comment '主键ID'
        primary key,
    trade_date   date                               not null comment '交易日期',
    updated_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_trade_date
        unique (trade_date)
)
    comment '股票交易日历表';

