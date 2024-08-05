package org.smartboot.http.common.io;

import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.socket.transport.AioSession;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/12/6
 */
public class ChunkedInputStream extends InputStream {
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(8);
    private boolean readFlag = true;
    private final AioSession session;
    private InputStream inputStream;
    private boolean eof = false;
    private Map<String, String> trailerFields;
    /**
     * 剩余可读字节数
     */
    private int remainingThreshold;
    private final Consumer<Map<String, String>> consumer;
    private String trailerName;

    public ChunkedInputStream(AioSession session, int maxPayload, Consumer<Map<String, String>> consumer) {
        this.session = session;
        this.remainingThreshold = maxPayload;
        this.consumer = consumer;
    }

    @Override
    public int read() throws IOException {
        readChunkedLength();
        if (eof) {
            return -1;
        }
        int i = inputStream.read();
        if (i != -1) {
            return i;
        }
        inputStream.close();
        inputStream = session.getInputStream();
        readCrlf();
        readFlag = true;
        return read();
    }

    @Override
    public int read(byte[] data, int off, int len) throws IOException {
        readChunkedLength();
        if (eof) {
            return -1;
        }
        int i = inputStream.read(data, off, len);
        if (i == -1) {
            inputStream.close();
            inputStream = session.getInputStream();
            readCrlf();
            readFlag = true;
            return read(data, off, len);
        }
        return i;
    }

    private void readChunkedLength() throws IOException {
        while (readFlag) {
            inputStream = session.getInputStream();
            int b = inputStream.read();
            if (b == -1) {
                throw new IOException("inputStream is closed");
            }
            if (b == Constant.LF) {
                int length = Integer.parseInt(buffer.toString(), 16);
                remainingThreshold = remainingThreshold - 2 - buffer.size() - length;
                if (remainingThreshold < 0) {
                    throw new HttpException(HttpStatus.PAYLOAD_TOO_LARGE);
                }
                buffer.reset();
                if (length == 0) {
                    eof = true;
                    //trailerFields
                    parseTrailerFields();
//                    readCrlf();
                    break;
                }
                inputStream.close();
                inputStream = session.getInputStream(length);
                readFlag = false;
            } else if (b != Constant.CR) {
                buffer.write(b);
            }
        }
    }

    private void readCrlf() throws IOException {
        if (inputStream.read() != Constant.CR) {
            throw new HttpException(HttpStatus.BAD_REQUEST);
        }
        if (inputStream.read() != Constant.LF) {
            throw new HttpException(HttpStatus.BAD_REQUEST);
        }
    }

    private void parseTrailerFields() throws IOException {
        while (true) {
            int b = inputStream.read();
            if (b == Constant.LF) {
                if (buffer.size() == 0) {
                    consumer.accept(trailerFields);
                    return;
                }
                trailerFields.put(trailerName, buffer.toString());
                buffer.reset();
            } else if (b == ':') {
                trailerName = buffer.toString();
                buffer.reset();
            } else if (b != Constant.CR) {
                if (trailerFields == null) {
                    trailerFields = new HashMap<>();
                }
                buffer.write(b);
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
            inputStream = null;
        }
    }
}
