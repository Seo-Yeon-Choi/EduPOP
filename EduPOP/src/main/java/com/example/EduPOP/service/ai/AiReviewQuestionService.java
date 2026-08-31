package com.example.EduPOP.service.ai;

import com.example.EduPOP.domain.exam.ExamQuestion;
import com.example.EduPOP.domain.exam.ExamQuestionChoice;
import com.example.EduPOP.dto.ai.AiGeneratedQuestion;
import com.example.EduPOP.repository.exam.AiQuestionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiReviewQuestionService {

    private final AiQuestionService aiQuestionService;
    private final AiQuestionMapper aiQuestionMapper;


    /**
     * AI 문제 조회 또는 생성
     *
     * 이미 존재하면 API를 호출하지 않고 기존 문제를 재사용한다.
     * 존재하지 않으면 OpenAI 호출 → DB 저장
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public ExamQuestion getOrCreate(
            ExamQuestion original
    ) {

        if (original == null ||
                original.getQuestionId() == null) {

            throw new IllegalArgumentException(
                    "원본 문제가 존재하지 않습니다."
            );
        }


        // =================================================
        // 1. 이미 생성된 AI 문제가 있는지 확인
        // =================================================

        ExamQuestion existing =
                aiQuestionMapper.findExistingAiQuestion(
                        original.getQuestionId()
                );

        if (existing != null) {

            log.info(
                    "기존 AI 문제 재사용 - sourceQuestionId={}, aiQuestionId={}",
                    original.getQuestionId(),
                    existing.getQuestionId()
            );

            return existing;
        }


        // =================================================
        // 2. 현재는 객관식만 AI 문제 생성
        // =================================================

        if (!"MULTIPLE_CHOICE".equalsIgnoreCase(
                original.getQuestionType()
        )) {

            throw new IllegalArgumentException(
                    "현재 AI 유사문제 생성은 객관식만 지원합니다."
            );
        }


        // =================================================
        // 3. 원본 선택지 확인
        // =================================================

        List<ExamQuestionChoice> choices =
                original.getChoices();

        if (choices == null ||
                choices.size() != 4) {

            throw new IllegalArgumentException(
                    "AI 문제 생성을 위해 원본 문제에 4개의 선택지가 필요합니다."
            );
        }


        // =================================================
        // 4. 원본 정답 번호 확인
        // =================================================

        Integer correctAnswer =
                resolveCorrectAnswerNumber(
                        original,
                        choices
                );


        // =================================================
        // 5. OpenAI API 호출
        // =================================================

        AiGeneratedQuestion generated =
                aiQuestionService.generateSimilarQuestion(

                        original.getQuestionId(),

                        original.getQuestionText(),

                        choices.get(0).getChoiceText(),
                        choices.get(1).getChoiceText(),
                        choices.get(2).getChoiceText(),
                        choices.get(3).getChoiceText(),

                        correctAnswer,

                        original.getLargeCategory(),
                        original.getSmallCategory()
                );


        // =================================================
        // 6. 새로운 question_number 생성
        // =================================================

        Integer nextQuestionNumber =
                aiQuestionMapper.findNextQuestionNumber(
                        original.getExamId()
                );


        // =================================================
        // 7. AI DTO → ExamQuestion 변환
        // =================================================

        ExamQuestion aiQuestion =
                new ExamQuestion();


        // 기존 시험 정보 계승
        aiQuestion.setExamId(
                original.getExamId()
        );

        aiQuestion.setSectionId(
                original.getSectionId()
        );


        // 새로운 문제 번호
        aiQuestion.setQuestionNumber(
                nextQuestionNumber
        );


        // 객관식 타입 계승
        aiQuestion.setQuestionType(
                original.getQuestionType()
        );


        // 원본 대분류 / 소분류 계승
        aiQuestion.setLargeCategory(
                original.getLargeCategory()
        );

        aiQuestion.setSmallCategory(
                original.getSmallCategory()
        );


        // 기존 문제와 같은 배점 사용
        aiQuestion.setScore(
                original.getScore()
        );


        // AI가 만든 정답
        aiQuestion.setCorrectAnswer(
                String.valueOf(
                        generated.correctAnswer
                )
        );


        // AI가 만든 문제
        aiQuestion.setQuestionText(
                generated.questionText
        );


        // 정렬 번호
        aiQuestion.setSortOrder(
                nextQuestionNumber
        );


        // 핵심: 어떤 문제를 기반으로 만들었는지
        aiQuestion.setSourceQuestionId(
                original.getQuestionId()
        );


        // 핵심: AI 생성 문제
        aiQuestion.setAiGenerated(true);


        // =================================================
        // 8. exam_questions INSERT
        // =================================================

        aiQuestionMapper.insertAiQuestion(
                aiQuestion
        );


        /*
         * useGeneratedKeys=true 때문에
         *
         * INSERT 후
         *
         * aiQuestion.getQuestionId()
         *
         * 에 새 question_id가 들어간다.
         */

        Long newQuestionId =
                aiQuestion.getQuestionId();


        if (newQuestionId == null) {

            throw new IllegalStateException(
                    "AI 문제 저장 후 question_id를 가져오지 못했습니다."
            );
        }


        // =================================================
        // 9. AI 선택지 INSERT
        // =================================================

        for (int i = 0;
             i < generated.choices.size();
             i++) {

            aiQuestionMapper.insertAiChoice(

                    newQuestionId,

                    i + 1,

                    generated.choices.get(i),

                    i + 1
            );
        }


        log.info(
                "AI 문제 생성 및 저장 완료 - questionId={}, sourceQuestionId={}",
                newQuestionId,
                original.getQuestionId()
        );


        return aiQuestion;
    }


    /**
     * correct_answer가
     *
     * "3"
     *
     * 형태라면 바로 번호로 처리하고,
     *
     * 혹시 "went" 같은 실제 선택지 텍스트가 저장되어 있다면
     * 해당 선택지 번호를 찾아준다.
     */
    private Integer resolveCorrectAnswerNumber(
            ExamQuestion original,
            List<ExamQuestionChoice> choices
    ) {

        String correctAnswer =
                original.getCorrectAnswer();

        if (correctAnswer == null ||
                correctAnswer.isBlank()) {

            throw new IllegalArgumentException(
                    "원본 문제의 정답이 없습니다."
            );
        }


        String trimmed =
                correctAnswer.trim();


        // -----------------------------------------
        // 정답이 "1", "2", "3", "4"인 경우
        // -----------------------------------------

        try {

            int number =
                    Integer.parseInt(trimmed);

            if (number >= 1 &&
                    number <= 4) {

                return number;
            }

        } catch (NumberFormatException ignored) {
        }


        // -----------------------------------------
        // 정답 자체가 선택지 텍스트인 경우
        // -----------------------------------------

        for (int i = 0;
             i < choices.size();
             i++) {

            String choiceText =
                    choices.get(i)
                            .getChoiceText();

            if (choiceText != null &&
                    choiceText.trim()
                            .equalsIgnoreCase(trimmed)) {

                return i + 1;
            }
        }


        throw new IllegalArgumentException(
                "원본 문제의 정답을 선택지 번호로 변환할 수 없습니다. "
                        + "questionId="
                        + original.getQuestionId()
        );
    }
}