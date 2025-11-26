package com.mxr.sdk.impl;

import com.mxr.sdk.MxrPdfService;
import com.mxr.docs.pdf.mapping.PdfTableParsingEngine;
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
