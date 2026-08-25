package com.example.EduPOP.service.reading;

import com.example.EduPOP.domain.reading.Book;
import com.example.EduPOP.domain.reading.ReadingFeedback;
import com.example.EduPOP.domain.reading.ReadingReport;
import com.example.EduPOP.repository.reading.ReadingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service // 독서 기능의 실제 처리와 판단을 담당하는 객체로 등록
@RequiredArgsConstructor // final 필드를 매개변수로 받는 생성자를 자동 생성
@Transactional(readOnly = true) // 조회 메서드는 기본적으로 DB 내용을 변경하지 않도록 설정
public class ReadingService {

    private final ReadingMapper readingMapper; // 독서 관련 SQL 실행을 Mapper에 요청하기 위해 주입


    @Transactional // 도서 등록 중 실행되는 DB 작업을 하나의 작업 단위로 처리
    public Book registerBook(
            String title,
            String author,
            String coverImageUrl
    ) {
        String normalizedTitle =
                requireText(title, "읽은 도서 제목을 입력해 주세요."); // 제목 공백 제거 및 필수 입력 확인

        String normalizedAuthor =
                normalizeOptionalText(author); // 저자가 빈 값이면 null로 변환

        String normalizedCoverImageUrl =
                normalizeOptionalText(coverImageUrl); // 표지 주소가 빈 값이면 null로 변환

        validateMaxLength(normalizedTitle, 200, "도서 제목"); // 도서 제목 최대 길이 확인
        validateMaxLength(normalizedAuthor, 100, "지은이, 저자"); // 저자 최대 길이 확인
        validateMaxLength(normalizedCoverImageUrl, 500, "표지 이미지 주소"); // 표지 주소 최대 길이 확인

        Book duplicatedBook = readingMapper.findBookByTitleAndAuthor(
                normalizedTitle,
                normalizedAuthor
        ); // 제목과 저자가 모두 같은 도서가 있는지 조회

        if (duplicatedBook != null) {
            throw new IllegalStateException(
                    "도서 제목과 지은이, 저자가 모두 같은 도서가 이미 등록되어 있습니다."
            );
        }

        Book book = new Book(); // DB에 저장할 도서 객체 생성
        book.setTitle(normalizedTitle); // 정리한 도서 제목 저장
        book.setAuthor(normalizedAuthor); // 정리한 저자 저장
        book.setCoverImageUrl(normalizedCoverImageUrl); // 정리한 표지 이미지 주소 저장

        int insertedCount = readingMapper.insertBook(book); // 도서 정보를 DB에 등록

        if (insertedCount != 1 || book.getBookId() == null) {
            throw new IllegalStateException("도서 정상 등록에 실패했습니다.");
        }

        return book; // 자동 생성된 도서 번호가 포함된 객체 반환
    }


    public Book getBook(Long bookId) {
        validateId(bookId, "도서 번호"); // 올바른 도서 번호인지 확인

        Book book = readingMapper.findBookById(bookId); // 도서 번호로 도서 조회

        if (book == null) {
            throw new IllegalStateException("해당 도서를 찾을 수 없습니다.");
        }

        return book; // 조회한 도서 반환
    }


    public List<Book> searchBooks(String keyword) {
        String normalizedKeyword =
                keyword == null ? "" : keyword.trim(); // 검색어가 없으면 빈 문자열로 변환

        return readingMapper.searchBooksByKeyword(
                normalizedKeyword
        ); // 제목 또는 저자로 도서 검색
    }

    @Transactional // 사용되지 않은 도서를 DB에서 삭제
    public void deleteBook(Long bookId) {
        validateId(bookId, "도서 번호"); // 올바른 도서 번호인지 확인

        getBook(bookId); // 삭제할 도서가 실제로 존재하는지 확인

        int readingReportCount =
                readingMapper.countReadingReportsByBookId(
                        bookId
                ); // 해당 도서를 사용한 독서감상문 개수 조회

        if (readingReportCount > 0) {
            throw new IllegalStateException(
                    "독서감상문에 사용 중인 도서는 삭제할 수 없습니다."
            );
        }

        int deletedCount =
                readingMapper.deleteBookById(
                        bookId
                ); // 사용되지 않은 도서를 DB에서 삭제

        if (deletedCount != 1) {
            throw new IllegalStateException(
                    "도서 삭제에 실패했습니다."
            );
        }
    }


