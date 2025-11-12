package com.vector.utils.vector;

import com.vector.utils.ik.IkAnalyzerUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @className: VectorUtils
 * @author: WJH(yuanJie)
 * @date: 2025/11/12 08:11
 * @description: TODO后期优化为基于 jdk vector接口的 实现 （依赖jdk版本）
 */
public class VectorUtils {

    /**
     * 最大文本长度限制
     */
    public static final int MAX_STRING_LENGTH = 1000;

    /**
     * 近似阈值
     */
    public static final double MATCH_THRESHOLD = 0.7;

    /**
     * 计算内容相似度（基于矢量相似度）
     * 句子 A 向量 vs 句子 B 向量
     *
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 内容相似度
     */
    public static double calculateContentSimilarity(String str1, String str2) {
        // 处理输入字符串
        str1 = preProcessText(str1);
        str2 = preProcessText(str2);

        // 适配合适的向量维度
        int dimension = Math.max(str1.length(), str2.length());

        // 将字符串转换为特征向量
        VectorStorage vector1 = stringToVector(str1, dimension);
        VectorStorage vector2 = stringToVector(str2, dimension);

        // 计算余弦相似度
        return cosSimilarity(vector1, vector2);
    }

    /**
     * 计算句子在热点词 中的 匹配度
     * 句子向量 vs 「热点词集合的理想向量」
     *
     * @param str      输入字符串
     * @param hotWords 输入热点词集合
     * @return 内容相似度
     */
    public static double calculateHotSpotSimilarity(String str, List<HotWordStorage> hotWords) {
        if (CollectionUtils.isEmpty(hotWords)) return 0.0;
        // 处理输入字符串
        str = preProcessText(str);

        // 将字符串转换为特征向量
        VectorStorage vector1 = stringToVector(str, hotWords);
        // 创建热点词换为特征向量
        VectorStorage vector2 = buildHotspotVector(hotWords);
        return cosSimilarity(vector1, vector2);
    }

    /**
     * 文本清洗
     */
    public static String preProcessText(String text) {
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("输入字符串不能为空");
        }
        if (text.length() > MAX_STRING_LENGTH) {
            throw new IllegalArgumentException("输入字符串长度不能超过" + MAX_STRING_LENGTH);
        }
        return text.replace(" ", "").toLowerCase();
    }

    /**
     * 将字符串转换为特征向量
     *
     * @param str 输入字符串
     * @return 特征向量
     */
    private static VectorStorage stringToVector(String str, int dimension) {
        // 初始化特征向量
        double[] vector = new double[dimension];

        // 创建字符频率映射
        Map<Character, Integer> charFrequency = new HashMap<>();

        // 统计字符频率
        for (char c : str.toCharArray()) {
            charFrequency.put(c, charFrequency.getOrDefault(c, 0) + 1);
        }

        // 将字符频率映射到特征向量
        for (char c : charFrequency.keySet()) {
            int index = Math.abs(c) % dimension;
            vector[index] += charFrequency.get(c);
        }

        // 归一化向量
        return normalizeVector(vector);
    }

    /**
     * 将句子转换为特征向量
     *
     * @param str      输入字符串
     * @param hotWords 热点词集合
     * @return 热点词特征向量
     */
    private static VectorStorage stringToVector(String str, List<HotWordStorage> hotWords) {
        // 创建热点词向量
        int dimension = hotWords.size();

        // ik分词
        Set<String> segmentResult = IkAnalyzerUtils.getSegmentResult(str, false);
        double[] vector = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            HotWordStorage hotWordStorage = hotWords.get(i);
            if (hotWordStorage == null) continue;
            String hotWord = preProcessText(hotWordStorage.keyword);
            /**
             * TODO 可能需要优化为近似阈值匹配
             * 目标 关键词组存在即匹配（粗粒度、高覆盖率）
             * 热词组 -> 句子向量
             * 现在设计分析：
             *     热词组向量化的局限性：将热词组映射为句子向量时，
             *          由于单句无法同时包含某一热词组（如动作库热词组）中的所有词汇，
             *          会导致向量中大部分维度失活，进而降低匹配度。
             *     动作词汇的匹配要求：无论单句包含动作热词组中的哪类动作词汇（如 “下蹲、爬行、跑、跳跃” 等），
             *          都应被匹配至动作处理器中进行处理。
             *     句子分词匹配的弊端：若对句子进行分词后再执行热词匹配，不仅可能降低匹配度、违背高覆盖率的核心目标，
             *          其实际效果与直接判断原句是否包含热词也几乎无差异。
             * 因此考虑：
             * 方案1.放弃待分析字符串和热词组的近似匹配
             * 方案2.仅对热词组本身进行分词处理；
             * 方案3.采用 “分词后字符串与热词组中每个词汇近似匹配并累加积分” 的方式，取消全局统一匹配阈值；
             * 转而选取累计积分最高的热词组作为最终匹配结果。
             */

            vector[i] = segmentResult.contains(hotWord) ? hotWordStorage.weight : 0.0;
        }

        // 归一化向量
        return normalizeVector(vector);
    }

    /**
     * 构建热点词特征向量
     */
    private static VectorStorage buildHotspotVector(List<HotWordStorage> hotWords) {
        int dimension = hotWords.size();
        double[] vector = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            HotWordStorage hotWordStorage = hotWords.get(i);
            if (hotWordStorage == null) continue;
            vector[i] = hotWordStorage.weight;
        }
        return normalizeVector(vector);
    }


    /**
     * 归一化向量
     *
     * @param vector 输入向量
     */
    private static VectorStorage normalizeVector(double[] vector) {
        double magnitude = 0.0;


        // 计算向量模长
        for (double value : vector) {
            magnitude += value * value;
        }
        magnitude = Math.sqrt(magnitude);

        // 归一化向量
        if (magnitude > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= magnitude;
            }
        }
        return new VectorStorage(vector, magnitude, vector.length);
    }

    /**
     * 计算余弦相似度
     *
     * @param storage1 向量1
     * @param storage2 向量2
     * @return 余弦相似度
     */
    private static double cosSimilarity(VectorStorage storage1, VectorStorage storage2) {
        if (storage1.dimension != storage2.dimension) {
            throw new IllegalArgumentException("向量维度不匹配");
        }
        if (storage1.magnitude == 0.0 || storage2.magnitude == 0.0) {
            return 0.0;
        }
        double dotProduct = 0.0; // 矢量点积
        double[] vec1 = storage1.vector;
        double[] vec2 = storage2.vector;
        for (int i = 0; i < storage1.dimension; i++) {
            dotProduct += vec1[i] * vec2[i];
        }
        return dotProduct / (storage1.magnitude * storage2.magnitude);
    }
}
