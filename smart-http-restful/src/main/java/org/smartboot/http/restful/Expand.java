package org.smartboot.http.restful;

import org.smartboot.http.restful.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.util.List;

public interface Expand<T extends Annotation> {

    void init(ApplicationContext context, List<Class<T>> clazz) throws Exception;

    Class<T> expandAnnotation();
}
