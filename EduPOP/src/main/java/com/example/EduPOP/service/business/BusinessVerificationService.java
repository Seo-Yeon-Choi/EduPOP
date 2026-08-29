package com.example.EduPOP.service.business;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BusinessVerificationService {

    private static final String VALIDATE_URL =
            "https://api.odcloud.kr/api/nts-businessman/v1/validate";

    private static final String STATUS_URL =
            "https://api.odcloud.kr/api/nts-businessman/v1/status";

    @Value("${nts.business-api-key}")
    private String serviceKey;

    public boolean verify(
            String businessNumber,
            String representativeName,
            LocalDate businessStartDate
    ) {
        System.out.println(
                "NTS API KEY 등록 여부 = "
                        + (serviceKey != null && !serviceKey.isBlank())
        );
        if (businessNumber == null
                || representativeName == null
                || representativeName.trim().isEmpty()
                || businessStartDate == null) {
            return false;
        }

        String number =
                businessNumber.replaceAll("[^0-9]", "");

        if (number.length() != 10) {
            return false;
        }

        if (!validateBusiness(
                number,
                representativeName.trim(),
                businessStartDate)) {
            return false;
        }

        return isActiveBusiness(number);
    }

    private boolean validateBusiness(
            String businessNumber,
            String representativeName,
            LocalDate businessStartDate
    ) {

        String startDate =
                businessStartDate.format(
                        DateTimeFormatter.ofPattern("yyyyMMdd")
                );

        Map<String, Object> business = Map.of(
                "b_no", businessNumber,
                "start_dt", startDate,
                "p_nm", representativeName
        );

        Map<String, Object> body =
                Map.of(
                        "businesses",
                        List.of(business)
                );

        JsonNode response =
                callApi(
                        VALIDATE_URL,
                        body
                );

        System.out.println("진위확인 전체 응답 = " + response);

        JsonNode data = response.get("data");

        if (data == null || data.isEmpty()) {
            System.out.println("진위확인 data가 없음");
            return false;
        }

        String valid =
                data.get(0)
                        .get("valid")
                        .asText();

        return "01".equals(valid);
    }

    private boolean isActiveBusiness(
            String businessNumber
    ) {

        Map<String, Object> body =
                Map.of(
                        "b_no",
                        List.of(businessNumber)
                );

        JsonNode response =
                callApi(
                        STATUS_URL,
                        body
                );

        JsonNode data = response.get("data");

        if (data == null || data.isEmpty()) {
            return false;
        }

        String statusCode =
                data.get(0)
                        .get("b_stt_cd")
                        .asText();

        return "01".equals(statusCode);
    }

    private JsonNode callApi(
            String apiUrl,
            Map<String, Object> body
    ) {

        RestTemplate restTemplate =
                new RestTemplate();

        ObjectMapper objectMapper =
                new ObjectMapper();

        String url =
                UriComponentsBuilder
                        .fromUriString(apiUrl)
                        .queryParam(
                                "serviceKey",
                                serviceKey
                        )
                        .build()
                        .encode()
                        .toUriString();

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(
                        body,
                        headers
                );

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        request,
                        String.class
                );

        System.out.println("===== 국세청 API 호출 결과 =====");
        System.out.println("HTTP STATUS = " + response.getStatusCode());
        System.out.println("RESPONSE BODY = " + response.getBody());
        System.out.println("=============================");

        try {

            return objectMapper.readTree(
                    response.getBody()
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "사업자등록정보 응답 처리 중 오류가 발생했습니다.",
                    e
            );
        }
    }
}