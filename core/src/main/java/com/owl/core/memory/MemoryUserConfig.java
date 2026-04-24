package com.owl.core.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 记忆系统用户配置类
 * <p>
 * 用于存储和管理用户的记忆系统配置参数，包括上下文管理、数据备份、
 * 记忆分级、语义相似度等设置。
 * </p>
 *
 * <h3>配置项说明：</h3>
 * <ul>
 *   <li><b>contextMaxTokenRate</b> - 上下文 Token 使用率，控制单次对话使用的最大上下文比例</li>
 *   <li><b>coldBackupExpireDays</b> - 冷备份保留天数，超期自动清理</li>
 *   <li><b>sDowngradeDays / sDeleteDays</b> - S 级记忆降级和删除天数</li>
 *   <li><b>aDowngradeDays / aDeleteDays</b> - A 级记忆降级和删除天数</li>
 *   <li><b>bDeleteDays</b> - B 级记忆删除天数</li>
 *   <li><b>semanticSimilarThreshold</b> - 语义相似度阈值，用于检测重复记忆</li>
 *   <li><b>compressRatio</b> - 记忆压缩比率</li>
 * </ul>
 *
 * @author Owl Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryUserConfig {

    /**
     * 上下文最大 Token 使用率
     * <p>
     * 控制在单次对话中允许使用的最大上下文比例，范围 0.0-1.0。
     * 默认值为 0.3，表示最多使用可用上下文的 30%。
     * </p>
     */
    @Builder.Default
    private Double contextMaxTokenRate = 0.3;

    /**
     * 冷备份过期天数
     * <p>
     * 冷备份数据的保留期限，超过此天数的备份将被清理。
     * 默认值为 90 天。
     * </p>
     */
    @Builder.Default
    private Integer coldBackupExpireDays = 90;

    /**
     * S 级记忆降级天数
     * <p>
     * S 级（核心知识）记忆在多少天后降级为 A 级。
     * 默认值为 14 天。
     * </p>
     */
    @Builder.Default
    private Integer sDowngradeDays = 14;

    /**
     * S 级记忆删除天数
     * <p>
     * S 级记忆在多少天后被永久删除。
     * 默认值为 30 天。
     * </p>
     */
    @Builder.Default
    private Integer sDeleteDays = 30;

    /**
     * A 级记忆降级天数
     * <p>
     * A 级（重要知识）记忆在多少天后降级为 B 级。
     * 默认值为 7 天。
     * </p>
     */
    @Builder.Default
    private Integer aDowngradeDays = 7;

    /**
     * A 级记忆删除天数
     * <p>
     * A 级记忆在多少天后被永久删除。
     * 默认值为 14 天。
     * </p>
     */
    @Builder.Default
    private Integer aDeleteDays = 14;

    /**
     * B 级记忆删除天数
     * <p>
     * B 级（一般知识）记忆在多少天后被永久删除。
     * 默认值为 7 天。
     * </p>
     */
    @Builder.Default
    private Integer bDeleteDays = 7;

    /**
     * 语义相似度阈值
     * <p>
     * 用于判断两条记忆是否语义相似的阈值，范围 0.0-1.0。
     * 默认值为 0.85，表示相似度达到 85% 以上视为相似，需要合并或去重。
     * </p>
     */
    @Builder.Default
    private Double semanticSimilarThreshold = 0.85;

    /**
     * 记忆压缩比率
     * <p>
     * 记忆内容的压缩比率配置，数值越大压缩程度越高。
     * 默认值为 10。
     * </p>
     */
    @Builder.Default
    private Integer compressRatio = 10;
}
