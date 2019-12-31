/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: LoggerFactory.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/
package org.smartboot.http.logging;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ServiceLoader;
import java.util.logging.LogManager;

public class LoggerFactory {

    private static final LoggerFactory singleton = new LoggerFactory();

    private final Constructor<? extends Logger> discoveredLogConstructor;

    /**
     * Private constructor that is not available for public use.
     */
    private LoggerFactory() {
        // Look via a ServiceLoader for a Log implementation that has a
        // constructor taking the String name.
        ServiceLoader<Logger> logLoader = ServiceLoader.load(Logger.class);
        Constructor<? extends Logger> m = null;
        for (Logger log : logLoader) {
            Class<? extends Logger> c = log.getClass();
            try {
                m = c.getConstructor(String.class);
                break;
            } catch (NoSuchMethodException | SecurityException e) {
                throw new Error(e);
            }
        }
        discoveredLogConstructor = m;
    }

    public static LoggerFactory getFactory() throws LogConfigurationException {
        return singleton;
    }

    public static Logger getLogger(Class<?> clazz)
            throws LogConfigurationException {
        return (getFactory().getInstance(clazz));

    }

    public static Logger getLogger(String name)
            throws LogConfigurationException {
        return (getFactory().getInstance(name));

    }

    public static void release(ClassLoader classLoader) {
        // JULI's log manager looks at the current classLoader so there is no
        // need to use the passed in classLoader, the default implementation
        // does not so calling reset in that case will break things
        if (!LogManager.getLogManager().getClass().getName().equals(
                "java.util.logging.LogManager")) {
            LogManager.getLogManager().reset();
        }
    }

    public Logger getInstance(String name) throws LogConfigurationException {
        if (discoveredLogConstructor == null) {
            return DirectJDKLog.getInstance(name);
        }

        try {
            return discoveredLogConstructor.newInstance(name);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
                InvocationTargetException e) {
            throw new LogConfigurationException(e);
        }
    }

    public Logger getInstance(Class<?> clazz) throws LogConfigurationException {
        return getInstance(clazz.getName());
    }
}
