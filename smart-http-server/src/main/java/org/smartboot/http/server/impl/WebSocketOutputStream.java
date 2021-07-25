/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: WebSocketOutputStream.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.socket.transport.AioSession;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
final class WebSocketOutputStream extends AbstractOutputStream {

    public WebSocketOutputStream(WebSocketRequestImpl request, WebSocketResponseImpl response, AioSession writeBuffer) {
        super(request, response, writeBuffer);
        super.chunked = false;
    }

    @Override
    protected final byte[] getHeadPart() {
        return getBytes(request.getProtocol() + " " + response.getHttpStatus() + " " + response.getReasonPhrase() + "\r\n"
                + HeaderNameEnum.CONTENT_TYPE.getName() + ":" + response.getContentType() + "\r\n");
    }
}
