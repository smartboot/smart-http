package org.smartboot.http.restful.context;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ClassScanner {

    public static Map<Class<? extends Annotation>, List<Class<?>>> findClassesWithAnnotation(List<Class<? extends Annotation>> annotations, String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        List<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
//        if (classes.isEmpty()) {
//            URL url = classLoader.getResource(path + ".class");
//            if (url != null) {
//                classes.add(Class.forName(packageName));
//            }
//        }
        Map<Class<? extends Annotation>, List<Class<?>>> annotatedClasses = new HashMap<>();
        for (Class<?> clazz : classes) {
            annotations.forEach(annotation -> {
                if (clazz.isAnnotationPresent(annotation)) {
                    List<Class<?>> list = annotatedClasses.computeIfAbsent(annotation, a -> new ArrayList<>());
                    list.add(clazz);
                }
            });

        }
        return annotatedClasses;
    }

    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}