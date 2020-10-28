package com.zcw.simpledata.config;

import com.zcw.simpledata.base.utils.ClassUtil;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

@Component
@Log4j2
public class Init {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Environment environment;

    private static Pattern linePattern = Pattern.compile("_(\\w)");

    private static String idName = null;

    private static boolean flag = true;

    private static final String entityPackageName = "com.zcw.simple-data.entity-package";
    private static final String voPackageName = "com.zcw.simple-data.vo-package";
    private static final String controllerPackageName = "com.zcw.simple-data.controller-package";

    private String entityPackage;

    private String voPackage;

    private String controllerPackage;

    private List<String> baseFields = new ArrayList();

    private String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    @SneakyThrows
    @PostConstruct
    public void initClass() {
        String path = System.getProperty("user.dir") + "/src/main/java/";
        File file = new File(path);
        boolean isInit = ClassUtil.loop(file, "");
        if (!isInit) {
            return;
        }
        if(!environment.containsProperty(entityPackageName)){
            System.err.println("请配置实体类包路径:xxx.xxx.xxx");
            log.error("请配置实体类包路径:xxx.xxx.xxx");
            return;
        }
        if(!environment.containsProperty(voPackageName)){
            System.err.println("请配置vo包路径:xxx.xxx.xxx");
            log.error("请配置vo包路径:xxx.xxx.xxx");
            return;
        }
        if(!environment.containsProperty(controllerPackageName)){
            System.err.println("请配置controller包路径:xxx.xxx.xxx");
            log.error("请配置controller包路径:xxx.xxx.xxx");
            return;
        }
        entityPackage = environment.getProperty(entityPackageName);
        voPackage = environment.getProperty(voPackageName);
        controllerPackage = environment.getProperty(controllerPackageName);
        Map<TableAndId, List<SqlTable>> tableInfo = new HashMap();
        baseFields.add("createdAt");
        baseFields.add("updatedAt");
        baseFields.add("disabled");
        baseFields.add("deleted");
        baseFields.add("version");
        String sql = "show tables";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        list.forEach(table -> {
            for (Map.Entry<String, Object> entry : table.entrySet()) {
                String tableName = (String) entry.getValue();
                List<SqlTable> sqlTables = jdbcTemplate.query("show full fields from " + tableName, new RowMapper<SqlTable>() {
                    @Override
                    public SqlTable mapRow(ResultSet resultSet, int rowNum) throws SQLException {
                        SqlTable sqlTable = new SqlTable();
                        sqlTable.setFieldName(resultSet.getString("Field"));
                        sqlTable.setFieldType(resultSet.getString("Type"));
                        if (resultSet.getString("Key").equals("PRI")) {
                            if (flag) {
                                idName = resultSet.getString("Field");
                                flag = false;
                            }
                        }
                        return sqlTable;
                    }
                });
                TableAndId tableAndId = new TableAndId();
                tableAndId.setTableName(tableName);
                tableAndId.setIdName(lineToHump(idName));
                tableInfo.put(tableAndId, sqlTables);
            }
            flag = true;
        });
        for (Map.Entry<TableAndId, List<SqlTable>> entry : tableInfo.entrySet()) {
            generateEntity(entry);
            generateVO(entry);
            generateController(entry);
        }

    }

    private void generateController(Map.Entry<TableAndId, List<SqlTable>> entry) {
        TableAndId tableAndId = entry.getKey();
        String entityName = tableAndId.getTableName();
        entityName = lineToHump(entityName);
        entityName = entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
        String controllerName = entityName + "Controller";
        String classString = "package " + controllerPackage + ";\n" +
                "import com.zcw.simpledata.base.controller.BaseController;\n" +
                "import " + entityPackage + "." + entityName + ";\n" +
                "import " + voPackage + "." + entityName + "VO;\n" +
                "import org.springframework.web.bind.annotation.RequestMapping;\n" +
                "import org.springframework.web.bind.annotation.RestController;\n" +
                "@RestController\n" +
                "@RequestMapping(value = \"/" + lineToHump(entry.getKey().getTableName()) + "\")\n" +
                "public class " + controllerName + " extends BaseController<" + entityName + "," + entityName + "VO>{\n" +
                "\tpublic " + entityName + "Controller() {\n" +
                "        super(" + entityName + ".class, " + entityName + "VO.class);\n" +
                "\t}\n" +
                "}";
        String[] packageStr = controllerPackage.split("\\.");
        String fileName = System.getProperty("user.dir") + "/src/main/java/";
        for (String pack : packageStr) {
            fileName += (pack + "/");
        }
        fileName += (controllerName + ".java");
        generateClass(fileName, classString, controllerPackage, controllerName);
    }

