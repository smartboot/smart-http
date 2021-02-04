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
import org.smartboot.http.utils.AttachKey;
import org.smartboot.http.utils.Attachment;
import org.smartboot.http.utils.Constant;
import org.smartboot.http.utils.FixedLengthFrameDecoder;
import org.smartboot.http.utils.SmartDecoder;
import org.smartboot.http.utils.StringUtils;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/3
 */
public class HttpChunkedBodyDecoder implements Decoder {
    private final AttachKey<SmartDecoder> ATTACH_KEY_FIX_LENGTH_DECODER = AttachKey.valueOf("chunkedContentDecoder");
    private final AttachKey<StringBuilder> ATTACH_KEY_CHUNKED_CONTENT = AttachKey.valueOf("chunkedContentSB");
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
        String contentLength = StringUtils.convertToString(byteBuffer, byteBuffer.position() - length - 1, length - 1, StringUtils.String_CACHE_URL);
        int len = Integer.parseInt(contentLength, 16);
        Attachment attachment = aioSession.getAttachment();
        if (len == 0) {
            StringBuilder stringBuilder = attachment.get(ATTACH_KEY_CHUNKED_CONTENT);
            response.setBody(stringBuilder.toString());
            attachment.remove(ATTACH_KEY_CHUNKED_CONTENT);
            return decode(byteBuffer, aioSession, response);
        }
        SmartDecoder smartDecoder = attachment.get(ATTACH_KEY_FIX_LENGTH_DECODER);
        if (smartDecoder == null) {
            smartDecoder = new FixedLengthFrameDecoder(len);
            attachment.put(ATTACH_KEY_FIX_LENGTH_DECODER, smartDecoder);
        }
        return httpChunkedContentDecoder.decode(byteBuffer, aioSession, response);
    }

    public class HttpChunkedContentDecoder implements Decoder {

        @Override
        public Decoder decode(ByteBuffer byteBuffer, AioSession aioSession, Response response) {
            Attachment attachment = aioSession.getAttachment();
            SmartDecoder smartDecoder = attachment.get(ATTACH_KEY_FIX_LENGTH_DECODER);
            if (!smartDecoder.decode(byteBuffer)) {
                return this;
            }
            String chunkedContent = new String(smartDecoder.getBuffer().array(), Charset.forName(response.getCharacterEncoding()));
            StringBuilder stringBuilder = attachment.get(ATTACH_KEY_CHUNKED_CONTENT);
            if (stringBuilder == null) {
                stringBuilder = new StringBuilder(chunkedContent);
                attachment.put(ATTACH_KEY_CHUNKED_CONTENT, stringBuilder);
            } else {
                stringBuilder.append(chunkedContent);
            }
            stringBuilder.setLength(stringBuilder.length());
            attachment.remove(ATTACH_KEY_FIX_LENGTH_DECODER);
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
