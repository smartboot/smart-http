package org.smartboot.http.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.enums.HttpMethodEnum;
import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.enums.State;
import org.smartboot.http.exception.HttpException;
import org.smartboot.http.utils.AttachKey;
import org.smartboot.http.utils.Attachment;
import org.smartboot.http.utils.CharsetUtil;
import org.smartboot.http.utils.Consts;
import org.smartboot.http.utils.DelimiterFrameDecoder;
import org.smartboot.http.utils.FixedLengthFrameDecoder;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.http.utils.HttpVersion;
import org.smartboot.http.utils.SmartDecoder;
import org.smartboot.http.utils.StringUtils;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 三刀
 * @version V1.0 , 2018/8/31
 */
public class HttpRequestProtocol implements Protocol<Http11Request> {

    static final AttachKey<Http11Request> ATTACH_KEY_REQUEST = AttachKey.valueOf("request");
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestProtocol.class);
    private static final ThreadLocal<char[]> CHAR_CACHE_LOCAL = new ThreadLocal<char[]>() {
        @Override
        protected char[] initialValue() {
            return new char[1024];
        }
    };
    private static final char[] SCAN_URI = new char[]{' ', '?'};
    private final List<StringCache>[] String_CACHE_URL = new List[512];
    private final List<StringCache>[] String_CACHE_HEADER_NAME = new List[32];
    private final List<StringCache>[] String_CACHE_HEADER_VALUE = new List[512];

    {
        for (int i = 0; i < String_CACHE_URL.length; i++) {
            String_CACHE_URL[i] = new ArrayList<>(8);
        }
        for (int i = 0; i < String_CACHE_HEADER_NAME.length; i++) {
            String_CACHE_HEADER_NAME[i] = new ArrayList<>(8);
        }
        for (int i = 0; i < String_CACHE_HEADER_VALUE.length; i++) {
            String_CACHE_HEADER_VALUE[i] = new ArrayList<>(8);
        }
    }

    @Override
    public Http11Request decode(ByteBuffer buffer, AioSession<Http11Request> session) {
        Attachment attachment = session.getAttachment();
        Http11Request request = attachment.get(ATTACH_KEY_REQUEST);
        char[] cacheChars = CHAR_CACHE_LOCAL.get();
        if (cacheChars.length < buffer.remaining()) {
            cacheChars = new char[buffer.remaining()];
            CHAR_CACHE_LOCAL.set(cacheChars);
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
                                request.setMethod(HttpMethodEnum.GET);
                            }
                            break;
                        case 'P':
                            buffer.position(mPos + 3);
                            if (buffer.get() == Consts.SP) {
                                request.setMethod(HttpMethodEnum.PUT);
                            } else if (buffer.get() == Consts.SP) {
                                request.setMethod(HttpMethodEnum.POST);
                            }
                            break;
                        case 'H':
                            buffer.position(mPos + 4);
                            if (buffer.get() == Consts.SP) {
                                request.setMethod(HttpMethodEnum.HEAD);
                            }
                            break;
                        case 'D':
                            buffer.position(mPos + 6);
                            if (buffer.get() == Consts.SP) {
                                request.setMethod(HttpMethodEnum.DELETE);
                            }
                            break;
                        case 'C':
                            buffer.position(mPos + 7);
                            if (buffer.get() == Consts.SP) {
                                request.setMethod(HttpMethodEnum.CONNECT);
                            }
                            break;
                        case 'O':
                            buffer.position(mPos + 7);
                            if (buffer.get() == Consts.SP) {
                                request.setMethod(HttpMethodEnum.OPTIONS);
                            }
                            break;
                        case 'T':
                            buffer.position(mPos + 5);
                            if (buffer.get() == Consts.SP) {
                                request.setMethod(HttpMethodEnum.TRACE);
                            }
                            break;
                    }
                    if (request.getMethodEnum() == null) {
                        byte[] b1 = new byte[buffer.remaining()];
                        buffer.get(b1);
                        LOGGER.info(new String(b1));
                        throw new HttpException(HttpStatus.METHOD_NOT_ALLOWED);
                    }
                case uri:
                    int uriLength = scanUntilAndTrim(buffer, SCAN_URI, cacheChars);
                    if (uriLength > 0) {
                        request._originalUri = convertToString(cacheChars, uriLength);
                        if (buffer.get(buffer.position() - 1) == '?') {
                            curState = State.queryString;
                        } else {
                            curState = State.protocol;
                            flag = true;
                            break;
                        }
                    } else {
                        break;
                    }
                case queryString:
                    int queryLength = scanUntil(buffer, Consts.SP, cacheChars);
                    if (queryLength == 0) {
                        curState = State.protocol;
                    } else if (queryLength > 0) {
                        request.setQueryString(convertToString(cacheChars, queryLength));
                        curState = State.protocol;
                    } else {
                        break;
                    }
                case protocol:
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
                            throw new HttpException(HttpStatus.HTTP_VERSION_NOT_SUPPORTED);
                        }
                        curState = State.request_line_end;
                        buffer.position(pos + 9);
                    } else {
                        throw new HttpException(HttpStatus.HTTP_VERSION_NOT_SUPPORTED);
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
                    int nameLength = scanUntilAndTrim(buffer, Consts.COLON, cacheChars);
                    if (nameLength > 0) {
                        curState = State.head_value;
                        request.tmpHeaderName = convertToString(cacheChars, nameLength, String_CACHE_HEADER_NAME);
                    } else {
                        break;
                    }
                case head_value:
                    if (request.headValueDecoderEnable) {
                        DelimiterFrameDecoder valueDecoder = request.getHeaderValueDecoder();
                        if (valueDecoder.decode(buffer)) {
                            curState = State.head_line_LF;
                            ByteBuffer valBuffer = valueDecoder.getBuffer();
                            trim(valueDecoder.getBuffer());
                            byte[] valBytes = new byte[valBuffer.remaining()];
                            valBuffer.get(valBytes);
//                            request.setHeader(request.tmpHeaderName, convertToString(valBytes, valBytes.length, String_CACHE_HEADER_VALUE));
                            request.setHeader(request.tmpHeaderName, new String(valBytes, CharsetUtil.US_ASCII));
                            valueDecoder.reset();
                        } else {
                            break;
                        }
                    } else {
                        int valueLength = scanUntilAndTrim(buffer, Consts.CR, cacheChars);
                        if (valueLength > 0) {
                            curState = State.head_line_LF;
                            request.setHeader(request.tmpHeaderName, convertToString(cacheChars, valueLength, String_CACHE_HEADER_VALUE));
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
                    if (HttpMethodEnum.POST == request.getMethodEnum()
                            && StringUtils.startsWith(request.getContentType(), HttpHeaderConstant.Values.X_WWW_FORM_URLENCODED)) {
                        int postLength = request.getContentLength();
                        if (postLength > Consts.maxPostSize) {
                            throw new HttpException(HttpStatus.PAYLOAD_TOO_LARGE);
                        } else if (postLength <= 0) {
                            throw new HttpException(HttpStatus.LENGTH_REQUIRED);
                        }
                        attachment.put(Consts.ATTACH_KEY_FIX_LENGTH_DECODER, new FixedLengthFrameDecoder(request.getContentLength()));
                        curState = State.body;
                    } else {
                        curState = State.finished;
                        break;
                    }
                case body:
                    SmartDecoder smartDecoder = attachment.get(Consts.ATTACH_KEY_FIX_LENGTH_DECODER);
                    if (smartDecoder.decode(buffer)) {
                        request.setPostData(smartDecoder.getBuffer().array());
                        attachment.remove(Consts.ATTACH_KEY_FIX_LENGTH_DECODER);
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
            throw new RuntimeException("buffer is too small when decode " + curState + " ," + request.tmpHeaderName);
        }
        return null;
    }

    private String convertToString(char[] bytes, int length) {
        return convertToString(bytes, length, String_CACHE_URL);
    }

    private String convertToString(char[] bytes, int length, List<StringCache>[] cacheList) {
        if (cacheList == null) {
            cacheList = String_CACHE_URL;
        }
        if (length >= cacheList.length) {
            return new String(bytes, 0, length);
        }
        List<StringCache> list = cacheList[length];
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
            String str = new String(bytes, 0, length);
            list.add(new StringCache(str.toCharArray(), str));
            return str;
        }
    }

    private boolean equals(char[] b0, char[] b1) {
        for (int i = b0.length - 1; i > 0; i--) {
            if (b0[i] != b1[i]) {
                return false;
            }
        }
        return b0[0] == b1[0];
    }


    private int scanUntil(ByteBuffer buffer, byte split, char[] bytes) {
        int avail = buffer.remaining();
        for (int i = 0; i < avail; ) {
            bytes[i] = (char) (buffer.get() & 0xFF);
            if (bytes[i] == split) {
                buffer.mark();
                return i;
            }
            i++;
        }
        buffer.reset();
        return -1;
    }

    private int scanUntilAndTrim(ByteBuffer buffer, byte split, char[] bytes) {
        int avail = buffer.remaining();
        byte firtByte = 0;
        while ((firtByte = buffer.get()) == Consts.SP && --avail > 0) ;
        bytes[0] = (char) (firtByte & 0xFF);

        for (int i = 1; i < avail; ) {
            bytes[i] = (char) (buffer.get() & 0xFF);
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

    private int scanUntilAndTrim(ByteBuffer buffer, char[] splits, char[] cacheChars) {
        int avail = buffer.remaining();
        byte firstByte = 0;
        while ((firstByte = buffer.get()) == Consts.SP && --avail > 0) ;
        cacheChars[0] = (char) (firstByte & 0xFF);
        for (int i = 1; i < avail; ) {
            cacheChars[i] = (char) (buffer.get() & 0xFF);
            for (char split : splits) {
                if (cacheChars[i] == split) {
                    buffer.mark();
                    //反向去空格
                    while (cacheChars[i - 1] == Consts.SP) {
                        i--;
                    }
                    return i;
                }
            }
            i++;
        }
        buffer.reset();
        return 0;
    }

    private void trim(ByteBuffer buffer) {
        int pos = buffer.position();
        int limit = buffer.limit();

        while (pos < limit) {
            byte b = buffer.get(pos);
            if (b != Consts.SP && b != Consts.CR && b != Consts.LF) {
                break;
            }
            pos++;
        }
        buffer.position(pos);

        while (pos < limit) {
            byte b = buffer.get(limit - 1);
            if (b != Consts.SP && b != Consts.CR && b != Consts.LF) {
                break;
            }
            limit--;
        }
        buffer.limit(limit);
    }


    private class StringCache {
        final char[] bytes;
        final String value;

        public StringCache(char[] bytes, String value) {
            this.bytes = bytes;
            this.value = value;
        }
    }

}
