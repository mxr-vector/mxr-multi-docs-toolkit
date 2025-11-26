package com.mxr.docs.excel;

import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.support.ExcelTypeEnum;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.idev.excel.write.metadata.style.WriteCellStyle;
import cn.idev.excel.write.style.HorizontalCellStyleStrategy;
import cn.idev.excel.write.style.column.SimpleColumnWidthStyleStrategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * FastExcel工具类，用于简化Excel导出操作
 * 该工具类结合了mybatisPlus框架实现,其中 {@link com.mxr.docs.excel.Cursor}
 */
@Slf4j
@Component
public class FastExcelHandler {

    // 日期格式化器，用于格式化日期到yyyyMMdd
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    // 批量存储最大值,也影响sheet页数
    private static final int MAX_SHEET_DATA = 50000;

    // 内存最大值
    private static final int MAX_MEMORY_DATA = 1000;

    /**
     * 使用FastExcel生成Excel  xls
     *
     * @param response      响应对象，用于输出Excel文件
     * @param fileNameParam 文件名参数
     * @param sheetName     工作表名称
     * @param clazz         数据模型类
     * @param t             泛型对象，用于获取游标
     * @param func          函数式接口，用于获取数据游标 一般是数据库交互方法
     * @throws Exception 可能抛出的异常
     */
    public <T> void writeExcelXls(HttpServletResponse response, String fileNameParam,
                                  String sheetName, Class<?> clazz, T t,
                                  Function<T, Cursor<?>> func) throws Exception {
        streamExportExcel(response, fileNameParam, sheetName, clazz, ExcelTypeEnum.XLS.getValue(), t, func);
    }

    /**
     * 使用FastExcel生成Excel  xlsx
     *
     * @param response      响应对象，用于输出Excel文件
     * @param fileNameParam 文件名参数
     * @param sheetName     工作表名称
     * @param clazz         数据模型类
     * @param t             泛型对象，用于获取游标
     * @param func          函数式接口，用于获取数据游标 一般是数据库交互方法
     * @throws Exception 可能抛出的异常
     */
    public <T> void writeExcelXlsx(HttpServletResponse response, String fileNameParam,
                                   String sheetName, Class<?> clazz, T t,
                                   Function<T, Cursor<?>> func) throws Exception {
        streamExportExcel(response, fileNameParam, sheetName, clazz, ExcelTypeEnum.XLSX.getValue(), t, func);
    }

