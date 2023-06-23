package org.smartboot.http.restful;

import org.smartboot.http.restful.annotation.Bean;
import org.smartboot.http.restful.annotation.Controller;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/7/2
 */
public class RestfulBootstrap {
    private final HttpBootstrap httpBootstrap = new HttpBootstrap();
    private final RestHandler restHandler;
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
        restHandler.addBean(name, object);
        return this;
    }

    public RestfulBootstrap addBean(Object object) throws Exception {
        restHandler.addBean(object.getClass().getSimpleName().substring(0, 1).toLowerCase() + object.getClass().getSimpleName().substring(1), object);
        return this;
    }

    public RestfulBootstrap scan(String packageName) throws Exception {
        Map<Class<? extends Annotation>, List<Class<?>>> map = ClassScanner.findClassesWithAnnotation(Arrays.asList(Controller.class, Bean.class), packageName);
        //注册Bean
        if (map.containsKey(Bean.class)) {
            for (Class<?> clazz : map.get(Bean.class)) {
                restHandler.addBean(clazz);
            }
        }


        //注册Controller
        if (map.containsKey(Controller.class)) {
            for (Class<?> clazz : map.get(Controller.class)) {
                controller(clazz);
            }
        }
        restHandler.dependencyInversion();
        return this;
    }

    public static HttpBootstrap controller(List<Class<?>> controllers) throws Exception {
        RestfulBootstrap restfulBootstrap = getInstance();
        for (Class<?> controller : controllers) {
            restfulBootstrap.controller(controller);
        }
        return restfulBootstrap.httpBootstrap;
    }

    public static RestfulBootstrap getInstance() throws Exception {
        return getInstance(DEFAULT_HANDLER);
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
