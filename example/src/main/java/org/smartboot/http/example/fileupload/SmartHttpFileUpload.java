package org.smartboot.http.example.fileupload;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.smartboot.http.HttpRequest;

import java.io.IOException;


public class SmartHttpFileUpload extends FileUpload {

    public FileItemIterator getItemIterator(HttpRequest request)
            throws FileUploadException, IOException {
        return super.getItemIterator(new SmartHttpRequestContext(request));
    }
}
