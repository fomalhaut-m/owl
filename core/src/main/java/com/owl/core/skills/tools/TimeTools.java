package com.owl.core.skills.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 时间工具类
 * <p>
 * 提供获取当前系统时间和指定时区时间的功能。
 * 实现 {@link ToolComponent} 接口，通过 {@link Tool} 注解暴露给 LLM，使其能够查询时间信息。
 * </p>
 *
 * <h3>提供的工具：</h3>
 * <ul>
 *   <li><b>getCurrentSystemTime()</b> - 获取当前系统时间</li>
 *   <li><b>getCurrentTimeByZone(String)</b> - 获取指定时区的当前时间</li>
 * </ul>
 *
 * <h3>使用示例（LLM 调用）：</h3>
 * <pre>{@code
 * // LLM 可以这样调用：
 * // "现在几点？" -> 调用 getCurrentSystemTime()
 * // "纽约现在几点？" -> 调用 getCurrentTimeByZone("America/New_York")
 * // "伦敦时间是多少？" -> 调用 getCurrentTimeByZone("Europe/London")
 * }</pre>
 *
 * <h3>常见时区 ID：</h3>
 * <ul>
 *   <li>Asia/Shanghai - 中国标准时间 (UTC+8)</li>
 *   <li>Asia/Tokyo - 日本标准时间 (UTC+9)</li>
 *   <li>Europe/London - 英国时间 (UTC+0/UTC+1)</li>
 *   <li>America/New_York - 美国东部时间 (UTC-5/UTC-4)</li>
 *   <li>America/Los_Angeles - 美国太平洋时间 (UTC-8/UTC-7)</li>
 *   <li>UTC - 协调世界时</li>
 * </ul>
 *
 * @author OWL Team
 * @version 1.0
 * @see ToolComponent
 * @see org.springframework.ai.tool.annotation.Tool
 * @since 2026-04-16
 */
public class TimeTools implements ToolComponent{

    /**
     * 获取当前系统时间
     * <p>
     * 返回服务器所在时区的当前时间，格式为 yyyy-MM-dd HH:mm:ss。
     * 这是最简单的时间查询工具，无需任何参数。
     * </p>
     *
     * <h3>返回格式：</h3>
     * <pre>当前系统时间：2026-04-16 14:30:00</pre>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>用户询问当前时间</li>
     *   <li>需要记录操作时间戳</li>
     *   <li>计算时间间隔的基准点</li>
     * </ul>
     *
     * @return 格式化后的时间字符串，格式：yyyy-MM-dd HH:mm:ss
     *
     * <h3>LLM 调用示例：</h3>
     * <pre>{@code
     * 用户："现在几点？"
     * LLM：调用 getCurrentSystemTime()
     * 返回："当前系统时间：2026-04-16 14:30:00"
     * }</pre>
     */
    @Tool(name = "owl_time_current_system", description = "获取当前系统时间")
    public String getCurrentSystemTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "当前系统时间：" + LocalDateTime.now().format(formatter);
    }

    /**
     * 获取指定时区的当前时间
     * <p>
     * 根据提供的时区 ID，返回该时区的当前时间。
     * 支持全球所有标准时区，包括夏令时自动调整。
     * </p>
     *
     * <h3>返回格式：</h3>
     * <pre>时区[Asia/Shanghai] 当前时间：2026-04-16 14:30:00</pre>
     *
     * <h3>参数说明：</h3>
     * <ul>
     *   <li><b>zoneId</b> - IANA 时区 ID，如 Asia/Shanghai、Europe/London 等</li>
     * </ul>
     *
     * <h3>错误处理：</h3>
     * <ul>
     *   <li>如果时区 ID 无效，返回友好的错误提示</li>
     *   <li>不会抛出异常，保证 LLM 调用的稳定性</li>
     * </ul>
     *
     * @param zoneId 时区ID，例如 Asia/Shanghai、Europe/London、America/New_York
     * @return 格式化后的时间字符串，格式：yyyy-MM-dd HH:mm:ss
     *         如果时区 ID 无效，返回错误提示信息
     *
     * <h3>LLM 调用示例：</h3>
     * <pre>{@code
     * 用户："纽约现在几点？"
     * LLM：调用 getCurrentTimeByZone("America/New_York")
     * 返回："时区[America/New_York] 当前时间：2026-04-16 02:30:00"
     * 
     * 用户："东京时间是多少？"
     * LLM：调用 getCurrentTimeByZone("Asia/Tokyo")
     * 返回："时区[Asia/Tokyo] 当前时间：2026-04-16 15:30:00"
     * }</pre>
     *
     * <h3>常见时区 ID 参考：</h3>
     * <table border="1">
     *   <tr><th>地区</th><th>时区 ID</th><th>UTC 偏移</th></tr>
     *   <tr><td>北京/上海</td><td>Asia/Shanghai</td><td>UTC+8</td></tr>
     *   <tr><td>东京</td><td>Asia/Tokyo</td><td>UTC+9</td></tr>
     *   <tr><td>伦敦</td><td>Europe/London</td><td>UTC+0/UTC+1</td></tr>
     *   <tr><td>巴黎</td><td>Europe/Paris</td><td>UTC+1/UTC+2</td></tr>
     *   <tr><td>纽约</td><td>America/New_York</td><td>UTC-5/UTC-4</td></tr>
     *   <tr><td>洛杉矶</td><td>America/Los_Angeles</td><td>UTC-8/UTC-7</td></tr>
     *   <tr><td>悉尼</td><td>Australia/Sydney</td><td>UTC+10/UTC+11</td></tr>
     * </table>
     */
    @Tool(name = "owl_time_current_by_zone", description = "获取指定时区的当前系统时间")
    public String getCurrentTimeByZone(@ToolParam(description = "时区ID，例如 Asia/Shanghai、Europe/London、America/New_York") String zoneId) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now(ZoneId.of(zoneId));
            return "时区[" + zoneId + "] 当前时间：" + now.format(formatter);
        } catch (Exception e) {
            return "无效的时区ID：" + zoneId + "，请使用标准时区格式，如 Asia/Shanghai";
        }
    }
}