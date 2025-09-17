package com.vector.sdk.impl;

import com.vector.docs.word.entity.Bookmark;
import com.vector.docs.word.enums.EnumWordTemplate;
import com.vector.sdk.MxrWordService;
import com.vector.docs.word.bookmark.BookmarkHandler;
import com.vector.docs.word.mapping.WordExportHandler;
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
