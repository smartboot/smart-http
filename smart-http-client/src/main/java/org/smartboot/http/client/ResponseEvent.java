/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: ResponseEvent.java
 * Date: 2021-07-24
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.client.impl.Response;

import java.nio.ByteBuffer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/7/24
 */
public interface ResponseEvent {
    default void onHeader(Response response) {
    }

    boolean onBody(ByteBuffer buffer, Response baseHttpResponse);
}
