/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RequestLineDecoder.java
 * Date: 2020-03-30
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.decode;

import org.smartboot.http.server.Http11Request;
import org.smartboot.http.server.HttpRequestProtocol;
import org.smartboot.http.utils.Attachment;
import org.smartboot.http.utils.Consts;
import org.smartboot.http.utils.SmartDecoder;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/30
 */
public class HttpBodyDecoder implements DecodeChain<Http11Request> {

    @Override
    public DecodeChain<Http11Request> deocde(ByteBuffer byteBuffer, char[] cacheChars, AioSession<Http11Request> aioSession, Http11Request request) {
        Attachment attachment = aioSession.getAttachment();
        SmartDecoder smartDecoder = attachment.get(Consts.ATTACH_KEY_FIX_LENGTH_DECODER);
        if (smartDecoder.decode(byteBuffer)) {
            request.setFormUrlencoded(new String(smartDecoder.getBuffer().array()));
            attachment.remove(Consts.ATTACH_KEY_FIX_LENGTH_DECODER);
            return HttpRequestProtocol.FINISH_DECODER;
        } else {
            return this;
        }
    }
}
