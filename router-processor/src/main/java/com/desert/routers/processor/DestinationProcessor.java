package com.desert.routers.processor;

import com.desert.router.annotations.Destination;
import com.google.auto.service.AutoService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class DestinationProcessor extends AbstractProcessor {
    private static final String TAG = "DestinationProcessor";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //防止被多次处理
        if (roundEnv.processingOver()) {
            return false;
        }
        System.out.println(TAG + " >>> process start");

        String root_project_dir = processingEnv.getOptions().get("ROOT_PROJECT_DIR");

        System.out.println(TAG + " >>> process root_project_dir =" + root_project_dir);

        //获取被 @Destination 的元素
        Set<TypeElement> allDestinationElement = (Set<TypeElement>) roundEnv.getElementsAnnotatedWith(Destination.class);
        System.out.println(TAG + " >>> allDestinationElement count= " + allDestinationElement.size());

        // 当未收集到 @Destination 注解的时候，跳过后续流程
        if (allDestinationElement.size() < 1) {
            return false;
        }

        JsonArray jsonArray = new JsonArray();

        //遍历@Destination 的元素
        for (TypeElement typeElement : allDestinationElement) {
            //获取注解
            Destination destination = typeElement.getAnnotation(Destination.class);
            final String url = destination.url();
            final String description = destination.description();
            final String realPath = typeElement.getQualifiedName().toString();
            System.out.println(TAG + " >>> url = " + url);
            System.out.println(TAG + " >>> description = " + description);
            System.out.println(TAG + " >>> realPath = " + realPath);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("url", url);
            jsonObject.addProperty("description", description);
            jsonObject.addProperty("realPath", realPath);
            jsonArray.add(jsonObject);
        }

        generateClassFile(allDestinationElement);
        //generateJsonFile(root_project_dir, jsonArray.toString());
        System.out.println(TAG + " >>> process finish");
        return false;
    }

    private void generateJsonFile(String rootPath, String jsonContent) {
        //检测父目录是否存在
        File rootDirFile = new File(rootPath);
        if (!rootDirFile.exists()) {
            throw new RuntimeException("root_project_dir not exists!");
        }
        //创建router_mapping
        File routerFileDir = new File(rootDirFile, "router_mapping");
        if (!routerFileDir.exists()) {
            routerFileDir.mkdir();
        }
        File jsonFile = new File(routerFileDir, "router_" + System.currentTimeMillis() + ".json");
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(jsonFile));
            bufferedWriter.write(jsonContent);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Error  create json file", e);
        }
    }

    private void generateClassFile(Set<TypeElement> allDestinationElement) {
        String className = "RouterMapping_" + System.currentTimeMillis();
        StringBuilder builder = new StringBuilder();

        builder.append("package com.desert.router.mapping;\n\n")
                .append("import java.util.HashMap;\n")
                .append("import java.util.Map;\n\n")
                .append("public class ")
                .append(className)
                .append("{\n")
                .append("    public static Map<String, String> get() {\n")
                .append("        Map<String, String> map = new HashMap<>();\n");
        for (TypeElement typeElement : allDestinationElement) {
            Destination destination = typeElement.getAnnotation(Destination.class);
            final String url = destination.url();
            final String realPath = typeElement.getQualifiedName().toString();
            builder.append("        map.put(\"").append(url).append("\",\"").append(realPath).append("\");\n");
        }
        builder.append("        return map;\n")
                .append("    }\n")
                .append("}\n");

        String mappingFullClassName = "com.desert.router.mapping." + className;
        System.out.println(TAG + " >>> process mappingFullClassName= " + mappingFullClassName);
        System.out.println(TAG + " >>> process class content= \n" + builder);

        try {
            JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(mappingFullClassName);
            Writer writer = sourceFile.openWriter();
            writer.write(builder.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Error create file", e);
        }
    }

    /**
     * 支持的注解类型集合
     *
     * @return 注解器所支持的注解类型集合，如果没有这样的类型，则返回一个空集合
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Destination.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}