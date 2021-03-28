package com.desert.router.gradle

import java.util.jar.JarEntry
import java.util.jar.JarFile


/**
 * 收集目标CLASS文件
 */
public class RouterMappingCollector {
    private static final String CLASS_PACKAGE_NAME = "com\\desert\\router\\mapping"
    private static final String CLASS_PACKAGE_NAME_FOR_JAR = "com/desert/router/mapping"
    private static final String CLASS_NAME_SUFFIX = ".class"
    private static final String CLASS_NAME_PREFIX = "RouterMapping_"

    private Set<String> classNames = new HashSet<>()

    Set<String> getClassNames() {
        return classNames
    }

    /**
     * 收集CLASS文件或者CLASS文件目录中的映射表类
     *
     * @param file File
     */
    void collect(File file) {
        if (file == null || !file.exists()) {
            return
        }
        if (file.isFile()) {
            if (file.absolutePath.contains(CLASS_PACKAGE_NAME) && file.name.startsWith(CLASS_NAME_PREFIX) && file.name.endsWith(CLASS_NAME_SUFFIX)) {
                String className = file.name.replace(CLASS_NAME_SUFFIX, "")
                classNames.add(className)
            }
        } else {
            file.listFiles().each {
                collect(it)
            }
        }
    }
    /**
     * 收集jar中的目标类
     * @param jarfile
     */
    void collectForJarFile(File jarfile) {
        if (jarfile == null || !jarfile.exists()) return
        Enumeration<JarEntry> jar = new JarFile(jarfile).entries()
        while (jar.hasMoreElements()) {
            String entryName = jar.nextElement()
            //println("entryName :"+entryName)
            if (entryName.contains(CLASS_PACKAGE_NAME_FOR_JAR) && entryName.contains(CLASS_NAME_PREFIX) && entryName.contains(CLASS_NAME_SUFFIX)) {
                String className = entryName.replace(CLASS_PACKAGE_NAME_FOR_JAR, "")
                        .replace("/", "")
                        .replace(CLASS_NAME_SUFFIX, "")
                classNames.add(className)
            }
        }

    }
}
