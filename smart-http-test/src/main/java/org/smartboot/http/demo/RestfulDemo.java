package org.smartboot.http.demo;

import org.smartboot.http.restful.RestfulBootstrap;
import org.smartboot.http.restful.annotation.Controller;
import org.smartboot.http.restful.annotation.RequestMapping;
import org.smartboot.http.restful.annotation.RequestMethod;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2023/1/27
 */
@Controller
public class RestfulDemo {

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String helloworld() {
        return "hello world";
    }

    public static void main(String[] args) throws Exception {
        RestfulBootstrap bootstrap = RestfulBootstrap.getInstance().controller(RestfulDemo.class);
        bootstrap.bootstrap().setPort(8080).start();
    }
}
