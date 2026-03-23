package com.minitalk.infra.redis;

public final class RedisKeyPrefix {

    private RedisKeyPrefix() {
    }

    public static final String PRESENCE_USER = "presence:user:";
    public static final String TYPING_ROOM = "typing:room:";
    public static final String JWT_BLACKLIST = "jwt:blacklist:";
    public static final String CHANNEL_ROOM = "channel:room:";
    public static final String CHANNEL_PRESENCE = "channel:presence";
    public static final String CALL_ACTIVE = "call:active:";
    public static final String WS_SESSION = "ws:session:";
}
