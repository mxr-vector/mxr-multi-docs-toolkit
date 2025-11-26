package com.mxr.utils.vector;

/**
 * @className: HotspotStorage
 * @author: WJH(yuanJie)
 * @date: 2025/11/12 10:11
 * @description: 热点词存储
 */
public class HotWordStorage {

    public String keyword;

    public float weight;

    public HotWordStorage(String keyword, float weight) {
        this.keyword = keyword.toLowerCase();
        this.weight = weight;
    }
}
