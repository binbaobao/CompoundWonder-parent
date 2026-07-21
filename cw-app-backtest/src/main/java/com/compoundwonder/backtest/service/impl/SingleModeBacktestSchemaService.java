package com.compoundwonder.backtest.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** 确保独立的单模式回测表存在，不改动原有混合回测表。 */
@Service
@DS("trade")
public class SingleModeBacktestSchemaService {
    private final JdbcTemplate jdbcTemplate;
    private volatile boolean initialized;

    public SingleModeBacktestSchemaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public synchronized void ensureSchema() {
        if (initialized) return;
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS single_mode_backtest_run (
                  id BIGINT NOT NULL AUTO_INCREMENT,
                  start_date DATE NOT NULL,
                  end_date DATE NOT NULL,
                  trade_mode INT NOT NULL,
                  status INT NOT NULL,
                  last_completed_date DATE NULL,
                  total_samples INT NOT NULL DEFAULT 0,
                  processed_samples INT NOT NULL DEFAULT 0,
                  bought_samples INT NOT NULL DEFAULT 0,
                  closed_samples INT NOT NULL DEFAULT 0,
                  error_message VARCHAR(1000) NULL,
                  started_time DATETIME NULL,
                  finished_time DATETIME NULL,
                  created_time DATETIME NOT NULL,
                  updated_time DATETIME NULL,
                  PRIMARY KEY (id),
                  KEY idx_single_mode_run_mode_id (trade_mode, id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS single_mode_backtest_sample (
                  id BIGINT NOT NULL AUTO_INCREMENT,
                  run_id BIGINT NOT NULL,
                  symbol VARCHAR(6) NOT NULL,
                  symbol_name VARCHAR(32) NULL,
                  trade_mode INT NOT NULL,
                  limit_up_score INT NULL,
                  recommend_date DATE NOT NULL,
                  trade_date DATE NOT NULL,
                  selection_board INT NOT NULL DEFAULT 1,
                  status INT NOT NULL,
                  no_buy_reason VARCHAR(1000) NULL,
                  buy_date DATE NULL,
                  buy_time INT NULL,
                  buy_price INT NULL,
                  buy_rule_code INT NULL,
                  buy_remark VARCHAR(2000) NULL,
                  buy_day_kline_state INT NULL,
                  sell_date DATE NULL,
                  sell_time INT NULL,
                  sell_price INT NULL,
                  sell_rule_code INT NULL,
                  sell_remark VARCHAR(2000) NULL,
                  sell_board INT NULL,
                  holding_trade_days INT NOT NULL DEFAULT 0,
                  return_rate DECIMAL(18,6) NULL,
                  max_floating_return_rate DECIMAL(18,6) NULL,
                  max_drawdown_rate DECIMAL(18,6) NULL,
                  max_sealed_boards INT NOT NULL DEFAULT 1,
                  max_touched_boards INT NOT NULL DEFAULT 1,
                  potential_max_return_rate DECIMAL(18,6) NULL,
                  post_sell_max_return_rate DECIMAL(18,6) NULL,
                  sample_end_date DATE NULL,
                  created_time DATETIME NOT NULL,
                  updated_time DATETIME NULL,
                  PRIMARY KEY (id),
                  UNIQUE KEY uk_single_mode_sample (run_id, recommend_date, symbol),
                  KEY idx_single_mode_sample_run_status (run_id, status),
                  KEY idx_single_mode_sample_run_date (run_id, recommend_date, id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        initialized = true;
    }
}
