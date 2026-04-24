package com.owl.core.memory;

import java.util.List;

/**
 * 记忆仓库接口
 * <p>
 * 定义了记忆的持久化操作规范，包括记忆的保存、读取、删除和查询功能。
 * 支持基于用户 ID 和路径的记忆管理，提供时间范围筛选能力。
 * </p>
 *
 * @author Owl Team
 */
public interface MemoryRepo {


    /**
     * 保存记忆
     * <p>
     * 将指定用户的记忆内容保存到存储系统中。
     * </p>
     *
     * @param userId  用户唯一标识
     * @param path    记忆的存储路径，用于定位和检索记忆
     * @param content 记忆的具体内容
     */
    void saveMemory(String userId, String path, String content);

    /**
     * 获取记忆
     * <p>
     * 根据用户 ID 和路径读取对应的记忆内容。
     * </p>
     *
     * @param userId 用户唯一标识
     * @param path   记忆的存储路径
     * @return 记忆内容，如果不存在则返回 null
     */
    String getMemory(String userId, String path);

    /**
     * 删除记忆
     * <p>
     * 从存储系统中删除指定用户的记忆。
     * </p>
     *
     * @param userId 用户唯一标识
     * @param path   记忆的存储路径
     */
    int deleteMemory(String userId, String path);

    /**
     * 获取所有记忆元数据
     * <p>
     * 获取指定用户的所有记忆路径及其元数据信息列表。
     * </p>
     *
     * @param userId 用户唯一标识
     * @return 记忆元数据数组，如果没有记忆则返回空数组
     */
    List<MemoryMetadata> getMemoryPaths(String userId);

    /**
     * 获取指定时间范围内的记忆元数据
     * <p>
     * 根据时间戳范围筛选并获取指定用户的记忆路径及其元数据信息列表。
     * </p>
     *
     * @param userId    用户唯一标识
     * @param startTime 起始时间戳（毫秒）
     * @param endTime   结束时间戳（毫秒）
     * @return 符合时间范围的记忆元数据数组，如果没有符合条件的记忆则返回空数组
     */
    List<MemoryMetadata> getMemoryPaths(String userId, long startTime, long endTime);

    /**
     * 获取指定path时间范围内的记忆元数据
     */
    List<MemoryMetadata> getMemoryPathsByPath(String userId, String rootPath, long startTime, long endTime);


    /**
     * 获取用户配置
     * <p>
     * 读取指定用户的记忆管理配置。
     * </p>
     *
     * @param userId 用户唯一标识
     * @return 用户配置对象，如果不存在则返回 null
     */
    MemoryUserConfig getUserConfig(String userId);

    /**
     * 保存用户配置
     * <p>
     * 更新指定用户的记忆管理配置。
     * </p>
     *
     * @param userId 用户唯一标识
     * @param config 配置 JSON 字符串
     */
    void saveUserConfig(String userId, MemoryUserConfig config);
}
