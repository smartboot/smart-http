package org.smartboot.http.test.restful;

import org.smartboot.http.restful.annotation.Controller;
import org.smartboot.http.restful.annotation.RequestMapping;

@Controller("demo2")
class Demo2Controller {
    @RequestMapping
    public String test1() {
        return "hello world";
    }
}
