package com.owl.core.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 记忆元数据类
 * <p>
 * 封装记忆的元数据信息，包括路径（目录结构）、文件名、时间戳和内容大小等属性。
 * path 表示文件的目录路径列表（如 ["working", "P0"]），
 * name 表示文件名（如 "test.md"）。
 * </p>
 *
 * @author Owl Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryMetadata {

    /**
     * 记忆存储路径（目录部分）
     * <p>
     * 文件所在的目录路径，以列表形式存储，便于分类检索。
     * 例如：["working", "P0"] 表示文件位于 working/P0/ 目录下。
     * </p>
     */
    private List<String> path;

    /**
     * 记忆文件名称
     * <p>
     * 记忆文件的名称，包含扩展名。
     * 例如："test.md"、"20260423_notes.md"。
     * </p>
     */
    private String name;

    /**
     * 创建时间戳（毫秒）
     * <p>
     * 记忆首次创建的时间，使用 Unix 时间戳表示（毫秒）。
     * </p>
     */
    private Long createTime;

    /**
     * 更新时间戳（毫秒）
     * <p>
     * 记忆最后一次更新的时间，使用 Unix 时间戳表示（毫秒）。
     * 如果记忆未被修改过，则与创建时间相同。
     * </p>
     */
    private Long updateTime;

    /**
     * 内容大小（字节）
     * <p>
     * 记忆内容的字节大小，用于评估存储空间占用。
     * </p>
     */
    private Long contentSize;
}
