package com.desert.router.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project

class RouterPlugin implements Plugin<Project> {
    //注入插件的逻辑
    void apply(Project project) {
        //注册自定义的Transform
        if (project.plugins.hasPlugin(AppPlugin)) {
            AppExtension appExtension = project.extensions.getByType(AppExtension)
            appExtension.registerTransform(new RouterMappingTransform())
        }


        //1. 自动实现用户传递路径参数到注解处理器
        def cl = { project1 ->
            if (project1.extensions.findByName("kapt") != null) {
                project1.extensions.findByName("kapt").arguments {
                    arg("ROOT_PROJECT_DIR", project.rootDir.absolutePath)
                }
            }
        }
        project.rootProject.subprojects.eachWithIndex { subProject, int index ->
            if (subProject.name == "app") {
                cl(subProject)
                return
            }
            //配置阶段结束之后 设置kapt
            subProject.afterEvaluate {
                cl(subProject)
            }
        }
        //2. 实现旧的构建产物的自动清理
        project.clean.doFirst {
            File routerMappingDir = new File(project.rootDir, "router_mapping")
            if (routerMappingDir.exists()) {
                routerMappingDir.deleteDir()
            }
        }

        //3. 在Javac任务后，汇总生成文档
        project.getExtensions().create("router", RouterExtensions.class)

        //生产各行文档
        project.afterEvaluate {
            println("用户设置的WIKI路径为：${project.router.wikiDir} ")
            //在javac任务（compileDebugJavaWithJavac）后，汇总文档
            project.tasks.findAll { task ->
                return task.name.startsWith("compile") && task.name.endsWith("JavaWithJavac")
            }.each { task ->
                task.doLast {
                    //定位到存在json文件的File
                    File routerMappingDir = new File(project.rootDir, "router_mapping")
                    if (!routerMappingDir.exists()) {
                        return
                    }
                    File[] childFiles = routerMappingDir.listFiles()
                    if (childFiles.size() < 1) {
                        return
                    }

                    StringBuilder markdownBuild = new StringBuilder()
                    markdownBuild.append("# 路由文档\n\n")
                    childFiles.each { childFile ->
                        if (childFile.name.endsWith('json')) {
                            JsonSlurper jsonSlurper = new JsonSlurper()
                            def contentArray = jsonSlurper.parse(childFile)
                            contentArray.each { innerContent ->
                                println("innerContent:" + innerContent)
                                def url = innerContent['url']
                                def description = innerContent['description']
                                def realPath = innerContent['realPath']
                                markdownBuild.append("## $description\n")
                                markdownBuild.append("- url:$url\n")
                                markdownBuild.append("- realPath:$realPath\n\n")
                            }
                        }
                    }
                    File wikiDirFile = new File(project.router.wikiDir)
                    if (!wikiDirFile.exists()) {
                        wikiDirFile.mkdir()
                    }

                    File wikiFile = new File(wikiDirFile, "路由文档.md")
                    if (!wikiDirFile.exists()) {
                        wikiDirFile.delete()
                    }

                    wikiFile.write(markdownBuild.toString())
                }
            }
        }
    }
}