    public String getUserName(Long userId) {
        validateId(userId, "사용자 번호"); // 올바른 사용자 번호인지 확인

        String userName =
                readingMapper.findUserNameById(userId); // 사용자 번호로 이름 조회

        if (userName == null || userName.isBlank()) {
            throw new IllegalStateException("해당 사용자를 찾을 수 없습니다.");
        }

        return userName; // 조회한 사용자 이름 반환
    }


    @Transactional // 감상문 등록 작업을 하나의 작업 단위로 처리
    public Long createReadingReport(
            Long studentId,
            Long bookId,
            String title,
            String content
    ) {
        validateId(studentId, "학생 번호"); // 올바른 학생 번호인지 확인
        validateId(bookId, "도서 번호"); // 올바른 도서 번호인지 확인

        getBook(bookId); // 실제로 존재하는 도서인지 확인

        String normalizedTitle =
                normalizeOptionalText(title); // 감상문 제목이 빈 값이면 null로 변환

        String normalizedContent =
                validateReadingReportContent(content); // 감상문 내용 필수 입력과 길이 확인

        validateMaxLength(
                normalizedTitle,
                200,
                "독서 감상문 제목"
        ); // 감상문 제목 최대 길이 확인

        ReadingReport readingReport =
                new ReadingReport(); // DB에 저장할 감상문 객체 생성

        readingReport.setStudentId(studentId); // 작성한 학생 번호 저장
        readingReport.setBookId(bookId); // 읽은 도서 번호 저장
        readingReport.setTitle(normalizedTitle); // 정리한 감상문 제목 저장
        readingReport.setContent(normalizedContent); // 검사한 감상문 내용 저장

        int insertedCount =
                readingMapper.insertReadingReport(readingReport); // 감상문을 DB에 등록

        if (insertedCount != 1
                || readingReport.getReadingReportId() == null) {

            throw new IllegalStateException(
                    "독서감상문 등록에 실패했습니다."
            );
        }

        return readingReport.getReadingReportId(); // 자동 생성된 감상문 번호 반환
    }


    public ReadingReport getStudentReadingReport(
            Long studentId,
            Long readingReportId
    ) {
        validateId(studentId, "학생 번호"); // 올바른 학생 번호인지 확인
        validateId(readingReportId, "감상문 번호"); // 올바른 감상문 번호인지 확인

        ReadingReport readingReport =
                readingMapper.findReadingReportByIdAndStudentId(
                        readingReportId,
                        studentId
                ); // 학생 본인이 작성한 감상문인지 확인하면서 조회

        if (readingReport == null) {
            throw new IllegalStateException(
                    "감상문이 없거나 해당 감상문에 접근할 수 없습니다."
            );
        }

        return readingReport; // 조회한 본인 감상문 반환
    }


    public List<ReadingReport> getStudentReadingReports(
            Long studentId
    ) {
        validateId(studentId, "학생 번호"); // 올바른 학생 번호인지 확인

        return readingMapper.findReadingReportsByStudentId(
                studentId
        ); // 학생이 작성한 감상문 목록 반환
    }


    public ReadingReport getTeacherReadingReport(
            Long teacherId,
            Long readingReportId
    ) {
        validateId(teacherId, "교사 번호"); // 올바른 교사 번호인지 확인
        validateId(readingReportId, "감상문 번호"); // 올바른 감상문 번호인지 확인

        ReadingReport readingReport =
                readingMapper.findReadingReportByIdAndTeacherId(
                        readingReportId,
                        teacherId
                ); // 담당 반 학생의 감상문인지 확인하면서 조회

        if (readingReport == null) {
            throw new IllegalStateException(
                    "감상문이 없거나 해당 감상문에 접근할 수 없습니다."
            );
        }

        return readingReport; // 교사가 열람할 수 있는 감상문 반환
    }


