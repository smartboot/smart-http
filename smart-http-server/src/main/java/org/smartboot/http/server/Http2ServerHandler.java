/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpServerHandle.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.enums.HttpTypeEnum;
import org.smartboot.http.server.h2.codec.DataFrame;
import org.smartboot.http.server.h2.codec.HeadersFrame;
import org.smartboot.http.server.h2.codec.Http2Frame;
import org.smartboot.http.server.h2.codec.SettingsFrame;
import org.smartboot.http.server.h2.codec.WindowUpdateFrame;
import org.smartboot.http.server.impl.AbstractResponse;
import org.smartboot.http.server.impl.Http2RequestImpl;
import org.smartboot.http.server.impl.Http2Session;
import org.smartboot.http.server.impl.HttpRequestImpl;
import org.smartboot.http.server.impl.Request;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Http消息处理器
 *
 * @author 三刀
 * @version V1.0 , 2018/2/6
 */
public class Http2ServerHandler implements ServerHandler<HttpRequest, HttpResponse> {
    private static final byte[] H2C_PREFACE = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".getBytes();
    private static final int FRAME_HEADER_SIZE = 9;
    private final HttpServerHandler httpServerHandler;

    public Http2ServerHandler(HttpServerHandler httpServerHandler) {
        this.httpServerHandler = httpServerHandler;
    }

    @Override
    public void onHeaderComplete(Request request) throws IOException {
        if (request.getRequestType() == HttpTypeEnum.HTTP_2) {
            if (!"PRI".equals(request.getMethod()) || !"*".equals(request.getUri()) || request.getHeaderSize() > 0) {
                throw new IllegalStateException();
            }
            Http2Session session = request.newHttp2Session();
            session.setState(Http2Session.STATE_PREFACE_SM);
        } else {
            //解析 Header 中的 setting
            String http2Settings = request.getHeader(HeaderNameEnum.HTTP2_SETTINGS.getName());
            byte[] bytes = Base64.getUrlDecoder().decode(http2Settings);
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            SettingsFrame settingsFrame = new SettingsFrame(0, 0, bytes.length);
            settingsFrame.decode(byteBuffer);
            Http2Session session = request.newHttp2Session();
            session.setState(Http2Session.STATE_PREFACE);
            //更新服务端的 setting
            session.updateSettings(settingsFrame);

            HttpRequestImpl req = request.newHttpRequest();
            AbstractResponse response = req.getResponse();
            response.setHttpStatus(HttpStatus.SWITCHING_PROTOCOLS);
            response.setContentType(null);
            response.setHeader(HeaderNameEnum.UPGRADE.getName(), HeaderValueEnum.H2C.getName());
            response.setHeader(HeaderNameEnum.CONNECTION.getName(), HeaderValueEnum.UPGRADE.getName());
            OutputStream outputStream = response.getOutputStream();
            outputStream.flush();

        }
        request.setServerHandler(new ServerHandler<HttpRequest, HttpResponse>() {
            @Override
            public void onBodyStream(ByteBuffer buffer, Request request) {
                Http2ServerHandler.this.onBodyStream(buffer, request);
            }

            @Override
            public void handle(HttpRequest request, HttpResponse response, CompletableFuture<Object> completableFuture) throws Throwable {
                CompletableFuture<Object> future = new CompletableFuture<>();
                httpServerHandler.handle(request, response, future);
            }
        });
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, CompletableFuture<Object> completableFuture) throws Throwable {
        httpServerHandler.handle(request, response, completableFuture);
        throw new IllegalStateException();
    }