    private void generateEntity(Map.Entry<TableAndId, List<SqlTable>> entry) {
        TableAndId tableAndId = entry.getKey();
        String entityName = tableAndId.getTableName();
        entityName = lineToHump(entityName);
        entityName = entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
        List<String> fieldNames = entry.getValue().stream().map((sqlTable) -> {
            String fieldName = sqlTable.getFieldName();
            fieldName = lineToHump(fieldName);
            return fieldName;
        }).collect(Collectors.toList());
        String classString = null;
        boolean isContains = fieldNames.containsAll(baseFields);
        if (isContains) {
            classString =
                    "package " + entityPackage + ";\n" +
                            "import com.zcw.simpledata.base.entity.BaseEntity;\n" +
                            "import com.zcw.simpledata.base.annotations.Id;\n" +
                            "public class " + entityName + " extends BaseEntity{\n";
        } else {
            classString =
                    "package " + entityPackage + ";\n" +
                            "import com.zcw.simpledata.base.annotations.Id;\n" +
                            "public class " + entityName + "{\n";
        }
        List<SqlTable> sqlTables = entry.getValue();
        for (SqlTable sqlTable : sqlTables) {
            String fieldName = sqlTable.getFieldName();
            fieldName = lineToHump(fieldName);
            if (baseFields.contains(fieldName) && isContains) {
                continue;
            }
            if (tableAndId.getIdName().equals(fieldName)) {
                classString += "@Id\n";
                sqlTable.setFieldType("id");
            }
            classString += "private " + sqlTable.getFieldType() + " " + fieldName + ";\n";
        }
        for (SqlTable sqlTable : sqlTables) {
            String fieldName = sqlTable.getFieldName();
            fieldName = lineToHump(fieldName);
            if (baseFields.contains(fieldName) && isContains) {
                continue;
            }
            String upFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            classString += "public void set" + upFieldName + "(" + sqlTable.getFieldType() + " " + fieldName + "){this." + fieldName + "=" + fieldName + ";}\n";
            classString += "public " + sqlTable.getFieldType() + " get" + upFieldName + "(){return this." + fieldName + ";}\n";
        }
        classString += "}";
        String[] packageStr = entityPackage.split("\\.");
        String fileName = System.getProperty("user.dir") + "/src/main/java/";
        for (String pack : packageStr) {
            fileName += (pack + "/");
        }
        fileName += (entityName + ".java");
        generateClass(fileName, classString, entityPackage, entityName);
    }

    private void generateVO(Map.Entry<TableAndId, List<SqlTable>> entry) {
        TableAndId tableAndId = entry.getKey();
        String entityName = tableAndId.getTableName();
        entityName = lineToHump(entityName);
        entityName = entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
        entityName += "VO";
        List<String> fieldNames = entry.getValue().stream().map((sqlTable) -> {
            String fieldName = sqlTable.getFieldName();
            fieldName = lineToHump(fieldName);
            return fieldName;
        }).collect(Collectors.toList());
        boolean isContains = fieldNames.containsAll(baseFields);
        String classString = null;
        if (isContains) {
            classString =
                    "package " + voPackage + ";\n" +
                            "import com.zcw.simpledata.base.entity.vo.BaseVO;\n" +
                            "public class " + entityName + " extends BaseVO{\n";
        } else {
            classString =
                    "package " + voPackage + ";\n" +
                            "public class " + entityName + "{\n";
        }

        List<SqlTable> sqlTables = entry.getValue();
        for (SqlTable sqlTable : sqlTables) {
            String fieldName = sqlTable.getFieldName();
            fieldName = lineToHump(fieldName);
            if (baseFields.contains(fieldName) && isContains) {
                continue;
            }
            classString += "private " + sqlTable.getFieldType() + " " + fieldName + ";\n";
        }
        for (SqlTable sqlTable : sqlTables) {
            String fieldName = sqlTable.getFieldName();
            fieldName = lineToHump(fieldName);
            if (baseFields.contains(fieldName) && isContains) {
                continue;
            }
            String upFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            classString += "public void set" + upFieldName + "(" + sqlTable.getFieldType() + " " + fieldName + "){this." + fieldName + "=" + fieldName + ";}\n";
            classString += "public " + sqlTable.getFieldType() + " get" + upFieldName + "(){return this." + fieldName + ";}\n";
        }
        classString += "}";
        String[] packageStr = voPackage.split("\\.");
        String fileName = System.getProperty("user.dir") + "/src/main/java/";
        for (String pack : packageStr) {
            fileName += (pack + "/");
        }
        fileName += (entityName + ".java");
        generateClass(fileName, classString, voPackage, entityName);
    }

    @SneakyThrows
    private void generateClass(String fileName, String classString, String packageName, String className) {
        File file = new File(fileName);
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(classString);
        fileWriter.flush();
        fileWriter.close();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> javaFileObjects = manager.getJavaFileObjects(fileName);
        String dest = System.getProperty("user.dir") + "/target/classes";
        List<String> options = new ArrayList<String>();
        options.add("-d");
        options.add(dest);
        JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null, options, null, javaFileObjects);
        task.call();
        manager.close();
        URL[] urls = new URL[]{new URL("file:/" + System.getProperty("user.dir") + "/target/classes")};
        ClassLoader classLoader = new URLClassLoader(urls);
        Object obj = classLoader.loadClass(packageName + "." + className).newInstance();
    }
}
