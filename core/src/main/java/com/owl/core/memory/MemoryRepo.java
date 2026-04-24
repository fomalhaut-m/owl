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
     * 保存记忆（新增或更新）
     * <p>
     * JPA 风格：persist/save 实体。
     * 将指定用户的记忆内容保存到存储系统中。
     * </p>
     *
     * @param userId  用户唯一标识
     * @param path    记忆的存储路径（目录部分）
     * @param name    记忆文件名称
     * @param content 记忆的具体内容
     */
    void save(String userId, List<String> path, String name, String content);

    /**
     * 根据 ID 查询记忆（findById）
     * <p>
     * JPA 风格：根据唯一标识查询实体。
     * 根据用户 ID、路径和文件名读取对应的记忆内容。
     * </p>
     *
     * @param userId 用户唯一标识
     * @param path   记忆的存储路径（目录部分）
     * @param name   记忆文件名称
     * @return 记忆内容，如果不存在则返回 null
     */
    String findByUserIdAndPathAndName(String userId, List<String> path, String name);

    /**
     * 删除记忆（delete）
     * <p>
     * JPA 风格：delete 实体。
     * 从存储系统中删除指定用户的记忆。
     * </p>
     *
     * @param userId 用户唯一标识
     * @param path   记忆的存储路径（目录部分）
     * @param name   记忆文件名称
     */
    void delete(String userId, List<String> path, String name);

    /**
     * 查询所有记忆（findAll）
     * <p>
     * JPA 风格：findAll 查询。
     * 获取指定用户的所有记忆路径及其元数据信息列表。
     * </p>
     *
     * @param userId 用户唯一标识
     * @return 记忆元数据列表，如果没有记忆则返回空列表
     */
    List<MemoryMetadata> findAllByUserId(String userId);

    /**
     * 根据时间范围查询记忆
     * <p>JPA 风格：findAllByUserIdAndTimeBetween</p>
     */
    List<MemoryMetadata> findAllByUserIdAndTimeBetween(String userId, long startTime, long endTime);

    /**
     * 根据路径和时间范围查询记忆
     * <p>JPA 风格：findAllByUserIdAndSubPathAndTimeBetween</p>
     */
    List<MemoryMetadata> findAllByUserIdAndSubPathAndTimeBetween(String userId, String subPath, long startTime, long endTime);

    /**
     * 查询用户配置（findConfig）
     * <p>
     * JPA 风格：findByUserId。
     * 读取指定用户的记忆管理配置。
     * </p>
     *
     * @param userId 用户唯一标识
     * @return 用户配置对象，如果不存在则返回 null
     */
    MemoryUserConfig findConfigByUserId(String userId);

    /**
     * 保存用户配置（saveConfig）
     * <p>
     * JPA 风格：save/saveAndFlush。
     * 更新指定用户的记忆管理配置。
     * </p>
     *
     * @param userId 用户唯一标识
     * @param config 配置对象
     */
    void saveConfig(String userId, MemoryUserConfig config);
}
