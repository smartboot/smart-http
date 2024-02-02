package org.smartboot.http.client;

public class FileMultipart extends Multipart {
    private String contentType;
    private String contentDisposition;

    private String fileName;
    private byte[] bytes;

    @Override
    void write(PostBody post) {

    }
}
