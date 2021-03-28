package com.desert.router.gradle

import com.android.annotations.NonNull
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils

import java.util.function.Consumer
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry


/**
 * 自定义Transform 在字节码之后，dex之前会回调此Transform
 */
class RouterMappingTransform extends Transform {
    /**
     * 当前Transform的名字
     *
     * @return 通常为类名
     */
    @Override
    String getName() {
        return "RouterMappingTransform"
    }

    /**
     * 告诉编译器，当前Transform消费的类型
     * CLASS，RESOURCES  其他类型只有android 编译前才支持
     *
     * @return 消费的类型
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 告诉编译器，当前Transform收集的范围
     * 通常为全部工程
     *
     * @return 范围集合
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    /**
     * 是否支持增加
     * 支持增量的时候有四种状态 notification/changed/removed/added
     *
     * @return true支持  false不支持
     */
    @Override
    boolean isIncremental() {
        return false
    }

    /**
     * 所有的CLASS文件 会传入此方法
     *
     * @param transformInvocation TransformInvocation
     * @throws TransformException @TransformException
     * @throws TransformException @TransformException
     * @throws IOException        @IOException
     */
    void transform(@NonNull TransformInvocation transformInvocation)
            throws TransformException, InterruptedException, IOException {
        //1.遍历所有的input 2. 对input进行二次处理 3.拷贝input到目标目录（也是下一个transform的输入）
        RouterMappingCollector routerMappingCollector = new RouterMappingCollector()
        Collection<TransformInput> inputs = transformInvocation.getInputs()
        inputs.forEach(new Consumer<TransformInput>() {
            @Override
            void accept(TransformInput transformInput) {
                //遍历目录CLASS 并拷贝
                transformInput.getDirectoryInputs().forEach(new Consumer<DirectoryInput>() {
                    @Override
                    void accept(DirectoryInput directoryInput) {
                        def dest = transformInvocation.getOutputProvider().getContentLocation(directoryInput.getName(),
                                directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY)

                        routerMappingCollector.collect(directoryInput.getFile())

                        FileUtils.copyFile(directoryInput.getFile(), dest)
                    }
                })
                //遍历jar  并拷贝到目标目录
                transformInput.getJarInputs().each { jarInput ->
                    def dest = transformInvocation.outputProvider.getContentLocation(jarInput.name, jarInput.contentTypes,
                            jarInput.scopes, Format.JAR)

                    routerMappingCollector.collectForJarFile(jarInput.file)

                    FileUtils.copyFile(jarInput.file, dest)

                }

            }
        })

        println("${getName()} all mapping class name : ${routerMappingCollector.getClassNames()}")

        //将生成的字节码写入本地文件
        File mappingJarFile = transformInvocation.outputProvider.getContentLocation("router_mapping", getInputTypes(),
                getScopes(), Format.JAR)

        println("${getName()}  mappingJarFile = $mappingJarFile")

        if (!mappingJarFile.getParentFile().exists()) {
            mappingJarFile.getParentFile().mkdir()
        }

        if (mappingJarFile.exists()){
            mappingJarFile.delete()
        }

        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(mappingJarFile))
        ZipEntry zipEntry=new ZipEntry(RouterMappingByteCodeBuilder.CLASS_NAME+".class")
        jarOutputStream.putNextEntry(zipEntry)
        jarOutputStream.write(RouterMappingByteCodeBuilder.get(routerMappingCollector.getClassNames()))
        jarOutputStream.closeEntry()
        jarOutputStream.close()
    }
}
