package com.owl.core.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户偏好配置类
 * <p>
 * 用于存储和管理用户的个性化配置参数，包括上下文管理、数据备份、语义相似度等设置。
 * 所有字段都提供了默认值，可通过 Builder 模式进行自定义配置。
 * </p>
 *
 * @author Owl Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {


    /**
     * 是否自动整理记忆
     */
    @Builder.Default
    private Boolean autoOrganizeMemory = false;
}