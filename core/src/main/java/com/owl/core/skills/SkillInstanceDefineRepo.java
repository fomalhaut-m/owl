package com.owl.core.skills;

import java.util.List;

/**
 * Agent 技能仓库接口（JPA 命名风格）
 * <p>
 * 定义了 Agent 技能的持久化操作规范，包括技能的获取和设置功能。
 * 支持基于用户 ID 和技能类型的技能管理。
 * </p>
 *
 * <h3>JPA 命名风格：</h3>
 * <ul>
 *   <li>findAgentSkillsByUserIdAndType() - 查询技能</li>
 *   <li>saveAgentSkills() - 保存技能</li>
 * </ul>
 *
 * @author Owl Team
 */
public interface SkillInstanceDefineRepo {

    /**
     * 查询 Agent 技能
     * <p>JPA 风格：findByUserIdAndType</p>
     */
    String findAgentSkillsByUserIdAndType(String userId, String type);

    /**
     * 保存 Agent 技能
     * <p>JPA 风格：save</p>
     */
    void saveAgentSkills(String userId, String type, String content);
}
