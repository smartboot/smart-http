/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpLifecycle.java
 * Date: 2021-07-14
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.http.server.impl.Request;

import java.nio.ByteBuffer;

/**
 * Http body解码器
 *
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/7/12
 */
public interface HttpLifecycle {

    /**
     * Http header 完成解析
     */
    default void onHeaderComplete(Request request) {
    }

    /**
     * 解析 body 数据流
     *
     * @param buffer
     * @param request
     * @return
     */
    boolean onBodyStream(ByteBuffer buffer, Request request);

    /**
     * 断开 TCP 连接
     */
    default void onClose() {
    }
}
