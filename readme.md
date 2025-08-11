把生成的com文件，覆盖解压的aspose-word中，并重新打包

**aspose属于商业产品，仅供学习研究使用**
**引入虚拟线程，支持jdk21以上**
**使用SpringContext获取对象上下文**
jar cvfm aspose-words-24.3-jdk17-crack.jar META-INF/MANIFEST.MF com/

使用方式：在路径`src/main/java/com/vector/utils/pdf/handler`下，自定义映射处理器，继承TextParsingResultMapper类。

源码解析请参考：https://blog.csdn.net/m0_50913327/article/details/146550653?sharetype=blogdetail&sharerId=146550653&sharerefer=PC&sharesource=m0_50913327&spm=1011.2480.3001.8118
# 待解决的问题清单

## 1.减少映射器调用次数。解析器按页分批次交付给映射处理器。收集一页的表格交给子线程映射处理。（已完成）

### 初步方案：
（1）选择合适的线程池/虚拟线程，解析器收集一定数量的表格，提交给子线程去调用映射器。 √
（2）分页缓冲队列（容量50页，假设每页1个表格）配合超时批量提交策略。 √
（3）对象池复用Table实例，预处理元数据缓存 √

## 2.如何处理跨页分表格。 √ （已完成）

### 初步方案：
因为我们是按照页为单位进行映射处理的。因为该方式避免了大多数安全隐患。如大批量表格恶意攻击, 内存溢出风险，但是无法解决跨表数据的关联性问题
（1）结构验证：表头相似度>85%时合并（样式+列特征指纹），如手动添加一行表头，如 xx表一，xx表二。 余弦角相似匹配，如表头相同，则合并
（2）资源限制：单文档≤100页，每页≤10表，总单元格≤1000
（3）异常跳过：检测同类型表格连续重复超过5次，跳过该类型处理。

## 3.解析内容转义,避免出现特殊符号,导致字符串中断，形成执行语句。 （已完成）

### 初步方案
（1）输入过滤：白名单正则过滤非常用符号
（2）双重转义：HTML实体化+CSS编码双保险
（3）输出防护：按SQL/HTML/JSON上下文动态编码
成功拦截：DROP TABLE类注入攻击，输出无害字符

4.测试横向表头的表格

~~5.考虑ServerLoader Java SPI取代SpringContext，让其适配非Spring框架  √~~
 
6.重构余弦角相似匹配，让其至少21可用，强制用户废除8/11  √ （已完成）

7.优化映射器逻辑，减少复杂度，提高性能 √ （已完成）

8.基于虚拟线程优化性能  √ （已完成）

# Word导出功能使用说明

## 功能概述
Word导出功能基于Apache POI，支持动态表格和静态占位符的模板处理，可以根据模板生成包含数据的Word文档。

## 核心组件

### 1. WordExportHandler（主处理器）
- **位置**: `src/main/java/com/vector/utils/word/WordExportHandler.java`
- **功能**: 负责Word文档的生成和模板处理
- **主要方法**:
  - `generateWordDocument(String templatePath, String templateName)`: 生成Word文档

### 2. 抽象模板类

#### AbstractDynamicTemplate（动态表格模板）
- **位置**: `src/main/java/com/vector/utils/word/AbstractDynamicTemplate.java`
- **功能**: 处理动态表格数据，支持表格行的动态添加
- **核心方法**:
  - `execute(XWPFTable table)`: 执行动态表格处理逻辑
  - `getTemplateName()`: 返回模板名称
  - `setCellTextWithStyle()`: 设置单元格文本和样式

#### AbstractStaticTemplate（静态占位符模板）
- **位置**: `src/main/java/com/vector/utils/word/AbstractStaticTemplate.java`
- **功能**: 处理静态占位符替换
- **核心方法**:
  - `execute(XWPFParagraph paragraph)`: 执行静态占位符替换
  - `placeholderMapping()`: 定义占位符映射关系

## 使用方式

### 1. 创建动态表格处理器

