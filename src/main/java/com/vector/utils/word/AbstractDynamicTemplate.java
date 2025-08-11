package com.vector.utils.word;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc;


/**
 * @ClassName AbstractDynamicTemplate
 * @description: 动态模板抽象类
 * @author YuanJie
 * @date 2025/8/11 08:53
 */
@Slf4j
public abstract class AbstractDynamicTemplate {
    /**
     * 所属word模板名称
     */
    protected String getTemplateName() {
        return "word_demo_template";
    }
    /**
     * 动态表格执行方法
     * @param table
     * @return boolean
     * @author YuanJie
     * @date 2025/8/11 08:55
     */
    protected abstract boolean execute(XWPFTable table);


}
