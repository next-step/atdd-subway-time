package nextstep.subway.auth.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.utils.AcceptanceTest;
import nextstep.subway.auth.dto.TokenResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static nextstep.subway.member.acceptance.MemberRequestSteps.*;
import static nextstep.subway.member.acceptance.MemberVerificationSteps.회원_정보_조회_됨;
import static nextstep.subway.utils.BaseDocumentSteps.givenDefault;

public class AuthAcceptanceTest extends AcceptanceTest {

    private static final String EMAIL = "email@email.com";
    private static final String PASSWORD = "password";
    private static final Integer AGE = 20;

    @Test
    @DisplayName("Session 로그인 후 내 정보 조회")
    void myInfoWithSession() {
        회원_생성_요청(givenDefault(), EMAIL, PASSWORD, AGE);

        ExtractableResponse<Response> response = 내_회원_정보_조회_요청(EMAIL, PASSWORD);

        회원_정보_조회_됨(response, EMAIL, AGE);
    }

    @Test
    @DisplayName("Bearer Auth")
    void myInfoWithBearerAuth() {
        회원_생성_요청(givenDefault(), EMAIL, PASSWORD, AGE);
        TokenResponse tokenResponse = 로그인_되어_있음(EMAIL, PASSWORD);

        ExtractableResponse<Response> response = 내_회원_정보_조회_요청(givenDefault(), tokenResponse);

        회원_정보_조회_됨(response, EMAIL, AGE);
    }
}