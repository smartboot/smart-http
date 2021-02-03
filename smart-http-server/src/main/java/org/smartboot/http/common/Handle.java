/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: Handle.java
 * Date: 2021-02-03
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.common;

import java.io.IOException;

/**
 * 消息处理器
 *
 * @author 三刀
 * @version V1.0 , 2018/2/6
 */
public abstract class Handle<REQ, RSP> {
    /**
     * 持有下一个处理器的句柄
     */
    protected Handle<REQ, RSP> nextHandle;

    Handle() {
    }

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
    public abstract void doHandle(REQ request, RSP response) throws IOException;

    protected final void doNext(REQ request, RSP response) throws IOException {
        if (nextHandle != null) {
            nextHandle.doHandle(request, response);
        }
    }
}