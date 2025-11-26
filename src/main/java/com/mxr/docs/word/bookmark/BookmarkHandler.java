package com.mxr.docs.word.bookmark;

import com.mxr.docs.word.entity.Bookmark;
import com.mxr.docs.word.enums.EnumBookmarkType;
import com.mxr.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTMarkupRange;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Classname BookmarkHandler
 * @Description 书签替换处理
 * @Date 2025/9/14 20:25
 * @Author YuanJie
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookmarkHandler {


    /**
     * 替换文档中的书签
     *
     * @param path
     * @param bookmarks
     * @return
     */
    public String replaceBookmarks(String path, List<Bookmark> bookmarks) {
        try (XWPFDocument document = loadLocalBackupDoc(path);
             FileOutputStream fos = new FileOutputStream(path)) {
            // 1. 处理文档中的段落
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                replaceInParagraph(paragraph, bookmarks);
            }

            // 2. 处理文档中的表格
            for (XWPFTable table : document.getTables()) {
                replaceInTable(table, bookmarks);
            }

            // 写回文件（可覆盖原文件或另存）
            document.enforceUpdateFields();
            document.write(fos);
            return path;
        } catch (Exception e) {
            log.info("Error replacing bookmarks: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从原文件生成备份，并返回备份文件的 XWPFDocument
     */
    private static XWPFDocument loadLocalBackupDoc(String path) throws Exception {
        File originalFile = new File(path);
        if (!originalFile.exists()) {
            throw new IllegalArgumentException("文件不存在: " + path);
        }

        // 备份文件路径：xxx_bak.docx
        String bakPath = getBackupPath(path);
        File bakFile = new File(bakPath);

        // 如果备份文件已存在，先删除
        if (bakFile.exists()) {
            bakFile.delete();
        }

        // 把原文件改名为 _bak
        if (!originalFile.renameTo(bakFile)) {
            log.info("重命名文件失败: {}", originalFile.getAbsolutePath());
        }

        // 打开备份文件返回
        return new XWPFDocument(Files.newInputStream(bakFile.toPath()));
    }

    /**
     * 生成备份文件路径
     * e.g. D:/test.docx -> D:/test_bak.docx
     */
    private static String getBackupPath(String path) {
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex == -1) {
            return path + "_bak"; // 没有后缀的情况
        }
        return path.substring(0, dotIndex) + "_bak" + path.substring(dotIndex);
    }

    /**
     * 在段落中替换书签内容（优化版）
     *
     * @param paragraph 包含书签的段落对象
     * @param bookmarks 书签列表，包含书签名称和对应的内容
     */
    private void replaceInParagraph(XWPFParagraph paragraph, List<Bookmark> bookmarks) {
        if (bookmarks == null || bookmarks.isEmpty()) {
            return;
        }
        // 获取 paragraph 的 DOM 节点
        CTP ctp = paragraph.getCTP();
        NodeList childNodes = ctp.getDomNode().getChildNodes();
        if (childNodes.getLength() == 0) return;

        // 1. 将书签列表转为 Map，加快匹配速度
        Map<String, Bookmark> bookmarkMap = new HashMap<>(bookmarks.size());
        for (Bookmark b : bookmarks) {
            bookmarkMap.put(b.getName(), b);
        }

        // 2. 将 bookmarkEndList 建立 id -> end 映射，避免每次都全量遍历
        Map<Integer, CTMarkupRange> endMap = new HashMap<>();
        for (CTMarkupRange end : ctp.getBookmarkEndList()) {
            endMap.put(end.getId().intValue(), end);
        }

        // 3. 遍历所有书签开始标记
        for (CTBookmark ctBookmark : ctp.getBookmarkStartList()) {
            String bookmarkName = ctBookmark.getName();
            Bookmark b = bookmarkMap.get(bookmarkName);
            if (b == null) {
                continue; // 不在目标替换列表中
            }
            // 书签类型错误,下一个书签
            EnumBookmarkType bookmarkType = EnumBookmarkType.getByName(b.getType());
            if (bookmarkType == null) continue;

            int bookmarkStartId = ctBookmark.getId().intValue();
            CTMarkupRange end = endMap.get(bookmarkStartId);
            if (end == null) {
                continue; // 找不到结束标记
            }

            // 删除书签之间的 runs
            removeRunsBetween(paragraph, childNodes, ctBookmark, end);

            // 在书签结束位置插入新的 run
            int insertPos = findInsertPos(paragraph, childNodes, end);
            XWPFRun run = paragraph.insertNewRun(insertPos);

            final String context = b.getContext();
            switch (bookmarkType) {
                case TEXT -> {
                    run.setText(context);
                }
                case IMAGE -> {
                    try (InputStream imageStream = FileUtils.openFileStream(context)) {
                        BufferedImage image = ImageIO.read(imageStream);
                        int originalWidth = image.getWidth(); // 原始宽度（像素）
                        int originalHeight = image.getHeight(); // 原始高度（像素）
                        // 将图片的原始尺寸转换为 EMU
                        int emuWidth = Units.toEMU(originalWidth);
                        int emuHeight = Units.toEMU(originalHeight);

                        int picType = FileUtils.getPictureTypeForWPS(context);
                        // 读取图片文件（如图像路径，图片文件必须存在）
                        imageStream.reset();
                        run.addPicture(imageStream, picType, context, emuWidth, emuHeight); // 设置图片宽高
                    } catch (IOException | InvalidFormatException e) {
                        log.error("替换标签: {} 失败, case: {}", b.getName(), e.getMessage());
                        continue; // 如果图片读取失败，跳过当前书签替换
                    }
                }
                case TABLE -> {
                    // TODO
                }
            }

        }
    }


    /**
     * 删除书签范围内的 runs
     */
    private static void removeRunsBetween(XWPFParagraph paragraph, NodeList childNodes, CTBookmark start, CTMarkupRange end) {
        int startPos = findNodePos(childNodes, start.getDomNode());
        int endPos = findNodePos(childNodes, end.getDomNode());

        if (startPos >= 0 && endPos >= 0 && endPos > startPos) {
            // 倒序删除 run
            for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
                XWPFRun run = paragraph.getRuns().get(i);
                int runPos = findNodePos(childNodes, run.getCTR().getDomNode());
                if (runPos > startPos && runPos < endPos) {
                    paragraph.removeRun(i);
                }
            }
        }
    }

    /**
     * 找到在 bookmarkEnd 后插入 run 的位置
     */
    private static int findInsertPos(XWPFParagraph paragraph, NodeList childNodes, CTMarkupRange end) {
        int endPos = findNodePos(childNodes, end.getDomNode());
        // 找最近的 run 索引
        for (int i = 0; i < paragraph.getRuns().size(); i++) {
            int runPos = findNodePos(childNodes, paragraph.getRuns().get(i).getCTR().getDomNode());
            if (runPos > endPos) {
                return i;
            }
        }
        return paragraph.getRuns().size(); // 默认插到末尾
    }

    /**
     * 找到某个节点在 childNodes 里的位置
     */
    private static int findNodePos(NodeList nodeList, Node target) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).isSameNode(target)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 遍历表格中的所有单元格，递归处理其中的段落和嵌套表格
     */
    private void replaceInTable(XWPFTable table, List<Bookmark> bookmarks) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                // 处理单元格中的段落
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    replaceInParagraph(paragraph, bookmarks);
                }
                // 如果单元格中还有嵌套表格，递归处理
                for (XWPFTable nestedTable : cell.getTables()) {
                    replaceInTable(nestedTable, bookmarks);
                }
            }
        }
    }


}
