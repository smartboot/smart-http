/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: Handle.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.common;

import org.smartboot.http.common.enums.BodyStreamStatus;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 消息处理器
 *
 * @author 三刀
 * @version V1.0 , 2018/2/6
 */
public interface Handler<T> {

    /**
     * 解析 body 数据流
     *
     * @param buffer
     * @param request
     * @return
     */
    BodyStreamStatus onBodyStream(ByteBuffer buffer, T request);


    /**
     * Http header 完成解析
     */
    default void onHeaderComplete(T request) throws IOException {
    }

    /**
     * 断开 TCP 连接
     */
    default void onClose(T request) {
    }
}
