/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpRequestProtocol.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.http.server.decode.DecodeChain;
import org.smartboot.http.server.decode.FinishDecoder;
import org.smartboot.http.server.decode.HttpMethodDecoder;
import org.smartboot.http.utils.AttachKey;
import org.smartboot.http.utils.Attachment;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2018/8/31
 */
public class HttpRequestProtocol implements Protocol<Http11Request> {

    public static final AttachKey<WebSocketRequest> ATTACH_KEY_WS_REQ = AttachKey.valueOf("ws");
    public static final FinishDecoder FINISH_DECODER = new FinishDecoder();
    static final AttachKey<Http11Request> ATTACH_KEY_REQUEST = AttachKey.valueOf("request");
    private static final ThreadLocal<char[]> CHAR_CACHE_LOCAL = new ThreadLocal<char[]>() {
        @Override
        protected char[] initialValue() {
            return new char[1024];
        }
    };
    private static final AttachKey<DecodeChain<Http11Request>> ATTACH_KEY_DECHDE_CHAIN = AttachKey.valueOf("decodeChain");

    private final HttpMethodDecoder httpMethodDecoder = new HttpMethodDecoder();

    @Override
    public Http11Request decode(ByteBuffer buffer, AioSession<Http11Request> session) {
        Attachment attachment = session.getAttachment();
        Http11Request request = attachment.get(ATTACH_KEY_REQUEST);
        char[] cacheChars = CHAR_CACHE_LOCAL.get();
        if (cacheChars.length < buffer.remaining()) {
            cacheChars = new char[buffer.remaining()];
            CHAR_CACHE_LOCAL.set(cacheChars);
        }
        DecodeChain<Http11Request> decodeChain = attachment.get(ATTACH_KEY_DECHDE_CHAIN);
        if (decodeChain == null) {
            decodeChain = httpMethodDecoder;
        }

        decodeChain = decodeChain.deocde(buffer, cacheChars, session, request);

        if (decodeChain == FINISH_DECODER) {
            attachment.remove(ATTACH_KEY_DECHDE_CHAIN);
            return request;
        }
        attachment.put(ATTACH_KEY_DECHDE_CHAIN, decodeChain);
        if (buffer.remaining() == buffer.capacity()) {
            throw new RuntimeException("buffer is too small when decode " + decodeChain.getClass().getName() + " ," + request);
        }
        return null;
    }
}

