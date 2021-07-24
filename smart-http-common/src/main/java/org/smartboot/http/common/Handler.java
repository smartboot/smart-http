/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: Handle.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.common;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 消息处理器
 *
 * @author 三刀
 * @version V1.0 , 2018/2/6
 */
public abstract class Handler<REQ, RSP, T> {

    /**
     * 解析 body 数据流
     *
     * @param buffer
     * @param request
     * @return
     */
    public abstract boolean onBodyStream(ByteBuffer buffer, T request);

    /**
     * 执行当前处理器逻辑。
     * <p>
     * 当前handle运行完后若还有后续的处理器，需要调用doNext
     * </p>
     *
     * @param request
     * @param response
     * @throws IOException
     */
    public abstract void handle(REQ request, RSP response) throws IOException;

    /**
     * Http header 完成解析
     */
    public void onHeaderComplete(T request) throws IOException {
    }

    /**
     * 断开 TCP 连接
     */
    public void onClose(T request) {
    }
}
