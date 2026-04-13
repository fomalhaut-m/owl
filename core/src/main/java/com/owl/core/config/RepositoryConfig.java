package com.owl.core.config;

import com.owl.core.skills.definition.SkillDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 仓库配置类 - Spring Boot 方式
 */
@Component
public class RepositoryConfig {
    
    @Autowired
    private SkillDefinitionRepository skillDefinitionRepository;
    
    /**
     * 获取技能定义仓库实例
     */
    public SkillDefinitionRepository getSkillDefinitionRepository() {
        return skillDefinitionRepository;
    }
}