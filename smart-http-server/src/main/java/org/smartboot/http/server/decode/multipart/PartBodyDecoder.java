package org.smartboot.http.server.decode.multipart;

import org.smartboot.http.common.Multipart;
import org.smartboot.http.common.Part;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.server.HttpServerConfiguration;
import org.smartboot.http.server.decode.AbstractDecoder;
import org.smartboot.http.server.decode.Decoder;
import org.smartboot.http.server.impl.Request;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: TODO
 * @Author MiSinG
 * @Date 2024/6/26
 * @Version V1.0
 **/
public class PartBodyDecoder extends AbstractDecoder {

    private static volatile PartBodyDecoder INSTANCE;

    public PartBodyDecoder(HttpServerConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected Decoder decode0(ByteBuffer byteBuffer, Request request) {

        Multipart multipart = request.getMultipart();

//        List<Part> parts = request.getParts();
//        Part part = parts.get(parts.size() - 1);
//
//        int i = readBodyData(byteBuffer, multipart, part);
//        if (i == 1) {
//            return BoundaryDecoder.getInstance(getConfiguration()).decode(byteBuffer, request);
//        }
        return this;
    }

    private int readBodyData(ByteBuffer byteBuffer, Multipart multipart, Part part) {

        byte[] separator = multipart.getSeparator();
        int separatorIndex = multipart.getSeparatorIndex();
        int pos = multipart.getCRLFIndex();
        int start = byteBuffer.position();
        boolean isMatch = multipart.isMatch();

        FileOutputStream fos = null;
        if (part.isFile()) {
            fos = initFileOutputStream(part);
            // 设置文件追踪
            multipart.addItemTracker(part);
        }

        try {
            while (byteBuffer.hasRemaining()) {
                byte b = byteBuffer.get();

                // 检测CRLF
                if (b == Constant.CRLF_BYTES[pos]) {
                    pos++;
                    if (!isMatch && pos == Constant.CRLF_BYTES.length) {
                        isMatch = true;
                        pos = 0;
                        multipart.setMatch(true);
                        multipart.setCRLFIndex(0);
                        continue;
                    }
                } else {
                    pos = 0;
                }

                // 匹配边界
                if (isMatch) {
                    //不匹配
                    if (separatorIndex < separator.length && b != separator[separatorIndex]) {
                        isMatch = false;
                        separatorIndex = 0;
                        multipart.setMatch(false);
                    } else {
                        separatorIndex++;
                        if (separatorIndex == separator.length) {
                            handleMatchedBoundary(part, fos, byteBuffer, start, separator.length);
                            multipart.setSeparatorIndex(0);
                            return 1;
                        }
                    }
                }

                //到达buffer阈值
                if (byteBuffer.position() == byteBuffer.limit()) {
                    handleBufferLimitReached(part, fos, byteBuffer, separatorIndex, start);
                    multipart.setSeparatorIndex(separatorIndex);
                    return 0;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }

    private FileOutputStream initFileOutputStream(Part part) {
        File tempFile = part.getTempFile();
        FileOutputStream fos = part.getFos();

        if (tempFile == null || fos == null) {
            try {
                tempFile = File.createTempFile("tempInputStreamRange", null);
                tempFile.deleteOnExit();
                fos = new FileOutputStream(tempFile, true);

                part.setTempFile(tempFile);
                part.setFos(fos);
            } catch (IOException e) {
                throw new RuntimeException("failed to initialize the file");
            }
        }

        return fos;
    }

    private void handleMatchedBoundary(Part part, FileOutputStream fos, ByteBuffer byteBuffer, int start, int separatorLength) throws IOException {
        int writeLength = byteBuffer.position() - separatorLength - 2 - start;

        if (part.isFile() && fos != null) {
            writeToFile(fos, byteBuffer, start, writeLength);
            fos.close();
        } else {
            writeToContent(part, byteBuffer, start, writeLength);
        }
    }

    private void handleBufferLimitReached(Part part, FileOutputStream fos, ByteBuffer byteBuffer, int separatorIndex, int start) throws IOException {
        int writeLength = byteBuffer.limit() - start;

        if (separatorIndex != 0) {
            writeLength -= separatorIndex + 2;
        }

        if (part.isFile() && fos != null) {
            writeToFile(fos, byteBuffer, start, writeLength);
        } else {
            writeToContent(part, byteBuffer, start, writeLength);
        }

    }

    private void writeToFile(FileOutputStream fos, ByteBuffer byteBuffer, int start, int length) throws IOException {
        if (length > 0) {
            fos.write(byteBuffer.array(), start, length);
        }
    }

    private void writeToContent(Part part, ByteBuffer byteBuffer, int start, int length) {
        int cachedContentLength = part.getCachedContentLength();
        byte[] content = part.getCachedContent();

        if (cachedContentLength + length < part.getSizeThreshold()) {
            byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), start, start + length);
            System.arraycopy(bytes, 0, content, cachedContentLength, bytes.length);
            part.setCachedContentLength(cachedContentLength + bytes.length);
        } else {
            throw new RuntimeException("The field content exceeded the threshold");
        }
    }

    public static PartBodyDecoder getInstance(HttpServerConfiguration configuration) {
        if (INSTANCE == null) {
            synchronized (PartBodyDecoder.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PartBodyDecoder(configuration);
                }
            }
        }
        return INSTANCE;
    }
}
