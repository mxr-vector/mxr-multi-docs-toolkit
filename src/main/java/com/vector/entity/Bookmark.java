package com.vector.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Bookmark {

    /**
     * 书签名称
     */
    private String name;
    /**
     * 书签类型
     */
    private String type;

    /**
     * 书签内容
     */
    private String context;
}
