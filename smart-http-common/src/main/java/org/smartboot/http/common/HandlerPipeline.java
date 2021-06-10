/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HandlePipeline.java
 * Date: 2021-02-03
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.common;

import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2019/11/3
 */
public final class HandlerPipeline<REQ, RSP> extends Handler<REQ, RSP> implements Pipeline<REQ, RSP> {
    /**
     * 管道尾
     */
    private Handler<REQ, RSP> tailHandler;

    /**
     * 添加HttpHandle至末尾
     *
     * @param handler 尾部handler
     * @return 当前管道对象
     */
    public Pipeline<REQ, RSP> next(Handler<REQ, RSP> handler) {
        if (nextHandler == null) {
            nextHandler = tailHandler = handler;
            return this;
        }
        Handler<REQ, RSP> httpHandler = tailHandler;
        while (httpHandler.nextHandler != null) {
            httpHandler = httpHandler.nextHandler;
        }
        httpHandler.nextHandler = handler;
        return this;
    }

    @Override
    public void handle(REQ request, RSP response) throws IOException {
        nextHandler.handle(request, response);
    }
}
