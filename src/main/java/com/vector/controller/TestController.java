package com.vector.controller;

import com.vector.entity.Bookmark;
import com.vector.sdk.MxrPdfService;
import com.vector.sdk.MxrWordService;
import com.vector.utils.context.TtlContextHolderUtil;
import com.vector.enums.EnumWordTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author YuanJie
 * @ClassName TestController
 * @description: 测试类
 * @date 2025/3/5 11:34
 */
@Slf4j
@RequestMapping("/test")
@RestController
@RequiredArgsConstructor
public class TestController {

    private final MxrPdfService pdfService;


    private final MxrWordService wordService;


    @GetMapping("/aspose-pdf")
    public String asposePdf() {
        String path = System.getProperty("user.dir") + File.separator + "assets" + File.separator + "pdf" + File.separator + "入职申请表.pdf";
//        path = "/static/横向表头.pdf";
        pdfService.pdfToObject(path);
        return "hello";
    }


    /**
     * 替换预设占位符的模板并导出
     *
     * @throws IOException
     */
    @GetMapping("/placeholder-word")
    public String replacePresetPlaceholders() throws IOException {
        TtlContextHolderUtil.getContext().addProperty("data", "测试隐式传值");
        String savePath = System.getProperty("user.dir") + File.separator + "assets" + File.separator + "word" + File.separator + "word_result.docx";
        XWPFDocument document = wordService.replacePresetPlaceholders(EnumWordTemplate.GG);
        if (document == null) {
            return "error";
        }
        // 将文档写入本地文件
        try (document; FileOutputStream out = new FileOutputStream(savePath)) {
            document.write(out);
        }
        return "success";
    }

    /**
     * 书签替换导出
     *
     * @return
     */
    @GetMapping("/rep-bookmark")
    public String replaceBookmarks() throws IOException {
        Bookmark bookmark1 = new Bookmark();
        bookmark1.setName("bookmark01");
        bookmark1.setType("text");
        bookmark1.setContext("测试书签文本");

        Bookmark bookmark2 = new Bookmark();
        bookmark2.setName("bookmark02");
        bookmark2.setType("table");
        bookmark2.setContext("测试书签表格");

        Bookmark bookmark3 = new Bookmark();
        bookmark3.setName("bookmark03");
        bookmark3.setType("image");
        bookmark3.setContext(System.getProperty("user.dir")+File.separator + "assets"+File.separator+"img"+File.separator+"img.png");

        List<Bookmark> bookmarks = new ArrayList<>(List.of(bookmark1, bookmark2, bookmark3));
        String path = System.getProperty("user.dir") + File.separator + "assets" + File.separator + "word" + File.separator + "bookmark_test.docx";
        return wordService.replaceBookmarks(path, bookmarks);
    }


}
