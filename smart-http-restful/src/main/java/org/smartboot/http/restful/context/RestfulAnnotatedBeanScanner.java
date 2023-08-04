package org.smartboot.http.restful.context;

import org.smartboot.http.restful.annotation.Bean;
import org.smartboot.http.restful.annotation.Controller;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

/**
 * @author qinluo
 * @date 2023-08-02 12:15:18
 * @since 1.2.8
 */
public class RestfulAnnotatedBeanScanner extends AnnotatedBeanScanner {

    @Override
    protected List<Class<? extends Annotation>> getAnnotations() {
        return Arrays.asList(Controller.class, Bean.class);
    }

    @Override
    protected void doRegister(ApplicationContext ctx, Class<? extends Annotation> annotatedType, List<Class<?>> classes) {
        if (annotatedType == (Bean.class)) {
            for (Class<?> clazz : classes) {
                try {
                    ctx.addBean(clazz);
                } catch (Exception e) {

                }
            }
        }

        //注册Controller
        if (annotatedType == (Controller.class)) {
            for (Class<?> clazz : classes) {
                try {
                    ctx.addController(clazz);
                } catch (Exception e) {

                }
            }
        }
    }
}
