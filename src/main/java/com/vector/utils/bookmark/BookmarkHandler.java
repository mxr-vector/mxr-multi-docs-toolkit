package com.vector.utils.bookmark;

import com.vector.entity.Bookmark;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTMarkupRange;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

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


    public XWPFDocument replaceBookmarks(String path, List<Bookmark> bookmarks) {
        try {
            XWPFDocument document = loadBackupDocument(path);
            // 1. 处理文档中的段落
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                replaceInParagraph(paragraph, bookmarks);
            }

            // 2. 处理文档中的表格
            for (XWPFTable table : document.getTables()) {
                replaceInTable(table, bookmarks);
            }
            return document;
        } catch (Exception e) {
            log.info("Error replacing bookmarks: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从原文件生成备份，并返回备份文件的 XWPFDocument
     */
    private static XWPFDocument loadBackupDocument(String path) throws Exception {
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
        return new XWPFDocument(new FileInputStream(bakFile));
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
     * 在段落中查找书签并替换内容
     */
    private void replaceInParagraph(XWPFParagraph paragraph, List<Bookmark> bookmarks) {
        CTP ctp = paragraph.getCTP();
        NodeList childNodes = ctp.getDomNode().getChildNodes();

        for (CTBookmark ctBookmark : ctp.getBookmarkStartList()) {
            String bookmarkName = ctBookmark.getName();

            for (Bookmark b : bookmarks) {
                if (bookmarkName.equals(b.getName())) {
                    int bookmarkStartId = ctBookmark.getId().intValue();

                    // 找到对应的 bookmarkEnd
                    for (CTMarkupRange end : ctp.getBookmarkEndList()) {
                        if (end.getId().intValue() == bookmarkStartId) {
                            // 删除书签之间的 runs
                            removeRunsBetween(paragraph, childNodes, ctBookmark, end);

                            // 在书签结束位置插入新的 run
                            XWPFRun run = paragraph.insertNewRun(findInsertPos(paragraph, childNodes, end));
                            run.setText(b.getContext());
                            break;
                        }
                    }
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
