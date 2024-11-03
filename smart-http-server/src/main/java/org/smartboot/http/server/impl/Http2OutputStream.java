/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpOutputStream.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.server.h2.codec.ContinuationFrame;
import org.smartboot.http.server.h2.codec.DataFrame;
import org.smartboot.http.server.h2.codec.HeadersFrame;
import org.smartboot.http.server.h2.codec.Http2Frame;
import org.smartboot.http.server.h2.codec.PushPromiseFrame;
import org.smartboot.http.server.h2.hpack.Encoder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
final class Http2OutputStream extends AbstractOutputStream {
    private final int streamId;
    private final boolean push;
    private final Http2Session http2Session;
    private final int promisedStreamId;

    public Http2OutputStream(int streamId, Http2RequestImpl httpRequest, Http2ResponseImpl response, boolean push) {
        super(httpRequest, response);
        disableChunked();
        this.http2Session = httpRequest.getSession();
        this.streamId = streamId;
        this.push = push;
        if (push) {
            promisedStreamId = http2Session.getPushStreamId().addAndGet(2);
        } else {
            promisedStreamId = 0;
        }
    }

    protected void writeHeader(HeaderWriteSource source) throws IOException {
        if (committed) {
            if (source == HeaderWriteSource.CLOSE && !closed) {
                System.err.println("before close..., stream:" + (push ? promisedStreamId : streamId));
//                writeBuffer.flush();
                DataFrame dataFrame1 = new DataFrame((push ? promisedStreamId : streamId), DataFrame.FLAG_END_STREAM, 0);
                dataFrame1.writeTo(writeBuffer, new byte[0], 0, 0);
//                writeBuffer.flush();
                System.err.println("after close..., stream:" + (push ? promisedStreamId : streamId));
            }
            return;
        }
        //转换Cookie
        convertCookieToHeader();
        // Create HEADERS frame
        if (!push) {
            response.setHeader(":status", String.valueOf(response.getHttpStatus()));
        }


        List<ByteBuffer> buffers = new ArrayList<>();
        Encoder encoder = http2Session.getHpackEncoder();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        for (Map.Entry<String, HeaderValue> entry : response.getHeaders().entrySet()) {
            if (entry.getKey().charAt(0) != ':') {
                continue;
            }
            System.out.println("encode: " + entry.getKey() + ":" + entry.getValue().getValue());
            HeaderValue headerValue = entry.getValue();
            while (headerValue != null) {
                encoder.header(entry.getKey().toLowerCase(), headerValue.getValue());
                while (!encoder.encode(buffer)) {
                    buffer.flip();
                    buffers.add(buffer);
                    buffer = ByteBuffer.allocate(1024);
                }
                headerValue = headerValue.getNextValue();
            }
        }

        for (Map.Entry<String, HeaderValue> entry : response.getHeaders().entrySet()) {
            if (entry.getKey().charAt(0) == ':') {
                continue;
            }
            System.out.println("encode: " + entry.getKey() + ":" + entry.getValue().getValue());
            HeaderValue headerValue = entry.getValue();
            while (headerValue != null) {
                encoder.header(entry.getKey().toLowerCase(), headerValue.getValue());
                while (!encoder.encode(buffer)) {
                    buffer.flip();
                    buffers.add(buffer);
                    buffer = ByteBuffer.allocate(1024);
                }
                headerValue = headerValue.getNextValue();
            }
        }
        buffer.flip();
        if (buffer.hasRemaining()) {
            buffers.add(buffer);
        }

        boolean multipleHeaders = buffers.size() > 1;
        if (push) {
            PushPromiseFrame headersFrame = new PushPromiseFrame(streamId, multipleHeaders ? 0 : Http2Frame.FLAG_END_HEADERS, 0);
            headersFrame.setPromisedStream(promisedStreamId);
            if (!buffers.isEmpty()) {
                headersFrame.setFragment(buffers.get(0));
            }

            headersFrame.writeTo(writeBuffer);
        } else {
            HeadersFrame headersFrame = new HeadersFrame(streamId, multipleHeaders ? 0 : Http2Frame.FLAG_END_HEADERS, 0);
            headersFrame.setFragment(buffers.isEmpty() ? null : buffers.get(0));
            headersFrame.writeTo(writeBuffer);
        }
        for (int i = 1; i < buffers.size() - 1; i++) {
            ContinuationFrame continuationFrame = new ContinuationFrame(streamId, 0, 0);
            continuationFrame.setFragment(buffers.get(i));
            continuationFrame.writeTo(writeBuffer);
        }
        if (multipleHeaders) {
            ContinuationFrame continuationFrame = new ContinuationFrame(streamId, Http2Frame.FLAG_END_HEADERS, 0);
            continuationFrame.setFragment(buffers.get(buffers.size() - 1));
            continuationFrame.writeTo(writeBuffer);
        }
//        writeBuffer.flush();
        System.err.println("StreamID: " + streamId + " Header已发送...");
        committed = true;
    }

    protected byte[] getHeadPart(boolean hasHeader) {
        //编码成http2
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        writeHeader(HeaderWriteSource.WRITE);
        if(len==0){
            return;
        }
        System.out.println("write streamId:" + (push ? promisedStreamId : streamId));
        DataFrame dataFrame = new DataFrame(push ? promisedStreamId : streamId, 0, len);
        dataFrame.writeTo(writeBuffer, b, off, len);
    }

}
