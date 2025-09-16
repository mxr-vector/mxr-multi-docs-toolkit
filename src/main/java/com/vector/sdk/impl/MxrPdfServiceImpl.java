package com.vector.sdk.impl;

import com.vector.sdk.MxrPdfService;
import com.vector.utils.pdf.PdfTableParsingEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MxrPdfServiceImpl implements MxrPdfService {

    private final PdfTableParsingEngine pdfTableParsingEngine;

    /*
     * pdf转结构化对象
     */
    @Override
    public void pdfToObject(String path) {
        pdfTableParsingEngine.tableAnalyze(path);
    }
}
