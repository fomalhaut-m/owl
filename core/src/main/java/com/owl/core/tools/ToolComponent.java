package com.owl.core.tools;

import org.springframework.ai.chat.model.ToolContext;

import java.util.Optional;

/**
 * 工具组件标记接口
 * <p>
 * 标记一个类为 LLM 工具组件，表示该类包含可以被大语言模型调用的工具方法。
 * 实现此接口的类会被框架自动扫描和注册为可用工具。
 * </p>
 *
 * <h3>命名规范（必须遵循）：</h3>
 * <ul>
 *   <li>格式：<code>{模块}_{资源}_{动作}</code>，使用 snake_case</li>
 *   <li>必须以 <code>owl_</code> 前缀开头，确保全局唯一性</li>
 *   <li>示例：<code>owl_memory_save</code>, <code>owl_agent_config_get</code></li>
 * </ul>
 *
 * <h3>已注册的 Tool 名称：</h3>
 * <table>
 *   <tr><th>Tool Name</th><th>类</th><th>方法</th></tr>
 *   <tr><td>owl_memory_save</td><td>MemoryTools</td><td>saveMemory</td></tr>
 *   <tr><td>owl_memory_get</td><td>MemoryTools</td><td>getMemory</td></tr>
 *   <tr><td>owl_memory_delete</td><td>MemoryTools</td><td>deleteMemory</td></tr>
 *   <tr><td>owl_memory_list_all</td><td>MemoryTools</td><td>getMemoryPaths</td></tr>
 *   <tr><td>owl_memory_list_by_time</td><td>MemoryTools</td><td>getMemoryPathsByTimeRange</td></tr>
 *   <tr><td>owl_memory_config_save</td><td>MemoryTools</td><td>saveUserConfig</td></tr>
 *   <tr><td>owl_agent_config_set</td><td>SkillInstanceDefineTools</td><td>settingUserConfig</td></tr>
 *   <tr><td>owl_agent_config_get</td><td>SkillInstanceDefineTools</td><td>getUserConfig</td></tr>
 *   <tr><td>owl_chat_history_get</td><td>ChatTools</td><td>getChatHistory</td></tr>
 *   <tr><td>owl_time_current_system</td><td>TimeTools</td><td>getCurrentSystemTime</td></tr>
 *   <tr><td>owl_time_current_by_zone</td><td>TimeTools</td><td>getCurrentTimeByZone</td></tr>
 * </table>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 1. 实现 ToolComponent 接口
 * public class TimeTools implements ToolComponent {
 *
 *     @Tool(name = "owl_time_current", description = "获取当前时间")
 *     public String getCurrentTime() {
 *         return LocalDateTime.now().toString();
 *     }
 * }
 *
 * // 2. 框架会自动扫描并注册所有实现 ToolComponent 的类
 * // 3. 启动时 ToolNameValidator 会验证名称唯一性
 * }</pre>
 *
 * <h3>与相关概念的关系：</h3>
 * <ul>
 *   <li><b>ToolComponent</b> - 标记接口，标识工具类（类级别）</li>
 *   <li><b>@Tool</b> - Spring AI 注解，标记可调用方法（方法级别）</li>
 *   <li><b>@ToolParam</b> - Spring AI 注解，标记方法参数（参数级别）</li>
 * </ul>
 *
 * <h3>设计优势（相比注解）：</h3>
 * <ul>
 *   <li>✅ 支持接口继承，可以定义工具组件的通用行为</li>
 *   <li>✅ 类型安全，编译时检查</li>
 *   <li>✅ 支持多态，便于扩展和测试</li>
 *   <li>✅ 符合面向接口编程原则</li>
 * </ul>
 *
 * <h3>注意事项：</h3>
 * <ul>
 *   <li>✅ 工具类应该是无状态的，避免共享可变状态</li>
 *   <li>✅ 工具方法应该幂等，多次调用结果一致</li>
 *   <li>✅ 工具方法应该有清晰的名称和描述</li>
 *   <li>⚠️ 避免在工具方法中执行耗时操作</li>
 *   <li>⚠️ 注意工具方法的安全性和权限控制</li>
 *   <li>⚠️ 确保工具类的线程安全性</li>
 * </ul>
 *
 * @author OWL Team
 * @version 1.0
 * @see org.springframework.ai.tool.annotation.Tool
 * @since 2026-04-16
 */
public interface ToolComponent {

    /**
     * 用户 ID 在 ToolContext 中的键名常量
     */
    String USER_ID_KEY = "userId";
    /**
     * 主用户 ID 常量
     */
    String MAIN_USER_ID = "main";


    /**
     * 从工具上下文中获取用户 ID
     *
     * @param context 工具上下文对象
     * @return 用户 ID，如果不存在则返回空 Optional
     */
    default Optional<String> getUserId(ToolContext context) {
        return Optional.ofNullable(context.getContext().get(USER_ID_KEY)).map(Object::toString);
    }
}
