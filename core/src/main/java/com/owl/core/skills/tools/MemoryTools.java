package com.owl.core.skills.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.owl.core.memory.MemoryMetadata;
import com.owl.core.memory.MemoryRepo;
import com.owl.core.memory.MemoryUserConfig;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.Optional;

/**
 * 记忆管理工具类
 * <p>
 * 提供基于 LLM 的记忆管理功能，包括记忆的保存、读取、删除和查询操作。
 * 实现了 ToolComponent 接口，可被框架自动扫描并注册为可用工具。
 * </p>
 *
 * <h3>功能特性：</h3>
 * <ul>
 *   <li>✅ 支持用户级别的记忆隔离</li>
 *   <li>✅ 提供完整的 CRUD 操作</li>
 *   <li>✅ 支持时间范围查询</li>
 *   <li>✅ 自动获取上下文中的用户 ID</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // LLM 可以调用以下工具：
 * // 1. saveMemory - 保存记忆
 * // 2. getMemory - 获取记忆
 * // 3. deleteMemory - 删除记忆
 * // 4. getMemoryPaths - 获取所有记忆路径
 * // 5. getMemoryPaths(startTime, endTime) - 获取指定时间范围内的记忆路径
 * }</pre>
 *
 * @author Owl Team
 * @version 1.0
 * @see ToolComponent
 * @see MemoryRepo
 * @since 2026-04-23
 */
public class MemoryTools implements ToolComponent {

    /**
     * 记忆仓库实例
     */
    private final MemoryRepo memoryRepo;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    /**
     * 构造记忆管理工具
     *
     * @param memoryRepo 记忆仓库实现
     */
    public MemoryTools(MemoryRepo memoryRepo) {
        this.memoryRepo = memoryRepo;
    }

    /**
     * 保存记忆
     * <p>
     * 将指定的记忆内容保存到存储系统中，自动从上下文中获取用户 ID。
     * 如果上下文中没有用户 ID，则使用主用户 ID。
     * </p>
     *
     * @param context 工具上下文对象，包含用户信息
     * @param path    记忆的存储路径，用于定位和检索记忆
     * @param content 记忆的具体内容
     */
    @Tool(name = "memory_save", description = "保存记忆")
    public void saveMemory(ToolContext context,
                           @ToolParam(description = "记忆路径") String path,
                           @ToolParam(description = "记忆内容") String content) {
        Optional<String> userId = getUserId(context);
        memoryRepo.saveMemory(userId.orElse(MAIN_USER_ID), path, content);
    }

    /**
     * 获取记忆
     * <p>
     * 根据路径读取对应的记忆内容，自动从上下文中获取用户 ID。
     * 如果上下文中没有用户 ID，则使用主用户 ID。
     * </p>
     *
     * @param context 工具上下文对象，包含用户信息
     * @param path    记忆的存储路径
     * @return 记忆内容，如果不存在则返回 null
     */
    @Tool(name = "memory_get", description = "获取记忆")
    public String getMemory(ToolContext context,
                            @ToolParam(description = "记忆路径") String path) {
        Optional<String> userId = getUserId(context);
        return memoryRepo.getMemory(userId.orElse(MAIN_USER_ID), path);
    }

    /**
     * 删除记忆
     * <p>
     * 从存储系统中删除指定的记忆，自动从上下文中获取用户 ID。
     * 如果上下文中没有用户 ID，则使用主用户 ID。
     * </p>
     *
     * @param context 工具上下文对象，包含用户信息
     * @param path    记忆的存储路径
     * @return 成功删除的记忆数量
     */
    @Tool(name = "memory_delete", description = "删除记忆: 返回的是删除成功的数量")
    public int deleteMemory(ToolContext context,
                            @ToolParam(description = "记忆路径") String path) {
        Optional<String> userId = getUserId(context);
       return memoryRepo.deleteMemory(userId.orElse(MAIN_USER_ID), path);
    }

    /**
     * 获取所有记忆元数据
     * <p>
     * 获取指定用户的所有记忆路径及其元数据信息，自动从上下文中获取用户 ID。
     * 如果上下文中没有用户 ID，则使用主用户 ID。
     * </p>
     *
     * @param context 工具上下文对象，包含用户信息
     * @return 记忆元数据列表，如果没有记忆则返回空列表
     */
    @Tool(name = "memory_list_all", description = "获取所有记忆路径")
    public List<MemoryMetadata> getMemoryPaths(ToolContext context) {
        Optional<String> userId = getUserId(context);
        return memoryRepo.getMemoryPaths(userId.orElse(MAIN_USER_ID));
    }

    /**
     * 获取指定时间范围内的记忆元数据
     * <p>
     * 根据时间戳范围筛选并获取指定用户的记忆路径及其元数据信息，
     * 自动从上下文中获取用户 ID。如果上下文中没有用户 ID，则使用主用户 ID。
     * </p>
     *
     * @param context   工具上下文对象，包含用户信息
     * @param startTime 起始时间戳（毫秒）
     * @param endTime   结束时间戳（毫秒）
     * @return 符合时间范围的记忆元数据列表，如果没有符合条件的记忆则返回空列表
     */
    @Tool(name = "memory_list_by_time", description = "获取指定时间范围内的记忆路径")
    public List<MemoryMetadata> getMemoryPathsByTimeRange(ToolContext context,
                                               @ToolParam(description = "起始时间戳") long startTime,
                                               @ToolParam(description = "结束时间戳") long endTime) {
        Optional<String> userId = getUserId(context);
        return memoryRepo.getMemoryPaths(userId.orElse(MAIN_USER_ID), startTime, endTime);
    }

    /**
     * 保存用户配置
     * <p>
     * 更新指定用户的记忆管理配置，自动从上下文中获取用户 ID。
     * 如果上下文中没有用户 ID，则使用主用户 ID。
     * </p>
     *
     * @param context 工具上下文对象，包含用户信息
     * @param config  配置
     */
    @Tool(name = "memory_config_save", description = "保存用户记忆配置")
    public void saveUserConfig(ToolContext context,
                               @ToolParam(description = "配置") MemoryUserConfig config) {
        Optional<String> userId = getUserId(context);
        memoryRepo.saveUserConfig(userId.orElse(MAIN_USER_ID), config);
    }
}
