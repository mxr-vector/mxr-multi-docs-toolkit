package com.mxr.utils.vector;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import jdk.incubator.vector.*;
import java.util.*;

/**
 * @className: VectorUtils
 * @author: yuanJie
 * @date: 2025/11/12 08:11
 * import jdk.incubator.vector.*; 即可使用
 * jvm选项添加 --add-modules jdk.incubator.vector
 */
@Slf4j
public class VectorUtils {

    /**
     * 最大文本长度限制
     */
    public static final int MAX_STRING_LENGTH = 1000;

    /**
     * 近似阈值
     */
    public static final float MATCH_THRESHOLD = 0.7F;

    /**
     * 固定维度
     */
    public static final int FIXED_DIMENSION = 2 << 8;

    /**
     * 是否开启 jdk21+ 矢量计算
     */
    public static boolean VECTOR_API_ENABLE = true;

    private static final VectorSpecies<Float> species = FloatVector.SPECIES_PREFERRED;



    static class VectorStorage {
        // 向量值
        public float[] vector;
        // 向量模长
        public float magnitude;
        // 向量维度
        public int dimension;

        public VectorStorage(float[] vector) {
            this.vector = vector;
            this.magnitude = computeMagnitude(vector);
            this.dimension = FIXED_DIMENSION;
        }

        public float computeMagnitude(float[] vector) {
            float magnitude = 0.0F;
            for (float value : vector) {
                magnitude += value * value;
            }
            return (float) Math.sqrt(magnitude);
        }


    }

    /**
     * 计算内容相似度（基于矢量相似度）
     * 句子 A 向量 vs 句子 B 向量
     *
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 内容相似度
     */
    public static float calculateContentSimilarity(String str1, String str2) {
        // 处理输入字符串
        str1 = preProcessText(str1);
        str2 = preProcessText(str2);

        // 将字符串转换为特征向量
        VectorStorage vector1 = stringToVector(str1);
        VectorStorage vector2 = stringToVector(str2);

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
    public static float calculateHotSpotSimilarity(String str, List<HotWordStorage> hotWords) {
        if (CollectionUtils.isEmpty(hotWords)) return 0.0F;
        // 处理输入字符串
        str = preProcessText(str);

        // 将字符串转换为特征向量
        VectorStorage vector1 = stringToVector(str);
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
    private static VectorStorage stringToVector(String str) {
        // 初始化特征向量
        float[] vector = new float[FIXED_DIMENSION];

        // 将字符频率映射到特征向量
        for (char c : str.toCharArray()) {

            int h1 = mix32(c);
            int index1 = h1 & (FIXED_DIMENSION - 1);
            int h2 = mix32(h1 ^ 0x9e3779b9);
            int index2 = h2 & (FIXED_DIMENSION - 1); // 第二次 hash增强特征
            vector[index1] += 1.0F;
            vector[index2] += 0.5F;            // 降低第二 hash 权重
        }

        // 归一化向量
        return normalizeVector(vector);
    }

    /** 32bit AVA 混合函数（极快，高质量） */
    private static int mix32(int x) {
        x ^= (x >>> 16); // 高低位第一次混合
        x *= 0x7feb352d; // 扩散 bit 模式
        x ^= (x >>> 15); // 修正低位偏差
        x *= 0x846ca68b; // 再扩散，保证接近均匀
        x ^= (x >>> 16); // 雪崩定型
        return x;
    }

    /**
     * 构建热点词特征向量
     */
    private static VectorStorage buildHotspotVector(List<HotWordStorage> hotWords) {
        float[] v = new float[FIXED_DIMENSION];
        for (HotWordStorage hotWord : hotWords) {
            VectorStorage curWord = stringToVector(hotWord.keyword);
            for (int i = 0; i < curWord.dimension; i++) {
                // 加权求和,避免双语导致分数被稀释
                v[i] += curWord.vector[i] * hotWord.weight;
            }
        }
        return normalizeVector(v);
    }


    /**
     * 归一化向量
     *
     * @param vector 输入向量
     */
    private static VectorStorage normalizeVector(float[] vector) {
        VectorStorage vec = new VectorStorage(vector);
        // 归一化向量
        if (vec.magnitude > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= vec.magnitude;
            }
        }
        return vec;
    }

    /**
     * 计算余弦相似度
     *
     * @param storage1 向量1
     * @param storage2 向量2
     * @return 余弦相似度
     */
    private static float cosSimilarity(VectorStorage storage1, VectorStorage storage2) {
        if (storage1.magnitude < 1 || storage2.magnitude < 1) {
            return 0.0F;
        }
        float dotProduct = 0.0F; // 矢量点积
        float[] vec1 = storage1.vector;
        float[] vec2 = storage2.vector;
        if(VECTOR_API_ENABLE){
            int loopBound = species.loopBound(FIXED_DIMENSION);
            for (int i = 0; i < loopBound; i += species.length()) {
                FloatVector v1 = FloatVector.fromArray(species, vec1, i);
                FloatVector v2 = FloatVector.fromArray(species, vec2, i);
                dotProduct += v1.mul(v2).reduceLanes(VectorOperators.ADD);
            }
        }else {
            for (int i = 0; i < FIXED_DIMENSION; i++) {
                dotProduct += vec1[i] * vec2[i];
            }
        }
        return dotProduct / (storage1.magnitude * storage2.magnitude);
    }

    static void main() {
        log.info("{}", calculateContentSimilarity("helho world", "hello world"));
        VECTOR_API_ENABLE = false;
        log.info("{}", calculateContentSimilarity("helho world", "hello world"));
    }
}
