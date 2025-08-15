package com.vector.utils.word;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName AbstractStaticTemplate
 * @description: 静态模板占位符处理抽象类
 * @author YuanJie
 * @date 2025/8/11 15:16
 */
@Slf4j
public abstract class AbstractStaticTemplate {

    /**
     * 所属word模板名称
     * 静态占位符整体决定了一个word骨架。可以是1对1.动态占位符则是1对多.
     * 因此静态模板子类实现需要继承该抽象类，并实现该方法。
     */
    protected abstract String getTemplateName();

    /**
     * 替换静态占位符内容
     * @param paragraph 段落
     */
    protected void execute(XWPFParagraph paragraph) {
        log.info(">>>>>>>>>替换静态占位符内容<<<<<<<<<<<");
        String text = paragraph.getText();

        if (text == null || text.isBlank()) return;
        // 收集 占位符-真实值
        ConcurrentHashMap<String, String> placeholderMap = new ConcurrentHashMap<>();
        placeholderMapping(placeholderMap);
        // 批量替换占位符
        String originalText = text;

        for (Map.Entry<String, String> entry : placeholderMap.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }


        if (text.equals(originalText)) return;
        // 清空原有内容并重新设置
        // 清空段落中的所有run
        for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
            paragraph.removeRun(i);
        }
        // 添加新的内容
        XWPFRun run = paragraph.createRun();
        run.setText(text);
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
    protected abstract void placeholderMapping(ConcurrentHashMap<String, String> placeholderMap);
}
