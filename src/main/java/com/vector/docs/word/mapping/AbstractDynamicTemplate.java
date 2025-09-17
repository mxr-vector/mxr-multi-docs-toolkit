package com.vector.docs.word.mapping;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;


/**
 * @ClassName AbstractDynamicTemplate
 * @description: 动态模板抽象类
 * @author YuanJie
 * @date 2025/8/11 08:53
 */
@Slf4j
public abstract class AbstractDynamicTemplate {

    /**
     * 动态表格执行方法
     * @param table
     * @param tableText
     * @return boolean
     * @author YuanJie
     * @date 2025/8/11 08:55
     */
    protected abstract boolean execute(XWPFTable table,String tableText);


}
