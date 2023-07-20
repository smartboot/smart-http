package org.smartboot.http.restful.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.restful.annotation.Autowired;
import org.smartboot.http.restful.annotation.Bean;
import org.smartboot.http.restful.annotation.Controller;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 6/23/23
 */
public class ApplicationContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationContext.class);
    private final Map<String, Object> namedBeans = new HashMap<>();

    private final List<Object> controllers = new ArrayList<>();

    public void start() throws Exception {
        //依赖注入
        for (Map.Entry<String, Object> entry : namedBeans.entrySet()) {
            initialBean(entry.getValue());
        }
    }

    public void scan(List<String> packages) throws Exception {
        for (String p : packages) {
            Map<Class<? extends Annotation>, List<Class<?>>> map = ClassScanner.findClassesWithAnnotation(Arrays.asList(Controller.class, Bean.class), p);
            //注册Bean
            if (map.containsKey(Bean.class)) {
                for (Class<?> clazz : map.get(Bean.class)) {
                    addBean(clazz);
                }
            }

            //注册Controller
            if (map.containsKey(Controller.class)) {
                for (Class<?> clazz : map.get(Controller.class)) {
                    addController(clazz);
                }
            }
        }
    }

    public void addBean(String name, Object object) throws Exception {
        if (namedBeans.containsKey(name)) {
            throw new IllegalStateException("duplicated name[" + name + "] for " + object.getClass().getName());
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("add bean:{} for class:{}", name, object);
        }
        namedBeans.put(name, object);
        for (Method method : object.getClass().getDeclaredMethods()) {
            Bean bean = method.getAnnotation(Bean.class);
            if (bean == null) {
                continue;
            }
            Object o = method.invoke(object);
            if (StringUtils.isNotBlank(bean.value())) {
                addBean(bean.value(), o);
            } else {
                addBean(method.getReturnType().getSimpleName().substring(0, 1).toLowerCase() + method.getReturnType().getSimpleName().substring(1), o);
            }
        }
    }

    public void addBean(Class<?> clazz) throws Exception {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Bean bean = clazz.getAnnotation(Bean.class);
        boolean suc = false;
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameters().length != 0) {
                continue;
            }
            constructor.setAccessible(true);
            Object object = constructor.newInstance();
            if (StringUtils.isNotBlank(bean.value())) {
                addBean(bean.value(), object);
            } else {
                addBean(clazz.getSimpleName().substring(0, 1).toLowerCase() + clazz.getSimpleName().substring(1), object);
            }
            suc = true;
        }
        if (!suc) {
            LOGGER.warn("no public no-args constructor found for beanClass:{}", clazz.getName());
        }
    }

    public void addController(Class<?> clazz) throws Exception {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        boolean suc = false;
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameters().length != 0) {
                continue;
            }
            constructor.setAccessible(true);
            Object object = constructor.newInstance();
            addBean(clazz.getName(), object);
            controllers.add(object);
            suc = true;
        }
        if (!suc) {
            LOGGER.warn("no public no-args constructor found for controllerClass: {}", clazz.getName());
        }
    }

    public List<Object> getControllers() {
        return controllers;
    }

    private void initialBean(Object object) throws IllegalAccessException, InvocationTargetException {
        for (Field field : object.getClass().getDeclaredFields()) {
            Autowired autowired = field.getAnnotation(Autowired.class);
            if (autowired != null) {
                field.setAccessible(true);
                Object value = namedBeans.get(field.getName());
                if (value == null) {
                    throw new IllegalStateException();
                }
                if (field.getType().isAssignableFrom(value.getClass())) {
                    field.set(object, value);
                } else {
                    throw new IllegalStateException();
                }
            }
        }
        for (Method method : object.getClass().getDeclaredMethods()) {
            PostConstruct postConstruct = method.getAnnotation(PostConstruct.class);
            if (postConstruct != null) {
                method.setAccessible(true);
                method.invoke(object);
            }
        }
    }


    public void destroy() throws InvocationTargetException, IllegalAccessException {
        //依赖注入
        for (Map.Entry<String, Object> entry : namedBeans.entrySet()) {
            Object bean = entry.getValue();
            for (Method method : bean.getClass().getDeclaredMethods()) {
                PreDestroy preDestroy = method.getAnnotation(PreDestroy.class);
                if (preDestroy != null) {
                    method.setAccessible(true);
                    method.invoke(bean);
                }
            }
        }
    }
}
