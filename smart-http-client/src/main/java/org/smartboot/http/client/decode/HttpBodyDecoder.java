/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RequestLineDecoder.java
 * Date: 2020-03-30
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.decode;

import org.smartboot.http.client.impl.HttpResponseProtocol;
import org.smartboot.http.client.impl.Response;
import org.smartboot.http.client.impl.ResponseAttachment;
import org.smartboot.http.common.utils.FixedLengthFrameDecoder;
import org.smartboot.http.common.utils.SmartDecoder;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/30
 */
class HttpBodyDecoder implements Decoder {

    @Override
    public Decoder decode(ByteBuffer byteBuffer, AioSession aioSession, Response response) {
        ResponseAttachment attachment = aioSession.getAttachment();
        SmartDecoder smartDecoder = attachment.getBodyDecoder();
        if (smartDecoder == null) {
            smartDecoder = new FixedLengthFrameDecoder(response.getContentLength());
            attachment.setBodyDecoder(smartDecoder);
        }

        if (smartDecoder.decode(byteBuffer)) {
            response.setBody(new String(smartDecoder.getBuffer().array(), Charset.forName(response.getCharacterEncoding())));
            attachment.setBodyDecoder(null);
            return HttpResponseProtocol.HTTP_FINISH_DECODER;
        } else {
            return this;
        }
    }
}
