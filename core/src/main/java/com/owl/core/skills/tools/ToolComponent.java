package com.owl.core.skills.tools;

import org.springframework.ai.chat.model.ToolContext;

import java.util.Optional;

/**
 * 工具组件标记接口
 * <p>
 * 标记一个类为 LLM 工具组件，表示该类包含可以被大语言模型调用的工具方法。
 * 实现此接口的类会被框架自动扫描和注册为可用工具。
 * </p>
 *
 * <h3>作用：</h3>
 * <ul>
 *   <li>标识包含 {@link org.springframework.ai.tool.annotation.Tool} 方法的类</li>
 *   <li>方便框架自动扫描和注册工具</li>
 *   <li>提供类型安全的标记，支持接口继承和多态</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 1. 实现 ToolComponent 接口
 * public class TimeTools implements ToolComponent {
 *
 *     @Tool(name = "getCurrentTime", description = "获取当前时间")
 *     public String getCurrentTime() {
 *         return LocalDateTime.now().toString();
 *     }
 * }
 *
 * // 2. 框架会自动扫描并注册所有实现 ToolComponent 的类
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
