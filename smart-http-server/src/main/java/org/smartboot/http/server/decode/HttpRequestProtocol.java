package org.smartboot.http.server.decode;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.enums.MethodEnum;
import org.smartboot.http.enums.State;
import org.smartboot.http.utils.CharsetUtil;
import org.smartboot.http.utils.Consts;
import org.smartboot.http.utils.HeaderNameEnum;
import org.smartboot.http.utils.HttpHeaderConstant;
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
        Http11Request entityV2 = session.getAttachment();
        byte[] b = BYTE_LOCAL.get();
        if (b.length < buffer.remaining()) {
            b = new byte[buffer.remaining()];
            BYTE_LOCAL.set(b);
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
                    if (protocolLength == 0) {
                        break;
                    } else if (protocolLength == 8) {
                        if (b[0] == 'H' && b[1] == 'T' && b[2] == 'T' && b[3] == 'P' && b[4] == '/' && b[6] == '.') {
                            switch (b[5]) {
                                case '0':
                                    if (b[7] == '9') {
                                        entityV2.protocol = "HTTP/0.9";
                                    }
                                    break;
                                case '1':
                                    if (b[7] == '0') {
                                        entityV2.protocol = "HTTP/1.0";
                                    } else if (b[7] == '1') {
                                        entityV2.protocol = "HTTP/1.1";
                                    }
                                    break;
                                case '2':
                                    if (b[7] == '0') {
                                        entityV2.protocol = "HTTP/2.0";
                                    }
                                    break;
                                default:
                                    throw new DecoderException("unsupport");
                            }
                            if (entityV2.protocol == null) {
                                throw new DecoderException("unKnow protocol");
                            }
                            curState = State.request_line_end;
                        }

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
                        if (StringUtils.startsWith(entityV2.getContentType(), HttpHeaderConstant.Values.MULTIPART_FORM_DATA)) {
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

    private String convertToString(byte[] bytes, int length) {
        if (length >= String_CACHE.length) {
            return new String(bytes, 0, length, CharsetUtil.US_ASCII);
        }
        List<StringCache> list = String_CACHE[length];
        for (int i = 0, j = list.size(); i < j; i++) {
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
        for (int i = 0, j = b0.length; i < j; i++) {
            if (b0[i] != b1[i]) {
                return false;
            }
        }
        return true;
    }


    private String getHeaderName(byte[] bytes, int length) {
        for (HeaderNameEnum nameEnum : HttpHeaderConstant.HEADER_NAME_ENUM_LIST) {
            if (nameEnum.equals(bytes, length)) {
                return nameEnum.getName();
            }
        }
        return convertToString(bytes, length);
    }

    private int scanUntil(ByteBuffer buffer, byte split, byte[] bytes) {
        int avail = buffer.remaining();
        for (int i = 0; i < avail; ) {
            bytes[i] = buffer.get();
            if (i == 0 && bytes[i] == Consts.SP) {
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
