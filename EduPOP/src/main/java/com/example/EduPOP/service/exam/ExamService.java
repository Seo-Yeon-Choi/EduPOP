package com.example.EduPOP.service.exam;

import com.example.EduPOP.domain.exam.*;
import com.example.EduPOP.repository.exam.ExamMapper;
import com.example.EduPOP.repository.exam.ExamQuestionChoiceMapper;
import com.example.EduPOP.repository.exam.ExamQuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamMapper examMapper;
    private final ExamQuestionMapper examQuestionMapper;
    private final ExamQuestionChoiceMapper examQuestionChoiceMapper;

    public List<Exam> getExamList() {
        return examMapper.findAll();
    }

    public List<Exam> getExamListByTeacher(Long teacherId) {
        return examMapper.findByTeacherId(teacherId);
    }

    public Exam getExamDetailForTeacher(Long examId, Long teacherId) {
        Exam exam = findOwnedExam(examId, teacherId);
        List<ExamQuestion> questions = examQuestionMapper.findByExamId(examId);

        for (ExamQuestion question : questions) {
            question.setChoices(examQuestionChoiceMapper.findByQuestionId(question.getQuestionId()));
        }

        exam.setQuestions(questions);
        return exam;
    }

    @Transactional
    public Long createExam(Exam exam) {
        if (exam.getStatus() == null) {
            exam.setStatus(ExamStatus.DRAFT);
        }

        if (exam.getExamRound() == null) {
            exam.setExamRound(1);
        }

        if (exam.getExamMode() == null) {
            exam.setExamMode(ExamMode.PAPER);
        }

        examMapper.insert(exam);

        for (ExamQuestion question : exam.getQuestions()) {
            question.setExamId(exam.getExamId());

            if (question.getSortOrder() == null) {
                question.setSortOrder(question.getQuestionNumber());
            }

            examQuestionMapper.insert(question);

            if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE
                    && question.getChoices() != null) {
                int number = 1;

                for (ExamQuestionChoice choice : question.getChoices()) {
                    if (isBlank(choice.getChoiceText())) {
                        continue;
                    }

                    choice.setQuestionId(question.getQuestionId());
                    choice.setChoiceNumber(number);
                    choice.setSortOrder(number);
                    examQuestionChoiceMapper.insert(choice);
                    number++;
                }
            }
        }

        return exam.getExamId();
    }

    @Transactional
    public void updateExam(Long examId, Long teacherId, Exam requestedExam) {
        Exam savedExam = findOwnedExam(examId, teacherId);
        validateExamInfo(requestedExam);

        List<ExamQuestion> savedQuestions = examQuestionMapper.findByExamId(examId);
        Map<Long, ExamQuestion> requestedQuestions = indexQuestions(requestedExam.getQuestions());
        Set<Long> savedQuestionIds = new HashSet<>();

        for (ExamQuestion savedQuestion : savedQuestions) {
            savedQuestionIds.add(savedQuestion.getQuestionId());
        }

        if (!savedQuestionIds.equals(requestedQuestions.keySet())) {
            throw new IllegalArgumentException("기존 문항만 수정할 수 있습니다. 문항 추가 또는 삭제는 지원하지 않습니다.");
        }

        savedExam.setClassId(requestedExam.getClassId());
        savedExam.setTitle(requestedExam.getTitle().trim());
        savedExam.setExamType(requestedExam.getExamType());
        savedExam.setExamMode(requestedExam.getExamMode());
        savedExam.setStatus(requestedExam.getStatus());
        savedExam.setExamDate(requestedExam.getExamDate());
        examMapper.update(savedExam);

        for (ExamQuestion savedQuestion : savedQuestions) {
            ExamQuestion requestedQuestion = requestedQuestions.get(savedQuestion.getQuestionId());
            validateQuestion(requestedQuestion);

            savedQuestion.setQuestionTypeTag(trimToNull(requestedQuestion.getQuestionTypeTag()));
            savedQuestion.setScore(requestedQuestion.getScore());
            savedQuestion.setCorrectAnswer(trimToNull(requestedQuestion.getCorrectAnswer()));
            savedQuestion.setQuestionText(requestedQuestion.getQuestionText().trim());
            savedQuestion.setPassage(trimToNull(requestedQuestion.getPassage()));
            examQuestionMapper.update(savedQuestion);

            updateExistingChoices(savedQuestion.getQuestionId(), requestedQuestion.getChoices());
        }
    }

    @Transactional
    public void addQuestion(Long examId, ExamQuestion question) {
        question.setExamId(examId);

        if (question.getSortOrder() == null) {
            question.setSortOrder(question.getQuestionNumber());
        }

        examQuestionMapper.insert(question);

        if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE && question.getChoices() != null) {
            int number = 1;

            for (ExamQuestionChoice choice : question.getChoices()) {
                if (isBlank(choice.getChoiceText())) {
                    continue;
                }

                choice.setQuestionId(question.getQuestionId());
                choice.setChoiceNumber(number);
                choice.setSortOrder(number);
                examQuestionChoiceMapper.insert(choice);
                number++;
            }
        }
    }

    private Exam findOwnedExam(Long examId, Long teacherId) {
        Exam exam = examMapper.findById(examId);

        if (exam == null) {
            throw new IllegalArgumentException("존재하지 않는 시험입니다.");
        }

        if (!teacherId.equals(exam.getTeacherId())) {
            throw new IllegalArgumentException("해당 시험을 조회하거나 수정할 권한이 없습니다.");
        }

        return exam;
    }

    private Map<Long, ExamQuestion> indexQuestions(List<ExamQuestion> questions) {
        if (questions == null) {
            throw new IllegalArgumentException("문항 정보가 필요합니다.");
        }

        Map<Long, ExamQuestion> indexedQuestions = new HashMap<>();

        for (ExamQuestion question : questions) {
            if (question.getQuestionId() == null
                    || indexedQuestions.put(question.getQuestionId(), question) != null) {
                throw new IllegalArgumentException("유효하지 않은 문항 정보입니다.");
            }
        }

        return indexedQuestions;
    }

    private void updateExistingChoices(Long questionId, List<ExamQuestionChoice> requestedChoices) {
        List<ExamQuestionChoice> savedChoices = examQuestionChoiceMapper.findByQuestionId(questionId);
        Map<Long, ExamQuestionChoice> requestedChoiceMap = new HashMap<>();

        if (requestedChoices != null) {
            for (ExamQuestionChoice choice : requestedChoices) {
                if (choice.getChoiceId() == null
                        || requestedChoiceMap.put(choice.getChoiceId(), choice) != null) {
                    throw new IllegalArgumentException("유효하지 않은 선지 정보입니다.");
                }
            }
        }

        Set<Long> savedChoiceIds = new HashSet<>();
        for (ExamQuestionChoice choice : savedChoices) {
            savedChoiceIds.add(choice.getChoiceId());
        }

        if (!savedChoiceIds.equals(requestedChoiceMap.keySet())) {
            throw new IllegalArgumentException("기존 선지만 수정할 수 있습니다. 선지 추가 또는 삭제는 지원하지 않습니다.");
        }

        for (ExamQuestionChoice savedChoice : savedChoices) {
            ExamQuestionChoice requestedChoice = requestedChoiceMap.get(savedChoice.getChoiceId());

            if (isBlank(requestedChoice.getChoiceText())) {
                throw new IllegalArgumentException("선지 내용은 비워 둘 수 없습니다.");
            }

            savedChoice.setChoiceText(requestedChoice.getChoiceText().trim());
            examQuestionChoiceMapper.update(savedChoice);
        }
    }

    private void validateExamInfo(Exam exam) {
        if (exam.getClassId() == null || isBlank(exam.getTitle())
                || exam.getExamType() == null || exam.getExamMode() == null
                || exam.getStatus() == null) {
            throw new IllegalArgumentException("시험 기본 정보를 모두 입력해주세요.");
        }
    }

    private void validateQuestion(ExamQuestion question) {
        if (question == null || isBlank(question.getQuestionText())
                || isBlank(question.getQuestionTypeTag())) {
            throw new IllegalArgumentException("문제 내용과 유형 태그를 모두 입력해주세요.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
