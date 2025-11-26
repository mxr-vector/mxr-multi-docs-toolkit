package com.mxr.docs.excel;

import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.read.listener.ReadListener;
import cn.idev.excel.util.ListUtils;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.function.Function;

/**
 * 多例对象
 * 比对监听器
 * @param <T>
 * @param <R>
 * @param <E>
 */
public class CompareListener <T,R,E> implements ReadListener<T> {

    /**
     * 每隔5条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 1000;
    /**
     * 缓存的数据
     */
    private final List<T> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    /**
     * excel和数据库关联属性
     */
    private final Function<T,R> getterField;

    /**
     * 查询数据库方法
     */
    private final Function<List<E>,List<?>> dumpFunc;
    /**
     * 响应数据
     */
    private final HttpServletResponse response;
    /**
     * 要导出的实体头
     */
    private final Class<?> clazz;

    /**
     * @param getterField excel和数据库关联属性
     * @param dumpFunc 查询数据库方法
     */
    public CompareListener(HttpServletResponse response,Class<?> clazz,Function<T,R> getterField,Function<List<E>,List<?>> dumpFunc) {
        this.response = response;
        this.clazz = clazz;
        this.getterField = getterField;
        this.dumpFunc = dumpFunc;
    }

    /**
     * 从来源获取关键词，到数据库数据中比对，并获取比对正确的结果
     * @param o
     * @param analysisContext
     */
    @Override
    public void invoke(Object o, AnalysisContext analysisContext) {
        cachedDataList.add((T) o);
        if (cachedDataList.size() >= BATCH_COUNT) {
            List<E> fieldVals = (List<E>) cachedDataList.stream().map(getterField).toList();
            compareTo(fieldVals, dumpFunc);
            // 存储完成清理 list
            cachedDataList.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        List<E> fieldVals = (List<E>) cachedDataList.stream().map(getterField).toList();
        compareTo(fieldVals, dumpFunc);
    }

    /**
     * @param fieldVals excel收集到的key的值集合
     * @param dumpFunc 查询数据库方法
     */
    private void compareTo(List<E> fieldVals, Function<List<E>,List<?>> dumpFunc) {
        List<?> apply = dumpFunc.apply(fieldVals);
        new FastExcelHandler().exportHeadAndData(response,"compareTo.xlsx",clazz,apply,null);
    }
}
