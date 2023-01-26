package org.smartboot.http.restful;

import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/7/2
 */
public class RestfulBootstrap {
    private final HttpBootstrap httpBootstrap = new HttpBootstrap();
    private final RestHandler restHandler;

    private RestfulBootstrap(HttpServerHandler defaultHandler) throws Exception {
        this.restHandler = new RestHandler(defaultHandler);
        restHandler.addController(DefaultController.class);
        httpBootstrap.httpHandler(restHandler);
    }

    public static HttpBootstrap controller(List<Class<?>> controllers) throws Exception {
        RestfulBootstrap restfulBootstrap = getInstance();
        for (Class<?> controller : controllers) {
            restfulBootstrap.controller(controller);
        }
        return restfulBootstrap.httpBootstrap;
    }

    public static RestfulBootstrap getInstance() throws Exception {
        return getInstance(null);
    }

    public static RestfulBootstrap getInstance(HttpServerHandler defaultHandler) throws Exception {
        return new RestfulBootstrap(defaultHandler);
    }

    public static HttpBootstrap controller(Class<?>... controllers) throws Exception {
        return controller(Arrays.asList(controllers));
    }

    public RestfulBootstrap controller(Class<?> controllerClass) throws Exception {
        restHandler.addController(controllerClass);
        return this;
    }

    public RestfulBootstrap controller(Object controller) throws Exception {
        restHandler.addController(controller);
        return this;
    }

    public RestfulBootstrap inspect(BiConsumer<HttpRequest, HttpResponse> consumer) {
        restHandler.setInspect(consumer);
        return this;
    }

    public HttpBootstrap bootstrap() {
        return httpBootstrap;
    }
}
