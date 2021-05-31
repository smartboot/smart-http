/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpHeaderConstant.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.common.utils;

import org.smartboot.http.common.enums.HeaderNameEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/8
 */
public class HttpHeaderConstant {
    public static final Map<String, HeaderNameEnum> HEADER_NAME_ENUM_MAP = new HashMap<>();
    public static final Map<String, byte[]> HEADER_NAME_EXT_MAP = new ConcurrentHashMap<>();

    static {
        for (HeaderNameEnum headerNameEnum : HeaderNameEnum.values()) {
            HEADER_NAME_ENUM_MAP.put(headerNameEnum.getName(), headerNameEnum);
        }
    }

    public interface Values {
        String CHUNKED = "chunked";

        String MULTIPART_FORM_DATA = "multipart/form-data";

        String X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

        String UPGRADE = "Upgrade";

        String WEBSOCKET = "websocket";

        String KEEPALIVE = "Keep-Alive";

        String DEFAULT_CONTENT_TYPE = "text/html; charset=utf-8";
    }


}
