package com.owl.core.user;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
public class UserConfigProperties {

    private String defaultUserId = "main";

    private UserPreferences preferences = UserPreferences.builder().build();
}