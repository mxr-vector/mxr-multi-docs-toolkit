package com.vector.utils.vector;

/**
 * @className: HotspotStorage
 * @author: WJH(yuanJie)
 * @date: 2025/11/12 10:11
 * @description: 热点词存储
 */
public class HotWordStorage {

    public String keyword;

    public double weight;

    public HotWordStorage(String keyword, double weight) {
        this.keyword = keyword;
        this.weight = weight;
    }
}
