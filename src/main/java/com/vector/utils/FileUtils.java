package com.vector.utils;

import org.apache.poi.xwpf.usermodel.Document;

import java.io.*;
import java.net.URI;

public class FileUtils {

    /**
     * 根据文件路径/URL 打开图片输入流
     */
    public static InputStream openFileStream(String path) throws IOException {
        if (path == null || path.isBlank()) throw new IllegalArgumentException("路径不能为空");

        String lower = path.toLowerCase().trim();
        // 禁止 http://
        if (lower.startsWith("http://")) {
            throw new RuntimeException("不支持 http://，请使用 https:// 或 file://");
        }

        InputStream rawStream;

        // https:// 网络文件
        if (lower.startsWith("https://")) {
            rawStream =  URI.create(path).toURL().openStream();
        }

        // file:// URI
        else if (lower.startsWith("file://")) {
            try {
                URI uri = URI.create(path);
                File file = new File(uri);
                rawStream =  new FileInputStream(file);
            } catch (IllegalArgumentException e) {
                throw new IOException("非法的 file:// 路径: " + path, e);
            }
        }else {
            // 默认：按普通文件路径解析（支持绝对路径、相对路径）
            rawStream =  new FileInputStream(path);
        }
        // 使用包装流，可以方便回溯流等
        rawStream = new BufferedInputStream(rawStream);
        rawStream.mark(Integer.MAX_VALUE);
        return rawStream;
    }


    /**
     * 根据图片后缀推断 Word 支持的图片类型
     */
    public static int getPictureTypeForWPS(String filePath) {
        String lower = filePath.toLowerCase();
//        if (lower.endsWith(".emf")) return Document.PICTURE_TYPE_EMF;
//        if (lower.endsWith(".wmf")) return Document.PICTURE_TYPE_WMF;
//        if (lower.endsWith(".pict")) return Document.PICTURE_TYPE_PICT;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return Document.PICTURE_TYPE_JPEG;
        if (lower.endsWith(".png")) return Document.PICTURE_TYPE_PNG;
//        if (lower.endsWith(".dib")) return Document.PICTURE_TYPE_DIB;
        if (lower.endsWith(".gif")) return Document.PICTURE_TYPE_PNG; // Word 会自动转成 PNG
//        if (lower.endsWith(".bmp")) return Document.PICTURE_TYPE_PNG; // Word 会自动转成 PNG
        throw new IllegalArgumentException("不支持的图片格式: " + filePath);
    }
}
