/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpOutputStream.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.server.h2.codec.DataFrame;
import org.smartboot.http.server.h2.codec.HeadersFrame;
import org.smartboot.http.server.h2.codec.Http2Frame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
final class Http2OutputStream extends AbstractOutputStream {
    private final int streamId;

    public Http2OutputStream(int streamId, Request httpRequest, Http2ResponseImpl response) {
        super(httpRequest, response);
        disableChunked();
        this.streamId = streamId;
    }

    protected void writeHeader(HeaderWriteSource source) throws IOException {
        if (committed) {
            if (source == HeaderWriteSource.CLOSE && !closed) {
                DataFrame dataFrame1 = new DataFrame(streamId, DataFrame.FLAG_END_STREAM, 0);
                dataFrame1.writeTo(writeBuffer, new byte[0], 0, 0);
                writeBuffer.flush();
                System.out.println("close..., stream:" + streamId);
            }
            return;
        }
        // Create HEADERS frame
        HeadersFrame headersFrame = new HeadersFrame(request.newHttp2Session(), streamId, Http2Frame.FLAG_END_HEADERS, 0);

        List<HeaderValue> headers = new ArrayList<>();
        headers.add(new HeaderValue(":status", String.valueOf(response.getHttpStatus())));
        response.getHeaders().forEach((k, v) -> headers.add(new HeaderValue(k, v.getValue())));
        headersFrame.setHeaders(headers);
        headersFrame.writeTo(writeBuffer);
        writeBuffer.flush();
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
        System.out.println("write streamId:" + streamId);
        DataFrame dataFrame = new DataFrame(streamId, 0, len);
        dataFrame.writeTo(writeBuffer, b, off, len);
    }

}
