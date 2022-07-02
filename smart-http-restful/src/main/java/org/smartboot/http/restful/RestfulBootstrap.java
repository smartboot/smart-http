package org.smartboot.http.restful;

import org.smartboot.http.server.HttpBootstrap;

import java.util.Arrays;
import java.util.List;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/7/2
 */
public class RestfulBootstrap {
    public static HttpBootstrap controller(List<Class<?>> controllers) {
        RestHandler restHandler = new RestHandler();
        restHandler.addController(DefaultController.class);
        controllers.forEach(restHandler::addController);
        HttpBootstrap httpBootstrap = new HttpBootstrap();
        httpBootstrap.httpHandler(restHandler);
        return httpBootstrap;
    }

    public static HttpBootstrap controller(Class<?>... controllers) {
        return controller(Arrays.asList(controllers));
    }

}
