package com.mxr.docs.excel;

import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.read.listener.ReadListener;
import cn.idev.excel.util.ListUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Function;

/**
 * @author YuanJie
 * @projectName mxr-server
 * @package com.mxr.common.utils.fastExcel
 * @className com.mxr.common.utils.fastExcel.ReadDataListener
 * @copyright Copyright 2020 mxr, Inc All rights reserved.
 * @date 2023/8/29 9:41
 * T 为数据实体类
 */
@Slf4j
public class ReadDataListener<T> implements ReadListener<T> {
    /**
     * 每隔5条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 1000;
    /**
     * 缓存的数据
     */
    private final List<T> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    private final Function<List<T>, Integer> func;

    public ReadDataListener(Function<List<T>, Integer> func) {
        this.func = func;
    }


    /**
     * 这个每一条数据解析都会来调用
     *
     * @param data    one row value. Is is same as {@link AnalysisContext#readRowHolder()}
     * @param context
     */
    @Override
    public void invoke(T data, AnalysisContext context) {
        cachedDataList.add(data);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            // 存储完成清理 list
            cachedDataList.clear();
        }
    }

    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库
        saveData();
        log.info("所有数据解析完成！");
    }

    /**
     * 加上存储数据库
     */
    private void saveData() {
        if(func == null) return;
        log.info("{}条数据，开始存储数据库！", cachedDataList.size());
        func.apply(cachedDataList);
        log.info("存储数据库成功！");
    }
}
