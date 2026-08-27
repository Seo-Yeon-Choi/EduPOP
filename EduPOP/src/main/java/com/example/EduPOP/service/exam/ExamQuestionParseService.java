package com.example.EduPOP.service.exam;

import com.example.EduPOP.domain.exam.ExamQuestion;
import com.example.EduPOP.domain.exam.ExamQuestionChoice;
import com.example.EduPOP.domain.exam.QuestionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ExamQuestionParseService {

    // =========================================
    // 일반 시험 문제 번호
    // 예:
    // 1.
    // 2)
    // =========================================

    private static final Pattern QUESTION_PATTERN = Pattern.compile("(?m)^\\s*(\\d+)\\s*[.)]\\s*");

    // =========================================
    // 객관식 선지 시작 문자
    // =========================================

    private static final Pattern CHOICE_START_PATTERN = Pattern.compile("[①②③④⑤]");

    // =========================================
    // 일반 시험 PDF 파싱
    // =========================================

    public List<ExamQuestion> parseNormalExam(String text) {
        text = normalizeText(text);

        List<ExamQuestion> result = new ArrayList<>();

        Matcher matcher = QUESTION_PATTERN.matcher(text);

        List<Integer> starts = new ArrayList<>();

        List<Integer> numbers = new ArrayList<>();

        // -------------------------------------
        // 문제 번호 위치 저장
        // -------------------------------------

        while (matcher.find()) {
            starts.add(matcher.start());

            numbers.add(Integer.parseInt(matcher.group(1)));
        }

        // -------------------------------------
        // 문제 단위로 분리
        // -------------------------------------

        for (int i = 0; i < starts.size(); i++) {

            int start = starts.get(i);

            int end = i + 1 < starts.size() ? starts.get(i + 1) : text.length();

            String block = text.substring(start, end).trim();

            ExamQuestion question = parseQuestionBlock(numbers.get(i), block);

            result.add(question);
        }

        return result;
    }


    // =========================================
    // 단어 시험 PDF 파싱
    // =========================================

    public List<ExamQuestion> parseWordExam(String text) {

        text = normalizeWordText(text);


        List<ExamQuestion> result = new ArrayList<>();

        String[] lines = text.split("\\n");

        int questionNumber = 1;

        for (String line : lines) {
            String trimmed = line.trim();

            // 빈 줄 제외
            if (trimmed.isEmpty()) {
                continue;
            }

            // ---------------------------------
            // 제목 / 헤더 제외
            // ---------------------------------

            if (isWordHeader(trimmed)) {
                continue;
            }

            // ---------------------------------
            // 한 줄에서 단어 / 뜻 추출
            // ---------------------------------

            WordPair wordPair = parseWordLine(trimmed);

            // 단어-뜻 형식이 아니면 무시
            if (wordPair == null) {
                continue;
            }

            // ---------------------------------
            // ExamQuestion 생성
            // ---------------------------------

            ExamQuestion question = new ExamQuestion();

            question.setQuestionNumber(questionNumber);

            question.setSortOrder(questionNumber);

            question.setQuestionType("SHORT_ANSWER");

            question.setLargeCategory("");
            question.setSmallCategory("");

            question.setQuestionText(wordPair.word());

            question.setCorrectAnswer(wordPair.meaning());

            question.setPassage("");

            question.setScore(new BigDecimal("1.00"));

            question.setChoices(new ArrayList<>());

            result.add(question);

            questionNumber++;
        }

        return result;
    }

    // =========================================
    // 일반 시험 문제 하나 파싱
    // =========================================

    private ExamQuestion parseQuestionBlock(int number, String block) {

        ExamQuestion question = new ExamQuestion();

        // -------------------------------------
        // 기본값
        // -------------------------------------

        question.setQuestionNumber(number);

        question.setScore(new BigDecimal("5.00"));

        question.setSortOrder(number);

        // -------------------------------------
        // 문제 번호 제거
        // -------------------------------------

        block = removeQuestionNumber(block);

        // -------------------------------------
        // 선지 시작 위치 찾기
        // -------------------------------------

        Matcher choiceMatcher = CHOICE_START_PATTERN.matcher(block);

        int choiceStart = -1;

        if (choiceMatcher.find()) {
            choiceStart = choiceMatcher.start();
        }

        String beforeChoices;

        // =====================================
        // 객관식
        // =====================================

        if (choiceStart >= 0) {
            beforeChoices = block.substring(0, choiceStart).trim();

            String choicesText = block.substring(choiceStart).trim();

            question.setQuestionType("MULTIPLE_CHOICE");

            question.setChoices(parseChoices(choicesText));
        }

        // =====================================
        // 주관식
        // =====================================

        else {
            beforeChoices = block.trim();

            question.setQuestionType("SHORT_ANSWER");

            question.setChoices(new ArrayList<>());
        }

        // -------------------------------------
        // 문제 / 지문 분리
        // -------------------------------------

        parseQuestionAndPassage(question, beforeChoices);

        return question;
    }

    // =========================================
    // 객관식 선지 파싱
    // =========================================

    private List<ExamQuestionChoice> parseChoices(String choicesText) {
        List<ExamQuestionChoice> choices = new ArrayList<>();

        Pattern pattern = Pattern.compile("([①②③④⑤])\\s*(.*?)(?=[①②③④⑤]|$)", Pattern.DOTALL);

        Matcher matcher = pattern.matcher(choicesText);

        int number = 1;

        while (matcher.find()) {

            String choiceText = matcher.group(2).replace("\n", " ").replaceAll("\\s+", " ").trim();

            if (choiceText.isEmpty()) {
                continue;
            }

            ExamQuestionChoice choice = new ExamQuestionChoice();

            choice.setChoiceNumber(number);

            choice.setSortOrder(number);

            choice.setChoiceText(choiceText);

            choices.add(choice);

            number++;
        }

        return choices;
    }

    // =========================================
    // 일반 시험 문제 / 지문 분리
    //
    // 문제 문장은 . 또는 ? 로 끝나는 것으로 판단
    // =========================================

    private void parseQuestionAndPassage(ExamQuestion question, String text) {
        String normalized = text.replace("\r\n", "\n").replace("\r", "\n").trim();

        String[] lines = normalized.split("\\n");

        StringBuilder questionBuilder = new StringBuilder();

        StringBuilder passageBuilder = new StringBuilder();

        boolean questionFinished = false;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.isEmpty()) {
                continue;
            }

            // ---------------------------------
            // 아직 문제 문장 처리 중
            // ---------------------------------

            if (!questionFinished) {
                if (questionBuilder.length() > 0) {
                    questionBuilder.append(" ");
                }

                questionBuilder.append(trimmed);

                // 문제 문장의 끝 판단
                if (trimmed.endsWith(".") || trimmed.endsWith("?")) {
                    questionFinished = true;
                }
            }

            // ---------------------------------
            // 문제 종료 후 지문
            // ---------------------------------

            else {
                if (passageBuilder.length() > 0) {
                    passageBuilder.append("\n");
                }

                passageBuilder.append(trimmed);
            }
        }

        question.setQuestionText(questionBuilder.toString().trim());

        question.setPassage(passageBuilder.toString().trim());
    }

    // =========================================
    // 단어시험 한 줄 파싱
    // =========================================

    private WordPair parseWordLine(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }

        String cleaned = line.trim();

        // -------------------------------------
        // 앞의 번호 제거
        //
        // 1. apple
        // 1) apple
        // -------------------------------------

        cleaned = cleaned.replaceFirst("^\\s*\\d+\\s*[.)]\\s*", "");

        // =====================================
        // 1. 단어 - 뜻
        //
        // apple - 사과
        // =====================================

        Pattern hyphenPattern = Pattern.compile("^([A-Za-z][A-Za-z\\s'-]*)\\s+-\\s+(.+)$");

        Matcher hyphenMatcher = hyphenPattern.matcher(cleaned);

        if (hyphenMatcher.matches()) {
            String word = hyphenMatcher.group(1).trim();

            String meaning = hyphenMatcher.group(2).trim();

            if (!word.isEmpty() && !meaning.isEmpty()) {
                return new WordPair(word, meaning);
            }
        }

        // =====================================
        // 2. TAB으로 구분
        //
        // apple    사과
        // =====================================

        String[] tabParts = cleaned.split("\\t+", 2);

        if (tabParts.length == 2 && !tabParts[0].isBlank() && !tabParts[1].isBlank()) {
            return new WordPair(tabParts[0].trim(), tabParts[1].trim());
        }

        // =====================================
        // 3. 공백이 여러 칸인 경우
        //
        // apple        사과
        //
        // PDF 표가 이런 형태로 추출될 수 있음
        // =====================================

        String[] spaceParts = cleaned.split("\\s{2,}", 2);

        if (spaceParts.length == 2 && !spaceParts[0].isBlank() && !spaceParts[1].isBlank()) {
            return new WordPair(spaceParts[0].trim(), spaceParts[1].trim());
        }

        // =====================================
        // 4. 영어 단어 + 공백 + 한글 뜻
        //
        // apple 사과
        //
        // PDFBox가 공백을 하나로 만들어버린 경우
        // =====================================

        Pattern koreanMeaningPattern = Pattern.compile("^([A-Za-z][A-Za-z\\s'-]*?)\\s+([가-힣].*)$");

        Matcher koreanMeaningMatcher = koreanMeaningPattern.matcher(cleaned);

        if (koreanMeaningMatcher.matches()) {
            String word = koreanMeaningMatcher.group(1).trim();

            String meaning = koreanMeaningMatcher.group(2).trim();

            return new WordPair(word, meaning
            );
        }

        return null;
    }

    // =========================================
    // 단어시험 PDF 제목/헤더 판단
    // =========================================

    private boolean isWordHeader(String text) {

        if (text == null) {
            return true;
        }

        String trimmed = text.trim();

        if (trimmed.isEmpty()) {
            return true;
        }


        return trimmed.contains(
                "EduPOP"
        )
                || trimmed.startsWith(
                "표준형:"
        )
                || trimmed.startsWith(
                "표 형식:"
        )
                || trimmed.startsWith(
                "변형형:"
        )
                || trimmed.equals(
                "단어 뜻"
        )
                || trimmed.equals(
                "단어"
        )
                || trimmed.equals(
                "뜻"
        );
    }

    // =========================================
    // 일반 시험 문제 번호 제거
    // =========================================

    private String removeQuestionNumber(String block) {
        return block.replaceFirst("^\\s*\\d+\\s*[.)]\\s*", "");
    }

    // =========================================
    // 일반 시험 텍스트 정리
    // =========================================

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace(
                        "\r\n",
                        "\n"
                )
                .replace(
                        "\r",
                        "\n"
                )
                .replaceAll(
                        "[ \\t]+",
                        " "
                )
                .trim();
    }

    // =========================================
    // 단어 시험 텍스트 정리
    //
    // 주의:
    // 여러 공백이나 TAB을 지우면 안 됨.
    // 표의 열 구분에 사용될 수 있기 때문.
    // =========================================

    private String normalizeWordText(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace(
                        "\r\n",
                        "\n"
                )
                .replace(
                        "\r",
                        "\n"
                )
                .trim();
    }

    // =========================================
    // 단어 / 뜻 임시 저장 객체
    // =========================================

    private record WordPair(String word, String meaning) { }
}