package com.owl.core.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 记忆元数据类
 * <p>
 * 封装记忆的元数据信息，包括路径、时间戳和内容大小等属性。
 * 用于提供记忆的详细信息，支持记忆管理和查询功能。
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
     * 记忆存储路径
     * <p>
     * 用于定位和检索记忆的唯一标识路径。
     * </p>
     */
    private String path;

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
