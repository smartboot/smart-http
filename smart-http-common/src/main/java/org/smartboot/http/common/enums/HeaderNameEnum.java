/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HeaderNameEnum.java
 * Date: 2020-04-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.common.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2018/12/6
 */
public enum HeaderNameEnum {
    ACCEPT("Accept"),
    ACCEPT_CHARSET("Accept-Charset"),
    ACCEPT_ENCODING("Accept-Encoding"),
    ACCEPT_LANGUAGE("Accept-Language"),
    ACCEPT_RANGE("Accept-Range"),
    AGE("Age"),
    ALLOW("Allow"),
    AUTHORIZATION("Authorization"),
    CACHE_CONTROL("Cache-Control"),
    CONNECTION("Connection"),
    CONTENT_ENCODING("Content-Encoding"),
    CONTENT_LANGUAGE("Content-Language"),
    CONTENT_LENGTH("Content-Length"),
    CONTENT_LOCATION("Content-Location"),
    CONTENT_MD5("Content-MD5"),
    CONTENT_RANGE("Content-Range"),
    CONTENT_TYPE("Content-Type"),
    DATE("Date"),
    ETAG("ETag"),
    EXPECT("Expect"),
    EXPIRES("Expires"),
    FROM("From"),
    HOST("Host"),
    IF_MATCH("If-Match"),
    IF_MODIFIED_SINCE("If-Modified-Since"),
    IF_NONE_MATCH("If-None-Match"),
    IF_RANGE("If-Range"),
    IF_UNMODIFIED_SINCE("If-Unmodified-Since"),
    LAST_MODIFIED("Last-Modified"),
    LOCATION("Location"),
    MAX_FORWARDS("Max-Forwards"),
    PRAGMA("Pragma"),
    PROXY_AUTHENTICATE("Proxy-Authenticate"),
    PROXY_AUTHORIZATION("Proxy-Authorization"),
    RANGE("Range"),
    REFERER("Referer"),
    RETRY_AFTER("Retry-After"),
    SERVER("Server"),
    TE("TE"),
    TRAILER("Trailer"),
    TRANSFER_ENCODING("Transfer-Encoding"),
    UPGRADE("Upgrade"),
    USER_AGENT("User-Agent"),
    VARY("Vary"),
    VIA("Via"),
    WARNING("Warning"),
    WWW_AUTHENTICATE("WWW-Authenticate"),
    Sec_WebSocket_Accept("Sec-WebSocket-Accept"),
    COOKIE("Cookie"),
    SET_COOKIE("Set-Cookie"),
    Sec_WebSocket_Key("Sec-WebSocket-Key"),
    HTTP2_SETTINGS("HTTP2-Settings");
    public static final Map<String, HeaderNameEnum> HEADER_NAME_ENUM_MAP = new HashMap<>();

    static {
        for (HeaderNameEnum headerNameEnum : HeaderNameEnum.values()) {
            HEADER_NAME_ENUM_MAP.put(headerNameEnum.getName(), headerNameEnum);
        }
    }

    private final String name;

    private final byte[] bytes;

    private final byte[] bytesWithColon;


    HeaderNameEnum(String name) {
        this.name = name;
        this.bytes = name.getBytes();
        this.bytesWithColon = (name + ":").getBytes();
    }


    public String getName() {
        return name;
    }

    public boolean equals(byte[] bytes, int length) {
        if (this.bytes.length != length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (this.bytes[i] != bytes[i]) {
                return false;
            }
        }
        return true;
    }

    public byte[] getBytesWithColon() {
        return bytesWithColon;
    }
}
