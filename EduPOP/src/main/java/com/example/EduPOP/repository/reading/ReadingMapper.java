package com.example.EduPOP.repository.reading;


import com.example.EduPOP.domain.reading.Book;
import com.example.EduPOP.domain.reading.ReadingFeedback;
import com.example.EduPOP.domain.reading.ReadingReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper// MyBatis가 이 인터페이스를 독서 기능의 DB 작업용 객체로 등록
public interface ReadingMapper {
    int insertBook(
            Book book
    ); // 도서 정보를 books 테이블에 등록

    Book findBookById(
            @Param("bookId") Long bookId
    ); // 도서 번호로 도서 한 권 조회

    Book findBookByTitleAndAuthor(
            @Param("title") String title,
            @Param("author") String author
    ); // 제목과 저자가 모두 같은 도서 조회

    List<Book> searchBooksByKeyword(
            @Param("keyword") String keyword
    ); // 제목 또는 저자에 검색어가 포함된 도서 목록 조회

    int countReadingReportsByBookId(
            @Param("bookId") Long bookId
    ); // 해당 도서를 사용한 독서감상문 개수 조회

    int deleteBookById(
            @Param("bookId") Long bookId
    ); // 도서 번호가 일치하는 도서 삭제


    String findUserNameById(
            @Param("userId") Long userId
    ); // 학생 또는 교사 번호로 사용자 이름 조회


    int insertReadingReport(
            ReadingReport readingReport
    ); // 독서감상문을 reading_reports 테이블에 등록

    ReadingReport findReadingReportById(
            @Param("readingReportId") Long readingReportId
    ); // 감상문 번호로 감상문 한 개 조회

    ReadingReport findReadingReportByIdAndStudentId(
            @Param("readingReportId") Long readingReportId,
            @Param("studentId") Long studentId
    ); // 작성 학생 본인의 감상문인지 확인하면서 조회

    ReadingReport findReadingReportByIdAndTeacherId(
            @Param("readingReportId") Long readingReportId,
            @Param("teacherId") Long teacherId
    ); // 담당 교사가 열람할 수 있는 감상문인지 확인하면서 조회

    List<ReadingReport> findReadingReportsByStudentId(
            @Param("studentId") Long studentId
    ); // 특정 학생이 작성한 모든 감상문 조회

    List<ReadingReport> findReadingReportsByTeacherId(
            @Param("teacherId") Long teacherId
    ); // 담당 교사 반 학생들이 작성한 모든 감상문 조회

    int countReadingReportsByStudentIdAndBookId(
            @Param("studentId") Long studentId,
            @Param("bookId") Long bookId
    ); // 한 학생이 같은 책으로 작성한 감상문 개수 조회

    int updateReadingReportByStudentId(
            ReadingReport readingReport
    ); // 작성 학생 본인의 감상문 수정

    int deleteReadingReportByStudentId(
            @Param("readingReportId") Long readingReportId,
            @Param("studentId") Long studentId
    ); // 작성 학생 본인의 감상문 삭제


    int insertReadingFeedback(
            ReadingFeedback readingFeedback
    ); // 교사의 첨삭 내용을 reading_feedbacks 테이블에 등록

    ReadingFeedback findReadingFeedbackByReportId(
            @Param("readingReportId") Long readingReportId
    ); // 감상문 번호로 첨삭 내용 조회

    int countReadingFeedbacksByReportId(
            @Param("readingReportId") Long readingReportId
    ); // 해당 감상문에 첨삭이 있는지 개수로 확인

    int updateReadingFeedbackByReportIdAndTeacherId(
            ReadingFeedback readingFeedback
    ); // 첨삭을 작성한 교사 본인의 첨삭 내용 수정


}