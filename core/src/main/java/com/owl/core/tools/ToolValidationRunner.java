package com.owl.core.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * Tool 名称验证启动器
 * <p>
 * 在 Spring Boot 启动时自动验证所有 @Tool 名称的唯一性。
 * </p>
 *
 * @author Owl Team
 * @since 2026-04-23
 */
public class ToolValidationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ToolValidationRunner.class);

    @Override
    public void run(ApplicationArguments args) {
        log.info("========== 开始验证 Tool 名称唯一性 ==========");
        try {
            var registry = ToolNameValidator.validateAndScan();
            log.info("========== Tool 名称验证通过，共 {} 个工具 ==========", registry.size());
        } catch (IllegalStateException e) {
            log.error("========== Tool 名称验证失败 ==========");
            throw e;
        }
    }
}
