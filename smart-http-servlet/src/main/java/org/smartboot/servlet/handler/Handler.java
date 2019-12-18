package org.smartboot.servlet.handler;

import org.smartboot.servlet.HandlerContext;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public abstract class Handler {
    /**
     * 持有下一个处理器的句柄
     */
    protected Handler nextHandle;

    public abstract void handleRequest(HandlerContext handlerContext) throws Exception;

    protected final void doNext(HandlerContext handlerContext) throws Exception {
        if (nextHandle != null) {
            nextHandle.handleRequest(handlerContext);
        }
    }
}
