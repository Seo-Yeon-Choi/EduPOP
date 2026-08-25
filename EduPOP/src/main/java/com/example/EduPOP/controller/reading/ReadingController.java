package com.example.EduPOP.controller.reading;

import com.example.EduPOP.domain.reading.Book;
import com.example.EduPOP.domain.reading.ReadingFeedback;
import com.example.EduPOP.domain.reading.ReadingReport;
import com.example.EduPOP.domain.user.User;
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

@Controller // 학생과 교사의 독서 관련 웹 요청을 받는 Controller
@RequiredArgsConstructor // final 필드에 생성자 만들어서 객체로변환
public class ReadingController {

    // 로그인하지 않은 사용자의 직접 URL 접근 방지 코드--------------------------------------
    private final ReadingService readingService; // 서비스를 객체로만들어서 연결
    @GetMapping("/student/reading") // 학생 독서감상문 목록 웹페이지
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

    //책 검색 + 새 책 등록 + 감상문 작성 기능-------------------------------------------------
    @GetMapping("/student/reading-reports/create") // 독서감상문 작성 웹페이지를 준비해서 보여주는 코드
    public String studentReadingReportCreateForm(

    @RequestParam(
    name = "keyword", // URL로 들어오는 keyword 값을 아래 keyword 변수에 연결
    required = false, // URL에 keyword가 없어도 오류 안나게
    defaultValue = "" // keyword가 없으면 빈 문자열 ""을 기본값으로 사용
    ) String keyword, // 사용자가 입력한 책 키워드를 저장

     @RequestParam(
     name = "bookId", //URL에 들어오는 DB 책ID
     required = false // bookId가 없어도 오류 없이 작성 웹페이지에 들어올 수 있음
     ) Long selectedBookId, // 사용자가 선택한 도서의 DB 고유 번호를 저장

     HttpSession session, // 현재 로그인한 사용자 정보를 확인하기 위해 세션을 받음
     Model model // HTML에 전달하기 위한 Model을 준비
    ) {

        Long studentId =
                getLoginUserId(session); // 현제 로그인한 유저 세션 가져오기

        if (studentId == null) { // 세션에서 로그인한 사용자 번호를 가져오지 못했는지 확인
            return "redirect:/login"; // 로그인하지 않았다면 로그인 웹페이지로 다시 이동
        }

        model.addAttribute(
                "books", // 웹페이지에서 검색된 책을 books라는 이름으로 사용
                readingService.searchBooks(keyword) // 검색한 것을 서비스에 보내고 검색된 도서 목록을 받아옴
        ); // 검색된 책을 Model에 저장

        model.addAttribute(
                "keyword", // 검색했던 책이름
                keyword // Controller가 받은 실제 책이름 전달
        ); // 검색어를 Model에 저장

        model.addAttribute( // model에 넣어서 웹페이지에 전달
                "selectedBookId", // 웹페이지에서 선택된 책 정보
                selectedBookId // URL에서 받은 실제 선택 책 정보
        );

        model.addAttribute( // 새 도서 등록 페이지에서 사용할 객체를 전달
                "book", // 빈 Book 객체
                new Book() // 아직 값이 들어있지 않은 새로운 Book 객체를 생성
        ); // 빈 Book 객체를 Model에 저장 완료

        model.addAttribute( // 새 독서감상문 작성 폼에서 사용할 객체를 HTML에 전달
                "readingReport", // HTML에서 빈 독서감상문 객체를 readingReport라는 이름으로 사용
                new ReadingReport() // 아직 제목과 내용 등이 없는 새로운 ReadingReport 객체를 생성
        ); // 빈 ReadingReport 객체를 Model에 저장 완료

        return "student/reading-report/create"; // student/reading-report/create.html 독서감상문 작성 웹페이지를 보여줌
    } // 학생 독서감상문 작성 웹페이지 준비 메서드 끝


