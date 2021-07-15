/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: IgnoreHeaderDecoder.java
 * Date: 2021-04-10
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.decode;

import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.server.impl.HttpRequestProtocol;
import org.smartboot.http.server.impl.Request;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/4/10
 */
public class IgnoreHeaderDecoder implements Decoder {

    @Override
    public Decoder decode(ByteBuffer byteBuffer, AioSession aioSession, Request httpHeader) {
        while (byteBuffer.remaining() >= 4) {
            byte b = byteBuffer.get(byteBuffer.position() + 3);
            if (b != Constant.CR && b != Constant.LF) {
                byteBuffer.position(byteBuffer.position() + 4);
//                System.out.println("skip");
                continue;
            }
//            System.out.println("read");
            int index = 0;
            while (byteBuffer.get() == Constant.HEADER_END[index]) {
                if (index == 3) {
                    return HttpRequestProtocol.BODY_STREAM_DECODER;
                } else {
                    index++;
                }
            }
        }
        return this;
    }
}
