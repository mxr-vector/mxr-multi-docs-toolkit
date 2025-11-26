package com.mxr.sdk.impl;

import com.mxr.docs.word.entity.Bookmark;
import com.mxr.docs.word.enums.EnumWordTemplate;
import com.mxr.sdk.MxrWordService;
import com.mxr.docs.word.bookmark.BookmarkHandler;
import com.mxr.docs.word.mapping.WordExportHandler;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MxrWordServiceImpl implements MxrWordService {

    private final WordExportHandler wordExportHandler;


    private final BookmarkHandler bookmarkHandler;
    @Override
    public XWPFDocument replacePresetPlaceholders(EnumWordTemplate enumWord) throws IOException {
        return wordExportHandler.generateWordDocument(enumWord);
    }

    @Override
    public String  replaceBookmarks(String path, List<Bookmark> bookmarks) {
        return bookmarkHandler.replaceBookmarks(path,bookmarks);
    }
}
