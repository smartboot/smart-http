package org.smartboot.http.restful.context;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class ClassScanner {

    public static Map<Class<? extends Annotation>, List<Class<?>>> findClassesWithAnnotation(List<Class<? extends Annotation>> annotations, String packageName) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<Class<?>> classes = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if ("file".equals(resource.getProtocol())) {
                classes.addAll(findClasses(new File(resource.getFile()), packageName));
            } else if ("jar".equals(resource.getProtocol())) {
                classes.addAll(scanClassesInJar(resource, path));
            } else {
                System.out.println(resource.getProtocol());
            }

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

    /**
     * 扫描指定jar包中的子目录
     */
    private static List<Class<?>> scanClassesInJar(URL url, String packagePath) throws IOException, URISyntaxException {
        List<Class<?>> classNames = new ArrayList<>();

        URI uri = url.toURI();
        try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
            Path basePath = fileSystem.getPath(packagePath);
            if (!Files.exists(basePath)) {
                return classNames;
            }

            try (Stream<Path> walk = Files.walk(basePath)) {
                walk.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".class"))
                        .forEach(path -> {
                            String className = path.toString()
                                    .substring(1)
                                    .replace("/", ".")
                                    .replace(".class", "");
                            try {
                                classNames.add(Class.forName(className));
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }

        return classNames;
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