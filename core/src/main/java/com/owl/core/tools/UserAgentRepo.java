package com.owl.core.tools;

public interface UserAgentRepo {
    /// 获取
    public String getUserConfig(String userId, String type);

    /// 设置
    ///
    /// @return
    public String setUserConfig(String userId, String type, String content);
}
