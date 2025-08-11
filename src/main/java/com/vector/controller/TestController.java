package com.vector.controller;

import com.vector.utils.context.TtlContextHolderUtil;
import com.vector.utils.word.WordExportHandler;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.bind.annotation.*;
import com.vector.utils.pdf.PdfTableParsingEngine;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author YuanJie
 * @ClassName TestController
 * @description: 测试类
 * @date 2025/3/5 11:34
 */
@RequestMapping("/test")
@RestController
@RequiredArgsConstructor
public class TestController {

    private final PdfTableParsingEngine pdfTableParsingEngine;

    private final WordExportHandler wordExportHandler;


    @GetMapping("/aspose-pdf")
    public String asposePdf(){
        String path = "/static/入职申请表.pdf";
//        path = "/static/横向表头.pdf";
        pdfTableParsingEngine.tableAnalyze(path);
        return "hello";
    }

    @GetMapping("/poi-word")
    public void poiWord() throws IOException {
        TtlContextHolderUtil.getContext().addProperty("data", "测试隐式传值");
        String templatePath = "/static/word_demo_template.docx";
        String savePath = System.getProperty("user.dir");
        XWPFDocument document = wordExportHandler.generateWordDocument(templatePath, "word_demo_template");
        // 将文档写入本地文件
        try (FileOutputStream out = new FileOutputStream(savePath +"/" +"word_result.docx")) {
            document.write(out);
        } finally {
            document.close();
        }
    }

}