在 `src/main/java/com/vector/utils/word/converter/` 目录下创建自定义动态表格处理器：

```java
@Component
@Slf4j
public class YourDynamicTemplate extends AbstractDynamicTemplate {
    
    @Override
    protected String getTemplateName() {
        return "your_template_name"; // 返回模板名称
    }
    
    @Override
    protected boolean execute(XWPFTable table) {
        try {
            // 1. 检查表格标识
            String tableText = getTableText(table);
            if (!tableText.contains("${YOUR_DYNAMIC_FLAG}")) {
                return false;
            }
            
            // 2. 获取数据
            List<YourDataModel> dataList = getYourData();
            
            // 3. 清除现有行（保留表头）
            while (table.getRows().size() > 1) {
                table.removeRow(1);
            }
            
            // 4. 添加数据行
            for (YourDataModel data : dataList) {
                XWPFTableRow row = table.createRow();
                List<XWPFTableCell> cells = row.getTableCells();
                
                // 确保有足够的单元格
                while (cells.size() < 3) {
                    row.createCell();
                    cells = row.getTableCells();
                }
                
                // 填充数据
                setCellTextWithStyle(cells.get(0), data.getField1());
                setCellTextWithStyle(cells.get(1), data.getField2());
                setCellTextWithStyle(cells.get(2), data.getField3());
            }
            
            return true;
        } catch (Exception e) {
            log.error("处理动态表格失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
```

### 2. 创建静态占位符处理器

```java
@Component
@Slf4j
public class YourStaticTemplate extends AbstractStaticTemplate {
    
    @Override
    protected String getTemplateName() {
        return "your_template_name"; // 返回模板名称
    }
    
    @Override
    protected void placeholderMapping(ConcurrentHashMap<String, String> placeholderMap) {
        // 异步获取数据
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            // 获取基本信息
            placeholderMap.put("${COMPANY_NAME}", getCompanyName());
            placeholderMap.put("${PROJECT_NAME}", getProjectName());
        });
        
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            // 获取其他信息
            placeholderMap.put("${DATE}", getCurrentDate());
            placeholderMap.put("${AUTHOR}", getAuthor());
        });
        
        // 等待所有异步任务完成
        CompletableFuture.allOf(future1, future2).join();
    }
}
```

### 3. 使用WordExportHandler生成文档

```java
@Service
public class YourService {
    
    @Autowired
    private WordExportHandler wordExportHandler;
    
    public void generateDocument() {
        try {
            // 生成Word文档
            XWPFDocument document = wordExportHandler.generateWordDocument(
                "/templates/your_template.docx", // 模板文件路径
                "your_template_name"             // 模板名称
            );
            
            // 保存文档
            try (FileOutputStream out = new FileOutputStream("output.docx")) {
                document.write(out);
            }
            
            document.close();
        } catch (IOException e) {
            log.error("生成Word文档失败: {}", e.getMessage(), e);
        }
    }
}
```

## 模板文件要求

### 1. 动态表格模板
- 在表格中添加标识符，如 `${DYNAMIC_01}`
- 保留表头行，数据行会被动态替换

### 2. 静态占位符模板
- 使用 `${PLACEHOLDER_NAME}` 格式的占位符
- 占位符会被对应的实际值替换

## 注意事项

1. **模板名称**: 每个处理器的 `getTemplateName()` 方法返回的名称必须与调用时传入的模板名称一致
2. **Spring组件**: 所有自定义处理器必须添加 `@Component` 注解以便Spring自动注入
3. **异常处理**: 建议在处理器中添加适当的异常处理逻辑
4. **性能优化**: 静态模板支持异步数据获取，可以使用 `CompletableFuture` 提高性能
5. **模板文件**: 模板文件应放在 `src/main/resources` 目录下

## 示例参考

- 动态表格示例: `src/main/java/com/vector/utils/word/converter/Dynamic_01.java`
- 静态占位符示例: `src/main/java/com/vector/utils/word/converter/Static_01.java`
