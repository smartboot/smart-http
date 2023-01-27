package org.smartboot.http.restful;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.utils.DateUtils;
import org.smartboot.http.common.utils.Mimetypes;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2023/1/26
 */
public class StaticResourceHandler extends HttpServerHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(StaticResourceHandler.class);
    private final Date lastModifyDate = new Date(System.currentTimeMillis() / 1000 * 1000);

    private final String lastModifyDateFormat = DateUtils.formatLastModified(lastModifyDate);

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        String fileName = request.getRequestURI();

        if (StringUtils.endsWith(fileName, "/")) {
            fileName += "index.html";
        }

        //304
        try {
            String requestModified = request.getHeader(HeaderNameEnum.IF_MODIFIED_SINCE.getName());
            if (StringUtils.isNotBlank(requestModified) && lastModifyDate.getTime() <= DateUtils.parseLastModified(requestModified).getTime()) {
                response.setHttpStatus(HttpStatus.NOT_MODIFIED);
                return;
            }
        } catch (Exception e) {
            LOGGER.error("exception", e);
        }
        response.setHeader(HeaderNameEnum.LAST_MODIFIED.getName(), lastModifyDateFormat);

        try (InputStream inputStream = StaticResourceHandler.class.getClassLoader().getResourceAsStream("static" + fileName)) {
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
}
