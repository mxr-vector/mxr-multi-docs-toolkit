package com.mxr.sdk;


/**
 * pdf操作SDK 接口
 * @author YuanJie
 * @date 2025/9/14 17：56
 */
public interface MxrPdfService {
    /**
     * pdf映射为结构化对象入库
     */
    void pdfToObject(String  path);
}
