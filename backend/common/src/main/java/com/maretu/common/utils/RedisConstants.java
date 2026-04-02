package com.maretu.common.utils;

public class RedisConstants {
    public static final String APP_NAME = "maretu";
    public static final String USER_SERVICE_PREFIX = APP_NAME + ":users:";

    //  Null value
    public static final String NULL_VALUE = "NULL";
    public static final Long NULL_CACHE_TTL = 5L;

    //  Lock
    public static final String LOCK_KEY = "lock:";
    public static final Long LOCK_TTL = 30L;

    //  User-service
    public static final String VERIFY_CODE_KEY = USER_SERVICE_PREFIX + "verify:";
    public static final Long VERIFY_CODE_TTL = 10L;
    public static final String RESET_PASSWORD_KEY = USER_SERVICE_PREFIX + "reset:";
    public static final Long RESET_PASSWORD_TTL = 10L;
    public static final String ROLES_KEY = USER_SERVICE_PREFIX + "roles";
    public static final String USER_ROLES_KEY = USER_SERVICE_PREFIX + "user-roles:";
    public static final Long USER_ROLES_TTL = 30L; // 30分钟

    //  Chat-service
    public static final String CHAT_SERVICE_PREFIX = APP_NAME + ":chat:";
    public static final String SESSION_OWNER_KEY = CHAT_SERVICE_PREFIX + "session:";
    public static final Long SESSION_OWNER_TTL = 30L;
}