    @Override
    public void onBodyStream(ByteBuffer buffer, Request request) {
        Http2Session req = request.newHttp2Session();
        switch (req.getState()) {
            case Http2Session.STATE_FIRST_REQUEST: {
                HttpRequestImpl httpRequest = request.newHttpRequest();
                request.setType(HttpTypeEnum.HTTP_2);
                httpServerHandler.onBodyStream(buffer, request);
                return;
            }
            case Http2Session.STATE_PREFACE_SM: {
                if (buffer.remaining() < 6) {
                    return;
                }
                for (int i = H2C_PREFACE.length - 6; i < H2C_PREFACE.length; i++) {
                    if (H2C_PREFACE[i] != buffer.get()) {
                        throw new IllegalStateException();
                    }
                }
                req.setPrefaced(true);
                req.setState(Http2Session.STATE_FRAME_HEAD);
                onBodyStream(buffer, request);
                return;
            }
            case Http2Session.STATE_PREFACE: {
                if (buffer.remaining() < H2C_PREFACE.length) {
                    break;
                }
                for (byte b : H2C_PREFACE) {
                    if (b != buffer.get()) {
                        throw new IllegalStateException();
                    }
                }
                req.setPrefaced(true);
                req.setState(Http2Session.STATE_FRAME_HEAD);
            }
            case Http2Session.STATE_FRAME_HEAD: {
                if (buffer.remaining() < FRAME_HEADER_SIZE) {
                    break;
                }
                Http2Frame frame = parseFrame(buffer);
                req.setCurrentFrame(frame);
                req.setState(Http2Session.STATE_FRAME_PAYLOAD);
            }
            case Http2Session.STATE_FRAME_PAYLOAD: {
                Http2Frame frame = req.getCurrentFrame();
                if (!frame.decode(buffer)) {
                    break;
                }
                req.setState(Http2Session.STATE_FRAME_HEAD);
                req.setCurrentFrame(null);
                try {
                    doHandler(frame, request);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                onBodyStream(buffer, request);
            }
        }
    }

    private void doHandler(Http2Frame frame, Request req) throws IOException {
        Http2Session session = req.newHttp2Session();
        switch (frame.type()) {
            case Http2Frame.FRAME_TYPE_SETTINGS: {
                SettingsFrame settingsFrame = (SettingsFrame) frame;
                if (settingsFrame.getFlag(SettingsFrame.ACK)) {
                    SettingsFrame settingAckFrame = new SettingsFrame(settingsFrame.streamId(), SettingsFrame.ACK, 0);
                    settingAckFrame.writeTo(req.getAioSession().writeBuffer());
                    req.getAioSession().writeBuffer().flush();
                    System.err.println("Setting ACK报文已发送");
//                    req.newHttp2Session().setState(Http2Session.STATE_FRAME_HEAD);
                } else {
                    System.out.println("settingsFrame:" + settingsFrame);
                    settingsFrame.writeTo(req.getAioSession().writeBuffer());
                    req.getAioSession().writeBuffer().flush();
                    System.err.println("Setting报文已发送");
                }
            }
            break;
            case Http2Frame.FRAME_TYPE_WINDOW_UPDATE: {
                WindowUpdateFrame windowUpdateFrame = (WindowUpdateFrame) frame;
                System.out.println(windowUpdateFrame.getUpdate());
                SettingsFrame ackFrame = new SettingsFrame(windowUpdateFrame.streamId(), SettingsFrame.ACK, 0);
                ackFrame.writeTo(req.getAioSession().writeBuffer());
            }
            break;
            case Http2Frame.FRAME_TYPE_HEADERS: {
                HeadersFrame headersFrame = (HeadersFrame) frame;
                Http2RequestImpl request = session.getStream(headersFrame.streamId());
                request.checkState(Http2RequestImpl.STATE_HEADER_FRAME);
                Map<String, HeaderValue> headers = request.getHeaders();
                headersFrame.getHeaders().forEach(h -> headers.put(h.getName(), h));
                if (headersFrame.getFlag(Http2Frame.FLAG_END_HEADERS)) {
                    request.setState(Http2RequestImpl.STATE_DATA_FRAME);
                }
                break;
            }
            case Http2Frame.FRAME_TYPE_DATA: {
                DataFrame dataFrame = (DataFrame) frame;
                System.out.println("dataFrame:" + dataFrame);
                Http2RequestImpl request = session.getStream(dataFrame.streamId());
                request.checkState(Http2RequestImpl.STATE_DATA_FRAME);
                ByteBuffer buffer = dataFrame.getDataBuffer();
                if (request.getReadBuffer() != null) {
                    buffer = ByteBuffer.allocate(request.getReadBuffer().remaining() + dataFrame.getDataBuffer().remaining());
                    buffer.put(request.getReadBuffer());
                    buffer.put(dataFrame.getDataBuffer());
                    buffer.flip();
                }

                if (dataFrame.getFlags() == DataFrame.FLAG_END_STREAM) {
                    System.out.println("END_STREAM");
                    req.getAioSession().close();
                }
            }
            break;
            default:
                throw new IllegalStateException();
        }
    }


    private static Http2Frame parseFrame(ByteBuffer buffer) {
        int first = buffer.getInt();
        int length = first >> 8;
        int type = first & 0x0f;
        int flags = buffer.get();
        int streamId = buffer.getInt();
        if ((streamId & 0x80000000) != 0) {
            throw new IllegalStateException();
        }
        switch (type) {
            case Http2Frame.FRAME_TYPE_HEADERS:
                return new HeadersFrame(streamId, flags, length);
            case Http2Frame.FRAME_TYPE_SETTINGS:
                return new SettingsFrame(streamId, flags, length);
            case Http2Frame.FRAME_TYPE_WINDOW_UPDATE:
                return new WindowUpdateFrame(streamId, flags, length);
            case Http2Frame.FRAME_TYPE_DATA:
                return new DataFrame(streamId, flags, length);
        }
        throw new IllegalStateException("invalid type :" + type);
    }
}
