/*
 * Copyright (c) 2018, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: HttpV2Protocol.java
 * Date: 2018-01-23
 * Author: sandao
 */

package org.smartboot.http.common;

import org.smartboot.http.common.enums.MethodEnum;
import org.smartboot.http.common.utils.Consts;
import org.smartboot.http.common.utils.HttpHeaderConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.extension.decoder.FixedLengthFrameDecoder;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * Http消息解析器,仅解析Header部分即可
 * Created by 三刀 on 2017/6/20.
 */
public final class HttpRequestProtocol implements Protocol<HttpEntityV2> {


    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestProtocol.class);

    @Override
    public HttpEntityV2 decode(ByteBuffer buffer, AioSession<HttpEntityV2> session, boolean eof) {
        if (!buffer.hasRemaining() || eof) {
            return null;
        }
        buffer.mark();

        HttpEntityV2 entityV2 = session.getAttachment();
        buffer.position(entityV2.getCurrentPosition());


        State curState = entityV2.state;
        boolean flag = false;
        do {
            flag = false;
            switch (curState) {
                case method:
                    entityV2.initPosition = buffer.position();
                    scanUntil(buffer, Consts.SP, entityV2.methodRange);
                    if (entityV2.methodRange.isOk) {
                        curState = State.uri;
                        entityV2.buffer = buffer;
                    } else {
                        break;
                    }
                case uri:
                    scanUntil(buffer, Consts.SP, entityV2.uriRange);
                    if (entityV2.uriRange.isOk) {
                        curState = State.protocol;
                    } else {
                        break;
                    }
                case protocol:
                    scanUntil(buffer, Consts.CR, entityV2.protocolRange);
                    if (entityV2.protocolRange.isOk) {
                        curState = State.request_line_end;
                    } else {
                        break;
                    }
                case request_line_end:
                    if (buffer.remaining() >= 2) {
                        if (buffer.get() != Consts.LF) {
                            LOGGER.error(buffer.toString());
                            throw new RuntimeException("");
                        }
                        if (buffer.get(buffer.position()) == Consts.CR) {
                            curState = State.head_line_end;
                        } else {
                            curState = State.head_line;
                        }
                    } else {
                        break;
                    }
                case head_line:
                    BufferRange headRange = entityV2.headerRanges.getReadableRange();
                    scanUntil(buffer, Consts.CR, headRange);
                    if (headRange.isOk) {
                        curState = State.head_line_LF;
                    } else {
                        break;
                    }
                case head_line_LF:
                    if (buffer.remaining() >= 2) {
                        if (buffer.get() != Consts.LF) {
                            throw new RuntimeException("");
                        }
                        if (buffer.get(buffer.position()) == Consts.CR) {
                            curState = State.head_line_end;
                        } else {
                            curState = State.head_line;
                            flag = true;
                            break;
                        }
                    } else {
                        break;
                    }
                case head_line_end:
                    if (buffer.remaining() < 2) {
                        break;
                    }
                    if (buffer.get() == Consts.CR && buffer.get() == Consts.LF) {
                        curState = State.head_finished;
                    } else {
                        throw new RuntimeException();
                    }
                case head_finished:
                    //Post请求
                    if (entityV2.getMethodRange() == MethodEnum.POST) {
                        entityV2.decodeHead();
                        buffer.mark();
                        //文件上传
                        if (HttpHeaderConstant.Values.MULTIPART_FORM_DATA.equals(entityV2.getContentType())) {
                            curState = State.finished;
                            break;
                        } else {
                            entityV2.setBodyForm(new FixedLengthFrameDecoder(entityV2.getContentLength()));
                            curState = State.body;
                        }
                    } else {
                        curState = State.finished;
                        break;
                    }
                case body:
                    if (entityV2.getBodyForm().decode(buffer)) {
                        curState = State.finished;
                    }
                    buffer.mark();
                    break;
                case finished:
                    break;
                default:
                    throw new RuntimeException("aa");
            }
        } while (flag);
        if (curState == State.finished) {
            entityV2.setCurrentPosition(buffer.position());
            return entityV2;
        }
        entityV2.setCurrentPosition(buffer.position());
        entityV2.state = curState;
        buffer.reset();
        if (curState != State.body && buffer.limit() == buffer.capacity()) {
            throw new RuntimeException("buffer full");
        }
        return null;
    }

    @Override
    public ByteBuffer encode(HttpEntityV2 httpRequest, AioSession<HttpEntityV2> session) {
        return null;
    }

    private void scanUntil(ByteBuffer buffer, byte split, BufferRange bufferRange) {
        int index = buffer.position();
        bufferRange.start = index;
        int remaining = buffer.remaining();
        byte[] data = buffer.array();
        while (remaining-- > 0) {
            if (data[index] == split) {
                bufferRange.length = index - bufferRange.start;
                bufferRange.isOk = true;
                buffer.position(++index);
                return;
            } else {
                index++;
            }
        }
        bufferRange.start = -1;
    }


}
