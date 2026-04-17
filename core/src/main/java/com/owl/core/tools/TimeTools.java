package com.owl.core.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@ToolComponent
public class TimeTools {

    @Tool
    public String getCurrentSystemTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "当前系统时间：" + LocalDateTime.now().format(formatter);
    }

    /**
     * 获取指定时区的当前时间
     *
     * @param zoneId 时区ID，例如 Asia/Shanghai、Europe/London、America/New_York
     * @return 格式化后的时间字符串 yyyy-MM-dd HH:mm:ss
     */
    @Tool(name = "getCurrentTimeByZone", description = "获取指定时区的当前系统时间")
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