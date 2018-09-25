package org.smartboot.http.server.decode;

import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.enums.MethodEnum;
import org.smartboot.http.enums.State;
import org.smartboot.http.utils.Consts;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.extension.decoder.DelimiterFrameDecoder;
import org.smartboot.socket.extension.decoder.FixedLengthFrameDecoder;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.util.BufferUtils;
import org.smartboot.socket.util.DecoderException;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2018/8/31
 */
public class HttpRequestProtocol implements Protocol<Http11Request> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestProtocol.class);
    private static final ThreadLocal<byte[]> BYTE_LOCAL = new ThreadLocal<byte[]>() {
        @Override
        protected byte[] initialValue() {
            return new byte[1024];
        }
    };


    @Override
    public Http11Request decode(ByteBuffer buffer, AioSession<Http11Request> session) {
        Http11Request entityV2 = session.getAttachment();
        byte[] b = BYTE_LOCAL.get();
        if (b.length < buffer.remaining()) {
            b = new byte[buffer.remaining()];
        }
        buffer.mark();
        State curState = entityV2.state;
        boolean flag = false;
        do {
            flag = false;
            switch (curState) {
                case method:
                    int methodLength = scanUntil(buffer, Consts.SP, b);
                    if (methodLength > 0) {
                        curState = State.uri;
                        entityV2.methodEnum = MethodEnum.getByMethod(b, 0, methodLength);
                        if (entityV2.methodEnum == null) {
                            byte[] b1 = new byte[buffer.remaining()];
                            buffer.get(b1);
                            LOGGER.info(new String(b1));
                            throw new DecoderException(new String(b, 0, methodLength));
                        }
                    } else {
                        break;
                    }
                case uri:
                    int uriLength = scanUntil(buffer, Consts.SP, b);
                    if (uriLength > 0) {
                        curState = State.protocol;
                        entityV2.originalUri = convertToString(b, uriLength);
                    } else {
                        break;
                    }
                case protocol:
                    int protocolLength = scanUntil(buffer, Consts.CR, b);
                    if (protocolLength > 0) {
                        entityV2.protocol = convertToString(b, protocolLength);
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
                            curState = State.head_name;
                        }
                    } else {
                        break;
                    }
                case head_name:
                    int nameLength = scanUntil(buffer, Consts.COLON, b);
                    if (nameLength > 0) {
                        curState = State.head_value;
                        entityV2.tmpHeaderName = convertToString(b, nameLength);
                    } else {
                        break;
                    }
                case head_value:
                    if (entityV2.tmpValEnable) {
                        if (entityV2.tmpHeaderValue.decode(buffer)) {
                            curState = State.head_line_LF;
                            ByteBuffer valBuffer = entityV2.tmpHeaderValue.getBuffer();
                            BufferUtils.trim(entityV2.tmpHeaderValue.getBuffer());
                            byte[] valBytes = new byte[valBuffer.remaining()];
                            valBuffer.get(valBytes);
                            entityV2.headMap.put(entityV2.tmpHeaderName, convertToString(valBytes, valBytes.length));
                            entityV2.tmpHeaderValue.reset();
                        } else {
                            break;
                        }
                    } else {
                        int valueLength = scanUntil(buffer, Consts.CR, b);
                        if (valueLength > 0) {
                            curState = State.head_line_LF;
                            entityV2.headMap.put(entityV2.tmpHeaderName, convertToString(b, valueLength));
                        }
                        //value字段长度超过readBuffer空间大小
                        else if (buffer.remaining() == buffer.capacity()) {
                            entityV2.tmpValEnable = true;
                            entityV2.tmpHeaderValue.decode(buffer);
                            break;
                        } else {
                            break;
                        }
                    }
                case head_line_LF:
                    if (buffer.remaining() >= 2) {
                        if (buffer.get() != Consts.LF) {
                            throw new RuntimeException("");
                        }
                        if (buffer.get(buffer.position()) == Consts.CR) {
                            curState = State.head_line_end;
                        } else {
                            curState = State.head_name;
                            flag = true;
                            buffer.mark();
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
                        entityV2.setContentLength(NumberUtils.toInt(entityV2.getHeader(HttpHeaderConstant.Names.CONTENT_LENGTH), -1));
                        //文件上传
                        if (HttpHeaderConstant.Values.MULTIPART_FORM_DATA.equals(entityV2.getContentType())) {
                            try {
                                entityV2.setInputStream(session.getInputStream(entityV2.getContentLength()));
                            } catch (IOException e) {
                                throw new DecoderException("session.getInputStream exception,", e);
                            }
                            curState = State.finished;
                            break;
                        } else {
                            entityV2.bodyContentDecoder = entityV2.getContentLength() > 0 ? new FixedLengthFrameDecoder(entityV2.getContentLength()) : new DelimiterFrameDecoder(Consts.CRLF, 64);
                            curState = State.body;
                        }
                    } else {
                        curState = State.finished;
                        break;
                    }
                case body:
                    if (entityV2.bodyContentDecoder.decode(buffer)) {
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
            return entityV2;
        }
        LOGGER.debug("continue");
        entityV2.state = curState;
        if (buffer.remaining() == buffer.capacity()) {
            LOGGER.error("throw exception");
            throw new DecoderException("buffer is too small when decode " + curState + " ," + entityV2.tmpHeaderName);
        }
        return null;
    }

    @Override
    public ByteBuffer encode(Http11Request msg, AioSession<Http11Request> session) {
        return null;
    }


    private String convertToString(byte[] bytes, int length) {
        int offset = 0;
        while (offset < length) {
            if (bytes[offset] != Consts.SP) {
                break;
            }
            offset++;
        }
        length -= offset;
        if (length == 0) {
            return "";
        }
        int endIndex = offset + length - 1;
        while (endIndex >= offset) {
            if (bytes[endIndex] != Consts.SP) {
                break;
            }
            endIndex--;
        }
        return endIndex < offset ? "" : new String(bytes, offset, endIndex - offset + 1);
    }

    private int scanUntil(ByteBuffer buffer, byte split, byte[] bytes) {
        int avail = buffer.remaining();
        for (int i = 0; i < avail; ) {
            bytes[i] = buffer.get();
            if (bytes[i] == split) {
                buffer.mark();
                return i;
            }
            i++;
        }
        buffer.reset();
        return 0;
    }
}
