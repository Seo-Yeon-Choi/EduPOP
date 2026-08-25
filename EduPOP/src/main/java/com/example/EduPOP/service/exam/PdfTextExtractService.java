package com.example.EduPOP.service.exam;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PdfTextExtractService {

    public String extractText(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PDF 파일이 없습니다.");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException(
                    "PDF 파일만 업로드할 수 있습니다."
            );
        }

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();

            // 실제 표시 순서에 조금 더 가깝게
            stripper.setSortByPosition(true);

            return stripper.getText(document);

        } catch (IOException e) {

            throw new RuntimeException("PDF 텍스트 추출에 실패했습니다.", e);
        }
    }
}