package com.example.EduPOP.controller.reading;

import com.example.EduPOP.domain.reading.Book;
import com.example.EduPOP.domain.reading.ReadingFeedback;
import com.example.EduPOP.domain.reading.ReadingReport;
import com.example.EduPOP.service.reading.ReadingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller // 학생과 교사의 독서 관련 웹 요청을 받는 객체로 등록
@RequiredArgsConstructor // final 필드를 매개변수로 받는 생성자를 자동 생성
public class ReadingController {
    private static final String LOGIN_USER_ID =
            "loginUserId"; // 로그인한 사용자 번호를 저장할 세션 이름

    private final ReadingService readingService; // 실제 독서 처리를 Service에 요청하기 위해 주입


    @GetMapping("/student/reading") // 학생 독서 메뉴의 감상문 목록 요청 처리
    public String studentReadingList(
            HttpSession session,
            Model model
    ) {
        Long studentId =
                getLoginUserId(session); // 세션에서 로그인한 학생 번호 조회

        if (studentId == null) {
            return "redirect:/login"; // 로그인 정보가 없으면 로그인 화면으로 이동
        }

        addStudentReadingListModel(
                studentId,
                model
        ); // 학생 감상문 목록에 필요한 데이터를 Model에 저장

        return "student/reading-report/list"; // 학생 감상문 목록 HTML 실행
    }


    @GetMapping("/student/reading-reports/create") // 감상문 작성 화면 요청 처리
    public String studentReadingReportCreateForm(
            @RequestParam(
                    name = "keyword",
                    required = false,
                    defaultValue = ""
            ) String keyword,

            @RequestParam(
                    name = "bookId",
                    required = false
            ) Long selectedBookId,

            HttpSession session,
            Model model
    ) {
        Long studentId =
                getLoginUserId(session); // 세션에서 로그인한 학생 번호 조회

        if (studentId == null) {
            return "redirect:/login"; // 로그인 정보가 없으면 로그인 화면으로 이동
        }

        model.addAttribute(
                "books",
                readingService.searchBooks(keyword)
        ); // 검색어에 해당하는 도서 목록 전달

        model.addAttribute(
                "keyword",
                keyword
        ); // 입력했던 검색어를 화면에 다시 전달

        model.addAttribute(
                "selectedBookId",
                selectedBookId
        ); // 선택한 도서 번호를 화면에 전달

        model.addAttribute(
                "book",
                new Book()
        ); // 도서 등록 폼에서 사용할 빈 도서 객체 전달

        model.addAttribute(
                "readingReport",
                new ReadingReport()
        ); // 감상문 작성 폼에서 사용할 빈 감상문 객체 전달

        return "student/reading-report/create"; // 학생 감상문 작성 HTML 실행
    }