    public List<ReadingReport> getTeacherReadingReports(
            Long teacherId
    ) {
        validateId(teacherId, "교사 번호"); // 올바른 교사 번호인지 확인

        return readingMapper.findReadingReportsByTeacherId(
                teacherId
        ); // 담당 반 학생들의 감상문 목록 반환
    }


    public int getReadingCount(
            Long studentId,
            Long bookId
    ) {
        validateId(studentId, "학생 번호"); // 올바른 학생 번호인지 확인
        validateId(bookId, "도서 번호"); // 올바른 도서 번호인지 확인

        return readingMapper.countReadingReportsByStudentIdAndBookId(
                studentId,
                bookId
        ); // 같은 책으로 작성한 감상문 개수 반환
    }


    @Transactional // 감상문 수정 작업을 하나의 작업 단위로 처리
    public void updateReadingReport(
            Long studentId,
            Long readingReportId,
            String title,
            String content
    ) {
        ReadingReport savedReport =
                getStudentReadingReport(
                        studentId,
                        readingReportId
                ); // 작성 학생 본인의 감상문인지 확인

        int feedbackCount =
                readingMapper.countReadingFeedbacksByReportId(
                        readingReportId
                ); // 교사 첨삭이 등록되어 있는지 확인

        if (feedbackCount > 0) {
            throw new IllegalStateException(
                    "교사 Feedback이 등록된 감상문은 수정할 수 없습니다."
            );
        }

        String normalizedTitle =
                normalizeOptionalText(title); // 감상문 제목이 빈 값이면 null로 변환

        String normalizedContent =
                validateReadingReportContent(content); // 감상문 내용 필수 입력과 길이 확인

        validateMaxLength(
                normalizedTitle,
                200,
                "독서감상문 제목"
        ); // 감상문 제목 최대 길이 확인

        savedReport.setTitle(normalizedTitle); // 수정할 감상문 제목 저장
        savedReport.setContent(normalizedContent); // 수정할 감상문 내용 저장

        int updatedCount =
                readingMapper.updateReadingReportByStudentId(
                        savedReport
                ); // 작성 학생 본인의 감상문 수정

        if (updatedCount != 1) {
            throw new IllegalStateException(
                    "독서감상문 수정에 실패했습니다."
            );
        }
    }


    @Transactional // 감상문 삭제 작업을 하나의 작업 단위로 처리
    public void deleteReadingReport(
            Long studentId,
            Long readingReportId
    ) {
        getStudentReadingReport(
                studentId,
                readingReportId
        ); // 작성 학생 본인의 감상문인지 확인

        int feedbackCount =
                readingMapper.countReadingFeedbacksByReportId(
                        readingReportId
                ); // 교사 첨삭이 등록되어 있는지 확인

        if (feedbackCount > 0) {
            throw new IllegalStateException(
                    "교사 Feedback이 등록된 감상문은 삭제할 수 없습니다."
            );
        }

        int deletedCount =
                readingMapper.deleteReadingReportByStudentId(
                        readingReportId,
                        studentId
                ); // 작성 학생 본인의 감상문 삭제

        if (deletedCount != 1) {
            throw new IllegalStateException(
                    "독서감상문 삭제에 실패했습니다."
            );
        }
    }


    public ReadingFeedback getReadingFeedback(
            Long readingReportId
    ) {
        validateId(readingReportId, "감상문 번호"); // 올바른 감상문 번호인지 확인

        return readingMapper.findReadingFeedbackByReportId(
                readingReportId
        ); // 감상문에 등록된 첨삭 반환
    }


