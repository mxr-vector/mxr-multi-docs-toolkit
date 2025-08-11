package com.vector.utils.word;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @ClassName Main
 * @description: TODO
 * @author YuanJie
 * @date 2025/8/7 21:02
 */
@Slf4j
public class Main<T, R> {

    private static final String path = "/static/word_test.docx";

    private static final String savePath = System.getProperty("user.dir");
    public static void main(String[] args) throws IOException {
        XWPFDocument document = new Main().generateWordDocument(path,null);
        // 将文档写入本地文件
        try (FileOutputStream out = new FileOutputStream(savePath +"/" +"word_result.docx")) {
            document.write(out);
        } finally {
            document.close();
        }
    }

    /**
     * 生成Word文档
     * @param path 模板文件路径
     * @param  functions 为读取多个动态表格数据做准备
     * @return Word文档对象
     */
    private XWPFDocument generateWordDocument(String path,Map<T, Function<T, R>> functions) throws IOException {
        // 使用模板文件
        try (InputStream inputStream = this.getClass().getResourceAsStream(path);) {
            if (inputStream == null) {
                throw new IOException("模板文件未找到");
            }
            XWPFDocument document = new XWPFDocument(inputStream);
            // 替换模板中的占位符
            replaceTemplateContent(document);
            return document;
        }
    }

