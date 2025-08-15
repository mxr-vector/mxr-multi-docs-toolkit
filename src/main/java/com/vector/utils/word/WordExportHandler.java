package com.vector.utils.word;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @ClassName DynamicTableHandler
 * @description: 动态表格处理器
 * @author YuanJie
 * @date 2025/8/11 09:38
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WordExportHandler {
    /**
     * 动态表格模板
     */
    private final List<AbstractDynamicTemplate> dynamicTemplates;
    /**
     * 静态表格模板
     */
    private final List<AbstractStaticTemplate> staticTemplates;

    /**
     * 生成Word文档
     * @param enumWord 枚举模板
     * @return Word文档对象
     */
    public XWPFDocument generateWordDocument(EnumWordTemplate enumWord) throws IOException {
        String templatePath = enumWord.getPath();
        String templateName = enumWord.getName();
        if (templatePath==null || templatePath.isBlank())
            throw new RuntimeException("参数错误");
        if(templateName==null || templateName.isBlank())
            throw new RuntimeException("参数错误");
        // 使用模板文件
        try (InputStream inputStream = this.getClass().getResourceAsStream(templatePath);) {
            if (inputStream == null) {
                throw new IOException("模板文件未找到");
            }
            XWPFDocument document = new XWPFDocument(inputStream);
            // 处理表格数据
            handleTable(document, templateName);
            return document;
        }
    }

    /**
     * 处理表格数据
     *
     * @param document
     */
    private void handleTable(XWPFDocument document, String templateName) {
        try {
            // 替换段落中的占位符, 现在无需求
//            for (XWPFParagraph paragraph : document.getParagraphs()) {
//                handleInParagraph(paragraph,templateName);
//            }

            // 替换表格中的占位符
            for (XWPFTable table : document.getTables()) {
                // 处理动态表格数据
                log.info(">>>>>>>>>处理动态表格<<<<<<<<<<<");
                boolean b = handleDynamicTable(table);
                if (b) continue;
                log.info(">>>>>>>>>处理静态表格: {}<<<<<<<<<<<", templateName);
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            // 替换静态占位符内容
                            handleInParagraph(paragraph, templateName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("替换模板内容失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理动态表格  多个动态表格实现对应一个动态模板
     *
     * @param table
     */
    private boolean handleDynamicTable(XWPFTable table) {
        boolean flag = false;
        // 检查表格 是否包含 指定的动态表格标识
        String tableText = WordCommonUtil.getTableText(table);
        if (tableText == null || tableText.isBlank()) return false;

        for (AbstractDynamicTemplate dynamicTemplate : dynamicTemplates) {
            if(flag) break;
            flag =  dynamicTemplate.execute(table,tableText);
        }
        return flag;
    }

    /**
     * 处理静态段落  静态占位符为 一个staticTemplate对应一个静态模板
     * @param  paragraph
     * @return
     * @author YuanJie
     * @date 2025/8/11 15:06
     */
    private void handleInParagraph(XWPFParagraph paragraph, String templateName) {
        for (AbstractStaticTemplate template : staticTemplates) {
            if (templateName.equals(template.getTemplateName())) {
                template.execute(paragraph);
                return;
            }
        }
        log.warn("未找到匹配的静态表格模板: {}", templateName);
    }


}
