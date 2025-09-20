package com.vector.sdk.impl;

import cn.idev.excel.FastExcel;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.read.listener.ReadListener;
import cn.idev.excel.util.ListUtils;
import com.vector.docs.excel.CompareListener;
import com.vector.docs.excel.Cursor;
import com.vector.docs.excel.FastExcelHandler;
import com.vector.sdk.MxrExcelService;
import com.vector.utils.FileUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class MxrExcelServiceImpl implements MxrExcelService {

    private final FastExcelHandler excelHandler;

    private final HttpServletResponse response;

    @Override
    public <T> void writeExcelXlsx(String fileNameParam,
                                   String sheetName, Class<?> clazz, T t,
                                   Function<T, Cursor<?>> func) throws Exception {
        excelHandler.writeExcelXlsx(response, fileNameParam, sheetName, clazz, t, func);
    }

    @Override
    public <T, R, E> void compareToData(String path,
                                        Class<?> clazz,
                                        Function<T, R> getterField,
                                        Function<List<E>, List<?>> dumpFunc) throws IOException {
        FastExcel.read(FileUtils.openFileStream(path), clazz, new CompareListener(response,clazz, getterField, dumpFunc))
                .sheet()
                .doRead();
    }

}
