/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpServerHandle.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.utils.SmartDecoder;
import org.smartboot.http.server.h2.DataFrame;
import org.smartboot.http.server.h2.Http2Frame;
import org.smartboot.http.server.h2.SettingsFrame;
import org.smartboot.http.server.h2.WindowUpdateFrame;
import org.smartboot.http.server.impl.AbstractResponse;
import org.smartboot.http.server.impl.Http2RequestImpl;
import org.smartboot.http.server.impl.Request;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Http消息处理器
 *
 * @author 三刀
 * @version V1.0 , 2018/2/6
 */
public class Http2ServerHandler implements ServerHandler<HttpRequest, HttpResponse> {
    private final Map<Request, SmartDecoder> bodyDecoderMap = new ConcurrentHashMap<>();
    private static final byte[] H2C_PREFACE = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".getBytes();
    private static final int FRAME_HEADER_SIZE = 9;
    private HttpServerHandler httpServerHandler;

    public Http2ServerHandler(HttpServerHandler httpServerHandler) {
        this.httpServerHandler = httpServerHandler;
    }

    @Override
    public void onHeaderComplete(Request request) throws IOException {
        String http2Settings = request.getHeader(HeaderNameEnum.HTTP2_SETTINGS.getName());
        //还原http2Settings
        byte[] bytes = Base64.getUrlDecoder().decode(http2Settings);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        SettingsFrame settingsFrame = new SettingsFrame(0, 0, bytes.length);
        settingsFrame.decode(byteBuffer);
        System.out.println("http2Settings:" + settingsFrame);

        Http2RequestImpl req = request.newHttp2Request();
        AbstractResponse response = req.getResponse();
        response.setHttpStatus(HttpStatus.SWITCHING_PROTOCOLS);
        response.setHeader(HeaderNameEnum.UPGRADE.getName(), HeaderValueEnum.H2C.getName());
        response.setHeader(HeaderNameEnum.CONNECTION.getName(), HeaderValueEnum.UPGRADE.getName());
        OutputStream outputStream = response.getOutputStream();
        outputStream.flush();
        settingsFrame.writeTo(request.getAioSession().writeBuffer());

        req.setState(Http2RequestImpl.STATE_FIRST_REQUEST);
    }

    @Override
    public void onBodyStream(ByteBuffer buffer, Request request) {
        Http2RequestImpl req = request.newHttp2Request();
        switch (req.getState()) {
            case Http2RequestImpl.STATE_FIRST_REQUEST: {
                
            }
            case Http2RequestImpl.STATE_PREFACE: {
                if (buffer.remaining() < H2C_PREFACE.length) {
                    break;
                }
                for (byte b : H2C_PREFACE) {
                    if (b != buffer.get()) {
                        throw new IllegalStateException();
                    }
                }
                req.setPrefaced(true);
                req.setState(Http2RequestImpl.STATE_FRAME_HEAD);
            }
            case Http2RequestImpl.STATE_FRAME_HEAD: {
                if (buffer.remaining() < FRAME_HEADER_SIZE) {
                    break;
                }
                Http2Frame frame = parseFrame(buffer);
                req.setCurrentFrame(frame);
                req.setState(Http2RequestImpl.STATE_FRAME_PAYLOAD);
            }
            case Http2RequestImpl.STATE_FRAME_PAYLOAD: {
                Http2Frame frame = req.getCurrentFrame();
                if (!frame.decode(buffer)) {
                    break;
                }
                req.setState(Http2RequestImpl.STATE_FRAME_HEAD);
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
        switch (frame.type()) {
            case SettingsFrame.TYPE: {
                SettingsFrame settingsFrame = (SettingsFrame) frame;
                System.out.println("settingsFrame:" + settingsFrame);
                SettingsFrame ackFrame = new SettingsFrame(settingsFrame.streamId(), SettingsFrame.ACK, 0);
//                ackFrame.writeTo(req.getAioSession().writeBuffer());
            }
            break;
            case WindowUpdateFrame.TYPE: {
                WindowUpdateFrame windowUpdateFrame = (WindowUpdateFrame) frame;
                System.out.println(windowUpdateFrame.getUpdate());
                SettingsFrame ackFrame = new SettingsFrame(windowUpdateFrame.streamId(), SettingsFrame.ACK, 0);
                ackFrame.writeTo(req.getAioSession().writeBuffer());
            }
            break;
            case DataFrame.TYPE: {
                DataFrame dataFrame = (DataFrame) frame;
                System.out.println("dataFrame:" + dataFrame);
                if (dataFrame.getFlags() == DataFrame.FLAG_END_STREAM) {
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
            case SettingsFrame.TYPE:
                return new SettingsFrame(streamId, flags, length);
            case WindowUpdateFrame.TYPE:
                return new WindowUpdateFrame(streamId, flags, length);
            case DataFrame.TYPE:
                return new DataFrame(streamId, flags, length);
        }
        throw new IllegalStateException();
    }
}
