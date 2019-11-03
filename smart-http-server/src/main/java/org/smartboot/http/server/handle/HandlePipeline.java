package org.smartboot.http.server.handle;

import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;
import org.smartboot.http.Pipeline;

import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2019/11/3
 */
public final class HandlePipeline extends HttpHandle implements Pipeline {
    /**
     * 管道头
     */
    private HttpHandle headHandle;
    /**
     * 管道尾
     */
    private HttpHandle tailHandle;

    /**
     * 添加HttpHandle至末尾
     *
     * @param nextHandle 尾部handle
     * @return 当前管道对象
     */
    public Pipeline next(HttpHandle nextHandle) {
        if (headHandle == null) {
            headHandle = tailHandle = nextHandle;
            return this;
        }
        HttpHandle httpHandle = tailHandle;
        while (httpHandle.nextHandle != null) {
            httpHandle = httpHandle.nextHandle;
        }
        httpHandle.nextHandle = nextHandle;
        return this;
    }

    @Override
    public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
        headHandle.doNext(request, response);
    }
}
