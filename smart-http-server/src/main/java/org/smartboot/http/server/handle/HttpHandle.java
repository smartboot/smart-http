/*
 * Copyright (c) 2018, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: HttpHandle.java
 * Date: 2018-02-15
 * Author: sandao
 */

package org.smartboot.http.server.handle;

import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;

import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/6
 */
public abstract class HttpHandle {
    /**
     * 持有下一个处理器的句柄
     */
    protected HttpHandle nextHandle;

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
    public abstract void doHandle(HttpRequest request, HttpResponse response) throws IOException;

    protected final void doNext(HttpRequest request, HttpResponse response) throws IOException {
        if (nextHandle != null) {
            nextHandle.doHandle(request, response);
        }
    }
}
