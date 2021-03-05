/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: AttachKey.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.common.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 三刀
 * @version V1.0 , 2018/6/2
 */
public final class AttachKey<T> {
    /**
     * 缓存同名Key
     */
    private static final ConcurrentMap<String, AttachKey> KEY_CACHE = new ConcurrentHashMap<>();
    /**
     * 附件名称
     */
    private final String key;

    private AttachKey(String key) {
        this.key = key;
    }

    public static <T> AttachKey<T> valueOf(String name) {
        AttachKey<T> option = KEY_CACHE.get(name);
        if (option == null) {
            option = new AttachKey<>(name);
            AttachKey<T> old = KEY_CACHE.putIfAbsent(name, option);
            if (old != null) {
                option = old;
            }
        }
        return option;
    }

    public String getKey() {
        return key;
    }
}