    @Transactional // 첨삭 등록 또는 수정 작업을 하나의 작업 단위로 처리
    public void saveReadingFeedback(
            Long teacherId,
            Long readingReportId,
            String content
    ) {
        validateId(teacherId, "교사 번호"); // 올바른 교사 번호인지 확인
        validateId(readingReportId, "감상문 번호"); // 올바른 감상문 번호인지 확인

        getTeacherReadingReport(
                teacherId,
                readingReportId
        ); // 담당 교사가 열람할 수 있는 감상문인지 확인

        String normalizedContent =
                requireText(
                        content,
                        "Feedback 내용을 입력해 주세요."
                ); // 첨삭 내용 공백 제거 및 필수 입력 확인

        validateMaxLength(
                normalizedContent,
                1000,
                "Feedback 내용"
        ); // 첨삭 내용 최대 길이 확인

        ReadingFeedback savedFeedback =
                readingMapper.findReadingFeedbackByReportId(
                        readingReportId
                ); // 기존 첨삭이 있는지 조회

        if (savedFeedback == null) {
            ReadingFeedback newFeedback =
                    new ReadingFeedback(); // 새로 등록할 첨삭 객체 생성

            newFeedback.setReadingReportId(
                    readingReportId
            ); // 첨삭 대상 감상문 번호 저장

            newFeedback.setTeacherId(
                    teacherId
            ); // 첨삭을 작성한 교사 번호 저장

            newFeedback.setContent(
                    normalizedContent
            ); // 검사한 첨삭 내용 저장

            int insertedCount =
                    readingMapper.insertReadingFeedback(
                            newFeedback
                    ); // 첨삭 내용을 DB에 등록

            if (insertedCount != 1
                    || newFeedback.getFeedbackId() == null) {

                throw new IllegalStateException(
                        "Feedback 등록에 실패했습니다."
                );
            }

            return;
        }

        if (!Objects.equals(
                savedFeedback.getTeacherId(),
                teacherId
        )) {
            throw new IllegalStateException(
                    "다른 교사가 작성한 Feedback은 수정할 수 없습니다."
            );
        }

        savedFeedback.setContent(
                normalizedContent
        ); // 기존 첨삭에 수정할 내용 저장

        int updatedCount =
                readingMapper.updateReadingFeedbackByReportIdAndTeacherId(
                        savedFeedback
                ); // 작성 교사 본인의 첨삭 내용 수정

        if (updatedCount != 1) {
            throw new IllegalStateException(
                    "Feedback 수정에 실패했습니다."
            );
        }
    }


    private String validateReadingReportContent(
            String content
    ) {
        String normalizedContent =
                requireText(
                        content,
                        "독서감상문 내용을 입력해 주세요."
                ); // 감상문 내용 공백 제거 및 필수 입력 확인

        if (normalizedContent.length() < 50) {
            throw new IllegalArgumentException(
                    "독서감상문 내용은 50자 이상 입력해 주세요."
            );
        }

        if (normalizedContent.length() > 10000) {
            throw new IllegalArgumentException(
                    "독서감상문 내용은 10000자 이하로 입력해 주세요."
            );
        }

        return normalizedContent; // 길이 검사를 통과한 감상문 내용 반환
    }


    private String requireText(
            String value,
            String errorMessage
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(errorMessage);
        }

        return value.trim(); // 필수 문자열의 앞뒤 공백을 제거해 반환
    }


    private String normalizeOptionalText(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null; // 선택 입력값이 비어 있으면 DB에 null로 저장
        }

        return value.trim(); // 선택 입력값의 앞뒤 공백을 제거해 반환
    }


    private void validateMaxLength(
            String value,
            int maxLength,
            String fieldName
    ) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + "은(는) "
                            + maxLength
                            + "자 이하로 입력해 주세요."
            );
        }
    }


    private void validateId(
            Long id,
            String fieldName
    ) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "올바른 "
                            + fieldName
                            + "이(가) 필요합니다."
            );
        }
    }
}