    @PostMapping("/student/reading/books") // 웹페이지에서 새 책을 실제 DB에 등록하는 코드
    public String registerBook(
            @ModelAttribute("book") Book book, //독후감 등록하기전에 선택한 책을 객체에 저장
            HttpSession session,//현제 등록하려는 회원 세션 가져오기
            RedirectAttributes redirectAttributes //등록후 메시지 띄우는 객체생성
    ) {
        Long studentId =
                getLoginUserId(session); // 로그인 유저의 세션에서 사용자 DB 번호를 꺼내서 변수에 저장

        if (studentId == null) { //로그인 검사
            return "redirect:/login"; // 로그인 정보가 없으면 로그인 화면으로 이동
        }

        try {
            Book savedBook = // Service 안에있는 registerBook에 저장후 savedBook안에 넣겠다
                    readingService.registerBook(
                            book.getTitle(), //책제목
                            book.getAuthor(), //저자
                            book.getCoverImageUrl() //이미지 url주소
                    ); // 책제목,저자,이미지 주소가 들어있는 객체가 Service에 전달된다

            // 책 등록 성공 문구 전달
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "도서가 등록되었습니다."
            );

            redirectAttributes.addAttribute(
                    "bookId",
                    savedBook.getBookId()
            ); // 등록한 도서에 이어서 독후감 작성할수있게 선택된 책 고유번호 전달

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            ); // 이동한 화면에 도서 등록 실패 이유 전달
        }

        return "redirect:/student/reading-reports/create"; // 감상문 작성 화면으로 이동
    }

    @PostMapping("/student/reading/books/{bookId}/delete") // 학생의 도서 삭제 요청 처리
    public String deleteBook(
            @PathVariable("bookId") Long bookId,
            @RequestParam(
                    name = "keyword",
                    required = false,
                    defaultValue = ""
            ) String keyword,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Long studentId =
                getLoginUserId(session); // 세션에서 로그인한 학생 번호 조회

        if (studentId == null) {
            return "redirect:/login"; // 로그인 정보가 없으면 로그인 화면으로 이동
        }

        try {
            readingService.deleteBook(bookId); // 사용되지 않은 도서 삭제 요청

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "도서가 삭제되었습니다."
            ); // 이동한 화면에 도서 삭제 성공 문구 전달

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            ); // 이동한 화면에 도서 삭제 실패 이유 전달
        }

        if (!keyword.isBlank()) {
            redirectAttributes.addAttribute(
                    "keyword",
                    keyword
            ); // 삭제 후에도 사용자가 입력한 검색어 유지
        }

        return "redirect:/student/reading-reports/create"; // 도서 검색 및 감상문 작성 화면으로 이동
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
//--------------------------------------------------------------------------왜 등록 안됐을때 사유전달
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            ); // 이동한 화면에 감상문 등록 실패 이유 전달

            if (readingReport.getBookId() != null) {
                redirectAttributes.addAttribute(
                        "bookId",
                        readingReport.getBookId()
                ); // 실패 후에도 선택했던 책 그대로 다시 넣어주기
            }

            return "redirect:/student/reading-reports/create"; // 작성 화면으로 이동
        }
    }


    @GetMapping("/student/reading-reports/{readingReportId}") // 학생이 피드백 받은 독서 감상문 웹 페이지
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
                            studentId,//학생 고유번호
                            readingReportId//작성한 독후감
                    ); // 학생 고유번호와 독후감을 전달 후 학생 본인이 작성한 감상문인지 확인

            Book book =
                    readingService.getBook(
                            readingReport.getBookId()
                    ); // 감상문에 연결된 책 조회

            ReadingFeedback feedback =
                    readingService.getReadingFeedback(
                            readingReportId
                    ); // 감상문에 등록된 피드백 조회

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
            ); // 피드백을 상세 화면에 전달

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
                ); // 피드백을 작성한 교사 이름 전달
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


    @GetMapping("/student/reading-reports/{readingReportId}/edit") // 감상문 수정 후 웹페이지
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
                    ); // 교사 피드백 등록 여부 조회

            if (feedback != null) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "담임 선생님의 Feedback이 등록된 감상문은 수정할 수 없습니다."
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
            @PathVariable("readingReportId") Long readingReportId, //@PathVariable = URL 경로에 들어있는 값을 변수로 변환후 객체변환
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
                    "독서감상문이 정상 수정되었습니다."
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
                    "독서감상문이 정상 삭제되었습니다."
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
                    ); // 기존 피드백 내용 조회

            if (feedback == null) {
                feedback =
                        new ReadingFeedback(); // 입력 폼에서 사용할 빈 피드백 객체 생성
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
                    "feedback 내용이 정상 저장되었습니다."
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


    private Long getLoginUserId(HttpSession session) {

        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser == null) {
            return null;
        }

        return loginUser.getUserId();
    }
}