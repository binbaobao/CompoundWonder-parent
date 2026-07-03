package com.compoundwonder.hxdata.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置。
 * 作用：扫描当前服务下的 Mapper 接口，让 Mapper 可以被 Spring 容器注入使用。
 */
@Configuration
@MapperScan("com.compoundwonder.hxdata.mapper")
public class MybatisPlusConfig {
}
