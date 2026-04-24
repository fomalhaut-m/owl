package com.owl.core.skills.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.owl.core.memory.MemoryMetadata;
import com.owl.core.memory.MemoryRepo;
import com.owl.core.memory.MemoryUserConfig;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.Optional;

/**
 *
 */
public class ChatTools implements ToolComponent {


    /**
     * 根据时间区间, 获取近期的聊天记录数据, 分页
     */
    @Tool(name = "owl_chat_history_get", description = "获取近期的聊天记录数据")
    public List<ChatMetadata> getChatHistory(ToolContext context,
                                               @ToolParam(description = "开始时间") long startTime,
                                               @ToolParam(description = "结束时间") long endTime     ) {
        Optional<String> userId = getUserId(context);
        return List.of();
    }

}
