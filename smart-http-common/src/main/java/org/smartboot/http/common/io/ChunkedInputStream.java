package org.smartboot.http.common.io;

import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.socket.transport.AioSession;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/12/6
 */
public class ChunkedInputStream extends AbstractInputStream {
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(8);

    private Map<String, String> trailerFields;
    /**
     * 剩余可读字节数
     */
    private int remainingThreshold;
    private final Consumer<Map<String, String>> consumer;
    private String trailerName;
    private int chunkedRemaining;

    public ChunkedInputStream(AioSession session, int maxPayload, Consumer<Map<String, String>> consumer) {
        super(session);
        this.remainingThreshold = maxPayload;
        this.consumer = consumer;
    }

    @Override
    public int read() throws IOException {
        readChunkedLength();
        if (eof) {
            return -1;
        }
        if (chunkedRemaining > 0) {
            chunkedRemaining--;
            return readByte();
        } else {
            readCrlf();
            readFlag = true;
            return read();
        }
    }

    @Override
    public int read(byte[] data, int off, int len) throws IOException {
        readChunkedLength();
        if (eof) {
            return -1;
        }
        if (len == 0) {
            return 0;
        }

        ByteBuffer byteBuffer = session.readBuffer();
        if (chunkedRemaining > 0 && !byteBuffer.hasRemaining()) {
            session.read();
        }
        int readLength = Math.min(len, byteBuffer.remaining());
        readLength = Math.min(readLength, chunkedRemaining);
        byteBuffer.get(data, off, readLength);
        chunkedRemaining = chunkedRemaining - readLength;

        if (chunkedRemaining > 0) {
            return readLength + read(data, off + readLength, len - readLength);
        }
        readCrlf();
        readFlag = true;
        readChunkedLength();
        if (eof) {
            return readLength;
        } else {
            return readLength + read(data, off + readLength, len - readLength);
        }
    }

    private void readChunkedLength() throws IOException {
        while (readFlag) {
            byte b = readByte();
            if (b == Constant.LF) {
                chunkedRemaining = Integer.parseInt(buffer.toString(), 16);
                remainingThreshold = remainingThreshold - 2 - buffer.size() - chunkedRemaining;
                if (remainingThreshold < 0) {
                    throw new HttpException(HttpStatus.PAYLOAD_TOO_LARGE);
                }
                buffer.reset();
                readFlag = false;
                if (chunkedRemaining == 0) {
                    eof = true;
                    //trailerFields
                    parseTrailerFields();
//                    readCrlf();
                    break;
                }
            } else if (b != Constant.CR) {
                buffer.write(b);
            }
        }
    }

    private void readCrlf() throws IOException {
        if (readByte() != Constant.CR) {
            throw new HttpException(HttpStatus.BAD_REQUEST);
        }
        if (readByte() != Constant.LF) {
            throw new HttpException(HttpStatus.BAD_REQUEST);
        }
    }

    private void parseTrailerFields() throws IOException {
        while (true) {
            byte b = readByte();
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


}
