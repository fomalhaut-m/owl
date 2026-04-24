package com.owl.core.skills.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.owl.core.memory.MemoryMetadata;
import com.owl.core.memory.MemoryRepo;
import com.owl.core.memory.MemoryUserConfig;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 记忆管理工具类
 * <p>
 * 提供基于 LLM 的记忆管理功能，包括记忆的保存、读取、删除和查询操作。
 * 实现了 ToolComponent 接口，可被框架自动扫描并注册为可用工具。
 * </p>
 *
 * <h3>JPA 命名风格：</h3>
 * <ul>
 *   <li>save() - 保存记忆</li>
 *   <li>findByUserIdAndPathAndName() - 获取记忆</li>
 *   <li>delete() - 删除记忆</li>
 *   <li>findAllByUserId() - 获取所有记忆</li>
 *   <li>findAllByUserIdAndTimeBetween() - 按时间范围获取记忆</li>
 *   <li>saveConfig() - 保存配置</li>
 * </ul>
 *
 * @author Owl Team
 * @version 1.0
 * @see ToolComponent
 * @see MemoryRepo
 * @since 2026-04-23
 */
public class MemoryTools implements ToolComponent {

    private final MemoryRepo memoryRepo;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public MemoryTools(MemoryRepo memoryRepo) {
        this.memoryRepo = memoryRepo;
    }

    /**
     * 保存记忆
     * <p>JPA 风格：save</p>
     */
    @Tool(
        name = "owl_memory_save",
        description = """
            保存用户记忆到文件系统
            - 支持创建新记忆或更新已有记忆
            - 路径格式：目录路径/文件名称，如 working/P0/note.md
            - 用户隔离：自动从上下文获取用户ID
            """
    )
    public void saveMemory(
            ToolContext context,
            @ToolParam(description = "记忆存放的目录路径，支持多级目录，如 working/P0") String path,
            @ToolParam(description = "记忆文件名称，必须以.md结尾，如 note.md、meeting.md") String name,
            @ToolParam(description = "记忆的具体内容，支持Markdown格式") String content) {
        Optional<String> userId = getUserId(context);
        List<String> pathList = parsePath(path);
        memoryRepo.save(userId.orElse(MAIN_USER_ID), pathList, name, content);
    }

    /**
     * 获取记忆
     * <p>JPA 风格：findByUserIdAndPathAndName</p>
     */
    @Tool(
        name = "owl_memory_get",
        description = """
            根据路径获取用户的单条记忆内容
            - 返回记忆的完整内容
            - 如果记忆不存在返回null
            - 用户隔离：自动从上下文获取用户ID
            """
    )
    public String getMemory(
            ToolContext context,
            @ToolParam(description = "记忆存放的目录路径，如 working/P0") String path,
            @ToolParam(description = "记忆文件名称，如 note.md") String name) {
        Optional<String> userId = getUserId(context);
        List<String> pathList = parsePath(path);
        return memoryRepo.findByUserIdAndPathAndName(userId.orElse(MAIN_USER_ID), pathList, name);
    }

    /**
     * 删除记忆
     * <p>JPA 风格：delete</p>
     */
    @Tool(
        name = "owl_memory_delete",
        description = """
            删除用户的指定记忆
            - 删除成功后记忆将无法恢复
            - 如果记忆不存在不会报错
            - 用户隔离：自动从上下文获取用户ID
            """
    )
    public void deleteMemory(
            ToolContext context,
            @ToolParam(description = "记忆存放的目录路径，如 working/P0") String path,
            @ToolParam(description = "记忆文件名称，如 note.md") String name) {
        Optional<String> userId = getUserId(context);
        List<String> pathList = parsePath(path);
        memoryRepo.delete(userId.orElse(MAIN_USER_ID), pathList, name);
    }

    /**
     * 获取所有记忆
     * <p>JPA 风格：findAllByUserId</p>
     */
    @Tool(
        name = "owl_memory_list_all",
        description = """
            获取用户的所有记忆元数据列表
            - 返回包含路径、名称、大小、创建/更新时间等信息
            - 适合用于展示记忆列表或让用户选择要操作的记忆
            - 用户隔离：自动从上下文获取用户ID
            """
    )
    public List<MemoryMetadata> getMemoryPaths(ToolContext context) {
        Optional<String> userId = getUserId(context);
        return memoryRepo.findAllByUserId(userId.orElse(MAIN_USER_ID));
    }

    /**
     * 获取指定时间范围内的记忆
     * <p>JPA 风格：findAllByUserIdAndTimeBetween</p>
     */
    @Tool(
        name = "owl_memory_list_by_time",
        description = """
            获取用户在指定时间范围内的所有记忆
            - startTime: 起始时间，13位时间戳（毫秒）
            - endTime: 结束时间，13位时间戳（毫秒）
            - 可用于获取最近N小时/天的记忆
            - 用户隔离：自动从上下文获取用户ID
            """
    )
    public List<MemoryMetadata> getMemoryPathsByTimeRange(
            ToolContext context,
            @ToolParam(description = "起始时间，13位时间戳（毫秒），如 1704067200000") long startTime,
            @ToolParam(description = "结束时间，13位时间戳（毫秒），如 1704153600000") long endTime) {
        Optional<String> userId = getUserId(context);
        return memoryRepo.findAllByUserIdAndTimeBetween(userId.orElse(MAIN_USER_ID), startTime, endTime);
    }

    /**
     * 保存用户配置
     * <p>JPA 风格：saveConfig</p>
     */
    @Tool(
        name = "owl_memory_config_save",
        description = """
            保存用户的记忆配置
            - contextMaxTokenRate: 上下文最大Token占比，范围0.1-1.0
            - compressRatio: 压缩比，表示保留最近N条记忆
            - 用户隔离：自动从上下文获取用户ID
            """
    )
    public void saveUserConfig(
            ToolContext context,
            @ToolParam(description = "用户配置对象，包含contextMaxTokenRate和compressRatio") MemoryUserConfig config) {
        Optional<String> userId = getUserId(context);
        memoryRepo.saveConfig(userId.orElse(MAIN_USER_ID), config);
    }

    /**
     * 解析路径字符串为路径列表
     * <p>将 "working/P0" 转换为 ["working", "P0"]</p>
     */
    private List<String> parsePath(String path) {
        if (path == null || path.isBlank()) {
            return List.of();
        }
        return Arrays.stream(path.split("/"))
                .filter(part -> !part.isBlank())
                .toList();
    }
}
