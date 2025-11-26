package com.mxr.sdk;


import com.mxr.docs.excel.Cursor;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/**
 * excel操作SDK 接口
 *
 * @author YuanJie
 * @date 2025/9/14 17：56
 */
public interface MxrExcelService {
    /**
     * 流式导出 Excel
     *
     * @param fileNameParam 文件名参数
     * @param sheetName     工作表名称
     * @param clazz         数据模型类
     * @param t             泛型对象，用于获取游标
     * @param func          函数式接口，用于获取数据游标
     * @throws Exception 可能抛出的异常
     */
    <T> void writeExcelXlsx(String fileNameParam,
                            String sheetName, Class<?> clazz, T t,
                            Function<T, Cursor<?>> func) throws Exception;

    /**
     * excel数据比对
     * 比对excel数据和数据库对应数据的部分属性差异
     *
     * @param path excel文件路径
     * @param getterField excel和数据库的关系key
     */
    <T, R,E> void compareToData(String path, Class<?> clazz, Function<T, R> getterField,Function<List<E>,List<?>> dumpFunc) throws IOException;
}
