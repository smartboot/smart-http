/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpLifecycle.java
 * Date: 2021-07-15
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.client.impl.Response;

import java.nio.ByteBuffer;

/**
 * Http body解码器
 *
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/7/12
 */
public interface HttpLifecycle {

    /**
     * Http 解码成功
     *
     * @param response
     */
    default void onHeaderComplete(Response response) {
    }

    boolean onBodyStream(ByteBuffer buffer, Response response);
}
