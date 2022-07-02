package org.smartboot.http.restful;

import org.smartboot.http.restful.annotation.Controller;
import org.smartboot.http.restful.annotation.RequestMapping;
import org.smartboot.http.restful.annotation.RequestMethod;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/7/2
 */
@Controller
public class DefaultController {
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String helloWorld() {
        return "hello smart-http-rest";
    }
}
