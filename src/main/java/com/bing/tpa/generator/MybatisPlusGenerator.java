package com.bing.tpa.generator;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("all")
@Slf4j
@Data
public class MybatisPlusGenerator {

    // 数据库连接配置
    protected static String URL = "jdbc:mysql://localhost:3306/tpa_system?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
    protected static String USERNAME = "root";
    protected static String PASSWORD = "123mysql";

    // 数据源配置构建器
    protected static DataSourceConfig.Builder DATA_SOURCE_CONFIG = new DataSourceConfig
            .Builder(URL, USERNAME, PASSWORD);

    // 项目基础配置
    private static final String PROJECT_PATH = System.getProperty("user.dir");
    private static final String OUTPUT_DIR = PROJECT_PATH + "/src/main/java";    // 代码生成目录
    private static final String MAPPER_XML_DIR = PROJECT_PATH + "/src/main/resources/mapper";  // XML文件目录
    private static final String PACKAGE_NAME = "com.bing.tpa";                   // 基础包名
    private static final String MODULE_NAME = "tpa_system";                          // 模块名称

    // 需要生成的表名列表
    private static final List<String> TABLE_NAMES = Arrays.asList(
            "student_class","tpa_class", "tpa_design_before", "tpa_homework", "tpa_homework_details",
            "tpa_homework_track", "tpa_interaction", "tpa_preview_task", "tpa_preview_track",
            "tpa_student", "tpa_subject", "tpa_subject_syllabus", "tpa_syllabus",
            "tpa_teach_design", "tpa_teacher"
    );
    // 需要过滤的表前缀
//    private static final List<String> TABLE_PREFIX = Arrays.asList("t_", "sys_");
    private static final List<String> TABLE_PREFIX = Collections.emptyList();

    public static void main(String[] args) {
        // 代码生成入口
        FastAutoGenerator.create(DATA_SOURCE_CONFIG)
                // 全局配置
                .globalConfig(builder -> {
                    builder.author("splitPPTVideoFile/liuc")              // 设置作者
                            .enableSwagger()                // 开启swagger支持
                            .dateType(DateType.TIME_PACK)   // 使用Java8日期类型
                            .disableOpenDir()               // 禁止打开输出目录
                            .commentDate("yyyy-MM-dd")      // 注释日期格式
                            .outputDir(OUTPUT_DIR);         // 输出目录
                })
                // 包配置
                .packageConfig(builder -> {
                    builder.parent(PACKAGE_NAME)            // 父包名
                            .moduleName(MODULE_NAME)        // 模块包名
                            .entity("model.entity")         // 实体类包名
                            .mapper("mapper")               // Mapper接口包名
                            .service("service")             // Service接口包名
                            .serviceImpl("service.impl")    // Service实现类包名
                            .controller("controller")       // Controller包名
                            .pathInfo(Collections.singletonMap(
                                    OutputFile.xml, MAPPER_XML_DIR  // XML文件路径
                            ));
                })
                // 策略配置
                .strategyConfig(builder -> {
                    builder.addInclude(TABLE_NAMES)         // 设置需要生成的表名
                            .addTablePrefix(TABLE_PREFIX)   // 过滤表前缀

                            // 实体类策略
                            .entityBuilder()
                            .enableLombok()                 // 启用Lombok
                            .enableTableFieldAnnotation()   // 字段添加注解
                            .addTableFills(new Column("create_time", FieldFill.INSERT))     // 自动填充字段
                            .addTableFills(new Column("update_time", FieldFill.INSERT_UPDATE))
                            .versionColumnName("version")   // 乐观锁字段
                            .logicDeleteColumnName("deleted")  // 逻辑删除字段

                            // Mapper策略
                            .mapperBuilder()
                            .enableMapperAnnotation()       // 启用@Mapper注解
                            .enableBaseResultMap()          // 生成resultMap

                            // Service策略
                            .serviceBuilder()
                            .formatServiceFileName("%sService")  // 服务接口命名格式
                            .formatServiceImplFileName("%sServiceImpl")  // 服务实现类命名格式

                            // Controller策略
                            .controllerBuilder()
                            .enableRestStyle()              // 启用RestController
                            .formatFileName("%sController"); // 控制器命名格式
                })
                // 模板引擎配置
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();

        log.info("代码生成完成，输出目录：{}", OUTPUT_DIR);
    }
}
