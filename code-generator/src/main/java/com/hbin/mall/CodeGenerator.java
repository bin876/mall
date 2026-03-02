package com.hbin.mall;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.nio.file.Paths;
import java.util.Collections;

public class CodeGenerator {
    public static void main(String[] args) {
        String url = "jdbc:mysql://192.168.72.130:3306/mall?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";
        String username = "dbadmin";
        String password = "123456";

        String projectPath = System.getProperty("user.dir");
        String javaOutputDir = Paths.get(projectPath, "code-generator", "src", "main", "java").toString();
        String xmlOutputDir = Paths.get(projectPath, "code-generator", "src", "main", "resources", "mapper").toString();

        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> builder
                        .author("hbin")
                        .outputDir(javaOutputDir)
                        .commentDate("yyyy-MM-dd")
                )
                .packageConfig(builder -> builder
                        .parent("com.hbin.mall")
                        .moduleName("")
                        .entity("domain")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .controller("controller")
                        .pathInfo(Collections.singletonMap(OutputFile.xml, xmlOutputDir))
                )
                .strategyConfig(builder -> builder
                        // .addInclude("auth_account", "identity_mapping")

                        .controllerBuilder()
                        .enableRestStyle()
                        .formatFileName("%sController")

                        .entityBuilder()
                        .enableLombok()

                        .serviceBuilder()
                        .formatServiceFileName("%sService")
                        .formatServiceImplFileName("%sServiceImpl")

                        .mapperBuilder()
                        .enableMapperAnnotation()
                        .formatMapperFileName("%sMapper")
                        .formatXmlFileName("%sMapper")
                )
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}