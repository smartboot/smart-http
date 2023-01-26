package org.smartboot.http.restful;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.utils.Mimetypes;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2023/1/26
 */
public class StaticResourceHandler extends HttpServerHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(StaticResourceHandler.class);

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        String fileName = request.getRequestURI();

        InputStream inputStream = StaticResourceHandler.class.getClassLoader().getResourceAsStream("static" + fileName);
        if (inputStream == null) {
            response.setHttpStatus(HttpStatus.NOT_FOUND);
            return;
        }
        String contentType = Mimetypes.getInstance().getMimetype(fileName);
        response.setHeader(HeaderNameEnum.CONTENT_TYPE.getName(), contentType + "; charset=utf-8");
        byte[] bytes = new byte[1024];
        int length;
        while ((length = inputStream.read(bytes)) > 0) {
            response.getOutputStream().write(bytes, 0, length);
        }
    }
}
