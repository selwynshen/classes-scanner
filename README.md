# classes-scanner
同名类扫描工具

### 功能描述
扫描Java多模块项目下同类名，列出模块名以及包名。主要是为了清理同包同类名的问题（P.S. 正常情况下不应该有）

### 使用方法
1. checkout代码，通过IDE，启动，访问接口地址http://localhost:8888/?projPath=<java proj path to scan>
2. checkout jar包，通过java -jar 启动，然后访问1中接口地址

### 示例输出
```text
MaterialsCode.java:
    module: core, package: xx.core.constant
    module: overdue, package: xx.overdue.manage.constant
SensitiveInfoUtils.java:
    module: quartz, package: xx.report.util
    module: report, package: xx.report.infrastructure.util
```


