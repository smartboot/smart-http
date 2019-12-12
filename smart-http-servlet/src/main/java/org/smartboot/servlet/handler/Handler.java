package org.smartboot.servlet.handler;

import org.smartboot.servlet.HandlerContext;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public interface Handler {
    public void handleRequest(HandlerContext handlerContext) throws Exception;
}
