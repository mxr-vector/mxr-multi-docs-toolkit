package com.vector.utils.word;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
     * TODO 静态模板归属名 这里考虑整改
     * 因为子类实现应该是具备复用性和模块化的, 模板归属意味着仅能用一个模板，失去了子类复用能力
     */
    protected String getTemplateName() {
        return "word_demo_template";
    }
    /**
     * 替换静态占位符内容
     * @param paragraph 段落
     */
    protected void execute(XWPFParagraph paragraph) {
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
    protected abstract void placeholderMapping(ConcurrentHashMap<String, String> placeholderMap);
}
