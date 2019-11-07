package org.smartboot.http.server.handle;

import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;
import org.smartboot.http.enums.HttpStatus;

import java.io.IOException;

/**
 * 404处理器
 *
 * @author 三刀
 * @version V1.0 , 2019/11/7
 */
public class NotFoundHandle extends HttpHandle {
    @Override
    public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
        response.setHttpStatus(HttpStatus.NOT_FOUND);
    }
}