    /**
     * 流式导出 Excel
     *
     * @param response      响应对象，用于输出Excel文件
     * @param fileNameParam 文件名参数
     * @param sheetName     工作表名称
     * @param clazz         数据模型类
     * @param excelType     Excel文件类型
     * @param t             泛型对象，用于获取游标
     * @param func          函数式接口，用于获取数据游标
     * @throws Exception 可能抛出的异常
     */
    private <T> void streamExportExcel(HttpServletResponse response, String fileNameParam,
                                       String sheetName, Class<?> clazz, String excelType,
                                       T t, Function<T, Cursor<?>> func) throws Exception {
        String fileName = fileNameParam + DATE_TIME_FORMATTER.format(LocalDateTime.now()) + excelType;
        try (OutputStream outputStream = getOutputStream(fileName, response, excelType);
             ExcelWriter excelWriter = FastExcel.write(outputStream, clazz)
                     .registerWriteHandler(new CustomCellWeightStrategy())
                     .build();
             Cursor<?> cursor = func.apply(t)) {

            if (cursor == null || !cursor.iterator().hasNext()) {
                log.warn("No data to export for file: {}", fileName);
                return;
            }

            WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
            contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
            contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            contentWriteCellStyle.setWrapped(true);

            HorizontalCellStyleStrategy horizontalCellStyleStrategy =
                    new HorizontalCellStyleStrategy(null, contentWriteCellStyle);

            List<Object> list = new ArrayList<>();
            int page = 0;
            int count = 0;
            WriteSheet writeSheet = createWriteSheet(++page, sheetName, horizontalCellStyleStrategy);

            for (Object o : cursor) {
                list.add(o);
                if (list.size() >= MAX_MEMORY_DATA) {
                    count += list.size();
                    excelWriter.write(list, writeSheet);
                    list.clear();
                    if (count >= MAX_SHEET_DATA) {
                        writeSheet = createWriteSheet(++page, sheetName, horizontalCellStyleStrategy);
                        count = 0;
                    }
                }
            }

            if (!list.isEmpty()) {
                excelWriter.write(list, writeSheet);
            }
        } catch (Exception e) {
            log.error("Error occurred while exporting Excel: {}", e.getMessage(), e);
            response.reset();
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.displayName());
//            String json = JacksonInstance.toJson(R.errorResult("下载文件失败：" + e.getMessage()));
            String json = new ObjectMapper().writeValueAsString("File download failed: " + e.getMessage());
            response.getWriter().println(json);
        }
    }

    /**
     * 创建WriteSheet对象
     *
     * @param page      页码
     * @param sheetName 工作表名称
     * @param strategy  样式策略
     * @return WriteSheet对象
     */
    private WriteSheet createWriteSheet(int page, String sheetName, HorizontalCellStyleStrategy strategy) {
        return FastExcel.writerSheet(page, sheetName + page)
                .registerWriteHandler(strategy)
                .build();
    }

    /**
     * 导出文件时为Writer生成OutputStream
     *
     * @param finalName 最终文件名
     * @param response  响应对象
     * @param excelType Excel文件类型
     * @return OutputStream对象
     * @throws Exception 可能抛出的异常
     */
    private OutputStream getOutputStream(String finalName, HttpServletResponse response, String excelType) throws Exception {
        response.reset();
        finalName = URLEncoder.encode(finalName, StandardCharsets.UTF_8);
        if (ExcelTypeEnum.XLSX.getValue().equals(excelType)) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } else if (ExcelTypeEnum.XLS.getValue().equals(excelType)) {
            response.setContentType("application/vnd.ms-excel");
        }
        response.setCharacterEncoding(StandardCharsets.UTF_8.displayName());
        response.setHeader("Content-Disposition", "attachment; filename=" + finalName);
        response.setHeader("Pragma", "public");
        response.setHeader("Cache-Control", "no-store");
        response.addHeader("Cache-Control", "max-age=0");
        return response.getOutputStream();
    }


    /**
     * 导出模板
     *
     * @param response
     * @param fileName
     * @param clazz
     * @return void
     * @author YuanJie
     * @date 2025/4/21 11:56
     */
    public void exportTemplate(HttpServletResponse response, String fileName, Class<?> clazz) {
        try {
            FastExcel.write(getOutputStream(fileName, response, ExcelTypeEnum.XLSX.getValue()))
                    .excelType(ExcelTypeEnum.XLSX)
                    .registerWriteHandler(new SimpleColumnWidthStyleStrategy(25))
                    .sheet(fileName)
                    .head(clazz)
                    .doWrite(Collections.emptyList());
        } catch (Exception e) {
            throw new RuntimeException("Excel export error", e);
        }
    }

    /**
     * 公共导出方法
     *
     * @param response  响应对象
     * @param fileName  文件名
     * @param list      数据列表 表格是二维的 外面行 里面列
     * @param excelType Excel文件类型枚举
     * @param <E>       泛型类型
     */
    public <E> void exportList(HttpServletResponse response, String fileName, List<E> list, ExcelTypeEnum excelType) {
        try {
            excelType = excelType == null ? ExcelTypeEnum.XLSX : excelType;
            fileName = fileName == null || fileName.isEmpty() ? "demo.xlsx" : fileName;
            log.debug("导出的数据行数为：{}", list.size());
            FastExcel.write(getOutputStream(fileName, response, excelType.getValue()))
                    .excelType(excelType)
                    .registerWriteHandler(new SimpleColumnWidthStyleStrategy(25))
                    .sheet(fileName)
                    .doWrite(list);
        } catch (Exception e) {
//            throw new BadCommonException("导出异常", e);
            throw new RuntimeException("Excel export error", e);
        }
    }


    /**
     * 导出包含表头和数据的Excel
     *
     * @param response 响应对象
     * @param fileName 文件名
     * @param clazz    表头列表
     * @param list     数据列表
     */
    public <E> void exportHeadAndData(HttpServletResponse response, String fileName, Class<?> clazz, List<E> list, ExcelTypeEnum excelType) {
        try {
            excelType = excelType == null ? ExcelTypeEnum.XLSX : excelType;
            fileName = fileName == null || fileName.isEmpty() ? "demo.xlsx" : fileName;
            FastExcel.write(getOutputStream(fileName, response, excelType.getValue()))
                    .excelType(excelType)
                    .registerWriteHandler(new SimpleColumnWidthStyleStrategy(25))
                    .sheet(fileName)
                    .head(clazz)
                    .doWrite(list);
        } catch (Exception e) {
//            throw new BadCommonException("导出异常", e);
            throw new RuntimeException("Excel export error", e);
        }
    }
}
