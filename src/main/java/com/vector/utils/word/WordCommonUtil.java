package com.vector.utils.word;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc;

/**
 * @ClassName WordCommonUtil
 * @description: TODO
 * @author YuanJie
 * @date 2025/8/11 17:42
 */
@Slf4j
public class WordCommonUtil {

    /**
     * 获取表格中的所有文本内容
     * @param table Word表格对象
     * @return 表格文本内容
     */
    public static String getTableText(XWPFTable table) {
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
    public static void setCellTextWithStyle(XWPFTableCell cell, String text) {
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
}