    @PostMapping("/student/reading/books") // 학생이 입력한 새 도서 등록 요청 처리
    public String registerBook(
            @ModelAttribute("book") Book book,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Long studentId =
                getLoginUserId(session); // 세션에서 로그인한 학생 번호 조회

        if (studentId == null) {
            return "redirect:/login"; // 로그인 정보가 없으면 로그인 화면으로 이동
        }

        try {
            Book savedBook =
                    readingService.registerBook(
                            book.getTitle(),
                            book.getAuthor(),
                            book.getCoverImageUrl()
                    ); // 입력받은 도서 정보를 Service에 전달해 등록

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "도서가 등록되었습니다."
            ); // 이동한 화면에 도서 등록 성공 문구 전달

            redirectAttributes.addAttribute(
                    "bookId",
                    savedBook.getBookId()
            ); // 등록한 도서를 바로 선택하도록 도서 번호 전달

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            ); // 이동한 화면에 도서 등록 실패 이유 전달
        }

        return "redirect:/student/reading-reports/create"; // 감상문 작성 화면으로 이동
    }


    @PostMapping("/student/reading-reports") // 학생이 작성한 감상문 등록 요청 처리
    public String createReadingReport(
            @ModelAttribute("readingReport") ReadingReport readingReport,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Long studentId =
                getLoginUserId(session); // 세션에서 로그인한 학생 번호 조회

        if (studentId == null) {
            return "redirect:/login"; // 로그인 정보가 없으면 로그인 화면으로 이동
        }

        try {
            Long readingReportId =
                    readingService.createReadingReport(
                            studentId,
                            readingReport.getBookId(),
                            readingReport.getTitle(),
                            readingReport.getContent()
                    ); // 학생 번호와 감상문 입력값을 Service에 전달해 등록

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "독서감상문이 등록되었습니다."
            ); // 이동한 화면에 감상문 등록 성공 문구 전달

            return "redirect:/student/reading-reports/"
                    + readingReportId; // 등록한 감상문 상세 화면으로 이동

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            ); // 이동한 화면에 감상문 등록 실패 이유 전달

            if (readingReport.getBookId() != null) {
                redirectAttributes.addAttribute(
                        "bookId",
                        readingReport.getBookId()
                ); // 실패 후에도 선택했던 도서 번호 유지
            }

            return "redirect:/student/reading-reports/create"; // 작성 화면으로 이동
        }
    }


    @GetMapping("/student/reading-reports/{readingReportId}") // 학생 감상문 상세 화면 요청 처리
    public String studentReadingReportDetail(
            @PathVariable("readingReportId") Long readingReportId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long studentId =
                getLoginUserId(session); // 세션에서 로그인한 학생 번호 조회

        if (studentId == null) {
            return "redirect:/login"; // 로그인 정보가 없으면 로그인 화면으로 이동
        }

        try {
            ReadingReport readingReport =
                    readingService.getStudentReadingReport(
                            studentId,
                            readingReportId
                    ); // 학생 본인이 작성한 감상문인지 확인하면서 조회

            Book book =
                    readingService.getBook(
                            readingReport.getBookId()
                    ); // 감상문에 연결된 도서 조회

            ReadingFeedback feedback =
                    readingService.getReadingFeedback(
                            readingReportId
                    ); // 감상문에 등록된 첨삭 조회

            model.addAttribute(
                    "readingReport",
                    readingReport
            ); // 감상문 정보를 상세 화면에 전달

            model.addAttribute(
                    "book",
                    book
            ); // 도서 정보를 상세 화면에 전달

            model.addAttribute(
                    "feedback",
                    feedback
            ); // 첨삭 정보를 상세 화면에 전달

            model.addAttribute(
                    "readingCount",
                    readingService.getReadingCount(
                            studentId,
                            readingReport.getBookId()
                    )
            ); // 같은 책으로 작성한 전체 감상문 개수 전달

            if (feedback != null) {
                model.addAttribute(
                        "feedbackTeacherName",
                        readingService.getUserName(
                                feedback.getTeacherId()
                        )
                ); // 첨삭을 작성한 교사 이름 전달
            }

            return "student/reading-report/detail"; // 학생 감상문 상세 HTML 실행

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            ); // 이동한 화면에 상세 조회 실패 이유 전달

            return "redirect:/student/reading"; // 학생 감상문 목록 화면으로 이동
        }
    }


    @GetMapping("/student/reading-reports/{readingReportId}/edit") // 감상문 수정 화면 요청 처리
    public String studentReadingReportEditForm(
            @PathVariable("readingReportId") Long readingReportId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long studentId =
                getLoginUserId(session); // 세션에서 로그인한 학생 번호 조회

        if (studentId == null) {
            return "redirect:/login"; // 로그인 정보가 없으면 로그인 화면으로 이동
        }

        try {
            ReadingReport readingReport =
                    readingService.getStudentReadingReport(
                            studentId,
                            readingReportId
                    ); // 학생 본인의 감상문인지 확인하면서 조회

            ReadingFeedback feedback =
                    readingService.getReadingFeedback(
                            readingReportId
                    ); // 교사 첨삭 등록 여부 조회

            if (feedback != null) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "교사 첨삭이 등록된 감상문은 수정할 수 없습니다."
                ); // 상세 화면에 수정 불가 이유 전달

                return "redirect:/student/reading-reports/"
                        + readingReportId; // 감상문 상세 화면으로 이동
            }

            model.addAttribute(
                    "readingReport",
                    readingReport
            ); // 수정할 감상문 정보를 화면에 전달

            model.addAttribute(
                    "book",
                    readingService.getBook(
                            readingReport.getBookId()
                    )
            ); // 수정 화면에 연결된 도서 정보 전달

            return "student/reading-report/edit"; // 학생 감상문 수정 HTML 실행

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            ); // 이동한 화면에 수정 화면 조회 실패 이유 전달

            return "redirect:/student/reading"; // 학생 감상문 목록 화면으로 이동
        }
    }


    @PostMapping("/student/reading-reports/{readingReportId}/edit") // 학생 감상문 수정 요청 처리
    public String updateReadingReport(
            @PathVariable("readingReportId") Long readingReportId,
            @ModelAttribute("readingReport") ReadingReport readingReport,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Long studentId =
                getLoginUserId(session); // 세션에서 로그인한 학생 번호 조회

        if (studentId == null) {
            return "redirect:/login"; // 로그인 정보가 없으면 로그인 화면으로 이동
        }

        try {
            readingService.updateReadingReport(
                    studentId,
                    readingReportId,
                    readingReport.getTitle(),
                    readingReport.getContent()
            ); // 학생 번호와 수정할 감상문 값을 Service에 전달

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "독서감상문이 수정되었습니다."
            ); // 이동한 화면에 감상문 수정 성공 문구 전달

            return "redirect:/student/reading-reports/"
                    + readingReportId; // 수정한 감상문 상세 화면으로 이동

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            ); // 이동한 화면에 감상문 수정 실패 이유 전달

            return "redirect:/student/reading-reports/"
                    + readingReportId
                    + "/edit"; // 감상문 수정 화면으로 이동
        }
    }


    @PostMapping("/student/reading-reports/{readingReportId}/delete") // 학생 감상문 삭제 요청 처리
    public String deleteReadingReport(
            @PathVariable("readingReportId") Long readingReportId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Long studentId =
                getLoginUserId(session); // 세션에서 로그인한 학생 번호 조회

        if (studentId == null) {
            return "redirect:/login"; // 로그인 정보가 없으면 로그인 화면으로 이동
        }

        try {
            readingService.deleteReadingReport(
                    studentId,
                    readingReportId
            ); // 학생 본인의 감상문 삭제를 Service에 요청

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "독서감상문이 삭제되었습니다."
            ); // 목록 화면에 감상문 삭제 성공 문구 전달

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            ); // 목록 화면에 감상문 삭제 실패 이유 전달
        }

        return "redirect:/student/reading"; // 학생 감상문 목록 화면으로 이동
    }


    @GetMapping("/teacher/reading") // 교사 독서 메뉴의 담당 학생 감상문 목록 요청 처리
    public String teacherReadingList(
            HttpSession session,
            Model model
    ) {
        Long teacherId =
                getLoginUserId(session); // 세션에서 로그인한 교사 번호 조회

        if (teacherId == null) {
            return "redirect:/login"; // 로그인 정보가 없으면 로그인 화면으로 이동
        }

        addTeacherReadingListModel(
                teacherId,
                model
        ); // 교사 감상문 목록에 필요한 데이터를 Model에 저장

        return "teacher/reading-report/list"; // 교사 감상문 목록 HTML 실행
    }


    @GetMapping("/teacher/reading-reports/{readingReportId}") // 교사 감상문 상세 화면 요청 처리
    public String teacherReadingReportDetail(
            @PathVariable("readingReportId") Long readingReportId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long teacherId =
                getLoginUserId(session); // 세션에서 로그인한 교사 번호 조회

        if (teacherId == null) {
            return "redirect:/login"; // 로그인 정보가 없으면 로그인 화면으로 이동
        }

        try {
            ReadingReport readingReport =
                    readingService.getTeacherReadingReport(
                            teacherId,
                            readingReportId
                    ); // 담당 반 학생이 작성한 감상문인지 확인하면서 조회

            ReadingFeedback feedback =
                    readingService.getReadingFeedback(
                            readingReportId
                    ); // 기존 첨삭 내용 조회

            if (feedback == null) {
                feedback =
                        new ReadingFeedback(); // 입력 폼에서 사용할 빈 첨삭 객체 생성
            }

            model.addAttribute(
                    "readingReport",
                    readingReport
            ); // 감상문 정보를 교사 상세 화면에 전달

            model.addAttribute(
                    "book",
                    readingService.getBook(
                            readingReport.getBookId()
                    )
            ); // 감상문에 연결된 도서 정보 전달

            model.addAttribute(
                    "studentName",
                    readingService.getUserName(
                            readingReport.getStudentId()
                    )
            ); // 감상문을 작성한 학생 이름 전달

            model.addAttribute(
                    "feedback",
                    feedback
            ); // 기존 첨삭 또는 빈 첨삭 객체 전달

            model.addAttribute(
                    "readingCount",
                    readingService.getReadingCount(
                            readingReport.getStudentId(),
                            readingReport.getBookId()
                    )
            ); // 학생이 같은 책으로 작성한 감상문 개수 전달

            return "teacher/reading-report/detail"; // 교사 감상문 상세 HTML 실행

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            ); // 이동한 화면에 상세 조회 실패 이유 전달

            return "redirect:/teacher/reading"; // 교사 감상문 목록 화면으로 이동
        }
    }


    @PostMapping("/teacher/reading-reports/{readingReportId}/feedback") // 교사 첨삭 저장 요청 처리
    public String saveReadingFeedback(
            @PathVariable("readingReportId") Long readingReportId,
            @ModelAttribute("feedback") ReadingFeedback feedback,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Long teacherId =
                getLoginUserId(session); // 세션에서 로그인한 교사 번호 조회

        if (teacherId == null) {
            return "redirect:/login"; // 로그인 정보가 없으면 로그인 화면으로 이동
        }

        try {
            readingService.saveReadingFeedback(
                    teacherId,
                    readingReportId,
                    feedback.getContent()
            ); // 교사 번호와 첨삭 내용을 Service에 전달

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "첨삭 내용이 저장되었습니다."
            ); // 상세 화면에 첨삭 저장 성공 문구 전달

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            ); // 상세 화면에 첨삭 저장 실패 이유 전달
        }

        return "redirect:/teacher/reading-reports/"
                + readingReportId; // 교사 감상문 상세 화면으로 이동
    }


    private void addStudentReadingListModel(
            Long studentId,
            Model model
    ) {
        List<ReadingReport> readingReports =
                readingService.getStudentReadingReports(
                        studentId
                ); // 학생이 작성한 감상문 목록 조회

        Map<Long, Book> booksById =
                new LinkedHashMap<>(); // 도서 번호별 도서 정보 저장

        Map<Long, ReadingFeedback> feedbacksByReportId =
                new LinkedHashMap<>(); // 감상문 번호별 첨삭 정보 저장

        Map<Long, Integer> readingCountsByBookId =
                new LinkedHashMap<>(); // 도서 번호별 전체 독서 횟수 저장

        for (ReadingReport readingReport : readingReports) {
            Long bookId =
                    readingReport.getBookId(); // 현재 감상문의 도서 번호 조회

            Long readingReportId =
                    readingReport.getReadingReportId(); // 현재 감상문 번호 조회

            if (!booksById.containsKey(bookId)) {
                booksById.put(
                        bookId,
                        readingService.getBook(bookId)
                ); // 아직 조회하지 않은 도서 정보 저장
            }

            feedbacksByReportId.put(
                    readingReportId,
                    readingService.getReadingFeedback(
                            readingReportId
                    )
            ); // 현재 감상문에 등록된 첨삭 저장

            if (!readingCountsByBookId.containsKey(bookId)) {
                readingCountsByBookId.put(
                        bookId,
                        readingService.getReadingCount(
                                studentId,
                                bookId
                        )
                ); // 아직 계산하지 않은 도서의 전체 독서 횟수 저장
            }
        }

        model.addAttribute(
                "readingReports",
                readingReports
        ); // 감상문 목록을 화면에 전달

        model.addAttribute(
                "booksById",
                booksById
        ); // 도서 번호별 도서 정보를 화면에 전달

        model.addAttribute(
                "feedbacksByReportId",
                feedbacksByReportId
        ); // 감상문 번호별 첨삭 정보를 화면에 전달

        model.addAttribute(
                "readingCountsByBookId",
                readingCountsByBookId
        ); // 도서 번호별 독서 횟수를 화면에 전달
    }


    private void addTeacherReadingListModel(
            Long teacherId,
            Model model
    ) {
        List<ReadingReport> readingReports =
                readingService.getTeacherReadingReports(
                        teacherId
                ); // 담당 반 학생들이 작성한 감상문 목록 조회

        Map<Long, Book> booksById =
                new LinkedHashMap<>(); // 도서 번호별 도서 정보 저장

        Map<Long, String> studentNamesById =
                new LinkedHashMap<>(); // 학생 번호별 이름 저장

        Map<Long, ReadingFeedback> feedbacksByReportId =
                new LinkedHashMap<>(); // 감상문 번호별 첨삭 정보 저장

        for (ReadingReport readingReport : readingReports) {
            Long bookId =
                    readingReport.getBookId(); // 현재 감상문의 도서 번호 조회

            Long studentId =
                    readingReport.getStudentId(); // 감상문을 작성한 학생 번호 조회

            Long readingReportId =
                    readingReport.getReadingReportId(); // 현재 감상문 번호 조회

            if (!booksById.containsKey(bookId)) {
                booksById.put(
                        bookId,
                        readingService.getBook(bookId)
                ); // 아직 조회하지 않은 도서 정보 저장
            }

            if (!studentNamesById.containsKey(studentId)) {
                studentNamesById.put(
                        studentId,
                        readingService.getUserName(studentId)
                ); // 아직 조회하지 않은 학생 이름 저장
            }

            feedbacksByReportId.put(
                    readingReportId,
                    readingService.getReadingFeedback(
                            readingReportId
                    )
            ); // 현재 감상문에 등록된 첨삭 저장
        }

        model.addAttribute(
                "readingReports",
                readingReports
        ); // 담당 학생 감상문 목록을 화면에 전달

        model.addAttribute(
                "booksById",
                booksById
        ); // 도서 번호별 도서 정보를 화면에 전달

        model.addAttribute(
                "studentNamesById",
                studentNamesById
        ); // 학생 번호별 이름을 화면에 전달

        model.addAttribute(
                "feedbacksByReportId",
                feedbacksByReportId
        ); // 감상문 번호별 첨삭 정보를 화면에 전달
    }


    private Long getLoginUserId(
            HttpSession session
    ) {
        Object loginUserId =
                session.getAttribute(
                        LOGIN_USER_ID
                ); // 세션에서 로그인한 사용자 번호 조회

        if (loginUserId instanceof Number number) {
            return number.longValue(); // 숫자 형태의 사용자 번호를 Long으로 변환
        }

        if (loginUserId instanceof String text) {
            try {
                return Long.valueOf(text); // 문자열 사용자 번호를 Long으로 변환

            } catch (NumberFormatException ignored) {
                return null; // 숫자로 변환할 수 없으면 로그인 정보 없음으로 처리
            }
        }

        return null; // 세션에 사용자 번호가 없으면 null 반환
    }
}

