package com.owl.core.skills.tools;

import java.util.Map;

/**
 * 工具接口 - 定义所有原子工具的基础接口
 */
public interface Tool {
    
    /**
     * 获取工具名称
     */
    String getName();
    
    /**
     * 获取工具描述
     */
    String getDescription();
    
    /**
     * 执行工具
     * @param parameters 工具执行参数
     * @return 执行结果
     */
    Object execute(Map<String, Object> parameters);
}