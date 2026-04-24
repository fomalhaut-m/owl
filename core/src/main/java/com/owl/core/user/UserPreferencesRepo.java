package com.owl.core.user;

public interface UserPreferencesRepo {

    UserPreferences getUserPreferences(String userId);

    UserPreferences setUserPreferences(String userId, UserPreferences preferences);
}