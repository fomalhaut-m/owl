package com.owl.core.skills.definition;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 技能定义实体 - 可持久化的技能定义
 * 包含技能的提示词和所需工具列表
 */
@Entity
@Table(name = "skill_definitions")
public class SkillDefinition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "prompt_template", length = 5000, columnDefinition = "TEXT")
    private String promptTemplate;
    
    @ElementCollection
    @CollectionTable(name = "skill_tools", joinColumns = @JoinColumn(name = "skill_definition_id"))
    @Column(name = "tool_name")
    private List<String> toolNames;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.id = UUID.randomUUID().toString();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public SkillDefinition() {}
    
    public SkillDefinition(String name, String description, String promptTemplate, List<String> toolNames) {
        this.name = name;
        this.description = description;
        this.promptTemplate = promptTemplate;
        this.toolNames = toolNames;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getPromptTemplate() {
        return promptTemplate;
    }
    
    public void setPromptTemplate(String promptTemplate) {
        this.promptTemplate = promptTemplate;
    }
    
    public List<String> getToolNames() {
        return toolNames;
    }
    
    public void setToolNames(List<String> toolNames) {
        this.toolNames = toolNames;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}