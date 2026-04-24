package com.owl.core.skills;

/**
 * Agent 技能仓库接口
 * <p>
 * 定义了 Agent 技能的持久化操作规范，包括技能的获取和设置功能。
 * 支持基于用户 ID 和技能类型的技能管理。
 * </p>
 *
 * @author Owl Team
 */
public interface AgentSkillRepo {
    /**
     * 获取 Agent 技能
     * <p>
     * 根据用户 ID 和技能类型读取对应的技能内容。
     * </p>
     *
     * @param userId 用户唯一标识
     * @param type   技能类型，用于区分不同种类的技能
     * @return 技能内容，如果不存在则返回 null
     */
    String getAgentSkills(String userId, String type);

    /**
     * 设置 Agent 技能
     * <p>
     * 将指定的技能内容保存到存储系统中。
     * </p>
     *
     * @param userId  用户唯一标识
     * @param type    技能类型，用于区分不同种类的技能
     * @param content 技能的具体内容
     * @return 操作结果信息
     */
    String setAgentSkills(String userId, String type, String content);
}
