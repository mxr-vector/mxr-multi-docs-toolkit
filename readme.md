# Multi-Docs-Toolkit

Multi-Docs-Toolkit 是一个功能强大的文档处理工具包，支持 Excel、PDF 和 Word 文档的复杂操作。该工具包基于主流开源框架和商业组件构建，提供了高效、易用的 API 接口。

## 功能特性

### Excel 处理
- **大数据流式传输**：支持海量数据的流式导出，避免内存溢出
- **模板导出**：基于 FastExcel 和 MyBatis-Plus 实现，支持复杂模板导出
- **高性能处理**：通过批量存储和内存优化策略提升导出性能

### PDF 处理
- **文本解析**：将 PDF 文档中的文本内容解析为结构化对象
- **表格提取**：基于 Aspose.PDF 实现，支持复杂表格的识别和提取
- **结构化处理**：支持跨页表格合并和数据结构化处理

### Word 处理
- **书签替换**：支持基于书签的文本、图片、表格内容替换
- **占位符模板**：支持基于占位符的动态模板数据映射
- **高性能操作**：基于 Apache POI 实现，提供高效的 Word 文档处理能力

## 核心组件

### Excel 工具类
- `FastExcelUtil`：提供简化的 Excel 导出操作
- `Cursor`：数据游标接口，支持大数据量处理
- `CustomCellWeightStrategy`：自定义单元格宽度策略

### PDF 处理引擎
- `PdfTableParsingEngine`：PDF 表格解析引擎
- `TableBatchProcessor`：表格批处理器，支持多线程处理
- `AbstractTextMappingTemplate`：文本映射模板抽象类

### Word 处理组件
- `BookmarkHandler`：书签替换处理器
- `WordExportHandler`：Word 导出主处理器
- `AbstractDynamicTemplate`：动态表格模板抽象类
- `AbstractStaticTemplate`：静态占位符模板抽象类

## 技术栈

- **Java 版本**：JDK 21+
- **Spring Boot**：3.3.4
- **Apache POI**：5.4.1 (Word 处理)
- **Aspose PDF**：21.11 (PDF 处理)
- **Aspose Words**：24.3 (Word 处理)
- **FastExcel**：1.1.0 (Excel 处理)
- **PDFBox**：3.0.4 (PDF 处理辅助)

## 使用说明

### Excel 导出示例

```java
// 流式导出大数据量Excel
FastExcelUtil.writeExcelXlsx(response, "fileName", "sheetName", YourDataClass.class,
                            yourParam, yourService::getDataCursor);
```

### PDF 解析示例

```java
// 解析PDF文档为结构化对象
PdfTableParsingEngine engine = new PdfTableParsingEngine();
engine.tableAnalyze("path/to/your/document.pdf");
```

### Word 书签替换示例

```java
// 书签替换
List<Bookmark> bookmarks = Arrays.asList(
    new Bookmark("bookmark1", "text", "替换文本"),
    new Bookmark("bookmark2", "image", "path/to/image.jpg")
);
BookmarkHandler handler = new BookmarkHandler();
handler.replaceBookmarks("path/to/document.docx", bookmarks);
```

## 项目结构

```
src/main/java/com/vector/
├── docs/                    # 文档处理核心组件
│   ├── excel/              # Excel处理相关类
│   ├── pdf/                # PDF处理相关类
│   └── word/               # Word处理相关类
├── config/                 # 配置类
├── controller/             # 控制器
├── entity/                 # 实体类
├── enums/                  # 枚举类
├── sdk/                    # SDK接口
├── utils/                  # 工具类
└── service/                # 服务实现
```

## 依赖说明

项目使用了以下主要依赖：

1. **Aspose 组件**：商业级文档处理组件，提供强大的 PDF 和 Word 处理能力
2. **Apache POI**：开源 Word 处理框架
3. **FastExcel**：高性能 Excel 处理框架
4. **PDFBox**：开源 PDF 处理框架
5. **Spring Boot**：应用框架

## 注意事项

1. **Aspose 授权**：Aspose 属于商业产品，仅供学习研究使用
2. **JDK 版本**：项目使用虚拟线程特性，需要 JDK 21 以上版本
3. **Spring 上下文**：使用 SpringContext 获取对象上下文

## 待解决的问题

- 优化映射器逻辑，减少复杂度，提高性能
- 基于虚拟线程进一步优化性能
- 完善跨页表格处理机制
- 增强数据转义处理，提高安全性

## 许可证

本项目仅供学习研究使用，不提供任何商业授权支持。