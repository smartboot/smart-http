/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RequestLineDecoder.java
 * Date: 2020-03-30
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.decode;

import org.smartboot.http.common.utils.FixedLengthFrameDecoder;
import org.smartboot.http.common.utils.SmartDecoder;
import org.smartboot.http.server.impl.HttpRequestProtocol;
import org.smartboot.http.server.impl.Request;
import org.smartboot.http.server.impl.RequestAttachment;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/30
 */
class HttpBodyDecoder implements Decoder {

    @Override
    public Decoder decode(ByteBuffer byteBuffer, AioSession aioSession, Request request) {
        RequestAttachment attachment = aioSession.getAttachment();
        SmartDecoder smartDecoder = attachment.getBodyDecoder();
        if (smartDecoder == null) {
            smartDecoder = new FixedLengthFrameDecoder(request.getContentLength());
            attachment.setBodyDecoder(smartDecoder);
        }

        if (smartDecoder.decode(byteBuffer)) {
            request.setFormUrlencoded(new String(smartDecoder.getBuffer().array()));
            attachment.setBodyDecoder(null);
            return HttpRequestProtocol.HTTP_FINISH_DECODER;
        } else {
            return this;
        }
    }
}
