package com.mxr.sdk.impl;

import cn.idev.excel.FastExcel;
import com.mxr.docs.excel.CompareListener;
import com.mxr.docs.excel.Cursor;
import com.mxr.docs.excel.FastExcelHandler;
import com.mxr.sdk.MxrExcelService;
import com.mxr.utils.FileUtils;
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
