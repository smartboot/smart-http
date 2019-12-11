package org.smartboot.servlet.handler;

import org.smartboot.servlet.HttpServerExchange;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public interface Handler {
    public void handleRequest(HttpServerExchange exchange) throws Exception;
}
