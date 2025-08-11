package com.vector.utils.word.converter.biz1;

/**
 * @ClassName Dynamic_02
 * @description: TODO
 * @author YuanJie
 * @date 2025/8/11 17:16
 */

import com.vector.utils.word.AbstractDynamicTemplate;
import com.vector.utils.word.WordCommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName Dynamic01
 * @description: 动态实例demo
 * @author YuanJie
 * @date 2025/8/11 09:08
 */
@Slf4j
@Component
public class Dynamic_02 extends AbstractDynamicTemplate {


    @Override
    protected boolean execute(XWPFTable table) {
        try {
            int cellNum = 4;
            // 检查表格 是否包含 指定的动态表格标识
            String tableText = WordCommonUtil.getTableText(table);
            // 处理动态表格
            if (!tableText.contains("${DYNAMIC_02}")) {
                return false;
            }

            // 获取数据
            List<String> list = List.of("data1", "data2", "data3","data4");
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
                WordCommonUtil.setCellTextWithStyle(cells.get(0), item != null ? item : "");
                WordCommonUtil.setCellTextWithStyle(cells.get(1), item != null ? item : "");
                WordCommonUtil.setCellTextWithStyle(cells.get(2), item != null ? item + "%" : "");
                WordCommonUtil.setCellTextWithStyle(cells.get(3), item != null ? item + "%" : "");
            }

            log.info("成功处理动态表格，数据行数: {}", list.size());
            return true;

        } catch (Exception e) {
            log.error("处理动态表格失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
