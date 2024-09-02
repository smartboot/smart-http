/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: FixedLengthFrameDecoder.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.decode;

import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.common.multipart.MultipartConfig;
import org.smartboot.http.common.multipart.PartImpl;
import org.smartboot.http.common.utils.ByteTree;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.HttpServerConfiguration;
import org.smartboot.http.server.ServerHandler;
import org.smartboot.http.server.impl.HttpRequestProtocol;
import org.smartboot.http.server.impl.Request;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * 指定长度的解码器
 *
 * @author 三刀
 * @version V1.0 , 2017/10/20
 */
public class MultipartFormDecoder extends AbstractDecoder {
    private final LfDecoder lfDecoder;
    private final byte[] boundary;

    private PartImpl currentPart;
    private final MultipartConfig multipartConfig;
    private long writeFileSize;
    private final Decoder endDecoder = new Decoder() {
        @Override
        public Decoder decode(ByteBuffer byteBuffer, Request request) {
            if (byteBuffer.remaining() < 2) {
                return this;
            }
            if (byteBuffer.get() == Constant.CR && byteBuffer.get() == Constant.LF) {
                request.multipartParsed();
                return HttpRequestProtocol.BODY_READY_DECODER;
            }
            throw new HttpException(HttpStatus.BAD_REQUEST);
        }
    };

    public MultipartFormDecoder(Request request, MultipartConfig configElement) {
        super(request.getConfiguration());
        this.boundary = ("--" + request.getContentType().substring(request.getContentType().indexOf("boundary=") + 9)).getBytes();
        MultipartHeaderDecoder multipartHeaderDecoder = new MultipartHeaderDecoder(request.getConfiguration());
        lfDecoder = new LfDecoder(multipartHeaderDecoder, multipartHeaderDecoder.getConfiguration());
        this.multipartConfig = configElement;
    }

    @Override
    protected Decoder decode0(ByteBuffer byteBuffer, Request request) {
        if (byteBuffer.remaining() < boundary.length + 2) {
            return this;
        }
        for (byte b : boundary) {
            if (byteBuffer.get() != b) {
                throw new HttpException(HttpStatus.BAD_REQUEST);
            }
        }
        byte b = byteBuffer.get();
        if (b == '-' && byteBuffer.get() == '-') {
            return endDecoder.decode(byteBuffer, request);
        } else {
            currentPart = new PartImpl(multipartConfig);
            request.setPart(currentPart);
            return lfDecoder.decode0(byteBuffer, request);
        }
    }

    public class MultipartHeaderDecoder extends AbstractDecoder {
        private final LfDecoder lfDecoder = new LfDecoder(MultipartHeaderDecoder.this, getConfiguration());
        private final HeaderValueDecoder headerValueDecoder = new HeaderValueDecoder(lfDecoder);
        private final ContentDispositionDecoder contentDispositionDecoder = new ContentDispositionDecoder(lfDecoder);
        private final MultipartFieldValueDecoder fieldValueDecoder;
        private final MultipartFileDecoder fileDecoder;

        public MultipartHeaderDecoder(HttpServerConfiguration configuration) {
            super(configuration);
            fieldValueDecoder = new MultipartFieldValueDecoder();
            fileDecoder = new MultipartFileDecoder();
        }

        @Override
        protected Decoder decode0(ByteBuffer byteBuffer, Request request) {
            if (byteBuffer.remaining() < 2) {
                return this;
            }
            //header解码结束
            byteBuffer.mark();
            if (byteBuffer.get() == Constant.CR) {
                if (byteBuffer.get() != Constant.LF) {
                    throw new HttpException(HttpStatus.BAD_REQUEST);
                }
                //区分文件和普通字段
                if (currentPart.getSubmittedFileName() == null) {
                    return fieldValueDecoder.decode(byteBuffer, request);
                } else {
                    return fileDecoder.decode(byteBuffer, request);
                }

            }
            byteBuffer.reset();
            //Header name解码
            ByteTree<Function<String, ServerHandler<?, ?>>> name = StringUtils.scanByteTree(byteBuffer, COLON_END_MATCHER, getConfiguration().getHeaderNameByteTree());
            if (name == null) {
                return this;
            }
            System.out.println("headerName: " + name.getStringValue());
            currentPart.setHeaderTemp(name.getStringValue());
            if (HeaderNameEnum.CONTENT_DISPOSITION.getName().equals(name.getStringValue())) {
                return contentDispositionDecoder.decode(byteBuffer, request);
            }
            return headerValueDecoder.decode(byteBuffer, request);
        }


    }

    /**
     * Value值解码
     */
    class HeaderValueDecoder implements Decoder {
        private final Decoder nextDecoder;

        public HeaderValueDecoder(Decoder nextDecoder) {
            this.nextDecoder = nextDecoder;
        }

        @Override
        public Decoder decode(ByteBuffer byteBuffer, Request request) {
            ByteTree<?> value = StringUtils.scanByteTree(byteBuffer, CR_END_MATCHER, getConfiguration().getByteCache());
            if (value == null) {
                if (byteBuffer.remaining() == byteBuffer.capacity()) {
                    throw new HttpException(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);
                }
                return this;
            }
            currentPart.setHeadValue(value.getStringValue());
            return nextDecoder.decode(byteBuffer, request);
        }
    }

