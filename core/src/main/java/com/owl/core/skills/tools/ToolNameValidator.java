package com.owl.core.skills.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Tool 名称验证器
 * <p>
 * 在项目启动时扫描所有 ToolComponent 子类，验证 @Tool 注解的 name 是否全局唯一。
 * 如果发现重复名称，启动失败并抛出异常。
 * </p>
 *
 * <h3>命名规范：</h3>
 * <ul>
 *   <li>格式：{模块}_{资源}_{动作}，使用 snake_case</li>
 *   <li>必须以 "owl_" 前缀开头，确保全局唯一性</li>
 *   <li>示例：owl_memory_save, owl_agent_config_get, owl_time_current_system</li>
 * </ul>
 *
 * <h3>已注册的 Tool 名称：</h3>
 * <table>
 *   <tr><th>Tool Name</th><th>类</th><th>方法</th></tr>
 *   <tr><td>owl_memory_save</td><td>MemoryTools</td><td>saveMemory</td></tr>
 *   <tr><td>owl_memory_get</td><td>MemoryTools</td><td>getMemory</td></tr>
 *   <tr><td>owl_memory_delete</td><td>MemoryTools</td><td>deleteMemory</td></tr>
 *   <tr><td>owl_memory_list_all</td><td>MemoryTools</td><td>getMemoryPaths</td></tr>
 *   <tr><td>owl_memory_list_by_time</td><td>MemoryTools</td><td>getMemoryPathsByTimeRange</td></tr>
 *   <tr><td>owl_memory_config_save</td><td>MemoryTools</td><td>saveUserConfig</td></tr>
 *   <tr><td>owl_agent_config_set</td><td>AgentTools</td><td>settingUserConfig</td></tr>
 *   <tr><td>owl_agent_config_get</td><td>AgentTools</td><td>getUserConfig</td></tr>
 *   <tr><td>owl_chat_history_get</td><td>ChatTools</td><td>getChatHistory</td></tr>
 *   <tr><td>owl_time_current_system</td><td>TimeTools</td><td>getCurrentSystemTime</td></tr>
 *   <tr><td>owl_time_current_by_zone</td><td>TimeTools</td><td>getCurrentTimeByZone</td></tr>
 * </table>
 *
 * @author Owl Team
 * @since 2026-04-23
 */
public class ToolNameValidator {

    private static final Logger log = LoggerFactory.getLogger(ToolNameValidator.class);

    /**
     * 扫描并验证所有 ToolComponent 子类中的 @Tool 名称唯一性
     *
     * @return 扫描到的所有 Tool 名称及对应信息
     * @throws IllegalStateException 如果发现重复的 Tool 名称
     */
    public static Map<String, ToolInfo> validateAndScan() {
        Map<String, ToolInfo> toolRegistry = new LinkedHashMap<>();
        Set<String> duplicates = new HashSet<>();

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new org.springframework.core.type.filter.AssignableTypeFilter(ToolComponent.class));

        for (BeanDefinition bd : scanner.findCandidateComponents("com.owl.core.skills")) {
            String className = bd.getBeanClassName();
            if (className == null) continue;

            try {
                Class<?> clazz = Class.forName(className);
                scanClass(clazz, toolRegistry, duplicates);
            } catch (ClassNotFoundException e) {
                log.warn("无法加载类: {}", className);
            }
        }

        if (!duplicates.isEmpty()) {
            String message = String.format(
                "发现重复的 Tool 名称: %s\n请确保所有 @Tool 的 name 全局唯一",
                duplicates
            );
            log.error(message);
            throw new IllegalStateException(message);
        }

        log.info("Tool 名称验证通过，共注册 {} 个工具", toolRegistry.size());
        return toolRegistry;
    }

    private static void scanClass(Class<?> clazz, Map<String, ToolInfo> registry, Set<String> duplicates) {
        for (Method method : clazz.getDeclaredMethods()) {
            Tool tool = method.getAnnotation(Tool.class);
            if (tool != null && tool.name() != null && !tool.name().isEmpty()) {
                String toolName = tool.name();
                ToolInfo info = new ToolInfo(toolName, clazz.getSimpleName(), method.getName());

                if (registry.containsKey(toolName)) {
                    duplicates.add(toolName);
                    log.error("重复的 Tool 名称: '{}' 在 {} 的 {} 方法和 {} 的 {} 方法",
                        toolName,
                        registry.get(toolName).className(), registry.get(toolName).methodName(),
                        info.className(), info.methodName());
                } else {
                    registry.put(toolName, info);
                    log.debug("注册 Tool: {} -> {}.{}",
                        toolName, info.className(), info.methodName());
                }
            }
        }
    }

    /**
     * Tool 信息记录
     */
    public record ToolInfo(String name, String className, String methodName) {}
}
