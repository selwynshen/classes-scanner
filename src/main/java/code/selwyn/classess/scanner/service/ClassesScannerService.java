/**
 * Hrfax Inc.
 * Copyright (c) 2020-2066 All Rights Reserved.
 */
package code.selwyn.classess.scanner.service;

import com.google.common.base.Splitter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Selwyn
 * @since 2021/5/28
 */
@Component
@Slf4j
public class ClassesScannerService {

    private final static String SRC_PATH_NAME = "src/main/java";

    private final Predicate<Path> excludedFolderPredicate = p -> {
        String fileName = p.getFileName().toString().toLowerCase();
        return !(fileName.startsWith(".") || fileName.equals("target"));
    };

    private List<Path> listJavaFiles(Path srcPath) throws IOException {
        try(Stream<Path> stream = Files.walk(srcPath)) {
            return stream.filter(p -> !Files.isDirectory(p))
                    .filter(p -> p.toString().toLowerCase().endsWith(".java"))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 截取获取包名
     * @param fileDirPath
     * @param srcPathName  src/main/java
     * @return
     */
    private String getPackageName(String fileDirPath, String srcPathName) {
        srcPathName = srcPathName + "/";

        fileDirPath = fileDirPath.replaceAll("\\\\", "/");
        List<String> resultList = Splitter.on(srcPathName).splitToList(fileDirPath);
        if (resultList.size() < 1) {
            return null;
        }
        return resultList.get(1).replaceAll("/", ".");
    }

    private void doCollect(Path pathToScan, final List<ModuleClasses> mcList) throws IOException{
        try (Stream<Path> stream = Files.list(pathToScan).filter(p -> Files.isDirectory(p)).filter(excludedFolderPredicate)) {
            stream.forEach(p -> {
                ModuleClasses moduleClasses = new ModuleClasses();
                moduleClasses.setModule(p.getFileName().toString());
                //扫描类名
                Path srcPath = Paths.get(p.toString(), SRC_PATH_NAME);
                if (!Files.exists(srcPath)) {
                    try {
                        this.doCollect(p, mcList);
                    } catch (IOException e) {
                        log.error("进入子模块扫描失败: {}", srcPath, e);
                    }
                } else {
                    try {
                        List<Path> javaFiles = this.listJavaFiles(srcPath);
                        List<PackageClassName> packageClassNameList =
                                javaFiles.stream().map(j -> {
                                    String packageName = getPackageName(j.getParent().toString(), SRC_PATH_NAME);
                                    return new PackageClassName(packageName, j.getFileName().toString());
                                }).collect(Collectors.toList());
                        moduleClasses.setPackgeClassNameList(packageClassNameList);
                        mcList.add(moduleClasses);
                    } catch (Exception e) {
                        log.error("扫描java文件出错", e);
                    }
                }
            });
        }
    }

    private List<ModuleClasses> collect(Path projPath) throws IOException {
        final List<ModuleClasses> mcList = new ArrayList<>();
        this.doCollect(projPath, mcList);
        return mcList;
    }

    public String scan(Path projPath) throws Exception {
        List<ModuleClasses> moduleClassesList = this.collect(projPath);
        //先平铺
        final List<ModuleClassName> flatList = new ArrayList<>();
        moduleClassesList.forEach(m -> {
            m.getPackgeClassNameList().forEach(pcn -> flatList.add(new ModuleClassName(m.getModule(), pcn.getPackageName(), pcn.getClassName())));
        });
        //然后分类
        Function<ModuleClassName, String> func = ModuleClassName::getClassName;
        Map<String, List<ModuleClassName>> classNameMap =
                flatList.stream().collect(Collectors.groupingBy(func));
        //按要求输出
        String resultStr = this.showResult(classNameMap);
        log.info("检测出的同类名信息：\n{}", resultStr);
        return resultStr;
    }

    private String showResult(Map<String, List<ModuleClassName>> classNameMap) {
        //只输出有多个list的
        final StringBuffer sb = new StringBuffer();
        classNameMap.forEach((s, l) -> {
            if (l.size() > 1) {
                sb.append(String.format("%s: \n", s));
                l.forEach(mcn -> {
                    sb.append(String.format("\tmodule: %s, package: %s\n", mcn.getModule(), mcn.getPackageName()));
                });
            }
        });
        if (sb.length() == 0) {
            sb.append("N/A");
        }
        return sb.toString();
    }


    @Data
    @NoArgsConstructor
    private static class ModuleClasses {
        private String module;
        private List<PackageClassName> packgeClassNameList = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class PackageClassName {
        private String packageName;
        private String className;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ModuleClassName {
        private String module;
        private String packageName;
        private String className;
    }
}
