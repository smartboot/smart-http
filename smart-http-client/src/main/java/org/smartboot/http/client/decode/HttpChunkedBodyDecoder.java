/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpChunkedBodyDecoder.java
 * Date: 2021-02-03
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.decode;

import org.smartboot.http.client.impl.HttpResponseProtocol;
import org.smartboot.http.client.impl.Response;
import org.smartboot.http.client.impl.ResponseAttachment;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.FixedLengthFrameDecoder;
import org.smartboot.http.common.utils.SmartDecoder;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/3
 */
public class HttpChunkedBodyDecoder implements Decoder {
    private final HttpChunkedContentDecoder httpChunkedContentDecoder = new HttpChunkedContentDecoder();
    private final HttpChunkedEndDecoder httpChunkedEndDecoder = new HttpChunkedEndDecoder();

    @Override
    public Decoder decode(ByteBuffer byteBuffer, AioSession aioSession, Response response) {
        int length = StringUtils.scanUntilAndTrim(byteBuffer, Constant.LF);
        if (length < 0) {
            return this;
        }
        if (length == 1) {
            return HttpResponseProtocol.HTTP_FINISH_DECODER;
        }
        String contentLength = StringUtils.convertToString(byteBuffer, byteBuffer.position() - length - 1, length - 1);
        int len = Integer.parseInt(contentLength, 16);
        ResponseAttachment attachment = aioSession.getAttachment();
        if (len == 0) {
            StringBuilder stringBuilder = attachment.getChunkBodyContent();
            if (stringBuilder != null) {
                response.setBody(stringBuilder.toString());
            }
            attachment.setChunkBodyContent(null);
            return decode(byteBuffer, aioSession, response);
        }
        SmartDecoder smartDecoder = attachment.getBodyDecoder();
        if (smartDecoder == null) {
            smartDecoder = new FixedLengthFrameDecoder(len);
            attachment.setBodyDecoder(smartDecoder);
        }
        return httpChunkedContentDecoder.decode(byteBuffer, aioSession, response);
    }

    public class HttpChunkedContentDecoder implements Decoder {

        @Override
        public Decoder decode(ByteBuffer byteBuffer, AioSession aioSession, Response response) {
            ResponseAttachment attachment = aioSession.getAttachment();
            SmartDecoder smartDecoder = attachment.getBodyDecoder();
            if (!smartDecoder.decode(byteBuffer)) {
                return this;
            }
            String chunkedContent = new String(smartDecoder.getBuffer().array(), Charset.forName(response.getCharacterEncoding()));
            StringBuilder stringBuilder = attachment.getChunkBodyContent();
            if (stringBuilder == null) {
                stringBuilder = new StringBuilder(chunkedContent);
                attachment.setChunkBodyContent(stringBuilder);
            } else {
                stringBuilder.append(chunkedContent);
            }
            stringBuilder.setLength(stringBuilder.length());
            attachment.setBodyDecoder(null);
            return httpChunkedEndDecoder.decode(byteBuffer, aioSession, response);
        }
    }

    public class HttpChunkedEndDecoder implements Decoder {
        @Override
        public Decoder decode(ByteBuffer byteBuffer, AioSession aioSession, Response response) {
            if (byteBuffer.remaining() < 2) {
                return this;
            }
            if (byteBuffer.get() == Constant.CR && byteBuffer.get() == Constant.LF) {
                return HttpChunkedBodyDecoder.this.decode(byteBuffer, aioSession, response);
            }
            throw new IllegalStateException();
        }
    }
}
