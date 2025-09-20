package com.vector.controller;

import com.aspose.words.Document;
import com.vector.config.WordAuthLicense;
import com.vector.docs.excel.entity.UserInfo;
import com.vector.docs.word.entity.Bookmark;
import com.vector.sdk.MxrExcelService;
import com.vector.sdk.MxrPdfService;
import com.vector.sdk.MxrWordService;
import com.vector.utils.FileUtils;
import com.vector.utils.context.TtlContextHolderUtil;
import com.vector.docs.word.enums.EnumWordTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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

    private final MxrExcelService  excelService;


    @GetMapping("/aspose-pdf")
    public String asposePdf() {
        String path = System.getProperty("user.dir") + File.separator + "assets" + File.separator + "pdf" + File.separator + "struct_mapping_test1.pdf";
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
        path =  wordService.replaceBookmarks(path, bookmarks);

        try {
            // 初始化文档处理授权许可
            WordAuthLicense.setAuthLicense();
            Document document = new Document(FileUtils.openFileStream(path));
            document.updateFields();
            document.save(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return path;
    }

    /**
     * excel数据比对
     * 比对excel数据和数据库对应数据的部分属性差异
     */
    @GetMapping("/compareTo-excel")
    public void compareToExcel() throws IOException {
        String path = System.getProperty("user.dir") + File.separator + "assets" + File.separator + "excel" + File.separator + "compareTo_test.xlsx";

        excelService.compareToData(path,UserInfo.class,UserInfo::getId,this::getUsers);
    }

    private <T> List<UserInfo> getUsers(List<T> ids){
        UserInfo user1 = new UserInfo("D61626B9A85E7E9DE0530D5051AC2BD1","yuanjie","渊洁","123456","qwe","teacher");
        UserInfo user2 = new UserInfo("EmVgYvGGX4yHIgaP5kJ","yuanjie2","渊洁2","123456","wer","teacher");
        UserInfo user3 = new UserInfo("asdas","yuanjie3","渊洁3","123456","wer","teacher");

        return Stream.of(user1, user2, user3)
                .filter(u -> ids.contains(u.getId()))
                .toList();
    }


    @GetMapping("/stream-export-excel")
//    @Transactional
    public void export() throws Exception {
        Long params = 110101001000L;

//        excelService.writeExcelXlsx(
//                response,
//                "地市信息",
//                "地市区域",
//                AreaCodeDto.class,
//                params,
//                param -> testMapper.export(null));
    }
}
