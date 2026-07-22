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
                  source_run_id BIGINT NULL,
                  strategy_version VARCHAR(64) NULL,
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
                  source_sample_id BIGINT NULL,
                  symbol VARCHAR(6) NOT NULL,
                  symbol_name VARCHAR(32) NULL,
                  trade_mode INT NOT NULL,
                  limit_up_score INT NULL,
                  recommend_date DATE NOT NULL,
                  trade_date DATE NOT NULL,
                  selection_board INT NOT NULL DEFAULT 1,
                  status INT NOT NULL,
                  position_type INT NOT NULL DEFAULT 0,
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
        ensureColumn("single_mode_backtest_run", "source_run_id",
                "ALTER TABLE single_mode_backtest_run ADD COLUMN source_run_id BIGINT NULL AFTER id");
        ensureColumn("single_mode_backtest_run", "strategy_version",
                "ALTER TABLE single_mode_backtest_run ADD COLUMN strategy_version VARCHAR(64) NULL AFTER source_run_id");
        ensureColumn("single_mode_backtest_sample", "source_sample_id",
                "ALTER TABLE single_mode_backtest_sample ADD COLUMN source_sample_id BIGINT NULL AFTER run_id");
        ensureColumn("single_mode_backtest_sample", "position_type",
                "ALTER TABLE single_mode_backtest_sample ADD COLUMN position_type INT NOT NULL DEFAULT 0 AFTER status");
        ensureColumn("single_mode_backtest_sample", "selection_trigger",
                "ALTER TABLE single_mode_backtest_sample ADD COLUMN selection_trigger VARCHAR(40) NULL AFTER selection_board");
        ensureColumn("single_mode_backtest_sample", "selection_strength",
                "ALTER TABLE single_mode_backtest_sample ADD COLUMN selection_strength VARCHAR(20) NULL AFTER selection_trigger");
        ensureColumn("single_mode_backtest_sample", "strategy_version",
                "ALTER TABLE single_mode_backtest_sample ADD COLUMN strategy_version VARCHAR(64) NULL AFTER selection_strength");
        ensureColumn("single_mode_backtest_sample", "selection_run_id",
                "ALTER TABLE single_mode_backtest_sample ADD COLUMN selection_run_id BIGINT NULL AFTER strategy_version");
        ensureColumn("single_mode_backtest_sample", "relay_candidate_record_id",
                "ALTER TABLE single_mode_backtest_sample ADD COLUMN relay_candidate_record_id BIGINT NULL AFTER selection_run_id");
        // 新列首次加入旧表时默认值为 0；按既有买入记录回填实际成交，保持历史汇总口径不变。
        jdbcTemplate.update("""
                UPDATE single_mode_backtest_sample
                SET position_type = 1
                WHERE position_type = 0
                  AND buy_date IS NOT NULL
                  AND (buy_rule_code IS NULL OR buy_rule_code <> 0)
                """);
        // 回测线程不跨进程恢复；服务重启后仍标记为运行中的任务均为上次被中断的任务。
        jdbcTemplate.update("""
                UPDATE single_mode_backtest_run
                SET status = 3,
                    error_message = '服务重启导致任务中断，请重新发起回放',
                    finished_time = NOW(),
                    updated_time = NOW()
                WHERE status = 1
                """);
        initialized = true;
    }

    private void ensureColumn(String tableName, String columnName, String alterSql) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND column_name = ?
                """, Integer.class, tableName, columnName);
        if (count != null && count == 0) {
            jdbcTemplate.execute(alterSql);
        }
    }
}
