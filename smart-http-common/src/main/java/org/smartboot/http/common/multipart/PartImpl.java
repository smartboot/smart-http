package org.smartboot.http.common.multipart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collection;

public class PartImpl implements Part {
    private String name;
    private String fileName;
    private InputStream inputStream;
    private OutputStream diskOutputStream;

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public String getContentType() {
        return "";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSubmittedFileName() {
        return fileName;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public void write(String fileName) throws IOException {

    }

    @Override
    public void delete() throws IOException {

    }

    @Override
    public String getHeader(String name) {
        return "";
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return null;
    }

    @Override
    public boolean isFormField() {
        return false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public OutputStream getDiskOutputStream() throws IOException {
        if (fileName == null) {
            throw new IllegalStateException();
        }
        if (diskOutputStream == null) {
            File file = File.createTempFile("multipart" + this.hashCode() + "_", fileName);
            System.out.println("filePath: " + file.getAbsolutePath());
            file.deleteOnExit();
            diskOutputStream = Files.newOutputStream(file.toPath());
        }
        return diskOutputStream;
    }
}
