package org.smartboot.http.restful.fileupload;

import java.io.InputStream;

public class MultipartFile {
    private final String fileName;

    private final InputStream inputStream;

    public MultipartFile(String fileName, InputStream inputStream) {
        this.fileName = fileName;
        this.inputStream = inputStream;
    }

    public String getFileName() {
        return fileName;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
