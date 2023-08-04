package org.smartboot.http.restful.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * @author qinluo
 * @date 2023-08-02 11:29:29
 * @since 1.0.0
 */
public abstract class AnnotatedBeanScanner implements BeanScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotatedBeanScanner.class);

    @Override
    public void scanAndRegister(ApplicationContext ctx, List<String> packages) {
        for (String p : packages) {
            try {
                Map<Class<? extends Annotation>, List<Class<?>>> map = ClassScanner.findClassesWithAnnotation(getAnnotations(), p);
                map.forEach((k, v) -> doRegister(ctx, k, v));

            } catch (Exception e) {
                LOGGER.error("{} scan package {} failed", getClass(), packages, e);
            }
        }
    }

    protected abstract List<Class<? extends Annotation>> getAnnotations();
    protected abstract void doRegister(ApplicationContext ctx, Class<? extends Annotation> annotatedType, List<Class<?>> classes);
}
