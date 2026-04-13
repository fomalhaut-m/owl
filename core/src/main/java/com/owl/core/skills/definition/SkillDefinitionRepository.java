package com.owl.core.skills.definition;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 技能定义仓库接口 - 使用 Spring Data JPA
 */
@Repository
public interface SkillDefinitionRepository extends JpaRepository<SkillDefinition, String> {
    
    /**
     * 根据技能名称查找技能定义
     */
    Optional<SkillDefinition> findByName(String name);
    
    /**
     * 检查技能名称是否存在
     */
    boolean existsByName(String name);
}