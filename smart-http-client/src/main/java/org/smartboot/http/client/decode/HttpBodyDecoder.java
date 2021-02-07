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
import org.smartboot.http.common.utils.AttachKey;
import org.smartboot.http.common.utils.Attachment;
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
    private final AttachKey<SmartDecoder> ATTACH_KEY_FIX_LENGTH_DECODER = AttachKey.valueOf("fixLengthDecoder");

    @Override
    public Decoder decode(ByteBuffer byteBuffer, AioSession aioSession, Response response) {
        Attachment attachment = aioSession.getAttachment();
        SmartDecoder smartDecoder = attachment.get(ATTACH_KEY_FIX_LENGTH_DECODER);
        if (smartDecoder == null) {
            smartDecoder = new FixedLengthFrameDecoder(response.getContentLength());
            attachment.put(ATTACH_KEY_FIX_LENGTH_DECODER, smartDecoder);
        }

        if (smartDecoder.decode(byteBuffer)) {
            response.setBody(new String(smartDecoder.getBuffer().array(), Charset.forName(response.getCharacterEncoding())));
            attachment.remove(ATTACH_KEY_FIX_LENGTH_DECODER);
            return HttpResponseProtocol.HTTP_FINISH_DECODER;
        } else {
            return this;
        }
    }
}
