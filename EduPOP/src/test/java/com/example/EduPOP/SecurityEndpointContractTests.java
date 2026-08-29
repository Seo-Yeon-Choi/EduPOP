package com.example.EduPOP;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityEndpointContractTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @TestFactory
    Stream<DynamicTest> everyProtectedEndpointRejectsAnonymousRequests() {
        return applicationEndpoints()
                .filter(endpoint -> !isPublic(endpoint.path()))
                .map(endpoint -> DynamicTest.dynamicTest(
                        endpoint.displayName() + " rejects anonymous access",
                        () -> {
                            int status = mockMvc.perform(request(endpoint))
                                    .andReturn()
                                    .getResponse()
                                    .getStatus();

                            assertThat(status >= 200 && status < 300)
                                    .as("%s must not succeed for an anonymous user (status=%s)",
                                            endpoint.displayName(), status)
                                    .isFalse();
                        }
                ));
    }

    @TestFactory
    Stream<DynamicTest> roleProtectedEndpointsRejectTheWrongRole() {
        return applicationEndpoints()
                .map(endpoint -> new RoleCase(endpoint, wrongRoleFor(endpoint.path())))
                .filter(roleCase -> roleCase.wrongRole() != null)
                .map(roleCase -> DynamicTest.dynamicTest(
                        roleCase.endpoint().displayName() + " rejects role " + roleCase.wrongRole(),
                        () -> mockMvc.perform(
                                        request(roleCase.endpoint())
                                                .with(user("wrong-role-user").roles(roleCase.wrongRole()))
                                )
                                .andExpect(result ->
                                        assertThat(result.getResponse().getStatus())
                                                .as(roleCase.endpoint().displayName())
                                                .isEqualTo(403))
                ));
    }

    private Stream<Endpoint> applicationEndpoints() {
        return handlerMapping.getHandlerMethods().entrySet().stream()
                .filter(entry -> isApplicationController(entry.getValue()))
                .flatMap(entry -> endpoints(entry.getKey()))
                .filter(endpoint -> !"/error".equals(endpoint.path()))
                .distinct()
                .sorted(Comparator
                        .comparing(Endpoint::path)
                        .thenComparing(endpoint -> endpoint.method().name()));
    }

    private boolean isApplicationController(HandlerMethod handlerMethod) {
        Package controllerPackage = handlerMethod.getBeanType().getPackage();
        return controllerPackage != null
                && controllerPackage.getName().startsWith("com.example.EduPOP.controller");
    }

    private Stream<Endpoint> endpoints(RequestMappingInfo mappingInfo) {
        Set<RequestMethod> configuredMethods =
                mappingInfo.getMethodsCondition().getMethods();

        List<RequestMethod> methods = configuredMethods.isEmpty()
                ? List.of(RequestMethod.GET)
                : configuredMethods.stream().toList();

        return mappingInfo.getPatternValues().stream()
                .flatMap(path -> methods.stream().map(method -> new Endpoint(path, method)));
    }

    private MockHttpServletRequestBuilder request(Endpoint endpoint) {
        String concretePath = endpoint.path().replaceAll("\\{[^}]+}", "1");

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
                HttpMethod.valueOf(endpoint.method().name()),
                concretePath
        );

        if (endpoint.method() != RequestMethod.GET
                && endpoint.method() != RequestMethod.HEAD
                && endpoint.method() != RequestMethod.OPTIONS) {
            request.with(csrf());
        }

        return request;
    }

    private boolean isPublic(String path) {
        return path.equals("/")
                || path.equals("/LocalLogin")
                || path.equals("/signUp")
                || path.equals("/login")
                || path.startsWith("/login/route/")
                || path.startsWith("/kakao/")
                || path.startsWith("/naver/")
                || path.startsWith("/google/")
                || path.startsWith("/share/");
    }

    private String wrongRoleFor(String path) {
        if (path.startsWith("/student/")
                || path.startsWith("/api/student-reports/")
                || path.startsWith("/api/exp/")
                || path.equals("/main/studentMain")) {
            return "TEACHER";
        }

        if (path.startsWith("/teacher/")
                || path.startsWith("/exam/")
                || path.startsWith("/analytics/")
                || path.startsWith("/api/teacher/")
                || path.equals("/main/teacherMain")) {
            return "STUDENT";
        }

        if (path.startsWith("/admin/")
                || path.startsWith("/classroom/")
                || path.equals("/main/adminMain")
                || path.equals("/adminMain")) {
            return "STUDENT";
        }

        return null;
    }

    private record Endpoint(String path, RequestMethod method) {
        String displayName() {
            return method.name() + " " + path;
        }
    }

    private record RoleCase(Endpoint endpoint, String wrongRole) {
    }
}
