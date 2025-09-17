package com.vector.docs.word.enums;


import lombok.Getter;

/**
 * @author ：YuanJie
 * @ClassName EnumBookmarkType
 * @description: 书签类型枚举
 * @date ：2025/9/12 17:42
 */
@Getter
public enum EnumBookmarkType {

    /**
     * 文本
     */
    TEXT,
    /**
     * 表格
     */
    TABLE,
    /**
     * 图片
     */
    IMAGE,
    ;

    /**
     * 根据名称获取
     * @param name
     * @return
     */
    public static EnumBookmarkType getByName(String name) {
        for (EnumBookmarkType value : values()) {
            String str = value.name().toLowerCase();
            if (str.equals(name.toLowerCase())) {
                return value;
            }
        }
        return null;
    }
}
