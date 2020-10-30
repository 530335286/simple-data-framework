package com.zcw.simpledata.config;

import com.zcw.simpledata.base.exceptions.derive.LoopException;
import com.zcw.simpledata.base.utils.ClassUtil;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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

    public static boolean version = false;

    private static Pattern linePattern = Pattern.compile("_(\\w)");

    private static String idName = null;

    private static boolean flag = true;

    private static final String entityPackageName = "simple-data.entity-package";
    private static final String voPackageName = "simple-data.vo-package";
    private static final String controllerPackageName = "simple-data.controller-package";

    private String entityPackage;

    private String voPackage;

    private String controllerPackage;

    public static String mainClassName;

    public static Long cacheTime;

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

    private void errLog(boolean isEntity, boolean isVo, boolean isController) {
        if (isEntity && isVo && isController) {
            return;
        }
        if (!isEntity && !isVo && !isController) {
            log.error("Simple-Data : 请配置实体类路径:" + entityPackageName + ":xxx.xxx.xxx");
            log.error("Simple-Data : 请配置vo路径:" + voPackageName + ":xxx.xxx.xxx");
            log.error("Simple-Data : 请配置controller路径:" + controllerPackageName + ":xxx.xxx.xxx");
            return;
        }
        if (!isEntity && !isVo) {
            log.error("Simple-Data : 请配置实体类路径:" + entityPackageName + ":xxx.xxx.xxx");
            log.error("Simple-Data : 请配置vo路径:" + voPackageName + ":xxx.xxx.xxx");
            return;
        }
        if (!isVo && !isController) {
            log.error("Simple-Data : 请配置vo路径:" + voPackageName + ":xxx.xxx.xxx");
            log.error("Simple-Data : 请配置controller路径:" + controllerPackageName + ":xxx.xxx.xxx");
            return;
        }
        if (!isEntity && !isController) {
            log.error("Simple-Data : 请配置实体类路径:" + entityPackageName + ":xxx.xxx.xxx");
            log.error("Simple-Data : 请配置controller路径:" + controllerPackageName + ":xxx.xxx.xxx");
            return;
        }
        if (!isEntity) {
            log.error("Simple-Data : 请配置实体类路径:" + entityPackageName + ":xxx.xxx.xxx");
            return;
        }
        if (!isVo) {
            log.error("Simple-Data : 请配置vo路径:" + voPackageName + ":xxx.xxx.xxx");
            return;
        }
        log.error("Simple-Data : 请配置controller路径:" + controllerPackageName + ":xxx.xxx.xxx");
    }

    @SneakyThrows
    @PostConstruct
    public void initClass() {
        String path = System.getProperty("user.dir") + "/src/main/java/";
        File file = new File(path);
        boolean isInit = false;
        try {
            isInit = ClassUtil.loop(file, "");
        } catch (LoopException loopException) {
            isInit = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!isInit) {
            return;
        }
        boolean isEntity = environment.containsProperty(entityPackageName);
        boolean isVo = environment.containsProperty(voPackageName);
        boolean isController = environment.containsProperty(controllerPackageName);
        errLog(isEntity, isVo, isController);
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
        path = path.replace("/", "\\");
        mainClassName = mainClassName.replace(".", "\\");
        String MainPackageFileName = path + mainClassName + ".java";
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(MainPackageFileName)));
        StringBuffer MainJavaContent = new StringBuffer(bufferedReader.readLine());
        String str;
        while ((str = bufferedReader.readLine()) != null) {
            if (str.contains("initClass")) {
                if(str.contains("version")){
                    str = "@EnableSimpleData(version = " + version + ")";
                }else{
                    str = "@EnableSimpleData";
                }
            }
            MainJavaContent.append(str + "\n");
            str = null;
        }
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(MainPackageFileName)));
        bufferedWriter.write(MainJavaContent.toString());
        bufferedWriter.flush();
        log.info("Simple-Data : 类初始化完成 请刷新目录查看");
        System.exit(0);
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

    private void generateClass(String fileName, String classString, String packageName, String className) {
        List<String> classNameList = getClasses(packageName).stream().map((c) -> {
            return c.getSimpleName();
        }).collect(Collectors.toList());
        if (classNameList.contains(fileName)) {
            return;
        }
        File file = new File(fileName);
        try {
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
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            log.error("Simple-Data : 找不到指定的文件,类初始化中断");
            throw new LoopException("Simple-Data : 找不到指定的文件,类初始化中断");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static List<Class<?>> getClasses(String packageName) {

        //第一个class类的集合
        List<Class<?>> classes = new ArrayList<Class<?>>();
        //是否循环迭代
        boolean recursive = true;
        //获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        //定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            //循环迭代下去
            while (dirs.hasMoreElements()) {
                //获取下一个元素
                URL url = dirs.nextElement();
                //得到协议的名称
                String protocol = url.getProtocol();
                //如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    //获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    //以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    //如果是jar包文件
                    //定义一个JarFile
                    JarFile jar;
                    try {
                        //获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        //从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        //同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            //获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            //如果是以/开头的
                            if (name.charAt(0) == '/') {
                                //获取后面的字符串
                                name = name.substring(1);
                            }
                            //如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                //如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    //获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                //如果可以迭代下去 并且是一个包
                                if ((idx != -1) || recursive) {
                                    //如果是一个.class文件 而且不是目录
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        //去掉后面的".class" 获取真正的类名
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        try {
                                            //添加到classes
                                            classes.add(Class.forName(packageName + '.' + className));
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    public static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, List<Class<?>> classes) {
        //获取此包的目录 建立一个File
        File dir = new File(packagePath);
        //如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        //如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter() {
            //自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        //循环所有文件
        for (File file : dirfiles) {
            //如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(),
                        file.getAbsolutePath(),
                        recursive,
                        classes);
            } else {
                //如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    //添加到集合中去
                    classes.add(Class.forName(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