    /**
     * 替换模板中的内容
     * @param document Word文档
     */
    private void replaceTemplateContent(XWPFDocument document) {
        try {
            // 替换段落中的占位符, 现在无需求
//            for (XWPFParagraph paragraph : document.getParagraphs()) {
//                replaceInParagraph(paragraph);
//            }

            // 替换表格中的占位符
            for (XWPFTable table : document.getTables()) {
                // 处理动态表格数据
                boolean b = processDynamicTable(table);
                if (b) continue;
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            // 替换静态占位符内容
                            replaceStaticPlaceholders(paragraph);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("替换模板内容失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理动态表格数据
     * @param table Word表格对象
     * @return 是否处理了动态表格
     */
    private boolean processDynamicTable(XWPFTable table) {
        try {
            // 检查表格 是否包含 指定的动态表格标识
            String tableText = getTableText(table);

            // 处理动态表格
            if (tableText.contains("${DYNAMIC_01}")) {
                return processBizDynamicTable(table, 3);
            }
        } catch (Exception e) {
            log.error("处理动态表格失败: {}", e.getMessage(), e);
        }

        return false;
    }

    /**
     * 获取表格中的所有文本内容
     * @param table Word表格对象
     * @return 表格文本内容
     */
    private String getTableText(XWPFTable table) {
        StringBuilder sb = new StringBuilder();
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                sb.append(cell.getText()).append(" ");
            }
        }
        return sb.toString();
    }

    /**
     * 设置表格单元格样式，保持与模板一致
     * @param cell 表格单元格
     * @param text 文本内容
     */
    private void setCellTextWithStyle(XWPFTableCell cell, String text) {
        try {
            // 清空原有内容
            while (!cell.getParagraphs().isEmpty()) {
                cell.removeParagraph(0);
            }

            // 创建新段落
            XWPFParagraph paragraph = cell.addParagraph();
            paragraph.setAlignment(ParagraphAlignment.CENTER);

            // 创建文本运行
            XWPFRun run = paragraph.createRun();
            run.setText(text != null ? text : "");
            run.setFontFamily("宋体");
            run.setFontSize(10);

            // 设置单元格垂直居中对齐
            CTTcPr tcPr = cell.getCTTc().getTcPr();
            if (tcPr == null) {
                tcPr = cell.getCTTc().addNewTcPr();
            }
            if (tcPr.getVAlign() == null) {
                tcPr.addNewVAlign().setVal(STVerticalJc.CENTER);
            } else {
                tcPr.getVAlign().setVal(STVerticalJc.CENTER);
            }

        } catch (Exception e) {
            log.warn("设置单元格样式失败: {}", e.getMessage());
            // 降级处理：直接设置文本
            cell.setText(text != null ? text : "");
        }
    }

    /**
     * 处理动态表格1
     * @param table Word表格对象
     * @return 是否成功处理
     */
    private boolean processBizDynamicTable(XWPFTable table, Integer cellNum) {
        try {
            // 获取数据
            List<String> list = List.of("test1", "test2", "test3");
            // 清除现有行（保留表头）
            while (table.getRows().size() > 1) {
                table.removeRow(1);
            }

            // 添加数据行
            for (String item : list) {
                XWPFTableRow row = table.createRow();
                List<XWPFTableCell> cells = row.getTableCells();

                // 确保有足够的单元格
                while (cells.size() < cellNum) {
                    row.createCell();
                    cells = row.getTableCells();
                }

                // 填充数据：土地用途、用地面积、土地用途占比
                setCellTextWithStyle(cells.get(0), item != null ? item : "");
                setCellTextWithStyle(cells.get(1), item != null ? item : "");
                setCellTextWithStyle(cells.get(2), item != null ? item + "%" : "");
            }

            log.info("成功处理动态表格，数据行数: {}", list.size());
            return true;

        } catch (Exception e) {
            log.error("处理动态表格失败: {}", e.getMessage(), e);
            return false;
        }
    }


    /**
     * 替换静态占位符内容
     * @param paragraph 段落
     */
    private void replaceStaticPlaceholders(XWPFParagraph paragraph) {
        log.info(">>>>>>>>>替换静态占位符内容<<<<<<<<<<<");
        String text = paragraph.getText();

        if (StringUtils.isNotBlank(text)) {
            // 收集 占位符-真实值
            ConcurrentHashMap<String, String> placeholderMap = new ConcurrentHashMap<>();
            placeholderMapping(placeholderMap);
            // 批量替换占位符
            String originalText = text;

            for (Map.Entry<String, String> entry : placeholderMap.entrySet()) {
                text = text.replace(entry.getKey(), entry.getValue());
            }

            // 清空原有内容并重新设置
            if (!text.equals(originalText)) {
                // 清空段落中的所有run
                for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
                    paragraph.removeRun(i);
                }
                // 添加新的内容
                XWPFRun run = paragraph.createRun();
                run.setText(text);
            }
        }
    }

    /**
     *  占位符映射内容
     * /static/file/hebeiGG_template.docx 模板文档
     * @param placeholderMap
     * @return void
     * @author YuanJie
     * @date 2025/8/6 16:19
     *
     */
    private void placeholderMapping(ConcurrentHashMap<String, String> placeholderMap) {
        // 1.获取建设单位信息 和 获取项目基本信息
        /**
         * {@link GgGhxkController#input}
         */
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            log.info("获取项目基本信息,threadName: {},threadId: {}", Thread.currentThread().getName(), Thread.currentThread().getId());

            placeholderMap.put("${JSDW}", Optional.ofNullable("建设单位").orElse(""));
            placeholderMap.put("${TYSHXYDM}", Optional.ofNullable("统一社会信用代码").orElse(""));
            placeholderMap.put("${LXR}", Optional.ofNullable("联系人").orElse(""));
            placeholderMap.put("${LXRDH}", Optional.ofNullable("联系人电话").orElse(""));
            placeholderMap.put("${DWDZ}", Optional.ofNullable("单位地址").orElse(""));
        });

        // 2.获取证书信息
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            log.info("获取证书信息,threadName: {},threadId: {}", Thread.currentThread().getName(), Thread.currentThread().getId());
            placeholderMap.put("${ZSBH}", Optional.ofNullable("证书编号").orElse(""));
            placeholderMap.put("${FZJG}", Optional.ofNullable("发证机关").orElse(""));
            placeholderMap.put("${FZRQ}", Optional.ofNullable("发证日期").orElse(""));
            placeholderMap.put("${ZSYXQ}", Optional.ofNullable("证书有效期").orElse(""));
            placeholderMap.put("${ZSFTFJ}", Optional.ofNullable("证书附件图").orElse(""));
        });
        String s = null;
        CompletableFuture.allOf(future1, future2).join();
        placeholderMap.put("${ZSYXQ}", Optional.ofNullable(s).orElse(""));

    }
}
