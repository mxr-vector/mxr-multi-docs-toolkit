package com.vector.utils.word;

import lombok.Getter;

/**
 * @ClassName EnumWordTemplate
 * @description: word模板导出枚举类
 * @author YuanJie
 * @date 2025/8/15 15:51
 */
@Getter
public enum EnumWordTemplate {

    // demo 模板
    GG("gg","/static/word/word_demo_template.docx"),
    ;

    private final String name;
    private final String path;


    EnumWordTemplate(String name, String path){
        this.name = name;
        this.path = path;
    }
}
