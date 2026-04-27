package com.owl.core.tools;

import com.owl.core.skills.SkillInstanceDefineRepo;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Optional;

public class SkillInstanceDefineTools implements ToolComponent {

    private final SkillInstanceDefineRepo userAgentRepo;

    public SkillInstanceDefineTools(SkillInstanceDefineRepo userAgentRepo) {
        this.userAgentRepo = userAgentRepo;
    }

    ///  从上下文中获取 userId, 设置USER.md
    @Tool(name = "owl_agent_config_set", description = "设置指定类型的用户配置")
    public void settingUserConfig(ToolContext context,
                                  @ToolParam(description = "配置类型:IDENTITY,SOUL,TOOLS,HEARTBEAT") String type,
                                  @ToolParam(description = "配置内容") String content) {
        Optional<String> userId = getUserId(context);
        userAgentRepo.saveAgentSkills(userId.orElse(MAIN_USER_ID), type, content);
    }

    ///  从上下文中获取 userId, 获取指定类型的用户配置
    @Tool(name = "owl_agent_config_get", description = "获取指定类型的用户配置")
    public String getUserConfig(ToolContext context,
                                @ToolParam(description = "配置类型:IDENTITY,SOUL,TOOLS,HEARTBEAT") String type) {
        Optional<String> userId = getUserId(context);
        return userAgentRepo.findAgentSkillsByUserIdAndType(userId.orElse(MAIN_USER_ID), type);
    }

}
