package com.owl.core.skills;

public interface AgentSkillRepo {
    /// 获取
    public String getAgentSkills(String userId, String type);

    /// 设置
    ///
    /// @return
    public String setAgentSkills(String userId, String type, String content);
}
