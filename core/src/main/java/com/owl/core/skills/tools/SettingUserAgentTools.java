package com.owl.core.skills.tools;

import com.owl.core.skills.AgentSkillRepo;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Optional;

public class SettingUserAgentTools implements ToolComponent {

    private final AgentSkillRepo userAgentRepo;

    public SettingUserAgentTools(AgentSkillRepo userAgentRepo) {
        this.userAgentRepo = userAgentRepo;
    }

    ///  从上下文中获取 userId, 设置USER.md
    @Tool(name = "setUserConfig", description = "设置指定类型的用户配置")
    public void settingUserConfig(ToolContext context,
                                  @ToolParam(description = "配置类型:USER,IDENTITY,SOUL,TOOLS,HEARTBEAT") String type,
                                  @ToolParam(description = "配置内容") String content) {
        Optional<String> userId = getUserId(context);
        userAgentRepo.setAgentSkills(userId.orElse(null), type, content);
    }

    ///  从上下文中获取 userId, 获取指定类型的用户配置
    @Tool(name = "getUserConfig", description = "获取指定类型的用户配置")
    public String getUserConfig(ToolContext context,
                                @ToolParam(description = "配置类型:USER,IDENTITY,SOUL,TOOLS,HEARTBEAT") String type) {
        Optional<String> userId = getUserId(context);
        return userAgentRepo.getAgentSkills(userId.orElse(null), type);
    }

    ///  从上下文中获取 userId, 获取USER.md
    @Tool(name = "getUserId", description = "获取当前用户的ID")
    private Optional<String> getUserId(ToolContext context) {
        return Optional.ofNullable(context.getContext().get("userId")).map(Object::toString);
    }

}
