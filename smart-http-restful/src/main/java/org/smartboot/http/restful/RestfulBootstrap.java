package org.smartboot.http.restful;

import org.smartboot.http.restful.context.ApplicationContext;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.BiConsumer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/7/2
 */
public class RestfulBootstrap {
    private final HttpBootstrap httpBootstrap = new HttpBootstrap();
    private final RestHandler restHandler;
    private final ApplicationContext applicationContext = new ApplicationContext();
    private static final HttpServerHandler DEFAULT_HANDLER = new HttpServerHandler() {
        private final byte[] BYTES = "hello smart-http-rest".getBytes();

        @Override
        public void handle(HttpRequest request, HttpResponse response) throws IOException {
            response.getOutputStream().write(BYTES);
        }
    };

    private RestfulBootstrap(HttpServerHandler defaultHandler) {
        if (defaultHandler == null) {
            throw new NullPointerException();
        }
        this.restHandler = new RestHandler(defaultHandler);
        httpBootstrap.httpHandler(restHandler);
    }

    public RestfulBootstrap addBean(String name, Object object) throws Exception {
        applicationContext.addBean(name, object);
        return this;
    }

    public RestfulBootstrap addBean(Object object) throws Exception {
        applicationContext.addBean(object.getClass().getSimpleName().substring(0, 1).toLowerCase() + object.getClass().getSimpleName().substring(1), object);
        return this;
    }

    public RestfulBootstrap scan(String... packageName) throws Exception {
        applicationContext.scan(Arrays.asList(packageName));
        applicationContext.getControllers().forEach(restHandler::addController);
        return this;
    }

    public RestfulBootstrap controller(Class<?>... classes) throws Exception {
        for (Class<?> clazz : classes) {
            Object o = applicationContext.addController(clazz);
            applicationContext.initialBean(o);
            restHandler.addController(o);
        }
        return this;
    }

    public static RestfulBootstrap getInstance() throws Exception {
        return getInstance(DEFAULT_HANDLER);
    }

    public static RestfulBootstrap getInstance(HttpServerHandler defaultHandler) throws Exception {
        return new RestfulBootstrap(defaultHandler);
    }


    public RestfulBootstrap inspect(BiConsumer<HttpRequest, HttpResponse> consumer) {
        restHandler.setInspect(consumer);
        return this;
    }

    public HttpBootstrap bootstrap() {
        return httpBootstrap;
    }
}
