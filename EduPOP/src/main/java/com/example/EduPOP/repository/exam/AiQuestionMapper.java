package com.example.EduPOP.repository.exam;

import com.example.EduPOP.domain.exam.ExamQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiQuestionMapper {

    /**
     * 특정 원본 문제에서 이미 생성된 AI 문제가 있는지 조회
     */
    ExamQuestion findExistingAiQuestion(
            @Param("sourceQuestionId") Long sourceQuestionId
    );

    /**
     * 같은 시험에서 사용할 다음 문항 번호
     *
     * exam_questions에는
     * UNIQUE (exam_id, question_number)가 있으므로
     * AI 문제에도 새로운 question_number가 필요하다.
     */
    Integer findNextQuestionNumber(
            @Param("examId") Long examId
    );

    /**
     * AI 문제 저장
     *
     * useGeneratedKeys를 통해
     * 저장 후 question.questionId에 PK가 들어간다.
     */
    int insertAiQuestion(
            ExamQuestion question
    );

    /**
     * AI 객관식 선택지 저장
     */
    int insertAiChoice(
            @Param("questionId") Long questionId,
            @Param("choiceNumber") Integer choiceNumber,
            @Param("choiceText") String choiceText,
            @Param("sortOrder") Integer sortOrder
    );
}