    /**
     * Value值解码
     */
    class ContentDispositionDecoder implements Decoder {
        private final Decoder nextDecoder;

        public ContentDispositionDecoder(Decoder nextDecoder) {
            this.nextDecoder = nextDecoder;
        }

        @Override
        public Decoder decode(ByteBuffer byteBuffer, Request request) {
            ByteTree<?> value = StringUtils.scanByteTree(byteBuffer, CR_END_MATCHER, getConfiguration().getByteCache());
            if (value == null) {
                if (byteBuffer.remaining() == byteBuffer.capacity()) {
                    throw new HttpException(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);
                }
                return this;
            }
            currentPart.setHeadValue(value.getStringValue());
            for (String partVal : value.getStringValue().split(";")) {
                partVal = partVal.trim();
                if (StringUtils.startsWith(partVal, "filename")) {
                    if (partVal.charAt(8) == '=') {
                        currentPart.setFileName(StringUtils.substring(partVal, 10, partVal.length() - 1));
                    } else if (partVal.charAt(8) == '*' && partVal.charAt(9) == '=') {
                        int characterSetIndex = partVal.indexOf('\'', 10);
                        int languageIndex = partVal.indexOf('\'', characterSetIndex + 1);
                        String characterSet = partVal.substring(10, characterSetIndex);
                        try {
                            String fileNameURLEncoded = partVal.substring(languageIndex + 1);
                            currentPart.setFileName(URLDecoder.decode(fileNameURLEncoded, characterSet));
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        throw new HttpException(HttpStatus.BAD_REQUEST);
                    }
                } else if (StringUtils.startsWith(partVal, "name")) {
                    currentPart.setName(StringUtils.substring(partVal, partVal.indexOf("=\"") + 2, partVal.length() - 1));
                }
            }
            return nextDecoder.decode(byteBuffer, request);
        }
    }

    public class MultipartFieldValueDecoder implements Decoder {
        private final LfDecoder lfDecoder = new LfDecoder(MultipartFormDecoder.this, getConfiguration());

        @Override
        public Decoder decode(ByteBuffer byteBuffer, Request request) {
            // 判断是否是结束标记
            byteBuffer.mark();
            boolean match = true;
            while (byteBuffer.hasRemaining()) {
                match = true;
                for (int i = 0; i < boundary.length; i++) {
                    byte b = byteBuffer.get();
                    if (boundary[i] != b) {
                        match = false;
                        if (i > 0) {
                            byteBuffer.position(byteBuffer.position() - i);
                        }
                        break;
                    }
                }
                //完成匹配，跳出
                if (match) {
                    break;
                }
            }
            if (!match) {
                byteBuffer.reset();
                if (byteBuffer.remaining() == byteBuffer.capacity()) {
                    throw new HttpException(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);
                }
                return this;
            }
            int position = byteBuffer.position();
            byteBuffer.reset();
            byte[] bytes = new byte[position - byteBuffer.position() - boundary.length - 2];
            byteBuffer.get(bytes);
            currentPart.setInputStream(new ByteArrayInputStream(bytes));
            currentPart.setFormSize(bytes.length);
            currentPart = null;
            if (byteBuffer.get() != Constant.CR) {
                throw new HttpException(HttpStatus.BAD_REQUEST);
            }
            return lfDecoder.decode(byteBuffer, request);
        }


    }

    public class MultipartFileDecoder implements Decoder {
        private final LfDecoder lfDecoder = new LfDecoder(MultipartFormDecoder.this, getConfiguration());


        @Override
        public Decoder decode(ByteBuffer byteBuffer, Request request) {
            if (byteBuffer.remaining() < boundary.length + 2) {
                return this;
            }

            // 判断是否是结束标记
            byteBuffer.mark();
            boolean match = true;
            while (byteBuffer.hasRemaining()) {
                match = true;
                for (int i = 0; i < boundary.length; i++) {
                    byte b = byteBuffer.get();
                    if (boundary[i] != b) {
                        match = false;
                        if (i > 0) {
                            byteBuffer.position(byteBuffer.position() - i);
                        }
                        break;
                    }
                }
                //完成匹配，跳出
                if (match) {
                    break;
                }
            }
            int position = byteBuffer.position();
            byteBuffer.reset();
            byte[] bytes = new byte[position - byteBuffer.position() - boundary.length - 2];
            byteBuffer.get(bytes);
            if (multipartConfig.getMaxFileSize() > 0) {
                writeFileSize += bytes.length;
                if (writeFileSize > multipartConfig.getMaxFileSize()) {
                    throw new HttpException(HttpStatus.PAYLOAD_TOO_LARGE);
                }
            }
            try {
                currentPart.getDiskOutputStream().write(bytes);
                if (match) {
                    if (byteBuffer.get() != Constant.CR) {
                        throw new RuntimeException();
                    }
                    currentPart.getDiskOutputStream().flush();
                    currentPart.getDiskOutputStream().close();

                    currentPart = null;
                    return lfDecoder.decode(byteBuffer, request);
                } else {
                    return this;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
