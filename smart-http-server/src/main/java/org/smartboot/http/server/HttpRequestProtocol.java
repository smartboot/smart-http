package org.smartboot.http.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.enums.MethodEnum;
import org.smartboot.http.enums.State;
import org.smartboot.http.utils.CharsetUtil;
import org.smartboot.http.utils.Consts;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.http.utils.HttpVersion;
import org.smartboot.http.utils.NumberUtils;
import org.smartboot.http.utils.StringUtils;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.extension.decoder.DelimiterFrameDecoder;
import org.smartboot.socket.extension.decoder.FixedLengthFrameDecoder;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.util.BufferUtils;
import org.smartboot.socket.util.DecoderException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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

    private final List<StringCache>[] String_CACHE = new List[512];

    {
        for (int i = 0; i < String_CACHE.length; i++) {
            String_CACHE[i] = new ArrayList<>();
        }
    }

    @Override
    public Http11Request decode(ByteBuffer buffer, AioSession<Http11Request> session) {
        Http11Request request = session.getAttachment();
        byte[] b = BYTE_LOCAL.get();
        if (b.length < buffer.remaining()) {
            b = new byte[buffer.remaining()];
            BYTE_LOCAL.set(b);
        }
        buffer.mark();
        State curState = request._state;
        boolean flag;
        do {
            flag = false;
            switch (curState) {
                case method:
                    int mPos = buffer.position();
                    if (buffer.remaining() < 8) {
                        break;
                    }
                    byte firstByte = buffer.get();
                    switch (firstByte) {
                        case 'G':
                            buffer.position(mPos + 3);
                            if (buffer.get() == Consts.SP) {
                                request.setMethod(MethodEnum.GET);
                            }
                            break;
                        case 'P':
                            buffer.position(mPos + 3);
                            if (buffer.get() == Consts.SP) {
                                request.setMethod(MethodEnum.PUT);
                            } else if (buffer.get() == Consts.SP) {
                                request.setMethod(MethodEnum.POST);
                            }
                            break;
                        case 'H':
                            buffer.position(mPos + 4);
                            if (buffer.get() == Consts.SP) {
                                request.setMethod(MethodEnum.HEAD);
                            }
                            break;
                        case 'D':
                            buffer.position(mPos + 6);
                            if (buffer.get() == Consts.SP) {
                                request.setMethod(MethodEnum.DELETE);
                            }
                            break;
                        case 'C':
                            buffer.position(mPos + 7);
                            if (buffer.get() == Consts.SP) {
                                request.setMethod(MethodEnum.CONNECT);
                            }
                            break;
                        case 'O':
                            buffer.position(mPos + 7);
                            if (buffer.get() == Consts.SP) {
                                request.setMethod(MethodEnum.OPTIONS);
                            }
                            break;
                        case 'T':
                            buffer.position(mPos + 5);
                            if (buffer.get() == Consts.SP) {
                                request.setMethod(MethodEnum.TRACE);
                            }
                            break;
                    }
                    if (request.getMethodEnum() == null) {
                        byte[] b1 = new byte[buffer.remaining()];
                        buffer.get(b1);
                        LOGGER.info(new String(b1));
                        throw new DecoderException("invalid method");
                    }
                case uri:
                    int uriLength = scanUntil(buffer, Consts.SP, b);
                    if (uriLength > 0) {
                        curState = State.protocol;
                        request._originalUri = convertToString(b, uriLength);
                    } else {
                        break;
                    }
                case protocol:
//                    int protocolLength = scanUntil(buffer, Consts.CR, b);
                    int pos = buffer.position();
                    if (buffer.remaining() < 9) {
                        break;
                    } else if (buffer.get(pos + 8) == Consts.CR) {
                        byte p5 = buffer.get(pos + 5);
                        byte p7 = buffer.get(pos + 7);
                        if (p5 == '0' && p7 == '9') {
                            request.setProtocol(HttpVersion.HTTP_0_9);
                        } else if (p5 == '1') {
                            if (p7 == '0') {
                                request.setProtocol(HttpVersion.HTTP_1_0);
                            } else if (p7 == '1') {
                                request.setProtocol(HttpVersion.HTTP_1_1);
                            }
                        } else if (p5 == '2') {
                            request.setProtocol(HttpVersion.HTTP_2_0);
                        }
                        if (request.getProtocol() == null) {
                            throw new DecoderException("unKnow protocol");
                        }
                        curState = State.request_line_end;
                        buffer.position(pos + 9);
                    } else {
                        throw new DecoderException("unsupport now");
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
                        request.tmpHeaderName = convertToString(b, nameLength);
                    } else {
                        break;
                    }
                case head_value:
                    if (request.headValueDecoderEnable) {
                        DelimiterFrameDecoder valueDecoder = request.getHeaderValueDecoder();
                        if (valueDecoder.decode(buffer)) {
                            curState = State.head_line_LF;
                            ByteBuffer valBuffer = valueDecoder.getBuffer();
                            BufferUtils.trim(valueDecoder.getBuffer());
                            byte[] valBytes = new byte[valBuffer.remaining()];
                            valBuffer.get(valBytes);
                            request.setHeader(request.tmpHeaderName, convertToString(valBytes, valBytes.length));
                            valueDecoder.reset();
                        } else {
                            break;
                        }
                    } else {
                        int valueLength = scanUntil(buffer, Consts.CR, b);
                        if (valueLength > 0) {
                            curState = State.head_line_LF;
                            request.setHeader(request.tmpHeaderName, convertToString(b, valueLength));
                        }
                        //value字段长度超过readBuffer空间大小
                        else if (buffer.remaining() == buffer.capacity()) {
                            request.headValueDecoderEnable = true;
                            request.getHeaderValueDecoder().decode(buffer);
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
                    if (MethodEnum.POST.getMethod().equals(request.getMethod())) {
                        request.setContentLength(NumberUtils.toInt(request.getHeader(HttpHeaderConstant.Names.CONTENT_LENGTH), -1));
                        //文件上传
                        if (StringUtils.startsWith(request.getContentType(), HttpHeaderConstant.Values.MULTIPART_FORM_DATA)) {
                            try {
                                request.setInputStream(session.getInputStream(request.getContentLength()));
                            } catch (IOException e) {
                                throw new DecoderException("session.getInputStream exception,", e);
                            }
                            curState = State.finished;
                            break;
                        } else {
                            request.bodyContentDecoder = request.getContentLength() > 0 ? new FixedLengthFrameDecoder(request.getContentLength()) : new DelimiterFrameDecoder(Consts.CRLF, 64);
                            curState = State.body;
                        }
                    } else {
                        curState = State.finished;
                        break;
                    }
                case body:
                    if (request.bodyContentDecoder.decode(buffer)) {
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
            return request;
        }
        LOGGER.debug("continue");
        request._state = curState;
        if (buffer.remaining() == buffer.capacity()) {
            LOGGER.error("throw exception");
            throw new DecoderException("buffer is too small when decode " + curState + " ," + request.tmpHeaderName);
        }
        return null;
    }

    private String convertToString(byte[] bytes, int length) {
        if (length >= String_CACHE.length) {
            return new String(bytes, 0, length, CharsetUtil.US_ASCII);
        }
        List<StringCache> list = String_CACHE[length];
        for (int i = list.size() - 1; i > -1; i--) {
            StringCache cache = list.get(i);
            if (equals(cache.bytes, bytes)) {
                return cache.value;
            }
        }
        synchronized (list) {
            for (StringCache cache : list) {
                if (equals(cache.bytes, bytes)) {
                    return cache.value;
                }
            }
            String str = new String(bytes, 0, length, CharsetUtil.US_ASCII);
            byte[] bak = new byte[length];
            System.arraycopy(bytes, 0, bak, 0, bak.length);
            list.add(new StringCache(bak, str));
            return str;
        }
    }

    private boolean equals(byte[] b0, byte[] b1) {
        for (int i = b0.length - 1; i > 0; i--) {
            if (b0[i] != b1[i]) {
                return false;
            }
        }
        return b0[0] == b1[0];
    }

    private int scanUntil(ByteBuffer buffer, byte split, byte[] bytes) {
        int avail = buffer.remaining();
        for (int i = 0; i < avail; ) {
            bytes[i] = buffer.get();
            if (i == 0 && bytes[i] == Consts.SP) {
                avail--;
                continue;
            }
            if (bytes[i] == split) {
                buffer.mark();
                //反向去空格
                while (bytes[i - 1] == Consts.SP) {
                    i--;
                }
                return i;
            }
            i++;
        }
        buffer.reset();
        return 0;
    }

    private class StringCache {
        final byte[] bytes;
        final String value;

        public StringCache(byte[] bytes, String value) {
            this.bytes = bytes;
            this.value = value;
        }
    }

}
