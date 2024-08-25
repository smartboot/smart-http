package org.smartboot.http.common.multipart;

import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.common.enums.HeaderNameEnum;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PartImpl implements Part {
    private String name;
    private String fileName;
    private InputStream inputStream;
    private OutputStream diskOutputStream;
    private String formData;
    private File diskFile;
    private final List<HeaderValue> headers = new ArrayList<>(8);
    private int headerSize = 0;
    private String headerTemp;
    private String contentType;

    @Override
    public InputStream getInputStream() throws IOException {
        if (fileName == null && inputStream == null) {
            throw new IllegalStateException();
        }
        if (inputStream != null) {
            return inputStream;
        }
        return inputStream = Files.newInputStream(diskFile.toPath());
    }

    @Override
    public String getContentType() {
        if (contentType != null) {
            return contentType;
        }
        contentType = getHeader(HeaderNameEnum.CONTENT_TYPE.getName());
        return contentType;
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
        if (diskFile != null) {
            return diskFile.length();
        } else {
            return formData.getBytes().length;
        }
    }

    @Override
    public void write(String fileName) throws IOException {

    }

    @Override
    public void delete() throws IOException {

    }

    @Override
    public String getHeader(String name) {
        for (int i = 0; i < headerSize; i++) {
            HeaderValue headerValue = headers.get(i);
            if (headerValue.getName().equalsIgnoreCase(name)) {
                return headerValue.getValue();
            }
        }
        return null;
    }

    public void setHeadValue(String value) {
        setHeader(headerTemp, value);
    }

    public void setHeaderTemp(String headerTemp) {
        this.headerTemp = headerTemp;
    }

    public void setHeader(String headerName, String value) {
        if (headerSize < headers.size()) {
            HeaderValue headerValue = headers.get(headerSize);
            headerValue.setName(headerName);
            headerValue.setValue(value);
        } else {
            headers.add(new HeaderValue(headerName, value));
        }
        headerSize++;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        List<String> value = new ArrayList<>(4);
        for (int i = 0; i < headerSize; i++) {
            HeaderValue headerValue = headers.get(i);
            if (headerValue.getName().equalsIgnoreCase(name)) {
                value.add(headerValue.getValue());
            }
        }
        return value;
    }

    @Override
    public Collection<String> getHeaderNames() {
        Set<String> nameSet = new HashSet<>();
        for (int i = 0; i < headerSize; i++) {
            nameSet.add(headers.get(i).getName());
        }
        return nameSet;
    }

    @Override
    public boolean isFormField() {
        return fileName == null;
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
            diskFile = getFile();
            System.out.println("filePath: " + diskFile.getAbsolutePath());
            diskFile.deleteOnExit();
            diskOutputStream = Files.newOutputStream(diskFile.toPath());
        }
        return diskOutputStream;
    }

    private File getFile() throws IOException {
        return File.createTempFile("multipart" + this.hashCode() + "_", fileName);
    }

    public String getFormData() {
        return formData;
    }

    public void setFormData(String formData) {
        this.formData = formData;
    }
}
