package com.vector.utils.vector;

/**
 * @className: VectorStorage
 * @author: WJH(yuanJie)
 * @date: 2025/11/12 10:09
 * @description: 向量存储
 */
public class VectorStorage {

    // 向量值
    public double[] vector;
    // 向量模长
    public double magnitude;
    // 向量维度
    public int dimension;

    public VectorStorage(double[] vector, double magnitude, int dimension) {
        this.vector = vector;
        this.magnitude = magnitude;
        this.dimension = dimension;
    }
}
