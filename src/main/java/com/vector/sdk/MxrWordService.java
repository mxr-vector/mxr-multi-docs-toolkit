package com.vector.sdk;


import com.vector.entity.Bookmark;
import com.vector.enums.EnumWordTemplate;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.IOException;
import java.util.List;

/**
 * word操作SDK 接口
 * @author YuanJie
 * @date 2025/9/14 17：56
 */
public interface MxrWordService {

    /**
     * 预设模板的占位符替换
     * @param enumWord word模板枚举
     */
    XWPFDocument replacePresetPlaceholders(EnumWordTemplate enumWord) throws IOException;

    /**
     * 书签替换
     * @param path word文件路径
     * @param bookmarks 书签列表
     */
    XWPFDocument replaceBookmarks(String path,List<Bookmark> bookmarks);
}
