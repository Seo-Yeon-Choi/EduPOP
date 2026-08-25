package com.example.EduPOP.service.exam;

import com.example.EduPOP.domain.exam.*;
import com.example.EduPOP.repository.exam.ExamMapper;
import com.example.EduPOP.repository.exam.ExamQuestionChoiceMapper;
import com.example.EduPOP.repository.exam.ExamQuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamMapper examMapper;

    private final ExamQuestionMapper examQuestionMapper;

    private final ExamQuestionChoiceMapper examQuestionChoiceMapper;

    private final ExamQuestionChoiceMapper choiceMapper;

    public List<Exam> getExamList(){
        return examMapper.findAll();
    }

    public List<Exam> getExamListByTeacher(Long teacherId) {
        return examMapper.findByTeacherId(teacherId);
    }
    /**
     * 시험 생성
     */
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

        // 1. 시험 저장
        examMapper.insert(exam);

        // 2. 문제 저장
        for (ExamQuestion question : exam.getQuestions()) {
            question.setExamId(exam.getExamId());

            // 문제 순서 기본값
            if (question.getSortOrder() == null) {
                question.setSortOrder(question.getQuestionNumber());
            }
            examQuestionMapper.insert(question);
            // 3. 선지 저장
            if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE
                            && question.getChoices() != null) {
                int number = 1;
                for ( ExamQuestionChoice choice : question.getChoices()) {
                    if (choice.getChoiceText() == null || choice.getChoiceText().trim().isEmpty()) {
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

    /**
     * 문제 저장
     */
    @Transactional
    public void addQuestion(
            Long examId,
            ExamQuestion question) {

        question.setExamId(examId);

        if (question.getSortOrder() == null) {
            question.setSortOrder(question.getQuestionNumber());
        }

        /*
         * 먼저 문제 저장
         */
        examQuestionMapper.insert(question);

        /*
         * 객관식일 경우 선지 저장
         */
        if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE && question.getChoices() != null) {

            int number = 1;

            for (ExamQuestionChoice choice : question.getChoices()) {

                // 빈 선지는 저장하지 않음
                if (choice.getChoiceText() == null || choice.getChoiceText().trim().isEmpty()) {
                    continue;
                }

                // FK
                choice.setQuestionId(question.getQuestionId());

                // 선지 번호
                choice.setChoiceNumber(number);

                // 출력 순서
                choice.setSortOrder(number);

                choiceMapper.insert(choice);

                number++;
            }
        }

    }

}