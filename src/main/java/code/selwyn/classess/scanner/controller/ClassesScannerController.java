/**
 * Hrfax Inc.
 * Copyright (c) 2020-2066 All Rights Reserved.
 */
package code.selwyn.classess.scanner.controller;

import code.selwyn.classess.scanner.service.ClassesScannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Selwyn
 * @since 2021/5/28
 */
@RestController
public class ClassesScannerController {

    @Autowired
    private ClassesScannerService classesScannerService;

    /**
     * 请求示例：http://localhost:8401/?projPath=/ProjData/IDEA/estage/modules
     * @param projPath
     * @return
     * @throws Exception
     */
    @GetMapping(produces = "text/html")
    public String scan(@RequestParam("projPath") String projPath) throws Exception {
        Path path = Paths.get(projPath);
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException(projPath + " is not a dir");
        }
        String scanResult = this.classesScannerService.scan(path);
        return scanResult.replaceAll("\\n", "<br/>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
    }

}
