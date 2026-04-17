package com.owl.core.skills;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智能体提示词定义类
 * 包含智能体的各种提示词模板和配置
 */
@Data
@NoArgsConstructor
public class SkillConfig {

    /// 工作流程 - 描述智能体的执行步骤和任务处理逻辑
    private String agents;
    
    /// 行为准则 - 定义智能体的核心价值观和交互原则
    private String soul;
    
    /// 智能体档案 - 描述智能体的身份、角色定位和专业领域
    private String identity;
    
    /// 用户档案 - 存储和管理用户的个性化信息和偏好
    private String user;
    
    /// 工具&本地笔记 - 描述可用的工具集和本地知识库
    private String tools;

    /// 初始引导脚本 - 智能体启动时的初始化指令
    private String bootstrap;
    
    /// 心跳检查模板 - 用于定期检测智能体状态
    private String heartbeat;

}
