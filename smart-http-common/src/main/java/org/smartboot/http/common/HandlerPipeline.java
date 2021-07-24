/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HandlePipeline.java
 * Date: 2021-02-03
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.common;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2019/11/3
 */
public final class HandlerPipeline<REQ, RSP, T> extends Handler<REQ, RSP, T> implements Pipeline<REQ, RSP, T> {
    /**
     * 管道尾
     */
    private Handler<REQ, RSP, T> tailHandler;

    @Override
    public void onHeaderComplete(T request) throws IOException {
        Handler<REQ, RSP, T> httpHandler = nextHandler;
        while (httpHandler != null) {
            httpHandler.onHeaderComplete(request);
            httpHandler = httpHandler.nextHandler;
        }
    }

    /**
     * 解析 Http body内容
     *
     * @param buffer
     * @param request
     * @return
     */
    @Override
    public int onBodyStream(ByteBuffer buffer, T request) {
        Handler<REQ, RSP, T> httpHandler = nextHandler;
        int result = BODY_FINISH;
        while (httpHandler != null) {
            result = httpHandler.onBodyStream(buffer, request);
            httpHandler = httpHandler.nextHandler;
        }
        return result == BODY_CONTINUE ? BODY_CONTINUE : BODY_FINISH;
    }

    /**
     * 添加HttpHandle至末尾
     *
     * @param handler 尾部handler
     * @return 当前管道对象
     */
    public Pipeline<REQ, RSP, T> next(Handler<REQ, RSP, T> handler) {
        if (nextHandler == null) {
            nextHandler = tailHandler = handler;
            return this;
        }
        Handler<REQ, RSP, T> httpHandler = tailHandler;
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


    @Override
    public void onClose(T t) {
        Handler<REQ, RSP, T> httpHandler = nextHandler;
        while (httpHandler != null) {
            httpHandler.onClose(t);
            httpHandler = httpHandler.nextHandler;
        }
    }
}
