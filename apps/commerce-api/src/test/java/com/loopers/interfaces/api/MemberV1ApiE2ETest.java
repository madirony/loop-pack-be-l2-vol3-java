package com.loopers.interfaces.api;

import com.loopers.domain.member.MemberRepository;
import com.loopers.interfaces.api.member.MemberV1Dto;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MemberV1ApiE2ETest {

    private static final String SIGNUP_ENDPOINT = "/api/v1/members/signup";

    private final TestRestTemplate testRestTemplate;
    private final MemberRepository memberRepository;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public MemberV1ApiE2ETest(
            TestRestTemplate testRestTemplate,
            MemberRepository memberRepository,
            DatabaseCleanUp databaseCleanUp
    ) {
        this.testRestTemplate = testRestTemplate;
        this.memberRepository = memberRepository;
        this.databaseCleanUp = databaseCleanUp;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("POST /api/v1/members/signup")
    @Nested
    class Signup {

        @DisplayName("유효한 회원 정보로 회원가입하면 성공한다.")
        @Test
        void signup_success() {
            // arrange
            MemberV1Dto.SignupRequest request = new MemberV1Dto.SignupRequest(
                    "user1",
                    "Password1!",
                    "홍길동",
                    "test@test.com",
                    "1997-01-01"
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<MemberV1Dto.SignupRequest> httpEntity = new HttpEntity<>(request, headers);

            // act
            ParameterizedTypeReference<ApiResponse<MemberV1Dto.SignupResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<MemberV1Dto.SignupResponse>> response =
                    testRestTemplate.exchange(SIGNUP_ENDPOINT, HttpMethod.POST, httpEntity, responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.SUCCESS),
                    () -> assertThat(response.getBody().data().memberId()).isEqualTo("user1")
            );
        }

        @DisplayName("중복된 ID로 회원가입하면 409 CONFLICT 응답을 받는다.")
        @Test
        void signup_fail_duplicate_id() {
            // arrange - 먼저 회원가입
            MemberV1Dto.SignupRequest firstRequest = new MemberV1Dto.SignupRequest(
                    "user1",
                    "Password1!",
                    "홍길동",
                    "test@test.com",
                    "1997-01-01"
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<MemberV1Dto.SignupRequest> httpEntity = new HttpEntity<>(firstRequest, headers);

            testRestTemplate.exchange(SIGNUP_ENDPOINT, HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ApiResponse<MemberV1Dto.SignupResponse>>() {});

            // 같은 ID로 다시 회원가입 시도
            MemberV1Dto.SignupRequest duplicateRequest = new MemberV1Dto.SignupRequest(
                    "user1",
                    "Password2!",
                    "김철수",
                    "test2@test.com",
                    "1998-02-02"
            );

            HttpEntity<MemberV1Dto.SignupRequest> duplicateHttpEntity = new HttpEntity<>(duplicateRequest, headers);

            // act
            ParameterizedTypeReference<ApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Object>> response =
                    testRestTemplate.exchange(SIGNUP_ENDPOINT, HttpMethod.POST, duplicateHttpEntity, responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT),
                    () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL)
            );
        }

        @DisplayName("잘못된 이메일 형식으로 회원가입하면 400 BAD_REQUEST 응답을 받는다.")
        @Test
        void signup_fail_invalid_email() {
            // arrange
            MemberV1Dto.SignupRequest request = new MemberV1Dto.SignupRequest(
                    "user1",
                    "Password1!",
                    "홍길동",
                    "invalid-email",
                    "1997-01-01"
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<MemberV1Dto.SignupRequest> httpEntity = new HttpEntity<>(request, headers);

            // act
            ParameterizedTypeReference<ApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Object>> response =
                    testRestTemplate.exchange(SIGNUP_ENDPOINT, HttpMethod.POST, httpEntity, responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                    () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL)
            );
        }

        @DisplayName("비밀번호 정책을 위반하면 400 BAD_REQUEST 응답을 받는다.")
        @Test
        void signup_fail_invalid_password() {
            // arrange - 특수문자 없는 비밀번호
            MemberV1Dto.SignupRequest request = new MemberV1Dto.SignupRequest(
                    "user1",
                    "Password1",
                    "홍길동",
                    "test@test.com",
                    "1997-01-01"
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<MemberV1Dto.SignupRequest> httpEntity = new HttpEntity<>(request, headers);

            // act
            ParameterizedTypeReference<ApiResponse<Object>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Object>> response =
                    testRestTemplate.exchange(SIGNUP_ENDPOINT, HttpMethod.POST, httpEntity, responseType);

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                    () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL)
            );
        }
    }
}
