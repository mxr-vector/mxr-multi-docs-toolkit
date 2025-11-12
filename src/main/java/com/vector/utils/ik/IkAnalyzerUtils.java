package com.vector.utils.ik;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * @className: IKanalyzerUtils
 * @author: WJH(yuanJie)
 * @date: 2025/11/12 11:45
 * @description: 词法分析工具
 * 使用shenyanchao版本的IKAnalyzer https://github.com/blueshen/ik-analyzer
 */
@Slf4j
public class IkAnalyzerUtils {

    public static Configuration ikConfig = IKConfig.getInstance();

    public static void main(String[] args) throws IOException {
        String text = "哈利波特";
        // 创建分词对象
        ikConfig.setUseSmart(false);
        IKSegmenter ikSegmenter = new IKSegmenter(new StringReader(text), ikConfig);
        Lexeme lexeme;
        StringBuilder sb = new StringBuilder();
        while ((lexeme = ikSegmenter.next()) != null) {
            sb.append(lexeme.getLexemeText()).append(" ");
        }
        log.info("分词结果：{}", sb);
    }

    /**
     * 获取分词结果
     *
     * @param text     文本
     * @param useSmart 是否使用智能分词- false:细粒度分词  true:智能分词
     */
    public static Set<String> getSegmentResult(String text, boolean useSmart) {
        if (StringUtils.isBlank(text)) return Collections.emptySet();
        Set<String> segmentSet = new HashSet<>();
        ikConfig.setUseSmart(useSmart);
        IKSegmenter ikSegmenter = new IKSegmenter(new StringReader(text), ikConfig);
        Lexeme lexeme;
        String word;
        try {
            while ((lexeme = ikSegmenter.next()) != null) {
                word = lexeme.getLexemeText();
                if (word.length() > 1) {
                    segmentSet.add(word);
                }
            }
        } catch (IOException e) {
            log.error("中文文本分词失败，输入文本：{}", text, e);
        }
        return segmentSet;
    }
}
