package com.example.EduPOP.controller.reading.dto;

public class ReadingReportCreateRequest {

    private String bookTitle; // 읽은 책의 제목 저장
    private String bookAuthor; // 읽은 책의 저자 저장
    private String reportTitle; // 독서록 제목 저장
    private String content; // 학생이 작성한 독서록 본문 저장

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
